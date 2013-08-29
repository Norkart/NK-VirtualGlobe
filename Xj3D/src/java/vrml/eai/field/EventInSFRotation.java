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
 * VRML eventIn class for SFRotation.
 * <P>
 * Rotation values are specified according to the VRML IS Specification
 * Section 5.8 SFRotation and MFRotation.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInSFRotation extends EventIn
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFRotation
   */
  protected EventInSFRotation()
  {
    super(SFRotation);
  }

  /**
   * Set the rotation value in the given eventIn.
   * <P>
   * The value array must contain at least four elements. If the array
   * contains more than 4 values only the first 4 values will be used and
   * the rest ignored.
   * <P>
   * If the array of values does not contain at least 4 elements an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param value The array of rotation values where<BR>
   *    value[0] = X component [0-1] <BR>
   *    value[1] = Y component [0-1] <BR>
   *    value[2] = Z component [0-1] <BR>
   *    value[3] = Angle of rotation [-PI - PI] (nominally).
   *
   * @exception ArrayIndexOutOfBoundsException The value did not contain at least 4
   *    values for the rotation.
   */
  public abstract void setValue(float[] value);
}









