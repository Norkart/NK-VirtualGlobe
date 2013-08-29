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
 * Representation of a MFRotation field.
 * <P>
 * Rotation values are specified according to the VRML IS Specification
 *  Section 5.8 SFRotation and MFRotation.
 *
 * @version 1.0 30 April 1998
 */
public interface MFRotation extends MField {

	/** Places a new value at the end of the existing value, increasing
	 *  the field length accordingly.
	 *  
	 * @param value The value to append
     *    value[0] = X component [0-1] <BR>
     *    value[1] = Y component [0-1] <BR>
     *    value[2] = Z component [0-1] <BR>
     *    value[3] = Angle of rotation [-PI - PI] (nominally).
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
	 */
	public void append(float value[]);
	
	/**
	 * Removes all values in the field and changes the field size to zero.
	 */
	public void clear();
	
    /**
     * Write the value of the event out to the given array.
     *
     * @param vec The array to be filled in where<BR>
     *    value[i][0] = X component [0-1] <BR>
     *    value[i][1] = Y component [0-1] <BR>
     *    value[i][2] = Z component [0-1] <BR>
     *    value[i][3] = Angle of rotation [-PI - PI] (nominally).
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[][] vec);

    /**
     * Get the values of the event out flattened into a single 1D array. The
     * array must be at least 4 times the size of the array.
     *
     * @param vec The array to be filled in where the
     *    value[i + 0] = X component [0-1] <BR>
     *    value[i + 1] = Y component [0-1] <BR>
     *    value[i + 2] = Z component [0-1] <BR>
     *    value[i + 3] = Angle of rotation [-PI - PI] (nominally).
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[] vec);

    /**
     * Get the value of a particular rotation value in the event out array.
     *
     * @param index The position to get the vectory value from.
     * @param vec The array to place the value in where.
     *    vec[0] = X component [0-1] <BR>
     *    vec[1] = Y component [0-1] <BR>
     *    vec[2] = Z component [0-1] <BR>
     *    vec[3] = Angle of rotation [-PI - PI] (nominally).
     * @exception ArrayIndexOutOfBoundsException The provided array was too small or
     *     the index was outside the current data array bounds.
     */
    public void get1Value(int index, float[] vec);

    /**
     * Inserts a value into an existing index of the field.  Current field values
     * from the index to the end of the field are shifted down and the field
     * length is increased by one to accomodate the new element.
     * 
     * If the index is out of the bounds of the current field an
     * ArrayIndexOutofBoundsException will be generated.
     * 
     * @param index The position at which to insert
     * @param value The new element to insert
     *    value[0] = X component [0-1] <BR>
     *    value[1] = Y component [0-1] <BR>
     *    value[2] = Z component [0-1] <BR>
     *    value[3] = Angle of rotation [-PI - PI] (nominally).
     * 
     * @exception ArrayIndexOutOfBoundsException The index was outside the current field
     *    size.
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void insertValue(int index, float value[]) 
        throws ArrayIndexOutOfBoundsException;
    
    /**
     * Removes one value from the field.  Values at indices above the
     * removed element will be shifted down by one and the size of the
     * field will be reduced by one.
     * 
     * @param index The position of the value to remove.
     * @exception ArrayIndexOutOfBoundsException The index was outside the current field
     *    size.
     */
    public void removeValue(int index)
        throws ArrayIndexOutOfBoundsException;
    
    /**
     * Set the value of the array of rotations. Input is an array of floats
     * values in order required to specify an SFRotation. If value[i] does not
     * contain at least four values an ArrayIndexOutOfBoundsException will be
     * generated. If value[i] contains more than four items only the first
     * four values will be used and the rest ignored.
     *
     * @param numRotations The number of rotations to copy from this array
     * @param value The array of rotation values where<BR>
     *    value[i] = X component [0-1] <BR>
     *    value[i+1] = Y component [0-1] <BR>
     *    value[i+2] = Z component [0-1] <BR>
     *    value[i+3] = Angle of rotation [-PI - PI] (nominally).
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least four
     *    values for the rotation
     */
    public void setValue(int numRotations, float[] value);

    /**
     * Set the value of the array of rotations. Input is an array of floats
     * values in order required to specify an SFRotation. If value[i] does not
     * contain at least four values an ArrayIndexOutOfBoundsException will be
     * generated. If value[i] contains more than four items only the first
     * four values will be used and the rest ignored.
     *
     * @param numRotations The number of rotations to copy from this array
     * @param value The array of rotation values where<BR>
     *    value[i][0] = X component [0-1] <BR>
     *    value[i][1] = Y component [0-1] <BR>
     *    value[i][2] = Z component [0-1] <BR>
     *    value[i][3] = Angle of rotation [-PI - PI] (nominally).
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least four
     *    values for the rotation
     */
    public void setValue(int numRotations, float[][] value);

    /**
     * Set a particular rotation in the given eventIn array. To the VRML
     * world this will generate a full MFRotation event with the nominated index
     * value changed.
     * <P>
     * The value array must contain at least four elements. If the array
     * contains more than 4 values only the first four values will be used and
     * the rest ignored.
     * <P>
     * If the index is out of the bounds of the current array of data values or
     * the array of values does not contain at least 4 elements an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the rotation value
     * @param value The array of rotation values where<BR>
     *    value[0] = X component [0-1] <BR>
     *    value[1] = Y component [0-1] <BR>
     *    value[2] = Z component [0-1] <BR>
     *    value[3] = Angle of rotation [-PI - PI] (nominally).
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least four
     *    values for the rotation
     */
    public void set1Value(int index, float[] value);
}
