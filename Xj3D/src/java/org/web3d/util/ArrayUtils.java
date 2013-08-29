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
 * Utility class for doing array manipulation.
 * <p>
 *
 * The main use of this class is to provide a central location that
 * converts 2D arrays to flat structures and back again. All methods assume
 * that you have passed a target array in of at least the minimum length. If
 * you don't an array index exception will be generated.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class ArrayUtils {

    /**
     * Flatten a 2D array with 2 items in the second dimension into a 1D array.
     *
     * @param in The array to be flattened
     * @param size The number of items to copy from the in array
     * @param out The output array to write the values to
     */
    public static void flatten2(float[][] in, int size, float[] out) {
        int count = size * 2 - 1;

        for(int i = size; --i >= 0; ) {
            out[count--] = in[i][1];
            out[count--] = in[i][0];
        }
    }

    /**
     * Flatten a 2D array with 3 items in the second dimension into a 1D array.
     *
     * @param in The array to be flattened
     * @param size The number of items to copy from the in array
     * @param out The output array to write the values to
     */
    public static void flatten3(float[][] in, int size, float[] out) {
        int count = size * 3 - 1;

        for(int i = size; --i >= 0; ) {
            out[count--] = in[i][2];
            out[count--] = in[i][1];
            out[count--] = in[i][0];
        }
    }

    /**
     * Flatten a 2D array with 4 items in the second dimension into a 1D array.
     *
     * @param in The array to be flattened
     * @param size The number of items to copy from the in array
     * @param out The output array to write the values to
     */
    public static void flatten4(float[][] in, int size, float[] out) {
        int count = size * 4 - 1;

        for(int i = size; --i >= 0; ) {
            out[count--] = in[i][3];
            out[count--] = in[i][2];
            out[count--] = in[i][1];
            out[count--] = in[i][0];
        }
    }

    /**
     * Flatten a 2D array with n items in the second dimension into a 1D array.
     * Generic but slower then the static sized conterparts.
     *
     * @param in The array to be flattened
     * @param size The number of items to copy from the in array
     * @param width The number of items in the second dimension
     * @param out The output array to write the values to
     */
    public static void flattenN(float[][] in, int size, int width,
        float[] out) {

        int count = size * width - 1;

        for(int i = size; --i >= 0; ) {
            for (int j = width; --j >= 0; ) {
                out[count--] = in[i][j];
            }
        }
    }

    /**
     * Flatten a 2D array with 2 items in the second dimension into a 1D array.
     *
     * @param in The array to be flattened
     * @param size The number of items to copy from the in array
     * @param out The output array to write the values to
     */
    public static void flatten2(double[][] in, int size, double[] out) {
        int count = size * 2 - 1;

        for(int i = size; --i >= 0; ) {
            out[count--] = in[i][1];
            out[count--] = in[i][0];
        }
    }

    /**
     * Flatten a 2D array with 3 items in the second dimension into a 1D array.
     *
     * @param in The array to be flattened
     * @param size The number of items to copy from the in array
     * @param out The output array to write the values to
     */
    public static void flatten3(double[][] in, int size, double[] out) {
        int count = size * 3 - 1;

        for(int i = size; --i >= 0; ) {
            out[count--] = in[i][2];
            out[count--] = in[i][1];
            out[count--] = in[i][0];
        }
    }

    /**
     * Flatten a 2D array with n items in the second dimension into a 1D array.
     *
     * @param in The array to be flattened
     * @param size The number of items to copy from the in array
     * @param width The number of items in the second dimension
     * @param out The output array to write the values to
     */
    public static void flattenN(double[][] in, int size, int width,
        double[] out) {

        int count = size * width - 1;

        for(int i = size; --i >= 0; ) {
            for (int j = width; --j >= 0; ) {
                out[count--] = in[i][j];
            }
        }
    }

    // Now head the other way.....

    /**
     * Raise a 1D array into a 2D array with 2 items in the second dimension.
     *
     * @param in The array to be raised
     * @param size The number of vectors to copy from the in array
     * @param out The output array to write the values to
     */
    public static void raise2(float[] in, int size, float[][] out) {
        int count = size * 2 - 1;

        for(int i = size; --i >= 0; ) {
            out[i][1] = in[count--];
            out[i][0] = in[count--];
        }
    }

    /**
     * Raise a 1D array into a 2D array with 3 items in the second dimension.
     *
     * @param in The array to be raised
     * @param size The number of vectors to copy from the in array
     * @param out The output array to write the values to
     */
    public static void raise3(float[] in, int size, float[][] out) {
        int count = size * 3 - 1;

        for(int i = size; --i >= 0; ) {
            out[i][2] = in[count--];
            out[i][1] = in[count--];
            out[i][0] = in[count--];
        }
    }

    /**
     * Raise a 1D array into a 2D array with 4 items in the second dimension.
     *
     * @param in The array to be raised
     * @param size The number of vectors to copy from the in array
     * @param out The output array to write the values to
     */
    public static void raise4(float[] in, int size, float[][] out) {
        int count = size * 4 - 1;

        for(int i = size; --i >= 0; ) {
            out[i][3] = in[count--];
            out[i][2] = in[count--];
            out[i][1] = in[count--];
            out[i][0] = in[count--];
        }
    }

    /**
     * Raise a 1D array into a 2D array with N items in the second dimension.
     * Generic but slower then the static sized conterparts.
     *
     * @param in The array to be raised
     * @param size The number of vectors to copy from the in array
     * @param width The size of the vectors
     * @param out The output array to write the values to
     */
    public static void raiseN(float[] in, int size, int width, float[][] out) {
        int count = size * width - 1;

        for(int i = size; --i >= 0; ) {
            for (int j = width; --j >= 0; ) {
                out[i][j] = in[count--];
            }
        }
    }

    /**
     * Raise a 1D array into a 2D array with 2 items in the second dimension.
     *
     * @param in The array to be raised
     * @param size The number of vectors to copy from the in array
     * @param out The output array to write the values to
     */
    public static void raise2(double[] in, int size, double[][] out) {
        int count = size * 2 - 1;

        for(int i = size; --i >= 0; ) {
            out[i][1] = in[count--];
            out[i][0] = in[count--];
        }
    }

    /**
     * Raise a 1D array into a 2D array with 3 items in the second dimension.
     *
     * @param in The array to be raised
     * @param size The number of vectors to copy from the in array
     * @param out The output array to write the values to
     */
    public static void raise3(double[] in, int size, double[][] out) {
        int count = size * 3 - 1;

        for(int i = size; --i >= 0; ) {
            out[i][2] = in[count--];
            out[i][1] = in[count--];
            out[i][0] = in[count--];
        }
    }

    /**
     * Raise a 1D array into a 2D array with 4 items in the second dimension.
     *
     * @param in The array to be raised
     * @param size The number of vectors to copy from the in array
     * @param out The output array to write the values to
     */
    public static void raise4(double[] in, int size, double[][] out) {
        int count = size * 4 - 1;

        for(int i = size; --i >= 0; ) {
            out[i][3] = in[count--];
            out[i][2] = in[count--];
            out[i][1] = in[count--];
            out[i][0] = in[count--];
        }
    }

    /**
     * Raise a 1D array into a 2D array with N items in the second dimension.
     * Generic but slower then the static sized conterparts.
     *
     * @param in The array to be raised
     * @param size The number of vectors to copy from the in array
     * @param width The size of the vectors
     * @param out The output array to write the values to
     */
    public static void raiseN(double[] in, int size, int width,
        double[][] out) {

        int count = size * width - 1;

        for(int i = size; --i >= 0; ) {
            for (int j = width; --j >= 0; ) {
                out[i][j] = in[count--];
            }
        }
    }
}
