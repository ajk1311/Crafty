package com.akausejr.crafty.data.model;

import android.text.TextUtils;

import com.akausejr.crafty.R;

/**
 * Data model that represents a mapped location from brewerydb.com
 *
 * @author AJ Kause
 * Created on 7/7/14.
 */
public class BreweryLocation {

    public interface Type {
        public static final String ALL_TYPES = "";
        public static final String NANO_BREWERY = "nano";
        public static final String OFFICE = "office";
        public static final String TASTING_ROOM = "tasting";
        public static final String MICRO_BREWERY = "micro";
        public static final String PRODUCTION_FACILITY = "production";
        public static final String RESTAURANT = "restaurant";
        public static final String MACRO_BREWERY = "macro";
        public static final String BREWPUB = "brewpub";
    }

    public static int getColorResIdForType(String type) {
        switch (type) {
            case Type.NANO_BREWERY: return R.color.nano_brewery;
            case Type.OFFICE: return R.color.office;
            case Type.TASTING_ROOM: return R.color.tasting_room;
            case Type.MICRO_BREWERY: return R.color.micro_brewery;
            case Type.PRODUCTION_FACILITY: return R.color.production_facility;
            case Type.RESTAURANT: return R.color.restaurant_ale_house;
            case Type.MACRO_BREWERY: return R.color.macro_brewery;
            case Type.BREWPUB: return R.color.brewpub;
            default: return android.R.color.black;
        }
    }

    public static int getMarkerResIdForType(String type) {
        switch (type) {
            case Type.NANO_BREWERY: return R.drawable.ic_marker_light_green;
            case Type.OFFICE: return R.drawable.ic_marker_blue_grey;
            case Type.TASTING_ROOM: return R.drawable.ic_marker_yellow;
            case Type.MICRO_BREWERY: return R.drawable.ic_marker_deep_purple;
            case Type.PRODUCTION_FACILITY: return R.drawable.ic_marker_deep_orange;
            case Type.RESTAURANT: return R.drawable.ic_marker_blue;
            case Type.MACRO_BREWERY: return R.drawable.ic_marker_red;
            case Type.BREWPUB: return R.drawable.ic_marker_light_blue;
            default: return 0;
        }
    }

    public final String id;

    public final String status;

    public final String locationType;

    public final String locationTypeDisplay;

    public final double latitude;

    public final double longitude;

    public final String streetAddress;

    public final String locality;

    public final String region;

    public final String postalCode;

    public final String phone;

    public final String breweryId;

    public final Brewery brewery;

    BreweryLocation(String id,
                    String status,
                    String locationType,
                    String locationTypeDisplay,
                    double latitude,
                    double longitude,
                    String streetAddress,
                    String locality,
                    String region,
                    String postalCode,
                    String phone,
                    String breweryId,
                    Brewery brewery) {
        this.id = id;
        this.status = status;
        this.locationType = locationType;
        this.locationTypeDisplay = locationTypeDisplay;
        this.latitude = latitude;
        this.longitude = longitude;
        this.streetAddress = streetAddress;
        this.locality = locality;
        this.region = region;
        this.postalCode = postalCode;
        this.phone = phone;
        this.breweryId = breweryId;
        this.brewery = brewery;
    }

    public String getDisplayAddress() {
        StringBuilder address = new StringBuilder();
        if (!TextUtils.isEmpty(streetAddress)) {
            address.append(streetAddress).append('\n');
        }
        boolean hasLocality = false;
        if (!TextUtils.isEmpty(locality)) {
            hasLocality = true;
            address.append(locality);
        }
        boolean hasRegion = false;
        if (!TextUtils.isEmpty(region)) {
            hasRegion = true;
            if (hasLocality) {
                address.append(", ");
            }
            address.append(region);
        }
        if (!TextUtils.isEmpty(postalCode)) {
            if (hasRegion) {
                address.append(' ');
            } else if (address.length() > 0) {
                address.append(", ");
            }
            address.append(postalCode);
        }
        return address.toString();
    }
}
