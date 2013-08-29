/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.swt.util;

// External imports
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

// Local imports
import org.web3d.image.NIOBufferImage;
import org.web3d.image.NIOBufferImageType;

import org.web3d.util.MathUtils;

/**
 * Utilities for processing SWT ImageData objects to be used as textures.
 *
 * @author  Rex Melton
 * @version $Revision: 1.1 $
 */
public class ImageUtils {
    
    /** How should we rescale the image, up to nearest power of two, lower */
    private boolean imageScaleUp = false;
    
    /** The maximum texture size per dimension, must be a power of 2 */
    private int maxTextureSize = -1;
    
    /** Flag indicating whether to create mipmaps when processing an image */
    private boolean useMipMaps = true;
    
    /**
     * Constructor
     */
    public ImageUtils( ) {
    }
    
    /**
     * Constructor
     *
     * @param imageScaleUp Flag indicating how image rescales should be handled.
     * true if the image should be rescaled up to the nearest power of two, false
     * if the image should be rescaled down to the nearest power of two.
     * @param useMipMaps Flag indicating whether to create mipmaps when processing
     * an image.
     * @param maxTextureSize The maximum texture size per dimension, must be a 
     * power of two. A negative integer allows an unlimited image size. A positive
     * non-power of two integer will be converted to the nearest smaller power of two.
     */
    public ImageUtils( boolean imageScaleUp, boolean useMipMaps, int maxTextureSize ) {
        
        this.imageScaleUp = imageScaleUp;
        this.useMipMaps = useMipMaps;
    
        if ( maxTextureSize != -1 ) {
            int newSize = MathUtils.nearestPowerTwo( maxTextureSize, false );
            if ( maxTextureSize != newSize ) {
                maxTextureSize = newSize;
            }
        }
        this.maxTextureSize = maxTextureSize;
    }
    
