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
 * Representation of a SFDouble field.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface SFDouble extends X3DField {

    /**
     * Ge the double value in the given eventOut.
     *
     * @return The double value to of the eventOut
     */
    public double getValue();

    /**
     * Set the double value in the given eventIn.
     *
     * @param value The array of double value to set.
     */
    public void setValue(double value);
}
