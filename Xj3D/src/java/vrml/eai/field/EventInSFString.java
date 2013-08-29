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
 * VRML eventIn class for SFString.
 * <P>
 * Strings are represented using standard java.lang.String representations.
 * The implementation of this class will provide any necessary conversions
 * to the UTF8 format required for VRML support.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInSFString extends EventIn
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFString.
   */
  protected EventInSFString()
  {
    super(SFString);
  }

  /**
   * Set the string value in the given eventIn.
   * <P>
   * A string is not required to be valid. A null string reference will
   * be considered equivalent to a zero length string resulting in the
   * string being cleared.
   *
   * @param value The string to set.
   */
  public abstract void setValue(String value);
}









