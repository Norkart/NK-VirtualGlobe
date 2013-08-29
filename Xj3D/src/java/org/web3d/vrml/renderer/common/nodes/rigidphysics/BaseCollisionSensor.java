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

package org.web3d.vrml.renderer.common.nodes.rigidphysics;

// External imports
import org.odejava.*;

import org.odejava.collision.BulkContact;

import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseSensorNode;
import org.web3d.util.IntHashMap;

/**
 * Implementation of the CollidableSensor node.
 * <p>
 *
 *
 * The X3D definition of CollisionSensor is:
 * <pre>
 * CollisionSensor : X3DNode {
 *   MFNode [in,out] collidables NULL  [CollisionCollection]
 *   SFBool [in,out] enabled     TRUE
 *   SFNode [in,out] metadata    NULL  [X3DMetadataObject]
 *   MFNode [out]    contacts
 *   SFBool [out]    isActive
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public abstract class BaseCollisionSensor extends BaseSensorNode
    implements VRMLNBodySensorNodeType {

    // Field index constants

    /** The field index for collidables */
    protected static final int FIELD_COLLIDABLES = LAST_SENSOR_INDEX + 1;

    /** The field index for enabled */
    protected static final int FIELD_ENABLED = LAST_SENSOR_INDEX + 2;

    /** The field index for contacts */
    protected static final int FIELD_CONTACTS = LAST_SENSOR_INDEX + 3;

    /** The field index for contacts */
    protected static final int FIELD_INTERSECTIONS = LAST_SENSOR_INDEX + 4;

    /** Last index used by this base node */
    protected static final int LAST_COLLECTION_INDEX = FIELD_INTERSECTIONS;

    /** Number of fields in this node */
    private static final int NUM_FIELDS = LAST_COLLECTION_INDEX + 1;

    /** Message for when the node in setValue() is not a primitive */
    protected static final String COLLIDABLE_PROTO_MSG =
        "Collidables field proto value does not describe a CollisionCollection.";

    /** Message for when the node in setValue() is not a primitive */
    protected static final String COLLIDABLE_NODE_MSG =
        "Collidables field node value does not describe a CollisionCollection.";

    /** Message when the user attempts to write to the hinge1Angle field */
    private static final String CONTACT_WRITE_MSG =
        "contacts is outputOnly and cannot be set";

    /** Message when the user attempts to write to the intersections field */
    private static final String INTERSECTION_WRITE_MSG =
        "intersections is outputOnly and cannot be set";

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    // The VRML field values

    /** The value of the collidables field */
    protected VRMLNBodyGroupNodeType vfCollidables;

    /** The proto version of the collidables */
    protected VRMLProtoInstance pCollidables;


    /** Value of the output field contacts */
    protected VRMLNodeType[] vfContacts;

    /** The number of valid contacts from the last frame */
    protected int numContacts;

    /** Value of the output field intersections */
    protected VRMLNodeType[] vfIntersections;

    /** The number of valid intersections from the last frame */
    protected int numIntersections;

    // Other vars

    /** Local pool of BaseContact instances that we use to grab from. */
    private ArrayList contactPool;

    /**
     * Static constructor to initialise all the field values.
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_COLLIDABLES,
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_COLLIDABLES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "collidables");
        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");
        fieldDecl[FIELD_CONTACTS] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFNode",
                                     "contacts");
        fieldDecl[FIELD_INTERSECTIONS] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFNode",
                                     "intersections");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = new Integer(FIELD_COLLIDABLES);
        fieldMap.put("collidables", idx);
        fieldMap.put("set_collidables", idx);
        fieldMap.put("collidables_changed", idx);

        fieldMap.put("contacts",new Integer(FIELD_CONTACTS));
        fieldMap.put("intersections",new Integer(FIELD_INTERSECTIONS));
        fieldMap.put("isActive",new Integer(FIELD_IS_ACTIVE));
    }

    /**
     * Construct a new default CollisionSensor node object.
     */
    public BaseCollisionSensor() {
        super("CollisionSensor");

        contactPool = new ArrayList();
        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseCollisionSensor(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLSensorNodeType)node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNBodySensorsNodeType
    //----------------------------------------------------------

    /**
     * Update the outputs of this sensor now, based on the given set of
     * ODE contact information.
     *
     * @param contacts The list of contacts to process
     * @param numContacts The number of contacts to process from the list
     */
    public void updateContacts(IntHashMap bodyIdMap,
                               IntHashMap geomIdMap) {

        if((vfCollidables == null) || !vfCollidables.isEnabled())
            return;

        BulkContact contacts = vfCollidables.getContacts();
        int totalContacts = vfCollidables.numContacts();

        if(totalContacts == 0) {
            if(vfIsActive) {
                vfIsActive = false;
                hasChanged[FIELD_IS_ACTIVE] = true;
                fireFieldChanged(FIELD_IS_ACTIVE);
            }

            numContacts = 0;
            numIntersections = 0;
        } else {
            // Loop through the contacts and create/update instances.

            // First ensure that we have enough objects here to play with.
            // Ideally this code should check to see which component level of
            // this node was requested and not issue contact events if it was
            // not level 2.
            contactPool.ensureCapacity(totalContacts);
            int pool_size = contactPool.size();
            if(pool_size < totalContacts) {
                for(int i = pool_size; i < totalContacts; i++)
                    contactPool.add(generateNewContact());
            }

            // Resize outputs if needed
            if(vfContacts == null || vfContacts.length < totalContacts) {
                vfContacts = new VRMLNodeType[totalContacts];
                vfIntersections = new VRMLNodeType[totalContacts];
            }

            for(int i = 0; i < totalContacts; i++) {
                BaseContact c = (BaseContact)contactPool.get(i);
                c.setContact(contacts, i, bodyIdMap, geomIdMap);
                vfContacts[i] = c;
            }

            numContacts = totalContacts;

            hasChanged[FIELD_CONTACTS] = true;
            fireFieldChanged(FIELD_CONTACTS);

            if(!vfIsActive) {
                vfIsActive = true;
                hasChanged[FIELD_IS_ACTIVE] = true;
                fireFieldChanged(FIELD_IS_ACTIVE);
            }
        }
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

        if(pCollidables != null)
            pCollidables.setupFinished();
        else if(vfCollidables != null)
            vfCollidables.setupFinished();
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.nBodyCollisionSensorNodeType;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A shape of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_COLLECTION_INDEX)
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
            case FIELD_COLLIDABLES:
                if(pCollidables != null)
                    fieldData.nodeValue = pCollidables;
                else
                    fieldData.nodeValue = vfCollidables;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_CONTACTS:
                fieldData.clear();
                fieldData.nodeArrayValue = vfContacts;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = numContacts;
                break;

            case FIELD_INTERSECTIONS:
                fieldData.clear();
                fieldData.nodeArrayValue = vfIntersections;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = numIntersections;
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
                case FIELD_COLLIDABLES:
                    if(pCollidables != null)
                        destNode.setValue(destIndex, pCollidables);
                    else
                        destNode.setValue(destIndex, vfCollidables);
                    break;

                case FIELD_CONTACTS:
                    destNode.setValue(destIndex, vfContacts, numContacts);
                    break;

                case FIELD_INTERSECTIONS:
                    destNode.setValue(destIndex,
                                      vfIntersections,
                                      numIntersections);
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
            case FIELD_CONTACTS:
                throw new InvalidFieldAccessException(CONTACT_WRITE_MSG);

            case FIELD_INTERSECTIONS:
                throw new InvalidFieldAccessException(INTERSECTION_WRITE_MSG);

            case FIELD_COLLIDABLES:
                setCollidables(node);
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
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_CONTACTS:
                throw new InvalidFieldAccessException(CONTACT_WRITE_MSG);

            case FIELD_INTERSECTIONS:
                throw new InvalidFieldAccessException(INTERSECTION_WRITE_MSG);

            default:
                super.setValue(index, children, numValid);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set node content as replacement for the collidables field.
     *
     * @param coll The new Collidables node to use
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setCollidables(VRMLNodeType coll)
        throws InvalidFieldValueException {

        VRMLNBodyGroupNodeType node;
        VRMLNodeType old_node;

        if(pCollidables != null)
            old_node = pCollidables;
        else
            old_node = vfCollidables;

        if(coll instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)coll).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLNBodyGroupNodeType))
                throw new InvalidFieldValueException(COLLIDABLE_PROTO_MSG);

            node = (VRMLNBodyGroupNodeType)impl;
            pCollidables = (VRMLProtoInstance)coll;

        } else if(coll != null && !(coll instanceof VRMLNBodyGroupNodeType)) {
            throw new InvalidFieldValueException(COLLIDABLE_NODE_MSG);
        } else {
            pCollidables = null;
            node = (VRMLNBodyGroupNodeType)coll;
        }

        vfCollidables = node;
        if(coll != null)
            updateRefs(coll, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            stateManager.registerAddedNode(node);

            hasChanged[FIELD_COLLIDABLES] = true;
            fireFieldChanged(FIELD_COLLIDABLES);
        }
    }

    /**
     * Generate me a default renderer-specific instance of the Contact node
     * now.
     *
     * @return A new instance of the contact
     */
    protected abstract BaseContact generateNewContact();
}
