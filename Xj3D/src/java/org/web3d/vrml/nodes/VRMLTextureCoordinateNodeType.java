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
 * Specifies a set of 2D texture coordinates.  May contain multiple texture
 * coordinate sets.
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public interface VRMLTextureCoordinateNodeType
    extends VRMLGeometricPropertyNodeType {

    /**
     * Get the number of components defined for this texture type. SHould
     * be one of 2, 3 or 4 for 2D, 3D or time-driven textures.
     *
     * @return one of 2, 3 or 4
     */
    public int getNumTextureComponents();

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets();

    /**
     * Get the size of the specified set. The size is the total size of the
     * array, taking into account the number of texture components in use.
     *
     * @param setNum The set to size
     */
    public int getSize(int setNum);

    /**
     * Accessor method to set a new value for field attribute point.  Attempts
     * to set nodes > numSets will throw an exception.
     *
     * @param setNum The set which this point belongs.
     * @param newPoint New value for the point field
     * @param numValid The number of valid values to copy from the array
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setPoint(int setNum, float[] newPoint, int numValid);

    /**
     * Accessor method to get current value of field point.
     *
     * @param setNum The set which this point belongs.
     * @param point The array to initialize with the point value
     */
    public void getPoint(int setNum, float[] point);

    /**
     * Determine if this index is shared via DEF/USE inside this set
     *
     * @param index The index to check
     * @return The index if not shared or the original index DEFed
     */
    public int isShared(int index);

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum);
}
