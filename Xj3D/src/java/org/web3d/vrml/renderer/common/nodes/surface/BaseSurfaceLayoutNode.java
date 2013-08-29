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
// None

// Application specific imports
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.util.FieldValidator;

/**
 * Base implementation of any surface layout node.
 * <p>
 *
 * The implementation does not define the windowChanged method because it
 * assumes the derived class has all the specific information about the
 * required rules to update and place the children.
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public abstract class BaseSurfaceLayoutNode extends BaseSurfaceChildNode
    implements VRMLSurfaceLayoutNodeType, VRMLSurfaceLayoutListener {

    // Field index constants

    /** The field index for cycleInterval */
    protected static final int FIELD_CHILDREN = LAST_SURFACE_CHILD_INDEX + 1;

    /** The last field index used by this class */
    protected static final int LAST_LAYOUT_INDEX = FIELD_CHILDREN;

    /** Message for when the proto is not a Appearance */
    protected static final String BAD_PROTO_MSG =
        "Proto does not describe a SurfaceChildNode object";

    /** Message for when the node in setValue() is not a Appearance */
    protected static final String BAD_NODE_MSG =
        "Node does not describe a SurfaceChildNode object";

    // The VRML field values

    /** The value of the children exposedField */
    protected VRMLNodeType[] vfChildren;

    /**
     * Construct a new default Overlay object
     */
    protected BaseSurfaceLayoutNode(String name) {
        super(name);

        vfChildren = new VRMLNodeType[0];
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLSurfaceChildNodeType interface.
    //-------------------------------------------------------------

    /**
     * Set the visibility state of the surface. A non-visible surface will
     * still take events and update, just not be rendered.
     *
     * @param state true to make this node visible, false to hide
     */
    public void setVisible(boolean state) {
        super.setVisible(state);

        updateVisibility();
    }

    /**
     * Notification from the parent node about this node's visiblity state.
     * Used to control the rendering so that if a parent is not visible it can
     * inform this node that it is also not visible without needing to stuff
     * with the local visibility state.
     *
     * @param state true to make this node visible, false to hide
     */
    public void setParentVisible(boolean state) {

        super.setParentVisible(state);

        updateVisibility();
    }

    //----------------------------------------------------------
    // Methods required by the VRMLSurfaceLayoutNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the drawable content of this node to the surface. If value is set
     * to null, then it clears all the renderable list and nothing is show.
     * The nodes provided must be {@link VRMLSurfaceChildNodeType} or
     * {@link VRMLProtoInstance}.
     *
     * @param kids The list of new children to render
     * @throws InvalidFieldValueException The nodes are not one of the required
     *   types.
     */
    public void setChildren(VRMLNodeType[] kids)
        throws InvalidFieldValueException {

        if(kids == null)
            vfChildren = null;
        else {
            for(int i = 0; i < kids.length; i++) {
                if(!(kids[i] instanceof VRMLSurfaceChildNodeType)) {
                    if(!(kids[i] instanceof VRMLProtoInstance))
                        throw new InvalidFieldValueException(BAD_NODE_MSG);

                    // check the proto for the correct type.
                    VRMLProtoInstance proto = (VRMLProtoInstance)kids[i];
                    VRMLNodeType impl = proto.getImplementationNode();

                    if(!(impl instanceof VRMLSurfaceChildNodeType))
                        throw new InvalidFieldValueException(BAD_PROTO_MSG);
                }
            }

            vfChildren = kids;
        }

        updateVisibility();

        hasChanged[FIELD_CHILDREN] = true;
        fireFieldChanged(FIELD_CHILDREN);
    }

    /**
     * Get the list of current relationships used by this node. If none are
     * defined, this returns a null value.
     * be {@link VRMLOverlayRelationshipNodeType} or {@link VRMLProtoInstance}.
     *
     * @return The list of current relationships or null
     */
    public VRMLNodeType[] getChildren() {
        return vfChildren;
    }

    /**
     * Set the new window size, requesting that the layout implementation
     * rebuild and re-evalutate all of the items it contains. If this layout
     * is a child of another layout, the size set will be the allocated size
     * for the child window. The location is in standard 2D screen coordinates
     * of the top-left corner.
     * <p>
     *
     * After setting the screen bounds, this will call
     * {@link #updateManagedNodes()}.
     *
     * @param x The x location of the window in pixels
     * @param y The y location of the window in pixels
     * @param width The width of the window in pixels
     * @param height The height of the window in pixels
     */
    public void windowChanged(int x, int y, int width, int height) {

        screenBounds.setBounds(x, y, width, height);

        updateManagedNodes();
    }

    //----------------------------------------------------------
    // Methods required by the VRMLSurfaceLayoutListener interface.
    //----------------------------------------------------------

    /**
     * Notification that its size has changed. Values shall not be negative.
     *
     * @param width The new width of the surface
     * @param height The new height of the surface
     */
    public void surfaceResized(int width, int height) {
        updateManagedNodes();
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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

        int size = (vfChildren == null) ? 0 : vfChildren.length;

        for(int i = 0; i < size; i++) {
            if(vfChildren[i] != null)
                vfChildren[i].setupFinished();
        }

        updateVisibility();
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.SurfaceLayoutNodeType;
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
                fieldData.clear();
                fieldData.nodeArrayValue = vfChildren;
                fieldData.numElements =
                    (vfChildren == null) ? 0 : vfChildren.length;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                break;

            default:
                return super.getFieldValue(index);
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
                    destNode.setValue(destIndex,
                                      vfChildren,
                                      vfChildren.length);
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
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_CHILDREN:
                if(inSetup)
                    appendNode(child);
                else
                    setChildren(new VRMLNodeType[] { child });
                break;

            default:
                super.setValue(index, child);
        }

        if(!inSetup) {
            hasChanged[FIELD_CHILDREN] = true;
            fireFieldChanged(FIELD_CHILDREN);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_CHILDREN:
                setChildren(children);

                if(!inSetup) {
                    hasChanged[FIELD_CHILDREN] = true;
                    fireFieldChanged(FIELD_CHILDREN);
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
     * Convenience method to update the children node(s) to the new positions.
     * The position may have changed because the window size changed, or the
     * layout node settings have changed. The default implementation does
     * nothing, but is called when the screen size changed. Derived classes
     * should override this to do something useful.
     */
    protected void updateManagedNodes() {
    }

    /**
     * Add a child node to the current list. This is only called during the
     * setup process. Once setupFinished has been called, this method will
     * not be called again. Ok for derived class to override.
     *
     * @param node The new node to add
     */
    protected void appendNode(VRMLNodeType node) {

        if(node != null) {
            if(node instanceof VRMLSurfaceChildNodeType) {
                ((VRMLSurfaceChildNodeType)node).setLayoutListener(this);
            } else {
                if(!(node instanceof VRMLProtoInstance))
                    throw new InvalidFieldValueException(BAD_NODE_MSG);

                // check the proto for the correct type.
                VRMLProtoInstance proto = (VRMLProtoInstance)node;
                VRMLNodeType impl = proto.getImplementationNode();

                if(!(impl instanceof VRMLSurfaceChildNodeType))
                    throw new InvalidFieldValueException(BAD_PROTO_MSG);

                ((VRMLSurfaceChildNodeType)impl).setLayoutListener(this);
            }
        }

        if(vfChildren != null) {
            int size = vfChildren.length + 1;
            VRMLNodeType[] tmp = new VRMLNodeType[size];

            System.arraycopy(vfChildren, 0, tmp, 0, size - 1);
            vfChildren = tmp;
            vfChildren[size - 1] = node;
        }
        else
        {
            vfChildren = new VRMLNodeType[] { node };
        }
    }

    /**
     * Update the parent visibility flag on all the children
     */
    private void updateVisibility() {
        VRMLSurfaceChildNodeType impl;

        for(int i = 0; i < vfChildren.length; i++) {
            if(!(vfChildren[i] instanceof VRMLSurfaceChildNodeType)) {
                VRMLProtoInstance proto = (VRMLProtoInstance)vfChildren[i];
                impl = (VRMLSurfaceChildNodeType)proto.getImplementationNode();
            } else {
                impl = (VRMLSurfaceChildNodeType)vfChildren[i];
            }

            impl.setParentVisible(vfVisible && parentVisibility);
        }
    }
}
