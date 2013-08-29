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

package org.web3d.vrml.renderer.mobile.nodes.interpolator;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.interpolator.BaseNormalInterpolator;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * Implementation of a NormalInterpolator.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class MobileNormalInterpolator extends BaseNormalInterpolator
    implements MobileVRMLNode {

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     */
    public MobileNormalInterpolator() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not an interpolator node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public MobileNormalInterpolator(VRMLNodeType node) {
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
