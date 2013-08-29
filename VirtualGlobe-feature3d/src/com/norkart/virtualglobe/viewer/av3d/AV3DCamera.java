/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  AV3DCamera.java
 *
 * Created on 8. mai 2008, 13:20
 *
 */

package com.norkart.virtualglobe.viewer.av3d;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

import com.norkart.virtualglobe.globesurface.GLCleanup;

import com.norkart.virtualglobe.util.ApplicationSettings;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.AbstractCamera;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;
import com.norkart.virtualglobe.viewer.PostDrawListener;

import org.j3d.aviatrix3d.Group;
import org.j3d.aviatrix3d.Viewpoint;
import org.j3d.aviatrix3d.SimpleScene;
import org.j3d.aviatrix3d.TransformGroup;
import org.j3d.aviatrix3d.ApplicationUpdateObserver;
import org.j3d.aviatrix3d.SimpleViewport;
import org.j3d.aviatrix3d.SimpleLayer;
import org.j3d.aviatrix3d.Layer;
import org.j3d.aviatrix3d.NodeUpdateListener;

import org.j3d.aviatrix3d.rendering.RenderEffectsProcessor;
import org.j3d.aviatrix3d.rendering.ProfilingData;

import org.j3d.aviatrix3d.management.DisplayCollection;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.FrustumCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.ViewportResizeManager;
import org.j3d.aviatrix3d.pipeline.graphics.StateAndTransparencyDepthSortStage;

import org.j3d.aviatrix3d.output.graphics.BaseSurface;
import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLCapabilities;

/**
 *
 * @author runaas
 */
public abstract class AV3DCamera
        extends AbstractCamera
        implements ApplicationUpdateObserver, WindowListener,
        NodeUpdateListener, RenderEffectsProcessor {
    protected BaseSurface              surface;
    protected DefaultGraphicsPipeline  pipeline;
    protected TransformGroup           view_transform ;
    protected Group                    camera_root;
    protected Viewpoint                vp;
    protected SimpleScene              scene;
    protected SimpleLayer              layer;
    protected Component                canvas;
    protected ViewportResizeManager    resize_manager = new ViewportResizeManager();
    
    protected DisplayCollection displayCollection = new SingleDisplayCollection();
    
    protected Matrix4f matrix  = new Matrix4f();
    
    Vector4f[] frustumPlanes = { new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f() };
    
    
    /** Creates a new instance of AV3DCamera */
    public AV3DCamera(ViewerManager manager, GlobeNavigator nav) {
        super(manager, nav);
        
        // Scenegraph objects
        vp = new Viewpoint();
        view_transform = new TransformGroup();
        view_transform.addChild(vp);
        
        camera_root = new Group();
        camera_root.addChild(view_transform);
        
        scene = new SimpleScene();
        scene.setRenderedGeometry(((AV3DViewerManager)manager).scene_root);
        scene.setActiveView(vp);
        
        // Drawable
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);
        
        int multisampleSettings = ApplicationSettings.getApplicationSettings().getMultisampleSettings();
        if (multisampleSettings > ApplicationSettings.MULTISAMPLE_NONE) {
            int aaLevel = multisampleSettings*2;
            System.out.println("Using Antialiasing level: " + aaLevel);
            caps.setSampleBuffers(true);
            caps.setNumSamples(aaLevel);
        }
        
        BaseSurface reference_surface = null;
        AbstractCamera referenceCamera = manager.getCamera(0);
        if (referenceCamera instanceof AV3DCamera) {
            AV3DCamera av3d_referenceCamera = (AV3DCamera)referenceCamera;
            reference_surface = av3d_referenceCamera.surface;
            
            GLContext referenceContext = ((GLCanvas)reference_surface.getSurfaceObject()).getContext();
            if (referenceContext != null)
                referenceContext.setSynchronized(true);
        }
        
        if (reference_surface == null)
            surface = new SimpleAWTSurface(caps);
        else
            surface = new SimpleAWTSurface(caps, reference_surface);
        surface.setClearColor(0.f, 0.f, 0.f, 1);
        
        // Cull stage
        GraphicsCullStage culler = new FrustumCullStage();
        culler.setOffscreenCheckEnabled(false);
        
        // Sort stage
        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage(); // Blinker, 13fps
        // SortStage sorter = new DepthSortedTransparencyStage(); // 8fps
        // SortStage sorter = new StateSortStage(); // Blinker, 14fps, ingen transparens
        // SortStage sorter = new SimpleTransparencySortStage(); // 8fps
        // SortStage sorter = new NullSortStage(); // 8fps, ingen transparens
        
        
        // Pipeline
        pipeline = new DefaultGraphicsPipeline();
        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);
        
        
        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        canvas = (Component)surface.getSurfaceObject();
        graphics_view.add(canvas, BorderLayout.CENTER);
        
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 500, 500);
        view.setScene(scene);
        
        resize_manager.addManagedViewport(view);
        surface.addGraphicsResizeListener(resize_manager);
        
        displayCollection.addPipeline(pipeline);
        
        layer = new SimpleLayer();
        layer.setViewport(view);
        Layer [] layers = {layer};
        displayCollection.setLayers(layers, 1);
        
        surface.enableSingleThreaded(false);
        
        scene.setRenderEffectsProcessor(this);
    }
    
    public void start() {
        displayCollection.setEnabled(true);
        ((AV3DViewerManager)manager).sceneManager.setEnabled(true);
    }
    
    public void stop() {
        displayCollection.setEnabled(false);
    }
    
    public void shutdown() {
        displayCollection.shutdown();
    }
    
    public boolean isRunning() {
        return displayCollection.isEnabled();
    }
    
    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------
    
    public void windowOpened(WindowEvent evt) {
        start();
    }
    
    public void windowClosing(WindowEvent e) {
        stop();
    }
    public void windowActivated(WindowEvent evt) { }
    
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
    
    //---------------------------------------------------------------