    /**
     * Return an <code>NIOBufferImage</code> representation of the argument image.
     *
     * @param image The image to covert
     * @return The <code>NIOBufferImage</code> representation
     */
    public NIOBufferImage toNIOBufferImage( ImageData image ) {
        
        int height = image.height;
        int width = image.width;
        
        PaletteData palette = image.palette;
        
        ByteBuffer buffer = null;
        NIOBufferImageType format = null;
        boolean isGrayScale = true;
        
        int transparency = image.getTransparencyType( );
        
        if ( transparency != SWT.TRANSPARENCY_NONE ) {
            // there IS transparency, therefore a 4 component image
            format = NIOBufferImageType.RGBA;
            
            buffer = ByteBuffer.allocate( width * height * format.size );
            buffer.order( ByteOrder.nativeOrder( ) );
            
            int[] pixel = new int[width];
            
            if ( palette.isDirect ) {
                // a direct palette
                if ( transparency == SWT.TRANSPARENCY_PIXEL ) {
                    // with a transparent pixel
                    int transparent_pixel = image.transparentPixel;
                    
                    int y_inv = height - 1;
                    
                    for( int y = 0; y < height; y++ ) {
                        image.getPixels( 0, y_inv, width, pixel, 0 );
                        for( int x = 0; x < width; x++ ) {
                            int pix = pixel[x];
                            RGB rgb = palette.getRGB( pix );
                            if ( isGrayScale ) { 
                                isGrayScale = (rgb.red == rgb.green) | (rgb.green == rgb.blue); 
                            }
                            buffer.put( (byte)rgb.red );
                            buffer.put( (byte)rgb.green );
                            buffer.put( (byte)rgb.blue );
                            byte alpha = ( pix == transparent_pixel ) ? (byte)0 : (byte)255;
                            buffer.put( alpha );
                        }
                        y_inv--;
                    }
                }
                else if ( image.alpha == -1 ) {
                    // no global alpha, therefore alpha defined per pixel
                    byte[] alpha = new byte[width];
                    
                    int y_inv = height - 1;
                    
                    for( int y = 0; y < height; y++ ) {
                        image.getAlphas( 0, y_inv, width, alpha, 0 );
                        image.getPixels( 0, y_inv, width, pixel, 0 );
                        for( int x = 0; x < width; x++ ) {
                            RGB rgb = palette.getRGB( pixel[x] );
                            if ( isGrayScale ) { 
                                isGrayScale = (rgb.red == rgb.green) | (rgb.green == rgb.blue); 
                            }
                            buffer.put( (byte)rgb.red );
                            buffer.put( (byte)rgb.green );
                            buffer.put( (byte)rgb.blue );
                            buffer.put( alpha[x] );
                        }
                        y_inv--;
                    }
                }
                else {
                    // a global alpha, applied to each pixel
                    byte alpha = (byte)image.alpha;
                    
                    int y_inv = height - 1;
                    
                    for( int y = 0; y < height; y++ ) {
                        image.getPixels( 0, y_inv, width, pixel, 0 );
                        for( int x = 0; x < width; x++ ) {
                            RGB rgb = palette.getRGB( pixel[x] );
                            if ( isGrayScale ) { 
                                isGrayScale = (rgb.red == rgb.green) | (rgb.green == rgb.blue); 
                            }
                            buffer.put( (byte)rgb.red );
                            buffer.put( (byte)rgb.green );
                            buffer.put( (byte)rgb.blue );
                            buffer.put( alpha );
                        }
                        y_inv--;
                    }
                }
            }
            else {
                // an indexed palette, with transparency - i.e,  a transparent pixel
                int transparent_pixel_index = image.transparentPixel;
                
                int y_inv = height - 1;
                
                for( int y = 0; y < height; y++ ) {
                    image.getPixels( 0, y_inv, width, pixel, 0 );
                    for( int x = 0; x < width; x++ ) {
                        int pixel_index = pixel[x];
                        RGB rgb = palette.getRGB( pixel_index );
                        if ( isGrayScale ) { 
                            isGrayScale = (rgb.red == rgb.green) | (rgb.green == rgb.blue); 
                        }
                        buffer.put( (byte)rgb.red );
                        buffer.put( (byte)rgb.green );
                        buffer.put( (byte)rgb.blue );
                        byte alpha = ( pixel_index == transparent_pixel_index ) ? (byte)0 : (byte)255;
                        buffer.put( alpha );
                    }
                    y_inv--;
                }
            }
        }
        else {
            // there is NO transparency, therefore a 3 component image
            format = NIOBufferImageType.RGB;
            
            buffer = ByteBuffer.allocate( width * height * format.size );
            buffer.order( ByteOrder.nativeOrder( ) );
            
            int[] pixel = new int[width];
            
            if ( palette.isDirect ) {
                // a direct palette, no transparency
                
                int y_inv = height - 1;
                
                for( int y = 0; y < height; y++ ) {
                    image.getPixels( 0, y_inv, width, pixel, 0 );
                    for( int x = 0; x < width; x++ ) {
                        RGB rgb = palette.getRGB( pixel[x] );
                        if ( isGrayScale ) { 
                            isGrayScale = (rgb.red == rgb.green) | (rgb.green == rgb.blue); 
                        }
                        buffer.put( (byte)rgb.red );
                        buffer.put( (byte)rgb.green );
                        buffer.put( (byte)rgb.blue );
                    }
                    y_inv--;
                }
            }
            else {
                // an indexed palette, no transparency
                
                int y_inv = height - 1;
                
                for( int y = 0; y < height; y++ ) {
                    image.getPixels( 0, y_inv, width, pixel, 0 );
                    for( int x = 0; x < width; x++ ) {
                        int pixel_index = pixel[x];
                        RGB rgb = palette.getRGB( pixel_index );
                        if ( isGrayScale ) { 
                            isGrayScale = (rgb.red == rgb.green) | (rgb.green == rgb.blue); 
                        }
                        buffer.put( (byte)rgb.red );
                        buffer.put( (byte)rgb.green );
                        buffer.put( (byte)rgb.blue );
                    }
                    y_inv--;
                }
            }
        }
        
        return( new NIOBufferImage( width, height, format, isGrayScale, buffer ) );
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
    public ImageData[] preprocess( ImageData image ) {
        
        int width = image.width;
        int height = image.height;
        
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
        
        if ( width != newWidth || height != newHeight ) {
            image = scale( image, newWidth, newHeight );
        }
        
        ImageData[] ret_val;
        
        if ( useMipMaps ) {
            int idx=0;
            int level = Math.max( 
                MathUtils.computeLog( newWidth ), 
                MathUtils.computeLog( newHeight ) ) + 1;
            
            ret_val = new ImageData[level];
            ret_val[0] = image;
            
            for( int i = 1; i < level; i++ ) {
                if ( newWidth > 1 ) {
                    newWidth >>= 1;
                }
                if ( newHeight > 1 ) {
                    newHeight >>= 1;
                }
                ret_val[i] = scale( image, newWidth, newHeight );
            }
        } else {
            ret_val = new ImageData[1];
            ret_val[0] = image;
        }
        return( ret_val );
    }
    
    /**
     * Convert the ImageData objects into NIOBufferImages and
     * extract the individual image byte buffers from the resulting
     * NIOBufferImages and consolidate them into a single byte buffer
     * array in the returned NIOBufferImage. This is used to place 
     * the rescaled image and it's mipmaps into a single container.
     *
     * @param imageArray The primary image and set of mipmaps
     * @return The consolidated NIOBufferImage
     */
    public NIOBufferImage consolidate( ImageData[] imageArray ) {
        NIOBufferImage ret_img = null;
        if ( ( imageArray != null ) && ( imageArray[0] != null ) ) {
            ret_img = toNIOBufferImage( imageArray[0] );
            int levels = imageArray.length;
            if ( levels > 1 ) {
                // if there are mipmaps, create the buffer array
                ByteBuffer[] buffer = new ByteBuffer[levels];
                buffer[0] = ret_img.getBuffer( );
                for ( int i = 1; i < levels; i++ ) {
                    NIOBufferImage image = toNIOBufferImage( imageArray[i] );
                    buffer[i] = image.getBuffer( );
                }
                ret_img.setBuffer( buffer );
            }
        }
        return( ret_img );
    }
    
    /**
     * Return a new <code>ImageData</code> object scaled from the argument
     * <code>ImageData</code>to the specified width and height. If the argument
     * is already the specified size, it will be returned unchanged.
     *
     * @param image The image to scale
     * @param width The width for the new image
     * @param height The height of the new image
     * @return An <code>ImageData</code> sized as requested
     */
    public ImageData scale( ImageData image, int width, int height ) {
        
        if ( ( width != image.width ) && ( height != image.height ) ) {
            return( image.scaledTo( width, height) );
        }
        else {
            return( image );
        }
    }
}
