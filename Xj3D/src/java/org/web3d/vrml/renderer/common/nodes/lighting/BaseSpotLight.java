/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.lighting;

// External imports
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLLightNodeType;
import org.web3d.vrml.renderer.common.nodes.BaseLightNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Common implementation code for a SpotLight node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.13 $
 */
public abstract class BaseSpotLight extends BaseLightNode {

    /** Index of the attentuation field */
    protected static final int FIELD_ATTENUATION = LAST_LIGHT_INDEX + 1;

    /** Index of the beamWidth field */
    protected static final int FIELD_BEAMWIDTH = LAST_LIGHT_INDEX + 2;

    /** Index of the cutOfAngle field */
    protected static final int FIELD_CUTOFFANGLE = LAST_LIGHT_INDEX + 3;

    /** Index of the direction field */
    protected static final int FIELD_DIRECTION = LAST_LIGHT_INDEX + 4;

    /** Index of the location field */
    protected static final int FIELD_LOCATION = LAST_LIGHT_INDEX + 5;

    /** Index of the radius field */
    protected static final int FIELD_RADIUS = LAST_LIGHT_INDEX + 6;

    /** Last value used as an index */
    public static final int LAST_SPOTLIGHT_INDEX = FIELD_RADIUS;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_SPOTLIGHT_INDEX + 1;

    // VRML Field declarations

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;


    /** exposedField SFVec3f attenuation 1 0 0 */
    protected float[] vfAttenuation;

    /** exposedField SFFloat beamWidth 1.570796 */
    protected float vfBeamWidth;

    /** exposedField SFFloat cutOffAngle 0.785398 */
    protected float vfCutOffAngle;

    /** exposedField SFVec3f direction 0 0 -1 */
    protected float[] vfDirection;

    /** exposedField SFVec3f location 0 0 0 */
    protected float[] vfLocation;

