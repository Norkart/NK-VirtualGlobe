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
 * VRML eventIn class for SFNode.
 * <P>
 * Get the value of a node. The java <CODE>null</CODE> reference is treated to
 * be equivalent to the VRML <CODE>NULL</CODE> field values. If the node field
 * contains a NULL reference then reading this eventOut will result in a
 * java null being returned.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutSFNode extends EventOut
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFNode.
   */
  protected EventOutSFNode()
  {
    super(SFNode);
  }

  /**
   * Get the node value in the given eventOut. If no node reference is set then
   * null is returned to the user.
   * <P>
   * @return The new node reference set.
   */
  public abstract Node getValue();
}









