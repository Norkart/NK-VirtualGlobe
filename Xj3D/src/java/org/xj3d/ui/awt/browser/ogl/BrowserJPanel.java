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
 *****************************************************************************/

package org.xj3d.ui.awt.browser.ogl;

// External imports
import java.awt.*;
import java.awt.event.*;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.Properties;

import javax.media.opengl.GL;
import javax.media.opengl.GLCapabilities;

import javax.swing.JPanel;

import org.ietf.uri.*;

import org.j3d.aviatrix3d.output.graphics.ElumensAWTSurface;
import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

// Local imports
import org.xj3d.ui.awt.widgets.*;

import org.web3d.browser.BrowserComponent;
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.browser.Xj3DConstants;

import org.web3d.net.protocol.Web3DResourceFactory;
import org.web3d.net.content.VRMLFileNameMap;

import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.VRMLScene;

import org.web3d.vrml.renderer.ogl.browser.OGLBrowserCanvas;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;

import org.xj3d.core.eventmodel.DeviceFactory;
import org.xj3d.core.eventmodel.ViewpointManager;

import org.xj3d.core.loading.WorldLoaderManager;

import org.xj3d.ui.awt.device.AWTDeviceFactory;

import org.xj3d.ui.awt.net.content.AWTContentHandlerFactory;


import org.xj3d.sai.BrowserConfig;
import org.xj3d.sai.BrowserInterfaceTypes;

/**
 * Common Swing JPanel implementation of the browser component for use in
 * either SAI or EAI, that wraps the functionality of a VRML browser
 * into a convenient, easy to use form.
 * <p>
 *
 * This base class needs to be extended to provide the SAI or EAI-specific
 * implementation interfaces, as well as any startup required for both of
 * those environments, such as scripting engines etc.
 *
 * @author Justin Couch, Brad Vender
 * @version $Revision: 1.30 $
 */
