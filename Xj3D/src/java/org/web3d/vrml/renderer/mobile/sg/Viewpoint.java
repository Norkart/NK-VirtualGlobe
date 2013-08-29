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
import javax.vecmath.Matrix4f;

// Application specific imports
import gl4java.GLFunc;
import gl4java.GLUFunc;
import gl4java.GLContext;
import gl4java.GLEnum;
import gl4java.drawable.GLDrawable;
import gl4java.utils.glut.GLUTFunc;
import gl4java.utils.glut.GLUTFuncLightImpl;

/**
 * A viewpoint into the scene.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */

public class Viewpoint extends Leaf {
    /** The display list for this item */
    private static int dispList;

    /** A local scratch variable to change row/col order for vecmatch to openGL */
    private float[] mat;

    /** Is this the active viewpoint? */
    private boolean isActive;

    /**
     * The default constructor
     */
    public Viewpoint() {
        mat = new float[16];
        isActive = false;
    }

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     */
    public void render(GLDrawable gld) {
    }

    /**
      * Setup the viewing matrix
      */
    public void setupView(GLDrawable gld) {
        GLFunc gl = gld.getGL();

        gl.glLoadIdentity();

        // Wait till the nodes are initialized
        if (transform != null) {
            gl.glMultMatrixf(transform.data);
            //System.out.println("View matrix: " + transform);
            //SGUtils.printMatrix(gld,0);

        }
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     */
    public void postRender(GLDrawable gld) {
    }

    /**
     * Tell a node to update its transform.
     */
    public void updateTransform(Matrix4f parentTrans) {
        if (parentTrans != null) {
            // Default for all nodes is to use its parent transform
            transform = parentTrans;
        }
    }

    /**
     * Set this viewpoint to be the active viewpoint.
     */
    protected void setActive(boolean active) {
         isActive = active;
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------
}