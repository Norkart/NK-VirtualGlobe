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

import vrml.eai.field.EventInSFImage;

/**
 * Client side implementation of SFImage field.
 * EventIn objects double as buffers for data inbound to the event system.
 */
public class EventInSFImageWrapper extends EventInSFImage 
    implements ExternalEvent, EventWrapper {

    /** The stored value iff hasStoredValue */
    int[] storedValue;
    
    /** Does this field have a stored value? */
    boolean hasStoredValue;
    
    /** The queue for managing events */
    NetworkEventQueue eventQueue;

    /** The network ID of this field */
    int fieldID;
    
    /** Handler for field services */
    FieldAndNodeRequestProcessor requestProcessor;

    /**
     * @param fieldID The network field ID
     * @param requestProcessor Handler for field services
     * @param eventQueue The queue to send events to
     */
    public EventInSFImageWrapper(
        int fieldID, 
        FieldAndNodeRequestProcessor requestProcessor,
        NetworkEventQueue eventQueue
    ) {
        this.fieldID=fieldID;
        this.requestProcessor=requestProcessor;
        this.eventQueue=eventQueue;
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
		return false;
	}
    
    /** * @see vrml.eai.field.EventInSFImage#setValue(int, int, int, int[])  */
    public void setValue(int width, int height, int components, int[] pixels) {
        EventInSFImageWrapper queuedElement;
        if (!hasStoredValue)
            queuedElement=this;
        else
            queuedElement=new EventInSFImageWrapper(fieldID,requestProcessor,eventQueue);
        if (queuedElement.storedValue==null || queuedElement.storedValue.length!=pixels.length+3)
            queuedElement.storedValue=new int[pixels.length+3];
        queuedElement.storedValue[0]=width;
        queuedElement.storedValue[1]=height;
        queuedElement.storedValue[2]=components;
        System.arraycopy(pixels,0,queuedElement.storedValue,3,pixels.length);
        queuedElement.hasStoredValue=true;
        eventQueue.processEvent(queuedElement);
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
        int numInts=input.readInt();
        storedValue=new int[numInts];
        for (int counter=0; counter<storedValue.length; counter++)
            storedValue[counter]=input.readInt();
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.EventWrapper#writeFieldValue(java.io.DataOutputStream)  */
    public void writeFieldValue(DataOutputStream output) throws IOException {
        output.writeInt(storedValue.length);
        for (int counter=0; counter<storedValue.length; counter++)
            output.writeInt(storedValue[counter]);
    }

}
