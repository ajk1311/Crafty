package com.akausejr.crafty.model;

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

    public String id;

    public String name;

    public String status;

    public String statusDisplay;

    public String locationType;

    public String locationTypeDisplay;

    public double latitude;

    public double longitude;

    public String streetAddress;

    public String locality;

    public String region;

    public String postalCode;

    public String countryIsoCode;

    public String phone;

    public String breweryId;

    public Brewery brewery;

    public String website;

    public String isPrimary;

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
