package com.foodwell.app

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Huawei Health (watch) data through the FoodWell Cloudflare Worker's /huawei/ routes.
 * The Worker holds the Huawei client secret and talks to Health Kit; the app only keeps the
 * user's refresh token and the Worker URL (the same "AI Endpoint" the page already stores).
 */
class HuaweiHealth(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("huawei_health", Context.MODE_PRIVATE)

    val connected get() = prefs.getString(KEY_RT, null) != null

    /** Remembers the Worker from an endpoint like https://x.workers.dev/?k=TOKEN; returns its login URL. */
    fun loginUrl(endpoint: String): String? {
        val u = runCatching { Uri.parse(endpoint.trim()) }.getOrNull() ?: return null
        if (u.scheme != "https" || u.host.isNullOrBlank()) return null
        prefs.edit().putString(KEY_ORIGIN, "https://${u.host}").putString(KEY_K, u.getQueryParameter("k") ?: "").apply()
        return "https://${u.host}/huawei/login?k=${Uri.encode(u.getQueryParameter("k") ?: "")}"
    }

    /** Handles foodwell://huawei?refresh_token=… from the Worker's callback page. */
    fun handleRedirect(uri: Uri?): Boolean {
        if (uri?.scheme != "foodwell" || uri.host != "huawei") return false
        val rt = uri.getQueryParameter("refresh_token") ?: return false
        prefs.edit().putString(KEY_RT, rt).apply()
        return true
    }

    fun disconnect() = prefs.edit().remove(KEY_RT).apply()

    /** Blocking — call off the main thread. Returns the page's health data shape (steps/activeKcal/heart…). */
    fun fetchToday(): JSONObject {
        val origin = prefs.getString(KEY_ORIGIN, null) ?: throw IllegalStateException("ยังไม่ได้ตั้งค่า Endpoint")
        val rt = prefs.getString(KEY_RT, null) ?: throw IllegalStateException("ยังไม่ได้เชื่อมต่อ Huawei")
        val now = ZonedDateTime.now()
        val body = JSONObject()
            .put("refresh_token", rt)
            .put("day", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
            .put("timeZone", now.format(DateTimeFormatter.ofPattern("xx")))
        val c = URL("$origin/huawei/today?k=${Uri.encode(prefs.getString(KEY_K, "") ?: "")}").openConnection() as HttpURLConnection
        try {
            c.requestMethod = "POST"
            c.connectTimeout = 20_000
            c.readTimeout = 60_000
            c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json")
            c.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = c.responseCode
            val text = (if (code in 200..299) c.inputStream else c.errorStream)?.bufferedReader()?.use { it.readText() } ?: ""
            val out = runCatching { JSONObject(text) }.getOrElse { JSONObject().put("error", "HTTP $code") }
            if (code !in 200..299) throw IllegalStateException(out.optString("error", "HTTP $code"))
            out.optString("refresh_token").takeIf { it.isNotEmpty() }?.let { prefs.edit().putString(KEY_RT, it).apply() }
            out.remove("refresh_token")
            return out
        } finally {
            c.disconnect()
        }
    }

    companion object {
        private const val KEY_RT = "refresh_token"
        private const val KEY_ORIGIN = "origin"
        private const val KEY_K = "k"
        /** Values Huawei provides; when connected they replace Health Connect's phone-sensor numbers. */
        val PROVIDED = listOf("steps", "activeKcal", "heart")
    }
}
