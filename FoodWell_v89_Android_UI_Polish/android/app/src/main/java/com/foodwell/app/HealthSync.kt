package com.foodwell.app

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

/**
 * Reads steps, active calories and workouts that watches and health apps (Samsung Health, Google Fit,
 * Garmin, Fitbit, Mi Fitness …) write to Android's Health Connect, and weight / body fat from smart scales
 * (their apps — Zepp Life, Huawei Health, Withings, Eufy, Renpho … — save each weigh-in there). Read-only; FoodWell never writes there.
 * Answers the page through onHealth(kind, json).
 */
class HealthSync(private val activity: ComponentActivity, private val emit: (fn: String, arg: String) -> Unit) {
    val perms = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
    )
    private val launcher: ActivityResultLauncher<Set<String>> =
        activity.registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
            emit("onHealth", JSONObject.quote("permission") + "," + JSONObject.quote(JSONObject().put("granted", JSONArray(granted.map { short(it) })).toString()))
        }

    /** "ok" | "update" (Health Connect needs installing/updating) | "none" (not supported on this phone) */
    fun sdk(): String = when (HealthConnectClient.getSdkStatus(activity)) {
        HealthConnectClient.SDK_AVAILABLE -> "ok"
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> "update"
        else -> "none"
    }

    private val client get() = HealthConnectClient.getOrCreate(activity)
    private fun short(p: String) = p.substringAfterLast('.')

    suspend fun granted(): Set<String> = if (sdk() == "ok") client.permissionController.getGrantedPermissions() else emptySet()

    fun connect() {
        when (sdk()) {
            "ok" -> launcher.launch(perms)
            "update" -> runCatching {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PROVIDER&url=healthconnect%3A%2F%2Fonboarding"))
                    .setPackage("com.android.vending"))
            }.onFailure {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$PROVIDER")))
            }
            else -> emit("onHealth", JSONObject.quote("error") + "," + JSONObject.quote("none"))
        }
    }

    suspend fun disconnect() { if (sdk() == "ok") client.permissionController.revokeAllPermissions() }

    /** Per-day steps and active kcal for the last [days] days, plus workouts, as JSON. */
    suspend fun read(days: Int): String {
        val g = granted()
        val zone = ZoneId.systemDefault()
        val from = LocalDate.now().minusDays((days - 1).toLong()).atStartOfDay()
        val to = LocalDate.now().plusDays(1).atStartOfDay()
        val out = JSONObject()
        val perDay = JSONObject()
        // Each data type is read on its own: if Health Connect refuses one (a permission switched off in its
        // settings, or not yet granted on this phone) the others still sync; refused types are listed in "skipped".
        val skipped = JSONArray()
        suspend fun part(name: String, perm: String, block: suspend () -> Unit) {
            if (perm !in g) return
            try { block() } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; Log.w("FoodWell", "health $name", e); skipped.put(name) }
        }
        fun day(d: String): JSONObject = perDay.optJSONObject(d) ?: JSONObject().also { perDay.put(d, it) }
        part("steps", HealthPermission.getReadPermission(StepsRecord::class)) {
            for (grp in client.aggregateGroupByPeriod(AggregateGroupByPeriodRequest(setOf(StepsRecord.COUNT_TOTAL), TimeRangeFilter.between(from, to), Period.ofDays(1))))
                grp.result[StepsRecord.COUNT_TOTAL]?.let { day(grp.startTime.toLocalDate().toString()).put("steps", it) }
        }
        part("kcal", HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)) {
            for (grp in client.aggregateGroupByPeriod(AggregateGroupByPeriodRequest(setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL), TimeRangeFilter.between(from, to), Period.ofDays(1))))
                grp.result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.let { day(grp.startTime.toLocalDate().toString()).put("kcal", Math.round(it.inKilocalories)) }
        }
        out.put("days", perDay)
        val sessions = JSONArray()
        part("exercise", HealthPermission.getReadPermission(ExerciseSessionRecord::class)) {
            val res = client.readRecords(
                ReadRecordsRequest(ExerciseSessionRecord::class, TimeRangeFilter.between(from.atZone(zone).toInstant(), Instant.now()))
            )
            for (r in res.records) {
                sessions.put(JSONObject()
                    .put("id", r.metadata.id)
                    .put("type", TYPES[r.exerciseType] ?: "other")
                    .put("title", r.title ?: "")
                    .put("start", r.startTime.toEpochMilli())
                    .put("end", r.endTime.toEpochMilli())
                    .put("src", r.metadata.dataOrigin.packageName))
            }
        }
        out.put("sessions", sessions)
        // smart-scale weigh-ins: [{id, t, kg, src}] and body fat [{t, pct}]
        val since = from.atZone(zone).toInstant()
        val weights = JSONArray()
        part("weight", HealthPermission.getReadPermission(WeightRecord::class)) {
            for (r in client.readRecords(ReadRecordsRequest(WeightRecord::class, TimeRangeFilter.between(since, Instant.now()))).records)
                weights.put(JSONObject().put("id", r.metadata.id).put("t", r.time.toEpochMilli())
                    .put("kg", Math.round(r.weight.inKilograms * 10) / 10.0).put("src", r.metadata.dataOrigin.packageName))
        }
        out.put("weights", weights)
        val fat = JSONArray()
        part("bodyFat", HealthPermission.getReadPermission(BodyFatRecord::class)) {
            for (r in client.readRecords(ReadRecordsRequest(BodyFatRecord::class, TimeRangeFilter.between(since, Instant.now()))).records)
                fat.put(JSONObject().put("t", r.time.toEpochMilli()).put("pct", Math.round(r.percentage.value * 10) / 10.0))
        }
        out.put("bodyFat", fat)
        out.put("granted", JSONArray(g.map { short(it) }))
        out.put("skipped", skipped)
        return out.toString()
    }

    companion object {
        const val PROVIDER = "com.google.android.apps.healthdata"
        private val TYPES = mapOf(
            ExerciseSessionRecord.EXERCISE_TYPE_WALKING to "walk",
            ExerciseSessionRecord.EXERCISE_TYPE_HIKING to "hike",
            ExerciseSessionRecord.EXERCISE_TYPE_RUNNING to "run",
            ExerciseSessionRecord.EXERCISE_TYPE_RUNNING_TREADMILL to "run",
            ExerciseSessionRecord.EXERCISE_TYPE_BIKING to "bike",
            ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY to "bike",
            ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL to "swim",
            ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER to "swim",
            ExerciseSessionRecord.EXERCISE_TYPE_YOGA to "yoga",
            ExerciseSessionRecord.EXERCISE_TYPE_PILATES to "yoga",
            ExerciseSessionRecord.EXERCISE_TYPE_STRETCHING to "stretch",
            ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING to "strength",
            ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING to "strength",
            ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING to "hiit",
            ExerciseSessionRecord.EXERCISE_TYPE_ELLIPTICAL to "elliptical",
            ExerciseSessionRecord.EXERCISE_TYPE_ROWING_MACHINE to "row",
            ExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING to "stairs",
            ExerciseSessionRecord.EXERCISE_TYPE_DANCING to "dance",
            ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON to "badminton",
            ExerciseSessionRecord.EXERCISE_TYPE_TENNIS to "tennis",
            ExerciseSessionRecord.EXERCISE_TYPE_SOCCER to "football",
            ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL to "basketball",
        )
    }
}
