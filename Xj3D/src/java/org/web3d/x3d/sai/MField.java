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
public interface MField extends X3DField {

    /**
     * Get the size of the underlying data array. The size is the number of
     * elements for that data type. So for an MFFloat the size would be the
     * number of float values, but for an MFVec3f, it is the number of vectors
     * in the returned array (where a vector is 3 consecutive array indexes in
     * a flat array).
     *
     * @return The number of elements in this field
     */
    public int getSize();
}
