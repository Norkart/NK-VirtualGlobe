//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.gui;

import com.norkart.virtualglobe.MapPanel;
import com.norkart.virtualglobe.viewer.PerspectiveCamera;
import com.norkart.virtualglobe.viewer.MapView;
import com.norkart.virtualglobe.viewer.navigator.MapNavigationAdapter;

import com.norkart.virtualglobe.util.ApplicationSettings;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.HeadlessException;
import java.awt.event.WindowListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Component;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class MapDialog extends JDialog {
    private MapPanel mapPanel;
    
    public MapDialog(JFrame frame, PerspectiveCamera camera) throws HeadlessException {
        super(frame, ApplicationSettings.getApplicationSettings().getResourceString("MAP_DIALOG"));
        
        mapPanel = new MapPanel(camera);
        MapView map = mapPanel.getMap();
        if (map instanceof WindowListener)
            addWindowListener((WindowListener)map);
        getContentPane().add(mapPanel, BorderLayout.CENTER);
        pack();
    }
}