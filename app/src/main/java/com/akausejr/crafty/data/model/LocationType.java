package com.akausejr.crafty.data.model;

import android.content.Context;

import com.akausejr.crafty.R;

import java.util.Arrays;
import java.util.List;

/**
 * Data model representing a brewery location type
 *
 * @author AJ Kause
 * Created on 7/17/14.
 */
public class LocationType {

    public final String type;
    public final String display;
    public final int color;

    public LocationType(String type, String display, int color) {
        this.type = type;
        this.display = display;
        this.color = color;
    }

    @Override
    public String toString() {
        return display;
    }

    public static List<LocationType> list(Context context) {
        return Arrays.asList(
            new LocationType(
                BreweryLocation.Type.ALL_TYPES,
                context.getString(R.string.all_types),
                context.getResources().getColor(R.color.all_types)),
            new LocationType(
                BreweryLocation.Type.NANO_BREWERY,
                context.getString(R.string.nano_brewery),
                context.getResources().getColor(R.color.nano_brewery)),
            new LocationType(
                BreweryLocation.Type.OFFICE,
                context.getString(R.string.office),
                context.getResources().getColor(R.color.office)),
            new LocationType(
                BreweryLocation.Type.TASTING_ROOM,
                context.getString(R.string.tasting_room),
                context.getResources().getColor(R.color.tasting_room)),
            new LocationType(
                BreweryLocation.Type.MICRO_BREWERY,
                context.getString(R.string.micro_brewery),
                context.getResources().getColor(R.color.micro_brewery)),
            new LocationType(
                BreweryLocation.Type.PRODUCTION_FACILITY,
                context.getString(R.string.production_facility),
                context.getResources().getColor(R.color.production_facility)),
            new LocationType(
                BreweryLocation.Type.RESTAURANT,
                context.getString(R.string.restaurant_ale_house),
                context.getResources().getColor(R.color.restaurant_ale_house)),
            new LocationType(
                BreweryLocation.Type.MACRO_BREWERY,
                context.getString(R.string.macro_breweries),
                context.getResources().getColor(R.color.macro_brewery)),
            new LocationType(
                BreweryLocation.Type.BREWPUB,
                context.getString(R.string.brewpub),
                context.getResources().getColor(R.color.brewpub))
            );
    }
}
