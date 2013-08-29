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

package org.web3d.vrml.scripting.external.eai;

// External imports
import vrml.eai.field.*;

import vrml.eai.Node;

// Local imports
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEvent;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;

/**
 *  EventInMFNodeWrapper provides the functional implementation of
 *  EventInMFNode for the SimpleBrowser class.  The functionality of this class
 *  is specified in the EAI 2.0 specification.
 *
 * @author Brad Vender
 * @version $Revision: 1.13 $
 */
class EventInMFNodeWrapper extends EventInMFNode
    implements ExternalEvent, EventWrapper {

    /** Flag indicating if a value has been stored for later transmission. */
    private boolean hasStoredValue;

    /** Flag indicating that this event is a Set1Value or accumulation of
     ** Set1Value calls.  This flag is checked during the processing of
     ** set1Value calls in order to correctly either generate a new event,
     ** or merge with a previous set1Value call.*/
    private boolean isSet1Value;

    /** The value stored in this event (if hasStoredValue is true) */
    private VRMLNodeType storedValue[];

    /** The underlying Node implementation uses integer field ID's */
    private int theFieldID;

    /** Reference to the underlying Node implementation */
    private VRMLNodeType theNode;

    /** The factory instance used to convert between Node and VRMLNodeType */
    private VRMLNodeFactory theNodeFactory;

    /** The queue to post events to.*/
    private ExternalEventQueue theQueue;

    /** The basic constructor.
      * @param aNode VRMLNodeType owning this field
      * @param id The field ID for this field on the owning VRMLNodeType
      * @param aNodeFactory The VRMLNodeFactory to use in mapping between
        VRMLNodeType and vrml.eai.Node
      * @param aQueue The queue to post events to
      */
    EventInMFNodeWrapper(
        VRMLNodeType aNode, int id, VRMLNodeFactory aNodeFactory,
        ExternalEventQueue aQueue
    ) {
        fieldType=MFNode;
        theNode=aNode;
        theFieldID=id;
        theNodeFactory=aNodeFactory;
        theQueue=aQueue;
    }

    /** Constructor for use in event queueing system.  The new field value is
      * specified in the constructor.
      * @param aNode VRMLNodeType owning this field
      * @param id The field ID for this field on the owning VRMLNodeType
      * @param aNodeFactory The VRMLNodeFactory to use in mapping between
        VRMLNodeType and vrml.eai.Node
      * @param aQueue The queue to post events to
      * @param newValue The new value to post to the field
      **/
    EventInMFNodeWrapper(
        VRMLNodeType aNode, int id, VRMLNodeFactory aNodeFactory,
        ExternalEventQueue aQueue, Node newValue[]
    ) {
        this(aNode,id,aNodeFactory,aQueue);
        storeValue(newValue);
    }

    /** Constructor for use in set1Value event queueing.  The current
      * field value will be loaded before applying the change.
      * @param aNode VRMLNodeType owning this field
      * @param id The field ID for this field on the owning VRMLNodeType
      * @param aNodeFactory The VRMLNodeFactory to use in mapping between
        VRMLNodeType and vrml.eai.Node
      * @param aQueue The queue to post events to
      * @param index The index of the field to change
      * @param newValue storedValue[index]=newValue
      */
    EventInMFNodeWrapper(
        VRMLNodeType aNode, int id, VRMLNodeFactory aNodeFactory,
        ExternalEventQueue aQueue, int index, Node newValue
    ) {
        this(aNode,id,aNodeFactory,aQueue);
        loadValue();
        store1Value(index,newValue);
    }

    /** The EventIn*Wrapper classes implement doEvent by posting their
        stored values to the underlying implementation.
        @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#doEvent
     **/
    public void doEvent() {
        try {
            try {
                theNode.setValue(theFieldID,storedValue,storedValue.length);
            } finally {
                for (int counter=0; counter<storedValue.length; counter++)
                    storedValue[counter]=null;
                isSet1Value=false;
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

    /** Two WrapperFields are equal if they point to the same actual node and
      field.
      * @param other The object to compare against
    */
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
        return isSet1Value;
    }

    /** Load the current event value from the underlying implmementation.
     ** This method is used to implement the semantics of the set1Value
     ** methods.  After this method returns, hasStoredValue will be
     ** true, and storedValue represents the value of the field at the
     ** time called. */
    private void loadValue() {
        try {
            VRMLFieldData fieldValue=theNode.getFieldValue(theFieldID);
            if (storedValue==null || storedValue.length!=fieldValue.numElements)
                storedValue=new VRMLNodeType[fieldValue.numElements];
            System.arraycopy(
                fieldValue.nodeArrayValue,0,storedValue,0,fieldValue.numElements
            );
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException(
                "InvalidFieldException Error setting EventIn value."
            );
        } catch (org.web3d.vrml.lang.InvalidFieldValueException ifve) {
            throw new RuntimeException(
                "InvalidFieldValueException Error setting EventIn value."
            );
        }
        hasStoredValue=true;
    }

    /** @see vrml.eai.field.EventIn#setUserData */
    public void setUserData(Object data) {
        try {
            theNode.setUserData(theFieldID,data);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error setting user data");
        }
    }

    /** @see vrml.eai.field.EventInMFNode#setValue */
    public void setValue(Node[] newValue) {
        EventInMFNodeWrapper queuedElement;
        if (!hasStoredValue) {
            queuedElement=this;
            storeValue(newValue);
        } else
            queuedElement=new EventInMFNodeWrapper(
                theNode,theFieldID,theNodeFactory,theQueue,newValue
            );
        theQueue.processEvent(queuedElement);
    }

    /** @see vrml.eai.field.EventInMFNode#set1Value */
    public void set1Value(int index, Node aValue) {
        synchronized(theQueue.eventLock) {
            EventInMFNodeWrapper queuedElement=(EventInMFNodeWrapper)
            theQueue.getLast(this);
            if (queuedElement==null || !queuedElement.isSet1Value) {
                if (!hasStoredValue) {
                    queuedElement=this;
                    loadValue();
                    store1Value(index,aValue);
                } else
                    queuedElement=new EventInMFNodeWrapper(
                        theNode,theFieldID,theNodeFactory,theQueue,index,aValue
                    );
                theQueue.processEvent(queuedElement);
            } else
                queuedElement.store1Value(index,aValue);
          }
    }

    /** Store a value in this EventIn as a place holder for the buffering
      * system.
      * @param newValue The value to post to the field
      */
    private void storeValue(Node[] newValue) {
        hasStoredValue=true;
        if (newValue!=null) {
            if (storedValue==null || newValue.length!=storedValue.length)
                storedValue=new VRMLNodeType[newValue.length];
            int counter;
            for (counter=0; counter<newValue.length; counter++)
                storedValue[counter]=
                    theNodeFactory.getVRMLNode(newValue[counter]);
        } else
            storedValue=null;
    }

    /** Change one index of the currently stored value.
      * @param index oldValue[index]=conversion(aValue)
      * @param aValue oldValue[index]=conversion(aValue)
      */
    private void store1Value(int index, Node aValue) {
        isSet1Value=true;
        storedValue[index]=theNodeFactory.getVRMLNode(aValue);
    }

}
