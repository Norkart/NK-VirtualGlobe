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

package org.web3d.vrml.renderer.common.nodes.particle;

// External imports
import java.util.HashMap;

import org.j3d.geom.particle.WindParticleFunction;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of a WindPhysicsModel node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.3 $
 */
public abstract class BaseWindPhysicsModel extends BasePhysicsModel {

    // Field index constants

    /** The field index for direction */
    protected static final int FIELD_DIRECTION = LAST_PHYSICS_INDEX + 1;

    /** The field index for gustiness */
    protected static final int FIELD_GUSTINESS = LAST_PHYSICS_INDEX + 2;

    /** The field index for turbulence */
    protected static final int FIELD_TURBULENCE = LAST_PHYSICS_INDEX + 3;

    /** The field index for speed */
    protected static final int FIELD_SPEED = LAST_PHYSICS_INDEX + 4;

    /** The last field index used by this class */
    protected static final int LAST_WIND_INDEX = FIELD_SPEED;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_WIND_INDEX + 1;

    /** Message when the turbulence is outside of [0,1] */
    private static final String TURBULENCE_RANGE_ERR =
        "Value of turbulence is outside the acceptable range [0,1]: ";

    /** Message for a negative gustiness value */
    private static final String NEG_GUSTINESS_ERR =
        "The gustiness value being set is negative: ";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // The VRML field values

    /** The value of the cycle time field */
    protected float[] vfDirection;

    /** The value of the gustiness field */
    protected float vfGustiness;

    /** The value of the speed field */
    protected float vfSpeed;

