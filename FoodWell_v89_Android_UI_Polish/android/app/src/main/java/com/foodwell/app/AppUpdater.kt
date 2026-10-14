package com.foodwell.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.pm.PackageInfoCompat
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import org.json.JSONObject

/**
 * "Check for updates" (window.FoodWellUpdate) using Google Play in-app updates: when Play has a newer
 * version, Play's own full-screen update screen opens. Only works for installs from Google Play; for
 * anything else the page falls back to opening the Play Store listing.
 * Results go to the page as onAppUpdate({state: none|available|started|cancelled|error, code, msg}).
 */
class AppUpdater(private val act: ComponentActivity, private val emit: (String, String) -> Unit) {
    private val mgr = AppUpdateManagerFactory.create(act)
    private val launcher = act.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { r ->
        send(if (r.resultCode == Activity.RESULT_OK) "started" else "cancelled")
    }

    private fun send(state: String, code: Int = 0, msg: String = "") =
        emit("onAppUpdate", JSONObject.quote(JSONObject().put("state", state).put("code", code).put("msg", msg).toString()))

    @JavascriptInterface
    fun version(): String = runCatching {
        val p = act.packageManager.getPackageInfo(act.packageName, 0)
        JSONObject().put("name", p.versionName ?: "").put("code", PackageInfoCompat.getLongVersionCode(p)).toString()
    }.getOrDefault("{}")

    /** start = true opens Play's update screen right away when an update is there. */
    @JavascriptInterface
    fun check(start: Boolean) = act.runOnUiThread {
        mgr.appUpdateInfo.addOnSuccessListener { info ->
            val avail = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            if (!avail) { send("none"); return@addOnSuccessListener }
            send("available", info.availableVersionCode())
            if (start && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                runCatching { mgr.startUpdateFlowForResult(info, launcher, AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()) }
                    .onFailure { send("error", msg = it.message ?: "") }
            }
        }.addOnFailureListener {
            Log.w("FoodWell", "update check", it)
            send("error", msg = it.message ?: "")
        }
    }

    @JavascriptInterface
    fun openStore() = act.runOnUiThread {
        val id = act.packageName
        runCatching { act.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id")).setPackage("com.android.vending")) }
            .recoverCatching { act.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$id"))) }
    }

    /** An immediate update that was interrupted (app sent to background) must be resumed. */
    fun onResume() {
        mgr.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                runCatching { mgr.startUpdateFlowForResult(info, launcher, AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()) }
            }
        }
    }
}
