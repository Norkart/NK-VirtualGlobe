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
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.util.NodeArray;

import org.xj3d.core.loading.ScriptLoader;

/**
 * Abstract representation of a class that can be used to provide script
 * lifecycle management facilities to the browser core.
 * <p>
 *
 * The loader also takes part in the event model evaluation as well. For this,
 * it must hook into the URL change handling through the
 * {@link org.web3d.vrml.nodes.VRMLUrlListener} interface and participate
 * as part of the larger event model. Method calls are provided in this
 * interface for other classes
 * (eg {@link org.xj3d.core.eventmodel.EventModelEvaluator}) to make
 * calls to at the appropriate time in the event model. This interface does
 * not extend the URLListener interface directly as it assumes that an
 * implementation may take other internal arrangements. It assumes that
 * listeners will be registered with the script nodes.
 * <p>
 *
 * For VRML97/X3D conformant behaviour, the implied architecture of an
 * implementation of this interface is 5 separate buckets of data:
 *
 * <ol>
 * <li>Waiting to be loaded</li>
 * <li>In the proces of being loaded</li>
 * <li>Load complete (or failed) and waiting for initialize() to be called</li>
 * <li>Load finished (or failed), nothing left to do</li>
 * <li>Previously loaded scripts that have had set_url called and need to
 *     shutdown the old script and start the load process for others.
 * </li>
 * </ol>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface ScriptManager {

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
     * Set the script loader to be used by this manager. Setting a null value
     * will clear the current instance and disable all script loading.
     *
     * @param ldr The loader instance to use
     */
    public void setScriptLoader(ScriptLoader ldr);

    /**
     * Get the current script loader to be used by this manager. If none is
     * currently in use, null will be returned.
     *
     * @return The current loader instance or null
     */
    public ScriptLoader getScriptLoader();

    /**
     * The loader should now shutdown any scripts that have had their set_url
     * events called.
     */
    public void shutdownActiveScripts();

    /**
     * Shutdown all scripts as the system is about to shutdown all of the
     * current world and is starting again.
     */
    public void shutdown();

    /**
     * Initialise any newly loaded scripts and then put them into the completed
     * basket.
     *
     * @param timestamp The VRML time that the initialisation occured at
     */
    public void initializeScripts(double timestamp);

    /**
     * Setup the scripts for the new timestamp. For X3D, this will also call
     * the prepareEvents() method/function on the script, if it has one.
     *
     * @param timestamp The current time in VRML time
     */
    public void prepareEvents(double timestamp);

    /**
     * Process any events that scripts need to send. This looks for any inputs
     * that have changed, and also any outputs and propogates the values to the
     * eventOuts of the script. If the eventOuts are routed somewhere, it is
     * the RouteManager's responsibility to look after sending those changed
     * values.
     */
    public void processEvents();

    /**
     * Notification that all of the processing is finished and that
     * eventsProcessed() should now be called.
     */
    public void eventsProcessed();

    /**
     * Remove this list of scripts from active service. They have been deleted
     * from the scene graph and therefore the listener should no longer be
     * registered with them.
     *
     * @param list The list of scripts to remove
     */
    public void removeScripts(NodeArray list);

    /**
     * Queue the scripts to add to the scene.
     *
     * @param list the list of scripts to add
     */
    public void addScripts(NodeArray list);

    /**
     * Copy all the processed scripts to date into the given list. After this
     * is done, clear the internal collection. The calling script should be
     * empty because it will replace the contents of the given list with this
     * collection of values.
     *
     * @param list The list to copy values into
     */
    public void getProcessedScripts(NodeArray list);
}

