/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.shape;

// External imports
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Common base implementation of a two-sided material extension node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public abstract class BaseTwoSidedMaterial extends AbstractNode
    implements VRMLMaterialNodeType {

    /** Field index for ambientIntensity */
    protected static final int FIELD_AMBIENT_INTENSITY = LAST_NODE_INDEX + 1;

    /** Field index for backAmbientIntensity */
    protected static final int FIELD_BACK_AMBIENT_INTENSITY = LAST_NODE_INDEX + 2;

    /** Field index for diffuseColor */
    protected static final int FIELD_DIFFUSE_COLOR = LAST_NODE_INDEX + 3;

    /** Field index for backDiffuseColor */
    protected static final int FIELD_BACK_DIFFUSE_COLOR = LAST_NODE_INDEX + 4;

    /** Field index for emissiveColor */
    protected static final int FIELD_EMISSIVE_COLOR = LAST_NODE_INDEX + 5;

    /** Field index for backEmissiveColor */
    protected static final int FIELD_BACK_EMISSIVE_COLOR = LAST_NODE_INDEX + 6;

    /** Field index for shininess */
    protected static final int FIELD_SHININESS = LAST_NODE_INDEX + 7;

    /** Field index for backShininess */
    protected static final int FIELD_BACK_SHININESS = LAST_NODE_INDEX + 8;

    /** Field index for specularColor */
    protected static final int FIELD_SPECULAR_COLOR = LAST_NODE_INDEX + 9;

    /** Field index for backSpecularColor */
    protected static final int FIELD_BACK_SPECULAR_COLOR = LAST_NODE_INDEX + 10;

    /** Field index for transparency */
    protected static final int FIELD_TRANSPARENCY = LAST_NODE_INDEX + 11;

    /** Field index for backTransparency */
    protected static final int FIELD_BACK_TRANSPARENCY = LAST_NODE_INDEX + 12;

    /** Field index for separateBackColor */
    protected static final int FIELD_SEPARATE_BACK_COLOR = LAST_NODE_INDEX + 13;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = FIELD_SEPARATE_BACK_COLOR + 1;

    /** Error message when the user code barfs */
    private static final String EMISSIVE_ERROR_MSG =
        "Error sending emissive changed notification to: ";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** exposedField SFFloat ambientIntensity 0.2 */
    protected float vfAmbientIntensity;

    /** exposedField SFFloat backAmbientIntensity 0.2 */
    protected float vfBackAmbientIntensity;

    /** exposedField SFColor diffuseColor 0.8 0.8 0.8 */
    protected float[] vfDiffuseColor;

    /** exposedField SFColor backDiffuseColor 0.8 0.8 0.8 */
    protected float[] vfBackDiffuseColor;

    /** exposedField SFColor emissiveColor 0 0 0 */
    protected float[] vfEmissiveColor;

    /** exposedField SFColor backEmissiveColor 0 0 0 */
    protected float[] vfBackEmissiveColor;

    /** exposedField SFFloat shininess 0.2 */
    protected float vfShininess;

    /** exposedField SFFloat backShininess 0.2 */
    protected float vfBackShininess;

    /** exposedField SFColor specularColor 0 0 0 */
    protected float[] vfSpecularColor;

    /** exposedField SFColor backSpecularColor 0 0 0 */
    protected float[] vfBackSpecularColor;

    /** exposedField SFFloat transparency 0 */
    protected float vfTransparency;

    /** exposedField SFFloat backTransparency 0 */
    protected float vfBackTransparency;

    /** exposedField SFBool separateBackColor FALSE */
    protected boolean vfSeparateBackColor;

    /** Should we ignore the specified diffuseColor */
    protected boolean ignoreDiffuse;

    /** Color listener for passing back emissive changes */
    private MaterialColorListener colorListener;

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
        fieldDecl[FIELD_BACK_AMBIENT_INTENSITY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "backAmbientIntensity");
        fieldDecl[FIELD_DIFFUSE_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "diffuseColor");
        fieldDecl[FIELD_BACK_DIFFUSE_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "backDiffuseColor");
        fieldDecl[FIELD_EMISSIVE_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "emissiveColor");
        fieldDecl[FIELD_BACK_EMISSIVE_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "backEmissiveColor");
        fieldDecl[FIELD_SHININESS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "shininess");
        fieldDecl[FIELD_BACK_SHININESS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "backShininess");
        fieldDecl[FIELD_SPECULAR_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "specularColor");
        fieldDecl[FIELD_BACK_SPECULAR_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "backSpecularColor");
        fieldDecl[FIELD_TRANSPARENCY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "transparency");
        fieldDecl[FIELD_BACK_TRANSPARENCY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "backTransparency");
        fieldDecl[FIELD_SEPARATE_BACK_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "separateBackColor");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_AMBIENT_INTENSITY);
        fieldMap.put("ambientIntensity", idx);
        fieldMap.put("set_ambientIntensity", idx);
        fieldMap.put("ambientIntensity_changed", idx);

        idx = new Integer(FIELD_BACK_AMBIENT_INTENSITY);
        fieldMap.put("backAmbientIntensity", idx);
        fieldMap.put("set_backAmbientIntensity", idx);
        fieldMap.put("backAmbientIntensity_changed", idx);

        idx = new Integer(FIELD_TRANSPARENCY);
        fieldMap.put("transparency", idx);
        fieldMap.put("set_transparency", idx);
        fieldMap.put("transparency_changed", idx);

        idx = new Integer(FIELD_BACK_TRANSPARENCY);
        fieldMap.put("backTransparency", idx);
        fieldMap.put("set_backTransparency", idx);
        fieldMap.put("backTransparency_changed", idx);

        idx = new Integer(FIELD_DIFFUSE_COLOR);
        fieldMap.put("diffuseColor", idx);
        fieldMap.put("set_diffuseColor", idx);
        fieldMap.put("diffuseColor_changed", idx);

        idx = new Integer(FIELD_BACK_DIFFUSE_COLOR);
        fieldMap.put("backDiffuseColor", idx);
        fieldMap.put("set_backDiffuseColor", idx);
        fieldMap.put("backDiffuseColor_changed", idx);

        idx = new Integer(FIELD_EMISSIVE_COLOR);
        fieldMap.put("emissiveColor", idx);
        fieldMap.put("set_emissiveColor", idx);
        fieldMap.put("emissiveColor_changed", idx);

        idx = new Integer(FIELD_BACK_EMISSIVE_COLOR);
        fieldMap.put("backEmissiveColor", idx);
        fieldMap.put("set_backEmissiveColor", idx);
        fieldMap.put("backEmissiveColor_changed", idx);

        idx = new Integer(FIELD_SHININESS);
        fieldMap.put("shininess", idx);
        fieldMap.put("set_shininess", idx);
        fieldMap.put("shininess_changed", idx);

        idx = new Integer(FIELD_BACK_SHININESS);
        fieldMap.put("backShininess", idx);
        fieldMap.put("set_backShininess", idx);
        fieldMap.put("backShininess_changed", idx);

        idx = new Integer(FIELD_SPECULAR_COLOR);
        fieldMap.put("specularColor", idx);
        fieldMap.put("set_specularColor", idx);
        fieldMap.put("specularColor_changed", idx);

        idx = new Integer(FIELD_BACK_SPECULAR_COLOR);
        fieldMap.put("backSpecularColor", idx);
        fieldMap.put("set_backSpecularColor", idx);
        fieldMap.put("backSpecularColor_changed", idx);

        idx = new Integer(FIELD_SEPARATE_BACK_COLOR);
        fieldMap.put("separateBackColor", idx);
        fieldMap.put("set_separateBackColor", idx);
        fieldMap.put("separateBackColor_changed", idx);
    }

    /**
     * Construct a default instance of the material
     */
    protected BaseTwoSidedMaterial() {
        super("TwoSidedMaterial");

        hasChanged = new boolean[NUM_FIELDS];
        vfAmbientIntensity = 0.2f;
        vfBackAmbientIntensity = 0.2f;
        vfDiffuseColor = new float[] { 0.8f, 0.8f, 0.8f };
        vfBackDiffuseColor = new float[] { 0.8f, 0.8f, 0.8f };
        vfEmissiveColor = new float[] { 0, 0, 0 };
        vfBackEmissiveColor = new float[] { 0, 0, 0 };
        vfShininess = 0.2f;
        vfBackShininess = 0.2f;
        vfSpecularColor = new float[] { 0, 0, 0 };
        vfBackSpecularColor = new float[] { 0, 0, 0 };
        vfTransparency = 0;
        vfBackTransparency = 0;
        vfSeparateBackColor = true;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseTwoSidedMaterial(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("shininess");
            VRMLFieldData field = node.getFieldValue(index);
            vfShininess = field.floatValue;

            index = node.getFieldIndex("backShininess");
            field = node.getFieldValue(index);
            vfBackShininess = field.floatValue;

            index = node.getFieldIndex("transparency");
            field = node.getFieldValue(index);
            vfTransparency = field.floatValue;

            index = node.getFieldIndex("backTransparency");
            field = node.getFieldValue(index);
            vfBackTransparency = field.floatValue;

            index = node.getFieldIndex("ambientIntensity");
            field = node.getFieldValue(index);
            vfAmbientIntensity = field.floatValue;

            index = node.getFieldIndex("backAmbientIntensity");
            field = node.getFieldValue(index);
            vfBackAmbientIntensity = field.floatValue;

            index = node.getFieldIndex("separateBackColor");
            field = node.getFieldValue(index);
            vfSeparateBackColor = field.booleanValue;

            index = node.getFieldIndex("diffuseColor");
            field = node.getFieldValue(index);
            vfDiffuseColor[0] = field.floatArrayValue[0];
            vfDiffuseColor[1] = field.floatArrayValue[1];
            vfDiffuseColor[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("backDiffuseColor");
            field = node.getFieldValue(index);
            vfBackDiffuseColor[0] = field.floatArrayValue[0];
            vfBackDiffuseColor[1] = field.floatArrayValue[1];
            vfBackDiffuseColor[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("emissiveColor");
            field = node.getFieldValue(index);
            vfEmissiveColor[0] = field.floatArrayValue[0];
            vfEmissiveColor[1] = field.floatArrayValue[1];
            vfEmissiveColor[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("backEmissiveColor");
            field = node.getFieldValue(index);
            vfBackEmissiveColor[0] = field.floatArrayValue[0];
            vfBackEmissiveColor[1] = field.floatArrayValue[1];
            vfBackEmissiveColor[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("specularColor");
            field = node.getFieldValue(index);
            vfSpecularColor[0] = field.floatArrayValue[0];
            vfSpecularColor[1] = field.floatArrayValue[1];
            vfSpecularColor[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("backSpecularColor");
            field = node.getFieldValue(index);
            vfBackSpecularColor[0] = field.floatArrayValue[0];
            vfBackSpecularColor[1] = field.floatArrayValue[1];
            vfBackSpecularColor[2] = field.floatArrayValue[2];
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLMaterialNodeType
    //------------------------------------------------------------

    /**
     * Set a new value for ambientIntensity. How much ambient omnidirectional
     * light is reflected from all light sources.
     *
     * @param intensity The new intensity value
     */
    public void setAmbientIntensity(float intensity)
        throws InvalidFieldValueException {

        FieldValidator.checkFloat("Material.ambientIntensity",intensity);
        vfAmbientIntensity = intensity;

        if(!inSetup) {
            hasChanged[FIELD_AMBIENT_INTENSITY] = true;
            fireFieldChanged(FIELD_AMBIENT_INTENSITY);
        }
    }

    /**
     * Accessor method to get current value of field <b>ambientIntensity</b>,
     * default value is <code>0.2</code>
     *
     * @return The current ambientIntensity
     */
    public float getAmbientIntensity() {
        return vfAmbientIntensity;
    }

    /**
     * Set a new value for diffuseColor.  How much direct, angle-dependent
     * light is reflected from all light sources.
     *
     * @param color The new value of diffuseColor
     */
    public void setDiffuseColor(float[] color)
        throws InvalidFieldValueException {

        FieldValidator.checkColorVector("Material.diffuseColor", color);
        vfDiffuseColor[0] = color[0];
        vfDiffuseColor[1] = color[1];
        vfDiffuseColor[2] = color[2];

        if(!inSetup) {
            hasChanged[FIELD_DIFFUSE_COLOR] = true;
            fireFieldChanged(FIELD_DIFFUSE_COLOR);
        }
    }

    /**
     * Accessor method to get current value of field <b>diffuseColor</b>,
     * default value is <code>0.8 0.8 0.8</code>.
     *
     * @return The current value of diffuseColor
     */
    public float[] getDiffuseColor() {
        return vfDiffuseColor;
    }

    /**
     * Set a new value for emissiveColor. How much glowing light is emitted
     * from this object.
     *
     * @param color The new value of EmissiveColor
     */
    public void setEmissiveColor(float[] color)
        throws InvalidFieldValueException {

        FieldValidator.checkColorVector("Material.emmissiveColor", color);
        vfEmissiveColor[0] = color[0];
        vfEmissiveColor[1] = color[1];
        vfEmissiveColor[2] = color[2];

        if(!inSetup) {
            hasChanged[FIELD_EMISSIVE_COLOR] = true;
            fireFieldChanged(FIELD_EMISSIVE_COLOR);
            fireEmissiveColorChanged(colorListener);
        }
    }

    /**
     * Accessor method to get current value of field <b>emissiveColor</b>,
     * default value is <code>0 0 0</code>.
     *
     * @return The current value of EmissiveColor
     */
    public float[] getEmissiveColor() {
        return vfEmissiveColor;
    }

    /**
     * Set a new value for shininess. Low values provide soft specular glows,
     * high values provide sharper, smaller highlights.
     *
     * @param shininess The new value of Shininess
     */
    public void setShininess(float shininess)
        throws InvalidFieldValueException  {

        FieldValidator.checkFloat("Material.shininess", shininess);
        vfShininess = shininess;

        if(!inSetup) {
            hasChanged[FIELD_SHININESS] = true;
            fireFieldChanged(FIELD_SHININESS);
        }
    }

    /**
     * Accessor method to get current value of field <b>shininess</b>,
     * default value is <code>0.2</code>.
     *
     * @return The current value of Shininess
     */
    public float getShininess() {
        return vfShininess;
    }

    /**
     * Set a new value for the specularColor. Specular highlights are
     * brightness reflections (example:  shiny spots on an apple).
     *
     * @param color The new value of SpecularColor
     */
    public void setSpecularColor (float[] color)
        throws InvalidFieldValueException {

        FieldValidator.checkColorVector("Material.specularColor", color);
        vfSpecularColor[0] = color[0];
        vfSpecularColor[1] = color[1];
        vfSpecularColor[2] = color[2];

        if(!inSetup) {
            hasChanged[FIELD_SPECULAR_COLOR] = true;
            fireFieldChanged(FIELD_SPECULAR_COLOR);
        }
    }

    /**
     * Accessor method to get current value of field <b>specularColor</b>,
     * default value is <code>0 0 0</code>.
     *
     * @return The current value of SpecularColor
     */
    public float[] getSpecularColor () {
        return vfSpecularColor;
    }

    /**
     * Set a new value for the transparency.  How "clear" an object is:  1.0 is
     * completely transparent, 0.0 is completely opaque .
     *
     * @param trans The new value of Transparency
     */
    public void setTransparency(float trans)
        throws InvalidFieldValueException {

        FieldValidator.checkFloat("Material.transparency", trans);
        vfTransparency = trans;

        if(!inSetup) {
            hasChanged[FIELD_TRANSPARENCY] = true;
            fireFieldChanged(FIELD_TRANSPARENCY);
        }
    }

    /**
     * Ignore the diffuseColor color term and use 1,1,1 for the diffuse color.
     *
     * @param ignore True to ignore the diffuse term
     */
    public void setIgnoreDiffuse(boolean ignore) {
        ignoreDiffuse = ignore;
    }

    /**
     * Accessor method to get current value of field <b>transparency</b>,
     * default value is <code>0</code>
     *
     * @return The current value of Transparency
     */
    public float getTransparency() {
        return vfTransparency;
    }

    /**
     * Add a listener instance for the material color change notifications.
     * Adding the same instance more than once is ignored. Adding null values
     * are ignored.
     *
     * @param l The new instance to add
     */
    public void addMaterialColorListener(MaterialColorListener l) {
        colorListener = MaterialColorListenerMulticaster.add(colorListener, l);

        // Send a color update to the geometry right now
        if(l != null)
            fireEmissiveColorChanged(l);

    }

    /**
     * Remove a listener instance from this node. If the listener is not
     * currently registered, the request is silently ignored.
     *
     * @param l The new instance to remove
     */
    public void removeMaterialColorListener(MaterialColorListener l) {
        colorListener =
            MaterialColorListenerMulticaster.remove(colorListener, l);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

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
        Integer index = (Integer)fieldMap.get(fieldName);

        return (index == null) ? -1 : index.intValue();
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
        if(index < 0  || index > NUM_FIELDS - 1)
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
        return TypeConstants.MaterialNodeType;
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
            case FIELD_AMBIENT_INTENSITY:
                fieldData.clear();
                fieldData.floatValue = vfAmbientIntensity;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_BACK_AMBIENT_INTENSITY:
                fieldData.clear();
                fieldData.floatValue = vfBackAmbientIntensity;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_DIFFUSE_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfDiffuseColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BACK_DIFFUSE_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfBackDiffuseColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_EMISSIVE_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfEmissiveColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BACK_EMISSIVE_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfBackEmissiveColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_TRANSPARENCY:
                fieldData.clear();
                fieldData.floatValue = vfTransparency;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_BACK_TRANSPARENCY:
                fieldData.clear();
                fieldData.floatValue = vfBackTransparency;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SHININESS:
                fieldData.clear();
                fieldData.floatValue = vfShininess;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_BACK_SHININESS:
                fieldData.clear();
                fieldData.floatValue = vfBackShininess;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SPECULAR_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfSpecularColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BACK_SPECULAR_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfBackSpecularColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_SEPARATE_BACK_COLOR:
                fieldData.clear();
                fieldData.booleanValue = vfSeparateBackColor;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
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

                case FIELD_BACK_AMBIENT_INTENSITY :
                    destNode.setValue(destIndex, vfBackAmbientIntensity);
                    break;

                case FIELD_DIFFUSE_COLOR:
                    destNode.setValue(destIndex, vfDiffuseColor, 3);
                    break;

                case FIELD_BACK_DIFFUSE_COLOR:
                    destNode.setValue(destIndex, vfBackDiffuseColor, 3);
                    break;

                case FIELD_EMISSIVE_COLOR:
                    destNode.setValue(destIndex, vfEmissiveColor, 3);
                    break;

                case FIELD_BACK_EMISSIVE_COLOR:
                    destNode.setValue(destIndex, vfBackEmissiveColor, 3);
                    break;

                case FIELD_SHININESS:
                    destNode.setValue(destIndex, vfShininess);
                    break;

                case FIELD_BACK_SHININESS:
                    destNode.setValue(destIndex, vfBackShininess);
                    break;

                case FIELD_SPECULAR_COLOR:
                    destNode.setValue(destIndex, vfSpecularColor, 3);
                    break;

                case FIELD_BACK_SPECULAR_COLOR:
                    destNode.setValue(destIndex, vfBackSpecularColor, 3);
                    break;

                case FIELD_TRANSPARENCY:
                    destNode.setValue(destIndex, vfTransparency);
                    break;

                case FIELD_BACK_TRANSPARENCY:
                    destNode.setValue(destIndex, vfBackTransparency);
                    break;

                case FIELD_SEPARATE_BACK_COLOR:
                    destNode.setValue(destIndex, vfSeparateBackColor);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_SEPARATE_BACK_COLOR:
                setSeparateBackColor(value);
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
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_AMBIENT_INTENSITY:
                setAmbientIntensity(value);
                break;

            case FIELD_BACK_AMBIENT_INTENSITY:
                setBackAmbientIntensity(value);
                break;

            case FIELD_SHININESS:
                setShininess(value);
                break;

            case FIELD_BACK_SHININESS:
                setBackShininess(value);
                break;

            case FIELD_TRANSPARENCY:
                setTransparency(value);
                break;

            case FIELD_BACK_TRANSPARENCY:
                setBackTransparency(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFColor field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_DIFFUSE_COLOR:
                setDiffuseColor(value);
                break;

            case FIELD_BACK_DIFFUSE_COLOR:
                setBackDiffuseColor(value);
                break;

            case FIELD_EMISSIVE_COLOR:
                setEmissiveColor(value);
                break;

            case FIELD_BACK_EMISSIVE_COLOR:
                setBackEmissiveColor(value);
                break;

            case FIELD_SPECULAR_COLOR:
                setSpecularColor(value);
                break;

            case FIELD_BACK_SPECULAR_COLOR:
                setBackSpecularColor(value);
                break;

            default :
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set a new value for ambientIntensity. How much ambient omnidirectional
     * light is reflected from all light sources.
     *
     * @param intensity The new intensity value
     */
    protected void setBackAmbientIntensity(float intensity)
        throws InvalidFieldValueException {

        FieldValidator.checkFloat("Material.backAmbientIntensity",intensity);
        vfBackAmbientIntensity = intensity;

        if(!inSetup) {
            hasChanged[FIELD_BACK_AMBIENT_INTENSITY] = true;
            fireFieldChanged(FIELD_BACK_AMBIENT_INTENSITY);
        }
    }

    /**
     * Set a new value for diffuseColor.  How much direct, angle-dependent
     * light is reflected from all light sources.
     *
     * @param color The new value of diffuseColor
     */
    protected void setBackDiffuseColor(float[] color)
        throws InvalidFieldValueException {

        FieldValidator.checkColorVector("Material.backDiffuseColor", color);
        vfBackDiffuseColor[0] = color[0];
        vfBackDiffuseColor[1] = color[1];
        vfBackDiffuseColor[2] = color[2];

        if(!inSetup) {
            hasChanged[FIELD_BACK_DIFFUSE_COLOR] = true;
            fireFieldChanged(FIELD_BACK_DIFFUSE_COLOR);
        }
    }

    /**
     * Set a new value for emissiveColor. How much glowing light is emitted
     * from this object.
     *
     * @param color The new value of EmissiveColor
     */
    protected void setBackEmissiveColor(float[] color)
        throws InvalidFieldValueException {

        FieldValidator.checkColorVector("Material.backEmissiveColor", color);
        vfBackEmissiveColor[0] = color[0];
        vfBackEmissiveColor[1] = color[1];
        vfBackEmissiveColor[2] = color[2];

        if(!inSetup) {
            hasChanged[FIELD_BACK_EMISSIVE_COLOR] = true;
            fireFieldChanged(FIELD_BACK_EMISSIVE_COLOR);
        }
    }

    /**
     * Set a new value for shininess. Low values provide soft specular glows,
     * high values provide sharper, smaller highlights.
     *
     * @param shininess The new value of Shininess
     */
    protected void setBackShininess(float shininess)
        throws InvalidFieldValueException  {

        FieldValidator.checkFloat("Material.backShininess", shininess);
        vfBackShininess = shininess;

        if(!inSetup) {
            hasChanged[FIELD_BACK_SHININESS] = true;
            fireFieldChanged(FIELD_BACK_SHININESS);
        }
    }

    /**
     * Set a new value for the specularColor. Specular highlights are
     * brightness reflections (example:  shiny spots on an apple).
     *
     * @param color The new value of SpecularColor
     */
    protected void setBackSpecularColor(float[] color)
        throws InvalidFieldValueException {

        FieldValidator.checkColorVector("Material.backSpecularColor", color);
        vfBackSpecularColor[0] = color[0];
        vfBackSpecularColor[1] = color[1];
        vfBackSpecularColor[2] = color[2];

        if(!inSetup) {
            hasChanged[FIELD_BACK_SPECULAR_COLOR] = true;
            fireFieldChanged(FIELD_BACK_SPECULAR_COLOR);
        }
    }

    /**
     * Set a new value for the transparency.  How "clear" an object is:  1.0 is
     * completely transparent, 0.0 is completely opaque .
     *
     * @param trans The new value of Transparency
     */
    protected void setBackTransparency(float trans)
        throws InvalidFieldValueException {

        FieldValidator.checkFloat("Material.backTransparency", trans);
        vfBackTransparency = trans;

        if(!inSetup) {
            hasChanged[FIELD_BACK_TRANSPARENCY] = true;
            fireFieldChanged(FIELD_BACK_TRANSPARENCY);
        }
    }

    /**
     * Set a new value for the separated back face colour state.
     *
     * @param state true to use different front from back colours
     */
    protected void setSeparateBackColor(boolean state) {
        vfSeparateBackColor = state;

        if(!inSetup) {
            hasChanged[FIELD_SEPARATE_BACK_COLOR] = true;
            fireFieldChanged(FIELD_SEPARATE_BACK_COLOR);
        }
    }

    /**
     * Send the emissive color change event to our internal listeners.
     *
     * @param l The listener isntance
     */
    private void fireEmissiveColorChanged(MaterialColorListener l) {
        try {
            if(l != null)
                l.emissiveColorChanged(vfEmissiveColor);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(EMISSIVE_ERROR_MSG + l,
                                          (Exception)th);
            else {
                System.out.println("Unknown BAAAAD error: " + th);
                th.printStackTrace();
            }
        }
    }
}
