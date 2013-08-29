/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  GaussKrugerTransform.java
 *
 * Created on 13. august 2007, 15:00
 *
 */

package com.norkart.geopos;

/**
 *
 * @author runaas
 */
public class GaussKrugerProjection {
    private Ellipsoid ellps;
    
    double lon0, x0, y0, k0;
    
    
    /** Creates a new instance of GaussKrugerTransform */
    public GaussKrugerProjection(Ellipsoid ellps, double lon0, double x0, double y0, double k0) {
        this.ellps = ellps;
        this.lon0 = lon0;
        this.x0 = x0;
        this.y0 = y0;
        this.k0 = k0;
    }
    
    public void forward(double [] lonlat, int lonlat_ix, double [] xy, int xy_ix) {
        
    }
    
    public void reverse(double [] xy, int xy_ix, double [] lonlat, int lonlat_ix) {
        
    }
    
    
// Fra geografiske koordinater til gaussiske
    void Geodetisk2Gausisk(double [] lonlat, int lonlat_ix, double [] xy, int xy_ix
            /*double dB, double dL, CVGProjeksjon *pProjeksjon, double *pdX, double *pdY */) {
        
        
        //Dersom punkt ikke ligger i arktiste strøk          : 1.256637  rad = 72 deg
        // OG
        //Dersom punkt ikke ligger langt fra sentralmeridian : 0.1221730 rad =  7 deg
        // Brukes enkle raske formler
        double dDL = Ellipsoid.adjlon( lonlat[lonlat_ix*2]-lon0 );          // dL : lengdeforskjell
        if (Math.abs(dDL)<0.1221730 && Math.abs(lonlat[lonlat_ix*2+1])<1.256637)
            Geodetisk2Gausisk_trad( lonlat, lonlat_ix, xy, xy_ix );
        else
            Geodetisk2Gausisk_hyp( lonlat, lonlat_ix, xy, xy_ix );
        xy[xy_ix*2  ] = xy[xy_ix*2  ]*k0 + x0;
        xy[xy_ix*2+1] = xy[xy_ix*2+1]*k0 + y0;
    }
    
    /* ************************************************************ */
    
// Fra geografiske koordinater til gaussiske
// HYPERBOLIC FUNCTION FOR THE GAUSSIAN PROJECTION
// The formulas are found in the appendix to "DEN GAUSSISKE PROJEKSJONEN", O. Mathisen
    void Geodetisk2Gausisk_hyp(double [] lonlat, int lonlat_ix, double [] xy, int xy_ix
            /*double dB, double dL, CVGProjeksjon *pProjeksjon, double *pdX, double *pdY */) {
        int     Err=0;
        double   a, f, l0, e;
        double b0,b1,b2,b3;
        double u,v,w;
        
        a  = ellps.getA();   // a : lang halvakse
        f  = ellps.getF();   // f : flattrykning
        e  = Math.sqrt(f*(2-f));	                    // Eccentricity
        
        double df2 = f*f;  // pow(f,2)
        double df3 = df2*f;// pow(f,3)
        
        double dL = Ellipsoid.adjlon( lonlat[lonlat_ix*2]-lon0 );          // dL : lengdeforskjell
        double dB = lonlat[lonlat_ix*2+1];
        
        b0 = a * ( 1 - f/2.0 + df2/16.0 + df3/32.0 );
        b1 = a * ( f/4 - df2/6.0 - df3*11.0/384.0 );
        b2 = a * ( df2*13.0/192.0 - df3*79.0/1920.0 );
        b3 = a * ( df3*61.0/1920.0 );
        
        w = (Math.atan( Math.tan(dB/2.0 + Math.PI/4.0)*Math.pow((1.0-e*Math.sin(dB))/(1.0+e*Math.sin(dB)),e/2.0)) - Math.PI/4.0)*2.0;
        
        u = Math.atan2(Math.tan(w),Math.cos(dL));
        v = Math.log( (1.0+Math.cos(w)*Math.sin(dL))/(1.0-Math.cos(w)*Math.sin(dL)) )/2.0;
        
        double d2u = 2.0*u;
        double d2v = 2.0*v;
        double d4u = 4.0*u;
        double d4v = 4.0*v;
        double d6u = 6.0*u;
        double d6v = 6.0*v;
        
        xy[xy_ix*2+1] = b0*u + b1*Math.sin(d2u)*Math.cosh(d2v) + b2*Math.sin(d4u)*Math.cosh(d4v) + b3*Math.sin(d6u)*Math.cosh(d6v);
        xy[xy_ix*2  ] = b0*v + b1*Math.cos(d2u)*Math.sinh(d2v) + b2*Math.cos(d4u)*Math.sinh(d4v) + b3*Math.cos(d6u)*Math.sinh(d6v);
    }
    
