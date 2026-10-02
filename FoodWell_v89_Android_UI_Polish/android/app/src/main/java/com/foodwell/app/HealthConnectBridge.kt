package com.foodwell.app

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
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
    val permissions = setOf(stepsPerm, heartPerm, sleepPerm, exercisePerm)

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

        if (stepsPerm in granted) {
            val r = client.aggregate(AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), today))
            out.put("steps", r[StepsRecord.COUNT_TOTAL] ?: 0L)
        }
        if (heartPerm in granted) {
            val r = client.readRecords(
                ReadRecordsRequest(HeartRateRecord::class, lastDay, ascendingOrder = false, pageSize = 1)
            )
            r.records.firstOrNull()?.samples?.maxByOrNull { it.time }?.let { out.put("heart", it.beatsPerMinute) }
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
        }
        return out
    }

    companion object {
        const val PROVIDER = "com.google.android.apps.healthdata"
    }
}
