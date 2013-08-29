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

package org.xj3d.ui.awt.browser.j3d;

// External imports
import java.awt.*;
import java.awt.event.*;

import org.ietf.uri.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

import javax.swing.JPanel;
import javax.media.j3d.GraphicsConfigTemplate3D;

// Local imports
import org.web3d.browser.*;

import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.renderer.common.browser.CursorManager;
import org.web3d.vrml.renderer.j3d.browser.VRMLBrowserCanvas;
import org.web3d.vrml.renderer.j3d.browser.J3DStandardBrowserCore;

import org.xj3d.core.eventmodel.DeviceFactory;
import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.loading.ScriptLoader;

/**
 * Common Swing JPanel implementation of the browser component for use in
 * either SAI or EAI, that wraps the functionality of a VRML browser
 * into a convenient, easy to use form.
 * <p>
 *
 * This panel is designed for use by the EAI and therefore will, by default,
 * restrict the content to VRML97 rather than X3D. An alternate constructor
 * allows the use of non VRML97 content.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public abstract class BrowserJPanel extends JPanel
    implements BrowserComponent,
               ComponentListener,
               WindowListener,
               KeyListener {

    /** Error message when setting up the system properties */
    private static final String PROPERTY_SETUP_ERR =
        "Error setting up system properties in BrowserJPanel";

    /** Framerate for paused mode */
    private static final int PAUSED_FPS = 1;

    /** The real component that is being rendered to */
    private Canvas3D j3dCanvas;

    /** The toolbar holding viewpoint information */
    private ViewpointManager vpManager;

    /** The toolbar holding navigation information */
    private SwingNavigationToolbar navToolbar;

    /** The status bar */
    protected SwingStatusBar statusBar;

    /** Area to push error messages to */
    protected SwingConsoleWindow console;

    /** The canvas used to display the world */
    protected VRMLBrowserCanvas mainCanvas;

    /** The universe to place our scene into */
    protected J3DStandardBrowserCore universe;

    /** The frame cycle interval set, -1 if unset */
    private int frameCycleTime;

    /** Wireframe or filled mode */
    private boolean wireframe;

    /** Are we in Elumens Spherical mode */
    private boolean elumensMode;

    /** Number of antialiasing samples */
    private int numSamples;

    /**
     * Create an instance of the panel configured to show or hide the controls
     * and only shows VRML97 content.
     *
     * @param showDash true to show the navigation bar
     * @param dashTop true to put the nav bar at the top
     * @param showUrl true to show the URL location bar
     * @param urlTop true to put the location bar at the top
     * @param urlReadOnly true to make the location bar read only
     * @param showConsole true if the console should be shown immediately
     * @param showOpenButton true to put an open button with the URL location bar
     * @param showReloadButton true to put a reload button with the URL location bar
     * @param showStatusBar true to show a status bar
     * @param showFPS true to show the current FPS
     * @param contentDirectory initial directory to load content from.  Must be a full path.
     * @param antialiased true to turn on antialiasing
     * @param antialiasingQuality low, medium, high, antialiasing must be turned on for this to matter.
     * @param primitiveQuality low, medium, high.
     * @param textureQuality low, medium, high.
     */
    public BrowserJPanel(boolean showDash,
                         boolean dashTop,
                         boolean showUrl,
                         boolean urlTop,
                         boolean urlReadOnly,
                         boolean showConsole,
                         boolean showOpenButton,
                         boolean showReloadButton,
                         boolean showStatusBar,
                         boolean showFPS,
                         boolean antialiased,
                         String contentDirectory,
                         String antialiasingQuality,
                         String primitiveQuality,
                         String textureQuality) {
        this(showDash,
             dashTop,
             showUrl,
             urlTop,
             urlReadOnly,
             showConsole,
             showOpenButton,
             showReloadButton,
             showStatusBar,
             showFPS,
             contentDirectory,
             antialiased,
             antialiasingQuality,
             primitiveQuality,
             textureQuality,
             null);
    }

    /**
     * Create an instance of the panel configured to show or hide the controls
     * and only shows VRML97 content.
     *
     * @param showDash true to show the navigation bar
     * @param dashTop true to put the nav bar at the top
     * @param showUrl true to show the URL location bar
     * @param urlTop true to put the location bar at the top
     * @param urlReadOnly true to make the location bar read only
     * @param showConsole true if the console should be shown immediately
     * @param showOpenButton true to put an open button with the URL location bar
     * @param showReloadButton true to put a reload button with the URL location bar
     * @param showStatusBar true to show a status bar
     * @param showFPS true to show the current FPS
     * @param contentDirectory initial directory to load content from.  Must be a full path.
     * @param antialiased true to turn on antialiasing
     * @param antialiasingQuality low, medium, high, antialiasing must be turned on for this to matter.
     * @param primitiveQuality low, medium, high.
     * @param textureQuality low, medium, high.
     * @param skinProperties Customisation of the browser buttons etc
     * @param serverPortNumber The port to listen for incoming data on
     * @throws IOException
     */
    public BrowserJPanel(boolean showDash,
                         boolean dashTop,
                         boolean showUrl,
                         boolean urlTop,
                         boolean urlReadOnly,
                         boolean showConsole,
                         boolean showOpenButton,
                         boolean showReloadButton,
                         boolean showStatusBar,
                         boolean showFPS,
                         String contentDirectory,
                         boolean antialiased,
                         String antialiasingQuality,
                         String primitiveQuality,
                         String textureQuality,
                         Properties skinProperties,
                         int serverPortNumber)
        throws IOException {

        this(showDash,
             dashTop,
             showUrl,
             urlTop,
             urlReadOnly,
             showConsole,
             showOpenButton,
             showReloadButton,
             showStatusBar,
             showFPS,
             contentDirectory,
             antialiased,
             antialiasingQuality,
             primitiveQuality,
             textureQuality,
             skinProperties);

        // And now the three most difficult lines
        ServerSocket s = new ServerSocket(serverPortNumber);
        NetworkBrowserServer browser =
            new NetworkBrowserServer(s,
                                     mainCanvas.getUniverse(),
                                     browserImpl,
                                     clock);

    }

    /**
     * Create an instance of the panel configured to show or hide the controls
     * and only shows VRML97 content.
     *
     * @param showDash true to show the navigation bar
     * @param dashTop true to put the nav bar at the top
     * @param showUrl true to show the URL location bar
     * @param urlTop true to put the location bar at the top
     * @param urlReadOnly true to make the location bar read only
     * @param showConsole true if the console should be shown immediately
     * @param skinProperties The properties object to configure appearance with
     * @param showStatusBar true to show a status bar
     * @param showFPS true to show the current FPS
     * @param contentDirectory initial directory to load content from.  Must be a full path.
     * @param antialiased true to turn on antialiasing
     * @param antialiasingQuality low, medium, high, antialiasing must be turned on for this to matter.
     * @param primitiveQuality low, medium, high.
     * @param textureQuality low, medium, high.
     * @param skinProperties Customisation of the browser buttons etc
     */
    public BrowserJPanel(boolean showDash,
                         boolean dashTop,
                         boolean showUrl,
                         boolean urlTop,
                         boolean urlReadOnly,
                         boolean showConsole,
                         boolean showOpenButton,
                         boolean showReloadButton,
                         boolean showStatusBar,
                         boolean showFPS,
                         String contentDirectory,
                         boolean antialiased,
                         String antialiasingQuality,
                         String primitiveQuality,
                         String textureQuality,
                         Properties skinProperties) {

        super(new BorderLayout());

        setSize(800, 600);

        numSamples = 1;
        frameCycleTime = -1;
        wireframe = false;
        elumensMode = false;

        // JC: Copied from the OpenGL code, so does nothing right now.
        if(antialiased) {
            if(antialiasingQuality.equals("low")) {
                numSamples = 2;
//                caps.setNumSamples(numSamples);
            } else if(antialiasingQuality.equals("medium")) {
                // TODO: Really need to find the max allowable.  But JOGL startup issues make this a problem
                System.out.println("Trying for 4 samples of antialiasing.");
                numSamples = 4;
//                caps.setNumSamples(numSamples);
            } else if(antialiasingQuality.equals("high")) {
                System.out.println("Trying for 8 samples of antialiasing.");
                numSamples = 8;
//                caps.setNumSamples(numSamples);
            }
        }

        console = new SwingConsoleWindow();
        console.messageReport("Initializing Java3D VRML browser.\n");

        addComponentListener(this);

        // We also need a canvas to display stuff with and a universe to set
        // the content in.
        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(GraphicsConfigTemplate3D.REQUIRED);
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        GraphicsConfiguration gfx_cfg = dev.getBestConfiguration(template);

        mainCanvas = new VRMLBrowserCanvas(gfx_cfg, true);
        mainCanvas.initialize();
        mainCanvas.setErrorReporter(console);

        j3dCanvas = mainCanvas;

        CursorManager cm =
            new CursorManager(mainCanvas, skinProperties, console);

        universe = mainCanvas.getUniverse();
        universe.addSensorStatusListener(cm);
        universe.addNavigationStateListener(cm);

        vpManager = mainCanvas.getViewpointManager();

        setupProperties(textureQuality);

        eaiBrowser = new EAIBrowser(universe,
                                    browserImpl,
                                    eventQueue,
                                    console);

        add(mainCanvas, BorderLayout.CENTER);

        // Create these all the time
        statusLabel = new JLabel();
        fpsLabel = new JLabel();
        SwingLocationToolbar tb = null;

        if(showUrl) {
            tb =
                new SwingLocationToolbar(universe,
                                         mainCanvas.getWorldLoaderManager(),
                                         urlReadOnly,
                                         showOpenButton,
                                         showReloadButton,
                                         contentDirectory,
                                         skinProperties,
                                         console);

            if(urlTop)
                add(tb, BorderLayout.NORTH);
            else
                add(tb, BorderLayout.SOUTH);
        }

// Need to fix this as this panel will trash the existing one if they are
// both at the top or bottom.

        if(showDash) {
            JPanel p2 = new JPanel(new BorderLayout());

            if(dashTop)
                add(p2, BorderLayout.NORTH);
            else
                add(p2, BorderLayout.SOUTH);

            navToolbar = new SwingNavigationToolbar(universe,
                                                    skinProperties,
                                                    console);
            //navToolbar.setAllowUserStateChange(true);
            SwingViewpointToolbar vp_tb =
                new SwingViewpointToolbar(universe,
                                          vpManager,
                                          skinProperties,
                                          console);

            SwingConsoleButton console_button =
                new SwingConsoleButton(console, skinProperties);

            p2.add(navToolbar, BorderLayout.WEST);
            p2.add(vp_tb, BorderLayout.CENTER);
            p2.add(console_button, BorderLayout.EAST);

            if(showFPS || showStatusBar) {
                statusBar = new SwingStatusBar(universe,
                                               showStatusBar,
                                               showFFS,
                                               skinProperties,
                                               console);

                if (tb != null) {
                    tb.setProgressListener(statusBar.getProgressListener());
                }

                p2.add(statusBar, BorderLayout.SOUTH);
            }
        }

        if(showConsole)
            console.setVisible(true);
    }

    //----------------------------------------------------------
    // Methods defined by KeyListener
    //----------------------------------------------------------

    /**
     * Notification that a key is pressed.
     *
     * @param e The event that caused this method to be called
     */
    public void keyPressed(KeyEvent e) {
    }

    /**
     * Notification that a key is released.
     *
     * @param e The event that caused this method to be called
     */
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_A:
                console.messageReport("Antialiasing not supported on Java3D yet.");
