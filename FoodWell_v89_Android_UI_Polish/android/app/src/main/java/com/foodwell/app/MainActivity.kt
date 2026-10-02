package com.foodwell.app

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.WindowInsetsController
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var web: WebView
    private lateinit var health: HealthConnectBridge
    private lateinit var permissionLauncher: ActivityResultLauncher<Set<String>>
    private lateinit var fileChooserLauncher: ActivityResultLauncher<Intent>
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var cloud: CloudSync

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dark = Color.rgb(16, 17, 22)
        @Suppress("DEPRECATION")
        window.statusBarColor = dark
        @Suppress("DEPRECATION")
        window.navigationBarColor = dark

        health = HealthConnectBridge(this)
        permissionLauncher = registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { granted -> onPermissionResult(granted) }
        fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
            filePathCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(r.resultCode, r.data))
            filePathCallback = null
        }
        cloud = CloudSync(this) { fn, arg -> callJs(fn, arg) }

        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.textZoom = 100
            overScrollMode = WebView.OVER_SCROLL_NEVER
            setBackgroundColor(dark)
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) = syncHealth(userInitiated = false)
            }
            // Without a WebChromeClient file chooser, <input type=file> (food photos, backup import) does nothing.
            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(
                    view: WebView, callback: ValueCallback<Array<Uri>>, params: FileChooserParams
                ): Boolean {
                    filePathCallback?.onReceiveValue(null)
                    filePathCallback = callback
                    return try {
                        fileChooserLauncher.launch(Intent.createChooser(params.createIntent(), null))
                        true
                    } catch (e: ActivityNotFoundException) {
                        filePathCallback = null
                        false
                    }
                }
            }
            // The page calls window.AndroidHealthConnect.connect()/sync()/openSettings().
            addJavascriptInterface(AndroidHealthConnectApi(), "AndroidHealthConnect")
            addJavascriptInterface(NativeHealthApi(), "FoodWellHealth")
            addJavascriptInterface(FilesApi(), "FoodWellFiles")
            addJavascriptInterface(CloudApi(), "FoodWellCloud")
            loadUrl("file:///android_asset/index.html")
        }

        // Android 15+ forces edge-to-edge for targetSdk 35, so the page was drawn under the
        // status bar and navigation bar. Opt in on every version and pad the WebView by the
        // system bar / cutout / keyboard insets instead.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val root = FrameLayout(this).apply {
            setBackgroundColor(dark)
            addView(web, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            WindowInsetsCompat.CONSUMED
        }
        setContentView(root)
        // getInsetsController() needs the DecorView, which exists only after setContentView().
        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        }

        onBackPressedDispatcher.addCallback(this) {
            if (web.canGoBack()) {
                web.goBack()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun connectHealth() {
        when (health.status()) {
            HealthConnectClient.SDK_AVAILABLE -> lifecycleScope.launch {
                val granted = runCatching { health.grantedPermissions() }.getOrDefault(emptySet())
                if (granted.containsAll(health.permissions)) {
                    callJs("onHealthConnectConnected", "true")
                    syncHealth(userInitiated = true)
                } else {
                    permissionLauncher.launch(health.permissions)
                }
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                callJs("onHealthConnectError", JSONObject.quote("ต้องติดตั้งหรืออัปเดต Health Connect ก่อน"))
                openHealthConnectInStore()
            }
            else -> callJs("onHealthConnectError", JSONObject.quote("อุปกรณ์นี้ยังไม่รองรับ Health Connect"))
        }
    }

    private fun onPermissionResult(granted: Set<String>) {
        if (granted.isEmpty()) {
            callJs("onHealthConnectConnected", "false")
            return
        }
        callJs("onHealthConnectConnected", "true")
        syncHealth(userInitiated = true)
    }

    private fun syncHealth(userInitiated: Boolean) {
        if (!health.isAvailable()) {
            if (userInitiated) callJs("onHealthConnectError", JSONObject.quote("Health Connect ยังไม่พร้อมใช้งาน"))
            return
        }
        lifecycleScope.launch {
            try {
                val granted = health.grantedPermissions()
                if (granted.isEmpty()) {
                    if (userInitiated) callJs("onHealthConnectError", JSONObject.quote("ยังไม่ได้อนุญาตสิทธิ์ · กด ⌚ เชื่อมต่อ ก่อน"))
                    return@launch
                }
                val data = health.readToday(granted)
                Log.i(TAG, "synced $data")
                callJs("onHealthConnectData", JSONObject.quote(data.toString()))
            } catch (e: Exception) {
                Log.w(TAG, "sync failed", e)
                if (userInitiated) callJs("onHealthConnectError", JSONObject.quote("ซิงก์ไม่สำเร็จ: ${e.message}"))
            }
        }
    }

    private fun openHealthSettings() {
        try {
            startActivity(Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS))
        } catch (e: ActivityNotFoundException) {
            openHealthConnectInStore()
        }
    }

    private fun openHealthConnectInStore() {
        val uri = Uri.parse("market://details?id=${HealthConnectBridge.PROVIDER}&url=healthconnect%3A%2F%2Fonboarding")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).setPackage("com.android.vending"))
        } catch (e: ActivityNotFoundException) {
            toast("ไม่พบ Play Store สำหรับติดตั้ง Health Connect")
        }
    }

    /** arg must already be a JS literal (e.g. JSONObject.quote(...), "true"). */
    private fun callJs(fn: String, arg: String) = runOnUiThread {
        web.evaluateJavascript("window.$fn && window.$fn($arg)", null)
    }

    private fun toast(msg: String) = runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }

    // JavascriptInterface methods run on a background thread; hop to the UI thread for anything Android-side.
    inner class AndroidHealthConnectApi {
        @JavascriptInterface fun connect() = runOnUiThread { connectHealth() }
        @JavascriptInterface fun sync() = runOnUiThread { syncHealth(userInitiated = true) }
        @JavascriptInterface fun openSettings() = runOnUiThread { openHealthSettings() }
        @JavascriptInterface fun isAvailable(): Boolean = health.isAvailable()
    }

    // ---- Gmail sign-in + multi-device sync (Firebase) -----------------------------------------

    inner class CloudApi {
        /** {configured, user|null} — called synchronously by the page on load. */
        @JavascriptInterface fun status(): String =
            JSONObject().put("configured", cloud.configured).put("user", cloud.userJson() ?: JSONObject.NULL).toString()

        @JavascriptInterface fun signIn() = runOnUiThread {
            if (!cloud.configured) {
                callJs("onCloudError", JSONObject.quote(CloudSync.NOT_CONFIGURED))
                return@runOnUiThread
            }
            lifecycleScope.launch {
                try {
                    val user = cloud.signIn()
                    callJs("onCloudUser", JSONObject.quote(user.toString()))
                } catch (e: Exception) {
                    Log.w(TAG, "sign-in failed", e)
                    callJs("onCloudError", JSONObject.quote(e.message ?: "เข้าสู่ระบบไม่สำเร็จ"))
                }
            }
        }

        @JavascriptInterface fun signOut() = runOnUiThread {
            lifecycleScope.launch {
                runCatching { cloud.signOut() }
                callJs("onCloudUser", "null")
            }
        }

        @JavascriptInterface fun start() = runOnUiThread { cloud.start() }

        @JavascriptInterface fun push(key: String, value: String, updatedAt: Double, device: String) = runOnUiThread {
            lifecycleScope.launch {
                try {
                    cloud.push(key, value, updatedAt.toLong(), device)
                    callJs("onCloudPushed", JSONObject.quote(key))
                } catch (e: Exception) {
                    Log.w(TAG, "push failed", e)
                    callJs("onCloudError", JSONObject.quote(cloud.errorText(e)))
                }
            }
        }
    }

    /** WebView can't download blob: links, so backups are written to Downloads through this bridge. */
    inner class FilesApi {
        @JavascriptInterface
        fun saveText(name: String, text: String): String = try {
            val safe = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, safe)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("insert failed")
                contentResolver.openOutputStream(uri)!!.use { it.write(text.toByteArray()) }
                "บันทึกไฟล์สำรองไว้ในโฟลเดอร์ Download แล้ว: $safe"
            } else {
                val dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: filesDir
                val f = File(dir, safe).apply { writeText(text) }
                "บันทึกไฟล์สำรองแล้ว: ${f.absolutePath}"
            }
        } catch (e: Exception) {
            Log.w(TAG, "saveText failed", e)
            "บันทึกไฟล์ไม่สำเร็จ: ${e.message}"
        }
    }

    /** Older bridge name kept for compatibility. */
    inner class NativeHealthApi {
        @JavascriptInterface fun availability(): String = if (health.isAvailable()) "available" else "unavailable"
        @JavascriptInterface fun requestPermissions() = runOnUiThread { connectHealth() }
        @JavascriptInterface fun bridgeVersion(): String = "v94"
    }

    companion object {
        private const val TAG = "FoodWellHC"
    }
}
