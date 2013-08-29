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

package org.web3d.vrml.renderer.common.nodes.hanim;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

import org.j3d.geom.hanim.HAnimDisplacer;
import org.j3d.geom.hanim.HAnimFactory;
import org.j3d.geom.hanim.HAnimObject;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLCoordinateNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLHAnimNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.renderer.common.nodes.BaseGeometricPropertyNode;

/**
 * Common base implementation for the field handling of a HAnimDisplacer node.
 * <p>
 *
 * The node is defined as follows:
 *
 * <pre>
 *  HAnimDisplacer : X3DGeometricPropertyNode {
 *    MFInt32  [in,out] coordIndex    []   [0,inf) or -1
 *    MFVec3f  [in,out] displacements []   [X3DCoordinateNode]
 *    SFNode   [in,out] metadata      NULL [X3DMetadataObject]
 *    SFString [in,out] name          ""
 *  }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public abstract class BaseHAnimDisplacer extends BaseGeometricPropertyNode
    implements VRMLHAnimNodeType {

    /** Field Index for coordIndex */
    protected static final int FIELD_COORD_INDEX = LAST_NODE_INDEX + 1;

    /** Field Index for displacemnts */
    protected static final int FIELD_DISPLACEMENTS = LAST_NODE_INDEX + 2;

    /** Field Index for name */
    protected static final int FIELD_NAME = LAST_NODE_INDEX + 3;


    /** The last field index used by this class */
    protected static final int LAST_DISPLACER_INDEX = FIELD_NAME;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_DISPLACER_INDEX + 1;

    /** Message for when the proto is not a Appearance */
    protected static final String DISPLACEMENT_PROTO_MSG =
        "Proto does not describe a Coordinate object";

    /** Message for when the node in setValue() is not a Appearance */
    protected static final String DISPLACEMENT_NODE_MSG =
        "Node does not describe a Coordinate object";


    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** exposedField MFInt32 coordIndex */
    protected int[] vfCoordIndex;

    /** actual length of vfCoordIndex */
    protected int numCoordIndex;

    /** List of coordinate nodes this displacer manages */
    protected float[] vfDisplacements;

    /** actual length of vfDisplacements */
    protected int numDisplacements;

    /** The name of this displacer */
    protected String vfName;

    /** The generic internal representation of the node */
    protected HAnimDisplacer hanimImpl;

    /** Factory used to generate the implementation node */
    protected HAnimFactory hanimFactory;


    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA, FIELD_DISPLACEMENTS };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_COORD_INDEX] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFInt32",
                                     "coordIndex");

        fieldDecl[FIELD_DISPLACEMENTS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "displacements");
        fieldDecl[FIELD_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "name");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_COORD_INDEX);
        fieldMap.put("coordIndex", idx);
        fieldMap.put("set_coordIndex", idx);
        fieldMap.put("coordIndex_changed", idx);

        idx = new Integer(FIELD_DISPLACEMENTS);
        fieldMap.put("displacements", idx);
        fieldMap.put("set_displacements", idx);
        fieldMap.put("displacements_changed", idx);

        idx = new Integer(FIELD_NAME);
        fieldMap.put("name", idx);
        fieldMap.put("set_name", idx);
        fieldMap.put("name_changed", idx);
    }

    /**
     * Constructor to create a default instance of this node.
     */
    public BaseHAnimDisplacer() {
        super("HAnimDisplacer");

        vfCoordIndex = FieldConstants.EMPTY_MFINT32;
        vfDisplacements = FieldConstants.EMPTY_MFFLOAT;
        vfName = null;

        hasChanged = new boolean[LAST_DISPLACER_INDEX + 1];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseHAnimDisplacer(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("coordIndex");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfCoordIndex = new int[field.numElements];
                System.arraycopy(field.intArrayValue,
                                 0,
                                 vfCoordIndex,
                                 0,
                                 field.numElements);

                numCoordIndex = field.numElements;
            }

            index = node.getFieldIndex("name");
            field = node.getFieldValue(index);
            vfName = field.stringValue;

            index = node.getFieldIndex("displacements");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfDisplacements = new float[field.numElements * 3];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfDisplacements,
                                 0,
                                 field.numElements * 3);

                numDisplacements = field.numElements * 3;
            }

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLHAnimNodeType
    //----------------------------------------------------------

    /**
     * Set the current node factory to use. If this is set again, replace the
     * current implementation node with a new one from this new instance. This
     * may be needed at times when the user makes a change that forces the old
     * way to be incompatible and thus needing a different implementation.
     *
     * @param fac The new factory instance to use
     */
    public void setHAnimFactory(HAnimFactory fac) {
        hanimFactory = fac;

        hanimImpl = fac.createDisplacer();
        hanimImpl.setName(vfName);
        hanimImpl.setCoordIndex(vfCoordIndex, numCoordIndex);
        hanimImpl.setDisplacements(vfDisplacements, numDisplacements);
//        hanimImpl.setWeight(vfWeight);
    }

    /**
     * Get the HAnim implementation node. Since the HAnim class instance is not
     * the same as the basic geometry instance of the particular rendering API, we
     * need to fetch this higher-level construct so that the scene graph can be
     * constructed.
     *
     * @return The HAnimObject instance for this node
     */
    public HAnimObject getHAnimObject() {
        return hanimImpl;
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
        if (index < 0  || index > LAST_DISPLACER_INDEX)
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
        return TypeConstants.GeometricPropertyNodeType;
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
            case FIELD_COORD_INDEX:
                fieldData.clear();
                fieldData.intArrayValue = vfCoordIndex;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = numCoordIndex;
                break;

            case FIELD_DISPLACEMENTS:
                fieldData.clear();
                fieldData.floatArrayValue = vfDisplacements;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numDisplacements / 3;
                break;

            case FIELD_NAME:
                fieldData.clear();
                fieldData.stringValue = vfName;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                fieldData.numElements = 1;
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
                case FIELD_COORD_INDEX:
                    destNode.setValue(destIndex, vfCoordIndex, numCoordIndex);
                    break;

                case FIELD_DISPLACEMENTS:
                    destNode.setValue(destIndex,
                                      vfDisplacements,
                                      numDisplacements);
                    break;

                case FIELD_NAME:
                    destNode.setValue(destIndex, vfName);
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
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFInt32, SFImage, and MFImage field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_COORD_INDEX:
                setCoordIndex(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, MFVec3f etc field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_DISPLACEMENTS:
                setDisplacements(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_NAME:
                vfName = value;

                if(!inSetup) {
                    hanimImpl.setName(vfName);
                    hasChanged[FIELD_NAME] = true;
                    fireFieldChanged(FIELD_NAME);
                }
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Set a new value for the displacements field. Derived classes may override
     * as needed, but should call this one as well.
     *
     * @param disp New value for the field
     * @param num The number of valid values to use from the array
     * @throws InvalidFieldValueException An index value was < -1
     */
    protected void setDisplacements(float[] disp, int num)
        throws InvalidFieldValueException {

        if(num > vfDisplacements.length)
            vfDisplacements = new float[num];

        numDisplacements = num;
        System.arraycopy(disp, 0, vfDisplacements, 0, numDisplacements);

        // We have to send the new value here because it will be the
        // correct length.
        if(!inSetup) {
            hasChanged[FIELD_DISPLACEMENTS] = true;
            fireFieldChanged(FIELD_DISPLACEMENTS);
        }
    }

    /**
     * Set a new value for the coordIndex field. Derived classes may override
     * as needed, but should call this one as well.
     *
     * @param indices New value for the field
     * @param num The number of valid values to use from the array
     * @throws InvalidFieldValueException An index value was < -1
     */
    protected void setCoordIndex(int[] indices, int num)
        throws InvalidFieldValueException {

        // Check for valid values
        for(int i = 0; i < num; i++) {
            if(indices[i] < -1)
                throw new InvalidFieldValueException(
                    "The value at index " + i + " is less than -1");

        }

        if(vfCoordIndex == null || num > vfCoordIndex.length)
            vfCoordIndex = new int[num];

        numCoordIndex = num;
        System.arraycopy(indices, 0, vfCoordIndex, 0, numCoordIndex);

        // We have to send the new value here because it will be the
        // correct length.
        if(!inSetup) {
            hasChanged[FIELD_COORD_INDEX] = true;
            fireFieldChanged(FIELD_COORD_INDEX);
        }
    }
}
