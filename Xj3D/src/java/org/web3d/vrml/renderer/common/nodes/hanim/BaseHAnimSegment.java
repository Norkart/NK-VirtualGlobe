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
import java.util.HashMap;
import java.util.ArrayList;

import org.j3d.geom.hanim.HAnimDisplacer;
import org.j3d.geom.hanim.HAnimFactory;
import org.j3d.geom.hanim.HAnimObject;
import org.j3d.geom.hanim.HAnimSegment;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;

/**
 * Common base implementation for the field handling of a HAnimSegment node.
 * <p>
 *
 * The node is defined as follows:
 * <pre>
 *  HAnimSegment : X3DGroupingNode {
 *    MFNode   [in]     addChildren                          [X3DChildNode]
 *    MFNode   [in]     removeChildren                       [X3DChildNode]
 *    SFVec3f  [in,out] centerOfMass     0 0 0               (-inf,inf)
 *    MFNode   [in,out] children         []                  [X3DChildNode]
 *    SFNode   [in,out] coord            NULL                [X3DCoordinateNode]
 *    MFNode   [in,out] displacers       []                  [HAnimDisplacer]
 *    SFFloat  [in,out] mass             0                   (0,inf)
 *    SFNode   [in,out] metadata         NULL                [X3DMetadataObject]
 *    MFFloat  [in,out] momentsOfInertia [0 0 0 0 0 0 0 0 0] [0,inf)
 *    SFString [in,out] name             ""
 *    SFVec3f  []       bboxCenter       0 0 0               (-inf,inf)
 *    SFVec3f  []       bboxSize         -1 -1 -1            [0,inf) or -1 -1 -1
 *  }
 * </pre>
 *
 * This class does not pass the children field values along to the render
 * implementation node because that needs to be a renderer-specific object.
 * Derived classes need to handle this in the setHAnimFactory() call and
 * any time the children are set.
 *
 * @author Justin Couch
 * @version $Revision: 2.4 $
 */
