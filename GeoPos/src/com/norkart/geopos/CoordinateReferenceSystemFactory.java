/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  CoordinateReferenceSystemFactory.java
 *
 * Created on 15. august 2007, 08:27
 *
 */

package com.norkart.geopos;


import java.util.Hashtable;

/**
 *
 * @author runaas
 */
public class CoordinateReferenceSystemFactory {
    
    private static Hashtable<String, CoordinateReferenceSystem> crs_table = new Hashtable();
    
    private static Ellipsoid ellpsWGS84 = new Ellipsoid(6378137.0, 1/298.25722);
    
    static private class TransMerc84CRS implements CoordinateReferenceSystem {
        private GaussKrugerProjection prj;
        
        TransMerc84CRS(double lon0, double x0, double y0, double k0) {
            prj = new GaussKrugerProjection(ellpsWGS84, lon0, x0, y0, k0);
        }
        
        public void toCRS(double [] lonlat, int lonlat_ix, double [] xy, int xy_ix) {
            prj.Geodetisk2Gausisk(lonlat, lonlat_ix, xy, xy_ix);
        }
        
        public void fromCRS(double [] xy, int xy_ix, double [] lonlat, int lonlat_ix) {
            prj.Gausisk2Geodetisk(xy, xy_ix, lonlat, lonlat_ix);
        }
    }
    
    static private class Utm84CRS extends TransMerc84CRS {
        Utm84CRS(int zone, boolean north) {
            super(Math.toRadians(zone*6-183), 500000, north?0:10000000, 0.9996);
        }
    }
    
    /** Creates a new instance of CoordinateReferenceSystemFactory */
    public CoordinateReferenceSystemFactory() {
    }
    
    
    // Parse a crs name and return the crs
    public static CoordinateReferenceSystem getCRS(String name) {
        if (name == null)
            return null;
        else {
            CoordinateReferenceSystem crs = crs_table.get(name);
            if (crs != null)
                return crs;
        }
        
        String [] tokens = name.split(":");
        
        if (tokens[0].equals("urn")) {
            if (tokens[1].equals("ogc")) {
                if (tokens[2].equals("def")) {
                    if (tokens[3].equals("crs")) {
                        if (tokens[4].equals("EPSG")) {
                            String version = tokens[5];
                            String EPSG_nr_str = tokens[6];
                            int EPSG_nr = Integer.parseInt(EPSG_nr_str);
                            
                            CoordinateReferenceSystem crs = null;
                            if (EPSG_nr > 32600 && EPSG_nr <= 32660) { 
                                int zone = EPSG_nr - 32600;
                                crs = new Utm84CRS(zone, true);
                            }
                            else if (EPSG_nr > 32700 && EPSG_nr <= 32760) { 
                                int zone = EPSG_nr - 32700;
                                crs = new Utm84CRS(zone, false);
                            }
                            
                            if (crs != null) {
                                crs_table.put(name, crs);
                                return crs;
                            }
                        }
                    }
                }
            }
           
        }
        return null;
    }
}
