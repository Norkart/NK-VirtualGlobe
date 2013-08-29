/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geospatial;

// External imports
import javax.vecmath.Point3d;

import org.j3d.util.interpolator.Interpolator;

// Local imports
// none

/**
 * Custom implementation of the  interpolator that works with double precision
 * positional coordinates.
 * <P>
 *
 * The interpolation routine is just a simple linear interpolation between
 * each of the points. The interpolator may take arbitrarily spaced keyframes
 * and compute correct values.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class PositionInterpolator extends Interpolator
{
    /** Reference to the shared Point3f return value for key values */
    private Point3d sharedPoint;

    /** Reference to the shared float array return value for key values */
    private double[] sharedVector;

    /** The key values indexed as [index][x, y, z] */
    private double[][] keyValues;

    /**
     * Create a new linear interpolator instance with the default size for the
     * number of key values.
     */
    PositionInterpolator()
    {
        this(DEFAULT_SIZE);
    }

    /**
     * Create a interpolator with the given basic size using the interpolation
     * type.
     *
     * @param size The starting number of items in interpolator
     * @param type The type of interpolation scheme to use
     */
    PositionInterpolator(int size)
    {
        super(size, LINEAR);

        keys = new float[size];
        keyValues = new double[size][3];

        sharedPoint = new Point3d();
        sharedVector = new double[3];
    }

    /**
     * Add a key frame set of values at the given key point. This will insert
     * the values at the correct position within the array for the given key.
     * If two keys have the same value, the new key is inserted before the old
     * one.
     *
     * @param key The value of the key to use
     * @param x The x coordinate of the position at this key
     * @param y The y coordinate of the position at this key
     * @param z The z coordinate of the position at this key
     */
    void addKeyFrame(float key, double x, double y, double z)
    {
        int loc = findKeyIndex(key);

        // loc is now the largest key less than the new key.
        // adjust loc up to the first key greater than the new key.
        if(loc < 0)
            loc = 0;
        while (loc<currentSize && keys[loc]<=key)
            loc++;

        realloc();

        double[] new_val;

        if(loc >= currentSize)
        {
            // append to the end
            new_val = keyValues[currentSize];
        }
        else
        {
            // insert. Shuffle everything up one spot
            int num_moving = currentSize - loc;

            System.arraycopy(keyValues, loc, keyValues, loc + 1, num_moving);
            System.arraycopy(keys, loc, keys, loc + 1, num_moving);

            new_val = new double[3];
            keyValues[loc] = new_val;
        }

        new_val[0] = x;
        new_val[1] = y;
        new_val[2] = z;

        keys[loc] = key;
        currentSize++;
    }

    /**
     * Add a key frame set of values at the given key point. This will insert
     * the values at the correct position within the array for the given key.
     * If two keys have the same value, the new key is inserted before the old
     * one.
     *
     * @param key The value of the key to use
     * @param pt The point data to take information from
     */
    void addKeyFrame(float key, Point3d pt)
    {
        addKeyFrame(key, pt.x, pt.y, pt.z);
    }

    /**
     * Get the interpolated value of the point at the given key value. If the
     * key lies outside the range of the values defined, it will be clamped to
     * the end point value. For speed reasons, this will return a reusable
     * point instance. Do not modify the values or keep a reference to this as
     * it will change values between calls.
     *
     * @param key The key value to get the position for
     * @return A point representation of the value at that position
     */
    Point3d pointValue(float key)
    {
        int loc = findKeyIndex(key);

        if(loc < 0)
           sharedPoint.set(keyValues[0]);
        else if(loc >= (currentSize - 1))
           sharedPoint.set(keyValues[currentSize - 1]);
        else
        {
            double[] p1 = keyValues[loc + 1];
            double[] p0 = keyValues[loc];

            double x_dist = p1[0] - p0[0];
            double y_dist = p1[1] - p0[1];
            double z_dist = p1[2] - p0[2];

            float fraction = 0;

            // just in case we get two keys the same
            float prev_key = keys[loc];
            float found_key = keys[loc + 1];

            if(found_key != prev_key)
                fraction = (key - prev_key) / (found_key - prev_key);

            sharedPoint.x = p0[0] + fraction * x_dist;
            sharedPoint.y = p0[1] + fraction * y_dist;
            sharedPoint.z = p0[2] + fraction * z_dist;
        }

        return sharedPoint;
    }

    /**
     * Get the interpolated value of the point at the given key value. If the
     * key lies outside the range of the values defined, it will be clamped to
     * the end point value. For speed reasons, this will return a reusable
     * float array. Do not modify the values or keep a reference to this as
     * it will change values between calls.
     *
     * @param key The key value to get the position for
     * @return An array of the values at that position [x, y, z]
     */
    double[] floatValue(float key)
    {
        int loc = findKeyIndex(key);

        if(loc < 0)
        {
           sharedVector[0] = keyValues[0][0];
           sharedVector[1] = keyValues[0][1];
           sharedVector[2] = keyValues[0][2];
        }
        else if(loc >= (currentSize - 1))
        {
           sharedVector[0] = keyValues[currentSize - 1][0];
           sharedVector[1] = keyValues[currentSize - 1][1];
           sharedVector[2] = keyValues[currentSize - 1][2];
        }
        else
        {
            double[] p1 = keyValues[loc + 1];
            double[] p0 = keyValues[loc];

            double x_dist = p1[0] - p0[0];
            double y_dist = p1[1] - p0[1];
            double z_dist = p1[2] - p0[2];

            float fraction = 0;

            // just in case we get two keys the same
            float prev_key = keys[loc];
            float found_key = keys[loc + 1];

            if(found_key != prev_key)
                fraction = (key - prev_key) / (found_key - prev_key);

/*
System.out.println("Prev key " + prev_key);
System.out.println("Next key " + found_key);
System.out.println("Reqd key " + key);
System.out.println("Fraction is " + fraction);
System.out.println("x " + p0[0] + " x_dist " + x_dist);
System.out.println("y " + p0[1] + " y_dist " + y_dist);
System.out.println("z " + p0[2] + " z_dist " + z_dist);
*/
            sharedVector[0] = p0[0] + fraction * x_dist;
            sharedVector[1] = p0[1] + fraction * y_dist;
            sharedVector[2] = p0[2] + fraction * z_dist;
        }

        return sharedVector;
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------

    /**
     * Resize the allocated space for the keyValues array if needed. Marked
     * as final in order to encourage the compiler to inline the code for
     * faster execution.
     */
    private final void realloc()
    {
        if(currentSize == allocatedSize)
        {
            int new_size = allocatedSize + ARRAY_INCREMENT;

            // Don't acutally allocate the space for the float[3] values as the
            // arraycopy will set these. Just make sure we allocate after that
            // the remaining new, empty, places.
            double[][] new_values = new double[new_size][];

            System.arraycopy(keyValues, 0, new_values, 0, allocatedSize);

            for(int i = allocatedSize; i < new_size; i++)
                new_values[i] = new double[3];

            float[] new_keys = new float[new_size];

            System.arraycopy(keys, 0, new_keys, 0, allocatedSize);

            keys = new_keys;
            keyValues = new_values;

            allocatedSize = new_size;
        }
    }

    /**
     * Create a string representation of this interpolator's values
     *
     * @return A nicely formatted string representation
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer("<position interpolator>\n");

        for(int i = 0; i < currentSize; i++)
        {
            buf.append(i);
            buf.append(" key: ");
            buf.append(keys[i]);
            buf.append(" x: ");
            buf.append(keyValues[i][0]);
            buf.append(" y: ");
            buf.append(keyValues[i][1]);
            buf.append(" z: ");
            buf.append(keyValues[i][2]);
            buf.append("\n");
        }

        buf.append("</position interpolator>");
        return buf.toString();
    }
}
