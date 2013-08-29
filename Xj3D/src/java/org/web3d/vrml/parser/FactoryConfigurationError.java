/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.parser;

// Standard imports
// none

// Application specific imports
// none

/**
 * An error thrown when a ParserFactory fails to instantiate.
 * <p>
 * A factory will normally fail to instantiate when it cannot find the
 * class that generates node instances or that the property was not defined.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class FactoryConfigurationError extends Error {

    /** The wrapped exception if given */
    private Exception other;

    /**
     * Create an empty error with no message or wrapped exception.
     */
    public FactoryConfigurationError() {
    }

    /**
     * Create an error that wraps the given exception but contains no message.
     *
     * @param e The exception to include
     */
    public FactoryConfigurationError(Exception e) {
        other = e;
    }

    /**
     * Create an error that has a message but no wrapped exception
     *
     * @param msg The message to include with the error
     */
    public FactoryConfigurationError(String msg) {
        super(msg);
    }

    /**
     * Create an error that has a message and wrapped exception.
     *
     * @param e The exception to include
     * @param msg The message to include with the error
     */
    public FactoryConfigurationError(Exception e, String msg) {
        super(msg);

        other = e;
    }

    /**
     * Convenience method to see if this has a wrapped exception.
     *
     * @return true if this error contains a nested exception
     */
    public boolean hasException() {
        return (other != null);
    }

    /**
     * Get the wrapped exception for this error. If there was no exception
     * given then this returns null.
     */
    public Exception getException() {
        return other;
    }
}
