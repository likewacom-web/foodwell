package com.foodwell.app

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.util.UUID

/**
 * window.FoodWellScale — reads Bluetooth bathroom scales directly (Android 12+, no location permission).
 *
 * Most low-cost smart scales (Orsen and other OEM scales) broadcast the reading in their Bluetooth
 * advertisement. This class only passes the raw advertisement bytes to the page, which learns where the
 * weight sits by comparing them with the number the user reads off the scale once (see the page's
 * "scale" block). Scales that implement the standard Bluetooth Weight Scale service are read directly.
 *
 * Answers the page through onScale(kind, json):
 *  "adv"  {addr, name, rssi, raw}   raw = advertisement + scan response as hex
 *  "std"  {addr, name, kg}          standard Weight Scale measurement
 *  "scan" "on" | "off"
 *  "error" "old" | "none" | "perm" | "off"
 */
class BleScale(private val activity: ComponentActivity, private val emit: (fn: String, arg: String) -> Unit) {
    private val main = Handler(Looper.getMainLooper())
    private val adapter: BluetoothAdapter? get() = activity.getSystemService(BluetoothManager::class.java)?.adapter
    private var scanning = false
    private var secs = 60
    private val lastSent = HashMap<String, Pair<String, Long>>()
    private var gatt: BluetoothGatt? = null

    private val perms = if (Build.VERSION.SDK_INT >= 31)
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT) else emptyArray()

    private val permLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.isNotEmpty() && r.values.all { it }) begin() else error("perm")
    }
    private val btLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (adapter?.isEnabled == true) begin() else error("off")
    }

    private fun q(s: String) = JSONObject.quote(s)
    private fun error(code: String) = emit("onScale", q("error") + "," + q(code))
    private fun granted() = perms.all { ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED }

    /** "ok" | "old" (Android 11 or older) | "none" (no Bluetooth LE) */
    @JavascriptInterface
    fun status(): String = when {
        Build.VERSION.SDK_INT < 31 -> "old"
        adapter == null || !activity.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) -> "none"
        else -> "ok"
    }

    /** Scans for [seconds] (asks for the Bluetooth permission and to turn Bluetooth on first if needed). */
    @JavascriptInterface
    fun start(seconds: Int) = main.post {
        secs = seconds.coerceIn(5, 180)
        val st = status()
        when {
            st != "ok" -> error(st)
            !granted() -> permLauncher.launch(perms)
            else -> begin()
        }
    }

    @JavascriptInterface
    fun stop() = main.post { end() }

    @SuppressLint("MissingPermission")
    private fun begin() {
        if (adapter?.isEnabled != true) { btLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)); return }
        val scanner = adapter?.bluetoothLeScanner ?: return error("off")
        if (scanning) runCatching { scanner.stopScan(cb) }
        lastSent.clear()
        try {
            scanner.startScan(null, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), cb)
        } catch (e: Exception) {
            Log.w(TAG, "scan failed", e); return error("off")
        }
        scanning = true
        emit("onScale", q("scan") + "," + q("on"))
        main.removeCallbacks(timeout)
        main.postDelayed(timeout, secs * 1000L)
    }

    private val timeout = Runnable { end() }

    @SuppressLint("MissingPermission")
    private fun end() {
        main.removeCallbacks(timeout)
        if (scanning) runCatching { adapter?.bluetoothLeScanner?.stopScan(cb) }
        scanning = false
        runCatching { gatt?.close() }; gatt = null
        emit("onScale", q("scan") + "," + q("off"))
    }

    fun onPause() { if (scanning) end() }

    private val cb = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, r: ScanResult) {
            val rec = r.scanRecord ?: return
            val addr = r.device.address
            // the record is zero-padded: keep only its length-prefixed AD structures
            val b = rec.bytes
            var end = 0
            while (end < b.size && (b[end].toInt() and 0xff) != 0) end += (b[end].toInt() and 0xff) + 1
            val hex = b.copyOfRange(0, end.coerceAtMost(b.size)).joinToString("") { "%02x".format(it) }
            val now = System.currentTimeMillis()
            val prev = lastSent[addr]
            // send changes right away, unchanged adverts at most once a second
            if (prev != null && prev.first == hex && now - prev.second < 1000) return
            lastSent[addr] = hex to now
            val name = rec.deviceName ?: runCatching { r.device.name }.getOrNull() ?: ""
            emit("onScale", q("adv") + "," + q(JSONObject().put("addr", addr).put("name", name).put("rssi", r.rssi).put("raw", hex).toString()))
            // standard Bluetooth Weight Scale (0x181D): connect once and listen for measurements
            if (gatt == null && rec.serviceUuids?.any { it.uuid == WEIGHT_SERVICE } == true) {
                gatt = r.device.connectGatt(activity, false, gattCb)
            }
        }

        override fun onScanFailed(errorCode: Int) { scanning = false; error("off") }
    }

    private val gattCb = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) g.discoverServices()
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) { runCatching { g.close() }; if (gatt === g) gatt = null }
        }

        @SuppressLint("MissingPermission")
        @Suppress("DEPRECATION")
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            val ch = g.getService(WEIGHT_SERVICE)?.getCharacteristic(WEIGHT_MEASUREMENT) ?: return
            g.setCharacteristicNotification(ch, true)
            ch.getDescriptor(CCCD)?.let { it.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE; g.writeDescriptor(it) }
        }

        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(g: BluetoothGatt, ch: BluetoothGattCharacteristic) {
            if (Build.VERSION.SDK_INT < 33) measurement(g, ch, ch.value ?: return)
        }

        override fun onCharacteristicChanged(g: BluetoothGatt, ch: BluetoothGattCharacteristic, value: ByteArray) =
            measurement(g, ch, value)

        @SuppressLint("MissingPermission")
        private fun measurement(g: BluetoothGatt, ch: BluetoothGattCharacteristic, v: ByteArray) {
            if (ch.uuid != WEIGHT_MEASUREMENT || v.size < 3) return
            val imperial = v[0].toInt() and 1 == 1
            val raw = (v[1].toInt() and 0xff) or ((v[2].toInt() and 0xff) shl 8)
            val kg = if (imperial) raw * 0.01 * 0.45359237 else raw * 0.005
            val name = runCatching { g.device.name }.getOrNull() ?: ""
            emit("onScale", q("std") + "," + q(JSONObject().put("addr", g.device.address).put("name", name)
                .put("kg", Math.round(kg * 10) / 10.0).toString()))
        }
    }

    companion object {
        private const val TAG = "FoodWellScale"
        private val WEIGHT_SERVICE: UUID = UUID.fromString("0000181d-0000-1000-8000-00805f9b34fb")
        private val WEIGHT_MEASUREMENT: UUID = UUID.fromString("00002a9d-0000-1000-8000-00805f9b34fb")
        private val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
