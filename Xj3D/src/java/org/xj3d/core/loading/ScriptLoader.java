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

package org.xj3d.core.loading;

// External imports
import java.util.List;

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLScriptNodeType;
import org.web3d.vrml.scripting.ScriptEngine;

/**
 * Abstract representation of a class that can be used to provide script
 * loading and initlisation facilities to the browser core.
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
 * @version $Revision: 1.2 $
 */
public interface ScriptLoader {

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
     * Register a new scripting engine with this loader. There can be only one
     * scripting engine per content type so if the new engine supports a
     * content type that is already registered, the new engine will replace the
     * old one.
     *
     * @param engine The new engine instance to register
     */
    public void registerScriptingEngine(ScriptEngine engine);

    /**
     * Unregister a the scripting engine with this loader. If the engine was
     * not registered in the first place, this method is ignored.
     *
     * @param engine The engine instance to deregister
     */
    public void unregisterScriptingEngine(ScriptEngine engine);

    /**
     * Set the script load status listener for this loader. A null value is
     * used to clear the reference.
     *
     * @param l The listener instance to use or null
     */
    public void setStatusListener(ScriptLoadStatusListener l);

    /**
     * Attempt to load the script. Queues the script and lets the internals
     * deal with it.
     *
     * @param script The script instance to load
     */
    public void loadScript(VRMLScriptNodeType script);

    /**
     * Get the number of items in progress of loading.
     *
     * @return The number of items queued.
     */
    public int getNumberInProgress();

    /**
     * Notification that the manager needs to shut down all the currently
     * running threads. Normally called when the application is exiting, so it
     * is not expected that the manager needs to act sanely after this method
     * has been called.
     */
    public void shutdown();
}
