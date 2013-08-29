/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  JOGLViewerManager.java
 *
 * Created on 22. april 2008, 15:37
 *
 */

package com.norkart.virtualglobe.viewer.jogl;

import com.norkart.virtualglobe.globesurface.BttSurface;
import com.norkart.virtualglobe.viewer.GlobeSurfaceGraphics;
import com.norkart.virtualglobe.viewer.MapView;
import com.norkart.virtualglobe.viewer.PerspectiveCamera;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;

import javax.media.opengl.*;

/**
 *
 * @author runaas
 */
public class JOGLViewerManager extends ViewerManager {
    
    /** Creates a new instance of JOGLViewerManager */
    public JOGLViewerManager() {
    }
    public PerspectiveCamera createPerspectiveCamera(GlobeNavigator nav) {
        JOGLPerspectiveCamera c = new JOGLPerspectiveCamera(this, nav);
        cameras.add(c);
        return c;
    }
    
    /**
     * Create a map view camera
     * @param camera
     * @return
     */
    public MapView createMapView(PerspectiveCamera camera) {
        JOGLMapView mv = new JOGLMapView(camera);
        cameras.add(mv);
        return mv;
    }
    /**
     * Create a surface rendering system
     */
    public GlobeSurfaceGraphics addGlobeSurface(BttSurface surface) {
        JOGLGlobeSurfaceGraphics gsg = new JOGLGlobeSurfaceGraphics(this, surface);
        synchronized (graphics){
            graphics.add(gsg);
        }
        return gsg;
    }
    
    public void draw(JOGLCamera camera, GLAutoDrawable drawable) {
        synchronized (graphics) {
            for (GlobeSurfaceGraphics g : graphics) {
                if (g instanceof JOGLGlobeSurfaceGraphics)
                    ((JOGLGlobeSurfaceGraphics)g).draw(camera, drawable);
            }
        }
        
    }
}
