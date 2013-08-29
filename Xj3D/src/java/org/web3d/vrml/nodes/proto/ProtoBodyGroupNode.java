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
package org.web3d.vrml.nodes.proto;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

/**
 * A private group node implementation that represents the body of a proto
 * declaration.
 * <p>
 *
 * This, like a normal group has only one field named "children". We ignore
 * all bounds information and return default values. Values should never,
 * ever be routed into this group so we completely ignore the set value
 * for everything except the two node methods.
 *
 * @author Justin Couch
 * @version $Revision: 1.21 $
 */
class ProtoBodyGroupNode implements VRMLGroupingNodeType {

    /** Index of the children field */
    private static final int FIELD_CHILDREN = 0;

    /** The index of the last field in this node */
    private static final int LAST_BODY_INDEX = FIELD_CHILDREN;

    /** The number of fields in this node */
    private static final int NUM_FIELDS = LAST_BODY_INDEX + 1;

    /** Constant used to generate arrays from ArrayList */
    private static final VRMLNodeType[] EMPTY_ARRAY = new VRMLNodeType[0];

    /** The stock name for this node */
    private static final String NODE_NAME = "ProtoBodyGroupNode";

    /** Message when the user tries to set/get UserData */
    private static final String USER_DATA_MSG = "Invalid use of UserData";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Reporter instance for handing out errors */
    protected ErrorReporter errorReporter;

    /** The list of children to use */
    private ArrayList children;

    /** Flag indicating if this node contains bindable/activatable nodes */
    private boolean hasBindables;

    /** Counter for the number of sharing references this has */
    private int shareCount;

    /** Is this node still being setup/parsed. Cleared by setupFinished */
    private boolean inSetup;

    /** Flag indicating this is a DEF node */
    private boolean isDEF;

