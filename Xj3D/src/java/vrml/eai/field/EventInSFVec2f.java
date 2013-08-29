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
 * VRML eventIn class for SFVec2f.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInSFVec2f extends EventIn
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFVec2f.
   */
  protected EventInSFVec2f()
  {
    super(SFVec2f);
  }

  /**
   * Set the vector value in the given eventIn.
   * <P>
   * The value array must contain at least two elements. If the array
   * contains more than 2 values only the first 2 values will be used and
   * the rest ignored.
   * <P>
   * If the array of values does not contain at least 2 elements an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param value The array of vector components where<BR>
   *    value[0] = X<BR>
   *    value[1] = Y<BR>
   *
   * @exception ArrayIndexOutOfBoundsException The value did not contain at least two
   *    values for the vector
   */
  public abstract void setValue(float[] value);
}









