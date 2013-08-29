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

package org.web3d.vrml.renderer.mobile.nodes.networking;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.networking.BaseAnchor;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;


/**
 * OpenGL implementation of an Anchor node.
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
 * <p>
 *
 * This implementation doesn't do anything currently. It needs to implement
 * all the grouping node concepts in the OpenGL code.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class MobileAnchor extends BaseAnchor implements MobileVRMLNode {

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public MobileAnchor() {
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
    public MobileAnchor(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods from MobileVRMLNode class.
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }
}
