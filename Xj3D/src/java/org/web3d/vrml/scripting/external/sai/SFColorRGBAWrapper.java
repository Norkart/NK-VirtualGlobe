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
 * Representation of a SFColorRGBA field.
 * <P>
 * Colour values are represented as floating point numbers between [0 - 1]
 * as per the VRML IS specification Section 4.4.5 Standard units and
 * coordinate system.
 *
 */
class SFColorRGBAWrapper extends BaseFieldWrapper implements SFColorRGBA,
ExternalEvent, ExternalOutputBuffer {

    /** Default field value for use when field value is null */
    static final float DEFAULT_FIELD_VALUE[]=new float[]{0.0f,0.0f,0.0f,0.0f};

    /** The value stored in this buffer iff storedInput */
    float storedInputValue[];

    /** The value stored in this buffer iff storedOutput */
    float storedOutputValue[];

    /** Basic constructor for wrappers without preloaded values
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     */
    SFColorRGBAWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAIEventAdapterFactory factory) {
        super(node,field,aQueue,factory);
        storedInputValue=new float[4];
        storedOutputValue=new float[4];
    }

    /** Constructor to use when a value needs to be preloaded
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     * @param isInput if isInput load value into storedInputValue, else load into storedOutputValue
     */
    SFColorRGBAWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
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
            theNode.setValue(fieldIndex,storedInputValue,4);
        } finally {
            storedInput=false;
        }
    }

    /**
     * Write the value of the colour to the given array.
     *
     * @param col The array of colour values to be filled in where<BR>
     *    value[0] = Red component [0-1] <BR>
     *    value[1] = Green component [0-1] <BR>
     *    value[2] = Blue component [0-1] <BR>
     *    value[3] = Alpha component [0-1] <BR>
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[] col) {
        if (storedOutput) {
            System.arraycopy(storedOutputValue,0,col,0,4);
        } else {
        	checkReadAccess();
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (data.floatArrayValue==null)
                System.arraycopy(DEFAULT_FIELD_VALUE,0,col,0,4);
            else
                System.arraycopy(data.floatArrayValue,0,col,0,4);
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
        if (value.floatArrayValue==null)
            System.arraycopy(DEFAULT_FIELD_VALUE,0,storedInputValue,0,4);
        else
            System.arraycopy(value.floatArrayValue,0,storedInputValue,0,4);
        storedInput=true;
    }

    /** Load the current field value from the underlying node and store it as the output value.
     *
     */
    public void loadOutputValue() {
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        if (value.floatArrayValue==null)
            System.arraycopy(DEFAULT_FIELD_VALUE,0,storedOutputValue,0,4);
        else
            System.arraycopy(value.floatArrayValue,0,storedOutputValue,0,4);
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
     * Set the colour value in the given eventIn.  Colour values are required
     * to be in the range [0-1].
     * <P>
     * The value array must contain at least three elements. If the array
     * contains more than 4 values only the first three values will be used and
     * the rest ignored.
     * <P>
     * If the array of values does not contain at least 3 elements an
     * ArrayIndexOutOfBoundsException will be generated. If the colour values are
     * out of range an IllegalArgumentException will be generated.
     *
     * @param value The array of colour values where<BR>
     *    value[0] = Red component [0-1] <BR>
     *    value[1] = Green component [0-1] <BR>
     *    value[2] = Blue component [0-1] <BR>
     *    value[3] = Alpha component [0-1] <BR>
     *
     * @exception IllegalArgumentException A colour value(s) was out of range
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the colour component
     */
    public void setValue(float[] value) {
    	checkWriteAccess();
        SFColorRGBAWrapper queuedElement=this;
        // Input and output buffers do not mix
        if (storedInput || storedOutput)
            queuedElement=new SFColorRGBAWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
        System.arraycopy(value,0,queuedElement.storedInputValue,0,4);
        theEventQueue.processEvent(queuedElement);
    }
}
