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
 * Representation of a SFBool field.
 *
 * @version 1.0 30 April 1998
 */
public interface SFBool extends X3DField {

    /**
     * Get the value in the given eventOut.
     * <P>
     * @return The boolean value of the eventOut
     */
    public boolean getValue();

    /**
     * Set the value in the given eventIn.
     * <P>
     * @param value The boolean value to set the eventIn to.
     */
    public void setValue(boolean value);
}
