package com.example.sportcenterapp.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public final class PermissionHelper {
    private PermissionHelper() {}

    public static void ensureImagePermission(Activity activity) {
        String perm = (Build.VERSION.SDK_INT >= 33)
                ? android.Manifest.permission.READ_MEDIA_IMAGES
                : android.Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(activity, perm)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{ perm }, 1001);
        }
    }
}
