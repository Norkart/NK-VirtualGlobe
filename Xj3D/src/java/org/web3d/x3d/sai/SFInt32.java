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
 * Representation of a SFInt32 field.
 *
 * @version 1.0 30 April 1998
 */
public interface SFInt32 extends X3DField {

    /**
     * Get the value in the given eventOut.
     *
     * @return The value of the eventOut
     */
    public int getValue();

    /**
     * Set the value in the given eventIn.
     *
     * @param value The value to be set
     */
    public void setValue(int value);
}
