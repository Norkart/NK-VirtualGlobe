/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005 - 2006
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
// none

// Local imports
import org.web3d.util.ErrorReporter;

/**
 * Factory for generating renderer-specific instances of LayerManagers.
 * <p>
 *
 * Layer managers are inherently renderer-specific and this factory is used as
 * the glue between the generalised event model handling, and the
 * renderer-specific codes for per-layer handling
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface LayerManagerFactory {

    /**
     * Create a new layer manager instance.
     *
     * @return a new clean layer manager
     */
    public LayerManager createLayerManager();

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);
}

