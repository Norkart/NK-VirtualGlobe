// External imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// stuff pulled from DemoFrame
import java.awt.*;
import javax.media.j3d.GraphicsConfigTemplate3D;

import javax.media.j3d.TransformGroup;
import javax.swing.*;

// Local imports
import org.web3d.vrml.sav.*;

import org.ietf.uri.ContentHandlerFactory;
import org.ietf.uri.FileNameMap;
import org.ietf.uri.URIResourceStreamFactory;
import org.ietf.uri.URI;

import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.JavascriptResourceFactory;
import org.web3d.vrml.j3d.J3DSceneBuilderFactory;
import org.web3d.vrml.j3d.browser.VRMLBrowserCanvas;
import org.web3d.vrml.j3d.browser.J3DStandardBrowserCore;
import org.web3d.vrml.j3d.input.LinkSelectionListener;
import org.web3d.vrml.j3d.nodes.J3DViewpointNodeType;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.VRMLLinkNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.xj3d.core.eventmodel.ExecutionSpaceManager;
import org.xj3d.core.eventmodel.RouteManager;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.scripting.SceneBuilder;
import org.web3d.vrml.scripting.SceneBuilderFactory;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.xj3d.core.loading.ContentLoadManager;
import org.xj3d.core.loading.ScriptLoader;

import org.xj3d.impl.core.eventmodel.ListsRouterFactory;
import org.xj3d.impl.core.loading.MemCacheLoadManager;
import org.xj3d.impl.core.loading.DefaultLoadManager;

import org.w3c.dom.*;
import org.web3d.vrml.j3d.J3DVRMLScene;
import org.web3d.x3d.dom.j3d.DOMtoJ3D;
import org.web3d.x3d.dom.j3d.DOMEventHandler;
import org.web3d.x3d.dom.j3d.DOMtoJ3DException;
import org.web3d.vrml.j3d.nodes.J3DWorldRoot;

