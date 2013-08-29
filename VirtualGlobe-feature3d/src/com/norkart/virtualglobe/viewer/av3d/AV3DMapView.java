/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  AV3DMapView.java
 *
 * Created on 9. mai 2008, 15:42
 *
 */

package com.norkart.virtualglobe.viewer.av3d;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import com.norkart.virtualglobe.viewer.MapView;
import com.norkart.virtualglobe.viewer.PerspectiveCamera;

import org.j3d.aviatrix3d.ViewEnvironment;
import org.j3d.aviatrix3d.LineStripArray;
import org.j3d.aviatrix3d.Shape3D;
import org.j3d.aviatrix3d.Appearance;
import org.j3d.aviatrix3d.Material;
import org.j3d.aviatrix3d.TransformGroup;
import org.j3d.aviatrix3d.rendering.ProfilingData;

import javax.media.opengl.GL;

// import javax.vecmath.SingularMatrixException;
import javax.vecmath.Matrix4f;

/**
 *
 * @author runaas
 */
public class AV3DMapView extends AV3DCamera implements MapView {
    private PerspectiveCamera camera;
    private float horizon_scale = 4;
    private boolean north_up = false;
    private double[] frustum_dim = new double[6];
    private float resolution = 1000;

    
    /** Creates a new instance of AV3DMapView */
    public AV3DMapView(PerspectiveCamera camera) {
        super(camera.getViewerManager(), camera.getNavigator());
        this.camera = camera;
        
        
        scene.getViewEnvironment().setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
    }
    
 
    public void updateSceneGraph() {
        super.updateSceneGraph();
        
        int width  = layer.getViewport().getWidth();
        int height = layer.getViewport().getHeight();
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
        
        w *= scale;
        h *= scale;
        
        scene.getViewEnvironment().setOrthoParams(-w/2, w/2, -h/2, h/2);
        
        double view_h = Math.max(10*horizon, navigator.getEllipsHeight());
        
        matrix = navigator.computeViewMatrix(navigator.getLat(), navigator.getLon(),
                view_h, north_up?0:navigator.getAzimut(), -Math.PI/2, matrix);
        
        scene.getViewEnvironment().setClipDistance(view_h-horizon, view_h+horizon);
        
        if (!north_up) {
            matrix.m03 += h/4*matrix.m01;
            matrix.m13 += h/4*matrix.m11;
            matrix.m23 += h/4*matrix.m21;
        }

        resolution = (float)(.254*scale/dpi);
        
        scene.getViewEnvironment().getViewFrustum(frustum_dim);
        computeFrustum(frustum_dim, matrix, frustumPlanes, false);
    }
    
    
    public void	postDraw(GL gl, ProfilingData prof_data, java.lang.Object userData) {
        gl.glMatrixMode(GL.GL_PROJECTION);
        // gl.glPushMatrix();
        gl.glLoadIdentity();
        
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        
        float h = 2;
        float w = 2*width;
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
    
    public float getResolution() {
        return resolution;
    }
    
    public  Component getCameraPanel( ) { return null; }
    
    public  BufferedImage getCapture() { return null; }
}
