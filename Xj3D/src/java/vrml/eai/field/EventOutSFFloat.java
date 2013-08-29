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
 * VRML eventOut class for SFFloat.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutSFFloat extends EventOut
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFFloat.
   */
  protected EventOutSFFloat()
  {
    super(SFFloat);
  }

  /**
   * Ge the float value in the given eventOut.
   *
   * @return The float value to of the eventOut
   */
  public abstract float getValue();
}









