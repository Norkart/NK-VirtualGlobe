//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.av3d;

import com.norkart.virtualglobe.viewer.Camera;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.GlobeSurfaceGraphics;
import com.norkart.virtualglobe.viewer.PerspectiveCamera;
import com.norkart.virtualglobe.viewer.MapView;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;

import com.norkart.virtualglobe.util.ApplicationSettings;

import com.norkart.virtualglobe.globesurface.BttSurface;

import org.j3d.aviatrix3d.Group;
import org.j3d.aviatrix3d.Node;
import org.j3d.aviatrix3d.Geometry;

import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.ApplicationUpdateObserver;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.vecmath.Point3f;
import javax.vecmath.Vector4f;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class AV3DViewerManager extends ViewerManager implements ApplicationUpdateObserver {
    SingleThreadRenderManager sceneManager;
    
    Group  view_root    = new Group();
    Group  scene_root   = new Group();
    Group  model_group  = new Group();
    Group  feature_root = new Group();
    Group  globe_root   = new Group();
    private ConcurrentLinkedQueue<ApplicationUpdateObserver> updateObserverList = new ConcurrentLinkedQueue();
    
    long lastfast = 0;
    protected long prevframetime = 0;
    
    public AV3DViewerManager() {
        model_group.addChild(feature_root);
        model_group.addChild(globe_root);
        scene_root.addChild(model_group);
        scene_root.addChild(view_root);
        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.setApplicationObserver(this);
        // sceneManager.setEnabled(true);
    }
    
    public PerspectiveCamera createPerspectiveCamera(GlobeNavigator nav) {
        AV3DPerspectiveCamera c = new AV3DPerspectiveCamera(this, nav);
        cameras.add(c);
        
        boolean was_enabled = sceneManager.isEnabled();
        
        if (was_enabled)
            sceneManager.setEnabled(false);
        sceneManager.addDisplay(c.displayCollection);
        if (was_enabled)
            sceneManager.setEnabled(true);
        
        addApplicationUpdateObserver(c);
        return c;
    }
    
    /**
     * Create a map view camera
     * @param camera
     * @return
     */
    public MapView createMapView(PerspectiveCamera camera) {
        AV3DMapView mv = new AV3DMapView(camera);
        cameras.add(mv);
        
        boolean was_enabled = sceneManager.isEnabled();
        
        if (was_enabled)
            sceneManager.setEnabled(false);
        sceneManager.addDisplay(mv.displayCollection);
        if (was_enabled)
            sceneManager.setEnabled(true);
        
        addApplicationUpdateObserver(mv);
        return mv;
    }
    /**
     * Create a surface rendering system
     */
    public GlobeSurfaceGraphics addGlobeSurface(BttSurface surface) {
        AV3DGlobeSurfaceGraphics gsg = new AV3DGlobeSurfaceGraphics(surface);
        synchronized (graphics){
            graphics.add(gsg);
        }
        return gsg;
    }
    
    public Camera getMatchingCamera(Vector4f[] frustum_planes) {
        Camera result = null;
        float bestfit = .01f;
        Iterator<Camera> it = cameras.iterator();
        while (it.hasNext()) {
            Camera cam = it.next();
            if (cam instanceof AV3DCamera) {
                AV3DCamera pcam = (AV3DCamera)cam;
                for (int i=0; i<6; ++i) {
                    float d =
                            frustum_planes[i].x * frustum_planes[i].x +
                            frustum_planes[i].y * frustum_planes[i].y +
                            frustum_planes[i].z * frustum_planes[i].z;
                    d = (float)Math.sqrt(d);
                    
                    float fit = 
                            Math.abs(pcam.frustumPlanes[i].w - frustum_planes[i].w/d) / 
                            (10 + Math.abs(pcam.frustumPlanes[i].w*100));
                    
                    fit = Math.max(fit, Math.abs(pcam.frustumPlanes[i].x - frustum_planes[i].x/d));
                    fit = Math.max(fit, Math.abs(pcam.frustumPlanes[i].y - frustum_planes[i].y/d));
                    fit = Math.max(fit, Math.abs(pcam.frustumPlanes[i].z - frustum_planes[i].z/d));
                    if (fit <bestfit) {
                        bestfit = fit;
                        result = pcam;
                    }
                }
            }
        }
        return result;
    }
    
    //---------------------------------------------------------------
// Methods defined by ApplicationUpdateObserver
//---------------------------------------------------------------
    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    public void appShutdown() {
        // do nothing
    }
    public void updateSceneGraph() {
    /*
        // Do fps calculations
        framecnt++;
        long currtime = System.currentTimeMillis();
        if (currtime-prevechotime > 1000) {
            float fps = framecnt*1000.f/(currtime-prevechotime);
            if (framerate_label != null) {
                framerate_arg[0] = new Float(fps);
                framerate_label.setText(framerate_format.format(framerate_arg));
            }
            prevechotime = currtime;
            framecnt = 0;
        }
        prevframetime = currtime - prevframetime;
     
        long timeleft = currtime - lastfast > 1000 && !hasActiveUpdater() ?
            (long)(1000.f/base_fps) :
            (long)(1000.f/ApplicationSettings.getApplicationSettings().getMaxFPS());
        timeleft -= prevframetime;
        if (timeleft > 0) {
            synchronized (this) {
                try {wait(timeleft);} catch (InterruptedException ex) {}
            }
        }
     
        prevframetime = System.currentTimeMillis();
     */
        
        long currtime = System.currentTimeMillis();
        long timeleft = (currtime - lastfast > 200 && !hasActiveUpdater()) ?
            (long)(1000.f/base_fps) :
            (long)(1000.f/ApplicationSettings.getApplicationSettings().getMaxFPS());
        timeleft -= (currtime - prevframetime);
        if (timeleft > 0) {
            synchronized (this) {
                try {this.wait(timeleft);}  catch (InterruptedException ex) {}
            }
            
        } else
            Thread.yield();
        
        prevframetime = System.currentTimeMillis();
        
        for (ApplicationUpdateObserver obs : updateObserverList) {
            if (!sceneManager.isEnabled()) return;
            obs.updateSceneGraph();
        }
        
        Iterator<Update> it = updates.iterator();
        while (it.hasNext()) {
            Update u = it.next();
            if (u.src instanceof Node) {
                Node n = (Node)u.src;
                if (n.isLive()) {
                    if ((u.status & UPDATE_BOUNDS) != 0)
                        n.boundsChanged(u.l);
                    if ((u.status & UPDATE_DATA) != 0)
                        n.dataChanged(u.l);
                    it.remove();
                }
            } else if (u.src instanceof Geometry) {
                Geometry g = (Geometry)u.src;
                if (g.isLive()) {
                    if ((u.status & UPDATE_BOUNDS) != 0)
                        g.boundsChanged(u.l);
                    if ((u.status & UPDATE_DATA) != 0)
                        g.dataChanged(u.l);
                    it.remove();
                }
            }
        }
    }
    
    
    public void addApplicationUpdateObserver(ApplicationUpdateObserver observer) {
        updateObserverList.add(observer);
    }
    
    /**
     * Notification that now is a good time to update the scene graph.
     */
    public final static int UPDATE_DATA = 1;
    public final static int UPDATE_BOUNDS = 2;
    
    private class Update {
        Object src;
        NodeUpdateListener l;
        int status;
        Update(Object src, NodeUpdateListener l, int status) {
            this.src = src; this.l = l; this.status = status;
        }
    }
    ConcurrentLinkedQueue<Update> updates  = new ConcurrentLinkedQueue();
    
    public void updateNode(Object src, NodeUpdateListener l, int status) {
        updates.add(new Update(src, l, status));
    }
    
    public Group getFeatureRoot() {
        return feature_root;
    }
    
    protected boolean hasActiveUpdater() {
        Iterator<Camera> it = cameras.iterator();
        while (it.hasNext()) {
            GlobeNavigator nav = it.next().getNavigator();
            if (nav != null && nav.getUpdater() != null && nav.getUpdater().isActive())
                return true;
        }
        return false;
    }
    
    //  private long prevechotime = 0, prevframetime = 0;
    // private int framecnt = 0;
}