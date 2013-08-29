/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import org.ietf.uri.*;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

// Local imports
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.input.*;

import org.xj3d.core.loading.*;
import org.xj3d.core.eventmodel.*;
import org.xj3d.ui.awt.widgets.*;

import org.web3d.browser.BrowserCore;
import org.xj3d.core.loading.ScriptLoader;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.j3d.browser.VRMLBrowserCanvas;
import org.web3d.vrml.renderer.j3d.browser.J3DStandardBrowserCore;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;

/**
 * A simple browser example that has one window and the coder does all of the
 * setup locally..
 * <p>
 *
 * The simple browser does not respond to changes in the list of viewpoints
 * in the virtual world. This is OK because scripts are not used or needed in
 * this simple environment. Once we implement scripts, we have to look at
 * something different.
 *
 * @author Justin Couch
 * @version $Revision: 1.23 $
 */
public class CanvasBrowser extends DemoFrame {

    /** The universe to place our scene into */
    private J3DStandardBrowserCore universe;

    /** The toolbar holding viewpoint information */
    private SwingViewpointToolbar vpToolbar;

    /** The toolbar holding navigation information */
    private SwingNavigationToolbar navToolbar;

    /** Flag to indicate we are in the setup of the scene currently */
    private boolean inSetup;

    /** Mapping of def'd Viewpoints to their real implementation */
    private HashMap viewpointDefMap;

    /** The current viewpoint model */
    private VRMLViewpointNodeType currentViewpoint;

    /** Place for error messages to go */
    private ConsoleWindow console;

    /** The graphics config template that is best to use */
    private GraphicsConfiguration gfxConfig;

    /** The main browser canvas to view stuff with */
    private VRMLBrowserCanvas mainCanvas;

    /** Global clock */
    private VRMLClock clock;

    /**
     * Create an instance of the demo class.
     */
    public CanvasBrowser() {
        super("VRMLCanvas Browser");

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        viewpointDefMap = new HashMap();

        Container content_pane = getContentPane();

        JPanel p1 = new JPanel(new BorderLayout());
        content_pane.add(p1, BorderLayout.CENTER);

        console = new ConsoleWindow();

        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(template.REQUIRED);
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        gfxConfig = dev.getBestConfiguration(template);
        mainCanvas = new VRMLBrowserCanvas(gfxConfig, false);
        mainCanvas.initialize();

        RouteManager route_manager = mainCanvas.getRouteManager();
        WorldLoaderManager world_loader = mainCanvas.getWorldLoaderManager();
        FrameStateManager state_manager = mainCanvas.getFrameStateManager();

        universe = mainCanvas.getUniverse();
        clock = universe.getVRMLClock();
        ScriptManager script_manager = mainCanvas.getScriptManager();
        ScriptLoader script_loader = script_manager.getScriptLoader();

        ScriptEngine jsai = new VRML97ScriptEngine(universe,
                                                   route_manager,
                                                   state_manager,
                                                   world_loader);
        jsai.setErrorReporter(console);

        ScriptEngine ecma = new JavascriptScriptEngine(universe,
                                                       route_manager,
                                                       state_manager,
                                                       world_loader);
        ecma.setErrorReporter(console);

        ScriptEngine java_sai = new JavaSAIScriptEngine(universe,
                                                        vp_manager,
                                                        route_manager,
                                                        state_manager,
                                                        world_loader);
        java_sai.setErrorReporter(console);

        ScriptEngine ecma_sai = new ECMAScriptEngine(universe,
                                                     vp_manager,
                                                     route_manager,
                                                     state_manager,
                                                     world_loader);
        ecma_sai.setErrorReporter(console);

        script_loader.registerScriptingEngine(jsai);
        script_loader.registerScriptingEngine(ecma);
        script_loader.registerScriptingEngine(java_sai);
        script_loader.registerScriptingEngine(ecma_sai);

        p1.add(mainCanvas, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout());
        p1.add(p2, BorderLayout.SOUTH);

        navToolbar = new SwingNavigationToolbar(universe, console);
        p2.add(navToolbar, BorderLayout.WEST);

        vpToolbar = new SwingViewpointToolbar(universe, console);

        p2.add(vpToolbar, BorderLayout.CENTER);

        setupProperties(universe, world_loader);

        console.setVisible(true);

        DownloadProgressListener dl_list =
            new DownloadProgressListener(statusLabel, console);

        ResourceConnection.addGlobalProgressListener(dl_list);
    }

    //----------------------------------------------------------
    // Implmentation of base class abstract methods
    //----------------------------------------------------------

    /**
     * Go to the named URL location. No checking is done other than to make
     * sure it is a valid URL.
     *
     * @param url The URL to open
     */
    public void gotoLocation(URL url) {
        mainCanvas.loadWorld(url.toString());
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param file The file to load
     */
    public void gotoLocation(File file) {
        try {
            gotoLocation(file.toURL());
        } catch(MalformedURLException mue) {
            statusLabel.setText(mue.getMessage());
            console.errorReport(mue.getMessage(), mue);
        }
    }

    protected void setWarning(String msg) {
        statusLabel.setText(msg);
        console.warningReport(msg, null);
    }

    protected void setError(String msg) {
        statusLabel.setText(msg);
        console.errorReport(msg, null);
    }

    /**
     * Create an instance of this class and run it. The single argument, if
     * supplied is the name of the file to load initially. If not supplied it
     * will start with a blank document.
     *
     * @param argv The list of arguments for this application.
     */
    public static void main(String[] argv) {
        CanvasBrowser browser = new CanvasBrowser();
        browser.show();
    }
}
