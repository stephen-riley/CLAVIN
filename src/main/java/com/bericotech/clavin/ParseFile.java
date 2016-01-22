package com.bericotech.clavin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bericotech.clavin.gazetteer.BasicGeoName;
import com.bericotech.clavin.gazetteer.GeoName;
import com.bericotech.clavin.resolver.ResolvedLocation;
import com.bericotech.clavin.util.TextUtils;

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
        System.out.println( "[" );
        List<String> chunks = new ArrayList<String>();

        for (ResolvedLocation resolvedLocation : resolvedLocations) {
            StringBuilder sb = new StringBuilder();
            GeoName gn = resolvedLocation.getGeoname();
            sb
                    .append("{ ")
                    .append("\"name\": \"")
                    .append(gn.getName())
                    .append( "\", " )
                    .append("\"description\": \"")
                    .append(gn.toString())
                    .append( "\", " )
                    .append("\"type\": \"")
                    .append(gn.getFeatureClass())
                    .append( "\", " )
                    .append("\"subtype\": \"")
                    .append(gn.getFeatureCode())
                    .append( "\", " )
                    .append("\"latitude\": ")
                    .append(gn.getLatitude())
                    .append( ", " )
                    .append("\"longitude\": ")
                    .append(gn.getLongitude())
                    .append(" }");
            chunks.add(sb.toString());
        }

        System.out.println( String.join(",\n",chunks));

        System.out.println("]");
    }
}
