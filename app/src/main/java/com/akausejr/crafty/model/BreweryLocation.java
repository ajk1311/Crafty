package com.akausejr.crafty.model;

import android.text.TextUtils;

/**
 * Data model that represents a mapped location from brewerydb.com
 *
 * @author AJ Kause
 * Created on 7/7/14.
 */
public class BreweryLocation {

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
            if (hasLocality && !hasRegion) {
                address.append(", ");
            }
            address.append(postalCode);
        }
        return address.toString();
    }
}
