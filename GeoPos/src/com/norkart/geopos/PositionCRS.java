/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  PositionCRS.java
 *
 * Created on 24. mai 2007, 14:06
 *
 */

package com.norkart.geopos;

/**
 *
 * @author runaas
 */
public class PositionCRS extends Position {
    protected CoordinateReferenceSystem crs;
    
    protected double [] xy = new double[2];
    
    /** Creates a new instance of PositionCRS */
    public PositionCRS() {
        crs = null;
        xy[0] = Double.NaN;
        xy[1] = Double.NaN;
    }
    
    public PositionCRS(double x, double y, CoordinateReferenceSystem crs) {
        this.crs = crs;
        xy[0] = x;
        xy[1] = y;
        crs.fromCRS(xy, 0, pos, 0);
    }
    
    public PositionCRS(Position p, CoordinateReferenceSystem crs) {
        this.crs = crs;
        pos[0] = p.pos[0];
        pos[1] = p.pos[1];
        crs.toCRS(pos, 0, xy, 0);
    }
    
    public Object clone() throws CloneNotSupportedException {
        PositionCRS retval  = (PositionCRS)super.clone();
        if (xy != null)
            retval.xy = (double [])(xy.clone());
        return retval;
    }
    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        
        PositionCRS p = (PositionCRS)obj;
        return p.xy[0] == xy[0] && p.xy[1] == xy[1];
    }
    
    public double getX() {
        return xy[0];
    }
    
    public double getY() {
        return xy[1];
    }
    
    /**
     * Set the latitude
     *
     * @param latitude the new latitude
     */
    public void setLatLong(double latitude, double longitude) {
        pos[1] = latitude;
        pos[0] = longitude;
        crs.toCRS(pos, 0, xy, 0);
    }
    
    public void setXY(double x, double y) {
        xy[0] = x;
        xy[1] = y;
        crs.fromCRS(xy, 0, pos, 0);
    }
}
