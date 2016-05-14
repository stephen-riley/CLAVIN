package com.bericotech.clavin.resolver;

/**
 * Created by administrator on 1/31/16.
 */

import com.bericotech.clavin.gazetteer.GeoName;

import java.util.Comparator;

public class OutputEntry {
    public OutputEntry() { }
    public int id;
    public String name;
    public String description;
    public OutputEntry parent;
    public String featureClass;
    public String featureCode;
    public double score;
    public double latitude;
    public double longitude;

    public static OutputEntry fromGeoName( GeoName geoname ) {
        OutputEntry oe = new OutputEntry();
        oe.id = geoname.getGeonameID();
        oe.name = geoname.getName();
        oe.description = geoname.toString();
        oe.featureClass = geoname.getFeatureClass().toString();
        oe.featureCode = geoname.getFeatureCode().toString();
        oe.latitude = geoname.getLatitude();
        oe.longitude = geoname.getLongitude();
        if( geoname.getParent() != null ) {
            oe.parent = OutputEntry.fromGeoName(geoname.getParent());
        }
        return oe;
    }

    public static Comparator<OutputEntry> ScoreComparitor = new Comparator<OutputEntry>() {

        public int compare(OutputEntry s1, OutputEntry s2) {
            // sort by confidence, descending
            return Double.compare(s2.score, s1.score);
        }
    };
}