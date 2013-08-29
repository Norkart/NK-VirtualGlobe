//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.jogl;

import com.norkart.virtualglobe.globesurface.BttSurface;
import com.norkart.virtualglobe.globesurface.BttSurfaceView;
import com.norkart.virtualglobe.viewer.AbstractCamera;
import com.norkart.virtualglobe.globesurface.GLSettings;
import com.norkart.virtualglobe.viewer.GlobeSurfaceGraphics;
import com.norkart.virtualglobe.viewer.ViewerManager;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class JOGLGlobeSurfaceGraphics implements GlobeSurfaceGraphics {
    // The surface to be drawn
    private BttSurface surface;
    // The graphics core
    private ViewerManager manager;
    // THe camera - view relation
    private ArrayList cameraTable = new ArrayList();
    // Is this under deletion?
    private boolean isDeleting = false;
    
    private Thread cameraCleaner;
    
    private GLU glu = new GLU();
    
    private float transparency = 1;
    private boolean wireframe = false;
    
    // The active views for a camera
    
  /*
  private long prev_time;
  private long sum_loop_time;
  private long sum_wait_time;
  private long sum_render_time;
   */
    public JOGLGlobeSurfaceGraphics(ViewerManager core, BttSurface surface) {
        this.surface = surface;
        this.manager    = core;
        
        
        cameraCleaner = new Thread() {
            public void run() {
                while (!isDeleting) {
                    try {
                        sleep(1000);
                    } catch(InterruptedException ex) {}
                    if (!isDeleting)
                        getView(null);
                }
            }
        };
        cameraCleaner.start();
    }
    
    public BttSurface getSurface() {
        return surface;
    }
    
    public void setTransparency(float transparency) {
        this.transparency = transparency;
    }
    
    
    public void      setWireframe(boolean b) { wireframe = b; }
    public boolean   isWireframe() { return wireframe; }
    
    private class CameraView {
        private BttSurfaceView view;
        private long lastused;
        private AbstractCamera camera;
        
        private CameraView(BttSurfaceView view, AbstractCamera camera) {
            this.view   = view;
            this.camera = camera;
            lastused = System.currentTimeMillis();
        }
    }
    
    
    protected BttSurfaceView getView(JOGLCamera camera) {
        BttSurfaceView view = null;
        synchronized (cameraTable) {
            Iterator it = cameraTable.iterator();
            long current_time = System.currentTimeMillis();
            while (it.hasNext()) {
                CameraView cv = (CameraView)it.next();
                if (cv.camera == camera) {
                    view = cv.view;
                    cv.lastused = current_time;
                } else if (current_time - cv.lastused > 5000) {
                    it.remove();
                    cv.view.close();
                }
            }
            
            if (view == null && camera != null) {
                CameraView cv = null;
                if (camera instanceof JOGLMapView)
                    cv = new CameraView(new BttSurfaceView(surface, 1<<13), camera);
                else
                    cv = new CameraView(new BttSurfaceView(surface), camera);
                view = cv.view;
                manager.addOriginUpdateListener(view);
                cameraTable.add(cv);
            }
        }
        return view;
    }
    
    void draw(JOGLCamera camera, GLAutoDrawable drawable) {
        if (isDeleting) return;
        BttSurfaceView view = getView(camera);
        if (isDeleting) return;
        /*
        if (!camera.getFrustum().isPerspective()) {
            javax.vecmath.Vector4f[] planes = camera.getFrustum().getPlanes();
            System.out.println(camera.getFrustum().getResolution() + " : " + planes[0] + " : " + planes[1] + " : " + planes[2] + " : " + planes[3] + " : " + planes[4] + " : " + planes[5]);
        }
        */              
        view.initiateUpdate(camera.getFrustum());
        if (isDeleting) return;
        GL  gl  = drawable.getGL();
        GLSettings gl_cap = GLSettings.get(gl);
        
        if (transparency != 1) {
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA,
                    GL.GL_ONE_MINUS_SRC_ALPHA);
        }
        
        // Set up the drawing environment
        gl.glDisable(GL.GL_COLOR_MATERIAL);
        
        if (gl_cap.hasSeparateSpecularColor())
            gl.glLightModeli(GL.GL_LIGHT_MODEL_COLOR_CONTROL, gl_cap.separateSpecularCommand());
        
        // Test for vertex buffer objects
        
        float spec_int  = .3f;
        float[] white    = {1, 1, 1, transparency};
        float[] black    = {0, 0, 0, 1};
        float[] spec_mat = {spec_int, spec_int, spec_int, 1f};
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, white, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, white, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, spec_mat, 0);
        gl.glMateriali(GL.GL_FRONT, GL.GL_SHININESS, 64);
        
        // WIREFRAME?
        gl.glPolygonMode(GL.GL_FRONT, isWireframe() ? GL.GL_LINE : GL.GL_FILL );
        view.render(gl, glu);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
        if (transparency != 1)
            gl.glDisable(GL.GL_BLEND);
    }
    
    public void clear() {
        isDeleting = true;
        synchronized (cameraTable) {
            Iterator it = cameraTable.iterator();
            while (it.hasNext()) {
                CameraView cv = (CameraView)it.next();
                it.remove();
                cv.view.close();
            }
        }
        manager.removeGlobeSurface(surface);
       
        surface     = null;
        manager        = null;
        cameraTable = null;
    }
}