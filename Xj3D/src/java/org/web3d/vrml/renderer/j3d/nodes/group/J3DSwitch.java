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

package org.web3d.vrml.renderer.j3d.nodes.group;

// Standard imports
import javax.media.j3d.*;

import java.util.HashMap;
import java.util.ArrayList;

import javax.vecmath.Point3f;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DGroupingNode;
import org.web3d.vrml.renderer.CRProtoInstance;

/**
 * Java3D implementation of a switch node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.19 $
 */
public class J3DSwitch extends J3DGroupingNode {

    /** Index of the whichChoice field */
    private static final int FIELD_WHICH_CHOICE = LAST_GROUP_INDEX + 1;

    /** The last field index used by this class */
    private static final int LAST_SWITCH_INDEX = FIELD_WHICH_CHOICE;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_SWITCH_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** The whichChoice field */
    private int vfWhichChoice = -1;

    /** This is where we keep the switch behaviors */
    private Switch implSwitch;

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        nodeFields = new int[] { FIELD_CHILDREN, FIELD_METADATA };

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
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxSize");
        fieldDecl[FIELD_WHICH_CHOICE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "whichChoice");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);
        fieldMap.put("choice", idx);
        fieldMap.put("set_choice", idx);
        fieldMap.put("choice_changed", idx);

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));

        idx = new Integer(FIELD_WHICH_CHOICE);
        fieldMap.put("whichChoice", idx);
        fieldMap.put("set_whichChoice", idx);
        fieldMap.put("whichChoice_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public J3DSwitch() {
        super("Switch");

        hasChanged = new boolean[NUM_FIELDS];

        implSwitch = new Switch(Switch.CHILD_MASK);
        implGroup = implSwitch;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public J3DSwitch(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("whichChoice");
            VRMLFieldData field = node.getFieldValue(index);

            setWhichChoice(field.intValue);
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
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

        implGroup.setCapability(BranchGroup.ALLOW_DETACH);
        implGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        implGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        implSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        implSwitch.setCapability(Switch.ALLOW_CHILDREN_WRITE);
        implSwitch.setCapability(Switch.ALLOW_CHILDREN_EXTEND);
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
        if(index < 0  || index > LAST_SWITCH_INDEX)
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
            case FIELD_WHICH_CHOICE:
                fieldData.clear();
                fieldData.intValue = vfWhichChoice;
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
                case FIELD_WHICH_CHOICE:
                    destNode.setValue(destIndex, vfWhichChoice);
                    break;
                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid fieldValue: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_WHICH_CHOICE :
                setWhichChoice(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Internal methods required by the class
    //----------------------------------------------------------

    /**
     * Set which child to display.  -1 = NONE.
     *
     * @param newChoice The new child to display
     * @throws InvalidFieldValueException The value is not a valid choice
     */
    private void setWhichChoice(int newChoice)
        throws InvalidFieldValueException {

        if(newChoice < -1)
            throw new InvalidFieldValueException("whichChoice < -1");

        vfWhichChoice = newChoice;

        if (vfWhichChoice == -1)
            implSwitch.setWhichChild(Switch.CHILD_NONE);
        else {
            int num_kids = vfChildren.size();
            J3DVRMLNode kid;
            int real_idx = 0;

            for(int i = 0; i < num_kids; i++) {
                kid = (J3DVRMLNode)vfChildren.get(i);

                if(kid instanceof CRProtoInstance &&
                   ((CRProtoInstance)kid).getComplete() == false)
                    continue;

                if(!(kid instanceof VRMLChildNodeType) &&
                    !(kid instanceof VRMLProtoInstance)) {
                    throw new InvalidNodeTypeException(kid.getVRMLNodeName(),
                                                "Groups can only contain ChildNodes");
                }

                Node j3d_node = (Node) kid.getSceneGraphObject();

                if(i == vfWhichChoice && j3d_node == null) {
                    real_idx = Switch.CHILD_NONE;
                    break;
                }

                if (j3d_node == null)
                    continue;

                if (i == vfWhichChoice)
                    break;

                real_idx++;
            }

            implSwitch.setWhichChild(real_idx);
        }

        if (!inSetup) {
            hasChanged[FIELD_WHICH_CHOICE] = true;
            fireFieldChanged(FIELD_WHICH_CHOICE);
        }
    }
}
