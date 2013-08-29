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

package org.web3d.net.content;

// External imports
import org.ietf.uri.ContentHandlerFactory;
import org.ietf.uri.ContentHandler;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.util.HashSet;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * A Factory implementation for VRML content handlers that produces VRML
 * output.
 * <p>
 *
 * The factory implementation only handles UTF8 encoded files currently.
 * We have not yet built a handler for XML encoded files.
 * <p>
 *
 * This factory is not automatically registered with the system. If you want
 * or need to load inlines, then you must create and instance of this factory
 * and register it as a content handler factory with the URI system. For
 * example
 * <pre>
 *     BrowserCore core = ....
 *     WorldLoaderManager loader = ....
 *
 *     ContentHandlerFactory c_fac = URI.getContentHandlerFactory();
 *     if(!(c_fac instanceof VRMLContentHandlerFactory)) {
 *         c_fac = new VRMLContentHandlerFactory(core, loader, c_fac);
 *         URI.setContentHandlerFactory(c_fac);
 *     }
 * </pre>
 *
 * If a previous content handler was registered and it was not one of this
 * class it will be chained to this class. If we can't find the content then
 * we can ask the chained factory.
 *
 * @author  Justin Couch
 * @version $Revision: 1.13 $
 */
public class VRMLContentHandlerFactory implements ContentHandlerFactory {

    /** The set of accepted mime types for UTF8 content */
    private static final HashSet UTF8_TYPES;

    /** The set of accepted types for XML content */
    private static final HashSet XML_TYPES;

    /** The set of accepted mime types for binary content */
    private static final HashSet BINARY_TYPES;

    private static final HashSet ECMASCRIPT_TYPES;

    /** The set of accepted types for audio content */
    private static final HashSet AUDIO_TYPES;

    /** The set of accepted types for movie content */
    private static final HashSet MOVIE_TYPES;

    /** A previous content handler factory we could delegate to */
    private ContentHandlerFactory nextFactory;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** Browser core for the loading */
    private BrowserCore core;

    /**
     * Static constructor builds the type lists for use by all instances
     */
    static {
        UTF8_TYPES = new HashSet();

        UTF8_TYPES.add("model/vrml");
        UTF8_TYPES.add("x-world/x-vrml");
        UTF8_TYPES.add("model/x3d+vrml");

        XML_TYPES = new HashSet();
        XML_TYPES.add("model/x3d+xml");

        BINARY_TYPES = new HashSet();
        BINARY_TYPES.add("model/x3d+binary");
        BINARY_TYPES.add("model/x3d+fastinfoset");

        // no types registered yet.
        ECMASCRIPT_TYPES = new HashSet();
        ECMASCRIPT_TYPES.add("application/x-javascript");
        ECMASCRIPT_TYPES.add("application/javascript");
        ECMASCRIPT_TYPES.add("application/x-ecmascript");
        ECMASCRIPT_TYPES.add("application/ecmascript");

        AUDIO_TYPES = new HashSet();
        AUDIO_TYPES.add("audio/x-wav");
        AUDIO_TYPES.add("audio/wav");

        MOVIE_TYPES = new HashSet();
        MOVIE_TYPES.add("video/mpeg");
        MOVIE_TYPES.add("video/x-mpeg");
        MOVIE_TYPES.add("video/mpeg-system");
        MOVIE_TYPES.add("video/x-mpeg-system");
    }

    /**
     * Create a default content handler factory that does not chain to any
     * further packages. This is the same as calling
     * <code>VRMLContentHandlerFactory(factory, clk, null)</code>.
     *
     * @param browser The core representation of the browser
     * @param wlm Loader manager for doing async calls
     */
    public VRMLContentHandlerFactory(BrowserCore browser,
                                     WorldLoaderManager wlm) {
        this(browser, wlm, null);
    }

    /**
     * Create a content handler factory that may delegate to another factory
     * if this one cannot support the content types requested. If the reference
     * is null, then there is no chained factory.
     *
     * @param browser The core representation of the browser
     * @param fac The factory to delegate to if we fail
     * @param wlm Loader manager for doing async calls
     */
    public VRMLContentHandlerFactory(BrowserCore browser,
                                     WorldLoaderManager wlm,
                                     ContentHandlerFactory fac) {
        nextFactory = fac;
        core = browser;
        worldLoader = wlm;
    }

    /**
     * Create a content handler for the given mime type. If the factory cannot
     * create a handler then it shall return <CODE>null</CODE> to allow other
     * methods to be used.
     *
     * @param contentType The MIME type of the handler needed
     * @return A valid content handler for the type or null
     */
    public ContentHandler createContentHandler(String contentType) {
        ContentHandler ret_val = null;

        if(UTF8_TYPES.contains(contentType)) {
            ret_val = new Utf8ContentHandler(core, worldLoader);
        } else if(XML_TYPES.contains(contentType)) {
            ret_val = new Utf8ContentHandler(core, worldLoader);
        } else if(BINARY_TYPES.contains(contentType)) {
System.out.println("***Should this be a BinaryContentHandler instead of utf8?");
            ret_val = new Utf8ContentHandler(core, worldLoader);
        } else if(ECMASCRIPT_TYPES.contains(contentType)) {
            ret_val = new JavascriptContentHandler();
        } else if(AUDIO_TYPES.contains(contentType)) {
            ret_val = new AudioContentHandler();
        } else if(MOVIE_TYPES.contains(contentType)) {
            ret_val = new MovieContentHandler();
        } else if(nextFactory != null) {
            ret_val = nextFactory.createContentHandler(contentType);
        }
        return ret_val;
    }

    /**
     * Get the chained content handler factory if it has been set. If none
     * has been set, this will return null.
     *
     * @return The wrapped content handler factory or null
     */
    public ContentHandlerFactory getWrappedFactory() {
        return nextFactory;
    }
}
