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
 * Defines a set of 3D surface normals.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public interface VRMLNormalNodeType extends VRMLGeometricPropertyNodeType {
    /**
     * Accessor method to set a new value for field attribute vector.
     * Vector is an array of triplicates of Vec3f data
     * ie [V3f0-0,V3f0-1,V3f0-2,V3f1-0,V3f1-1,V3f1-2,V3f2...]
     *
     * @param newVector The new value for vector
     * @param numValid The number of valid values to copy from the array
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setVector(float[] newVector, int numValid);

    /**
     * Get the number of items in the normal array now. The number returned is
     * the total number of values in the flat array. This will allow the caller
     * to construct the correct size array for the getVector() call.
     *
     * @return The number of values in the array
     */
    public int getNumNormals();

    /**
     * Get current value of field vector. Vector is an array of SFVec3f float
     * triples. Don't call if there are no vectors in the array.
     *
     * @param normals The array to copy the vector values into
     */
    public void getVector(float[] normals);

    /**
     * Get the internal reference to the raw or converted array of normals. Some
     * of the concrete node types end up needing to convert the point values
     * from double precision to single precision or needing to make geo-spatial
     * projections. This is a reference to the post-processed data that may be
     * directly used for rendering.
     * <p>
     * Note that the array may well be longer than the actual number of valid
     * normals. Use {@link #getNumNormals()} to determine the number of valid
     * entries.
     *
     * @return An array of float[] values for rendering process
     */
    public float[] getVectorRef();
}
