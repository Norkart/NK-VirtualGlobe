/******************************************************************************
 *
 *                      VRML Browser basic classes
 *                   For External Authoring Interface
 *
 *                   (C) 1998 Justin Couch
 *
 *  Written by Justin Couch: justin@vlc.com.au
 *
 * This code is free software and is distributed under the terms implied by
 * the GNU LGPL. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai.field;

import vrml.eai.Node;

/**
 * VRML eventOut class for SFImage.
 * <P>
 * Images are represented as arrays of integers as per the VRML IS
 * specification Section 5.5 SFImage. Pixel values are between 0 and 256 and
 * represented as integers to maintain consistency with java's ImageConsumer
 * interface and PixelGrabber class.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutSFImage extends EventOut
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFImage.
   */
  protected EventOutSFImage()
  {
    super(SFImage);
  }

  /**
   * Get the width of the image.
   *
   * @return The width of the image in pixels
   */
  public abstract int getWidth();

  /**
   * Get the height of the image.
   *
   * @return The height of the image in pixels
   */
  public abstract int getHeight();

  /**
   * Get the number of colour components in the image. The value will
   * always be between 1 and 4 indicating the number of components of
   * the colour specification to be read from the image pixel data.
   *
   * @return The number of components
   */
  public abstract int getComponents();

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
   *    intensity = pixel[i] & 0xFF;
   * </PRE>
   * <P>
   * <B>2 Component Images</B><BR>
   * The integer has the intensity value stored in the lowest byte and the
   * transparency in the top byte:
   * <PRE>
   *    intensity = pixel[i] & 0xFF;
   *    alpha = (pixel[i] >> 24) & 0xFF00;
   * </PRE>
   * <I>Note</I> that this is different to the VRML representation of the image
   * which would store the values in the text file as
   * <CODE>alpha = pixel[i] >> 8) & 0xFF</CODE>. The reason for the difference
   * is to maintain as much compatibility with the java image model as
   * possible. Java does not contain a separate intensity only image model,
   * instead it sets all three colour components to the same value. This way,
   * the user of SFImages can take a full colour image and turn it to B&W by
   * just setting each of the bytes to the same value as the lowest byte.
   * <P>
   * <B>3 Component Images</B><BR>
   * The three colour components are stored in the integer array as follows:
   * <PRE>
   *    red   = (pixel[i] >> 16) & 0xFF;
   *    green = (pixel[i] >>  8) & 0xFF;
   *    blue  = (pixel[i]      ) & 0xFF;
   * </PRE>
   * <P>
   * <B>4 Component Images</B><BR>
   * The integer has the value stored in the array as follows:
   * <PRE>
   *    alpha = (pixel >> 24) & 0xff;
   *    red   = (pixel >> 16) & 0xff;
   *    green = (pixel >>  8) & 0xff;
   *    blue  = (pixel      ) & 0xff;
   * </PRE>
   * <P>
   * The width and height values must be greater than or equal to zero. The
   * number of components is between 1 and 4. Any value outside of these
   * bounds will generate an IllegalArgumentException.
   *
   * @return The array of pixel values as specified above.
   */
  public abstract int[] getPixels();

  /**
   * Write the pixel values to the given array. Returns values in the same
   * format as that used by the no argument version of this method. The length
   * of the array must be at least width * height elements.
   *
   * @param pixels The array to write pixels to.
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getPixels(int[] pixels);
}









