/*
 * GlobeSurfaceGraphics.java
 *
 * Created on 12. april 2007, 10:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.viewer;

import com.norkart.virtualglobe.globesurface.BttSurface;

/**
 *
 * @author runaas
 */
public interface GlobeSurfaceGraphics {
    public void       setTransparency(float transparency);
    public void       setWireframe(boolean b);
    public boolean    isWireframe();
    public BttSurface getSurface();
    public void       clear();
}
