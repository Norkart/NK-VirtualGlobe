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

package org.xj3d.ui.awt.browser.j3d;

// External imports
import java.awt.*;
import java.awt.event.*;

import org.ietf.uri.*;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.PolygonAttributes;

// Local imports
import org.web3d.browser.*;
import org.web3d.util.*;

import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.X3DResourceFactory;

import org.web3d.vrml.nodes.VRMLLinkNodeType;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.renderer.j3d.browser.VRMLBrowserCanvas;
import org.web3d.vrml.renderer.j3d.browser.J3DStandardBrowserCore;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.VRMLParseException;

import org.web3d.vrml.scripting.browser.X3DCommonBrowser;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIBrowser;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;

import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.X3DComponent;

import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.ScriptLoader;
import org.xj3d.core.loading.WorldLoaderManager;
import org.xj3d.core.loading.WorldLoader;

/**
 * A brower that uses the AWT panel and labels to draw render the
 * UI with.
 * <P>
 *
 * X3DBrowserAWTPanel is the AWT based alternative to BrowserJPanel.
 * At the moment, it offers minimal functionality.
 * The "dashboard" is a text label, there aren't any navigation functions.
 * That, and urlReadOnly, urlTop, and dashTop are ignored.
 *
 * @author Brad Vender, Justin Couch
 * @version $Revision: 1.6 $
 */
