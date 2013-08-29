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
import gl4java.drawable.GLDrawable;

/**
 * A Node class is the base class for all renderable nodes in the SceneGraph.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */

public abstract class Node extends SceneGraphObject {
    /** The parent of this node */
    protected Node parent;

    /** The premultiplied transformation applied to this node */
    protected Matrix4f transform;

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     */
    public void render(GLDrawable gld) {}

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     */
    public void postRender(GLDrawable gld) {}

    /**
     * Specify this nodes parent.
     */
    public void setParent(Node newParent) {
        parent = newParent;
    }

    /**
     * Tell a node to update its bounding rep.
     */
    public void updateBounds() {
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

    public Matrix4f getTransform() {
        return transform;
    }
}