/*
 * Camera.java
 *
 * Created on 23. april 2008, 16:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.viewer;

import java.awt.Component;
import java.awt.image.BufferedImage;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;

/**
 *
 * @author runaas
 */
public interface Camera {
    public  boolean isRunning();
    public  void start();
    public  void stop();
    public  void shutdown();
    
    public  Component getCanvas();
    public  Component getGraphicsView();
    
    public  BufferedImage getCapture();
    public void addPostDrawListener(PostDrawListener pdl);
    
    public ViewerManager getViewerManager();
    public GlobeNavigator getNavigator();
}
