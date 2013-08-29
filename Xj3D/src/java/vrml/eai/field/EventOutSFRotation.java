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
 * VRML eventOut class for SFRotation.
 * <P>
 * Rotation values are specified according to the VRML IS Specification
 * Section 5.8 SFRotation and MFRotation.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutSFRotation extends EventOut
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFRotation
   */
  protected EventOutSFRotation()
  {
    super(SFRotation);
  }

  /**
   * Get the rotation value in the given eventOut.
   *
   * @return The array of rotation values where<BR>
   *    value[0] = X component [0-1] <BR>
   *    value[1] = Y component [0-1] <BR>
   *    value[2] = Z component [0-1] <BR>
   *    value[3] = Angle of rotation [-PI - PI] (nominally).
   */
  public abstract float[] getValue();

  /**
   * Write the rotation value to the given eventOut
   *
   * @param vec The array of vector values to be filled in where<BR>
   *    value[0] = X component [0-1] <BR>
   *    value[1] = Y component [0-1] <BR>
   *    value[2] = Z component [0-1] <BR>
   *    value[3] = Angle of rotation [-PI - PI] (nominally).
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(float[] vec);
}









