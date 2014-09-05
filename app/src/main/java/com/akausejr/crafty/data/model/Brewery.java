package com.akausejr.crafty.data.model;

import java.util.List;

/**
 * Data model that represents a brewery from brewerydb.com
 *
 * @author AJ Kause
 * Created on 7/7/14.
 */
public class Brewery {

    public final String id;

    public final String name;

    public final String status;

    public final String statusDisplay;

    public final String description;

    public final String established;

    public final Images images;

    public final boolean isOrganic;

    public final String website;

    public final List<SocialSite> socialAccounts;

    Brewery(String id,
           String name,
           String status,
           String statusDisplay,
           String description,
           String established,
           Images images,
           boolean isOrganic,
           String website,
           List<SocialSite> socialAccounts) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.statusDisplay = statusDisplay;
        this.description = description;
        this.established = established;
        this.images = images;
        this.isOrganic = isOrganic;
        this.website = website;
        this.socialAccounts = socialAccounts;
    }

    public static class Images {

        public final String icon;

        public final String medium;

        public final String large;

        Images(String icon, String medium, String large) {
            this.icon = icon;
            this.medium = medium;
            this.large = large;
        }
    }

    public static class SocialSite {

        public final String id;

        public final String name;

        public final String website;

        SocialSite(String id, String name, String website) {
            this.id = id;
            this.name = name;
            this.website = website;
        }
    }
}
