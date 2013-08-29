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

package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
import org.web3d.util.IntHashMap;
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * A sensor for reporting nBody collisions back to the X3D scene graph.
 * <p>
 *
 * See the specification definition at
 * http://www.xj3d.org/extensions/rigid_physics.html
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface VRMLNBodySensorNodeType extends VRMLSensorNodeType {
    /**
     * Update the outputs of this sensor now, based on the given set of
     * ODE contact information.
     *
     */
    public void updateContacts(IntHashMap bodyIdMap,
                               IntHashMap geomIdMap);
}
