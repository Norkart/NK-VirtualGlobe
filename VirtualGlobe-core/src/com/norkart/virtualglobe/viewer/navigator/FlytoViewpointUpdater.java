/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  FlytoViewpointUpdater2.java
 *
 * Created on 13. mars 2008, 11:02
 *
 */

package com.norkart.virtualglobe.viewer.navigator;

import com.norkart.geopos.Ellipsoid;

/**
 *
 * @author runaas
 */
public class FlytoViewpointUpdater extends FlytoUpdater  {
    protected long prevtime = Long.MAX_VALUE;
    protected long next_recompute_time = 0;
    protected double to_lat;
    protected double to_lon;
    protected double to_h;
    protected double to_ha;
    protected double to_az;
    
    protected double timeleft;
    
    protected static final double stoptime = 3;
    protected static final double max_turnrate = .5;
    protected static final long   recompute_interval = 500;

    protected double prev_az = Double.MAX_VALUE;
    
    protected Track track = null;
    

    FlytoViewpointUpdater(GlobeNavigator navigator, double to_lon, double to_lat, double to_h, double to_az, double to_ha) {
        super(navigator);
        this.to_lon = to_lon;
        this.to_lat = to_lat;
        this.to_h   = to_h;
        this.to_az  = to_az;
        this.to_ha  = to_ha;
    }
    
    FlytoViewpointUpdater(double to_lon, double to_lat, double to_h, double to_az, double to_ha, ChainedUpdater next) {
        super(next);
        this.to_lon = to_lon;
        this.to_lat = to_lat;
        this.to_h   = to_h;
        this.to_az  = to_az;
        this.to_ha  = to_ha;
    }
    
    private void computeTrack() {
        double start_lon = navigator.getLon();
        double start_lat = navigator.getLat();
        double start_h   = navigator.getEllipsHeight();
        
        double end_h = to_h;
        double end_terr_h = navigator.getGlobe().getElevation(to_lon, to_lat);
        if (end_h < end_terr_h + 2)
            end_h = end_terr_h + 2;
        
        // System.out.println("End lon: " + Math.toDegrees(llaz.lon) + " lat: " + Math.toDegrees(llaz.lat) + " h: " + end_h);
        
        if (track == null)
            track = new Track(start_lon, start_lat, start_h, to_lon, to_lat, end_h, -0.001);
        else
            track.setValues(start_lon, start_lat, start_h, to_lon, to_lat, end_h);
        timeleft = track.optimizeCurve();
    }
    
    public  void update() {
        if (navigator.getGlobe() == null) return;
        long currtime = System.currentTimeMillis();
        
        if (next_recompute_time  <= currtime)  {
            computeTrack();
            next_recompute_time = currtime + recompute_interval;
        }
        
        while (prevtime < currtime) {
            long interval = Math.min(currtime - prevtime, 100);
            prevtime += interval;
            
            daz = navigator.getGlobe().getEllipsoid().inverseGeodesic(navigator.getLat(), navigator.getLon(), track.end_lat, track.end_lon, daz);
            double x = daz.dist;
            double deriv = -track.h1(x);
            
            // The line length traversed in the current step
            double ds = navigator.getMaxSpeed()*interval/1000;
            // Projected to horizontal...
            double dx = ds / Math.sqrt(1+deriv*deriv);
            // And vertical axis
            double dz = dx * deriv;
            
            
            // If we are there
            if (timeleft <= 0) {
                navigator.setLat(to_lat);
                navigator.setLon(to_lon);
                navigator.setHeightAngle(to_ha);
                navigator.setAzimut(to_az);
                navigator.setEllipsHeight(to_h);
                navigator.setUpdater(next);
                break;
            }
            
            // Position of next point
            llaz = navigator.getGlobe().getEllipsoid().forwGeodesic(navigator.getLat(), navigator.getLon(), dx, daz.az12, llaz);
            navigator.setLat(llaz.lat);
            navigator.setLon(llaz.lon);
            
            if (timeleft  < stoptime) {
                // If we are closing in to the target,
                navigator.setAzimut(navigator.getAzimut() + Ellipsoid.adjlon(to_az - navigator.getAzimut())*interval*.001/timeleft);
                navigator.setHeightAngle(navigator.getHeightAngle() + (to_ha - navigator.getHeightAngle())*interval*.001/timeleft);
            } else {
                double max_turn = interval*max_turnrate/1000;
                
                // Elevation angle
                double r = navigator.getGlobe().getEllipsoid().getN(navigator.getLat());
                double horizon = Math.sqrt((2*r + navigator.getEllipsHeight())*navigator.getEllipsHeight()) + Math.sqrt((2*r + to_h)*to_h);
                double d_ha = -navigator.getHeightAngle();
                if (daz.dist > horizon) {
                    d_ha += -Math.sqrt((2*r+navigator.getEllipsHeight())*navigator.getEllipsHeight())/(r+navigator.getEllipsHeight());
                } else {
                    d_ha += Math.atan2(to_h-navigator.getEllipsHeight(), daz.dist) - daz.dist/(2*r);
                }
                
                if (d_ha >  max_turn) d_ha =  max_turn;
                if (d_ha < -max_turn) d_ha = -max_turn;
                navigator.setHeightAngle(navigator.getHeightAngle() + d_ha);
                
                // Azimuth
                if (prev_az == Double.MAX_VALUE)  prev_az = daz.az12;
                max_turn += 1.2*Math.abs(daz.az12 - prev_az);
                prev_az = daz.az12;
                
                double d_az = Ellipsoid.adjlon(llaz.az + Math.PI - navigator.getAzimut());
                if (d_az >  max_turn) d_az =  max_turn;
                if (d_az < -max_turn) d_az = -max_turn;
                navigator.setAzimut(navigator.getAzimut() + d_az);
            }
            navigator.setEllipsHeight(navigator.getEllipsHeight() + dz);
            
            timeleft -= interval/1000.;
        }
        prevtime = currtime;
    }
    
    public boolean isActive() {
        return true;
    }
}
