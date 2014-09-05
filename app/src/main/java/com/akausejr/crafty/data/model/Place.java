package com.akausejr.crafty.data.model;

/**
 * Represents a place on Earth in Google's Places database
 *
 * @author AJ Kause
 * Created on 9/2/14.
 */
public class Place {

    public final String id;

    public final String name;

    Place(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
