//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer;

import javax.vecmath.Vector4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3d;

import java.util.ArrayList;


import java.awt.Component;

import java.nio.FloatBuffer;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.JPanel;
import java.awt.color.ColorSpace;



import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import com.norkart.geopos.Ellipsoid;
import com.norkart.virtualglobe.globesurface.GlobeElevationModel;
import com.norkart.virtualglobe.viewer.CullFrustum;
import com.norkart.virtualglobe.util.ApplicationSettings;

import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public abstract class AbstractCamera  implements DrawNowListener, Camera {
    protected ViewerManager          manager;
    // Mouse and key listeners
    protected GlobeNavigator navigator;
    
    
    // The Java window that parents the  drawable
    protected JPanel graphics_view;
    
    
    protected GLU glu = new GLU();
    protected ArrayList<PostDrawListener> postDrawListeners = new ArrayList();
    
    static protected ColorModel glColorModel;
    static protected ColorModel glAlphaColorModel;
    static {
        glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] {8,8,8,0},
                false,
                false,
                ComponentColorModel.OPAQUE,
                DataBuffer.TYPE_BYTE);
        glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] {8,8,8,8},
                true,
                false,
                ComponentColorModel.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);
    }
    
    
    public class PositionListener extends MouseInputAdapter implements PostDrawListener {
        private int point_x = -1, point_y = -1;
        private int [] viewport = new int [4];
        private double [] modelview = new double [16];
        private double [] projection = new double [16];
        private FloatBuffer z_buf = FloatBuffer.allocate(1);
        private double [] pos = new double[3];
        
        private Point3d pos_point = new Point3d();
        private Point3d eye       = new Point3d();
        private Ellipsoid.LatLonH llh;
        
        public void postDraw(GL gl) {
            if (point_x >= 0 && point_y >= 0) {
                gl.glGetDoublev( GL.GL_MODELVIEW_MATRIX, modelview, 0 );
                gl.glGetDoublev( GL.GL_PROJECTION_MATRIX, projection, 0 );
                gl.glGetIntegerv( GL.GL_VIEWPORT, viewport, 0 );
                z_buf.clear();
                gl.glReadPixels( point_x, point_y, 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, z_buf );
                
                glu.gluUnProject( point_x, point_y, z_buf.get(0), modelview, 0, projection, 0, viewport, 0, pos, 0);
                GlobeElevationModel globe = getNavigator().getGlobe();
                if (globe == null) return;
                llh = globe.getEllipsoid().fromCartesian(
                        pos_point.x = pos[0]+getNavigator().getOrigin().x,
                        pos_point.y = pos[1]+getNavigator().getOrigin().y,
                        pos_point.z = pos[2]+getNavigator().getOrigin().z, llh);
                
                double h = getNavigator().getGlobe().getElevation(llh.lon, llh.lat);
                double dist = navigator.getEye(eye).distance(pos_point);
                navigator.setPointer(llh.lon, llh.lat, Math.max(llh.h, h), dist);
                //System.out.println("Pekepos: " + Math.toDegrees(llh.lon) + ", " +  Math.toDegrees(llh.lat) + ", " + llh.h);
                // System.out.println("["+pos[0]+", "+pos[1]+", "+pos[2]+"]");
            }
        }
        
        private void clearPointerPosition() {
            point_x = -1;
            point_y = -1;
        }
        
        private void setPointerPosition(MouseEvent e) {
            point_x = e.getX();
            point_y = e.getComponent().getHeight() - e.getY();
        }
        
        public void	mouseDragged(MouseEvent e) {
            setPointerPosition(e);
        }
        public void	mouseMoved(MouseEvent e) {
            setPointerPosition(e);
        }
        public void	mouseEntered(MouseEvent e) {
            setPointerPosition(e);
        }
        public void 	mouseExited(MouseEvent e) {
            clearPointerPosition();
        }
    }
    
    protected AbstractCamera(ViewerManager manager, GlobeNavigator navigator) {
        this.manager = manager;
        this.navigator = navigator;
        
        navigator.addDrawNowListener(this);
        manager.addOriginUpdateListener(navigator);
        
        graphics_view = new JPanel();
        graphics_view.setLayout(new BorderLayout());
    }
    
    /**
     * Get the navigator for this camera
     * @return
     */
    public GlobeNavigator getNavigator() {
        return navigator;
    }
    public ViewerManager getViewerManager() {
        return manager;
    }
    
    protected boolean hasActiveUpdater() {
        GlobeNavigator nav = getNavigator();
        if (nav == null) return false;
        return nav.getUpdater() != null && nav.getUpdater().isActive();
    }
    /**
     * Stop waiting for updates and release graphics for drawing
     */
    
    public void addPostDrawListener(PostDrawListener pdl) {
        postDrawListeners.add(pdl);
    }
}