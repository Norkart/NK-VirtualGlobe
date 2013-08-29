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
 * VRML eventOut class for MFInt32.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutMFInt32 extends EventOutMField
{
  /**
   * Construct an instance of this class. The superclass is called with the
   * type set to MFInt32.
   */
  protected EventOutMFInt32()
  {
    super(MFInt32);
  }

  /**
   * Get the value of the array of integers.
   *
   * @return The current array of values.
   */
  public abstract int[] getValue();

  /**
   * Write the value of the array of the ints to the given array.
   *
   * @param values The array to be filled in
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(int[] values);

  /**
   * Get a particular value from the eventOut array.
   * <P>
   * If the index is out of the bounds of the current array of data values an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param index The position to be retrieved
   * @return The value at that position
   *
   * @exception ArrayIndexOutOfBoundsException The index was outside the current data
   *    array bounds.
   */
  public abstract int get1Value(int index)
        throws ArrayIndexOutOfBoundsException;
}











