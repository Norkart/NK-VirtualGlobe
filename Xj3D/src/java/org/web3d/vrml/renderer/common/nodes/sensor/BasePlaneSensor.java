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

package org.web3d.vrml.renderer.common.nodes.sensor;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLDragSensorNodeType;

import org.web3d.vrml.renderer.common.nodes.BaseDragSensorNode;

/**
 * Java3D implementation of a PlaneSensor node.
 * <p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.14 $
 */
public abstract class BasePlaneSensor extends BaseDragSensorNode {

    // Field index constants

    /** The field index for maxPosition */
    protected static final int FIELD_MAXPOSITION = LAST_DRAG_SENSOR_INDEX + 1;

    /** The field index for minPosition */
    protected static final int FIELD_MINPOSITION = LAST_DRAG_SENSOR_INDEX + 2;

    /** The field index for translation_changed */
    protected static final int FIELD_TRANSLATION_CHANGED = LAST_DRAG_SENSOR_INDEX + 3;

    /** The field index for offset */
    protected static final int FIELD_OFFSET = LAST_DRAG_SENSOR_INDEX + 4;

    /** The last field index used by this class */
    protected static final int LAST_PLANESENSOR_INDEX = FIELD_OFFSET;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_PLANESENSOR_INDEX + 1;

    /**
     * Empty origin used for plane equation representation. Plane lies in the
     * X-Y axis with no Z offset, so the value is set to 0,0,1,0 for the
     * plane equation (ax + by + cz + d = 0).  The initial position is used
     * to get the zoffset.
     */
    private float[] plane_normal;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** The value of the maxPosition field */
    protected float[] vfMaxPosition;

    /** The value of the minPosition field*/
    protected float[] vfMinPosition;

    /** The value of the offset field */
    protected float[] vfOffset;

    /** The value of the translation_changed field*/
    protected float[] vfTranslationChanged;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
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

        fieldDecl[FIELD_TRANSLATION_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec3f",
                                     "translation_changed");


        fieldDecl[FIELD_MINPOSITION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "minPosition");

        fieldDecl[FIELD_MAXPOSITION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "maxPosition");

        fieldDecl[FIELD_OFFSET] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
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

        idx = new Integer(FIELD_DESCRIPTION);
        fieldMap.put("description", idx);
        fieldMap.put("set_description", idx);
        fieldMap.put("description_changed", idx);

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

        idx = new Integer(FIELD_MAXPOSITION);
        fieldMap.put("maxPosition", idx);
        fieldMap.put("set_maxPosition", idx);
        fieldMap.put("maxPosition_changed", idx);

        idx = new Integer(FIELD_MINPOSITION);
        fieldMap.put("minPosition", idx);
        fieldMap.put("set_minPosition", idx);
        fieldMap.put("minPosition_changed", idx);

