package com.foodwell.app

import android.content.Context
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
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Banner ads (AdMob) plus a one-time Google Play purchase that removes them for good.
 *
 * Ads only run when the build sets ADS_ENABLED (gradle -Pads=true, see MONETIZATION.md); otherwise
 * this class still handles the purchase so the "remove ads" card can be tried. The purchase is
 * remembered locally and restored from Google Play on every launch (and on reinstall/new phone).
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

    var adFree: Boolean
        get() = prefs.getBoolean("adFree", false)
        private set(v) {
            prefs.edit().putBoolean("adFree", v).apply()
            if (v) activity.runOnUiThread { removeAds() }
        }

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

    private fun emitStatus() = emit("onProStatus", JSONObject.quote(statusJson().toString()))

    private suspend fun loadProduct() {
        val params = QueryProductDetailsParams.newBuilder().setProductList(
            listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_ID).setProductType(BillingClient.ProductType.INAPP).build()
            )
        ).build()
        val r = billing.queryProductDetails(params)
        product = r.productDetailsList?.firstOrNull()
        if (product == null) message = "ยังไม่พบสินค้า \"$PRODUCT_ID\" ใน Google Play Console"
        emitStatus()
    }

    fun buy() = activity.runOnUiThread {
        val pd = product
        if (!billingReady || pd == null) {
            message = message.ifEmpty { "ซื้อได้เมื่อติดตั้งแอปจาก Google Play" }
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
                message = "เชื่อมต่อ Google Play ไม่ได้ · ลองใหม่อีกครั้ง"
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
            if (!quiet && !adFree && !pending) message = "ไม่พบการซื้อในบัญชี Google Play นี้"
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
                adFree = true
            }
            Purchase.PurchaseState.PENDING -> {
                pending = true
                message = "รอการชำระเงิน · โฆษณาจะหายไปเมื่อชำระเสร็จ"
            }
            else -> Unit
        }
    }

    // ---- Ads -----------------------------------------------------------------------------------

    private fun showAds() {
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
    }

    private fun removeAds() {
        adView?.destroy()
        adView = null
        adContainer.removeAllViews()
        adContainer.visibility = View.GONE
    }

    fun onPause() = adView?.pause()
    fun onResume() = adView?.resume()
    fun onDestroy() {
        adView?.destroy()
        billing.endConnection()
    }

    private fun billingText(r: BillingResult) = when (r.responseCode) {
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "ซื้อได้เมื่อติดตั้งแอปจาก Google Play"
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
        BillingClient.BillingResponseCode.NETWORK_ERROR -> "ไม่มีอินเทอร์เน็ต · ลองใหม่อีกครั้ง"
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "สินค้านี้ยังไม่เปิดขาย"
        else -> "Google Play ตอบกลับ ${r.responseCode} ${r.debugMessage}".trim()
    }

    companion object {
        /** One-time ("in-app") product to create in Play Console → Monetize → In-app products. */
        const val PRODUCT_ID = "remove_ads"
        private const val TAG = "FoodWell"
    }
}
