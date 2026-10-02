package com.foodwell.app;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.health.connect.client.PermissionController;
import java.util.Set;

public class MainActivity extends ComponentActivity {
    WebView web;
    HealthConnectBridge health;
    ActivityResultLauncher<Set<String>> permissionLauncher;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        Window w = getWindow();
        w.setStatusBarColor(android.graphics.Color.rgb(16,17,22));
        w.setNavigationBarColor(android.graphics.Color.rgb(16,17,22));
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
        // Android 15+ forces edge-to-edge for targetSdk 35, so the page was drawn under the
        // status bar and navigation bar. Opt in on every version and pad the WebView by the
        // system bar / cutout / keyboard insets instead.
        WindowCompat.setDecorFitsSystemWindows(w, false);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(android.graphics.Color.rgb(16,17,22));
        root.addView(web, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
        setContentView(root);
        // getInsetsController() needs the DecorView, which exists only after setContentView().
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = w.getInsetsController();
            if (c != null) c.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
        }
    }

    private void showHealthStatus() {
        // JavascriptInterface calls arrive on a background thread; Toast needs the UI thread.
        runOnUiThread(() -> Toast.makeText(this, health.isAvailable() ? "Health Connect พร้อมใช้งาน" : "ยังไม่พร้อมใช้งาน", Toast.LENGTH_SHORT).show());
    }

    public final class NativeHealthApi {
        @JavascriptInterface public String availability() { return health.isAvailable() ? "available" : "unavailable"; }
        @JavascriptInterface public void requestPermissions() {
            if (!health.isAvailable()) { showHealthStatus(); return; }
            runOnUiThread(() -> {
                try {
                    permissionLauncher.launch(health.requestedReadPermissions());
                } catch (RuntimeException e) {
                    showHealthStatus();
                }
            });
        }
        @JavascriptInterface public String bridgeVersion() { return "v89"; }
    }

    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
