/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.ui.swt.browser.ogl;

// External imports
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.Map;
import java.util.Properties;

import javax.media.opengl.GL;
import javax.media.opengl.GLCapabilities;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPartReference;

import org.eclipse.ui.part.ViewPart;

import org.ietf.uri.*;

import org.ietf.uri.event.ProgressListener;

import org.j3d.aviatrix3d.output.graphics.ElumensSWTSurface;
import org.j3d.aviatrix3d.output.graphics.SimpleSWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

// Local imports
import org.web3d.browser.BrowserComponent;
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.browser.Xj3DConstants;

import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.Web3DResourceFactory;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.VRMLScene;

import org.web3d.vrml.renderer.ogl.browser.OGLBrowserCanvas;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;

import org.xj3d.core.eventmodel.DeviceFactory;
import org.xj3d.core.eventmodel.ViewpointManager;

import org.xj3d.core.loading.WorldLoaderManager;

import org.xj3d.sai.BrowserConfig;

import org.xj3d.ui.swt.device.SWTDeviceFactory;

import org.xj3d.ui.swt.net.content.SWTContentHandlerFactory;

import org.xj3d.ui.swt.widgets.ConsoleTool;
import org.xj3d.ui.swt.widgets.ConsoleWindow;
import org.xj3d.ui.swt.widgets.CursorManager;
import org.xj3d.ui.swt.widgets.ImageLoader;
import org.xj3d.ui.swt.widgets.LoadProgressListener;
import org.xj3d.ui.swt.widgets.LocationToolbar;
import org.xj3d.ui.swt.widgets.MessageBuffer;
import org.xj3d.ui.swt.widgets.NavigationToolbar;
import org.xj3d.ui.swt.widgets.StatusBar;
import org.xj3d.ui.swt.widgets.ViewpointToolbar;

/**
 * Common SWT implementation of the browser component for use in
 * either SAI or EAI, that wraps the functionality of an X3D browser
 * into a convenient, easy to use form.
 * <p>
 *
 * This base class needs to be extended to provide the SAI or EAI-specific
 * implementation interfaces, as well as any startup required for either of
 * those environments, such as scripting engines etc.
 *
 * @author Rex Melton
 * @version $Revision: 1.25 $
 */
