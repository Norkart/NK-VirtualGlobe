package com.norkart.virtualglobe.feature3d;
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
import com.norkart.virtualglobe.components.FeatureSet;

import com.norkart.virtualglobe.cache.CacheManagerFactory;
import com.norkart.virtualglobe.cache.jdbm.CacheManagerJdbmFactory;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager;

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
        ViewerManager.setInstance(new AV3DViewerManager());
        
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
        
          WorldComponentFactory.getInstance().add(GlobeSurface.class, "feature-set",
                new WorldComponentFactory.Creator() {
            public WorldComponent create(WorldComponent parent) {
                return new FeatureSet(parent);
            }
        });
    }
    
     static public URL extractDataset(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].indexOf("-dataset=") == 0) {
                try {
                    return new URL(args[i].substring("-dataset=".length()));
                } catch (MalformedURLException ex) {}
            }
        }
        return null;
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
        graphic_view.setPreferredSize(new Dimension(300, 300));
        this.getContentPane().add(graphic_view, BorderLayout.CENTER);
        
        URL dataset = extractDataset(args);
        universe.setDataSet(dataset);
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
