/*
 * PositionList.java
 *
 * Created on 8. februar 2007, 11:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.geopos;

/**
 * A list of geographic positions, latitude, longitude decimal degrees WGS84
 * @author runaas
 */
public class PositionList implements Cloneable {
    protected double [] pos;
    protected int size;
    
    /** Creates a new empty instance of PositionList */
    public PositionList() {
        size = 0;
    }
    
    public Object clone() throws CloneNotSupportedException {
        PositionList retval  = (PositionList)super.clone();
        if (pos != null)
            retval.pos = (double [])(pos.clone());
        retval.size = size;
        return retval;
    }
    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        
        PositionList p = (PositionList)obj;
        if (p.size != size)
            return false;
        for (int i=0; i<size*2; ++i)
            if (p.pos[i] != pos[i])
                return false;
        return true;
    }
    
    protected void ensureCapacity(int new_size) {
        if (pos != null && pos.length >= new_size*2)
            return;
        if (new_size < 4)
            new_size = 4;
        else
            new_size = (int)Math.ceil(Math.pow(2, Math.ceil(Math.log(new_size)/Math.log(2))));
        double [] tmp = new double[new_size*2];
        if (pos != null)
            System.arraycopy(pos, 0, tmp, 0, size*2);
        pos = tmp;
    }
    
    /**
     * @return the number of positions in the list
     */
    public int size() {
        return size;
    }
    
    /**
     * @param pos position index
     * @return longitude for position
     */
    public double getLongitude(int ix) {
        return pos[ix*2];
    }
    
    /**
     * @param pos position index
     * @return latitude for position
     */
    public double getLatitude(int ix) {
        return pos[ix*2+1];
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
        pos[ix*2]    = longitude;
        pos[ix*2+1]  = latitude;
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
    }
    
    /**
     * Remove the position at the index, the rest of the list is shifted one place to the "left"
     * 
     * @param pos position index
     */
    public void remove(int ix) {
        System.arraycopy(pos, (ix+1)*2, pos, ix*2, (size-ix-1)*2);
        --size;
    }
}
