/*
 * Position.java
 *
 * Created on 8. februar 2007, 11:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.syndication.feed.module.georss.geometries;

/**
 *
 * @author runaas
 */
public class Position implements Cloneable {
    private double latitude;
    private double longitude;
    
    /** Creates a new instance of Position */
    public Position() {
        latitude  = Double.NaN;
        longitude = Double.NaN;
    }
    
     public Position(double longitude, double latitude) {
        this.latitude  = latitude;
        this.longitude = longitude;
    }
      
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
     }
    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        
        Position p = (Position)obj;
        return p.latitude == latitude && p.longitude == longitude;
    }
     
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
