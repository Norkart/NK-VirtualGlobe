/*
 * CoordinateReferenceSystem.java
 *
 * Created on 24. mai 2007, 13:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.geopos;

/**
 *
 * @author runaas
 */
public interface CoordinateReferenceSystem {
    public void toCRS(double [] lonlat, int lonlat_ix, double [] xy, int xy_ix);
    public void fromCRS(double [] xy, int xy_ix, double [] lonlat, int lonlat_ix);
}
