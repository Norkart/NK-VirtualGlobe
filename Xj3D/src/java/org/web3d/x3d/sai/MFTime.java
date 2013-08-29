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
 * Representations of a MFTime field.
 * <P>
 * Time values are represented as per the VRML IS specification Section
 * 4.11 Time. That is, time is set as VRML "Time" - the number of seconds since
 * Jan 1, 1970 GMT, rather than a Java time which is a long, the number
 * of milliseconds since Jan 1, 1970 GMT. To convert between the two simply
 * divide java time by 1000 and cast to a double.
 * <P>
 * Note that in setting time values from an external application, the idea of
 * the time that java represents and the time that the VRML world currently
 * has set may well be different. It is best to source the current "time" from
 * a node or eventOut in the VRML world rather than relying exclusively on
 * the value returned from <CODE>System.currentTimeMillies</CODE>. This is
 * especially important to note if you are dealing with high speed, narrow
 * interval work such as controlling animation.
 *
 * @version 1.0 30 April 1998
 */
public interface MFTime extends MField {

	/** Places a new value at the end of the existing value, increasing
	 *  the field length accordingly.
	 *  
	 * @param value The value to append
	 */
	public void append(double value);
	
	/**
	 * Removes all values in the field and changes the field size to zero.
	 */
	public void clear();
	
    /**
     * Write the value of the event out to the given array.
     *
     * @param vec The array to be filled in where
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(double[] vec);

    /**
     * Get a particular time value in the given eventOut array.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to get the time value
     * @return The time value.
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside of the bounds of
     * the current array.
     */
    public double get1Value(int index);

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
    public void insertValue(int index, double value) 
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
     * Set the value of the array of times. Time values are not required to
     * conform to any range checks.
     *
     * @param size The number of items to be copied from the array
     * @param value The array of time values
     */
    public void setValue(int size, double[] value);

    /**
     * Set the value of the array of times based on Java time values. Time values
     * are not required to conform to any range checks.
     *
     * @param size The number of items to be copied from the array
     * @param value The array of time values
     */
    public void setValue(int size, long[] value);

    /**
     * Set a particular time value in the given eventIn array. To the VRML
     * world this will generate a full MFTime event with the nominated index
     * value changed.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the time value
     * @param value The time value to set.
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside of the bounds of
     * the current array.
     */
    public void set1Value(int index, double value);
}
