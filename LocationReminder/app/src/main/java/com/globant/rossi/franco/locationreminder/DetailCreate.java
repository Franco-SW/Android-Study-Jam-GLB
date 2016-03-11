package com.globant.rossi.franco.locationreminder;

import android.content.res.ColorStateList;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

public class DetailCreate extends AppCompatActivity {
//    ColorStateList locationIconColor;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_create);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        EditText locationET = (EditText) findViewById(R.id.location);
//        locationIconColor = locationET.getCompoundDrawableTintList();
        locationET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                locationFocused(v, hasFocus);
            }
        });
        locationET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(v);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    private void locationFocused(View v, boolean hasFocus) {
        if (hasFocus) {
            getLocation(v);
//            ((EditText) v).setCompoundDrawableTintList(ColorStateList.valueOf(getColor(R.color.colorAccent)));
        }
/*
        else{
            ((EditText) v).setCompoundDrawableTintList(locationIconColor);
        }
*/
    }

    public void getLocation(View v) {
//        Toast.makeText(DetailCreate.this, "Get Location", Toast.LENGTH_SHORT).show();

    }
}
