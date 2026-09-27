package com.foodwell.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.health.connect.client.PermissionController;
import java.util.Set;

public class MainActivity extends Activity {
    WebView web;
    HealthConnectBridge health;
    ActivityResultLauncher<Set<String>> permissionLauncher;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        Window w = getWindow();
        w.setStatusBarColor(android.graphics.Color.rgb(16,17,22));
        w.setNavigationBarColor(android.graphics.Color.rgb(16,17,22));
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = w.getInsetsController();
            if (c != null) c.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
        }
        health = new HealthConnectBridge(this);
        permissionLauncher = registerForActivityResult(
            PermissionController.createRequestPermissionResultContract(),
            granted -> showHealthStatus()
        );
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setTextZoom(100);
        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        web.setBackgroundColor(android.graphics.Color.rgb(16,17,22));
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new NativeHealthApi(), "FoodWellHealth");
        web.loadUrl("file:///android_asset/index.html");
        setContentView(web);
    }

    private void showHealthStatus() {
        Toast.makeText(this, health.isAvailable() ? "Health Connect พร้อมใช้งาน" : "ยังไม่พร้อมใช้งาน", Toast.LENGTH_SHORT).show();
    }

    public final class NativeHealthApi {
        @JavascriptInterface public String availability() { return health.isAvailable() ? "available" : "unavailable"; }
        @JavascriptInterface public void requestPermissions() {
            if (!health.isAvailable()) { showHealthStatus(); return; }
            permissionLauncher.launch(health.requestedReadPermissions());
        }
        @JavascriptInterface public String bridgeVersion() { return "v89"; }
    }

    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
