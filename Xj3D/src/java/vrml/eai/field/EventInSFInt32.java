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
 * VRML eventIn class for SFInt32.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInSFInt32 extends EventIn
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFInt32.
   */
  protected EventInSFInt32()
  {
    super(SFInt32);
  }

  /**
   * Set the value in the given eventIn.
   *
   * @param value The value to be set
   */
  public abstract void setValue(int value);
}











