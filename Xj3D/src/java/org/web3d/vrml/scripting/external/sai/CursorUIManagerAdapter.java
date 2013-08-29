/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.external.sai;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.Xj3DConstants;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.xj3d.sai.Xj3DCursorUIManager;
import org.xj3d.sai.Xj3DCursorFilter;
import org.xj3d.core.eventmodel.CursorManager;
import org.xj3d.core.eventmodel.CursorFilter;

/**
 * An abstract interface for cursor-specific user interface control of the
 * the browser.
 * <p>
 * This allows an external application to replace the cursor logic
 * with their own.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
class CursorUIManagerAdapter implements Xj3DCursorUIManager {

    /** Manager of viewpoint stuff within the internals of the browser */
    private CursorManager cursorManager;

    /** The adapter from internal to external filtering */
    private CursorFilterAdapter filterAdapter;

    /** Core of the browser. used for viewpoint handling */
    private BrowserCore browserCore;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /**
     * Create a new instance of this adapter that works with the given
     * viewpoint management interface. The viewpoint manager interface
     * should not be null.
     *
     * @param cm The manager of cursors
     * @param core The browser core
     */
    CursorUIManagerAdapter(CursorManager cm, BrowserCore core) {
        cursorManager = cm;
        browserCore = core;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by Xj3DCursorUIManager
    //----------------------------------------------------------

    /**
     * Add a cursor filter.
     *
     * @param cf The filter
     */
    public void setCursorFilter(Xj3DCursorFilter cf) {
        if (cursorManager == null) {
            errorReporter.errorReport("No cursor manager, filtering disabled", null);
            return;
        }

        filterAdapter = new CursorFilterAdapter(cf);

        cursorManager.setCursorFilter(filterAdapter);
    }

    /**
     * Set the cursor to the currently specified image.  Normal changes
     * can still occur.  Monitor the cursorFilter for changes.
     *
     * @param url The image to use.
     * @param x The center x coordinate
     * @param y The center y coordinate
     */
    public void setCursor(String url, int x, int y) {
        cursorManager.setCursor(url, x, y);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------


    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }
}
