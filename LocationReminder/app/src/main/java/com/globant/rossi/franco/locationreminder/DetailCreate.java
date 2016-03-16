package com.globant.rossi.franco.locationreminder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.security.Provider;

public class DetailCreate extends AppCompatActivity {
    public static final String IS_SAVE = "IS_SAVE";
    private static final int PLACE_PICKER_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 3;

    private EditText descriptionEditText;
    private EditText locationEditText;
    private EditText titleEditText;
    private Reminder reminder;

    //TODO: Add Permission Requests for Place Picker (INTERNET, FINE_LOCATION, READ_GSERVICES)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_create);

        //Get the Intent that called this activity
        Intent creatorIntent = getIntent();

        //Find all the Elements needed
        descriptionEditText = (EditText) findViewById(R.id.reminder_description);
        locationEditText = (EditText) findViewById(R.id.reminder_location);
        titleEditText = (EditText) findViewById(R.id.reminder_title);
        Button saveButton = (Button) findViewById(R.id.save);
        Button deleteButton = (Button) findViewById(R.id.delete);

        reminder = Reminder.getRemainderFromExtra(creatorIntent);
        if (reminder.isValid()) {
            titleEditText.setText(reminder.title);
            descriptionEditText.setText(reminder.description);
            locationEditText.setText(reminder.getPlaceIdentifier());
        } else {
            deleteButton.setVisibility(View.GONE);
            RelativeLayout.LayoutParams buttonLayoutParams = (RelativeLayout.LayoutParams) saveButton.getLayoutParams();
            buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            saveButton.setLayoutParams(buttonLayoutParams);
        }

        //Listeners
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        locationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                locationFocused(hasFocus);
            }
        });
        locationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlacePicker();
            }
        });
    }

    private void delete(){
        getIntent().putExtra(IS_SAVE, false);
        closeActivity();
    }

    private void save(){
        if(validateFieldsAndShowErrors()) {
            updateReminder();
            getIntent().putExtra(IS_SAVE, true);
            reminder.setRemainderAsExtra(getIntent());
            closeActivity();
        }
    }

    private boolean validateFieldsAndShowErrors(){
        //TODO: Add all validations
        return true;
    }

    private void updateReminder() {
        reminder.title = titleEditText.getText().toString();
        reminder.description = descriptionEditText.getText().toString();
    }

    private void closeActivity(){
        setResult(RESULT_OK, getIntent());
        finish();
    }

    private void locationFocused(boolean hasFocus) {
        if (hasFocus) {
            showPlacePicker();
        }
    }

    public void showPlacePicker() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};

        PermissionManager.CheckAndRequestPermissions(this, permissions, PERMISSION_REQUEST_CODE);

        try {
            PlacePicker.IntentBuilder placeIntentBuilder = new PlacePicker.IntentBuilder();
            if (reminder.isValid()) {
                LatLng startPoint = new LatLng(reminder.lat, reminder.lng);
                LatLngBounds startView = LatLngBounds.builder().include(startPoint).build();
                placeIntentBuilder.setLatLngBounds(startView);
            }
            Intent placeIntent = placeIntentBuilder.build(this);
            startActivityForResult(placeIntent, PLACE_PICKER_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    //Callback executed when the Place Picker is closed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(data, this);
            CharSequence name = place.getName();
            CharSequence address = place.getAddress();
            LatLng latLng = place.getLatLng();
            reminder.setPlace(name.toString(), address.toString(),
                    latLng.latitude, latLng.longitude);
            locationEditText.setText(reminder.getPlaceIdentifier());
        }
    }
}