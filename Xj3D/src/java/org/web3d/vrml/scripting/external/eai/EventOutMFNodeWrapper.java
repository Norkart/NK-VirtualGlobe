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

import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

import vrml.eai.event.VrmlEventListener;
import vrml.eai.field.*;
import vrml.eai.Node;

/**
 *  EventOutMFNodeWrapper provides the functional implementation of
 *  EventOutMFNode for the SimpleBrowser class.  The functionality of this class
 *  is specified in the EAI 2.0 specification.
 */

class EventOutMFNodeWrapper extends EventOutMFNode 
implements ExternalOutputBuffer, EventWrapper {
    /** Indicates whether to load the value locally or retrieve from node */
    boolean isStored;

    /** The stored value if isStored is true.  Unused otherwise. */
    Node[] storedValue;

    /** Used to get the ExternalEventAdapter. */
    EAIEventAdapterFactory theEventAdapterFactory;

    /** The underlying Node implementation uses unique integer field ID's */
    int theFieldID;

    /** Reference to the underlying Node implementation */
    VRMLNodeType theNode;

    /** The VRMLNodeFactory to use for mapping between VRMLNodeType and 
      * vrml.eai.Node */
    VRMLNodeFactory theNodeFactory;

    /** Construct the EventOut wrapper instance.
      * @param buffer Should the value be loaded immediately.
      * @param aNode  The underlying VRMLNodeType instance.
      * @param ID     The field ID on the underlying node.
      * @param anAdapterFactory The Event Adapter factory.
      */
    EventOutMFNodeWrapper(
        VRMLNodeType aNode, int ID, VRMLNodeFactory aNodeFactory, 
        EAIEventAdapterFactory anAdapterFactory, boolean buffer
    ) {
        fieldType=MFNode; 
        theNode=aNode;
        theFieldID=ID;
        theNodeFactory=aNodeFactory;
        theEventAdapterFactory=anAdapterFactory;
        if (buffer)
            loadOutputValue();
    }

    /** @see vrml.eai.field.EventOut#addVrmlEventListener */
    public void addVrmlEventListener(VrmlEventListener listener) {
        theEventAdapterFactory.getAdapter(theNode).addListener(
            theFieldID,listener
        );
    }

    /** Two eventOut's are equal if they are connected to the same actual 
      * node and field.
      * @param other The object to compare against.
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

    /** @see vrml.eai.field.EventOut#getUserData */
    public Object getUserData() {
        try {
            return theNode.getUserData(theFieldID);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error getting user data",ife);
        }
    }

    /** @see vrml.eai.field.EventOutMFNode#get1Value */
    public Node get1Value(int index) {
        if (isStored)
            return storedValue[index];
        else
            try {
                VRMLFieldData fieldData=theNode.getFieldValue(theFieldID);
                if (index<0 || index>=fieldData.numElements)
                    throw new ArrayIndexOutOfBoundsException();
                else
                    return theNodeFactory.getEAINode(
                        (VRMLNodeType)(fieldData.nodeArrayValue[index])
                    );
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting field value.");
            }
    }

    /** @see vrml.eai.field.EventOutMFNode#getValue */
    public void getValue(Node[] dest) {
        if (isStored)
            System.arraycopy(storedValue,0,dest,0,storedValue.length);
        else
            try {
                VRMLFieldData fieldData=theNode.getFieldValue(theFieldID);
                VRMLNode[] realValues=fieldData.nodeArrayValue;
                Node results[]=null;
                if (fieldData.numElements!=0) {
                    results=new Node[fieldData.numElements];
                    int counter;
                    for (counter=0;counter<results.length;counter++)
                        results[counter]=theNodeFactory.getEAINode(
                            (VRMLNodeType)(realValues[counter])
                        );
                    System.arraycopy(results,0,dest,0,results.length);
                }
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting value.");
            }
    }

    /** @see vrml.eai.field.EventOutMFNode#getValue */
    public Node[] getValue() {
        Node[] value=new Node[size()];
        getValue(value);
        return value;
    }

	/** Since the equals implementation is given by the spec,
	 *  it is implied that hashCode is defined so that equal
	 *  items have equal hash codes.
	  * @see java.lang.Object#hashCode
	 **/
	public int hashCode() {
		return theNode.hashCode()+theFieldID;
	}

    /** @see ExternalOutputBuffer#loadValue */
    public void loadOutputValue() {
        isStored=false;
        // Set isStored false to read live value.
        // Do this because we re-use instances.
        int size=size();
        if (storedValue==null || storedValue.length!=size)
            storedValue=new Node[size];
        getValue(storedValue);
        isStored=true;
    }

    /** @see vrml.eai.field.EventOutMField#size */
    public int size() {
        if (isStored)
            return storedValue.length;
        else
            try {
                return theNode.getFieldValue(theFieldID).numElements;
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting field size.");
            }
    }

    /** @see vrml.eai.field.EventOut#removeVrmlEventListener */
    public void removeVrmlEventListener(VrmlEventListener listener) {
        theEventAdapterFactory.getAdapter(theNode).removeListener(
            theFieldID,listener
        );
    }

    /** @see vrml.eai.field.EventOut#setUserData */
    public void setUserData(Object data) {
        try {
            theNode.setUserData(theFieldID,data);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error setting user data");
        }
    }

    /** Re-initialize the buffer so that it can service another node
      * @param aNode The new underlying node
      * @param ID The new field ID
      */
    public void initialize(VRMLNodeType aNode, int ID) {
        theNode=aNode;
        theFieldID=ID;
    }

    /** Clear out any stored VRMLNodeType references */
    public void reset() {
        theNode=null;
        theFieldID=-1;
        isStored=false;
        for (int counter=0; counter<storedValue.length; counter++)
            storedValue[counter]=null;
    }

}
