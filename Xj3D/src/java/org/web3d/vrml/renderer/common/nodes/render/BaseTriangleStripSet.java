/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.render;

// Standard imports
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseTriangleGeometryNode;

/**
 * An abstract implementation of the TriangleStripSet node.
 * <p>
 *
 * A stripSet adds the stripCount field over the normal TriangleSet.
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.12 $
 */
public abstract class BaseTriangleStripSet extends BaseTriangleGeometryNode {

    /** Index of the stripCount field */
    protected static final int FIELD_STRIP_COUNT = LAST_GEOMETRY_INDEX + 1;

    /** Last index in this node */
    protected static final int LAST_TRIANGLE_INDEX = FIELD_STRIP_COUNT;

    /** The number of fields in this node */
    private static final int NUM_FIELDS = LAST_TRIANGLE_INDEX + 1;

    /** One of the lineCount values was < 2 */
    private static final String BAD_COUNT_MSG =
        "Strip count values less that 3 are not permitted. Offending value " +
        "found at index: ";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** field MFInt32 stripCount */
    protected int[] vfStripCount;

    /** Number of valid values in the strip count field */
    protected int numStripCount;

    /**
     * Static constructor sets up the field declarations
     */
    static {
        nodeFields = new int[] {
            FIELD_COORD,
            FIELD_NORMAL,
            FIELD_TEXCOORD,
            FIELD_COLOR,
            FIELD_FOG_COORD,
            FIELD_ATTRIBS,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "color");
        fieldDecl[FIELD_COORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "coord");
        fieldDecl[FIELD_NORMAL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "normal");
        fieldDecl[FIELD_TEXCOORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "texCoord");
        fieldDecl[FIELD_FOG_COORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "fogCoord");
        fieldDecl[FIELD_ATTRIBS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "attrib");
        fieldDecl[FIELD_SOLID] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "solid");
        fieldDecl[FIELD_CCW] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "ccw");
        fieldDecl[FIELD_COLORPERVERTEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "colorPerVertex");
        fieldDecl[FIELD_NORMALPERVERTEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "normalPerVertex");
        fieldDecl[FIELD_STRIP_COUNT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFInt32",
                                     "stripCount");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_COLOR);
        fieldMap.put("color", idx);
        fieldMap.put("set_color", idx);
        fieldMap.put("color_changed", idx);

        idx = new Integer(FIELD_COORD);
        fieldMap.put("coord", idx);
        fieldMap.put("set_coord", idx);
        fieldMap.put("coord_changed", idx);

        idx = new Integer(FIELD_NORMAL);
        fieldMap.put("normal", idx);
        fieldMap.put("set_normal", idx);
        fieldMap.put("normal_changed", idx);

        idx = new Integer(FIELD_TEXCOORD);
        fieldMap.put("texCoord", idx);
        fieldMap.put("set_texCoord", idx);
        fieldMap.put("texCoord_changed", idx);

        idx = new Integer(FIELD_FOG_COORD);
        fieldMap.put("fogCoord", idx);
        fieldMap.put("set_fogCoord", idx);
        fieldMap.put("fogCoord_changed", idx);

        idx = new Integer(FIELD_ATTRIBS);
        fieldMap.put("attrib", idx);
        fieldMap.put("set_attrib", idx);
        fieldMap.put("attrib_changed", idx);

        idx = new Integer(FIELD_STRIP_COUNT);
        fieldMap.put("stripCount", idx);
        fieldMap.put("set_stripCount", idx);
        fieldMap.put("stripCount_changed", idx);

        fieldMap.put("solid",new Integer(FIELD_SOLID));
        fieldMap.put("ccw",new Integer(FIELD_CCW));
        fieldMap.put("colorPerVertex",new Integer(FIELD_COLORPERVERTEX));
        fieldMap.put("normalPerVertex", new Integer(FIELD_NORMALPERVERTEX));
    }

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     *
     * @param name The name of the type of node
     */
    protected BaseTriangleStripSet() {
        super("TriangleStripSet");

        vfStripCount = FieldConstants.EMPTY_MFINT32;
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
    protected BaseTriangleStripSet(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLComponentGeometryNodeType)node);

        try {
            int index = node.getFieldIndex("stripCount");
            VRMLFieldData field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfStripCount = new int[field.numElements];
                System.arraycopy(field.intArrayValue,
                                 0,
                                 vfStripCount,
                                 0,
                                 field.numElements);

                numStripCount = field.numElements;
            }
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
        if(index < 0  || index > LAST_TRIANGLE_INDEX)
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
            case FIELD_STRIP_COUNT:
                destNode.setValue(destIndex, vfStripCount, numStripCount);
                break;

            default:
                super.sendRoute(time, srcIndex, destNode, destIndex);
        }
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
            case FIELD_STRIP_COUNT:
                fieldData.clear();
                fieldData.intArrayValue = vfStripCount;
                fieldData.numElements = numStripCount;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
        
        switch(index) {
            case FIELD_STRIP_COUNT:
                setStripCount(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the value of the stripCount field.
     *
     * @param counts The list of counts provided
     * @param numValid The number of valid values to copy from the array
     * @throw InvalidFieldValueException One or more values were < 2
     */
    protected void setStripCount(int[] counts, int numValid)
        throws InvalidFieldValueException {

        for(int i = 0; i < numValid; i++) {
            if(counts[i] < 3)
                throw new InvalidFieldValueException(BAD_COUNT_MSG + i);
        }

        if(vfStripCount.length < numValid)
            vfStripCount = new int[numValid];

        System.arraycopy(counts, 0, vfStripCount, 0, numValid);
        numStripCount = numValid;

        if(!inSetup) {
            hasChanged[FIELD_STRIP_COUNT] = true;
            fireFieldChanged(FIELD_STRIP_COUNT);

            buildImpl();
        }
    }
}
