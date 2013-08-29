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
 * Representations of a MFTime field.
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
 */
class MFTimeWrapper extends BaseFieldWrapper
    implements MFTime, ExternalEvent, ExternalOutputBuffer {

    /** Is this the result of set1Value calls? */
    private boolean isSetOneValue;

    /** The number of elements used in the input buffer */
    private int storedInputLength;

    /** The value stored in this buffer iff storedInput */
    private double[] storedInputValue;

    /** The value stored in this buffer iff storedOutput */
    private double[] storedOutputValue;

    /** Basic constructor for wrappers without preloaded values
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     */
    MFTimeWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
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
    MFTimeWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAIEventAdapterFactory factory, boolean isInput) {
            this(node,field,aQueue,factory);
            if (isInput)
                loadInputValue();
            else
                loadOutputValue();
        }

    /**
     * @see org.web3d.x3d.sai.MFTime#append(float[])
     */
    public void append(double value) {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFTimeWrapper queuedElement=
                (MFTimeWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFTimeWrapper(
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
     * @see org.web3d.x3d.sai.MFTime#clear()
     */
    public void clear() {
        setValue(0,new double[0]);
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
        double newArray[]=new double[newSize];
        if (storedInputValue!=null)
            System.arraycopy(storedInputValue,0,newArray,0,storedInputLength);
        storedInputValue=newArray;
    }

    /**
     * Get a particular time value in the given eventOut array.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to get the time value
     * @return The time value.
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside of the bounds of
     * the current array.
     */
    public double get1Value(int index) {
        if (storedOutput)
            return storedOutputValue[index];
        else {
            checkReadAccess();
            VRMLFieldData value=theNode.getFieldValue(fieldIndex);
            // doubleArrayValue may be null if numElements == 0
            if (index<0 || index>=value.numElements)
                throw new ArrayIndexOutOfBoundsException();
            else
                return value.doubleArrayValue[index];
        }
    }

    /**
     * Write the value of the event out to the given array.
     *
     * @param vec The array to be filled in where
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(double[] vec) {
        if (storedOutput)
            System.arraycopy(storedOutputValue,0,vec,0,storedOutputValue.length);
        else {
            checkReadAccess();
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (data.numElements!=0)
                System.arraycopy(data.booleanArrayValue,0,vec,0,data.numElements);
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
     * @see org.web3d.x3d.sai.MFTime#insertValue(int, boolean)
     */
    public void insertValue(int index, double value) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFTimeWrapper queuedElement=
                (MFTimeWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFTimeWrapper(
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

    /** Load the current field value from the underlying node and store it as the input value.
     *
     */
    private void loadInputValue() {
        if(!isReadable())
            return;
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        if (storedInputValue == null || storedInputValue.length!=value.numElements)
            storedInputValue=new double[value.numElements];
        if (value.numElements!=0)
            System.arraycopy(value.doubleArrayValue,0,storedInputValue,0,value.numElements);
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
            storedOutputValue=new double[value.numElements];
        if (value.numElements!=0)
            System.arraycopy(value.doubleArrayValue,0,storedOutputValue,0,value.numElements);
        storedOutput=true;
    }

    /**
     * @see org.web3d.x3d.sai.MFTime#removeValue(int)
     */
    public void removeValue(int index) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFTimeWrapper queuedElement=
                (MFTimeWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFTimeWrapper(
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
     * Set a particular time value in the given eventIn array. To the VRML
     * world this will generate a full MFTime event with the nominated index
     * value changed.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the time value
     * @param value The time value to set.
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside of the bounds of
     * the current array.
     */
    public void set1Value(int index, double value)
    throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized (theEventQueue.eventLock) {
            MFTimeWrapper queuedElement =
                (MFTimeWrapper) theEventQueue.getLast(this);
            boolean newEvent=true;
            if (queuedElement == null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement = this;
                    loadInputValue();
                    isSetOneValue = true;
                } else {
                    queuedElement =
                        new MFTimeWrapper(
                            theNode,
                            fieldIndex,
                            theEventQueue,
                            theEventAdapterFactory,
                            true);
                    queuedElement.isSetOneValue = true;
                }
            }
            queuedElement.storedInputValue[index] = value;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * Set the value of the array of times. Time values are not required to
     * conform to any range checks.
     *
     * @param size The number of items to be copied from the array
     * @param value The array of time values
     */
    public void setValue(int size, double[] value) {
        checkWriteAccess();
        MFTimeWrapper queuedElement=this;
        // Input and output buffers do not mix and don't overwrite
        // set1Value calls.
        if (isSetOneValue || storedInput || storedOutput) {
            queuedElement=new MFTimeWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
        }
        queuedElement.storedInput=true;
        if (queuedElement.storedInputValue==null || queuedElement.storedInputValue.length!=size)
            queuedElement.storedInputValue=new double[size];
        System.arraycopy(value,0,queuedElement.storedInputValue,0,size);
        queuedElement.storedInputLength=size;
        theEventQueue.processEvent(queuedElement);
    }

    /**
     * Set the value of the array of times based on Java time values. Time values
     * are not required to conform to any range checks.
     *
     * @param size The number of items to be copied from the array
     * @param value The array of time values
     */
    public void setValue(int size, long[] value) {
        checkWriteAccess();
        //ToDo: Figure out whether this method is supposed to be here.
        throw new RuntimeException("Not yet implemented");
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