    /* ************************************************************ */
    
// Fra geografiske koordinater til gaussiske
    void Geodetisk2Gausisk_trad(double [] lonlat, int lonlat_ix, double [] xy, int xy_ix
            /*double dB, double dL, CVGProjeksjon *pProjeksjon, double *pdX, double *pdY */) {
        int     Err=0;
        double  a, f, B, l0, l, ll, lll, N, sfi, cfi, c3fi, c5fi, t2fi, t4fi, Eps2;
        
        a  = ellps.getA();          // a : lang halvakse
        f  = ellps.getF();          // f : flattrykning
        
        l    = Ellipsoid.adjlon( lonlat[lonlat_ix*2]-lon0 );               // l : lengdeforskjell
        double dB = lonlat[lonlat_ix*2+1];
        
        ll   = l*l;
        lll  = ll*l;
        B    = ellps.toMeridianArc( dB );// B : Meridianbuelengde
        N    = ellps.getN( dB );
        sfi  = Math.sin( dB );
        cfi  = Math.cos( dB );
        c3fi = cfi * cfi * cfi;
        c5fi = c3fi * cfi * cfi;
        t2fi = Math.tan(dB);
        t2fi = t2fi*t2fi;
        t4fi = t2fi*t2fi;
        Eps2 = ellps.getEpsilon_2( dB );
        
        xy[xy_ix*2+1] =   B
                + ll/2.0 * N*sfi*cfi
                + ll*ll/24.0 * N*sfi*c3fi * ( 5.0 - t2fi + 9.0*Eps2 + 4.0*Eps2*Eps2 )
                + lll*lll/720.0 * N*sfi*c5fi * ( 61.0 - 58.0*t2fi + t4fi );
        
        xy[xy_ix*2  ] =   l*N*cfi
                + lll/6.0 * N*c3fi * ( 1.0 - t2fi + Eps2 )
                + lll*ll/120.0 * N*c5fi * ( 5.0 - 18.0*t2fi + t4fi );
    }
    
    
    
    
    /* ************************************************************ */
    
// Fra gaussiske (x,y) til geografiske (dB,dL) koordinater
    void Gausisk2Geodetisk(double [] xy, int xy_ix, double [] lonlat, int lonlat_ix
            /*double dX, double dY, CVGProjeksjon *pProjeksjon, double *pdB, double *pdL */) {
        
        
        //Dersom punkt ikke ligger i arktiste strøk          : 7988932 = 72 deg
        // OG
        //Dersom punkt ikke ligger langt fra sentralmeridian : 0.1221730 rad =  7 deg
        // Brukes enkle raske formler
        if (Math.abs((xy[xy_ix*2]-x0)/k0)<240946 && Math.abs((xy[xy_ix*2+1]-y0)/k0)<7988932)
            Gausisk2Geodetisk_trad( xy, xy_ix, lonlat, lonlat_ix  );
        else
            Gausisk2Geodetisk_hyp( xy, xy_ix, lonlat, lonlat_ix );
    }
    
