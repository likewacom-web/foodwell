package com.foodwell.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.pm.PackageInfoCompat
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import org.json.JSONObject

/**
 * Google Play in-app updates (window.FoodWellUpdate).
 *
 * Every time the app is opened (and when it comes back after [AUTO_EVERY_MS]) it asks Play on its own
 * whether a newer version exists. If so, Play's small "Update?" dialog appears and the new version
 * downloads in the background while the app stays usable (FLEXIBLE); once downloaded the page shows
 * "restart to finish" and [complete] installs it. A version that has been out for [FORCE_AFTER_DAYS]
 * days uses Play's full-screen update instead (IMMEDIATE). If the user says "No thanks", the same
 * version isn't offered again for [SNOOZE_MS].
 *
 * The "Check for updates" row still calls [check] for an immediate update on demand. Only works for
 * installs from Google Play; elsewhere the page falls back to opening the Play Store listing.
 * Results go to the page as onAppUpdate({state: none|available|downloading|downloaded|installing|
 * started|cancelled|error, code, msg, pct}).
 */
class AppUpdater(private val act: ComponentActivity, private val emit: (String, String) -> Unit) {
    private val mgr = AppUpdateManagerFactory.create(act)
    private val prefs = act.getSharedPreferences("foodwell_update", Context.MODE_PRIVATE)
    private var lastAuto = 0L
    private var offered = 0
    private var lastPct = -1
    /** Last answer sent, for a page that loads after it went out (see [state]). */
    @Volatile private var last = "{}"

    private val launcher = act.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) send("started")
        else {
            // "No thanks" (or back): don't ask again for this version for a while
            if (offered > 0) prefs.edit().putInt("snoozeCode", offered).putLong("snoozeAt", System.currentTimeMillis()).apply()
            send("cancelled")
        }
        offered = 0
    }

    private val listener = InstallStateUpdatedListener { s ->
        when (s.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val total = s.totalBytesToDownload()
                val pct = if (total > 0) (s.bytesDownloaded() * 100 / total).toInt() else 0
                // the page only needs a few steps, not every byte
                if (pct / 10 != lastPct / 10) { lastPct = pct; send("downloading", pct = pct) }
            }
            InstallStatus.DOWNLOADED -> send("downloaded")
            InstallStatus.INSTALLING -> send("installing")
            InstallStatus.FAILED -> send("error", msg = "install failed (${s.installErrorCode()})")
            else -> Unit
        }
    }

    init {
        mgr.registerListener(listener)
    }

    private fun send(state: String, code: Int = 0, msg: String = "", pct: Int = -1) {
        val o = JSONObject().put("state", state).put("code", code).put("msg", msg)
        if (pct >= 0) o.put("pct", pct)
        last = o.toString()
        emit("onAppUpdate", JSONObject.quote(last))
    }

    /** The latest onAppUpdate answer, e.g. "downloaded" sent while the page was still loading. */
    @JavascriptInterface
    fun state(): String = last

    @JavascriptInterface
    fun version(): String = runCatching {
        val p = act.packageManager.getPackageInfo(act.packageName, 0)
        JSONObject().put("name", p.versionName ?: "").put("code", PackageInfoCompat.getLongVersionCode(p)).toString()
    }.getOrDefault("{}")

    /** start = true opens Play's full-screen update right away when an update is there. */
    @JavascriptInterface
    fun check(start: Boolean) = act.runOnUiThread {
        mgr.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) { send("downloaded"); return@addOnSuccessListener }
            if (!hasUpdate(info)) { send("none"); return@addOnSuccessListener }
            send("available", info.availableVersionCode())
            if (start && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) launch(info, AppUpdateType.IMMEDIATE)
        }.addOnFailureListener {
            Log.w("FoodWell", "update check", it)
            send("error", msg = it.message ?: "")
        }
    }

    /** Installs an update that finished downloading in the background (the app restarts). */
    @JavascriptInterface
    fun complete() = act.runOnUiThread { mgr.completeUpdate() }

    @JavascriptInterface
    fun openStore() = act.runOnUiThread {
        val id = act.packageName
        runCatching { act.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id")).setPackage("com.android.vending")) }
            .recoverCatching { act.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$id"))) }
    }

    /** Called from MainActivity.onResume: also runs right after the app is opened. */
    fun onResume() {
        mgr.appUpdateInfo.addOnSuccessListener { info ->
            when {
                // an immediate update that was interrupted (app sent to background) must be resumed
                info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS &&
                    info.installStatus() !in setOf(InstallStatus.PENDING, InstallStatus.DOWNLOADING, InstallStatus.DOWNLOADED) ->
                    launch(info, AppUpdateType.IMMEDIATE)
                // downloaded in the background earlier: remind the page to offer the restart
                info.installStatus() == InstallStatus.DOWNLOADED -> send("downloaded")
                else -> autoCheck(info)
            }
        }
    }

    private fun autoCheck(info: AppUpdateInfo) {
        val now = SystemClock.elapsedRealtime()
        if (lastAuto != 0L && now - lastAuto < AUTO_EVERY_MS) return
        lastAuto = now
        if (!hasUpdate(info)) return
        val code = info.availableVersionCode()
        send("available", code)
        if (info.installStatus() == InstallStatus.DOWNLOADING || info.installStatus() == InstallStatus.PENDING) return
        if (prefs.getInt("snoozeCode", 0) == code && System.currentTimeMillis() - prefs.getLong("snoozeAt", 0) < SNOOZE_MS) return
        val stale = (info.clientVersionStalenessDays() ?: 0) >= FORCE_AFTER_DAYS
        when {
            stale && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> launch(info, AppUpdateType.IMMEDIATE)
            info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> launch(info, AppUpdateType.FLEXIBLE)
            info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> launch(info, AppUpdateType.IMMEDIATE)
        }
    }

    private fun hasUpdate(info: AppUpdateInfo) =
        info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
            info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS

    private fun launch(info: AppUpdateInfo, type: Int) {
        offered = info.availableVersionCode()
        runCatching { mgr.startUpdateFlowForResult(info, launcher, AppUpdateOptions.newBuilder(type).build()) }
            .onFailure { offered = 0; send("error", msg = it.message ?: "") }
    }

    fun onDestroy() = mgr.unregisterListener(listener)

    companion object {
        /** Coming back to the app checks again only after this long (a fresh start always checks). */
        private const val AUTO_EVERY_MS = 3 * 60 * 60 * 1000L
        /** After "No thanks", the same version waits this long before it is offered again. */
        private const val SNOOZE_MS = 24 * 60 * 60 * 1000L
        /** A version out this many days is installed with Play's full-screen update. */
        private const val FORCE_AFTER_DAYS = 7
    }
}
