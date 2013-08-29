package com.norkart.virtualglobe.core;
/*
 * Main.java
 *
 * Created on 25. april 2008, 09:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import com.norkart.virtualglobe.viewer.PerspectiveCamera;
import java.net.URL;
import java.net.MalformedURLException;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;
import com.norkart.virtualglobe.viewer.navigator.WalkPerspectiveNavigationAdapter;

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

/**
 *
 * @author runaas
 */
public class Main extends JFrame {
    
    private Universe          universe = new Universe();
    private PerspectiveCamera camera;
    private GlobeNavigator    navigator;
    
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
    
   
    
    /** Creates a new instance of Main */
    public Main(String[] args) {
        camera = ViewerManager.getInstance().createPerspectiveCamera(new GlobeNavigator(null));
        
        WalkPerspectiveNavigationAdapter navigationAdapter = new WalkPerspectiveNavigationAdapter(camera);
        camera.getCanvas().addKeyListener(navigationAdapter);
        camera.getCanvas().addMouseListener(navigationAdapter);
        camera.getCanvas().addMouseMotionListener(navigationAdapter);
        camera.getCanvas().addMouseWheelListener(navigationAdapter);
        if (camera instanceof WindowListener)
            addWindowListener((WindowListener)camera);
        
        Component graphic_view = camera.getGraphicsView();
        graphic_view.setPreferredSize(new Dimension(800, 600));
        this.getContentPane().add(graphic_view, BorderLayout.CENTER);
        
        URL dataset = ApplicationUtils.extractDataset(args);
        universe.setDataSet(dataset);
        
        double [] vp = ApplicationUtils.extractViewpoint(args);
        if (vp != null) {
            camera.getNavigator().gotoViewpoint(vp[0], vp[1], vp[2], vp[3], vp[4]);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Main mainframe = new Main(args);
        
        mainframe.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        mainframe.pack();
        mainframe.setVisible(true);
    }
    
}
