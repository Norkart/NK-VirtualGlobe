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
 * The exception that is thrown when a reference to any field is not valid.
 * Generally used as a base class to more specific invalid field methods.
 * <P>
 * An field may be invalid for a number of reasons:
 * <UL>
 * <LI>The user may have typed in the wrong name through a typo.
 * <LI>The name may not correspond to a field in that node at all.
 * <LI>The name given refers to a valid field but the field cannot be
 *     accessed as an outputOnly field.
 * </UL>
 *
 * @version $Revision: 1.3 $
 */
public class InvalidFieldException extends X3DException {
    /**
     * Construct a basic instance of this exception with no error message
     */
    public InvalidFieldException() {
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public InvalidFieldException(String msg) {
        super(msg);
    }
}
