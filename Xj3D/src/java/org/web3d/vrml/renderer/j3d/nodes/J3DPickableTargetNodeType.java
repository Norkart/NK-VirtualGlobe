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
import javax.media.j3d.Group;

// Local imports
// None

/**
 * An abstract representation of a node that can be used as a target for
 * picking within the Java3D system.
 * <p>
 *
 * Since picking requires an object to pick and something to pick against and
 * this interface provides the Java3D Group to pick against as a target
 * for picking. The group that is returned must have pick reporting
 * turned on.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface J3DPickableTargetNodeType {

    /**
     * Fetch the group that this target will pick against.
     *
     * @return The valid branchgroup to use
     */
    public Group getPickableGroup();
}
