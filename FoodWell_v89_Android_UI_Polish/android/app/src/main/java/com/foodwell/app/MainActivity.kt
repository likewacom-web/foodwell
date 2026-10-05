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
import android.app.AlertDialog
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.JavascriptInterface
import android.widget.EditText
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var web: WebView
    private lateinit var fileChooserLauncher: ActivityResultLauncher<Intent>
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var cloud: CloudSync
    private lateinit var pro: Monetization
    private lateinit var notifyPermission: ActivityResultLauncher<String>
    private var pendingPage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Last theme chosen in the page (light by default), so the first frame isn't the wrong colour.
        val themePrefs = getSharedPreferences("foodwell_theme", MODE_PRIVATE)
        val dark = runCatching { Color.parseColor(themePrefs.getString("bg", "#f6f7fa")) }.getOrDefault(Color.rgb(246, 247, 250))
        val lightBars = themePrefs.getBoolean("light", true)
        @Suppress("DEPRECATION")
        window.statusBarColor = dark
        @Suppress("DEPRECATION")
        window.navigationBarColor = dark

        fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
            filePathCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(r.resultCode, r.data))
            filePathCallback = null
        }
        cloud = CloudSync(this) { fn, arg -> callJs(fn, arg) }
        notifyPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
            callJs("onNotifyPermission", JSONObject.quote(if (ok) "granted" else "denied"))
        }
        pendingPage = intent?.getStringExtra("page")

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
                override fun onPageFinished(view: WebView, url: String?) {
                    // Opened from a reminder: jump to its page once the app has loaded.
                    pendingPage?.let { p -> pendingPage = null; view.postDelayed({ callJs("go", JSONObject.quote(p)) }, 400) }
                }
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

                // The page's alert/confirm/prompt in the app's own dialog, without WebView's
                // "The page at file:// says:" title.
                private fun dialog() = AlertDialog.Builder(this@MainActivity)
                private fun ok() = Lang.t(this@MainActivity, "ตกลง", "OK")
                private fun cancel() = Lang.t(this@MainActivity, "ยกเลิก", "Cancel")

                override fun onJsAlert(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
                    dialog().setMessage(message).setPositiveButton(ok()) { _, _ -> result.confirm() }
                        .setOnCancelListener { result.confirm() }.show()
                    return true
                }

                override fun onJsConfirm(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
                    dialog().setMessage(message)
                        .setPositiveButton(ok()) { _, _ -> result.confirm() }
                        .setNegativeButton(cancel()) { _, _ -> result.cancel() }
                        .setOnCancelListener { result.cancel() }.show()
                    return true
                }

                override fun onJsPrompt(view: WebView, url: String?, message: String?, defaultValue: String?, result: JsPromptResult): Boolean {
                    val input = EditText(this@MainActivity).apply { setText(defaultValue ?: ""); setSingleLine(); selectAll() }
                    val pad = (20 * resources.displayMetrics.density).toInt()
                    val box = FrameLayout(this@MainActivity).apply { setPadding(pad, pad / 2, pad, 0); addView(input) }
                    dialog().setMessage(message).setView(box)
                        .setPositiveButton(ok()) { _, _ -> result.confirm(input.text.toString()) }
                        .setNegativeButton(cancel()) { _, _ -> result.cancel() }
                        .setOnCancelListener { result.cancel() }.show()
                    return true
                }
            }
            addJavascriptInterface(FilesApi(), "FoodWellFiles")
            addJavascriptInterface(CloudApi(), "FoodWellCloud")
            addJavascriptInterface(ProApi(), "FoodWellPro")
            addJavascriptInterface(NotifyApi(), "FoodWellNotify")
            addJavascriptInterface(ThemeApi(), "FoodWellTheme")
            addJavascriptInterface(ScanApi(), "FoodWellScan")
            addJavascriptInterface(WidgetApi(), "FoodWellWidget")
            addJavascriptInterface(PrintApi(), "FoodWellPrint")
            addJavascriptInterface(LangApi(), "FoodWellLang")
            loadUrl("file:///android_asset/index.html")
        }

        // Android 15+ forces edge-to-edge for targetSdk 35, so the page was drawn under the
        // status bar and navigation bar. Opt in on every version and pad the WebView by the
        // system bar / cutout / keyboard insets instead.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Banner ad slot under the page; stays GONE unless ads are enabled and not removed.
        val adSlot = FrameLayout(this).apply { visibility = View.GONE; setBackgroundColor(dark) }
        val root = LinearLayout(this).apply {
            this@MainActivity.rootLayout = this
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(dark)
            addView(web, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(adSlot, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            WindowInsetsCompat.CONSUMED
        }
        setContentView(root)
        // getInsetsController() needs the DecorView, which exists only after setContentView().
        applyBars(dark, lightBars)

        pro = Monetization(this, adSlot) { fn, arg -> callJs(fn, arg) }
        pro.start()

        // Back: the page closes an open popup or returns to the home page first; on the home page
        // a second press within 2 seconds leaves the app.
        onBackPressedDispatcher.addCallback(this) {
            web.evaluateJavascript("(window.fwBack && window.fwBack()) || 'exit'") { r ->
                if (r?.contains("handled") == true) return@evaluateJavascript
                val now = System.currentTimeMillis()
                if (now - lastBack < 2000) finish()
                else { lastBack = now; toast(Lang.t(this@MainActivity, "กดย้อนกลับอีกครั้งเพื่อออกจากแอป", "Press back again to exit")) }
            }
        }
    }

    private var rootLayout: LinearLayout? = null
    private var lastBack = 0L

    /** Status/navigation bar colour and icon contrast to match the page theme. */
    private fun applyBars(color: Int, light: Boolean) {
        @Suppress("DEPRECATION")
        window.statusBarColor = color
        @Suppress("DEPRECATION")
        window.navigationBarColor = color
        rootLayout?.setBackgroundColor(color)
        if (::web.isInitialized) web.setBackgroundColor(color)
        val flags = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.setSystemBarsAppearance(if (light) flags else 0, flags)
        } else {
            WindowCompat.getInsetsController(window, window.decorView).run {
                isAppearanceLightStatusBars = light
                isAppearanceLightNavigationBars = light
            }
        }
    }

    /** Reports: the page sends a self-contained HTML document; Android's print dialog prints it or saves it as PDF. */
    private var printWeb: WebView? = null

    inner class PrintApi {
        @JavascriptInterface fun print(html: String, title: String) = runOnUiThread {
            val name = title.ifBlank { "FoodWell" }
            var sent = false
            printWeb = WebView(this@MainActivity).apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        if (sent) return
                        sent = true
                        val pm = getSystemService(PRINT_SERVICE) as android.print.PrintManager
                        pm.print(
                            name, view.createPrintDocumentAdapter(name),
                            android.print.PrintAttributes.Builder().setMediaSize(android.print.PrintAttributes.MediaSize.ISO_A4).build()
                        )
                    }
                }
                loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null)
            }
        }
    }

    /** Home-screen widget: the page sends today's numbers and collects water added from the widget. */
    inner class WidgetApi {
        @JavascriptInterface fun update(json: String) = TodayWidget.saveSnapshot(this@MainActivity, json)
        @JavascriptInterface fun drain(): String = TodayWidget.drain(this@MainActivity)
    }

    /** Barcode scanning through Google Play services' scanner UI (no camera permission needed). */
    inner class ScanApi {
        @JavascriptInterface fun scan() = runOnUiThread {
            val opts = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
                .build()
            GmsBarcodeScanning.getClient(this@MainActivity, opts).startScan()
                .addOnSuccessListener { b -> callJs("onBarcode", JSONObject.quote(b.rawValue ?: "")) }
                .addOnCanceledListener { callJs("onBarcodeCancel", "") }
                .addOnFailureListener { e -> Log.w(TAG, "scan failed", e); callJs("onBarcodeError", JSONObject.quote(e.message ?: "")) }
        }
    }

    inner class LangApi {
        /** The page tells us its language so notifications and the widget match it. */
        @JavascriptInterface fun set(lang: String) {
            if (Lang.isEn(this@MainActivity) == (lang == "en")) return
            Lang.set(this@MainActivity, lang)
            Reminders.ensureChannel(this@MainActivity)
            TodayWidget.refresh(this@MainActivity)
        }
    }

    inner class ThemeApi {
        @JavascriptInterface fun setBars(hex: String, light: Boolean) = runOnUiThread {
            val c = runCatching { Color.parseColor(hex) }.getOrNull() ?: return@runOnUiThread
            getSharedPreferences("foodwell_theme", MODE_PRIVATE).edit().putString("bg", hex).putBoolean("light", light).apply()
            applyBars(c, light)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("page")?.let { callJs("go", JSONObject.quote(it)) }
    }

    override fun onPause() {
        pro.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        pro.onResume()
    }

    override fun onDestroy() {
        pro.onDestroy()
        super.onDestroy()
    }

    /** arg must already be a JS literal (e.g. JSONObject.quote(...), "true"). */
    private fun callJs(fn: String, arg: String) = runOnUiThread {
        web.evaluateJavascript("window.$fn && window.$fn($arg)", null)
    }

    private fun toast(msg: String) = runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }

    // JavascriptInterface methods run on a background thread; hop to the UI thread for anything Android-side.

    // ---- Gmail sign-in + multi-device sync (Firebase) -----------------------------------------

    inner class CloudApi {
        /** {configured, user|null} — called synchronously by the page on load. */
        @JavascriptInterface fun status(): String =
            JSONObject().put("configured", cloud.configured).put("reason", cloud.notReadyReason ?: "")
                .put("user", cloud.userJson() ?: JSONObject.NULL).toString()

        @JavascriptInterface fun signIn() = runOnUiThread {
            if (!cloud.configured) {
                callJs("onCloudError", JSONObject.quote(cloud.notReadyReason ?: CloudSync.NOT_CONFIGURED))
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

        @JavascriptInterface fun pushImage(hash: String, data: String) = runOnUiThread {
            lifecycleScope.launch {
                try {
                    cloud.pushImage(hash, data)
                    callJs("onCloudImagePushed", JSONObject.quote(hash))
                } catch (e: Exception) {
                    Log.w(TAG, "image push failed", e)
                }
            }
        }

        /** Answers with onCloudImage(hash, dataUrl), or an empty string when it isn't uploaded yet. */
        @JavascriptInterface fun fetchImage(hash: String) = runOnUiThread {
            lifecycleScope.launch {
                val data = try { cloud.fetchImage(hash) } catch (e: Exception) { Log.w(TAG, "image fetch failed", e); null }
                callJs("onCloudImage", JSONObject.quote(hash) + "," + JSONObject.quote(data ?: ""))
            }
        }

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

    // ---- Reminders (real Android notifications) -------------------------------------------------

    inner class NotifyApi {
        /** "granted" | "denied" | "prompt" */
        @JavascriptInterface fun permission(): String = when {
            Reminders.canNotify(this@MainActivity) -> "granted"
            Build.VERSION.SDK_INT >= 33 && shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> "prompt"
            Build.VERSION.SDK_INT >= 33 && getSharedPreferences("foodwell_reminders", MODE_PRIVATE).getBoolean("asked", false) -> "denied"
            else -> "prompt"
        }

        /** Answers with onNotifyPermission("granted"|"denied"). */
        @JavascriptInterface fun request() = runOnUiThread {
            if (Reminders.canNotify(this@MainActivity) || Build.VERSION.SDK_INT < 33) {
                callJs("onNotifyPermission", JSONObject.quote("granted"))
            } else {
                getSharedPreferences("foodwell_reminders", MODE_PRIVATE).edit().putBoolean("asked", true).apply()
                notifyPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        /** Opens the system notification settings for this app (after the user said "don't ask again"). */
        @JavascriptInterface fun openSettings() = runOnUiThread {
            val i = if (Build.VERSION.SDK_INT >= 26) Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
            else Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
            runCatching { startActivity(i) }
        }

        @JavascriptInterface fun configure(json: String) = Reminders.configure(this@MainActivity, json)
        @JavascriptInterface fun report(json: String) = Reminders.report(this@MainActivity, json)
        @JavascriptInterface fun test() = Reminders.post(this@MainActivity, 999, Lang.t(this@MainActivity, "🔔 ทดสอบการแจ้งเตือน", "🔔 Test notification"), Lang.t(this@MainActivity, "FoodWell จะเตือนแบบนี้ตามเวลาที่ตั้งไว้", "FoodWell will remind you like this at the times you set"), "more")
    }

    // ---- One-time purchase that removes ads (Google Play Billing) ------------------------------

    inner class ProApi {
        /** {adsEnabled, adFree, billingReady, price, pending, message} */
        @JavascriptInterface fun status(): String = pro.statusJson().toString()
        @JavascriptInterface fun buy() = pro.buy()
        @JavascriptInterface fun restore() = pro.restore()
        @JavascriptInterface fun showRewarded() = pro.showRewarded()
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

    companion object {
        private const val TAG = "FoodWell"
    }
}
