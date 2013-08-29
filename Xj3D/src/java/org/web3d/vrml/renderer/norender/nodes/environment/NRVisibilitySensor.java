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

package org.web3d.vrml.renderer.norender.nodes.environment;

// Standard imports
//none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.environment.BaseVisibilitySensor;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * NoRender implementation of a VisibilitySensor node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class NRVisibilitySensor extends BaseVisibilitySensor
    implements NRVRMLNode {

    /**
     * Construct a new visibility sensor object
     */
    public NRVisibilitySensor() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the right type
     */
    public NRVisibilitySensor(VRMLNodeType node) {
        super(node);
    }
}