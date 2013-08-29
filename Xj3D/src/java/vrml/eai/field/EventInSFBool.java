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
 * VRML eventIn class for SFBool.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInSFBool extends EventIn
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFBool.
   */
  protected EventInSFBool()
  {
    super(SFBool);
  }

  /**
   * Set the value in the given eventIn.
   * <P>
   * @param value The boolean value to set the eventIn to.
   */
  public abstract void setValue(boolean value);
}









