/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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

// External imports
// None

// Local imports
// None

/**
 * A sensor that handles pick intersection tests.
 * <p>
 *
 * The picking sensor capabilities is an Xj3D extension specification. You
 * can find more details about it at
 * <a href="http://www.xj3d.org/extensions/picking.html">
 * http://www.xj3d.org/extensions/picking.html</a>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public interface VRMLPickingSensorNodeType extends VRMLSensorNodeType {

    /** The picking type is not defined because there isn't a pickGeometry given */
    public static final int UNDEFINED_PICK = 0;

    /** The picking type is point */
    public static final int POINT_PICK = 1;

    /** The picking type is line */
    public static final int LINE_PICK = 2;

    /** The picking type is sphere */
    public static final int SPHERE_PICK = 3;

    /** The picking type is box */
    public static final int BOX_PICK = 4;

    /** The picking type is cone */
    public static final int CONE_PICK = 5;

    /** The picking type is cylinder */
    public static final int CYLINDER_PICK = 6;

    /** The picking type is polytope/volume */
    public static final int VOLUME_PICK = 7;

    /** Sort order is CLOSEST */
    public static final int SORT_CLOSEST = 1;

    /** Sort order is ALL */
    public static final int SORT_ALL = 2;

    /** Sort order is ALL_SORTED */
    public static final int SORT_ALL_SORTED = 3;

    /** Sort order is ANY */
    public static final int SORT_ANY = 4;

    /** Intersection test type is BOUNDS */
    public static final int INTERSECT_BOUNDS = 1;

    /** Intersection test type is GEOMETRY */
    public static final int INTERSECT_GEOMETRY = 2;

    /**
     * Set the list of picking targets that this object corresponds to.
     * These can be an array of strings.
     *
     * @param types The list of object type strings to use
     * @param numValid The number of valid values to read from the array
     */
    public void setObjectType(String[] types, int numValid);

    /**
     * Get the current number of valid object type strings.
     *
     * @return a number >= 0
     */
    public int numObjectType();

    /**
     * Fetch the number of object type values in use currently.
     *
     * @param val An array to copy the values to
     */
    public void getObjectType(String[] val);

    /**
     * Get the picking type that this class represents. A shortcut way of
     * quickly determining the picking strategy to be used by the internal
     * implementation to avoid unnessary calculations.
     *
     * @return One of the *_PICK constants
     */
    public int getPickingType();

    /**
     * Get the intersection type requested for this node
     *
     * @return one of the SORT_* constants
     */
    public int getSortOrder();

    /**
     * Get the intersection type requested for this node
     *
     * @return one of the INTERSECT_* constants
     */
    public int getIntersectionType();


    /**
     * Set the goemetry used to perform the picking.
     *
     * @param geom VRMLGeometryNodeType
     * @throws InvalidFieldValueException The geometry is not acceptable for
     *    this picking type.
     */
    public void setPickingGeometry(VRMLNodeType geom);

    /**
     * Fetch the real node that is being used to pick the geometry. This
     * returns the real node that may be buried under one or more proto
     * instances as part of the geometry picking scheme. If the picker is an
     * externproto that hasn't resolved, obviously this will return null.
     *
     * @return The valid geometry node or null if not set
     */
    public VRMLNodeType getPickingGeometry();

    /**
     * Get the list of nodes that are used for the target geometry. This can
     * be a internal listing of children. Any node valid entries in the can be
     * set to null.
     */
    public VRMLNodeType[] getPickingTargets();

    /**
     * Notification that this sensor has just been clicked on to start the
     * pick action.
     *
     * @param numPicks The number of items picked in the array
     * @param nodes The geometry that was picked
     * @param points Optional array of points that are the intersection points
     * @param normals Optional array of normals that are the intersection points
     * @param texCoords Optional array of texture coordinates that are the intersection points
     */
    public void notifyPickStart(int numPicks,
                                VRMLNodeType[] nodes,
                                float[] points,
                                float[] normals,
                                float[] texCoords);

    /**
     * Notify the drag sensor that a sensor is currently dragging this device
     * and that it's position and orientation are as given.
     *
     * @param numPicks The number of items picked in the array
     * @param nodes The geometry that was picked
     * @param points Optional array of points that are the intersection points
     * @param normals Optional array of normals that are the intersection points
     * @param texCoords Optional array of texture coordinates that are the intersection points
     */
    public void notifyPickChange(int numPicks,
                                 VRMLNodeType[] nodes,
                                 float[] points,
                                 float[] normals,
                                 float[] texCoords);

    /**
     * Notification that this sensor has finished a picking action.
     */
    public void notifyPickEnd();
}
