package com.foodwell.app

import android.content.Context

/** App language chosen on the page's Settings (th / en); used for notifications, the widget and toasts. */
object Lang {
    private fun prefs(c: Context) = c.getSharedPreferences("foodwell_lang", Context.MODE_PRIVATE)

    fun isEn(c: Context) = prefs(c).getString("lang", "th") == "en"

    fun set(c: Context, lang: String) = prefs(c).edit().putString("lang", if (lang == "en") "en" else "th").apply()

    /** Thai or English text depending on the chosen language. */
    fun t(c: Context, th: String, en: String) = if (isEn(c)) en else th
}
