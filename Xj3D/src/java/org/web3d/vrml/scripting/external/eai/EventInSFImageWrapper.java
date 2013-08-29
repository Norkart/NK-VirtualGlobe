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
 * EventInSFImageWrapper provides the functional implementation of
 * EventInSFImage for the SimpleBrowser class.
 */

class EventInSFImageWrapper extends EventInSFImage
implements ExternalEvent, EventWrapper {
    /** Flag indicating if a value has been stored for later transmission. */
    boolean hasStoredValue;

    /** The value stored in this event (if hasStoredValue is true) */
    int storedValue[];

    /** The underlying Node implementation uses integer Field ID's */
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
    EventInSFImageWrapper(
        VRMLNodeType aNode, int id, ExternalEventQueue aQueue
    ) {
        fieldType=SFImage;
        theNode=aNode;
        theFieldID=id;
        theQueue=aQueue;
    }

    /** Constructor for use in event queueing system.
      * Note that this constructor keeps the array and doesn't copy it.
      * @param aNode The underlying target node
      * @param id The underlying target field
      * @param aQueue The queue to send events to
      * @param newValue The value for this event
      */
    EventInSFImageWrapper(
        VRMLNodeType aNode, int id, ExternalEventQueue aQueue, int newValue[]
    ) {
        this(aNode,id,aQueue);
        hasStoredValue=true;
        storedValue=newValue;
    }

    /** The EventIn*Wrapper classes implement doEvent by posting their
      * stored values to the underlying implementation.
      * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#doEvent
     **/
    public void doEvent() {
        try {
            try {
                theNode.setValue(theFieldID,storedValue,storedValue.length);
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
      * field.
      * @param other The object to compare against. */
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
            theNode.setUserData(theFieldID,data);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error setting user data");
        }
    }

    /**
     @see vrml.eai.field.EventInSFImage#setValue
     */
    public void setValue(int width, int height, int components, int[] pixels) {
        /* According to the Mr. Couch, the underlying representation is
         * roughly according to section 5.5 of the VRML97 specification */
        if (width<0 || height<0)
            throw new IllegalArgumentException("Invalid image size");
        if (components<0 || components >4)
            throw new IllegalArgumentException("Invalid number of components");
        ExternalEvent queuedElement;
        if (!hasStoredValue) {
            queuedElement=this;
            storeValue(width,height,components,pixels);
        } else {
            // This is the current 'optimized for minimal allocation' version.
            // Since we can't use the user's array, the new event will
            // keep the one we allocate here.
            int actualData[]=new int[width*height+3];
            actualData[0]=width;
            actualData[1]=height;
            actualData[2]=components;
            System.arraycopy(pixels,0,actualData,3,height*width);
            queuedElement=new EventInSFImageWrapper(
                theNode,theFieldID,theQueue,actualData
            );
        }
        theQueue.processEvent(queuedElement);
    }


    /** Store a value in this EventIn as a place holder for the buffering
      * system.  */
    private void storeValue(
        int width, int height, int components, int pixels[]
    ) {
        hasStoredValue=true;
        int imgSize=height*width;
        if (storedValue==null || storedValue.length!=(imgSize+3))
            storedValue=new int[imgSize+3];
        storedValue[0]=width;
        storedValue[1]=height;
        storedValue[2]=components;
        System.arraycopy(pixels,0,storedValue,3,imgSize);
    }

}
