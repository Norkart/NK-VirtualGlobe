/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  ApplicationUtils.java
 *
 * Created on 9. juni 2008, 09:31
 *
 */

package com.norkart.virtualglobe.util;

import java.net.URL;
import java.net.MalformedURLException;

/**
 *
 * @author runaas
 */
public class ApplicationUtils {
    
    static public URL extractDataset(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].indexOf("-dataset=") == 0) {
                try {
                    return new URL(args[i].substring("-dataset=".length()));
                } catch (MalformedURLException ex) {}
            }
        }
        return null;
    }
    
    static public double[] extractViewpoint(String value) {
        double[] v = new double[5];
        v[4] = -90;
        v[2] =12000000;
        
        String[] pos_str_arr = value.split(",");
        if (pos_str_arr.length > 0) {
            // Lon
            try {
                v[0] = Math.toRadians(Double.parseDouble(pos_str_arr[0]));
            } catch (NumberFormatException ex) {}
        }
        if (pos_str_arr.length > 1) {
            // Lat
            try {
                v[1] = Math.toRadians(Double.parseDouble(pos_str_arr[1]));
            } catch (NumberFormatException ex) {}
        }
        if (pos_str_arr.length > 2) {
            // height
            try {
                v[2] = Double.parseDouble(pos_str_arr[2]);
            } catch (NumberFormatException ex) {}
        }
        if (pos_str_arr.length > 3) {
            // Azimuth
            try {
                v[3] = Math.toRadians(Double.parseDouble(pos_str_arr[3]));
            } catch (NumberFormatException ex) {}
        }
        if (pos_str_arr.length > 4) {
            // Elevation angle
            try {
                v[4] = Math.toRadians(Double.parseDouble(pos_str_arr[4]));
            } catch (NumberFormatException ex) {}
        }
        return v;
    }
    
    static public double[] extractViewpoint(String[] args) {
        // Parse argument list
        
        for (int i = 0; i < args.length; ++i) {
            if (args[i].indexOf("-viewpoint=") == 0) {
                return extractViewpoint(args[i].substring("-viewpoint=".length()));
            }
        }
        return null;
    }
    
    static public double[] extractLookat(String value) {
        String[] pos_str_arr = value.split(",");
        if (pos_str_arr.length == 3) {
            try {
                double [] lookat = new double[3];
                // Lon
                lookat[0] = Math.toRadians(Double.parseDouble(pos_str_arr[0]));
                // Lat
                lookat[1] = Math.toRadians(Double.parseDouble(pos_str_arr[1]));
                // Distance
                lookat[2] = Double.parseDouble(pos_str_arr[2]);
                return lookat;
            } catch (NumberFormatException ex) {
            }
        }
        return null;
    }
    
    static public double[] extractLookat(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].indexOf("-lookat=") == 0) {
                double[] lookat = extractLookat(args[i].substring("-lookat=".length()));
                if (lookat != null)
                    return lookat;
            }
        }
        return null;
    }
    
}
