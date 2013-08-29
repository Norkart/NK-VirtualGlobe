/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  GJKPoint3d.java
 *
 * Created on 26. mai 2008, 11:45
 *
 */

package com.norkart.virtualglobe.util;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author runaas
 */
public class GJKPoint3d extends Point3d implements GJKBody {
     public void support(Vector3d v, Vector3d w) {
         w.set(this);
     }
}
