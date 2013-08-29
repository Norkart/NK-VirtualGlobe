//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.navigator;


import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import javax.swing.SwingUtilities;
import java.awt.Toolkit;
import java.awt.Component;

import com.norkart.virtualglobe.viewer.AbstractCamera;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public abstract class MouseKeyNavigationAdapter
        implements MouseListener, MouseMotionListener,
        KeyListener, MouseWheelListener {
    protected GlobeNavigator navigator;
    
    
    protected MouseKeyNavigationAdapter(GlobeNavigator navigator) {
        this.navigator = navigator;
    }
    
    /*public void setNavigator(GlobeNavigator navigator) {
        this.navigator = navigator;
    }
    */
    public GlobeNavigator getNavigator() {
        return navigator;
    }
    
    public void mouseClicked(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mouseMoved(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {
    }
    public void mouseWheelMoved(MouseWheelEvent e) {
    }
    public void keyPressed(KeyEvent e) {
    }
    public void keyReleased(KeyEvent e) {
    }
    public void keyTyped(KeyEvent e) {
    }
}