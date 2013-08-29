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

package org.web3d.vrml.renderer;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.sav.Locator;

/**
 * A default locator implementation for when the parser does not provide one.
 * <p>
 * This implementation always returns -1.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class DefaultLocator implements Locator {

    /**
     * Default constructor. Does nothing.
     */
    public DefaultLocator() {
    }

    /**
     * Get the current column number at the end of the last processing event.
     * If column number support is not provided, this should always return -1.
     *
     * @return The column number of the last processing event
     */
    public int getColumnNumber() {
        return -1;
    }

    /**
     * Get the current line number of the last event processing step. If the
     * last processing step takes more than one line, this is the first line
     * of the processing that called the callback event.
     *
     * @return The line number of the last processing step.
     */
    public int getLineNumber() {
        return -1;
    }
}
