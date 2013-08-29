//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.jogl;

import com.norkart.virtualglobe.viewer.AbstractCamera;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;
import com.norkart.virtualglobe.viewer.navigator.MapNavigationAdapter;
import com.norkart.virtualglobe.viewer.LightModel;
import com.norkart.virtualglobe.viewer.MapView;
import com.norkart.virtualglobe.viewer.PerspectiveCamera;

import javax.vecmath.*;
import javax.media.opengl.*;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.KeyEvent;
import java.awt.BorderLayout;

import javax.swing.SwingUtilities;
import javax.swing.JPanel;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class JOGLMapView extends JOGLCamera implements MapView {
    //private int width, height;
    private float horizon_scale = 4;
    private boolean north_up = false;
    
    // private long prevframetime = 0;
    private PerspectiveCamera camera;
    
    JOGLMapView(PerspectiveCamera camera) {
        super(camera.getViewerManager(), camera.getNavigator());
        this.camera = camera;
    }
    
    public float getHorizonScale() {
        return horizon_scale;
    }
    
    public void setHorizonScale(float horizon_scale) {
        this.horizon_scale = horizon_scale;
    }
    
    public void setNorthUp(boolean north_up) {
        this.north_up = north_up;
    }
    
    public boolean isNorthUp(){
        return north_up;
    }
    
    // JOGL methods
    /**
     * From interface GLEventListener
     * Initialize graphic system
     * @param drawable
     */
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glClearColor(0.2f, 0.2f, 0.5f, 1.0f);
        
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_DEPTH_TEST);
        // gl.glDepthFunc(GL.GL_LESS);
    }
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // this.width  = width;
        // this.height = height;
    }
    
    /**
     * Stop waiting for updates and release graphics for drawing
     */
    public void drawNow(Object originator) {
        if (originator instanceof MapNavigationAdapter) {
            super.drawNow(originator);
        }
    }
    protected boolean hasActiveUpdater()  { return false; }
    
    public void display(GLAutoDrawable drawable) {
        draw(drawable, drawable.getWidth(), drawable.getHeight());
    }
    
    public void draw(GLAutoDrawable drawable, int width, int height) {
        GL gl = drawable.getGL();
        
        // Compute scale from elevation
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        float w = (float)(.254*width/dpi);
        float h = (float)(.254*height/dpi);
        
        double ch = Math.max(navigator.getEllipsHeight(),
                navigator.getTerrainHeight());
        double r = navigator.getGlobe() == null ? 0 :
            navigator.getGlobe().getEllipsoid().getN(navigator.getLat());
        
        // Near and far clipping planes
        // double horizon  = 0;
        // horizon += Math.sqrt((2*r + 100.)*100.);
        double horizon = Math.sqrt((2*r + ch)*ch);
        horizon /= horizon_scale;
        
        double scale = horizon / Math.min(w, h);
        
        // ((MapNavigationAdapter)navigationAdapter).setScaleFactor(scale_factor);
        
        gl.glClearColor(0f, 0f, 0f, 1f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        
        // Projection matrix
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        
        w *= scale;
        h *= scale;
        gl.glOrtho(-w/2, w/2, -h/2, h/2, 1000, 10000000);
        // glu.gluPerspective(fov, aspect, Math.cos(Math.toRadians(fov/2))*near, far);
        gl.glGetFloatv(GL.GL_PROJECTION_MATRIX, proj, 0);
        
        // Modelview matrix
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        
        // Lights
        // This code sets up a light and enables it
        gl.glEnable(GL.GL_LIGHTING);
        
        LightModel light_model = manager.getLightModel();
        float spec_int    = light_model.getSpecularIntensity();
        float diffuse_int = light_model.getDiffuseIntensity();
        float ambient_int = light_model.getAmbientIntensity();
        
        float specular[] = {spec_int, spec_int, spec_int, 1};
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, specular, 0);
        float diffuse[] = {diffuse_int, diffuse_int, diffuse_int, 1};
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, diffuse, 0);
        float ambient[] = {ambient_int, ambient_int, ambient_int, 1};
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, ambient, 0);
        gl.glEnable(GL.GL_LIGHT1); //enable the light
        
        // set light position
        float lightpos[] = { -.7071f, .7071f, .8660f, 0.0f };
        gl.glLightfv(GL.GL_LIGHT1,GL.GL_POSITION, lightpos, 0);
        
        
        // Set the view matrix
        // Compute from navigator data
        
        inv_view_mat = navigator.computeViewMatrix(navigator.getLat(), navigator.getLon(),
                100000, north_up?0:navigator.getAzimut(), -Math.PI/2, inv_view_mat);
        viewMatrix(inv_view_mat, modl, camera_center);
        // Set modelview matrix
        if (!north_up)
            gl.glTranslatef(0,-h/4,0);
        gl.glMultMatrixf(modl, 0);
        
        
        // Compute clipping planes
        clippingPlanes(modl, proj, planes);
        
        frustum.setValues(planes, camera_center,
                navigator.getOrigin(),
                (float)(.254*scale/dpi), false);
        
        // Do the drawing
        super.draw(drawable, width, height);
        
        // Save matrixes
        // gl.glPushMatrix();
        
        gl.glMatrixMode(GL.GL_PROJECTION);
        // gl.glPushMatrix();
        gl.glLoadIdentity();
        
        h = 2;
        w = 2*width;
        w /= height;
        float y_offs = north_up?0:-.5f;
        gl.glOrtho(-w/2, w/2, -h/2-y_offs, h/2-y_offs, -1, 1);
        
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        
        // Draw camera view angle
        double az = navigator.getAzimut();
        double cv = Math.toRadians(camera.getFov());
        
        if (north_up)
            gl.glRotatef((float)Math.toDegrees(az), 0,0,-100);
        
        gl.glDepthMask(false);
        gl.glDisable(GL.GL_LIGHT1);
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_LIGHTING);
        
        float [] curr_col = new float[4];
        gl.glGetFloatv(GL.GL_CURRENT_COLOR, curr_col, 0);
        
        // Draw camera frustum wedge
        gl.glColor3f(.9f,.9f,.9f);
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex2f((float)(2*Math.sin(-cv/2)), (float)(2*Math.cos(-cv/2)));
        gl.glVertex2f(0, 0);
        gl.glVertex2f((float)(2*Math.sin(cv/2)),  (float)(2*Math.cos(cv/2)));
        gl.glEnd();
        
        // Draw north arrow
        float s8 = (float)Math.sin(Math.PI/8);
        float c8 = (float)Math.cos(Math.PI/8);
        float s4 = (float)Math.sin(Math.PI/4);
        float c4 = (float)Math.cos(Math.PI/4);
        
        gl.glRotatef((float)Math.toDegrees(az), 0,0,100);
        gl.glScalef(.1f, .1f, .1f);
        gl.glColor3f(0,0,1);
        
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex2f(0,4);
        gl.glVertex2f(s8, c8);
        gl.glVertex2f(2*s4,2*c4);
        gl.glVertex2f(c8,s8);
        gl.glVertex2f(2,0);
        gl.glVertex2f(c8,-s8);
        gl.glVertex2f(2*s4,-2*c4);
        gl.glVertex2f(s8,-c8);
        gl.glVertex2f(0,-2);
        gl.glVertex2f(-s8,-c8);
        gl.glVertex2f(-2*s4,-2*c4);
        gl.glVertex2f(-c8,-s8);
        gl.glVertex2f(-2,0);
        gl.glVertex2f(-c8,s8);
        gl.glVertex2f(-2*s4,2*c4);
        gl.glVertex2f(-s8,c8);
        gl.glVertex2f(0,4);
        gl.glEnd();
        
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex2f(-.5f,4);
        gl.glVertex2f(-.5f,5);
        gl.glVertex2f(.5f,4);
        gl.glVertex2f(.5f,5);
        gl.glEnd();
        // Restore matrixes
        // gl.glPopMatrix();
        
        gl.glDepthMask(true);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glColor3fv(curr_col, 0);
        gl.glLoadIdentity();
        
        // gl.glPopMatrix();
    /*
    long currtime = System.currentTimeMillis();
    prevframetime = currtime - prevframetime;
    long timeleft = (long)(1000./10)-prevframetime;
    if (timeleft > 0) {
      synchronized (this) {
        try {wait(timeleft);}
        catch (InterruptedException ex) {}
      }
    }
    else
      Thread.yield();
    prevframetime = currtime;
     */
    }
    
    public  Component getCameraPanel( ) { return null; }
    
    public  BufferedImage getCapture() { return null; }
}