package com.foodwell.app

import android.app.backup.BackupManager
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * window.FoodWellBackup — automatic daily snapshots of the app's data in files/backups/
 * (one file per day, newest 7 kept). Only that folder is included in Android's backup to the
 * phone's Google account (res/xml/backup_rules.xml, data_extraction_rules.xml), so the snapshots
 * come back after a reinstall or on a new phone and the page offers to restore them.
 */
class LocalBackup(private val context: Context) {

    private val dir get() = File(context.filesDir, "backups").apply { mkdirs() }
    private val dayRe = Regex("\\d{4}-\\d{2}-\\d{2}")

    /** Writes today's snapshot (replacing an earlier one from the same day). [meta] is a small JSON summary. */
    @JavascriptInterface
    fun save(day: String, json: String, meta: String): Boolean = try {
        require(dayRe.matches(day)) { "bad day" }
        val tmp = File(dir, "auto-$day.json.tmp")
        tmp.writeText(json)
        tmp.renameTo(File(dir, "auto-$day.json")) || error("rename failed")
        File(dir, "auto-$day.meta").writeText(meta)
        files().drop(KEEP).forEach { d -> File(dir, "auto-$d.json").delete(); File(dir, "auto-$d.meta").delete() }
        BackupManager(context).dataChanged()
        true
    } catch (e: Exception) {
        Log.w(TAG, "snapshot failed", e)
        false
    }

    /** Newest first: [{day, size, at, meta}] */
    @JavascriptInterface
    fun list(): String {
        val out = JSONArray()
        files().forEach { d ->
            val f = File(dir, "auto-$d.json")
            val meta = runCatching { JSONObject(File(dir, "auto-$d.meta").readText()) }.getOrNull() ?: JSONObject()
            out.put(JSONObject().put("day", d).put("size", f.length()).put("at", f.lastModified()).put("meta", meta))
        }
        return out.toString()
    }

    /** "Clear data on this device" removes the snapshots too. */
    @JavascriptInterface
    fun clear(): Boolean {
        dir.listFiles()?.forEach { it.delete() }
        BackupManager(context).dataChanged()
        return true
    }

    @JavascriptInterface
    fun read(day: String): String =
        if (!dayRe.matches(day)) "" else runCatching { File(dir, "auto-$day.json").readText() }.getOrDefault("")

    private fun files(): List<String> =
        (dir.list() ?: emptyArray()).mapNotNull { Regex("^auto-(\\d{4}-\\d{2}-\\d{2})\\.json$").find(it)?.groupValues?.get(1) }
            .sortedDescending()

    companion object {
        private const val TAG = "FoodWellBackup"
        private const val KEEP = 7
    }
}
