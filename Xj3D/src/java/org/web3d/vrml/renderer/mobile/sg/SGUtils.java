/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.mobile.sg;

// Standard imports
// none

// Application specific imports
import gl4java.GLFunc;
import gl4java.GLEnum;

import gl4java.drawable.GLDrawable;

public class SGUtils {
    public static void printMatrix(GLDrawable gld, int type) {
        GLFunc gl = gld.getGL();
        float mat[] = new float[16];

        gl.glGetFloatv(GLEnum.GL_MODELVIEW_MATRIX,mat);
        for(int i=0; i < mat.length; i++) {
            System.out.print(mat[i] + " ");
            if (i == 3 || i==7 || i ==11) System.out.println();
        }
        System.out.println();
    }
}