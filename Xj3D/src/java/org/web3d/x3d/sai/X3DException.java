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
 * The basic exception that is thrown by any VRML method call that wishes to
 * throw exceptions.
 * <p>
 *
 * Based on RuntimeException so that the user has the choice of deciding
 * whether to catch the exception or not.
 *
 * @version 1.0 30 April 1998
 */
public class X3DException extends RuntimeException {
    /**
     * Construct a basic instance of this exception with no error message
     */
    public X3DException() {
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public X3DException(String msg) {
        super(msg);
    }
}
