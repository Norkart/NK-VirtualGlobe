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
 * Representation of a MFNode field.
 * <P>
 * Get the values of a node array. The java <CODE>null</CODE> reference is
 * treated to be equivalent to the VRML <CODE>NULL</CODE> field values.
 * <P>
 * It is not illegal to construct an array where some members of the array are
 * null pointers. Due to no specification on the intended result in the VRML
 * specification, the response given by the browser is implementation
 * dependent. Calls will not generate an exception, but the value of actual
 * event received from the scenegraph may vary until the issue is resolved.
 *
 */
class MFNodeWrapper extends BaseFieldWrapper
    implements MFNode, ExternalEvent, ExternalOutputBuffer {

    /** Is this the result of set1Value calls? */
    private boolean isSetOneValue;

    /** The node factory for mapping field values to SAINode values */
    private SAINodeFactory nodeFactory;

    /** The number of elements used in the input buffer */
    private int storedInputLength;

    /** The value stored in this buffer iff storedInput */
    private VRMLNodeType[] storedInputValue;

    /** The value stored in this buffer iff storedOutput */
    private X3DNode[] storedOutputValue;

    /** Basic constructor for wrappers without preloaded values
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param nodeFactory The factory instance for mapping nodes
     * @param factory The adapter factory for registering interest
     */
    MFNodeWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAINodeFactory aNodeFactory,
        SAIEventAdapterFactory factory
    ) {
        super(node,field,aQueue,factory);
        this.nodeFactory=aNodeFactory;
    }

    /** Basic constructor for wrappers without preloaded values
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param nodeFactory The factory instance for mapping nodes
     * @param factory The adapter factory for registering interest
     * @param isInput if isInput load value into storedInputValue, else load into storedOutputValue
     */
    MFNodeWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAINodeFactory nodeFactory,
        SAIEventAdapterFactory factory, boolean isInput
    ) {
        this(node,field,aQueue,nodeFactory,factory);
        if (isInput)
            loadInputValue();
        else
            loadOutputValue();
    }

    /**
     * @see org.web3d.x3d.sai.MFNode#append(float[])
     */
    public void append(X3DNode value) {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            VRMLNodeType actualNode=nodeFactory.getVRMLNode(value);
            if (actualNode!=null)
                theEventQueue.postRealizeNode(actualNode);
            MFNodeWrapper queuedElement=
                (MFNodeWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFNodeWrapper(
                            theNode,fieldIndex,theEventQueue,nodeFactory,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.ensureArraySize(queuedElement.storedInputLength+1);
            queuedElement.storedInputValue[queuedElement.storedInputLength++]=actualNode;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * @see org.web3d.x3d.sai.MFNode#clear()
     */
    public void clear() {
        setValue(0,new X3DNode[0]);
    }

    /** Post any queued field values to the target field */
    public void doEvent() {
        try {
            theNode.setValue(fieldIndex,storedInputValue,storedInputLength);
        } finally {
            if (storedInputValue!=null)
                for (int counter=0; counter<storedInputValue.length;counter++)
                    storedInputValue[counter]=null;
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
        VRMLNodeType newArray[]=new VRMLNodeType[newSize];
        if (storedInputValue!=null)
            System.arraycopy(storedInputValue,0,newArray,0,storedInputLength);
        storedInputValue=newArray;
    }

    /**
     * Get a particular node value in the given eventOut array.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated. If the Node value is
     * NULL because the browser implementation keeps null references then this
     * method will return a null pointer without throwing any exception.
     * <P>
     * @param index The position to read the values from
     * @return The node reference
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current data
     *    array bounds.
     */
    public X3DNode get1Value(int index) {
        if (storedOutput)
            return storedOutputValue[index];
        else {
            checkReadAccess();
            VRMLFieldData value=theNode.getFieldValue(fieldIndex);
            // nodeArrayValue may be null if numElements == 0
            if (index<0 || index>=value.numElements)
                throw new ArrayIndexOutOfBoundsException();
            else
                return nodeFactory.getSAINode((VRMLNodeType)value.nodeArrayValue[index]);
        }
    }

    /**
     * Write the value of the array of the nodes to the given array. Individual
     * elements in the array may be null depending on the implementation
     * of the browser and whether it maintains null references.
     *
     * @param nodes The node array to be filled in
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(X3DNode[] nodes) {
        if (storedOutput)
            System.arraycopy(storedOutputValue,0,nodes,0,storedOutputValue.length);
        else {
            checkReadAccess();
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            for (int counter=0; counter<data.numElements; counter++)
                nodes[counter]=nodeFactory.getSAINode((VRMLNodeType) data.nodeArrayValue[counter]);
        }
    }

    /** @see org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer#initialize(org.web3d.vrml.nodes.VRMLNodeType, int)
     */
    public void initialize(VRMLNodeType srcNode, int fieldNumber) {
        theNode=srcNode;
        fieldIndex=fieldNumber;
    }

    /**
     * @see org.web3d.x3d.sai.MFNode#insertValue(int, org.web3d.x3d.sai.X3DNode)
     */
    public void insertValue(int index, X3DNode value) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            VRMLNodeType actualNode=nodeFactory.getVRMLNode(value);
            if (actualNode!=null)
                theEventQueue.postRealizeNode(actualNode);
            MFNodeWrapper queuedElement=
                (MFNodeWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFNodeWrapper(
                        theNode,fieldIndex,theEventQueue,nodeFactory,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.ensureArraySize(storedInputLength+1);
            System.arraycopy(queuedElement.storedInputValue,index,queuedElement.storedInputValue,index+1,queuedElement.storedInputLength-index);
            queuedElement.storedInputValue[index]=actualNode;
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
            storedInputValue=new VRMLNodeType[value.numElements];
        for (int counter=0; counter<value.numElements; counter++)
            storedInputValue[counter]=(VRMLNodeType)(value.nodeArrayValue[counter]);
        storedInput=true;
        storedInputLength=storedInputValue.length;
    }

    /** Load the current field value from the underlying node and store it as the output value.
     *
     */
    public void loadOutputValue() {
        if(!isReadable())
            return;
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        if (storedOutputValue == null || storedOutputValue.length!=value.numElements)
            storedOutputValue=new X3DNode[value.numElements];
        for (int counter=0; counter<value.numElements; counter++)
            storedOutputValue[counter]=nodeFactory.getSAINode((VRMLNodeType) value.nodeArrayValue[counter]);
        storedOutput=true;
    }

    /**
     * @see org.web3d.x3d.sai.MFNode#removeValue(int)
     */
    public void removeValue(int index) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFNodeWrapper queuedElement=
                (MFNodeWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFNodeWrapper(
                        theNode,fieldIndex,theEventQueue,nodeFactory,theEventAdapterFactory,true
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
        if (storedOutputValue!=null)
            for (int counter=0; counter<storedOutputValue.length;counter++)
                storedOutputValue[counter]=null;
    }

    /**
     * Set a particular node value in the given eventIn array. To the VRML
     * world this will generate a full MFNode event with the nominated index
     * value changed.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated. If the Node value is
     * null the behaviour will be undefined as far as generating an event to the
     * scenegraph is concerned in order to be consistent with the behaviour
     * described in the class introduction. This method call will not generate
     * an exception if the node reference is null.
     * <P>
     * If the node reference passed to this method has already had the dispose
     * method called then an InvalidNodeException will be generated.
     *
     * @param index The position to set the colour value
     * @param value The node reference
     *
     * @exception InvalidNodeException The node has been "disposed" of
     * @exception ArrayIndexOutOfBoundsException The index was out of bounds of the
     *     array currently.
     */
    public void set1Value(int index, X3DNode value)
    throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            VRMLNodeType actualNode=nodeFactory.getVRMLNode(value);
            if (actualNode!=null)
                theEventQueue.postRealizeNode(actualNode);
            MFNodeWrapper queuedElement=
                (MFNodeWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFNodeWrapper(
                        theNode,fieldIndex,theEventQueue,nodeFactory,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.storedInputValue[index]=actualNode;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * Set the value of the array of nodes. Input is an array of valid Node
     * references. If the length is zero or the node reference is null, then
     * the actions to take are according to the class introduction above. If the
     * array contains a null reference then th resulting event passed to the
     * eventIn is implementation dependent
     * <P>
     * If any of the node references have had their dispose methods called, an
     * InvalidNodeException will be generated and no event sent to the
     * scenegraph.
     *
     * @param size The number of nodes to copy from this array
     * @param value The array of node references
     * @exception InvalidNodeException At least one node has been "disposed" of
     */
    public void setValue(int size, X3DNode[] value) {
        checkWriteAccess();
        MFNodeWrapper queuedElement=this;
        // Input and output buffers do not mix and further don't overwrite
        // set1Value calls

        if (isSetOneValue || storedInput || storedOutput) {
            queuedElement = new MFNodeWrapper(theNode, fieldIndex, theEventQueue, nodeFactory, theEventAdapterFactory);
        }

        queuedElement.storedInput=true;

        if (queuedElement.storedInputValue == null || queuedElement.storedInputValue.length != size)
            queuedElement.storedInputValue = new VRMLNodeType[size];

        for (int counter=0; counter<size; counter++) {
            VRMLNodeType actualNode=nodeFactory.getVRMLNode(value[counter]);
            if (actualNode!=null)
                theEventQueue.postRealizeNode(actualNode);
            queuedElement.storedInputValue[counter]=actualNode;
        }
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
        if (storedOutput)
            return storedOutputValue.length;
        else {
            return theNode.getFieldValue(fieldIndex).numElements;
        }
    }
}
