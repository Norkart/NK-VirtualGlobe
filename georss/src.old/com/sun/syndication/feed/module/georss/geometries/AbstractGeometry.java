/*
 * Geometry.java
 *
 * Created on 8. februar 2007, 10:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.syndication.feed.module.georss.geometries;

/**
 *
 * @author runaas
 */
public abstract class AbstractGeometry implements Cloneable {
    
    /** Creates a new instance of Geometry */
    public AbstractGeometry() {
    }
    
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
     public boolean equals(Object obj) {
         return obj != null && obj.getClass() == getClass();
     }
}
