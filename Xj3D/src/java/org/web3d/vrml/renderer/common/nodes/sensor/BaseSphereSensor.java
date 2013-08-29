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

package org.web3d.vrml.renderer.common.nodes.sensor;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLDragSensorNodeType;

import org.web3d.vrml.renderer.common.nodes.BaseDragSensorNode;

/**
 * Java3D implementation of a SphereSensor node.
 * <p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.12 $
 */
public abstract class BaseSphereSensor extends BaseDragSensorNode {

    // Field index constants

    /** The field index for translation_changed */
    protected static final int FIELD_ROTATION_CHANGED = LAST_DRAG_SENSOR_INDEX + 3;

    /** The field index for offset */
    protected static final int FIELD_OFFSET = LAST_DRAG_SENSOR_INDEX + 4;

    /** The last field index used by this class */
    protected static final int LAST_PLANESENSOR_INDEX = FIELD_OFFSET;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_PLANESENSOR_INDEX + 1;

    /** Empty origin used for sphere center representation */
    private static final float[] ORIGIN;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** The value of the offset field */
    protected float[] vfOffset;

    /** The value of the rotation_changed field*/
    protected float[] vfRotationChanged;

    /** Radius to use for this sphere during the drag */
    private float dragRadius;

    /** Normalised version of the vector to the initial position */
    private float[] initialNormal;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        ORIGIN = new float[3];

        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_AUTOOFFSET] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "autoOffset");
        fieldDecl[FIELD_TRACKPOINT_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec3f",
                                     "trackPoint_changed");
        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");
        fieldDecl[FIELD_IS_OVER] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isOver");
        fieldDecl[FIELD_ROTATION_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFRotation",
                                     "rotation_changed");
        fieldDecl[FIELD_OFFSET] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFRotation",
                                     "offset");
        fieldDecl[FIELD_DESCRIPTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "description");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        fieldMap.put("isActive",new Integer(FIELD_IS_ACTIVE));
        fieldMap.put("isOver",new Integer(FIELD_IS_OVER));
        fieldMap.put("trackPoint_changed",new Integer(FIELD_TRACKPOINT_CHANGED));

        idx = new Integer(FIELD_AUTOOFFSET);
        fieldMap.put("autoOffset", idx);
        fieldMap.put("set_autoOffset", idx);
        fieldMap.put("autoOffset_changed", idx);

        idx = new Integer(FIELD_OFFSET);
        fieldMap.put("offset", idx);
        fieldMap.put("set_offset", idx);
        fieldMap.put("offset_changed", idx);

        idx = new Integer(FIELD_DESCRIPTION);
        fieldMap.put("description", idx);
        fieldMap.put("set_description", idx);
        fieldMap.put("description_changed", idx);


        fieldMap.put("rotation_changed",new Integer(FIELD_ROTATION_CHANGED));
    }

    /**
     * Construct a new time sensor object
     */
    public BaseSphereSensor() {
        super("SphereSensor");

        hasChanged = new boolean[NUM_FIELDS];

        vfOffset = new float[4];
        vfRotationChanged = new float[4];
        initialNormal = new float[3];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseSphereSensor(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLDragSensorNodeType)node);

        try {
            int index = node.getFieldIndex("offset");
            VRMLFieldData field = node.getFieldValue(index);

            vfOffset[0] = field.floatArrayValue[0];
            vfOffset[1] = field.floatArrayValue[1];
            vfOffset[2] = field.floatArrayValue[2];
            vfOffset[3] = field.floatArrayValue[3];

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //--------------------------------------------------------------------
    // Methods defined by VRMLPointingDeviceSensorNodeType
    //--------------------------------------------------------------------

    /**
     * Notification that this sensor has just been clicked on to start a drag
     * action.
     *
     * @param hitPoint Where the input device intersected the object sensor
     * @param position Where the sensor origin is in local coordinates
     */
    public void notifySensorDragStart(float[] hitPoint, float[] location) {

        super.notifySensorDragStart(hitPoint, location);

        dragRadius = (float)Math.sqrt(hitPoint[0] * hitPoint[0] +
                                      hitPoint[1] * hitPoint[1] +
                                      hitPoint[2] * hitPoint[2]);

        initialNormal[0] = hitPoint[0] / dragRadius;
        initialNormal[1] = hitPoint[1] / dragRadius;
        initialNormal[2] = hitPoint[2] / dragRadius;
    }

    /**
     * Notification that this sensor has finished a drag action.
     *
     * @param position Where the sensor origin is in local coordinates
     * @param direction Vector showing the direction the sensor is pointing
     */
    public void notifySensorDragEnd(float[] position, float[] direction) {

        super.notifySensorDragEnd(position, direction);

        if(vfAutoOffset) {
            vfOffset[0] = vfRotationChanged[0];
            vfOffset[1] = vfRotationChanged[1];
            vfOffset[2] = vfRotationChanged[2];
            vfOffset[3] = vfRotationChanged[3];

            hasChanged[FIELD_OFFSET] = true;
            fireFieldChanged(FIELD_OFFSET);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer)fieldMap.get(fieldName);

        return (index == null) ? -1 : index.intValue();
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_PLANESENSOR_INDEX)
            return null;

        return fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @param The number of fields.
     */
    public int getNumFields() {
        return fieldDecl.length;
    }

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_ROTATION_CHANGED:
                fieldData.clear();
                fieldData.floatArrayValue = vfRotationChanged;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_OFFSET:
                fieldData.clear();
                fieldData.floatArrayValue = vfOffset;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_ROTATION_CHANGED:
                    destNode.setValue(destIndex, vfRotationChanged, 4);
                    break;

                case FIELD_OFFSET:
                    destNode.setValue(destIndex, vfOffset, 4);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseSphereSensor.sendRoute: No field! " + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseSphereSensor.sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }
    /**
     * Set the value of the field at the given index as a float array. This is
     * be used to set SFVec2f and SFVec3f field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_OFFSET:
                vfOffset[0] = value[0];
                vfOffset[1] = value[1];
                vfOffset[2] = value[2];
                vfOffset[3] = value[3];
                break;

            default:
                super.setValue(index, value, numValid);
        }

        if(!inSetup) {
            hasChanged[index] = true;
            fireFieldChanged(index);
        }
    }

    /**
     * Convenience method to generate the tracking output based on
     * the input hit position.
     *
     * @param location The position of the sensor locally
     * @param direction Vector showing the direction the sensor is pointing
     */
    protected void processDrag(float[] location, float[] direction) {

        // Intersect the ray with the geometry to work out where the object
        // virtual geometry has been hit. If the geometry has not been hit
        // then don't generate an event. Just leave it as the last one.
        if(!intersectionUtils.raySphere(location,
                                        direction,
                                        ORIGIN,
                                        dragRadius,
                                        wkPoint))
            return;


        // Calculate the normalised form of the position
        float dist = (float)Math.sqrt(wkPoint[0] * wkPoint[0] +
                                      wkPoint[1] * wkPoint[1] +
                                      wkPoint[2] * wkPoint[2]);

        float x = 0;
        float y = 0;
        float z = 0;

        if(dist != 0) {
            x = wkPoint[0] / dist;
            y = wkPoint[1] / dist;
            z = wkPoint[2] / dist;
        }

        // To calculate the trackPoint, normalise the vector to turn it into a
        // unit sphere, then multiply by the initial radius calculated to get the
        // position on the sphere.
        vfTrackPointChanged[0] = x;
        vfTrackPointChanged[1] = y;
        vfTrackPointChanged[2] = z;

        hasChanged[FIELD_TRACKPOINT_CHANGED] = true;
        fireFieldChanged(FIELD_TRACKPOINT_CHANGED);

        // The rotation direction is a cross product of the initial position
        // and the current position. The angle is then derived from the angle
        // between the two vectors.
        float x1 = initialPosition[0];
        float y1 = initialPosition[1];
        float z1 = initialPosition[2];

        float x2 = wkPoint[0];
        float y2 = wkPoint[1];
        float z2 = wkPoint[2];

        float cross_x = y1 * z2 - z1 * y2;
        float cross_y = z1 * x2 - x1 * z2;
        float cross_z = x1 * y2 - y1 * x2;

        // Angle is  cos(theta) = (A / |A|) . (B / |B|)
        // A is treated as the inital normal. B is x,y,z calculated above.
        double dot = initialNormal[0] * x +
                     initialNormal[1] * y +
                     initialNormal[2] * z;

        float angle = (float)Math.acos(dot);

        vfRotationChanged[0] = cross_x;
        vfRotationChanged[1] = cross_y;
        vfRotationChanged[2] = cross_z;
        vfRotationChanged[3] = angle;

        hasChanged[FIELD_ROTATION_CHANGED] = true;
        fireFieldChanged(FIELD_ROTATION_CHANGED);
    }
}
