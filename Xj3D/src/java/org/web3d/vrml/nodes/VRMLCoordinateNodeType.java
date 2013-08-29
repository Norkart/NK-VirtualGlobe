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
package org.web3d.vrml.nodes;

/**
 * Nodes which have coordinate information
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public interface VRMLCoordinateNodeType extends VRMLGeometricPropertyNodeType {

    /**
     * Accessor method to set a new value for field attribute point.
     * point is an array of Vec3f doubles
     *
     * @param newPoint New value for the point field
     * @param numValid The number of valid values to copy from the array
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setPoint(float[] newPoint, int numValid);

    /**
     * Get the number of items in the point array now. The number returned is
     * the total number of values in the flat array. This will allow the caller
     * to construct the correct size array for the getPoint() call.
     *
     * @return The number of values in the array
     */
    public int getNumPoints();

    /**
     * Get current value of field point. Point is an array of Vec3f float
     * triples. Don't call if there are no points in the array.
     *
     * @param points The array to copy the values into
     */
    public void getPoint(float[] points);

    /**
     * Get the internal reference to the raw or converted point array. Some
     * of the concrete node types end up needing to convert the point values
     * from double precision to single precision or needing to make geo-spatial
     * projections. This is a reference to the post-processed data that may be
     * directly used for rendering. In the case of CoordinateDouble, then the
     * array may be a set of down-cast values to floats.
     * <p>
     * Note that the array may well be longer than the actual number of valid
     * coordinates. Use {@link #getNumPoints()} to determine the number of
     * valid entries.
     *
     * @return An array of float[] values for rendering process
     */
    public float[] getPointRef();
}
