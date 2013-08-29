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

package org.web3d.vrml.renderer.norender.nodes.group;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.group.BaseSwitch;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Norender version of a Switch node.
 * <p>
 *
 * This code assigns a LOD node as the implGroup. As this code is a
 * grouping node, we allow the use of the children being specified as
 * either the <code>childre</code> field or the <code>level</code> field.
 * The former is VRML3.0 and the latter VRML 2.0
 * <p>
 *
 * The LOD Behavior node is kept as a child of the group node that works
 * here. When the VRML node is removed from the scene, the behavior is too.
 * When the behaivor is asked to be disabled, we just call the
 * <code>setEnable</code> method on the behavior, we do not remove it.
 * <p>
 * If someone routes a range change to us, then we see if it is the same
 * length. If it is, we just set the range values in the existing behavior.
 * If not, then we have to replace the old LOD with a new LOD that has the
 * correct number of nodes in it. If the size of the range grows such that it
 * is greater than the number of children nodes, we disable the behavior until
 * the number of children increase to the correct amount. In the branchgroup,
 * the behavior is always
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class NRSwitch extends BaseSwitch
    implements NRVRMLNode {

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public NRSwitch() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public NRSwitch(VRMLNodeType node) {
        super(node);
    }
}
