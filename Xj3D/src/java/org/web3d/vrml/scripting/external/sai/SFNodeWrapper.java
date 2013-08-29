package org.web3d.vrml.scripting.external.sai;

/*****************************************************************************
 * Copyright North Dakota State University, 2001-2005
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
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

/**
 * Representation of a SFNode field.
 * <P>
 * Get the value of a node. The java <CODE>null</CODE> reference is treated to
 * be equivalent to the VRML <CODE>NULL</CODE> field values. If the node field
 * contains a NULL reference then reading this eventOut will result in a
 * java null being returned.
 *
 */
class SFNodeWrapper extends BaseFieldWrapper implements SFNode, ExternalEvent, ExternalOutputBuffer {

    /** The factory for converting field values from VRMLNodeType to
      * X3DNode */
    SAINodeFactory nodeFactory;

    /** The value stored in this buffer iff storedInput */
    VRMLNodeType storedInputValue;

    /** The value stored in this buffer iff storedInput */
    X3DNode storedOutputValue;

    /** Basic constructor for wrappers without preloaded values
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param aNodeFactory The node factory for converting between X3DNode and VRMLNodeType
     * @param factory The adapter factory for registering interest
     */
    SFNodeWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAINodeFactory aNodeFactory, SAIEventAdapterFactory factory
    ) {
        super(node,field,aQueue,factory);
        nodeFactory=aNodeFactory;
    }

    SFNodeWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAINodeFactory aNodeFactory, SAIEventAdapterFactory factory,
        boolean isInput
    ) {
        this(node,field,aQueue,aNodeFactory,factory);
        if (isInput)
            loadInputValue();
        else
            loadOutputValue();
    }

    /** Post any queued field values to the target field */
    public void doEvent() {
    	try {
            theNode.setValue(fieldIndex,storedInputValue);
        } finally {
            storedInputValue=null;
            storedInput=false;
        }
    }

    /**
     * Get the node value in the given eventOut. If no node reference is set then
     * null is returned to the user.
     * <P>
     * @return The new node reference set.
     */
    public X3DNode getValue() {
        if (storedOutput)
            return storedOutputValue;
        else {
        	checkReadAccess();
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            return nodeFactory.getSAINode((VRMLNodeType) data.nodeValue);
        }
    }

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer#initialize(org.web3d.vrml.nodes.VRMLNodeType, int)
     */
    public void initialize(VRMLNodeType srcNode, int fieldNumber) {
        theNode=srcNode;
        fieldIndex=fieldNumber;
    }

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
     */
    public boolean isConglomerating() {
        return false;
    }

    /** Load the current field value from the underlying node and store it as the input value.
     *
     */
    private void loadInputValue() {
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        storedInputValue=(VRMLNodeType)value.nodeValue;
        storedInput=true;
    }

    /** Load the current field value from the underlying node and store it as the output value.
     *
     */
    public void loadOutputValue() {
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        storedOutputValue=nodeFactory.getSAINode((VRMLNodeType)value.nodeValue);
        storedOutput=true;
    }

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer#reset()
     */
    public void reset() {
        theNode=null;
        fieldIndex=-1;
        storedOutput=false;
        storedOutputValue=null;
    }

    /**
     * Set the node value in the given eventIn.
     * <P>
     * If the node reference passed to this method has already had the dispose
     * method called then an InvalidNodeException will be generated.
     *
     * @param value The new node reference to be used.
     *
     * @exception InvalidNodeException The node reference passed has already
     *    been disposed.
     */
    public void setValue(X3DNode value)
      throws InvalidNodeException {
    	checkWriteAccess();
    	SFNodeWrapper queuedElement=this;

        VRMLNodeType actualNode=nodeFactory.getVRMLNode(value);
        if (actualNode!=null)
        	theEventQueue.postRealizeNode(actualNode);
        // Input and output buffers do not mix
        if (storedInput || storedOutput)
            queuedElement=new SFNodeWrapper(theNode, fieldIndex, theEventQueue, nodeFactory,theEventAdapterFactory);
        queuedElement.storedInput=true;
        queuedElement.storedInputValue=actualNode;
        theEventQueue.processEvent(queuedElement);
    }
}
