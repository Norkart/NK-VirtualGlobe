/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

// External imports
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;

// Local imports
// none

/**
 * Representation of a MFImage field.
 * <P>
 * Images are represented as arrays of integers as per the X3D
 * specification Section 5.7 SFImage and MFImage. Pixel values are between 0
 * and 256 and represented as integers to maintain consistency with java's
 * ImageConsumer interface and PixelGrabber class.
 *
 * @version $Revision: 1.5 $
 */
public interface MFImage extends MField {

	/** Places a new value at the end of the existing value, increasing
	 *  the field length accordingly.
	 *  
	 * @param value The value to append
	 */
	public void append(int value);	
	
	/**
	 * Removes all values in the field and changes the field size to zero.
	 */
	public void clear();
	
    /**
     * Get the width of the image at index i in the array.
     *
     * @param imgIndex The index of the image in the array
     * @return The width of the image in pixels
     */
    public int getWidth(int imgIndex);

    /**
     * Get the height of the image.
     *
     * @param imgIndex The index of the image in the array
     * @return The height of the image in pixels
     */
    public int getHeight(int imgIndex);

    /**
     * Get the number of colour components in the image. The value will
     * always be between 1 and 4 indicating the number of components of
     * the colour specification to be read from the image pixel data.
     *
     * @param imgIndex The index of the image in the array
     * @return The number of components
     */
    public int getComponents(int imgIndex);

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
    public void getPixels(int imgIndex, int[] pixels);

    /**
     * Fetch the Java representation of the underlying image from these pixels.
     * This is the same copy that the browser uses to generate texture
     * information from.
     *
     * @param imgIndex The index of the image in the array
     * @return The image reference representing the current state
     */
    public WritableRenderedImage getImage(int imgIndex);

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
        throws ArrayIndexOutOfBoundsException;
    /**
     * Removes one value from the field.  Values at indices above the
     * removed element will be shifted down by one and the size of the
     * field will be reduced by one.  This method modifies the integer
     * array rather than the conceptual image array.
     * 
     * @param index The position of the value to remove.
     * @exception ArrayIndexOutOfBoundsException The index was outside the current field
     *    size.
     */
    public void removeValue(int index)
        throws ArrayIndexOutOfBoundsException;
    
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
               InvalidFieldException;

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
               InvalidFieldException;

    /**
     * Set one pixel value at the correct index in this field. Basically a
     * single pixel set, but it could also be width, height, number of
     * components. This must be used with great care as it could well corrupt
     * the user data.
     *
     * @param index The position in the array to set
     * @param value The value to use at that position
     */
    public void set1Value(int index, int value);

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
                          int[] pixels);

    /**
     * Set the value of the array to this value. The values are raw integers and
     * are to be interpreted according to the rules of the MFImage field. So the
     * first three values of the array will be width, height, depth and then
     * the appropriate number of pixels, more width height depth etc.
     *
     * @param numValues The number of valid elements in value
     * @param value The value to use
     * @exception IllegalArgumentException The number of components or width/
     *    height are illegal values.
     * @exception ArrayIndexOutOfBoundsException The number of pixels provided by the
     *    caller is not enough for the width * height.
     */
    public void setValue(int numValues, int[] value);

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
               InvalidFieldException;
}
