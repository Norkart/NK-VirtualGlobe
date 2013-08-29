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

package org.web3d.vrml.renderer.norender.nodes.networking;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;
import org.web3d.vrml.renderer.common.nodes.networking.BaseAnchor;

/**
 * norender implementation of an Anchor node.
 * <p>
 *
 * The anchor node represents a standard grouping node that also contains
 * URL information.
 * <p>
 *
 * For dealing with user input, the current implementation automatically
 * overwrites any immediate child sensors that have been registered. This
 * is not correct behaviour, but our nodes do not handle multiple sensors
 * at the same level yet.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NRAnchor extends BaseAnchor implements NRVRMLNode {

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public NRAnchor() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     * <P>
     * Note that the world URL has not been set by this call and will need to
     * be called separately.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public NRAnchor(VRMLNodeType node) {
        super(node);
    }
}
