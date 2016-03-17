package com.globant.rossi.franco.locationreminder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import java.util.List;

public class LocationTracker {
    public static final int MINIMUM_MINUTES_DIFFERENCE = 15 * 6 * 1000; //1.5Min
    public static final int MINIMUM_METERS_DIFFERENCE = 5; //5m

    private Context mContext;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    private final android.os.Handler handler = new android.os.Handler();
    private final Runnable stopRequest = new Runnable() {
        public void run() {
            stopRequest();
        }
    };

    public LocationTracker(Context context, LocationListener locationListener) {
        mContext = context;
        mLocationListener = locationListener;
    }

    public Location requestLocation() throws SecurityException {
        mLocationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);

        Location bestLocation = null;

        //GPS status
        boolean isGPSEnabled = mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        //Network status
        boolean isNetworkEnabled = mLocationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            showLocationDisableAlertDialog();
        } else {
            //Check if there are providers. If there are none it means we have no permissions.
            if (mLocationManager.getProviders(false).isEmpty()) {
                throw new SecurityException();
            }

            List<String> locationProviders = mLocationManager.getProviders(true);
            for (String provider : locationProviders) {
                Location providerLocation = mLocationManager.getLastKnownLocation(provider);
                if (bestLocation == null || isBetterThan(providerLocation, bestLocation)) {
                    bestLocation = providerLocation;
                }
                mLocationManager.requestSingleUpdate(provider, mLocationListener, null);
            }
        }
        handler.removeCallbacks(stopRequest);
        handler.postDelayed(stopRequest, MINIMUM_MINUTES_DIFFERENCE * 2);
        return bestLocation;
    }

    public static boolean isBetterThan(Location newLocation, Location previousLocation) {
        boolean response = newLocation != null && previousLocation != null;
        if (response) {
            boolean isMoreRecent = newLocation.getTime() - previousLocation.getTime() > MINIMUM_MINUTES_DIFFERENCE;
            boolean isMoreAccurate = newLocation.hasAccuracy() && previousLocation.hasAccuracy() &&
                    previousLocation.getAccuracy() - newLocation.getAccuracy() > MINIMUM_METERS_DIFFERENCE;
            boolean hasAccuracy = newLocation.hasAccuracy() && !previousLocation.hasAccuracy();
            response = isMoreRecent || isMoreAccurate || hasAccuracy;
        }
        return response;
    }

    public void showLocationDisableAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle(R.string.not_enabled_location_title);

        alertDialog
                .setMessage(R.string.not_enabled_location_message);

        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    public void stopRequest() {
        try {
            mLocationManager.removeUpdates(mLocationListener);
        } catch (SecurityException sE) {
            sE.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}