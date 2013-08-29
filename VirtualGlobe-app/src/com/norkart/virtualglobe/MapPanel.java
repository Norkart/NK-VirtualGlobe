//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
/*
 * MapPanel.java
 *
 * Created on 19. september 2006, 15:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe;

import com.norkart.virtualglobe.util.ApplicationSettings;
import java.awt.event.WindowListener;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JCheckBox;
import javax.swing.Box;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.norkart.virtualglobe.viewer.MapView;
import com.norkart.virtualglobe.viewer.PerspectiveCamera;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.navigator.MapNavigationAdapter;


/**
 *
 * @author runaas
 */
public class MapPanel extends JPanel {
    private MapView map;
   
    
    // private
    
    
    /** Creates a new instance of MapPanel */
    public MapPanel(PerspectiveCamera camera) {
        super(new BorderLayout());
        
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        // Create 3D browser window
        map = ViewerManager.getInstance().createMapView(camera);
        if (map != null) {
            MapNavigationAdapter navigationAdapter = new MapNavigationAdapter(map);
            map.getCanvas().addKeyListener(navigationAdapter);
            map.getCanvas().addMouseListener(navigationAdapter);
            map.getCanvas().addMouseMotionListener(navigationAdapter);
            map.getCanvas().addMouseWheelListener(navigationAdapter);
            
            // addComponentListener((JOGLMapView)map);
            
            add(map.getGraphicsView(), BorderLayout.CENTER);
        }
        
       
            Box tools = Box.createHorizontalBox();
            
            final JCheckBox north_up = new JCheckBox(settings.getResourceString("NORTH_UP"));
            north_up.setSelected(map.isNorthUp());
            north_up.setToolTipText(settings.getResourceString("TOOL_NORTH_UP"));
            north_up.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                   map.setNorthUp(north_up.isSelected());
                }
            });
            tools.add(north_up);
            
            final JSlider slider = new JSlider(1, 40, (int)map.getHorizonScale());
            slider.setToolTipText(settings.getResourceString("TOOL_MAP_SCALE_SLIDER"));
            slider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    map.setHorizonScale(slider.getValue());
                }
            });
            tools.add(slider);
            add(tools, BorderLayout.SOUTH);
     
        setMinimumSize(new Dimension(100,100));
        setPreferredSize(new Dimension(300,300));
    }
    
    public MapView getMap(){
        return map;
    }
}
