/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  PositionListCRS.java
 *
 * Created on 24. mai 2007, 14:17
 *
 */

package com.norkart.geopos;

/**
 *
 * @author runaas
 */
public class PositionListCRS extends PositionList {
    protected CoordinateReferenceSystem crs;
    protected double [] xy;
    
    /** Creates a new instance of PositionListCRS */
    public PositionListCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }
    
    public Object clone() throws CloneNotSupportedException {
        PositionListCRS retval  = (PositionListCRS)super.clone();
        if (xy != null)
            retval.xy = (double [])(xy.clone());
        return retval;
    }
    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        
        PositionListCRS p = (PositionListCRS)obj;
        for (int i=0; i<size*2; ++i)
            if (p.xy[i] != xy[i])
                return false;
        return true;
    }
    
    protected void ensureCapacity(int new_size) {
        super.ensureCapacity(new_size);
        if (xy != null && xy.length >= new_size*2)
            return;
        if (new_size < 4)
            new_size = 4;
        else
            new_size = (int)Math.ceil(Math.pow(2, Math.ceil(Math.log(new_size)/Math.log(2))));
        double [] tmp = new double[new_size*2];
        if (xy != null)
            System.arraycopy(xy, 0, tmp, 0, size*2);
        xy = tmp;
    }
    
    /**
     * Add a position at the end of the list
     * @param latitude
     * @param longitude
     */
    public void addLatLong(double latitude, double longitude) {
        ensureCapacity(size+1);
        pos[size*2]    = longitude;
        pos[size*2+1]  = latitude;
        crs.toCRS(pos, size, xy, size);
        ++size;
    }
    
    /**
     * Add a position at a given index in the list. The rest of the list is
     * shifted one place to the "right"
     *
     * @param pos position index
     * @param latitude
     * @param longitude
     */
    public void insertLatLong(int ix, double latitude, double longitude) {
        ensureCapacity(size+1);
        System.arraycopy(pos, ix*2, pos, (ix+1)*2, (size-ix)*2);
        System.arraycopy(xy,  ix*2, xy,  (ix+1)*2, (size-ix)*2);
        pos[ix*2]    = longitude;
        pos[ix*2+1]  = latitude;
        crs.toCRS(pos, ix, xy, ix);
        ++size;
    }
    
    /**
     * Replace the position at the index with new values
     *
     * @param pos position index
     * @param latitude
     * @param longitude
     */
    public void setLatLong(int ix, double latitude, double longitude) {
        pos[ix*2]    = longitude;
        pos[ix*2+1]  = latitude;
        crs.toCRS(pos, ix, xy, ix);
    }
    
    /**
     * Remove the position at the index, the rest of the list is shifted one place to the "left"
     *
     * @param pos position index
     */
    public void remove(int ix) {
        System.arraycopy(xy, (ix+1)*2, xy, ix*2, (size-ix-1)*2);
        super.remove(ix);
    }
    
    /**
     * Add a position at the end of the list
     * @param latitude
     * @param longitude
     */
    public void addXY(double x, double y) {
        ensureCapacity(size+1);
        xy[size*2]    = x;
        xy[size*2+1]  = y;
        crs.fromCRS(xy, size, pos, size);
        ++size;
    }
    
    /**
     * Add a position at a given index in the list. The rest of the list is
     * shifted one place to the "right"
     *
     * @param pos position index
     * @param latitude
     * @param longitude
     */
    public void insertXY(int ix, double x, double y) {
        ensureCapacity(size+1);
        System.arraycopy(pos, ix*2, pos, (ix+1)*2, (size-ix)*2);
        System.arraycopy(xy,  ix*2, xy,  (ix+1)*2, (size-ix)*2);
        xy[ix*2]    = x;
        xy[ix*2+1]  = y;
        crs.fromCRS(xy, ix, pos, ix);
        ++size;
    }
    
    /**
     * Replace the position at the index with new values
     *
     * @param pos position index
     * @param latitude
     * @param longitude
     */
    public void setXY(int ix, double x, double y) {
        xy[ix*2]    = x;
        xy[ix*2+1]  = y;
        crs.fromCRS(xy, ix, pos, ix);
    }
}
