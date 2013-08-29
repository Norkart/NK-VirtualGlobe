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

package org.web3d.x3d.jaxp;

// Standard imports
import org.xml.sax.Locator;

// Application specific imports
// None

/**
 * Adapter between the SAX Locator and the SAV Locator.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class LocatorAdapter implements org.web3d.vrml.sav.Locator {

    /** The SAX variant of the locator */
    private Locator saxLocator;

    /**
     * Construct a new instance of the adapter for the given locator
     * instance. Expected to be non-null.
     *
     * @param loc The locator instance to use
     */
    LocatorAdapter(Locator loc) {
        saxLocator = loc;
    }

    //----------------------------------------------------------
    // Methods defined by org.web3d.vrml.sav.Locator
    //----------------------------------------------------------

    /**
     * Get the current column number at the end of the last processing event.
     * If column number support is not provided, this should always return -1.
     *
     * @return The column number of the last processing event
     */
    public int getColumnNumber() {
        return saxLocator.getColumnNumber();
    }

    /**
     * Get the current line number of the last event processing step. If the
     * last processing step takes more than one line, this is the first line
     * of the processing that called the callback event.
     *
     * @return The line number of the last processing step.
     */
    public int getLineNumber() {
        return saxLocator.getLineNumber();
    }
}
