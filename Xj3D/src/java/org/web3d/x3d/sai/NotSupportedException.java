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
 * The exception that is thrown when an operation is not supported by an
 * underlying implementation.
 * <P>
 * Typically this class is used when one of the implementations are not
 * supported in BrowserFactory
 *
 * @see BrowserFactory
 * @see BrowserFactoryImpl
 *
 * @version 1.0 23rd June 1998
 */
public class NotSupportedException extends X3DException {
    /**
     * Construct a basic instance of this exception with no error message
     */
    public NotSupportedException() {
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public NotSupportedException(String msg) {
        super(msg);
    }
}
