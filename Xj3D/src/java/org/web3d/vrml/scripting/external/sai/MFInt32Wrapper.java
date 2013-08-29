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
 * Representation of a MFInt32 field.
 *
 */
class MFInt32Wrapper extends BaseFieldWrapper
    implements MFInt32, ExternalEvent, ExternalOutputBuffer {

    /** Is this the result of set1Value calls? */
    private boolean isSetOneValue;

    /** The number of elements used in the input buffer */
    private int storedInputLength;

    /** The value stored in this buffer iff storedInput */
    private int[] storedInputValue;

    /** The value stored in this buffer iff storedOutput */
    private int[] storedOutputValue;

    /** Basic constructor for wrappers without preloaded values
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     */
    MFInt32Wrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
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
    MFInt32Wrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAIEventAdapterFactory factory, boolean isInput) {
            this(node,field,aQueue,factory);
            if (isInput)
                loadInputValue();
            else
                loadOutputValue();
        }

    /**
     * @see org.web3d.x3d.sai.MFInt32#append(int)
     */
    public void append(int value) {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFInt32Wrapper queuedElement=
                (MFInt32Wrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFInt32Wrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.ensureArraySize(queuedElement.storedInputLength+1);
            queuedElement.storedInputValue[queuedElement.storedInputLength++]=value;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * @see org.web3d.x3d.sai.MFInt32#clear()
     */
    public void clear() {
        setValue(0,new int[0]);
    }

    /** Post any queued field values to the target field */
    public void doEvent() {
        try {
           theNode.setValue(fieldIndex,storedInputValue,storedInputLength);
       } finally {
           isSetOneValue=false;
           storedInput=false;
       }
   }

    /** Ensures that there is atleast a certain number of elements
     *  in the storedInputValue array.
     * @param newSize The size to ensure.
     */
    protected void ensureArraySize(int newSize) {
        if (storedInputValue!=null && newSize<storedInputValue.length)
            return;
        int newArray[]=new int[newSize];
        if (storedInputValue!=null)
            System.arraycopy(storedInputValue,0,newArray,0,storedInputLength);
        storedInputValue=newArray;
    }

    /**
     * Get a particular value from the eventOut array.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to be retrieved
     * @return The value at that position
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current data
     *    array bounds.
     */
    public int get1Value(int index)
        throws ArrayIndexOutOfBoundsException {
            if (storedOutput)
                return storedOutputValue[index];
            else {
                checkReadAccess();
                VRMLFieldData value=theNode.getFieldValue(fieldIndex);
                // intArrayValue may be null if numElements == 0
                if (index<0 || index>=value.numElements)
                    throw new ArrayIndexOutOfBoundsException();
                else
                    return value.intArrayValue[index];
            }
    }

    /**
     * Write the value of the array of the ints to the given array.
     *
     * @param values The array to be filled in
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(int[] values) {
        if (storedOutput)
            System.arraycopy(storedOutputValue,0,values,0,storedOutputValue.length);
        else {
            checkReadAccess();
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (data.numElements!=0)
                System.arraycopy(data.intArrayValue,0,values,0,data.numElements);
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
     * @see org.web3d.x3d.sai.MFInt32#insertValue(int, int)
     */
    public void insertValue(int index, int value) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFInt32Wrapper queuedElement=
                (MFInt32Wrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFInt32Wrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.ensureArraySize(queuedElement.storedInputLength+1);
            System.arraycopy(queuedElement.storedInputValue,index,queuedElement.storedInputValue,index+1,queuedElement.storedInputLength-index);
            queuedElement.storedInputValue[index]=value;
            queuedElement.storedInputLength++;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
     */
    public boolean isConglomerating() {
        return isSetOneValue;
    }

    private void loadInputValue() {
        if(!isReadable())
            return;
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        if (storedInputValue == null || storedInputValue.length!=value.numElements)
            storedInputValue=new int[value.numElements];
        if (value.numElements!=0)
            System.arraycopy(value.intArrayValue,0,storedInputValue,0,value.numElements);
        storedInput=true;
        storedInputLength=storedInputValue.length;
    }

    /** Load the current field value from the underlying node and store it as the output value.
     *
     */
    public void loadOutputValue() {
        if(!isWritable())
            return;
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        if (storedOutputValue == null || storedOutputValue.length!=value.numElements)
            storedOutputValue=new int[value.numElements];
        if (value.numElements!=0)
            System.arraycopy(value.intArrayValue,0,storedOutputValue,0,value.numElements);
        storedOutput=true;
    }

    /**
     * @see org.web3d.x3d.sai.MFInt32#removeValue(int)
     */
    public void removeValue(int index) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFInt32Wrapper queuedElement=
                (MFInt32Wrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFInt32Wrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            if (queuedElement.storedInputLength>0) {
                if (index+1<queuedElement.storedInputLength)
                    System.arraycopy(queuedElement.storedInputValue,
                    		index+1,
                    		queuedElement.storedInputValue,
                    		index,
                    		queuedElement.storedInputLength-index-1);
                queuedElement.storedInputLength--;
                if (newEvent)
                    theEventQueue.processEvent(queuedElement);
            } else {
                // Free up the buffer before throwing the exception
                if (newEvent)
                    queuedElement.isSetOneValue=false;
                throw new ArrayIndexOutOfBoundsException();
            }
        }
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
     * Set a particular value in the given eventIn array. To the VRML
     * world this will generate a full MFInt32 event with the nominated index
     * value changed.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the colour value
     * @param value The value to be set
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the colour component
     */
    public void set1Value(int index, int value)
    throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFInt32Wrapper queuedElement=
                (MFInt32Wrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFInt32Wrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.storedInputValue[index]=value;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * Set the value of the array of integers. If the value array is length zero
     * this is equivalent of clearing the field.
     *
     * @param size The number of items to copy from this array
     * @param value The array of values to be set
     */
    public void setValue(int size, int[] value) {
        checkWriteAccess();
        MFInt32Wrapper queuedElement=this;
        // Input and output buffers do not mix and further don't overwrite
        // set1Value calls
        if (isSetOneValue || storedInput || storedOutput) {
            queuedElement=new MFInt32Wrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
        }
        queuedElement.storedInput=true;
        if (queuedElement.storedInputValue==null || queuedElement.storedInputValue.length!=size)
            queuedElement.storedInputValue=new int[size];
        System.arraycopy(value,0,queuedElement.storedInputValue,0,size);
        queuedElement.storedInputLength=size;
        theEventQueue.processEvent(queuedElement);
    }

    /**
     * Get the size of the underlying data array. The size is the number of
     * elements for that data type. So for an MFFloat the size would be the
     * number of float values, but for an MFVec3f, it is the number of vectors
     * in the returned array (where a vector is 3 consecutive array indexes in
     * a flat array).
     *
     * @return The number of elements in this field
     */
    public int getSize() {
        if (storedOutput) {
            return storedOutputValue.length;
        } else {
            return theNode.getFieldValue(fieldIndex).numElements;
        }
    }
}
