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

package org.web3d.vrml.renderer.mobile.nodes;

// Standard imports
import java.util.Map;

// Application specific imports
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Base node for all mobile implementations.
 * <p>
 * Each node will keep its own fieldDeclarations and fieldMaps.  These will be
 * created in a static constructor so only one copy per class will be created.
 * <p>
 * Each node will maintain its own LAST_*_INDEX which tells others what the
 * last field declared by this node.
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.1 $
 */
public abstract class MobileNode extends AbstractNode implements MobileVRMLNode {

    /**
     * Create a new instance of this node with the given node type name.
     * The isDEF field is set to false and inSetup set to true.
     *
     * @param name The name of the type of node
     */
    public MobileNode(String name) {
        super(name);
    }
}
