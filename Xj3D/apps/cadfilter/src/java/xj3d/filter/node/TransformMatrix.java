/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.node;

// External imports
import javax.vecmath.Matrix4f;

// Local imports
import org.web3d.vrml.renderer.common.nodes.group.BaseTransform;

/**
 * Wrapper for the BaseTransform node. Used to calculate the transform matrix.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class TransformMatrix extends BaseTransform {
    
    /**
     * Constructor
     */
    TransformMatrix() {
        super();
    }
    
    /**
     * Return the transform matrix
     *
     * @return the transform matrix
     */
    Matrix4f getMatrix() {
        updateMatrix();
        return(tmatrix);
    }
}
