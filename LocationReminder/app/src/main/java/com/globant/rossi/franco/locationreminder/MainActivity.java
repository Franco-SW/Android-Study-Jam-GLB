package com.globant.rossi.franco.locationreminder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private LocationTracker locationTracker;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    private static final int DETAIL_CREATION = 2;
    private static final String SAVED_REMINDERS = "SAVED_REMINDERS";
    private static final String CURRENT_REMINDER_ID = "CURRENT_REMINDER_ID";
    private List<Reminder> mReminders;

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

        LoadSavedLocations();
        getLocation();
        UpdateRemindersList();
        //TODO: Add Logic to order Reminders by uID (default ordering when can't get position)
    }

    private void LoadSavedLocations()
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String savedReminders = sharedPref.getString(SAVED_REMINDERS, "");

        if(savedReminders != "")
        {
            Gson gson = new Gson();
            Type collectionType = new TypeToken<List<Reminder>>(){}.getType();
            mReminders = gson.fromJson(savedReminders, collectionType);
        }
        else
        {
            mReminders = new ArrayList<Reminder>();
        }
    }

    public void addReminder(View v) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int currentReminderId = sharedPref.getInt(CURRENT_REMINDER_ID, 0);

        Intent intent = new Intent(this, DetailCreate.class);
        Reminder rem = new Reminder();
        rem.uID = currentReminderId;
        rem.setRemainderAsExtra(intent);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(CURRENT_REMINDER_ID, currentReminderId + 1);
        editor.commit();

        startActivityForResult(intent, DETAIL_CREATION);
    }

    public void editReminder(Reminder rem) {
        Intent intent = new Intent(this, DetailCreate.class);
        rem.setRemainderAsExtra(intent);

        startActivityForResult(intent, DETAIL_CREATION);
    }

    //Callback executed when the Place Picker is closed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == DETAIL_CREATION && resultCode == RESULT_OK) {
            Reminder reminder = Reminder.getRemainderFromExtra(data);
            if(data.getBooleanExtra("IS_SAVE", true))
            {
                AddReminder(reminder);
            }
            else
            {
                DeleteReminder(reminder);
            }

            SaveReminers();
            UpdateRemindersList();
        }
    }

    public void AddReminder(Reminder reminder)
    {
        int indexToRemove = -1;
        for(Reminder item : mReminders)
        {
            if(item.uID == reminder.uID)
            {
                indexToRemove = mReminders.indexOf(item);
            }
        }

        if(indexToRemove > -1)
        {
            mReminders.remove(indexToRemove);
        }

        mReminders.add(reminder);
    }

    public void DeleteReminder(Reminder reminder)
    {
        for(Reminder item : mReminders)
        {
            if(item.uID == reminder.uID)
            {
                mReminders.remove(mReminders.indexOf(item));
                break;
            }
        }
    }

    public void SaveReminers()
    {
        Gson gson = new Gson();
        String jsonReminders = gson.toJson(mReminders);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SAVED_REMINDERS, jsonReminders);
        editor.commit();
    }

    public void UpdateRemindersList()
    {
        ReminderListAdapter adapter = new ReminderListAdapter(this, mReminders, getLayoutInflater());
        ListView listView = (ListView) findViewById(R.id.remainder_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                editReminder(mReminders.get(position));
            }
        });
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