/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.util;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.lang.VRMLNodeTemplate;

/**
 * Simple dynamic array structure that holds VRMLNodeTemplate instances.
 * <p>
 *
 * Idea is to save implementation weight when we don't really want to use a
 * full java.util collections class, but don't want to have to re-implement
 * the node copy/paste stuff every time.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NodeTemplateArray {

    /** The default size of this array */
    private static final int DEFAULT_SIZE = 512;

    /** The increment size of the array */
    private static final int INCREMENT_SIZE = 256;

    /** The number of items in this array currently */
    private int valueCount;

    /** The contents of this array */
    private VRMLNodeTemplate[] array;

    /**
     * Create a new default array with size 512 items
     */
    public NodeTemplateArray() {
        this(DEFAULT_SIZE);
    }

    /**
     * Create an array with the given initial size
     *
     * @param initialSize The size to start with
     */
    public NodeTemplateArray(int initialSize) {
        array = new VRMLNodeTemplate[initialSize];
        valueCount = 0;
    }

    /**
     * Get the count of the number of items in the array.
     *
     * @return The number of items in the array
     */
    public int size() {
        return valueCount;
    }

    /**
     * Clear the array so that it contains no values
     */
    public void clear() {
        for(int i = 0; i < valueCount; i++) {
            array[i] = null;
        }

        valueCount = 0;
    }

    /**
     * Add a new value to the array. Will resize the array if needed to
     * accommodate new values.
     *
     * @param newNode the value to be added
     */
    public void add(VRMLNodeTemplate newNode) {

        if(valueCount == array.length) {
            VRMLNodeTemplate[] newArray =
                new VRMLNodeTemplate[array.length + INCREMENT_SIZE];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }

        array[valueCount++] = newNode;
    }

    /**
     * Add an array of values in bulk to the array. The array should not
     * be null.
     *
     * @param values The values to be added
     */
    public void add(VRMLNodeTemplate[] values) {
        if(values == null)
            return;

        int req_size = valueCount + values.length;

        if(req_size >= array.length) {
            VRMLNodeTemplate[] newArray = new VRMLNodeTemplate[req_size];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }

        System.arraycopy(values, 0, array, valueCount, values.length);
        valueCount = req_size;
    }

    /**
     * Add the contents of the given array to this array.
     *
     * @param nodeList The values to be added
     */
    public void add(NodeTemplateArray nodeList) {
        if(nodeList == null)
            return;

        int req_size = valueCount + nodeList.valueCount;

        if(req_size >= array.length) {
            VRMLNodeTemplate[] newArray = new VRMLNodeTemplate[req_size];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }

        System.arraycopy(nodeList.array,
                         0,
                         array,
                         valueCount,
                         nodeList.valueCount);
        valueCount = req_size;
    }

    /**
     * Add an subset of the array of values in bulk to the array. The array
     * should not * be null.
     *
     * @param values The values to be added
     * @param offset The offset into the array to copy over
     * @param len The number of items to copy
     */
    public void add(VRMLNodeTemplate[] values, int offset, int len) {
        int req_size = valueCount + len;

        if(req_size >= array.length) {
            VRMLNodeTemplate[] newArray = new VRMLNodeTemplate[req_size];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }

        System.arraycopy(values, offset, array, valueCount, len);
        valueCount = req_size;
    }

    /**
     * Get the value at the given index.
     *
     * @param index The position to get values from
     * @return The value at that index
     * @throws IndexOutOfBoundsException The index was not legal
     */
    public VRMLNodeTemplate get(int index) {
        if((index < 0) || (index >= valueCount))
            throw new IndexOutOfBoundsException();

        return array[index];
    }

    /**
     * Set the value at the given index. If the index is out of the range
     * of the current items, it will generate an index exception.
     *
     * @param index The position to get values from
     * @param value The new value to set
     * @throws IndexOutOfBoundsException The index was not legal
     */
    public void set(int index, VRMLNodeTemplate value) {
        if((index < 0) || (index >= valueCount))
            throw new IndexOutOfBoundsException();

        array[index] = value;
    }

    /**
     * Replace the contents of this list to the values in the given list.
     * All current values will be removed.
     *
     * @param list The list to copy values from
     */
    public void set(NodeTemplateArray list) {
        int req_size = list.valueCount;

        if(req_size >= array.length) {
            array = new VRMLNodeTemplate[req_size];
        } else if(req_size < valueCount) {
            // clear the entire array only if the new size is smaller than
            // the old lot
            clear();
        }

        System.arraycopy(list.array, 0, array, 0, list.valueCount);
        valueCount = list.valueCount;
    }

    /**
     * Remove the value at the given index.
     *
     * @param index The position to remove the value from
     * @return The value at that index
     * @throws IndexOutOfBoundsException The index was not legal
     */
    public VRMLNodeTemplate remove(int index) {
        if((index < 0) || (index >= valueCount))
            throw new IndexOutOfBoundsException();

        VRMLNodeTemplate ret_val = array[index];

        System.arraycopy(array, index + 1, array, index, array.length - index - 1);
        valueCount--;

        return ret_val;
    }

    /**
     * Remove the given value from the array. Uses a very inefficient linear
     * search to locate the object. If the value is not in the array, silently
     * exists with a null return value. If the same object is in the array
     * multiple times, only the first instance is removed.
     *
     * @param object The value to remove
     * @return The value at that index
     * @throws IndexOutOfBoundsException The index was not legal
     */
    public VRMLNodeTemplate remove(VRMLNodeTemplate object) {
        if(object == null)
            return null;

        int found_index = -1;

        for(int i = 0; i < valueCount; i++) {
            if(object.equals(array[i])) {
                found_index = -1;
                break;
            }
        }

        VRMLNodeTemplate ret_val = null;

        if(found_index != -1)
            ret_val = remove(found_index);

        return ret_val;
    }

    /**
     * Convenience method to remove the array of values given from this array.
     * Any item in the provided array and not in this array is ignored.
     *
     * @param nodeList The list of objects to remove
     */
    public void remove(NodeTemplateArray nodeList) {
        if(nodeList == null)
            return;

        for(int i = 0; i < nodeList.valueCount; i++)
            remove(nodeList.array[i]);
    }

    /**
     * Turn the values of this array into a real array. Returns an array with
     * the exact number of items in it. This is a separate copy of the internal
     * array.
     *
     * @return The array of values
     */
    public VRMLNodeTemplate[] toArray() {
        VRMLNodeTemplate[] ret_val = new VRMLNodeTemplate[valueCount];

        System.arraycopy(array, 0, ret_val, 0, valueCount);

        return ret_val;
    }

    /**
     * Turn the values of this array into a real array by copying them into
     * the given array if possible. If the array is big enough then it will
     * copy the values straight in. If not, it will ignore that array and
     * create it's own copy and return that. If the passed array is used, the
     * return value will be a reference to the passed array, otherwise it will
     * be the new copy.
     *
     * @param values The array to copy values to
     * @return The array of values
     */
    public VRMLNodeTemplate[] toArray(VRMLNodeTemplate[] values) {

        VRMLNodeTemplate[] ret_val = null;

        if(values.length >= valueCount)
            ret_val = values;
        else
            ret_val = new VRMLNodeTemplate[valueCount];

        System.arraycopy(array, 0, ret_val, 0, valueCount);

        return ret_val;
    }
}
