/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geom2d;

// External imports
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * An abstract implementation of the Polypoint2D.
 * <p>
 *
 *
 * Spec notes:
 *  Mentions solid but no solid field
 *  What to do when only 1 vertex is provided.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public abstract class BaseTriangleSet2D extends AbstractNode
   implements VRMLGeometryNodeType {

    /** Index of the solid field */
    protected static final int FIELD_SOLID = LAST_NODE_INDEX + 1;

    /** Index of the point field */
    protected static final int FIELD_VERTICES = LAST_NODE_INDEX + 2;

    /** Last field declaration in this node */
    private static final int LAST_TRISET2D_INDEX = FIELD_VERTICES;

    /** The number of fields in this node */
    private static final int NUM_FIELDS = LAST_TRISET2D_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** The value of the point field */
    protected float[] vfVertices;

    /** Number of valid values in the point field */
    protected int numVertices;

    /** The valid of the solid field */
    protected boolean vfSolid;

    /**
     * Static constructor sets up the field declarations
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_VERTICES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFVec2f",
                                     "vertices");
        fieldDecl[FIELD_SOLID] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "solid");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_VERTICES);
        fieldMap.put("vertices", idx);
        fieldMap.put("set_vertices", idx);
        fieldMap.put("vertices_changed", idx);

        fieldMap.put("solid", new Integer(FIELD_SOLID));
    }

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     *
     * @param name The name of the type of node
     */
    protected BaseTriangleSet2D() {
        super("TriangleSet2D");

        vfVertices = FieldConstants.EMPTY_MFVEC2F;
        vfSolid = false;

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseTriangleSet2D(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("point");
            VRMLFieldData field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfVertices = new float[field.numElements * 2];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfVertices,
                                 0,
                                 field.numElements * 2);
                numVertices = field.numElements * 2;
            }

            index = node.getFieldIndex("solid");
            field = node.getFieldValue(index);
            vfSolid = field.booleanValue;

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

        switch(index) {
            case FIELD_VERTICES:
                fieldData.clear();
                fieldData.floatArrayValue = vfVertices;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numVertices / 2;
                break;

            case FIELD_SOLID:
                fieldData.clear();
                fieldData.booleanValue = vfSolid;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
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
        if(index < 0  || index > LAST_TRISET2D_INDEX)
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

        switch(srcIndex) {
            case FIELD_VERTICES:
                destNode.setValue(destIndex, vfVertices, numVertices);
                break;

            default:
                super.sendRoute(time, srcIndex, destNode, destIndex);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_SOLID:
                if(!inSetup)
                    throw new InvalidFieldAccessException(
                        INIT_ONLY_WRITE_MSG + " solid");
                else
                    vfSolid = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFVec2f field types.
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
            case FIELD_VERTICES:
                setVertices(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the value of the vertices field.
     *
     * @param coords The list of coordinates to use
     * @param numValid The number of valid values to copy from the array
     * @throw InvalidFieldValueException One or more values were < 2
     */
    protected void setVertices(float[] coords, int numValid)
        throws InvalidFieldValueException {

        if(vfVertices.length < numValid)
            vfVertices = new float[numValid];

        System.arraycopy(coords, 0, vfVertices, 0, numValid);
        numVertices = numValid;

        if(!inSetup) {
            hasChanged[FIELD_VERTICES] = true;
            fireFieldChanged(FIELD_VERTICES);
        }
    }
}
