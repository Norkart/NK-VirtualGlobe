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
 * VRML eventOut class for MFString.
 * <P>
 * Strings are represented using standard java.lang.String representations.
 * The implementation of this class will provide any necessary conversions
 * to the UTF8 format required for VRML support.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutMFString extends EventOutMField
{
  /**
   * Construct an instance of this class. The superclass constructor is called
   * with the type MFString
   */
  protected EventOutMFString()
  {
    super(MFString);
  }

  /**
   * Get the value of the array of strings. Individual elements in the string
   * array may be null depending on the implementation of the browser and
   * whether it maintains null references
   *
   * @return The array of strings.
   *
   * @see EventInMFString#setValue
   */
  public abstract String[] getValue();

  /**
   * Write the value of the array of the strings to the given array. Individual
   * elements in the string array may be null depending on the implementation
   * of the browser and whether it maintains null references.
   *
   * @param value The string array to be filled in
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(String[] value);

  /**
   * Get a particular string value in the given eventOut array.
   * <P>
   * If the index is out of the bounds of the current array of data values an
   * ArrayIndexOutOfBoundsException will be generated. If the array reference
   * was null when set, an empty string will be returned to the caller.
   *
   * @param index The position to get the string value from
   * @return The string value
   *
   * @exception ArrayIndexOutOfBoundsException The index value was out of bounds of
   *     the current array.
   *
   * @see EventInMFString#set1Value
   */
  public abstract String get1Value(int index);
}









