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
 * Representation of a SFFloat field.
 *
 * @version 1.0 30 April 1998
 */
public interface SFFloat extends X3DField {

    /**
     * Ge the float value in the given eventOut.
     *
     * @return The float value to of the eventOut
     */
    public float getValue();

    /**
     * Set the float value in the given eventIn.
     *
     * @param value The array of float value to set.
     */
    public void setValue(float value);
}
