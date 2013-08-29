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

package org.web3d.vrml.scripting.ecmascript;

// External imports
import org.mozilla.javascript.*;

// Local imports
import org.web3d.vrml.scripting.ecmascript.builtin.*;

import org.web3d.browser.BrowserCore;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.scripting.InvalidScriptContentException;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.ScriptWrapper;
import org.web3d.vrml.scripting.ecmascript.builtin.FieldFactory;
import org.web3d.vrml.scripting.ecmascript.builtin.ECMAFieldFactory;
import org.web3d.vrml.scripting.ecmascript.x3d.Browser;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * A scripting engine that provides capabilities for X3D ECMAScript.
 * <P>
 *
 * The setup process uses delayed loading of the Javascript interpreter. It
 * won't actually initialise Rhino until the first script wrapper needs to
 * be built.
 *
 * @author Justin Couch
 * @version $Revision: 1.17 $
 */
public class ECMAScriptEngine implements ScriptEngine {

    /** The set of MIME types this engine will support */
    private static final String[] MIME_TYPES = {
        "application/x-ecmascript",
        "application/ecmascript"
    };

    /** Factory for creating and maintaining browser instances */
    private ECMABrowserFactory browserFactory;

    /** The field factory for all to share */
    private FieldFactory fieldFactory;

    /** Global scope of standard objects that is shared among all */
    private Scriptable globalScope;

    /** The class that manages the error message management from Rhino */
    private ReportAdapter errorHandler;

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

    /** The global function impls */
    private Global global;

    /**
     * Construct a new script engine with a reference to the enclosing
     * browser.
     *
     * @param browser The core representation of the browser
     * @param vpm The manager for viewpoints
     * @param rm A route manager for users creating/removing routes
     * @param fsm The per-frame state manager
     * @param wlm Loader for full files
     * @throws IllegalArgumentException The browser reference is null
     */
    public ECMAScriptEngine(BrowserCore browser,
                            ViewpointManager vpm,
                            RouteManager rm,
                            FrameStateManager fsm,
                            WorldLoaderManager wlm) {

        if(browser == null)
            throw new IllegalArgumentException("BrowserCore is null");

        if(wlm == null)
            throw new IllegalArgumentException("WorldLoadManager is null");

        if(rm == null)
            throw new IllegalArgumentException("RouteManager is null");

        if(vpm == null)
            throw new IllegalArgumentException("ViewpointManager is null");

        if(fsm == null)
            throw new IllegalArgumentException("FrameState manager is null");

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        browserFactory = new ECMABrowserFactory(browser, vpm, rm, fsm, wlm);
        browserFactory.setErrorReporter(errorReporter);
    }

    /**
     * Get the version of the specification that this engine implementation
     * supports. A script engine only supports a single major version of any
     * given spec.
     *
     * @return A number greater than one indicating the supported spec.
     */
    public int getSupportedSpecificationVersion() {
        return 3;
    }

    /**
     * Get a listing of the content types that this engine implementation
     * can handle.
     *
     * @return A non-empty list of content types
     */
    public String[] getSupportedContentTypes() {
        return MIME_TYPES;
    }

    /**
     * Create a wrapper for the given script content. The wrapper will be
     * built based on the details from the mime type.
     *
     * @param space The execution space this script belongs to
     * @param contentType The MIME type of the engine
     * @param content The actual content loaded from a stream
     * @return An appropriate wrapper instance
     * @throws InvalidScriptContentException The form of the content does
     *    not match the requirements of the engine.
     */
    public synchronized ScriptWrapper buildWrapper(VRMLExecutionSpace space,
                                      String contentType,
                                      Object content)
        throws InvalidScriptContentException {

        if(!(content instanceof String))
            throw new InvalidScriptContentException("Not a javascript script");

        if(globalScope == null)
            setupRhino();

        Browser browser = browserFactory.getBrowser(space);
        browser.setErrorReporter(errorReporter);

        String script = (String)content;

        // TODO: The wrapper is not told the root URL yet :(
        ECMAScriptWrapper wrapper = new ECMAScriptWrapper(script,
                                                          null,
                                                          browser,
                                                          globalScope,
                                                          fieldFactory);

        wrapper.setErrorReporter(errorHandler);

        return wrapper;
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();

        if(errorHandler != null)
            errorHandler.setReporter(errorReporter);

        browserFactory.setErrorReporter(errorReporter);
    }

    /**
     * Build the standard objects now.
     */
    private void setupRhino() {
        errorHandler = new ReportAdapter();
        errorHandler.setReporter(errorReporter);

        fieldFactory = new ECMAFieldFactory();

        Context context = Context.enter();
        global = new Global();

        if (errorReporter != null)
            global.setErrorReporter(errorReporter);

        globalScope = context.initStandardObjects(global);

        String[] names = {"print"};

        try {
            global.defineFunctionProperties(names, Global.class,
                                            ScriptableObject.DONTENUM);
        } catch (PropertyException e) {
             throw new Error(e.getMessage());
        }

        Class[] std_classes = {
            SFColor.class,
            SFColorRGBA.class,
            SFImage.class,
            SFRotation.class,
            SFVec2f.class,
            SFVec3f.class,
            SFVec2d.class,
            SFVec3d.class,
            SFNode.class,
            MFBool.class,
            MFColor.class,
            MFColorRGBA.class,
            MFDouble.class,
            MFInt32.class,
            MFImage.class,
            MFFloat.class,
            MFTime.class,
            MFVec2f.class,
            MFVec3f.class,
            MFVec2d.class,
            MFVec3d.class,
            MFString.class,
            MFRotation.class,
            MFNode.class,
            Matrix3.class,
            Matrix4.class
        };

        // register all the standard classes for nodes and VRMLMatrix. This
        // is registered here so that they are shared across all instances of
        // scripts.
        for(int i = 0; i < std_classes.length; i++) {
            try {
                ScriptableObject.defineClass(globalScope, std_classes[i]);
            } catch(Exception iae) {
                // should never get this, so just dump the error msg
                StringBuffer buf = new StringBuffer("Error loading class ");
                buf.append(std_classes[i].getName());
                buf.append('\n');
                buf.append(iae.getMessage());

                Context.reportWarning(buf.toString());
            }
        }
        context.setErrorReporter(errorHandler);
        context.exit();
    }
}
