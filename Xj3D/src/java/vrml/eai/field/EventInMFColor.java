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
 * VRML eventIn class for MFColor.
 * <P>
 * Colour values are represented as floating point numbers between [0 - 1]
 * as per the VRML IS specification Section 4.4.5 Standard units and
 * coordinate system
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInMFColor extends EventIn
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the type set to MFColor.
   */
  protected EventInMFColor()
  {
    super(MFColor);
  }

  /**
   * Set the value of the array of colours. Input is an array of colour
   * values in RGB order. All colour values are required to be in the
   * range 0-1. Colour values outside of this range will generate an
   * IllegalArgumentException. If value[i] that does not contain at
   * least three values will generate an ArrayIndexOutOfBoundsException.
   * If value[i] contains more than three items only the first three values
   * will be used and the rest ignored.
   *
   * @param value The array of colour values where<BR>
   *    value[i][0] = Red component [0-1] <BR>
   *    value[i][1] = Green component [0-1] <BR>
   *    value[i][2] = Blue component [0-1] <BR>
   *
   * @exception IllegalArgumentException A colour value(s) was out of range
   * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
   *    values for the colour component
   */
  public abstract void setValue(float[][] value);

  /**
   * Set a particular colour value in the given eventIn array. To the VRML
   * world this will generate a full MFColor event with the nominated index
   * value changed. Colour values are required to be in the range [0-1].
   * <P>
   * The value array must contain at least three elements. If the array
   * contains more than 3 values only the first three values will be used and
   * the rest ignored.
   * <P>
   * If the index is out of the bounds of the current array of data values or
   * the array of values does not contain at least 3 elements an
   * ArrayIndexOutOfBoundsException will be generated. If the colour values are
   * out of range an IllegalArgumentException will be generated.
   *
   * @param index The position to set the colour value
   * @param value The array of colour values where<BR>
   *    value[0] = Red component [0-1] <BR>
   *    value[1] = Green component [0-1] <BR>
   *    value[2] = Blue component [0-1] <BR>
   *
   * @exception IllegalArgumentException A colour value(s) was out of range
   * @exception ArrayIndexOutOfBoundsException A value did not contain at least
   *    three values for the colour component
   */
  public abstract void set1Value(int index, float[] value);
}









