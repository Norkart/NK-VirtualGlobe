/*
 * MapView.java
 *
 * Created on 22. april 2008, 14:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.viewer;

/**
 *
 * @author runaas
 */
public interface MapView extends Camera {
    public boolean isNorthUp();
    public void    setNorthUp(boolean north_up);
    public float   getHorizonScale();
    public void    setHorizonScale(float hor_scale);
}
