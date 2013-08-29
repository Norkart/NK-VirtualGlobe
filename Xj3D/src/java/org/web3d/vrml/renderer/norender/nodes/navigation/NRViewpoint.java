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

package org.web3d.vrml.renderer.norender.nodes.navigation;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.navigation.BaseViewpoint;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null renderer implementation of a Viewpoint node.
 * <p>
 *
 * VRML requires the use of a headlight from the NavigationInfo node.
 * For convenience, we provide a headlight here that binds with the same
 * transform as the view platform.
 * <p>
 *
 * Viewpoints cannot be shared using DEF/USE. They may be named as such for
 * Anchor purposes, but attempting to reuse them will cause an error. This
 * implementation does not provide any protection against USE of this node
 * and attempting to do so will result in Java3D throwing exceptions - most
 * probably in the grouping node that includes this node.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class NRViewpoint extends BaseViewpoint implements NRVRMLNode {

    /**
     * Construct a default viewpoint instance
     */
    public NRViewpoint() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public NRViewpoint(VRMLNodeType node) {
        super(node);
    }
}
