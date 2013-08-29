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

import org.web3d.util.ArrayUtils;
import org.web3d.vrml.scripting.external.buffer.ExternalEvent;
import org.web3d.vrml.scripting.external.buffer.NetworkEventQueue;

import vrml.eai.field.EventInMFColor;

/**
 * Client side implementation of EventInMFColor field.
 * EventIn objects double as buffers for data inbound to the event system.
 */
public class EventInMFColorWrapper extends EventInMFColor 
    implements ExternalEvent, EventWrapper {

    /** The queue for managing events */
    NetworkEventQueue eventQueue;
    
    /** Does this field have a stored value? */
    boolean hasStoredValue;
    
    /** Flag indicating that this event is a Set1Value or accumulation of
     ** Set1Value calls.  This flag is checked during the processing of
     ** set1Value calls in order to correctly either generate a new event,
     ** or merge with a previous set1Value call.*/
    boolean isSet1Value;

    /** The stored value iff hasStoredValue */
    float[] storedValue;
    
    /** The network ID of this field */
    int fieldID;
    
    /** Handler for field services */
    FieldAndNodeRequestProcessor requestProcessor;
    
    /**
     * @param fieldID The network field ID
     * @param requestProcessor Handler for field services
     * @param eventQueue The queue to send events to
     */
    public EventInMFColorWrapper(
        int fieldID, 
        FieldAndNodeRequestProcessor requestProcessor,
        NetworkEventQueue eventQueue
    ) {
        this.fieldID=fieldID;
        this.requestProcessor=requestProcessor;
        this.eventQueue=eventQueue;
    }

    /**
     * @param fieldID The network field ID
     * @param requestProcessor Handler for field services
     * @param eventQueue The queue to send events to
     * @param newValue The value to initialize the field with
     */
    public EventInMFColorWrapper(
            int fieldID,  
            FieldAndNodeRequestProcessor requestProcessor,
            NetworkEventQueue eventQueue,
            float newValue[][]) {
        this(fieldID,requestProcessor,eventQueue);
        storeValue(newValue);
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
    
    /** * @see vrml.eai.field.EventInMFColor#setValue(float[][])  */
    public void setValue(float[][] value) {
        EventInMFColorWrapper queuedElement;
        if (!hasStoredValue) {
            queuedElement=this;
            storeValue(value);
        } else
            queuedElement=new EventInMFColorWrapper(
                fieldID,requestProcessor,eventQueue,value
            );
        eventQueue.processEvent(queuedElement);
    }

    /** * @see vrml.eai.field.EventInMFColor#set1Value(int, float[])  */
    public void set1Value(int index, float[] value) {
        synchronized(eventQueue.eventLock) {
            EventInMFColorWrapper queuedElement=(EventInMFColorWrapper)
                eventQueue.getLast(this);
            if (queuedElement==null || !queuedElement.isSet1Value) {
                if (!hasStoredValue) {
                    queuedElement=this;
                    requestProcessor.getFieldValue(fieldID,queuedElement);
                    store1Value(index,value);
                } else {
                    queuedElement=new EventInMFColorWrapper(
                        fieldID,requestProcessor,eventQueue);
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
        storedValue=new float[numFields*3];
        for (int counter=0; counter<storedValue.length; counter++)
            storedValue[counter]=input.readFloat();
    }

    /** Store a value in this EventIn as a place holder for the buffering
    system.  */
    private void store1Value(int index, float[] newValue) {
        isSet1Value=true;
        System.arraycopy(newValue,0,storedValue,index*3,3);
    }

    /** Store a value in this EventIn as a place holder for the buffering
     * system.  */
    private void storeValue(float[][] newValue) {
        if (newValue!=null) {
            if (storedValue==null || storedValue.length!=(newValue.length*3))
                storedValue=new float[newValue.length*3];
            ArrayUtils.flatten3(newValue,newValue.length,storedValue);
        } else
            storedValue=null;
        hasStoredValue=true;
    }
    
    /** * @see org.web3d.vrml.scripting.external.neteai.EventWrapper#writeFieldValue(java.io.DataOutputStream)  */
    public void writeFieldValue(DataOutputStream output) throws IOException {
        output.writeInt(storedValue.length/3);
        for (int counter=0; counter<storedValue.length; counter++)
            output.writeFloat(storedValue[counter]);
    }

}
