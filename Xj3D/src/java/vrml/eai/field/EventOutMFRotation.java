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
 * VRML eventOut class for MFRotation.
 * <P>
 * Rotation values are specified according to the VRML IS Specification
 *  Section 5.8 SFRotation and MFRotation.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutMFRotation extends EventOutMField
{
  /**
   * Construct an instance of this class. The superclass constructor is called
   * with a type of MFRotation.
   */
  protected EventOutMFRotation()
  {
    super(MFRotation);
  }

  /**
   * Get the value of the array of rotations. Output is an array of floats
   * values in order required to specify an SFRotation.
   *
   * @return The array of rotation values where<BR>
   *    value[i][0] = X component [0-1] <BR>
   *    value[i][1] = Y component [0-1] <BR>
   *    value[i][2] = Z component [0-1] <BR>
   *    value[i][3] = Angle of rotation [-PI - PI] (nominally).
   */
  public abstract float[][] getValue();

  /**
   * Write the value of the event out to the given array.
   *
   * @param vec The array to be filled in where<BR>
   *    value[i][0] = X component [0-1] <BR>
   *    value[i][1] = Y component [0-1] <BR>
   *    value[i][2] = Z component [0-1] <BR>
   *    value[i][3] = Angle of rotation [-PI - PI] (nominally).
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(float[][] vec);

  /**
   * Get the values of the event out flattened into a single 1D array. The
   * array must be at least 4 times the size of the array.
   *
   * @param vec The array to be filled in where the
   *    value[i + 0] = X component [0-1] <BR>
   *    value[i + 1] = Y component [0-1] <BR>
   *    value[i + 2] = Z component [0-1] <BR>
   *    value[i + 3] = Angle of rotation [-PI - PI] (nominally).
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(float[] vec);

  /**
   * Get a particular rotation in the given eventOut array.
   * <P>
   * If the index is out of the bounds of the current array of data values an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param index The position to get the rotation value
   * @return The array of rotation values where<BR>
   *    value[0] = X component [0-1] <BR>
   *    value[1] = Y component [0-1] <BR>
   *    value[2] = Z component [0-1] <BR>
   *    value[3] = Angle of rotation [-PI - PI] (nominally).
   *
   * @exception ArrayIndexOutOfBoundsException The index was outside the current data
   *    array bounds.
   */
  public abstract float[] get1Value(int index);

  /**
   * Get the value of a particular rotation value in the event out array.
   *
   * @param index The position to get the vectory value from.
   * @param vec The array to place the value in where.
   *    vec[0] = X component [0-1] <BR>
   *    vec[1] = Y component [0-1] <BR>
   *    vec[2] = Z component [0-1] <BR>
   *    vec[3] = Angle of rotation [-PI - PI] (nominally).
   * @exception ArrayIndexOutOfBoundsException The provided array was too small or
   *     the index was outside the current data array bounds.
   */
  public abstract void get1Value(int index, float[] vec);
}









