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

package org.web3d.vrml.renderer.j3d.nodes;

// External imports
import java.util.Map;

import javax.media.j3d.SceneGraphPath;

// Local imports
import org.web3d.vrml.nodes.VRMLPickingSensorNodeType;

/**
 * An abstract representation of a node that is picking sensor within the
 * Java3D renderer.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface J3DPickingSensorNodeType
    extends VRMLPickingSensorNodeType, J3DVRMLNode {

    /**
     * A check to see if the parent scene graph path has changed from last
     * time we checked for this node. Assumes that the call is being made on
     * a node that we checked on last frame. If this has been just changed with
     * a new binding call then the caller should just immediately request the
     * current path anyway.
     *
     * @return true if the parent path has changed since last frame
     */
    public boolean hasScenePathChanged();

    /**
     * Fetch the scene graph path from the root of the scene to this node.
     * Typically used for the getLocalToVWorld transformation handling.
     * the node returns null then there is no path to the root of the scene
     * ie this node is somehow orphaned during the last frame.
     *
     * @return The fully qualified path from the root to here
     */
    public SceneGraphPath getSceneGraphPath();

    /**
     * Get the set of target branchgroups that this sensor manages. If there
     * are none, return an empty set.
     *
     * @return A set of J3D nodes mapped to their VRML wrapper
     */
    public Map getTargetGroups();
}
