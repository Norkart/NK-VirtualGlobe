/*****************************************************************************
 * Copyright North Dakota State University, 2004
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.vrml.scripting.external.neteai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.web3d.vrml.scripting.external.buffer.ExternalEvent;
import org.web3d.vrml.scripting.external.buffer.NetworkEventQueue;

import vrml.eai.Node;
import vrml.eai.field.EventInMFNode;

/**
 * Client side implementation of EventInMFNode field.
 * EventIn objects double as buffers for data inbound to the event system.
 */
public class EventInMFNodeWrapper extends EventInMFNode 
    implements ExternalEvent, EventWrapper {

    /** The stored value iff hasStoredValue */
    Node[] storedValue;
    
    /** Does this field have a stored value? */
    boolean hasStoredValue;
    
    /** Flag indicating that this event is a Set1Value or accumulation of
     ** Set1Value calls.  This flag is checked during the processing of
     ** set1Value calls in order to correctly either generate a new event,
     ** or merge with a previous set1Value call.*/
    boolean isSet1Value;

    /** The queue for managing events */
    NetworkEventQueue eventQueue;

    /** The network ID of this field */
    int fieldID;
    
    /** Handler for field services */
    FieldAndNodeRequestProcessor requestProcessor;

    /** Factory for constructing Node instances */
    EAIFieldAndNodeFactory fieldFactory;
    
    /**
     * @param fieldID The network field ID
     * @param requestProcessor Handler for field services
     * @param eventQueue The queue to send events to
     */
    public EventInMFNodeWrapper(
        int fieldID, 
        FieldAndNodeRequestProcessor requestProcessor,
        EAIFieldAndNodeFactory fieldFactory,
        NetworkEventQueue eventQueue
      ) {
        this.fieldID=fieldID;
        this.requestProcessor=requestProcessor;
        this.eventQueue=eventQueue;
        this.fieldFactory=fieldFactory;
    }

    /**
     * @param fieldID The network field ID
     * @param requestProcessor Handler for field services
     * @param eventQueue The queue to send events to
     */
    public EventInMFNodeWrapper(
        int fieldID, 
        FieldAndNodeRequestProcessor requestProcessor,
        EAIFieldAndNodeFactory fieldFactory,
        NetworkEventQueue eventQueue,
		Node suppliedValue[]
      ) {
    	this(fieldID,requestProcessor,fieldFactory,eventQueue);
    	storeValue(suppliedValue);
    }

    /** The EventIn*Wrapper classes implement doEvent by posting their
      * stored values to the underlying implementation.
      * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#doEvent
     **/
    public void doEvent() {
        try {
            try {
                requestProcessor.setFieldValue(fieldID,this);
            } finally {
                hasStoredValue=false;
                isSet1Value=false;
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

    /** Two eventIn's are equal if they point to the same actual node and 
      * field
      * @param other The object to compare against
      */
    public boolean equals(Object other) {
        if (other == null)
            return false;
        else if (other instanceof EventWrapper) {
            EventWrapper otherWrapper=(EventWrapper)other;
            return (
                otherWrapper.getFieldID()==fieldID && 
                otherWrapper.getType()==getType()
            );
        } else
            return super.equals(other);
    }

    /** The underlying field ID 
      * @see org.web3d.vrml.scripting.external.neteai.EventWrapper#getFieldID
      */
    public int getFieldID() {
        return fieldID;
    }
  
    /** In order to make the event queueing system easier, and since
      * an equals method is required by the specification, compute the hashcode
      * based on the field number and underlying node hashcode.
      * @see java.lang.Object#hashCode
     **/
    public int hashCode() {
        return fieldID;
    }

    /**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
	 */
	public boolean isConglomerating() {
		return isSet1Value;
	}
    
    /** * @see vrml.eai.field.EventInMFNode#setValue(vrml.eai.Node[])  */
    public void setValue(Node[] value) {
        EventInMFNodeWrapper queuedElement;
        if (!hasStoredValue) {
            queuedElement=this;
            storeValue(value);
        } else {
            queuedElement=new EventInMFNodeWrapper(fieldID,requestProcessor,fieldFactory,eventQueue);
        	queuedElement.storeValue(value);
        }
        eventQueue.processEvent(queuedElement);
    }

    void storeValue(Node[] newValue) {
        if (storedValue==null || storedValue.length!=newValue.length)
            storedValue=new Node[newValue.length];
        System.arraycopy(newValue,0,storedValue,0,newValue.length);
        hasStoredValue=true;
    }
    
    void store1Value(int index, Node newValue) {
    	isSet1Value=true;
    	storedValue[index]=newValue;
    }
    
    /** * @see vrml.eai.field.EventInMFNode#set1Value(int, vrml.eai.Node)  */
    public void set1Value(int index, Node value) {
        synchronized(eventQueue.eventLock) {
            EventInMFNodeWrapper queuedElement=(EventInMFNodeWrapper)
                eventQueue.getLast(this);
            if (queuedElement==null || !queuedElement.isSet1Value) {
                if (!hasStoredValue) {
                    queuedElement=this;
                    requestProcessor.getFieldValue(fieldID,queuedElement);
                    store1Value(index,value);
                } else {
                    queuedElement=new EventInMFNodeWrapper(
                        fieldID,requestProcessor,fieldFactory, eventQueue);
                	queuedElement.store1Value(index,value);
                } eventQueue.processEvent(queuedElement);
            } else
                queuedElement.store1Value(index,value);
        }
    }

    /** * @see vrml.eai.field.EventIn#setUserData(java.lang.Object)  */
    public void setUserData(Object data) {
        requestProcessor.setUserData(fieldID,data);
    }

    /** * @see vrml.eai.field.EventIn#getUserData()  */
    public Object getUserData() {
        return requestProcessor.getUserData(fieldID);
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.EventWrapper#loadFieldValue(java.io.DataInputStream)  */
    public void loadFieldValue(DataInputStream input) throws IOException {
        int numFields=input.readInt();
        storedValue=new Node[numFields];
        for (int counter=0; counter<numFields; counter++)
            storedValue[counter]=fieldFactory.createNode(input.readInt());
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.EventWrapper#writeFieldValue(java.io.DataOutputStream)  */
    public void writeFieldValue(DataOutputStream output) throws IOException {
        output.writeInt(storedValue.length);
        for (int counter=0; counter<storedValue.length; counter++)
            output.writeInt(fieldFactory.getNodeID(storedValue[counter]));
    }

}
