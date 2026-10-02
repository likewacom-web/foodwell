package com.foodwell.app

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import org.json.JSONObject
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Keeps Health Connect access behind one native boundary. Reads only what the user granted. */
class HealthConnectBridge(context: Context) {
    private val ctx = context.applicationContext

    private val stepsPerm = HealthPermission.getReadPermission(StepsRecord::class)
    private val heartPerm = HealthPermission.getReadPermission(HeartRateRecord::class)
    private val sleepPerm = HealthPermission.getReadPermission(SleepSessionRecord::class)
    private val exercisePerm = HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    private val activeKcalPerm = HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
    private val totalKcalPerm = HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    val permissions = setOf(stepsPerm, heartPerm, sleepPerm, exercisePerm, activeKcalPerm, totalKcalPerm)

    // getOrCreate() throws when Health Connect isn't available, so only touch it after status() == SDK_AVAILABLE.
    private val client by lazy { HealthConnectClient.getOrCreate(ctx) }

    fun status(): Int = try {
        HealthConnectClient.getSdkStatus(ctx, PROVIDER)
    } catch (e: RuntimeException) {
        HealthConnectClient.SDK_UNAVAILABLE
    }

    fun isAvailable() = status() == HealthConnectClient.SDK_AVAILABLE

    suspend fun grantedPermissions(): Set<String> = client.permissionController.getGrantedPermissions()

    /** Today's totals (since local midnight), latest heart rate and last 24h of sleep, in the page's data shape. */
    suspend fun readToday(granted: Set<String>): JSONObject {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = TimeRangeFilter.between(LocalDate.now(zone).atStartOfDay(zone).toInstant(), now)
        val lastDay = TimeRangeFilter.between(now.minus(Duration.ofHours(24)), now)
        val out = JSONObject().put("syncedAt", now.toString()).put("deviceName", "Health Connect")
        // Which apps contributed each value (Health Connect de-duplicates by the user's app priority),
        // so a mismatch with the watch app can be traced to the source.
        val sources = JSONObject()
        fun note(metric: String, r: AggregationResult) {
            val apps = r.dataOrigins.map { it.packageName }.filter { it.isNotBlank() }
            if (apps.isNotEmpty()) sources.put(metric, org.json.JSONArray(apps))
        }

        if (stepsPerm in granted) {
            val r = client.aggregate(AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), today))
            out.put("steps", r[StepsRecord.COUNT_TOTAL] ?: 0L)
            note("steps", r)
        }
        if (heartPerm in granted) {
            val r = client.readRecords(
                ReadRecordsRequest(HeartRateRecord::class, lastDay, ascendingOrder = false, pageSize = 1)
            )
            r.records.firstOrNull()?.let { rec ->
                rec.samples.maxByOrNull { it.time }?.let { out.put("heart", it.beatsPerMinute) }
                sources.put("heart", org.json.JSONArray(listOf(rec.metadata.dataOrigin.packageName)))
            }
        }
        if (sleepPerm in granted) {
            val r = client.aggregate(AggregateRequest(setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL), lastDay))
            r[SleepSessionRecord.SLEEP_DURATION_TOTAL]?.takeIf { !it.isZero }?.let {
                out.put("sleep", "${it.toHours()} ชม. ${it.toMinutes() % 60} นาที")
            }
        }
        if (exercisePerm in granted) {
            val r = client.aggregate(AggregateRequest(setOf(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL), today))
            out.put("move", "${r[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]?.toMinutes() ?: 0} นาที")
            note("move", r)
        }
        if (activeKcalPerm in granted) {
            val r = client.aggregate(AggregateRequest(setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL), today))
            r[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.let { out.put("activeKcal", Math.round(it.inKilocalories)) }
            note("activeKcal", r)
        }
        if (totalKcalPerm in granted) {
            val r = client.aggregate(AggregateRequest(setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL), today))
            r[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.let { out.put("totalKcal", Math.round(it.inKilocalories)) }
            note("totalKcal", r)
        }
        out.put("sources", sources)
        return out
    }

    companion object {
        const val PROVIDER = "com.google.android.apps.healthdata"
    }
}
