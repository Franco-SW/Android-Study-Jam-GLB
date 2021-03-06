package com.globant.rossi.franco.locationreminder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final int DETAIL_CREATION_REQUEST_CODE = 2;
    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final String CURRENT_REMINDER_ID = "CURRENT_REMINDER_ID";
    private static final String SAVED_REMINDERS = "SAVED_REMINDERS";

    private Location lastLocation;
    private LocationTracker locationTracker;
    private List<Reminder> mRemindersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationTracker = new LocationTracker(this, this);

        loadRemindersList();
        getLocation();
        updateRemindersListView();

        //Event Listeners
        FloatingActionButton addReminderFAB = (FloatingActionButton) findViewById(R.id.add_fab);
        addReminderFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReminder();
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
    }

    private void loadRemindersList() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String savedReminders = sharedPref.getString(SAVED_REMINDERS, "");

        if (savedReminders != "") {
            Gson gson = new Gson();
            Type collectionType = new TypeToken<List<Reminder>>() {
            }.getType();
            mRemindersList = gson.fromJson(savedReminders, collectionType);
        } else {
            mRemindersList = new ArrayList<Reminder>();
        }
    }

    private void getLocation() {
        try {
            Location location = locationTracker.requestLocation();

            if (isBetterThanLastLocation(location)) {
                lastLocation = location;
            }
        } catch (SecurityException sE) {
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            PermissionManager.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean isBetterThanLastLocation(Location location) {
        return LocationTracker.isBetterThanProviderBased(location, lastLocation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE &&
                PermissionManager.checkPermissionsGranted(grantResults)) {
            getLocation();
        }
    }

    private void updateRemindersListView() {
        ReminderListAdapter adapter = new ReminderListAdapter(this, mRemindersList, getLayoutInflater(), this);
        ListView listView = (ListView) findViewById(R.id.remainder_list);
        adapter.setListView(listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                detailReminder(mRemindersList.get(position));
            }
        });
    }

    private void createReminder() {
        Intent intent = new Intent(this, DetailCreate.class);
        startActivityForResult(intent, DETAIL_CREATION_REQUEST_CODE);
    }

    private void detailReminder(Reminder reminder) {
        Intent intent = new Intent(this, DetailCreate.class);
        reminder.setRemainderAsExtra(intent);
        startActivityForResult(intent, DETAIL_CREATION_REQUEST_CODE);
    }

    //Callback executed when DetailCreate is closed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DETAIL_CREATION_REQUEST_CODE && resultCode == RESULT_OK) {
            Reminder reminder = Reminder.getRemainderFromExtra(data);
            if (data.getBooleanExtra(DetailCreate.IS_SAVE, true)) {
                addOrUpdateReminder(reminder);
            } else {
                deleteReminder(reminder);
            }
        }
    }

    private void addOrUpdateReminder(Reminder reminder) {
        if (reminder.isValid()) {
            int reminderIndex = mRemindersList.indexOf(reminder);
            if (reminderIndex >= 0) {
                mRemindersList.set(reminderIndex, reminder);
            }
        } else {
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            int currentReminderId = sharedPref.getInt(CURRENT_REMINDER_ID, 0);
            reminder.uID = currentReminderId;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(CURRENT_REMINDER_ID, currentReminderId + 1);
            editor.commit();
            mRemindersList.add(reminder);
        }
        sortRemainderList();
        saveRemindersList();
        updateRemindersListView();
    }

    public void deleteReminder(Reminder reminder) {
        int reminderIndex = mRemindersList.indexOf(reminder);
        if (reminderIndex >= 0) {
            mRemindersList.remove(reminderIndex);
            saveRemindersList();
            updateRemindersListView();
        }
    }

    private void sortRemainderList() {
        Comparator<Reminder> comparator = new Comparator<Reminder>() {
            @Override
            public int compare(Reminder r1, Reminder r2) {
                double result = r1.distanceToLocationSquared(lastLocation) - r2.distanceToLocationSquared(lastLocation);

                if (result > 0) {
                    return 1;
                } else if (result < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };

        Collections.sort(mRemindersList, comparator);
    }

    private void saveRemindersList() {
        Gson gson = new Gson();
        String jsonRemindersList = gson.toJson(mRemindersList);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SAVED_REMINDERS, jsonRemindersList);
        editor.commit();
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
        if (isBetterThanLastLocation(location)) {
            lastLocation = location;
            sortRemainderList();
            updateRemindersListView();
            saveRemindersList();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}