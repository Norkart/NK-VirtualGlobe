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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.ietf.uri.*;

// Local imports
import org.web3d.browser.BrowserCore;

import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.Web3DResourceFactory;
import org.web3d.net.resolve.Web3DURNResolver;

import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.WorldLoaderManager;

import org.xj3d.ui.awt.net.content.AWTContentHandlerFactory;

import org.xj3d.ui.awt.widgets.CursorManager;
import org.xj3d.ui.awt.widgets.SwingConsoleButton;
import org.xj3d.ui.awt.widgets.SwingConsoleWindow;
import org.xj3d.ui.awt.widgets.SwingLocationToolbar;
import org.xj3d.ui.awt.widgets.SwingNavigationToolbar;
import org.xj3d.ui.awt.widgets.SwingProgressListener;
import org.xj3d.ui.awt.widgets.SwingStatusBar;
import org.xj3d.ui.awt.widgets.SwingViewpointToolbar;


/**
 * A basic frame that does all the setup needed for these demo programs.
 * <p>
 *
 * The window contains a textfield at the top and a Go button to tell it to
 * load the URL described. In the bottom of the frame is a text label for
 * information messages.
 *
 * @author Justin Couch
 * @version $Revision: 1.25 $
 */
public abstract class DemoFrame extends JFrame {

    // Constants for the URN setup

    /** Set this to the install directory that UMEL uses */
    private static final String UMEL_INSTALL_DIR = null;

    /** Set this to the install directory that GeoVRML uses */
    private static final String GEOVRML_INSTALL_DIR = null;
//    private static final String GEOVRML_INSTALL_DIR =
//        "c:\\Program Files\\GeoVRML\\";

    /** NSS prefix used by UMEL */
    private static final String UMEL_PREFIX = "umel";

    /** NSS prefix used by GeoVRML */
    private static final String GEOVRML_PREFIX = "geovrml";

    /** The real component that is being rendered to */
    protected Component canvas;

    /** The status bar */
    protected SwingStatusBar statusBar;

    /** Area to push error messages to */
    protected SwingConsoleWindow console;

    /** Created by the derived class */
    protected BrowserCore universe;

    /**
     * Create an instance of the demo frame now. The console is created
     * here, but nothing else.
     *
     * @param title The window title to use for this app
     */
    public DemoFrame(String title) {

        super(title);

        console = new SwingConsoleWindow();
        console.messageReport("Initializing Demo Browser");

        setSize(600, 600);
        setLocation(40, 40);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Create the window contents now. Assumes that the universe
     * variable is already set.
     */
    protected void createWindow(ViewpointManager vpMgr,
                                WorldLoaderManager wlm) {

        CursorManager cm =
            new CursorManager(canvas, null, console);
        universe.addSensorStatusListener(cm);
        universe.addNavigationStateListener(cm);

        Container content_pane = getContentPane();

        SwingLocationToolbar tb =
            new SwingLocationToolbar(universe,
                                     wlm,
                                     false,
                                     true,
                                     true,
                                     System.getProperty("user.dir"),
                                     null,
                                     console);

        SwingNavigationToolbar nav_tb =
            new SwingNavigationToolbar(universe,
                                       null,
                                       console);

        SwingViewpointToolbar vp_tb =
            new SwingViewpointToolbar(universe,
                                      vpMgr,
                                      null,
                                      console);

        SwingConsoleButton console_button =
            new SwingConsoleButton(console, null);

        statusBar = new SwingStatusBar(universe,
                                       true,
                                       true,
                                       null,
                                       console);

        JPanel p2 = new JPanel(new BorderLayout());

        p2.add(nav_tb, BorderLayout.WEST);
        p2.add(vp_tb, BorderLayout.CENTER);
        p2.add(console_button, BorderLayout.EAST);
        p2.add(statusBar, BorderLayout.SOUTH);

        content_pane.add(tb, BorderLayout.NORTH);
        content_pane.add(p2, BorderLayout.SOUTH);

        console.setVisible(true);
    }

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     *
     * @param core The core representation of the browser
     * @param loader Loader manager for doing async calls
     */
    protected void setupProperties(BrowserCore core, WorldLoaderManager loader) {
        // Disable font cache to fix getBounds nullPointer bug
        System.setProperty("sun.awt.font.advancecache","off");

        System.setProperty("uri.content.handler.pkgs",
                           "vlc.net.content");

        System.setProperty("uri.protocol.handler.pkgs",
                           "vlc.net.protocol");

        System.setProperty("java.content.handler.pkgs",
                           "vlc.net.content");


        // Imageloader
/*
        System.setProperty("java.content.handler.pkgs",
                           "vlc.net.content");
*/
        URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
        if(!(res_fac instanceof Web3DResourceFactory)) {
            res_fac = new Web3DResourceFactory(res_fac);
            URI.setURIResourceStreamFactory(res_fac);
        }

        ContentHandlerFactory c_fac = URI.getContentHandlerFactory();
        if(!(c_fac instanceof AWTContentHandlerFactory)) {
            c_fac = new AWTContentHandlerFactory(core, loader, c_fac);
            URI.setContentHandlerFactory(c_fac);
        }

        FileNameMap fn_map = URI.getFileNameMap();
        if(!(fn_map instanceof VRMLFileNameMap)) {
            fn_map = new VRMLFileNameMap(fn_map);
            URI.setFileNameMap(fn_map);
        }

        Web3DURNResolver resolver = new Web3DURNResolver();
        resolver.registerPrefixLocation(UMEL_PREFIX, UMEL_INSTALL_DIR);
        resolver.registerPrefixLocation(GEOVRML_PREFIX, GEOVRML_INSTALL_DIR);

        URN.addResolver(resolver);
    }
}
