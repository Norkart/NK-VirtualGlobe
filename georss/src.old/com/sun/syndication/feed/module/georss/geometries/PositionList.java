/*
 * PositionList.java
 *
 * Created on 8. februar 2007, 11:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.syndication.feed.module.georss.geometries;

/**
 *
 * @author runaas
 */
public class PositionList implements Cloneable {
    private double [] latitude;
    private double [] longitude;
    private int size;
    
    /** Creates a new instance of PositionList */
    public PositionList() {
    }
    
     public Object clone() throws CloneNotSupportedException {
         PositionList retval  = (PositionList)super.clone();
         if (latitude != null)
             retval.latitude = (double [])(latitude.clone());
         if (longitude != null)
             retval.longitude = (double [])(longitude.clone());
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
        for (int i=0; i<size; ++i) 
            if (p.latitude[i] != latitude[i] || p.longitude[i] != longitude[i])
                return false;
        return true;
    }
     
    private void ensureCapacity(int new_size) {
        if (longitude != null && longitude.length >= new_size) 
            return;
        if (new_size < 4)
            new_size = 4;
        else
            new_size = (int)Math.ceil(Math.pow(2, Math.ceil(Math.log(new_size)/Math.log(2))));
        double [] tmp = new double[new_size];
        if (longitude != null)
            System.arraycopy(longitude, 0, tmp, 0, size);
        longitude = tmp;
        tmp = new double[new_size];
        if (latitude != null)
            System.arraycopy(latitude, 0, tmp, 0, size);
        latitude = tmp;
    }
    
    public int size() { 
        return size;
    }
    
    public double getLongitude(int pos) {
        return longitude[pos];
    }
    
    public double getLatitude(int pos) {
        return latitude[pos];
    }
    
    public void add(double longitude, double latitude) {
        ensureCapacity(size+1);
        this.longitude[size] = longitude;
        this.latitude[size]  = latitude;
        ++size;
    }
    
    public void insert(int pos, double longitude, double latitude) {
        ensureCapacity(size+1);
        System.arraycopy(this.longitude, pos, this.longitude, pos+1, size-pos);
        System.arraycopy(this.latitude,  pos, this.latitude,  pos+1, size-pos);
        this.longitude[pos] = longitude;
        this.latitude[pos]  = latitude;
         ++size;
    }
    
    public void replace(int pos, double longitude, double latitude) {
        this.longitude[pos] = longitude;
        this.latitude[pos]  = latitude;
    }
    
    public void remove(int pos) {
        System.arraycopy(longitude, pos+1, longitude, pos, size-pos-1);
        System.arraycopy(latitude,  pos+1, latitude,  pos, size-pos-1);
        --size;
    }
}
