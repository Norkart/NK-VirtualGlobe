/*
 * Main.java
 *
 * Created on 12. juni 2008, 11:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe;

import java.net.URL;

import com.norkart.virtualglobe.util.ApplicationUtils;
import com.norkart.virtualglobe.cache.CacheManagerFactory;
import com.norkart.virtualglobe.cache.jdbm.CacheManagerJdbmFactory;

import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager;
import com.norkart.virtualglobe.viewer.jogl.JOGLViewerManager;

import com.norkart.virtualglobe.components.WorldComponentFactory;
import com.norkart.virtualglobe.components.GlobeSurface;
import com.norkart.virtualglobe.components.WorldComponent;
import com.norkart.virtualglobe.components.LayeredPyramidCoverage;
import com.norkart.virtualglobe.components.World;
import com.norkart.virtualglobe.components.FeatureSet;
import com.norkart.virtualglobe.components.GeoRSSFeed;
import com.norkart.virtualglobe.components.PlaceNames;

/**
 *
 * @author runaas
 */
public class Main {
     private ApplicationFrame mainframe;
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
        
         WorldComponentFactory.getInstance().add(GlobeSurface.class, "georss-feed",
                new WorldComponentFactory.Creator() {
            public WorldComponent create(WorldComponent parent) {
                return new GeoRSSFeed(parent);
            }
        });
        
         WorldComponentFactory.getInstance().add(GlobeSurface.class, "place-names",
                new WorldComponentFactory.Creator() {
            public WorldComponent create(WorldComponent parent) {
                return new PlaceNames(parent);
            }
        });
         
        
    }
    
    
    /** Creates a new instance of Main */
    public Main(String[] args) {
        mainframe = new StandardApplicationFrame();
        
        double[] v = ApplicationUtils.extractViewpoint(args);
        URL dataset = ApplicationUtils.extractDataset(args);
        // String name = extractName(args);
        // final Lookat lookat = extractLookat(args);
        
        if (dataset != null)
            mainframe.universe.setDataSet(dataset);
        if (v != null) {
            /*
            v.dataset = dataset;
            v.name    = name;
            mainframe.viewpointDialog.addViewpoint(v);
             */
            mainframe.perspectiveCamera.getNavigator().gotoViewpoint(v[0], v[1], v[2], v[3], v[4]);
            mainframe.startPoint = v;
        } 
        
        mainframe.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
         mainframe.pack();
        mainframe.setVisible(true);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Main main_app = new Main(args);
    }
    
}
