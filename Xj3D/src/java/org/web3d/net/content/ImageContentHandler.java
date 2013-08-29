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

package org.web3d.net.content;

// External imports
import java.awt.image.AffineTransformOp;

import java.util.HashMap;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.nio.ByteBuffer;

import org.ietf.uri.ContentHandler;
import org.ietf.uri.ResourceConnection;

// Local imports
import org.web3d.image.AreaAveragingScaleFilter;
import org.web3d.image.NIOBufferImage;
import org.web3d.image.NIOBufferImageType;
import org.web3d.image.ScaleFilter;

import org.web3d.util.PropertyTools;
import org.web3d.util.MathUtils;

/**
 * Base abstract ContentHandler implementation for loading images
 * from a URI resource connection.
 *
 * @author  Rex Melton
 * @version $Revision: 1.4 $
 */
public abstract class ImageContentHandler extends ContentHandler {
    
    /** The -prefered- image content Class */
    private static final String PREFERED_IMAGE_CLASSNAME = 
        "vlc.image.ByteBufferImage";
        
    /** Property describing the rescaling method to use */
    private static final String RESCALE_PROP =
        "org.web3d.vrml.renderer.common.nodes.shape.rescale";
    
    /** The default rescale method */
    private static final int DEFAULT_RESCALE =
        AffineTransformOp.TYPE_BILINEAR;
    
    /** Property mapping for rescale */
    private static final HashMap rescaleMap;
    
    /** Property describing the default max size */
    private static final String MAX_TEXTURE_SIZE_PROP =
        "org.web3d.vrml.renderer.common.nodes.shape.maxTextureSize";
    
    /** The default maxTextureSize value, -1 = infinite */
    private static final int DEFAULT_MAX_TEXTURE_SIZE = -1;
    
    /** Property describing the use of mipmaps */
    private static final String USE_MIPMAPS_PROP =
        "org.web3d.vrml.renderer.common.nodes.shape.useMipMaps";
    
    /** The default useMipMaps value */
    private static final boolean DEFAULT_USE_MIPMAPS = false;
    
    /** The value read from the system property for RESCALE */
    protected static final int rescale;
    
    /** How should we rescale the image, up to nearest power of two, lower */
    protected static final boolean imageScaleUp;
    
    /** The value read from the system property for MAX_TEXTURE_SIZE_PROP */
    protected static final int maxTextureSize;
    
    /** The value read from the system property for MIPMAPS */
    protected static boolean useMipMaps;
    
    /** The -prefered- image Class */
    protected static Class image_class;
    
    // Static constructor
    static {
        imageScaleUp = false;
        
        rescaleMap = new HashMap(2);
        rescaleMap.put("BILINEAR",
            new Integer(AffineTransformOp.TYPE_BILINEAR));
        rescaleMap.put("NEAREST_NEIGBOR",
            new Integer(AffineTransformOp.TYPE_NEAREST_NEIGHBOR));
        
        rescale = PropertyTools.fetchSystemProperty(RESCALE_PROP,
            DEFAULT_RESCALE,
            rescaleMap);
        
        int size = PropertyTools.fetchSystemProperty(MAX_TEXTURE_SIZE_PROP,
            DEFAULT_MAX_TEXTURE_SIZE);
        
        int newSize = MathUtils.nearestPowerTwo( size, false );
        
        if (size != -1 && size != newSize) {
            System.out.println("Maximum texture size not a power of two.  Changed to: " + newSize);
            maxTextureSize = newSize;
        } else {
            maxTextureSize = size;
        }
        
        useMipMaps = PropertyTools.fetchSystemProperty(USE_MIPMAPS_PROP,
            DEFAULT_USE_MIPMAPS);
        
        /////////////////////////////////////////////////////////////////////////
        try {
            // first, check that the ImageDecoder can be instantiated.
            Class decoder_class = Class.forName( "vlc.net.content.image.ImageDecoder" );
            Object decoder_object = decoder_class.newInstance();
            
            // if that works, then get the image loader class
            image_class = Class.forName( PREFERED_IMAGE_CLASSNAME );
            
        } catch (Throwable t) {
            //System.out.println( "Native image loading unavailable, using default." );
        }
        /////////////////////////////////////////////////////////////////////////
    }
    
    /**
     * Construct a new instance of the content handler.
     */
    protected ImageContentHandler( ) {
    }
    
    /**
     * Given a fresh stream from a ResourceConnection, read and create an object
     * instance.
     *
     * @param resc The resource connection to read the data from
     * @return The object read in by the content handler
     * @exception IOException The connection stuffed up.
     */
    public abstract Object getContent(ResourceConnection resc)
        throws IOException;
    
