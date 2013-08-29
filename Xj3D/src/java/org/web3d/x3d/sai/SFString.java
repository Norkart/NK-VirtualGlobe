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
 * Representation of a SFString field.
 * <P>
 * Strings are represented using standard java.lang.String representations.
 * The implementation of this class will provide any necessary conversions
 * to the UTF8 format required for VRML support.
 *
 * @version 1.0 30 April 1998
 */
public interface SFString extends X3DField {

    /**
     * Get the string value in the given eventOut.
     *
     * @return The current string value.
     */
    public String getValue();

    /**
     * Set the string value in the given eventIn.
     * <P>
     * A string is not required to be valid. A null string reference will
     * be considered equivalent to a zero length string resulting in the
     * string being cleared.
     *
     * @param value The string to set.
     */
    public void setValue(String value);
}
