//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.globesurface;

import com.norkart.geopos.Ellipsoid;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;

import java.util.ArrayList;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public interface GlobeElevationModel {
    public static class Point {
        public double lon, lat, h;
        
        public Point(double lon, double lat, double h) {
            this.lon = lon;
            this.lat = lat;
            this.h   = h;
        }
    }
    
    /**
     * 
     * @param lon 
     * @param lat 
     * @return 
     */
    public double    getElevation(double lon, double lat);
    public ArrayList project(double lon1, double lat1, double lon2, double lat2);
    public double    getElevationScale();
    public Ellipsoid getEllipsoid();
    
    public Point3d   getIntersection(Point3d p1, Point3d p2, Point3d result);
    public Point3d   getIntersection(Point3d p,  Vector3d v, Point3d result);
    
    public void addGlobeElevationUpdateListener(GlobeElevationUpdateListener gel);
    // public void removeGlobeElevationUpdateListener(GlobeElevationUpdateListener gel);
    // Point3d   getOrigin();
}