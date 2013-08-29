/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
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

// Standard imports
// none

// Application specific imports
// none

/**
 * Representation of a node that supplies terrain data.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface VRMLTerrainSource {
    /**
     * Get the height data.  Provide a
     * direct reference to the height data to avoid a copy.  Height
     * data is usually a field, no listeners needed for changes.
     *
     * @return The height data
     */
    public double[] getHeight();

    /**
     * Get the spacing data.
     *
     * @return The spacing data
     */
    public double[] getSpacing();

    /**
     * Get the xSize.
     *
     * @return the number of points in the x direction.
     */
    public int getXSize();

    /**
     * Get the zSize.
     *
     * @return the number of points in the z direction.
     */
    public int getZSize();

    /**
     * Should we use Roam to display this terrain.
     */
    public boolean useRoam();

    /**
     * Get the origin of this source in GC coordinates.
     */
    public double[] getGeoOrigin();

    public float[][][] getTextureCoords();
}
