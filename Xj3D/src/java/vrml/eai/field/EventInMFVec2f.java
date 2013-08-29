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
 * VRML eventIn class for MFVec2f.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInMFVec2f extends EventIn
{
  /**
   * Construct an instance of this class. The superclass is called with the
   * type MFVec2f
   */
  protected EventInMFVec2f()
  {
    super(MFVec2f);
  }

  /**
   * Set the value of the array of 2D vectors. Input is an array of floats
   * If value[i] does not contain at least two values it will generate an
   * ArrayIndexOutOfBoundsException. If value[i] contains more than two items
   * only the first two values will be used and the rest ignored.
   * <P>
   * If one or more of the values for value[i] are null then the resulting
   * event that is sent to the VRML scenegraph is implementation dependent but
   * no error indicator will be set here.
   *
   * @param value The array of vec2f values where<BR>
   *    value[i][0] = X<BR>
   *    value[i][1] = Y
   *
   * @exception ArrayIndexOutOfBoundsException A value did not contain at least two
   *    values for the vector definition.
   */
  public abstract void setValue(float[][] value);

  /**
   * Set a particular vector value in the given eventIn array. To the VRML
   * world this will generate a full MFVec2f event with the nominated index
   * value changed.
   * <P>
   * The value array must contain at least two elements. If the array
   * contains more than 2 values only the first 2 values will be used and
   * the rest ignored.
   * <P>
   * If the index is out of the bounds of the current array of data values or
   * the array of values does not contain at least 2 elements an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param index The position to set the vector value
   * @param value The array of vector values where<BR>
   *    value[0] = X<BR>
   *    value[1] = Y
   *
   * @exception ArrayIndexOutOfBoundsException A value did not contain at least 2
   *    values for the vector
   */
  public abstract void set1Value(int index, float[] value);
}









