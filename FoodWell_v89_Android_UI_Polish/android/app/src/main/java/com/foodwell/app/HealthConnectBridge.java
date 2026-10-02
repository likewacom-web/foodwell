package com.foodwell.app;

import android.content.Context;
import androidx.health.connect.client.HealthConnectClient;
import java.util.HashSet;
import java.util.Set;

/** v83 Health Connect bridge. Keeps Health Connect access behind one native boundary. */
public final class HealthConnectBridge {
    private final Context context;
    private HealthConnectClient client;

    public HealthConnectBridge(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean isAvailable() {
        try {
            int status = HealthConnectClient.getSdkStatus(context, "com.google.android.apps.healthdata");
            return status == HealthConnectClient.SDK_AVAILABLE;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public Set<String> requestedReadPermissions() {
        Set<String> permissions = new HashSet<>();
        // Same strings HealthPermission.getReadPermission() returns; avoids Kotlin KClass interop from Java.
        permissions.add("android.permission.health.READ_STEPS");
        permissions.add("android.permission.health.READ_HEART_RATE");
        return permissions;
    }

    /** Returns null when Health Connect is not installed/available; getOrCreate() throws in that case. */
    public HealthConnectClient getClient() {
        if (client == null && isAvailable()) {
            try {
                client = HealthConnectClient.getOrCreate(context);
            } catch (RuntimeException e) {
                client = null;
            }
        }
        return client;
    }
}
