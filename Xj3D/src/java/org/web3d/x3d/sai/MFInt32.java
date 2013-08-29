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
 * Representation of a MFInt32 field.
 *
 * @version 1.0 30 April 1998
 */
public interface MFInt32 extends MField {

	/** Places a new value at the end of the existing value, increasing
	 *  the field length accordingly.
	 *  
	 * @param value The value to append
	 */
	public void append(int value);
	
	/**
	 * Removes all values in the field and changes the field size to zero.
	 */
	public void clear();
	
    /**
     * Write the value of the array of the ints to the given array.
     *
     * @param values The array to be filled in
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(int[] values);

    /**
     * Get a particular value from the eventOut array.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to be retrieved
     * @return The value at that position
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current data
     *    array bounds.
     */
    public int get1Value(int index)
        throws ArrayIndexOutOfBoundsException;

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
     * 
     * @exception ArrayIndexOutOfBoundsException The index was outside the current field
     *    size.
     */
    public void insertValue(int index, int value) 
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
     * Set the value of the array of integers. If the value array is length zero
     * this is equivalent of clearing the field.
     *
     * @param size The number of items to copy from this array
     * @param value The array of values to be set
     */
    public void setValue(int size, int[] value);

    /**
     * Set a particular value in the given eventIn array. To the VRML
     * world this will generate a full MFInt32 event with the nominated index
     * value changed.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the colour value
     * @param value The value to be set
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the colour component
     */
    public void set1Value(int index, int value)
        throws ArrayIndexOutOfBoundsException;
}
