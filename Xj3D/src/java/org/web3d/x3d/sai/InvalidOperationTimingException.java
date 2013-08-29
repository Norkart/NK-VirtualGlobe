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
 * The exception that is thrown when the user attempts to use an API call
 * outside of the pre-defined times.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class InvalidOperationTimingException extends X3DException {
    /**
     * Construct a basic instance of this exception with no error message
     */
    public InvalidOperationTimingException() {
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public InvalidOperationTimingException(String msg) {
        super(msg);
    }
}
