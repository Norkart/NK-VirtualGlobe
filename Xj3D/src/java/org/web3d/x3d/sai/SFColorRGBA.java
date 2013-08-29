/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

/**
 * Representation of a SFColorRGBA field.
 * <P>
 * Colour values are represented as floating point numbers between [0 - 1]
 * as per the VRML IS specification Section 4.4.5 Standard units and
 * coordinate system.
 *
 * @version $Revision: 1.3 $
 */
public interface SFColorRGBA extends X3DField {

    /**
     * Write the value of the colour to the given array.
     *
     * @param col The array of colour values to be filled in where<BR>
     *    value[0] = Red component [0-1] <BR>
     *    value[1] = Green component [0-1] <BR>
     *    value[2] = Blue component [0-1] <BR>
     *    value[3] = Alpha component [0-1] <BR>
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[] col);

    /**
     * Set the colour value in the given eventIn.  Colour values are required
     * to be in the range [0-1].
     * <P>
     * The value array must contain at least three elements. If the array
     * contains more than 4 values only the first three values will be used and
     * the rest ignored.
     * <P>
     * If the array of values does not contain at least 3 elements an
     * ArrayIndexOutOfBoundsException will be generated. If the colour values are
     * out of range an IllegalArgumentException will be generated.
     *
     * @param value The array of colour values where<BR>
     *    value[0] = Red component [0-1] <BR>
     *    value[1] = Green component [0-1] <BR>
     *    value[2] = Blue component [0-1] <BR>
     *    value[3] = Alpha component [0-1] <BR>
     *
     * @exception IllegalArgumentException A colour value(s) was out of range
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the colour component
     */
    public void setValue(float[] value);
}
