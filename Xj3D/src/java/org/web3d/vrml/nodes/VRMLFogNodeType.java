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

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * Representation of any type of fog in the system
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public interface VRMLFogNodeType extends
    VRMLChildNodeType {

    /** Constant used to set the fog type to disabled. */
    public static final int FOG_TYPE_DISABLE = 0;

    /** Constant used to set the fog type to linear. */
    public static final int FOG_TYPE_LINEAR = 1;

    /** Constant used to set the fog type to exponential.  */
    public static final int FOG_TYPE_EXPONENTIAL = 2;

    /**
     * Get the color of the current fog.
     *
     * @param color An array to copy the current color to
     */
    public void getColor(float[] color);

    /**
     * Set the color of the current fog. If the color values are out of range
     * or the array is invalid, an exception will be generated.
     *
     * @param color The new colors to set
     * @throws InvalidFieldValueException The colour values were out of range.
     */
    public void setColor(float[] color) throws InvalidFieldValueException;

    /**
     * Get the current visibility limit on the fog to be viewed. A value of
     * zero would disable the fog. It will always be a positive number.
     *
     * @return A non-negative number indicating the distance
     */
    public float getVisibilityRange();

    /**
     * Set the visibility limit on the fog to be viewed to a new value. The
     * value of zero will disable the fog. A negative number will generate an
     * exception.
     *
     * @param range A non-negative number indicating the distance
     * @throws InvalidFieldValueException The number was negative
     */
    public void setVisibilityRange(float range)
        throws InvalidFieldValueException;

    /**
     * Get the currently set fog type. Will be one of the above
     * constant values.
     *
     * @return One of FOG_TYPE_LINEAR or FOG_TYPE_EXPONENTIAL
     */
    public int getFogType();

    /**
     * Set the fog type to one of the new values. If the value is not known,
     * issue an exception and leave the value at the current.
     *
     * @param type Constant indicating the type. Should be one of
     *   FOG_TYPE_LINEAR or FOG_TYPE_EXPONENTIAL
     * @throws InvalidFieldValueException The value type is unknown
     */
    public void setFogType(int type) throws InvalidFieldValueException;
}
