//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.jogl;

import com.norkart.virtualglobe.viewer.CullFrustum;
import com.norkart.virtualglobe.globesurface.GLCleanup;
import com.norkart.virtualglobe.util.ApplicationSettings;
import com.norkart.virtualglobe.viewer.AbstractCamera;
import com.norkart.virtualglobe.viewer.PostDrawListener;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;

import java.util.*;

import java.nio.FloatBuffer;

import javax.vecmath.*;
import javax.swing.JPanel;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.awt.BorderLayout;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
// import java.awt.event.ComponentListener;
// import java.awt.event.ComponentEvent;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.awt.EventQueue;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import com.sun.opengl.util.Animator;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public abstract class JOGLCamera extends AbstractCamera
        implements GLEventListener, WindowListener /*, ComponentListener */ {
    
    // The gl drawable
    protected GLCanvas glcanvas;
    
    
    // For geometric calculations of view matrixes frustum etc...
    protected Matrix4f inv_view_mat = new Matrix4f();
    protected float modl[] = new float[16];
    protected float proj[] = new float[16];
    protected Vector4f planes[] = new Vector4f[6];
    protected Point3f  camera_center = new Point3f();
    protected CullFrustum frustum = new CullFrustum();
    protected long lastfast = 0;

    // The main render loop
    protected class CameraAnimator extends Animator {
        Thread thread;
        Runnable runnable;
        boolean shouldStop = false;
        
        public CameraAnimator() {
            super(glcanvas);
        }
        
        class MainLoop implements Runnable {
            public void run() {
                long prevframetime = System.currentTimeMillis();
                try {
                    while (!shouldStop) {
                        display();
                        if (shouldStop) break;
                        long currtime = System.currentTimeMillis();
                        long timeleft = (currtime - lastfast > 200 && !hasActiveUpdater()) ?
                                        (long)(1000.f/manager.getBaseFps()) :
                                        (long)(1000.f/ApplicationSettings.getApplicationSettings().getMaxFPS());
                        timeleft -= (currtime - prevframetime);
                        if (timeleft > 0) {
                            synchronized (JOGLCamera.this) {
                                try {JOGLCamera.this.wait(timeleft);}  catch (InterruptedException ex) {}
                            }
                            
                        } else
                            Thread.yield();
                        prevframetime = System.currentTimeMillis();
                    }
                } finally {
                    shouldStop = false;
                    synchronized (CameraAnimator.this) {
                        thread = null;
                        CameraAnimator.this.notify();
                    }
                }
            }
        }
        
        // Starts this animator.
        public synchronized void start() {
            if (thread != null) {
                throw new GLException("Already started");
            }
            if (runnable == null) {
                runnable = new MainLoop();
            }
            thread = new Thread(runnable, "JOGL Animator");
            thread.start();
        }
        
        // Indicates whether this animator is currently running. This
        // should only be used as a heuristic to applications because in
        // some circumstances the Animator may be in the process of
        // shutting down and this method will still return true.
        public synchronized boolean isAnimating() {
            return (thread != null);
        }
        
        // Stops this animator. In most situations this method blocks until
        // completion, except when called from the animation thread itself
        // or in some cases from an implementation-internal thread like the
        // AWT event queue thread.
        public synchronized void stop() {
            shouldStop = true;
            notifyAll();
            // It's hard to tell whether the thread which calls stop() has
            // dependencies on the Animator's internal thread. Currently we
            // use a couple of heuristics to determine whether we should do
            // the blocking wait().
            if ((Thread.currentThread() == thread) || EventQueue.isDispatchThread()) {
                return;
            }
            while (shouldStop && thread != null) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                }
            }
        }
    }
    protected CameraAnimator animator;
    

    
    /**
     * Constructor, just create panels, the rest is done in subclasses
     */
    JOGLCamera(ViewerManager mgr, GlobeNavigator navigator) {
        super(mgr, navigator);
        
        for (int i=0;i<6;i++)
            planes[i] = new Vector4f();
        
        // GLAutoDrawableFactory fac = GLAutoDrawableFactory.getFactory();
        GLCapabilities cap = new GLCapabilities();
        
        AbstractCamera referenceCamera = mgr.getCamera(0);
        GLContext referenceContext = referenceCamera == null ? null : ((GLCanvas)referenceCamera.getCanvas()).getContext();
        if (referenceContext != null)
            referenceContext.setSynchronized(true);
        
        // Antialiasing
        int multisampleSettings = ApplicationSettings.getApplicationSettings().getMultisampleSettings();
        if (multisampleSettings > ApplicationSettings.MULTISAMPLE_NONE) {
            int aaLevel = multisampleSettings*2;
            System.out.println("Using Antialiasing level: " + aaLevel);
            cap.setSampleBuffers(true);
            cap.setNumSamples(aaLevel);
            glcanvas = new GLCanvas(cap, new MultisampleChooser(), referenceContext, null);
        } else
            glcanvas = new GLCanvas(cap, null, referenceContext, null);
        // glcanvas.setGL(new TraceGL(glcanvas.getGL(), System.err));
        // glcanvas.setGL(new DebugGL(glcanvas.getGL()));
        
        glcanvas.addGLEventListener(this);
        
        // ((GLCanvas)glcanvas).setAutoSwapBufferMode(false);
        // ((Component)glcanvas).setIgnoreRepaint(true);
        
        // GLContext canvasContext = ((GLAutoDrawable)glcanvas).getContext();
        // canvasContext.setSynchronized(true);
        
        PositionListener pos_listener = new PositionListener();
        postDrawListeners.add(pos_listener);
        glcanvas.addMouseListener(pos_listener);
        glcanvas.addMouseMotionListener(pos_listener);
        graphics_view.add(glcanvas,  BorderLayout.CENTER);
        
        animator = new CameraAnimator();
    }
    
    public void start() {
        if (animator == null) return;
        try {
            animator.start();
        } catch (javax.media.opengl.GLException ex) {}
    }
    
    public void stop() {
        if (animator == null) return;
        try {
            animator.stop();
        } catch (javax.media.opengl.GLException ex) {}
    }
    
    public void shutdown() {
        if (animator == null) return;
        try {
            animator.stop();
        } catch (javax.media.opengl.GLException ex) {}
        animator = null;
    }
    
    //-------------------------------------------------
    // WIndowListener methods
    //-------------------------------------------------
    
    public void windowOpened(WindowEvent evt) {
        start();
    }
    
    public void windowClosing(WindowEvent e) {
        stop();
    }
    public void windowActivated(WindowEvent evt) {
        start();
    }
    
    public void windowDeactivated(WindowEvent evt) { }
    
    public void windowDeiconified(WindowEvent evt) {
        start();
    }
    
    public void windowIconified(WindowEvent evt) {
        stop();
    }
    
    public void windowClosed(WindowEvent evt) {
        shutdown();
    }
    
    //-------------------------------------------
    // Own class methods
    //-------------------------------------------
    
    public void drawNow(Object originator) {
        lastfast = System.currentTimeMillis();
        synchronized (this) {
            notify();
        }
    }

    
    public boolean isRunning() {
        return animator.isAnimating();
    }
    
    public Component getCanvas() {
        return glcanvas;
    }
    
    /**
     * Get the panel that contains the drawable
     * @return
     */
    public Component getGraphicsView() {
        return graphics_view;
    }
    
    
    /**
     * Get the current frustum object
     * @return frustum
     */
    public    CullFrustum getFrustum() {
        return frustum;
    }
    
    // Simple class to warn if results are not going to be as expected
    static private class MultisampleChooser extends DefaultGLCapabilitiesChooser {
        public int chooseCapabilities(GLCapabilities desired,
                GLCapabilities[] available,
                int windowSystemRecommendedChoice) {
            boolean anyHaveSampleBuffers = false;
            for (int i = 0; i < available.length; i++) {
                GLCapabilities caps = available[i];
                if (caps != null && caps.getSampleBuffers()) {
                    anyHaveSampleBuffers = true;
                    break;
                }
            }
            int selection = super.chooseCapabilities(desired, available, windowSystemRecommendedChoice);
            if (!anyHaveSampleBuffers) {
                System.err.println("WARNING: antialiasing will be disabled because none of the available pixel formats had it to offer");
            } else if (!available[selection].getSampleBuffers()) {
                System.err.println("WARNING: antialiasing will be disabled because the DefaultGLCapabilitiesChooser didn't supply it");
            } else {
                System.err.println("Num sample buffers: " + available[selection].getNumSamples());
            }
            return selection;
        }
    }
    
    
    // Geometric methods for computing view matrixes and clipping planes
    private  float clip[] = new float[16];
    protected void clippingPlanes(float modl[], float proj[], Vector4f planes[]) {
        // Compute clipping planes
        
        // Combine The Two Matrices (Multiply Projection By Modelview)
        clip[ 0] = modl[ 0] * proj[ 0] + modl[ 1] * proj[ 4] + modl[ 2] * proj[ 8] + modl[ 3] * proj[12];
        clip[ 1] = modl[ 0] * proj[ 1] + modl[ 1] * proj[ 5] + modl[ 2] * proj[ 9] + modl[ 3] * proj[13];
        clip[ 2] = modl[ 0] * proj[ 2] + modl[ 1] * proj[ 6] + modl[ 2] * proj[10] + modl[ 3] * proj[14];
        clip[ 3] = modl[ 0] * proj[ 3] + modl[ 1] * proj[ 7] + modl[ 2] * proj[11] + modl[ 3] * proj[15];
        
        clip[ 4] = modl[ 4] * proj[ 0] + modl[ 5] * proj[ 4] + modl[ 6] * proj[ 8] + modl[ 7] * proj[12];
        clip[ 5] = modl[ 4] * proj[ 1] + modl[ 5] * proj[ 5] + modl[ 6] * proj[ 9] + modl[ 7] * proj[13];
        clip[ 6] = modl[ 4] * proj[ 2] + modl[ 5] * proj[ 6] + modl[ 6] * proj[10] + modl[ 7] * proj[14];
        clip[ 7] = modl[ 4] * proj[ 3] + modl[ 5] * proj[ 7] + modl[ 6] * proj[11] + modl[ 7] * proj[15];
        
        clip[ 8] = modl[ 8] * proj[ 0] + modl[ 9] * proj[ 4] + modl[10] * proj[ 8] + modl[11] * proj[12];
        clip[ 9] = modl[ 8] * proj[ 1] + modl[ 9] * proj[ 5] + modl[10] * proj[ 9] + modl[11] * proj[13];
        clip[10] = modl[ 8] * proj[ 2] + modl[ 9] * proj[ 6] + modl[10] * proj[10] + modl[11] * proj[14];
        clip[11] = modl[ 8] * proj[ 3] + modl[ 9] * proj[ 7] + modl[10] * proj[11] + modl[11] * proj[15];
        
        clip[12] = modl[12] * proj[ 0] + modl[13] * proj[ 4] + modl[14] * proj[ 8] + modl[15] * proj[12];
        clip[13] = modl[12] * proj[ 1] + modl[13] * proj[ 5] + modl[14] * proj[ 9] + modl[15] * proj[13];
        clip[14] = modl[12] * proj[ 2] + modl[13] * proj[ 6] + modl[14] * proj[10] + modl[15] * proj[14];
        clip[15] = modl[12] * proj[ 3] + modl[13] * proj[ 7] + modl[14] * proj[11] + modl[15] * proj[15];
        
        // Clip planes
        // Extract The Numbers For The RIGHT Plane
        planes[0].x = clip[ 3] - clip[ 0];
        planes[0].y = clip[ 7] - clip[ 4];
        planes[0].z = clip[11] - clip[ 8];
        planes[0].w = clip[15] - clip[12];
        
        // Extract The Numbers For The LEFT Plane
        planes[1].x = clip[ 3] + clip[ 0];
        planes[1].y = clip[ 7] + clip[ 4];
        planes[1].z = clip[11] + clip[ 8];
        planes[1].w = clip[15] + clip[12];
        
        // Extract The BOTTOM Plane
        planes[2].x = clip[ 3] + clip[ 1];
        planes[2].y = clip[ 7] + clip[ 5];
        planes[2].z = clip[11] + clip[ 9];
        planes[2].w = clip[15] + clip[13];
        
        // Extract The TOP Plane
        planes[3].x = clip[ 3] - clip[ 1];
        planes[3].y = clip[ 7] - clip[ 5];
        planes[3].z = clip[11] - clip[ 9];
        planes[3].w = clip[15] - clip[13];
        
        // Extract The FAR Plane
        planes[4].x = clip[ 3] - clip[ 2];
        planes[4].y = clip[ 7] - clip[ 6];
        planes[4].z = clip[11] - clip[10];
        planes[4].w = clip[15] - clip[14];
        
        // Extract The NEAR Plane
        planes[5].x = clip[ 3] + clip[ 2];
        planes[5].y = clip[ 7] + clip[ 6];
        planes[5].z = clip[11] + clip[10];
        planes[5].w = clip[15] + clip[14];
        
        // Normalize clip planes
        for (int i=0;i<6;i++) {
            float x = planes[i].x;
            float y = planes[i].y;
            float z = planes[i].z;
            float d = (float)Math.sqrt(x*x+y*y+z*z);
            planes[i].scale(1/d);
        }
    }
    
    private Matrix4f view_mat = new Matrix4f();
    protected void viewMatrix(Matrix4f inv_view_mat, float modl[], Point3f camera_center) {
        
        // Camera center
        camera_center.set(0.f,0.f,0.f);
        inv_view_mat.transform(camera_center);
        
        // Invert view matrix
        try {
            view_mat.invert(inv_view_mat);
        } catch (SingularMatrixException ex) {
            view_mat.setIdentity();
        }
        
        // Transpose in place (for openGL)
        modl[0] = view_mat.m00;
        modl[1] = view_mat.m10;
        modl[2] = view_mat.m20;
        modl[3] = view_mat.m30;
        modl[4] = view_mat.m01;
        modl[5] = view_mat.m11;
        modl[6] = view_mat.m21;
        modl[7] = view_mat.m31;
        modl[8] = view_mat.m02;
        modl[9] = view_mat.m12;
        modl[10] = view_mat.m22;
        modl[11] = view_mat.m32;
        modl[12] = view_mat.m03;
        modl[13] = view_mat.m13;
        modl[14] = view_mat.m23;
        modl[15] = view_mat.m33;
    }
    
    
    
    void draw(GLAutoDrawable drawable, int width, int height) {
        GL gl = drawable.getGL();
        // Do the drawing
        // Free old textures
        GLCleanup.cleanupAll(gl, glu);
        
        // Do drawing
        ((JOGLViewerManager)manager).draw(this, drawable);
        
        // Test for camera-origin distance
        // Compute tolerance (elevation dependent)
        float tol = Math.max(0.01f, (float)getNavigator().getTerrainHeight()*getFrustum().getResolution());
        manager.testOrigin(getFrustum().getCameraCenter(), tol);
        
        // After drawing
        synchronized (postDrawListeners) {
            for (PostDrawListener pdl : postDrawListeners) {
                pdl.postDraw(gl);
            }
        }
    }
}
