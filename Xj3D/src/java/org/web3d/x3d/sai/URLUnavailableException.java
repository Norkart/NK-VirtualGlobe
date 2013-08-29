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
 * The exception that is thrown when the URL is not specified for the currently
 * browser instance or there is some other problem.
 *
 * @version 1.0 13th August 1998
 */
public class URLUnavailableException extends X3DException {
    /**
     * Construct a basic instance of this exception with no error message
     */
    public URLUnavailableException() {
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public URLUnavailableException(String msg) {
        super(msg);
    }
}
