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

package org.web3d.vrml.renderer.common.nodes;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.parser.VRMLFieldReader;

/**
 * Common functionality that represents a node that has its fields dynamically
 * assigned.
 * <p>
 *
 * Dynamic node structures are used in two places - scripts and protos. This
 * class provides the common functionality for managing dynamically assigned
 * fields.
 * <p>
 *
 * The implementation does not handle deletion of fields particularly well
 * at this point in time. Works fine if the node is not realised yet, but
 * doesn't work well if the node is live.
 *
 * @author Justin Couch
 * @version $Revision: 1.22 $
 */
public abstract class AbstractDynamicFieldNode implements VRMLNodeType {

    /**
     * A standard message for when the supplied node is wrong. Just add the
     * node name of the wrong type to the end.
     */
    protected static final String BAD_NODE_MSG =
        "The supplied node cannot be copied as it's type is wrong. The type " +
        "supplied is ";

    /** Message for when the proto is not a Metadata */
    protected static final String METADATA_PROTO_MSG =
        "Proto does not describe a Metadata object";

    /** Message for when the node in setValue() is not a Metadata */
    protected static final String METADATA_NODE_MSG =
        "Node does not describe a Metadata object";

    /** When the same field is defined twice, but with different types */
    protected static final String FIELD_CLASH_MSG = "The same field has been " +
        "declared twice in this node, but the data types or access types are " +
        "different.";

    /**
     * Message when the reference count attempts to decrement a layer ID that
     * does not currently refrence this node. Under correct implementation,
     * this message should never be seen.
     */
    private static final String NO_LAYER_REF_MSG =
        "An attempt was made to decrement a reference count to a layer that " +
        "is not currently referencing this node. Layer ID is ";

    /** Reporter instance for handing out errors */
    protected ErrorReporter errorReporter;

    /** Data mapping holding the field name -> index (Integer) mapping */
    protected final Map fieldIndexMap;

    /**
     * List of the field maps registered in order for their definitions.
     * When created, this list will have a null value in each index position
     * for the size of the number of declared fields. This allows a user to
     * set(int, object) the value if they want. However, it is recommended that
     * you use the {@link #appendField(VRMLFieldDeclaration)} method instead.
     */
    protected final List fieldDeclList;

    /** The name of the node. "Script" or the proto name.*/
    protected final String nodeName;

    /** Scratch class var for returning field data. Assigned at construction. */
    protected final ThreadLocal<VRMLFieldData> fieldLocalData;

    /** State manager for propogating updates */
    protected FrameStateManager stateManager;

    /** The count of the last added field index */
    protected int fieldCount;

    /** Flag indicating this node is following VRML97 rules */
    protected boolean isVrml97;

    /** Is this node still being setup/parsed.  Cleared by setupFinished */
    protected boolean inSetup;

    /** The current listener(s) registered */
    private VRMLNodeListener nodeListener;

    /**
     * The current number of references to this node. This is for informational
     * purposes only and should never be touched by derived classes.
     */
    protected int[] refCounts;

    /**
     * The list of layer IDs that reference this node. Should correspond 1:1
     * to the counts in {@link #refCounts}. If none, this is null.
     */
    protected int[] layerIds;

    /** The list of IDs that have been marked as being removed. */
    protected int[] removedLayerIds;

    /** Indices of the fields that are MFNode or SFnode */
    private int[] nodeFields;

    /** SFNode metadata NULL */
    protected VRMLNodeType vfMetadata;

    /** proto representation of the metadata node */
    protected VRMLProtoInstance pMetadata;

