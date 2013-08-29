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
 * VRML eventIn class for MFVec3f.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInMFVec3f extends EventIn
{
  /**
   * Construct an instance of this class. The superclass is called with the
   * type MFVec3f
   */
  protected EventInMFVec3f()
  {
    super(MFVec3f);
  }

  /**
   * Set the value of the array of 3D vectors. Input is an array of floats
   * If value[i] does not contain at least three values it will generate an
   * ArrayIndexOutOfBoundsException. If value[i] contains more than three items
   * only the first three values will be used and the rest ignored.
   * <P>
   * If one or more of the values for value[i] are null then the resulting
   * event that is sent to the VRML scenegraph is implementation dependent but
   * no error indicator will be set here.
   *
   * @param value The array of vec2f values where<BR>
   *    value[i][0] = X<BR>
   *    value[i][1] = Y<BR>
   *    value[i][2] = Z
   *
   * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
   *    values for the vector definition.
   */
  public abstract void setValue(float[][] value);

  /**
   * Set a particular vector value in the given eventIn array. To the VRML
   * world this will generate a full MFVec3f event with the nominated index
   * value changed.
   * <P>
   * The value array must contain at least three elements. If the array
   * contains more than 3 values only the first 3 values will be used and
   * the rest ignored.
   * <P>
   * If the index is out of the bounds of the current array of data values or
   * the array of values does not contain at least 3 elements an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param index The position to set the vector value
   * @param value The array of vector values where<BR>
   *    value[0] = X<BR>
   *    value[1] = Y<BR>
   *    value[2] = Z
   *
   * @exception ArrayIndexOutOfBoundsException A value did not contain at least 3
   *    values for the vector
   */
  public abstract void set1Value(int index, float[] value);
}









