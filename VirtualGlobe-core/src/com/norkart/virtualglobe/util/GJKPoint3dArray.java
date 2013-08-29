/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  GJKPolytope.java
 *
 * Created on 23. mai 2008, 16:48
 *
 */

package com.norkart.virtualglobe.util;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author runaas
 */
public class GJKPoint3dArray implements GJKBody {
    Vector3d[] p;
    
    /** Creates a new instance of GJKPoint3dArray */
    public GJKPoint3dArray(Point3d[] p) {
        this.p = new Vector3d[p.length];
        for (int i=p.length; --i>=0; ) {
            this.p[i] = new Vector3d(p[i]);
        }
    }
    public void support(Vector3d v, Vector3d w) {
        if (v == null || (v.x == 0 && v.y == 0 && v.z == 0)) {
            w.set(p[0]);
            return;
        }
        int ix = p.length-1;
        double dist = v.dot(p[ix]);
        for (int i=ix; --i>=0; ) {
            double d = v.dot(p[i]);
            if (d > dist) {
                ix = i;
                dist = d;
            }
        }
        w.set(p[ix]);
    }
}
