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

package org.web3d.vrml.renderer.norender.nodes.scripting;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.scripting.BaseScript;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Scene graph representation of a script node.
 * <p>
 *
 * The script is different to all the other nodes. While it represents
 * a script, it doesn't have the normal content of a Java3D node. It is
 * also a bit different to the ordinary Abstract node implementation in
 * that a script can have fields added and removed on demand.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class NRScript extends BaseScript
    implements NRVRMLNode {

    /**
     * Construct a default instance of the script
     */
    public NRScript() {
        super();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public NRScript(VRMLNodeType node) {
        super(node);
    }
}
