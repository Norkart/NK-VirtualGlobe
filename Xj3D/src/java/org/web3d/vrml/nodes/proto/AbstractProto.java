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
 * Base representation of the common functionality for the PROTO and
 * EXTERNPROTO node types.
 * <p>
 *
 * <b>Note</b>: This implementation does not handle IS values to pass field
 * information to body nodes.
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public abstract class AbstractProto implements VRMLNodeTemplate {

    /**
     * A standard message for when the supplied node is wrong. Just add the
     * node name of the wrong type to the end.
     */
    protected static final String BAD_NODE_MSG =
        "The supplied node cannot be copied as it's type is wrong. The type " +
        "supplied is ";

    /** Message when the field type is not valid in VRML97 */
    protected static final String VRML97_FIELD_MSG =
        "Field type not supported in VRML97: ";

    /** When the same field is defined twice, but with different types */
    protected static final String FIELD_CLASH_MSG = "The same field has been " +
        "declared twice in this node, but the data types or access types are " +
        "different.";

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

    /** Reporter instance for handing out errors */
    protected ErrorReporter errorReporter;

    /** The count of the last added field index */
    private int fieldCount;

    /** The current listener(s) registered */
    private VRMLNodeListener nodeListener;

    /** The major version of the spec this instance belongs to. */
    protected final int vrmlMajorVersion;

    /** The minor version of the spec this instance belongs to. */
    protected final int vrmlMinorVersion;

    /** Flag indicating VRML97 semantics */
    protected boolean isVrml97;

    /**
     * The proto creator that is responsible for creating new instances of
     * this node. This will be, by necessity a renderer-specific class based on
     * this abstract interface.
     */
    protected NodeTemplateToInstanceCreator protoCreator;

    /**
     * Create a new instance of a proto that has the given name.
     *
     * @param name The name of the proto to use
     * @param majorVersion The major version number of this scene
     * @param minorVersion The minor version number of this scene
     * @param creator The node creator for generating instances of ourself
     */
    public AbstractProto(String name,
                         int majorVersion,
                         int minorVersion,
                         NodeTemplateToInstanceCreator creator) {
        nodeName = name;
        protoCreator = creator;
        vrmlMajorVersion = majorVersion;
        vrmlMinorVersion = minorVersion;

        isVrml97 = majorVersion == 2;

        fieldIndexMap = new HashMap();
        fieldDeclList = new ArrayList();
        fieldLocalData = new VRMLFieldDataThreaded();
        fieldCount = 0;

        // Automatically create a field that represents the Metadata node.
        VRMLFieldDeclaration decl =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        appendField(decl);
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeTemplate
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

    //----------------------------------------------------------
    // Common methods but not actually from an interface
    //----------------------------------------------------------

    /**
     * Add a listener to this node instance. If the listener is already added
     * or null the request is silently ignored.
     *
     * @param l The listener instance to add
     */
    public void addNodeListener(VRMLNodeListener l) {
        nodeListener = NodeListenerMulticaster.remove(nodeListener, l);
    }

    /**
     * Remove a listener from this node instance. If the listener is null or
     * not registered, the request is silently ignored.
     *
     * @param l The listener to be removed
     */
    public void removeNodeListener(VRMLNodeListener l) {
        nodeListener = NodeListenerMulticaster.add(nodeListener, l);
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
     * Get the name of this node as a string.
     *
     * @return The name of the node
     */
    public String getVRMLNodeName() {
        return nodeName;
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
     * @throws InvalidFieldException The field type is not valid for this
     *   specification version
     */
    public int appendField(VRMLFieldDeclaration field)
        throws FieldExistsException, InvalidFieldException {

        String name = field.getName();

        // Sanity check to prevent VRML97 content from declaring bogus fields.
        if(isVrml97) {
            switch(field.getFieldType()) {
                case FieldConstants.SFDOUBLE:
                case FieldConstants.MFDOUBLE:
                case FieldConstants.SFLONG:
                case FieldConstants.MFLONG:
                case FieldConstants.MFBOOL:
                case FieldConstants.SFCOLORRGBA:
                case FieldConstants.MFCOLORRGBA:
                case FieldConstants.SFVEC3D:
                case FieldConstants.MFVEC3D:
                case FieldConstants.MFIMAGE:
                    throw new InvalidFieldException(VRML97_FIELD_MSG +
                                                    field.getFieldTypeString());
                default:
                    // Do nothing.
            }
        }

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

    /**
     * Check to see if the node is using VRML97 semantics
     *
     * @return true if this is a VRML97 node
     */
    public boolean isVRML97() {
        return isVrml97;
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
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        int ret_val = -1;

        Integer index = (Integer)fieldIndexMap.get(fieldName);

        if(index != null)
            ret_val = index.intValue();

        return ret_val;
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
        return (VRMLFieldDeclaration)fieldDeclList.get(index);
    }

    /**
     * Get the number of fields defined for this node.
     *
     * @return The number of fields.
     */
    public int getNumFields() {
        return fieldDeclList.size();
    }

}