public abstract class BaseHAnimSegment extends BaseGroupingNode
    implements VRMLHAnimNodeType {

    /** Index of the field centerOfMass */
    protected static final int FIELD_CENTER_OF_MASS = LAST_GROUP_INDEX + 1;

    /** Index of the field coord */
    protected static final int FIELD_COORD = LAST_GROUP_INDEX + 2;

    /** Index of the field displacers */
    protected static final int FIELD_DISPLACERS = LAST_GROUP_INDEX + 3;

    /** Index of the field mass */
    protected static final int FIELD_MASS = LAST_GROUP_INDEX + 4;

    /** Index of the field momentsOfIntertia */
    protected static final int FIELD_MOMENTS_OF_INERTIA = LAST_GROUP_INDEX + 5;

    /** Index of the field name */
    protected static final int FIELD_NAME = LAST_GROUP_INDEX + 6;

    /** Last index declared for these fields */
    protected static final int LAST_DISPLACER_INDEX = FIELD_NAME;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_DISPLACER_INDEX + 1;

    /** Message when the mass value is <= zero */
    private static final String MASS_ERR_MSG =
        "Mass must be greater than zero";

    /** Message for when the proto is not a Coordinate */
    protected static final String COORD_PROTO_MSG =
        "Proto does not describe a Coordinate object";

    /** Message for when the node in setValue() is not a Coordinate */
    protected static final String COORD_NODE_MSG =
        "Node does not describe a Coordinate object";

    /** Message for when the proto is not a HAnimDisplacer */
    protected static final String DISPLACEMENT_PROTO_MSG =
        "Proto does not describe a HAnimDisplacer object";

    /** Message for when the node in setValue() is not a HAnimDisplacer */
    protected static final String DISPLACEMENT_NODE_MSG =
        "Node does not describe a HAnimDisplacer object";


    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** exposedField SFVec3f centerOfMass */
    protected float[] vfCenterOfMass;

    /** exposedField SFNode coord */
    protected VRMLCoordinateNodeType vfCoord;

    /** proto version of coord */
    protected VRMLProtoInstance pCoord;

    /** exposedField MFNode displacers */
    protected ArrayList vfDisplacers;

    /** exposedField SFFloat mass */
    protected float vfMass;

    /** exposedField MFFloat momentsOfIntertia */
    protected float[] vfMomentsOfInertia;

    /** Number of valid values in the moments of inertia field */
    protected int numMomentsOfInertia;

    /** Internal scratch var for dealing with added/removed children */
    private VRMLNodeType[] nodeTmp;

    /** exposedField SFString name */
    protected String vfName;

    /** The generic internal representation of the node */
    protected HAnimSegment hanimImpl;

    /** Factory used to generate the implementation node */
    protected HAnimFactory hanimFactory;


    // Static constructor
    static {
        nodeFields = new int[] {
            FIELD_CHILDREN,
            FIELD_METADATA,
            FIELD_COORD,
            FIELD_DISPLACERS
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "MFNode",
                                 "children");
        fieldDecl[FIELD_ADDCHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "MFNode",
                                 "addChildren");
        fieldDecl[FIELD_REMOVECHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "MFNode",
                                 "removeChildren");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                  "SFVec3f",
                                 "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFVec3f",
                                 "bboxSize");
        fieldDecl[FIELD_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "name");
        fieldDecl[FIELD_COORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "coord");
        fieldDecl[FIELD_CENTER_OF_MASS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "centerOfMass");
        fieldDecl[FIELD_DISPLACERS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "displacers");
        fieldDecl[FIELD_MASS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "mass");
        fieldDecl[FIELD_MOMENTS_OF_INERTIA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "momentsOfInertia");




        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        idx = new Integer(FIELD_ADDCHILDREN);
        fieldMap.put("addChildren", idx);
        fieldMap.put("set_addChildren", idx);

        idx = new Integer(FIELD_REMOVECHILDREN);
        fieldMap.put("removeChildren", idx);
        fieldMap.put("set_removeChildren", idx);

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));

        idx = new Integer(FIELD_CENTER_OF_MASS);
        fieldMap.put("centerOfMass", idx);
        fieldMap.put("set_centerOfMass", idx);
        fieldMap.put("centerOfMass_changed", idx);

        idx = new Integer(FIELD_COORD);
        fieldMap.put("coord", idx);
        fieldMap.put("set_coord", idx);
        fieldMap.put("coord_changed", idx);

        idx = new Integer(FIELD_DISPLACERS);
        fieldMap.put("displacers", idx);
        fieldMap.put("set_displacers", idx);
        fieldMap.put("displacers_changed", idx);

        idx = new Integer(FIELD_MASS);
        fieldMap.put("mass", idx);
        fieldMap.put("set_mass", idx);
        fieldMap.put("mass_changed", idx);

        idx = new Integer(FIELD_MOMENTS_OF_INERTIA);
        fieldMap.put("momentsOfInertia", idx);
        fieldMap.put("set_momentsOfInertia", idx);
        fieldMap.put("momentsOfInertia_changed", idx);

        idx = new Integer(FIELD_NAME);
        fieldMap.put("name", idx);
        fieldMap.put("set_name", idx);
        fieldMap.put("name_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseHAnimSegment() {
        super("HAnimSegment");

        vfCenterOfMass = new float[3];
        vfDisplacers = new ArrayList();
        vfMass = 0;
        vfMomentsOfInertia = new float[9]; // all zeroes as per spec
        numMomentsOfInertia = 9;

        hasChanged = new boolean[LAST_GROUP_INDEX + 1];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseHAnimSegment(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("centerOfMass");
            VRMLFieldData field = node.getFieldValue(index);

            vfCenterOfMass[0] = field.floatArrayValue[0];
            vfCenterOfMass[1] = field.floatArrayValue[1];
            vfCenterOfMass[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("mass");
            field = node.getFieldValue(index);
            vfMass = field.floatValue;

            index = node.getFieldIndex("momentsOfInertia");
            field = node.getFieldValue(index);

            if(field.numElements > vfMomentsOfInertia.length)
                vfMomentsOfInertia = new float[field.numElements];

            System.arraycopy(field.floatArrayValue,
                             0,
                             vfMomentsOfInertia,
                             0,
                             field.numElements);
            numMomentsOfInertia = field.numElements;

            index = node.getFieldIndex("name");
            field = node.getFieldValue(index);
            vfName = field.stringValue;
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

        // Loop through all the displacers and set their factory first.
        int num_displacers = vfDisplacers.size();
        for(int i = 0; i < num_displacers; i++)
        {
            VRMLHAnimNodeType disp = (VRMLHAnimNodeType)vfDisplacers.get(i);
            disp.setHAnimFactory(fac);
        }

        hanimImpl = fac.createSegment();

        hanimImpl.setName(vfName);
        hanimImpl.setBboxCenter(vfBboxCenter);
        hanimImpl.setBboxSize(vfBboxSize);
        hanimImpl.setMomentsOfInertia(vfMomentsOfInertia, numMomentsOfInertia);
        hanimImpl.setCenterOfMass(vfCenterOfMass);
        hanimImpl.setMass(vfMass);

        HAnimDisplacer[] disp_list = new HAnimDisplacer[num_displacers];

        for(int i = 0; i < num_displacers; i++)
        {
            VRMLHAnimNodeType disp = (VRMLHAnimNodeType)vfDisplacers.get(i);
            disp_list[i] = (HAnimDisplacer)disp.getHAnimObject();
        }

        hanimImpl.setDisplacers(disp_list, num_displacers);
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
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(pCoord != null)
            pCoord.setupFinished();

        if(vfCoord != null)
            vfCoord.setupFinished();

        int size = vfDisplacers.size();
        for(int i = 0; i < size; i++) {
            VRMLNodeType n = (VRMLNodeType)vfDisplacers.get(i);
            n.setupFinished();
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
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
        if(index < 0  || index > LAST_DISPLACER_INDEX)
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

        fieldData.clear();

        switch(index) {
            case FIELD_CENTER_OF_MASS:
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfCenterOfMass;
                break;

            case FIELD_COORD:
                if(pCoord != null)
                    fieldData.nodeValue = pCoord;
                else
                    fieldData.nodeValue = vfCoord;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_DISPLACERS:
                fieldData.numElements = vfDisplacers.size();

                if((nodeTmp == null) || nodeTmp.length < fieldData.numElements)
                    nodeTmp = new VRMLNodeType[fieldData.numElements];

                vfDisplacers.toArray(nodeTmp);
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                break;

            case FIELD_MASS:
                fieldData.floatValue = vfMass;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MOMENTS_OF_INERTIA:
                fieldData.numElements = numMomentsOfInertia;
                fieldData.floatArrayValue = vfMomentsOfInertia;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_NAME:
                fieldData.stringValue = vfName;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
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
                case FIELD_CENTER_OF_MASS:
                    destNode.setValue(destIndex, vfCenterOfMass, 3);
                    break;

                case FIELD_MOMENTS_OF_INERTIA:
                    destNode.setValue(destIndex,
                                      vfMomentsOfInertia,
                                      numMomentsOfInertia);
                    break;

                case FIELD_COORD:
                    if(pCoord != null)
                        destNode.setValue(destIndex, pCoord);
                    else
                        destNode.setValue(destIndex, vfCoord);
                    break;

                case FIELD_DISPLACERS:
                    int size = vfDisplacers.size();

                    if((nodeTmp == null) || nodeTmp.length < size)
                        nodeTmp = new VRMLNodeType[size];
                    vfDisplacers.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, size);
                    break;

                case FIELD_MASS:
                    destNode.setValue(destIndex, vfMass);
                    break;

                case FIELD_NAME:
                    destNode.setValue(destIndex, vfName);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseHAnimSegment.sendRoute: No field!" + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseHAnimSegment.sendRoute: Invalid fieldValue: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a single float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value is not in the acceptable
     *   range for the given field
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_MASS:
                if(value < 0)
                    throw new InvalidFieldValueException(MASS_ERR_MSG);

                vfMass = value;

                if(!inSetup) {
                    hasChanged[FIELD_MASS] = true;
                    fireFieldChanged(FIELD_MASS);
                }
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
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value is not in the acceptable
     *   range for the given field
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_CENTER_OF_MASS:
                setCenterOfMass(value);
                break;

            case FIELD_MOMENTS_OF_INERTIA:
                setMomentsOfInertia(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a single string.
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

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_DISPLACERS:
                if(!inSetup)
                    clearDisplacers();

                if(child != null)
                    addDisplacerNode(child);

                if(!inSetup) {
                    hasChanged[FIELD_DISPLACERS] = true;
                    fireFieldChanged(FIELD_DISPLACERS);
                }
                break;

            case FIELD_COORD:
                setCoord(child);
                break;

            default:
                super.setValue(index, child);
        }

    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_DISPLACERS:
                if(!inSetup)
                    clearDisplacers();

                for(int i = 0; i < numValid; i++ )
                    addDisplacerNode(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_DISPLACERS] = true;
                    fireFieldChanged(FIELD_DISPLACERS);
                }
                break;

            default:
                super.setValue(index, children, numValid);
        }

    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the center of mass field value.
     *
     * @param value The new value to set
     */
    protected void setCenterOfMass(float[] value) {
        vfCenterOfMass[0] = value[0];
        vfCenterOfMass[1] = value[1];
        vfCenterOfMass[2] = value[2];

        if(!inSetup) {
            hasChanged[FIELD_CENTER_OF_MASS] = true;
            fireFieldChanged(FIELD_CENTER_OF_MASS);
        }
    }

    /**
     * Set the new values for momentsOfInertia. All values are required to be
     * [0, inf).
     *
     * @param value The new value of the field to use
     * @param numValues The number of valid values to use from the array
     * @throws IllegalFieldValueException A value was less than zero
     */
    protected void setMomentsOfInertia(float[] value, int numValues)
        throws InvalidFieldValueException {

        for(int i = 0; i < numValues; i++) {
            if(value[i] < 0)
                throw new InvalidFieldValueException(
                    "momentsOfInertia value at index " + i +
                    " is less than zero");
        }

        if(vfMomentsOfInertia.length < numValues)
            vfMomentsOfInertia = new float[numValues];

        System.arraycopy(value, 0, vfMomentsOfInertia, 0, numValues);
        numMomentsOfInertia = numValues;
    }

    /**
     * Set node content as replacement for the skinCoord field.
     *
     * @param coord The new coordinate node.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setCoord(VRMLNodeType coord)
        throws InvalidFieldValueException {

        VRMLNodeType old_node;

        if(pCoord != null)
            old_node = pCoord;
        else
            old_node = vfCoord;

        if(coord instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)coord).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLCoordinateNodeType))
                throw new InvalidFieldValueException(COORD_PROTO_MSG);

            pCoord = (VRMLProtoInstance)coord;
            vfCoord = (VRMLCoordinateNodeType)impl;

        } else if((coord != null) &&
                  !(coord instanceof VRMLCoordinateNodeType)) {
            throw new InvalidFieldValueException(COORD_NODE_MSG);
        } else {
            pCoord = null;
            vfCoord = (VRMLCoordinateNodeType)coord;
        }

        if(coord != null)
            updateRefs(coord, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(coord != null)
                stateManager.registerAddedNode(coord);

            hasChanged[FIELD_COORD] = true;
            fireFieldChanged(FIELD_COORD);
        }
    }

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearDisplacers() {
        int num_kids = vfDisplacers.size();

        if((nodeTmp == null) || nodeTmp.length < num_kids)
            nodeTmp = new VRMLNodeType[num_kids];

        vfDisplacers.toArray(nodeTmp);

        for(int i = 0; i < num_kids; i++)
            updateRefs(nodeTmp[i], false);

        if(num_kids > 0)
            stateManager.registerRemovedNodes(nodeTmp);

        vfDisplacers.clear();
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addDisplacerNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof BaseHAnimDisplacer))
                throw new InvalidFieldValueException(DISPLACEMENT_PROTO_MSG);

        } else if(node != null && !(node instanceof BaseHAnimDisplacer)) {
            throw new InvalidFieldValueException(DISPLACEMENT_NODE_MSG);
        }

        vfDisplacers.add(node);

        if(node != null) {
            updateRefs(node, true);

            if(!inSetup)
                stateManager.registerAddedNode(node);
        }
    }
}
