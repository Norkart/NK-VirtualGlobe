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

/**
 * VRML eventOut class for SFVec3f.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutSFVec3f extends EventOut
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFVec3f.
   */
  protected EventOutSFVec3f()
  {
    super(SFVec3f);
  }

  /**
   * Get the vector value in the given eventOut.
   *
   * @return The array of vector components where<BR>
   *    value[0] = X<BR>
   *    value[1] = Y<BR>
   *    value[2] = Z
   */
  public abstract float[] getValue();

  /**
   * Write the vector value to the given eventOut
   *
   * @param vec The array of vector values to be filled in where<BR>
   *    vec[0] = X<BR>
   *    vec[1] = Y<BR>
   *    vec[2] = Z
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(float[] vec);
}









