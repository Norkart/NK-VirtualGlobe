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

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLSensorNodeType;
import org.web3d.vrml.nodes.VRMLTouchSensorNodeType;

import org.web3d.vrml.renderer.common.nodes.BaseSensorNode;

/**
 * Common base implementation of a TouchSensor node.
 * <p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.17 $
 */
public class BaseTouchSensor extends BaseSensorNode
    implements VRMLTouchSensorNodeType {

    // Field index constants

    /** The field index for hitNormal_Changed */
    protected static final int FIELD_HITNORMAL_CHANGED = LAST_SENSOR_INDEX + 1;

    /** The field index for hitPoint_Changed */
    protected static final int FIELD_HITPOINT_CHANGED = LAST_SENSOR_INDEX + 2;

    /** The field index for hitTexCoord_Changed */
    protected static final int FIELD_HITTEXCOORD_CHANGED = LAST_SENSOR_INDEX + 3;

    /** The field index for isOver  */
    protected static final int FIELD_IS_OVER = LAST_SENSOR_INDEX + 4;

    /** The field index for touchTime */
    protected static final int FIELD_TOUCH_TIME = LAST_SENSOR_INDEX + 5;

    /** The field index for description */
    protected static final int FIELD_DESCRIPTION = LAST_SENSOR_INDEX + 6;

    /** The last field index used by this class */
    protected static final int LAST_TOUCHSENSOR_INDEX = FIELD_DESCRIPTION;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_TOUCHSENSOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** The value of the hitNormal_changed field */
    protected float[] vfHitNormalChanged;

    /** The value of the hitPoint_changed field*/
    protected float[] vfHitPointChanged;

    /** The value of the hitTexCoordChanged field */
    protected float[] vfHitTexCoordChanged;

    /** The value of the isOver field */
    protected boolean vfIsOver;

    /** The value of the touchTime field */
    protected double vfTouchTime;

    /** The value of the description field */
    protected String vfDescription;

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

        fieldDecl[FIELD_DESCRIPTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "description");

        fieldDecl[FIELD_HITNORMAL_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec3f",
                                     "hitNormal_changed");
        fieldDecl[FIELD_HITPOINT_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec3f",
                                     "hitPoint_changed");
        fieldDecl[FIELD_HITTEXCOORD_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec2f",
                                     "hitTexCoord_changed");
        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");
        fieldDecl[FIELD_IS_OVER] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isOver");
        fieldDecl[FIELD_TOUCH_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "touchTime");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_DESCRIPTION);
        fieldMap.put("description", idx);
        fieldMap.put("set_description", idx);
        fieldMap.put("description_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        fieldMap.put("hitNormal_changed",new Integer(FIELD_HITNORMAL_CHANGED));
        fieldMap.put("hitPoint_changed",new Integer(FIELD_HITPOINT_CHANGED));
        fieldMap.put("hitTexCoord_changed",
            new Integer(FIELD_HITTEXCOORD_CHANGED));

        fieldMap.put("isActive",new Integer(FIELD_IS_ACTIVE));
        fieldMap.put("isOver",new Integer(FIELD_IS_OVER));
        fieldMap.put("touchTime",new Integer(FIELD_TOUCH_TIME));
    }

    /**
     * Construct a new time sensor object
     */
    public BaseTouchSensor() {
        super("TouchSensor");

        hasChanged = new boolean[NUM_FIELDS];

        vfHitNormalChanged = new float[3];
        vfHitPointChanged = new float[3];
        vfHitTexCoordChanged = new float[2];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseTouchSensor(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLSensorNodeType)node);
    }

    //--------------------------------------------------------------------
    // Methods defined by VRMLPointingDeviceSensorNodeType
    //--------------------------------------------------------------------

    /**
     * Flag to notify the user whether the node implementation only needs the
     * hit point information, or it needs everything else as well. This is an
     * optimisation method that allows the internals of the event model to
     * avoid doing unnecessary work. If the return value is true, then the
     * hitNormal and hitTexCoord parameter values will not be supplied (they'll
     * be null references).
     *
     * @return true if the node implementation only requires hitPoint information
     */
    public boolean requiresPointOnly() {
        return false;
    }

    /**
     * Get the description to associate with the link. This is a line of text
     * suitable for mouseovers, status information etc. If there is no
     * description set then it returns null.
     *
     * @return The current description or null
     */
    public String getDescription() {
        return vfDescription;
    }

    /**
     * Set the description string for this link. Setting a value of null will
     * clear the current description.
     *
     * @param desc The new description to set
     */
    public void setDescription(String desc) {
        vfDescription = desc;

        if(!inSetup) {
            hasChanged[FIELD_DESCRIPTION] = true;
            fireFieldChanged(FIELD_DESCRIPTION);
        }
    }

    /**
     * Set the flag describing whether the pointing device is over this sensor.
     * The result should be that isOver SFBool output only field is set
     * appropriately at the node level.
     *
     * @param newIsOver The new value for isOver
     */
    public void setIsOver(boolean newIsOver) {
        vfIsOver = newIsOver;
        hasChanged[FIELD_IS_OVER] = true;
        fireFieldChanged(FIELD_IS_OVER);
    }

    /**
     * Get the current value of the isOver field.
     *
     * @return The current value of isOver
     */
    public boolean getIsOver() {
        return vfIsOver;
    }

    //--------------------------------------------------------------------
    // Methods defined by VRMLTouchSensorNodeType
    //--------------------------------------------------------------------

    /**
     * Notify the node that a button was pushed down
     *
     * @param button The button that was pressed
     * @param simTime The VRML simulation time it happened
     * @param hitPoint The location clicked in object space coordinates
     * @param hitNormal Surface normal vector at the intersection point
     * @param hitTexCoord The texture coordinate at the intersection point
     */
    public void notifyPressed(int button,
                              double simTime,
                              float[] hitPoint,
                              float[] hitNormal,
                              float[] hitTexCoord) {

        if (!vfEnabled)
            return;

        vfIsActive = true;
        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);
    }

    /**
     * Notify the node that a button was released
     *
     * @param button The button that was released
     * @param simTime The VRML simulation time it happened
     * @param hitPoint The location clicked in object space coordinates
     * @param hitNormal Surface normal vector at the intersection point
     * @param hitTexCoord The texture coordinate at the intersection point
     */
    public void notifyReleased(int button,
                               double simTime,
                               float[] hitPoint,
                               float[] hitNormal,
                               float[] hitTexCoord) {

        if (!vfEnabled)
            return;

        vfIsActive = false;
        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);

        if (vfIsOver) {
            vfTouchTime = simTime;
            hasChanged[FIELD_TOUCH_TIME] = true;
            fireFieldChanged(FIELD_TOUCH_TIME);
        }
    }

    /**
     * Notify the node that the device moved.
     *
     * @param hitPoint The current location in object space coordinates
     * @param hitNormal Surface normal vector at the intersection point
     * @param hitTexCoord The texture coordinate at the intersection point
     */
    public void notifyHitChanged(float[] hitPoint,
                                 float[] hitNormal,
                                 float[] hitTexCoord) {
        if (!vfEnabled)
            return;

        vfHitPointChanged[0] = hitPoint[0];
        vfHitPointChanged[1] = hitPoint[1];
        vfHitPointChanged[2] = hitPoint[2];

        vfHitNormalChanged[0] = hitNormal[0];
        vfHitNormalChanged[1] = hitNormal[1];
        vfHitNormalChanged[2] = hitNormal[2];

        vfHitTexCoordChanged[0] = hitTexCoord[0];
        vfHitTexCoordChanged[1] = hitTexCoord[1];

        hasChanged[FIELD_HITPOINT_CHANGED] = true;
        fireFieldChanged(FIELD_HITPOINT_CHANGED);

        hasChanged[FIELD_HITNORMAL_CHANGED] = true;
        fireFieldChanged(FIELD_HITNORMAL_CHANGED);

        hasChanged[FIELD_HITTEXCOORD_CHANGED] = true;
        fireFieldChanged(FIELD_HITTEXCOORD_CHANGED);
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
        if(index < 0  || index > LAST_TOUCHSENSOR_INDEX)
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.PointingDeviceSensorNodeType;
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
            case FIELD_HITNORMAL_CHANGED:
                fieldData.clear();
                fieldData.floatArrayValue = vfHitNormalChanged;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_HITPOINT_CHANGED:
                fieldData.clear();
                fieldData.floatArrayValue = vfHitPointChanged;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_HITTEXCOORD_CHANGED:
                fieldData.clear();
                fieldData.floatArrayValue = vfHitTexCoordChanged;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_IS_OVER:
                fieldData.clear();
                fieldData.booleanValue = vfIsOver;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_TOUCH_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfTouchTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_DESCRIPTION:
                fieldData.clear();
                fieldData.stringValue = vfDescription;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
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
                case FIELD_HITNORMAL_CHANGED:
                    destNode.setValue(destIndex, vfHitNormalChanged, 3);
                    break;

                case FIELD_HITPOINT_CHANGED:
                    destNode.setValue(destIndex, vfHitPointChanged, 3);
                    break;

                case FIELD_HITTEXCOORD_CHANGED:
                    destNode.setValue(destIndex, vfHitTexCoordChanged, 2);
                    break;

                case FIELD_IS_OVER:
                    destNode.setValue(destIndex, vfIsOver);
                    break;

                case FIELD_TOUCH_TIME:
                    destNode.setValue(destIndex, vfTouchTime);
                    break;

                case FIELD_DESCRIPTION:
                    destNode.setValue(destIndex, vfDescription);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseTouchSensor.sendRoute: No field! " + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseTouchSensor.sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFString field "title".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_DESCRIPTION:
                setDescription(value);
                break;

            default :
                super.setValue(index, value);
        }
    }
}