/**
 * A simple browser example that has one window and the coder does all of the
 * setup.
 * <p>
 *
 * The simple browser does not respond to changes in the list of viewpoints
 * in the virtual world. This is OK because scripts are not used or needed in
 * this simple environment. Once we implement scripts, we have to look at
 * something different.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class X3DViewer extends JPanel
    implements LinkSelectionListener {

    // stuff pulled from DemoFrame
    protected GraphicsConfiguration gfxConfig;
    protected JLabel statusLabel;

    /** The parser factory that we are going to use. */
    private VRMLParserFactory parserFactory;

    /** The document builder instance used by the factory */
    private SceneBuilder sceneBuilder;

    /** The universe to place our scene into */
    private J3DStandardBrowserCore universe;

    /** The toolbar holding viewpoint information */
    private SwingViewpointToolbar vpToolbar;

    /** The toolbar holding navigation information */
    private NavigationToolbar navToolbar;

    /** Flag to indicate we are in the setup of the scene currently */
    private boolean inSetup;

    /** Mapping of def'd Viewpoints to their real implementation */
    private HashMap viewpointDefMap;

    /** Place for error messages to go */
    //    private ConsoleWindow console;

    /**
     * Create an instance of the demo class.
     */
    public X3DViewer() {
        super(new BorderLayout());

        // stuff pulled from DemoFrame
        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(template.REQUIRED);
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        gfxConfig = dev.getBestConfiguration(template);

        // begin original stuff
        viewpointDefMap = new HashMap();

        J3DSceneBuilderFactory b_fac =
            new J3DSceneBuilderFactory(false,
                                       true,
                                       true,
                                       true,
                                       true,
                                       true,
                                       true,
                                       true);

        sceneBuilder = b_fac.createBuilder();

        try {
            parserFactory = VRMLParserFactory.newVRMLParserFactory();
        } catch(FactoryConfigurationError fce) {
            throw new RuntimeException("Failed to load factory");
        }

    //        Container content_pane = getContentPane();

    //        JPanel p1 = new JPanel(new BorderLayout());
    //        content_pane.add(p1, BorderLayout.CENTER);

    //        console = new ConsoleWindow();

        // We also need a canvas to display stuff with and a universe to set
        // the content in.
        VRMLBrowserCanvas canvas = new VRMLBrowserCanvas(gfxConfig, false);

//        ContentLoadManager lm = new DefaultLoadManager();
        ContentLoadManager lm = new MemCacheLoadManager();
        ScriptLoader sl = new ScriptLoader();
        RouteManager rm = new ExecutionSpaceManager(lm, sl);
        rm.setRouterFactory(new ListsRouterFactory());

        // Need to load a bunch of script engines here....

        universe = new J3DStandardBrowserCore(rm, lm, sl);

        ScriptEngine jsai = new VRML97ScriptEngine(b_fac,
                                                   universe,
                                                   parserFactory,
                                                   rm);


    //        jsai.setErrorReporter(console);
        sl.registerScriptingEngine(jsai);

        ScriptEngine ecma = new ECMAScriptEngine(b_fac,
                                                 universe,
                                                 parserFactory,
                                                 rm);

    //        ecma.setErrorReporter(console);
        sl.registerScriptingEngine(ecma);

    //        p1.add(canvas, BorderLayout.CENTER);
        add(canvas, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout());
    //        p1.add(p2, BorderLayout.SOUTH);
        add(p2, BorderLayout.SOUTH);

        navToolbar = new NavigationToolbar(console);
        p2.add(navToolbar, BorderLayout.WEST);

        vpToolbar = new SwingViewpointToolbar(console);

        p2.add(vpToolbar, BorderLayout.CENTER);

        universe.setLinkSelectionListener(this);
        universe.addBrowserCanvas(canvas);

        setupProperties(b_fac);

    //        console.setVisible(true);

        statusLabel = new JLabel("Enter a world to load");
    }

    //----------------------------------------------------------
    // Methods required by the LinkSelectionListener interface.
    //----------------------------------------------------------

    /**
     * Invoked when a link node has been activated. This is the node that has
     * been selected.
     *
     * @param node The selected node
     */
    public void linkSelected(VRMLLinkNodeType node) {

        String[] url_list = node.getUrl();
        boolean success = false;

        for(int i = 0; i < url_list.length; i++) {
            if(url_list[i].charAt(0) == '#') {
                // move to the viewpoint.
                String def_name = url_list[i].substring(1);
                J3DViewpointNodeType vp =
                    (J3DViewpointNodeType)viewpointDefMap.get(def_name);

                if(vp != null) {
                    universe.changeViewpoint(vp);
                    success = true;
                } else {
                    statusLabel.setText("Unknown Viewpoint " + def_name);
            //                    console.warningReport("Unknown Viewpoint " + def_name, null);
                }
            } else {
                // load the world.
                try {
                    URL url = new URL(url_list[i]);
                    InputSource is = new InputSource(url);
                    if(success = load(is))
                        break;

                } catch(MalformedURLException mue) {
                    statusLabel.setText("Invalid URL");
            //                    console.warningReport("Invalid URL: " + url_list[i], mue);
                }
            }
        }

    //        if(!success)
    //            console.errorReport("No valid URLs were found", null);

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
        InputSource is = new InputSource(url);

        load(is);
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param file The file to load
     */
    public void gotoLocation(File file) {
        InputSource is = new InputSource(file);

        load(is);
    }

    protected void setWarning(String msg) {
        statusLabel.setText(msg);
    //        console.warningReport(msg, null);
    }

    protected void setError(String msg) {
        statusLabel.setText(msg);
    //        console.errorReport(msg, null);
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param is The inputsource for this reader
     * @return true if the world loaded correctly
     */
    public boolean load(InputSource is) {
        inSetup = true;

        boolean ret_val = false;

        VRMLReader vrml_reader = parserFactory.newVRMLReader();
        sceneBuilder.reset(universe.getVRMLClock());

    //        vrml_reader.setErrorHandler(console);
        vrml_reader.setContentHandler(sceneBuilder);
        vrml_reader.setScriptHandler(sceneBuilder);
        vrml_reader.setProtoHandler(sceneBuilder);
        vrml_reader.setRouteHandler(sceneBuilder);

        try {
            vrml_reader.parse(is);
        } catch(IOException ioe) {
            statusLabel.setText("I/O Error: " + ioe.getMessage());
        //            console.errorReport("I/O Error: ", ioe);
            return false;
        } catch(VRMLParseException vpe) {
            StringBuffer buf = new StringBuffer("Error Parsing VRML file\n");
            buf.append("Line: ");
            buf.append(vpe.getLineNumber());
            buf.append("\nStarting at column: ");
            buf.append(vpe.getColumnNumber());
            buf.append("\nMessage: ");
            buf.append(vpe.getMessage());
        //            console.warningReport(buf.toString(), null);
            return false;
        } catch(VRMLException se) {
            // everything else. Just a format exception
        //            console.errorReport(se.getMessage(), se);
            return false;
        }

        VRMLScene parsed_scene = sceneBuilder.getScene();
        universe.setScene(parsed_scene);

        ret_val = true;

        // Grab the list of viewpoints and place them into the toolbar.
        List vp_list =
            parsed_scene.getBindableNodes(VRMLScene.VIEWPOINT_BINDABLE);

        if((vp_list == null) || (vp_list.size() == 0))
            return ret_val;

        Iterator itr = vp_list.iterator();

        J3DViewpointNodeType active_vp = universe.getViewpoint();
        ViewpointData active_data = null;
        J3DViewpointNodeType node;
        ViewpointData[] data = new ViewpointData[vp_list.size()];
        int count = 0;
        String desc;
        TransformGroup tg;

        while(itr.hasNext()) {
            node = (J3DViewpointNodeType)itr.next();
            desc = node.getDescription();

            if((desc == null) || (desc.length() == 0)) {
                desc = "Viewpoint " + count;
            }

            tg = node.getPlatformGroup();

            data[count] = new ViewpointData(desc, count, tg);
            data[count].userData = node;

            if(node == active_vp)
                active_data = data[count];

            count++;
        }

        vpToolbar.setViewpoints(data);
        if(active_data != null) {
            vpToolbar.selectViewpoint(active_data);
        }

        // Finally set up the viewpoint def name list. Have to start from
        // the list of DEF names as the Viewpoint nodes don't store the DEF
        // name locally.
        viewpointDefMap.clear();
        Map def_map = parsed_scene.getDEFNodes();
        itr = def_map.keySet().iterator();

        while(itr.hasNext()) {
            String key = (String)itr.next();
            Object vp = def_map.get(key);

            if(vp instanceof J3DViewpointNodeType)
                viewpointDefMap.put(key, vp);
        }

        inSetup = false;

        return ret_val;
    }

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     *
     * @param fac The factory for scene builders
     */
    private void setupProperties(SceneBuilderFactory fac) {
        System.setProperty("uri.content.handler.pkgs",
            "vlc.net.content");
        System.setProperty("uri.protocol.handler.pkgs",
            "vlc.net.protocol");

        URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
        if(!(res_fac instanceof JavascriptResourceFactory)) {
            res_fac = new JavascriptResourceFactory(res_fac);
            URI.setURIResourceStreamFactory(res_fac);
        }

        ContentHandlerFactory c_fac = URI.getContentHandlerFactory();

        if(!(c_fac instanceof VRMLContentHandlerFactory)) {
            c_fac = new VRMLContentHandlerFactory(fac,
                                                  universe.getVRMLClock(),
                                                  c_fac);
            URI.setContentHandlerFactory(c_fac);
        }

        FileNameMap fn_map = URI.getFileNameMap();
        if(!(fn_map instanceof VRMLFileNameMap)) {
            fn_map = new VRMLFileNameMap(fn_map);
            URI.setFileNameMap(fn_map);
        }
    }

    /**
     * Convert the document to the Java3D representation.
     */
    public void setJava3D(Document document,
              String worldURL) {

        J3DVRMLScene sceneNode=null;

        // Convert DOM tree to J3D rep
        try {
            DOMtoJ3D domtoj3d = new DOMtoJ3D(worldURL, universe.getVRMLClock());
            sceneNode = domtoj3d.convertDoc(document);
        //      HashMap refMap = domtoj3d.getRefMap();

            //setSupportEvents(supportEvents);

            if (sceneNode.getRootNode() == null) {
                System.out.println("No RootNode.  Invalid URL?");
                J3DWorldRoot root = new J3DWorldRoot();
                sceneNode.setRootNode(root);
            }

            if (sceneNode != null) {
                universe.setScene(sceneNode);
            }
        } catch (DOMtoJ3DException e) {
            System.err.println("Error converting document" + e);
            e.printStackTrace();
            return;
        }

    }
}
