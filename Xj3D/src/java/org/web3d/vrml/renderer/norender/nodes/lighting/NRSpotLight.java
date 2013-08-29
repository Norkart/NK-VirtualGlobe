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

package org.web3d.vrml.renderer.norender.nodes.lighting;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.lighting.BaseSpotLight;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * no-render implementation of a spotlight.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class NRSpotLight extends BaseSpotLight
    implements NRVRMLNode {

    /**
     * Construct a new default instance of this class.
     */
    public NRSpotLight() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a light node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRSpotLight(VRMLNodeType node) {
        super(node);
    }
}
