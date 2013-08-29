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
 * Representation of a SFRotation field.
 * <P>
 * Rotation values are specified according to the VRML IS Specification
 * Section 5.8 SFRotation and MFRotation.
 *
 * @version 1.0 30 April 1998
 */
public interface SFRotation extends X3DField {

    /**
     * Write the rotation value to the given eventOut
     *
     * @param vec The array of vector values to be filled in where<BR>
     *    value[0] = X component [0-1] <BR>
     *    value[1] = Y component [0-1] <BR>
     *    value[2] = Z component [0-1] <BR>
     *    value[3] = Angle of rotation [-PI - PI] (nominally).
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[] vec);

    /**
     * Set the rotation value in the given eventIn.
     * <P>
     * The value array must contain at least four elements. If the array
     * contains more than 4 values only the first 4 values will be used and
     * the rest ignored.
     * <P>
     * If the array of values does not contain at least 4 elements an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param value The array of rotation values where<BR>
     *    value[0] = X component [0-1] <BR>
     *    value[1] = Y component [0-1] <BR>
     *    value[2] = Z component [0-1] <BR>
     *    value[3] = Angle of rotation [-PI - PI] (nominally).
     *
     * @exception ArrayIndexOutOfBoundsException The value did not contain at least 4
     *    values for the rotation.
     */
    public void setValue(float[] value);
}