public abstract class BrowserComposite extends Composite implements
    BrowserComponent, BrowserCoreListener, ShellListener, DisposeListener,
    KeyListener, IPartListener2 {

    /** Initialization message */
    private static final String INITIALIZATION_MSG =
        "Initializing SWT OpenGL X3D browser";

    /** Error message when setting up the system properties */
    private static final String PROPERTY_SETUP_ERR =
        "Error setting up system properties in BrowserComposite";

    /** Wireframe rendering mode message */
    private static final String WIREFRAME_RENDERING_MODE_MSG =
        "Wireframe rendering mode enabled";

    /** Point rendering mode message */
    private static final String POINT_RENDERING_MODE_MSG =
        "Point rendering mode enabled";

    /** SHaded rendering mode message */
    private static final String SHADED_RENDERING_MODE_MSG =
        "Shaded rendering mode enabled";

    /** Keycode for the 'a' key */
    private static final int A_KEY = 0x0061;

    /** Keycode for the 'e' key */
    private static final int E_KEY = 0x0065;

    /** Keycode for the 'f' key */
    private static final int F_KEY = 0x0066;

    /** Keycode for the 'p' key */
    private static final int P_KEY = 0x0070;

    /** Keycode for the 'w' key */
    private static final int W_KEY = 0x0077;

    /** Keycode for the 'z' key */
    private static final int Z_KEY = 0x007A;

    /** The minimum frame period when the browser is visible.
     *  Essentially - as fast as it can go, but limited by user configuration */
    private static final int FRAME_PERIOD_VISIBLE = 0;

    /** The minimum frame period when the browser is hidden.
     *  Equivalent to 1 frame per second */
    private static final int FRAME_PERIOD_HIDDEN = 1000;

    /** The shell ancestor of this composite */
    private Shell shell;

    /** The composite parent of the surface object */
    private Composite surfaceComposite;

    /** The real component that is being rendered to */
    private Canvas glCanvas;

    /** The manager of viewpoint information */
    private ViewpointManager viewpointManager;

    /** The manager of cursors */
    protected CursorManager cursorManager;

    /** The dialog that manages the display of messages from the message buffer */
    private ConsoleWindow console;

    /** The toolbar holding viewpoint information */
    private ViewpointToolbar vpToolbar;

    /** The toolbar holding navigation information */
    protected NavigationToolbar navToolbar;

    /** The toolbar holding location information */
    protected LocationToolbar locToolbar;

    /** The status bar */
    protected StatusBar statusBar;

    /** Area to push error messages to */
    protected MessageBuffer messageBuffer;

    /** Progress listener on the connection */
    private ProgressListener loadProgressListener;

    /** The canvas used to display the world */
    protected OGLBrowserCanvas mainCanvas;

    /** The internal browser core */
    protected OGLStandardBrowserCore browserCore;

    /** Wireframe or filled mode */
    private boolean wireframe;

    /** point or filled mode */
    private boolean pointrender;

    /** Are we in Elumens Spherical mode */
    private boolean elumensMode;

    /** The glCapabilities choosen */
    private GLCapabilities caps;

    /** Number of antialiasing samples */
    private int numSamples;

    /** HAve we used the sample chooser yet? */
    private boolean maxChooserStarted;

    /** Chooser for dealing with max multisampling */
    //private SampleChooser sampleChooser;

    /** Flag indicating that the parent shell is active and visible */
    private boolean shellIsVisible;

    /** Flag indicating that this view is active and visible */
    private boolean viewIsVisible;

    /** The Eclipse ViewPart that this browser is associated with. Used to identify
     *  part service events of interest to this instance of a browser */
    private ViewPart viewPart;

    /** The workbench window part service associated with the ViewPart. Used
     *  to establish the part service listener */
    private IPartService partService;

    /**
     * Create an instance of a Composite configured per the arguments.
     *
     * @param parentComposite the SWT Composite widget that this will be added to
     * @param viewPart The Eclipse ViewPart that this browser is associated with.
     * May be null if the browser is running standalone in SWT.
     * @param parameters The object containing the browser's configuration parameters
     */
    protected BrowserComposite(
        Composite parentComposite,
        ViewPart viewPart,
        BrowserConfig parameters ) {

        super( parentComposite, SWT.NONE );
        addDisposeListener( this );

        if ( viewPart != null ) {
            this.viewPart = viewPart;
            partService = viewPart.getViewSite( ).getWorkbenchWindow( ).getPartService( );
            partService.addPartListener( this );
        }

        Properties skinProperties =
            ( parameters.browserSkin == null ) ? new Properties( ) : parameters.browserSkin;
        if ( parameters.resourceMap != null ) {
            ImageLoader.initializeCache( parameters.resourceMap );
        }

        GridLayout gridLayout = new GridLayout( );
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        super.setLayout( gridLayout );

        GridData gridData;
        Composite topComposite = null;
        if ( ( parameters.showDash && parameters.dashTop ) ||
            ( parameters.showUrl && parameters.urlTop ) ||
            ( parameters.showDash && parameters.showUrl ) ) {

            // one way or another we need a composite at the top
            // add it to the layout now
            topComposite = new Composite( this, SWT.NONE );

            // we'll worry about how to fill it in later....
            topComposite.setLayout( new FillLayout( ) );
            gridData = new GridData( );
            gridData.horizontalAlignment = GridData.FILL;
            gridData.grabExcessHorizontalSpace = true;
            topComposite.setLayoutData( gridData );

            // we've preserved the arguments that come from the awt factory impl,
            // but they are prone to ambiguity about where the toolbars
            // go - so we'll sort that out immediately
            if ( ( parameters.showDash && parameters.showUrl ) &&
                ( parameters.dashTop == parameters.urlTop ) ) {

                // the user has specified that they want both location and
                // navigation toolbars - and they want them both in the same
                // place. clearly the user is confused. the location bar will
                // go on top - end of story.
                parameters.urlTop = true;
                parameters.dashTop = false;
            }
        }

        shell = parentComposite.getShell( );
        shell.addShellListener( this );
        shellIsVisible = true;
        viewIsVisible = true;

        messageBuffer = new MessageBuffer( );
        messageBuffer.messageReport( INITIALIZATION_MSG );

        numSamples = 1;
        wireframe = false;
        pointrender = false;
        maxChooserStarted = false;

        setSize(800, 600);

        caps = new GLCapabilities( );
        caps.setDoubleBuffered( true );
        caps.setHardwareAccelerated( true );

        if ( parameters.antialiased ) {
            caps.setSampleBuffers( true );
            if( parameters.antialiasingQuality.equals( "low" ) ) {
                numSamples = 2;
                caps.setNumSamples( numSamples );
            } else if( parameters.antialiasingQuality.equals( "medium" ) ) {
                // TODO: Really need to find the max allowable.
                // But JOGL startup issues make this a problem
                messageBuffer.messageReport( "Trying for 4 samples of antialiasing." );
                numSamples = 4;
                caps.setNumSamples(numSamples);
            } else if( parameters.antialiasingQuality.equals( "high" ) ) {
                messageBuffer.messageReport( "Trying for 8 samples of antialiasing." );
                numSamples = 8;
                caps.setNumSamples( numSamples );
            }
        }

        surfaceComposite = new Composite( this, SWT.NONE );
        surfaceComposite.setLayout( new FillLayout( ) );
        SimpleSWTSurface surface = new SimpleSWTSurface( surfaceComposite, SWT.NONE, caps );

        gridData = new GridData( );
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
        surfaceComposite.setLayoutData( gridData );

        glCanvas = (Canvas)surface.getSurfaceObject( );
        glCanvas.addKeyListener( this );

        DeviceFactory devFactory = new SWTDeviceFactory(
            glCanvas,
            Xj3DConstants.OPENGL_ID,
            surface,
            messageBuffer );

        mainCanvas = new OGLBrowserCanvas( surface, devFactory, parameters );
        mainCanvas.initialize( );
        mainCanvas.setErrorReporter( messageBuffer );

        cursorManager = new CursorManager(
            glCanvas,
            skinProperties,
            messageBuffer );

        browserCore = mainCanvas.getUniverse( );
        browserCore.addCoreListener( this );
        browserCore.addSensorStatusListener( cursorManager );
        browserCore.addNavigationStateListener( cursorManager );

        viewpointManager = mainCanvas.getViewpointManager( );

        WorldLoaderManager worldLoaderManager = mainCanvas.getWorldLoaderManager( );

        setupProperties( browserCore, worldLoaderManager, parameters.textureQuality );

        // setup the remainder of the ui
        console = new ConsoleWindow( shell, messageBuffer );

        if( parameters.showUrl ) {
            if ( parameters.urlTop ) {
                locToolbar =
                    new LocationToolbar(
                    topComposite,
                    browserCore,
                    worldLoaderManager,
                    parameters.urlReadOnly,
                    parameters.showOpenButton,
                    parameters.showReloadButton,
                    parameters.contentDirectory,
                    skinProperties,
                    messageBuffer );
            } else {
                locToolbar =
                    new LocationToolbar(
                    this,
                    browserCore,
                    worldLoaderManager,
                    parameters.urlReadOnly,
                    parameters.showOpenButton,
                    parameters.showReloadButton,
                    parameters.contentDirectory,
                    skinProperties,
                    messageBuffer );

                gridData = new GridData( );
                gridData.horizontalAlignment = GridData.FILL;
                gridData.grabExcessHorizontalSpace = true;
                locToolbar.setLayoutData( gridData );
            }
        }
        if ( parameters.showDash ) {
            Composite dashComposite;
            if ( parameters.dashTop ) {
                dashComposite = new Composite( topComposite, SWT.NONE );
            }
            else {
                dashComposite = new Composite( this, SWT.NONE );

                gridData = new GridData( );
                gridData.horizontalAlignment = GridData.FILL;
                gridData.grabExcessHorizontalSpace = true;
                dashComposite.setLayoutData( gridData );
            }

            gridLayout = new GridLayout( );
            gridLayout.marginHeight = 0;
            gridLayout.marginWidth = 0;
            gridLayout.verticalSpacing = 0;
            gridLayout.horizontalSpacing = 0;
            gridLayout.numColumns = 3;
            gridLayout.makeColumnsEqualWidth = false;
            dashComposite.setLayout( gridLayout );

            navToolbar = new NavigationToolbar(
                dashComposite,
                browserCore,
                skinProperties,
                messageBuffer );

            gridData = new GridData( );
            navToolbar.setLayoutData( gridData );

            ViewpointManager vp_mgr = mainCanvas.getViewpointManager( );
            vpToolbar = new ViewpointToolbar(
                dashComposite,
                browserCore,
                vp_mgr,
                skinProperties,
                messageBuffer );

            gridData = new GridData( );
            gridData.horizontalAlignment = GridData.FILL;
            gridData.grabExcessHorizontalSpace = true;
            vpToolbar.setLayoutData( gridData );

            ConsoleTool consoleTool = new ConsoleTool(
                dashComposite,
                console,
                skinProperties,
                messageBuffer );
        }
        if ( parameters.showFPS || parameters.showStatusBar ) {
            statusBar = new StatusBar(
                this,
                browserCore,
                parameters.showStatusBar,
                parameters.showFPS,
                skinProperties );

            gridData = new GridData( );
            gridData.horizontalAlignment = GridData.FILL;
            gridData.grabExcessHorizontalSpace = true;
            statusBar.setLayoutData( gridData );
        }
        if ( parameters.showStatusBar ) {
            loadProgressListener = new LoadProgressListener( statusBar, messageBuffer );
        }
        else {
            loadProgressListener = new LoadProgressListener( null, messageBuffer );
        }
        ResourceConnection.addGlobalProgressListener( loadProgressListener );
        worldLoaderManager.setProgressListener( loadProgressListener );

        if ( parameters.showConsole ) {
            console.open( );
        }
        /*
        getMaximumNumSamples();
        */

        mainCanvas.setEnabled(true);
    }

    //---------------------------------------------------------
    // Methods overridden in Composite
    //---------------------------------------------------------

    /** Do nothing, we do our own layout */
    public void setLayout( Layout layout ) {
    }

    //----------------------------------------------------------
    // Methods defined by KeyListener
    //----------------------------------------------------------

    /**
     * Notification that a key is pressed. Ignored
     *
     * @param ke The event that caused this method to be called
     */
    public void keyPressed( KeyEvent ke ) {
    }

    /**
     * Notification that a key is released.
     *
     * @param ke The event that caused this method to be called
     */
    public void keyReleased( KeyEvent ke ) {
        int keyCode = ke.keyCode;
        switch( ke.stateMask ) {
        case SWT.NONE:
            switch( keyCode ) {
            case SWT.PAGE_DOWN:
                viewpointManager.nextViewpoint( );
                break;

            case SWT.PAGE_UP:
                viewpointManager.previousViewpoint( );
                break;

            case SWT.HOME:
                viewpointManager.firstViewpoint( );
                break;

            case SWT.END:
                viewpointManager.lastViewpoint( );
                break;
            }
            break;

        case SWT.ALT:
            if ( keyCode == A_KEY ) {
                /*
                numSamples = numSamples * 2;

                int max = getMaximumNumSamples();

                // Busy wait till answer comes.  Should already be here
                while(max < 0) {
                try {
                Thread.sleep(50);
                } catch(Exception e2) {}
                max = getMaximumNumSamples();
                }

                if(numSamples > max)
                numSamples = 1;

                if(statusBar != null)
                statusBar.setStatusText("Antialiasing samples: " +
                numSamples + " out of max: " +
                max);

                caps.setSampleBuffers(true);
                caps.setNumSamples(numSamples);

                resetSurface();
                */
            } else if ( keyCode == P_KEY ) {
                pointrender = !pointrender;
                if ( pointrender ) {
                    setStatusText( POINT_RENDERING_MODE_MSG );
                    browserCore.setRenderingStyle( Xj3DConstants.RENDER_POINTS );
                } else if( wireframe ) {
                    setStatusText( WIREFRAME_RENDERING_MODE_MSG );
                    browserCore.setRenderingStyle( Xj3DConstants.RENDER_LINES );
                } else {
                    setStatusText( SHADED_RENDERING_MODE_MSG );
                    browserCore.setRenderingStyle( Xj3DConstants.RENDER_SHADED );
                }
            } else if ( keyCode == W_KEY ) {
                wireframe = !wireframe;
                if ( wireframe ) {
                    setStatusText( WIREFRAME_RENDERING_MODE_MSG );
                    browserCore.setRenderingStyle( Xj3DConstants.RENDER_LINES );
                } else if( pointrender ) {
                    setStatusText( POINT_RENDERING_MODE_MSG );
                    browserCore.setRenderingStyle( Xj3DConstants.RENDER_POINTS );
                } else {
                    setStatusText( SHADED_RENDERING_MODE_MSG );
                    browserCore.setRenderingStyle( Xj3DConstants.RENDER_SHADED );
                }
            } else if ( keyCode == Z_KEY ) {
                // Enter/Exit Elumens mode
                //elumensMode = !elumensMode;
                resetSurface( );
            }
            break;

        case SWT.CTRL:
            if ( keyCode == E_KEY ) {
                browserCore.setNavigationMode( Xj3DConstants.EXAMINE_NAV_MODE );
            } else if ( keyCode == F_KEY ) {
                browserCore.setNavigationMode( Xj3DConstants.FLY_NAV_MODE );
            } else if ( keyCode == W_KEY ) {
                browserCore.setNavigationMode( Xj3DConstants.WALK_NAV_MODE );
            }
            break;
        }
    }

    //---------------------------------------------------------------
    // Methods defined by ShellListener
    //---------------------------------------------------------------

    /**
     *  Ignored. The shell has been given focus.
     *
     * @param evt The shell event that caused the method to be called.
     */
    public void shellActivated( ShellEvent evt ) {
    }

    /**
     * Ignored. The shell is being closed.
     *
     * @param evt The shell event that caused the method to be called.
     */
    public void shellClosed( ShellEvent evt ) {
    }

    /**
     * Ignored. The shell has lost focus.
     *
     * @param evt The shell event that caused the method to be called.
     */
    public void shellDeactivated( ShellEvent evt ) {
    }

    /**
     * The shell has changed from a minimized to a normal state.
     * Restore the browser frame rate to normal if it is visible.
     *
     * @param evt The shell event that caused the method to be called.
     */
    public void shellDeiconified( ShellEvent evt ) {
        shellIsVisible = true;
        configureThrottle( );
    }

    /**
     * The shell has changed from a normal state to minimized. Throttle
     * back the browser frame rate.
     *
     * @param evt The window event that caused the method to be called.
     */
    public void shellIconified( ShellEvent evt ) {
        shellIsVisible = false;
        configureThrottle( );
    }

    //----------------------------------------------------------
    // Methods defined by DisposeListener
    //----------------------------------------------------------

    /**
     * Notification we're being disposed of. Notify the browser's Canvas
     * to clean up in anticipation of it's dispose method being
     * called through the normal swt chain of destruction.
     * Notify the ImageLoader and CursorManager to dispose of
     * their graphics resources.
     */
    public void widgetDisposed( DisposeEvent evt )
    {
        // make sure that the browser is shutdown and disposed of.
        destroy( );
        // and then clean up locally
        console.close( );
        if ( partService != null ) {
            partService.removePartListener( this );
        }
        shell.removeShellListener( this );
        ImageLoader.dispose( );
        cursorManager.dispose( );
    }

    //----------------------------------------------------------
    // Methods defined by BrowserComponent
    //----------------------------------------------------------

    /**
     * Get the spec version that is supported.
     *
     * @return a number representing the spec major version
     */
    public int supportedSpecificationVersion() {
        return 3;
    }

    /**
     * Get the SWT component holding this browser.
     *
     * @return The component
     */
    public Object getCanvas() {
        return glCanvas;
    }

    /**
     * Get the renderer type.
     *
     * @return The BrowserCore type
     */
    public int getRendererType() {
        return browserCore.getRendererType();
    }

    /**
     * Get the core browser implementation.
     *
     * @return the BrowserCore
     */
    public BrowserCore getBrowserCore() {
        return browserCore;
    }

    /**
     * Fetch the error handler so that application code can post messages
     * too.
     *
     * @return The current error handler instance
     */
    public ErrorReporter getErrorReporter() {
        return messageBuffer;
    }

    /**
     * Set the minimum frame interval time to limit the CPU resources taken up
     * by the 3D renderer. By default it will use all of them. The second
     * parameter is used to control whether this is a user-set hard minimum or
     * something set by the browser internals. User set values are always
     * treated as the minimum unless the browser internals set a value that is
     * a slower framerate than the user set. If the browser then sets a faster
     * framerate than the user set value, the user value is used instead.
     *
     * @param millis The minimum time in milleseconds.
     * @param userSet true if this is an end-user set minimum
     */
    public void setMinimumFrameInterval(int millis, boolean userSet) {
        mainCanvas.setMinimumFrameInterval(millis, userSet);
    }

    /**
     * Called to instruct the component instance to start rendering now.
     */
    public void start() {
        mainCanvas.setEnabled(true);
    }

    /**
     * Called to instruct the component instance to stop and suspend its state.
     * The renderer should stop at this point.
     */
    public void stop() {
        mainCanvas.setEnabled(false);
    }

    /**
     * Called to instruct the component instance to destroy itself and any
     * used resources. It will not be used again.
     */
    public void destroy( ) {
        if ( browserCore != null ) {
            // if the user has already called dispose on X3DBrowserComposite
            // then the browser shutdown has already happened and the reference
            // to the BrowserCore will be null. otherwise - call dispose on the
            // BrowserCore directly.
            browserCore.dispose( );
        }
    }

    //----------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //----------------------------------------------------------

    /**
     * Ignored. Notification that the browser is shutting down the current content.
     */
    public void browserShutdown( ) {
    }

    /**
     * The browser has been disposed by the user calling the
     * dispose method on the ExternalBrowser instance. Release
     * our reference to the browser core so that when this control
     * is disposed, we don't bother with releasing browser resources.
     */
    public void browserDisposed( ) {
        browserCore = null;
        // rem !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // note: this is temporary to eliminate a persistant
        // reference to root that will prevent gc. this will
        // 'impact' other running browsers (if there are any).
        URI.setContentHandlerFactory( null );
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        ResourceConnection.removeGlobalProgressListener( loadProgressListener );
    }

    /**
     * Ignored. The browser tried to load a URL and failed.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed( String msg ) {
    }

    /**
     * Ignored. Notification that a world has been loaded into the browser.
     *
     * @param scene The new scene that has been loaded
     */
    public void browserInitialized( VRMLScene scene ) {
    }

    // ---------------------------------------------------------
    // Methods defined by IPartListener2
    // ---------------------------------------------------------

    /**
     * Ignored. Notification that the referenced part has been activated.
     *
     * @param partRef Reference object for the part that was activated.
     */
    public void partActivated( IWorkbenchPartReference partRef ) {
    }

    /**
     * Ignored. Notification that the referenced part was brought to the top.
     *
     * @param partRef Reference object for the part that was brought to the top.
     */
    public void partBroughtToTop( IWorkbenchPartReference partRef ) {
    }

    /**
     * Ignored. Notification that the referenced part was closed.
     *
     * @param partRef Reference object for the part that was closed.
     */
    public void partClosed( IWorkbenchPartReference partRef ) {
    }

    /**
     * Ignored. Notification that the referenced part was deactivated.
     *
     * @param partRef Reference object for the part that was deactivated.
     */
    public void partDeactivated( IWorkbenchPartReference partRef ) {
    }

    /**
     * Notification that the referenced part was hidden. Used to throttle
     * CPU usage while the browser is not visible on screen.
     *
     * @param partRef Reference object for the part that was hidden.
     */
    public void partHidden( IWorkbenchPartReference partRef ) {
        if ( partRef instanceof IViewReference ) {
            IViewReference viewRef = (IViewReference)partRef;
            if ( viewRef.getView( false ) == viewPart ) {
                viewIsVisible = false;
                configureThrottle( );
            }
        }
    }

    /**
     * Ignored. Notification that the referenced part was hidden.
     *
     * @param partRef Reference object for the part that was hidden.
     */
    public void partInputChanged( IWorkbenchPartReference partRef ) {
    }

    /**
     * Ignored. Notification that the referenced part was hidden.
     *
     * @param partRef Reference object for the part that was hidden.
     */
    public void partOpened( IWorkbenchPartReference partRef ) {
    }

    /**
     * Notification that the referenced part was made visible. Used to
     * establish rendering and event model CPU related parameters.
     *
     * @param partRef Reference object for the part that was made visible.
     */
    public void partVisible( IWorkbenchPartReference partRef ) {
        if ( partRef instanceof IViewReference ) {
            IViewReference viewRef = (IViewReference)partRef;
            if ( viewRef.getView( false ) == viewPart ) {
                viewIsVisible = true;
                configureThrottle( );
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Throttle back or restore rendering and event model processing
     * depending on the visibility of the ui widgets.
     */
    private void configureThrottle( ) {
        if ( shellIsVisible && viewIsVisible ) {
            mainCanvas.setMinimumFrameInterval( FRAME_PERIOD_VISIBLE, false );
            mainCanvas.enableRenderPipeline( true );
        } else {
            mainCanvas.setMinimumFrameInterval( FRAME_PERIOD_HIDDEN, false );
            mainCanvas.enableRenderPipeline( false );
        }
    }

    /**
     * Enable elumens mode.
     *
     * @param enabled Whether the mode is enabled.
     */
    public void setElumensMode(boolean enabled) {
        elumensMode = enabled;

        resetSurface();
    }
    /**
     * Is Elumens mode enabled?
     *
     * @return Whether the mode is enabled.
     */
    public boolean getElumensMode() {
        return elumensMode;
    }

    /**
     * Update the surface to change the rendering mode, if set.
     */
    private void resetSurface( ) {
        stop( );
        glCanvas.removeKeyListener( this );
        glCanvas.dispose( );

        GraphicsOutputDevice surface;

        if( elumensMode ) {
            surface = new ElumensSWTSurface( surfaceComposite, SWT.NONE, caps );
            ((ElumensSWTSurface)surface).setNumberOfChannels( 3 );
            browserCore.setHardwareFOV( 180 );
        } else {
            surface = new SimpleSWTSurface( surfaceComposite, SWT.NONE, caps );
            browserCore.setHardwareFOV( 0 );
        }

        glCanvas = (Canvas)surface.getSurfaceObject( );

        DeviceFactory deviceFactory = new SWTDeviceFactory(
            glCanvas,
            Xj3DConstants.OPENGL_ID,
            surface,
            messageBuffer );

        mainCanvas.setSurface( surface, deviceFactory );

        cursorManager.resetCanvas( glCanvas );

        glCanvas.addKeyListener( this );

        surfaceComposite.layout( );

        start( );
    }

    /**
     * Get the maximum number of samples we can use.
     */
    /*
    private int getMaximumNumSamples() {
    int ret_val = -1;

    if(!maxChooserStarted) {
    sampleChooser = new SampleChooser();

    Thread thread = new Thread(sampleChooser);
    thread.start();

    ret_val = sampleChooser.getMaxSamples();

    maxChooserStarted = true;
    } else {
    ret_val = sampleChooser.getMaxSamples();
    }

    return ret_val;
    }
    */
    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     *
     * @param core The BrowserCore
     * @param wlm The WorldLoaderManager
     * @param textureQuality A String indicating the level of texture quality,
     * choices are "low", "medium" and "high".
     */
    private void setupProperties( final BrowserCore core, final WorldLoaderManager wlm,
        final String textureQuality ) {

        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction () {
                    public Object run() {
                        String prop = System.getProperty("uri.content.handler.pkgs","");
                        if(prop.indexOf("vlc.net.content") == -1) {
                            System.setProperty("uri.content.handler.pkgs",
                                "vlc.net.content");
                        }

                        prop = System.getProperty("uri.protocol.handler.pkgs","");
                        if(prop.indexOf("vlc.net.protocol") == -1) {
                            System.setProperty("uri.protocol.handler.pkgs",
                                "vlc.net.protocol");
                        }

                        try {
                            // check if the image loader can be instantiated successfully
                            Class cls = Class.forName("vlc.net.content.image.ImageDecoder");
                            Object obj = cls.newInstance();
                            // if so, then -enable- the image loaders
                            prop = System.getProperty("java.content.handler.pkgs","");
                            if(prop.indexOf("vlc.net.content") == -1) {
                                System.setProperty("java.content.handler.pkgs",
                                    "vlc.net.content");
                            }
                        } catch(Throwable t) {
                            messageBuffer.warningReport("Image loaders not available", null);
                        }
                        
                        URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
                        if(!(res_fac instanceof Web3DResourceFactory)) {
                            res_fac = new Web3DResourceFactory(res_fac);
                            URI.setURIResourceStreamFactory(res_fac);
                        }

                        ContentHandlerFactory c_fac = URI.getContentHandlerFactory();
                        if(!(c_fac instanceof SWTContentHandlerFactory)) {
                            c_fac = new SWTContentHandlerFactory(core, wlm);
                            URI.setContentHandlerFactory(c_fac);
                        }

                        FileNameMap fn_map = URI.getFileNameMap();
                        if(!(fn_map instanceof VRMLFileNameMap)) {
                            fn_map = new VRMLFileNameMap(fn_map);
                            URI.setFileNameMap(fn_map);
                        }

                        if(textureQuality.equals("medium")) {
                            System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.useMipMaps", "true");
                            System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.anisotropicDegree", "2");
                        } else if(textureQuality.equals("high")) {
                            System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.useMipMaps", "true");
                            System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.anisotropicDegree", "16");
                        }

                        return null;
                    }
                }
                );
        } catch (PrivilegedActionException pae) {
            messageBuffer.warningReport(PROPERTY_SETUP_ERR, null);
        }
    }

    /**
     * Forward a message to the status bar - if it exists
     *
     * @param msg The message to display
     */
    private void setStatusText( String msg ) {
        if ( statusBar != null ) {
            statusBar.setStatusText( msg );
        }
    }
}
