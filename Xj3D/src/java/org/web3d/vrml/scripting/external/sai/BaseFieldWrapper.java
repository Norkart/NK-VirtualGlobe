package org.web3d.vrml.scripting.external.sai;

/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import org.web3d.x3d.sai.*;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

/**
 * The base field wrapper for the SAI field wrappers.
 */
abstract class BaseFieldWrapper implements X3DField {

    /** Error message for reading from an inputOnly field */
    private static final String IMPROPER_READ_FROM_INPUTONLY_FIELD = "It is improper to read from an inputOnly field.";

    /** Error message for reading from an initializeOnly field after setup */
    private static final String IMPROPER_READ_FROM_INITIALIZEONLY_FIELD = "It is improper to read from an initializeOnly field on a realized node.";

    /** Error message for reading from an outputOnly field during setup */
    private static final String IMPROPER_READ_FROM_OUTPUTONLY_FIELD = "It is improper to read from an outputOnly field on a node still in setup.";

    /** Error message for writing to an outputOnly field */
    private static final String IMPROPER_WRITE_TO_OUTPUTONLY_FIELD = "It is improper to write to an outputOnly field.";

    /** Error message for writing to an initializeOnly field after setup */
    private static final String IMPROPER_WRITE_TO_INITIAlIZEONLY_FIELD = "It is improper to write to an initializeOnly field on a realized node.";

    /** Error message for writing to an inputOnly field during setup */
    private static final String IMPROPER_WRITE_TO_INPUTONLY_FIELD = "It is improper to write to an inputOnly field on a node still in setup.";

    /** Does the buffer contain a stored value to send to underlying node? */
    protected boolean storedInput;

    /** Does the buffer contain a stored value coming from underlying node? */
    protected boolean storedOutput;

    /** The queue to send events to */
    protected ExternalEventQueue theEventQueue;

    /** The underlying Xj3D node. */
    protected VRMLNodeType theNode;

    /** The event adapter for connecting to the event model */
    protected SAIEventAdapterFactory theEventAdapterFactory;

    /** The field index for this field on the underlying node */
    protected int fieldIndex;

    /** The field definition in case anyone needs it */
    protected SAIFieldDefinition fieldDefinition;

    protected BaseFieldWrapper(VRMLNodeType node,
        int field,
        ExternalEventQueue aQueue,
        SAIEventAdapterFactory factory
    ) {
        theNode=node;
        theEventAdapterFactory=factory;
        theEventQueue=aQueue;
        fieldIndex=field;
    }

    /**
     * Checks to ensure that the field is readable at the time
     * the call is made, and generate an intelligible error message
     * and exception if it isn't.
     */
    protected void checkReadAccess() {
        int accessType=getAccessType();
        if (theEventQueue.isNodeRealized(theNode))
            switch (accessType) {
                case FieldConstants.EVENTIN:
                    throw new InvalidReadableFieldException(IMPROPER_READ_FROM_INPUTONLY_FIELD);
                case FieldConstants.FIELD:
                    throw new InvalidReadableFieldException(IMPROPER_READ_FROM_INITIALIZEONLY_FIELD);
                default:
                    // Access is okay.
                    return;
            }
        else
            switch (accessType) {
                case FieldConstants.EVENTIN:
                    throw new InvalidReadableFieldException(IMPROPER_READ_FROM_INPUTONLY_FIELD);
                case FieldConstants.EVENTOUT:
                    throw new InvalidReadableFieldException(IMPROPER_READ_FROM_OUTPUTONLY_FIELD);
                default:
                    // Access is okay
                    return;
            }
    }

    /**
     * Checks to ensure that the field is writable at the time
     * the call is made, and generate an intelligible error message
     * and exception if it isn't.
     */
    protected void checkWriteAccess() {
        int accessType=getAccessType();
        if (theEventQueue.isNodeRealized(theNode))
            switch (accessType) {
                default:
                    // Access is okay.
                    return;
                case FieldConstants.FIELD:
                    throw new InvalidWritableFieldException(IMPROPER_WRITE_TO_INITIAlIZEONLY_FIELD);
                case FieldConstants.EVENTOUT:
                    throw new InvalidWritableFieldException(IMPROPER_WRITE_TO_OUTPUTONLY_FIELD);
            }
        else
            switch (accessType) {
                default:
                    // Access is okay;
                    return;
                case FieldConstants.EVENTIN:
                    throw new InvalidWritableFieldException(IMPROPER_WRITE_TO_INPUTONLY_FIELD);
                case FieldConstants.EVENTOUT:
                    throw new InvalidWritableFieldException(IMPROPER_WRITE_TO_OUTPUTONLY_FIELD);
            }
    }

    /**
     * Compute equality of field wrappers.
     * Two field wrappers which point to the same node and same field
     * are equal
     */
    public boolean equals(Object obj) {
        if (obj instanceof BaseFieldWrapper) {
            BaseFieldWrapper other=(BaseFieldWrapper)obj;
            return fieldIndex==other.fieldIndex && theNode==other.theNode;
        } else
            return super.equals(obj);
    }

    /**
     * Protected method for determining the access type of the field.
     *  Copied from SAIFieldDefinition to avoid the creation of a secondary
     *  object for routine field checks.
     * @return The access type constant as defined in X3DFieldDefinition.
     */
    protected int getAccessType() {
        int type=theNode.getFieldDeclaration(fieldIndex).getAccessType();
        return type;
    }

    /**
     * Get the definition of this field.
     *
     * @return The field definition to use
     */
    public X3DFieldDefinition getDefinition() {
        if (fieldDefinition==null)
            fieldDefinition=new SAIFieldDefinition(theNode,fieldIndex);
        return fieldDefinition;
    }

    /**
     * Compute a hash code for equality purposes
     * @return The field's hash code
     */
    public int hashCode() {
        return theNode.hashCode()+fieldIndex;
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
        int accessType=getAccessType();
        if (theEventQueue.isNodeRealized(theNode))
            return accessType==FieldConstants.EVENTOUT ||
               accessType==FieldConstants.EXPOSEDFIELD;
        else
            return accessType==FieldConstants.FIELD ||
               accessType==FieldConstants.EXPOSEDFIELD;
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
        int accessType=getAccessType();
        if (theEventQueue.isNodeRealized(theNode))
            return accessType==FieldConstants.EVENTIN ||
                   accessType==FieldConstants.EXPOSEDFIELD;
        else
            return accessType==FieldConstants.EXPOSEDFIELD ||
                   accessType==FieldConstants.FIELD;
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
        theEventAdapterFactory.getAdapter(theNode).addListener(fieldIndex,l);
    }

    /**
     * Remove a listener for changes in the readable field. If the listener is
     * not currently registered, this request shall be silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeX3DEventListener(X3DFieldEventListener l) {
        theEventAdapterFactory.getAdapter(theNode).removeListener(fieldIndex,l);
    }

    /**
     * Associate user data with this field. Whenever an field is generated
     * on this field, this data will be available with the Event through
     * its getData method.
     *
     * @param data The data to associate with this eventOut instance
     */
    public void setUserData(Object data) {
        theNode.setUserData(fieldIndex,data);
    }

    /**
     * Get the user data that is associated with this field.
     *
     * @return The user data, if any, associated with this field
     */
    public Object getUserData() {
        return theNode.getUserData(fieldIndex);
    }

}
