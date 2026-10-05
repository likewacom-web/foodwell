package com.foodwell.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Home-screen widget: today's calories vs target and water vs goal, with "+250 ml water" (works
 * without opening the app; the page picks the queued water up on its next start) and "+ food".
 * The page sends a snapshot of today's numbers whenever it renders.
 */
class TodayWidget : AppWidgetProvider() {
    override fun onUpdate(c: Context, mgr: AppWidgetManager, ids: IntArray) = refresh(c)

    override fun onReceive(c: Context, i: Intent) {
        super.onReceive(c, i)
        if (i.action == ACTION_ADD_WATER) {
            val p = prefs(c)
            val q = runCatching { JSONArray(p.getString("pending", "[]")) }.getOrElse { JSONArray() }
            q.put(JSONObject().put("ts", System.currentTimeMillis()).put("ml", WATER_STEP))
            p.edit().putString("pending", q.toString()).apply()
            refresh(c)
        }
    }

    companion object {
        const val ACTION_ADD_WATER = "com.foodwell.app.ADD_WATER"
        private const val WATER_STEP = 250

        private fun prefs(c: Context) = c.getSharedPreferences("foodwell_widget", Context.MODE_PRIVATE)

        fun saveSnapshot(c: Context, json: String) {
            prefs(c).edit().putString("snap", json).apply()
            refresh(c)
        }

        /** Water added from the widget since the page last looked; returned once, then cleared. */
        fun drain(c: Context): String {
            val p = prefs(c)
            val q = p.getString("pending", "[]") ?: "[]"
            p.edit().putString("pending", "[]").apply()
            return q
        }

        private fun fmt(n: Int) = "%,d".format(n)

        fun refresh(c: Context) {
            val mgr = AppWidgetManager.getInstance(c)
            val ids = mgr.getAppWidgetIds(ComponentName(c, TodayWidget::class.java))
            if (ids.isEmpty()) return
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone).toString()
            val snap = runCatching { JSONObject(prefs(c).getString("snap", "{}")!!) }.getOrElse { JSONObject() }
            val isToday = snap.optString("date") == today
            val pending = runCatching { JSONArray(prefs(c).getString("pending", "[]")) }.getOrElse { JSONArray() }
            var queued = 0
            for (k in 0 until pending.length()) {
                val o = pending.getJSONObject(k)
                if (Instant.ofEpochMilli(o.optLong("ts")).atZone(zone).toLocalDate().toString() == today) queued += o.optInt("ml")
            }
            val kcal = if (isToday) snap.optInt("kcal") else 0
            val target = snap.optInt("target", 0)
            val water = (if (isToday) snap.optInt("water") else 0) + queued
            val waterGoal = snap.optInt("waterGoal", 2000).coerceAtLeast(1)

            val v = RemoteViews(c.packageName, R.layout.widget_today)
            v.setTextViewText(R.id.w_kcal, fmt(kcal))
            if (target > 0) {
                v.setTextViewText(R.id.w_kcal_sub, " / ${fmt(target)} kcal")
                val left = target - kcal
                v.setTextViewText(R.id.w_left, if (left >= 0) "เหลืออีก ${fmt(left)} kcal" else "เกินเป้า ${fmt(-left)} kcal")
                v.setProgressBar(R.id.w_kcal_bar, target, kcal.coerceAtMost(target), false)
            } else {
                v.setTextViewText(R.id.w_kcal_sub, " kcal วันนี้")
                v.setTextViewText(R.id.w_left, "ตั้งเป้าหมายในแอปเพื่อดูแคลอรี่ที่เหลือ")
                v.setProgressBar(R.id.w_kcal_bar, 100, 0, false)
            }
            v.setTextViewText(R.id.w_water, "💧 ${fmt(water)} / ${fmt(waterGoal)} ml")
            v.setProgressBar(R.id.w_water_bar, waterGoal, water.coerceAtMost(waterGoal), false)

            fun open(page: String, code: Int) = PendingIntent.getActivity(
                c, code, Intent(c, MainActivity::class.java).putExtra("page", page)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            v.setOnClickPendingIntent(R.id.widget_root, open("home", 2001))
            v.setOnClickPendingIntent(R.id.w_add_food, open("food", 2002))
            v.setOnClickPendingIntent(
                R.id.w_add_water,
                PendingIntent.getBroadcast(
                    c, 2003, Intent(c, TodayWidget::class.java).setAction(ACTION_ADD_WATER),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            mgr.updateAppWidget(ids, v)
        }
    }
}
