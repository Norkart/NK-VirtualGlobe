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
import vrml.eai.InvalidNodeException;

/**
 * VRML eventIn class for SFNode.
 * <P>
 * Set the value of a node to the given value. The java <CODE>null</CODE>
 * reference is treated to be equivalent to the VRML <CODE>NULL</CODE> field
 * values.
 * <P>
 * Calling the set method with a null node reference causes that field to
 * be cleared.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInSFNode extends EventIn
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFNode.
   */
  protected EventInSFNode()
  {
    super(SFNode);
  }

  /**
   * Set the node value in the given eventIn.
   * <P>
   * If the node reference passed to this method has already had the dispose
   * method called then an InvalidNodeException will be generated.
   *
   * @param value The new node reference to be used.
   *
   * @exception InvalidNodeException The node reference passed has already
   *    been disposed.
   */
  public abstract void setValue(Node value)
      throws InvalidNodeException;
}












