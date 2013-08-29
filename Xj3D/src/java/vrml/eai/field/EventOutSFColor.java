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

/**
 * VRML eventOut class for SFColor.
 * <P>
 * Colour values are represented as floating point numbers between [0 - 1]
 * as per the VRML IS specification Section 4.4.5 Standard units and
 * coordinate system.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutSFColor extends EventOut
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFColor.
   */
  protected EventOutSFColor()
  {
    super(SFColor);
  }

  /**
   * Get a the colour value in the given eventIn.  Colour values are
   * in the range [0-1].
   *
   * @return The array of colour values where<BR>
   *    value[0] = Red component [0-1] <BR>
   *    value[1] = Green component [0-1] <BR>
   *    value[2] = Blue component [0-1] <BR>
   */
  public abstract float[] getValue();

  /**
   * Write the value of the colour to the given array.
   *
   * @param col The array of colour values to be filled in where<BR>
   *    value[0] = Red component [0-1] <BR>
   *    value[1] = Green component [0-1] <BR>
   *    value[2] = Blue component [0-1] <BR>
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(float[] col);
}









