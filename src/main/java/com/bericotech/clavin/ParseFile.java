package com.bericotech.clavin;

import com.bericotech.clavin.resolver.OutputEntry;
import com.bericotech.clavin.resolver.ResolvedLocation;
import com.bericotech.clavin.util.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ParseFile {

    /**
     * Run this after installing & configuring CLAVIN to get a sense of
     * how to use it.
     *
     * @param args              not used
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        String inputFilename = args[0];
        double latitude = args[1] != null ? Double.parseDouble(args[1]) : 0.0;
        double longitude = args[1] != null ? Double.parseDouble(args[2]) : 0.0;

        // Instantiate the CLAVIN GeoParser
        GeoParser parser = GeoParserFactory.getDefaultWithBias("./IndexDirectory");

        // Unstructured text file about Somalia to be geoparsed
        File inputFile = new File(inputFilename);

        // Grab the contents of the text file as a String
        String inputString = TextUtils.fileToString(inputFile);

        // Parse location names in the text into geographic entities
        List<ResolvedLocation> resolvedLocations = parser.parse(inputString, latitude, longitude);

        // Display the ResolvedLocations found for the location names
        List<OutputEntry> results = new ArrayList<>();

        for( ResolvedLocation resolvedLocation : resolvedLocations ) {
            OutputEntry oe = OutputEntry.fromGeoName(resolvedLocation.getGeoname());
            oe.score = resolvedLocation.getConfidence();
            results.add(oe);
        }

        HashMap<Integer,OutputEntry> map = new HashMap<>();
        for( OutputEntry oe : results ) {
            if( ! map.containsKey( oe.id ) ) {
                map.put(oe.id, oe);
            }
        }

        List<OutputEntry> sortedResults = new ArrayList<>( map.values() );

        Collections.sort(sortedResults,OutputEntry.ScoreComparitor);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String output = gson.toJson( sortedResults );
        System.out.println( output );
    }
}
