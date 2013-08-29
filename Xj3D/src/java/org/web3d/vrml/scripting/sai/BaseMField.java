/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.sai;

// External imports
// None

// Local imports
import org.web3d.x3d.sai.MField;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * X3D base interface for MF field values.
 * <P>
 * Class provides a size method that determines the number of items available
 * in this array of values. Normally used in conjunction with the get1Value()
 * method of the MF field classes so that exceptions are not generated.
 * <P>
 * It is possible, although not recommended, that the size of the arrays
 * returned by the get methods may be larger than the actual amount of data
 * that is to be represented. Calling size() beforehand ensures that the
 * correct number of items in the array will be read.
 *
 * @version 1.0 30 April 1998
 */
abstract class BaseMField extends BaseField implements MField {

    /** Error message for when insertValue has an index out of bounds */
    protected static final String INSERT_OOB_ERR =
        "Attempting to insert a value past the end of the existing " +
        "number of elements.";

    /** Error message for when removeValue has an index out of bounds */
    protected static final String REMOVE_OOB_ERR =
        "Attempting to remove a value past the end of the existing " +
        "number of elements.";

    /** Error message for when setValue has an index out of bounds */
    protected static final String SET_OOB_ERR =
        "Attempting to set a value past the end of the existing " +
        "number of elements.";

    /** Error message for when getValue has an index out of bounds */
    protected static final String GET_OOB_ERR =
        "Attempting to fetch a value past the end of the existing " +
        "number of elements.";

    /** Error message when the index provided is negative */
    protected static final String NEGATIVE_INDEX_ERR =
        "The provided index is negative.";

    /** The number of elements of this field value type */
    protected int numElements;

    /**
     * Pass-through constructor to create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    BaseMField(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    /**
     * Removes all values in the field and changes the field size to zero.
     */
    public void clear() {
        checkAccess(true);

        // Clear by just setting the number of elements to zero. No need to
        // play with the array size itself. Maybe the user is about to come
        // back and reset the entire array anyway.
        numElements = 0;
        dataChanged = true;
    }

    /**
     * Get the size of the underlying data array. The size is the number of
     * elements for that data type. So for an MFFloat the size would be the
     * number of float values, but for an MFVec3f, it is the number of vectors
     * in the returned array (where a vector is 3 consecutive array indexes in
     * a flat array).
     *
     * @return The number of elements in this field
     */
    public int getSize() {
        return numElements;
    }
}
