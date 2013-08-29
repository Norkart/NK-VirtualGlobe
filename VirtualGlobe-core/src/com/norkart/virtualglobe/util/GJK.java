/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  PolytopeUtils.java
 *
 * Created on 19. mai 2008, 14:24
 *
 */

package com.norkart.virtualglobe.util;

import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;

/**
 *
 * @author runaas
 */
public final class GJK {
    private Vector3d priv_v = new Vector3d();
    private Vector3d w = new Vector3d();
    private Vector3d v_neg = new Vector3d();
    private Vector3d w_neg = new Vector3d();
    private Simplex  simplex = new Simplex();
    private static final double eps = 1e-7;
    
    private static ThreadLocal<GJK> instance = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new GJK();
        }
    };
    
    private class Simplex {
        Vector3d[]  X   = {new Vector3d(), new Vector3d(), new Vector3d(), new Vector3d()};
        double [][] dot = new double[4][4];
        double [][] D   = new double[16][4];
        int Ix;
        
        void initialize() {
            Ix = 0;
        }
        
        boolean update(Vector3d w, Vector3d v) {
            // Find place for new point,
            // test if w already in set (only happens in case of numerical errors)
            int free_ix = -1;
            for (int i = 0; i < 4; ++i) {
                if (((1 << i) & Ix) != 0) {
                    if (X[i].equals(w))
                        return false;
                } else if (free_ix < 0)
                    free_ix = i;
            }
            if (free_ix < 0) {
                v.set(0,0,0);
                return false;
            }
            // Set value and xpand simplex
            X[free_ix].set(w);
            Ix |= (1 << free_ix);
            
            // Fill dot matrix
            for (int i = 0; i < 4; ++i) {
                if (((1 << i) & Ix) != 0) {
                    dot[i][free_ix] = dot[free_ix][i] = X[free_ix].dot(X[i]);
                }
            }
            
            // Fill delta matrix
            for (int x = 1; x <= Ix; ++x) {
                // Is x a subset of the simplex?
                // Is the new point a member of x?
                if ((x & Ix) != x || (x & (1<<free_ix)) == 0) continue;
                
                for (int i = 0; i < 4; ++i) {
                    if (x == (1 << i))
                        D[x][i] = 1;
                    else if (0 != (x & (1<<i))) {
                        int sub_x = (x & ~(1<<i));
                        // Find lowest index
                        int k = 0;
                        for (; k<4 && (0 == (sub_x & (1<<k))); ++k);
                        D[x][i] = 0;
                        for (int j=0; j<4; ++j) {
                            if (0 != (x & (1<<j)))
                                D[x][i] += D[sub_x][j]*(dot[j][k]-dot[j][i]);
                        }
                    }
                }
            }
            
            // Compute new v and find smallest subset containing v
            for (int x = Ix; x > 0; --x) {
                // Is x a subset of the simplex?
                // Is the new point a member of x?
                if ((x & Ix) != x || (x & (1<<free_ix)) == 0) continue;
                
                // Test if v is contained in simplex
                boolean ok = true;
                for (int i = 0; i < 4 && ok; ++i) {
                    // is i member of the expanded simplex?
                    if (((1 << i) & Ix) != 0) {
                        if (((1 << i) & x) != 0) {
                            // Criterion 1
                            if (D[x][i] <= 0)
                                ok = false;
                        } else {
                            // Criterion 2
                            if (D[x | (1<<i)][i] > 0)
                                ok = false;
                        }
                    }
                }
                if (ok) {
                    // Ok, compute v as an affine combination of the spanning vectors
                    double vx = 0;
                    double vy = 0;
                    double vz = 0;
                    double d  = 0;
                    for (int i = 0; i < 4 && ok; ++i) {
                        if (((1 << i) & x) != 0) {
                            vx  += D[x][i] * X[i].x;
                            vy  += D[x][i] * X[i].y;
                            vz  += D[x][i] * X[i].z;
                            d   += D[x][i];
                        }
                    }
                    // d <= 0 should really be impossible...
                    if (d > 0) {
                        v.set(vx/d,vy/d,vz/d);
                        Ix = x;
                        return true;
                    }
                }
            }
            
            return false;
        }
    }
    
    public static GJK getInstance() {
        return instance.get();
    }
    
    /** Creates a new instance of PolytopeUtils */
    private GJK() {
    }
    
    public double dist(GJKBody A, GJKBody B) {
        double   my = 0;
        simplex.initialize();
        
        // Initialize v
        Vector3d v = priv_v;
        A.support(null, w);
        B.support(null, w_neg);
        v.sub(w, w_neg);
        for  (double dist = v.length(); dist > eps*1000; dist = v.length()) {
            // Compute the support point
            v_neg.negate(v);
            A.support(v_neg, w);
            B.support(v, w_neg);
            w.sub(w_neg);
            
            // Compute distance and test for completion
            double delta = v.dot(w)/dist;
            if (delta > my) my = delta;
            if (dist - my <= eps*dist)
                return dist;
            
            // Update the simplex and the v vector
            if (!simplex.update(w, v))
                // Return previous result if we fail to compute new value
                // This may occur when we have numerical instabilities in the
                // final iterations
                return v.length();
        }
        
        return 0;
    }
    
    public boolean intersect(GJKBody A, GJKBody B) {
        return  intersect(A, B, null);
    }
    
    public boolean intersect(GJKBody A, GJKBody B, Vector3d v) {
        simplex.initialize();
        
        // Initialize v
        if (v == null) {
            v = priv_v;
            v.set(0,0,0);
        }
        if (v.x == 0 && v.y == 0 && v.z == 0) {
            A.support(null, w);
            B.support(null, w_neg);
            v.sub(w, w_neg);
        }
        
        while (!(Math.abs(v.x) <= 1000*eps && Math.abs(v.y) <= 1000*eps && Math.abs(v.z) <= 1000*eps)) {
            // Compute the support point
            v_neg.negate(v);
            A.support(v_neg, w);
            B.support(v, w_neg);
            w.sub(w_neg);
            
            // test for completion
            double dot = v.dot(w);
            if (dot > eps) 
                return false;
                
            // Update the simplex and the v vector
            if (!simplex.update(w, v)) 
                return v.length() <= eps*1000;
        }
        
        return true;
    }
}
