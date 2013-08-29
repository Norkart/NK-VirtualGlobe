//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.geopos;

import javax.vecmath.Tuple3d;
import javax.vecmath.Point3d;
import javax.vecmath.Matrix4f;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public final class Ellipsoid {
    protected double a;
    protected double f;
    
    private static final double DTOL = 1e-9;
    
    public Ellipsoid(double a, double f) {
        this.a = a;
        this.f = f;
    }
    
    public static class LatLonAz {
        public double lat, lon, az;
    }
    
    public static class LatLonH {
        public double lat, lon, h;
    }
    
    
    /// Small assisting function, normalizing longitude.
    public static double adjlon(double lon) {
        if (Math.abs(lon) <= Math.PI)
            return lon;
        lon += Math.PI;  /* adjust to 0..2pi rad */
        lon -= 2 * Math.PI * Math.floor(lon / (2 * Math.PI)); /* remove integral # of 'revolutions'*/
        lon -= Math.PI;  /* adjust back to -pi..pi rad */
        return( lon );
    }
    
    public static double adjlonPos(double lon) {
        lon = adjlon(lon);
        if (lon < 0) lon += Math.PI*2;
        return( lon );
    }
    
    
    /// Major axis
    public final double getA()   { return a; }
    /// Flattening
    public final double getF()   { return f; }
    /// Minor axis
    public final double getB()   { return a*(1-f); }
    /// Reverse flattening
    public final double getRF()  { return 1./f; }
    /// Eccentricity squared
    public final double getE2()  { return (2.-f)*f; }
    /// Second eccentricity squared
    public final double getE22() {
        double e2 = getE2();
        return e2/(1.-e2);
    }
  /*
  if (ellipse = es != 0.) {
    onef*onef = 1 - (2-f)*f = 1 - 2*f + f*f = (1-f)**2
        onef = sqrt(1. - es);
        geod_f = 1 - onef;
        f2 = geod_f/2;
        f4 = geod_f/4;
        f64 = geod_f*geod_f/64;
} else {
        onef = 1.;
        geod_f = f2 = f4 = f64 = 0.;
        }
   */
    public final double getW(double lat) {
        double sin_lat = Math.sin(lat);
        return Math.sqrt(1.-getE2()*sin_lat*sin_lat);
    }
    
    /// Meridial radius of curvature
    public final double getM(double lat) {
        double w = getW(lat);
        return a*(1.-getE2())/(w*w*w);
    }
    
    /// Normal radius of curvature
    public final double getN(double lat) {
        return a/getW(lat);
    }
    
    public final void toCartesian(double lat, double lon, double h, Tuple3d p) {
        double n = getN(lat);
        double r = (n+h) * Math.cos(lat);
        
        p.x = r * Math.cos(lon);
        p.y = r * Math.sin(lon);
        p.z = (n * (1.-getE2()) + h) * Math.sin(lat);
    }
    
    public final LatLonH fromCartesian(double x, double y, double z, LatLonH llh) {
        if (llh == null)
            llh = new LatLonH();
        double r = Math.sqrt(x*x + y*y);
        llh.lon = Math.atan2(y, x);
        llh.lat = Math.atan2(z, r*(1.-getE2()));
        
        double oldLat = 2*Math.PI;
        while (Math.abs(oldLat - llh.lat) >= 0.000000001) {
            double n = getN(llh.lat);
            oldLat = llh.lat;
            llh.lat = Math.atan2(z + getE2()*n*Math.sin(llh.lat), r);
            
            
        }
        llh.h = r/Math.cos(llh.lat) - getN(llh.lat);
        
    /*
    eSq = (a*a - b*b) / (a*a);
  var p = Math.sqrt(x2*x2 + y2*y2);
  var phi = Math.atan2(z2, p*(1-eSq)), phiP = 2*Math.PI;
  while (Math.abs(phi-phiP) > precision) {
    nu = a / Math.sqrt(1 - eSq*Math.sin(phi)*Math.sin(phi));
    phiP = phi;
    phi = Math.atan2(z2 + eSq*nu*Math.sin(phi), p);
  }
  var lambda = Math.atan2(y2, x2);
  H = p/Math.cos(phi) - nu;
     */
        return llh;
    }
    
    private Tuple3d  eye       = new Point3d();
    private Matrix4f workTrans = new Matrix4f();
    public Matrix4f  computeSurfaceTransform(
            double lat, double lon, double hEllps,
            double az, double ha, Matrix4f mat, Tuple3d origin) {
        if (mat == null) {
            mat = new Matrix4f();
        } else {
            mat.m01 = mat.m10 = mat.m02 =
                    mat.m20 = mat.m12 = mat.m21 =
                    mat.m30 = mat.m31 = mat.m32 = 0;
        }
        mat.m00 = mat.m11 = mat.m22 = mat.m33 = 1;
        synchronized (eye) {
            toCartesian(lat, lon, hEllps, eye);
            mat.m03 = (float)(eye.x - origin.x);
            mat.m13 = (float)(eye.y - origin.y);
            mat.m23 = (float)(eye.z - origin.z);
        }
        synchronized (workTrans) {
            workTrans.rotZ((float)(Math.PI/2.+lon)); mat.mul(workTrans);
            workTrans.rotX((float)(Math.PI/2.-lat)); mat.mul(workTrans);
            workTrans.rotZ((float)(-az));            mat.mul(workTrans);
            workTrans.rotX((float)(Math.PI/2.+ha));  mat.mul(workTrans);
        }
        return mat;
    }
    
    /**
     * Compute forward geodesic (known start position, direction and distance)
     */
    public final LatLonAz forwGeodesic(double lat1, double lon1,
            double dist, double az12, LatLonAz llaz) {
        double lat2, lon2, az21;
        boolean ellipse = (f > 0.);
        // "Reduced" latitude
        double th1 = ellipse ? Math.atan((1.-f) * Math.tan(lat1)) : lat1;
        double costh1 = Math.cos(th1);
        double sinth1 = Math.sin(th1);
        
        // Sin and cos of azimuth
        az12 = adjlon(az12); /* reduce to  +- 0-PI */
        boolean signS = Math.abs(az12) > Math.PI/2.;
        double sina12 = Math.sin(az12);
        double cosa12, M;
        boolean merid = Math.abs(sina12) < DTOL;
        if (merid) {
            sina12 = 0.;
            cosa12 = Math.abs(az12) < Math.PI/2. ? 1. : -1.;
            M = 0.;
        } else {
            cosa12 = Math.cos(az12);
            M = costh1 * sina12;
        }
        double N = costh1 * cosa12;
        
        // Coeffecients
        double c1 = 0., c2 = 0., D = 0., P = 0.;
        if (ellipse) {
            if (merid) {
                c1 = 0.;
                c2 = f/4.;
                D = 1. - c2;
                D *= D;
                P = c2 / D;
            } else {
                c1 = f * M;
                c2 = f/4. * (1. - M * M);
                D = (1. - c2)*(1. - c2 - c1 * M);
                P = (1. + .5 * c1 * M) * c2 / D;
            }
        }
        double s1;
        if (merid)
            s1 = Math.PI/2. - th1;
        else {
            s1 = (Math.abs(M) >= 1.) ? 0. : Math.acos(M);
            s1 =  sinth1 == 0 ? 0 : sinth1 / Math.sin(s1);
            s1 = (Math.abs(s1) >= 1.) ? 0. : Math.acos(s1);
        }
        
        double ds,ss = 0.;
        if (ellipse) {
            double d = dist / (D * a);
            if (signS) d = -d;
            double u = 2. * (s1 - d);
            double V = Math.cos(u + d);
            double sind = Math.sin(d);
            double X = c2 * c2 * sind * Math.cos(d) * (2. * V * V - 1.);
            ds = d + X - 2. * P * V * (1. - 2. * P * Math.cos(u)) * sind;
            ss = s1 + s1 - ds;
        } else {
            ds = dist / a;
            if (signS) ds = - ds;
        }
        
        double cosds = Math.cos(ds);
        double sinds = Math.sin(ds);
        if (signS) sinds = - sinds;
        az21 = N * cosds - sinth1 * sinds;
        
        double de;
        if (merid) {
            lat2 = Math.atan( Math.tan(Math.PI/2. + s1 - ds) / (1.-f));
            if (az21 > 0.) {
                az21 = Math.PI;
                if (signS)
                    de = Math.PI;
                else {
                    lat2 = - lat2;
                    de = 0.;
                }
            } else {
                az21 = 0.;
                if (signS) {
                    lat2 = - lat2;
                    de = 0;
                } else
                    de = Math.PI;
            }
        } else {
            az21 = Math.atan(M / az21);
            if (az21 > 0)
                az21 += Math.PI;
            if (az12 < 0.)
                az21 -= Math.PI;
            az21 = adjlon(az21);
            lat2 = Math.atan(-(sinth1 * cosds + N * sinds) * Math.sin(az21) /
                    (ellipse ? (1.-f) * M : M));
            de = Math.atan2(sinds * sina12 ,
                    (costh1 * cosds - sinth1 * sinds * cosa12));
            if (ellipse)
                if (signS)
                    de += c1 * ((1. - c2) * ds +
                            c2 * sinds * Math.cos(ss));
                else
                    de -= c1 * ((1. - c2) * ds -
                            c2 * sinds * Math.cos(ss));
        }
        lon2 = adjlon( lon1 + de );
        if (llaz == null)
            llaz = new LatLonAz();
        llaz.lat = lat2;
        llaz.lon = lon2;
        llaz.az  = az21;
        return llaz;
    }
    
    public static class DistAz {
        public double dist, az12, az21;
    }
    
    public final DistAz inverseGeodesic(double lat1, double lon1,
            double lat2, double lon2, DistAz daz) {
        if (daz == null) daz = new DistAz();
        
        
        double	th1,th2,thm,dthm,dlamm,dlam,sindlamm,costhm,sinthm,cosdthm,
                sindthm,L,E,cosd,d,X,Y,T,sind,tandlammp,u,v,D,A,B;
        
        
        
        if (f != 0) {
            th1 = Math.atan((1-f) * Math.tan(lat1));
            th2 = Math.atan((1-f) * Math.tan(lat2));
        } else {
            th1 = lat1;
            th2 = lat2;
        }
        thm = .5 * (th1 + th2);
        dthm = .5 * (th2 - th1);
        dlamm = .5 * ( dlam = adjlon(lon2 - lon1) );
        if (Math.abs(dlam) < DTOL && Math.abs(dthm) < DTOL) {
            daz.az12 =  daz.az21 = daz.dist = 0.;
            return daz;
        }
        sindlamm = Math.sin(dlamm);
        costhm = Math.cos(thm);	sinthm = Math.sin(thm);
        cosdthm = Math.cos(dthm);	sindthm = Math.sin(dthm);
        L = sindthm * sindthm + (cosdthm * cosdthm - sinthm * sinthm)
        * sindlamm * sindlamm;
        d = Math.acos(cosd = 1 - L - L);
        if (f != 0) {
            E = cosd + cosd;
            sind = Math.sin( d );
            Y = sinthm * cosdthm;
            Y *= (Y + Y) / (1. - L);
            T = sindthm * costhm;
            T *= (T + T) / L;
            X = Y + T;
            Y -= T;
            T = sind == 0 ? 1 : d / sind;
            D = 4. * T * T;
            A = D * E;
            B = D + D;
            daz.dist = a * sind * (T - f/4 * (T * X - Y) +
                    f*f/64 * (X * (A + (T - .5 * (A - E)) * X) -
                    Y * (B + E * Y) + D * X * Y));
            tandlammp = Math.tan(.5 * (dlam - .25 * (Y + Y - E * (4. - X)) *
                    (f/2 * T + f*f/64 * (32. * T - (20. * T - A)
                    * X - (B + 4.) * Y)) * Math.tan(dlam)));
        } else {
            daz.dist = a * d;
            tandlammp = Math.tan(dlamm);
        }
        u = Math.atan2(sindthm , (tandlammp * costhm));
        v = Math.atan2(cosdthm , (tandlammp * sinthm));
        daz.az12 = adjlon(Math.PI*2 + v - u);
        daz.az21 = adjlon(Math.PI*2 - v - u);
        
        return daz;
    }
    
    double toMeridianArc(double lat) {
        double  ff, fff, b0, B;
        
        ff  = f*f;
        fff = ff*f;
        b0  = a * ( 1.0 - f/2.0 + ff/16.0 + fff/32.0 );
        B   =   lat
                - ( 0.75*f + 3.0/8.0*ff + 15.0/128.0*fff ) * Math.sin( 2.0*lat )
                + ( 15.0/64.0*ff + 15.0/64.0*fff ) * Math.sin( 4.0*lat )
                - ( 35.0/384.0*fff ) * Math.sin( 6.0*lat );
        B   = b0*B;
        
        // alternativ, Mathisen s.66 :
        // B = (1-f/2+ff/16+fff/32) * fi  -	(0.75*f-3./128*fff) * sin(2*fi)
        //   + (15./64*ff+15./128*fff) * sin(4*fi)  -  35./384*fff * sin(6*fi);
        // B = a*B;
        
        return( B );
    }
    
    double fromMeridianArc(double B) {
        double  ff, fff, b0, B_b0, fi;
        
        ff   = f*f;
        fff  = ff*f;
        b0   = a * ( 1.0 - f/2.0 + ff/16.0 + fff/32.0 );
        B_b0 = B / b0;
        fi   = B_b0
                + ( 0.75*f + 3.0/8.0*ff + 21.0/256.0*fff ) * Math.sin( 2.0*B_b0 )
                + ( 21.0/64.0 * ( ff+fff ) ) * Math.sin( 4.0*B_b0 )
                + ( 151.0/768.0*fff ) * Math.sin( 6.0*B_b0 );
        /*
        if ( fabs(fi) > PI/2.0 )  *Err = -1;
        else		                  *Err =  0;
         */
        return( fi );
    }
    
    // Beregner uttrykket N/M-1 = e^2/(1-e*e) * (cos(fi))^2
// Dette beneves som epsilon i annen
    double getEpsilon_2( double dFi ) {
        double  Eps2, e, ee;
        
        e = getE2();
        ee   = e*e;
        double cosfi = Math.cos(dFi);
        Eps2 = ee / ( 1.0-ee ) * cosfi * cosfi;
        
        return( Eps2 );
    }
}
