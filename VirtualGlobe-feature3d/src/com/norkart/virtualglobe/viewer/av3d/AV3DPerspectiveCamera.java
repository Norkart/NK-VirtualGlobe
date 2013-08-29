//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.av3d;

import com.norkart.virtualglobe.viewer.LightModel;
import com.norkart.virtualglobe.viewer.PerspectiveCamera;
import com.norkart.virtualglobe.viewer.AbstractCamera;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.PostDrawListener;

import com.norkart.virtualglobe.globesurface.GLCleanup;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;

import java.util.ArrayList;
//import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Hashtable;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.text.MessageFormat;

import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import com.norkart.virtualglobe.util.ApplicationSettings;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

import javax.vecmath.*;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.rendering.*;
import org.j3d.aviatrix3d.management.*;
import org.j3d.aviatrix3d.pipeline.*;
import org.j3d.aviatrix3d.output.graphics.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;
import org.j3d.aviatrix3d.picking.*;

import com.sun.opengl.util.Screenshot;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class AV3DPerspectiveCamera extends AV3DCamera
        implements PerspectiveCamera {
    
    
    
    private final float fogColor[] = {.3f, .3f, .6f, 1f};   // fog color
    private Fog    fog          = new Fog(Fog.LINEAR, fogColor);
    
    /** Matrix used to update the transform */
    
    private Point3d camera_pos = new Point3d();
    // private Matrix4f inv_mat = new Matrix4f();
    
    private double near, far;
    private float v;
    
    private float fov = 40;
    private float pixel_factor = 3;
    
    private JLabel framerate_label;
    private Object[] framerate_arg = { new Float(0) };
    private MessageFormat framerate_format;
    
    
    private Component camera_panel;
    private Component light_panel;
    
    private DirectionalLight dl = new DirectionalLight();
    
    private PickHandler pick_handler;
    
    private double[] frustum_dim = new double[6];
    
    
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
        super.updateNodeBoundsChanges(src);
        
        if (src == ((AV3DViewerManager)manager).scene_root) {
            ((AV3DViewerManager)manager).scene_root.addChild(dl);
            ((AV3DViewerManager)manager).scene_root.addChild(fog);
        }
    }
    
    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    private Vector3f light_v = new Vector3f();
    private Matrix4f light_trans = new Matrix4f();
    private float[] fv = new float[3];
    public void updateNodeDataChanges(Object src) {
        super.updateNodeDataChanges(src);
        
        if (src == dl) {
            double ha = navigator.getHeightAngle();
            
            LightModel light_model = manager.getLightModel();
            float spec_int    = light_model.getSpecularIntensity();
            float diffuse_int = light_model.getDiffuseIntensity();
            float ambient_int = light_model.getAmbientIntensity();
            
            // set light position
            double light_dir  = light_model.getLightAzimuth();
            double light_elev = light_model.getLightElevation();
            
            
            // set light position
            float lx = (float)(Math.sin(light_dir)*Math.cos(light_elev));
            float ly = (float)Math.sin(light_elev);
            float lz = (float)(Math.cos(light_dir)*Math.cos(light_elev));
            light_v.set(lx,
                    (float)(ly*Math.cos(ha)+lz*Math.sin(ha)),
                    (float)(lz*Math.cos(ha)-ly*Math.sin(ha)));
            
            view_transform.getTransform(light_trans);
            light_trans.transform(light_v);
            dl.setDirection(light_v.x, light_v.y, light_v.z);
            fv[0] = fv[1] = fv[2] = spec_int;
            dl.setSpecularColor(fv);
            fv[0] = fv[1] = fv[2] = diffuse_int;
            dl.setDiffuseColor(fv);
            fv[0] = fv[1] = fv[2] = ambient_int;
            dl.setAmbientColor(fv);
        }
        if (src == fog) {
            // Fog
            if (ApplicationSettings.getApplicationSettings().getUseHaze()) {
                float fog_near = (float)(near>2000?near:2000);
                float fog_far  = (float)(far*(1.1f + 0.4f*(1f-v)));
                if (fog_far < fog_near+2000)
                    fog_far = fog_near+2000;
                
                fog.setLinearDistance(fog_near, fog_far);
                fog.setEnabled(true);
//         scene.setActiveFog(fog);
            } else {
                fog.setEnabled(false);
                //        scene.setActiveFog(null);
            }
        }
        
    }
    
    
