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
 * A simple Box implementation.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */

public class Box extends Geometry {
    /** The radius of the Sphere */
    private float[] size;

    /** The display list for this item */
    private static int dispList;

    /**
     * The default constructor with a size of (2,2,2)
     */
    public Box() {
        this(new float[] {2.0f, 2.0f, 2.0f}, false);
    }

    /**
     * A constructor specifying the size.
     *
     * @param size The size of the box
     */
    public Box(float[] size, boolean byRef) {
        if (byRef) {
            this.size = size;
        } else {
            this.size = new float[3];

            this.size[0] = size[0];
            this.size[1] = size[1];
            this.size[2] = size[2];
        }
        dispList = -1;
    }

    /**
     * Issue ogl commands needed for this component
     */
    public void renderState(GLDrawable gld) {
        GLFunc gl = gld.getGL();

        // Use a matrix scale to handle size
        gl.glPushMatrix();
        gl.glScalef(size[0], size[1], size[2]);

        if (dispList == -1) {
            GLUFunc glu = gld.getGLU();

            dispList = gl.glGenLists(1);

            gl.glNewList(dispList, GLEnum.GL_COMPILE_AND_EXECUTE);
            GLUTFunc glut = null;
            glut = new GLUTFuncLightImpl(gl, glu);

            // Create a unit Cube
            glut.glutSolidCube(1.0);
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