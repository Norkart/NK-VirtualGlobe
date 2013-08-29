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
import com.norkart.virtualglobe.viewer.ViewerManager;

import com.norkart.virtualglobe.globesurface.GLSettings;
import com.norkart.virtualglobe.globesurface.GLCleanup;
import com.norkart.virtualglobe.util.ApplicationSettings;
import com.norkart.virtualglobe.viewer.LightModel;
import com.norkart.virtualglobe.viewer.PerspectiveCamera;
import com.norkart.virtualglobe.viewer.PerspectiveCameraPanel;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;

//import com.norkart.VirtualGlobe.Util.Navigation.GlobeNavigator;
//import com.norkart.VirtualGlobe.Util.Navigation.Navigator;
//import com.norkart.VirtualGlobe.Util.Navigation.WalkPerspectiveNavigationAdapter;

import javax.vecmath.*;
import javax.media.opengl.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;




/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class JOGLPerspectiveCamera extends JOGLCamera implements PerspectiveCamera {
    
    // private PerspectiveCameraPanel camera_panel;
    
    private float fov = 40;
    private float pixel_factor = 3;
    
    private final float fogColor[] = {.3f, .3f, .6f, 1f};   // fog color
    
    
    
/*
  private class CaptureRequester {
    BufferedImage image = null;
    boolean request = false;
  };
  private CaptureRequester captureRequester = new CaptureRequester();
 */
    private class Offscreen implements GLEventListener {
        GLPbuffer pbuffer;
        boolean   created = false;
        boolean   finnished = false;
        int width, height;
        BufferedImage image = null;
        
        public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
        public void init(GLAutoDrawable drawable) {
            // this.GLAutoDrawable = drawable;
            GL gl = drawable.getGL();
            gl.glColor3f(1.0f, 1.0f, 1.0f);
            gl.glClearColor(0.2f, 0.2f, 0.5f, 1.0f);
            
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glEnable(GL.GL_DEPTH_TEST);
        }
        public void display(GLAutoDrawable drawable) {
            draw(drawable, width, height);
            
            ColorModel cm = glAlphaColorModel;
            int num_comp = 4;
            int format = GL.GL_RGBA;
            GL gl = drawable.getGL();
            
            // Assure drawing is completed
            gl.glFinish();
            
            // Get data into a buffer
            ByteBuffer imageBuffer = ByteBuffer.allocateDirect(width*height*num_comp);
            imageBuffer.order(ByteOrder.nativeOrder());
            gl.glReadPixels(0, 0, width, height,
                    format, GL.GL_UNSIGNED_BYTE, imageBuffer);
            imageBuffer.rewind();
            
            // Copy to buffered image
            WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, num_comp, null);
            byte[] data = ((DataBufferByte) raster.getDataBuffer()).getData();
            for (int i = height-1; i >= 0; --i)
                imageBuffer.get(data, i*width*num_comp, width*num_comp);
            synchronized (this) {
                image = new BufferedImage(cm, raster, false, new Hashtable());
                notifyAll();
            }
        }
        
        public void create(GLAutoDrawable reference_drawable) {
            if (created) return;
            // Antialiasing
            GLCapabilities cap = new GLCapabilities();
            int multisampleSettings = ApplicationSettings.getApplicationSettings().getMultisampleSettings();
            if (multisampleSettings > ApplicationSettings.MULTISAMPLE_NONE) {
                int aaLevel = multisampleSettings*2;
                System.out.println("Using Antialiasing level: " + aaLevel);
                cap.setSampleBuffers(true);
                cap.setNumSamples(aaLevel);
            }
            pbuffer = GLDrawableFactory.getFactory().createGLPbuffer(cap, new DefaultGLCapabilitiesChooser(), width, height, reference_drawable.getContext());
            pbuffer.addGLEventListener(this);
            created = true;
        }
        
        Offscreen() {
            offscreens.add(this);
        }
    }
    Vector offscreens = new Vector();
    
    
    private class OffscreenImageCapture extends Offscreen {
        
        OffscreenImageCapture() {
            super();
        }
        public void create(GLAutoDrawable reference_drawable) {
            width  = reference_drawable.getWidth();
            height = reference_drawable.getHeight();
            super.create(reference_drawable);
        }
        public void display(GLAutoDrawable drawable) {
            super.display(drawable);
            finnished = true;
        }
    }
    
    public JOGLPerspectiveCamera(ViewerManager mgr, GlobeNavigator nav) {
        super(mgr, nav);
        // The navigation adapter
    }
    
    
    
    
    
    public float getFov() {
        return fov;
    }
    public void  setFov(float fov) {
        this.fov = fov;
    }
    
    public float getDetailSizeFactor() {
        return pixel_factor;
    }
    public void  setDetailSizeFactor(float detail_factor) {
        pixel_factor = detail_factor;
    }
    
    /**
     * From interface GLEventListener
     * Initialize graphic system
     * @param drawable
     */
    public void init(GLAutoDrawable drawable) {
        // this.GLAutoDrawable = drawable;
        GL gl = drawable.getGL();
        
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glClearColor(0.2f, 0.2f, 0.5f, 1.0f);
        
        System.out.print("OpenGL vendor: ");
        System.out.println(gl.glGetString(GL.GL_VENDOR));
        System.out.print("OpenGL renderer: ");
        System.out.println(gl.glGetString(GL.GL_RENDERER));
        System.out.print("OpenGL version: ");
        System.out.println(gl.glGetString(GL.GL_VERSION));
        System.out.print("OpenGL extensions: ");
        System.out.println(gl.glGetString(GL.GL_EXTENSIONS));
        
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_DEPTH_TEST);
        // gl.glDepthFunc(GL.GL_LESS);
    }
    
    /**
     * From interface GLEventListener
     * Set up viewing system based on this drawable size
     * @param drawable
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }
    
    protected void draw(GLAutoDrawable drawable, int width, int height) {
        GL gl = drawable.getGL();
        
        // Compute near and far clipping planes and background color
        double near = 1000., far = 20000000.;
        double ha = 0.;
        if (navigator.getGlobe() != null) {
            ha = navigator.getHeightAngle();
            double h = Math.max(navigator.getEllipsHeight(),
                    navigator.getTerrainHeight())+1000;
            double r = navigator.getGlobe().getEllipsoid().getN(navigator.getLat());
            
            // Near and far clipping planes
            far  = 0;
            // far += Math.sqrt((2*r + 1000.)*1000.);
            far += Math.sqrt((2*r + h)*h);
            near = Math.cos(Math.toRadians(fov/2))*navigator.getTerrainHeight()*0.9 - 10000;
            if (near < 2.)
                near = 2.;
            if (far < near*1.2)
                far = near*1.2;
            
            float v = (50000.f - (float)navigator.getEllipsHeight())/50000.f;
            if (v > 1) v = 1;
            if (v < 0) v = 0;
            
            // Background color
            ApplicationSettings as = ApplicationSettings.getApplicationSettings();
            if (as.getUseSkyColor())
                gl.glClearColor(0.05f + 0.3f*v, 0.05f + 0.6f*v, 0.2f + 0.6f*v, 1.0f);
            else
                gl.glClearColor(0f, 0f, 0f, 1f);
            
            // Fog
            if (as.getUseHaze()) {
                float fog_near = (float)(near>2000?near:2000);
                float fog_far  = (float)(far*(1.1f + 0.4f*(1f-v)));
                if (fog_far < fog_near+2000)
                    fog_far = fog_near+2000;
                
                gl.glEnable(GL.GL_FOG);   // turn on fog, otherwise you won't see any
                gl.glFogi(GL.GL_FOG_MODE, GL.GL_LINEAR);   // Fog fade using linear function
                gl.glFogfv(GL.GL_FOG_COLOR, fogColor, 0);   // Set the fog color
                gl.glFogf(GL.GL_FOG_START, fog_near);
                gl.glFogf(GL.GL_FOG_END, fog_far);
                gl.glHint(GL.GL_FOG_HINT, GL.GL_FASTEST);
            } else
                gl.glDisable(GL.GL_FOG);
        }
        
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        
        // Projection matrix
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        float aspect = (float)width/(float)height;
        glu.gluPerspective(fov/aspect, aspect, Math.cos(Math.toRadians(fov/2))*near, far);
        
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
        double light_dir  = light_model.getLightAzimuth();
        double light_elev = light_model.getLightElevation();
        
        float lx = (float)(Math.sin(light_dir)*Math.cos(light_elev));
        float ly = (float)Math.sin(light_elev);
        float lz = (float)(Math.cos(light_dir)*Math.cos(light_elev));
        
        float lightpos[] = {
            lx,
            (float)(ly*Math.cos(ha)+lz*Math.sin(ha)),
            (float)(lz*Math.cos(ha)-ly*Math.sin(ha)),
            0.0f}; // set last term to 0 for a spotlight
        gl.glLightfv(GL.GL_LIGHT1,GL.GL_POSITION, lightpos, 0);
        
        // Compute modelview matrix
        inv_view_mat= navigator.getViewMatrix(inv_view_mat);
        viewMatrix(inv_view_mat, modl, camera_center);
        
        // Set modelview matrix
        gl.glLoadMatrixf(modl, 0);
        
        // Compute clipping planes
        gl.glGetFloatv(GL.GL_PROJECTION_MATRIX, proj, 0);
        clippingPlanes(modl, proj, planes);
        
        // Set frustum values
        frustum.setValues(planes, camera_center,
                navigator.getOrigin(),
                (float)(fov*Math.PI/180./width)*pixel_factor, true);
        
        super.draw(drawable, width, height);
    }
    
    /**
     * From interface GLEventListener
     * Do the real drawing
     * @param drawable
     */
    public void display(GLAutoDrawable drawable) {
        draw(drawable, drawable.getWidth(), drawable.getHeight());
        
        // Do offscreens
        for (int i=0; i < offscreens.size(); ) {
            Object o = offscreens.get(i);
            Offscreen os = (Offscreen)o;
            if (!os.created) os.create(drawable);
            os.pbuffer.display();
            if (os.finnished) {
                os.pbuffer.destroy();
                offscreens.remove(os);
            } else
                ++i;
        }
        
        
        
        // prevframetime = currtime;
    }
    
    /**
     * Capture an image of the window.
     * This function must *not* be called in the awt thread
     * (in an ActionListener or similar), or a deadlock may occur.
     * Create a separate thread.
     * @return
     */
    public BufferedImage getCapture() {
    /*
    BufferedImage captured = null;
     
    synchronized (captureRequester) {
      captureRequester.request = true;
      while (captureRequester.request) {
        try { captureRequester.wait(); }
        catch (InterruptedException ex) {}
      }
      captured = captureRequester.image;
      captureRequester.image = null;
    }
    if (captured == null) return null;
     */
        OffscreenImageCapture oic = new OffscreenImageCapture();
        synchronized(oic) {
            while (oic.image == null) {
                try { oic.wait(); } catch (InterruptedException ex) {}
            }
        }
        BufferedImage image =
                GraphicsEnvironment.
                getLocalGraphicsEnvironment().
                getDefaultScreenDevice().
                getDefaultConfiguration().
                createCompatibleImage(oic.image.getWidth(), oic.image.getHeight());
        image.createGraphics().drawRenderedImage(oic.image, new AffineTransform());
        return image;
    }
    
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
    
    
}
