/*
 * Position.java
 *
 * Created on 8. februar 2007, 11:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.geopos;

/**
 * A two dimensional position represented by latitude and longitude decimal degrees in WGS84
 * @author runaas
 */
public class Position implements Cloneable {
    protected double [] pos = new double[2];
    
    /** Creates a new instance of Position */
    public Position() {
        pos[1] = Double.NaN;
        pos[0] = Double.NaN;
    }
    
    /**
     * Create Position from a pair of coordinate values
     *
     * @param latitude
     * @param longitude
     */
    public Position(double latitude, double longitude) {
        pos[1] = latitude;
        pos[0] = longitude;
    }
    
    public Object clone() throws CloneNotSupportedException {
        Position retval  = (Position)super.clone();
        if (pos != null)
            retval.pos = (double [])(pos.clone());
        return retval;
    }
    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        
        Position p = (Position)obj;
        return p.pos[0] == pos[0] && p.pos[1] == pos[1];
    }
    
    /**
     * @return latitude
     */
    public double getLatitude() {
        return pos[1];
    }
    
    /**
     * @return longitude
     */
    public double getLongitude() {
        return pos[0];
    }
    
    /**
     * Set the latitude
     *
     * @param latitude the new latitude
     */
    public void setLatLong(double latitude, double longitude) {
        pos[1] = latitude;
        pos[0] = longitude;
    }
}
