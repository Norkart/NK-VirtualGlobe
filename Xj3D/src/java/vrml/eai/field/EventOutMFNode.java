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
 * VRML eventIn class for MFNode.
 * <P>
 * Get the values of a node array. The java <CODE>null</CODE> reference is
 * treated to be equivalent to the VRML <CODE>NULL</CODE> field values.
 * <P>
 * It is not illegal to construct an array where some members of the array are
 * null pointers. Due to no specification on the intended result in the VRML
 * specification, the response given by the browser is implementation
 * dependent. Calls will not generate an exception, but the value of actual
 * event received from the scenegraph may vary until the issue is resolved.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutMFNode extends EventOutMField
{
  /**
   * Construct an instance of this class. The superclass constructor is called
   * with the type MFNode.
   */
  protected EventOutMFNode()
  {
    super(MFNode);
  }

  /**
   * Get the value of the array of nodes.
   * <P>
   *
   * @return The array of node references
   */
  public abstract Node[] getValue();

  /**
   * Write the value of the array of the nodes to the given array. Individual
   * elements in the array may be null depending on the implementation
   * of the browser and whether it maintains null references.
   *
   * @param nodes The node array to be filled in
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(Node[] nodes);

  /**
   * Get a particular node value in the given eventOut array.
   * <P>
   * If the index is out of the bounds of the current array of data values an
   * ArrayIndexOutOfBoundsException will be generated. If the Node value is
   * NULL because the browser implementation keeps null references then this
   * method will return a null pointer without throwing any exception.
   * <P>
   * @param index The position to read the values from
   * @return The node reference
   *
   * @exception ArrayIndexOutOfBoundsException The index was outside the current data
   *    array bounds.
   */
  public abstract Node get1Value(int index);
}











