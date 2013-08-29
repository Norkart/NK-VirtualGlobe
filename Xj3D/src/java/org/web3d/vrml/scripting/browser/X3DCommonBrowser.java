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

package org.web3d.vrml.scripting.browser;

// External imports
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringBufferInputStream;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.browser.BrowserCore;
import org.web3d.util.ErrorReporter;
import org.web3d.util.IntHashMap;
import org.web3d.vrml.util.URLChecker;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.VRMLParseException;

import org.web3d.x3d.jaxp.X3DEntityResolver;
import org.web3d.x3d.jaxp.X3DErrorHandler;
import org.web3d.x3d.jaxp.X3DSAVAdapter;

import org.web3d.x3d.sai.NotSupportedException;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.SceneBuilder;
import org.xj3d.core.loading.SceneBuilderFactory;
import org.xj3d.core.loading.WorldLoader;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * The common parts of a browser implementation suitable for X3D scripting.
 * <P>
 *
 * This class is a full implementation because the external and scripting
 * interfaces will use it differently. External use can just subclass and
 * add the SAI Browser interface. For scripting, the need to extend the
 * Browser base class means that this class will need to be delegated to.
 * <p>
 *
 * The current implementation ignores any parameter values provided by the
 * world.
 *
 * @author Justin Couch
 * @version $Revision: 1.24 $
 */
public class X3DCommonBrowser extends CommonBrowser {

    /** Reporter to interface with the DOM traversal */
    private X3DErrorHandler domErrorHandler;

    /** Transformation handler when dealing with importDocument */
    private Transformer transformer;

    /** The holder of the scene after parsing from the importDocument */
    private SceneBuilder sceneBuilder;

    /** Representation of the SAX output from importDocument */
    private SAXResult saxOutput;

    /** Resolver used to locate code. */
    private X3DEntityResolver xmlResolver;

    /** The viewpoint manager for next/previous/first/last calls */
    private ViewpointManager viewpointManager;

    /**
     * Create a browser instance that represents the given universe details.
     * If the scene builder or factory is null, then it will find one from
     * the global pool defined for the renderer used by the browser core.
     *
     * @param browser The core representation of the browser
     * @param vpm The manager for viewpoints
     * @param rm A route manager for users creating/removing routes
     * @param wlm Loader manager for doing async calls
     * @param fsm State manager for coordinating inter-frame processing
     * @throws IllegalArgumentException A paramter is null
     */
    public X3DCommonBrowser(BrowserCore browser,
                            ViewpointManager vpm,
                            RouteManager rm,
                            FrameStateManager fsm,
                            WorldLoaderManager wlm) {

        super(browser, rm, fsm, wlm);

        viewpointManager = vpm;
    }

    /**
     * Get the contained viewpoint manager instance. This may be used for more
     * precise control of the viewpoint handling than the basic interface
     * provided by this class.
     */
    public ViewpointManager getViewpointManager() {
        return viewpointManager;
    }

    /**
     * Replace the current world with the given nodes. Replaces the entire
     * contents of the VRML world with the new nodes. Any node references that
     * belonged to the previous world are still valid but no longer form part of
     * the scene graph (unless it is these nodes passed to this method). The
     * URL of the world still represents the just unloaded world.
     * <P>
     * Calling this method causes a SHUTDOWN event followed by an INITIALIZED
     * event to be generated.
     *
     * @param scene The new scene instance to use
     */
    public void replaceWorld(VRMLScene scene) {
        SceneUpdateTraverser.updateScene((AbstractScene)scene);
        core.setScene(scene, null);
    }

