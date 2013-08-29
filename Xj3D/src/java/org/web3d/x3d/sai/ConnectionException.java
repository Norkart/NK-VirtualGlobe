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
 * The exception that is thrown when an error occurs in the connection between
 * the external application and the VRML browser. Typically this might be a
 * network connection stopping or similar problem.
 *
 * @version 1.0 3 August 1998
 */
public class ConnectionException extends X3DException {
    /**
     * Construct a basic instance of this exception with no error message
     */
    public ConnectionException() {
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public ConnectionException(String msg) {
        super(msg);
    }
}
