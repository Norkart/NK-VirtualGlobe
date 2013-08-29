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
 * VRML eventIn class for MFRotation.
 * <P>
 * Rotation values are specified according to the VRML IS Specification
 *  Section 5.8 SFRotation and MFRotation.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInMFRotation extends EventIn
{
  /**
   * Construct an instance of this class. The superclass constructor is called
   * with a type of MFRotation.
   */
  protected EventInMFRotation()
  {
    super(MFRotation);
  }

  /**
   * Set the value of the array of rotations. Input is an array of floats
   * values in order required to specify an SFRotation. If value[i] does not
   * contain at least four values an ArrayIndexOutOfBoundsException will be
   * generated. If value[i] contains more than four items only the first
   * four values will be used and the rest ignored.
   *
   * @param value The array of rotation values where<BR>
   *    value[i][0] = X component [0-1] <BR>
   *    value[i][1] = Y component [0-1] <BR>
   *    value[i][2] = Z component [0-1] <BR>
   *    value[i][3] = Angle of rotation [-PI - PI] (nominally).
   *
   * @exception ArrayIndexOutOfBoundsException A value did not contain at least four
   *    values for the rotation
   */
  public abstract void setValue(float[][] value);

  /**
   * Set a particular rotation in the given eventIn array. To the VRML
   * world this will generate a full MFRotation event with the nominated index
   * value changed.
   * <P>
   * The value array must contain at least four elements. If the array
   * contains more than 4 values only the first four values will be used and
   * the rest ignored.
   * <P>
   * If the index is out of the bounds of the current array of data values or
   * the array of values does not contain at least 4 elements an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param index The position to set the rotation value
   * @param value The array of rotation values where<BR>
   *    value[0] = X component [0-1] <BR>
   *    value[1] = Y component [0-1] <BR>
   *    value[2] = Z component [0-1] <BR>
   *    value[3] = Angle of rotation [-PI - PI] (nominally).
   *
   * @exception ArrayIndexOutOfBoundsException A value did not contain at least four
   *    values for the rotation
   */
  public abstract void set1Value(int index, float[] value);
}









