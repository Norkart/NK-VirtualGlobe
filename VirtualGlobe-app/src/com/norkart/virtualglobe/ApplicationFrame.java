//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe;

import com.norkart.virtualglobe.viewer.PerspectiveCamera;
import com.norkart.virtualglobe.viewer.MapView;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;
import com.norkart.virtualglobe.viewer.navigator.WalkPerspectiveNavigationAdapter;
import com.norkart.virtualglobe.viewer.navigator.MouseKeyNavigationAdapter;

import com.norkart.virtualglobe.components.Universe;

import com.norkart.virtualglobe.util.ApplicationSettings;

import com.norkart.virtualglobe.gui.HelpDialog;
import com.norkart.virtualglobe.gui.ViewpointDialog;
import com.norkart.virtualglobe.gui.FlyPathDialog;

//import java.util.*;
//import java.io.File;
//import java.io.IOException;
//import java.text.MessageFormat;
//import java.net.*;
import java.awt.*;
import java.awt.event.*;

// import java.awt.image.BufferedImage;
import javax.swing.*;

//import javax.swing.filechooser.FileFilter;
//import javax.swing.event.*;

//import javax.swing.border.*;
//import javax.swing.ProgressMonitor;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class ApplicationFrame extends JFrame {
    JSplitPane  rootSplitPane = new JSplitPane();
    JSplitPane  mapDataSplitPane = new JSplitPane();
    
    JPanel      graphicView = new JPanel(new BorderLayout());
    
    JFileChooser saveCaptureFileChooser = new JFileChooser();
    
    // SettingsDialog settingsDialog;
    HelpDialog      helpDialog;
     
    ViewpointDialog viewpointDialog;

    FlyPathDialog   flyPathDialog;
    
    // VideoCapture     videoDialog;
    
    // MapDialog       mapDialog;
    MouseKeyNavigationAdapter navigationAdapter;
    Universe     universe = new Universe();
    PerspectiveCamera perspectiveCamera;
    MapPanel mapPanel;
    
    double[] startPoint = new double[] {0,0,12000000,0,Math.toRadians(-90)};
    
    public ApplicationFrame() throws HeadlessException {
        setTitle("Virtual Globe");
        
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        // Layout of main GUI components
        rootSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        mapDataSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        
        graphicView.setMinimumSize(new Dimension(300,300));
        graphicView.setPreferredSize(new Dimension(800,600));
        
        
        rootSplitPane.setRightComponent(graphicView);
        rootSplitPane.setLeftComponent(mapDataSplitPane);
        getContentPane().add(rootSplitPane);
        
        
        
        // Create 3D viewer window
        perspectiveCamera = ViewerManager.getInstance().createPerspectiveCamera(new GlobeNavigator(null));
        
        WalkPerspectiveNavigationAdapter navigationAdapter = new WalkPerspectiveNavigationAdapter(perspectiveCamera);
        perspectiveCamera.getCanvas().addKeyListener(navigationAdapter);
        perspectiveCamera.getCanvas().addMouseListener(navigationAdapter);
        perspectiveCamera.getCanvas().addMouseMotionListener(navigationAdapter);
        perspectiveCamera.getCanvas().addMouseWheelListener(navigationAdapter);
        if (perspectiveCamera instanceof WindowListener)
            addWindowListener((WindowListener)perspectiveCamera);
        
        Component graphic_view = perspectiveCamera.getGraphicsView();
        graphic_view.setPreferredSize(new Dimension(800, 600));
        graphicView.add(graphic_view, BorderLayout.CENTER);
        
        
        // Create 2D viewer window
        
        mapPanel = new MapPanel(perspectiveCamera);
        MapView map = mapPanel.getMap();
        if (map instanceof WindowListener)
            addWindowListener((WindowListener)map);
        mapDataSplitPane.setTopComponent(mapPanel);
        
        
        // Creating dialogs
        helpDialog = new HelpDialog(this);
        // settingsDialog = new SettingsDialog(this);
       
        viewpointDialog = new ViewpointDialog(this, universe, perspectiveCamera.getNavigator());
       
        flyPathDialog = new FlyPathDialog(this, universe, perspectiveCamera);
          /*
        videoDialog = new VideoCapture(this, universe, camera);

         */
    }
  /*
  public boolean postEvent(Event evt) {
    /**@todo Implement this java.awt.MenuContainer abstract method*/
    /*throw new java.lang.UnsupportedOperationException("Method postEvent() not yet implemented.");
  }
  public Font getFont() {
    /**@todo Implement this java.awt.MenuContainer abstract method*/
    /*throw new java.lang.UnsupportedOperationException("Method getFont() not yet implemented.");
  }
     */
    
    // JMFCapture capturer = null;
    
    
}