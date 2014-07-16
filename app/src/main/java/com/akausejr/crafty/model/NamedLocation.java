package com.akausejr.crafty.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Data model that represents the user's device's location at a given point in time
 *
 * @author AJ Kause
 * Created on 7/7/14.
 */
public class NamedLocation implements Parcelable {

    private final double lat;

    private final double lng;

    private final String name;

    public NamedLocation(double lat, double lng, String name) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public NamedLocation(Location location, String name) {
        this(location.getLatitude(), location.getLongitude(), name);
    }

    public NamedLocation(LatLng location, String name) {
        this(location.latitude, location.longitude, name);
    }

    public String getName() {
        return this.name;
    }

    public double latitude() {
        return this.lat;
    }

    public double longitude() {
        return this.lng;
    }

    public LatLng toLatLng() {
        return new LatLng(this.lat, this.lng);
    }

    /* Parcelable implementation */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dst, int i) {
        dst.writeDouble(this.lat);
        dst.writeDouble(this.lng);
        dst.writeString(this.name);
    }

    private NamedLocation(Parcel src) {
        this.lat = src.readDouble();
        this.lng = src.readDouble();
        this.name = src.readString();
    }

    public static final Creator<NamedLocation> CREATOR = new Creator<NamedLocation>() {
        @Override
        public NamedLocation createFromParcel(Parcel parcel) {
            return new NamedLocation(parcel);
        }

        @Override
        public NamedLocation[] newArray(int size) {
            return new NamedLocation[size];
        }
    };
}