    /**
     * Convert and return the argument content Object of the argument Class into
     * an NIOBufferImage. If the conversion fails for any reason, null will be
     * returned.
     *
     * @param image_class The Class of the content Object
     * @param content The Object containing the image data
     * @return An NIOBufferImage
     */
    protected static NIOBufferImage convert( Class image_class, Object content ) {
        NIOBufferImage image = null;
        try {
            Class[] nullClass = new Class[0];
            Object[] nullObject = new Object[0];
            
            Method getHeight = image_class.getMethod( "getHeight", nullClass );
            Method getWidth = image_class.getMethod( "getWidth", nullClass );
            Method getBuffer = image_class.getMethod( "getBuffer", nullClass );
            Method getType = image_class.getMethod( "getType", nullClass );
            
            Integer heightI = (Integer)getHeight.invoke( content, nullObject );
            Integer widthI = (Integer)getWidth.invoke( content, nullObject );
            ByteBuffer buffer = (ByteBuffer)getBuffer.invoke( content, nullObject );
            Integer typeI = (Integer)getType.invoke( content, nullObject );
            
            image = new NIOBufferImage(
                widthI.intValue( ),
                heightI.intValue( ),
                NIOBufferImageType.getType( typeI.intValue( ) ),
                buffer );
        }
        catch ( NoSuchMethodException nsme ) {
            System.out.println( nsme.getMessage( ) );
        }
        catch ( SecurityException se ) {
            System.out.println( se.getMessage( ) );
        }
        catch ( IllegalAccessException iae ) {
            System.out.println( iae.getMessage( ) );
        }
        catch ( InvocationTargetException ite ) {
            System.out.println( ite.getMessage( ) );
        }
        return( image );
    }
    
    /**
     * Check for the existance of our prefered image class. If found,
     * return it's Class object. Otherwise, return null.
     *
     * @return The prefered Class object, or null if it does not exist.
     */
    protected static Class getPreferedImageClass( ) {
        return( image_class );
    }
    
    /**
     * Scale and create mipmaps of the argument image. The initial image will
     * be resized as necessary to dimensions that are a power of 2 and less
     * than or equal to the maximum size (if defined). Mipmaps will be generated
     * as specified from the resized initial image and the array of images returned.
     *
     * @param image The initial image
     * @return The array containing the scaled image and it's mipmaps
     */
    protected NIOBufferImage[] preprocess( NIOBufferImage image ) {
        
        int width = image.getWidth( );
        int height = image.getHeight( );
        
        int newWidth = MathUtils.nearestPowerTwo( width, imageScaleUp );
        int newHeight = MathUtils.nearestPowerTwo( height, imageScaleUp );
        
        if (maxTextureSize > 0) {
            float factor;
            if (newWidth == newHeight) {
                if (newWidth > maxTextureSize) {
                    factor = maxTextureSize / (float) newWidth;
                    newWidth = (int)(factor * newWidth);
                    newHeight = (int)(factor * newHeight);
                }
            } else if (newWidth > newHeight) {
                if (newWidth > maxTextureSize) {
                    factor = maxTextureSize / (float) newWidth;
                    newWidth = (int)(factor * newWidth);
                    newHeight = (int)(factor * newHeight);
                }
            } else {
                if (newHeight > maxTextureSize) {
                    factor = maxTextureSize / (float) newHeight;
                    newWidth = (int)(factor * newWidth);
                    newHeight = (int)(factor * newHeight);
                }
            }
        }
        
        ScaleFilter filter = new AreaAveragingScaleFilter( image );
        
        if ( width != newWidth || height != newHeight ) {
            image = filter.getScaledImage( newWidth, newHeight );
        }
        
        NIOBufferImage[] ret_val;
        
        if ( useMipMaps ) {
            int idx=0;
            int level = Math.max( MathUtils.computeLog( newWidth ), MathUtils.computeLog( newHeight ) ) + 1;
            
            ret_val = new NIOBufferImage[level];
            ret_val[0] = image;
            
            for( int i = 1; i < level; i++ ) {
                if ( newWidth > 1 ) {
                    newWidth >>= 1;
                }
                if ( newHeight > 1 ) {
                    newHeight >>= 1;
                }
                ret_val[i] = filter.getScaledImage( newWidth, newHeight );
            }
        } else {
            ret_val = new NIOBufferImage[1];
            ret_val[0] = image;
        }
        return( ret_val );
    }
    
    /**
     * Extract the individual image byte buffers from the array of
     * NIOBufferImages and consolidate them into a single byte buffer
     * array in the returned NIOBufferImage. This is used to place 
     * the rescaled image and it's mipmaps into a single container.
     *
     * @param imageArray The primary image and set of mipmaps
     * @return The consolidated NIOBufferImage
     */
    protected NIOBufferImage consolidate( NIOBufferImage[] imageArray ) {
        NIOBufferImage ret_img = null;
        if ( ( imageArray != null ) && ( imageArray[0] != null ) ) {
            ret_img = imageArray[0];
            int levels = imageArray.length;
            if ( levels > 1 ) {
                // if there are mipmaps, create the buffer array
                ByteBuffer[] buffer = new ByteBuffer[levels];
                for ( int i = 0; i < levels; i++ ) {
                    buffer[i] = imageArray[i].getBuffer( );
                }
                ret_img.setBuffer( buffer );
            }
        }
        return( ret_img );
    }
}