        fieldMap.put("translation_changed",new Integer(FIELD_TRANSLATION_CHANGED));
    }

    /**
     * Construct a new time sensor object
     */
    public BasePlaneSensor() {
        super("PlaneSensor");

        hasChanged = new boolean[NUM_FIELDS];

        vfOffset = new float[3];
        vfTranslationChanged = new float[3];
        vfMinPosition = new float[2];
        vfMaxPosition = new float[] { -1, -1 };

        plane_normal = new float[4];
        plane_normal[2] = 1;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BasePlaneSensor(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLDragSensorNodeType)node);

        try {
            int index = node.getFieldIndex("offset");
            VRMLFieldData field = node.getFieldValue(index);

            vfOffset[0] = field.floatArrayValue[0];
            vfOffset[1] = field.floatArrayValue[1];
            vfOffset[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("minPosition");
            field = node.getFieldValue(index);

            vfMinPosition[0] = field.floatArrayValue[0];
            vfMinPosition[1] = field.floatArrayValue[1];

            index = node.getFieldIndex("maxPosition");
            field = node.getFieldValue(index);

            vfMaxPosition[0] = field.floatArrayValue[0];
            vfMaxPosition[1] = field.floatArrayValue[1];
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //--------------------------------------------------------------------
    // Methods defined by VRMLPointingDeviceSensorNodeType
    //--------------------------------------------------------------------

    /**
     * Notification that this sensor has finished a drag action.
     *
     * @param position Where the sensor origin is in local coordinates
     * @param direction Vector showing the direction the sensor is pointing
     */
    public void notifySensorDragEnd(float[] position, float[] direction) {

        super.notifySensorDragEnd(position, direction);

        if(vfAutoOffset) {
            vfOffset[0] = vfTranslationChanged[0];
            vfOffset[1] = vfTranslationChanged[1];

            // vfOffset[2] not set. Always 0

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
            case FIELD_TRANSLATION_CHANGED:
                fieldData.clear();
                fieldData.floatArrayValue = vfTranslationChanged;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_MAXPOSITION:
                fieldData.clear();
                fieldData.floatArrayValue = vfMaxPosition;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_MINPOSITION:
                fieldData.clear();
                fieldData.floatArrayValue = vfMinPosition;
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
                case FIELD_TRANSLATION_CHANGED:
                    destNode.setValue(destIndex, vfTranslationChanged, 3);
                    break;

                case FIELD_MAXPOSITION:
                    destNode.setValue(destIndex, vfMaxPosition, 2);
                    break;

                case FIELD_MINPOSITION:
                    destNode.setValue(destIndex, vfMinPosition, 2);
                    break;

                case FIELD_OFFSET:
                    destNode.setValue(destIndex, vfOffset, 3);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BasePlaneSensor.sendRoute: No field! " + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BasePlaneSensor.sendRoute: Invalid field value: " +
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
            case FIELD_MINPOSITION:
                vfMinPosition[0] = value[0];
                vfMinPosition[1] = value[1];

                if(!inSetup) {
                    hasChanged[index] = true;
                    fireFieldChanged(index);
                }
                break;

            case FIELD_MAXPOSITION:
                vfMaxPosition[0] = value[0];
                vfMaxPosition[1] = value[1];

                if(!inSetup) {
                    hasChanged[index] = true;
                    fireFieldChanged(index);
                }
                break;

            case FIELD_OFFSET:
                vfOffset[0] = value[0];
                vfOffset[1] = value[1];
                vfOffset[2] = value[2];

                if(!inSetup) {
                    hasChanged[index] = true;
                    fireFieldChanged(index);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Convenience method to generate the tracking output based on
     * the input hit position.
     *
     * @param location The position of the mouse locally
     * @param direction Vector showing the direction the sensor is pointing
     */
    protected void processDrag(float[] location, float[] direction) {

        // Intersect the ray with the geometry to work out where the object
        // virtual geometry has been hit. If the geometry has not been hit
        // then don't generate an event. Just leave it as the last one.

        plane_normal[3] = -initialPosition[2];

        if(!intersectionUtils.rayPlane(location,
                                       direction,
                                       plane_normal,
                                       wkPoint))
            return;

        float x = wkPoint[0] - initialPosition[0] + vfOffset[0];
        float y = wkPoint[1] - initialPosition[1] + vfOffset[1];

        float c_x = x;
        float c_y = y;

        // See if we need to clamp. One component at a time
        if(vfMaxPosition[0] >= vfMinPosition[0]) {
            if(x >= vfMaxPosition[0])
                c_x = vfMaxPosition[0];

            if(x < vfMinPosition[0])
                c_x = vfMinPosition[0];
        }

        if(vfMaxPosition[1] >= vfMinPosition[1]) {
            if(y >= vfMaxPosition[1])
                c_y = vfMaxPosition[1];

            if(y < vfMinPosition[1])
                c_y = vfMinPosition[1];
        }

        vfTranslationChanged[0] = c_x;
        vfTranslationChanged[1] = c_y;
        vfTranslationChanged[2] = vfOffset[2];

        hasChanged[FIELD_TRANSLATION_CHANGED] = true;
        fireFieldChanged(FIELD_TRANSLATION_CHANGED);


        // trackPoint_changed events represent the unclamped intersection
        // points on the surface of the tracking plane.
        vfTrackPointChanged[0] = x;
        vfTrackPointChanged[1] = y;

        hasChanged[FIELD_TRACKPOINT_CHANGED] = true;
        fireFieldChanged(FIELD_TRACKPOINT_CHANGED);
    }
}
