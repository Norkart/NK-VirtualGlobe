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

package org.web3d.vrml.scripting.sai;

// External imports
import java.lang.ref.ReferenceQueue;

// Local imports
import org.web3d.x3d.sai.Browser;
import org.web3d.x3d.sai.X3DScriptImplementation;
import org.web3d.browser.BrowserCore;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.scripting.InvalidScriptContentException;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.ScriptWrapper;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * An engine that supports X3D Java scripts only.
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public class JavaSAIScriptEngine implements ScriptEngine {

    /** The content types supported */
    private static final String[] CONTENT_TYPES = {
        "application/x-java",
        "application/java"
    };

    /** Factory for creating and maintaining browser instances */
    private JavaSAIScriptBrowserFactory browserFactory;

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

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
    public JavaSAIScriptEngine(BrowserCore browser,
                               ViewpointManager vpm,
                               RouteManager rm,
                               FrameStateManager fsm,
                               WorldLoaderManager wlm) {

        if(browser == null)
            throw new IllegalArgumentException("BrowserCore is null");

        if(wlm == null)
            throw new IllegalArgumentException("WorldLoaderManager is null");

        if(rm == null)
            throw new IllegalArgumentException("Routemanager is null");

        if(vpm == null)
            throw new IllegalArgumentException("ViewpointManager is null");

        if(fsm == null)
            throw new IllegalArgumentException("FrameStateManager is null");

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        browserFactory =
            new JavaSAIScriptBrowserFactory(browser, vpm, rm, wlm,fsm);
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
        return CONTENT_TYPES;
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
    public ScriptWrapper buildWrapper(VRMLExecutionSpace space,
                                      String contentType,
                                      Object content)
        throws InvalidScriptContentException {

        if(!(content instanceof Class))
            throw new InvalidScriptContentException("Not a Java class");

        // Let's check the base class.
        if(!X3DScriptImplementation.class.isAssignableFrom((Class)content))
            throw new InvalidScriptContentException("Not a X3D Java script");

        JavaSAIScriptWrapper wrapper;

        try {
            X3DScriptImplementation script =
                (X3DScriptImplementation)((Class)content).newInstance();

            wrapper = new JavaSAIScriptWrapper(script);
            InternalBrowser browser = browserFactory.getBrowser(space, wrapper);
            wrapper.setBrowser(browser);
        } catch(InstantiationException ie) {
            throw new InvalidScriptContentException("Could not instantiate " +
                                                    "script" +
                                                    content.getClass().getName());
        } catch(IllegalAccessException iae) {
            throw new InvalidScriptContentException("Could not instantiate " +
                                                    "script" +
                                                    content.getClass().getName());
        }

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

        browserFactory.setErrorReporter(errorReporter);
    }
}

