/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.dis;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

import javax.vecmath.*;

import mil.navy.nps.dis.*;
import mil.navy.nps.disEnumerations.PduTypeField;
import mil.navy.nps.math.Quaternion;

import org.web3d.xmsf.dis.*;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of a EntityManager node functionality.
 * <p>
 * Notifies content when an entity arrives or leaves.  Nodes which are
 * locally controlled by the simulation are ignored(ie any node which
 * matches the entityID,siteID,appID and has a networkWriter mode).
 *
 * AddedEntities events are issued when an EntityStatePdu is first detected.
 * Entities arrivals will only be notified once a simulation, unless
 * a removedEntity event is issued.
 *
 * RemovedEntities events issued when an EntityStatePdu has not arrived
 * within the DIS allowed heartbeart period.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public abstract class BaseDISEntityManager extends AbstractNode
    implements VRMLNetworkInterfaceNodeType, VRMLDISManagerNodeType {

    /** Protocol implemented */
    protected static final String PROTOCOL = "DIS";

    /** Field Index */
    protected static final int FIELD_SITE_ID = LAST_NODE_INDEX + 1;
    protected static final int FIELD_APPLICATION_ID = LAST_NODE_INDEX + 2;
    protected static final int FIELD_ADDRESS = LAST_NODE_INDEX + 3;
    protected static final int FIELD_PORT = LAST_NODE_INDEX + 4;
    protected static final int FIELD_ADDED_ENTITIES = LAST_NODE_INDEX + 5;
    protected static final int FIELD_REMOVED_ENTITIES = LAST_NODE_INDEX + 6;
    protected static final int FIELD_MAPPING = LAST_NODE_INDEX + 7;

    /** The last field index used by this class */
    protected static final int LAST_ENTITY_MANAGER_INDEX = FIELD_MAPPING;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_ENTITY_MANAGER_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** siteID SFInt32 0 */
    protected int vfSiteID;

    /** applicationID   SFInt32 0 */
    protected int vfApplicationID;

    /** address   SFString "localhost" */
    protected String vfAddress;

    /** port   SFInt32 0 */
    protected int vfPort;

    /** mapping MFNode [] */
    protected ArrayList vfMapping;

    /** addedEntities MFNode [] */
    protected ArrayList vfAddedEntities;

    /** removedEntities MFNode [] */
    protected ArrayList vfRemovedEntities;

    /** Factory for creating EspduTransform nodes */
    protected VRMLNodeFactory nodeFactory;

    /** Internal scratch var for dealing with added/removed children */
    private VRMLNodeType[] nodeTmp;

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        nodeFields = new int[] { FIELD_METADATA, FIELD_MAPPING };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_SITE_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "siteID");

        fieldDecl[FIELD_ADDRESS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "address");

        fieldDecl[FIELD_PORT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "port");

        fieldDecl[FIELD_APPLICATION_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "applicationID");

        fieldDecl[FIELD_MAPPING] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "mapping");

        fieldDecl[FIELD_ADDED_ENTITIES] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFNode",
                                     "addedEntities");

        fieldDecl[FIELD_REMOVED_ENTITIES] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFNode",
                                     "removedEntities");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_SITE_ID);
        fieldMap.put("siteID", idx);
        fieldMap.put("set_siteID", idx);
        fieldMap.put("siteID_changed", idx);

        idx = new Integer(FIELD_APPLICATION_ID);
        fieldMap.put("applicationID", idx);
        fieldMap.put("set_applicationID", idx);
        fieldMap.put("applicationID_changed", idx);

        idx = new Integer(FIELD_ADDRESS);
        fieldMap.put("address", idx);
        fieldMap.put("set_address", idx);
        fieldMap.put("address_changed", idx);

        idx = new Integer(FIELD_PORT);
        fieldMap.put("port", idx);
        fieldMap.put("set_port", idx);
        fieldMap.put("port_changed", idx);

        idx = new Integer(FIELD_MAPPING);
        fieldMap.put("mapping", idx);
        fieldMap.put("set_mapping", idx);
        fieldMap.put("mapping_changed", idx);

        idx = new Integer(FIELD_ADDED_ENTITIES);
        fieldMap.put("addedEntities", idx);
        fieldMap.put("addedEntities_changed", idx);

        idx = new Integer(FIELD_REMOVED_ENTITIES);
        fieldMap.put("removedEntities", idx);
        fieldMap.put("removedEntities_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseDISEntityManager() {
        super("DISEntityManager");

        hasChanged = new boolean[LAST_ENTITY_MANAGER_INDEX + 1];

        vfSiteID = 0;
        vfApplicationID = 0;
        vfPort = 0;
        vfAddress = "";
        vfAddedEntities = new ArrayList();
        vfRemovedEntities = new ArrayList();
        vfMapping = new ArrayList();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseDISEntityManager(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("siteID");
            VRMLFieldData field = node.getFieldValue(index);
            vfSiteID = field.intValue;

            index = node.getFieldIndex("applicationID");
            field = node.getFieldValue(index);
            vfApplicationID = field.intValue;

            index = node.getFieldIndex("address");
            field = node.getFieldValue(index);
            vfAddress = field.stringValue;

            index = node.getFieldIndex("port");
            field = node.getFieldValue(index);
            vfPort = field.intValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame. If the derived class needs to propogate the
     * changes then it should override the updateMatrix() method or this
     * and make sure this method is called first.
     */
    public void allEventsComplete() {
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.NetworkInterfaceNodeType;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        int len = vfMapping.size();
        VRMLNodeType node;
        for (int i=0; i < len; i++) {
          node = (VRMLNodeType) vfMapping.get(i);
          node.setupFinished();
        }

        inSetup = false;
    }

    //----------------------------------------------------------------
    // Methods defined by VRMLNetworkInterfaceNodeType
    //----------------------------------------------------------------

    /**
     * Get the protocol this node supports.
     *
     * @return The protocol.
     */
    public String getProtocol() {
        return PROTOCOL;
    }

    /**
     * Get the role of this node.
     *
     * @param The role, ROLE_*.
     */
    public int getRole() {
        return VRMLNetworkInterfaceNodeType.ROLE_MANAGER;
    }

    /**
     * Add a NetworkRoleListener.
     *
     * @param l The listener.  Duplicates and nulls are ignored.
     */
    public void addNetworkRoleListener(NetworkRoleListener l) {
        // Ingore as we never change roles
    }

    /**
     * Remove a NetworkRoleListener.
     *
     * @param l The listener
     */
    public void removeNetworkRoleListener(NetworkRoleListener l) {
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType interface.
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
        if(index < 0  || index > LAST_ENTITY_MANAGER_INDEX)
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
        fieldData.numElements = 1;
        fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;

        int num_kids;

        switch(index) {
            case FIELD_SITE_ID:
                fieldData.intValue = vfSiteID;
                break;

            case FIELD_APPLICATION_ID:
                fieldData.intValue = vfApplicationID;
                break;

            case FIELD_PORT:
                fieldData.intValue = vfPort;
                break;

            case FIELD_ADDRESS:
                fieldData.stringValue = vfAddress;
                break;

            case FIELD_MAPPING:
                VRMLNodeType kids[] = new VRMLNodeType[vfMapping.size()];
                vfMapping.toArray(kids);
                fieldData.clear();
                fieldData.nodeArrayValue = kids;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = kids.length;
                break;

            case FIELD_ADDED_ENTITIES:
                num_kids = vfAddedEntities.size();

                if((nodeTmp == null) || (nodeTmp.length < num_kids))
                    nodeTmp = new VRMLNodeType[num_kids];
                vfAddedEntities.toArray(nodeTmp);
                fieldData.clear();
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = num_kids;
                break;

            case FIELD_REMOVED_ENTITIES:
                num_kids = vfRemovedEntities.size();

                if((nodeTmp == null) || (nodeTmp.length < num_kids))
                    nodeTmp = new VRMLNodeType[num_kids];
                vfRemovedEntities.toArray(nodeTmp);
                fieldData.clear();
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = num_kids;
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

        int num_kids;

        try {
            switch(srcIndex) {
                case FIELD_SITE_ID:
                    destNode.setValue(destIndex, vfSiteID);
                    break;
                case FIELD_PORT:
                    destNode.setValue(destIndex, vfPort);
                    break;
                case FIELD_ADDRESS:
                    destNode.setValue(destIndex, vfAddress);
                    break;
                case FIELD_MAPPING:
                    VRMLNodeType kids[] = new VRMLNodeType[vfMapping.size()];
                    vfMapping.toArray(kids);
                    destNode.setValue(destIndex, kids, kids.length);
                    break;
                case FIELD_ADDED_ENTITIES:
                    num_kids = vfAddedEntities.size();

                    if((nodeTmp == null) || (nodeTmp.length < num_kids))
                        nodeTmp = new VRMLNodeType[num_kids];
                    vfAddedEntities.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, num_kids);
                    break;
                case FIELD_REMOVED_ENTITIES:
                    num_kids = vfRemovedEntities.size();

                    if((nodeTmp == null) || (nodeTmp.length < num_kids))
                        nodeTmp = new VRMLNodeType[num_kids];
                    vfRemovedEntities.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, num_kids);
                    break;

                default: super.sendRoute(time,srcIndex,destNode,destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseTransform.sendRoute: No field!" + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid fieldValue: " +
                ifve.getMessage());
        }
    }

    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_MAPPING:
                if(!inSetup)
                    vfMapping.clear();

                for(int i=0; i < children.length; i++)
                    vfMapping.add(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_MAPPING] = true;
                    fireFieldChanged(FIELD_MAPPING);
                }
                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_MAPPING:
                if (!inSetup)
                    vfMapping.clear();

                vfMapping.add(child);

                if(!inSetup) {
                    hasChanged[FIELD_MAPPING] = true;
                    fireFieldChanged(FIELD_MAPPING);
                }
                break;

            default:
                super.setValue(index, child);
        }
    }
    /**
     * Set the value of the field at the given index as an integer.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_SITE_ID:
                vfSiteID = value;

                if(!inSetup) {
                    hasChanged[FIELD_SITE_ID] = true;
                    fireFieldChanged(FIELD_SITE_ID);
                }
                break;
            case FIELD_APPLICATION_ID:
                vfApplicationID = value;
                if(!inSetup) {
                    hasChanged[FIELD_APPLICATION_ID] = true;
                    fireFieldChanged(FIELD_APPLICATION_ID);
                }
                break;
            case FIELD_PORT:
                vfPort = value;
                if(!inSetup) {
                    hasChanged[FIELD_PORT] = true;
                    fireFieldChanged(FIELD_PORT);
                }
                break;
            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a string.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ADDRESS:
                vfAddress = value;
                if(!inSetup) {
                    hasChanged[FIELD_ADDRESS] = true;
                    fireFieldChanged(FIELD_ADDRESS);
                }
                break;

            default:
                super.setValue(index, value);
        }

    }

    //----------------------------------------------------------
    // Methods for the VRMLDISNodeType
    //----------------------------------------------------------
    /**
     * Get the siteID specified for this node.
     *
     * @return the siteID.
     */
    public int getSiteID() {
        return vfSiteID;
    }

    /**
     * Get the applicationID specified for this node.
     *
     * @return the applicationID.
     */
    public int getAppID() {
        return vfApplicationID;
    }


    /**
     * Get the entityID specified for this node.
     *
     * @return the entityID.
     */
    public int getEntityID() {
        return 0;
    }


    /**
     * Get the network address to listen to.
     *
     * @param The address.
     */
    public String getAddress() {
        return vfAddress;
    }

    /**
     * Get the network port to listen to.
     *
     * @param The port.
     */
    public int getPort() {
        return vfPort;
    }

    /**
     * Get the chat room username.
     *
     * @return The username.  null if none provided
     */
    public String getUsername() {
        return null;
    }

    /**
     * Get the chat room password.
     *
     * @return The password.  null if none provided
     */
    public String getPassword() {
        return null;
    }

    /**
     * Get the chat room auth server.
     *
     * @return The auth server.  null if none provided
     */
    public String[] getAuthServer() {
        return null;
    }

    /**
     * Get the chat room mucServer.
     *
     * @return The mucServer.  null if none provided
     */
    public String getMucServer() {
        return null;
    }

    /**
     * Get the chat room mucRoom.
     *
     * @return The mucRoom.  null if none provided
     */
    public String getMucRoom() {
        return null;
    }

    /**
     * Set the isActive state for a DIS node.
     *
     * @param isActive Whether the node is active(traffic within 5 seconds).
     */
    public void setIsActive(boolean active) {
    }

    /**
     * Does the this node have new information to write.  This only
     * accounts for local values, not required DIS heartbeart rules.
     *
     * @return TRUE if values have changed.
     */
    public boolean valuesToWrite() {
        return false;
    }

    /**
     * Get the nodes current state.  Assume that a single local scratch var can
     * be reused each time.
     *
     * @return The DIS state.
     */
    public ProtocolDataUnit getState() {
        return null;
    }

    /**
     * Get the nodes current state.  Assume that a single local scratch var can
     * be reused each time.
     *
     * @return The DIS state.
     */
    public ProtocolDataUnitType getStateDX() {
        return null;
    }

    /**
     * Tell the DIS node that a packet arrived.  Used to update tiemstamp information.
     *
     */
    public void packetArrived(ProtocolDataUnit pdu) {
        // ingored
    }

    /**
     * Tell the DIS node that a packet arrived.  Used to update tiemstamp information.
     *
     */
    public void packetArrived(ProtocolDataUnitType pdu) {
        // ingored
    }

    //----------------------------------------------------------
    // Internal methods of the class
    //----------------------------------------------------------
}
