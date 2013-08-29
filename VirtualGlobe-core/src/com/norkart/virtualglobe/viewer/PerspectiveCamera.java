/*
 * PerspectiveCamera.java
 *
 * Created on 23. april 2008, 15:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.viewer;

/**
 *
 * @author runaas
 */
public interface PerspectiveCamera extends Camera {
    public float getFov();
    public void  setFov(float fov);
    
    public float getDetailSizeFactor();
    public void  setDetailSizeFactor(float detail_factor);
}
