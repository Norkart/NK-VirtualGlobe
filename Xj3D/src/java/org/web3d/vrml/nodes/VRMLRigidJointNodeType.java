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
import org.odejava.JointGroup;
import org.odejava.World;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;


/**
 * Representation of a X3DRigidJointNode extension node in Xj3D.
 * <p>
 *
 * A joint represents the connection between two bodies.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface VRMLRigidJointNodeType extends VRMLNodeType {

    /**
     * This node is about to be deleted due to a change in loaded world. Clear
     * up the ODE resources in use.
     */
    public void delete();

    /**
     * Get the number of valid fields that the user has requested updates for.
     *
     * @return a value greater than or equal to zero
     */
    public int numOutputs();

    /**
     * Get the array of output field indices for this joint. These are
     * previously mapped internally from the output listing to the field
     * index values corresponding to the user-supplied field names, as well
     * as processing for the special NONE and ALL types.
     *
     * @return an array of field indices that are to be used
     */
    public int[] getOutputFields();

    /**
     * Instruction to the node to fetch the appropriate field values' output
     * from the physics model and update the outputOnly field with the values.
     */
    public void updateRequestedOutputs();

    /**
     * Set the parent world that this body belongs to. A null value clears
     * the world and indicates the physics model or body is no longer in use
     * by this world (eg deletes it).
     *
     * @param wld The new world instance to use or null
     * @param grp The group that this joint should belong to
     */
    public void setODEWorld(World wld, JointGroup grp);

    /**
     * Set the node that should be used for the first body. Setting a value of
     * null will clear the current body in use.
     *
     * @param node The new instance to use or null
     * @throws InvalidFieldValueException This was not a body node type
     */
    public void setBody1(VRMLNodeType node)
        throws InvalidFieldValueException;

    /**
     * Fetch the reference to the second body that is used in this joint.
     *
     * @return The reference to the node implementing the body
     */
    public VRMLNodeType getBody2();

    /**
     * Set the node that should be used for the second body. Setting a value of
     * null will clear the current body in use.
     *
     * @param node The new instance to use or null
     * @throws InvalidFieldValueException This was not a body node type
     */
    public void setBody2(VRMLNodeType node)
        throws InvalidFieldValueException;
}
