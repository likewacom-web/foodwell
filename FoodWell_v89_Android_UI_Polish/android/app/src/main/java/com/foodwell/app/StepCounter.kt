package com.foodwell.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * Daily steps from the phone's own step-counter sensor (no other app needed).
 *
 * The sensor reports steps since the last reboot. Each reading adds the difference from the previous
 * reading to today's total; a background job reads it every ~15 minutes so steps land on the right day
 * even when the app stays closed. Totals per day are kept in SharedPreferences ("days": {"yyyy-MM-dd": n}).
 */
object StepCounter {
    private const val PREFS = "foodwell_steps"
    private const val WORK = "foodwell_steps_sample"
    private const val KEEP_DAYS = 120L

    fun hasSensor(c: Context) = sensor(c) != null
    private fun sensor(c: Context): Sensor? =
        c.getSystemService(SensorManager::class.java)?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    fun permitted(c: Context) = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(c, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED

    fun enabled(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("on", false)

    fun setEnabled(c: Context, on: Boolean) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("on", on).apply()
        val wm = WorkManager.getInstance(c)
        if (on) wm.enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<StepWorker>(15, TimeUnit.MINUTES).build())
        else wm.cancelUniqueWork(WORK)
    }

    /** Adds a new sensor total (steps since boot) to today's count. */
    @Synchronized
    fun record(c: Context, total: Float) {
        val p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val days = JSONObject(p.getString("days", "{}") ?: "{}")
        val today = LocalDate.now().toString()
        val last = p.getFloat("last", -1f)
        // after a reboot the sensor starts again from 0
        val delta = when {
            last < 0f -> 0f
            total >= last -> total - last
            else -> total
        }.coerceAtMost(60_000f)
        days.put(today, days.optInt(today, 0) + delta.toInt())
        val cutoff = LocalDate.now().minusDays(KEEP_DAYS).toString()
        days.keys().asSequence().toList().filter { it < cutoff }.forEach { days.remove(it) }
        p.edit().putFloat("last", total).putString("days", days.toString()).apply()
    }

    fun days(c: Context): JSONObject =
        JSONObject(c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("days", "{}") ?: "{}")

    /** One reading from the sensor (null if unavailable or no event within a few seconds). */
    suspend fun readOnce(c: Context): Float? {
        val s = sensor(c) ?: return null
        if (!permitted(c)) return null
        val sm = c.getSystemService(SensorManager::class.java) ?: return null
        return withTimeoutOrNull(5_000) {
            suspendCancellableCoroutine { cont ->
                val l = object : SensorEventListener {
                    override fun onSensorChanged(e: SensorEvent) {
                        sm.unregisterListener(this)
                        if (cont.isActive) cont.resume(e.values[0])
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                sm.registerListener(l, s, SensorManager.SENSOR_DELAY_NORMAL)
                cont.invokeOnCancellation { sm.unregisterListener(l) }
            }
        }
    }

    /** Live updates while the app is open. */
    class Live(private val c: Context, private val onChange: () -> Unit) : SensorEventListener {
        private val sm = c.getSystemService(SensorManager::class.java)
        fun start() {
            val s = sensor(c) ?: return
            if (enabled(c) && permitted(c)) sm?.registerListener(this, s, SensorManager.SENSOR_DELAY_UI)
        }
        fun stop() { sm?.unregisterListener(this) }
        private var lastEmit = 0L
        override fun onSensorChanged(e: SensorEvent) {
            record(c, e.values[0])
            // one update to the page every few seconds is plenty while walking
            val now = System.currentTimeMillis()
            if (now - lastEmit > 3_000) { lastEmit = now; onChange() }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
}

class StepWorker(c: Context, params: WorkerParameters) : CoroutineWorker(c, params) {
    override suspend fun doWork(): Result {
        if (!StepCounter.enabled(applicationContext)) return Result.success()
        StepCounter.readOnce(applicationContext)?.let { StepCounter.record(applicationContext, it) }
        return Result.success()
    }
}
