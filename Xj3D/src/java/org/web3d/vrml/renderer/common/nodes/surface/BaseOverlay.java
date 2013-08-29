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

package org.web3d.vrml.renderer.common.nodes.surface;

// Standard imports
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of an Overlay node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.13 $
 */
public abstract class BaseOverlay extends AbstractNode
    implements VRMLOverlayNodeType {

    // Field index constants

    /** The field index for visible */
    protected static final int FIELD_VISIBLE = LAST_NODE_INDEX + 1;

    /** The field index for windowSizeChanged */
    protected static final int FIELD_WINDOW_SIZE_CHANGED = LAST_NODE_INDEX + 2;

    /** The field index for layout */
    protected static final int FIELD_LAYOUT = LAST_NODE_INDEX + 3;

    /** The last field index used by this class */
    protected static final int LAST_OVERLAY_INDEX = FIELD_LAYOUT;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_OVERLAY_INDEX + 1;

    /** Message when the window dimension is < 0 */
    private static final String NEG_WINDOW_SIZE_MSG =
        "One of the window dimensions is less than zero";

    /** Message for when the proto is not a Appearance */
    protected static final String BAD_PROTO_MSG =
        "Proto does not describe a SurfaceChildNode object";

    /** Message for when the node in setValue() is not a Appearance */
    protected static final String BAD_NODE_MSG =
        "Node does not describe a SurfaceChildNode object";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** exposedField SFBool visible */
    protected boolean vfVisible;

    /** The value of the windowSizeChanged eventOut */
    protected float[] vfWindowSize;

    /** The value of the layout SFNode exposedField */
    protected VRMLSurfaceLayoutNodeType vfLayout;

    /** The proto representation of the the layout field */
    protected VRMLProtoInstance pLayout;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] { FIELD_LAYOUT, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_VISIBLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFBool",
                                    "visible");
        fieldDecl[FIELD_WINDOW_SIZE_CHANGED] =
           new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                    "SFVec2f",
                                    "windowSizeChanged");
        fieldDecl[FIELD_LAYOUT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFNode",
                                    "layout");

        fieldMap.put("windowSizeChanged",
                     new Integer(FIELD_WINDOW_SIZE_CHANGED));

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_VISIBLE);
        fieldMap.put("visible", idx);
        fieldMap.put("set_visible", idx);
        fieldMap.put("visible_changed", idx);

        idx = new Integer(FIELD_LAYOUT);
        fieldMap.put("layout", idx);
        fieldMap.put("set_layout", idx);
        fieldMap.put("layout_changed", idx);
    }

    /**
     * Construct a new default Overlay object
     */
    protected BaseOverlay() {
        super("Overlay");

        hasChanged = new boolean[NUM_FIELDS];

        // Set the default values for the fields
        vfWindowSize = new float[2];
        vfVisible = true;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseOverlay(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("visible");
            VRMLFieldData data = node.getFieldValue(index);
            vfVisible = data.booleanValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLSurfaceNodeType interface.
    //-------------------------------------------------------------

    /**
     * Get the current visibility state of this node.
     *
     * @return true if the node is current visible, false otherwise
     */
    public boolean isVisible() {
        return vfVisible;
    }

    /**
     * Set the visibility state of the surface. A non-visible surface will
     * still take events and update, just not be rendered.
     *
     * @param state true to make this node visible, false to hide
     */
    public void setVisible(boolean state) {
        if(state != vfVisible) {
            vfVisible = state;

            hasChanged[FIELD_VISIBLE] = true;
            fireFieldChanged(FIELD_VISIBLE);

            if(vfLayout != null)
                vfLayout.setParentVisible(state);
        }
    }

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

    //-------------------------------------------------------------
    // Methods required by the VRMLOverlayNodeType interface.
    //-------------------------------------------------------------

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
    public void setLayout(VRMLNodeType layout)
        throws InvalidFieldValueException {

        if(layout == null)
            vfLayout = null;
        else {
            if(!(layout instanceof VRMLSurfaceLayoutNodeType)) {
                if(!(layout instanceof VRMLProtoInstance))
                    throw new InvalidFieldValueException(BAD_NODE_MSG);

                // check the proto for the correct type.
                VRMLProtoInstance proto = (VRMLProtoInstance)layout;
                VRMLNodeType impl = proto.getImplementationNode();

                if(!(impl instanceof VRMLSurfaceLayoutNodeType))
                    throw new InvalidFieldValueException(BAD_PROTO_MSG);

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

    /**
     * Get the current layout of this overlay node. If none is set,
     * null is returned. The node returned will be either
     * {@link VRMLSurfaceLayoutNodeType} or {@link VRMLProtoInstance}.
     *
     * @return The current list of children or null
     */
    public VRMLNodeType getLayout() {
        if(pLayout == null)
            return vfLayout;
        else
            return pLayout;
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
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_OVERLAY_INDEX)
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
        return TypeConstants.OverlayNodeType;
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
            case FIELD_VISIBLE:
                fieldData.clear();
                fieldData.booleanValue = vfVisible;
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
                case FIELD_VISIBLE:
                    destNode.setValue(destIndex, vfVisible);
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
            case FIELD_VISIBLE:
                setVisible(value);
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

            default:
                super.setValue(index, child);
        }

        hasChanged[FIELD_LAYOUT] = true;
        fireFieldChanged(FIELD_LAYOUT);
    }
}
