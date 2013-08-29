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

package org.web3d.vrml.renderer.j3d.nodes.render;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.Geometry;
import javax.media.j3d.PointArray;
import javax.media.j3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.nodes.*;

import org.web3d.vrml.renderer.common.nodes.render.BasePointSet;

/**
 * Java3D implementation of an PointSet.
 * <p>
 *
 * The point set directly maps to Java3D's PointArray class. When the
 * coordinates change to a different length than the current set, it will
 * notify the geometry listener to fetch the new information.
 * <p>
 * If the VRML file did not provide a Coordinate node, then this class will
 * not present any geometry from the {@link #getGeometry()} or
 * {@link #getSceneGraphObject()} calls. If the user later specifies the
 * renderety through an event, the listener(s) will be notified.
 * <p>
 * In this implementation, if the length of the color array is shorter that
 * the length of the coordinate array, colors will be ignored.
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public class J3DPointSet extends BasePointSet
    implements J3DGeometryNodeType {

    /** Constant indicating the geometry at index 0 for listener updates */
    private static final int[] GEOM_CHANGED_INDEX = {0};

    /** The impl for this class */
    private PointArray implGeom;

    /** The array of listeners registered with this node */
    private ArrayList listeners;

    /**
     * The number of points in the current coordinate array. The value is
     * actually the number of points x 3 because we have a simple check for
     * array lengths then.
     */
    private int numPoints;

    /**
     * Flag indicating that color values were used last time the geometry was
     * generated. It is possible that we have a Color node set but didn't use
     * the color values because the array length was less than that of the
     * points. This flag indicates whether we did or not.
     */
    private boolean usedColor;

    /** Flag indicating the color was changed */
    private boolean colorChanged;

    /** Flag indicating the coord was changed */
    private boolean coordChanged;

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public J3DPointSet() {
        usedColor = false;
        listeners = new ArrayList();

        colorChanged = false;
        coordChanged = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public J3DPointSet(VRMLNodeType node) {
        this();

        checkNodeType(node);
    }

    //-------------------------------------------------------------
    // Methods defined by FrameStateListener
    //-------------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the j3d representation
     * only once per frame.
     */
    public void allEventsComplete() {
        if(coordChanged) {
            if(vfCoord == null) {
                fireGeometryRemoved(GEOM_CHANGED_INDEX);
            } else {
                float[] coords = vfCoord.getPointRef();
                int num = vfCoord.getNumPoints();

                PointArray new_render = updateGeometry(coords, num);
                if(new_render != implGeom) {
                    implGeom = new_render;

                    if(implGeom == null) {
                        numPoints = num;
                        fireGeometryAdded(GEOM_CHANGED_INDEX);
                    } else if(new_render == null) {
                        numPoints = 0;
                        fireGeometryRemoved(GEOM_CHANGED_INDEX);
                    } else {
                        numPoints = num;
                        fireGeometryChanged(GEOM_CHANGED_INDEX);
                    }
                }
            }

            coordChanged = false;
        }

        if(colorChanged) {
            float[] color_vals = null;

            if(vfColor != null) {
                int num_items = vfColor.getNumColors();
                color_vals = new float[num_items];
                vfColor.getColor(color_vals);
            }

            PointArray new_render = updateColor(color_vals);

            if(new_render != implGeom) {
                implGeom = new_render;
                fireGeometryChanged(GEOM_CHANGED_INDEX);
            }

            colorChanged = false;
        }
    }

    //-------------------------------------------------------------
    // Methods defined by J3DGeometryNodeType
    //-------------------------------------------------------------

    /*
     * Returns a J3D Geometry collection that represents this piece of
     * geometry. If there is only one piece of geometry this will return
     * an array of lenght 1.
     *
     * @return The geometry needed to represent this object
     */
    public Geometry[] getGeometry() {

        Geometry[] ret_val = null;

        if(implGeom != null) {
            ret_val = new Geometry[] { implGeom };
        }

        return ret_val;
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets() {
        return 0;
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum) {
        return null;
    }

    /**
     * Add a listener for geometry changed events. If the listener is null or
     * already registered it will silently ignore the requests.
     *
     * @param l The listener to add
     */
    public void addGeometryListener(J3DGeometryListener l) {
        if((l == null) || listeners.contains(l))
            return;

        listeners.add(l);
    }

    /**
     * Remove a listener for geometry changed events. If the listener is null
     * or has not been registered it will silently ignore the requests.
     *
     * @param l The listener to remove
     */
    public void removeGeometryListener(J3DGeometryListener l) {
        if((l == null) || !listeners.contains(l))
            return;

        listeners.remove(l);
    }

    //----------------------------------------------------------
    // Methods defined by J3DVRMLNode
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implGeom;
    }

    /**
     * Provide the set of mappings that override anything that the loader
     * might set.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Notify a node that an ExternProto has resolved.  This will verify the objects
     * type and add it to the render sceneGraph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
     public void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        if(inSetup)
            return;

        PointArray new_render;
        int num_items;

        switch(index) {
            case FIELD_COLOR:
                int[] alt_type = node.getSecondaryType();
                boolean found_type = false;

                for(int i = 0; i < alt_type.length && !found_type; i++) {
                    if(alt_type[i] == TypeConstants.ColorNodeType)
                        found_type = true;
                }

                if(!found_type)
                    throw new InvalidFieldValueException(COLOR_PROTO_MSG);

                colorChanged = true;

                pColor = (VRMLProtoInstance)node;
                vfColor = (VRMLColorNodeType)pColor.getImplementationNode();

                if(!inSetup)
                    stateManager.addEndOfThisFrameListener(this);
                break;

            case FIELD_COORD:
                alt_type = node.getSecondaryType();
                found_type = false;

                for(int i = 0; i < alt_type.length && !found_type; i++) {
                    if(alt_type[i] == TypeConstants.CoordinateNodeType)
                        found_type = true;
                }

                if(!found_type)
                    throw new InvalidFieldValueException(COORD_PROTO_MSG);

                coordChanged = true;

                pCoord = (VRMLProtoInstance)node;
                vfCoord = (VRMLCoordinateNodeType)pCoord.getImplementationNode();

                if(!inSetup)
                    stateManager.addEndOfThisFrameListener(this);
                break;

            default:
                System.out.println("J3DPointSet: Unknown field for notifyExternProtoLoaded");
        }
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        // If we don't have any coordinate data, it is pointless to go on.
        if(vfCoord == null || vfCoord.getNumPoints() < 1)
            return;

        float[] points = vfCoord.getPointRef();
        float[] color_vals = null;

        numPoints = points.length;

        if(vfColor != null) {
            color_vals = new float[vfColor.getNumColors()];
            vfColor.getColor(color_vals);
            usedColor = (color_vals.length >= numPoints);
        } else {
            usedColor = false;
        }

        implGeom = createPointArray(numPoints);

        // Set the geometry values
        implGeom.setCoordinates(0, points);

        if(usedColor)
            implGeom.setColors(0, color_vals, 0, numPoints / 3);
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNodeComponentListener
    //-------------------------------------------------------------

    /**
     * Notification that the field from the node has changed.
     *
     * @param node The component node that changed
     * @param index The index of the field that has changed
     */
    public void fieldChanged(VRMLNodeType node, int index) {
        int field;
        VRMLFieldData data;

        switch(index) {
            case FIELD_COORD:
                coordChanged = true;
                if(!inSetup)
                    stateManager.addEndOfThisFrameListener(this);
                break;


            case FIELD_COLOR:
                colorChanged = true;
                if(!inSetup)
                    stateManager.addEndOfThisFrameListener(this);
                break;
        }
    }

    //----------------------------------------------------------
    // Methods overriding BaseComponentGeometryNode
    //----------------------------------------------------------

    /**
     * Notification of the coordinate node being set. If the passed value is
     * null then that clears the node. The node passed is the actual geometry,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    protected void setCoordinateNode(VRMLCoordinateNodeType node) {
        if(inSetup)
            return;

        if(node == null) {
            fireGeometryRemoved(null);
        } else {
            float[] points = vfCoord.getPointRef();
            int num = vfCoord.getNumPoints();

            PointArray new_render = updateGeometry(points, num);

            // If the update changed the geometry, we want to send off an
            // event to the listeners.
            if(new_render != implGeom) {
                if(implGeom == null) {
                    numPoints = num;
                    fireGeometryAdded(null);
                } else if(new_render == null) {
                    numPoints = 0;
                    fireGeometryRemoved(GEOM_CHANGED_INDEX);
                } else {
                    numPoints = num;
                    fireGeometryChanged(GEOM_CHANGED_INDEX);
                }
            }
        }
    }

    /**
     * Notification of the color node being set. If the passed value is
     * null then that clears the node. The node passed is the actual color,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    protected void setColorNode(VRMLColorNodeType node) {

        if(!inSetup)
            return;

        float[] color_vals = null;

        if(node != null) {
            color_vals = new float[node.getNumColors()];
            node.getColor(color_vals);
        }

        PointArray new_render = updateColor(color_vals);

        if(new_render != implGeom) {
            implGeom = new_render;
            fireGeometryChanged(GEOM_CHANGED_INDEX);
        }
    }

    //----------------------------------------------------------
    // Methods internal to J3DPointSet
    //----------------------------------------------------------

    /**
     * Update the render geometry based on the new set of coordinates. This assumes
     * that we have a valid piece of geometry that we want to update. That is,
     * it will re-allocate a geometry array if one already exists.
     * <p>
     * If the array passed is null or less than 3 in length, we delete the
     * geometry as it is not able to be shown.
     * <p>
     * The return value may be:<br>
     * null: indicating that the point array should not be used for this set
     * of coordinates<br>
     * The current reference to implGeom indicating that we've just change
     * the internal values.<br>
     * A new geometry instance because the old one is not compatible with the
     * the requirements for these coordinates.
     *
     * @param coords The new coordinate array to use
     * @param numCoords The number of float values in coord
     * @return A reference to an updated PointArray
     */
    private PointArray updateGeometry(float[] coords, int numCoords) {
        float[] color_vals = null;
        PointArray ret_val = implGeom;

        // Do we have any coordinates or not enough?
        if((coords == null) || (numCoords < 3)) {
            ret_val = null;
        } else {
            if(vfColor != null) {
                color_vals = new float[vfColor.getNumColors()];
                vfColor.getColor(color_vals);
                usedColor = (color_vals.length >= coords.length);
            }

            // If the geometry length has changed, we will need to create
            // a new PointArray. Handles when the previous geometry item
            // was null because it wasn't set.
            if(numCoords != numPoints && numPoints > 0)
                ret_val = createPointArray(numPoints);

            if (ret_val != null) {
                // now setup the points.
                ret_val.setCoordinates(0, coords);

                if(usedColor)
                    ret_val.setColors(0, color_vals, 0, numPoints);
            }
        }

        return ret_val;
    }

    /**
     * Update the color based on the new set of values. The assumption is that
     * we currently have a valid piece of render geometry to update. The caller should
     * check to make sure that geometry is available before calling this
     * method otherwise it is likely to crash with a null pointer.
     * <p>
     * If the array passed is null or less than the current number of
     * coordinates in length, we do not add colour values to the geometry as it
     * is not able to be shown. Instead we may create a new geometry instance
     * to replace it if the old geometry has colour.
     * <p>
     * The return value will either be a new geometry array or the currently
     * existing one if we didn't have to change it to add color.
     *
     * @param colors The new colour values to use
     * @return An update geometry instance with the new colours installed
     */
    private PointArray updateColor(float[] color) {
        PointArray ret_val = implGeom;

        // Are we changing the colors or removing them? Don't use colours if
        // the array is null, contains less than the number of coordinate
        // points.
        if((color == null) || (color.length < numPoints)) {

            // If we previously used color values, we need to reallocate the
            // point array.
            if(usedColor && numPoints > 0) {
                usedColor = false;
                ret_val = createPointArray(numPoints);
                float[] coords = vfCoord.getPointRef();
                ret_val.setCoordinates(0, coords);
            }
        } else {
            // so we have a useful set of colors to use. Did we use color
            // before? If not then allocate a new point array. If so, we
            // don't care and will just set the new color values to the
            // existing array.

            if(!usedColor && numPoints > 0) {
                usedColor = true;
                ret_val = createPointArray(numPoints);
                float[] coords = vfCoord.getPointRef();

                ret_val.setCoordinates(0, coords);
            }

            // Finally! Set the color values
            if (ret_val != null)
                ret_val.setColors(0, color, 0, numPoints / 3);
        }

        return ret_val;
    }

    /**
     * Simple convenience method to allocate the new point array as we have to
     * do this all the time. This method uses the class variable usedColor to
     * determine the flags needed. If you want to create a new array that has
     * color this time, but maybe didn't last time, make sure the variable is
     * set before calling this.
     */
    private PointArray createPointArray(int length) {

        PointArray ret_val;

        int vertex_flags = PointArray.COORDINATES | PointArray.NORMALS;

        if(usedColor)
            vertex_flags |= PointArray.COLOR_3;

        float[] lfNormals = new float[length];
        for(int i=0; i < length / 3; i++) {
            lfNormals[i++] = 1;
            lfNormals[i++] = 0;
            lfNormals[i++] = 0;
        }

        ret_val = new PointArray(length / 3, vertex_flags);

        ret_val.setNormals(0,lfNormals);

        if(!isStatic) {
            ret_val.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
            ret_val.setCapability(PointArray.ALLOW_COLOR_WRITE);
        }

        return ret_val;
    }

    /**
     * fire a geometry added event to the listeners.
     *
     * @param items The geometry items that have been added
     */
    protected void fireGeometryAdded(int[] items) {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryAdded(items);
            } catch(Exception e) {
                System.out.println("Error sending geometry add message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * fire a geometry changed event to the listeners.
     *
     * @param items The geometry items that have changed or null for all
     */
    protected void fireGeometryChanged(int[] items) {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryChanged(items);
            } catch(Exception e) {
                System.out.println("Error sending geometry change message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * fire a geometry removed event to the listeners.
     *
     * @param items The geometry items that have removed or null for all
     */
    protected void fireGeometryRemoved(int[] items) {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryRemoved(items);
            } catch(Exception e) {
                System.out.println("Error sending geometry remove message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
