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

import vrml.eai.Node;
import vrml.eai.event.VrmlEventListener;
import vrml.eai.field.EventOutMFNode;

/**
 * Client side implementation of EventOutMFNodeWrapper field.
 * EventOut fields which are used by the vrmlEventChanged
 * broadcast system report only their stored value.
 * EventOut fields produced by Node.getEventOut report only
 * the 'live' value of the field.
 */
public class EventOutMFNodeWrapper extends EventOutMFNode 
    implements EventWrapper {

    /** Factory for constructing Node instances */
    EAIFieldAndNodeFactory nodeFactory;
    
    /** Does this field have a stored value? */
    boolean hasStoredValue;
    
    /** The stored value iff hasStoredValue */
    Node[] storedValue;
    
    /** The network ID of this field */
    int fieldID;
    
    /** Handler for field services */
    FieldAndNodeRequestProcessor requestProcessor;

    /**
     * @param fieldID The network field ID
     * @param requestProcessor Handler for field services
     */
    public EventOutMFNodeWrapper(
        int fieldID, 
        FieldAndNodeRequestProcessor requestProcessor,
        EAIFieldAndNodeFactory factory
    ) {
        this.fieldID=fieldID;
        this.requestProcessor=requestProcessor;
        nodeFactory=factory;
    }

    /**
     * @param fieldID The network field ID
     * @param requestProcessor Handler for field services
     * @param source The stream to read field values from
     * @throws IOException
     */
    public EventOutMFNodeWrapper(
        int fieldID, 
        FieldAndNodeRequestProcessor requestProcessor, 
        EAIFieldAndNodeFactory factory,
        DataInputStream source
    ) throws IOException {
        this(fieldID,requestProcessor,factory);
        loadFieldValue(source);
        hasStoredValue=true;
    }

    /** Two fields are equal if they point to the same actual node and 
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
  
    /** * @see vrml.eai.field.EventOutMFNode#getValue()  */
    public Node[] getValue() {
        Node result[]=new Node[size()];
        getValue(result);
        return result;
    }

    /** * @see vrml.eai.field.EventOutMFNode#getValue(vrml.eai.Node[])  */
    public void getValue(Node[] nodes) {
        if (hasStoredValue)
            System.arraycopy(storedValue,0,nodes,0,storedValue.length);
        else {
            requestProcessor.getFieldValue(fieldID,this);
            System.arraycopy(storedValue,0,nodes,0,storedValue.length);
        }
    }

    /** * @see vrml.eai.field.EventOutMFNode#get1Value(int)  */
    public Node get1Value(int index) {
        if (hasStoredValue)
            return storedValue[index];
        else
            throw new RuntimeException("Not yet implemented");
    }

    /** In order to make the event queueing system easier, and since
      * an equals method is required by the specification, compute the hashcode
      * based on the field number and underlying node hashcode.
      * @see java.lang.Object#hashCode
     **/
    public int hashCode() {
        return fieldID;
    }

    /** * @see vrml.eai.field.EventOutMField#size()  */
    public int size() {
        if (hasStoredValue)
            return storedValue.length;
        else
            return requestProcessor.getNumFieldValues(fieldID);
    }

    /** * @see vrml.eai.field.EventOut#addVrmlEventListener(vrml.eai.event.VrmlEventListener)  */
    public void addVrmlEventListener(VrmlEventListener l) {
        requestProcessor.addVrmlEventListener(fieldID,getType(),l);
    }

    /** * @see vrml.eai.field.EventOut#removeVrmlEventListener(vrml.eai.event.VrmlEventListener)  */
    public void removeVrmlEventListener(VrmlEventListener l) {
        requestProcessor.removeVrmlEventListener(fieldID,l);
    }

    /** * @see vrml.eai.field.EventOut#setUserData(java.lang.Object)  */
    public void setUserData(Object data) {
        requestProcessor.setUserData(fieldID,data);
    }

    /** * @see vrml.eai.field.EventOut#getUserData()  */
    public Object getUserData() {
        return requestProcessor.getUserData(fieldID);
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.EventWrapper#loadFieldValue(java.io.DataInputStream)  */
    public void loadFieldValue(DataInputStream input) throws IOException {
        int numFields=input.readInt();
        storedValue=new Node[numFields];
        for (int counter=0; counter<numFields; counter++)
            storedValue[counter]=nodeFactory.createNode(input.readInt());
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.EventWrapper#writeFieldValue(java.io.DataOutputStream)  */
    public void writeFieldValue(DataOutputStream output) throws IOException {
        output.writeInt(storedValue.length);
        for (int counter=0; counter<storedValue.length; counter++)
            output.writeInt(nodeFactory.getNodeID(storedValue[counter]));
    }

}
