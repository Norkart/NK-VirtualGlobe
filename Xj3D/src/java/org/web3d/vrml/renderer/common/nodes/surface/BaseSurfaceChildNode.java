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
import java.awt.Rectangle;

// Application specific imports
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.InvalidFieldAccessException;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLSurfaceChildNodeType;
import org.web3d.vrml.nodes.VRMLSurfaceLayoutListener;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.util.FieldValidator;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Base implementation of any surface child node.
 * <p>
 *
 * All surface child nodes start with an automatically sized bounding box and
 * are visible.
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public abstract class BaseSurfaceChildNode extends AbstractNode
    implements VRMLSurfaceChildNodeType {

    // Field index constants

    /** The field index for visible */
    protected static final int FIELD_VISIBLE = LAST_NODE_INDEX + 1;

    /** The field index for bboxSize */
    protected static final int FIELD_BBOX_SIZE = LAST_NODE_INDEX + 2;

    /** The last field index used by this class */
    protected static final int LAST_SURFACE_CHILD_INDEX = FIELD_BBOX_SIZE;

    // The VRML field values

    /** exposedField SFBool visible */
    protected boolean vfVisible;

    /** The real position of the overlay in screen ccordinates */
    protected float[] screenLocation;

    /** The user-supplied bounding box size field */
    protected float[] vfBboxSize;

    /** The real bounds of this object */
    protected Rectangle screenBounds;

    /** The listener for resize events */
    protected VRMLSurfaceLayoutListener listener;

    /** The visibility state of the parent node. Defaults to true */
    protected boolean parentVisibility;

    /**
     * Construct a new default Overlay object
     */
    protected BaseSurfaceChildNode(String name) {
        super(name);

        // Set the default values for the fields
        vfVisible = true;
        vfBboxSize = new float[] { -1, -1 };
        screenLocation = new float[2];
        screenBounds = new Rectangle();
        parentVisibility = true;
    }

    /**
     * Set the fields of the overlay child node that has the fields set
     * based on the fields of the passed in node.
     *
     * @param node The node to copy info from
     */
    protected void copy(VRMLSurfaceChildNodeType node) {
        float[] field = node.getBboxSize();

        vfBboxSize[0] = field[0];
        vfBboxSize[1] = field[1];

        Rectangle bounds = node.getRealBounds();
        screenBounds.setBounds(bounds);

        screenLocation[0] = bounds.x;
        screenLocation[1] = bounds.y;

        vfVisible = node.isVisible();
        parentVisibility = node.getParentVisible();
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLSurfaceChildNodeType interface.
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
        }
    }

    /**
     * Notification from the parent node about this node's visiblity state.
     * Used to control the rendering so that if a parent is not visible it can
     * inform this node that it is also not visible without needing to stuff
     * with the local visibility state. This implementation does nothing, but
     * derived classes may wish to do something with it.
     *
     * @param state true to make this node visible, false to hide
     */
    public void setParentVisible(boolean state) {
        parentVisibility = state;
    }

    /**
     * Request the node's current parent visibility state. Mainly used for
     * proto copying.
     *
     * @param state true to make this node visible, false to hide
     */
    public boolean getParentVisible() {
        return parentVisibility;
    }

    /**
     * Get the value of the 2D bounding box size of this overlay. The bounds
     * are given in pixel coordinates relative to the center of this overlay.
     * Although the return values are always floats, the values will alway be
     * integer based and will not contain fractional values.
     *
     * @return The current bounds
     */
    public float[] getBboxSize() {
        return vfBboxSize;
    }

    /**
     * Get the current value of the 2D bounding box of this overlay. The bounds
     * are given in pixel coordinates relative to the top left corner.
     *
     * @return The current bounds [x, y, width, height]
     */
    public Rectangle getRealBounds() {
        return screenBounds;
    }

    /**
     * Tell this overlay that it's position in window coordinates has been
     * changed to this new value. The position is always that of the top-left
     * corner of the bounding box in screen coordinate space.
     *
     * @param x The x location of the window in pixels
     * @param y The y location of the window in pixels
     */
    public void setLocation(int x, int y) {
        screenLocation[0] = x;
        screenLocation[1] = y;

        screenBounds.x = x;
        screenBounds.y = y;
    }

    /**
     * Set the layout listener for this node. Setting a value of null clears
     * the current listener instance.
     *
     * @param l The new listener to use or null
     */
    public void setLayoutListener(VRMLSurfaceLayoutListener l) {
        listener = l;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.SurfaceChildNodeType;
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

            case FIELD_BBOX_SIZE:
                fieldData.clear();
                fieldData.floatArrayValue = vfBboxSize;
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
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(vfBboxSize[0] != -1)
            screenBounds.width = (int)vfBboxSize[0];

        if(vfBboxSize[1] != -1)
            screenBounds.height = (int)vfBboxSize[1];
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
     * Set the value of the field at the given index as a boolean.
     * This would be used to set SFVec3f field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_BBOX_SIZE:
                setBboxSize(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }


    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Send an event to the listener with the new size information.
     *
     * @param width The new width to use
     * @param height The new height to use
     */
    protected void fireSizeChange(int width, int height) {
        if(listener == null)
            return;

        try {
            listener.surfaceResized(width, height);
        } catch(Exception e) {
            System.out.println("Error on surface resize " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Convenience method to check on the raw value of the bbox to make sure
     * it is OK.
     *
     * @param bbox The bounding box to check
     * @throws InvalidFieldValueException Value out of spec
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    private void setBboxSize(float[] bbox)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        if(!inSetup)
            throw new InvalidFieldAccessException("bboxSize cannot be " +
                                                  "changed at runtime");

        FieldValidator.checkBBoxSize2D(getVRMLNodeName(),bbox);

        vfBboxSize[0] = bbox[0];
        vfBboxSize[1] = bbox[1];
    }
}