    /** exposedField SFFloat radius 100 */
    protected float vfRadius;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_AMBIENT_INTENSITY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "ambientIntensity");
        fieldDecl[FIELD_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "color");
        fieldDecl[FIELD_INTENSITY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "intensity");
        fieldDecl[FIELD_ON] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "on");
        fieldDecl[FIELD_GLOBAL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "global");
        fieldDecl[FIELD_ATTENUATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "attenuation");
        fieldDecl[FIELD_BEAMWIDTH] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "beamWidth");
        fieldDecl[FIELD_CUTOFFANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "cutOffAngle");
        fieldDecl[FIELD_DIRECTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "direction");
        fieldDecl[FIELD_LOCATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "location");
        fieldDecl[FIELD_RADIUS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "radius");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_AMBIENT_INTENSITY);
        fieldMap.put("ambientIntensity", idx);
        fieldMap.put("set_ambientIntensity", idx);
        fieldMap.put("ambientIntensity_changed", idx);

        idx = new Integer(FIELD_ATTENUATION);
        fieldMap.put("attenuation", idx);
        fieldMap.put("set_attenuation", idx);
        fieldMap.put("attenuation_changed", idx);

        idx = new Integer(FIELD_COLOR);
        fieldMap.put("color", idx);
        fieldMap.put("set_color", idx);
        fieldMap.put("color_changed", idx);

        idx = new Integer(FIELD_RADIUS);
        fieldMap.put("radius", idx);
        fieldMap.put("set_radius", idx);
        fieldMap.put("radius_changed", idx);

        idx = new Integer(FIELD_INTENSITY);
        fieldMap.put("intensity", idx);
        fieldMap.put("set_intensity", idx);
        fieldMap.put("intensity_changed", idx);

        idx = new Integer(FIELD_ON);
        fieldMap.put("on", idx);
        fieldMap.put("set_on", idx);
        fieldMap.put("on_changed", idx);

        idx = new Integer(FIELD_GLOBAL);
        fieldMap.put("global", idx);
        fieldMap.put("set_global", idx);
        fieldMap.put("global_changed", idx);

        idx = new Integer(FIELD_LOCATION);
        fieldMap.put("location", idx);
        fieldMap.put("set_location", idx);
        fieldMap.put("location_changed", idx);

        idx = new Integer(FIELD_DIRECTION);
        fieldMap.put("direction", idx);
        fieldMap.put("set_direction", idx);
        fieldMap.put("direction_changed", idx);

        idx = new Integer(FIELD_CUTOFFANGLE);
        fieldMap.put("cutOffAngle", idx);
        fieldMap.put("set_cutOffAngle", idx);
        fieldMap.put("cutOffAngle_changed", idx);

        idx = new Integer(FIELD_BEAMWIDTH);
        fieldMap.put("beamWidth", idx);
        fieldMap.put("set_beamWidth", idx);
        fieldMap.put("beamWidth_changed", idx);
    }

    /**
     * Construct a new default instance of this class.
     */
    protected BaseSpotLight() {
        super("SpotLight");

        vfAttenuation = new float[] {1,0,0};
        vfBeamWidth = 1.570796f;
        vfCutOffAngle = 0.785398f;
        vfDirection = new float[] {0,0,-1};
        vfLocation = new float[] {0,0,0};
        vfRadius = 100f;
		vfGlobal = true;

        hasChanged = new boolean[LAST_SPOTLIGHT_INDEX + 1];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a light node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseSpotLight(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLLightNodeType)node);

        try {
            int index = node.getFieldIndex("attenuation");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfAttenuation[0] = field.floatArrayValue[0];
                vfAttenuation[1] = field.floatArrayValue[1];
                vfAttenuation[2] = field.floatArrayValue[2];
            }

            index = node.getFieldIndex("radius");
            field = node.getFieldValue(index);
            vfRadius = field.floatValue;

            index = node.getFieldIndex("beamWidth");
            field = node.getFieldValue(index);
            vfBeamWidth = field.floatValue;

            index = node.getFieldIndex("cuttOffAngle");
            field = node.getFieldValue(index);
            vfCutOffAngle = field.floatValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
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
        Integer index = (Integer) fieldMap.get(fieldName);

        int ret_val = (index == null) ? -1 : index.intValue();

        // Global field not defined before 3.1
        if((ret_val == FIELD_GLOBAL) &&
           ((vrmlMajorVersion == 2 ||
            (vrmlMajorVersion == 3 && vrmlMinorVersion == 0))))
            ret_val = -1;

        return ret_val;
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
        if (index < 0  || index > LAST_SPOTLIGHT_INDEX)
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
            case FIELD_ATTENUATION:
                fieldData.clear();
                fieldData.floatArrayValue = vfAttenuation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BEAMWIDTH:
                fieldData.clear();
                fieldData.floatValue = vfBeamWidth;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_CUTOFFANGLE:
                fieldData.clear();
                fieldData.floatValue = vfCutOffAngle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_DIRECTION:
                fieldData.clear();
                fieldData.floatArrayValue = vfDirection;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_LOCATION:
                fieldData.clear();
                fieldData.floatArrayValue = vfLocation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_RADIUS:
                fieldData.clear();
                fieldData.floatValue = vfRadius;
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

                case FIELD_LOCATION:
                    destNode.setValue(destIndex, vfLocation, 3);
                    break;

                case FIELD_RADIUS:
                    destNode.setValue(destIndex, vfRadius);
                    break;

                case FIELD_ATTENUATION:
                    destNode.setValue(destIndex, vfAttenuation, 3);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("DirLight sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("DirLight sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BEAMWIDTH:
                setBeamWidth(value);
                break;

            case FIELD_CUTOFFANGLE:
                setCutOffAngle(value);

            case FIELD_RADIUS:
                setRadius(value);
                break;

            default :
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFColor and SFVec3f field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_DIRECTION:
                setDirection(value);
                break;

            case FIELD_LOCATION:
                setLocation(value);
                break;

            case FIELD_ATTENUATION:
                setAttenuation(value);
                break;

            default :
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the direction ofthe spot light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param dir The new direction vector to use
     */
    protected void setDirection(float[] dir) {
        vfDirection[0] = dir[0];
        vfDirection[1] = dir[1];
        vfDirection[2] = dir[2];

        if(!inSetup) {
            hasChanged[FIELD_DIRECTION] = true;
            fireFieldChanged(FIELD_DIRECTION);
        }
    }

    /**
     * Set the location ofthe point light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param loc The new location to use
     */
    protected void setLocation(float[] loc) {
        vfLocation[0] = loc[0];
        vfLocation[1] = loc[1];
        vfLocation[2] = loc[2];

        if(!inSetup) {
            hasChanged[FIELD_LOCATION] = true;
            fireFieldChanged(FIELD_LOCATION);
        }
    }

    /**
     * Set the radius of the light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param radius The new radius to use
     * @throws InvalidFieldValueException Radius value was negative
     */
    protected void setRadius(float radius)
        throws InvalidFieldValueException {

        if(radius <= 0)
            throw new InvalidFieldValueException(
                "Radius is not (0, PI/2) range: " + radius);

        vfRadius = radius;

        if(!inSetup) {
            hasChanged[FIELD_RADIUS] = true;
            fireFieldChanged(FIELD_RADIUS);
        }
    }

    /**
     * Set the attenuation factor of the light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param factor The new attenuation factor to use
     * @throws InvalidFieldValueException Attenuation was not [0,1]
     */
    protected void setAttenuation(float[] factor)
        throws InvalidFieldValueException {

        if((factor[0] < 0) || (factor[1] < 0) || (factor[2] < 0) ||
           (factor[0] > 1) || (factor[1] > 1) || (factor[2] > 1))
            throw new InvalidFieldValueException("attenuation value out of range [0,1]");

        vfAttenuation[0] = factor[0];
        vfAttenuation[1] = factor[1];
        vfAttenuation[2] = factor[2];

        if(!inSetup) {
            hasChanged[FIELD_ATTENUATION] = true;
            fireFieldChanged(FIELD_ATTENUATION);
        }
    }

    /**
     * Set the beamWidth of the light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param value The new width to use in radians
     * @throws InvalidFieldValueException Width was not 0 to PI/2
     */
    protected void setBeamWidth(float value)
        throws InvalidFieldValueException {

        if(value <= 0 || value > Math.PI / 2)
            throw new InvalidFieldValueException(
                "beamWidth size out of range: " + value);

        vfBeamWidth = value;

        if(!inSetup) {
            hasChanged[FIELD_BEAMWIDTH] = true;
            fireFieldChanged(FIELD_BEAMWIDTH);
        }
    }

    /**
     * Set the cutOffAngle of the light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param value The new width to use in radians
     * @throws InvalidFieldValueException Width was not 0 to PI/2
     */
    protected void setCutOffAngle(float value)
        throws InvalidFieldValueException {

        if(value <= 0 || value > Math.PI / 2)
            throw new InvalidFieldValueException(
                "cutOffAngle size out of range: " + value);

        vfCutOffAngle = value;

        if(!inSetup) {
            hasChanged[FIELD_CUTOFFANGLE] = true;
            fireFieldChanged(FIELD_CUTOFFANGLE);
        }
    }
}
