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
 * VRML eventIn class for MFFloat.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInMFFloat extends EventIn
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the type MFFloat
   */
  protected EventInMFFloat()
  {
    super(MFFloat);
  }

  /**
   * Set the value of the eventIn to the new array of float values. This array
   * is copied internally so that the parameter array can be reused without
   * effecting the valid values of the eventIn.
   *
   * @param value The array of values to be used.
   */
  public abstract void setValue(float[] value);

  /**
   * Set the value of an individual item in the eventIn's value. This results in
   * a new event being generated that includes all of the array items with the
   * single element set.
   *
   * If the index is out of the bounds of the current array of data values an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param index The position to set the colour value
   * @param value The value to be set
   *
   * @exception ArrayIndexOutOfBoundsException A value did not contain at least
   *    three values for the colour component
   */
  public abstract void set1Value(int index, float value)
    throws ArrayIndexOutOfBoundsException;
}









