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

package org.xj3d.impl.core.loading;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.xj3d.core.loading.*;

import org.web3d.util.ErrorReporter;
import org.web3d.util.IntHashMap;
import org.web3d.vrml.nodes.VRMLScriptNodeType;
import org.web3d.vrml.scripting.ScriptEngine;

/**
 * A utility class who's sole job is to take a script node and load the
 * contents and get the runtime items happening.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class DefaultScriptLoader implements ScriptLoader {

    /**
     * The cache that we use. Everyone joins in in order to get maximum
     * amount of caching.
     */
    private static FileCache cache = new WeakRefFileCache();

    /** Pool manager of our script loading threads */
    private LoaderThreadPool loaderPool;

    /** The shared queue that all threads share */
    private ContentLoadQueue pending;

    /** The map of objects working in progress */
    private Map inProgress;

    /** Mapping of content type (key) to the supporting engine (value) */
    private IntHashMap engineMap;

    /** Status listener for load state */
    private ScriptLoadStatusListener statusListener;

    /**
     * Create a new script loader that represents the given browser.
     */
    public DefaultScriptLoader() {
        engineMap = new IntHashMap();
        loaderPool = LoaderThreadPool.getLoaderThreadPool();
        pending = loaderPool.getWaitingList();
        inProgress = loaderPool.getProgressMap();
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        loaderPool.setErrorReporter(reporter);
    }

    /**
     * Register a new scripting engine with this loader. There can be only one
     * scripting engine per content type so if the new engine supports a
     * content type that is already registered, the new engine will replace the
     * old one.
     *
     * @param engine The new engine instance to register
     */
    public void registerScriptingEngine(ScriptEngine engine) {
        String[] types = engine.getSupportedContentTypes();
        int spec_version = engine.getSupportedSpecificationVersion();
        HashMap spec_map = (HashMap)engineMap.get(spec_version);

        if(spec_map == null) {
            spec_map = new HashMap();
            engineMap.put(spec_version, spec_map);
        }

        for(int i = 0; i < types.length; i++)
            spec_map.put(types[i], engine);
    }

    /**
     * Unregister a the scripting engine with this loader. If the engine was
     * not registered in the first place, this method is ignored.
     *
     * @param engine The engine instance to deregister
     */
    public void unregisterScriptingEngine(ScriptEngine engine) {
        String[] types = engine.getSupportedContentTypes();
        int spec_version = engine.getSupportedSpecificationVersion();
        HashMap spec_map = (HashMap)engineMap.get(spec_version);

        for(int i = 0; i < types.length; i++) {
            Object suspect = spec_map.get(types[i]);

            if(suspect == engine)
                spec_map.remove(types[i]);
        }
    }

    /**
     * Set the script load status listener for this loader. A null value is
     * used to clear the reference.
     *
     * @param l The listener instance to use or null
     */
    public void setStatusListener(ScriptLoadStatusListener l) {
        statusListener = l;
    }

    /**
     * Attempt to load the script. Queues the script and lets the internals
     * deal with it.
     *
     * @param script The script instance to load
     */
    public void loadScript(VRMLScriptNodeType script) {

        // check to see if the node is currently in the loading process.
        // If it is anything but "not yet loaded" we don't queue it up.
        if(script.getLoadState() != VRMLScriptNodeType.NOT_LOADED)
            return;

        // If there is nothing specified, mark the script as loaded. Notify the
        // listener directly and then exit.
        String[] url = script.getUrl();
        if((url == null) || (url.length == 0)) {
            script.setLoadState(VRMLScriptNodeType.LOAD_COMPLETE);
            statusListener.loadCompleted(script);
        } else {
            // Insure threads are still around
            loaderPool.restartThreads();

            ScriptLoadDetails details = new ScriptLoadDetails();

            details.fieldIndex = -1;
            details.node = script;
            details.engineMap = engineMap;
            details.statusListener = statusListener;

            String[] urls = script.getUrl();

            if(inProgress.containsKey(urls)) {
                LoadRequest lrq = (LoadRequest)inProgress.get(urls);
                lrq.loadList.add(details);
            } else {
                ScriptLoadHandler handler = new ScriptLoadHandler(cache);
                pending.add(LoadConstants.SORT_SCRIPT,
                            urls,
                            handler,
                            details);
            }
        }
    }

    /**
     * Get the number of items in progress of loading.
     *
     * @return The number of items queued.
     */
    public int getNumberInProgress() {
        int ret_val = pending.size() + inProgress.size();

        return ret_val;
    }

    /**
     * Notification that the manager needs to shut down all the currently
     * running threads. Normally called when the application is exiting, so it
     * is not expected that the manager needs to act sanely after this method
     * has been called.
     */
    public void shutdown() {
        loaderPool.shutdown();
    }
}
