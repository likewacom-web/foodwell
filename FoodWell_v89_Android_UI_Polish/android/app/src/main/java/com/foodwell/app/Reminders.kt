package com.foodwell.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Daily reminders (meals, water, weekly weigh-in) posted as real Android notifications, so they
 * work with the app closed. The page sends the settings (configure) and what has been logged today
 * (report); a reminder whose job is already done today is skipped.
 */
object Reminders {
    const val CHANNEL = "reminders"
    private const val PREFS = "foodwell_reminders"

    data class Slot(val id: Int, val kind: String, val hour: Int, val minute: Int, val weekday: Int = 0)

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun configure(c: Context, json: String) {
        prefs(c).edit().putString("config", json).apply()
        scheduleAll(c)
    }

    fun report(c: Context, json: String) = prefs(c).edit().putString("status", json).apply()

    fun canNotify(c: Context) = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(c, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun hm(s: String?, def: String): Pair<Int, Int> {
        val p = (s ?: def).split(":")
        return Pair(p.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8, p.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0)
    }

    fun slots(c: Context): List<Slot> {
        val cfg = runCatching { JSONObject(prefs(c).getString("config", "{}")!!) }.getOrElse { JSONObject() }
        val out = mutableListOf<Slot>()
        if (cfg.optBoolean("meals")) {
            listOf(Triple(1, "breakfast", "08:00"), Triple(2, "lunch", "12:00"), Triple(3, "dinner", "18:30")).forEach { (id, k, d) ->
                val (h, m) = hm(cfg.optString(k, d), d); out += Slot(id, k, h, m)
            }
        }
        if (cfg.optBoolean("water")) {
            val from = cfg.optInt("waterFrom", 9).coerceIn(5, 22); val to = cfg.optInt("waterTo", 21).coerceIn(from, 23)
            val every = cfg.optInt("waterEvery", 2).coerceIn(1, 6)
            for (h in from..to step every) out += Slot(100 + h, "water", h, 0)
        }
        if (cfg.optBoolean("weigh")) {
            val (h, m) = hm(cfg.optString("weighTime", "07:30"), "07:30")
            out += Slot(50, "weigh", h, m, cfg.optInt("weighDay", 1).coerceIn(1, 7))
        }
        return out
    }

    private fun pending(c: Context, s: Slot) = PendingIntent.getBroadcast(
        c, s.id,
        Intent(c, ReminderReceiver::class.java).putExtra("id", s.id).putExtra("kind", s.kind)
            .putExtra("h", s.hour).putExtra("m", s.minute).putExtra("wd", s.weekday),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun scheduleAll(c: Context) {
        val am = c.getSystemService(AlarmManager::class.java)
        val old = runCatching { JSONArray(prefs(c).getString("ids", "[]")) }.getOrElse { JSONArray() }
        for (i in 0 until old.length()) {
            val id = old.getInt(i)
            am.cancel(PendingIntent.getBroadcast(c, id, Intent(c, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        }
        val list = slots(c)
        list.forEach { scheduleNext(c, it) }
        prefs(c).edit().putString("ids", JSONArray(list.map { it.id }).toString()).apply()
    }

    fun scheduleNext(c: Context, s: Slot) {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var t = now.withHour(s.hour).withMinute(s.minute).withSecond(0).withNano(0)
        if (s.weekday in 1..7) t = t.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(s.weekday)))
        if (!t.isAfter(now)) t = if (s.weekday in 1..7) t.plusWeeks(1) else t.plusDays(1)
        val at = t.atZone(zone).toInstant().toEpochMilli()
        // Inexact window: no exact-alarm permission needed, and Android can batch wake-ups.
        c.getSystemService(AlarmManager::class.java).setWindow(AlarmManager.RTC_WAKEUP, at, 10 * 60 * 1000L, pending(c, s))
    }

    /** Title/text for a reminder, or null when today's status says it's already done. */
    fun message(c: Context, kind: String): Triple<String, String, String>? {
        val st = runCatching { JSONObject(prefs(c).getString("status", "{}")!!) }.getOrElse { JSONObject() }
        val today = LocalDate.now().toString()
        val isToday = st.optString("date") == today
        val meals = st.optJSONArray("meals")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList()
        fun logged(meal: String) = isToday && meal in meals
        return when (kind) {
            "breakfast" -> if (logged("เช้า")) null else Triple("🌅 ได้เวลาอาหารเช้า", "กินอะไรไปบ้าง? แตะเพื่อบันทึกมื้อเช้า", "food")
            "lunch" -> if (logged("กลางวัน")) null else Triple("☀️ มื้อกลางวันแล้ว", "อย่าลืมบันทึกมื้อกลางวันใน FoodWell นะ", "food")
            "dinner" -> if (logged("เย็น")) null else Triple("🌙 บันทึกมื้อเย็น", "บันทึกมื้อเย็น แล้วดูสรุปพลังงานของวันนี้", "food")
            "water" -> {
                val ml = if (isToday) st.optInt("water") else 0
                val goal = st.optInt("waterGoal", 2000)
                if (ml >= goal) null else Triple("💧 ดื่มน้ำสักแก้ว", "วันนี้ดื่มไป ${"%,d".format(ml)} / ${"%,d".format(goal)} ml", "water")
            }
            "weigh" -> {
                val last = st.optString("lastWeigh")
                val recent = runCatching { !LocalDate.parse(last).isBefore(LocalDate.now().minusDays(5)) }.getOrDefault(false)
                if (recent) null else Triple("⚖️ ชั่งน้ำหนักประจำสัปดาห์", "บันทึกน้ำหนักเพื่อดูความคืบหน้าของแผน", "weightPlan")
            }
            else -> null
        }
    }

    fun ensureChannel(c: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            c.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, "การแจ้งเตือน FoodWell", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "เตือนบันทึกอาหาร ดื่มน้ำ และชั่งน้ำหนัก" }
            )
        }
    }

    fun post(c: Context, id: Int, title: String, text: String, page: String) {
        if (!canNotify(c)) return
        ensureChannel(c)
        val open = PendingIntent.getActivity(
            c, 1000 + id,
            Intent(c, MainActivity::class.java).putExtra("page", page)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_foodwell)
            .setColor(0xFFE887AA.toInt())
            .setContentTitle(title).setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open).setAutoCancel(true)
            .build()
        try { NotificationManagerCompat.from(c).notify(id, n) } catch (e: SecurityException) { /* permission revoked */ }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val s = Reminders.Slot(i.getIntExtra("id", 0), i.getStringExtra("kind") ?: return, i.getIntExtra("h", 8), i.getIntExtra("m", 0), i.getIntExtra("wd", 0))
        Reminders.message(c, s.kind)?.let { (title, text, page) -> Reminders.post(c, s.id, title, text, page) }
        // Only re-arm slots that are still configured (settings may have changed since).
        if (Reminders.slots(c).any { it.id == s.id }) Reminders.scheduleNext(c, Reminders.slots(c).first { it.id == s.id })
    }
}

/** Alarms are cleared on reboot and app update; set them again. */
class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) = Reminders.scheduleAll(c)
}
