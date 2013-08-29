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

package org.web3d.vrml.renderer.mobile.nodes.time;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.time.BaseTimeSensor;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * OpenGL-renderer implementation of a TimeSensor node.
 * <p>
 *
 * The implementation uses the standard VRML time clock to send and retrieve
 * time information. As an efficiency measure, if the time sensor is disabled
 * it will remove itself as a listener to the global clock. When it becomes
 * re-enabled that listener will be added back again.
 * <p>
 *
 * When setting values we always set the variable first and then set the flag
 * indicating that the field has changed. This is so that we don't end up with
 * a multi-threaded access thinking that a value has changed when it really
 * hasn't (yet) and then ignoring the value. As we desperately try to avoid
 * synchronized access internally, we try to set the real value first so that
 * if someone does check and find it's changed, we can then give them the real
 * value as soon as they ask for it.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class MobileTimeSensor extends BaseTimeSensor
    implements MobileVRMLNode {

    /**
     * Construct a new time sensor object
     */
    public MobileTimeSensor() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public MobileTimeSensor(VRMLNodeType node) {
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
