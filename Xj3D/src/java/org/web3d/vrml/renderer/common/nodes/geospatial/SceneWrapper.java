/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geospatial;

// External imports

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;


/**
 * Wraps a scene as a VRMLExecutionSpace.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class SceneWrapper implements VRMLExecutionSpace {
    /** The scene being wrapped */
    private BasicScene scene;

    public SceneWrapper(VRMLScene scene) {
        this.scene = scene;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLExecutionSpace
    //----------------------------------------------------------

    /**
     * Get the contained scene graph that this instance has. This represents
     * everything about the internal scene that the node declaration wraps.
     * This is a real-time representation so that if it the nodes contains a
     * script that changes the internal representation then this instance will
     * be updated to reflect and changes made.
     *
     * @return The scene contained by this node instance
     */
    public BasicScene getContainedScene() {
        return scene;
    }
}
