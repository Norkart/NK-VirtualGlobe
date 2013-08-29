/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  FlytoUpdater.java
 *
 * Created on 13. mars 2008, 10:43
 *
 */

package com.norkart.virtualglobe.viewer.navigator;

import com.norkart.geopos.Ellipsoid;

/**
 *
 * @author runaas
 */
public abstract class FlytoUpdater extends ChainedUpdater {
    
    protected class Track implements Func1 {
        double a, b, c;
        double dist, az;
        double end_lat, end_lon;
        double start_h, end_h;
        double curve;
        double t = Double.MAX_VALUE;
        protected double curve_mult  = 0.9;
        
        public Track(double start_lon, double start_lat, double start_h, double end_lon, double end_lat, double end_h, double curve) {
            setValues(start_lon, start_lat, start_h, end_lon, end_lat, end_h);
            setCurve(curve);
        }
        public void setValues(double start_lon, double start_lat, double start_h, double end_lon, double end_lat, double end_h) {
            this.end_lon = end_lon;
            this.end_lat = end_lat;
            this.start_h = start_h;
            this.end_h   = end_h;
            daz  = navigator.getGlobe().getEllipsoid().inverseGeodesic(start_lat, start_lon, end_lat, end_lon, daz);
            az   = daz.az21;
            dist = daz.dist;
            setCurve(curve);
        }
        public void setCurve(double curve) {
            this.curve   = curve;
            a = curve/2;
            c = end_h;
            b = dist <= 0 ? 0 : (start_h-end_h)/dist - curve*dist/2;
        }
        
        public double h(double x) {
            return (a*x+b)*x+c;
        }
        
        public double h1(double x) {
            return 2*a*x+b;
        }
        
        public double f(double x) {
            double h1 = h1(x);
            llaz = navigator.getGlobe().getEllipsoid().forwGeodesic(end_lat, end_lon, x, az, llaz);
            double h_terr = h(x)-navigator.globe.getElevation(llaz.lon, llaz.lat);
            double v = navigator.getMaxSpeed(h_terr);
            if (v<=0) return Double.MAX_VALUE;
            return Math.sqrt(1+h1*h1) / v;
        }
        
        public double optimizeCurve() {
            // double t = Double.MAX_VALUE, prev_t;
            double new_curve = curve;
            // int times = 100;
            for (;;) {
                setCurve(new_curve);
                // t = Integration.simpson(track, 0, track.dist, 5);
                double new_t = Integration.adaptiveSimpson(this, 0, dist, 0.1, 7);
                // System.out.println("Estimert flytid: " + t+ " curve: " + new_curve);
                if (new_t > t - 0.1 || new_t < 0 || Double.isInfinite(new_t) || Double.isNaN(new_t)) {
                    curve_mult = 1/curve_mult;
                    break;
                }
                t = new_t;
                curve = new_curve;
                new_curve *= curve_mult;
            }
            setCurve(curve);
            // System.out.println("Estimert flytid ferdig, a: " + a + " b: " + b + " c: " + c);
            return t;
        }
    }
    
    
    /** Creates a new instance of FlytoUpdater */
    public FlytoUpdater(GlobeNavigator navigator) {
        super(navigator);
        
    }
    public FlytoUpdater(ChainedUpdater next) {
        super(next);
    }
}
