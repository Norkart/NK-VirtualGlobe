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
import javax.vecmath.Vector3f;

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * Base representation of a node that provides navigation hints in the scene.
 * <p>
 *
 * A viewpoint provides a place to examine the contents of the virtual world.
 * Depending on the world type, the way of specifying a viewpoint's location
 * and orientation may change. This base interface describes all of the common
 * requirements for a viewpoint.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.11 $
 */
public interface VRMLNavigationInfoNodeType extends VRMLBindableNodeType {

    /** The navigation type is ANY */
    public String NAV_TYPE_ANY = "ANY";

    /** The navigation type is WALK */
    public String NAV_TYPE_WALK = "WALK";

    /** The navigation type is EXAMINE */
    public String NAV_TYPE_EXAMINE = "EXAMINE";

    /** The navigation type is LOOKAT */
    public String NAV_TYPE_LOOKAT = "LOOKAT";

    /** The navigation type is FLY */
    public String NAV_TYPE_FLY = "FLY";

    /** The navigation type is NONE */
    public String NAV_TYPE_NONE = "NONE";

    /** The transition type LINEAR */
    public String TRANSITION_TYPE_LINEAR = "LINEAR";

    /** The transition type TELEPORT */
    public String TRANSITION_TYPE_TELEPORT = "TELEPORT";

    /** The transition type ANIMATE */
    public String TRANSITION_TYPE_ANIMATE = "ANIMATE";

    /**
     * Get the list of navigation types that are to be used. It may contain
     * some or all of the constants above. The array will always be non-null.
     *
     * @return The list of navigation types set
     */
    public String[] getType();

    /**
     * Get the number of valid navigation types in the result from
     * getType().
     *
     * @return The number of elements in getType().
     */
    public int getNumTypes();


    /**
     * Set the navigation type to the new value(s). The array must be non-null.
     * If the underlying implementation does not support any of the types
     * requested, it shall default to the type NONE.
     *
     * @param types The list of types to now use in order of preference
     * @param numValid number of valid items to use from the size array
     * @throws InvalidFieldValueException The list was null or empty
     */
    public void setType(String[] types, int numValid)
        throws InvalidFieldValueException;

    /**
     * Get the dimensions of the avatar in use.
     *
     * @return A list of floats describing the dimension of the avatar.
     */
    public float[] getAvatarSize();

    /**
     * Set the dimensions of the avatar in use. The array must have at least
     * three values in it as required by the specification.
     *
     * @param size The new size values to use
     * @param numValid number of valid items to use from the size array
     * @throws InvalidFieldValueException The array did not contain 3 values
     */
    public void setAvatarSize(float[] size, int numValid)
        throws InvalidFieldValueException;

    /**
     * Get the speed that we are currently moving at.
     *
     * @return The current movement speed.
     */
    public float getSpeed();

    /**
     * Set the speed to move at. The speed value must be non-negative.
     *
     * @param newSpeed The new speed value to use
     * @throws InvalidFieldValueException The speed was negative
     */
    public void setSpeed(float newSpeed) throws InvalidFieldValueException;

    /**
     * Get the visibility limit that we are currently operating at.
     *
     * @return The current movement visibility limit.
     */
    public float getVisibilityLimit();

    /**
     * Set the visibility limie to move at. The visibility limit value must be
     * non-negative.
     *
     * @param limit The new visibility limit value to use
     * @throws InvalidFieldValueException The visibility limit was negative
     */
    public void setVisibilityLimit(float limit) throws InvalidFieldValueException;

    /**
     * Get the status of the headlight that we are operating with. A true
     * value represents the headlight being on.
     *
     * @return true if the headlight is to be used
     */
    public boolean getHeadlight();

    /**
     * Set the statte of the headlight to the new value.
     *
     * @param enable True if we are to use the headlight
     */
    public void setHeadlight(boolean enable);

    /**
     * Add a listener for navigation info changes. Duplicate adds are
     * ignored.
     *
     * @param l The new navigation info change listener
     */
    public void addNavigationChangedListener(NavigationInfoChangeListener l);

    /**
     * Remove the listener for navigation info changes. If not already added,
     * this request is ignored.
     *
     * @param l The new navigation info change listener
     */
    public void removeNavigationChangedListener(NavigationInfoChangeListener l);

    /**
     * Sets the current position in world coordinates.
     *
     * @param wcpos Location of the user in world coordinates(x,y,z)
     */
    public void setWorldLocation(Vector3f wcpos);
}
