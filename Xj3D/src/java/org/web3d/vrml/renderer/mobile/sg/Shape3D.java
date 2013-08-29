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
import javax.vecmath.Matrix4f;

// Application specific imports
import gl4java.GLFunc;
import gl4java.GLEnum;

import gl4java.drawable.GLDrawable;

/**
 * A Shape3D class wraps all geometry and appearance information.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */

public class Shape3D extends Leaf {
    private Geometry geom;
    private Appearance app;

    /** A local scratch variable to change row/col order for vecmatch to openGL */
    private float[] mat;

    /**
     * Creates a shape with no geometry or appearance set.
     */
    public Shape3D() {
        mat = new float[16];
    }

    /**
     * Set the geometry for this shape.
     *
     * @param newGeom The geometry
     */
    public void setGeometry(Geometry newGeom) {
         geom = newGeom;
         if (geom != null)
            geom.setSGManager(sgm);
    }

    /**
     * Set the appearance for this shape.
     *
     * @param newApp The appearance
     */
    public void setAppearance(Appearance newApp) {
         app = newApp;
         if (app != null)
            app.setSGManager(sgm);
    }

    /**
     * Set the scenegraph manager for this node.  It will notify
     * all its children of the value.
     *
     *@param sgm The parent SG
     */
    public void setSGManager(SGManager sgm) {
        this.sgm = sgm;

        if (app != null)
            app.setSGManager(sgm);

        if (geom != null)
            geom.setSGManager(sgm);
    }

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     */
    public void render(GLDrawable gld) {
        GLFunc gl = gld.getGL();

        gl.glPushMatrix();
        gl.glMultMatrixf(transform.data);

        //System.out.println();
        //SGUtils.printMatrix(gld, GLEnum.GL_MODELVIEW_MATRIX);

        if(app != null)
            app.renderState(gld);

        if(geom != null)
            geom.renderState(gld);
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     */
    public void postRender(GLDrawable gld) {
        GLFunc gl = gld.getGL();

        if(geom != null)
            geom.restoreState(gld);

        if(app != null)
            app.restoreState(gld);

        gl.glPopMatrix();
    }
}