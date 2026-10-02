package com.foodwell.app

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Minimal Google Drive REST client for one backup file in the app's hidden appDataFolder.
 * Blocking calls — run off the main thread. The access token comes from Google Identity
 * Services' AuthorizationClient (scope drive.appdata), so no client secret lives in the app.
 */
object GoogleDriveBackup {
    const val SCOPE_DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata"
    private const val FILE_NAME = "foodwell-backup.json"
    private const val API = "https://www.googleapis.com"

    fun userInfo(token: String): JSONObject =
        JSONObject(http("GET", "$API/oauth2/v3/userinfo", token))

    /** Returns {id, modifiedTime, size} of the backup, or null if none exists yet. */
    fun find(token: String): JSONObject? {
        val q = URLEncoder.encode("name='$FILE_NAME'", "UTF-8")
        val r = JSONObject(
            http(
                "GET",
                "$API/drive/v3/files?spaces=appDataFolder&q=$q&orderBy=modifiedTime%20desc&fields=files(id,modifiedTime,size)",
                token
            )
        )
        return r.optJSONArray("files")?.optJSONObject(0)
    }

    /** Creates or overwrites the backup; returns its {id, modifiedTime, size}. */
    fun upload(token: String, content: String): JSONObject {
        val bytes = content.toByteArray()
        val existing = find(token)
        val out = if (existing != null) {
            // HttpURLConnection has no PATCH; Google APIs accept the method override header.
            http(
                "POST",
                "$API/upload/drive/v3/files/${existing.getString("id")}?uploadType=media&fields=id,modifiedTime,size",
                token, bytes, "application/json", mapOf("X-HTTP-Method-Override" to "PATCH")
            )
        } else {
            val boundary = "foodwell${System.currentTimeMillis()}"
            val meta = JSONObject().put("name", FILE_NAME).put("parents", JSONArray().put("appDataFolder"))
            val body = ("--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n$meta\r\n" +
                "--$boundary\r\nContent-Type: application/json\r\n\r\n").toByteArray() +
                bytes + "\r\n--$boundary--\r\n".toByteArray()
            http(
                "POST", "$API/upload/drive/v3/files?uploadType=multipart&fields=id,modifiedTime,size",
                token, body, "multipart/related; boundary=$boundary"
            )
        }
        return JSONObject(out)
    }

    /** Returns the backup's content, or null if there is no backup. */
    fun download(token: String): String? {
        val f = find(token) ?: return null
        return http("GET", "$API/drive/v3/files/${f.getString("id")}?alt=media", token)
    }

    fun revoke(token: String) {
        runCatching {
            http("POST", "https://oauth2.googleapis.com/revoke?token=${URLEncoder.encode(token, "UTF-8")}", null,
                ByteArray(0), "application/x-www-form-urlencoded")
        }
    }

    class HttpError(val code: Int, body: String) : IOException("HTTP $code: ${body.take(300)}") {
        val apiNotEnabled = code == 403 && (body.contains("accessNotConfigured") || body.contains("SERVICE_DISABLED"))
    }

    private fun http(
        method: String, url: String, token: String?,
        body: ByteArray? = null, contentType: String? = null, headers: Map<String, String> = emptyMap()
    ): String {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = method
            c.connectTimeout = 20_000
            c.readTimeout = 60_000
            token?.let { c.setRequestProperty("Authorization", "Bearer $it") }
            headers.forEach { (k, v) -> c.setRequestProperty(k, v) }
            if (body != null) {
                c.doOutput = true
                contentType?.let { c.setRequestProperty("Content-Type", it) }
                c.setFixedLengthStreamingMode(body.size)
                c.outputStream.use { it.write(body) }
            }
            val code = c.responseCode
            val stream = if (code in 200..299) c.inputStream else c.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            if (code !in 200..299) throw HttpError(code, text)
            return text
        } finally {
            c.disconnect()
        }
    }
}
