package com.bericotech.clavin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.bericotech.clavin.gazetteer.BasicGeoName;
import com.bericotech.clavin.gazetteer.GeoName;
import com.bericotech.clavin.resolver.ResolvedLocation;
import com.bericotech.clavin.util.TextUtils;

public class ConsoleServer {

    /**
     * Run this after installing & configuring CLAVIN to get a sense of
     * how to use it.
     *
     * @param args              not used
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        // Instantiate the CLAVIN GeoParser
        GeoParser parser = GeoParserFactory.getDefaultWithBias("./IndexDirectory");

        String line;
        while( ( line = scanner.nextLine() ) != null ) {
            if( line.equalsIgnoreCase("quit") ) {
                break;
            }

            String[] pieces = line.split( "\\|", 3 );

            double biasLatitude = Double.parseDouble( pieces[0] );
            double biasLongitude = Double.parseDouble( pieces[1] );
            String doc = pieces[2];

            List<ResolvedLocation> resolvedLocations;
            if( biasLatitude == 0.0 && biasLongitude == 0.0 ) {
                resolvedLocations = parser.parse(doc);
            } else {
                resolvedLocations = parser.parse(doc, biasLatitude, biasLongitude);
            }

            System.out.println( renderOutput( resolvedLocations ) );
        }
    }

    private static String renderOutput( List<ResolvedLocation> resolvedLocations ) {
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

        return "[ " + String.join( ", ", chunks ) + " ]";
    }
}
