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
import java.io.InputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;

import org.eclipse.swt.graphics.ImageData;

import org.ietf.uri.ContentHandler;
import org.ietf.uri.ResourceConnection;

// Local imports
import org.web3d.image.NIOBufferImage;
import org.web3d.image.NIOBufferImageType;

import org.web3d.net.content.ImageContentHandler;

import org.web3d.util.MathUtils;

import org.xj3d.ui.swt.util.ImageUtils;

/**
 * Content handler implementation for loading images with the SWT
 * toolkit from a URI resource connection. The loaded images are
 * preprocessed to the appropriate size (power of 2) and mipmaps
 * are generated if specified.
 * <p>
 *
 * The following properties are used by this class:
 * <ul>
 * <li><code>org.web3d.vrml.renderer.common.nodes.shape.maxTextureSize</code> The
 *    maximum texture size to use.  By default texture sizes are unlimited.  Textures
 *    with a dimension over this value will be resized.  The resizing will try to
 *    preserve the aspect ratio.  This must be a power of two.</li>
 * <li><code>org.web3d.vrml.renderer.common.nodes.shape.useMipMaps</code> Force the
 *    use of mipmaps</li>
 * </ul>
 *
 * @author  Rex Melton
 * @version $Revision: 1.5 $
 */
class SWTImageContentHandler extends ImageContentHandler {
    
    /**
     * Construct a new instance of the content handler.
     */
    SWTImageContentHandler( ) {
    }
    
    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------
    
    /**
     * Given a fresh stream from a ResourceConnection, read and create an object
     * instance.
     *
     * @param resc The resource connection to read the data from
     * @return The object read in by the content handler
     * @exception IOException The connection stuffed up.
     */
    public Object getContent( ResourceConnection resc )
        throws IOException {
        
        // first, check for the existance of our prefered image format
        Class image_class = getPreferedImageClass( );
        if ( image_class != null ) {
            // if the image class exists, we presume that
            // it's handler is configured
            Class[] c = new Class[]{image_class};
            String url_string = resc.getURI( ).toExternalForm( );
            try {
                Object content = new URL( url_string ).openConnection( ).getContent( c );
                
                if ( image_class.isInstance( content ) ) {
                    NIOBufferImage niobi = convert( image_class, content );
                    if ( niobi != null ) {
                        // rescale the image if necessary and create mipmaps
                        NIOBufferImage[] imageArray = preprocess( niobi );
                        NIOBufferImage ret_img = consolidate( imageArray );
                        return( ret_img );
                    }
                }
            }
            catch ( MalformedURLException mue ) {
                System.out.println( mue.getMessage( ) );
            }
        }
        // otherwise, fallback to SWT toolkit specific mechanisms
        ImageData image = null;
        
        try {
            InputStream stream = resc.getInputStream( );
            image = new ImageData( stream );
        } catch ( SWTException swte ) {
            System.out.println( "Exception loading image: " + resc.getURI( ).toExternalForm( ) );
            System.out.println( swte.getMessage( ) );
        }
        
        if ( image != null ) {
            ImageUtils imageUtils = new ImageUtils( imageScaleUp, useMipMaps, maxTextureSize );
            ImageData[] imageArray = imageUtils.preprocess( image );
            NIOBufferImage ret_img = imageUtils.consolidate( imageArray );
            return( ret_img );
        }
        return( null );
    }
}