public abstract class BrowserJPanel extends JPanel
    implements BrowserComponent,
               BrowserCoreListener,
               ComponentListener,
               WindowListener,
               KeyListener {

    /** Error message when setting up the system properties */
    private static final String PROPERTY_SETUP_ERR =
        "Error setting up system properties in BrowserJPanel";

    /** Wireframe rendering mode message */
    private static final String WIREFRAME_RENDERING_MODE_MSG =
        "Wireframe rendering mode enabled";

    /** Point rendering mode message */
    private static final String POINT_RENDERING_MODE_MSG =
        "Point rendering mode enabled";

    /** SHaded rendering mode message */
    private static final String SHADED_RENDERING_MODE_MSG =
        "Shaded rendering mode enabled";

    /** Framerate for paused mode */
    private static final int PAUSED_FPS = 1;

    /** The top level component that this component descends from  */
    private Window window;

    /** The real component that is being rendered to */
    private Component glCanvas;

    /** The toolbar holding viewpoint information */
    private ViewpointManager vpManager;

    /** The toolbar holding navigation information */
    protected SwingNavigationToolbar navToolbar;

    /** The toolbar holding location information */
    protected SwingLocationToolbar locToolbar;

    /** The status bar */
    protected SwingStatusBar statusBar;

    /** Area to push error messages to */
    protected SwingConsoleWindow console;

    /** The canvas used to display the world */
    protected OGLBrowserCanvas mainCanvas;

    /** The cursorManager */
    protected CursorManager cursorManager;

    /** The internal universe */
    protected OGLStandardBrowserCore universe;

    /** The frame cycle interval set, -1 if unset */
    private int frameMillis;

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
    private SampleChooser sampleChooser;

    /**
     * Create an instance of the panel configured to show or hide the controls
     * and only shows VRML97 content.
     *
     * @param parameters The object containing the browser's configuration parameters
     */
    protected BrowserJPanel(BrowserConfig parameters) {
        super(new BorderLayout());

        Properties skinProperties =
            (parameters.browserSkin == null) ? new Properties() : parameters.browserSkin;

        numSamples = 1;
        frameMillis = -1;
        wireframe = false;
        pointrender = false;
        maxChooserStarted = false;

        setSize(800, 600);

        console = new SwingConsoleWindow();

        String browserType = null;
        if(parameters.vrml97Only) {
            browserType = "VRML";
        } else {
            browserType = "X3D";
        }
        console.messageReport("Initializing OpenGL "+ browserType +" browser.\n");

        caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        if(parameters.antialiased) {
            caps.setSampleBuffers(true);
            if(parameters.antialiasingQuality.equals("low")) {
                numSamples = 2;
                caps.setNumSamples(numSamples);

            } else if(parameters.antialiasingQuality.equals("medium")) {
                // TODO: Really need to find the max allowable.
                // But JOGL startup issues make this a problem
                console.messageReport("Trying for 4 samples of antialiasing.");
                numSamples = 4;
                caps.setNumSamples(numSamples);

            } else if(parameters.antialiasingQuality.equals("high")) {
                console.messageReport("Trying for 8 samples of antialiasing.");
                numSamples = 8;
                caps.setNumSamples(numSamples);
            }
        }

        boolean use_lightweight =
            parameters.interfaceType == BrowserInterfaceTypes.LIGHTWEIGHT;

        SimpleAWTSurface surface = new SimpleAWTSurface(caps, use_lightweight);

        glCanvas = (Component)surface.getSurfaceObject();

        DeviceFactory deviceFactory = new AWTDeviceFactory(
            glCanvas,
            Xj3DConstants.OPENGL_ID,
            surface,
            console);

        mainCanvas = new OGLBrowserCanvas(surface, deviceFactory, parameters);
        mainCanvas.initialize();
        mainCanvas.setErrorReporter(console);

        glCanvas.addComponentListener(this);
        glCanvas.addKeyListener(this);

        cursorManager = new CursorManager(glCanvas, skinProperties, console);

        universe = mainCanvas.getUniverse();
        universe.addCoreListener(this);
        universe.addSensorStatusListener(cursorManager);
        universe.addNavigationStateListener(cursorManager);

        vpManager = mainCanvas.getViewpointManager();

        // setup of the system properties requires the OGLBrowserCanvas
        setupProperties(parameters.textureQuality);

        add(glCanvas, BorderLayout.CENTER);

        // setup the UI components as specified in the parameters
        if((parameters.showDash && parameters.showUrl) &&
            (parameters.dashTop == parameters.urlTop)) {

            // the user has specified that they want both location and
            // navigation toolbars - and they want them both in the same
            // place. confusion reigns. the location bar will go on top -
            // end of story.
            parameters.urlTop = true;
            parameters.dashTop = false;
        }

        if(parameters.showUrl) {
            locToolbar = new SwingLocationToolbar(
                universe,
                mainCanvas.getWorldLoaderManager(),
                parameters.urlReadOnly,
                parameters.showOpenButton,
                parameters.showReloadButton,
                parameters.contentDirectory,
                skinProperties,
                console);

            if(parameters.urlTop) {
                add(locToolbar, BorderLayout.NORTH);
            } else {
                add(locToolbar, BorderLayout.SOUTH);
            }
        }

        if(parameters.showDash) {
            JPanel p2 = new JPanel(new BorderLayout());

            if(parameters.dashTop) {
                add(p2, BorderLayout.NORTH);
            } else {
                add(p2, BorderLayout.SOUTH);
            }
            navToolbar = new SwingNavigationToolbar(
                universe,
                skinProperties,
                console);

            SwingViewpointToolbar vp_tb = new SwingViewpointToolbar(
                universe,
                vpManager,
                skinProperties,
                console);

            SwingConsoleButton console_button =
                new SwingConsoleButton(console, skinProperties);

            p2.add(navToolbar, BorderLayout.WEST);
            p2.add(vp_tb, BorderLayout.CENTER);
            p2.add(console_button, BorderLayout.EAST);

            if(parameters.showFPS || parameters.showStatusBar) {
                statusBar = new SwingStatusBar(
                    universe,
                    parameters.showStatusBar,
                    parameters.showFPS,
                    skinProperties,
                    console);

                if(locToolbar != null) {
                    locToolbar.setProgressListener(statusBar.getProgressListener());
                }

                p2.add(statusBar, BorderLayout.SOUTH);
            }
        }

        if(parameters.showConsole) {
            console.setVisible(true);
        }
        //getMaximumNumSamples();
    }

    //----------------------------------------------------------
    // Methods overridden in Component
    //----------------------------------------------------------

    /**
     * This panel, or a container in which it 'lives' is being
     * removed from it's parent. Inform the canvas to stop rendering
     * before it's removeNotify() method is called, otherwise the
     * ui will lockup.
     */
    public void removeNotify() {
        stop();
        if(window != null) {
            window.removeWindowListener(this);
            window = null;
        }
        super.removeNotify();
    }

    /**
     * This panel, or a container in which it 'lives' is being
     * added to a parent. Inform the canvas to start rendering.
     * By default the canvas should be enabled initially. This
     * method is in place to restart rendering in the instance
     * that the component has been removed and is being reinserted
     * into the ui.
     */
    public void addNotify() {
        start();
        super.addNotify();
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

                if(numSamples > max)
                    numSamples = 1;

                setStatusText("Antialiasing samples: "+ numSamples +" out of max: "+ max);

                caps.setSampleBuffers(true);
                caps.setNumSamples(numSamples);

                resetSurface();
            }
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

        case KeyEvent.VK_P:
            if((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
                pointrender = !pointrender;

                if(pointrender) {
                    setStatusText(POINT_RENDERING_MODE_MSG);
                    universe.setRenderingStyle(Xj3DConstants.RENDER_POINTS);
                } else if(wireframe) {
                    setStatusText(WIREFRAME_RENDERING_MODE_MSG);
                    universe.setRenderingStyle(Xj3DConstants.RENDER_LINES);
                } else {
                    setStatusText(SHADED_RENDERING_MODE_MSG);
                    universe.setRenderingStyle(Xj3DConstants.RENDER_SHADED);
                }
            }
            break;

        case KeyEvent.VK_W:
            if((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                universe.setNavigationMode("WALK");
            } else if((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
                wireframe = !wireframe;

                if(wireframe) {
                    setStatusText(WIREFRAME_RENDERING_MODE_MSG);
                    universe.setRenderingStyle(Xj3DConstants.RENDER_LINES);
                } else if(pointrender) {
                    setStatusText(POINT_RENDERING_MODE_MSG);
                    universe.setRenderingStyle(Xj3DConstants.RENDER_POINTS);
                } else {
                    setStatusText(SHADED_RENDERING_MODE_MSG);
                    universe.setRenderingStyle(Xj3DConstants.RENDER_SHADED);
                }
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

                resetSurface();
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
        glCanvas.requestFocusInWindow();
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
     *
     * @param evt The window event that caused the method to be called.
     */
    public void windowDeiconified(WindowEvent evt) {
        if(frameMillis < 0)
            mainCanvas.setMinimumFrameInterval(0, false);
        else
            mainCanvas.setMinimumFrameInterval(frameMillis, false);

        glCanvas.requestFocusInWindow();
    }

    /**
     * Invoked when a window is changed from a normal state to minimized.
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
        glCanvas.requestFocusInWindow();
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
        // not explicitly stated in the javadoc -
        // componentResized is called whenever this
        // panel is realized. use it as an opportunity
        // to determine the top level component window
        // and establish a listener on it.
        if(window == null) {

            Container cnt = this;
            Container parentCnt;

            while (true) {
                parentCnt = cnt.getParent();
                if(parentCnt == null) {
                    window = (Window)cnt;
                    window.addWindowListener(this);
                    break;
                }
                cnt = parentCnt;
            }
        }
    }

    /**
     * Invoked when the component has been made visible.
     *
     * @param evt The event
     */
    public void componentShown(ComponentEvent evt) {
        if(frameMillis < 0)
            mainCanvas.setMinimumFrameInterval(0, false);
        else
            mainCanvas.setMinimumFrameInterval(frameMillis, false);
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
        return glCanvas;
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
        if(universe != null) {
            // if the core has already been shutdown then the
            // reference will be null. otherwise - call dispose
            // on the BrowserCore directly.
            universe.dispose();
        }
    }

    //----------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //----------------------------------------------------------

    /**
     * Ignored. Notification that the browser is shutting down the current content.
     */
    public void browserShutdown() {
    }

    /**
     * The browser has been disposed by the user calling the
     * dispose method on the ExternalBrowser instance. Release
     * our reference to the browser core.
     */
    public void browserDisposed() {
        universe = null;
        console.dispose();
        // rem !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // note: this is temporary to eliminate a persistant
        // reference to root that will prevent gc. this will
        // 'impact' other running browsers (if there are any).
        URI.setContentHandlerFactory(null);
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    /**
     * Ignored. The browser tried to load a URL and failed.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed(String msg) {
    }

    /**
     * Ignored. Notification that a world has been loaded into the browser.
     *
     * @param scene The new scene that has been loaded
     */
    public void browserInitialized(VRMLScene scene) {
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

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
    private void resetSurface() {
        remove(glCanvas);
        glCanvas.removeComponentListener(this);
        glCanvas.removeKeyListener(this);

        GraphicsOutputDevice surface;

        if(elumensMode) {
            surface = new ElumensAWTSurface(caps);
            ((ElumensAWTSurface)surface).setNumberOfChannels(3);
            universe.setHardwareFOV(180);
        } else {
            surface = new SimpleAWTSurface(caps);
            universe.setHardwareFOV(0);
        }

        glCanvas = (Canvas)surface.getSurfaceObject();

        DeviceFactory deviceFactory = new AWTDeviceFactory(
            glCanvas,
            Xj3DConstants.OPENGL_ID,
            surface,
            console);

        mainCanvas.setSurface(surface, deviceFactory);

        add(glCanvas);

        glCanvas.addComponentListener(this);
        glCanvas.addKeyListener(this);
        glCanvas.requestFocusInWindow();
    }

    /**
     * Get the maximum number of samples we can use.
     */
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
                            console.warningReport("Image loaders not available", null);
                        }
                        
                        BrowserCore core = mainCanvas.getUniverse();
                        WorldLoaderManager wlm =
                            mainCanvas.getWorldLoaderManager();

                        ContentHandlerFactory c_fac = URI.getContentHandlerFactory();

                        URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
                        if(!(res_fac instanceof Web3DResourceFactory)) {
                            res_fac = new Web3DResourceFactory(res_fac);
                            URI.setURIResourceStreamFactory(res_fac);
                        }

                        if(!(c_fac instanceof AWTContentHandlerFactory)) {
                            c_fac = new AWTContentHandlerFactory(core, wlm);
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
            console.warningReport(PROPERTY_SETUP_ERR, null);
        }
    }

    /**
     * Forward a message to the status bar - if it exists
     *
     * @param msg - The message to display
     */
    private void setStatusText(String msg) {
        if(statusBar != null) {
            statusBar.setStatusText(msg);
        }
    }
}
