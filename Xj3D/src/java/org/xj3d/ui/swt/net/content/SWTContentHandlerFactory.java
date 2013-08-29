/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.swt.net.content;

// External imports
import org.ietf.uri.ContentHandlerFactory;
import org.ietf.uri.ContentHandler;

// Local imports
import org.web3d.browser.BrowserCore;

import org.web3d.net.content.VRMLContentHandlerFactory;

import org.xj3d.core.loading.WorldLoaderManager;

/**
 * A <code>VRMLContentHandlerFactory</code> subclass supporting 
 * SWT UI toolkit dependent content handlers.
 *
 * @author  Rex Melton
 * @version $Revision: 1.2 $
 */
public class SWTContentHandlerFactory extends VRMLContentHandlerFactory {

    /** The mimetype prefix for image content */
    private static final String IMAGE_TYPE_PREFIX = "image/";
	
    /**
     * Create a default content handler factory that does not chain to any
     * further packages. This is the same as calling
     * <code>VRMLContentHandlerFactory(factory, clk, null)</code>.
     *
     * @param browser The core representation of the browser
     * @param wlm Loader manager for doing async calls
     */
    public SWTContentHandlerFactory( BrowserCore browser,
                                     WorldLoaderManager wlm) {
        super( browser, wlm, null );
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
    public SWTContentHandlerFactory( BrowserCore browser,
                                     WorldLoaderManager wlm,
                                     ContentHandlerFactory fac) {
        super( browser, wlm, fac );
    }

    /**
     * Create a content handler for the given mime type. If this cannot
     * create a handler then the request will be handled by it's parent.
     *
     * @param contentType The MIME type of the handler needed
     * @return A valid content handler for the type or null
     */
    public ContentHandler createContentHandler( String contentType ) {

		if ( contentType.startsWith( IMAGE_TYPE_PREFIX ) ) {
			return( new SWTImageContentHandler( ) );
        } 
		else {
            return( super.createContentHandler( contentType ) );
        }
    }
}
