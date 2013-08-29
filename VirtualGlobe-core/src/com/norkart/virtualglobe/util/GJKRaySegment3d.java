/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  GJKRaySegment3d.java
 *
 * Created on 26. mai 2008, 12:03
 *
 */

package com.norkart.virtualglobe.util;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author runaas
 */
public class GJKRaySegment3d implements GJKBody {
    Point3d  p0;
    Vector3d dv;
    double t1, t2;
    
    /** Creates a new instance of GJKRaySegment3d */
    public GJKRaySegment3d(Point3d  p, Vector3d v) {
        this(p, v, 0, 1);
    }
    
    public GJKRaySegment3d(Point3d  p, Vector3d v, double t1, double t2) {
        this.p0 = p;
        this.dv = v;
        this.t1 = t1;
        this.t2 = t2;
    }
    
    public void support(Vector3d v, Vector3d w) {
        if (v == null || (v.x*dv.x + v.y*dv.y + v.z*dv.z <= 0))
            w.set(t1*dv.x + p0.x, t1*dv.y + p0.y, t1*dv.z + p0.z);
        else
            w.set(t2*dv.x + p0.x, t2*dv.y + p0.y, t2*dv.z + p0.z);
    }
}
