/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005
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

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;
import org.web3d.vrml.nodes.ViewpointListener;
import org.web3d.vrml.renderer.common.nodes.BaseBindableNode;

/**
 * Common implementation of an OrthoViewpoint node.
 * <p>
 *
 * VRML requires the use of a headlight from the NavigationInfo node.
 * For convenience, we provide a headlight here that binds with the same
 * transform as the view platform.
 * <p>
 *
 * Viewpoints cannot be shared using DEF/USE. They may be named as such for
 * Anchor purposes, but attempting to reuse them will cause an error. This
 * implementation does not provide any protection against USE of this node
 * and attempting to do so will result in throwing exceptions - most
 * probably in the grouping node that includes this node.
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */
public abstract class BaseOrthoViewpoint extends BaseBindableNode
    implements VRMLViewpointNodeType {

    /** Index of the fieldOfView field */
    protected static final int FIELD_FIELDOFVIEW = LAST_BINDABLE_INDEX + 1;

    /** Index of the jump field */
    protected static final int FIELD_JUMP = LAST_BINDABLE_INDEX + 2;

    /** Index of the orientation field */
    protected static final int FIELD_ORIENTATION = LAST_BINDABLE_INDEX + 3;

    /** Index of the position field */
    protected static final int FIELD_POSITION = LAST_BINDABLE_INDEX + 4;

    /** Index of the description field */
    protected static final int FIELD_DESCRIPTION = LAST_BINDABLE_INDEX + 5;

    /** Index of the centerOfRotation field */
    protected static final int FIELD_CENTEROFROTATION =
        LAST_BINDABLE_INDEX + 6;

    /** Index of the retainUserOffsets field */
    protected static final int FIELD_RETAIN_USER_OFFSETS =
        LAST_BINDABLE_INDEX + 7;

    /** The last index of the nodes used by the viewpoint */
    protected static final int LAST_VIEWPOINT_INDEX = FIELD_RETAIN_USER_OFFSETS;


    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_VIEWPOINT_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** MFFloat fieldOfView -1,-1,1,1 */
    protected float[] vfFieldOfView;

    /** SFBool jump TRUE */
    protected boolean vfJump;

    /** SFBool retainUserOffsets FALSE */
    protected boolean vfRetainUserOffsets;

    /** SFRotation orientation 0 0 1 0 */
    protected float[] vfOrientation;

    /** SFVec3f position 0 0 10 */
    protected float[] vfPosition;

    /** SFVec3f centerOfRotation 0 0 0 */
    protected float[] vfCenterOfRotation;

    /** SFString description "" */
    protected String vfDescription;

    /** List of those who want to know about role changes, likely 1 */
    protected ArrayList viewpointListeners;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_BIND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFBool",
                                     "set_bind");
        fieldDecl[FIELD_BIND_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "bindTime");
        fieldDecl[FIELD_IS_BOUND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isBound");
        fieldDecl[FIELD_FIELDOFVIEW] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "fieldOfView");
        fieldDecl[FIELD_JUMP] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "jump");

        fieldDecl[FIELD_RETAIN_USER_OFFSETS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "retainUserOffsets");

        fieldDecl[FIELD_ORIENTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFRotation",
                                     "orientation");
        fieldDecl[FIELD_POSITION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "position");
        fieldDecl[FIELD_CENTEROFROTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "centerOfRotation");
        fieldDecl[FIELD_DESCRIPTION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "description");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_FIELDOFVIEW);
        fieldMap.put("fieldOfView", idx);
        fieldMap.put("set_fieldOfView", idx);
        fieldMap.put("fieldOfView_changed", idx);

        idx = new Integer(FIELD_JUMP);
        fieldMap.put("jump", idx);
        fieldMap.put("set_jump", idx);
        fieldMap.put("jump_changed", idx);

        idx = new Integer(FIELD_RETAIN_USER_OFFSETS);
        fieldMap.put("retainUserOffsets", idx);
        fieldMap.put("set_retainUserOffsets", idx);
        fieldMap.put("retainUserOffsets_changed", idx);

        idx = new Integer(FIELD_ORIENTATION);
        fieldMap.put("orientation", idx);
        fieldMap.put("set_orientation", idx);
        fieldMap.put("orientation_changed", idx);

        idx = new Integer(FIELD_POSITION);
        fieldMap.put("position", idx);
        fieldMap.put("set_position", idx);
        fieldMap.put("position_changed", idx);

        idx = new Integer(FIELD_CENTEROFROTATION);
        fieldMap.put("centerOfRotation", idx);
        fieldMap.put("set_centerOfRotation", idx);
        fieldMap.put("centerOfRotation_changed", idx);

        fieldMap.put("set_bind", new Integer(FIELD_BIND));
        fieldMap.put("bindTime", new Integer(FIELD_BIND_TIME));
        fieldMap.put("isBound", new Integer(FIELD_IS_BOUND));
        fieldMap.put("description", new Integer(FIELD_DESCRIPTION));
    }

    /**
     * Construct a default viewpoint instance
     */
    protected BaseOrthoViewpoint() {
        super("OrthoViewpoint");

        vfFieldOfView = new float[] { -1,-1,1,1 };
        vfJump = true;
        vfRetainUserOffsets = false;
        vfOrientation = new float[] { 0, 0, 1, 0 };
        vfPosition = new float[] { 0, 0, 10 };
        vfCenterOfRotation = new float[] { 0, 0, 0 };

        hasChanged = new boolean[NUM_FIELDS];

        viewpointListeners = new ArrayList(1);
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseOrthoViewpoint(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("fieldOfView");
            VRMLFieldData field = node.getFieldValue(index);

            vfFieldOfView[0] = field.floatArrayValue[0];
            vfFieldOfView[1] = field.floatArrayValue[1];
            vfFieldOfView[2] = field.floatArrayValue[2];
            vfFieldOfView[3] = field.floatArrayValue[3];

            index = node.getFieldIndex("jump");
            field = node.getFieldValue(index);
            vfJump = field.booleanValue;

            index = node.getFieldIndex("retainUserOffsets");
            field = node.getFieldValue(index);
            vfRetainUserOffsets = field.booleanValue;

            index = node.getFieldIndex("orientation");
            field = node.getFieldValue(index);
            vfOrientation[0] = field.floatArrayValue[0];
            vfOrientation[1] = field.floatArrayValue[1];
            vfOrientation[2] = field.floatArrayValue[2];
            vfOrientation[3] = field.floatArrayValue[3];

            index = node.getFieldIndex("position");
            field = node.getFieldValue(index);
            vfPosition[0] = field.floatArrayValue[0];
            vfPosition[1] = field.floatArrayValue[1];
            vfPosition[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("centerOfRotation");
            field = node.getFieldValue(index);
            vfCenterOfRotation[0] = field.floatArrayValue[0];
            vfCenterOfRotation[1] = field.floatArrayValue[1];
            vfCenterOfRotation[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("description");
            field = node.getFieldValue(index);
            vfDescription = field.stringValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLViewpointNodeType interface.
    //-------------------------------------------------------------

    /**
     * Add a ViewpointListener.
     *
     * @param l The listener.  Duplicates and nulls are ignored.
     */
    public void addViewpointListener(ViewpointListener l) {
        if(!viewpointListeners.contains(l))
            viewpointListeners.add(l);
    }

    /**
     * Remove a ViewpointListener.
     *
     * @param l The listener
     */
    public void removeViewpointListener(ViewpointListener l) {
        viewpointListeners.remove(l);
    }

    /**
     * Get the projection type.
     *
     * @return The type of projection.  One of the PROJECTION_ constants.
     */
    public int getProjectionType() {
        return VRMLViewpointNodeType.PROJECTION_ORTHO;
    }

    /**
     * Get the field of view used by this viewpoint. The value returned
     * is an angle that is not less than zero and less than or equal to PI.
     *
     * @return The field of view used by this viewpoint
     */
    public float[] getFieldOfView() {
        return vfFieldOfView;
    }

    /**
     * Set the field of view for this viewpoint. The value must be between
     * zero and pie or an exception will be thrown.
     *
     * @param fov The new field of view to use
     * @throws InvalidFieldValueException The field used is out of range
     */
    public void setFieldOfView(float[] fov) throws InvalidFieldValueException {

        //checkFieldOfView(fov);

        vfFieldOfView[0] = fov[0];
        vfFieldOfView[1] = fov[1];
        vfFieldOfView[2] = fov[2];
        vfFieldOfView[3] = fov[3];

        if(!inSetup) {
            hasChanged[FIELD_FIELDOFVIEW] = true;
            fireFieldChanged(FIELD_FIELDOFVIEW);

            fireFieldOfViewChanged(fov);
        }
    }

    /**
     * Get the Jump field value of this viewpoint.
     *
     * @return true if this viewpoint should jump to new positions
     */
    public boolean getJump() {
        return vfJump;
    }

    /**
     * Set the jump field value of this viewpoint to the new value
     *
     * @param jump True if the viewpoint should jump to ne positions
     */
    public void setJump(boolean jump) {
        vfJump = jump;

        if(!inSetup) {
            hasChanged[FIELD_JUMP] = true;
            fireFieldChanged(FIELD_JUMP);
        }
    }

    /**
     * Get the retainUserOffsets field value of this viewpoint.
     *
     * @return true if this viewpoint should retainUserOffsets on a bind
     */
    public boolean getRetainUserOffsets() {
        return vfRetainUserOffsets;
    }

    /**
     * Set the retainUserOffsets field value of this viewpoint to the new value
     *
     * @param retainUserOffsets True if the viewpoint should retainUserOffsets on a bind
     */
    public void setRetainUserOffsets(boolean retainUserOffsets) {
        vfRetainUserOffsets = retainUserOffsets;

        if(!inSetup) {
            hasChanged[FIELD_RETAIN_USER_OFFSETS] = true;
            fireFieldChanged(FIELD_RETAIN_USER_OFFSETS);
        }
    }

    /**
     * Get the description string associated with this viewpoint. If no
     * description is set, this will return null.
     *
     * @return The description string of this viewpoint
     */
    public String getDescription() {
        return vfDescription;
    }

    /**
     * Set the description string of this viewpoint. A zero length string or
     * null will clear the currently set description.
     *
     * @param desc The new description to use
     */
    public void setDescription(String desc) {
        vfDescription = desc;

        if(!inSetup) {
            hasChanged[FIELD_DESCRIPTION] = true;
            fireFieldChanged(FIELD_DESCRIPTION);
        }
    }

    /**
     * Get the center of rotation defined by this viewpoint. The center of
     * rotation is a point in space relative to the coordinate systems of
     * this node.
     *
     * @return The position of the center of rotation
     */
    public float[] getCenterOfRotation() {
        return vfCenterOfRotation;
    }

    /**
     * Set the center of rotation of this viewpoint. The center is a position
     * in 3-space.
     *
     * @param pos The new position to use
     * @throws InvalidFieldValueException The field used is not 3 values
     */
    public void setCenterOfRotation(float[] pos)
        throws InvalidFieldValueException {

        vfCenterOfRotation[0] = pos[0];
        vfCenterOfRotation[1] = pos[1];
        vfCenterOfRotation[2] = pos[2];

        if(!inSetup) {
            hasChanged[FIELD_CENTEROFROTATION] = true;
            fireFieldChanged(FIELD_CENTEROFROTATION);

            fireCenterOfRotationChanged(pos);
        }
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

        int ret_val = (index == null) ? -1 : index.intValue();
        // retainUserOffsets was added in 3.2. Change
        // the field index to say that they don't exist for VRML or
        // X3D 3.0 and 3.1
        if((ret_val == FIELD_RETAIN_USER_OFFSETS)
            &&
           ((vrmlMajorVersion == 2) ||
            ((vrmlMajorVersion == 3) && (vrmlMinorVersion < 2))))
            ret_val = -1;

        return ret_val;
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
        if(index < 0  || index > LAST_VIEWPOINT_INDEX)
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
        return TypeConstants.ViewpointNodeType;
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
            case FIELD_DESCRIPTION:
                fieldData.clear();
                fieldData.stringValue = vfDescription;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                fieldData.numElements = 1;
                break;
            case FIELD_FIELDOFVIEW:
                fieldData.clear();
                fieldData.floatArrayValue = vfFieldOfView;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 4;
                break;
            case FIELD_JUMP:
                fieldData.clear();
                fieldData.booleanValue = vfJump;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.numElements = 1;
                break;
            case FIELD_RETAIN_USER_OFFSETS:
                fieldData.clear();
                fieldData.booleanValue = vfRetainUserOffsets;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.numElements = 1;
                break;
            case FIELD_ORIENTATION:
                fieldData.clear();
                fieldData.floatArrayValue = vfOrientation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;
            case FIELD_POSITION:
                fieldData.clear();
                fieldData.floatArrayValue = vfPosition;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;
            case FIELD_CENTEROFROTATION:
                fieldData.clear();
                fieldData.floatArrayValue = vfCenterOfRotation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
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
                case FIELD_FIELDOFVIEW:
                    destNode.setValue(destIndex, vfFieldOfView, 4);
                    break;
                case FIELD_JUMP:
                    destNode.setValue(destIndex, vfJump);
                    break;
                case FIELD_RETAIN_USER_OFFSETS:
                    destNode.setValue(destIndex, vfRetainUserOffsets);
                    break;
                case FIELD_ORIENTATION:
                    destNode.setValue(destIndex, vfOrientation, 4);
                    break;
                case FIELD_POSITION:
                    destNode.setValue(destIndex, vfPosition, 3);
                    break;
                case FIELD_DESCRIPTION:
                    destNode.setValue(destIndex, vfDescription);
                    break;
                case FIELD_CENTEROFROTATION:
                    destNode.setValue(destIndex, vfCenterOfRotation, 3);
                    break;
                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseOrthoViewpoint.sendRoute: No field!" + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not relate to one of our
     *    fields
     * @throws InvalidFieldValueException The value does not contain an
     *    in range value or bad numerical type
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch (index) {
            case FIELD_JUMP:
                setJump(value);
                break;
            case FIELD_RETAIN_USER_OFFSETS:
                setRetainUserOffsets(value);
                break;

            default :
                super.setValue(index, value);
        }

    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not relate to one of our
     *    fields
     * @throws InvalidFieldValueException The value does not contain an
     *    in range value or bad numerical type
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_FIELDOFVIEW:
                setFieldOfView(value);
                break;

            case FIELD_ORIENTATION:
                setOrientation(value);
                break;

            case FIELD_POSITION:
                setPosition(value);
                break;

            case FIELD_CENTEROFROTATION:
                setCenterOfRotation(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_DESCRIPTION:
                vfDescription = value;
                if(!inSetup) {
                    hasChanged[FIELD_DESCRIPTION] = true;
                    fireFieldChanged(FIELD_DESCRIPTION);
                }
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Public access methods
    //----------------------------------------------------------

    /**
     * Convenience method to set the position of the viewpoint. Designed to
     * be overridden by derived classes. Make sure you also call this first
     * to set the field values.
     *
     * @param pos The position vector to use
     */
    protected void setPosition(float[] pos) {
        vfPosition[0] = pos[0];
        vfPosition[1] = pos[1];
        vfPosition[2] = pos[2];

        if(!inSetup) {
            hasChanged[FIELD_POSITION] = true;
            fireFieldChanged(FIELD_POSITION);
        }
    }

    /**
     * Convenience method to set the orientation of the viewpoint. Designed to
     * be overridden by derived classes. Make sure you also call this first
     * to set the field values.
     *
     * @param dir The orientation quaternion to use
     */
    protected void setOrientation(float[] dir) {
        vfOrientation[0] = dir[0];
        vfOrientation[1] = dir[1];
        vfOrientation[2] = dir[2];
        vfOrientation[3] = dir[3];

        if(!inSetup) {
            hasChanged[FIELD_ORIENTATION] = true;
            fireFieldChanged(FIELD_ORIENTATION);
        }
    }

    /**
     * Send a notification to the registered listeners the center of rotation has
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param val The new center of Rotation
     */
    protected void fireCenterOfRotationChanged(float[] val) {
        // Notify listeners of new value
        int num_listeners = viewpointListeners.size();
        ViewpointListener ul;

        for(int i = 0; i < num_listeners; i++) {
            ul = (ViewpointListener) viewpointListeners.get(i);
            ul.centerOfRotationChanged(val);
        }
    }

    /**
     * Send a notification to the registered listeners the field of view has
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param val The new center of Rotation
     */
    protected void fireFieldOfViewChanged(float[] val) {
        // Notify listeners of new value
        int num_listeners = viewpointListeners.size();
        ViewpointListener ul;

        for(int i = 0; i < num_listeners; i++) {
            ul = (ViewpointListener) viewpointListeners.get(i);
            ul.fieldOfViewChanged(val);
        }
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Check that the proposed fieldOfView is valid
     * FieldOfView must be (0,PI)
     *
     * @param newFov The new view to check on
     * @throws InvalidFieldValueException FoV out of range
     */
    private void checkFieldOfView(float newFov)
        throws InvalidFieldValueException {

        if (newFov <= 0.0f || newFov > (float) Math.PI) {
            throw new InvalidFieldValueException(
                "FieldOfView must be (0,PI)");
        }
    }
}
