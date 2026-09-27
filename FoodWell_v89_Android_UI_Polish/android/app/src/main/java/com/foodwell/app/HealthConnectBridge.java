package com.foodwell.app;

import android.content.Context;
import android.os.Build;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.PermissionController;
import androidx.health.connect.client.permission.HealthPermission;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.records.HeartRateRecord;
import java.util.HashSet;
import java.util.Set;

/** v83 Health Connect bridge. Keeps Health Connect access behind one native boundary. */
public final class HealthConnectBridge {
    private final Context context;
    private final HealthConnectClient client;

    public HealthConnectBridge(Context context) {
        this.context = context.getApplicationContext();
        this.client = HealthConnectClient.getOrCreate(this.context);
    }

    public boolean isAvailable() {
        int status = HealthConnectClient.getSdkStatus(context, "com.google.android.apps.healthdata");
        return status == HealthConnectClient.SDK_AVAILABLE;
    }

    public Set<String> requestedReadPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add(HealthPermission.getReadPermission(StepsRecord.class));
        permissions.add(HealthPermission.getReadPermission(HeartRateRecord.class));
        return permissions;
    }

    public androidx.health.connect.client.HealthConnectClient getClient() {
        return client;
    }
}
