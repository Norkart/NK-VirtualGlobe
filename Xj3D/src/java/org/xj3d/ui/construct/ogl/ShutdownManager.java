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

package org.xj3d.ui.construct.ogl;

// External imports
import java.security.AccessController;
import java.security.PrivilegedAction;

// Local imports
import org.web3d.browser.BrowserCoreListener;

import org.web3d.vrml.nodes.VRMLScene;

import org.xj3d.core.loading.ScriptLoader;

/**
 * A function module that performs a controlled and complete shutdown
 * of the browser constructs internals when either the browser or the
 * application are exiting.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class ShutdownManager implements Runnable, BrowserCoreListener {
    
    /** The logging identifer of this class */
    private static final String LOG_NAME = "ShutdownManager";
    
    /** The construct instance */
    protected OGLConstruct construct;
    
    /** Thread used to handle the system shutdown hook */
    protected Thread shutdownThread;

    /** 
     * Constructor
     * 
     * @param construct The OGLConstruct instance to manage
     */
    public ShutdownManager( OGLConstruct construct ) {
        if ( construct == null ) {
            throw new IllegalArgumentException( 
                LOG_NAME +": construct instance must be non-null" );
        }
        this.construct = construct;
        
        shutdownThread = new Thread( this, "Xj3D Shutdown Thread" );
        
        AccessController.doPrivileged(
            new PrivilegedAction( ) {
                public Object run( ) {
                    Runtime rt = Runtime.getRuntime( );
                    rt.addShutdownHook( shutdownThread );
                    return( null );
                }
            }
            );
        
        construct.core.addCoreListener( this );
    }
    
    //---------------------------------------------------------------
    // Method defined by Runnable
    //---------------------------------------------------------------

    /** The shutdown thread */
    public void run( ) {
        shutdown( true );
    }
    
    //---------------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //---------------------------------------------------------------

    /**
     * Ignored. Notification that the browser core is shutting down 
     * the current content.
     */
    public void browserShutdown( ) {
    }

    /**
     * The browser core has been disposed, all resources may be freed.
     */
    public void browserDisposed( ) {
        synchronized( this ) {
            if( shutdownThread != null ) {
                AccessController.doPrivileged(
                    new PrivilegedAction( ) {
                        public Object run( ) {
                            Runtime rt = Runtime.getRuntime( );
                            rt.removeShutdownHook( shutdownThread );
                            return( null );
                        }
                    } );
                shutdownThread = null;
            }
        }
        // when explicitly calling shutdown on the sceneManager,
        // ensure that the sceneManager's shutdown thread is 
        // cleaned up.
        construct.renderManager.disableInternalShutdown( );
        shutdown( false );
    }

    /**
     * Ignored. The browser core tried to load a URL and failed.
     *
     * @param msg An error message describing the failure
     */
    public void urlLoadFailed( String msg ) {
    }

    /**
     * Ignored. Notification that a world has been loaded.
     *
     * @param scene The new scene that has been loaded
     */
    public void browserInitialized( VRMLScene scene ) {
    }

    //---------------------------------------------------------------
    // Local methods.
    //---------------------------------------------------------------

    /** 
     * Do an orderly shutdown of the browser's resource managers 
     *
     * @param isShutdownThread Is this method being called from the 
     * Runtime shutdown hook.
     */
    protected void shutdown( boolean isShutdownThread ) {
        
        construct.getContentLoadManager( ).shutdown( );

        ScriptLoader scriptLoader = construct.getScriptLoader( );
        if( scriptLoader != null ) {
            scriptLoader.shutdown( );
        }

        construct.getEventModelEvaluator( ).shutdown( );

        if( !isShutdownThread ) {
            construct.renderManager.shutdown( );
        }
    }
}
