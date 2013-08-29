/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.external.sai;

// External imports
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;

// Local imports
import org.web3d.util.SFImageUtils;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.scripting.external.buffer.*;

import org.web3d.x3d.sai.*;


/**
 * Representation of a SFImage field.
 * <P>
 * Images are represented as arrays of integers as per the VRML IS
 * specification Section 5.5 SFImage. Pixel values are between 0 and 256 and
 * represented as integers to maintain consistency with java's ImageConsumer
 * interface and PixelGrabber class.
 *
 * @author Brad Vendor
 * @version $Revision: 1.14 $
 */
class SFImageWrapper extends BaseFieldWrapper implements SFImage, ExternalEvent, ExternalOutputBuffer
{
	
	/** Is this a setSubImage composite? */
	boolean isSetOneValue;
	
	/** The value stored in this buffer iff storedInput */
	int storedInputValue[];
	
	/** The value stored in this buffer iff storedOutput */
	int storedOutputValue[];
	
	/** Basic constructor for wrappers without preloaded values
	 * @param node The underlying Xj3D node
	 * @param field The field on the underlying node
	 * @param aQueue The event queue to send events to
	 * @param factory The adapter factory for registering interest
	 */
	SFImageWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
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
	SFImageWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
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
			theNode.setValue(fieldIndex,storedInputValue,storedInputValue.length);
		} finally {
			storedInput=false;
			isSetOneValue=false;
		}
	}
	
	/**
	 * Get the number of colour components in the image. The value will
	 * always be between 1 and 4 indicating the number of components of
	 * the colour specification to be read from the image pixel data.
	 *
	 * @return The number of components
	 */
	public int getComponents() {
		if (storedOutput)
			return storedOutputValue[2];
		else {
			checkReadAccess();
			VRMLFieldData imageData=theNode.getFieldValue(fieldIndex);
			if (imageData.intArrayValue!=null)
				return imageData.intArrayValue[2];
			else return 0;
		}
	}
	
	/**
	 * Get the height of the image.
	 *
	 * @return The height of the image in pixels
	 */
	public int getHeight() {
		if (storedOutput)
			return storedOutputValue[1];
		else {
			checkReadAccess();
			VRMLFieldData imageData=theNode.getFieldValue(fieldIndex);
			if (imageData.intArrayValue!=null)
				return imageData.intArrayValue[1];
			else return 0;
		}
	}
	
	/**
	 * Fetch the Java representation of the underlying image from these pixels.
	 * This is the same copy that the browser uses to generate texture
	 * information from.
	 *
	 * @return The image reference representing the current state
	 */
	public WritableRenderedImage getImage() {
		checkReadAccess();
		
		int width = getWidth( );
		int height = getHeight( );
		int components = getComponents( );
		int pixels[] = new int[width*height];
		getPixels( pixels );
		
		return( SFImageUtils.convertDataToRenderedImage( width, height, components, pixels ) );
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
	 * @param pixels The array to copy pixel values into
	 */
	public void getPixels(int[] pixels) {
		if (storedOutput) {
			int numPixels=storedOutputValue[0]*storedOutputValue[1];
			if (numPixels>0)
				System.arraycopy(storedOutputValue,3,pixels,0,numPixels);
		} else {
			checkReadAccess();
			VRMLFieldData imageData=theNode.getFieldValue(fieldIndex);
			if (imageData.intArrayValue!=null) {
				int numPixels=imageData.intArrayValue[0]*imageData.intArrayValue[1];
				if (numPixels>0)
					System.arraycopy(imageData.intArrayValue,3,pixels,0,numPixels);
			}
		}
	}
	
	/**
	 * Get the width of the image.
	 *
	 * @return The width of the image in pixels
	 */
	public int getWidth() {
		if (storedOutput)
			return storedOutputValue[0];
		else {
			checkReadAccess();
			VRMLFieldData imageData=theNode.getFieldValue(fieldIndex);
			if (imageData.intArrayValue!=null)
				return imageData.intArrayValue[0];
			else return 0;
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
		if (storedInputValue == null || storedInputValue.length < value.numElements)
			storedInputValue=new int[value.numElements];
		System.arraycopy(value.intArrayValue,0,storedInputValue,0,value.numElements);
		storedInput=true;
	}
	
	/** Load the current field value from the underlying node and store it as the output value.
	 *
	 */
	public void loadOutputValue() {
		VRMLFieldData imageData=theNode.getFieldValue(fieldIndex);
		int newWidth=imageData.intArrayValue[0];
		int newHeight=imageData.intArrayValue[1];
		if (storedOutputValue == null || storedOutputValue.length < imageData.numElements) {
			storedOutputValue=new int[imageData.numElements];
		}
		System.arraycopy(imageData.intArrayValue,0,storedOutputValue,0,imageData.numElements);
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
	 * Set the image value in the given writable field to the new image defined
	 * by a set of pixels.
	 * <P>
	 *
	 * @param img The new image to use as the source
	 */
	public void setImage(RenderedImage img)
		throws InvalidOperationTimingException,
	InvalidFieldValueException,
	InvalidWritableFieldException,
	InvalidFieldException {
		checkWriteAccess();
		// If only there were a way of directly accessing the pixel texture.
		// Alas there is only setFieldValue(int,int[]);
		int newSize=img.getHeight()*img.getWidth()+3;
		SFImageWrapper queuedElement=this;
		if (queuedElement.storedInput)
			queuedElement=new SFImageWrapper(theNode,fieldIndex,theEventQueue,theEventAdapterFactory);
		if (queuedElement.storedInputValue==null || queuedElement.storedInputValue.length!=newSize)
			queuedElement.storedInputValue=new int[newSize];
		SFImageUtils.convertRenderedImageToData(img,queuedElement.storedInputValue,0);
		queuedElement.storedInput=true;
		theEventQueue.processEvent(queuedElement);
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
	public void setSubImage(RenderedImage img,
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
		synchronized (theEventQueue.eventLock) {
			SFImageWrapper queuedElement=
				(SFImageWrapper) theEventQueue.getLast(this);
			if (queuedElement==null || !queuedElement.isSetOneValue) {
				if (!storedInput && !storedOutput) {
					queuedElement=this;
					loadInputValue();
					isSetOneValue=true;
				} else
					queuedElement=new SFImageWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
			}
			queuedElement.loadInputValue();
			if (queuedElement.getComponents()!=img.getColorModel().getNumComponents()
				|| queuedElement.getWidth()<srcWidth
				|| queuedElement.getHeight()<srcHeight ) {
				
				queuedElement.storedInput=false;
				throw new IllegalArgumentException("Subimage either too large or differs in components");
			}
			// Now do annoying bit blitting
			SFImageUtils.convertSubRenderedImageToData(img,queuedElement.storedInputValue,0,srcWidth,srcHeight,srcXOffset,srcYOffset,destXOffset,destYOffset);
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
	public void setValue(int width,
		int height,
		int components,
		int[] pixels) {
		if (width<0)
			throw new IllegalArgumentException("Invalid width");
		if (height<0)
			throw new IllegalArgumentException("Invalid height");
		if (components<0)
			throw new IllegalArgumentException("Invalid components");
		checkWriteAccess();
		SFImageWrapper queuedElement=this;
		// Input and output buffers do not mix
		if (storedInput || storedOutput) {
			queuedElement=new SFImageWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
		}
		queuedElement.storedInput=true;
		if (queuedElement.storedInputValue==null || queuedElement.storedInputValue.length!=width*height+3)
			queuedElement.storedInputValue=new int[width*height+3];
		if (queuedElement.storedInputValue.length>3)
			System.arraycopy(pixels,0,queuedElement.storedInputValue,3,width*height);
		queuedElement.storedInputValue[0]=width;
		queuedElement.storedInputValue[1]=height;
		queuedElement.storedInputValue[2]=components;
		theEventQueue.processEvent(queuedElement);
	}
}
