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
 * VRML eventIn class for MFColor.
 * <P>
 * Strings are represented using standard java.lang.String representations.
 * The implementation of this class will provide any necessary conversions
 * to the UTF8 format required for VRML support.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInMFString extends EventIn
{
  /**
   * Construct an instance of this class. The superclass constructor is called
   * with the type MFString
   */
  protected EventInMFString()
  {
    super(MFString);
  }

  /**
   * Set the value of the array of strings.  If value[i] contains a null
   * referenc this will not cause an exception to be generated. However,
   * the resulting event that the eventIn receives will be implementation
   * specific as this is not dealt with in the VRML specification.
   *
   * @param value The array of strings.
   */
  public abstract void setValue(String[] value);

  /**
   * Set a particular string value in the given eventIn array. To the VRML
   * world this will generate a full MFString event with the nominated index
   * value changed.
   * <P>
   * If the index is out of the bounds of the current array of data values an
   * ArrayIndexOutOfBoundsException will be generated. If the value reference
   * is null then the result is implementation specific in terms of the array
   * reference that reaches the eventIn. In any case, an event will reach the
   * destination eventIn, but the values in that array are implementation
   * specific. No exception will be generated in this case.
   *
   * @param index The position to set the string value
   * @param value The string value
   *
   * @exception ArrayIndexOutOfBoundsException The index value was out of bounds of
   *     the current array.
   */
  public abstract void set1Value(int index, String value);
}









