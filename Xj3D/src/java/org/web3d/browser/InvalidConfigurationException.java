/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.browser;

/**
 * Xj3D has failed to start due to a configuration error.  This is a fatal
 * condition.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class InvalidConfigurationException extends RuntimeException {
    /**
     * Create a default exception that does not contain a message.
     */
    public InvalidConfigurationException() {
    }

    /**
     * Create an exception that contains the given message.
     *
     * @param msg The message to associate
     */
    public InvalidConfigurationException(String msg) {
        super(msg);
    }
}