    /**
     * Parse the given input stream and turn this into a list of X3D nodes. Method
     * is a blocking call that won't return until all of the top level nodes
     * defined in the string have been returned.
     * <P>
     * At the point that this method returns, external files such as textures,
     * sounds and inlines may not have been loaded.
     * <P>
     * The stream may contain all legal X3D syntax. The X3D header line is not
     * required to be present in the string.
     *
     * @param stream The stream containing VRML string syntax
     * @return The scene that was generated from the syntax
     * @throws VRMLException General error during processing
     * @throws VRMLParseException If the string does not contain legal
     *    VRML syntax or no node instantiations
     */
    public VRMLScene createX3DFromStream(InputStream stream)
        throws VRMLException, VRMLParseException, IOException {

        InputSource is = new InputSource(core.getWorldURL(), stream);
        WorldLoader ldr = loaderManager.fetchLoader();

        int major = 0;
        int minor = 0;

        VRMLExecutionSpace space = core.getWorldExecutionSpace();

        if(space != null) {
            BasicScene old_scene = space.getContainedScene();
            major = old_scene.getSpecificationMajorVersion();
            minor = old_scene.getSpecificationMinorVersion();
        }

        VRMLScene scene = ldr.loadNow(core, is, true, major, minor);

        loaderManager.releaseLoader(ldr);

        return scene;
    }

    /**
     * Parse the given string and turn this into a list of X3D nodes. Method
     * is a blocking call that won't return until all of the top level nodes
     * defined in the string have been returned.
     * <P>
     * At the point that this method returns, external files such as textures,
     * sounds and inlines may not have been loaded.
     * <P>
     * The string may contain all legal X3D syntax. The X3D header line is not
     * required to be present in the string.
     *
     * @param x3dSyntax The string containing X3D string syntax
     * @return The scene that was generated from the syntax
     * @throws VRMLException General error during processing
     * @throws VRMLParseException If the string does not contain legal
     *    VRML syntax or no node instantiations
     */
    public VRMLScene createX3DFromString(String x3dSyntax)
        throws VRMLException, VRMLParseException, IOException {

        // Readers no longer work in InputSource, so switched to an InputStream
        //StringReader reader = new StringReader(x3dSyntax);
        StringBufferInputStream reader = new StringBufferInputStream(x3dSyntax);
        InputSource is = new InputSource(core.getWorldURL(), reader);
        WorldLoader ldr = loaderManager.fetchLoader();

        int major = 0;
        int minor = 0;

        VRMLExecutionSpace space = core.getWorldExecutionSpace();

        if(space != null) {
            BasicScene old_scene = space.getContainedScene();
            major = old_scene.getSpecificationMajorVersion();
            minor = old_scene.getSpecificationMinorVersion();
        }

        VRMLScene scene = ldr.loadNow(core, is, true, major, minor);

        loaderManager.releaseLoader(ldr);

        return scene;
    }

    /**
     * Create and load X3D from the given URL and place the returned values
     * as nodes into the given X3D node in the scene. The difference between
     * this and loadURL is that this method does not replace the entire scene
     * with the contents from the URL. Instead, it places the return values
     * as events in the nominated node and MFNode eventIn.
     *
     * @param urls The list of URLs in decreasing order of preference as defined
     *   in the X3D specification.
     */
    public VRMLScene createX3DFromURL(String[] urls) {

        VRMLScene ret_val = null;
        WorldLoader ldr = loaderManager.fetchLoader();

        urls = completeUrl(urls);

        int major = 0;
        int minor = 0;

        VRMLExecutionSpace space = core.getWorldExecutionSpace();

        if(space != null) {
            BasicScene old_scene = space.getContainedScene();
            major = old_scene.getSpecificationMajorVersion();
            minor = old_scene.getSpecificationMinorVersion();
        }

        for(int i = 0; i < urls.length; i++) {
            InputSource is = new InputSource(urls[i]);

            try {
                ret_val = ldr.loadNow(core, is, false, major, minor);
            } catch(IOException ioe) {
                ioe.printStackTrace();
            } catch(VRMLParseException e) {
                errorReporter.errorReport("Processing URL: " + urls[i] +
                                          " " + e.getMessage(), e);
            }

            if(ret_val != null)
                break;
        }

        loaderManager.releaseLoader(ldr);

        return ret_val;
    }

