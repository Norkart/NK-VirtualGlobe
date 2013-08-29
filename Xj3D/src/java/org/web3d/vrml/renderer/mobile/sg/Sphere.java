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
import java.util.HashMap;

// Application specific imports
import gl4java.GLFunc;
import gl4java.GLUFunc;
import gl4java.GLContext;
import gl4java.GLEnum;
import gl4java.drawable.GLDrawable;
import gl4java.utils.glut.GLUTFunc;
import gl4java.utils.glut.GLUTFuncLightImpl;

/**
 * A simple Sphere implementation.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */

public class Sphere extends Geometry {
    /** The radius of the Sphere */
    private float radius;

    /** The display list for this item */
    private static int dispList;

    /**
     * The default constructor with a radius of 1.
     */
    public Sphere() {
        this(1.0f);
    }

    /**
     * A constructor specifying the radius.
     *
     * @param radius The sphere's radius
     */
    public Sphere(float radius) {
         this.radius = radius;
         dispList = -1;
    }

    /**
     * Issue ogl commands needed for this component
     */
    public void renderState(GLDrawable gld) {
        GLFunc gl = gld.getGL();

        // Use a matrix scale to handle radius
        gl.glPushMatrix();
        gl.glScalef(radius, radius, radius);

        if (dispList == -1) {
            GLUFunc glu = gld.getGLU();

            dispList = gl.glGenLists(1);

            gl.glNewList(dispList, GLEnum.GL_COMPILE);
            GLUTFunc glut = null;
            glut = new GLUTFuncLightImpl(gl, glu);

            // Create a Unit Sphere
            glut.glutSolidSphere(1.0, 12, 12);
            gl.glEndList();
        } else {
            gl.glCallList(dispList);
        }

        gl.glPopMatrix();
    }

    /**
     * Restore all openGL state
     */
    public void restoreState(GLDrawable gld) {
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------
}