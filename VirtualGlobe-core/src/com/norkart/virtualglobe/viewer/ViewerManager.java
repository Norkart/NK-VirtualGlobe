//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer;

import com.norkart.virtualglobe.globesurface.BttSurface;

import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;

// import com.sintef.VirtualGlobe.Graphics.AV.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public abstract class ViewerManager {
    private static ViewerManager instance;
    /// Graphcs
    protected ArrayList<GlobeSurfaceGraphics> graphics = new ArrayList<GlobeSurfaceGraphics>();
    
    /// Cameras
    protected ArrayList<Camera> cameras  = new ArrayList();
    
    protected LightModel light_model = new LightModel();

    protected float base_fps = 2;

    
    public static ViewerManager getInstance() {
       return instance;
    }
     public static void setInstance(ViewerManager inst) {
       instance = inst;
    }
    
    /**
     * Create a Perspective camera
     * @return
     */
    public abstract PerspectiveCamera createPerspectiveCamera(GlobeNavigator navigator); 
    
    /**
     * Create a map view camera
     * @param camera
     * @return
     */
    public abstract MapView createMapView(PerspectiveCamera camera); 
    
    /**
     * Create a surface rendering system
     */
    public abstract GlobeSurfaceGraphics addGlobeSurface(BttSurface surface);
    
    public void removeGlobeSurface(BttSurface surface) {
        synchronized (graphics) {
            Iterator<GlobeSurfaceGraphics> it = graphics.iterator();
            while (it.hasNext()) {
                GlobeSurfaceGraphics gsg = it.next();
                if (gsg.getSurface() == surface) {
                    it.remove();
                    gsg.clear();
                }
            }
        }
    }
    
    
    /**
     * Get a camera
     * @param i
     * @return
     */
    public  AbstractCamera getCamera(int i) {
        try {
            return (AbstractCamera)cameras.get(i);
        } catch (IndexOutOfBoundsException ex) {}
        return null;
    }
   
    /**
     * Number of cameras
     * @return
     */
    public  int    getNumCamera() {
        return cameras.size();
    }
    
    public LightModel getLightModel() {
        return light_model;
    }
    
    // Handling of the hi-resolution origin
    /// The hi resolution origin
    private Point3d origin = new Point3d();
    /// Listeners that expect to be told when the hi-res origin changes
    private ArrayList<WeakReference<OriginUpdateListener>> originUpdateListeners = new ArrayList();
    private ArrayList<WeakReference<OriginUpdateListener>> originRequestListeners = new ArrayList();
    private ArrayList<WeakReference<OriginUpdateListener>> originCompleteListeners = new ArrayList();
    
    /**
     * Check if the hires origin is sufficiently close to the interesting areas
     * @param p Point to test
     * @param tol Acceptable tolerance (vWorld scale size)
     */
    public void testOrigin(Point3d p, float tol) {
        synchronized (originRequestListeners) {
            if (originRequestListeners.isEmpty()) {
                double orig_dist = origin.distance(p);
                
                // Move origin?
                if (orig_dist*1e-5f > tol) {
                    origin.set(p);
                    // System.err.println("Flytter origin: " + origin);
                    synchronized (originUpdateListeners) {
                        originRequestListeners.addAll(originUpdateListeners);
                    }
                }
            } else {
                Iterator<WeakReference<OriginUpdateListener>> i = originRequestListeners.iterator();
                while (i.hasNext()) {
                    WeakReference<OriginUpdateListener> ref = i.next();
                    OriginUpdateListener l = ref.get();
                    if (l == null) 
                          i.remove();
                    else if (l.requestUpdateOrigin(origin)) {
                        i.remove();
                        originCompleteListeners.add(ref);
                    }
                }
            }
                if (originRequestListeners.isEmpty()) {
                    // Call listeners
                    synchronized (originCompleteListeners) {
                        for (WeakReference<OriginUpdateListener> ref : originCompleteListeners) {
                            OriginUpdateListener l = ref.get();
                            if (l != null)
                                l.updateOrigin(origin);
                        }
                        originCompleteListeners.clear();
                    }
                
            }
        }
    }
    
    
    /**
     * Add origin update listeners
     * @param l
     */
    public void addOriginUpdateListener(OriginUpdateListener l) {
        // l.requestUpdateOrigin(origin);
        // l.updateOrigin(origin);
        synchronized (originUpdateListeners) {
            originUpdateListeners.add(new WeakReference(l));
        }
        synchronized (originRequestListeners) {
            originRequestListeners.add(new WeakReference(l));
        }
    }
    
    public Point3d getOrigin() {
        return origin;
    }
    
    public float getBaseFps() {
        return base_fps;
    }
}
