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

package org.web3d.vrml.scripting.external.sai;

// External imports
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;

// Local imports
import org.web3d.x3d.sai.*;
import org.web3d.util.SFImageUtils;
import org.web3d.vrml.scripting.external.buffer.*;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Representation of a MFImage field.
 * <P>
 * Images are represented as arrays of integers as per the X3D
 * specification Section 5.7 SFImage and MFImage. Pixel values are between 0
 * and 256 and represented as integers to maintain consistency with java's
 * ImageConsumer interface and PixelGrabber class.
 *
 * In the internal representation, each member of the MFImage field is
 * represented using the SFImage integer format.  In other words,
 *   <OL><LI>Image 0 width
 *       <LI>Image 0 height
 *       <LI>Image 0 components
 *       <LI>Image 0 pixels (if image size != 0)
 *       <LI>Image 1 width
 *       <LI>....
 *       <LI>Image n-1 width
 *       <LI>...
 *       <LI>Image n-1 pixels if image size != 0
 *   </OL>
 *   This does mean that finding the start of element n requires
 *   scanning through the previous n-1 image fields, and that changing
 *   image sizes causes array copying.
 *
 * Note that for MFImage field data, the number of elements in VRMLFieldData
 * is number of integer values, not the number of SFImage values.
 */
class MFImageWrapper extends MFInt32Wrapper
    implements MFImage, ExternalEvent, ExternalOutputBuffer {

    /** Is this the result of set1Value calls? */
    private boolean isSetOneValue;

    /** Has someone called been messing with the pixels directly? */
    private boolean rogueInputData;

    /** The number of elements used in the input buffer */
    private int storedInputLength;

    /** The value to be sent to the rendering system iff storedInput */
    private int[] storedInputValue;

    /** The value to be sent to the rendering system iff storedOutput */
    private int[] storedOutputValue;

    /** Basic constructor for wrappers without preloaded values
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     */
    MFImageWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
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
    MFImageWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
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
            theNode.setValue(fieldIndex,storedInputValue,storedInputLength);
        } finally {
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
     * Get the number of colour components in the image. The value will
     * always be between 1 and 4 indicating the number of components of
     * the colour specification to be read from the image pixel data.
     *
     * @param imgIndex The index of the image in the array
     * @return The number of components
     */
    public int getComponents(int imgIndex) {
        if (storedOutput) {
            if (imgIndex<0)
                throw new ArrayIndexOutOfBoundsException();
            return storedOutputValue[findStartOfImage(imgIndex,storedOutputValue)+2];
        } else {
            checkReadAccess();
            // This is inefficient
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (imgIndex<0 || imgIndex>=countActualImages(data.intArrayValue,data.numElements))
                throw new ArrayIndexOutOfBoundsException();
            return data.intArrayValue[findStartOfImage(imgIndex,data.intArrayValue)+2];
        }
    }

    /**
     * Inserts a value into an existing index of the field.  Current field values
     * from the index to the end of the field are shifted down and the field
     * length is increased by one to accomodate the new element.
     *
     * This method modifies the integer array rather than the
     * conceptual image array.
     *
     * If the index is out of the bounds of the current field an
     * ArrayIndexOutofBoundsException will be generated.
     *
     * @param index The position at which to insert
     * @param value The new element to insert
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current field
     *    size.
     */
    public void insertValue(int index, int value)
    throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFImageWrapper queuedElement=
                (MFImageWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFImageWrapper(
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
            queuedElement.rogueInputData=true;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }

    }

    /**
     * Get the height of the image.
     *
     * @param imgIndex The index of the image in the array
     * @return The height of the image in pixels
     */
    public int getHeight(int imgIndex) {
        //throw new RuntimeException("Not yet implemented");
        if (storedOutput) {
            if (imgIndex<0)
                throw new ArrayIndexOutOfBoundsException();
            return storedOutputValue[findStartOfImage(imgIndex,storedOutputValue)+1];
        } else {
            checkReadAccess();
            // This is inefficient
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (imgIndex<0 || imgIndex>=countActualImages(data.intArrayValue,data.numElements))
                throw new ArrayIndexOutOfBoundsException();
            return data.intArrayValue[findStartOfImage(imgIndex,data.intArrayValue)+1];
        }
    }

    /**
     * Fetch the Java representation of the underlying image from these pixels.
     * This is the same copy that the browser uses to generate texture
     * information from.
     *
     * @param imgIndex The index of the image in the array
     * @return The image reference representing the current state
     */
    public WritableRenderedImage getImage(int imgIndex) {
        // Formula:
        //   Copy all but the last line of getPixel
        //   Invoke the magic code to convert from SFImage to WritableRenderedImage
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * Get the image pixel value in the given eventOut.
     * <P>
     * The number of items in the pixels array will be
     * <CODE>width * height<CODE>. If there are less items than this an
     * ArrayIndexOutOfBoundsException will be generated. The integer values
     * are represented according to the number of components.
     * <P>
     * <B>1 Component Images</B><BR>
     * The integer has the intensity value stored in the lowest byte and can be
     * obtained:
     * <PRE>
     *    intensity = (pixel[i]     ) & 0xFF;
     * </PRE>
     * <P>
     * <B>2 Component Images</B><BR>
     * The integer has the transparency value stored in the lowest byte and the
     * intensity in the next byte:
     * <PRE>
     *    intensity = (pixel[i] >> 8) & 0xFF;
     *    alpha     = (pixel[i]     ) & 0xFF;
     * </PRE>
     * <P>
     * <B>3 Component Images</B><BR>
     * The three color components are stored in the integer array as follows:
     * <PRE>
     *    red   = (pixel[i] >> 16) & 0xFF;
     *    green = (pixel[i] >>  8) & 0xFF;
     *    blue  = (pixel[i]      ) & 0xFF;
     * </PRE>
     * <P>
     * <B>4 Component Images</B><BR>
     * The integer has the value stored in the array as follows:
     * <PRE>
     *    red   = (pixel[i] >> 24) & 0xFF;
     *    green = (pixel[i] >> 16) & 0xFF;
     *    blue  = (pixel[i] >>  8) & 0xFF;
     *    alpha = (pixel[i]      ) & 0xFF;
     * </PRE>
     * <P>
     * The width and height values must be greater than or equal to zero. The
     * number of components is between 1 and 4. Any value outside of these
     * bounds will generate an IllegalArgumentException.
     *
     * @param imgIndex The index of the image in the array
     * @param pixels The array to copy pixel values into
     */
    public void getPixels(int imgIndex, int[] pixels) {
        //throw new RuntimeException("Not yet implemented");
        int startOfImageData;
        int pixelSource[];
        if (storedOutput) {
            if (imgIndex<0)
                throw new ArrayIndexOutOfBoundsException();
            pixelSource=storedOutputValue;
        } else {
            checkReadAccess();
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (imgIndex<0 || imgIndex>=countActualImages(data.intArrayValue,data.numElements))
                throw new ArrayIndexOutOfBoundsException();
            pixelSource=data.intArrayValue;
        }
        startOfImageData=findStartOfImage(imgIndex,pixelSource);
        int imageSize=pixelSource[startOfImageData]*pixelSource[startOfImageData+1];
        System.arraycopy(pixelSource,startOfImageData+3,pixels,0,imageSize);
    }

    /**
     * Get the width of the image at index i in the array.
     *
     * @param imgIndex The index of the image in the array
     * @return The width of the image in pixels
     */
    public int getWidth(int imgIndex) {
        if (storedOutput) {
            if (imgIndex<0)
                throw new ArrayIndexOutOfBoundsException();
            return storedOutputValue[findStartOfImage(imgIndex,storedOutputValue)];
        } else {
            checkReadAccess();
            // This is inefficient
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (imgIndex<0 || imgIndex>=countActualImages(data.intArrayValue,data.numElements))
                throw new ArrayIndexOutOfBoundsException();
            return data.intArrayValue[findStartOfImage(imgIndex,data.intArrayValue)];
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
        return isSetOneValue;
    }

    /** Load the current field value from the underlying node and store it as the input value.
     *
     */
    private void loadInputValue() {
        if(!isReadable())
            return;
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        int requiredSize=value.numElements;
        //calculateRequiredStorage(value.numElements,value.intArrayValue);
        if (storedInputValue == null || storedInputValue.length < requiredSize)
            storedInputValue=new int[requiredSize];
        System.arraycopy(value.intArrayValue,0,storedInputValue,0,requiredSize);
        storedInput=true;
        storedInputLength=storedInputValue.length;
    }

    /** Load the current field value from the underlying node and store it as the output value.
     *
     */
    public void loadOutputValue() {
        if(!isWritable())
            return;
        //throw new RuntimeException("Not yet implemented");
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        int requiredSize=value.numElements;
        //calculateRequiredStorage(value.numElements,value.intArrayValue);
        if (storedOutputValue == null || storedOutputValue.length < requiredSize)
            storedOutputValue=new int[requiredSize];
        System.arraycopy(value.intArrayValue,0,storedOutputValue,0,requiredSize);
        storedOutput=true;
    }

    /**
     * @see org.web3d.x3d.sai.MFImage#removeValue(int)
     */
    public void removeValue(int index) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFImageWrapper queuedElement=
                (MFImageWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFImageWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            // Removing a raw byte, rather than an image value.
            if (queuedElement.storedInputLength>0) {
                if (index+1<queuedElement.storedInputLength)
                    System.arraycopy(queuedElement.storedInputValue,
                    		index+1,
                    		queuedElement.storedInputValue,
                    		index,
                    		queuedElement.storedInputLength-index-1);
                queuedElement.storedInputLength--;
                queuedElement.rogueInputData=true;
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
        rogueInputData=false;
    }

    /**
     * Set one pixel value at the correct index in this field. Basically a
     * single pixel set, but it could also be width, height, number of
     * components. This must be used with great care as it could well corrupt
     * the user data.
     *
     * @param index The position in the array to set
     * @param value The value to use at that position
     */
    public void set1Value(int index, int value)
    throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        if (value<0)
            throw new IllegalArgumentException("Pixels may not be negative.");
        if (index<0)
            throw new ArrayIndexOutOfBoundsException();
        synchronized(theEventQueue.eventLock) {
            MFImageWrapper queuedElement=
                (MFImageWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFImageWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.storedInputValue[index]=value;
            queuedElement.rogueInputData=true;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * Set the image value in the given writable field.
     * <P>
     * Image values are specified using a width, height and the number of
     * components. The number of items in the pixels array must be at least
     * <CODE>width * height<CODE>. If there are less items than this an
     * ArrayIndexOutOfBoundsException will be generated. The integer values
     * are represented according to the number of components. If the integer
     * contains values in bytes that are not used by the number of components
     * for that image, the values are ignored.
     * <P>
     * <B>1 Component Images</B><BR>
     * The integer has the intensity value stored in the lowest byte and can be
     * obtained:
     * <PRE>
     *    intensity = (pixel[i]     ) & 0xFF;
     * </PRE>
     * <P>
     * <B>2 Component Images</B><BR>
     * The integer has the transparency value stored in the lowest byte and the
     * intensity in the next byte:
     * <PRE>
     *    intensity = (pixel[i] >> 8) & 0xFF;
     *    alpha     = (pixel[i]     ) & 0xFF;
     * </PRE>
     * <P>
     * <B>3 Component Images</B><BR>
     * The three color components are stored in the integer array as follows:
     * <PRE>
     *    red   = (pixel[i] >> 16) & 0xFF;
     *    green = (pixel[i] >>  8) & 0xFF;
     *    blue  = (pixel[i]      ) & 0xFF;
     * </PRE>
     * <P>
     * <B>4 Component Images</B><BR>
     * The integer has the value stored in the array as follows:
     * <PRE>
     *    red   = (pixel[i] >> 24) & 0xFF;
     *    green = (pixel[i] >> 16) & 0xFF;
     *    blue  = (pixel[i] >>  8) & 0xFF;
     *    alpha = (pixel[i]      ) & 0xFF;
     * </PRE>
     * <P>
     * The width and height values must be greater than or equal to zero. The
     * number of components is between 1 and 4. Any value outside of these
     * bounds will generate an IllegalArgumentException.
     *
     * @param imgIndex The index of the image in the array
     * @param width The width of the image in pixels
     * @param height The height of the image in pixels
     * @param components The number of colour components [1-4]
     * @param pixels The array of pixel values as specified above.
     *
     * @exception IllegalArgumentException The number of components or width/
     *    height are illegal values.
     * @exception ArrayIndexOutOfBoundsException The number of pixels provided by the
     *    caller is not enough for the width * height.
     */
    public void set1Value(int imgIndex,
                          int width,
                          int height,
                          int components,
                          int[] pixels) {
        if (imgIndex<0)
            throw new ArrayIndexOutOfBoundsException();
        if (width<0 || height<0 || components < 0 || components > 4)
            throw new IllegalArgumentException();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFImageWrapper queuedElement=
                (MFImageWrapper) theEventQueue.getLast(this);
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    // Avoid clogging this buffer if
                    // index out of bounds
                    if (imgIndex>=storedInputValue.length)
                        throw new ArrayIndexOutOfBoundsException();
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    // If this generates an ArrayIndexOutOfBounds its okay,
                    // the element will be garbage.
                    queuedElement=new MFImageWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                queuedElement.rewriteImageToSize(imgIndex,width,height);
                int startingIndex=queuedElement.findStartOfInputImage(imgIndex);
                queuedElement.storedInputValue[startingIndex+2]=components;
                if (pixels.length!=0)
                    System.arraycopy(pixels,0,queuedElement.storedInputValue,startingIndex+3,pixels.length);
                theEventQueue.processEvent(queuedElement);
            } else {
                checkDataSanity();
                queuedElement.rewriteImageToSize(imgIndex,width,height);
                int startingIndex=queuedElement.findStartOfInputImage(imgIndex);
                queuedElement.storedInputValue[startingIndex+2]=components;
                if (pixels.length!=0)
                    System.arraycopy(pixels,0,queuedElement.storedInputValue,startingIndex+3,pixels.length);
            }
        }
    }

    /**
     * Set the image value in the given writable field to the new image defined
     * by a set of pixels.
     * <P>
     *
     * @param imgIndex The index of the image in the array
     * @param img The new image to use as the source
     */
    public void setImage(int imgIndex, RenderedImage img)
        throws InvalidOperationTimingException,
               InvalidFieldValueException,
               InvalidWritableFieldException,
               InvalidFieldException {
        if (imgIndex<0)
            throw new ArrayIndexOutOfBoundsException();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFImageWrapper queuedElement=
                (MFImageWrapper) theEventQueue.getLast(this);
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    // Avoid clogging this buffer if
                    // index out of bounds
                    if (imgIndex<0)
                        throw new ArrayIndexOutOfBoundsException();
                    loadInputValue();
                    if (imgIndex>=storedInputValue.length)
                        throw new ArrayIndexOutOfBoundsException();
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    // If this generates an ArrayIndexOutOfBounds its okay,
                    // the element will be garbage.
                    queuedElement=new MFImageWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                queuedElement.rewriteImageToSize(imgIndex,img.getWidth(),img.getHeight());
                SFImageUtils.convertRenderedImageToData(img,queuedElement.storedInputValue,queuedElement.findStartOfInputImage(imgIndex));
                theEventQueue.processEvent(queuedElement);
            } else {
                checkDataSanity();
                queuedElement.rewriteImageToSize(imgIndex,img.getWidth(),img.getHeight());
                SFImageUtils.convertRenderedImageToData(img,queuedElement.storedInputValue,queuedElement.findStartOfInputImage(imgIndex));
                queuedElement.getSize();
            }
        }
    }

    /**
     * Set the given writable field to the new array of image values.
     * <P>
     *
     * @param img The new images to use as the source
     */
    public void setImage(RenderedImage[] img)
        throws InvalidOperationTimingException,
               InvalidFieldValueException,
               InvalidWritableFieldException,
               InvalidFieldException {
        checkWriteAccess();
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * Copy a region of the argument RenderedImage to replace a portion of the
     * current SFimage.
     * <P>
     * The sub image set shall not resize the base image representation and
     * therefore performs an intersection clip of the provided image. The user
     * provided image shall be of the same format (pixel depth, pixel
     * representation) as the original image obtained through the getImage()
     * method.
     * <p>
     * RenderedImages are row order from top to bottom. A
     * 4x8 RenderImage is indexed as follows:
     *
     * <PRE>
     *
     * X >01234567
     *   ----------
     * 0 |********|
     * 1 |********|
     * 2 |********|
     * 3 |********|
     * ^ ----------
     * Y
     *
     * </PRE>
     *
     * SFImages are row order from bottom to top. A
     * 4x8 RenderImage is indexed as follows:
     *
     * <PRE>
     *
     * X >01234567
     *   ----------
     * 3 |********|
     * 2 |********|
     * 1 |********|
     * 0 |********|
     * ^ ----------
     * Y
     *
     * </PRE>
     *
     * <p>
     * Note: The parameter srcYOffset is referenced to the RenderedImage object
     * (indexed top to bottom).
     * <br>
     * The parameter destYOffset is referenced to the SFImage object
     * (indexed bottom to top).
     *
     * @param imgIndex The index of the image in the array
     * @param img The new image to use as the source
     * @param srcWidth The width of the argument sub-image region to copy
     * @param srcHeight The height of the argument sub-image region to copy
     * @param srcXOffset The initial x dimension (width) offset into the
     * argument sub-image that begins the region to copy
     * @param srcYOffset The initial y dimension (height) offset into the
     * argument sub-image that begins the region to copy
     * @param destXOffset The initial x dimension (width) offset in the SFimage
     * object that begins the region to receive the copy
     * @param destYOffset The initial y dimension (height) offset in the SFimage
     * object that begins the region to receive the copy
     */
    public void setSubImage(int imgIndex,
                            RenderedImage img,
                            int srcWidth,
                            int srcHeight,
                            int srcXOffset,
                            int srcYOffset,
                            int destXOffset,
                            int destYOffset)
        throws InvalidOperationTimingException,
               InvalidFieldValueException,
               InvalidWritableFieldException,
               InvalidFieldException {
        checkWriteAccess();
        if (imgIndex<0)
            throw new ArrayIndexOutOfBoundsException();
        synchronized(theEventQueue.eventLock) {
            MFImageWrapper queuedElement=
                (MFImageWrapper) theEventQueue.getLast(this);
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    // Avoid clogging this buffer if
                    // index out of bounds
                    if (imgIndex>=storedInputValue.length)
                        throw new ArrayIndexOutOfBoundsException();
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                    // TODO: Insert image values
                } else {
                    // If this generates an ArrayIndexOutOfBounds its okay,
                    // the element will be garbage.
                    queuedElement=new MFImageWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                    // TODO: Insert image values
                }
                theEventQueue.processEvent(queuedElement);
            } else {
                checkDataSanity();
                // TODO: Insert image values
            }
        }
    }

    /**
     * Set the value of the array to this value. The values are raw integers and
     * are to be interpreted according to the rules of the MFImage field. So the
     * first three values of the array will be width, height, depth and then
     * the appropriate number of pixels, more width height depth etc.
     *
     * @param value The value to use
     * @exception IllegalArgumentException The number of components or width/
     *    height are illegal values.
     * @exception ArrayIndexOutOfBoundsException The number of pixels provided by the
     *    caller is not enough for the width * height.
     */
    public void setValue(int[] value) {
        checkWriteAccess();
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
            return countActualImages(storedOutputValue,storedOutputValue.length);
        } else if (storedInput) {
            return countActualImages(storedInputValue,storedInputValue.length);
        }else {
            VRMLFieldData value=theNode.getFieldValue(fieldIndex);
            return countActualImages(value.intArrayValue,value.numElements);
        }
    }

    /** Calculate how many images are actually in the data
     * @param rawData
     * @param dataSize
     * @return
     */
    int countActualImages(int rawData[], int dataSize) {
        if (rawData==null)
            return 0;
        int imageCounter=0;
        int counter;
        for (counter=0; counter<dataSize; counter=counter+rawData[counter]*rawData[counter+1]+3)
            imageCounter++;
        if (counter!=rawData.length)
            throw new RuntimeException("Corrupted MFImage field.");
        return imageCounter;
    }

    /** Find out how much of the field array is actually being used.
     * The target array is a sequence of SFImage integer data, and there
     * may be padding on the end.  This method calculates how much
     * is actually used.
     * @param numFields
     * @param rawData
     * @return The size actually used
     */
    int calculateRequiredStorage(int numFields, int rawData[]) {
        int actualSize=0;
        for (int fieldCounter=0; fieldCounter<numFields; fieldCounter++) {
            int currentImageSize=rawData[actualSize]*rawData[actualSize+1]+3;
            actualSize+=currentImageSize;
        }
        return actualSize;
    }

    /** Utility method for calculating where in the raw array
     *  an image's data actually starts
     * @param index The index of the image
     * @param rawData The raw image sequence data
     * @return The index in rawData where the image starts
     */
    static int findStartOfImage(int index, int rawData[]) {
        int startOfImage=0;
        while (index>0) {
            int sizeOfImage=rawData[startOfImage]*rawData[startOfImage+1]+3;
            startOfImage+=sizeOfImage;
            index--;
        }
        return startOfImage;
    }

    int findStartOfInputImage(int index) {
        return MFImageWrapper.findStartOfImage(index,storedInputValue);
    }

    /** Determine whether the image data is corrupted.  Throws a runtime
     *  exception if this is true.  Clears rogueInputData otherwise.
     */
    void checkDataSanity() {
        if (!rogueInputData)
            return;
        try {
            int counter;
            for (counter = 0; counter < storedInputValue.length; counter++)
                if (storedInputValue[counter] < 0)
                    throw new RuntimeException(
                            "Corrupted data generated by user");
            for (counter = 0; counter < storedInputValue.length;)
                counter += storedInputValue[counter]
                        * storedInputValue[counter] + 3;
            if (counter != storedInputValue.length)
                throw new RuntimeException("Corrupted data generated by user");
            rogueInputData = false;
        } catch (ArrayIndexOutOfBoundsException aio) {
            throw new RuntimeException("Corrupted data generated by user");
        }
    }

    /** Magical method to make room for a new image to replace a current one.
     *  The newly reallocated image is not validly formatted.
     *  Replaces storedInputValue with a new array.
     * */
    void rewriteImageToSize(int imageIndex, int newWidth, int newHeight) {
        int startOfImage=findStartOfImage(imageIndex,storedInputValue);
        int newSize=newWidth*newHeight+3;
        int oldSize=storedInputValue[startOfImage]*storedInputValue[startOfImage+1]+3;
        int newArray[]=new int[storedInputValue.length-oldSize+newSize];
        System.arraycopy(storedInputValue,0,newArray,0,startOfImage);
        System.arraycopy(storedInputValue,startOfImage+oldSize,newArray,startOfImage+newSize,storedInputValue.length-startOfImage-oldSize);
        storedInputValue=newArray;
        //throw new RuntimeException("Not yet implemented");
    }
}
