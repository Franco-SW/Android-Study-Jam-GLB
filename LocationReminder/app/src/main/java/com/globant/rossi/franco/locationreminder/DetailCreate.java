package com.globant.rossi.franco.locationreminder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

public class DetailCreate extends AppCompatActivity {
    private Reminder reminder = null;
    private EditText locationET;
    private static final int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_create);

        //Get the Intent that called this activity
        Intent creatorIntent = getIntent();

        //Find all the Elements needed
        locationET = (EditText) findViewById(R.id.location);
        Button save = (Button) findViewById(R.id.save);
        Button delete = (Button) findViewById(R.id.delete);

        reminder = Reminder.getRemainderFromExtra(creatorIntent);

        if (reminder.isValid()) {
            ((EditText) findViewById(R.id.reminder_title)).setText(reminder.title);
            ((EditText) findViewById(R.id.reminder_description)).setText(reminder.description);
            locationET.setText(reminder.getPlaceIdentifier());
            //// TODO: Add all the changes to the layout and Views from being in Detail and Being in Add
        } else {
            delete.setVisibility(View.GONE);
            RelativeLayout.LayoutParams buttonLayoutParams = (RelativeLayout.LayoutParams) save.getLayoutParams();
            buttonLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            save.setLayoutParams(buttonLayoutParams);
        }

        //Listeners
        locationET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                locationFocused(v, hasFocus);
            }
        });
        locationET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlacePicker();
                //getLocation(v);
            }
        });
        //TODO: Add Save/Delete Buttons listeners
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });
    }


    //Functions for Event Listeners
    private void locationFocused(View v, boolean hasFocus) {
        if (hasFocus) {
            showPlacePicker();
        }
    }

    public void showPlacePicker() {
        try {
            PlacePicker.IntentBuilder placeIntentBuilder = new PlacePicker.IntentBuilder();
            if (reminder.isValid()) {
                LatLng startPoint = new LatLng(reminder.lat, reminder.lng);
                LatLngBounds startView = LatLngBounds.builder().include(startPoint).build();
                placeIntentBuilder.setLatLngBounds(startView);
            }
            Intent placeIntent = placeIntentBuilder.build(this);
            startActivityForResult(placeIntent, PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    //Callback executed when the Place Picker is closed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(data, this);
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            final LatLng latLng = place.getLatLng();

            reminder.placeName = name.toString();
            reminder.placeAddress = address.toString();
            reminder.lat = latLng.latitude;
            reminder.lng = latLng.longitude;

            locationET.setText(reminder.getPlaceIdentifier());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}