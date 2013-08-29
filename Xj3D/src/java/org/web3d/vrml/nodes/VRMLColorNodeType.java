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

/**
 * A node which specifies color information.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public interface VRMLColorNodeType extends VRMLGeometricPropertyNodeType {

    /**
     * Get the number of components defined for this texture type. SHould
     * be one of 1, 2, 3 or 4.
     *
     * @return one of 1, 2, 3 or 4
     */
    public int getNumColorComponents();

    /**
     * Accessor method to set a new value for field attribute color.
     *
     * @param newColor An array of 3 floats(r,g,b) specifying the new color
     * @param numValid The number of valid values to copy from the array
     * @throws ArrayIndexOutofBoundsException
     */
    public void setColor(float[] newColor, int numValid);

    /**
     * Get the number of items in the color array now. The number returned is
     * the total number of values in the flat array. This will allow the caller
     * to construct the correct size array for the getColor() call.
     *
     * @return The number of values in the array
     */
    public int getNumColors();

    /**
     * Get current value of field color. Color is an array of Color or ColorRGBA
     * floats. Don't call if there are no colors in the array.
     *
     * @param colors The array to copy the color values into
     */
    public void getColor(float[] colors);
}
