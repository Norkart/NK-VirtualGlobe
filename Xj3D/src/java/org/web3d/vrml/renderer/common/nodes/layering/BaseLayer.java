/*****************************************************************************
 * Copyright North Dakota State University, 2005 - 2006
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/
package org.web3d.vrml.renderer.common.nodes.layering;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * This is the base for the Layer node.
 * <p>
 *
 * The basic definition of Layer is:
 * <pre>
 * Layer : X3DLayerNode {
 *   MFNode  [in]     addChildren     []    [X3DChildNode]
 *   MFNode  [in]     removeChildren  []    [X3DChildNode]
 *   MFNode  [in,out] children        []    [X3DChildNode]
 *   SFBool  [in,out] isPickable      TRUE
 *   SFNode  [in,out] metadata        NULL  [X3DMetadataObject]
 *   SFNode  []       viewport        NULL  [X3DViewportNode]
 * }
 * </pre>
 *
 * @author Brad Vender, Justin Couch
 * @version $Revision: 1.9 $
 */
public class BaseLayer extends AbstractNode
    implements VRMLLayerNodeType {

    /** The field index for the addChildren input field */
    protected static final int FIELD_ADDCHILDREN=LAST_NODE_INDEX+1;

    /** The field index for the removeChildren input field */
    protected static final int FIELD_REMOVECHILDREN=LAST_NODE_INDEX+2;

    /** The field index for the children node field */
    protected static final int FIELD_CHILDREN=LAST_NODE_INDEX+3;

    /** The field index for the isPickable field */
    protected static final int FIELD_IS_PICKABLE=LAST_NODE_INDEX+4;

    /** The field index for the viewport node field */
    protected static final int FIELD_VIEWPORT=LAST_NODE_INDEX+5;

    /** The last valid field index for this node */
    protected static final int LAST_LAYER_INDEX = FIELD_VIEWPORT;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_LAYER_INDEX + 1;

    /** Message for when the proto is not a viewport */
    protected static final String VIEWPORT_PROTO_MSG =
        "Proto does not describe a X3DViewportNode object";

    /** Message for when the node in setValue() is not a viewport */
    protected static final String VIEWPORT_NODE_MSG =
        "Node does not describe a X3DViewportNode object";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** The Id of this layer */
    protected int layerId;

    // VRML Field declarations

    /** MFNode children NULL */
    protected ArrayList vfChildren;

    /** SFBool isPickable true */
    protected boolean vfIsPickable;

    /** Proto version of the viewport */
    protected VRMLProtoInstance pViewport;

    /** SFNode viewport. */
    protected VRMLViewportNodeType vfViewport;

    /** List of those who want to know about role changes, likely 1 */
    protected ArrayList layerListeners;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {

        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_VIEWPORT,
            FIELD_CHILDREN
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_ADDCHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                    "MFNode",
                    "addChildren");
        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                    "MFNode",
                    "children");
        fieldDecl[FIELD_IS_PICKABLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                    "SFBool",
                    "isPickable");
        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                    "SFNode",
                    "metadata");
        fieldDecl[FIELD_REMOVECHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                    "MFNode",
                    "removeChildren");
        fieldDecl[FIELD_VIEWPORT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                    "SFNode",
                    "viewport");


        Integer idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children",idx);
        fieldMap.put("set_children",idx);
        fieldMap.put("children_changed",idx);

        idx = new Integer(FIELD_IS_PICKABLE);
        fieldMap.put("isPickable",idx);
        fieldMap.put("set_isPickable",idx);
        fieldMap.put("isPickable_changed",idx);

        idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata",idx);
        fieldMap.put("set_metadata",idx);
        fieldMap.put("metadata_changed",idx);

        fieldMap.put("viewport",new Integer(FIELD_VIEWPORT));
        fieldMap.put("addChildren",new Integer(FIELD_ADDCHILDREN));
        fieldMap.put("removeChildren",new Integer(FIELD_REMOVECHILDREN));
    }

    /**
     * Construct a new default instance of this class.
     */
    protected BaseLayer() {
        super("Layer");

        vfIsPickable = true;
        vfChildren = new ArrayList();
        layerId = -1;

        layerListeners = new ArrayList(1);
        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a light node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseLayer(VRMLNodeType node) {
        this();
        checkNodeType(node);

        try {
            int index = node.getFieldIndex("isPickable");
            VRMLFieldData data = node.getFieldValue(index);
            vfIsPickable = data.booleanValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLLayerType
    //----------------------------------------------------------

    /**
     * Add a listener for layer state changes.  Duplicates will be ignored.
     *
     * @param listener The listener
     */
    public void addLayerListener(LayerListener listener) {
        if(!layerListeners.contains(listener))
            layerListeners.add(listener);
    }

    /**
     * Removed a listener for layer state changes.
     *
     * @param listener The listener
     */
    public void removeLayerListener(LayerListener listener) {
        layerListeners.remove(listener);
    }

    /**
     * See if this layer is currently pickable.
     *
     * @return true if the contents of this layer can be picked
     */
    public boolean isPickable() {
        return vfIsPickable;
    }

    /**
     * Set the value of the isPickable field.
     *
     * @param enable true if the contents of this layer can be picked
     */
    public void setPickable(boolean enable) {
        if(enable == vfIsPickable)
            return;

        vfIsPickable = enable;

        if(!inSetup) {
            hasChanged[FIELD_IS_PICKABLE] = true;
            fireFieldChanged(FIELD_IS_PICKABLE);
        }
    }

    /**
     * Set the viewport node instance used to control the size of screen
     * real estate to use for this layer. The node type passed in should be an
     * instance of VRMLViewportNodeType or a proto wrapper thereof.
     *
     * @param vp The node instance to use or null to clear
     * @throws InvalidFieldValueException The node is not a viewport node type
     * @throws InvalidFieldAccessException Attempting to write to the field
     *    after the setup is complete
     */
    public void setViewport(VRMLNodeType vp)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        if(!inSetup)
            throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG, "viewport");

        VRMLNodeType node;

        if(vp instanceof VRMLProtoInstance) {
            node = ((VRMLProtoInstance)vp).getImplementationNode();
            pViewport = (VRMLProtoInstance)vp;

            while((node != null) && (node instanceof VRMLProtoInstance))
                node = ((VRMLProtoInstance)node).getImplementationNode();

            if((node != null) && !(node instanceof VRMLViewportNodeType))
                throw new InvalidFieldValueException(VIEWPORT_PROTO_MSG);

        } else if(vp != null &&
                  (!(vp instanceof VRMLViewportNodeType))) {
            throw new InvalidFieldValueException(VIEWPORT_NODE_MSG);
        } else {
            pViewport = null;
            node = (VRMLViewportNodeType)vp;
        }

        vfViewport = (VRMLViewportNodeType)node;

        if(vp != null)
            updateRefs(vp, true);
    }

    /**
     * Fetch the viewport node instance that this layer currently has. If no
     * instance is used, returns null.
     *
     * @return The current node instance
     */
    public VRMLNodeType getViewport() {
        return vfViewport;
    }

    /**
     * Get the type of viewport layout policy that the contained viewport node
     * represents. This is a shortcut to fetching the viewport instance
     * directly, walking the proto heirarchy and so forth.
     * <p>
     * This determines how the viewport is managed by the system during window
     * resizes etc. It is a fixed value that never changes for the node
     * implementation.
     * <p>
     * If no viewport node is defined, return VIEWPORT_FULLWINDOW.
     * <p>
     * If no viewport is yet referenced courtesy of an
     * externproto, this returns VIEWPORT_UNDEFINED until it is updated.
     *
     * @return One of the VIEWPORT_* constant values
     */
    public int getViewportType() {
        int ret_val = VRMLViewportNodeType.VIEWPORT_FULLWINDOW;

        if(vfViewport == null) {
            if(pViewport != null)
                ret_val = VRMLViewportNodeType.VIEWPORT_UNDEFINED;
        } else
            ret_val = vfViewport.getViewportType();

        return ret_val;
    }

    /**
     * Set the ID of this layer. The layer should pass this down to all
     * contained geometry through the updateRefCount() method. This method
     * should only ever be called once, when just after construction.
     *
     * @param id The id of this layer
     */
    public void setLayerId(int id) {
        layerId = id;

        updateRefCount(id, true);

        int num_kids = vfChildren.size();

        // now propogate that through all the children.
        for(int i = 0; i < num_kids; i++) {
            VRMLNodeType n = (VRMLNodeType)vfChildren.get(i);

            if(n != null)
                updateRefs(n, true);
        }

        if(vfViewport != null)
            updateRefs(vfViewport, true);
    }

    /**
     * Get the ID of this layer. If none has been set, it should return a value
     * of -1.
     *
     * @return The ID of this layer or -1 if not yet set
     */
    public int getLayerId() {
        return layerId;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_LAYER_INDEX)
            return null;

        return fieldDecl[index];
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
     * Get the number of fields.
     *
     * @param The number of fields.
     */
    public int getNumFields() {
        return NUM_FIELDS;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.LayerNodeType;
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

        for(int i = 0; i < vfChildren.size(); i++) {
            VRMLNodeType n = (VRMLNodeType)vfChildren.get(i);
            n.setupFinished();
        }

        if(pViewport != null)
            pViewport.setupFinished();
        else if(vfViewport != null)
            vfViewport.setupFinished();
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
            case FIELD_CHILDREN:
                int num_kids = vfChildren.size();

                VRMLNodeType[] nodeTmp = new VRMLNodeType[num_kids];
                vfChildren.toArray(nodeTmp);
                fieldData.clear();
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = num_kids;
                break;

            case FIELD_VIEWPORT:
                fieldData.clear();
                if(pViewport != null)
                    fieldData.nodeValue = pViewport;
                else
                    fieldData.nodeValue = vfViewport;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_IS_PICKABLE:
                fieldData.clear();
                fieldData.booleanValue = vfIsPickable;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
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
                case FIELD_CHILDREN:
                    int num_kids = vfChildren.size();

                    VRMLNodeType[] nodeTmp = new VRMLNodeType[num_kids];
                    vfChildren.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, num_kids);
                    break;

                case FIELD_IS_PICKABLE:
                    destNode.setValue(destIndex, vfIsPickable);
                    break;

                case FIELD_VIEWPORT:
                    // Cannot route this as it is initOnly
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
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the field
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldAccessException {

        switch(index) {
            case FIELD_IS_PICKABLE:
                setPickable(value);
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
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        boolean notif = false;

        switch(index) {
            case FIELD_CHILDREN:
                if(!inSetup)
                    clearChildren();

                if(child != null)
                    addChildNode(child);

                notif = true;
                break;

            case FIELD_ADDCHILDREN:
                if(inSetup)
                    throw new InvalidFieldAccessException(
                          "Cannot set an inputOnly field in a file: addChildren");

                if(child != null)
                    addChildNode(child);

                notif = true;
                break;

            case FIELD_VIEWPORT:
                setViewport(child);
                break;

            case FIELD_REMOVECHILDREN:
                if(inSetup)
                    throw new InvalidFieldAccessException(
                          "Cannot set an inputOnly field in a file: removeChildren");

                if(child != null)
                    removeChildNode(child);

                notif = true;
                break;

            default:
                super.setValue(index, child);
        }

        if(!inSetup && notif) {
            // Send off events for the children field as that is what has
            // really been modified by the calls here. add/removeChildren
            // are eventIns, not the real value.
            hasChanged[FIELD_CHILDREN] = true;
            fireFieldChanged(FIELD_CHILDREN);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param children The new value to use for the node
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
            case FIELD_CHILDREN:
                if(!inSetup)
                    clearChildren();

                for(int i = 0; i < numValid; i++ )
                    addChildNode(children[i]);

                break;

            case FIELD_ADDCHILDREN:
                if(inSetup)
                    throw new InvalidFieldAccessException(
                          "Cannot set an inputOnly field in a file: addChildren");

                for(int i = 0; i < numValid; i++ )
                    addChildNode(children[i]);

                break;

            case FIELD_REMOVECHILDREN:
                if(inSetup)
                    throw new InvalidFieldAccessException(
                          "Cannot set an inputOnly field in a file: removehildren");

                for(int i = 0; i < numValid; i++ )
                    removeChildNode(children[i]);

                break;

            default:
                super.setValue(index, children, numValid);
        }

        if(!inSetup) {
            // Send off events for the children field as that is what has
            // really been modified by the calls here. add/removeChildren
            // are eventIns, not the real value.
            hasChanged[FIELD_CHILDREN] = true;
            fireFieldChanged(FIELD_CHILDREN);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearChildren() {
        int num_kids = vfChildren.size();

        VRMLNodeType[] nodeTmp = new VRMLNodeType[num_kids];

        vfChildren.toArray(nodeTmp);

        for(int i = 0; i < num_kids; i++) {
            if(nodeTmp[i] instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)nodeTmp[i]).setShared(false);

            if(layerId != -1)
                updateRefs(nodeTmp[i], false);
        }

        if(num_kids > 0)
            stateManager.registerRemovedNodes(nodeTmp);

        vfChildren.clear();
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addChildNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        if(node instanceof VRMLGroupingNodeType) {
            ((VRMLGroupingNodeType)node).setShared(true);
        } else if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if(impl instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)impl).setShared(true);
        }

        vfChildren.add(node);

        if(layerId != -1)
            updateRefs(node, true);

        if(!inSetup)
            stateManager.registerAddedNode(node);
    }

    /**
     * Remove the given node from this grouping node. If the node is not a
     * child of this node, the request is silently ignored.
     *
     * @param node The node to remove
     */
    protected void removeChildNode(VRMLNodeType node) {
        if(node instanceof VRMLGroupingNodeType) {
            ((VRMLGroupingNodeType)node).setShared(false);
        } else if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if(impl instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)impl).setShared(false);
        }

        if(layerId != -1)
            updateRefs(node, false);

        // TODO: This looks dodgy
        if(!inSetup)
            vfChildren.remove(node);

        if(!inSetup)
            stateManager.registerRemovedNode(node);
    }
}
