package com.foodwell.app

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.webkit.JavascriptInterface
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import org.json.JSONObject

/**
 * Speech to text for "what did you eat?" (window.FoodWellVoice). Opens the phone's own speech
 * recognizer (Google's voice typing on most phones), so the app needs no microphone permission.
 * Answers the page through onVoiceText({text}) or onVoiceText({error: "cancelled" | "none"}).
 */
class VoiceInput(private val act: ComponentActivity, private val emit: (String, String) -> Unit) {
    private val launcher = act.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val text = r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        send(if (r.resultCode == Activity.RESULT_OK && text.isNotBlank()) JSONObject().put("text", text) else JSONObject().put("error", "cancelled"))
    }

    private fun send(o: JSONObject) = emit("onVoiceText", JSONObject.quote(o.toString()))

    /** True when the phone has a speech recognizer the app can open. */
    @JavascriptInterface
    fun available(): Boolean = intent("th-TH", "").resolveActivity(act.packageManager) != null

    /** lang: "th-TH" or "en-US"; prompt: the line shown in the recognizer's dialog. */
    @JavascriptInterface
    fun listen(lang: String, prompt: String) = act.runOnUiThread {
        try {
            launcher.launch(intent(lang, prompt))
        } catch (e: ActivityNotFoundException) {
            send(JSONObject().put("error", "none"))
        }
    }

    private fun intent(lang: String, prompt: String) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang.ifBlank { "th-TH" })
        .putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
        .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
}
