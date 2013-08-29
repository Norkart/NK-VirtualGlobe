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
import org.odejava.PlaceableGeom;


// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;


/**
 * Representation of X3DCollidableNode in terms of the rigid body physics
 * component.
 * <p>
 *
 * See the specification definition at
 * http://www.xj3d.org/extensions/rigid_physics.html
 * <p>
 *
 * This class is named NBodyCollidable because there is already the generic
 * VRMLCollidableNodeType defined in Xj3D for representing user-collidable
 * geometry with the avatar.
 * <p>
 *
 * The X3D node definition is:
 * <pre>
 * X3DNBodyCollidableNode : X3DChildNode, X3DBoundedObject {
 *   SFNode     [in,out] metadata       NULL      [X3DMetadataObject]
 *   SFBool     [in,out] enabled     TRUE
 *   SFVec3f    [in,out] position    0 0 0    (-&#8734;,&#8734;)
 *   SFRotation [in,out] rotation    0 0 1 0  [0,1]
 *   SFVec3f    []       bboxCenter     0 0 0     (-&#8734;,&#8734;)
 *   SFVec3f    []       bboxSize       -1 -1 -1  [0,&#8734;) or -1 -1 -1
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface VRMLNBodyCollidableNodeType
    extends VRMLChildNodeType, VRMLBoundedNodeType {

    /**
     * Get the ODE object that represents the body to evaluate.
     *
     * @return The body object representing this node
     */
    public PlaceableGeom getODEGeometry();

    /**
     * ODE computation has finished, so go update the field values and the
     * rendering API structures with the final computed values.
     */
    public void updateFromODE();

    /**
     * Is this group enabled for use right now?
     *
     * @return true if this is enabled
     */
    public boolean isEnabled();

    /**
     * Set a new state for the enabled field
     *
     * @param state The new enabled value
     */
    public void setEnabled(boolean state);

    /**
     * Set the rotation component of the of transform. Setting a value
     * of null is an error
     *
     * @param rot The new rotation component
     * @throws InvalidFieldValueException The rotation was null
     */
    public void setRotation(float[] rot)
        throws InvalidFieldValueException;

    /**
     * Get the current rotation component of the transform.
     *
     * @return The current rotation
     */
    public float[] getRotation();

    /**
     * Set the translation component of the of transform. Setting a value
     * of null is an error
     *
     * @param tx The new translation component
     * @throws InvalidFieldValueException The translation was null
     */
    public void setTranslation(float[] tx)
        throws InvalidFieldValueException;

    /**
     * Get the current translation component of the transform.
     *
     * @return The current translation
     */
    public float[] getTranslation();
}
