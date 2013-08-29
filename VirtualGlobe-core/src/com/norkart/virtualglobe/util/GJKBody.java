/*
 * GJKBody.java
 *
 * Created on 21. mai 2008, 16:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.util;

import javax.vecmath.Vector3d;

/**
 * An interface representing a geometrical body used for distance and intersection 
 * computations using the GJK algorithm
 * @author runaas
 */
public interface GJKBody {
    /**
     * Compute the support point of the body; a point w given by:
     * w | w element of the convex body and v.dot(w) is the highest of all possible 
     * points in the body. Ie. w is a most extreme point of the body in the direction 
     * of v.
     * @param v The direction vector. If <CODE>v == null || v.equal(new Vector3d(0,0,0))</CODE> 
     * the method should return an arbitary point in the body
     * @param w The support point
     */
    public void support(Vector3d v, Vector3d w);
}
