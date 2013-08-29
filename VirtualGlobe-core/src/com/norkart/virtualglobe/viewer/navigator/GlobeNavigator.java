//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.navigator;

import com.norkart.virtualglobe.util.ApplicationSettings;
import com.norkart.geopos.Ellipsoid;
import com.norkart.virtualglobe.globesurface.GlobeElevationModel;
import com.norkart.virtualglobe.util.PathPoint;

//import com.norkart.VirtualGlobe.Util.PathPoint;
// import com.norkart.VirtualGlobe.Util.SpringUtilities;
// import com.norkart.VirtualGlobe.WorldComponents.FlyPath;
// import com.norkart.virtualglobe.core.viewer.*;



import javax.vecmath.*;


import java.util.Iterator;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.ref.WeakReference;
import com.norkart.virtualglobe.viewer.DrawNowListener;
import com.norkart.virtualglobe.viewer.OriginUpdateListener;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.lang.ref.WeakReference;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class GlobeNavigator implements OriginUpdateListener {
    protected Point3d origin = new Point3d();
    private ArrayList draw_now_listeners = new ArrayList();
    
    public synchronized void fireDrawNow(Object originator) {
        Iterator i = draw_now_listeners.iterator();
        while (i.hasNext()) {
            WeakReference ref = (WeakReference)i.next();
            DrawNowListener dl = (DrawNowListener)ref.get();
            if (dl != null)
                dl.drawNow(originator);
            else
                i.remove();
        }
    }
    
    public synchronized void addDrawNowListener(DrawNowListener dl) {
        draw_now_listeners.add(new WeakReference(dl));
    }
    
    public void updateOrigin(Point3d p) {
        origin.set(p);
    }
    
    public boolean requestUpdateOrigin(Point3d p) {
        return true;
    }
    
    public Point3d getOrigin() {
        return origin;
    }
    
    protected GlobeElevationModel globe;
    
    private final double    min_h = 2.;
    
    private double          lat;
    private double          lon;
    private double          hEllps;
    private double          hTerrain;
    private double          h = Double.MAX_VALUE;
    private double          ha;
    private double          az;
    private double          point_lat;
    private double          point_lon;
    private double          point_h;
    private double          point_dist;
    private long            last_gui_update_time = 0;
    
    
    // private Point3d         eye = new Point3d();
    // private Vector3f        eyeVector = new Vector3f();
    private Ellipsoid.LatLonAz lla = new Ellipsoid.LatLonAz();
    private Ellipsoid.DistAz   daz = new Ellipsoid.DistAz();
    // private Matrix4f        workTrans = new Matrix4f();
    

    private boolean lat_changed = true;
    private boolean lon_changed = true;
    private boolean ele_changed = true;
    private boolean az_changed  = true;
    private boolean ha_changed  = true;
    private boolean point_changed = true;
    
    private Timer gui_updater = new Timer();
    private ArrayList<GlobeNavigatorUpdateListener> navigatorUpdateListeners = new ArrayList();
    
    public void addGlobeNavigatorUpdateListener(GlobeNavigatorUpdateListener l) {
        navigatorUpdateListeners.add(l);
    }
    
    private NavigatorUpdater updater;
    public void setUpdater(NavigatorUpdater updater) {
        this.updater = updater;
    }
    public NavigatorUpdater getUpdater() {
        return updater;
    }
    
    protected WalkFlyUpdater walkFlyUpdater;
    public WalkFlyUpdater getWalkFlyUpdater() {
        if (walkFlyUpdater == null)
            walkFlyUpdater = new WalkFlyUpdater(this);
        
        if (updater != walkFlyUpdater) {
            walkFlyUpdater.stopSpeed();
            updater = walkFlyUpdater;
        }
        return walkFlyUpdater;
    }
    
    public GlobeNavigator(GlobeElevationModel globe) {
        this.globe = globe;
        
        ha = Math.toRadians(-90);
        hEllps = 12000000;
        
        
        gui_updater.schedule(new TimerTask() {
            public void run() {
                if (ha_changed || az_changed  || ele_changed || lon_changed || lat_changed || point_changed) {
                    for (GlobeNavigatorUpdateListener l : navigatorUpdateListeners)
                        l.updateNavigatorChanges(GlobeNavigator.this);
                    ha_changed = az_changed  = ele_changed = lon_changed = lat_changed = point_changed = false;
                }
            }
        }, 0, 200);
    }
    
    public GlobeElevationModel getGlobe() {
        return globe;
    }
    
    public void setGlobe(GlobeElevationModel globe) {
        this.globe = globe;
    }
    
    /*
    public void addNavigatorListener(GlobeNavigatorUpdateListener listener) {
        synchronized (navigatorUpdateListeners) {
            Iterator it = navigatorUpdateListeners.iterator();
            while (it.hasNext()) {
                WeakReference ref = (WeakReference)it.next();
                if (ref.get() == null)
                    it.remove();
                else if (ref.get() == listener)
                    return;
            }
            navigatorUpdateListeners.add(new WeakReference(listener));
        }
    }
     
    public void removeNavigatorListener(GlobeNavigatorUpdateListener listener) {
        synchronized (navigatorUpdateListeners) {
            Iterator it = navigatorUpdateListeners.iterator();
            while (it.hasNext()) {
                WeakReference ref = (WeakReference)it.next();
                if (ref.get() == null)
                    it.remove();
                else if (ref.get() == listener) {
                    it.remove();
                    return;
                }
            }
        }
    }
     
    protected void fireNavigatorListeners() {
        synchronized (navigatorUpdateListeners) {
            Iterator it = navigatorUpdateListeners.iterator();
            while (it.hasNext()) {
                WeakReference ref = (WeakReference)it.next();
                if (ref.get() == null)
                    it.remove();
                else {
                    GlobeNavigatorUpdateListener listener = (GlobeNavigatorUpdateListener)ref.get();
                    listener.updateNavigatorChanges(this);
                }
            }
        }
    }
     */
    
    /**
     * Create user interface
     * @return
     */
    

    
 
    
    private void updateElevation() {
        double my_h = h;
        if (my_h == Double.MAX_VALUE)
            h = my_h = globe==null?0:globe.getElevation(lon, lat)*globe.getElevationScale();
        if (Math.abs(hTerrain + my_h - hEllps) > 0.01) {
            hTerrain = hEllps - my_h;
            if (hTerrain < min_h) {
                hTerrain = min_h;
                hEllps = hTerrain + my_h;
            }
            ele_changed = true;
        }
    }
    
    public Matrix4f getViewMatrix(Matrix4f mat) {
        if (updater != null)
            updater.update();
        
        updateElevation();
        mat = computeViewMatrix(lat, lon, hEllps, az, ha, mat);
        // fireNavigatorListeners();
        // updateGui();
        
        return mat;
    }
    
    public Matrix4f computeViewMatrix(
            double lat, double lon, double hEllps,
            double az, double ha, Matrix4f mat) {
        if (mat == null) mat = new Matrix4f();
        if (globe != null)
            globe.getEllipsoid().computeSurfaceTransform(lat, lon, hEllps, az, ha, mat, origin);
        else
            mat.setIdentity();
    /*
    if (globe != null) {
      globe.getEllipsoid().toCartesian(lat, lon, hEllps, eye);
      eyeVector.set((float)(eye.x - origin.x),
                    (float)(eye.y - origin.y),
                    (float)(eye.z - origin.z));
    }
    else
      eyeVector.set(0,0,0);
    if (mat == null) mat = new Matrix4f();
    mat.set(eyeVector);
    workTrans.rotZ((float)(Math.PI/2.+lon)); mat.mul(workTrans);
    workTrans.rotX((float)(Math.PI/2.-lat)); mat.mul(workTrans);
    workTrans.rotZ((float)(-az));            mat.mul(workTrans);
    workTrans.rotX((float)(Math.PI/2.+ha));  mat.mul(workTrans);
     */
        return mat;
    }
/*
  public void setViewMatrix(Matrix4f mat) {
 
  }
 */
    
    /**
     * Set viewpoint position (geographic, radians) from external data.
     */
    public void setLat(double newLat) {
        if (lat != newLat) {
            lat = newLat;
            lat_changed = true;
            h = Double.MAX_VALUE;
        }
    }
    public void setLon(double newLon) {
        if (lon != newLon) {
            lon = newLon;
            lon_changed = true;
            h = Double.MAX_VALUE;
        }
    }
    
    public void setPosition(double newLat, double newLon) {
        if (lat != newLat || lon != newLon) {
            lat = newLat;
            lon = newLon;
            lon_changed = lat_changed = true;
            h = Double.MAX_VALUE;
        }
    }
    
    public void setPosition(double newLat, double newLon, double h) {
        if (lat != newLat || lon != newLon || h != hEllps) {
            lat     = newLat;
            lon     = newLon;
            hEllps  = h;
            ele_changed = lon_changed = lat_changed = true;
            this.h = Double.MAX_VALUE;
        }
    }
    
    public void setPointer(double lon, double lat, double h, double dist) {
        if (lat != point_lat || lon != point_lon || h != point_h) {
            point_lat = lat;
            point_lon = lon;
            point_h =  h;
            point_dist = Math.max(dist, 2);
            point_changed = true;
        }
    }
    
    public void gotoViewpoint(double to_lon, double to_lat, double to_h, double to_az, double to_ha) {
        // setUpdater(new GotoUpdater(vp_data, time));
        setUpdater(new FlytoViewpointUpdater(this, to_lon, to_lat, to_h, to_az, to_ha));
    }
    
    public void gotoLookat(double to_lon, double to_lat, double stop_dist) {
        setUpdater(new FlytoLookatUpdater(this, to_lon, to_lat, stop_dist));
    }
    
    public void gotoLookat(double to_lon, double to_lat, double stop_dist, boolean north_up, double to_ha) {
        if (north_up)
            setUpdater(new FlytoLookatUpdater(this, to_lon, to_lat, stop_dist, to_ha, 0));
        else
            setUpdater(new FlytoLookatUpdater(this, to_lon, to_lat, stop_dist, to_ha));
        /*
        Ellipsoid ellps = getGlobe().getEllipsoid();
        double h = getGlobe().getElevation(to_lon, to_lat);
        if (north_up) {
            lla = ellps.forwGeodesic(to_lat, to_lon, stop_dist*Math.cos(to_ha), Math.PI, lla);
            daz = ellps.inverseGeodesic(lla.lat, lla.lon, to_lat, to_lon, daz);
            setUpdater(new FlytoViewpointUpdater(this, lla.lon, lla.lat, h - stop_dist*Math.sin(to_ha), daz.az12, to_ha));
        }
        else {
            daz = ellps.inverseGeodesic(getLat(), getLon(), to_lat, to_lon, daz);
            lla = ellps.forwGeodesic(getLat(), getLon(), daz.dist - stop_dist*Math.cos(to_ha), daz.az12, lla);
            daz = ellps.inverseGeodesic(lla.lat, lla.lon, to_lat, to_lon, daz);
            setUpdater(new FlytoViewpointUpdater(this, lla.lon, lla.lat, h - stop_dist*Math.sin(to_ha), daz.az12, to_ha));
        }
         */
    }
    
    
    public void flyPath(List<PathPoint> path) {
        Iterator it = path.iterator();
        if (!it.hasNext()) return;
        PathPoint pp = (PathPoint)it.next();
        setUpdater(new FlytoViewpointUpdater(pp.data[0], pp.data[1], pp.data[2], pp.data[3], pp.data[4], new PathUpdater(this, path)));
    }
    /*
    public void setViewpoint(double [] vp_data) {
        double newLon = Math.toRadians(vp_data[0]);
        double newLat = Math.toRadians(vp_data[1]);
        double h      = vp_data[2];
        double newAz  = Math.toRadians(vp_data[3]);
        double newHa  = Math.toRadians(vp_data[4]);
        
        if (ha != newHa) {
            ha = newHa;
            ha_changed = true;
        }
        if (az != newAz) {
            az = Ellipsoid.adjlonPos(newAz);
            az_changed = true;
        }
        if (lat != newLat || lon != newLon || h != hEllps) {
            lat     = newLat;
            lon     = newLon;
            hEllps  = h;
            ele_changed = lon_changed = lat_changed = true;
            this.h = Double.MAX_VALUE;
        }
        
    }
    
    public void getViewpoint(double [] vp_data) {
        vp_data[0] = Math.toDegrees(lon);
        vp_data[1] = Math.toDegrees(lat);
        vp_data[2] = hEllps;
        vp_data[3] = Math.toDegrees(az);
        vp_data[4] = Math.toDegrees(ha);
    }
     **/
    public double getLat() {
        return lat;
    }
    
    public double getLon() {
        return lon;
    }
    
    public double getPointerLat() {
        return point_lat;
    }
    public double getPointerLon() {
        return point_lon;
    }
    public double getPointerH() {
        return point_h;
    }
    public double getPointerDist() {
        return point_dist;
    }
    
    public boolean isLatChanged() {
        return lat_changed;
    }
    public boolean isLonChanged() {
        return lon_changed;
    }
    
    public boolean isElevationChanged() {
        return ele_changed;
    }
    public boolean isAzimuthChanged() {
        return az_changed;
    }
    public boolean isHeightAngleChanged() {
        return ha_changed;
    }
    
    public boolean isPointerChanged() {
        return point_changed;
    }
    
    public double getMaxSpeed() {
        return getMaxSpeed(hTerrain);
    }
    
    public double getMaxSpeed(double h) {
        final double v0 = 50;
        final double dvdh = 1;
        final double h1 = 100;
        final double v1 = 60;
        final double d = (v1 - v0 * (h1+1)) / (dvdh*h1 - v1)*h1;
        return (dvdh*d*h*h - (h+1)*v0)/(d*h+1);
        // return Math.max(1000., hTerrain);
    }
    
    /**
     * Set viewpoint height (above terrain) from external data.
     */
    public void setTerrainHeight(double h) {
        h -= hTerrain;
        if (h != 0) {
            hEllps += h;
            ele_changed = true;
        }
    }
    
    /**
     * Set viewpoint height (above ellipsoid) from external data.
     */
    public void setEllipsHeight(double h) {
        if (h != hEllps) {
            hEllps = h;
            ele_changed = true;
        }
    }
    
    public double getTerrainHeight() {
        if (ele_changed) updateElevation();
        return hTerrain;
    }
    
    public double getEllipsHeight() {
        if (ele_changed) updateElevation();
        return hEllps;
    }
    
    /**
     * Set height angle (above horizontal plane) from external data.
     */
    public void setHeightAngle(double a) {
        if (ha != a) {
            ha = a;
            ha_changed = true;
        }
    }
    
    public double getHeightAngle() {
        return ha;
    }
    
    /**
     * Set azimut (direction rightwards from north) from external data.
     */
    public void setAzimut(double a) {
        if (az != a) {
            az = Ellipsoid.adjlonPos(a);
            az_changed = true;
        }
    }
    
    public double getAzimut() {
        return az;
    }
    
    public void translate(double d, double t_az, double t_ha) {
        translate(d, t_az, t_ha, false);
    }
    
    public void translate(double d, double t_az, double t_ha, boolean correct_az) {
        if (globe == null || d == 0) return;
        double hDist = d*Math.cos(t_ha);
        double vDist = d*Math.sin(t_ha);
        
        lla = globe.getEllipsoid().forwGeodesic(lat, lon, hDist, t_az, lla);
        lat = lla.lat;
        lon = lla.lon;
        hEllps += vDist;
        if (correct_az) {
            az = Ellipsoid.adjlonPos(az + lla.az + Math.PI - t_az);
            az_changed = true;
        }
        ele_changed = lat_changed = lon_changed = true;
        h = Double.MAX_VALUE;
    }
    
    /**
     * Move forward/backward a given distance.
     */
    public void translateForward(double d) {
        if (globe == null || d == 0) return;
        double hDist = d*Math.cos(ha);
        double vDist = d*Math.sin(ha);
        
        lla = globe.getEllipsoid().forwGeodesic(lat, lon, hDist, az, lla);
        lat = lla.lat;
        lon = lla.lon;
        az  = Ellipsoid.adjlonPos(lla.az + Math.PI);
        hEllps += vDist;
        ele_changed = lat_changed = lon_changed = az_changed = true;
        h = Double.MAX_VALUE;
    }
    
    /**
     * Move up/down a given distance.
     */
    public void translateUpDown(double d) {
        if (globe == null || d == 0) return;
        double hDist = d*Math.cos(Math.PI/2.+ha);
        double vDist = d*Math.sin(Math.PI/2.+ha);
        
        lla = globe.getEllipsoid().forwGeodesic(lat, lon, hDist, az, lla);
        lat = lla.lat;
        lon = lla.lon;
        az  = Ellipsoid.adjlonPos(lla.az + Math.PI);
        hEllps += vDist;
        ele_changed = lat_changed = lon_changed = az_changed = true;
        h = Double.MAX_VALUE;
    }
    
    /**
     * Move left/right a given distance.
     */
    public void translateSideway(double d) {
        if (globe == null || d == 0) return;
        lla = globe.getEllipsoid().forwGeodesic(lat, lon, -d, az+Math.PI/2., lla);
        lat = lla.lat;
        lon = lla.lon;
        az  = Ellipsoid.adjlonPos(lla.az + Math.PI/2.);
        lat_changed = lon_changed = az_changed = true;
        h = Double.MAX_VALUE;
    }
    
    /**
     * Rotate azimut (horizontal direction).
     */
    public void rotateAz(double d) {
        if (d != 0) {
            az = Ellipsoid.adjlonPos(az + d);
            az_changed = true;
        }
    }
    
    public void rotateAroundPointer(double x_rot, double y_rot) {
        if (hTerrain < 0) hTerrain = 0;
        if (Math.abs(point_dist*Math.sin(x_rot)) > hTerrain/10)
            x_rot = Math.asin(hTerrain/point_dist/10)*(x_rot>=0?1:-1);
        if (Math.abs(point_dist*Math.sin(y_rot)) > hTerrain/10)
            y_rot = Math.asin(hTerrain/point_dist/10)*(y_rot>=0?1:-1);
        
        // translateSideway(dist*Math.sin(x_rot))
        double d_x = point_dist*Math.sin(x_rot);
        lla = globe.getEllipsoid().forwGeodesic(lat, lon, -d_x*Math.cos(ha), az+Math.PI/2., lla);
        lat = lla.lat;
        lon = lla.lon;
        az  = Ellipsoid.adjlonPos(lla.az + Math.PI/2.);
        
        // translateUpDown(dist*Math.sin(y_rot));
        double d_y     = point_dist*Math.sin(y_rot);
        hEllps += d_y*Math.sin(Math.PI/2.+ha);
        lla = globe.getEllipsoid().forwGeodesic(lat, lon, d_y*Math.cos(Math.PI/2.+ha), az, lla);
        lat = lla.lat;
        lon = lla.lon;
        az  = Ellipsoid.adjlonPos(lla.az + Math.PI);
        
        // translateForward(dist*(1-Math.cos(x_rot))*(1-Math.cos(y_rot)));
        double d_xy = point_dist*(1-Math.cos(x_rot))*(1-Math.cos(y_rot));
        hEllps += d_xy*Math.sin(ha);
        lla = globe.getEllipsoid().forwGeodesic(lat, lon, d_xy*Math.cos(ha), az, lla);
        lat = lla.lat;
        lon = lla.lon;
        az  = Ellipsoid.adjlonPos(lla.az + Math.PI + x_rot);
        
        ha -= y_rot;
        if (ha >  Math.PI/2) ha =  Math.PI/2;
        if (ha < -Math.PI/2) ha = -Math.PI/2;
        ele_changed = lat_changed = lon_changed = az_changed = ha_changed = true;
        h = Double.MAX_VALUE;
    }
    
    /**
     * Rotate height angle (vertical direction)
     */
  /*
  public void rotateHa(double d) {
    if (d != 0) {
      ha += d;
      ha_changed = true;
    }
  }
   */
    
    public Point3d getEye(Point3d eye) {
        if (eye == null)
            eye = new Point3d();
        if (globe != null)
            globe.getEllipsoid().toCartesian(lat, lon, hEllps, eye);
        else
            eye.set(0,0,0);
        
        return eye;
    }
}