//---------------------------------------------------------------
// RenderEffectsProcessor
//---------------------------------------------------------------
    
    private class CaptureRequester {
        boolean request = false;
        BufferedImage image;
    }
    CaptureRequester captureRequester = new CaptureRequester();
    
    public void	postDraw(GL gl, ProfilingData prof_data, java.lang.Object userData) {
        super.postDraw(gl, prof_data, userData);
        // After drawing
        
        synchronized (captureRequester) {
            if (!captureRequester.request) return;
            Dimension dim = canvas.getSize();
            
            
            captureRequester.image = Screenshot.readToBufferedImage(dim.width, dim.height); // new BufferedImage(cm, raster, false, new Hashtable());
            captureRequester.request = false;
            captureRequester.notifyAll();
        }
    }
    
    private boolean is_initialized = false;
    public void updateSceneGraph() {
        super.updateSceneGraph();
        
        if (!is_initialized && ((AV3DViewerManager)manager).scene_root.isLive()) {
            is_initialized = true;
            
            scene.setActiveFog(fog);
        }
        if (!is_initialized)
            return;
        
        navigator.getEye(camera_pos);
        float tol = Math.max(0.01f, (float)navigator.getTerrainHeight()*(float)(fov*Math.PI/180./canvas.getWidth())*pixel_factor);
        getViewerManager().testOrigin(camera_pos, tol);
        
        // Compute near and far clipping planes and background color
        near = 1000.;
        far = 20000000.;
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
            
            v = (50000.f - (float)navigator.getEllipsHeight())/50000.f;
            if (v > 1) v = 1;
            if (v < 0) v = 0;
            
/*
 
 */
            // scene.setViewEnvironment(ve);
            
            // Background color
            ApplicationSettings as = ApplicationSettings.getApplicationSettings();
            if (as.getUseSkyColor())
                surface.setClearColor(0.05f + 0.3f*v, 0.05f + 0.6f*v, 0.2f + 0.6f*v, 1.0f);
            else
                surface.setClearColor(0f, 0f, 0f, 1f);
        }
        
        // Do the picking
        if (!((AV3DViewerManager)manager).sceneManager.isEnabled()) return;
        if (pick_handler != null) {
            try {
                pick_handler.handleLastPick();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        // Update the pointer position
        // pickPointerPosition();
        
        if (!((AV3DViewerManager)manager).sceneManager.isEnabled()) return;
        
        if (dl.isLive())
            dl.dataChanged(this);
        if (fog.isLive())
            fog.dataChanged(this);
        if (!((AV3DViewerManager)manager).sceneManager.isEnabled()) return;
        
        ViewEnvironment ve = scene.getViewEnvironment();
        // ve.dataChanged(this);
        float aspect = (float)((Component)surface.getSurfaceObject()).getWidth()/
                (float)((Component)surface.getSurfaceObject()).getHeight();
        
        ve.setAspectRatio(aspect);
        ve.setFieldOfView(fov/aspect);
        if (near < far)
            ve.setClipDistance(near, far);
        
        matrix = navigator.getViewMatrix(matrix);
        
        ve.getViewFrustum(frustum_dim);
        computeFrustum(frustum_dim, matrix, frustumPlanes, true);
    }
    
//---------------------------------------------------------------
// Local methods
//---------------------------------------------------------------
    
    public AV3DPerspectiveCamera(ViewerManager manager, GlobeNavigator nav) {
        super(manager, nav);
        
        PositionListener pos_listener = new PositionListener();
        postDrawListeners.add(pos_listener);
        canvas.addMouseMotionListener(pos_listener);
        canvas.addMouseListener(pos_listener);
        
        // scene.setViewEnvironment(ve);
        
        
        // Add a pick handler
        pick_handler = new PickHandler(surface, ((AV3DViewerManager)manager).model_group);
        ((Component)surface.getSurfaceObject()).addMouseListener(pick_handler);
        ((Component)surface.getSurfaceObject()).addMouseMotionListener(pick_handler);
        
        // Add some lights to help illuminate the model
        dl.setEnabled(true);
        // dl.setGlobalScope(true);
        
        LightModel light_model = manager.getLightModel();
        float spec_int    = light_model.getSpecularIntensity();
        float diffuse_int = light_model.getDiffuseIntensity();
        float ambient_int = light_model.getAmbientIntensity();
        
        dl.setSpecularColor(new float[] {spec_int, spec_int, spec_int});
        dl.setDiffuseColor(new float[] {diffuse_int, diffuse_int, diffuse_int});
        dl.setAmbientColor(new float[] {ambient_int, ambient_int, ambient_int});
    }
    
    
    
    public BufferedImage getCapture() {
        BufferedImage captured = null;
        synchronized (captureRequester) {
            captureRequester.request = true;
            while (captureRequester.request) {
                try { captureRequester.wait(); } catch (InterruptedException ex) {}
            }
            captured = captureRequester.image;
            captureRequester.image = null;
        }
        return captured;
    }
    
    
    public float getFov() { return fov; }
    public void  setFov(float fov) { this.fov = fov; }
    
    
    public float getDetailSizeFactor() {
        return pixel_factor;
    }
    public void  setDetailSizeFactor(float detail_factor) {
        pixel_factor = detail_factor;
    }
    
    public Component getCameraPanel() {
        if (camera_panel != null)
            return camera_panel;
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        Box box = Box.createVerticalBox();
        Box sub_box;
        
        sub_box = Box.createVerticalBox();
        sub_box.setBorder(BorderFactory.createTitledBorder(settings.getResourceString("FOV_BOX_TITLE")));
        final Object[] ma2 = { new Double(fov) };
        final MessageFormat mf2 = new MessageFormat( settings.getResourceString("FOV_LABEL"));
        final JLabel fovLabel = new JLabel(mf2.format(ma2));
        fovLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        final JSlider fovSlider = new JSlider();
        fovSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        fovSlider.setOrientation(JSlider.HORIZONTAL);
        fovSlider.setMaximum(120);
        fovSlider.setMinimum(10);
        fovSlider.setValue((int)fov);
        fovSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == fovSlider) {
                    float value = fovSlider.getValue();
                    ma2[0] = new Double(value);
                    fovLabel.setText(mf2.format(ma2));
                    fov = value;
                }
            }
        });
        fovSlider.setToolTipText(settings.getResourceString("FOV_TIPS"));
        sub_box.add(fovSlider);
        sub_box.add(fovLabel);
        box.add(sub_box);
        
        sub_box = Box.createVerticalBox();
        sub_box.setBorder(BorderFactory.createTitledBorder(settings.getResourceString("DETAIL_SIZE_BOX_TITLE")));
        final Object[] ma1 = { new Double(pixel_factor) };
        final MessageFormat mf1 = new MessageFormat( settings.getResourceString("DETAIL_SIZE_LABEL"));
        final JLabel detailLabel = new JLabel(mf1.format(ma1));
        detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        final JSlider detailSlider = new JSlider();
        detailSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailSlider.setOrientation(JSlider.HORIZONTAL);
        detailSlider.setMaximum(100);
        detailSlider.setMinimum(10);
        detailSlider.setValue((int)(pixel_factor*10));
        detailSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == detailSlider) {
                    float value = detailSlider.getValue();
                    value /= 10.0;
                    ma1[0] = new Double(value);
                    detailLabel.setText(mf1.format(ma1));
                    pixel_factor = value;
                }
            }
        });
        detailSlider.setToolTipText(settings.getResourceString("DETAIL_SIZE_TIPS"));
        sub_box.add(detailSlider);
        sub_box.add(detailLabel);
        box.add(sub_box);
        
        framerate_format = new MessageFormat( settings.getResourceString("FRAMERATE_LABEL"));
        framerate_label = new JLabel();
        framerate_label.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(framerate_label);
        
        camera_panel = box;
        
        return camera_panel;
    }
    
    
}