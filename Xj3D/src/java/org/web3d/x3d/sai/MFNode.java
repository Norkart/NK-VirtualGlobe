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
 * Representation of a MFNode field.
 * <P>
 * Get the values of a node array. The java <CODE>null</CODE> reference is
 * treated to be equivalent to the VRML <CODE>NULL</CODE> field values.
 * <P>
 * It is not illegal to construct an array where some members of the array are
 * null pointers. Due to no specification on the intended result in the VRML
 * specification, the response given by the browser is implementation
 * dependent. Calls will not generate an exception, but the value of actual
 * event received from the scenegraph may vary until the issue is resolved.
 *
 * @version 1.0 30 April 1998
 */
public interface MFNode extends MField {

	/** Places a new value at the end of the existing value, increasing
	 *  the field length accordingly.
	 *  
	 * @param value The value to append
	 */
	public void append(X3DNode value);
	
	/**
	 * Removes all values in the field and changes the field size to zero.
	 */
	public void clear();
	
    /**
     * Write the value of the array of the nodes to the given array. Individual
     * elements in the array may be null depending on the implementation
     * of the browser and whether it maintains null references.
     *
     * @param nodes The node array to be filled in
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(X3DNode[] nodes);

    /**
     * Get a particular node value in the given eventOut array.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated. If the Node value is
     * NULL because the browser implementation keeps null references then this
     * method will return a null pointer without throwing any exception.
     * <P>
     * @param index The position to read the values from
     * @return The node reference
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current data
     *    array bounds.
     */
    public X3DNode get1Value(int index);

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
    public void insertValue(int index, X3DNode value) 
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
     * Set the value of the array of nodes. Input is an array of valid Node
     * references. If the length is zero or the node reference is null, then
     * the actions to take are according to the class introduction above. If the
     * array contains a null reference then th resulting event passed to the
     * eventIn is implementation dependent
     * <P>
     * If any of the node references have had their dispose methods called, an
     * InvalidNodeException will be generated and no event sent to the
     * scenegraph.
     *
     * @param size The number of nodes to copy from this array
     * @param value The array of node references
     * @exception InvalidNodeException At least one node has been "disposed" of
     */
    public void setValue(int size, X3DNode[] value);

    /**
     * Set a particular node value in the given eventIn array. To the VRML
     * world this will generate a full MFNode event with the nominated index
     * value changed.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated. If the Node value is
     * null the behaviour will be undefined as far as generating an event to the
     * scenegraph is concerned in order to be consistent with the behaviour
     * described in the class introduction. This method call will not generate
     * an exception if the node reference is null.
     * <P>
     * If the node reference passed to this method has already had the dispose
     * method called then an InvalidNodeException will be generated.
     *
     * @param index The position to set the colour value
     * @param value The node reference
     *
     * @exception InvalidNodeException The node has been "disposed" of
     * @exception ArrayIndexOutOfBoundsException The index was out of bounds of the
     *     array currently.
     */
    public void set1Value(int index, X3DNode value);
}