    /**
     * Implementation of the importDocument optional capabilities.
     */
    public VRMLScene importDocument(Node domNode)
        throws NotSupportedException {

        if(!(domNode instanceof Document))
            throw new NotSupportedException("Xj3D does not support importing " +
                                            "anything other than a Document yet");


        Source dom_src = new DOMSource(domNode);

        if(domErrorHandler == null) {
            SceneBuilderFactory b_fac =
                loaderManager.getBuilderFactory(core.getRendererType());
            sceneBuilder = b_fac.createBuilder();
            sceneBuilder.setFrameStateManager(stateManager);
            sceneBuilder.setErrorReporter(errorReporter);

            domErrorHandler = new X3DErrorHandler();
            domErrorHandler.setErrorReporter(errorReporter);

            X3DSAVAdapter adap = new X3DSAVAdapter();
            adap.setContentHandler(sceneBuilder);
            adap.setProtoHandler(sceneBuilder);
            adap.setRouteHandler(sceneBuilder);
            adap.setScriptHandler(sceneBuilder);

            // Need a better way todo this
            String path = System.getProperty("user.dir");
            path = "file://" + path.substring(3);
            adap.setLoadState(path, "DOM", true);

            xmlResolver = new X3DEntityResolver();

            try {
                TransformerFactory t_fac = TransformerFactory.newInstance();
                transformer = t_fac.newTransformer();
            } catch(TransformerException te) {
                errorReporter.warningReport("Error creating transformer: ", te);
            }

            saxOutput = new SAXResult(adap);
        }

        VRMLScene parsed_scene = null;

        try {
            sceneBuilder.reset();
            transformer.transform(dom_src, saxOutput);

            parsed_scene = sceneBuilder.getScene();
            sceneBuilder.releaseScene();
        } catch(TransformerException te) {
            errorReporter.warningReport("Error importing document", te);
        }

        return parsed_scene;
    }

    /**
     * Register an error reporter with the CommonBrowser instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        super.setErrorReporter(reporter);

        if(domErrorHandler != null)
            domErrorHandler.setErrorReporter(errorReporter);
    }

    /**
     * Bind the next viewpoint in the list. The definition of "next" is not
     * specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     */
    public void nextViewpoint() {
        viewpointManager.nextViewpoint();
    }

    /**
     * Bind the next viewpoint in the list. The definition of "next" is not
     * specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     *
     * @param layer The ID of the layer. Must be between 0 and the maximum
     *   layer number
     */
    public void nextViewpoint(int layer) {
        viewpointManager.nextViewpoint(layer);
    }

    /**
     * Bind the previous viewpoint in the list. The definition of "previous" is
     * not specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     */
    public void previousViewpoint() {
        viewpointManager.previousViewpoint();
    }

    /**
     * Bind the previous viewpoint in the list. The definition of "previous" is
     * not specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     *
     * @param layer The ID of the layer. Must be between 0 and the maximum
     *   layer number
     */
    public void previousViewpoint(int layer) {
        viewpointManager.previousViewpoint(layer);
    }

    /**
     * Bind the first viewpoint in the list. This is the first viewpoint
     * declared in the user's file. ie The viewpoint that would be bound by
     * default on loading.
     */
    public void firstViewpoint() {
        viewpointManager.firstViewpoint();
    }

    /**
     * Bind the first viewpoint in the list. This is the first viewpoint
     * declared in the user's file. ie The viewpoint that would be bound by
     * default on loading.
     *
     * @param layer The ID of the layer. Must be between 0 and the maximum
     *   layer number
     */
    public void firstViewpoint(int layer) {
        viewpointManager.firstViewpoint(layer);
    }

    /**
     * Bind the last viewpoint in the list.
     */
    public void lastViewpoint() {
        viewpointManager.lastViewpoint();
    }

    /**
     * Bind the last viewpoint in the list.
     *
     * @param layer The ID of the layer. Must be between 0 and the maximum
     *   layer number
     */
    public void lastViewpoint(int layer) {
        viewpointManager.lastViewpoint(layer);
    }
}
