package com.akausejr.crafty.data.model;

/**
 * Represents a beer that a brewery offers
 *
 * @author AJ Kause
 * Created on 7/24/14.
 */
public class Beer {

    public final String id;

    public final String name;

    public final String description;

    public final double abv;

    public final boolean isOrganic;

    public final Style style;

    public final Labels labels;

    public final Availability available;

    Beer(String id,
         String name,
         String description,
         double abv,
         boolean isOrganic,
         Style style,
         Labels labels,
         Availability available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.abv = abv;
        this.isOrganic = isOrganic;
        this.style = style;
        this.labels = labels;
        this.available = available;
    }

    public static class Style {

        public final String id;

        public final String name;

        public final String description;

        Style(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }

    public static class Labels {

        public final String icon;

        public final String medium;

        public final String large;

        Labels(String icon, String medium, String large) {
            this.icon = icon;
            this.medium = medium;
            this.large = large;
        }
    }

    public static class Availability {

        public final String id;

        public final String name;

        public final String description;

        Availability(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }
}
