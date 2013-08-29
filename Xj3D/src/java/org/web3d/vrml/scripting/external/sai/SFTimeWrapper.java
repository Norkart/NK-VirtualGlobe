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

import org.web3d.x3d.sai.*;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

/**
 * Representation of a SFTime field.
 * <P>
 * Time values are represented as per the VRML IS specification Section
 * 4.11 Time. That is, time is set as VRML "Time" - the number of seconds since
 * Jan 1, 1970 GMT, rather than a Java time which is a long, the number
 * of milliseconds since Jan 1, 1970 GMT. To convert between the two simply
 * divide java time by 1000 and cast to a double.
 * <P>
 * Note that in setting time values from an external application, the idea of
 * the time that java represents and the time that the VRML world currently
 * has set may well be different. It is best to source the current "time" from
 * a node or eventOut in the VRML world rather than relying exclusively on
 * the value returned from <CODE>System.currentTimeMillies</CODE>. This is
 * especially important to note if you are dealing with high speed, narrow
 * interval work such as controlling animation.
 *
 * @version 1.0 30 April 1998
 */
class SFTimeWrapper extends BaseFieldWrapper implements SFTime, ExternalEvent, ExternalOutputBuffer {

	/** The value stored in this buffer iff storedInput */
	double storedInputValue;

	/** The value stored in this buffer iff storedOutput */
	double storedOutputValue;

	/** Basic constructor for wrappers without preloaded values
	 * @param node The underlying Xj3D node
	 * @param field The field on the underlying node
	 * @param aQueue The event queue to send events to
	 * @param factory The adapter factory for registering interest
	 */
	SFTimeWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
		SAIEventAdapterFactory factory) {
		super(node,field,aQueue,factory);
	}

	/** Constructor to use when a value needs to be preloaded
	 * @param node The underlying Xj3D node
	 * @param field The field on the underlying node
	 * @param aQueue The event queue to send events to
	 * @param factory The adapter factory for registering interest
	 * @param isInput if isInput load value into storedInputValue, else load into storedOutputValue
	 */
	SFTimeWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
		SAIEventAdapterFactory factory, boolean isInput) {
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
	 * Ge the double value in the given eventOut.
	 *
	 * @return The double value to of the eventOut
	 */
	public double getValue() {
		if (storedOutput)
			return storedOutputValue;
		else {
			checkReadAccess();
			VRMLFieldData data=theNode.getFieldValue(fieldIndex);
			return data.doubleValue;
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
		storedInputValue=value.doubleValue;
		storedInput=true;
	}

	/** Load the current field value from the underlying node and store it as the output value.
	 * 
	 */
	public void loadOutputValue() {
		VRMLFieldData value=theNode.getFieldValue(fieldIndex);
		storedOutputValue=value.doubleValue;
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
	 * Set the double value in the given eventIn.
	 *
	 * @param value The array of double value to set.
	 */
	public void setValue(double value) {
		checkWriteAccess();
		SFTimeWrapper queuedElement=this;
		// Input and output buffers do not mix
		if (storedInput || storedOutput)
			queuedElement=new SFTimeWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
		queuedElement.storedInput=true;
		queuedElement.storedInputValue=value;
		theEventQueue.processEvent(queuedElement);
	}
}
