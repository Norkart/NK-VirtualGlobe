/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting;

// Standard imports
// none

// Application specific imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.VRMLExecutionSpace;

/**
 * A representation of a class that is capable of being an interface to a
 * scripting engine (eg javascript, java etc).
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface ScriptEngine {

    /**
     * Get the version of the specification that this engine implementation
     * supports. A script engine only supports a single major version of any
     * given spec.
     *
     * @return A number greater than one indicating the supported spec.
     */
    public int getSupportedSpecificationVersion();

    /**
     * Get a listing of the content types that this engine implementation
     * can handle.
     *
     * @return A non-empty list of content types
     */
    public String[] getSupportedContentTypes();


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
        throws InvalidScriptContentException;

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);
}