    /**
     * Create a new instance of a proto that has the given name.
     *
     * @param name The name of the proto to use
     */
    public AbstractDynamicFieldNode(String name) {
        nodeName = name;

        fieldIndexMap = new HashMap();
        fieldDeclList = new ArrayList();
        fieldLocalData = new VRMLFieldDataThreaded();
        fieldCount = 0;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    /**
     * Ignored by this implementation.
     */
    public void allEventsComplete() {
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Get the name of this node as a string.
     *
     * @return The name of the node
     */
    public String getVRMLNodeName() {
        return nodeName;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Ask for the current number of references to this object in the given
     * layer. If this node represents a layer node itself, then the value
     * returned for that ID should always be one. If the layer requested does
     * not have a reference to this node, return zero.
     *
     * @param layer The id of the layer to get the ref count for
     * @return The number of references to this node
     */
    public int getRefCount(int layer) {
        if(layerIds == null)
            return 0;

        int ret_val = 0;

        for(int i = 0; i < layerIds.length; i++) {
            if(layerIds[i] == layer) {
                ret_val = refCounts[i];
                break;
            }
        }

        return ret_val;
    }

    /**
     * Change the reference count up or down by one for a given layer ID.
     * If there is no reference to the given layer ID previously, add one
     * now. A listing of the layer IDs that reference this node can be
     * retrieved through {@link #getLayerIds()}.
     *
     * @param layer The id of the layer to modify the ref count on
     * @param add true to increment the reference count, false to decrement
     */
    public synchronized void updateRefCount(int layer, boolean add) {

        // Go looking for the layer first to see if we have it.
        if(layerIds != null) {
            int ref_idx = -1;

            for(int i = 0; i < layerIds.length; i++) {
                if(layerIds[i] == layer) {
                    ref_idx = i;
                    break;
                }
            }

            if(ref_idx == -1) {
                // Decrementing a layer Id that doesn't exist? Something is
                // badly wrong, so let's toss an exception and debug WTF just
                // happened.
                if(!add)
                    throw new IllegalStateException(NO_LAYER_REF_MSG + layer);

                int[] tmp = new int[layerIds.length + 1];
                System.arraycopy(layerIds, 0, tmp, 0, layerIds.length);
                tmp[layerIds.length] = layer;
                layerIds = tmp;

                tmp = new int[refCounts.length + 1];
                System.arraycopy(refCounts, 0, tmp, 0, refCounts.length);
                tmp[refCounts.length] = 1;
                refCounts = tmp;

                // Check the removed layer ID list ot make sure that we don't
                // have this layer on the list. If so, remove it.
                int size = removedLayerIds == null ? 0 : removedLayerIds.length;

                for(int i = 0; i < size; i++) {
                    if(removedLayerIds[i] != layer)
                        continue;

                    if(size == 1) {
                        removedLayerIds = null;
                    } else {
                        int[] tmp1 = new int[size - 1];
                        System.arraycopy(removedLayerIds, 0, tmp1, 0, i - 1);
                        System.arraycopy(removedLayerIds,
                                         i + 1,
                                         tmp1,
                                         i,
                                         size - i - 1);
                        removedLayerIds = tmp1;
                    }
                }
            } else {
                if(add) {
                    refCounts[ref_idx]++;
                } else {
                    refCounts[ref_idx]--;

                    if(refCounts[ref_idx] <= 0) {
                        int[] tmp1 = new int[refCounts.length - 1];
                        int[] tmp2 = new int[refCounts.length - 1];

                        for(int i = 0; i < ref_idx; i++) {
                            tmp1[i] = refCounts[i];
                            tmp2[i] = layerIds[i];
                        }

                        for(int i = ref_idx + 1; i < refCounts.length; i++) {
                            tmp1[i - 1] = refCounts[i];
                            tmp2[i - 1] = layerIds[i];
                        }

                        refCounts = tmp1;
                        layerIds = tmp2;

                        if(removedLayerIds == null) {
                            removedLayerIds = new int[] { layer } ;
                        } else {
                            int[] tmp3 = new int[removedLayerIds.length + 1];
                            System.arraycopy(removedLayerIds,
                                             0,
                                             tmp3,
                                             0,
                                             removedLayerIds.length);
                            removedLayerIds = tmp3;
                        }
                    }

                    // Do we have no references left?
                    if(refCounts.length == 1 && refCounts[0] == 0) {
                        refCounts = null;
                        layerIds = null;
                    }
                }
            }
        } else {
            if(!add)
                throw new IllegalStateException(NO_LAYER_REF_MSG + layer);

            layerIds = new int[] { layer };
            refCounts = new int[] { 1 };
        }
    }

    /**
     * Get a listing of the current layer IDs that are directly or indirectly
     * referencing this node. If this layer is not part of a live scene graph
     * (eg held by a script code, but not by the script itself) then this will
     * will return a null value. The array will always be exactly equal in
     * length to the number of IDs that reference this node.
     *
     * @return An array of all the IDs referencing this node or null if none
     */
    public int[] getLayerIds() {
        return layerIds;
    }

    /**
     * Get the list of layer IDs that this node has just been removed from.
     * If it currently has not been removed from anything, this will return
     * null. This will remain updated until the next
     * {@link #clearRemovedLayerIds()} call.
     *
     * @return An array of all the IDs this is delete from or null if none
     */
    public int[] getRemovedLayerIds() {
        return removedLayerIds;
    }


    /**
     * Clear the current removed layer ID list. If there is nothing removed,
     * this method does nothing.
     */
    public void clearRemovedLayerIds() {
        removedLayerIds = null;
    }

    /**
     * Add a listener to this node instance. If the listener is already added
     * or null the request is silently ignored.
     *
     * @param l The listener instance to add
     */
    public void addNodeListener(VRMLNodeListener l) {
        nodeListener = NodeListenerMulticaster.add(nodeListener, l);
    }

    /**
     * Remove a listener from this node instance. If the listener is null or
     * not registered, the request is silently ignored.
     *
     * @param l The listener to be removed
     */
    public void removeNodeListener(VRMLNodeListener l) {
        nodeListener = NodeListenerMulticaster.remove(nodeListener, l);
    }

    /**
     * Ignored by this implementation.
     *
     * @param mgr The manager instance to use
     */
    public void setFrameStateManager(FrameStateManager mgr) {
        stateManager = mgr;
    }

    /**
     * Set the X3DMetadataObject that is associated with this node.
     *
     * @param data The node to register as the vfMetadata
     * @throws InvalidFieldValueException The object instance provided
     *     does not implment VRMLMetadataNodeType or is not a proto instance
     *     that encapsulates it as it's primary type
     */
    public void setMetadataObject(VRMLNodeType data)
        throws InvalidFieldValueException {

        if(data == null) {
            vfMetadata = null;
            pMetadata = null;
        } else {
            // check for the right interface
            if(data instanceof VRMLMetadataObjectNodeType) {
                vfMetadata = data;
                pMetadata = null;
            } else if(data instanceof VRMLProtoInstance) {
                VRMLProtoInstance proto = (VRMLProtoInstance)data;
                VRMLNodeType impl = proto.getImplementationNode();

                while(impl != null && impl instanceof VRMLProtoInstance)
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if(impl != null && !(impl instanceof VRMLMetadataObjectNodeType))
                    throw new InvalidFieldValueException(METADATA_PROTO_MSG);

                vfMetadata = impl;
                pMetadata = proto;
            } else
                throw new InvalidFieldValueException(METADATA_NODE_MSG);
        }
    }

    /**
     * Get the currently registered vfMetadata object instance. If none is set
     * then return null.
     *
     * @return The current vfMetadata object or null
     */
    public VRMLNodeType getMetadataObject() {
        return vfMetadata;
    }

    /**
     * Perform the setup completion routine.
     */
    public void setupFinished() {

        // Build the list of MF/SFNode fields for the list.
        int size = fieldDeclList.size();

        // first time through, count the number fields, then loop
        // again to assign.
        VRMLFieldDeclaration decl;
        int type;
        int cnt = 0;
        for(int i = 0; i < size; i++) {
            decl = (VRMLFieldDeclaration)fieldDeclList.get(i);
            type = decl.getFieldType();
            if((type == FieldConstants.MFNODE) ||
               (type == FieldConstants.SFNODE))
               cnt++;
        }

        if(cnt == 0)
            return;

        nodeFields = new int[cnt];
        cnt = 0;

        for(int i = 0; i < size; i++) {
            decl = (VRMLFieldDeclaration)fieldDeclList.get(i);
            type = decl.getFieldType();
            if((type == FieldConstants.MFNODE) ||
               (type == FieldConstants.SFNODE))
               nodeFields[cnt++] = getFieldIndex(decl.getName());
        }

        if(pMetadata != null)
            pMetadata.setupFinished();
        else if(vfMetadata != null)
            vfMetadata.setupFinished();

        inSetup = false;
    }

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer)fieldIndexMap.get(fieldName);

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
        return (index >= fieldDeclList.size()) ? null :
               (VRMLFieldDeclaration)fieldDeclList.get(index);
    }

    /**
     * Get the number of fields.
     *
     * @return The number of fields.
     */
    public int getNumFields() {
        return fieldDeclList.size();
    }


    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Check to see if the supplied node type is the same as this node. It
     * does a case sensitive string comparison based on thier node name. If
     * they are not the same then an IllegalArgumentException is thrown. If
     * the same, nothing happens.
     *
     * @param node The node to check
     * @throws IllegalArgumentException The nodes are not the same
     */
    protected void checkNodeType(VRMLNodeType node) {
        String type = node.getVRMLNodeName();

        if(!type.equals(nodeName))
            throw new IllegalArgumentException(BAD_NODE_MSG + "type");
    }

    /**
     * Check to see if the node is using VRML97 semantics
     *
     * @return true if this is a VRML97 node
     */
    public boolean isVRML97() {
        return isVrml97;
    }

    /**
     * Send a notification to the registered listeners that a field has been
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param index The index of the field that changed
     */
    protected void fireFieldChanged(int index) {
        if(nodeListener != null) {
            try {
                nodeListener.fieldChanged(index);
            } catch(Throwable th) {
                th.printStackTrace();
            }
        }
    }

    /**
     * Get the count of the number of fields currently registered.
     *
     * @return The number of fields available
     */
    public int getFieldCount() {
        return fieldCount;
    }

    /**
     * Append a field declaration to this node. This is added to the current
     * list on the end. If the field already exists with the given name but
     * different values exception will be generated. If the field has exactly
     * the same signature it will silently ignore the request.
     *
     * @param field The new field to add
     * @return The index that this field was added at
     * @throws FieldExistsException A conflicting field of the same name
     *   already exists for this node
     */
    public int appendField(VRMLFieldDeclaration field)
        throws FieldExistsException {

        String name = field.getName();

        // Test for conflicting field name
        Integer pos_index = (Integer)fieldIndexMap.get(name);
        if(pos_index != null) {

            VRMLFieldDeclaration existing =
                (VRMLFieldDeclaration)fieldDeclList.get(pos_index.intValue());

            if((field.getFieldType() != existing.getFieldType()) ||
               (field.getAccessType() != field.getAccessType()))
                throw new FieldExistsException(FIELD_CLASH_MSG, name);
        }

        fieldIndexMap.put(name, new Integer(fieldCount));
        fieldDeclList.add(field);

        int cnt = fieldCount++;

        return cnt;
    }

    /**
     * Delete the field at the given index. This will not shuffle fields down
     * from higher index values. That index just becomes invalid to set values
     * to. If no field exists at that index or it is out of range, an exception
     * will be generated.
     *
     * @param index The index of the field to delete
     * @throws InvalidFieldException The field does not exist at the index
     * @throws IndexOutOfBoundsException The index provided is out of
     *   range for the current field numbers
     */
    public void deleteField(int index)
        throws InvalidFieldException, IndexOutOfBoundsException {

        VRMLFieldDeclaration decl =
            (VRMLFieldDeclaration)fieldDeclList.get(index);

        if(decl == null)
            throw new InvalidFieldException();

        // replace the field with null, don't delete it.
        fieldDeclList.set(index, null);
        fieldIndexMap.remove(decl.getName());
    }

    /**
     * Delete the named field. This will not shuffle fields down from higher
     * index values. That index just becomes invalid to set values to. If no
     * field exists at that index or it is out of range, an exception will be
     * generated.
     *
     * @param field The field to delete
     * @throws InvalidFieldException The named field does not exist
     * @throws IndexOutOfBoundsException The index provided is out of
     *   range for the current field numbers
     */
    public void deleteField(VRMLFieldDeclaration field)
        throws InvalidFieldException, IndexOutOfBoundsException {

        String name = field.getName();

        Integer index = (Integer)fieldIndexMap.remove(name);

        if(index == null)
            throw new InvalidFieldException("No field here", name);

        fieldDeclList.set(index.intValue(), null);
    }

    /**
     * Make a listing of all fields that are currently registered in this
     * node. The list contains instances of
     * {@link org.web3d.vrml.lang.VRMLFieldDeclaration}.
     *
     * @return A list of the current field declarations
     */
    public List getAllFields() {
        ArrayList ret_val = new ArrayList();

        int size = fieldDeclList.size();

        // If the user is adding and removing fields, this may cause null
        // values in the list. Trim those out.
        for(int i = 0; i < size; i++) {
            Object data = fieldDeclList.get(i);
            if(data != null)
                ret_val.add(data);
        }

        return ret_val;
    }
}
