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
 * This is the base for the LayerSet node.
 * <p>
 * The basic definition of LayerSet is:
 * <pre>
 * LayerSet : X3DNode {
 *  MFNode  [in,out]  layers      []   [Layer]
 *  SFInt32 [in,out]  activeLayer 0    (-?,?)
 *  SFNode  [in,out]  metadata    NULL [X3DMetadataObject]
 *  MFInt32 [in,out]  order       []   [0,?)
 * }
 * </pre>
 *
 * @author Brad Vender, Justin Couch
 * @version $Revision: 1.5 $
 */
public class BaseLayerSet extends AbstractNode
    implements VRMLLayerSetNodeType {

    /** Index of the layers field */
    protected static final int FIELD_LAYERS = LAST_NODE_INDEX+1;

    /** Index of the mainLayer field */
    protected static final int FIELD_ACTIVE_LAYER = LAST_NODE_INDEX+2;

    /** Index of the order field */
    protected static final int FIELD_ORDER = LAST_NODE_INDEX+3;

    /** The last valid field index for this node */
    protected static final int LAST_LAYERSET_INDEX = FIELD_ORDER;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_LAYERSET_INDEX + 1;

    /** Message for when the proto is not a Layer */
    protected static final String LAYER_PROTO_MSG =
        "Proto does not describe a Layer object";

    /** Message for when the node in setValue() is not a Layer */
    protected static final String LAYER_NODE_MSG =
        "Node does not describe a Layer object";

    /** Message when one of the values in the order is negative */
    private static final String NEGATIVE_ORDER_ERR =
        "The order field of LayerSet cannot contain negative values.";

    /**
     * Message when the layerSet order field has an index that is greater
     * than the number of layers provided.
     */
    private static final String INVALID_ORDER_IDX_ERR =
        "One or more order index values in this LayerSet is greater than the " +
        "number of Layer nodes provided: ";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** MFNode children NULL */
    protected ArrayList vfLayers;

    /** SFInt32 activeLayer 0 */
    protected int vfActiveLayer;

    /** MFInt32 order [] */
    protected int[] vfOrder;

    /** The number of valid layers */
    private int layerCount;

    /** The number of valid values in vfOrder */
    private int numOrder;

    /** Flag indicating if the render order has changed this time */
    private boolean renderOrderChanged;

    /** Flag indicating if the layers have changed this time */
    private boolean layerListChanged;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {

        nodeFields = new int[]{FIELD_LAYERS,FIELD_METADATA};

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_LAYERS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                    "MFNode",
                    "layers");
        fieldDecl[FIELD_ACTIVE_LAYER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                    "SFInt32",
                    "activeLayer");
        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                    "SFNode",
                    "metadata");
        fieldDecl[FIELD_ORDER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                    "MFInt32",
                    "order");

        Integer idx=new Integer(FIELD_LAYERS);
        fieldMap.put("layers",idx);
        fieldMap.put("set_layers",idx);
        fieldMap.put("layers_changed",idx);

        idx=new Integer(FIELD_ACTIVE_LAYER);
        fieldMap.put("activeLayer",idx);
        fieldMap.put("set_activeLayer",idx);
        fieldMap.put("activeLayer_changed",idx);

        idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata",idx);
        fieldMap.put("set_metadata",idx);
        fieldMap.put("metadata_changed",idx);

        idx = new Integer(FIELD_ORDER);
        fieldMap.put("order",idx);
        fieldMap.put("set_order",idx);
        fieldMap.put("order_changed",idx);
    }

    /**
     * Construct a new default instance of this class.
     */
    protected BaseLayerSet() {
        super("LayerSet");

        vfActiveLayer = 0;
        vfOrder = FieldConstants.EMPTY_MFINT32;
        vfLayers = new ArrayList();
        renderOrderChanged = false;
        layerListChanged = false;

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
    protected BaseLayerSet(VRMLNodeType node) {
        this();
        checkNodeType(node);

        // TODO: Implement copying
    }


    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    /**
     * Process the end of frame event completion notification. This is used
     * to check range values for the layer nodes versus the order field
     * request.
     */
    public void allEventsComplete() {
        // The number of layers registered here, plus one for the default
        // layer.
        int num_layers = vfLayers.size() + 1;

        for(int i = 0; i < numOrder; i++) {
            if(vfOrder[i] >= num_layers)
                errorReporter.errorReport(INVALID_ORDER_IDX_ERR + vfOrder[i],
                                          null);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLLayerSetNodeType
    //----------------------------------------------------------

    /**
     * Get the layers this set manages. Provides a live reference not a copy.
     *
     * @return An array of VRMLNodeTypes
     */
    public VRMLNodeType[] getLayers() {
        VRMLNodeType layers[] = new VRMLNodeType[vfLayers.size()];

        vfLayers.toArray(layers);

        return layers;
    }

    /**
     * Set the layers field with a collection of new layers.
     *
     * @param layers Array of new layer instance nodes to use
     * @throws InvalidFieldValueException One or more of the passed nodes does
     *   not represent a layer node
     */
    public void setLayers(VRMLNodeType[] layers) {
        clearChildren();

        if(layers == null)
            return;

        layerCount = 0;

        for(int i = 0; i < layers.length; i++)
            addChildNode(layers[i]);

        hasChanged[FIELD_LAYERS] = true;
        fireFieldChanged(FIELD_LAYERS);
    }

    /**
     * Get the ID of the layer that should be active for navigation in
     * during this frame.
     *
     * @return An int greater than or equal to zero
     */
    public int getActiveNavigationLayer() {
        return vfActiveLayer;
    }

    /**
     * Query how many layers should be actively rendered this frame. This will
     * come from the order list's length
     *
     * @return A positive number
     */
    public int getNumRenderedLayers() {
        return numOrder;
    }

    /**
     * Get the rendering order of the layers, copied into the given array.
     * Assumes that the array must be at least {@link #getNumRenderedLayers()}
     * in length;
     *
     * @param order The array to copy values into
     */
    public void getRenderOrder(int[] order) {
        System.arraycopy(vfOrder, 0, order, 0, numOrder);
    }

    /**
     * Check to see if the layers themselves have changed this frame. If they
     * have, we need to rebuild the user interface next time around.
     *
     * @return true if the layer list has changed since last frame
     */
    public boolean hasLayerListChanged() {
        boolean ret_val = layerListChanged;

        layerListChanged = false;

        return ret_val;
    }

    /**
     * Check to see if the render order has changed this frame. If it has,
     * return true, otherwise return false. Can only be asked once per frame,
     * and is reset to false after calling.
     *
     * @return true if the render order has changed since last frame
     */
    public boolean hasRenderOrderChanged() {
        boolean ret_val = renderOrderChanged;

        renderOrderChanged = false;

        return ret_val;
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
        if(index < 0  || index > LAST_LAYERSET_INDEX)
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
        return TypeConstants.LayerSetNodeType;
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

        for(int i = 0; i < vfLayers.size(); i++) {
            VRMLNodeType n = (VRMLNodeType)vfLayers.get(i);
            n.setupFinished();
        }

        // Call allEventsComplete now to go check on the order
        // declaration.
        allEventsComplete();
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
            case FIELD_LAYERS:
                int num_kids = vfLayers.size();

                VRMLNodeType[] nodeTmp = new VRMLNodeType[num_kids];
                vfLayers.toArray(nodeTmp);
                fieldData.clear();
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = num_kids;
                break;

            case FIELD_ORDER:
                fieldData.clear();
                fieldData.intArrayValue = vfOrder;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = numOrder;
                break;

            case FIELD_ACTIVE_LAYER:
                fieldData.clear();
                fieldData.intValue = vfActiveLayer;
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
                case FIELD_LAYERS:
                    int num_kids = vfLayers.size();

                    VRMLNodeType[] nodeTmp = new VRMLNodeType[num_kids];
                    vfLayers.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, num_kids);
                    break;

                case FIELD_ORDER:
                    destNode.setValue(destIndex, vfOrder, numOrder);
                    break;

                case FIELD_ACTIVE_LAYER:
                    destNode.setValue(destIndex, vfActiveLayer);
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
     * Set the value of the field at the given index as an integer.
     * This would be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the field
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(index == FIELD_ACTIVE_LAYER) {
            vfActiveLayer = value;

            if(!inSetup) {
                hasChanged[FIELD_ACTIVE_LAYER] = true;
                fireFieldChanged(FIELD_ACTIVE_LAYER);
            }
        } else
            super.setValue(index, value);
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the field
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(index == FIELD_ORDER) {
            // Check for no negative numbers. Note that we can't check for
            // numbers over the currently registered set of layers here because
            // the end user may set this order and then add/remove the layer
            // nodes afterwards. We'll reject immediately a negative number
            // as an invalid field value, but have to let the other case
            // through as OK and look with the end of frame listener. The
            // end user of this node will have to check for the out of range
            // condition.
            for(int i = 0; i < numValid; i++)
                if(value[i] < 0)
                    throw new InvalidFieldValueException(NEGATIVE_ORDER_ERR);

            if(vfOrder.length < numValid)
                vfOrder = new int[numValid];

            System.arraycopy(value, 0, vfOrder, 0, numValid);
            numOrder = numValid;
            renderOrderChanged = true;

            if(!inSetup) {
                stateManager.addEndOfThisFrameListener(this);

                hasChanged[FIELD_ORDER] = true;
                fireFieldChanged(FIELD_ORDER);
            }
        } else
            super.setValue(index, value, numValid);
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

        switch(index) {
            case FIELD_LAYERS:
                if(!inSetup)
                    clearChildren();

                if(child != null)
                    addChildNode(child);

                layerListChanged = true;

                if(!inSetup) {
                    stateManager.addEndOfThisFrameListener(this);
                    hasChanged[FIELD_LAYERS] = true;
                    fireFieldChanged(FIELD_LAYERS);
                }
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
     * @param children The new value to use for the node
     * @param numValid The number of valid values to copy from the array
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
            case FIELD_LAYERS:
                if(!inSetup)
                    clearChildren();

                for(int i = 0; i < numValid; i++ )
                    addChildNode(children[i]);

                layerListChanged = true;

                if(!inSetup) {
                    stateManager.addEndOfThisFrameListener(this);

                    hasChanged[FIELD_LAYERS] = true;
                    fireFieldChanged(FIELD_LAYERS);
                }
                break;

            default:
                super.setValue(index, children, numValid);
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
        int num_kids = vfLayers.size();

        for(int i = 0; i < num_kids; i++) {
            VRMLNodeType node = (VRMLNodeType)vfLayers.get(i);
            stateManager.registerRemovedNode(node);
        }

        layerCount = 0;
        vfLayers.clear();
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

        VRMLLayerNodeType layer = null;

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLLayerNodeType))
                throw new InvalidFieldValueException(LAYER_PROTO_MSG);

            layer = (VRMLLayerNodeType)impl;
        } else if(node != null && !(node instanceof VRMLLayerNodeType)) {
            throw new InvalidFieldValueException(LAYER_NODE_MSG);
        } else
            layer = (VRMLLayerNodeType)node;

        // Increment first, then set. Layer ID 0 is always the main layer.
        layerCount++;
        layer.setLayerId(layerCount);

        vfLayers.add(node);

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

        layerCount--;

        // TODO: This looks dodgy
        if(!inSetup)
            vfLayers.remove(node);

        if(!inSetup)
            stateManager.registerRemovedNode(node);
    }
}
