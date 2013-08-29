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
 * VRML eventOut base class for MF field values.
 * <P>
 * Class provides a size method that determines the number of items available
 * in this array of values. Normally used in conjunction with the get1Value()
 * method of the MF field classes so that exceptions are not generated.
 * <P>
 * It is possible, although not recommended, that the size of the arrays
 * returned by the get methods may be larger than the actual amount of data
 * that is to be represented. Calling size() beforehand ensures that the
 * correct number of items in the array will be read.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutMField extends EventOut
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the type set to MFColor.
   *
   * @param type The type of this eventOut
   */
  protected EventOutMField(int type)
  {
    super(type);
  }

  /**
   * Get the size of the underlying data array.
   *
   * @return The size of the array
   */
  public abstract int size();
}









