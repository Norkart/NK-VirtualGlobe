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

package org.web3d.vrml.renderer.j3d.nodes.lighting;

// Standard imports
import javax.media.j3d.*;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLLightNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DGlobalStatus;
import org.web3d.vrml.renderer.j3d.nodes.J3DLightNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Java3D implementation of a spotlight.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.15 $
 */
public class J3DSpotLight extends J3DLightNode {

    /** Field Index */
    private static final int FIELD_ATTENUATION = LAST_LIGHT_INDEX + 1;
    private static final int FIELD_BEAMWIDTH = LAST_LIGHT_INDEX + 2;
    private static final int FIELD_CUTOFFANGLE = LAST_LIGHT_INDEX + 3;
    private static final int FIELD_DIRECTION = LAST_LIGHT_INDEX + 4;
    private static final int FIELD_LOCATION = LAST_LIGHT_INDEX + 5;
    private static final int FIELD_RADIUS = LAST_LIGHT_INDEX + 6;

    public static final int LAST_SPOTLIGHT_INDEX = FIELD_RADIUS;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_SPOTLIGHT_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** exposedField SFVec3f attenuation 1 0 0 */
    private Point3f vfAttenuation;

    /** exposedField SFFloat beamWidth 1.570796 */
    private float vfBeamWidth;

    /** exposedField SFFloat cutOffAngle 0.785398 */
    private float vfCutOffAngle;

    /** exposedField SFVec3f direction 0 0 -1 */
    private Vector3f vfDirection;

    /** exposedField SFVec3f location 0 0 0 */
    private Point3f vfLocation;

    /** exposedField SFFloat radius 100 */
    private float vfRadius;

    // Class Constants

    /** bounds for light scope */
    private Bounds bounds;

    /** Used to fetech the direction for passing along to others */
    private float[] flScratch;

    /** The calculated light colour */
    private Color3f lightColor;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

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

        Integer idx = new Integer(FIELD_AMBIENT_INTENSITY);
        fieldMap.put("ambientIntensity", idx);
        fieldMap.put("set_ambientIntensity", idx);
        fieldMap.put("ambientIntensity_changed", idx);

        idx = new Integer(FIELD_COLOR);
        fieldMap.put("color", idx);
        fieldMap.put("set_color", idx);
        fieldMap.put("color_changed", idx);

        idx = new Integer(FIELD_INTENSITY);
        fieldMap.put("intensity", idx);
        fieldMap.put("set_intensity", idx);
        fieldMap.put("intensity_changed", idx);

        idx = new Integer(FIELD_ON);
        fieldMap.put("on", idx);
        fieldMap.put("set_on", idx);
        fieldMap.put("on_changed", idx);

        idx = new Integer(FIELD_LOCATION);
        fieldMap.put("location", idx);
        fieldMap.put("set_location", idx);
        fieldMap.put("location_changed", idx);

        idx = new Integer(FIELD_RADIUS);
        fieldMap.put("radius", idx);
        fieldMap.put("set_radius", idx);
        fieldMap.put("radius_changed", idx);

        idx = new Integer(FIELD_ATTENUATION);
        fieldMap.put("attenuation", idx);
        fieldMap.put("set_attenuation", idx);
        fieldMap.put("attenuation_changed", idx);

        idx = new Integer(FIELD_DIRECTION);
        fieldMap.put("direction", idx);
        fieldMap.put("set_direction", idx);
        fieldMap.put("direction_changed", idx);

        idx = new Integer(FIELD_BEAMWIDTH);
        fieldMap.put("beamWidth", idx);
        fieldMap.put("set_beamWidth", idx);
        fieldMap.put("beamWidth_changed", idx);

