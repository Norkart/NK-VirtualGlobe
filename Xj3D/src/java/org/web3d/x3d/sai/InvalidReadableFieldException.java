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
 * The exception that is thrown when a reference to an readable field is not valid.
 * <P>
 * An readable field may be invalid for a number of reasons:
 * <UL>
 * <LI>The user may have typed in the wrong name through a typo.
 * <LI>The name may not correspond to a field in that node at all.
 * <LI>The name given refers to a valid field but the field cannot be
 *     accessed as an readable field.
 * </UL>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class InvalidReadableFieldException extends InvalidFieldException {

    /**
     * Construct a basic instance of this exception with no error message
     */
    public InvalidReadableFieldException() {
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public InvalidReadableFieldException(String msg) {
        super(msg);
    }
}
