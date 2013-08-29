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
 * VRML eventOut class for MFColor.
 * <P>
 * Colour values are represented as floating point numbers between [0 - 1]
 * as per the VRML IS specification Section 4.4.5 Standard units and
 * coordinate system
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutMFColor extends EventOutMField
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the type set to MFColor.
   */
  protected EventOutMFColor()
  {
    super(MFColor);
  }

  /**
   * Get the value of the array of colours. Input is an array of colour
   * values in RGB order. All colour values will to be in the
   * range 0-1.
   *
   * @return The array of colour values where<BR>
   *    value[i][0] = Red component [0-1] <BR>
   *    value[i][1] = Green component [0-1] <BR>
   *    value[i][2] = Blue component [0-1] <BR>
   */
  public abstract float[][] getValue();

  /**
   * Write the value of the event out to the given array.
   *
   * @param col The array to be filled in where<BR>
   *    col[i][0] = Red component [0-1] <BR>
   *    col[i][1] = Green component [0-1] <BR>
   *    col[i][2] = Blue component [0-1] <BR>
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(float[][] col);

  /**
   * Get the values of the event out flattened into a single 1D array. The
   * array must be at least 3 times the size of the array.
   *
   * @param col The array to be filled in where the
   *    col[i + 0] = Red component [0-1] <BR>
   *    col[i + 1] = Green component [0-1] <BR>
   *    col[i + 2] = Blue component [0-1] <BR>
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(float[] col);

  /**
   * Get a particular colour value in the given eventIn array. Colour values
   * are in the range [0-1].
   * <P>
   * If the index is out of the bounds of the current array of data values an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param index The position to get the colour value
   * @return value The array of colour values where<BR>
   *    value[0] = Red component [0-1] <BR>
   *    value[1] = Green component [0-1] <BR>
   *    value[2] = Blue component [0-1] <BR>
   *
   * @exception ArrayIndexOutOfBoundsException The index was outside the current data
   *    array bounds.
   */
  public abstract float[] get1Value(int index);

  /**
   * Get the value of a particular vector value in the event out array.
   *
   * @param index The position to get the vectory value from.
   * @param col The array to place the value in where.
   *    col[0] = Red component [0-1] <BR>
   *    col[1] = Green component [0-1] <BR>
   *    col[2] = Blue component [0-1] <BR>
   * @exception ArrayIndexOutOfBoundsException The provided array was too small or
   *     the index was outside the current data array bounds.
   */
  public abstract void get1Value(int index, float[] col);
}









