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

package org.web3d.vrml.scripting.sai;

// Extrernal imports
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;

// Local imports
import org.web3d.util.SFImageUtils;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.x3d.sai.*;

/**
 * Implementation of an SFImage field.
 *
 * @author Brad Vendor
 * @version $Revision: 1.5 $
 */
class SAISFImage extends BaseField implements SFImage {

    /** The basic array form of the image */
    private int[] localValue;

    /**                                                                               
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAISFImage(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    /**
     * Get the width of the image.
     *
     * @return The width of the image in pixels
     */
    public int getWidth() {
    	checkAccess(false);
    	if (localValue!=null && localValue.length>0)
    		return localValue[0];
    	else
    		return 0;
    }

    /**
     * Get the height of the image.
     *
     * @return The height of the image in pixels
     */
    public int getHeight() {
    	checkAccess(false);
    	if (localValue!=null && localValue.length>0)
    		return localValue[1];
    	else
    		return 0;
    }

    /**
     * Get the number of colour components in the image. The value will
     * always be between 1 and 4 indicating the number of components of
     * the colour specification to be read from the image pixel data.
     *
     * @return The number of components
     */
    public int getComponents() {
    	checkAccess(false);
        if (localValue!=null && localValue.length>0)
        	return localValue[2];
        else
        	return 0;
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
    	checkAccess(false);
    	if (localValue!=null && localValue.length>0) {
    		int numPixels=localValue[0]*localValue[1];
    		if (numPixels>0)
    			System.arraycopy(localValue,3,pixels,0,numPixels);
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
    	checkAccess(false);
		
    	int width = getWidth( );
        int height = getHeight( );
        int components = getComponents( );
        int pixels[] = new int[width*height];
        getPixels( pixels );
		
        return( SFImageUtils.convertDataToRenderedImage( width, height, components, pixels ) );
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
    	checkAccess(true);
    	int newSize=img.getHeight()*img.getWidth()+3;
    	if (localValue==null || localValue.length!=newSize)
    		localValue=new int[newSize];
        SFImageUtils.convertRenderedImageToData(img,localValue,0);
        dataChanged=true;
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
    	checkAccess(true);
        if (getComponents()!=img.getColorModel().getNumComponents()
        		|| getWidth()<srcWidth
        		|| getHeight()<srcHeight ) {
				
        	throw new IllegalArgumentException("Subimage either too large or differs in components");
        }
        SFImageUtils.convertSubRenderedImageToData(img,localValue,0,srcWidth,srcHeight,srcXOffset,srcYOffset,destXOffset,destYOffset);
        dataChanged=true;
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
        checkAccess(true);
        int requiredStorage=width*height+3;
        if (localValue==null || localValue.length!=requiredStorage)
        	localValue=new int[requiredStorage];
        localValue[0]=width;
        localValue[1]=height;
        localValue[2]=components;
        System.arraycopy(pixels,0,localValue,3,requiredStorage);
        dataChanged=true;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Notification to the field instance to update the value in the
     * underlying node now.
     */
    void updateNode() {
        node.setValue(fieldIndex,localValue,localValue.length);
        dataChanged = false;
    }


    /**
     * Notification to the field to update its field values from the
     * underlying node.
     */
    void updateField() {
        if(!isReadable())
            return;

        VRMLFieldData data = node.getFieldValue(fieldIndex);

        int req_size = data.intArrayValue.length;
        if((localValue == null) || (localValue.length < req_size))
            localValue = new int[req_size];

        System.arraycopy(data.intArrayValue, 0, localValue, 0, req_size);

        dataChanged = false;
    }
}
