package org.web3d.vrml.scripting.external.eai;

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

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEvent;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import vrml.eai.field.*;

/**
 *  EventInSFRotationWrapper provides the functional implementation of
 *  EventInSFRotation for the SimpleBrowser class.  The functionality of this
 *  class is specified in the EAI 2.0 specification.
 */

class EventInSFRotationWrapper extends EventInSFRotation
implements ExternalEvent, EventWrapper {
    /** Flag indicating if a value has been stored for later transmission. */
    boolean hasStoredValue;

    /** The value stored in this event (if hasStoredValue is true) */
    float storedValue[];

    /** The underlyng Node implementation uses integer Field ID's */
    int theFieldID;

    /** Reference to the underlying Node implementation */
    VRMLNodeType theNode;

    /** The queue to post events to.*/
    ExternalEventQueue theQueue;

    /** Basic constructor for general use.
      * @param aNode The underlying target node
      * @param id The underlying target field
      * @param aQueue The queue to send events to
    */
    EventInSFRotationWrapper(
        VRMLNodeType aNode, int id, ExternalEventQueue aQueue
    ) {
        fieldType=SFRotation;
        theNode=aNode;
        theFieldID=id;
        theQueue=aQueue;
        storedValue=new float[4];
    }

    /** Constructor for use in event queueing system.
      * @param aNode The underlying target node
      * @param id The underlying target field
      * @param aQueue The queue to send events to
      * @param newValue The value for this event
    */
    EventInSFRotationWrapper(
        VRMLNodeType aNode, int id, ExternalEventQueue aQueue, float newValue[]
    ) {
        this(aNode,id,aQueue);
        storeValue(newValue);
    }

    /** The EventIn*Wrapper classes implement doEvent by posting their
        stored values to the underlying implementation.
        @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#doEvent
     **/
    public void doEvent() {
        try {
            try {
                theNode.setValue(theFieldID,storedValue,4);
            } finally {
                hasStoredValue=false;
            }
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException(
                "InvalidFieldException setting EventIn value.",ife
            );
        } catch (org.web3d.vrml.lang.InvalidFieldValueException ifve) {
            throw new RuntimeException(
                "InvalidFieldValueException setting EventIn value.",ifve
            );
        }
    }

    /** Two evenIn's are euqal if they point to the same actual node and
      * field */
    public boolean equals(Object other) {
        if (other == null)
            return false;
        else if (other instanceof EventWrapper) {
            EventWrapper otherWrapper=(EventWrapper)other;
            return (
                otherWrapper.getFieldNode()==theNode &&
                otherWrapper.getFieldID()==theFieldID &&
                otherWrapper.getType()==getType()
            );
        } else
            return super.equals(other);
    }

    /** The underlying field ID
      * @see org.web3d.vrml.scripting.external.eai.EventWrapper#getFieldID
      */
    public int getFieldID() {
        return theFieldID;
    }

    /** The underlying implementation node.
      * @see org.web3d.vrml.scripting.external.eai.EventWrapper#getFieldNode
      */
    public VRMLNodeType getFieldNode() {
        return theNode;
    }

    /** @see vrml.eai.field.EventIn#getUserData */
    public Object getUserData() {
        try {
            return theNode.getUserData(theFieldID);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error getting user data");
        }
    }

    /** In order to make the event queueing system easier, and since
      * an equals method is required by the specification, compute the hashcode
      * based on the field number and underlying node hashcode.
      * @see java.lang.Object#hashCode
     **/
    public int hashCode() {
        return theNode.hashCode()+theFieldID;
    }

    /**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
	 */
	public boolean isConglomerating() {
		return false;
	}
    
    /** @see vrml.eai.field.EventIn#setUserData */
    public void setUserData(Object data) {
        try {
            theNode.setUserData(theFieldID, data);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error setting user data");
        }
    }

    /** @see vrml.eai.field.EventInSFRotation#setValue */
    public void setValue(float[] newValue) {
        ExternalEvent queuedElement;
        if (!hasStoredValue) {
            queuedElement=this;
            storeValue(newValue);
        } else
            queuedElement=new EventInSFRotationWrapper(
                theNode,theFieldID,theQueue,newValue
            );
        theQueue.processEvent(queuedElement);
    }

    /** Store a value in this EventIn as a place holder for the buffering
      * system.
      * @param newValue The value to store (copied).
      */
    private void storeValue(float newValue[]) {
        hasStoredValue=true;
        System.arraycopy(newValue,0,storedValue,0,4);
    }

}
