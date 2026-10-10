package com.foodwell.app

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import org.json.JSONArray
import java.io.File

/**
 * window.FoodWellShare — share text and an image card to other apps (LINE, Facebook, Instagram, X, …)
 * through Android's share sheet, or straight to one app when its package is given.
 */
class ShareApi(private val activity: ComponentActivity) {

    /** Social apps the page offers as buttons; only installed ones are reported. */
    private val known = listOf(
        "jp.naver.line.android", "com.facebook.katana", "com.instagram.android",
        "com.twitter.android", "com.facebook.orca", "com.whatsapp", "org.telegram.messenger",
    )

    @JavascriptInterface
    fun installed(): String = JSONArray(known.filter { isInstalled(it) }).toString()

    private fun isInstalled(pkg: String) = try {
        activity.packageManager.getPackageInfo(pkg, 0); true
    } catch (e: PackageManager.NameNotFoundException) { false }

    /** imageDataUrl: "data:image/png;base64,…" or empty; pkg: target app or empty for the share sheet. */
    @JavascriptInterface
    fun share(text: String, imageDataUrl: String, pkg: String) = activity.runOnUiThread {
        val send = Intent(Intent.ACTION_SEND)
        val png = decode(imageDataUrl)
        if (png != null) {
            val dir = File(activity.cacheDir, "share").apply { mkdirs(); listFiles()?.forEach { it.delete() } }
            val f = File(dir, "foodwell_${System.currentTimeMillis()}.png").apply { writeBytes(png) }
            val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.share", f)
            send.type = "image/png"
            send.putExtra(Intent.EXTRA_STREAM, uri)
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else send.type = "text/plain"
        if (text.isNotEmpty()) send.putExtra(Intent.EXTRA_TEXT, text)
        try {
            if (pkg.isNotEmpty() && isInstalled(pkg)) activity.startActivity(send.setPackage(pkg))
            else activity.startActivity(Intent.createChooser(send, Lang.t(activity, "แชร์ด้วย", "Share with")))
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(Intent.createChooser(send.setPackage(null), Lang.t(activity, "แชร์ด้วย", "Share with")))
        }
    }

    /** Saves the image to the gallery (Pictures/MeowFit). */
    @JavascriptInterface
    fun saveImage(imageDataUrl: String) = activity.runOnUiThread {
        val png = decode(imageDataUrl) ?: return@runOnUiThread
        val name = "foodwell_${System.currentTimeMillis()}.png"
        val ok = try {
            if (Build.VERSION.SDK_INT >= 29) {
                val v = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MeowFit")
                }
                val uri = activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v)
                uri != null && activity.contentResolver.openOutputStream(uri)?.use { it.write(png); true } == true
            } else {
                val dir = File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MeowFit").apply { mkdirs() }
                File(dir, name).writeBytes(png); true
            }
        } catch (e: Exception) { false }
        Toast.makeText(
            activity,
            if (ok) Lang.t(activity, "บันทึกรูปลงแกลเลอรีแล้ว (Pictures/MeowFit)", "Saved to your gallery (Pictures/MeowFit)")
            else Lang.t(activity, "บันทึกรูปไม่สำเร็จ", "Couldn't save the image"),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun decode(dataUrl: String): ByteArray? {
        val i = dataUrl.indexOf("base64,")
        if (i < 0) return null
        return try { Base64.decode(dataUrl.substring(i + 7), Base64.DEFAULT) } catch (e: IllegalArgumentException) { null }
    }
}
