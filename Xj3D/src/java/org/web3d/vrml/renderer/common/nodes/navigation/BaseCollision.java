/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.navigation;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;

/**
 * Common base implementation of a Collision node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.16 $
 */
public abstract class BaseCollision extends BaseGroupingNode
    implements VRMLEnvironmentalSensorNodeType,
               VRMLTimeDependentNodeType,
               VRMLCollidableNodeType {

    /** Message for when the proto is not a child */
    protected static final String PROXY_PROTO_MSG =
        "Proto does not describe a proxy object";

    /** Message for when the node in setValue() is not a child */
    protected static final String PROXY_NODE_MSG =
        "Node does not describe a proxy object";

    /** Secondary type constants for this node */
    private static final int[] SECONDARY_TYPE = {
        TypeConstants.EnvironmentalSensorNodeType,
        TypeConstants.TimeDependentNodeType,
        TypeConstants.CollidableNodeType,
    };

    /** Index of the collide field */
    protected static final int FIELD_COLLIDE = LAST_GROUP_INDEX + 1;

    /** Index of the proxy field */
    protected static final int FIELD_PROXY = LAST_GROUP_INDEX + 2;

    /** Index of the collideTime eventOut */
    protected static final int FIELD_COLLIDE_TIME = LAST_GROUP_INDEX + 3;

    /** Index of the isActive field */
    protected static final int FIELD_ISACTIVE = LAST_GROUP_INDEX + 4;

    /** Index of the enabled field */
    protected static final int FIELD_ENABLED = LAST_GROUP_INDEX + 5;

    /** Last index used by this node */
    protected static final int LAST_COLLIDE_INDEX = FIELD_ENABLED;

    /** Number of fields constant */
    private static final int NUM_FIELDS = FIELD_ENABLED + 1;

    /** Message when someone attempts to write to collideTime */
    private static final String COLLIDE_WRITE_MSG =
        "collideTime is an outputOnly field. You cannot write to it";

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final HashMap fieldMap;

    /** Indices of the fields that are MFNode or SFnode */
    private static final int[] nodeFields;

    /** exposedField SFBool collide */
    protected boolean vfCollide;

    /** field SFNode proxy */
    protected VRMLChildNodeType vfProxy;

    /** proto version of SFNode proxy */
    protected VRMLProtoInstance pProxy;

    /** eventOut SFTime collideTime */
    protected double vfCollideTime;

    /** eventout SFBool bind */
    protected boolean vfIsActive;

    /** The clock used to drive collide time eventOuts */
    protected VRMLClock vrmlClock;

    // Static constructor
    static {
        nodeFields = new int[] {
            FIELD_CHILDREN,
            FIELD_PROXY,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 2);

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
        fieldDecl[FIELD_COLLIDE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "SFBool",
                                 "collide");
        fieldDecl[FIELD_PROXY] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFNode",
                                 "proxy");
        fieldDecl[FIELD_COLLIDE_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                 "SFTime",
                                 "collideTime");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "SFBool",
                                 "enabled");
        fieldDecl[FIELD_ISACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                 "SFBool",
                                 "isActive");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        fieldMap.put("addChildren",new Integer(FIELD_ADDCHILDREN));
        fieldMap.put("removeChildren",new Integer(FIELD_REMOVECHILDREN));
        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));
        fieldMap.put("proxy",new Integer(FIELD_PROXY));
        fieldMap.put("collideTime",new Integer(FIELD_COLLIDE_TIME));
        fieldMap.put("isActive",new Integer(FIELD_ISACTIVE));

        idx = new Integer(FIELD_COLLIDE);
        fieldMap.put("collide", idx);
        fieldMap.put("set_collide", idx);
        fieldMap.put("collide_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    protected BaseCollision() {
        super("Collision");

        hasChanged = new boolean[NUM_FIELDS];

        vfCollide = true;
        vfIsActive = false;
        vfCollideTime = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseCollision(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTimeDependentNodeType
    //----------------------------------------------------------

    /**
     * Set the clock that this time dependent node will be running with.
     * The clock provides all the information and listeners for keeping track
     * of time. Setting a value of null will ask the node to remove the clock
     * from it's use so that the node may be removed from the scene.
     *
     * @param clock The clock to use for this node
     */
    public void setVRMLClock(VRMLClock clock) {
        vrmlClock = clock;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLEnvironmentalSensorNodeType
    //----------------------------------------------------------

    /**
     * Set the enabled state of the sensor.
     *
     * @param state True if the sensor is to be enabled
     */
    public void setEnabled(boolean state) {
        // Just call set collide as the two fields are the same.
        setCollide(state);
    }

    /**
     * Accessor method to get current value of the enabled field,
     * default value is <code>true</code>.
     *
     * @return The value of the enabled field
     */
    public boolean getEnabled() {
        return vfCollide;
    }

    /**
     * Accessor method to get current value of the isActive eventOut
     *
     * @return The current value of isActive
     */
    public boolean getIsActive () {
        return vfIsActive;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLCollidableNodeType
    //----------------------------------------------------------

    /**
     * Activate the collision status of this node. If the derived node has
     * detected a collision, this will generate an event out with the correct
     * time. It assumes that the derived node is taking into account the
     * enable and collide field settings.
     */
    public void collisionDetected() {

        if(!vfCollide)
            return;

        vfCollideTime = vrmlClock.getTime();

        hasChanged[FIELD_COLLIDE_TIME] = true;
        fireFieldChanged(FIELD_COLLIDE_TIME);
    }

    //----------------------------------------------------------
    // Methods overriding VRMLNodeType class.
    //----------------------------------------------------------

    /**
     * Get the secondary types of this node.  Replaces the instanceof mechanism
     * for use in switch statements. If there are no secondary types, it will
     * return a zero-length array.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
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
        if(index < 0  || index > LAST_COLLIDE_INDEX)
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
            case FIELD_COLLIDE:
            case FIELD_ENABLED:
                fieldData.clear();
                fieldData.booleanValue = vfCollide;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_ISACTIVE:
                fieldData.clear();
                fieldData.booleanValue = vfIsActive;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_COLLIDE_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfCollideTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_PROXY:
                fieldData.clear();
                if(pProxy != null)
                    fieldData.nodeValue = pProxy;
                else
                    fieldData.nodeValue = vfProxy;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
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
                case FIELD_COLLIDE:
                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfCollide);
                    break;

                case FIELD_ISACTIVE:
                    destNode.setValue(destIndex, vfIsActive);
                    break;

                case FIELD_COLLIDE_TIME:
                    destNode.setValue(destIndex, vfCollideTime);
                    break;

                case FIELD_PROXY:
                    if(pProxy != null)
                        destNode.setValue(destIndex, pProxy);
                    else
                        destNode.setValue(destIndex, vfProxy);

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
     * Set the value of the field at the given index as a boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_COLLIDE:
            case FIELD_ENABLED:
                setCollide(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a double. This would
     * be used to set SFDouble or SFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_COLLIDE_TIME:
                throw new InvalidFieldAccessException(COLLIDE_WRITE_MSG);

            default:
                super.setValue(index, value);
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
            case FIELD_PROXY:
                setProxy(node);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Set node content as replacement for <code>proxy</code>.
     *
     * @param app The new proxy.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setProxy(VRMLNodeType app)
        throws InvalidFieldValueException {

        VRMLChildNodeType node;
        VRMLNodeType old_node;

        if(pProxy != null)
            old_node = pProxy;
        else
            old_node = vfProxy;

        if(app instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)app).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLChildNodeType))
                throw new InvalidFieldValueException(PROXY_PROTO_MSG);

            node = (VRMLChildNodeType)impl;
            pProxy = (VRMLProtoInstance)app;

        } else if(app != null && !(app instanceof VRMLChildNodeType)) {
            throw new InvalidFieldValueException(PROXY_NODE_MSG);
        } else {
            pProxy = null;
            node = (VRMLChildNodeType)app;
        }

        vfProxy = node;
        if(app != null)
            updateRefs(app, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if (!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(app != null)
                stateManager.registerAddedNode(app);

            hasChanged[FIELD_PROXY] = true;
            fireFieldChanged(FIELD_PROXY);
        }
    }

    /**
     * Set the value of the collide field. Used to change the collision
     * status. Can be overridden by derived classes, but should be called
     * to make sure all field handling is done correctly.
     *
     * @param state true if the collision is being set
     */
    protected void setCollide(boolean state) {
        vfCollide = state;

        if(inSetup)
            return;

        // Send outputs to the right field based on the spec version.
        if(vrmlMajorVersion == 2) {
            hasChanged[FIELD_COLLIDE] = true;
            fireFieldChanged(FIELD_COLLIDE);
        } else {
            hasChanged[FIELD_ENABLED] = true;
            fireFieldChanged(FIELD_ENABLED);
        }
    }

    /**
     * Set the isActive field of this node. This will cause the node to be
     * the subject of input handling.
     *
     * @param enable True if this node is to be bound
     */
    protected void setIsActive(boolean enable) {
        vfIsActive = enable;

        if(!inSetup) {
            hasChanged[FIELD_ISACTIVE] = true;
            fireFieldChanged(FIELD_ISACTIVE);
        }
    }
}
