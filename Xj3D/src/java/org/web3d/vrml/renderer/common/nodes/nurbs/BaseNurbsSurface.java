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

package org.web3d.vrml.renderer.common.nodes.nurbs;

// External imports
import java.util.HashMap;

import org.j3d.geom.GeometryData;
import org.j3d.geom.spline.BSplinePatchGenerator;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of the NurbsCurve node.
 * <p>
 *
 * Because NURBS implementations involve complex retessellation,
 * the implementation will automatically register itself with the
 * frame state manager whenever any field changes.
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public abstract class BaseNurbsSurface extends AbstractNode
    implements VRMLParametricGeometryNodeType {

    /** Field index for controlPoint */
    protected static final int FIELD_CONTROL_POINT = LAST_NODE_INDEX + 1;

    /** Field index for texCoord */
    protected static final int FIELD_TEXCOORD = LAST_NODE_INDEX + 2;

    /** Field index for uTessellation */
    protected static final int FIELD_UTESSELLATION = LAST_NODE_INDEX + 3;

    /** Field index for vTessellation */
    protected static final int FIELD_VTESSELLATION = LAST_NODE_INDEX + 4;

    /** Field index for weight */
    protected static final int FIELD_WEIGHT = LAST_NODE_INDEX + 5;

    /** Field index for solid */
    protected static final int FIELD_SOLID = LAST_NODE_INDEX + 6;

    /** Field index for uDimension */
    protected static final int FIELD_UDIMENSION = LAST_NODE_INDEX + 7;

    /** Field index for uKnot */
    protected static final int FIELD_UKNOT = LAST_NODE_INDEX + 8;

    /** Field index for uOrder */
    protected static final int FIELD_UORDER = LAST_NODE_INDEX + 9;

    /** Field index for vDimension */
    protected static final int FIELD_VDIMENSION = LAST_NODE_INDEX + 10;

    /** Field index for vKnot */
    protected static final int FIELD_VKNOT = LAST_NODE_INDEX + 11;

    /** Field index for vOrder */
    protected static final int FIELD_VORDER = LAST_NODE_INDEX + 12;

    /** The last index in this node */
    protected static final int LAST_CURVE_INDEX = FIELD_VORDER;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_CURVE_INDEX + 1;

    /** Message for when the proto is not a Geometry */
    protected static final String TEXTURE_PROTO_MSG =
        "Proto does not describe a Texture object";

    /** Message for when the node in setValue() is not a Geometry */
    protected static final String TEXTURE_NODE_MSG =
        "Node does not describe a Texture object";


    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

     /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

   // VRML Field declarations

    /** Proto version of the texCoord */
    protected VRMLProtoInstance pTexCoord;

    /** SFNode texCoord NULL */
    protected VRMLTextureCoordinateNodeType vfTexCoord;

    /** The value of the controlPoint field */
    protected double[] vfControlPoint;

    /** The value of the vTessellation field  */
    protected int vfUTessellation;

    /** The value of the vTessellation field  */
    protected int vfVTessellation;

    /** The value of the weight field */
    protected double[] vfWeight;

    /** The value of the solid field */
    protected boolean vfSolid;

    /** The value of the uDimension field */
    private int vfUDimension;

    /** The value of the uKnot field */
    private double[] vfUKnot;

    /** The value of the uOrder field */
    private int vfUOrder;

    /** The valve of the vDimension field */
    private int vfVDimension;

    /** The valve of the vKnot field */
    private double[] vfVKnot;

    /** The valve of the vOrder field */
    private int vfVOrder;

    /** Generator used to tessellate the curve */
    private BSplinePatchGenerator generator;

    /** Data from the last tessellation run, LINE_STRIPS by default. */
    protected GeometryData geometryData;

    /** Flag indicating if the control points have changed since last update */
    private boolean controlPointsChanged;

    /** Flag indicating if the weights have changed since last update */
    private boolean weightsChanged;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_CONTROL_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "MFVec3d",
                                 "controlPoint");
        fieldDecl[FIELD_SOLID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "SFNode",
                                 "texCoord");
        fieldDecl[FIELD_UTESSELLATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "SFInt32",
                                 "uTessellation");
        fieldDecl[FIELD_VTESSELLATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "SFInt32",
                                 "vTessellation");
        fieldDecl[FIELD_WEIGHT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "MFDouble",
                                 "weight");
        fieldDecl[FIELD_SOLID] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFBool",
                                 "solid");
        fieldDecl[FIELD_VKNOT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "MFDouble",
                                 "vKnot");
        fieldDecl[FIELD_VDIMENSION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFInt32",
                                 "vDimension");
        fieldDecl[FIELD_VORDER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFInt32",
                                 "vOrder");
        fieldDecl[FIELD_UKNOT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "MFDouble",
                                 "uKnot");
        fieldDecl[FIELD_UDIMENSION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFInt32",
                                 "uDimension");
        fieldDecl[FIELD_UORDER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFInt32",
                                 "uOrder");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_UTESSELLATION);
        fieldMap.put("uTessellation", idx);
        fieldMap.put("set_uTessellation", idx);
        fieldMap.put("uTessellation_changed", idx);

        idx = new Integer(FIELD_VTESSELLATION);
        fieldMap.put("vTessellation", idx);
        fieldMap.put("set_vTessellation", idx);
        fieldMap.put("vTessellation_changed", idx);

        idx = new Integer(FIELD_CONTROL_POINT);
        fieldMap.put("controlPoint", idx);
        fieldMap.put("set_controlPoint", idx);
        fieldMap.put("controlPoint_changed", idx);

        idx = new Integer(FIELD_WEIGHT);
        fieldMap.put("weight", idx);
        fieldMap.put("set_weight", idx);
        fieldMap.put("weight_changed", idx);

        idx = new Integer(FIELD_TEXCOORD);
        fieldMap.put("texCoord", idx);
        fieldMap.put("set_texCoord", idx);
        fieldMap.put("texCoord_changed", idx);

        fieldMap.put("solid", new Integer(FIELD_SOLID));

        fieldMap.put("uDimension", new Integer(FIELD_UDIMENSION));
        fieldMap.put("vDimension", new Integer(FIELD_VDIMENSION));

        fieldMap.put("uKnot", new Integer(FIELD_UKNOT));
        fieldMap.put("uOrder", new Integer(FIELD_UORDER));

        fieldMap.put("vKnot", new Integer(FIELD_VKNOT));
        fieldMap.put("vOrder", new Integer(FIELD_VORDER));
    }

    /**
     * Create a new default instance of the node.
     */
    protected BaseNurbsSurface() {
        super("NurbsSurface");

        hasChanged = new boolean[NUM_FIELDS];
        vfUOrder = 3;
        vfVOrder = 3;
        vfSolid = true;

        generator = new BSplinePatchGenerator();
        geometryData = new GeometryData();
        geometryData.geometryType = GeometryData.TRIANGLE_STRIPS;
        geometryData.geometryComponents = GeometryData.NORMAL_DATA |
                                          GeometryData.TEXTURE_2D_DATA;

        controlPointsChanged = false;
        weightsChanged = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseNurbsSurface(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("controlPoint");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfControlPoint = new double[field.numElements * 3];
                System.arraycopy(field.doubleArrayValue,
                                 0,
                                 vfControlPoint,
                                 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("uTessellation");
            field = node.getFieldValue(index);
            vfUTessellation = field.intValue;

            index = node.getFieldIndex("vTessellation");
            field = node.getFieldValue(index);
            vfVTessellation = field.intValue;

            index = node.getFieldIndex("solid");
            field = node.getFieldValue(index);
            vfSolid = field.booleanValue;

            index = node.getFieldIndex("uOrder");
            field = node.getFieldValue(index);
            vfUOrder = field.intValue;

            index = node.getFieldIndex("vOrder");
            field = node.getFieldValue(index);
            vfVOrder = field.intValue;

            index = node.getFieldIndex("weight");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfWeight = new double[field.numElements];
                System.arraycopy(field.doubleArrayValue,
                                 0,
                                 vfWeight,
                                 0,
                                 field.numElements);
            }


            index = node.getFieldIndex("uKnot");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfUKnot = new double[field.numElements];
                System.arraycopy(field.doubleArrayValue,
                                 0,
                                 vfUKnot,
                                 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("vKnot");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfVKnot = new double[field.numElements];
                System.arraycopy(field.doubleArrayValue,
                                 0,
                                 vfVKnot,
                                 0,
                                 field.numElements);
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

    /**
     * Get the value of the solid field.
     *
     * @return true This object is solid (ie single sided)
     */
    public boolean isSolid() {
        return false;
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
    // Methods required by the NRVRMLNodeTypeType interface.
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(pTexCoord != null)
            pTexCoord.setupFinished();

        if(vfTexCoord != null)
            vfTexCoord.setupFinished();

        boolean valid_data = true;

        if(vfControlPoint != null)
            generator.setPatchControlPoints(vfControlPoint,
                                            vfUDimension,
                                            vfVDimension);
        else
            valid_data = false;

        if(vfUKnot != null && vfVKnot != null &&
           (vfUKnot.length >= (vfUDimension + vfUOrder)) &&
           (vfVKnot.length >= (vfVDimension + vfVOrder)))
            generator.setPatchKnots(vfUOrder - 1,
                                    vfUKnot,
                                    vfVOrder - 1,
                                    vfVKnot);
        else
            valid_data = false;

        if(vfWeight != null)
            generator.setPatchWeights(vfWeight, vfUDimension, vfVDimension);

        if(!valid_data || vfUOrder < 2 || vfVOrder < 2)
            geometryData.vertexCount = 0;

        updateFacetCount();
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
        if(index < 0  || index > LAST_CURVE_INDEX)
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
        return TypeConstants.ParametricGeometryNodeType;
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
            case FIELD_CONTROL_POINT:
                fieldData.clear();
                fieldData.doubleArrayValue = vfControlPoint;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfControlPoint == null ? 0 :
                                        vfControlPoint.length / 3;
                break;

            case FIELD_TEXCOORD:
                fieldData.clear();
                if(pTexCoord != null)
                    fieldData.nodeValue = pTexCoord;
                else
                    fieldData.nodeValue = vfTexCoord;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_WEIGHT:
                fieldData.clear();
                fieldData.doubleArrayValue = vfWeight;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfWeight == null ? 0 : vfWeight.length;
                break;

            case FIELD_UKNOT:
                fieldData.clear();
                fieldData.doubleArrayValue = vfUKnot;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfUKnot == null ? 0 : vfUKnot.length;
                break;

            case FIELD_VKNOT:
                fieldData.clear();
                fieldData.doubleArrayValue = vfVKnot;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfVKnot == null ? 0 : vfVKnot.length;
                break;

            case FIELD_UORDER:
                fieldData.clear();
                fieldData.intValue = vfUOrder;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_VORDER:
                fieldData.clear();
                fieldData.intValue = vfVOrder;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_UDIMENSION:
                fieldData.clear();
                fieldData.intValue = vfUDimension;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_VDIMENSION:
                fieldData.clear();
                fieldData.intValue = vfVDimension;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_UTESSELLATION:
                fieldData.clear();
                fieldData.intValue = vfUTessellation;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_VTESSELLATION:
                fieldData.clear();
                fieldData.intValue = vfVTessellation;
                fieldData.dataType = VRMLFieldData.INT_DATA;
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
                case FIELD_CONTROL_POINT:
                    destNode.setValue(destIndex, vfControlPoint, vfControlPoint.length);
                    break;

                case FIELD_UTESSELLATION:
                    destNode.setValue(destIndex, vfUTessellation);
                    break;

                case FIELD_VTESSELLATION:
                    destNode.setValue(destIndex, vfVTessellation);
                    break;

                case FIELD_TEXCOORD:
                    if(pTexCoord != null)
                        destNode.setValue(destIndex, pTexCoord);
                    else
                        destNode.setValue(destIndex, vfTexCoord);
                    break;

                case FIELD_WEIGHT:
                    destNode.setValue(destIndex, vfWeight, vfWeight.length);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an int.
     * This would be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {

            case FIELD_SOLID:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot write to field solid");

                vfSolid = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an int.
     * This would be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_UORDER:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set field uOrder");

                vfUOrder = value;
                if(vfUOrder < 2)
                    throw new InvalidFieldValueException("uOrder < 2: "+value);
                break;

            case FIELD_VORDER:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set field vOrder");

                vfVOrder = value;
                if(vfVOrder < 2)
                    throw new InvalidFieldValueException("vOrder < 2: "+value);
                break;

            case FIELD_UTESSELLATION:
                vfUTessellation = value;
                updateFacetCount();
                break;

            case FIELD_VTESSELLATION:
                vfVTessellation = value;
                updateFacetCount();
                break;

            case FIELD_UDIMENSION:
                vfUDimension = value;
                updateFacetCount();
                break;

            case FIELD_VDIMENSION:
                vfVDimension = value;
                updateFacetCount();
                break;

            default:
                super.setValue(index, value);
        }

        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);

            hasChanged[index] = true;
            fireFieldChanged(index);
        }
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_WEIGHT:
                setWeight(value, numValid);
                break;

            case FIELD_UKNOT:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set field uKnot");

                if(value != null) {
                    vfUKnot = new double[value.length];
                    System.arraycopy(value, 0, vfUKnot, 0, value.length);
                }

                break;

            case FIELD_VKNOT:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set field vKnot");

                if(value != null) {
                    vfVKnot = new double[value.length];
                    System.arraycopy(value, 0, vfVKnot, 0, value.length);
                }

                break;

            case FIELD_CONTROL_POINT:
                setControlPoints(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;

        switch(index) {
            case FIELD_TEXCOORD:
                setTexCoord(node);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //-------------------------------------------------------------
    // Internal convenience methods
    //-------------------------------------------------------------

    /**
     * Internal convenience method to update the weight values.
     *
     * @param weights The list of weight values to use
     */
    private void setWeight(double[] weights, int numValid) {

        // Always reallocate the array. We're going to assume that this
        // very rarely changes so optimise for this case.
        if(numValid != 0) {
            if(vfWeight == null || numValid > vfWeight.length)
                vfWeight = new double[numValid];

            System.arraycopy(weights, 0, vfWeight, 0, numValid);
        } else {
            vfWeight = null;
        }

        if(!inSetup) {
            weightsChanged = true;
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_WEIGHT] = true;
            fireFieldChanged(FIELD_WEIGHT);
        }
    }

    /**
     * Internal convenience method to setup the control points.
     *
     * @param points The new point array to use
     */
    private void setControlPoints(double[] points) {
        int num_points = 0;
        if(points != null) {
            vfControlPoint = new double[points.length];
            num_points = points.length;
        }

        if(num_points != 0)
            System.arraycopy(points, 0, vfControlPoint, 0, num_points);

        if(!inSetup) {
            controlPointsChanged = true;
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_CONTROL_POINT] = true;
            fireFieldChanged(FIELD_CONTROL_POINT);
        }
    }

    /**
     * Calculate the facetCount needed for the current state of the curve. The
     * spec states:
     *
     * 1. if a tessellation value is greater than 0, the number of tessellation
     * points is tessellation+1;
     *
     * 2. if a tessellation value is smaller than 0, the number of tessellation
     * points is (-tessellation × (number of control points)+1)
     *
     * 3. if a tessellation value is 0, the number of tessellation points is
     * (2 × (number of control points)+1.
     */
    private void updateFacetCount() {
        int w_facets = 0;
        int d_facets = 0;

        // Note that the -1 is not used here because this is the number of
        // faces to be created, not the number of points.

        if(vfUTessellation > 0)
            w_facets = vfUTessellation;
        else if(vfUTessellation < 0)
            w_facets = -vfUTessellation * vfUDimension;
        else
            w_facets = 2 * vfUDimension;

        if(vfVTessellation > 0)
            d_facets = vfVTessellation;
        else if(vfVTessellation < 0)
            d_facets = -vfVTessellation * vfUDimension;
        else
            d_facets = 2 * vfUDimension;

        generator.setFacetCount(w_facets, d_facets);
    }

    /**
     * Set node content as replacement for <code>appearance</code>.
     *
     * @param app The new appearance.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setTexCoord(VRMLNodeType tex)
        throws InvalidFieldValueException {

        VRMLTextureCoordinateNodeType node;

        VRMLNodeType old_node;

        if(pTexCoord != null)
            old_node = pTexCoord;
        else
            old_node = vfTexCoord;

        if (tex instanceof VRMLProtoInstance) {
            node = (VRMLTextureCoordinateNodeType)
                ((VRMLProtoInstance)tex).getImplementationNode();
            pTexCoord = (VRMLProtoInstance)tex;
            if ((node != null) && !(node instanceof VRMLTextureCoordinateNodeType)) {
                throw new InvalidFieldValueException(TEXTURE_PROTO_MSG);
            }
        } else if (tex != null &&
            (!(tex instanceof VRMLTextureCoordinateNodeType))) {
            throw new InvalidFieldValueException(TEXTURE_NODE_MSG);
        } else {
            pTexCoord = null;
            node = (VRMLTextureCoordinateNodeType)tex;
        }

        vfTexCoord = (VRMLTextureCoordinateNodeType)node;

        if(tex != null)
            updateRefs(tex, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if (!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(tex != null)
                stateManager.registerAddedNode(tex);

            hasChanged[FIELD_TEXCOORD] = true;
            fireFieldChanged(FIELD_TEXCOORD);
        }
    }

    /**
     * Request to regenerate the curve data now. The regenerated data will end
     * up in the geometryData object. If some ofthe data is invalid, then the
     * generation will create a curve with zero points in it and return false.
     *
     * @return true The generation succeeded
     */
    protected boolean regenerateSurface() {

        // sanity check to make sure all the data is valid before attempting to
        if(vfControlPoint == null ||
           vfUKnot == null ||
           vfVKnot == null ||
           vfUOrder < 2 ||
           vfVOrder < 2) {
            geometryData.vertexCount = 0;
            return false;
        }

        if(controlPointsChanged) {
            generator.setPatchControlPoints(vfControlPoint,
                                            vfUDimension,
                                            vfVDimension);
            controlPointsChanged = false;
        }

        if(weightsChanged) {
            generator.setPatchWeights(vfWeight, vfUDimension, vfVDimension);
            weightsChanged = false;
        }

        generator.generate(geometryData);
        return true;
    }
}
