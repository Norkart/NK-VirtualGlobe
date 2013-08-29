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
 * VRML eventOut class for MFFloat.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutMFFloat extends EventOutMField
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the type MFFloat
   */
  protected EventOutMFFloat()
  {
    super(MFFloat);
  }

  /**
   * Get the value of the eventOut array of float values.
   *
   * @return The array of values currently set.
   */
  public abstract float[] getValue();

  /**
   * Write the value of the array of the floats to the given array.
   *
   * @param values The array to be filled in
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(float[] values);

  /**
   * Get the value of an individual item in the eventOut's value.
   *
   * If the index is out of the bounds of the current array of data values an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param index The position to be retrieved
   * @return The value to at that position
   *
   * @exception ArrayIndexOutOfBoundsException The index was outside the current data
   *    array bounds.
   */
  public abstract float get1Value(int index)
        throws ArrayIndexOutOfBoundsException;
}












