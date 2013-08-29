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
 * Representation of a MFString field.
 * <P>
 * Strings are represented using standard java.lang.String representations.
 * The implementation of this class will provide any necessary conversions
 * to the UTF8 format required for VRML support.
 *
 * @version 1.0 30 April 1998
 */
public interface MFString extends MField {

	/** Places a new value at the end of the existing value, increasing
	 *  the field length accordingly.
	 *  
	 * @param value The value to append
	 */
	public void append(String value);
	
	/**
	 * Removes all values in the field and changes the field size to zero.
	 */
	public void clear();
	
    /**
     * Write the value of the array of the strings to the given array. Individual
     * elements in the string array may be null depending on the implementation
     * of the browser and whether it maintains null references.
     *
     * @param value The string array to be filled in
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(String[] value);

    /**
     * Get a particular string value in the given eventOut array.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated. If the array reference
     * was null when set, an empty string will be returned to the caller.
     *
     * @param index The position to get the string value from
     * @return The string value
     *
     * @exception ArrayIndexOutOfBoundsException The index value was out of bounds of
     *     the current array.
     */
    public String get1Value(int index);

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
    public void insertValue(int index, String value) 
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
     * Set the value of the array of strings.  If value[i] contains a null
     * referenc this will not cause an exception to be generated. However,
     * the resulting event that the eventIn receives will be implementation
     * specific as this is not dealt with in the VRML specification.
     *
     * @param numStrings The number of items to be copied from this array
     * @param value The array of strings.
     */
    public void setValue(int numStrings, String[] value);

    /**
     * Set a particular string value in the given eventIn array. To the VRML
     * world this will generate a full MFString event with the nominated index
     * value changed.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated. If the value reference
     * is null then the result is implementation specific in terms of the array
     * reference that reaches the eventIn. In any case, an event will reach the
     * destination eventIn, but the values in that array are implementation
     * specific. No exception will be generated in this case.
     *
     * @param index The position to set the string value
     * @param value The string value
     *
     * @exception ArrayIndexOutOfBoundsException The index value was out of bounds of
     *     the current array.
     */
    public void set1Value(int index, String value);
}
