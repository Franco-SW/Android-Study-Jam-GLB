package com.globant.rossi.franco.locationreminder;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private LocationTracker locationTracker;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationTracker = new LocationTracker(this, this);

        //Event Listeners
        FloatingActionButton addReminderFAB = (FloatingActionButton) findViewById(R.id.add_fab);
        addReminderFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReminder(v);
            }
        });
        ImageButton getLocationButton = (ImageButton) findViewById(R.id.get_location);
        getLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationTracker.stopRequest();
                getLocation();
            }
        });

        //TODO: Add logic to get the saved Reminders.
        //TODO: Add Logic to order Reminders by uID (default ordering when can't get position)

        getLocation();
    }

    public void addReminder(View v) {
        Intent intent = new Intent(this, DetailCreate.class);

        //TODO: Remove this when done. Only for TSTNG
        Reminder rem = new Reminder();
        rem.uID = 0;
        rem.title = "Tst Title";
        rem.description = "Description & Dirt";
        rem.lat=-32.9674028;
        rem.lng=-60.6468832;
        rem.placeName = "El rincon de la murga";
        rem.placeAddress = "Tst Address";
        rem.setRemainderAsExtra(intent);
        //Up to here

        startActivity(intent);
    }

    public void getLocation() {
        try {
            locationTracker.requestLocation();
        } catch (SecurityException sE) {
            boolean accessToFineLocation = (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            Toast.makeText(this, R.string.noPermisionLocationError, Toast.LENGTH_SHORT).show();
            hasPermission(accessToFineLocation);
        }
    }


    @TargetApi(23)
    private void hasPermission(boolean accessToFineLocation) {
        if (!accessToFineLocation) {
            String[] permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationTracker.requestLocation();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTracker.stopRequest();
    }

    // LocationListener methods
    //This callback is executed when the GPS returns the location
    @Override
    public void onLocationChanged(Location location) {
        //TODO: Remove Toast when done
        Toast.makeText(this, "Location: " + location.toString(), Toast.LENGTH_SHORT).show();
        //TODO: Add Ordering Logic of the Reminders here (by distance to current location)
    }

    //TODO: Remove the Toasts from this Methods when done
    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, provider + ": Provider Disabled", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, provider + ": Provider Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(this, provider + ": Provider Status Change", Toast.LENGTH_SHORT).show();
    }
}



class LocationTracker {

    private final Context mContext;
    private final LocationListener mLocationListener;

    private LocationManager locationManager;

    public LocationTracker(Context context, LocationListener locationListener) {
        mContext = context;
        mLocationListener = locationListener;
    }


    public void requestLocation() throws SecurityException {
        locationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);

        //GPS status
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        //Network status
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            showLocationDisableAlertDialog();
        } else {
            if (isGPSEnabled) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, null);
            } else {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
            }
        }
    }

    public void stopRequest() {
        try {
            locationManager.removeUpdates(mLocationListener);
        } catch (SecurityException sE) {
            sE.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}