    /**
     * Static constructor to build field declarations
     */
    static {
        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);
        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFNode",
                                     "children");

        fieldMap.put("children",new Integer(FIELD_CHILDREN));
    }

    /**
     * Construct an instance of this node.
     */
    ProtoBodyGroupNode() {

        isDEF = false;
        inSetup = true;

        hasBindables = false;
        shareCount = 0;

        children = new ArrayList();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLGroupingNodeType
    //----------------------------------------------------------

    /**
     * Get the children, provides a live reference not a copy
     *
     * @return An array of VRMLNodeTypes
     */
   public VRMLNodeType[] getChildren() {
       return (VRMLNodeType[])children.toArray(EMPTY_ARRAY);
    }

    /**
     * Accesor method to set the children field
     * If passed null this method will act like removeChildren
     *
     * @param newChildren Array of new children
     */
    public void setChildren(VRMLNodeType[] newChildren) {
        children.clear();

        for(int i = newChildren.length - 1; i >= 0; i--)
            children.add(newChildren[i]);

    }

    /**
     * Accessor method to set the children field
     * Creates an array containing only newChild
     * If passed null this method will act like removeChildren
     *
     * @param newChild The new child
     */
    public void setChildren(VRMLNodeType newChild) {
        children.clear();
        children.add(newChild);
    }

    /**
     * Append a new child node to the existing collection. Should be used
     * sparingly. It is really only provided for Proto handling purposes.
     *
     * @param newChild The new child
     */
    public void addChild(VRMLNodeType newChild) {
        children.add(newChild);
    }

    /**
     * Returns the number of children
     *
     * @return The number of children
     */
    public int getChildrenSize() {
        return children.size();
    }

    /**
     * Delete all children
     */
    public void deleteChildren() {
        children.clear();
    }

    /**
     * A check to see if this grouping node contains any bindable nodes. This
     * does a dynamic check of all the children <i>now</i> to see if any of
     * them are bindable.
     *
     * @return true if this or any of its children contain bindable nodes
     */
    public boolean containsBindableNodes() {
System.out.println("Proto body contains bindables not implemented yet");
        return hasBindables;
    }

    /**
     * Check to see if this node has been used more than once. If it has then
     * return true.
     *
     * @return true if this node is shared
     */
    public boolean isShared() {
        return (shareCount > 1);
    }

    /**
     * Adjust the sharing count up or down one increment depending on the flag.
     *
     * @param used true if this is about to have another reference added
     */
    public void setShared(boolean used) {

        if(used)
            shareCount++ ;
        else
            shareCount--;

        Object kid;
        int num_kids = children.size();

        for(int i = 0; i < num_kids; i++) {
            kid = children.get(i);

            if(kid instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)kid).setShared(used);
        }
    }

    /**
     * Accessor method to get current value of field vfBboxCenter.
     * Default value is <code>0 0 0</code>.
     *
     * @return Value of vfBboxCenter(SFVec3f)
     */
    public float[] getBboxCenter() {
        return FieldConstants.EMPTY_SFVEC3F;
    }

    /**
     * Accessor method to get current value of field vfBboxSize.
     * Default value is <code>-1 -1 -1</code>.
     *
     * @return The size of the bounding box(SFVec3f)
     */
    public float[] getBboxSize() {
        return FieldConstants.EMPTY_SFVEC3F;
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
        return NODE_NAME;
    }

    /**
     * Set the version of VRML that this node should represent. Different
     * versions have different capabilities, even within the same node.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param isStatic true if this node is under a static group and won't
     *    change after the setup is finished
     */
    public void setVersion(int major, int minor, boolean isStatic) {
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        int ret_val = -1;

        if(children.size() != 0) {
            VRMLNodeType first = (VRMLNodeType)children.get(0);
            ret_val = first.getPrimaryType();
        }

        return ret_val;
    }

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        int[] ret_val = TypeConstants.NO_SECONDARY_TYPE;

        if(children.size() != 0) {
            VRMLNodeType first = (VRMLNodeType)children.get(0);
            ret_val = first.getSecondaryType();
        }

        return ret_val;
    }

    /**
     * Ignored by this implementation.
     *
     * @param mgr The manager instance to use
     */
    public void setFrameStateManager(FrameStateManager mgr) {
    }

    /**
     * Notify a node that an ExternProto has resolved.  This will verify the objects
     * type and add it to the render sceneGraph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
    public void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        System.out.println("ProtoBodyGroupNode notifyExtern ProtoLoaded not implemented.");
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
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Set the body to be DEF'd. Never used for a proto body
     *
     * @throws IllegalStateException The setup is finished.
     */
    public void setDEF() {
    }

    /**
     * Check to see if this node has been DEFd. Returns true if it has and
     * the user should ask for the shared representation rather than the
     * normal one.
     *
     * @return true if this node has been DEFd
     */
    public boolean isDEF() {
        return isDEF;
    }

    /**
     * Ask for the current number of references to this object. Always returns
     * zero for the proxy.
     *
     * @param layer The id of the layer to modify the ref count on
     * @return 0
     */
    public int getRefCount(int layer) {
        // ignored as ref counting not needed on imports.
        return 0;
    }

    /**
     * Change the reference count up or down by one. Ignored for proto body
     * nodes.
     *
     * @param add true to increment the reference count, false to decrement
     * @param layer The id of the layer to modify the ref count on
     */
    public void updateRefCount(int layer, boolean add) {
        // ignored as ref counting not needed on imports.
    }

    /**
     * Get a listing of the current layer IDs that are directly or indirectly
     * referencing this node. Always returns null for proxies.
     *
     * @return null
     */
    public int[] getLayerIds() {
        return null;
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
        return null;
    }


    /**
     * Clear the current removed layer ID list. If there is nothing removed,
     * this method does nothing.
     */
    public void clearRemovedLayerIds() {
    }

    /**
     * Set the X3DMetadataObject that is associated with this node. Ignored
     * because proto body nodes don't take metadata.
     *
     * @param data The node to register as the metadata
     * @throws InvalidFieldValueException The object instance provided
     *     does not implment VRMLMetadataNodeType or is not a proto instance
     *     that encapsulates it as it's primary type
     */
    public void setMetadataObject(VRMLNodeType data)
        throws InvalidFieldValueException {
    }

    /**
     * Get the currently registered metadata object instance. If none is set
     * then return null. Ignored because because proto body nodes don't take
     * metadata.
     *
     * @return The current metadata object or null
     */
    public VRMLNodeType getMetadataObject() {
        return null;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        inSetup = false;

        int size = children.size();
        for(int i = 0; i < size; i++) {
            VRMLNodeType node = (VRMLNodeType)children.get(i);
            node.setupFinished();
        }
    }

    /**
     * Check to see if setupFinished() has already been called on this node.
     *
     * @return true if setupFinished() has been called
     */
    public boolean isSetupFinished() {
        return !inSetup;
    }

    /**
     * Set arbitrary data for a given field. Provided primarily to help the
     * EAI fullfil its requirements, but may be useful elsewhere.
     *
     * @param index The index of destination field to set
     * @param data The item to store for the field
     * @throws InvalidFieldException The field index is not known
     */
    public void setUserData(int index, Object data)
        throws InvalidFieldException {

        throw new InvalidFieldException(USER_DATA_MSG);
    }

    /**
     * Fetch the stored user data for a given field index. If nothing is
     * registered, null is returned.
     *
     * @param index The index of destination field to set
     * @return The item stored for the field or null
     * @throws InvalidFieldException The field index is not known
     */
    public Object getUserData(int index) throws InvalidFieldException {
        throw new InvalidFieldException(USER_DATA_MSG);
    }

    /**
     * Ignored by this implementation.
     *
     * @param l The listener instance to add
     */
    public void addNodeListener(VRMLNodeListener l) {
    }

    /**
     * Ignored by this implementation.
     *
     * @param l The listener to be removed
     */
    public void removeNodeListener(VRMLNodeListener l) {
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
        throw new InvalidFieldException("Invalid Index: " + index + " " + this);
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
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_BODY_INDEX)
            return null;

        return fieldDecl[index];
    }

    /**
     * Get the number of fields defined for this node.
     *
     * @return The number of fields.
     */
    public int getNumFields() {
        return fieldDecl.length;
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Empty for the proto body group node.
     *
     * @return null
     */
    public int[] getNodeFieldIndices() {
        return null;
    }

    /**
     * Check to see if the given field has changed since we last checked.
     * As this is the body group node, it should never change the field values.
     * Always returns false.
     *
     * @param index The index of the field to change.
     * @return true if the field has changed since last read
     */
    public boolean hasFieldChanged(int index) {
        return false;
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
    }

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
    }

    /**
     * Set the value of the field at the given index as a boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {
    }

    /**
     * Set the value of the field at the given index as an array of booleans.
     * This would be used to set MFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, boolean[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
    }

    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, long value)
        throws InvalidFieldException, InvalidFieldValueException {
    }

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, long[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
    }

    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException {
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
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

        if(!inSetup)
            throw new InvalidFieldValueException("Attempting to change the proto " +
                                                 "body after construction.");

        switch(index) {
            case FIELD_CHILDREN:
                if(child != null)
                    children.add(child);

                break;

            default : throw new InvalidFieldException(this +
                " setValue(VRMLNodeType): Invalid index: " + index);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Clear the child node list of all children - both VRML and Java3D.
     */
    private void clearChildren() {
        Object kid;
        int num_kids = children.size();

        for(int i = 0; i < num_kids; i++) {
            kid = children.get(i);

            if(kid instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)kid).setShared(false);
        }

        children.clear();
        hasBindables = false;
    }
}
