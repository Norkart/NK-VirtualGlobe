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
 * VRML eventIn class for SFColor.
 * <P>
 * Colour values are represented as floating point numbers between [0 - 1]
 * as per the VRML IS specification Section 4.4.5 Standard units and
 * coordinate system.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInSFColor extends EventIn
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFColor.
   */
  protected EventInSFColor()
  {
    super(SFColor);
  }

  /**
   * Set the colour value in the given eventIn.  Colour values are required
   * to be in the range [0-1].
   * <P>
   * The value array must contain at least three elements. If the array
   * contains more than 3 values only the first three values will be used and
   * the rest ignored.
   * <P>
   * If the array of values does not contain at least 3 elements an
   * ArrayIndexOutOfBoundsException will be generated. If the colour values are
   * out of range an IllegalArgumentException will be generated.
   *
   * @param value The array of colour values where<BR>
   *    value[0] = Red component [0-1] <BR>
   *    value[1] = Green component [0-1] <BR>
   *    value[2] = Blue component [0-1] <BR>
   *
   * @exception IllegalArgumentException A colour value(s) was out of range
   * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
   *    values for the colour component
   */
  public abstract void setValue(float[] value);
}









