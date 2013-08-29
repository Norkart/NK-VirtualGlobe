package org.web3d.vrml.scripting.external.sai;

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

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEvent;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer;

import org.web3d.x3d.sai.InvalidReadableFieldException;
import org.web3d.x3d.sai.InvalidWritableFieldException;
import org.web3d.x3d.sai.SFInt32;

/**
 * Representation of a SFInt32 field.
 * Search/replace from SFBoolWrapper.
 *
 */
class SFInt32Wrapper extends BaseFieldWrapper implements SFInt32, ExternalEvent, ExternalOutputBuffer {

	/** The value stored in this buffer iff storedInput */
	int storedInputValue;

	/** The value stored in this buffer iff storedOutput */
	int storedOutputValue;

	/** Basic constructor for wrappers without preloaded values
	 * @param node The underlying Xj3D node
	 * @param field The field on the underlying node
	 * @param aQueue The event queue to send events to
	 * @param factory The adapter factory for registering interest
	 */
	SFInt32Wrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
		SAIEventAdapterFactory factory
	) {
		super(node,field,aQueue,factory);
	}

	/** Constructor to use when a value needs to be preloaded
	 * @param node The underlying Xj3D node
	 * @param field The field on the underlying node
	 * @param aQueue The event queue to send events to
	 * @param factory The adapter factory for registering interest
	 * @param isInput if isInput load value into storedInputValue, else load into storedOutputValue
	 */
	SFInt32Wrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
		SAIEventAdapterFactory factory, boolean isInput
	) {
		this(node,field,aQueue,factory);
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
			storedInput=false;
		}
	}

	/**
	 * Get the value in the given eventOut.
	 * <P>
	 * @return The int value of the eventOut
	 */
	public int getValue() {
		if (storedOutput)
			return storedOutputValue;
		else {
			checkReadAccess();
			VRMLFieldData data=theNode.getFieldValue(fieldIndex);
			return data.intValue;
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
		storedInputValue=value.intValue;
		storedInput=true;
	}

	/** Load the current field value from the underlying node and store it as the output value.
	 * 
	 */
	public void loadOutputValue() {
		VRMLFieldData value=theNode.getFieldValue(fieldIndex);
		storedOutputValue=value.intValue;
		storedOutput=true;
	}

	/** 
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer#reset()
	 */
	public void reset() {
        theNode=null;
        fieldIndex=-1;
        storedOutput=false;
	}

	/**
	 * Set the value in the given eventIn.
	 * <P>
	 * @param value The int value to set the eventIn to.
	 */
	public void setValue(int value) {
		checkWriteAccess();
 		SFInt32Wrapper queuedElement=this;
		// Input and output buffers do not mix
		if (storedInput || storedOutput)
			queuedElement=new SFInt32Wrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
		queuedElement.storedInput=true;
		queuedElement.storedInputValue=value;
		theEventQueue.processEvent(queuedElement);
	}
}
