/*
 * LineString.java
 *
 * Created on 8. februar 2007, 10:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.syndication.feed.module.georss.geometries;

/**
 *
 * @author runaas
 */
public final class LineString extends AbstractCurve {
    private PositionList posList;
    
    /** Creates a new instance of LineString */
    public LineString() {
        
    }
   
     public LineString(PositionList posList) {
        this.posList = posList;
    }
     
    public Object clone() throws CloneNotSupportedException {
        LineString retval = (LineString)super.clone();
        if (posList != null)
            retval.posList = (PositionList)posList.clone();
        return retval;
    }
    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        return getPositionList().equals(((LineString)obj).getPositionList());
    }
    
    public PositionList getPositionList() {
        if (posList == null)
            posList = new PositionList();
        return posList;
    }
    
    public void setPositionList(PositionList posList) {
        this.posList = posList;
    } 
}
