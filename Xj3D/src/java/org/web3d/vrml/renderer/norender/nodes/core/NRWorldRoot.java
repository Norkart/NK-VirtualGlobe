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

package org.web3d.vrml.renderer.norender.nodes.core;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLChildNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.renderer.common.nodes.core.BaseWorldRoot;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Implementation of the world root class.
 * <p>
 *
 * Extends the basic root node functionality with Java3D specific capabilities
 * - namely the ability to get the root node BranchGroup instance. This is the
 * object returned by the <code>getSceneGraphObject()</code> method.
 * <p>
 * The world root has 3 fields available to use:
 * <ul>
 * <li>children</li>
 * <li>bboxSize</li>
 * <li>bboxCenter</li>
 * </ul>
 *
 * <b>Note:</b> This may want to extend BranchGroup instead.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class NRWorldRoot extends BaseWorldRoot implements NRVRMLNode {
    /** Message when an externproto does not fit the ChildNode requirement */
    private static final String BAD_PROTO_MSG =
        "The resolved proto instance is not a X3DChildNode type. Grouping " +
        "nodes may only use ChildNode types for the children field.";

    /**
     * Construct an instance of this node.
     */
    public NRWorldRoot() {
    }

    //----------------------------------------------------------
    // Overrding base methods
    //----------------------------------------------------------

    /**
     * Handle notification that an ExternProto has resolved.
     *
     * @param index The field index that got loaded
     * @param node The owner of the node
     */
    public synchronized void notifyExternProtoLoaded(int index, VRMLNodeType node) {

        if(!(node instanceof VRMLChildNodeType) && !(node instanceof VRMLProtoInstance))
            throw new InvalidFieldValueException(BAD_PROTO_MSG);

        if(inSetup)
            return;

        // ignore as we have no renderer implementation
    }

    /**
     * Add a single child node to the list of available children. This auto
     * matically deals with DEF/USE and adds links and branchgroups where
     * appropriate.
     *
     * @param node The node to view
     */
    protected void addChildNode(VRMLNodeType node) {

        if(node instanceof NRVRMLNode)
            super.addChildNode(node);
        else
            throw new InvalidFieldValueException("Node is not a NRVRMLNode");
    }
}
