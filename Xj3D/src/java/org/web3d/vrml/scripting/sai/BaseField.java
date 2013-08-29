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

package org.web3d.vrml.scripting.sai;

// Standard imports
import java.util.LinkedList;

// Application specific imports
import org.web3d.x3d.sai.*;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLNodeListener;

/**
 * Base implementation of an X3D field type.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
abstract class BaseField implements X3DField, VRMLNodeListener {

    /**
     * Error message for when the node is set to read-only status. This
     * should be used when the containing script node is set directOuput
     * to false.
     */
    protected static final String NO_DIRECT_OUT_ERR =
        "You cannot write to this field. It is not permitted when the " +
        "directOutput field is set to false.";

    /**
     * Error message when an operation occurs at an invalid time in the
     * event model. See 19775 Part 2, 4.8.3.7 User code lifecycle for
     * more information.
     */
    protected static final String INVALID_TIMING_ERR =
        "You are attempting to access a field when not permitted by the " +
        "specification. Please see 19775 Part 2, 4.8.3.7 User code lifecycle " +
        "for more information";

    /**
     * Error message for the field not being writable at this point in time.
     * Used for normal conditions such as attempting to write to an output-only
     * field etc.
     */
    protected static final String NOT_WRITABLE_ERR =
        "This field is not writable in the current state";

    /**
     * Error message for the field not being readable at this point in time.
     * Used for normal conditions such as attempting to read an input-only
     * field etc.
     */
    protected static final String NOT_READABLE_ERR =
        "This field is not readable in the current state";


    /** Cache for the event instances */
    private static LinkedList eventList;

    /** The node this field belongs to */
    protected VRMLNodeType node;

    /** The index of the field this instance represents */
    protected int fieldIndex;

    /** Field defintion for the node */
    private X3DFieldDefinition fieldDef;

    /** Listener registered for this field, if at all */
    private X3DFieldEventListener listener;

    /** Listener for dealing with the script wrapper for field access */
    protected FieldAccessListener fieldAccessListener;

    /**
     * Flag describing if this field is internal or external for the
     * purposes of readability.
     */
    private boolean internal;

    /** Flag to say whether the data has changed since last call */
    protected boolean dataChanged;

    /** Flag for determining the read/write timing ability */
    protected boolean accessPermitted;

    /** Flag to say this field is read only (directOutput == true) */
    protected boolean readOnly;

    /**
     * Static constructor to initialise the event caching
     */
    static {
        eventList = new LinkedList();
    }

    /**
     * Create a new instance of the base class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    BaseField(VRMLNodeType n, int field, boolean internal) {
        node = n;
        fieldIndex = field;
        this.internal = internal;

        dataChanged = false;
        accessPermitted = false;
        readOnly = false;
    }

    //----------------------------------------------------------
    // Methods defined by X3DField
    //----------------------------------------------------------

    /**
     * Get the definition of this field.
     *
     * @return The field definition to use
     */
    public X3DFieldDefinition getDefinition() {
        if(fieldDef == null) {
            VRMLFieldDeclaration decl = node.getFieldDeclaration(fieldIndex);
            fieldDef = new SAIFieldDefinition(decl.getName(),
                                              decl.getAccessType(),
                                              decl.getFieldType());
        }

        return fieldDef;
    }

    /**
     * Check to see if this field is readable. This may return two different
     * sets of values depending on the use. If this field is the field of a
     * script that has been passed to a script implementation, it will return
     * true if the field is an eventIn, exposedField or field and false for an
     * eventOut. If it is a field of any other circumstance (ie an external
     * application querying a node or a script querying another node it has a
     * reference to) it shall return true for eventOuts, exposedFields and
     * false for eventIn or field.
     *
     * @return true if the values of this field are readable
     * @throws InvalidFieldException The underlying node this field came from
     *    has been disposed of
     */
    public boolean isReadable() {
        boolean ret_val;

        VRMLFieldDeclaration decl = node.getFieldDeclaration(fieldIndex);
        int access = decl.getAccessType();

        if(internal)
            ret_val = (access == FieldConstants.EVENTIN) ||
                      (access == FieldConstants.FIELD) ||
                      (access == FieldConstants.EXPOSEDFIELD);
        else
            ret_val = (access == FieldConstants.EVENTOUT) ||
                      (access == FieldConstants.EXPOSEDFIELD);

        return ret_val;
    }

    /**
     * Check to see if this field is writable. This may return two different
     * sets of values depending on the use. If this field is the field of a
     * script that has been passed to a script implementation, it will return
     * true if the field is an eventOut, exposedField or field and false for an
     * eventIn. If it is a field of any other circumstance (ie an external
     * application querying a node or a script querying another node it has a
     * reference to) it shall return true for eventIns, exposedFields and
     * false for eventOut or field.
     *
     * @return true if the values of this field are readable
     * @throws InvalidFieldException The underlying node this field came from
     *    has been disposed of
     */
    public boolean isWritable() {
        boolean ret_val;

        VRMLFieldDeclaration decl = node.getFieldDeclaration(fieldIndex);
        int access = decl.getAccessType();

        if(internal)
            ret_val = (access == FieldConstants.EVENTOUT) ||
                      (access == FieldConstants.EXPOSEDFIELD) ||
                      (access == FieldConstants.FIELD);
        else if(node.isSetupFinished())
            ret_val = (access == FieldConstants.EVENTIN) ||
                      (access == FieldConstants.EXPOSEDFIELD);
        else
            ret_val = (access == FieldConstants.EXPOSEDFIELD) ||
                      (access == FieldConstants.FIELD);

        return ret_val;
    }

    /**
     * Add a listener for changes in this field. This works for listening to
     * changes in a readable field. A future extension to the specification,
     * or a browser-specific extension, may allow for listeners to be added
     * to writable nodes as well.
     * <p>
     * A listener instance cannot have multiple simulatenous registerations.
     * If the listener instance is currently registered, this request shall
     * be silently ignored.
     *
     * @param l The listener to add
     */
    public void addX3DEventListener(X3DFieldEventListener l) {
        if(isReadable()) {
            boolean empty_listener = (listener == null);
            listener = FieldListenerMulticaster.add(l, listener);

            if(empty_listener && !internal)
                node.addNodeListener(this);
        }
    }

    /**
     * Remove a listener for changes in the readable field. If the listener is
     * not currently registered, this request shall be silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeX3DEventListener(X3DFieldEventListener l) {
        if(isReadable()) {
            listener = FieldListenerMulticaster.remove(l, listener);

            if((listener == null) && !internal)
                node.removeNodeListener(this);
        }
    }

    /**
     * Associate user data with this field. Whenever an field is generated
     * on this field, this data will be available with the Event through
     * its getData method.
     *
     * @param data The data to associate with this eventOut instance
     */
    public void setUserData(Object data) {
        node.setUserData(fieldIndex, data);
    }

    /**
     * Get the user data that is associated with this field.
     *
     * @return The user data, if any, associated with this field
     */
    public Object getUserData() {
        return node.getUserData(fieldIndex);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeListener
    //----------------------------------------------------------

    /**
     * Notification that the field represented by the given index has changed.
     *
     * @param index The index of the field that has changed
     */
    public void fieldChanged(int index) {
        if(index == fieldIndex) {
            SAIFieldEvent evt = getEventInstance(this);
            evt.update(this,
                       (System.currentTimeMillis() * 0.001),
                       node.getUserData(fieldIndex));

            fieldAccessListener.childRequiresAccessStateChange(true);
            listener.readableFieldChanged(evt);
            fieldAccessListener.childRequiresAccessStateChange(false);
            releaseEvent(evt);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Notification to the field instance to update the value in the
     * underlying node now.
     */
    abstract void updateNode();

    /**
     * Notification to the field to update its field values from the
     * underlying node. If this is a SF/MFNode field, it should chain
     * the calls to the child node(s) that the field represents.
     */
    abstract void updateField();

    /**
     * Set the listener for dealing with field access callbacks.
     *
     * @param fal The access listener instance to use
     */
    void setFieldAccessListener(FieldAccessListener fal) {
        fieldAccessListener = fal;
    }

    /**
     * Set this field to be read only. Once set, cannot be turned off.
     */
    void setReadOnly() {
        readOnly = true;
    }

    /**
     * Control whether operations are valid on this field instance right
     * now.
     *
     * @param valid True if access operations are now permitted.
     */
    void setAccessValid(boolean valid) {
        accessPermitted = valid;
    }

    /**
     * Send an event notification now. Convenience method used for when the
     * classes represent script fields and need to send notifications that
     * something has changed externally.
     *
     * @param timestamp The time at which this even occurred
     */
    void fireEventNotification(double timestamp) {

        if(listener == null)
            return;

        SAIFieldEvent evt = getEventInstance(this);
        evt.update(this, timestamp, node.getUserData(fieldIndex));

        listener.readableFieldChanged(evt);

        releaseEvent(evt);
    }

    /**
     * Query this field object to see if it has changed since the last time
     * this method was called. In a single-threaded environment, calling this
     * method twice should return true and then false (assuming that data had
     * changed since the previous calls).
     *
     * @return true if the data has changed.
     */
    boolean hasChanged() {
        boolean ret_val = dataChanged;
        dataChanged = false;

        return ret_val;
    }

    /**
     * Convenience method that checks to see whether the field is valid for
     * access right now. Will through the appropriate exception for the
     * current situation if there is a problem.
     *
     * @param write true if this is a write operation, false for read
     */
    protected void checkAccess(boolean write)
        throws InvalidOperationTimingException,
               InvalidWritableFieldException,
               InvalidReadableFieldException {

        if(readOnly && write)
            throw new InvalidWritableFieldException(NO_DIRECT_OUT_ERR);

        if(!accessPermitted)
            throw new InvalidOperationTimingException(INVALID_TIMING_ERR);

        if(write) {
            if(!isWritable())
                throw new InvalidWritableFieldException(NOT_WRITABLE_ERR);
        } else {
            if(!isReadable())
                throw new InvalidReadableFieldException(NOT_READABLE_ERR);
        }
    }

    /**
     * Check the two field representations for equality. This is based
     * on the node and field index being.
     *
     * @param obj The object to compare to
     * @return true if the node reference and field index are the same
     */
    public boolean equals(Object obj) {
        if(!(obj instanceof BaseField))
            return false;

        BaseField f = (BaseField)obj;

        return (f.node == node) && (f.fieldIndex == fieldIndex);
    }

    /**
     * Caching to fetch a pre-built instance of the field event.
     *
     * @return An unused event instance
     */
    private static synchronized SAIFieldEvent getEventInstance(Object src) {

        SAIFieldEvent ret_val = null;

        if(eventList.size() != 0) {
            synchronized(eventList) {
                ret_val = (SAIFieldEvent)eventList.removeFirst();
            }
        } else {
            ret_val = new SAIFieldEvent(src);
        }

        return ret_val;
    }

    /**
     * Return a used instance of the event back to the queue
     */
    private static void releaseEvent(SAIFieldEvent evt) {
        synchronized(eventList) {
            eventList.add(evt);
        }
    }
}
