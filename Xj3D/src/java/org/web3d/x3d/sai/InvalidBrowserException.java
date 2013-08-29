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
 * The exception that is thrown when the user attempts to access a method in
 * the Browser interface after the reference has had the dispose method called.
 *
 * @version 1.0 7th March 1998
 */
public class InvalidBrowserException extends X3DException {
    /**
     * Construct a basic instance of this exception with no error message
     */
    public InvalidBrowserException() {
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public InvalidBrowserException(String msg) {
        super(msg);
    }
}
