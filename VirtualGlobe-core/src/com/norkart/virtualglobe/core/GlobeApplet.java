/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  GlobeApplet.java
 *
 * Created on 15. februar 2008, 10:59
 *
 */

package com.norkart.virtualglobe.core;

import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;
import com.norkart.virtualglobe.viewer.navigator.MouseKeyNavigationAdapter;
import com.norkart.virtualglobe.viewer.navigator.WalkPerspectiveNavigationAdapter;

import javax.swing.JApplet;

import java.awt.BorderLayout;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;

import com.norkart.virtualglobe.components.Universe;
import com.norkart.virtualglobe.viewer.PerspectiveCamera;

import com.norkart.virtualglobe.components.Universe;
import com.norkart.virtualglobe.components.World;
import com.norkart.virtualglobe.components.GlobeSurface;
import com.norkart.virtualglobe.components.WorldComponent;
import com.norkart.virtualglobe.components.LayeredPyramidCoverage;
import com.norkart.virtualglobe.components.WorldComponentFactory;

import com.norkart.virtualglobe.cache.CacheManagerFactory;
import com.norkart.virtualglobe.cache.jdbm.CacheManagerJdbmFactory;

import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.jogl.JOGLViewerManager;

import com.norkart.virtualglobe.util.ApplicationUtils;

import netscape.javascript.*;

/**
 *
 * @author runaas
 */
public class GlobeApplet extends JApplet {
    private Universe universe = null;
    private PerspectiveCamera camera     = null;

    private double[] startPoint = null;
    private JSObject win = null;
    
     static {
        CacheManagerFactory.setInstance(new CacheManagerJdbmFactory());
        ViewerManager.setInstance(new JOGLViewerManager());
        
        WorldComponentFactory.getInstance().add(GlobeSurface.class, "layered-pyramid-coverage",
                new WorldComponentFactory.Creator() {
            public WorldComponent create(WorldComponent parent) {
                return new LayeredPyramidCoverage(parent);
            }
        });
        
        WorldComponentFactory.getInstance().add(World.class, "globe-surface",
                new WorldComponentFactory.Creator() {
            public WorldComponent create(WorldComponent parent) {
                return new GlobeSurface(parent);
            }
        });
    }
    
    /** Creates a new instance of GlobeApplet */
    public GlobeApplet() {
        setLayout(new BorderLayout());
    }
    
    public void init() {
        try {
            win = JSObject.getWindow(this);
        } catch (Exception ex) {
            System.err.println("No applet scripting support");
        }
        
        universe = new Universe();
        
          camera = ViewerManager.getInstance().createPerspectiveCamera(new GlobeNavigator(null));
        
        WalkPerspectiveNavigationAdapter navigationAdapter = new WalkPerspectiveNavigationAdapter(camera);
        camera.getCanvas().addKeyListener(navigationAdapter);
        camera.getCanvas().addMouseListener(navigationAdapter);
        camera.getCanvas().addMouseMotionListener(navigationAdapter);
        camera.getCanvas().addMouseWheelListener(navigationAdapter);
        
        add(camera.getGraphicsView(), BorderLayout.CENTER);
        
        String dataset_str = getParameter("dataset");
        universe.setDataSet(dataset_str);
        
        String v_str = getParameter("viewpoint");
        double[] vp = v_str != null ? ApplicationUtils.extractViewpoint(v_str) : null;
        
        String lookat_str = getParameter("lookat");
        final double[] lookat = lookat_str != null ? ApplicationUtils.extractLookat(lookat_str) : null;
        
        if (vp != null) {
            startPoint = vp;
            camera.getNavigator().gotoViewpoint(vp[0], vp[1], vp[2], vp[3], vp[4]);
        } else if (lookat != null) {
            startPoint = lookat;
            camera.getNavigator().gotoLookat(lookat[0], lookat[1], lookat[2], true, Math.toRadians(-20));
            /*
            if (camera instanceof AVPerspectiveCamera) {
                new Thread() {
                    public void run() {
                        while (navigator.getGlobe() == null) {
                            try { Thread.sleep(200); } catch (InterruptedException ex) {}
                        }
                        final AVPerspectiveCamera camera =
                                (AVPerspectiveCamera)GlobeApplet.this.camera;
                        final PointMarker pm =
                                new PointMarker(navigator.getGlobe(),
                                lookat.lon, lookat.lat);
                        pm.addPin(PointMarker.WHITE_APPEARANCE,
                                PointMarker.BLUE_APPEARANCE,
                                navigator, .1f);
                        camera.getGraphicsCore().addOriginUpdateListener(pm);
                        if (camera.getFeatureRoot().isLive()) {
                            camera.updateNode(camera.getFeatureRoot(), new org.j3d.aviatrix3d.NodeUpdateListener() {
                                public void updateNodeBoundsChanges(Object src) {
                                    camera.getFeatureRoot().addChild(pm);
                                }
                                public void updateNodeDataChanges(Object src) {
                                }
                            }, AVPerspectiveCamera.UPDATE_BOUNDS);
                        } else
                            camera.getFeatureRoot().addChild(pm);
                    }
                }.start();
            }
             */
        }
        
        // Report position of doubleclicked pointer
        
        camera.getCanvas().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ev) {
                if (ev.getClickCount() != 2) return;
                
                GlobeNavigator nav = camera.getNavigator();
                double lat  = nav.getPointerLat();
                double lon  = nav.getPointerLon();
                // double dist = nav.getPointerDist();
                if (win != null) {
                    try {
                        win.call("centerMapOnLonLatCoords", new Object [] {lon, lat, false/*, dist*/});
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                     gotoLookat(lon, lat, 1000, true, Math.toRadians(-20));
                }
            }
        });
    }
    
    public void gotoLookat(double lon, double lat, double dist) {
        camera.getNavigator().gotoLookat(lon, lat, dist);
    }
    public void gotoLookat(double lon, double lat, double dist, boolean north_up, double ha) {
        camera.getNavigator().gotoLookat(lon, lat, dist, north_up, ha);
    }
    public void gotoStart() {
        if (startPoint == null)
            return;
        if ( startPoint.length == 5) {
            camera.getNavigator().gotoViewpoint(startPoint[0], startPoint[1], startPoint[2], startPoint[3], startPoint[4]);
        } else if (startPoint.length == 3) {
            camera.getNavigator().gotoLookat(startPoint[0], startPoint[1], startPoint[2], true, Math.toRadians(-20));
        }
    }
    
    
    public void start() {
        camera.start();
    }
    
    public void stop() {
        camera.stop();
    }
    
    public void destroy() {
        camera.shutdown();
    }
}
