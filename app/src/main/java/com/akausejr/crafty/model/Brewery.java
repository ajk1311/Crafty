package com.akausejr.crafty.model;

/**
 * Data model that represents a brewery from brewerydb.com
 *
 * @author AJ Kause
 * Created on 7/7/14.
 */
public class Brewery {

    public String id;

    public String name;

    public String status;

    public String statusDisplay;

    public String description;

    public String established;

    public Images images;

    public String isOrganic;

    public String website;

    public static class Images {

        public String medium;

        public String large;

        public String icon;
    }


}