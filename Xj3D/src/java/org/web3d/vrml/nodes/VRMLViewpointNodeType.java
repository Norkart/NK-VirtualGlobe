/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
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

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * Base representation of a viewpoint node in the scene.
 * <p>
 *
 * A viewpoint provides a place to examine the contents of the virtual world.
 * Depending on the world type, the way of specifying a viewpoint's location
 * and orientation may change. This base interface describes all of the common
 * requirements for a viewpoint.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.10 $
 */
public interface VRMLViewpointNodeType extends VRMLBindableNodeType {
    public static final int PROJECTION_PERSPECTIVE = 0;
    public static final int PROJECTION_ORTHO = 1;

    /**
     * Add a ViewpointListener.
     *
     * @param l The listener.  Duplicates and nulls are ignored.
     */
    public void addViewpointListener(ViewpointListener l);

    /**
     * Remove a ViewpointListener.
     *
     * @param l The listener
     */
    public void removeViewpointListener(ViewpointListener l);

    /**
     * Get the projection type.
     *
     * @return The type of projection.  One of the PROJECTION_ constants.
     */
    public int getProjectionType();

    /**
     * Get the field of view used by this viewpoint. The value returned
     * is an angle that is not less than zero and less than or equal to PI.
     * The number of items in the list is dependant on the Viewpoint type.
     *
     * @return The field of view used by this viewpoint
     */
    public float[] getFieldOfView();

    /**
     * Get the center of rotation defined by this viewpoint. The center of
     * rotation is a point in space relative to the coordinate systems of
     * this node.
     *
     * @return The position of the center of rotation
     */
    public float[] getCenterOfRotation();

    /**
     * Set the center of rotation of this viewpoint. The center is a position
     * in 3-space.
     *
     * @param pos The new position to use
     * @throws InvalidFieldValueException The field used is not 3 values
     */
    public void setCenterOfRotation(float[] pos)
        throws InvalidFieldValueException;

    /**
     * Get the Jump field value of this viewpoint.
     *
     * @return true if this viewpoint should jump to new positions
     */
    public boolean getJump();

    /**
     * Set the jump field value of this viewpoint to the new value
     *
     * @param jump True if the viewpoint should jump to ne positions
     */
    public void setJump(boolean jump);

    /**
     * Get the retainUserOffsets field value of this viewpoint.
     *
     * @return true if this viewpoint should retainUserOffsets on a bind
     */
    public boolean getRetainUserOffsets();

    /**
     * Set the retainUserOffsets field value of this viewpoint to the new value
     *
     * @param retainUserOffsets True if the viewpoint should retainUserOffsets on a bind
     */
    public void setRetainUserOffsets(boolean retainUserOffsets);

    /**
     * Get the description string associated with this viewpoint. If no
     * description is set, this will return null.
     *
     * @return The description string of this viewpoint
     */
    public String getDescription();

    /**
     * Set the description string of this viewpoint. A zero length string or
     * null will clear the currently set description.
     *
     * @param desc The new description to use
     */
    public void setDescription(String desc);
}
