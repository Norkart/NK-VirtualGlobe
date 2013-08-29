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

package org.web3d.vrml.renderer.common.nodes;

// External imports
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.util.FieldValidator;

/**
 * Abstract common implementation of a light node.
 * <p>
 *
 * The X3DLightNode is defined as:
 * <pre>
 * X3DLightNode : X3DChildNode {
 *   SFFloat [in,out] ambientIntensity 0     [0,1]
 *   SFColor [in,out] color            1 1 1 [0,1]
 *   SFBool  [in,out] global           TRUE
 *   SFFloat [in,out] intensity        1     [0,1]
 *   SFNode  [in,out] metadata         NULL  [X3DMetadataObject]
 *   SFBool  [in,out] on               TRUE
 * }
 * </pre>
 *
 * The <code>global</code> field is only available from 3.1 onwards.
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public abstract class BaseLightNode extends AbstractNode
    implements VRMLLightNodeType {

    /** ambientIntensity field fndex */
    protected static final int FIELD_AMBIENT_INTENSITY = LAST_NODE_INDEX + 1;

    /** color field index */
    protected static final int FIELD_COLOR = LAST_NODE_INDEX + 2;

    /** intensity field index */
    protected static final int FIELD_INTENSITY = LAST_NODE_INDEX + 3;

    /** on field index */
    protected static final int FIELD_ON = LAST_NODE_INDEX + 4;

    /** global light field index */
    protected static final int FIELD_GLOBAL = LAST_NODE_INDEX + 5;

    /** Value of the last index in use */
    protected static final int LAST_LIGHT_INDEX = FIELD_GLOBAL;

    /**
     * Error message if the global field is being set on an older spec version
     * of this node.
     */
    protected static final String GLOBAL_VERSION_ERR =
        "The global field does not exist for instances of a light node " +
        "before X3D 3.1";


    // VRML Field declarations

    /** exposedField SFFloat ambientIntensity 0 */
    protected float vfAmbientIntensity;

    /** exposedField SFColor color 1 1 1 */
    protected float[] vfColor;

    /** exposedField SFFloat intensity 1 */
    protected float vfIntensity;

    /** exposedField SFBool on TRUE */
    protected boolean vfOn;

    /** exposedField SFBool global FALSE */
    protected boolean vfGlobal;

    /**
     * Construct a new default instance of this class.
     *
     * @param name The name of the type of node
     */
    protected BaseLightNode(String name) {
        super(name);

        hasChanged = new boolean[LAST_LIGHT_INDEX + 1];

        vfAmbientIntensity = 0.0f;
        vfColor = new float[] {1.0f, 1.0f, 1.0f};
        vfIntensity = 1.0f;
        vfOn = true;

        // This should be modified by the derived node type
        vfGlobal = false;
    }

    /**
     * Set the fields of the light node that has the fields set
     * based on the fields of the passed in node.
     *
     * @param node The light node to copy info from
     */
    protected void copy(VRMLLightNodeType node) {
        vfAmbientIntensity = node.getAmbientIntensity();

        float[] field = node.getColor();
        vfColor[0] = field[0];
        vfColor[1] = field[1];
        vfColor[2] = field[2];

        vfIntensity = node.getIntensity();
        vfOn = node.getOn();
        vfGlobal = node.getGlobal();
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLLightNodeType
    //-------------------------------------------------------------

    /**
     * Get the current value of field ambientIntensity. Default value is 0.
     *
     * @return The current value of ambientIntensity
     */
    public float getAmbientIntensity() {
        return vfAmbientIntensity;
    }

    /**
     * Get the current value of field color. Default value is 1 1 1
     *
     * @return The current value of color
     */
    public float[] getColor() {
        float ret[] = new float[3];

        ret[0] = vfColor[0];
        ret[1] = vfColor[1];
        ret[2] = vfColor[2];

        return ret;
    }

    /**
     * Get the current value of field Intensity. Default value is 1.
     *
     * @return the current value of Intensity
     */
    public float getIntensity() {
        return vfIntensity;
    }

    /**
     * Get the current value of field On. Default value is true.
     *
     * @return the current value of On
     */
    public boolean getOn() {
        return vfOn;
    }

    /**
     * Accessor method to get current value of field global. Default value is
     * false if the node is from 3.1 onwards. The field does not exist in the
     * 3.0 or 2.0 specifications, so we return the default for the particular
     * light type and its spec-defined behaviour.
     *
     * @return the current value of the global field
     */
    public boolean getGlobal() {
        return vfGlobal;
    }

    /**
     * Set the new value of the ambientIntensity field.
     *
     * @param newAmbientIntensity A value between 0 and 1
     * @throws InvalidFieldValueException The value was out of the valid range
     */
    public void setAmbientIntensity(float newAmbientIntensity)
        throws InvalidFieldValueException {

        FieldValidator.checkFloat("Light.ambientIntensity",
                                  newAmbientIntensity);

        vfAmbientIntensity = newAmbientIntensity;

        if(!inSetup) {
            hasChanged[FIELD_AMBIENT_INTENSITY] = true;
            fireFieldChanged(FIELD_AMBIENT_INTENSITY);
        }
    }

    /**
     * Set the new value of the color field.
     *
     * @param newColor The new value. Each component must be between 0 and 1
     * @throws InvalidFieldValueException The value was out of the valid range
     */
    public void setColor(float[] newColor)
        throws InvalidFieldValueException {

        FieldValidator.checkColorVector("Light.setColor",newColor);
        vfColor[0] = newColor[0];
        vfColor[1] = newColor[1];
        vfColor[2] = newColor[2];

        if(!inSetup) {
            hasChanged[FIELD_COLOR] = true;
            fireFieldChanged(FIELD_COLOR);
        }
    }

    /**
     * Get the current value of field Intensity
     *
     * @param newIntensity A value between 0 and 1
     * @throws InvalidFieldValueException The value was out of the valid range
     */
    public void setIntensity(float newIntensity)
        throws InvalidFieldValueException {

        FieldValidator.checkFloat("Light.intensity",newIntensity);
        vfIntensity = newIntensity;

        if(!inSetup) {
            hasChanged[FIELD_INTENSITY] = true;
            fireFieldChanged(FIELD_INTENSITY);
        }
    }

    /**
     * Set the value of field On.
     *
     * @param newOn true will turn the light on, false to turn it off
     */
    public void setOn(boolean newOn) {

        vfOn = newOn;

        if(!inSetup) {
            hasChanged[FIELD_ON] = true;
            fireFieldChanged(FIELD_ON);
        }
    }

    /**
     * Set the current value of the global field.
     *
     * @param global true if this should have global effect, false for scoped
     * @throws InvalidFieldException Called on a node that belongs to VRML or
     *    X3D 3.0.
     */
    public void setGlobal(boolean global)
        throws InvalidFieldException {

        if(vrmlMajorVersion == 2 ||
           (vrmlMajorVersion == 3 && vrmlMinorVersion == 0))
            throw new InvalidFieldException(GLOBAL_VERSION_ERR);

        vfGlobal = global;

        if(!inSetup) {
            hasChanged[FIELD_GLOBAL] = true;
            fireFieldChanged(FIELD_GLOBAL);
        }
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.LightNodeType;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

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
            case FIELD_AMBIENT_INTENSITY:
                fieldData.clear();
                fieldData.floatValue = vfAmbientIntensity;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_ON:
                fieldData.clear();
                fieldData.booleanValue = vfOn;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_GLOBAL:
                fieldData.clear();
                fieldData.booleanValue = vfGlobal;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_INTENSITY:
                fieldData.clear();
                fieldData.floatValue = vfIntensity;
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
                case FIELD_AMBIENT_INTENSITY :
                    destNode.setValue(destIndex, vfAmbientIntensity);
                    break;

                case FIELD_COLOR :
                    destNode.setValue(destIndex, vfColor, 3);
                    break;

                case FIELD_INTENSITY :
                    destNode.setValue(destIndex, vfIntensity);
                    break;

                case FIELD_ON:
                    destNode.setValue(destIndex, vfOn);
                    break;

                case FIELD_GLOBAL:
                    destNode.setValue(destIndex, vfGlobal);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("Light sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("Light sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException {

        switch (index) {
            case FIELD_ON:
                setOn(value);
                break;

            case FIELD_GLOBAL:
                setGlobal(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_AMBIENT_INTENSITY:
                setAmbientIntensity(value);
                break;

            case FIELD_INTENSITY:
                setIntensity(value);
                break;

            default:
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
     * @throws InvalidFieldException The field index is not known
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_COLOR:
                setColor(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }
}
