//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.av3d.nodes;

import com.norkart.virtualglobe.globesurface.GlobeElevationModel;
import com.norkart.virtualglobe.globesurface.GlobeElevationUpdateListener;
import com.norkart.virtualglobe.viewer.OriginUpdateListener;
import com.norkart.virtualglobe.viewer.CullFrustum;

import javax.vecmath.*;

import org.j3d.aviatrix3d.*;
import com.norkart.virtualglobe.viewer.ViewerManager;
import com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class GlobeSurfaceGroup
        extends TransformGroup
        implements NodeUpdateListener, OriginUpdateListener, GlobeElevationUpdateListener {
    public static final int VERT_REF_TERRAIN = 0;
    public static final int VERT_REF_SEA     = 1;
    
    protected double lat, lon, hTerr, hEllps, az, ha;
    protected int vert_ref;
    protected GlobeElevationModel globe;
    protected Matrix4f mat;
    protected Point3d  origin = new Point3d();
    
    //-------------------------------------------------------
    // NodeUpdateListener methods
    //-------------------------------------------------------
    public void updateNodeBoundsChanges(java.lang.Object src) {
        ((GlobeSurfaceGroup)src).setTransform(mat);
        // System.err.println("GlobeSurfaceGroup origin: " + origin);
    }
    
    public void	updateNodeDataChanges(java.lang.Object src) {}
    
    //-------------------------------------------------------
    // OriginUpdateListener methods
    //-------------------------------------------------------
    public void updateOrigin(Point3d origin) {
        this.origin.set(origin);
        updateTransform();
    }
    
    public boolean requestUpdateOrigin(Point3d origin) {
        return true;
    }
    
    /*
     protected void updateBounds() {
        if (dirtyBoundsCount <= 0)
            System.err.println("GlobeSurfaceGroup dirtyBoundsCount: " + dirtyBoundsCount + " <= 0");
         super.updateBounds();
     }
    */
    
    //-------------------------------------------------------
    //   GlobeElevationUpdateListener
    //-------------------------------------------------------
    public void updateElevation(GlobeElevationModel globe) {
        if (globe != this.globe || vert_ref == VERT_REF_SEA) return;
        double h = globe.getElevation(lon, lat);
        if (Math.abs(hTerr + h - hEllps) > 0.01) {
            hEllps = hTerr + h;
            updateTransform();
        }
    }
    //-------------------------------------------------------
    // Own methods
    //-------------------------------------------------------
    public GlobeSurfaceGroup(GlobeElevationModel globe, double lon, double lat,  double h, int vert_ref, double az) {
        this.globe = globe;
        this.lon = lon;
        this.lat = lat;
        this.ha = 0;
        this.az = az;
        this.vert_ref = vert_ref;
        switch (vert_ref) {
            case VERT_REF_TERRAIN:
                hTerr  = h;
                hEllps = hTerr + globe.getElevation(lon, lat);
                globe.addGlobeElevationUpdateListener(this);
                break;
            case VERT_REF_SEA:
                hTerr  = Double.MAX_VALUE;
                hEllps = h;
                break;
        }
        updateTransform();
    }
    
    protected void updateTransform() {
        if (globe != null && globe.getEllipsoid() != null)
            mat = globe.getEllipsoid().computeSurfaceTransform(lat, lon, hEllps, az, ha, mat, origin);
        if (isLive())
            ((AV3DViewerManager)ViewerManager.getInstance()).updateNode(this, this, AV3DViewerManager.UPDATE_BOUNDS);
        else
            setTransform(mat);
    }
}