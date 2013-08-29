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
 * Representation of a SFRotation field.
 * <P>
 * Rotation values are specified according to the VRML IS Specification
 * Section 5.8 SFRotation and MFRotation.
 *
 */
class SFRotationWrapper extends BaseFieldWrapper implements SFRotation,
    ExternalEvent, ExternalOutputBuffer {

        /** Default field value for null field arrays */
        static final float DEFAULT_FIELD_VALUE[]=new float[]{0.0f,0.0f,1.0f,0.0f};

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
        SFRotationWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
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
        SFRotationWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
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
     * Write the rotation value to the given eventOut
     *
     * @param vec The array of vector values to be filled in where<BR>
     *    value[0] = X component [0-1] <BR>
     *    value[1] = Y component [0-1] <BR>
     *    value[2] = Z component [0-1] <BR>
     *    value[3] = Angle of rotation [-PI - PI] (nominally).
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[] vec) {
        if (storedOutput) {
            System.arraycopy(storedOutputValue,0,vec,0,4);
        } else {
        	checkReadAccess();
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (data.floatArrayValue==null)
                System.arraycopy(DEFAULT_FIELD_VALUE,0,vec,0,4);
            else
                System.arraycopy(data.floatArrayValue,0,vec,0,4);
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
     * Set the rotation value in the given eventIn.
     * <P>
     * The value array must contain at least four elements. If the array
     * contains more than 4 values only the first 4 values will be used and
     * the rest ignored.
     * <P>
     * If the array of values does not contain at least 4 elements an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param value The array of rotation values where<BR>
     *    value[0] = X component [0-1] <BR>
     *    value[1] = Y component [0-1] <BR>
     *    value[2] = Z component [0-1] <BR>
     *    value[3] = Angle of rotation [-PI - PI] (nominally).
     *
     * @exception ArrayIndexOutOfBoundsException The value did not contain at least 4
     *    values for the rotation.
     */
    public void setValue(float[] value) {
    	checkWriteAccess();
    	SFRotationWrapper queuedElement=this;
        // Input and output buffers do not mix
        if (storedInput || storedOutput)
            queuedElement=new SFRotationWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
        System.arraycopy(value,0,queuedElement.storedInputValue,0,4);
        theEventQueue.processEvent(queuedElement);
    }
}
