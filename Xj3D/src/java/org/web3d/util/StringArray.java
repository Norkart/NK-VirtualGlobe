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

package org.web3d.util;

// External import
// None

// Local import
// None

/**
 * Simple dynamic array structure that holds String primitives.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class StringArray {

    /** The default size of this array */
    private static final int DEFAULT_SIZE = 512;

    /** The increment size of the array */
    private static final int DEFAULT_INCREMENT = 256;

    /** The number of items in this array currently */
    private int valueCount;

    /** The contents of this array */
    private String[] array;

    /** Increment size to use */
    private final int incrementSize;

    /**
     * Create a new default array with size 512 items and increment size of
     * 256.
     */
    public StringArray() {
        this(DEFAULT_SIZE, DEFAULT_INCREMENT);
    }

    /**
     * Create an array with the given initial size.
     *
     * @param initialSize The size to start with
     */
    public StringArray(int initialSize) {
        this(initialSize, DEFAULT_INCREMENT);
    }

    /**
     * Create an array with the given initial size and resize strategy.
     *
     * @param initialSize The size to start with
     * @param inc The amount to increment each resize with
     */
    public StringArray(int initialSize, int inc) {
        incrementSize = inc;

        array = new String[initialSize];
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
        for(int i = valueCount - 1; i >= 0; i--)
            array[i] = null;

        valueCount = 0;
    }

    /**
     * Add a new value to the array. Will resize the array if needed to
     * accommodate new values.
     *
     * @param newString the value to be added
     */
    public void add(String newString) {

        if(valueCount == array.length) {
            String[] newArray = new String[array.length + incrementSize];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }

        array[valueCount++] = newString;
    }

    /**
     * Add an array of values in bulk to the array. The array should not
     * be null.
     *
     * @param values The values to be added
     */
    public void add(String[] values) {
        int req_size = valueCount + values.length;

        if(req_size >= array.length) {
            String[] newArray = new String[req_size];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }

        System.arraycopy(values, 0, array, valueCount, values.length);
        valueCount = req_size;
    }

    /**
     * Get the value at the given index.
     *
     * @param index The position to get values from
     * @return The value at that index
     * @throws IndexOutOfBoundsException The index was not legal
     */
    public String get(int index) {
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
    public void set(int index, String value) {
        if((index < 0) || (index >= valueCount))
            throw new IndexOutOfBoundsException();

        array[index] = value;
    }

    /**
     * Remove the value at the given index.
     *
     * @param index The position to remove the value from
     * @return The value at that index
     * @throws IndexOutOfBoundsException The index was not legal
     */
    public String remove(int index) {
        if((index < 0) || (index >= valueCount))
            throw new IndexOutOfBoundsException();

        String ret_val = array[index];

        System.arraycopy(array, index + 1, array, index, array.length - index - 1);
        valueCount--;

        return ret_val;
    }

    /**
     * Turn the values of this array into a real array. Returns an array with
     * the exact number of items in it. This is a separate copy of the Stringernal
     * array.
     *
     * @return The array of values
     */
    public String[] toArray() {
        String[] ret_val = new String[valueCount];

        System.arraycopy(array, 0, ret_val, 0, valueCount);

        return ret_val;
    }

    /**
     * Turn the values of this array into a real array by copying them Stringo
     * the given array if possible. If the array is big enough then it will
     * copy the values straight in. If not, it will ignore that array and
     * create it's own copy and return that. If the passed array is used, the
     * return value will be a reference to the passed array, otherwise it will
     * be the new copy.
     *
     * @param values The array to copy values to
     * @return The array of values
     */
    public String[] toArray(String[] values) {

        String[] ret_val = null;

        if(values.length >= valueCount)
            ret_val = values;
        else
            ret_val = new String[valueCount];

        System.arraycopy(array, 0, ret_val, 0, valueCount);

        return ret_val;
    }
}
