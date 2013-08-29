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

package org.web3d.vrml.renderer.common.nodes.layout;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of an Layer2D node.
 * <p>
 *
 * The basic definition of Layer2D is:
 * <pre>
 * Layer2D : X3DLayerNode {
 *   SFNode  [in,out] layout             []    [X3DLayoutChildNode]
 *   SFBool  [in,out] isPickable         TRUE
 *   SFNode  [in,out] metadata           NULL  [X3DMetadataObject]
 *   SFVec2f [out]    windowSizeChanged  NULL  [X3DMetadataObject]
 *   SFNode  []       viewport           NULL  [X3DViewportNode]
 * }
 *
 * Layout is always relative to the size of the viewport, not the full screen.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class BaseLayer2D extends AbstractNode
    implements VRMLLayerNodeType {

    // Field index constants

    /** The field index for windowSizeChanged */
    protected static final int FIELD_WINDOW_SIZE_CHANGED = LAST_NODE_INDEX + 1;

    /** The field index for layout */
    protected static final int FIELD_LAYOUT = LAST_NODE_INDEX + 2;

    /** The field index for the isPickable field */
    protected static final int FIELD_IS_PICKABLE = LAST_NODE_INDEX + 3;

    /** The field index for the viewport node field */
    protected static final int FIELD_VIEWPORT = LAST_NODE_INDEX + 4;

    /** The last field index used by this class */
    protected static final int LAST_LAYER_INDEX = FIELD_VIEWPORT;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_LAYER_INDEX + 1;

    /** Message when the window dimension is < 0 */
    private static final String NEG_WINDOW_SIZE_MSG =
        "One of the window dimensions is less than zero";

    /** Message for when the proto is not a Appearance */
    protected static final String LAYOUT_PROTO_MSG =
        "Proto does not describe a SurfaceChildNode object";

    /** Message for when the node in setValue() is not a Appearance */
    protected static final String LAYOUT_NODE_MSG =
        "Node does not describe a SurfaceChildNode object";

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

    // The VRML field values

    /** exposedField SFBool isPickable true */
    protected boolean vfIsPickable;

    /** The value of the windowSizeChanged eventOut */
    protected float[] vfWindowSize;

    /** The value of the layout SFNode exposedField */
    protected VRMLSurfaceLayoutNodeType vfLayout;

    /** The proto representation of the the layout field */
    protected VRMLProtoInstance pLayout;

    /** Proto version of the viewport */
    protected VRMLProtoInstance pViewport;

    /** SFNode viewport. */
    protected VRMLViewportNodeType vfViewport;

    /** The Id of this layer */
    protected int layerId;

    /** List of those who want to know about role changes, likely 1 */
    protected ArrayList layerListeners;


    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] {
            FIELD_LAYOUT,
            FIELD_VIEWPORT,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_WINDOW_SIZE_CHANGED] =
           new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                    "SFVec2f",
                                    "windowSizeChanged");
        fieldDecl[FIELD_LAYOUT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFNode",
                                    "layout");
        fieldDecl[FIELD_IS_PICKABLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                    "SFBool",
                    "isPickable");
        fieldDecl[FIELD_VIEWPORT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                    "SFNode",
                    "viewport");


        fieldMap.put("windowSizeChanged",
                     new Integer(FIELD_WINDOW_SIZE_CHANGED));
        fieldMap.put("viewport",new Integer(FIELD_VIEWPORT));

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_LAYOUT);
        fieldMap.put("layout", idx);
        fieldMap.put("set_layout", idx);
        fieldMap.put("layout_changed", idx);

        idx = new Integer(FIELD_IS_PICKABLE);
        fieldMap.put("isPickable",idx);
        fieldMap.put("set_isPickable",idx);
        fieldMap.put("isPickable_changed",idx);
     }

    /**
     * Construct a new default Layer2D object
     */
    protected BaseLayer2D() {
        super("Layer2D");

        hasChanged = new boolean[NUM_FIELDS];

        // Set the default values for the fields
        layerId = -1;
        vfIsPickable = true;
        vfWindowSize = new float[2];
        layerListeners = new ArrayList(1);
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseLayer2D(VRMLNodeType node) {
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

    //-------------------------------------------------------------
    // Methods defined by VRMLLayerNodeType
    //-------------------------------------------------------------

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
     * @param node The node instance to use or null to clear
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
        if(pViewport != null)
            return pViewport;
        else
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

        if(vfViewport != null)
            updateRefs(vfViewport, true);

        if(vfLayout != null)
            updateRefs(vfLayout, true);
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

    //-------------------------------------------------------------
    // Methods defined by VRMLSurfaceNodeType
    //-------------------------------------------------------------

    /**
     * Notification that the area allocated to the surface has changed. The
     * new size in pixels is given.
     *
     * @param width The width of the surface in pixels
     * @param height The height of the surface in pixels
     */
    public void surfaceSizeChanged(int width, int height) {
        if(width < 0 || height < 0)
            throw new IllegalArgumentException(NEG_WINDOW_SIZE_MSG);

        vfWindowSize[0] = width;
        vfWindowSize[1] = height;

        if(vfLayout != null)
            vfLayout.windowChanged(0, 0, width, height);

        hasChanged[FIELD_WINDOW_SIZE_CHANGED] = true;
        fireFieldChanged(FIELD_WINDOW_SIZE_CHANGED);
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
        if(index < 0  || index > LAST_LAYER_INDEX)
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
        return TypeConstants.LayerNodeType;
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

        if(pLayout != null)
            pLayout.setupFinished();
        else if(vfLayout != null)
            vfLayout.setupFinished();

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
            case FIELD_IS_PICKABLE:
                fieldData.clear();
                fieldData.booleanValue = vfIsPickable;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_LAYOUT:
                fieldData.clear();
                if(pLayout == null)
                    fieldData.nodeValue = vfLayout;
                else
                    fieldData.nodeValue = pLayout;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_VIEWPORT:
                fieldData.clear();
                if(pViewport != null)
                    fieldData.nodeValue = pViewport;
                else
                    fieldData.nodeValue = vfViewport;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_WINDOW_SIZE_CHANGED:
                fieldData.clear();
                fieldData.floatArrayValue = vfWindowSize;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
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
                case FIELD_IS_PICKABLE:
                    destNode.setValue(destIndex, vfIsPickable);
                    break;

                case FIELD_LAYOUT:
                    if(pLayout != null)
                        destNode.setValue(destIndex, pLayout);
                    else
                        destNode.setValue(destIndex, vfLayout);
                    break;

                case FIELD_WINDOW_SIZE_CHANGED:
                    destNode.setValue(destIndex, vfWindowSize, 2);
                    break;

                case FIELD_VIEWPORT:
                    // Cannot route this as it is initOnly
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field! " + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                               ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a boolean.
     * This would be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_IS_PICKABLE:
                setPickable(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a node.
     * This would be used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_LAYOUT:
                setLayout(child);
                break;

            case FIELD_VIEWPORT:
                setViewport(child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the layout to the new value. Setting a value of null will
     * clear the current layout and leave nothing visible on-screen. The node
     * provided must be either {@link VRMLSurfaceLayoutNodeType} or
     * {@link VRMLProtoInstance}.
     *
     * @param layout The new layout to use
     * @throws InvalidFieldValueException The nodes are not one of the required
     *   types.
     */
    protected void setLayout(VRMLNodeType layout)
        throws InvalidFieldValueException {

        if(layout == null)
            vfLayout = null;
        else {
            if(!(layout instanceof VRMLSurfaceLayoutNodeType)) {
                if(!(layout instanceof VRMLProtoInstance))
                    throw new InvalidFieldValueException(LAYOUT_NODE_MSG);

                // check the proto for the correct type.
                VRMLProtoInstance proto = (VRMLProtoInstance)layout;
                VRMLNodeType impl = proto.getImplementationNode();

                if(!(impl instanceof VRMLSurfaceLayoutNodeType))
                    throw new InvalidFieldValueException(LAYOUT_PROTO_MSG);

                pLayout = proto;
                vfLayout = (VRMLSurfaceLayoutNodeType)impl;
            } else {
                vfLayout = (VRMLSurfaceLayoutNodeType)layout;
                pLayout = null;
            }
        }

        if(!inSetup) {
            if(vfLayout != null)
                vfLayout.windowChanged(0,
                                       0,
                                       (int)vfWindowSize[0],
                                       (int)vfWindowSize[1]);

            hasChanged[FIELD_LAYOUT] = true;
            fireFieldChanged(FIELD_LAYOUT);
        }

    }
}
