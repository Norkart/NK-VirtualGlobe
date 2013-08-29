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

package org.web3d.vrml.renderer.common.nodes.eventutils;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of a TimeTrigger Node.
 * <p>
 *
 * The implementation uses the standard VRML time clock to send and retrieve
 * time information. As an efficiency measure, if the time sensor is disabled
 * it will remove itself as a listener to the global clock. When it becomes
 * re-enabled that listener will be added back again.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class BaseTimeTrigger extends AbstractNode
    implements VRMLTimeDependentNodeType {

    /** Secondary types of this node */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.TimeDependentNodeType };

    // Field index constants

    /** The field index for set_boolean */
    protected static final int FIELD_SET_BOOLEAN = LAST_NODE_INDEX + 1;

    /** The field index for triggerTime */
    protected static final int FIELD_TRIGGER_TIME = LAST_NODE_INDEX + 2;

    /** The last field index used by this class */
    protected static final int LAST_TIME_TRIGGER_INDEX = FIELD_TRIGGER_TIME;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_TIME_TRIGGER_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values
    /** The last TriggerTime value sent, or -1 initially */
    private double vfTriggerTime;

    private VRMLClock vrmlClock;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_SET_BOOLEAN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFBool",
                                     "set_boolean");

        fieldDecl[FIELD_TRIGGER_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "triggerTime");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_SET_BOOLEAN);
        fieldMap.put("set_boolean", idx);

        idx = new Integer(FIELD_TRIGGER_TIME);
        fieldMap.put("triggerTime", idx);
    }

    /**
     * Construct a new time sensor object
     */
    public BaseTimeTrigger() {
        super("TimeTrigger");

        hasChanged = new boolean[NUM_FIELDS];
        vfTriggerTime = -1;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseTimeTrigger(VRMLNodeType node) {
        this();

        checkNodeType(node);
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLTimeDependentNodeType interface.
    //-------------------------------------------------------------

    /**
     * Set the vrmlClock that this time dependent node will be running with.
     * The vrmlClock provides all the information and listeners for keeping track
     * of time. If we are enabled at the time that this method is called we
     * automatically register the listener. Then, all the events that need
     * to be generated will be handled at the next vrmlClock tick we get issued.
     *
     * @param clk The vrmlClock to use for this node
     */
    public void setVRMLClock(VRMLClock clk) {

        vrmlClock = clk;
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
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
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
            case FIELD_TRIGGER_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfTriggerTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
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
        if(index < 0  || index > LAST_TIME_TRIGGER_INDEX)
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
        return TypeConstants.ChildNodeType;
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
                case FIELD_TRIGGER_TIME:
                    destNode.setValue(destIndex, vfTriggerTime);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseTimeTrigger.sendRoute: No field! " + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseTimeTrigger.sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a boolean. This is
     * be used to set SFBool field types isActive, enabled and loop.
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
            case FIELD_TRIGGER_TIME:
                throw new InvalidFieldValueException("Cannot set eventout");

            case FIELD_SET_BOOLEAN:
                setBoolean();
                break;

            default:
                super.setValue(index, value);
        }
    }


    //----------------------------------------------------------
    // Private methods
    //----------------------------------------------------------

    /**
     * Handle setBoolean events.
     */
    private void setBoolean() {
        vfTriggerTime = vrmlClock.getTime();

        if (!inSetup) {
            hasChanged[FIELD_TRIGGER_TIME] = true;
            fireFieldChanged(FIELD_TRIGGER_TIME);
        }
    }
}
