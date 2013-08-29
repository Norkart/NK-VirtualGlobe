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

package org.web3d.vrml.renderer.mobile.nodes.navigation;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.navigation.BaseNavigationInfo;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * Null renderer implementation of a NavigationInfo node.
 * <p>
 *
 * The NavigationInfo node does not occupy a space in the Java 3D
 * scenegraph. This is used as a VRML construct only. When VRML changes the
 * values here, we pass them back courtesy of the listeners to the children
 * nodes.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class MobileNavigationInfo extends BaseNavigationInfo
    implements MobileVRMLNode {

    /**
     * Construct a default node with all of the values set to the given types.
     */
    public MobileNavigationInfo() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public MobileNavigationInfo(VRMLNodeType node) {
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
