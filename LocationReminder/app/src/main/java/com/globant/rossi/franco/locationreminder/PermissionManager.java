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

    public static boolean checkAllPermissionsAreGranted(Context context, String[] permissions){
        String[] permissionsToRequest = checkPermissionsNotGranted(context, permissions);
        return (permissionsToRequest.length == 0);
    }

    public static boolean CheckAndRequestPermissions(Activity activity, String[] permissions, int requestCode) {
        Context context = activity.getApplicationContext();
        String[] permissionsToRequest;
        permissionsToRequest = checkPermissionsNotGranted(context, permissions);
        if(permissionsToRequest.length > 0) {
            requestPermissions(activity, permissionsToRequest, requestCode);
            return false;
        }
        return true;
    }

    public static String[] checkPermissionsNotGranted(Context context, String[] permissions){
        List<String> permissionsNotGranted = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNotGranted.add(permission);
            }
        }
        return permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]);
    }

    public static void requestPermissions(Activity activity, String[] permissionsToRequest, int requestCode) {
        Context context = activity.getApplicationContext();
        String message = context.getString(R.string.no_permission_error);

        for (String permission : permissionsToRequest) {
            switch (permission) {
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    message = message + context.getString(R.string.ACCESS_FINE_LOCATION);
                    break;
                case Manifest.permission.ACCESS_NETWORK_STATE:
                    message = message + context.getString(R.string.INTERNET);
                    break;
                case Manifest.permission.INTERNET:
                    message = message + context.getString(R.string.INTERNET);
                    break;
            }
            message = message + ", ";
        }

        int lastCommaIndex = message.lastIndexOf(", ");
        if (lastCommaIndex >= 0) {
            message = message.substring(0, lastCommaIndex);
        }

        lastCommaIndex = message.lastIndexOf(", ");
        if (lastCommaIndex >= 0) {
            message = message.substring(0, lastCommaIndex) +
                    context.getString(R.string.no_permission_error_and) +
                    message.substring(lastCommaIndex + 2);
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        makePermissionsRequest(activity, permissionsToRequest, requestCode);
    }

    @TargetApi(23)
    private static void makePermissionsRequest(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public static boolean checkPermissionsGranted(int[] grantResults) {
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