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
 * VRML eventIn class for MFInt32.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInMFInt32 extends EventIn
{
  /**
   * Construct an instance of this class. The superclass is called with the
   * type set to MFInt32.
   */
  protected EventInMFInt32()
  {
    super(MFInt32);
  }

  /**
   * Set the value of the array of integers. If the value array is length zero
   * this is equivalent of clearing the field.
   *
   * @param value The array of values to be set.
   */
  public abstract void setValue(int[] value);

  /**
   * Set a particular value in the given eventIn array. To the VRML
   * world this will generate a full MFInt32 event with the nominated index
   * value changed.
   * <P>
   * If the index is out of the bounds of the current array of data values an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param index The position to set the colour value
   * @param value The value to be set.
   *
   * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
   *    values for the colour component
   */
  public abstract void set1Value(int index, int value)
    throws ArrayIndexOutOfBoundsException;
}









