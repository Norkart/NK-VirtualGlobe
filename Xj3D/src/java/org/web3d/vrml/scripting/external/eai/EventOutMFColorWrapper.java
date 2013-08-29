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

import org.web3d.util.ArrayUtils;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

import vrml.eai.field.*;
import vrml.eai.event.VrmlEventListener;

/**
 *  EventOutMFColorWrapper provides the functional implementation of
 *  EventOutMFColor for the SimpleBrowser class.  The functionality of this 
 *  class is specified in the EAI 2.0 specification.
 */

class EventOutMFColorWrapper extends EventOutMFColor 
implements ExternalOutputBuffer, EventWrapper {
    /** Indicates whether to load the value locally or retrieve from node */
    boolean isStored;

    /** The number of elements in the stored value (as opposed to the length
      * of the array). */
    int storedSize;

    /** The stored value if isStored is true.  Unused otherwise. */
    float[] storedValue;

    /** Used to get the ExternalEventAdapter. */
    EAIEventAdapterFactory theEventAdapterFactory;

    /** The underlying Node implementation uses unique Field ID's */
    int theFieldID;

    /** Reference to the underlying Node implementation */
    VRMLNodeType theNode;

    /** Construct the EventOut wrapper instance.
      * @param buffer Should the value be loaded immediately.
      * @param aNode  The underlying VRMLNodeType instance.
      * @param ID     The field ID on the underlying node.
      * @param anAdapterFactory The Event Adapter factory.
      */
    EventOutMFColorWrapper(
        VRMLNodeType aNode, int ID, EAIEventAdapterFactory anAdapterFactory, 
        boolean buffer
    ) {
        fieldType=MFColor;
        theNode=aNode;
        theFieldID=ID;
        theEventAdapterFactory=anAdapterFactory;
        if (buffer) {
            loadOutputValue();
        }
    }

    /** @see vrml.eai.field.EventOut#addVrmlEventListener */
    public void addVrmlEventListener(VrmlEventListener listener) {
        theEventAdapterFactory.getAdapter(theNode).addListener(
            theFieldID,listener
        );
    }

    /** Two eventOut's are equal if they are connected to the same actual 
      * node and field.
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

    /** @see vrml.eai.field.EventOut#getUserData */
    public Object getUserData() {
        try {
            return theNode.getUserData(theFieldID);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error getting user data",ife);
        }
    }

    /** Re-initialize the buffer so that it can service another node
     *  @param aNode The new underlying node
     *  @param ID The new field ID 
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
    }

    /** @see vrml.eai.field.EventOut#removeVrmlEventListener */
    public void removeVrmlEventListener(VrmlEventListener listener) {
        theEventAdapterFactory.getAdapter(theNode).removeListener(
            theFieldID,listener
        );
    }

    /** @see vrml.eai.field.EventOutMFColor#get1Value */
    public float[] get1Value(int index) {
        float[] result=new float[3];
        get1Value(index,result);
        return result;
    }

    /** @see vrml.eai.field.EventOutMFColor#get1Value */
    public void get1Value(int index, float dest[]) {
        if (isStored)
            System.arraycopy(storedValue,index*3,dest,0,3);
        else
            try {
                VRMLFieldData fieldData=theNode.getFieldValue(theFieldID);
                if (index<0 || index>=fieldData.numElements)
                	throw new ArrayIndexOutOfBoundsException();
                System.arraycopy(fieldData.floatArrayValue,index*3,dest,0,3);
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting field value");
            }
    }

    /** @see vrml.eai.field.EventOutMFColor#getValue */
    public void getValue(float[][] dest) {
        if (isStored)
            ArrayUtils.raise3(storedValue,storedSize,dest);
        else 
            try {
                // Not using ArrayUtils because we need to allocate the array.
                VRMLFieldData fieldData=theNode.getFieldValue(theFieldID);
                int arraySize=fieldData.numElements;
                ArrayUtils.raise3(fieldData.floatArrayValue,arraySize,dest);
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting field value");
            }
    }

    /** @see vrml.eai.field.EventOutMFColor#getValue */
    public float[][] getValue() {
        float result[][]=new float[size()][];
        for (int counter=0; counter<result.length; counter++)
            result[counter]=new float[3];
        if (isStored) {
            ArrayUtils.raise3(storedValue,storedSize,result);
            return result;
        } try {
            VRMLFieldData fieldData=theNode.getFieldValue(theFieldID);
            ArrayUtils.raise3(
                fieldData.floatArrayValue,fieldData.numElements,result
            );
            return result;
        } catch (InvalidFieldException ife) {
            throw new RuntimeException(
                "InvalidFieldException Error getting field value.  "+
                "Was looking for fieldID"+theFieldID+" in a "+
                theNode.getClass().getName()+"."
            );
        }
    }

    /** @see vrml.eai.field.EventOutMFColor#getValue */
    public void getValue(float[] dest) {
        if (isStored)
            System.arraycopy(storedValue,0,dest,0,storedValue.length);
        else
            try {
                VRMLFieldData fieldData=theNode.getFieldValue(theFieldID);
                System.arraycopy(
                    fieldData.floatArrayValue,0,dest,0,
                    fieldData.floatArrayValue.length
                );
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting field value");
            }
    }

    /** @see ExternalOutputBuffer#loadValue
      * Method not thread safe.
      */
    public void loadOutputValue() {
        isStored=false;
        // Set isStored false to read live value.
        // Do this because we re-use instances.
        storedSize=size();
        if (storedValue==null || storedValue.length!=(storedSize*3))
            storedValue=new float[storedSize*3];
        getValue(storedValue);
        isStored=true;
    }

    /** @see vrml.eai.field.EventOutMField#size */
    public int size() {
        if (isStored)
            return storedSize;
        else
            try {
                VRMLFieldData fieldData=theNode.getFieldValue(theFieldID);
                return fieldData.numElements;
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting field size");
            }
    }

    /** @see vrml.eai.field.EventOut#setUserData */
    public void setUserData(Object data) {
        try {
            theNode.setUserData(theFieldID,data);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error setting user data");
        }
    }

}
