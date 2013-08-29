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
import java.awt.GridLayout;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Screen3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import javax.vecmath.Vector3f;

// Local imports
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.input.*;

import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.SensorStatusListener;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.j3d.browser.VRMLBrowserCanvas;
import org.web3d.vrml.renderer.j3d.browser.VRMLDependentCanvas;
import org.web3d.vrml.renderer.j3d.browser.J3DStandardBrowserCore;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;

import org.xj3d.ui.awt.widgets.SwingNavigationToolbar;
import org.xj3d.ui.awt.widgets.SwingViewpointToolbar;

/**
 * A browser example showing the use of multiple canvases for rendering 3D
 * content
 * <p>
 *
 * The demo code shows how to create 3 canvases - a main and two centered at 45
 * degrees, one either side of the main one. This represents what might be a
 * typical wall setup in a car simulator.
 *
 * @author Justin Couch
 * @version $Revision: 1.26 $
 */
public class MultiCanvasBrowser extends DemoFrame
    implements SensorStatusListener {

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
    public MultiCanvasBrowser() {
        super("VRMLCanvas Browser");

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        viewpointDefMap = new HashMap();

        Container content_pane = getContentPane();

        console = new ConsoleWindow();

        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(template.REQUIRED);
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        gfxConfig = dev.getBestConfiguration(template);

        mainCanvas = new VRMLBrowserCanvas(gfxConfig, false);
        mainCanvas.initialize();

        // Add two dependent canvases that represent two views at 45 degrees to
        // the axis of the main canvas
        View shared_view = mainCanvas.getView();

        shared_view.setWindowEyepointPolicy(View.RELATIVE_TO_COEXISTENCE);
        shared_view.setWindowMovementPolicy(View.VIRTUAL_WORLD);
        shared_view.setWindowResizePolicy(View.VIRTUAL_WORLD);
        shared_view.setCoexistenceCenteringEnable(false);

        VRMLDependentCanvas l_canvas =
            new VRMLDependentCanvas(gfxConfig, shared_view);
        VRMLDependentCanvas r_canvas =
            new VRMLDependentCanvas(gfxConfig, shared_view);

        // Set the physical screens up to "represent" a 3 panel wall where
        // the left and right sides are angled at 45 degrees to the center
        // canvas like this:
        //
        //    __________
        //   /          \
        //  /            \
        // /      o       \
        //       -+-
        //       / \


        Transform3D t1 = new Transform3D();
        t1.rotY(-Math.PI / 4);
        Vector3f user_pos = new Vector3f(0, 0, 0.5f);
        t1.setTranslation(user_pos);

        Screen3D screen = l_canvas.getScreen3D();
        screen.setTrackerBaseToImagePlate(t1);

        t1.setIdentity();
        t1.rotY(Math.PI / 4);
        t1.setTranslation(user_pos);

        screen = r_canvas.getScreen3D();
        screen.setTrackerBaseToImagePlate(t1);

        mainCanvas.registerDependentCanvas(l_canvas);
        mainCanvas.registerDependentCanvas(r_canvas);

        RouteManager route_manager = mainCanvas.getRouteManager();
        WorldLoaderManager world_loader = mainCanvas.getWorldLoaderManager();
        FrameStateManager state_manager = mainCanvas.getFrameStateManager();

        universe = mainCanvas.getUniverse();
        universe.addSensorStatusListener(this);
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

        JPanel p1 = new JPanel(new GridLayout(1, 3));

        p1.add(l_canvas);
        p1.add(mainCanvas);
        p1.add(r_canvas);

//        p1.add(p0, BorderLayout.CENTER);
        content_pane.add(p1, BorderLayout.CENTER);


        JPanel p2 = new JPanel(new BorderLayout());
        content_pane.add(p2, BorderLayout.SOUTH);

        navToolbar = new SwingNavigationToolbar(universe, console);
        p2.add(navToolbar, BorderLayout.WEST);

        vpToolbar = new SwingViewpointToolbar(universe, console);

        p2.add(vpToolbar, BorderLayout.CENTER);

        setupProperties(universe, world_loader);

        console.setVisible(true);

        DownloadProgressListener dl_list =
            new DownloadProgressListener(statusLabel, console);

        ResourceConnection.addGlobalProgressListener(dl_list);
        setSize(900, 300);
    }

    //----------------------------------------------------------
    // Methods required by the SensorStatusListener interface.
    //----------------------------------------------------------

    /**
     * Invoked when a sensor/anchor is in contact with a tracker capable of picking.
     *
     * @param type The sensor type
     * @param desc The sensor's description string
     */
    public void deviceOver(int type, String desc) {
    }

    /**
     * Invoked when a tracker leaves contact with a sensor.
     *
     * @param type The sensor type
     */
    public void deviceNotOver(int type) {
    }

    /**
     * Invoked when a tracker activates the sensor.  Anchors will not receive
     * this event, they get a linkActivated call.
     *
     * @param type The sensor type
     */
    public void deviceActivated(int type) {
    }

    /**
     * Invoked when a tracker follows a link.
     *
     * @param url The url to load.
     */
    public void linkActivated(String[] url_list) {
        boolean success = false;

        for(int i = 0; i < url_list.length; i++) {
            if(url_list[i].charAt(0) == '#') {
                // move to the viewpoint.
                String def_name = url_list[i].substring(1);
                VRMLViewpointNodeType vp =
                    (VRMLViewpointNodeType)viewpointDefMap.get(def_name);

                if(vp != null) {
                    universe.changeViewpoint(vp);
                    success = true;
                } else {
                    statusLabel.setText("Unknown Viewpoint " + def_name);
                    console.warningReport("Unknown Viewpoint " + def_name, null);
                }
            } else {
                // load the world.
                try {
                    URL url = new URL(url_list[i]);
                    mainCanvas.loadWorld(url.toString());

                } catch(MalformedURLException mue) {
                    statusLabel.setText("Invalid URL");
                    console.warningReport("Invalid URL: " + url_list[i], mue);
                }
            }
        }

        if(!success)
            console.errorReport("No valid URLs were found", null);

    }

    /**
     * Invoked when a link node is contact with a tracker capable of picking.
     */
    public void linkSelectable(VRMLLinkNodeType node) {
    }

    /**
     * Invoked when a link node is contact with a tracker capable of picking.
     */
    public void linkNonSelectable(VRMLLinkNodeType node) {
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
        MultiCanvasBrowser browser = new MultiCanvasBrowser();
        browser.show();
    }
}
