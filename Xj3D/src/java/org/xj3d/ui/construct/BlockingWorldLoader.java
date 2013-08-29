/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.ui.construct;

// External imports
import java.util.Map;

// Local imports
import org.web3d.browser.BrowserCoreListener;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.FrameStateListener;
import org.web3d.vrml.nodes.VRMLScene;

import org.web3d.vrml.sav.InputSource;

import org.xj3d.core.loading.ContentLoadQueue;
import org.xj3d.core.loading.LoaderThreadPool;
import org.xj3d.core.loading.WorldLoader;

/**
 * A function module that handles world loading and blocks until the
 * world is loaded and all the content loader threads return to their
 * idle state. Additionally this loader reduces the frame rate while
 * loading is in process to prevent unnecessary CPU time being spent 
 * on rendering.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class BlockingWorldLoader implements Runnable, 
	BrowserCoreListener, FrameStateListener {
    
    /** The logging identifer of this class */
    private static final String LOG_NAME = "BlockingWorldLoader";
    
    /** The construct instance to load */
    protected Construct construct;
    
    /** The preprocessor to invoke after the scene is loaded, 
     * and before the scene is set to the Construct */
    protected ScenePreprocessor preprocessor;
    
    /** The error reporting mechanism */
    protected ErrorReporter errorReporter;

    /** The source to load from */
    protected InputSource source;
    
    /** The completion status of the load, true for success, false otherwise. */
    protected boolean loadStatus;
    
    /** Synchronization variable, indicating that loader threads are active */
    protected boolean loadInProgress;
    
    /** Manager of the loader threads */
    protected LoaderThreadPool loaderPool;
    
    /** Shared queue of load tasks */
    protected ContentLoadQueue pending;
    
    /** Map of in progress loader tasks */
    protected Map inProgress;
    
    /** 
     * Constructor
     * 
     * @param construct The Construct instance to load to
     */
    public BlockingWorldLoader( Construct construct ) {
        if ( construct == null ) {
            throw new IllegalArgumentException( 
                LOG_NAME +": construct instance must be non-null" );
        }
        this.construct = construct;
        errorReporter = construct.errorReporter;
        
        loaderPool = LoaderThreadPool.getLoaderThreadPool( );
        pending = loaderPool.getWaitingList( );
        inProgress = loaderPool.getProgressMap( );
    }
    
    //----------------------------------------------------------
    // Methods defined by Runnable
    //----------------------------------------------------------
    
    /**
     * Thread that handles the initial world load
     */
    public void run( ) {
        
        WorldLoader loader = construct.worldLoader.fetchLoader( );
        
        VRMLScene parsed_scene = null;
        
        try {
            parsed_scene = loader.loadNow( construct.core, source );
            loadStatus = true;
        } catch( Exception e ) {
            errorReporter.errorReport( LOG_NAME +": Failed to load file", e );
            loadStatus = false;
        }
        
        construct.worldLoader.releaseLoader( loader );
        
        if ( loadStatus ) {
			if ( preprocessor != null ) {
				preprocessor.preprocess( parsed_scene, construct );
			}
            construct.core.setScene( parsed_scene, null );
        } else {
            synchronized ( this ) {
                loadInProgress = false;
                notify( );
            }
        }
    }
    
    //----------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //----------------------------------------------------------
    
    /**
     * The browser has been initialised with new content. The content given
     * is found in the accompanying scene and description.
     *
     * @param scene The scene of the new content
     */
    public void browserInitialized( VRMLScene scene ) {
        // schedule a listener at the end of the frame to determine whether 
        // additional loading is required before the scene is 'complete'
        construct.stateManager.addEndOfThisFrameListener( this );
    }
    
    /**
     * Ignored. The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed( String msg ) {
    }
    
    /**
     * Ignored. The browser has been shut down and the previous
     * content is no longer valid.
     */
    public void browserShutdown( ) {
    }
    
    /**
     * Ignored. The browser has been disposed, all resources may be freed.
     */
    public void browserDisposed( ) {
    }
    
    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------
    
    public void allEventsComplete( ) {
        boolean isLoading = isLoadingInProgress( );
        if ( isLoading ) {
            // reschedule the listener until all loading threads are idle
            construct.stateManager.addEndOfThisFrameListener( this );
        } else {
            synchronized ( this ) {
                loadInProgress = false;
                notify( );
            }
        }
    }
	
    //----------------------------------------------------------
    // Local methods.
    //----------------------------------------------------------
    
    /** 
     * Load the world from the specified source, return when all
     * the loading threads have returned to the idle state.
     *
     * @param source The source to load
     */
    public boolean load( InputSource source ) {
        this.source = source;
        construct.core.setMinimumFrameInterval( 250, false );
        construct.core.addCoreListener( this );
        Thread thread = new Thread( this, "Xj3D World Loader" );
        thread.start( );
        try {
            synchronized ( this ) {
                loadInProgress = true;
                while ( loadInProgress ) {
                    wait( );
                }
            }
        } catch ( InterruptedException ie ) {
        }
        construct.core.removeCoreListener( this );
        construct.core.setMinimumFrameInterval( 0, false );
        return ( loadStatus );
    }
    
    /**
     * Return whether loading threads are active
     *
     * @return whether loading threads are active. true
     * if there are loading threads active, false if the 
     * loading threads are idle.
     */
    public boolean isLoadingInProgress( ) {
        
        int num_pending = pending.size( );
        int num_in_progress = inProgress.size( );
        int num_world_ldr = construct.worldLoader.getNumberLoadersActive( );
        
        int total = num_pending + num_in_progress + num_world_ldr;
            
        if ( false ) {
            System.out.println( 
                "Content Loader Threads: Pending = "+ num_pending +
                ", In Progress = "+ num_in_progress +", World Loaders = "+ num_world_ldr );
        }
        return( ( total > 0 ) );
    }
	
	/**
	 * Set the preprocessor module
	 *
	 * @param preprocessor The ScenePreprocessor module. If null, this clears
	 * any previous preprocessor module.
	 */
	public void setScenePreprocessor( ScenePreprocessor preprocessor ) {
		this.preprocessor = preprocessor;
	}
}