    /* ************************************************************ */
    
// Fra gaussiske (x,y) til geografiske (dB,dL) koordinater
// HYPERBOLIC FUNCTION FOR THE GAUSSIAN PROJECTION
// The formulas are found in the appendix to "DEN GAUSSISKE PROJEKSJONEN", O. Mathisen
    void Gausisk2Geodetisk_hyp(double [] xy, int xy_ix, double [] lonlat, int lonlat_ix
            /*double dX, double dY, CVGProjeksjon *pProjeksjon, double *pdB, double *pdL */) {
        // int		 Err=0;
        double   a, f, l0, e;
        double b0,c1,c2,c3;
        double u,v,p,l,w,fi;
        
        a  = ellps.getA();// a	 : lang halvakse
        f  = ellps.getF(); // f	 : flattrykning
        e  = Math.sqrt(f*(2.0-f));	// Eccentricity
        
        double df2 = f*f;  // pow(f,2)
        double df3 = df2*f;// pow(f,3)
        
        b0 = a * ( 1.0 - f/2.0 + df2/16.0 + df3/32.0 );
        c1 = f/4.0 - df2/24.0 - df3*43.0/768.0;
        c2 = df2/192.0 + df3*13.0/960.0;
        c3 = df3*17.0/3840.0;
        
        double dX = (xy[xy_ix*2+1] - y0)/k0;
        double dY = (xy[xy_ix*2  ] - x0)/k0;
        
        double d2X = 2.0*dX;
        double d2Y = 2.0*dY;
        double d4X = 4.0*dX;
        double d4Y = 4.0*dY;
        double d6X = 6.0*dX;
        double d6Y = 6.0*dY;
        
        u = dX/b0 - c1*Math.sin(d2X/b0)*Math.cosh(d2Y/b0) - c2*Math.sin(d4X/b0)*Math.cosh(d4Y/b0);// - c3*sin(d6X/b0)*cosh(d6Y/b0);
        v = dY/b0 - c1*Math.cos(d2X/b0)*Math.sinh(d2Y/b0) - c2*Math.cos(d4X/b0)*Math.sinh(d4Y/b0);// - c3*cos(d6X/b0)*sinh(d6Y/b0);
        
        p = (Math.atan(Math.exp(v)) - Math.PI/4.0)*2.0;
        
        // l	 : lengdeforskjell    (rad)
        l = Math.atan2(Math.tan(p),Math.cos(u));	// Longitude relative to the central meridian
        
        w = Math.atan2(Math.sin(u),Math.cos(u)*Math.cos(l)+Math.tan(p)*Math.sin(l));
        
        fi = w + (f + df2/3.0 - df3/6.0) * Math.sin(2.0*w) + (df2*7.0/12.0 + df3*23.0/60.0) * Math.sin(4.0*w) + df3* 7.0/15.0 * Math.sin(6.0*w);
        
        lonlat[lonlat_ix*2+1] = Math.toDegrees(fi);			             // Latitude in rad
        lonlat[lonlat_ix*2  ] = Math.toDegrees(Ellipsoid.adjlon( l + lon0 ));      // Longitude in rad
        
    }
    
    /* ************************************************************ */
    
// Fra gaussiske (x,y) til geografiske (dB,dL) koordinater
//Bruker tradisjonelle formler
    void Gausisk2Geodetisk_trad(double [] xy, int xy_ix, double [] lonlat, int lonlat_ix
            /*double dX, double dY, CVGProjeksjon *pProjeksjon, double *pdB, double *pdL */) {
        int	   Err=0;
        double   a, f, l0;
        double   yy, yyy, cfiF, tfiF, t2fiF, t4fiF, fiF, Eps2F, MF, NF, NF3, NF5, dl;
        
        
        a  = ellps.getA(); // a	 : lang halvakse
        f  = ellps.getF(); // f	 : flattrykning
        
        double dX = (xy[xy_ix*2+1] - y0)/k0;
        double dY = (xy[xy_ix*2  ] - x0)/k0;
        
        yy  = dY*dY;
        yyy = dY*yy;
        fiF = ellps.fromMeridianArc( dX );
        
        /*
        if ( Err < 0 ) {
         *pdB     = 0.0;
         *pdL = 0.0;
            return( Err );
        }*/
        
        cfiF	= Math.cos( fiF );
        tfiF	= Math.tan( fiF );
        t2fiF   = tfiF*tfiF;
        t4fiF    = t2fiF*t2fiF;
        MF	= ellps.getM( fiF );
        NF	= ellps.getN( fiF );
        NF3	= NF  * NF * NF;
        NF5	= NF3 * NF * NF;
        Eps2F	= NF/MF - 1.0;
        
        lonlat[lonlat_ix*2+1] =   Math.toDegrees(fiF
                - yy/2.0 * tfiF/( MF*NF )
                + yy*yy/24.0 * tfiF/( MF*NF3 ) *
                ( 5.0 + 3.0*t2fiF + Eps2F - 9.0*Eps2F*t2fiF - 4.0*Eps2F*Eps2F )
                - yyy*yyy/720.0 * tfiF/( MF*NF5 ) * ( 61.0 + 90.0*t2fiF + 45.0*t4fiF ));
        
        // dl	 : lengdeforskjell i forhold til sentralmeridian   (rad)
        dl =   dY / ( NF*cfiF )
        - yyy / ( 6.0*NF3*cfiF ) * ( 1.0 + 2.0*t2fiF + Eps2F )
        + yy*yyy / ( 120.0*NF5*cfiF ) * ( 5.0 + 28.0*t2fiF + 24.0*t4fiF );
        
        lonlat[lonlat_ix*2  ] = Math.toDegrees(Ellipsoid.adjlon( lon0+dl ));
    }
    
    
}
