/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.eventmodel;

// External imports
// None

// Local imports
import org.web3d.util.ErrorReporter;

/**
 * Abstract representation of code that presents an external view of the
 * browser to client code.
 * <p>
 *
 * This interface is used to present hooks from the internals of a VRML browser
 * to code that implements the EAI or SAI external interface. It allows
 * synchronisation and management to go back and forth and allow easy
 * synchronisation of updates to avoid multi-threading issues.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface ExternalView {

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Notification that the external view should now process and propogate to
     * the underlying nodes.
     */
    public void processEvents();
}

