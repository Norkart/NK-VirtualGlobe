/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter;

// External imports
import java.io.IOException;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.ErrorReporter;

/**
 * Representation of any parser implementation that would like to convert itself
 * to an equivalent X3D form to use within the filter conversion chain.
 * <p>
 *
 * The implied contract of implementing this interface is that the non-X3D/VRML
 * file format parser implementation knows how to convert it's own knowledge
 * into something that exactly replicates the X3D or VRML97 file format. It is
 * required that the implementor knows how to transform this foreign format to
 * X3D/VRML97 and issues the correct stream of events to make this happen.
 * Remember that the receiver of the stream is assuming correctly formated file
 * syntax, so if you give it an invalid ordering of callbacks, something is
 * bound to die rather horribly.
 * <p>
 *
 * Instances of this class are created using reflection. Each implementation
 * requires a public no-argument constructor to be present.
 *
 * @author Justin Couch
 * @version Grammar $Revision: 1.3 $
 */
public interface NonWeb3DFileParser  {

    /**
     * Initialise the internals of the parser at start up. If you are not using
     * the detailed constructors, this needs to be called to ensure that all
     * internal state is correctly set up.
     */
    public void initialize();

    /**
     * Set the base URL of the document that is about to be parsed. Users
     * should always call this to make sure we have correct behaviour for the
     * ContentHandler's <code>startDocument()</code> call.
     * <p>
     * The URL is cleared at the end of each document run. Therefore it is
     * imperative that it get's called each time you use the parser.
     *
     * @param url The document url to set
     */
    public void setDocumentUrl(String url);

    /**
     * Fetch the locator used by this parser. This is here so that the user of
     * this parser can ask for it and set it before calling startDocument().
     * Once the scene has started parsing in this class it is too late for the
     * locator to be set. This parser does set it internally when asked for a
     * {@link #Scene()} but there may be other times when it is not set.
     *
     * @return The locator used for syntax errors
     */
    public Locator getDocumentLocator();

    /**
     * Set the content handler instance.
     *
     * @param ch The content handler instance to use
     */
    public void setContentHandler(ContentHandler ch);

    /**
     * Set the route handler instance.
     *
     * @param rh The route handler instance to use
     */
    public void setRouteHandler(RouteHandler rh);

    /**
     * Set the script handler instance.
     *
     * @param sh The script handler instance to use
     */
    public void setScriptHandler(ScriptHandler sh);

    /**
     * Set the proto handler instance.
     *
     * @param ph The proto handler instance to use
     */
    public void setProtoHandler(ProtoHandler ph);

    /**
     * Set the error handler instance.
     *
     * @param eh The error handler instance to use
     */
    public void setErrorHandler(ErrorHandler eh);

    /**
     * Set the error reporter instance. If this is also an ErrorHandler
     * instance, the document locator will also be set.
     *
     * @param eh The error handler instance to use
     */
    public void setErrorReporter(ErrorReporter eh);

    /**
     * Parse the input now.
     *
     * @param input The stream to read from
     * @throws IOException An I/O error while reading the stream
     * @throws ImportFileFormatException A parsing error occurred in the file
     */
    public void parse(InputSource input)
        throws IOException, ImportFileFormatException;
}
