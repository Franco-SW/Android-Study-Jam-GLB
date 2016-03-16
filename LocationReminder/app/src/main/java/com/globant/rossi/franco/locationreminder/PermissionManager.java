package com.globant.rossi.franco.locationreminder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionManager {

    public static void CheckAndRequestPermissions(Activity activity, String[] permissions, int requestCode) {
        Context context = activity.getApplicationContext();
        List<String> permissionsToRequest = new ArrayList<>(Arrays.asList(permissions));
        String message = context.getString(R.string.no_permission_error);
        boolean previous = false;

        for (int i = 0; i < permissionsToRequest.size(); i++) {
            if (ContextCompat.checkSelfPermission(context, permissionsToRequest.get(i))
                    == PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.remove(i);
            } else {
                if (previous) {
                    message = message + ", ";
                }

                switch (permissionsToRequest.get(i)) {
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        message = message + context.getString(R.string.no_permission_error);
                        break;
                }

                previous = true;
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            int lastCommaIndex = message.lastIndexOf(", ");

            if (lastCommaIndex >= 0) {
                message = message.substring(0, lastCommaIndex) +
                        context.getString(R.string.no_permission_error_and) +
                        message.substring(lastCommaIndex + 2);
            }

            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            askPermission(activity, permissionsToRequest, requestCode);
        }
    }

    @TargetApi(23)
    private static void askPermission(Activity activity, List<String> permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                permissions.toArray(new String[permissions.size()]), requestCode);
    }

    public static boolean checkGrantResults(int[] grantResults) {
        if (grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}