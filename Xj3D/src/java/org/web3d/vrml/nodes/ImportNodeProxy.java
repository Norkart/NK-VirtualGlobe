/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.nodes;

// External imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.IntHashMap;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldAccessException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;

/**
 * A proxy node representation for allowing processing of X3D imports.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class ImportNodeProxy
    implements VRMLNodeType, VRMLNodeListener, Cloneable {

    /** The name returned to represent this "node type name" */
    private static final String IMPORT_NODE_NAME = "ImportProxy";

    /** The name of this node represented in the local file */
    private final String importedName;

    /** The DEF name of the inline this import refers to */
    private final String inlineName;

    /** The export name in the inline this is to use */
    private final String exportedName;

    /** Scratch class var for returning field data. Assigned at construction. */
    private final VRMLFieldData fieldData;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** hasChanged flags for fields, keyed by the local field index */
    private IntHashMap fieldChangedFlags;

    /** The major version of the spec this instance belongs to. */
    private int vrmlMajorVersion;

    /** The minor version of the spec this instance belongs to. */
    private int vrmlMinorVersion;

    /** State manager for propogating updates */
    private FrameStateManager stateManager;

    /** The current listener(s) registered */
    private VRMLNodeListener nodeListener;

    /** Keeping track of the fields that have be requested */
    private int lastFieldIndex;

    /** This is the real node instance that we're proxying for */
    private VRMLNodeType realNode;

    /** Map of called field names to our internally assigned index list */
    private HashMap nameToIndexMap;

    /** Map of our field index to the real node's field index */
    private IntHashMap indexToRealIndexMap;

    /** Reverse mapping of indexToRealIndexMap */
    private IntHashMap realIndexToIndexMap;

    /**
     * Local field declaration mapping. We build these based on the requested
     * field names, but have no idea of the type.
     */
    private IntHashMap fieldDeclMap;

    /**
     * Create a new instance of this node with the given node type name.
     * inSetup will be set to true and isDEF set to false.
     *
     * @param importedAs The name from the import statement
     * @param inlineDef The DEF name of the inline this import refers to
     * @param exportedAs The name the node was exported from the Inline as
     */
    public ImportNodeProxy(String importedAs,
                           String inlineDef,
                           String exportedAs) {

        importedName = importedAs;
        inlineName = inlineDef;
        exportedName = exportedAs;

        fieldData = new VRMLFieldData();

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        nameToIndexMap = new HashMap();
        fieldDeclMap = new IntHashMap();
        fieldChangedFlags = new IntHashMap();
        indexToRealIndexMap = new IntHashMap();
        realIndexToIndexMap = new IntHashMap();
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    /**
     * Ignored by this base implementation. Any class that needs this method
     * may override this method as required.
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
        return IMPORT_NODE_NAME;
    }

    /**
     * Check to see if this node has been DEFd. Returns true if it has and
     * the user should ask for the shared representation rather than the
     * normal one.
     *
     * @return true if this node has been DEFd
     */
    public boolean isDEF() {
        return false;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.UNRESOLVED_IMPORT_PROXY;
    }

    /**
     * Get the secondary types of this node.  Replaces the instanceof mechanism
     * for use in switch statements. If there are no secondary types, it will
     * return a zero-length array.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return TypeConstants.NO_SECONDARY_TYPE;
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
        vrmlMajorVersion = major;
        vrmlMinorVersion = minor;

        // isStatic ignored for proxies.
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
        VRMLFieldDeclaration ret_val;


        if(realNode == null)
            ret_val = (VRMLFieldDeclaration)fieldDeclMap.get(index);
        else {
            // When we have the real body, it is likely that an existing
            // route may have fetched a decl before this point. So, first
            // check the index mapping before fetching the real decl
            Integer real_index = (Integer)indexToRealIndexMap.get(index);
            ret_val = realNode.getFieldDeclaration(real_index.intValue());
        }

        return ret_val;
    }

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer)nameToIndexMap.get(fieldName);

        if(index == null) {
            if(realNode != null) {
                int other = realNode.getFieldIndex(fieldName);

                // Have we got a bogus field name from the real node?
                // If so, return immediately.
                if(other == -1)
                    return -1;

                indexToRealIndexMap.put(lastFieldIndex, new Integer(other));
                index = new Integer(lastFieldIndex++);

                realIndexToIndexMap.put(other, index);
            } else {
                // assign a new index;
                VRMLFieldDeclaration decl =
                    new VRMLFieldDeclaration(FieldConstants.UNKNOWN_IMPORT_ACCESS,
                                             VRMLFieldDeclaration.UNKNOWN_IMPORT,
                                             fieldName);
                fieldDeclMap.put(lastFieldIndex, decl);

                index = new Integer(lastFieldIndex++);
            }

            nameToIndexMap.put(fieldName, index);
            fieldChangedFlags.put(index.intValue(), Boolean.FALSE);
        }

        return (index == null) ? -1 : index.intValue();
    }

    /**
     * Get the number of fields.
     *
     * @return The number of fields.
     */
    public int getNumFields() {
        return lastFieldIndex;
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Empty for the default implementation. Any derived
     * class that has node children should override this method with real
     * values.
     *
     * @return null
     */
    public int[] getNodeFieldIndices() {
        // we don't know until we have the real node.

        return (realNode == null) ? null : realNode.getNodeFieldIndices();
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

        // Ignored. Imports cannot have user data directly applied.
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
        // Ignored. Imports cannot have user data directly applied.
        return null;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Set the X3DMetadataObject that is associated with this node.
     *
     * @param data The node to register as the metadata
     * @throws InvalidFieldValueException The object instance provided
     *     does not implment VRMLMetadataNodeType or is not a proto instance
     *     that encapsulates it as it's primary type
     */
    public void setMetadataObject(VRMLNodeType data)
        throws InvalidFieldValueException {

        // Ignored for the proxy. End user code should not be capable of
        // directly setting metadata on an import statement.
    }

    /**
     * Get the currently registered metadata object instance. If none is set
     * then return null.
     *
     * @return The current metadata object or null
     */
    public VRMLNodeType getMetadataObject() {
        // Ignored for the proxy. End user code should not be capable of
        // directly setting metadata on an import statement.

        return null;
    }

    /**
     * Check to see if setupFinished() has already been called on this node.
     *
     * @return true if setupFinished() has been called
     */
    public boolean isSetupFinished() {
        return true;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        // Ignored as proxies are always complete
    }

    /**
     * Notify this node that is has been DEFd. This method shall only be
     * called before setupFinished(). It is an error to call it any other
     * time. It is also guaranteed that this call will be made after
     * construction, but before any of the setValue() methods have been called.
     *
     * @throws IllegalStateException The setup is finished.
     */
    public void setDEF() {
        // Ignored because you cannot DEF an import
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
     * Change the reference count up or down by one. Ignored for node proxies.
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
     * Ignored by this implementation.
     *
     * @param mgr The manager instance to use
     */
    public void setFrameStateManager(FrameStateManager mgr) {
        stateManager = mgr;
    }

    /**
     * Check to see if the given field has changed since we last checked.
     * Calling this method will set the flag back to "not changed" so that two
     * consective reads after a changed value would result in a true and then
     * false being returned. If the field number is not recognized for this
     * node then this returns false.
     *
     * @param index The index of the field to change.
     * @return true if the field has changed since last read
     */
    public boolean hasFieldChanged(int index) {
        Boolean flag = (Boolean)fieldChangedFlags.get(index);
        fieldChangedFlags.put(index, Boolean.FALSE);
        return flag.booleanValue();
    }

    /**
     * Send a routed value from this node to the given destination node. Empty
     * implementation in the base class. Derived classes will need to override
     * this to make route management work.
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

        // Ignore anything until we have a real node instance
        if(realNode == null)
            return;

        Integer real_idx = (Integer)indexToRealIndexMap.get(srcIndex);

        // We should never have a case where this is null, hopefully!
        realNode.sendRoute(time, real_idx.intValue(), destNode, destIndex);
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
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        if(realNode != null) {
            Integer real_idx = (Integer)indexToRealIndexMap.get(index);

            if(real_idx == null)
                throw new InvalidFieldException("Unable to locate imported node's field");

            return realNode.getFieldValue(real_idx.intValue());
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
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

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of boolean.
     * This would be used to set MFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, boolean[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), value);
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
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, long value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, long[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
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

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), child);
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

        if(realNode != null) {
            Integer idx = (Integer)indexToRealIndexMap.get(index);
            realNode.setValue(idx.intValue(), children, numValid);
        }
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

        // Ignored for import proxies. Cannot be extern protos.
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeListener
    //----------------------------------------------------------

    /**
     * Notification that the field represented by the given index has changed.
     * Use this to pass into the root scene a notification that it's own output
     * has changed, as well as mark the local flag so that routing can happen.
     *
     * @param index The index of the field that changed.
     */
    public void fieldChanged(int index) {
        Integer local_idx = (Integer)realIndexToIndexMap.get(index);

        // If we haven't mapped it previously, just ignore the callback and
        // don't pass anything on.
        if(local_idx != null)
            fieldChangedFlags.put(local_idx.intValue(), Boolean.TRUE);
    }

    //----------------------------------------------------------
    // Methods defined by Cloneable
    //----------------------------------------------------------

    /**
     * Make a cloned copy of this class. Implementation makes something between
     * a shallow and deep copy, depending on the internal structure.
     *
     * @return A cloned instance of this instance
     * @throws CloneNotSupportedException Something lower couldn't clone
     */
    public Object clone() throws CloneNotSupportedException {
        ImportNodeProxy other = (ImportNodeProxy)super.clone();

        // realNode reference is not cloned, because it may come from an inline.
        // Reset it to null.
        other.realNode = null;
        other.nodeListener = null;

        // NOTES:
        // Wonder if the final VRMLFieldData instance will cause a problem with
        // multithreaded code? Clone will just replicate the reference to the
        // initial version, not create a new instance. This may end up with a
        // shared reference between the two, with obvious consequences in a
        // multithreaded access. However, since this is only used to return zero
        // entries when there is no realNode set, and is never set to anything,
        // hopefully this won't make any difference.

        other.fieldChangedFlags = new IntHashMap();

        // The standard shallow clones are fine for the following:
        other.nameToIndexMap = (HashMap)nameToIndexMap.clone();
        other.indexToRealIndexMap = (IntHashMap)indexToRealIndexMap.clone();
        other.realIndexToIndexMap = (IntHashMap)realIndexToIndexMap.clone();
        other.fieldDeclMap = (IntHashMap)fieldDeclMap.clone();

        return other;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the real node that this proxy is representing. At this time, do any
     * mapping from the fields to check for validity. If the parameter value is
     * null, clear the existing mappings. If a field has been requested due to
     * an external ROUTE that does not exist in the real node, an error message
     * is sent through the error reporter and that field is subsequently
     * ignored, but the rest of the mappings continue.
     *
     * @param node The node instance to use
     */
    public void setRealNode(VRMLNodeType node) {

        if(node == null) {
            if(realNode != null)
                realNode.removeNodeListener(this);
            indexToRealIndexMap.clear();
            realNode = node;

        } else {
            if(realNode != null)
                realNode.removeNodeListener(this);

            realNode = node;
            realNode.addNodeListener(this);

            Set field_names = nameToIndexMap.keySet();
            Iterator itr = field_names.iterator();

            while(itr.hasNext()) {
                String name = (String)itr.next();
                int idx = realNode.getFieldIndex(name);

                if(idx == -1) {
                    errorReporter.warningReport("Imported node " + importedName +
                                                " does not contain the " +
                                                "requested field name " + name,
                                                null);
                    continue;
                }

                Integer local_idx = (Integer)nameToIndexMap.get(name);

                indexToRealIndexMap.put(local_idx.intValue(),
                                        new Integer(idx));

                realIndexToIndexMap.put(idx, local_idx);
            }
        }
    }

    /**
     * Fetch the real node reference. If one is not set, this will return null.
     *
     * @return A reference to the real node or null
     */
    public VRMLNodeType getRealNode() {
        return realNode;
    }

    /**
     * Get the DEF name of the inline that this import works with.
     *
     * @return The DEF name string
     */
    public String getInlineDEFName() {
        return inlineName;
    }

    /**
     * Get the name that the node was exported as from the inline.
     *
     * @return The export name string
     */
    public String getExportedName() {
        return exportedName;
    }

    /**
     * Get the import name.
     *
     * @return The import name string
     */
    public String getImportedName() {
        return importedName;
    }

    /**
     * Send a notification to the registered listeners that a field has been
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param index The index of the field that changed
     */
    private void fireFieldChanged(int index) {
        if(nodeListener != null) {
            try {
                nodeListener.fieldChanged(index);
            } catch(Throwable th) {
                th.printStackTrace();
            }
        }
    }
}
