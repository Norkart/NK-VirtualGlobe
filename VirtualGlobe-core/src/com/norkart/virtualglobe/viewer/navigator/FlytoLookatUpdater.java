/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  FlytoLookatUpdater2.java
 *
 * Created on 6. mars 2008, 12:33
 *
 */

package com.norkart.virtualglobe.viewer.navigator;

import com.norkart.geopos.Ellipsoid;

/**
 *
 * @author runaas
 */
public class FlytoLookatUpdater  extends FlytoUpdater {
    protected long prevtime = Long.MAX_VALUE;
    protected long next_recompute_time = 0;
    protected double lookat_lat;
    protected double lookat_lon;
    protected double lookat_h;
    protected double lookat_ha;
    protected double lookat_az = Double.MAX_VALUE;
    protected double lookat_dist;
    protected double prev_az = Double.MAX_VALUE;
    
    protected Track track;
    
    protected static final double max_turnrate = .5;
    protected static final long   recompute_interval = 500;
    
    public FlytoLookatUpdater(GlobeNavigator navigator, double lookat_lon, double lookat_lat, double stop_dist) {
        this(navigator, lookat_lon, lookat_lat, stop_dist, Math.toRadians(-20), Double.MAX_VALUE);
    }
    
    public FlytoLookatUpdater(GlobeNavigator navigator, double lookat_lon, double lookat_lat, double stop_dist, double lookat_ha) {
        this(navigator, lookat_lon, lookat_lat, stop_dist, lookat_ha, Double.MAX_VALUE);
    }
    
    /** Creates a new instance of FlytoLookatUpdater2 */
    public FlytoLookatUpdater(GlobeNavigator navigator, double lookat_lon, double lookat_lat, double lookat_dist, double lookat_ha, double lookat_az) {
        super(navigator);
        
        this.lookat_lat  = lookat_lat;
        this.lookat_lon  = lookat_lon;
        this.lookat_dist = lookat_dist;
        this.lookat_ha   = lookat_ha;
        this.lookat_az = lookat_az;
        
        // computeTrack();
        // next_recompute_time = System.currentTimeMillis() + recompute_interval;
    }
    
    private void computeTrack() {
        double start_lon = navigator.getLon();
        double start_lat = navigator.getLat();
        double start_h   = navigator.getEllipsHeight();
        
        if (lookat_az == Double.MAX_VALUE)
            lookat_az = Ellipsoid.adjlon(navigator.getGlobe().getEllipsoid().inverseGeodesic(
                    navigator.getLat(), navigator.getLon(), lookat_lat, lookat_lon, daz).az21 - Math.PI);
        
        llaz = navigator.getGlobe().getEllipsoid().forwGeodesic(lookat_lat, lookat_lon, lookat_dist*Math.cos(lookat_ha), lookat_az - Math.PI, llaz);
        lookat_h = navigator.getGlobe().getElevation(lookat_lon, lookat_lat);
        double end_h  = lookat_h - lookat_dist*Math.sin(lookat_ha);
        double end_terr_h = navigator.getGlobe().getElevation(llaz.lon, llaz.lat);
        if (end_h < end_terr_h + 2)
            end_h = end_terr_h + 2;
        
        // System.out.println("End lon: " + Math.toDegrees(llaz.lon) + " lat: " + Math.toDegrees(llaz.lat) + " h: " + end_h);
        
        if (track == null)
            track = new Track(start_lon, start_lat, start_h, llaz.lon, llaz.lat, end_h, -0.001);
        else
            track.setValues(start_lon, start_lat, start_h, llaz.lon, llaz.lat, end_h);
        track.optimizeCurve();
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
            
            // Distance and direction to goal
            daz = navigator.getGlobe().getEllipsoid().inverseGeodesic(navigator.getLat(), navigator.getLon(), track.end_lat, track.end_lon, daz);
            double x = daz.dist;
            double deriv = -track.h1(x);
            
            // The line length traversed in the current step
            double ds = navigator.getMaxSpeed()*interval/1000;
            // Projected to horizontal...
            double dx = ds / Math.sqrt(1+deriv*deriv);
            // And vertical axis
            double dz = dx * deriv;
            
            // Position of next point
            // System.out.println("Az: " + daz.az12);
            llaz = navigator.getGlobe().getEllipsoid().forwGeodesic(navigator.getLat(), navigator.getLon(), dx, daz.az12, llaz);
            
            daz = navigator.getGlobe().getEllipsoid().inverseGeodesic(navigator.getLat(), navigator.getLon(), lookat_lat, lookat_lon, daz);
            
            double max_turn = interval*max_turnrate/1000;
            
            // Height angle
            double r = navigator.getGlobe().getEllipsoid().getN(navigator.getLat());
            double horizon = Math.sqrt((2*r + navigator.getEllipsHeight())*navigator.getEllipsHeight()) + Math.sqrt((2*r + lookat_h)*lookat_h);
            double d_ha = -navigator.getHeightAngle();
            if (daz.dist > horizon) {
                d_ha += -Math.sqrt((2*r+navigator.getEllipsHeight())*navigator.getEllipsHeight())/(r+navigator.getEllipsHeight());
            } else {
                d_ha += Math.atan2(lookat_h-navigator.getEllipsHeight(), daz.dist) - daz.dist/(2*r);
            }
            if (d_ha >  max_turn) d_ha =  max_turn;
            if (d_ha < -max_turn) d_ha = -max_turn;
            navigator.setHeightAngle(navigator.getHeightAngle() + d_ha);
            
            // Azimuth
            if (prev_az == Double.MAX_VALUE)  prev_az = daz.az12;
            max_turn += 1.2*Math.abs(daz.az12 - prev_az);
            prev_az = daz.az12;
            
            double d_az = Ellipsoid.adjlon(daz.az12 - navigator.getAzimut());
            if (d_az >  max_turn) d_az =  max_turn;
            if (d_az < -max_turn) d_az = -max_turn;
            navigator.setAzimut(navigator.getAzimut() + d_az);
            
            if (x > dx /*daz.dist > stop_dist*/) {
                navigator.setLat(llaz.lat);
                navigator.setLon(llaz.lon);
                navigator.setEllipsHeight(navigator.getEllipsHeight() + dz);
            } else if (Math.abs(d_ha) < 0.0001 && Math.abs(d_az) < 0.0001) {
                // If we are there
                navigator.setUpdater(next);
                break;
            }
        }
        prevtime = currtime;
    }
    
    public boolean isActive() {
        return true;
    }
}
