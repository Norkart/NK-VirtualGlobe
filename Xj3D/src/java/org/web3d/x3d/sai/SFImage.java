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

// Standard imports
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;

// Application specific imports

/**
 * Representation of a SFImage field.
 * <P>
 * Images are represented as arrays of integers as per the VRML IS
 * specification Section 5.5 SFImage. Pixel values are between 0 and 256 and
 * represented as integers to maintain consistency with java's ImageConsumer
 * interface and PixelGrabber class.
 *
 *
 * @version 1.0 30 April 1998
 */
public interface SFImage extends X3DField {

    /**
     * Get the width of the image.
     *
     * @return The width of the image in pixels
     */
    public int getWidth();

    /**
     * Get the height of the image.
     *
     * @return The height of the image in pixels
     */
    public int getHeight();

    /**
     * Get the number of color components in the image. The value will
     * always be between 1 and 4 indicating the number of components of
     * the color specification to be read from the image pixel data.
     *
     * @return The number of components
     */
    public int getComponents();

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
    public void getPixels(int[] pixels);

    /**
     * Fetch the Java representation of the underlying image from these pixels.
     * This is the same copy that the browser uses to generate texture
     * information from.
     *
     * @return The image reference representing the current state
     */
    public WritableRenderedImage getImage();

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
               InvalidFieldException;

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
     * @param components The number of color components [1-4]
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
                         int[] pixels);
}