        idx = new Integer(FIELD_CUTOFFANGLE);
        fieldMap.put("cutOffAngle", idx);
        fieldMap.put("set_cutOffAngle", idx);
        fieldMap.put("cutOffAngle_changed", idx);
    }

    /**
     * Construct a new default instance of this class.
     */
    public J3DSpotLight() {
        super("SpotLight");

        hasChanged = new boolean[LAST_SPOTLIGHT_INDEX + 1];

        vfDirection = new Vector3f(0, 0, -1);
        vfAttenuation = new Point3f(1, 0, 0);
        vfLocation = new Point3f(0, 0, 0);
        vfRadius = 100f;
        vfBeamWidth = 1.570796f;
        vfCutOffAngle = 0.785398f;

        flScratch = new float[3];
        lightColor = new Color3f();

        lightColor.x = vfColor[0] * vfIntensity;
        lightColor.y = vfColor[1] * vfIntensity;
        lightColor.z = vfColor[2] * vfIntensity;

        implLight = new SpotLight(vfOn,
                                  lightColor,
                                  vfLocation,
                                  vfAttenuation,
                                  vfDirection,
                                  (float)(vfCutOffAngle / Math.PI * 180),
                                  (float) (vfBeamWidth / (Math.PI / 2.0f) * 128.0f));

        bounds = new BoundingSphere(new Point3d(), vfRadius);
        implLight.setInfluencingBounds(bounds);
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a light node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public J3DSpotLight(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLLightNodeType)node);

        try {
            int index = node.getFieldIndex("attenuation");
            VRMLFieldData field = node.getFieldValue(index);
            vfAttenuation.x = field.floatArrayValue[0];
            vfAttenuation.y = field.floatArrayValue[1];
            vfAttenuation.z = field.floatArrayValue[2];

            index = node.getFieldIndex("location");
            field = node.getFieldValue(index);
            vfLocation.x = field.floatArrayValue[0];
            vfLocation.y = field.floatArrayValue[1];
            vfLocation.z = field.floatArrayValue[2];

            index = node.getFieldIndex("direction");
            field = node.getFieldValue(index);
            vfDirection.x = field.floatArrayValue[0];
            vfDirection.y = field.floatArrayValue[1];
            vfDirection.z = field.floatArrayValue[2];

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
    // Methods required by the J3DVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own animation engine.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
        int[] bits;
        int i;
        int size;

        if(capBits != null && capBits.containsKey(SpotLight.class)) {
            bits = (int[])capBits.get(SpotLight.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implLight.clearCapability(bits[i]);
            } else if(!isStatic) {
                // unset the cap bits that would have been set in setVersion()
                implLight.clearCapability(Light.ALLOW_COLOR_WRITE);
                implLight.clearCapability(Light.ALLOW_STATE_WRITE);
                implLight.clearCapability(SpotLight.ALLOW_DIRECTION_WRITE);
            }
        }

        // Now do the same for the frequency bit API
        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null ||
           !freqBits.containsKey(SpotLight.class))
            return;

        bits = (int[])freqBits.get(SpotLight.class);
        size = (bits == null) ? 0 : bits.length;

        if(size != 0) {
            for(i = 0; i < size; i++)
                implLight.clearCapabilityIsFrequent(bits[i]);
        } else if(!isStatic) {
            // unset the cap bits that would have been set in setVersion()
            implLight.clearCapabilityIsFrequent(Light.ALLOW_COLOR_WRITE);
            implLight.clearCapabilityIsFrequent(Light.ALLOW_STATE_WRITE);
            implLight.clearCapabilityIsFrequent(
                SpotLight.ALLOW_DIRECTION_WRITE);
        }
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
        int[] bits;
        int i;
        int size;

        if(capBits != null) {
            bits = (int[])capBits.get(SpotLight.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implLight.setCapability(bits[i]);
            }
        }

        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null)
            return;

        bits = (int[])freqBits.get(SpotLight.class);

        size = (bits == null) ? 0 : bits.length;

        if(size != 0) {
            for(i = 0; i < size; i++)
                implLight.setCapabilityIsFrequent(bits[i]);
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the version of VRML that this node should represent. Different
     * versions have different capabilities, even within the same node.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param isStatic true if this node is under a static group and won't
     *    change after the setup is finished
     */
    public void setVersion(int major, int minor, boolean isStatic) {
        super.setVersion(major, minor, isStatic);

        if(isStatic)
            return;

        implLight.setCapability(Light.ALLOW_COLOR_WRITE);
        implLight.setCapability(Light.ALLOW_STATE_WRITE);
        implLight.setCapability(SpotLight.ALLOW_DIRECTION_WRITE);
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
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer) fieldMap.get(fieldName);

        if (index == null) {
            return -1;
        }
        else {
            return index.intValue();
        }
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
        if (index < 0  || index > LAST_SPOTLIGHT_INDEX) {
            return null;
        }

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
                vfAttenuation.get(flScratch);
                fieldData.clear();
                fieldData.floatArrayValue = flScratch;
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
                vfDirection.get(flScratch);
                fieldData.clear();
                fieldData.floatArrayValue = flScratch;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_LOCATION:
                vfLocation.get(flScratch);
                fieldData.clear();
                fieldData.floatArrayValue = flScratch;
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
                case FIELD_BEAMWIDTH:
                    destNode.setValue(destIndex, vfBeamWidth);
                    break;

                case FIELD_CUTOFFANGLE:
                    destNode.setValue(destIndex, vfCutOffAngle);
                    break;

                case FIELD_DIRECTION:
                    vfDirection.get(flScratch);
                    destNode.setValue(destIndex, flScratch, 3);
                    break;

                case FIELD_LOCATION:
                    vfLocation.get(flScratch);
                    destNode.setValue(destIndex, flScratch, 3);
                    break;

                case FIELD_ATTENUATION:
                    vfAttenuation.get(flScratch);
                    destNode.setValue(destIndex, flScratch, 3);
                    break;

                case FIELD_RADIUS:
                    destNode.setValue(destIndex, vfRadius);
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
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
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
    // Methods internal to J3DSpotLight
    //----------------------------------------------------------

    private void setDirection(float[] newDirection) {
        vfDirection.x = newDirection[0];
        vfDirection.y = newDirection[1];
        vfDirection.z = newDirection[2];

        ((SpotLight)implLight).setDirection(vfDirection);

        if(!inSetup) {
            hasChanged[FIELD_DIRECTION] = true;
            fireFieldChanged(FIELD_DIRECTION);
        }
    }

    private void setLocation(float[] newLocation) {
        vfLocation.x = newLocation[0];
        vfLocation.y = newLocation[1];
        vfLocation.z = newLocation[2];

        ((SpotLight)implLight).setPosition(vfLocation);

        if(!inSetup) {
            hasChanged[FIELD_LOCATION] = true;
            fireFieldChanged(FIELD_LOCATION);
        }
    }

    private void setBeamWidth(float value) {
        if(value <= 0 || value > Math.PI / 2)
            throw new InvalidFieldValueException(
                "beamWidth size out of range: " + value);

        vfBeamWidth = value;

        ((SpotLight)implLight).setConcentration(
            (float) (vfBeamWidth / (Math.PI * 0.5f) * 128.0f));

        if(!inSetup) {
            hasChanged[FIELD_BEAMWIDTH] = true;
            fireFieldChanged(FIELD_BEAMWIDTH);
        }
    }

    private void setRadius(float value) {
        if(value <= 0)
            throw new InvalidFieldValueException(
                "radius out of range: " + value);


        vfRadius = value;
        bounds = new BoundingSphere(new Point3d(), vfRadius);
        implLight.setInfluencingBounds(bounds);

        if(!inSetup) {
            hasChanged[FIELD_RADIUS] = true;
            fireFieldChanged(FIELD_RADIUS);
        }
    }

    private void setAttenuation(float[] value) {
        if((value[0] < 0) || (value[1] < 0) || (value[2] < 0))
            throw new InvalidFieldValueException("attenuation value out of range");

        vfAttenuation.x = value[0];
        vfAttenuation.y = value[1];
        vfAttenuation.z = value[2];
        ((SpotLight)implLight).setAttenuation(vfAttenuation);

        if(!inSetup) {
            hasChanged[FIELD_ATTENUATION] = true;
            fireFieldChanged(FIELD_ATTENUATION);
        }
    }

    private void setCutOffAngle(float value) {
        if(value <= 0 || value > Math.PI / 2)
            throw new InvalidFieldValueException(
                "cutOffAngle size out of range: " + value);

        vfCutOffAngle = value;

        //TODO: Is this right?
        ((SpotLight)implLight).setSpreadAngle(vfCutOffAngle);

        if(!inSetup) {
            hasChanged[FIELD_CUTOFFANGLE] = true;
            fireFieldChanged(FIELD_CUTOFFANGLE);
        }
    }
}