public class X3DBrowserAWTPanel extends Panel
    implements X3DComponent,
               BrowserComponent,
               KeyListener,
               ComponentListener,
               WindowListener,
               Runnable,
               FileHandler {

    /** Framerate for paused mode */
    private static final int PAUSED_FPS = 1;

    /** Property in browser skin which determines 'show console' image */
    private static final String BROWSER_BUTTON_PROPERTY = "CONSOLE.button";

    /** Default image to use for 'show console' button */
    private static final String DEFAULT_BROWSER_BUTTON = "images/navigation/ButtonConsole.gif";

    /** Property in browser skin which determines 'open' image */
    private static final String BROWSER_OPEN_PROPERTY = "OPEN.button";

    /** Default image to use for reload button */
    private static final String DEFAULT_OPEN_BUTTON = "images/locationbar/openIcon32x32.gif";

    /** Property in browser skin which determines 'open' image */
    private static final String BROWSER_RELOAD_PROPERTY = "RELOAD.button";

    /** Default image to use for reload button */
    private static final String DEFAULT_RELOAD_BUTTON = "images/locationbar/reloadIcon32x32.gif";

    /** The Browser instance this is the display for */
    private SAIBrowser saiBrowser;

    /** The canvas used to display the world */
    private VRMLBrowserCanvas mainCanvas;

    /** The universe to place our scene into */
    private J3DStandardBrowserCore universe;

    /** Viewpoint manager for altering the current viewpoint */
    private ViewpointManager vpManager;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** The textfield to read the values from */
    private TextField urlTextField;

    /** The go button on the URl panel */
    private Button locationGoButton;

    /** The open button on the URl panel */
    private Button openButton;

    /** The reload button on the URl panel */
    private Button reloadButton;

    /** The label for status messages */
    private Label statusLabel;

    /** The Label to show the description text on */
    private Label descriptionLabel;

    /** The Label to show the current URL text */
    private Label urlLabel;

    /** Label for frames per second. */
    private Label fpsLabel;

    /** Area to push error messages to */
    private AWTConsoleWindow console;

    /** Number of antialiasing samples */
    private int numSamples;

    /** Wireframe or filled mode */
    private boolean wireframe;

    /** The polygon mode to display in */
    private int polygonMode;

    /** Should we display FPS counter */
    private boolean showFPS;

    /** The content directory to load content from.  NULL if none provided */
    private String contentDirectory;

    /** The frame cycle interval set, -1 if unset */
    private int frameCycleTime;

    /** The last FPS, used to avoid garbage generation. */
    private float lastFPS;

    /** Are we in Elumens Spherical mode */
    private boolean elumensMode;

    /** Have we gotten focus.  Need to wait till JOGL gets its AWT peer */
    private boolean firstFocused;

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
     * @param antialiased true to turn on antialiasing
     * @param antialiasingQuality low, medium, high, antialiasing must be turned on for this to matter.
     * @param primitiveQuality low, medium, high.
     * @param textureQuality low, medium, high.
     */
    public X3DBrowserAWTPanel(boolean showDash,
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
                           String textureQuality ) {

        this(false,
             showDash,
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
     * @param vrml97Only true if this is to be restricted to VRML97 only
     * @param showDash true to show the navigation bar
     * @param dashTop true to put the nav bar at the top
     * @param showUrl true to show the URL location bar
     * @param urlTop true to put the location bar at the top
     * @param urlReadOnly true to make the location bar read only
     * @param showConsole true if the console should be shown immediately
     * @param showStatusBar true to show a status bar
     * @param showFPS true to show the current FPS
     * @param contentDirectory initial directory to load content from.  Must be a full path.
     * @param antialiased true to turn on antialiasing
     * @param antialiasingQuality low, medium, high, antialiasing must be turned on for this to matter.
     * @param primitiveQuality low, medium, high.
     * @param textureQuality low, medium, high.
     */
    public X3DBrowserAWTPanel(boolean vrml97Only,
                              boolean showDash,
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
                              String textureQuality) {

        this(vrml97Only,
             showDash,
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
     * Create a VrmlComponent that belongs to an AWT panel.
     * and in that process construct the
     *  corresponding Browser, and the infrastructure required.
     *
     * @param vrml97Only true if this is to be restricted to VRML97 only
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
     */
    public X3DBrowserAWTPanel(boolean vrml97Only,
                              boolean showDash,
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
        firstFocused = false;
        frameCycleTime = -1;
        wireframe = false;
        elumensMode = false;
        this.showFPS = showFPS;
        this.contentDirectory = contentDirectory;

// JC: multisample disabled for now.
        if(antialiased) {
//            caps.setSampleBuffers(true);
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

        console = new AWTConsoleWindow();
        console.messageReport("Initializing Java3D X3D browser.\n");

        /** Make the panel */
        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();

        //template.setDoubleBuffer(template.REQUIRED);
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();
        GraphicsConfiguration gfxConfig = dev.getBestConfiguration(template);

        GraphicsConfiguration gfx_cfg = dev.getBestConfiguration(template);
        mainCanvas = new VRMLBrowserCanvas(gfx_cfg, vrml97Only);
        mainCanvas.initialize();

        mainCanvas.addComponentListener(this);
        mainCanvas.addKeyListener(this);

        descriptionLabel = new Label();
        urlLabel = new Label();

        add(mainCanvas,BorderLayout.CENTER);

        RouteManager route_manager = mainCanvas.getRouteManager();
        vpManager = mainCanvas.getViewpointManager();

        universe = mainCanvas.getUniverse();

        FrameStateManager state_manager = mainCanvas.getFrameStateManager();
        worldLoader = mainCanvas.getWorldLoaderManager();

        ScriptManager sm = mainCanvas.getScriptManager();
        ScriptLoader s_loader = sm.getScriptLoader();


        console = new AWTConsoleWindow();

        // Register all the other bits. Set up scripting engines next....
        ScriptEngine java_sai = new JavaSAIScriptEngine(universe,
                                                        vpManager,
                                                        route_manager,
                                                        state_manager,
                                                        worldLoader);
        java_sai.setErrorReporter(console);

        ScriptEngine ecma = new ECMAScriptEngine(universe,
                                                 vpManager,
                                                 route_manager,
                                                 state_manager,
                                                 worldLoader);
        ecma.setErrorReporter(console);

        s_loader.registerScriptingEngine(java_sai);
        s_loader.registerScriptingEngine(ecma);

        setupProperties(universe, worldLoader, textureQuality);

        X3DCommonBrowser browser_impl =
            new X3DCommonBrowser(universe,
                                 vpManager,
                                 route_manager,
                                 state_manager,
                                 worldLoader);

        browser_impl.setErrorReporter(console);

        ExternalEventQueue eventQueue=new ExternalEventQueue(console);
        mainCanvas.getEventModelEvaluator().addExternalView(eventQueue);

        saiBrowser = new SAIBrowser(universe,
                                    browser_impl,
                                    route_manager,
                                    state_manager,
                                    eventQueue,
                                    console);

        // Create these all the time
        urlTextField = new TextField();
        statusLabel = new Label();
        fpsLabel = new Label();

        if(showUrl) {
            Label l1 = new Label(" Location: ");
            locationGoButton = new Button(" Go! ");
            locationGoButton.setEnabled(!urlReadOnly);

            urlTextField.setEditable(!urlReadOnly);

            if(!urlReadOnly) {
                LoadURLAction loadURLAction =
                    new LoadURLAction(this, urlTextField);
                locationGoButton.addActionListener(loadURLAction);
                urlTextField.addActionListener(loadURLAction);
            }

            Panel p1 = new Panel(new BorderLayout());

            p1.add(l1, BorderLayout.WEST);
            p1.add(locationGoButton, BorderLayout.EAST);
            p1.add(urlTextField, BorderLayout.CENTER);


            if(showOpenButton || showReloadButton) {
                Panel p3 = new Panel(new BorderLayout());
                Panel p2 = new Panel(new BorderLayout());

                if(showOpenButton) {
//                    Image openImage = BrowserPanelUtilities.loadImage(skinProperties.getProperty(
//                            BROWSER_OPEN_PROPERTY,
//                            DEFAULT_OPEN_BUTTON));
//                    if (openImage == null)
                        openButton = new Button("Open");
//                    else
//                        openButton = new JButton(new ImageIcon(openImage,"Open"));
//                    openButton.setToolTipText("Open File");
//                    openButton.setMargin(new Insets(0,0,0,0));

// TODO:
//                    OpenAction openAction = new OpenAction(this, this, contentDirectory);
//                    openButton.addActionListener(openAction);

                    p3.add(openButton, BorderLayout.WEST);
                }

                if(showReloadButton) {
//                    Image reloadImage = BrowserPanelUtilities.loadImage(skinProperties.getProperty(
//                            BROWSER_RELOAD_PROPERTY,
//                            DEFAULT_RELOAD_BUTTON));
//                    if (reloadImage == null)
                        reloadButton = new Button("Reload");
//                    else
//                        reloadButton = new JButton(new ImageIcon(reloadImage,"Reload"));
//                    reloadButton.setToolTipText("Reload File");
//                    reloadButton.setMargin(new Insets(0,0,0,0));

// TODO:
//                    ReloadAction reloadAction = new ReloadAction(this, this, urlTextField);
//                    reloadButton.addActionListener(reloadAction);
                    p3.add(reloadButton, BorderLayout.EAST);
                }

                p2.add(p3, BorderLayout.WEST);
                p2.add(p1, BorderLayout.CENTER);

                if(urlTop)
                    add(p2, BorderLayout.NORTH);
                else
                    add(p2, BorderLayout.SOUTH);
            } else {
                if(urlTop)
                    add(p1, BorderLayout.NORTH);
                else
                    add(p1, BorderLayout.SOUTH);
            }
        }

        // Atleast humor the idea of parameters.
        if(showDash) {
            add(descriptionLabel,BorderLayout.SOUTH);
            add(urlLabel,BorderLayout.NORTH);
        }

        mainCanvas.setErrorReporter(console);

        if(showConsole)
            console.setVisible(true);

        if(showFPS)
            new Thread(this).start();
    }

    //-----------------------------------------------------------------------
    // Methods defined by X3DComponent
    //-----------------------------------------------------------------------

    /**
     * Return the vrml.eai.Browser object which corresponds to this VrmlComponent,
     * as required by the specification.
     *
     * @return The vrml.eai.Browser object associated with this VrmlComponent
     */
    public ExternalBrowser getBrowser() {
        return saiBrowser;
    }

    /**
     * @see org.web3d.x3d.sai.X3DComponent#getImplementation
     */
    public Object getImplementation() {
        return this;
    }

    /**
     * @see org.web3d.x3d.sai.X3DComponent#shutdown
     */
    public void shutdown() {
        saiBrowser.dispose();
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
                    console.messageReport("Antialiasing not supported on Java3D yet.");
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

                    if (numSamples > max)
                        numSamples = 1;

                    statusLabel.setText("Antialiasing samples: " + numSamples + " out of max: " + max);

                    caps.setSampleBuffers(true);
                    caps.setNumSamples(numSamples);

                    resetSurface();
*/
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
            case KeyEvent.VK_W:
                if((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    universe.setNavigationMode("WALK");
                } else if((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
                    wireframe = !wireframe;

                    if (wireframe) {
                        polygonMode = PolygonAttributes.POLYGON_LINE;
                        statusLabel.setText("Wireframe mode enabled");
/*
 LAYERS:
   This needs to work on some per-layer basis.

                        Scene scene = (Scene) universe.getRendererScene();
                        scene.setRenderEffectsProcessor(this);
*/
                    } else {
                        statusLabel.setText("Wireframe mode disabled");
                        polygonMode = PolygonAttributes.POLYGON_FILL;
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
     * Ignored
     */
    public void windowActivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowClosed(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowClosing(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt)
    {
    }

    /**
     * Invoked when a window is changed from a minimized to a normal state.
     *
     * @param evt The window event.
     */
    public void windowDeiconified(WindowEvent evt) {
        if (frameCycleTime < 0)
            mainCanvas.setMinimumFrameInterval(0, false);
        else
            mainCanvas.setMinimumFrameInterval(frameCycleTime, false);
    }

    /**
     * Invoked when a window is changed from a normal state to minimzed.
     *
     * @param evt The window event.
     */
    public void windowIconified(WindowEvent evt) {
        mainCanvas.setMinimumFrameInterval(1000 / PAUSED_FPS, false);
    }

    /**
     * Ignored
     */
    public void windowOpened(WindowEvent evt) {
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
        // Reget the parent each time as it might have changed.  Que changes by resize, correct?
        Container cnt = this.getParent();
        Container tmpCnt;

        while(true) {
            tmpCnt = cnt.getParent();
            if (tmpCnt == null)
                break;

            cnt = tmpCnt;
        }

        ((Window)cnt).addWindowListener(this);
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
        // TODO: Handle user-set values
        mainCanvas.setMinimumFrameInterval(millis);
        frameCycleTime = millis;
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

    //---------------------------------------------------------
    // Methods defined by FileHandler
    //---------------------------------------------------------

    /**
     * Change the panels content to the provided URL.
     *
     * @param url The URL to load.
     * @throws IOException On a failed load or badly formatted URL
     */
    public void loadURL(String url) throws IOException {
        urlTextField.setText(url);

        URL nextURL = new URL(url);

        VRMLScene parsed_scene = null;
        long startTime = System.currentTimeMillis();

        try {
            ResourceConnection conn = nextURL.getResource();
/*
            if (progressBar) {
                conn.addProgressListener(this);

                int maxSize = conn.getContentLength();

                pm = new ProgressMonitor(this,"Loading model:","Please wait",0,(int)maxSize);
            }
*/
            Object obj = conn.getContent();

            if (obj instanceof VRMLScene)
                parsed_scene = (VRMLScene) obj;
            else {
                if (obj != null )
                    System.out.println("Type: " + obj.getClass().toString());
                return;
            }
        } catch(IOException ioe) {
//            setError("IO Error loading file");
            return;
        } catch(VRMLParseException vpe) {
            console.errorReport("Exception parsing file at line: " +
                                vpe.getLineNumber() + " col: " +
                                vpe.getColumnNumber() + "\n" +
                                vpe.getMessage(),
                                vpe);
        } catch(Exception upe) {
            console.errorReport("Unexpected exception during parsing", upe);
        }

        if(parsed_scene == null)
            return;

        String vpUrl = nextURL.getRef();

        universe.setScene(parsed_scene, vpUrl);
    }

    //---------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------

    /**
     * Thread to update frames per second and status bar.
     */
    public void run() {
        while(true) {
            try {
                Thread.sleep(100);
            } catch(Exception e) {
            }

            if(!firstFocused) {
                mainCanvas.requestFocus();
                firstFocused = mainCanvas.isFocusOwner();
            }

            displayFPS();
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Display the frames per second.
     */
    private void displayFPS() {
        float fps = universe.getCurrentFrameRate();

        if (Math.abs(lastFPS - fps) > 0.01) {
            // TODO: Need todo this in a non-garbage generating way
            String txt = Float.toString(universe.getCurrentFrameRate());
            int len = txt.length();

            if(len > 0)
                fpsLabel.setText(txt.substring(0,Math.min(5,len)));

            lastFPS = fps;
        }
    }

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     *
     * @param core The core representation of the browser
     * @param wlm Loader manager for doing async calls
     */
    private void setupProperties(final BrowserCore core,
                                 final WorldLoaderManager wlm,
                                 final String textureQuality) {
        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction () {
                    public Object run() {
                        String prop = System.getProperty("uri.content.handler.pkgs","");
                        if (prop.indexOf("vlc.net.content") == -1) {
                            System.setProperty("uri.content.handler.pkgs",
                                "vlc.net.content");
                        }

                        prop = System.getProperty("uri.protocol.handler.pkgs","");
                        if (prop.indexOf("vlc.net.protocol") == -1) {
                            System.setProperty("uri.protocol.handler.pkgs",
                                "vlc.net.protocol");
                        }

                        URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
                        if(!(res_fac instanceof X3DResourceFactory)) {
                            res_fac = new X3DResourceFactory(res_fac);
                            URI.setURIResourceStreamFactory(res_fac);
                        }

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
            System.out.println("Error setting Properties in BrowserJPanel");
        }
    }
}
