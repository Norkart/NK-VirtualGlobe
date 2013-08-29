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

package org.web3d.vrml.renderer.common.nodes.geom3d;

// External imports
import java.util.HashMap;

// Local import
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of an Extrusion.
 *
 * @author Andrzej Kapolka
 * @version $Revision: 1.14 $
 */
public abstract class BaseExtrusion extends AbstractNode
    implements VRMLGeometryNodeType {

    /** Field Index for beginCap */
    protected static final int FIELD_BEGIN_CAP = LAST_NODE_INDEX + 1;

    /** Field Index for ccw */
    protected static final int FIELD_CCW = LAST_NODE_INDEX + 2;

    /** Field Index for convex */
    protected static final int FIELD_CONVEX = LAST_NODE_INDEX + 3;

    /** Field Index for creaseAngle */
    protected static final int FIELD_CREASE_ANGLE = LAST_NODE_INDEX + 4;

    /** Field Index for crossSection */
    protected static final int FIELD_CROSS_SECTION = LAST_NODE_INDEX + 5;

    /** Field Index for endCap */
    protected static final int FIELD_END_CAP = LAST_NODE_INDEX + 6;

    /** Field Index for orientation */
    protected static final int FIELD_ORIENTATION = LAST_NODE_INDEX + 7;

    /** Field Index for scale */
    protected static final int FIELD_SCALE = LAST_NODE_INDEX + 8;

    /** Field Index for solid */
    protected static final int FIELD_SOLID = LAST_NODE_INDEX + 9;

    /** Field Index for spine */
    protected static final int FIELD_SPINE = LAST_NODE_INDEX + 10;

    /** Field Index for set_crossSection */
    protected static final int FIELD_SET_CROSS_SECTION = LAST_NODE_INDEX + 11;

    /** Field Index for set_orientation */
    protected static final int FIELD_SET_ORIENTATION = LAST_NODE_INDEX + 12;

    /** Field Index for set_scale */
    protected static final int FIELD_SET_SCALE = LAST_NODE_INDEX + 13;

    /** Field Index for set_spine */
    protected static final int FIELD_SET_SPINE = LAST_NODE_INDEX + 14;


    /** The last index in this node */
    protected static final int LAST_EXTRUSION_INDEX = FIELD_SET_SPINE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_EXTRUSION_INDEX + 1;


    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** SFBool beginCap */
    protected boolean vfBeginCap;

    /** SFBool ccw */
    protected boolean vfCCW;

    /** SFBool convex */
    protected boolean vfConvex;

    /** SFFloat creaseAngle */
    protected float vfCreaseAngle;

    /** MFVec2f crossSection */
    protected float[] vfCrossSection;

    /** Number of valid items in vfCrossSection */
    protected int numCrossSection;

    /** SFBool endCap */
    protected boolean vfEndCap;

    /** MFRotation orientation */
    protected float[] vfOrientation;

    /** Number of valid items in vfOrientation */
    protected int numOrientation;

    /** MFVec2f scale */
    protected float[] vfScale;

    /** Number of valid items in vfScale */
    protected int numScale;

    /** SFBool solid */
    protected boolean vfSolid;

    /** MFVec3f spine */
    protected float[] vfSpine;

    /** Number of valid items in vfSpine */
    protected int numSpine;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_BEGIN_CAP] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "beginCap");
        fieldDecl[FIELD_CCW] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "ccw");
        fieldDecl[FIELD_CONVEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "convex");
        fieldDecl[FIELD_CREASE_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "creaseAngle");
        fieldDecl[FIELD_CROSS_SECTION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFVec2f",
                                     "crossSection");
        fieldDecl[FIELD_END_CAP] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "endCap");
        fieldDecl[FIELD_ORIENTATION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFRotation",
                                     "orientation");
        fieldDecl[FIELD_SCALE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFVec2f",
                                     "scale");
        fieldDecl[FIELD_SOLID] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "solid");
        fieldDecl[FIELD_SPINE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFVec3f",
                                     "spine");
        fieldDecl[FIELD_SET_CROSS_SECTION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFVec2f",
                                     "set_crossSection");
        fieldDecl[FIELD_SET_ORIENTATION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFRotation",
                                     "set_orientation");
        fieldDecl[FIELD_SET_SCALE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFVec2f",
                                     "set_scale");
        fieldDecl[FIELD_SET_SPINE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFVec3f",
                                     "set_spine");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("beginCap",new Integer(FIELD_BEGIN_CAP));
        fieldMap.put("endCap",new Integer(FIELD_END_CAP));
        fieldMap.put("ccw",new Integer(FIELD_CCW));
        fieldMap.put("convex",new Integer(FIELD_CONVEX));
        fieldMap.put("creaseAngle",new Integer(FIELD_CREASE_ANGLE));
        fieldMap.put("solid",new Integer(FIELD_SOLID));

        fieldMap.put("scale", new Integer(FIELD_SCALE));
        fieldMap.put("set_scale", new Integer(FIELD_SET_SCALE));

        fieldMap.put("orientation", new Integer(FIELD_ORIENTATION));
        fieldMap.put("set_orientation", new Integer(FIELD_SET_ORIENTATION));

        fieldMap.put("crossSection", new Integer(FIELD_CROSS_SECTION));
        fieldMap.put("set_crossSection", new Integer(FIELD_SET_CROSS_SECTION));

        fieldMap.put("spine", new Integer(FIELD_SPINE));
        fieldMap.put("set_spine", new Integer(FIELD_SET_SPINE));
    }

    /**
     * Construct a default extrusion instance
     */
    protected BaseExtrusion() {
        super("Extrusion");

        hasChanged = new boolean[NUM_FIELDS];

        vfBeginCap = true;
        vfCCW = true;
        vfConvex = true;
        vfCreaseAngle = 0.9f;
        vfCrossSection = new float[] { 1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
                                       -1.0f, -1.0f, 1.0f, 1.0f, 1.0f };
        numCrossSection = vfCrossSection.length / 2;

        vfEndCap = true;
        vfOrientation = new float[] { 0.0f, 0.0f, 1.0f, 0.0f };
        numOrientation = vfOrientation.length / 4;

        vfScale = new float[] { 1.0f, 1.0f };
        numScale = vfScale.length / 2;
        vfSolid = true;
        vfSpine = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f };
        numSpine = vfSpine.length / 3;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseExtrusion(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("beginCap");
            VRMLFieldData field = node.getFieldValue(index);
            vfBeginCap = field.booleanValue;

            index = node.getFieldIndex("ccw");
            field = node.getFieldValue(index);
            vfCCW = field.booleanValue;

            index = node.getFieldIndex("convex");
            field = node.getFieldValue(index);
            vfConvex = field.booleanValue;

            index = node.getFieldIndex("creaseAngle");
            field = node.getFieldValue(index);
            vfCreaseAngle = field.floatValue;

            index = node.getFieldIndex("crossSection");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfCrossSection = new float[field.numElements * 2];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfCrossSection,
                                 0,
                                 field.numElements * 2);

                numCrossSection = field.numElements;
            }

            index = node.getFieldIndex("endCap");
            field = node.getFieldValue(index);
            vfEndCap = field.booleanValue;

            index = node.getFieldIndex("orientation");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfOrientation = new float[field.numElements * 4];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfOrientation,
                                 0,
                                 field.numElements * 4);

                numOrientation = field.numElements;
            }

            index = node.getFieldIndex("scale");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfScale = new float[field.numElements * 2];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfScale,
                                 0,
                                 field.numElements * 2);

                numScale = field.numElements;
            }

            index = node.getFieldIndex("solid");
            field = node.getFieldValue(index);
            vfSolid = field.booleanValue;

            index = node.getFieldIndex("spine");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfSpine = new float[field.numElements * 3];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfSpine,
                                 0,
                                 field.numElements * 3);

                numSpine = field.numElements;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType
    //----------------------------------------------------------

    /**
     * Specified whether this node has color information.  If so, then it
     * will be used for diffuse terms instead of materials.
     *
     * @return true Use local color information for diffuse lighting.
     */
    public boolean hasLocalColors() {
        return false;
    }

    /**
     * Specified whether this node has alpha values in the local colour
     * information. If so, then it will be used for to override the material's
     * transparency value.
     *
     * @return true when the local color value has inbuilt alpha
     */
    public boolean hasLocalColorAlpha() {
        return false;
    }

    /**
     * Add a listener for local color changes.  Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    public void addLocalColorsListener(LocalColorsListener l) {
    }

    /**
     * Remove a listener for local color changes.  Nulls will be ignored.
     *
     * @param l The listener.
     */
    public void removeLocalColorsListener(LocalColorsListener l) {
    }

    /**
     * Add a listener for texture coordinate generation mode changes.
     * Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    public void addTexCoordGenModeChanged(TexCoordGenModeListener l) {
    }

    /**
     * Remove a listener for texture coordinate generation mode changes.
     * Nulls will be ignored.
     *
     * @param l The listener.
     */
    public void removeTexCoordGenModeChanged(TexCoordGenModeListener l) {
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
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets() {
        return 0;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType
    //----------------------------------------------------------

    /**
     * Get the value of the solid field.
     *
     * @return true This object is solid (ie single sided)
     */
    public boolean isSolid() {
        return vfSolid;
    }

    /**
     * Get the value of the CCW field. If the node does not have one, this will
     * return true.
     *
     * @return true if the vertices are CCW ordered
     */
    public boolean isCCW() {
        return true;
    }

    /**
     * Specifies whether this node requires lighting.
     *
     * @return Should lighting be enabled
     */
    public boolean isLightingEnabled() {
        return true;
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
        if(index < 0  || index > LAST_EXTRUSION_INDEX)
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
        return TypeConstants.GeometryNodeType;
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
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        fieldData.clear();

        switch(index) {
            case FIELD_BEGIN_CAP:
                fieldData.booleanValue = vfBeginCap;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_CCW:
                fieldData.booleanValue = vfCCW;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_CONVEX:
                fieldData.booleanValue = vfConvex;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_CREASE_ANGLE:
                fieldData.floatValue = vfCreaseAngle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_CROSS_SECTION:
                fieldData.floatArrayValue = vfCrossSection;
                fieldData.numElements = numCrossSection;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_END_CAP:
                fieldData.booleanValue = vfEndCap;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_ORIENTATION:
                fieldData.floatArrayValue = vfOrientation;
                fieldData.numElements = numOrientation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_SCALE:
                fieldData.floatArrayValue = vfScale;
                fieldData.numElements = numScale;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_SOLID:
                fieldData.booleanValue = vfSolid;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_SPINE:
                fieldData.floatArrayValue = vfSpine;
                fieldData.numElements = numSpine;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {


        switch(index) {
            case FIELD_BEGIN_CAP:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "beginCap");

                vfBeginCap = value;
                break;

            case FIELD_CCW:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "ccw");
                vfCCW = value;
                break;

            case FIELD_CONVEX:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "convex");
                vfConvex = value;
                break;

            case FIELD_END_CAP:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "endCap");
                vfEndCap = value;
                break;

            case FIELD_SOLID:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "solid");
                vfSolid = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index)
        {
            case FIELD_CREASE_ANGLE:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "creaseAngle");

                vfCreaseAngle = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(inSetup) {
            switch(index) {
                case FIELD_CROSS_SECTION:
                    vfCrossSection = value;
                    numCrossSection = numValid / 2;
                    break;

                case FIELD_ORIENTATION:
                    vfOrientation = value;
                    numOrientation = numValid / 4;
                    break;

                case FIELD_SCALE:
                    vfScale = value;
                    numScale = numValid / 2;
                    break;

                case FIELD_SPINE:
                    vfSpine = value;
                    numSpine = numValid / 3;
                    break;

                case FIELD_SET_CROSS_SECTION:
                    throw new InvalidFieldAccessException(
                        "Cannot set eventIn set_crossSection at startup");

                case FIELD_SET_ORIENTATION:
                    throw new InvalidFieldAccessException(
                        "Cannot set eventIn set_orientation at startup");

                case FIELD_SET_SCALE:
                    throw new InvalidFieldAccessException(
                        "Cannot set eventIn set_scale at startup");

                case FIELD_SET_SPINE:
                    throw new InvalidFieldAccessException(
                        "Cannot set eventIn set_spine at startup");

                default:
                    super.setValue(index, value, numValid);

            }
        } else {
            switch(index) {
                case FIELD_SET_CROSS_SECTION:
                    if(vfCrossSection.length < numValid)
                        vfCrossSection = new float[numValid];

                    if(numValid != 0)
                        System.arraycopy(value, 0, vfCrossSection, 0, numValid);

                    numCrossSection = numValid / 2;
                    break;

                case FIELD_SET_ORIENTATION:
                    if(vfOrientation.length < numValid)
                        vfOrientation = new float[numValid];

                    if(numValid != 0)
                        System.arraycopy(value, 0, vfOrientation, 0, numValid);

                    numOrientation = numValid / 4;
                    break;

                case FIELD_SET_SCALE:
                    if(vfScale.length < numValid)
                        vfScale = new float[numValid];

                    if(numValid != 0)
                        System.arraycopy(value, 0, vfScale, 0, numValid);

                    numScale = numValid / 2;
                    break;

                case FIELD_SET_SPINE:
                    if(vfSpine.length < numValid)
                        vfSpine = new float[numValid];

                    if(numValid != 0)
                        System.arraycopy(value, 0, vfSpine, 0, numValid);

                    numSpine = numValid / 3;
                    break;

                case FIELD_CROSS_SECTION:
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "crossSection.");

                case FIELD_ORIENTATION:
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "orientation.");

                case FIELD_SCALE:
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "scale.");

                case FIELD_SPINE:
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "spine.");

                default:
                    super.setValue(index, value, numValid);
            }

            hasChanged[index] = true;
            fireFieldChanged(index);
        }
    }
}
