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

package org.web3d.vrml.renderer.norender.nodes.enveffects;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.enveffects.BaseFog;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Non-renderable implementation of a fog node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class NRFog extends BaseFog implements NRVRMLNode {

    /**
     * Construct a new default instance of this class.
     */
    public NRFog() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public NRFog(VRMLNodeType node) {
        super(node);
    }
}
