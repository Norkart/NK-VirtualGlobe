/*
 * AbstractRing.java
 *
 * Created on 8. februar 2007, 11:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.syndication.feed.module.georss.geometries;

/**
 *
 * @author runaas
 */
public abstract class AbstractRing implements Cloneable {
    
    /** Creates a new instance of AbstractRing */
    public AbstractRing() {
    }
    
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
