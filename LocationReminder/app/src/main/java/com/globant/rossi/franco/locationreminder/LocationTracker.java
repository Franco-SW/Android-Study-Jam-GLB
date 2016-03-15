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

/**
 * Created by Franco on 13/03/2016.
 */
public class LocationTracker {
    private Context mContext;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    public LocationTracker(Context context, LocationListener locationListener) {
        mContext = context;
        mLocationListener = locationListener;
    }

    public Location requestLocation() throws SecurityException {
        mLocationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);

        //GPS status
        boolean isGPSEnabled = mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        //Network status
        boolean isNetworkEnabled = mLocationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            showLocationDisableAlertDialog();
        } else {
            List<String> providers = mLocationManager.getProviders(true);
            Location lastLocation = null;
            for (String provider:providers){
                Location providerLocation = mLocationManager.getLastKnownLocation(provider);
                if(lastLocation==null || isBetterThan(providerLocation, lastLocation)){
                   lastLocation = providerLocation;
                }
                mLocationManager.requestSingleUpdate(provider, mLocationListener, null);
            }
            return lastLocation;
        }
        return null;
    }

    private boolean isBetterThan(Location locationLH, Location locationRH){
        boolean response = locationLH != null && locationRH !=null;
        if(response) {
            boolean isMoreRecent = locationLH.getTime() - locationRH.getTime() > MainActivity.MINIMUM_MINUTES;
            boolean isMoreAccurate = locationLH.hasAccuracy() && locationRH.hasAccuracy() &&
                    locationLH.getAccuracy() < locationRH.getAccuracy();
            boolean hasAccuracy = locationLH.hasAccuracy() && !locationRH.hasAccuracy();
            response = isMoreRecent || isMoreAccurate || hasAccuracy;
        }
        return response;
    }

    public void showLocationDisableAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle(R.string.notEnabledLocationTitle);

        alertDialog
                .setMessage(R.string.notEnabledLocationMessage);

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