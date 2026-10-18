package com.foodwell.app

import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryPurchasesAsync
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Banner ads (AdMob) plus a one-time Google Play purchase that removes them for good.
 *
 * Ads only run when the build sets ADS_ENABLED (gradle -Pads=true, see MONETIZATION.md); otherwise
 * this class still handles the purchase so the "remove ads" card can be tried. The purchase is
 * remembered locally and restored from Google Play on every launch (and on reinstall/new phone).
 *
 * An activation code (made with the AI Worker's /admin page) gives the same full version for the
 * code's number of days, or for good. Each code works on one phone only; entering it again on that
 * phone (after a reinstall) restores it.
 */
class Monetization(
    private val activity: ComponentActivity,
    private val adContainer: FrameLayout,
    private val emit: (fn: String, arg: String) -> Unit,
) : PurchasesUpdatedListener {
    private val prefs = activity.getSharedPreferences("foodwell_pro", Context.MODE_PRIVATE)
    private val billing = BillingClient.newBuilder(activity)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()
    private var product: ProductDetails? = null
    private var billingReady = false
    private var pending = false
    private var message = ""
    private var adView: AdView? = null
    private var rewarded: RewardedAd? = null
    private var rewardedLoading = false
    private var adsStarted = false
    private var redeeming = false

    /** Bought "remove ads" on Google Play. */
    private var purchased: Boolean
        get() = prefs.getBoolean("adFree", false)
        set(v) {
            prefs.edit().putBoolean("adFree", v).apply()
            if (v) activity.runOnUiThread { removeAds() }
        }

    /** End of the full version from an activation code (epoch ms; Long.MAX_VALUE = for good, 0 = none). */
    private var codeUntil: Long
        get() = prefs.getLong("codeUntil", 0L)
        set(v) { prefs.edit().putLong("codeUntil", v).apply() }

    private fun codeActive() = codeUntil > System.currentTimeMillis()

    /** Full version: no ads and unlimited AI photo analysis. */
    val adFree: Boolean get() = purchased || codeActive()

    fun start() {
        connect()
        if (BuildConfig.ADS_ENABLED && !adFree) showAds()
    }

    private fun connect() {
        if (billing.isReady) return
        billing.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                billingReady = result.responseCode == BillingClient.BillingResponseCode.OK
                message = if (billingReady) "" else billingText(result)
                if (billingReady) activity.lifecycleScope.launch { loadProduct(); restore(quiet = true) }
                emitStatus()
            }

            override fun onBillingServiceDisconnected() {
                billingReady = false
            }
        })
    }

    fun statusJson(): JSONObject = JSONObject()
        .put("adsEnabled", BuildConfig.ADS_ENABLED)
        .put("adFree", adFree)
        .put("billingReady", billingReady && product != null)
        .put("price", product?.oneTimePurchaseOfferDetails?.formattedPrice ?: "")
        .put("pending", pending)
        .put("message", message)
        .put("rewardedReady", rewarded != null)
        .put("purchased", purchased)
        .put("codeUntil", if (codeActive()) codeUntil else 0L)
        .put("codeForever", codeActive() && codeUntil == Long.MAX_VALUE)
        .put("redeeming", redeeming)

    private fun emitStatus() = emit("onProStatus", JSONObject.quote(statusJson().toString()))

    private suspend fun loadProduct() {
        val params = QueryProductDetailsParams.newBuilder().setProductList(
            listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_ID).setProductType(BillingClient.ProductType.INAPP).build()
            )
        ).build()
        // Billing 8 hands back a QueryProductDetailsResult; the callback form keeps this independent of the ktx wrapper.
        product = suspendCancellableCoroutine { cont ->
            billing.queryProductDetailsAsync(params) { _, res -> if (cont.isActive) cont.resume(res.productDetailsList.firstOrNull()) }
        }
        if (product == null) message = Lang.t(activity, "ยังไม่พบสินค้า \"$PRODUCT_ID\" ใน Google Play Console", "Product \"$PRODUCT_ID\" not found in Google Play Console yet")
        emitStatus()
    }

    fun buy() = activity.runOnUiThread {
        val pd = product
        if (!billingReady || pd == null) {
            message = message.ifEmpty { Lang.t(activity, "ซื้อได้เมื่อติดตั้งแอปจาก Google Play", "Purchases work when the app is installed from Google Play") }
            connect()
            emitStatus()
            return@runOnUiThread
        }
        val flow = BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(pd).build())
        ).build()
        val r = billing.launchBillingFlow(activity, flow)
        if (r.responseCode != BillingClient.BillingResponseCode.OK) {
            message = billingText(r)
            emitStatus()
        }
    }

    fun restore(quiet: Boolean = false) {
        if (!billingReady) {
            if (!quiet) {
                message = Lang.t(activity, "เชื่อมต่อ Google Play ไม่ได้ · ลองใหม่อีกครั้ง", "Couldn't reach Google Play · try again")
                emitStatus()
            }
            connect()
            return
        }
        activity.lifecycleScope.launch {
            val r = billing.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
            )
            r.purchasesList.forEach { handle(it) }
            if (!quiet && !purchased && !pending) message = Lang.t(activity, "ไม่พบการซื้อในบัญชี Google Play นี้", "No purchase found on this Google Play account")
            emitStatus()
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> activity.lifecycleScope.launch {
                purchases?.forEach { handle(it) }
                emitStatus()
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> restore()
            BillingClient.BillingResponseCode.USER_CANCELED -> { message = ""; emitStatus() }
            else -> { message = billingText(result); emitStatus() }
        }
    }

    private suspend fun handle(p: Purchase) {
        if (PRODUCT_ID !in p.products) return
        when (p.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                // Unacknowledged purchases are refunded by Google Play after 3 days.
                if (!p.isAcknowledged) {
                    val r = billing.acknowledgePurchase(
                        AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()
                    )
                    if (r.responseCode != BillingClient.BillingResponseCode.OK) Log.w(TAG, "acknowledge: ${r.debugMessage}")
                }
                pending = false
                message = ""
                purchased = true
            }
            Purchase.PurchaseState.PENDING -> {
                pending = true
                message = Lang.t(activity, "รอการชำระเงิน · โฆษณาจะหายไปเมื่อชำระเสร็จ", "Payment pending · ads go away once it completes")
            }
            else -> Unit
        }
    }

    // ---- Ads -----------------------------------------------------------------------------------

    /** Checks [raw] with the Worker; a valid, unused code turns on the full version for its days. */
    fun redeem(raw: String) {
        val code = raw.uppercase().filter { it.isLetterOrDigit() }
        if (code.length < 8) {
            message = Lang.t(activity, "ใส่โค้ดให้ครบ", "Enter the whole code")
            emitStatus()
            return
        }
        if (redeeming) return
        redeeming = true
        message = ""
        emitStatus()
        activity.lifecycleScope.launch {
            val (status, body) = withContext(Dispatchers.IO) { postRedeem(code) }
            redeeming = false
            val err = body?.optString("error").orEmpty()
            if (status == 200 && body != null) {
                val now = System.currentTimeMillis()
                val until = when {
                    body.optBoolean("forever") -> Long.MAX_VALUE
                    // same code again on this phone (reinstall): restore its own end date
                    body.optBoolean("again") -> body.optLong("until")
                    // a new code adds its days on top of any time still left
                    else -> maxOf(now, codeUntil.coerceAtMost(Long.MAX_VALUE / 2)) + body.optLong("days") * 86_400_000L
                }
                if (until > now) {
                    codeUntil = maxOf(codeUntil, until)
                    message = ""
                    activity.runOnUiThread { removeAds() }
                } else {
                    message = Lang.t(activity, "โค้ดนี้หมดเวลาใช้งานแล้ว", "This code's time is already over")
                }
            } else {
                message = err.ifEmpty {
                    if (status == 0) Lang.t(activity, "ไม่มีอินเทอร์เน็ต · ลองใหม่อีกครั้ง", "No internet · try again")
                    else Lang.t(activity, "ตรวจโค้ดไม่สำเร็จ ($status)", "Couldn't check the code ($status)")
                }
            }
            emitStatus()
        }
    }

    /** (HTTP status, JSON body); status 0 when the request never got an answer. */
    private fun postRedeem(code: String): Pair<Int, JSONObject?> = try {
        val c = URL(REDEEM_URL).openConnection() as HttpURLConnection
        c.requestMethod = "POST"
        c.connectTimeout = 15000
        c.readTimeout = 15000
        c.doOutput = true
        c.setRequestProperty("Content-Type", "application/json")
        c.outputStream.use { it.write(JSONObject().put("code", code).put("device", deviceId()).toString().toByteArray()) }
        val status = c.responseCode
        val text = (if (status < 400) c.inputStream else c.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
        c.disconnect()
        status to runCatching { JSONObject(text) }.getOrNull()
    } catch (e: Exception) {
        Log.w(TAG, "redeem", e)
        0 to null
    }

    /** Stable per phone and app (survives reinstall), hashed so the raw Android ID never leaves the phone. */
    private fun deviceId(): String {
        val raw = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID).orEmpty() + ":" + activity.packageName
        return MessageDigest.getInstance("SHA-256").digest(raw.toByteArray()).joinToString("") { "%02x".format(it) }.take(32)
    }

    private fun showAds() {
        adsStarted = true
        // Google requires the consent form (EEA/UK) before personalised ads are requested.
        val consent = UserMessagingPlatform.getConsentInformation(activity)
        consent.requestConsentInfoUpdate(
            activity, ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { err ->
                    if (err != null) Log.w(TAG, "consent form: ${err.message}")
                    if (consent.canRequestAds()) loadBanner()
                }
            },
            { err -> Log.w(TAG, "consent update: ${err.message}"); if (consent.canRequestAds()) loadBanner() }
        )
        if (consent.canRequestAds()) loadBanner()
    }

    private fun loadBanner() = activity.runOnUiThread {
        if (adView != null || adFree) return@runOnUiThread
        MobileAds.initialize(activity) {}
        val m = activity.resources.displayMetrics
        adView = AdView(activity).apply {
            adUnitId = BuildConfig.ADMOB_BANNER_ID
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, (m.widthPixels / m.density).toInt()))
            loadAd(AdRequest.Builder().build())
        }
        adContainer.addView(adView)
        adContainer.visibility = View.VISIBLE
        loadRewarded()
    }

    // ---- Rewarded ads: watch a short ad for one more AI photo analysis --------------------------

    private fun loadRewarded() {
        if (!BuildConfig.ADS_ENABLED || adFree || rewarded != null || rewardedLoading) return
        rewardedLoading = true
        RewardedAd.load(activity, BuildConfig.ADMOB_REWARDED_ID, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) { rewarded = ad; rewardedLoading = false }
            override fun onAdFailedToLoad(e: LoadAdError) { rewarded = null; rewardedLoading = false; Log.w(TAG, "rewarded load: ${e.message}") }
        })
    }

    /** Answers onRewardEarned() after the ad closes with the reward, onRewardClosed() if skipped,
     *  or onRewardUnavailable(reason) when there is no ad to show (the page then lets the user through). */
    fun showRewarded() = activity.runOnUiThread {
        val ad = rewarded
        if (!BuildConfig.ADS_ENABLED || adFree) { emit("onRewardEarned", ""); return@runOnUiThread }
        if (ad == null) { emit("onRewardUnavailable", JSONObject.quote("no_ad")); loadRewarded(); return@runOnUiThread }
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewarded = null; loadRewarded()
                emit(if (earned) "onRewardEarned" else "onRewardClosed", "")
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                rewarded = null; loadRewarded()
                emit("onRewardUnavailable", JSONObject.quote(e.message ?: "show_failed"))
            }
        }
        ad.show(activity) { earned = true }
    }

    private fun removeAds() {
        adsStarted = false
        adView?.destroy()
        adView = null
        adContainer.removeAllViews()
        adContainer.visibility = View.GONE
    }

    fun onPause() = adView?.pause()
    fun onResume() {
        adView?.resume()
        // a code's time ran out while the app was in the background
        if (codeUntil != 0L && !codeActive()) { codeUntil = 0L; emitStatus() }
        if (BuildConfig.ADS_ENABLED && !adFree && !adsStarted) showAds()
    }
    fun onDestroy() {
        adView?.destroy()
        billing.endConnection()
    }

    private fun billingText(r: BillingResult) = when (r.responseCode) {
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> Lang.t(activity, "ซื้อได้เมื่อติดตั้งแอปจาก Google Play", "Purchases work when the app is installed from Google Play")
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
        BillingClient.BillingResponseCode.NETWORK_ERROR -> Lang.t(activity, "ไม่มีอินเทอร์เน็ต · ลองใหม่อีกครั้ง", "No internet · try again")
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> Lang.t(activity, "สินค้านี้ยังไม่เปิดขาย", "This item isn't on sale yet")
        else -> (Lang.t(activity, "Google Play ตอบกลับ", "Google Play replied") + " ${r.responseCode} ${r.debugMessage}").trim()
    }

    companion object {
        /** One-time ("in-app") product to create in Play Console → Monetize → In-app products. */
        const val PRODUCT_ID = "remove_ads"
        /** Activation codes are checked by the same Cloudflare Worker as the AI photo analysis (ai-proxy/worker.js). */
        private const val REDEEM_URL = "https://round-frog-a0d0meowfit-ai.meowfitcaloriestracker.workers.dev/redeem?k=70a9520d"
        private const val TAG = "FoodWell"
    }
}
