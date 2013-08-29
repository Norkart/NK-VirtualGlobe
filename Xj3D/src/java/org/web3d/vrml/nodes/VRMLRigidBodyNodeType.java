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
import org.odejava.Body;
import org.odejava.World;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;


/**
 * Representation of single RigidBody object in the rigid body physics model.
 * <p>
 *
 * See the specification definition at
 * http://www.xj3d.org/extensions/rigid_physics.html
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface VRMLRigidBodyNodeType extends VRMLNodeType {

    /**
     * Set the parent world that this body belongs to. A null value clears
     * the world and indicates the physics model or body is no longer in use
     * by this world (eg deletes it).
     *
     * @param wld The new world instance to use or null
     */
    public void setODEWorld(World wld);

    /**
     * Get the ODE object that represents the body to evaluate.
     *
     * @return The body object representing this node
     */
    public Body getODEBody();

    /**
     * Update any pre-evaluation values for the body. For example, forces
     * applied to the body need to be reset every frame as ODE will zero out
     * force and torque values after every frame.
     */
    public void updateODEFromNode();

    /**
     * Update the local fields after the physics model has been evaluated. For
     * example the position and orientation are most likely to have changed, so
     * these should be read back from ODE and updated in the local fields.
     */
    public void updateNodeFromODE();

    /**
     * Get the number of valid geometry that this body has as sub objects.
     *
     * @return A number greater than or equal to zero
     */
    public int numGeometry();

    /**
     * Get the geometry list, provides a live reference not a copy. The number of
     * valid values is available from numGeometry();
     *
     * @return An array of VRMLNodeTypes
     */
    public VRMLNodeType[] getGeometry();

    /**
     * Set the collection of geometry nodes that this body should use to render
     * the main scene transformation. If passed a zero for numValid this method
     * will remove all current values.
     * <p>
     *
     * Geometry is allowed to be one of the grouping nodes or a shape node
     * type. However, if you provide something like a LOD or Switch, don't
     * expect anything to actually work correctly.
     *
     * @param geometry Array of new geometry node instances to use
     * @param numValid The number of valid values to get from the array
     * @throws InvalidFieldValueException one of the provided nodes is not a
     *   X3DRigidBodyNode instance
     */
    public void setGeometry(VRMLNodeType[] geometry, int numValid);

    /**
     * Fetch the reference to the node that represents the mass density model.
     *
     * @return The reference to the node defining the mass model
     */
    public VRMLNodeType getMassDensityModel();

    /**
     * Set the node that should be used to represent the mass density model.
     * Setting a value of null will clear the current model in use and return
     * the system to a spherical model.
     * <p>
     *
     * The valid body nodes are Sphere, Box and Cone.
     *
     * @param node The new instance to use or null
     * @throws InvalidFieldValueException This was not a valid node type
     */
    public void setMassDensityModel(VRMLNodeType node)
        throws InvalidFieldValueException;
}
