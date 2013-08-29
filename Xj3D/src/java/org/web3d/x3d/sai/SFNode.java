/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

/**
 * Representation of a SFNode field.
 * <P>
 * Get the value of a node. The java <CODE>null</CODE> reference is treated to
 * be equivalent to the VRML <CODE>NULL</CODE> field values. If the node field
 * contains a NULL reference then reading this eventOut will result in a
 * java null being returned.
 *
 * @version 1.0 30 April 1998
 */
public interface SFNode extends X3DField {

    /**
     * Get the node value in the given eventOut. If no node reference is set then
     * null is returned to the user.
     * <P>
     * @return The new node reference set.
     */
    public X3DNode getValue();

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
    public void setValue(X3DNode value)
      throws InvalidNodeException;
}