/*
                if((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {

                    numSamples = numSamples * 2;

                    int max = getMaximumNumSamples();

                    // Busy wait till answer comes.  Should already be here
                    while(max < 0) {
                       try {
                           Thread.sleep(50);
                       } catch(Exception e2) {}
                       max = getMaximumNumSamples();
                    }

                    if (numSamples > max)
                        numSamples = 1;

                    statusLabel.setText("Antialiasing samples: " + numSamples + " out of max: " + max);

                    caps.setSampleBuffers(true);
                    caps.setNumSamples(numSamples);

                    resetSurface();
                }
*/
                break;

            case KeyEvent.VK_PAGE_DOWN:
                vpManager.nextViewpoint();
                break;

            case KeyEvent.VK_PAGE_UP:
                vpManager.previousViewpoint();
                break;

            case KeyEvent.VK_HOME:
                vpManager.firstViewpoint();
                break;

            case KeyEvent.VK_END:
                vpManager.lastViewpoint();
                break;

            case KeyEvent.VK_F:
                if((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    universe.setNavigationMode("FLY");
                }
                break;

            case KeyEvent.VK_W:
                if((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    universe.setNavigationMode("WALK");
                } else if((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
                    console.messageReport("Wireframe mode not supported on Java3D.");
                }
                break;

            case KeyEvent.VK_E:
                if((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    universe.setNavigationMode("EXAMINE");
                }
                break;

            case KeyEvent.VK_Z:
                if((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
                    // Enter/Exit Elumens mode
                    elumensMode = !elumensMode;

// JC: We're not dealing with this right now.
//                    resetSurface();
                }
                break;
        }
    }

    /**
     * Notification that a key is typed (press and release).
     *
     * @param e The event that caused this method to be called
     */
    public void keyTyped(KeyEvent e) {
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    /**
     * The window has been given focus.
     *
     * @param evt The window event that caused the method to be called.
     */
    public void windowActivated(WindowEvent evt) {
        j3dCanvas.requestFocusInWindow();
    }

    /**
     * Ignored
     */
    public void windowClosed(WindowEvent evt) {
    }

    /**
     * Ignored
     */
    public void windowClosing(WindowEvent evt) {
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt) {
    }

    /**
     * Invoked when a window is changed from a minimized to a normal state.
     *
     * @param evt The window event that caused the method to be called.
     */
    public void windowDeiconified(WindowEvent evt) {
        if(frameMillis < 0)
            mainCanvas.setMinimumFrameInterval(0, false);
        else
            mainCanvas.setMinimumFrameInterval(frameMillis, false);

        j3dCanvas.requestFocusInWindow();
    }

    /**
     * Invoked when a window is changed from a normal state to minimzed.
     *
     * @param evt The window event that caused the method to be called.
     */
    public void windowIconified(WindowEvent evt) {
        mainCanvas.setMinimumFrameInterval(1000 / PAUSED_FPS, false);
    }

    /**
     * The window has opened, so requst input focus.
     *
     * @param evt The window event that caused the method to be called.
     */
    public void windowOpened(WindowEvent evt) {
        j3dCanvas.requestFocusInWindow();
    }

    //----------------------------------------------------------
    // Methods defined by ComponentListener
    //----------------------------------------------------------

    /**
     * Invoked when the component has been made invisible.
     *
     * @param evt The event
     */
    public void componentHidden(ComponentEvent evt) {
        mainCanvas.setMinimumFrameInterval(1000 / PAUSED_FPS, false);
    }

    /**
     * Invoked when the component's position changes.
     *
     * @param evt The event
     */
    public void componentMoved(ComponentEvent evt) {
    }

    /**
     * Invoked when the component's size changes.
     *
     * @param evt The event
     */
    public void componentResized(ComponentEvent evt) {
    }

    /**
     * Invoked when the component has been made visible.
     *
     * @param evt The event
     */
    public void componentShown(ComponentEvent evt) {
        if (frameCycleTime < 0)
            mainCanvas.setMinimumFrameInterval(0, false);
        else
            mainCanvas.setMinimumFrameInterval(frameCycleTime, false);
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
     * Get the AWT component holding this browser.
     *
     * @return The component
     */
    public Object getCanvas() {
        return mainCanvas;
    }

    /**
     * Get the renderer type.
     *
     * @return The BrowserCore type
     */
    public int getRendererType() {
        return universe.getRendererType();
    }

    /**
     * Get the core browser implementation.
     *
     * @return the BrowserCore
     */
    public BrowserCore getBrowserCore() {
        return universe;
    }

    /**
     * Fetch the error handler so that application code can post messages
     * too.
     *
     * @return The current error handler instance
     */
    public ErrorReporter getErrorReporter() {
        return console;
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
    public void destroy() {
        mainCanvas.setEnabled(false);
        mainCanvas.browserShutdown();
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     */
    private void setupProperties(final String textureQuality) {
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
                        if (prop.indexOf("vlc.net.protocol") == -1) {
                            System.setProperty("uri.protocol.handler.pkgs",
                                               "vlc.net.protocol");
                        }

                        BrowserCore core = mainCanvas.getUniverse();
                        WorldLoaderManager wlm =
                            mainCanvas.getWorldLoaderManager();

                        ContentHandlerFactory c_fac = URI.getContentHandlerFactory();

                        if(!(c_fac instanceof VRMLContentHandlerFactory)) {
                            c_fac = new VRMLContentHandlerFactory(core, wlm);
                            URI.setContentHandlerFactory(c_fac);
                        }

                        FileNameMap fn_map = URI.getFileNameMap();
                        if(!(fn_map instanceof VRMLFileNameMap)) {
                            fn_map = new VRMLFileNameMap(fn_map);
                            URI.setFileNameMap(fn_map);
                        }

                        if (textureQuality.equals("medium")) {
                            System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.useMipMaps", "true");
                            System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.anisotropicDegree", "2");
                        } else if (textureQuality.equals("high")) {
                            System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.useMipMaps", "true");
                            System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.anisotropicDegree", "16");
                        }

                        return null;
                    }
                }
            );
        } catch (PrivilegedActionException pae) {
             console.warningReport(PROPERTY_SETUP_ERR, null);
        }
    }
}
