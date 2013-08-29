/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes;

// Standard imports
import javax.vecmath.Matrix4f;

// Application specific imports

/**
 * This node contains an aviatrix TransformGroup.  All OGL nodes which
 * have their impls under a TransformGroup must implement this interface.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface OGLTransformNodeType {
    /**
     * Get the transform matrix for this node.  A reference is ok as
     * the users of this method will not modify the matrix.  This should
     * be the X3D level value, not pulled from Aviatrix3D.
     *
     * @return The matrix.
     */
    public Matrix4f getTransform();
}