// Methods defined by NodeUpdateListener
//---------------------------------------------------------------
    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
        if (src == view_transform) {
            // if (framecnt > 5) {
            //}
            // matrix = navigator.getViewMatrix(matrix);
            // inv_mat.set(matrix);
            view_transform.setTransform(matrix);
        }
        if (src == ((AV3DViewerManager)manager).view_root)
            ((AV3DViewerManager)manager).view_root.addChild(camera_root);
    }
    
    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        
    }
    
    //---------------------------------------------------------------
// RenderEffectsProcessor
//---------------------------------------------------------------
    
    
    public void	postDraw(GL gl, ProfilingData prof_data, java.lang.Object userData) {
        // After drawing
        synchronized (postDrawListeners) {
            for (PostDrawListener pdl : postDrawListeners) {
                pdl.postDraw(gl);
            }
        }
    }
    
    private boolean first_time = true;
    public void	preDraw(GL gl, java.lang.Object userData) {
        if (first_time) {
            System.out.print("OpenGL vendor: ");
            System.out.println(gl.glGetString(GL.GL_VENDOR));
            System.out.print("OpenGL renderer: ");
            System.out.println(gl.glGetString(GL.GL_RENDERER));
            System.out.print("OpenGL version: ");
            System.out.println(gl.glGetString(GL.GL_VERSION));
            System.out.print("OpenGL extensions: ");
            System.out.println(gl.glGetString(GL.GL_EXTENSIONS));
            first_time = false;
        }
        
        // Free old textures
        GLCleanup.cleanupAll(gl, glu);
    }
    
    public void drawNow(Object originator) {
        ((AV3DViewerManager)manager).lastfast = System.currentTimeMillis();
        synchronized (manager) {
            manager.notify();
        }
    }
    
    
    protected static void computeFrustum(double [] frustum_dim, Matrix4f matrix, Vector4f[] frustumPlanes, boolean perspective) {
        // Compute frustum
        
        
        // Create projection matrix
        float left = (float)frustum_dim[0];
        float right = (float)frustum_dim[1];
        float bottom = (float)frustum_dim[2];
        float top = (float)frustum_dim[3];
        float nearval = (float)frustum_dim[4];
        float farval = (float)frustum_dim[5];
        float x, y, z, w;
        float a, b, c, d;
        
        Matrix4f prjMatrix = new Matrix4f();
        if (perspective) {
            x = (2.0f * nearval) / (right - left);
            y = (2.0f * nearval) / (top - bottom);
            a = (right + left) / (right - left);
            b = (top + bottom) / (top - bottom);
            c = -(farval + nearval) / ( farval - nearval);
            d = -(2.0f * farval * nearval) / (farval - nearval);
            
            prjMatrix.m00 = x;
            prjMatrix.m01 = 0;
            prjMatrix.m02 = a;
            prjMatrix.m03 = 0;
            prjMatrix.m10 = 0;
            prjMatrix.m11 = y;
            prjMatrix.m12 = b;
            prjMatrix.m13 = 0;
            prjMatrix.m20 = 0;
            prjMatrix.m21 = 0;
            prjMatrix.m22 = c;
            prjMatrix.m23 = d;
            prjMatrix.m30 = 0;
            prjMatrix.m31 = 0;
            prjMatrix.m32 = -1;
            prjMatrix.m33 = 0;
        } else {
            x = 2.0f / (right - left);
            y = 2.0f / (top - bottom);
            z = -2.0f / (farval - nearval);
            a = -(right + left) / (right - left);
            b = -(top + bottom) / (top - bottom);
            c = -(farval + nearval) / ( farval - nearval);
            
            prjMatrix.m00 = x;
            prjMatrix.m01 = 0;
            prjMatrix.m02 = 0;
            prjMatrix.m03 = a;
            prjMatrix.m10 = 0;
            prjMatrix.m11 = y;
            prjMatrix.m12 = 0;
            prjMatrix.m13 = b;
            prjMatrix.m20 = 0;
            prjMatrix.m21 = 0;
            prjMatrix.m22 = z;
            prjMatrix.m23 = c;
            prjMatrix.m30 = 0;
            prjMatrix.m31 = 0;
            prjMatrix.m32 = 0;
            prjMatrix.m33 = 1;
        }
        
        /*
        x = (2.0f * nearval) / (right - left);
        y = (2.0f * nearval) / (top - bottom);
        a = (right + left) / (right - left);
        b = (top + bottom) / (top - bottom);
        c = -(farval + nearval) / ( farval - nearval);
        d = -(2.0f * farval * nearval) / (farval - nearval);
         
         
        prjMatrix.m00 = x;
        prjMatrix.m01 = 0;
        prjMatrix.m02 = a;
        prjMatrix.m03 = 0;
        prjMatrix.m10 = 0;
        prjMatrix.m11 = y;
        prjMatrix.m12 = b;
        prjMatrix.m13 = 0;
        prjMatrix.m20 = 0;
        prjMatrix.m21 = 0;
        prjMatrix.m22 = c;
        prjMatrix.m23 = d;
        prjMatrix.m30 = 0;
        prjMatrix.m31 = 0;
        prjMatrix.m32 = -1;
        prjMatrix.m33 = 0;
         */
        
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.invert(matrix);
        
        viewMatrix.mul(prjMatrix, viewMatrix);
        
        // Put result into opengl format
        prjMatrix.m00 = viewMatrix.m00;
        prjMatrix.m01 = viewMatrix.m10;
        prjMatrix.m02 = viewMatrix.m20;
        prjMatrix.m03 = viewMatrix.m30;
        prjMatrix.m10 = viewMatrix.m01;
        prjMatrix.m11 = viewMatrix.m11;
        prjMatrix.m12 = viewMatrix.m21;
        prjMatrix.m13 = viewMatrix.m31;
        prjMatrix.m20 = viewMatrix.m02;
        prjMatrix.m21 = viewMatrix.m12;
        prjMatrix.m22 = viewMatrix.m22;
        prjMatrix.m23 = viewMatrix.m32;
        prjMatrix.m30 = viewMatrix.m03;
        prjMatrix.m31 = viewMatrix.m13;
        prjMatrix.m32 = viewMatrix.m23;
        prjMatrix.m33 = viewMatrix.m33;
        
        float t;
        /* Extract the numbers for the RIGHT plane */
        x = prjMatrix.m03 - prjMatrix.m00;
        y = prjMatrix.m13 - prjMatrix.m10;
        z = prjMatrix.m23 - prjMatrix.m20;
        w = prjMatrix.m33 - prjMatrix.m30;
        
        /* Normalize the result */
/*
        t = 1.0f / (float) Math.sqrt(x * x + y * y + z * z );
        frustumPlanes[0].x = x * t;
        frustumPlanes[0].y = y * t;
        frustumPlanes[0].z = z * t;
        frustumPlanes[0].w = w * t;
 */
        frustumPlanes[0].x = x;
        frustumPlanes[0].y = y;
        frustumPlanes[0].z = z;
        frustumPlanes[0].w = w;
        
        /* Extract the numbers for the LEFT plane */
        x = prjMatrix.m03 + prjMatrix.m00;
        y = prjMatrix.m13 + prjMatrix.m10;
        z = prjMatrix.m23 + prjMatrix.m20;
        w = prjMatrix.m33 + prjMatrix.m30;
        
/*
        t = (float) Math.sqrt(x * x + y * y + z * z);
        frustumPlanes[1].x = x * t;
        frustumPlanes[1].y = y * t;
        frustumPlanes[1].z = z * t;
        frustumPlanes[1].w = w * t;
 */
        frustumPlanes[1].x = x;
        frustumPlanes[1].y = y;
        frustumPlanes[1].z = z;
        frustumPlanes[1].w = w;
        
        /* Extract the BOTTOM plane */
        x = prjMatrix.m03 + prjMatrix.m01;
        y = prjMatrix.m13 + prjMatrix.m11;
        z = prjMatrix.m23 + prjMatrix.m21;
        w = prjMatrix.m33 + prjMatrix.m31;
        
/*
        t = (float) Math.sqrt(x * x + y * y + z * z);
        frustumPlanes[2].x = x * t;
        frustumPlanes[2].y = y * t;
        frustumPlanes[2].z = z * t;
        frustumPlanes[2].w = w * t;
 */
        frustumPlanes[2].x = x;
        frustumPlanes[2].y = y;
        frustumPlanes[2].z = z;
        frustumPlanes[2].w = w;
        
        /* Extract the TOP plane */
        x = prjMatrix.m03 - prjMatrix.m01;
        y = prjMatrix.m13 - prjMatrix.m11;
        z = prjMatrix.m23 - prjMatrix.m21;
        w = prjMatrix.m33 - prjMatrix.m31;
        
/*
        t = (float) Math.sqrt(x * x + y * y + z * z);
        frustumPlanes[3].x = x * t;
        frustumPlanes[3].y = y * t;
        frustumPlanes[3].z = z * t;
        frustumPlanes[3].w = w * t;
 */
        frustumPlanes[3].x = x;
        frustumPlanes[3].y = y;
        frustumPlanes[3].z = z;
        frustumPlanes[3].w = w;
        
        
        /* Extract the FAR plane */
        x = prjMatrix.m03 - prjMatrix.m02;
        y = prjMatrix.m13 - prjMatrix.m12;
        z = prjMatrix.m23 - prjMatrix.m22;
        w = prjMatrix.m33 - prjMatrix.m32;
        
/*
        t = (float) Math.sqrt(x * x + y * y + z * z);
        frustumPlanes[4].x = x * t;
        frustumPlanes[4].y = y * t;
        frustumPlanes[4].z = z * t;
        frustumPlanes[4].w = w * t;
 */
        frustumPlanes[4].x = x;
        frustumPlanes[4].y = y;
        frustumPlanes[4].z = z;
        frustumPlanes[4].w = w;
        
        /* Extract the NEAR plane */
        x = prjMatrix.m03 + prjMatrix.m02;
        y = prjMatrix.m13 + prjMatrix.m12;
        z = prjMatrix.m23 + prjMatrix.m22;
        w = prjMatrix.m33 + prjMatrix.m32;
/*
        t = (float) Math.sqrt(x * x + y * y + z * z);
        frustumPlanes[5].x = x * t;
        frustumPlanes[5].y = y * t;
        frustumPlanes[5].z = z * t;
        frustumPlanes[5].w = w * t;
 */
        
        frustumPlanes[5].x = x;
        frustumPlanes[5].y = y;
        frustumPlanes[5].z = z;
        frustumPlanes[5].w = w;
        
        for (int i=0; i<6; ++i) {
            float f =
                    frustumPlanes[i].x * frustumPlanes[i].x +
                    frustumPlanes[i].y * frustumPlanes[i].y +
                    frustumPlanes[i].z * frustumPlanes[i].z;
            f = (float)Math.sqrt(f);
            frustumPlanes[i].scale(1/f);
        }
        
    }
    
    protected void setup() {
        
    }
    
    public Component getCanvas() {
        return canvas;
    }
    
    public Component getGraphicsView() {
        return graphics_view;
    }
    
    public void appShutdown() {
        
    }
    
    private boolean is_initialized = false;
    public void updateSceneGraph() {
        if (!is_initialized && ((AV3DViewerManager)manager).scene_root.isLive()) {
            is_initialized = true;
            ((AV3DViewerManager)manager).view_root.boundsChanged(this);
            ((AV3DViewerManager)manager).scene_root.boundsChanged(this);
            
        }
        if (!is_initialized)
            return;
        
        resize_manager.sendResizeUpdates();
        if (view_transform.isLive())
            view_transform.boundsChanged(this);
    }
}
