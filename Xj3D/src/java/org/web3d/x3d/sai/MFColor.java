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
 * Representation of an MFColor field.
 * <P>
 * Colour values are represented as floating point numbers between [0 - 1]
 * as per the X3D IS specification Section 4.4.5 Standard units and
 * coordinate system
 *
 * @version 1.0 30 April 1998
 */
public interface MFColor extends MField {

	/** Places a new value at the end of the existing value, increasing
	 *  the field length accordingly.
	 *  
	 * @param value The value to append
     *    value[0] = Red component [0-1] <BR>
     *    value[1] = Green component [0-1] <BR>
     *    value[2] = Blue component [0-1] <BR>
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
     * @param col The array to be filled in where<BR>
     *    col[i][0] = Red component [0-1] <BR>
     *    col[i][1] = Green component [0-1] <BR>
     *    col[i][2] = Blue component [0-1] <BR>
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[][] col);

    /**
     * Get the values of the event out flattened into a single 1D array. The
     * array must be at least 3 times the size of the array.
     *
     * @param col The array to be filled in where the
     *    col[i + 0] = Red component [0-1] <BR>
     *    col[i + 1] = Green component [0-1] <BR>
     *    col[i + 2] = Blue component [0-1] <BR>
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[] col);

    /**
     * Get the value of a particular vector value in the event out array.
     *
     * @param index The position to get the vectory value from.
     * @param col The array to place the value in where.
     *    col[0] = Red component [0-1] <BR>
     *    col[1] = Green component [0-1] <BR>
     *    col[2] = Blue component [0-1] <BR>
     * @exception ArrayIndexOutOfBoundsException The provided array was too small or
     *     the index was outside the current data array bounds.
     */
    public void get1Value(int index, float[] col);

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
     *    value[0] = Red component [0-1] <BR>
     *    value[1] = Green component [0-1] <BR>
     *    value[2] = Blue component [0-1] <BR>
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
     * Set the value of from the flat array of colours. Input is an array of
     * colour values in RGB order [n, n+1, n+2]. All colour values are required
     * to be in the range 0-1. Colour values outside of this range will generate an
     * IllegalArgumentException. If the array does not contain at
     * least numColors * 3 values it will generate an ArrayIndexOutOfBoundsException.
     *
     * @param numColors The number of colour values in this array to copy
     * @param value The array of colour values where<BR>
     *    value[i] = Red component [0-1] <BR>
     *    value[i+1] = Green component [0-1] <BR>
     *    value[i+2] = Blue component [0-1] <BR>
     *
     * @exception IllegalArgumentException A colour value(s) was out of range
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the colour component
     */
    public void setValue(int numColors, float[] value);

    /**
     * Set the value of the array of colours. Input is an array of colour
     * values in RGB order. All colour values are required to be in the
     * range 0-1. Colour values outside of this range will generate an
     * IllegalArgumentException. If value[i] that does not contain at
     * least three values will generate an ArrayIndexOutOfBoundsException.
     * If value[i] contains more than three items only the first three values
     * will be used and the rest ignored.
     *
     * @param numColors The number of colour values in this array to copy
     * @param value The array of colour values where<BR>
     *    value[i][0] = Red component [0-1] <BR>
     *    value[i][1] = Green component [0-1] <BR>
     *    value[i][2] = Blue component [0-1] <BR>
     *
     * @exception IllegalArgumentException A colour value(s) was out of range
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the colour component
     */
    public void setValue(int numColors, float[][] value);

    /**
     * Set a particular colour value in the given eventIn array. To the VRML
     * world this will generate a full MFColor event with the nominated index
     * value changed. Colour values are required to be in the range [0-1].
     * <P>
     * The value array must contain at least three elements. If the array
     * contains more than 3 values only the first three values will be used and
     * the rest ignored.
     * <P>
     * If the index is out of the bounds of the current array of data values or
     * the array of values does not contain at least 3 elements an
     * ArrayIndexOutOfBoundsException will be generated. If the colour values are
     * out of range an IllegalArgumentException will be generated.
     *
     * @param index The position to set the colour value
     * @param value The array of colour values where<BR>
     *    value[0] = Red component [0-1] <BR>
     *    value[1] = Green component [0-1] <BR>
     *    value[2] = Blue component [0-1] <BR>
     *
     * @exception IllegalArgumentException A colour value(s) was out of range
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least
     *    three values for the colour component
     */
    public void set1Value(int index, float[] value);
}
