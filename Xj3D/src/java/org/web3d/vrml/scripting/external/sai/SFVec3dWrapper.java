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
 * Representation of a SFVec3f field.
 * Almost a search/replace of SFVec2dWrapper with 2=>3.
 */
class SFVec3dWrapper extends BaseFieldWrapper implements SFVec3d, ExternalEvent, ExternalOutputBuffer {

    /** Default field value for null field values */
    static final double DEFAULT_FIELD_VALUE[]=new double[]{0.0,0.0,0.0};

    /** The value stored in this buffer iff storedInput */
    double storedInputValue[];

    /** The value stored in this buffer iff storedOutput */
    double storedOutputValue[];

    /** Basic constructor for wrappers without preloaded values
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     */
    SFVec3dWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAIEventAdapterFactory factory) {
        super(node,field,aQueue,factory);
        storedInputValue=new double[3];
        storedOutputValue=new double[3];
    }

    /** Constructor to use when a value needs to be preloaded
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     * @param isInput if isInput load value into storedInputValue, else load into storedOutputValue
     */
    SFVec3dWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
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
            theNode.setValue(fieldIndex,storedInputValue,3);
        } finally {
            storedInput=false;
        }
    }

    /**
     * Write the vector value to the given eventOut
     *
     * @param vec The array of vector values to be filled in where<BR>
     *    vec[0] = X<BR>
     *    vec[1] = Y<BR>
     *    vec[2] = Z
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(double[] vec) {
        if (storedOutput) {
            System.arraycopy(storedOutputValue,0,vec,0,3);
        } else {
            checkReadAccess();
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (data.doubleArrayValue==null)
                System.arraycopy(DEFAULT_FIELD_VALUE,0,vec,0,3);
            else
                System.arraycopy(data.doubleArrayValue,0,vec,0,3);
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
        if (value.doubleArrayValue==null)
            System.arraycopy(DEFAULT_FIELD_VALUE,0,storedInputValue,0,3);
        else
            System.arraycopy(value.doubleArrayValue,0,storedInputValue,0,3);
        storedInput=true;
    }

    /** Load the current field value from the underlying node and store it as the output value.
     *
     */
    public void loadOutputValue() {
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        if (value.doubleArrayValue==null)
            System.arraycopy(DEFAULT_FIELD_VALUE,0,storedOutputValue,0,3);
        else
            System.arraycopy(value.doubleArrayValue,0,storedOutputValue,0,3);
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
     * Set the vector value in the given eventIn.
     * <P>
     * The value array must contain at least two elements. If the array
     * contains more than 3 values only the first 3 values will be used and
     * the rest ignored.
     * <P>
     * If the array of values does not contain at least 3 elements an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param value The array of vector components where<BR>
     *    value[0] = X<BR>
     *    value[1] = Y<BR>
     *    value[2] = Z
     *
     * @exception ArrayIndexOutOfBoundsException The value did not contain at least two
     *    values for the vector
     */
    public void setValue(double[] value) {
        checkWriteAccess();
        SFVec3dWrapper queuedElement=this;
        // Input and output buffers do not mix
        if (storedInput || storedOutput)
            queuedElement=new SFVec3dWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
        System.arraycopy(value,0,queuedElement.storedInputValue,0,3);
        theEventQueue.processEvent(queuedElement);
    }
}
