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
import java.util.Map;
import java.util.LinkedList;

import org.ietf.uri.event.ProgressListener;

// Local imports
import org.xj3d.core.loading.*;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;

import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.VRMLExecutionSpace;

import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;

import org.web3d.vrml.parser.VRMLParserFactory;

import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.VRMLParseException;

import org.xj3d.core.eventmodel.RouteManager;

/**
 * Independent thread used to load a world from a list of URLs and then
 * place it in the given node.
 * <p>
 *
 * This implementation is designed to work as both a loadURL() and
 * createVrmlFromUrl() call handler. The difference is defined by what data
 * is supplied to the thread. If the target node is specified, then we assume
 * that the caller wants us to put the results there. If it is null, then
 * assume that we're doing a loadURL call and replace the entire world.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class DefaultWorldLoaderManager implements WorldLoaderManager, BrowserCoreListener {
    
    /**
     * The cache that we use. Everyone joins in in order to get maximum
     * amount of caching.
     */
    private static FileCache cache = new WeakRefFileCache();
    
    /** The browser core instance used for world loading */
    private BrowserCore browserCore;
    
    /** The manager for route handling */
    private RouteManager routeManager;
    
    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;
    
    /** The frame state manager for nodes loaded by this class */
    private FrameStateManager stateManager;
    
    /** Blocking queue that contains the available list of managers */
    private LinkedList availableLoaders;
    
    /** Set of all loaders created */
    private HashSet allLoaders;
    
    /** Loader thread pool in use */
    private LoaderThreadPool loaderPool;
    
    /** The progress listener for loadURL calls */
    private ProgressListener progressListener;
    
    /**
     * Construct a new instance of the world loader that uses the given
     * frame state manager.
     *
     * @param core The core of the browser for fetching info from
     * @param fsm The state manager for this loader to use
     */
    public DefaultWorldLoaderManager(BrowserCore core,
        FrameStateManager fsm,
        RouteManager rm) {
        stateManager = fsm;
        browserCore = core;
        routeManager = rm;
        
        loaderPool = LoaderThreadPool.getLoaderThreadPool();
        
        availableLoaders = new LinkedList();
        allLoaders = new HashSet();
        
        errorReporter = DefaultErrorReporter.getDefaultReporter();
        
        browserCore.addCoreListener( this );
    }
    
    //-----------------------------------------------------------------------
    // Methods defined by WorldLoaderManager
    //-----------------------------------------------------------------------

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
        
        if(allLoaders.size() != 0) {
            Object[] ldr_list = allLoaders.toArray();
            
            for(int i = 0; i < ldr_list.length; i++) {
                DefaultWorldLoader ldr = (DefaultWorldLoader)ldr_list[i];
                ldr.setErrorReporter(errorReporter);
            }
        }
        
        loaderPool.setErrorReporter(errorReporter);
    }
    
    /**
     * Register a progress listener with the engine so progress can be tracked.  Only
     * loadURL calls will issue progress, not createURL.
     *
     * @param listener The instance to use or null
     */
    public void setProgressListener(ProgressListener listener) {
        progressListener = listener;
    }
    
    /**
     * Queue a request for a loadURL call.
     *
     * @param urls List of urls to load
     * @param params The parameters to accompany the URLs
     */
    public void queueLoadURL(String[] urls, Map params) {
        
        // Load URL is always using the last call. If any others are in
        // progress, kill them and make this one the current one. The minor
        // technical issue with this bug is that if there are more than one
        // window open on the same classloader, this will kill all loading
        // in all windows. See bug ID 239.
        loaderPool.clear();
        
        WorldLoadDetails details = new WorldLoadDetails();
        details.isLoadURL = true;
        details.core = browserCore;
        details.worldLoader = this;
        details.params = params;
        details.progressListener = progressListener;
        
        ContentLoadQueue loader = loaderPool.getWaitingList();
        
        loader.add(LoadConstants.SORT_LOAD_URL,
            urls,
            new WorldLoadHandler(cache),
            details);
    }
    
    /**
     * Queue a request for a loadURL call.
     *
     * @param urls List of urls to load
     * @param node The node to send the values to
     * @param field index of the field to write to
     * @param space The space the script is in for route adding
     */
    public void queueCreateURL(String[] urls,
        VRMLNodeType node,
        int field,
        VRMLExecutionSpace space) {
        
        WorldLoadDetails details = new WorldLoadDetails();
        details.isLoadURL = false;
        details.core = browserCore;
        details.worldLoader = this;
        details.node = node;
        details.fieldIndex = field;
        details.routeManager = routeManager;
        details.space = space;
        
        BasicScene sc = space.getContainedScene();
        details.majorVersion = sc.getSpecificationMajorVersion();
        details.minorVersion = sc.getSpecificationMinorVersion();
        
        ContentLoadQueue loader = loaderPool.getWaitingList();
        loader.add(LoadConstants.SORT_CREATE,
            urls,
            new WorldLoadHandler(cache),
            details);
    }
    
    /**
     * Fetch a world loader instance from the global pool to work on
     * loading of a world.
     *
     * @return A loader instance to use
     */
    public synchronized WorldLoader fetchLoader() {
        
        WorldLoader ret_val = null;
        
        if(availableLoaders.size() != 0)
            ret_val = (WorldLoader)availableLoaders.remove(0);
        else {
            DefaultWorldLoader ldr = new DefaultWorldLoader(stateManager);
            ldr.setErrorReporter(errorReporter);
            
            allLoaders.add(ldr);
            ret_val = ldr;
        }
        
        return ret_val;
    }
    
    /**
     * Release a currently used world loader back into the cache for others to
     * make use of.
     *
     * @param loader The instance to return
     */
    public synchronized void releaseLoader(WorldLoader loader) {
        availableLoaders.add(loader);
    }
    
    /**
     * Register the scene builder factory to be used for the given renderer
     * type. There can only be one for any given renderer type (where the type
     * value is defined by the constants in
     * {@link org.web3d.browser.BrowserCore}. If the factory instance is
     * null, it will clear the facctory for the given renderer type from the
     * map.
     *
     * @param renderer The ID of the renderer type
     * @param factory The instance of the factory to use
     */
    public void registerBuilderFactory(int renderer,
        SceneBuilderFactory factory) {
        DefaultWorldLoader.registerBuilderFactory(renderer, factory);
    }
    
    /**
     * Get the factory for the given renderer type. If no factory exists
     * return null.
     *
     * @param renderer The ID of the renderer type
     * @return The instance of the factory or null
     */
    public SceneBuilderFactory getBuilderFactory(int renderer) {
        return DefaultWorldLoader.getBuilderFactory(renderer);
    }
    
    /**
     * Register the parser factory to be used for the given renderer
     * type. There can only be one for any given renderer type (where the type
     * value is defined by the constants in
     * {@link org.web3d.browser.BrowserCore}. If the factory instance is
     * null, it will clear the facctory for the given renderer type from the
     * map.
     *
     * @param renderer The ID of the renderer type
     * @param factory The instance of the factory to use
     */
    public void registerParserFactory(int renderer, VRMLParserFactory factory) {
        DefaultWorldLoader.registerParserFactory(renderer, factory);
    }
    
    /**
     * Get the factory for the given renderer type. If no factory exists
     * return null.
     *
     * @param renderer The ID of the renderer type
     * @return The instance of the factory or null
     */
    public VRMLParserFactory getParserFactory(int renderer) {
        return DefaultWorldLoader.getParserFactory(renderer);
    }
    
    /**
     * Return the number of WorldLoaders that are currently allocated
     * from the pool and active loading.
     *
     * @return The number of WorldLoaders currently active.
     */
     public int getNumberLoadersActive( ) {
        return( allLoaders.size( ) - availableLoaders.size( ) );
    }

    //-----------------------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //-----------------------------------------------------------------------

    /**
     * The browser has been initialised with new content. The content given
     * is found in the accompanying scene and description.
     *
     * @param scene The scene of the new content
     */
    public void browserInitialized(VRMLScene scene) {
    }
    
    /**
     * The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed(String msg) {
    }
    
    /**
     * The browser has been shut down and the previous content is no longer
     * valid.
     */
    public void browserShutdown() {
    }
    
    /**
     * The browser has been disposed, all resources may be freed.
     */
    public void browserDisposed() {
        synchronized ( this ) {
            if(allLoaders.size() != 0) {
                Object[] ldr_list = allLoaders.toArray();
                
                for(int i = 0; i < ldr_list.length; i++) {
                    DefaultWorldLoader ldr = (DefaultWorldLoader)ldr_list[i];
                    ldr.shutdown();
                }
                allLoaders.clear( );
                availableLoaders.clear( );
            }
        }
    }
}