    /** The value of the turbulence field */
    protected float vfTurbulence;

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
        fieldDecl[FIELD_DIRECTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "direction");
        fieldDecl[FIELD_GUSTINESS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "gustiness");
        fieldDecl[FIELD_SPEED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "speed");
        fieldDecl[FIELD_TURBULENCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "turbulence");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_DIRECTION);
        fieldMap.put("direction", idx);
        fieldMap.put("set_direction", idx);
        fieldMap.put("direction_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = new Integer(FIELD_GUSTINESS);
        fieldMap.put("gustiness", idx);
        fieldMap.put("set_gustiness", idx);
        fieldMap.put("gustiness_changed", idx);

        idx = new Integer(FIELD_TURBULENCE);
        fieldMap.put("turbulence", idx);
        fieldMap.put("set_turbulence", idx);
        fieldMap.put("turbulence_changed", idx);

        idx = new Integer(FIELD_SPEED);
        fieldMap.put("speed", idx);
        fieldMap.put("set_speed", idx);
        fieldMap.put("speed_changed", idx);
    }

    /**
     * Construct a new time sensor object
     */
    public BaseWindPhysicsModel() {
        super("WindPhysics");

        hasChanged = new boolean[NUM_FIELDS];

        // Set the default values for the fields
        vfDirection = new float[3];
        vfTurbulence = 0;
        vfSpeed = 0.1f;
        vfGustiness = 0.1f;

        particleFunction = new WindParticleFunction(vfDirection,
                                                    vfSpeed,
                                                    vfGustiness,
                                                    vfTurbulence);
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseWindPhysicsModel(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLParticlePhysicsModelNodeType)node);

        try {
            int index = node.getFieldIndex("direction");
            VRMLFieldData field = node.getFieldValue(index);
            vfDirection[0] = field.floatArrayValue[0];
            vfDirection[1] = field.floatArrayValue[1];
            vfDirection[2] = field.floatArrayValue[2];

            ((WindParticleFunction)particleFunction).setDirection(vfDirection[0],
                                                                  vfDirection[1],
                                                                  vfDirection[2]);

            index = node.getFieldIndex("gustiness");
            field = node.getFieldValue(index);
            vfGustiness = field.floatValue;

            ((WindParticleFunction)particleFunction).setGustiness(vfGustiness);

            index = node.getFieldIndex("speed");
            field = node.getFieldValue(index);
            vfSpeed = field.floatValue;

            ((WindParticleFunction)particleFunction).setSpeed(vfSpeed);

            index = node.getFieldIndex("turbulence");
            field = node.getFieldValue(index);
            vfTurbulence = field.floatValue;

            ((WindParticleFunction)particleFunction).setTurbulence(vfTurbulence);
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer) fieldMap.get(fieldName);

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
        if(index < 0  || index > LAST_WIND_INDEX)
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
            case FIELD_DIRECTION:
                fieldData.clear();
                fieldData.floatArrayValue = vfDirection;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_GUSTINESS:
                fieldData.clear();
                fieldData.floatValue = vfGustiness;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SPEED:
                fieldData.clear();
                fieldData.floatValue = vfSpeed;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_TURBULENCE:
                fieldData.clear();
                fieldData.floatValue = vfTurbulence;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
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
                case FIELD_DIRECTION:
                    destNode.setValue(destIndex, vfDirection, 3);
                    break;

                case FIELD_GUSTINESS:
                    destNode.setValue(destIndex, vfGustiness);
                    break;

                case FIELD_SPEED:
                    destNode.setValue(destIndex, vfSpeed);
                    break;

                case FIELD_TURBULENCE:
                    destNode.setValue(destIndex, vfTurbulence);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field! " + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a float for the
     * SFFloat fields.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_GUSTINESS:
                setGustiness(value);
                break;

            case FIELD_SPEED:
                setSpeed(value);
                break;

            case FIELD_TURBULENCE:
                setTurbulence(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a double for the
     * SFVec3f fields.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_DIRECTION:
                setDirection(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Internal Methods
    //----------------------------------------------------------

    /**
     * Notification that the direction value has been changed.
     *
     * @param value The value to use.
     */
    private void setDirection(float[] value) {
        vfDirection[0] = value[0];
        vfDirection[1] = value[1];
        vfDirection[2] = value[2];

        ((WindParticleFunction)particleFunction).setDirection(vfDirection[0],
                                                              vfDirection[1],
                                                              vfDirection[2]);

        if(!inSetup) {
            hasChanged[FIELD_DIRECTION] = true;
            fireFieldChanged(FIELD_DIRECTION);
        }
    }

    /**
     * Set the speed field value to the new value.
     *
     * @param speed The new position to set
     */
    private void setSpeed(float speed)
        throws InvalidFieldValueException {

        vfSpeed = speed;
        ((WindParticleFunction)particleFunction).setSpeed(vfSpeed);

        if(!inSetup) {
            hasChanged[FIELD_SPEED] = true;
            fireFieldChanged(FIELD_SPEED);
        }
    }

    /**
     * Set the gustiness field value to the new value. If it is out of
     * range the throw an exception.
     *
     * @param value The new amount of gustiness to set
     * @throws InvalidFieldValueException The field is out of range
     */
    private void setGustiness(float value)
        throws InvalidFieldValueException {

        if(value < 0)
            throw new InvalidFieldValueException(NEG_GUSTINESS_ERR + value);

        vfGustiness = value;
        ((WindParticleFunction)particleFunction).setGustiness(vfGustiness);

        if(!inSetup) {
            hasChanged[FIELD_GUSTINESS] = true;
            fireFieldChanged(FIELD_GUSTINESS);
        }
    }

    /**
     * Set the turbulence field value to the new value.
     *
     * @param value The new amount of turbulence to set
     */
    private void setTurbulence(float value)
        throws InvalidFieldValueException {

        if(value < 0 || value > 1)
            throw new InvalidFieldValueException(TURBULENCE_RANGE_ERR + value);

        vfTurbulence = value;
        ((WindParticleFunction)particleFunction).setTurbulence(vfTurbulence);

        if(!inSetup) {
            hasChanged[FIELD_TURBULENCE] = true;
            fireFieldChanged(FIELD_TURBULENCE);
        }
    }
}
