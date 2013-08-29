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

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;


/**
 * Representation of the top-level collection of RigidBody objects in Xj3D.
 * <p>
 *
 * A collection of rigid bodies is a collective of nodes that are evaluated as
 * a single set of conditions that need to be solved.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public interface VRMLRigidBodyGroupNodeType extends VRMLNodeType {

    /**
     * Go through the list of input contacts, process them and send them off
     * to ODE.
     */
    public void processInputContacts();

    /**
     * Adjust the model's timestep to the new value (in seconds). This is
     * called periodically to adjust the timestep made based on the current
     * frame rate to adjust the model.
     *
     * @param deltaT The time change in seconds
     */
    public void setTimestep(float deltaT);

    /**
     * Instruct the group node to evaluate itself right now based on the given
     * time delta from the last time this was evaluated. Does not check to see
     * if the model is enabled first. That should be done by the user code.
     */
    public void evaluateModel();

    /**
     * Update everything from ODE, back into the node fields. This is done at
     * the start of the next frame so that all the events, listeners etc fire
     * at the right time in the event model.
     */
    public void updatePostSimulation();

    /**
     * This node is about to be deleted due to a change in loaded world. Clear
     * up the ODE resources in use.
     */
    public void delete();

    /**
     * Is this group enabled for use right now?
     *
     * @return true if this is enabled
     */
    public boolean isEnabled();

    /**
     * Set the global gravity direction for this collection.
     *
     * @param gravity An array of 3 values for the gravity vector
     */
    public void setGravity(float[] gravity);

    /**
     * Get the current gravity vector applying to this collection.
     *
     * @param gravity An array to copy the values into
     */
    public void getGravity(float[] gravity);

    /**
     * Get the number of valid joints that this collection has
     *
     * @return A number greater than or equal to zero
     */
    public int numJoints();

    /**
     * Get the joint list, provides a live reference not a copy. The number of
     * valid values is available from numJoints();
     *
     * @return An array of VRMLNodeTypes
     */
    public VRMLNodeType[] getJoints();

    /**
     * Set the collection of Joint nodes that this collection should manage.
     * If passed a zero for numValid this method will remove all current
     * values.
     *
     * @param joints Array of new joint node instances to use
     * @param numValid The number of valid values to get from the array
     * @throws InvalidFieldValueException one of the provided nodes is not a
     *   X3DRigidJointNode instance
     */
    public void setJoints(VRMLNodeType[] joints, int numValid);

    /**
     * Get the number of valid bodies that this collection has
     *
     * @return A number greater than or equal to zero
     */
    public int numBodies();

    /**
     * Get the body list, provides a live reference not a copy. The number of
     * valid values is available from numBodies();
     *
     * @return An array of VRMLNodeTypes
     */
    public VRMLNodeType[] getBodies();

    /**
     * Set the collection of RigidBody nodes that this collection should manage.
     * If passed a zero for numValid this method will remove all current
     * values.
     *
     * @param bodies Array of new body node instances to use
     * @param numValid The number of valid values to get from the array
     * @throws InvalidFieldValueException one of the provided nodes is not a
     *   VRMLRigitBodyNodeType instance
     */
    public void setBodies(VRMLNodeType[] bodies, int numValid);
}
