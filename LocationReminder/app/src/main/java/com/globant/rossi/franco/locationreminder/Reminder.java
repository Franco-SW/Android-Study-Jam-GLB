package com.globant.rossi.franco.locationreminder;

import android.content.Intent;
import android.location.Location;

public class Reminder {
    public static final int NO_UID = -1;

    public int uID;
    public String title;
    public String description;
    public double lat;
    public double lng;
    public String placeName;
    public String placeAddress;

    public Reminder() {
        uID = NO_UID;
        title = "";
        description = "";
        lat = 0;
        lng = 0;
        placeName = "";
        placeAddress = "";
    }


    @Override
    public boolean equals(Object o) {
        return super.equals(o) ||
                (o instanceof Reminder && (((Reminder) o).uID == this.uID));
    }

    public boolean isValid() {
        return (uID != NO_UID);
    }

    public String getPlaceIdentifier() {
        String result = "";
        boolean flag = false;

        if (placeName != null && !placeName.trim().equals("")) {
            result = placeName;
            flag = true;
        }

        if (placeAddress != null && !placeAddress.trim().equals("")) {
            if (flag && !placeAddress.contains(placeName.trim())) {
                result = result + " - " + placeAddress;
            } else {
                result = placeAddress;
                flag = true;
            }
        }

        if (flag) {
            result = result + " - ";
        }

        result = result + "(" + lat + " , " + lng + ")";

        return result;
    }

    public void setPlace(String placeName, String placeAddress, double lat, double lng) {
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.lat = lat;
        this.lng = lng;
    }

    public double distanceToLocationSquared(Location location) {
        double result;
        if (location != null) {
            result = Math.pow(location.getLatitude() - lat, 2) +
                    Math.pow(location.getLongitude() - lng, 2);
        } else {
            result = lat * lat + lng * lng;
        }
        return result;
    }

    public void setRemainderAsExtra(Intent intent) {
        intent.putExtra("UID", uID);
        intent.putExtra("Title", title);
        intent.putExtra("Description", description);
        intent.putExtra("Lat", lat);
        intent.putExtra("Lng", lng);
        intent.putExtra("PlaceName", placeName);
        intent.putExtra("PlaceAddress", placeAddress);
    }

    public static Reminder getRemainderFromExtra(Intent intent) {
        Reminder reminder = new Reminder();

        reminder.uID = intent.getIntExtra("UID", NO_UID);
        reminder.title = intent.getStringExtra("Title");
        reminder.description = intent.getStringExtra("Description");
        reminder.lat = intent.getDoubleExtra("Lat", 0);
        reminder.lng = intent.getDoubleExtra("Lng", 0);
        reminder.placeName = intent.getStringExtra("PlaceName");
        reminder.placeAddress = intent.getStringExtra("PlaceAddress");

        return reminder;
    }
}
