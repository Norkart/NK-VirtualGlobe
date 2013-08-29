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
import org.odejava.World;
import org.odejava.collision.BulkContact;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;


/**
 * Representation of collection of collidable objects that can be evaluated as
 * a single group.
 *
 * See the specification definition at
 * http://www.xj3d.org/extensions/rigid_physics.html
 * <p>
 *
 * A collection is the root node of a set of objects that can be interacted.
 * This node does not have an equivalent X3D abstract node type.
 * <p>
 *
 * In the interest of optimisations, the group can be told whether or not one
 * or more sensors are interested in knowing the output of this node or not. If
 * they are not, then there is no need to bring any of the results back up into
 * the java world before pushing the values on into the rigid body model.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface VRMLNBodyGroupNodeType extends VRMLNodeType {

    /**
     * Set the owning world for this collision space. This is a hack to get
     * around a bad assumption made by ODE - that there is only ever one world
     * current at time. This is the owner world of this collsion system.
     *
     * @param wld The world instance we're using.
     */
    public void setOwningWorld(World wld);

    /**
     * Tell the group to evaluate its contents now. This will generate contacts
     * as needed.
     */
    public void evaluateCollisions();

    /**
     * This node is about to be deleted due to a change in loaded world. Clear
     * up the ODE resources in use.
     */
    public void delete();

    /**
     * Fetch the number of contacts that were generated during the last
     * evaluation. Needed so that we can iterate through the BulkContact object
     * returned from {@link #getContacts()}.
     *
     * @return A non-negative size indicator
     */
    public int numContacts();

    /**
     * Fetch the most recent set of contacts that have been evaluated for this
     * space. The bulk object can be used to step through all the available
     * contacts that were generated.
     *
     * @return The set of bulk contacts generated
     */
    public BulkContact getContacts();

    /**
     * Apply the contacts right now. All processing is complete, so it's
     * fine to continue the evaluation from before.
     */
    public void applyContacts();

    /**
     * Is this group enabled for use right now?
     *
     * @return true if this is enabled
     */
    public boolean isEnabled();
}
