package com.akausejr.crafty.data.model;

import java.util.List;

/**
 * Represents the predictions Google Place API returns for a query
 *
 * @author AJ Kause
 * Created on 9/2/14.
 */
public class PlacesPrediction {

    public final List<Place> predictions;

    public PlacesPrediction(List<Place> predictions) {
        this.predictions = predictions;
    }
}
