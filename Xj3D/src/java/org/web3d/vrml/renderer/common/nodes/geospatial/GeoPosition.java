/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
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
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;

// Local imports
// None

/**
 * Holds the coordinates for a position within some coordinate reference system
 * for transformation within Xj3D's height data generator.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class GeoPosition implements DirectPosition {

    /** The position held by this instance */
    private double[] position;

    /**
     * Create a new instance of this class. The initial position is set to the
     * origin.
     */
    GeoPosition() {
        position = new double[3];
    }

    /**
     * Create a new instance of this class that copies the initial position from
     * the given array.
     *
     * @param pos The initial position to initialise to
     */
    GeoPosition(double [] pos) {
        this();
        position[0] = pos[0];
        position[1] = pos[1];
        position[2] = pos[2];
    }

    //----------------------------------------------------------
    // Methods defined by DirectPosition
    //----------------------------------------------------------

    /**
     * The length of coordinate sequence (the number of entries). Always
     * returns 3, as this always represents a 3D point.
     *
     * @return The dimensionality of this position.
     */
    public int getDimension() {
        return 3;
    }

    /**
     * Returns the sequence of numbers that hold the coordinate of this
     * position in its reference system. Returns a reference to the internal
     * value. Since this is only used by coordinate transformations to a
     * different object instance, we don't care if the internal value changes.
     * Thus we ignore that rule for OpenGIS.
     *
     * @return A copy of the coordinates
     */
    public double[] getCoordinates() {
        return position;
    }

    /**
     * Returns the ordinate at the specified dimension.
     *
     * @param  dimension The dimension in the range 0 to 3
     * @return The coordinate at the specified dimension.
     * @throws IndexOutOfBoundsException if the specified dimension is out of bounds.
     */
    public double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        return position[dimension];
    }

    /**
     * Sets the ordinate value along the specified dimension.
     *
     * @param dimension the dimension for the ordinate of interest.
     * @param value the ordinate value of interest.
     * @throws IndexOutOfBoundsException if the specified dimension is out of bounds.
     */
    public void setOrdinate(int dimension, double value)
        throws IndexOutOfBoundsException {
        position[dimension] = value;
    }

    /**
     * The coordinate reference system in which the coordinate is given.
     * Always returns <code>null</code> as the parent coordinate system is
     * defined by previously by the MathTransform in use.
     *
     * @return Always <code>null</code>.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return null;
    }

    /**
     * Makes an exact copy of this coordinate.
     */
    public Object clone() {
        return new GeoPosition(position);
    }

    //----------------------------------------------------------
    // Methods defined by Position
    //----------------------------------------------------------

    /**
     * Returns the direct position (ie itself)
     *
     * @return <code>this</code>
     */
    public DirectPosition getPosition() {
        return this;
    }

    //----------------------------------------------------------
    // Internal Methods
    //----------------------------------------------------------

    /**
     * Set a new position to the given values.
     *
     * @param pos The initial position to initialise to
     */
    void setPosition(double [] pos) {
        position[0] = pos[0];
        position[1] = pos[1];
        position[2] = pos[2];
    }

    /**
     * Set a new position to the given values.
     *
     * @param x The x component of the position
     * @param y The y component of the position
     * @param z The z component of the position
     */
    void setPosition(double x, double y, double z) {
        position[0] = x;
        position[1] = y;
        position[2] = z;
    }
}
