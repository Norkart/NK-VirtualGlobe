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

package org.web3d.vrml.renderer.mobile.nodes.sensor;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.sensor.BaseSphereSensor;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * OpenGL-renderer implementation of a SphereSensor node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class MobileSphereSensor extends BaseSphereSensor
    implements MobileVRMLNode {

    // Field index constants

    /**
     * Construct a new time sensor object
     */
    public MobileSphereSensor() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public MobileSphereSensor(VRMLNodeType node) {
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
