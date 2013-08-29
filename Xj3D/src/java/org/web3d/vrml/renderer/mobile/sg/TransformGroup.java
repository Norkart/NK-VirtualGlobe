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

/**
 * A grouping node that contains a transform.  The node contains a single
 * transformation that can position, scale and rotate all its children.
 *
 * The specified transformation must be Affine and have uniform scaling
 * components(SRT-transform).  This class will not
 * check this constraint, so expect odd results if you break this rule, up
 * to and including a possible core reactor meltdown in a foreign country.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */

public class TransformGroup extends Group {
    Matrix4f localTransform;

    /**
     * The default constructor
     */
    public TransformGroup() {
        super();

        localTransform = new Matrix4f();
        transform = new Matrix4f();
    }

    /**
     * Construct a TransformGroup given a matrix.
     *
     * @param trans The matrix to use for transformation
     */
    public TransformGroup(Matrix4f trans) {
        this();
        localTransform.set(trans);

        transform = new Matrix4f();
    }

    /**
     * Set the transform matrix for this class.
     *
     * @param trans The matrix.  Copy by value semantics.
     */
    public void setTransform(Matrix4f trans) {
        localTransform.set(trans);
        transform.mul(localTransform);
    }

    /**
     * Tell a node to update its transform.
     */
     public void updateTransform(Matrix4f parentTrans) {
        if (parentTrans != null) {
            transform.set(parentTrans);
            transform.mul(localTransform);
        }
     }

    /**
     * Specify this nodes parent.
     */
    public void setParent(Node newParent) {
        // Override the parents impl
        parent = newParent;
    }


}