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
 
package org.web3d.image;

// External imports
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// Local imports
// none

/**
 * A ScaleFilter implementation for scaling NIOBufferImages using area averaging.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class AreaAveragingScaleFilter implements ScaleFilter {
    
    /** The width of the source image. */
    private int srcWidth;
    
    /** The height of the source image. */
    private int srcHeight;
    
    /** The source image type */
    private NIOBufferImageType srcType;
    
    /** Flag indicating that the image data should be treated as
     *  grayscale, regardless of whether it has more than 2 components.
     *  Used to compensate for 'deficiencies' in 2 component image
     *  handling */
    private boolean srcIsGrayScale;
    
    /** The number of components in the source image */
    private int srcCmp;
    
    /** The image buffer */
    private ByteBuffer srcBuffer;
    
    /** The scale filter class */
    private static Class scale_filter_class;
    
    /** Constructor for the scale filter class object */
    private static Constructor scale_filter_constructor;
    
    /** Method within the scale filter class for generating the scaled image */
    private static Method scale_image_method;
    
    /** Flag indicating that native acceleration is available */
    private static boolean hasNativeLib;
    
    /** Scratch arrays for computing target pixel data */
    private float red[], green[], blue[], alpha[];
    
    /** precomputed pixel count of image */
    private float srcPixelCount;
    
    // static constructor
    static {
        try {
            // get the class 
            scale_filter_class = Class.forName( "vlc.image.ImageScaleFilter" );
            
            // get the class's constructor with the appropriate arguments
            scale_filter_constructor = scale_filter_class.getConstructor( new Class[]{ 
                    String.class } );
            
            // get the scalor method
            scale_image_method = scale_filter_class.getMethod( "getScaledImage", new Class[]{
                    int.class,			// srcWidth
                    int.class,			// srcHeight
                    int.class,			// srcCmp
                    ByteBuffer.class,	// srcBuffer
                    int.class,			// dstWidth
                    int.class,			// dstHeight
                } );
            
            // node-ify that we'll be going native
            hasNativeLib = true;
        }
        catch ( ClassNotFoundException cnfe ) {
            //System.out.println( "ClassNotFoundException: "+ cnfe.getMessage( ) );
        }
        catch ( NoSuchMethodException nsme ) {
            //System.out.println( "NoSuchMethodException: "+ nsme.getMessage( ) );
        }
        catch ( SecurityException se ) {
            //System.out.println( "SecurityException: "+ se.getMessage( ) );
        }
    }
    
    /**
     * Constructor
     *
     * @param image The source image that will be rescaled
     */
    public AreaAveragingScaleFilter( NIOBufferImage image ) {
        if ( image == null ) {
            throw new NullPointerException( "AreaAveragingScaleFilter: Argument image must be non-null" ); 
        }
        srcWidth = image.getWidth( );
        srcHeight = image.getHeight( );
        srcType = image.getType( );
        srcCmp = srcType.size;
        srcIsGrayScale = image.isGrayScale( );
        srcBuffer = image.getBuffer( );
        srcPixelCount = ((float)srcWidth) * srcHeight;
    }
    
    //----------------------------------------------------------
    // Method defined by ScaleFilter
    //----------------------------------------------------------
    
    /**
     * Return an image scaled to the specified width and height
     *
     * @param dstWidth The width of the returned image
     * @param dstHeight The height of the returned image
     * @return The scaled image
     */
    public NIOBufferImage getScaledImage( int dstWidth, int dstHeight ) {
        NIOBufferImage dstImage = null;
        if ( hasNativeLib ) {
            // use the native version of this filter
            try {
                // instantiate the object
                Object object = scale_filter_constructor.newInstance( new Object[]{ 
                        "AreaAverage" } );
                
                // invoke the scalor method to generate the buffer
                ByteBuffer dstBuffer = (ByteBuffer)scale_image_method.invoke( object, new Object[]{
                        srcWidth,
                        srcHeight,
                        srcCmp,
                        srcBuffer,
                        dstWidth,
                        dstHeight,
                    } );
                
                // create the new image
                dstImage = new NIOBufferImage(
                    dstWidth,
                    dstHeight,
                    srcType,
                    srcIsGrayScale,
                    dstBuffer );
                
            } catch ( InstantiationException ie ) {
                //System.out.println( "InstantiationException: "+ ie.getMessage( ) );
            } catch ( InvocationTargetException ite ) {
                //System.out.println( "InvocationTargetException: "+ ite.getMessage( ) );
            } catch ( IllegalAccessException iae ) {
                //System.out.println( "IllegalAccessException: "+ iae.getMessage( ) );
            }
        } else {
            // no native version of this filter is available, fallback to Java impl
            ByteBuffer dstBuffer = ByteBuffer.allocate( dstWidth * dstHeight * srcCmp );
            dstBuffer.order( ByteOrder.nativeOrder( ) );
            
            srcBuffer.rewind( );
            
            switch( srcCmp ) {
            case 4:
                fillFourCompBuffer( dstWidth, dstHeight, srcBuffer, dstBuffer );
                break;
            case 3:
                fillThreeCompBuffer( dstWidth, dstHeight, srcBuffer, dstBuffer );
                break;
            case 2:
                fillTwoCompBuffer( dstWidth, dstHeight, srcBuffer, dstBuffer );
                break;
            case 1:
                fillOneCompBuffer( dstWidth, dstHeight, srcBuffer, dstBuffer );
                break;
                
            }
            
            dstImage = new NIOBufferImage( 
                dstWidth, 
                dstHeight, 
                srcType, 
                srcIsGrayScale, 
                dstBuffer );
        }
        
        return( dstImage );
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Initialize the destination byte buffer with image data scaled to the
     * width and height specified from the source byte buffer.
     *
     * @param destWidth The width of the destination image
     * @param destHeight The height of the destination image
     * @param srcBuffer The image source data
     * @param destBuffer The buffer to initialize with the scaled image data
     */
    private void fillFourCompBuffer( 
        int destWidth, 
        int destHeight, 
        ByteBuffer srcBuffer, 
        ByteBuffer destBuffer ) {
        
        red = new float[destWidth];
        green = new float[destWidth];
        blue = new float[destWidth];
        alpha = new float[destWidth];
        
        byte[] row_data = new byte[destWidth*srcCmp];
        
        int sy = 0;
        int syrem = destHeight;
        int dy = 0;
        int dyrem = 0;
        
        int srcRowByteOffset = 0;
        
        while ( sy < srcHeight ) {
            int amty;
            if ( dyrem == 0 ) {
                for ( int i = 0; i < destWidth; i++ ) {
                    alpha[i] = red[i] = green[i] = blue[i] = 0f;
                }
                dyrem = srcHeight;
            }
            if ( syrem < dyrem ) {
                amty = syrem;
            } else {
                amty = dyrem;
            }
            int sx = 0;
            int dx = 0;
            int sxrem = 0;
            int dxrem = srcWidth;
            float a = 0f, r = 0f, g = 0f, b = 0f;
            while ( sx < srcWidth ) {
                if ( sxrem == 0 ) {
                    sxrem = destWidth;
                    int srcColByteOffset = sx * srcCmp;
                    r = 0xff & srcBuffer.get( srcRowByteOffset + srcColByteOffset++ );
                    g = 0xff & srcBuffer.get( srcRowByteOffset + srcColByteOffset++ );
                    b = 0xff & srcBuffer.get( srcRowByteOffset + srcColByteOffset++ );
                    a = 0xff & srcBuffer.get( srcRowByteOffset + srcColByteOffset );
                    // premultiply the components if necessary
                    if ( a != 255.0f ) {
                        float ascale = a / 255.0f;
                        r *= ascale;
                        g *= ascale;
                        b *= ascale;
                    }
                }
                int amtx;
                if ( sxrem < dxrem ) {
                    amtx = sxrem;
                } else {
                    amtx = dxrem;
                }
                float mult = ((float)amtx) * amty;
                alpha[dx] += mult * a;
                red[dx] += mult * r;
                green[dx] += mult * g;
                blue[dx] += mult * b;
                if ( ( sxrem -= amtx ) == 0 ) {
                    sx++;
                }
                if ( ( dxrem -= amtx ) == 0 ) {
                    dx++;
                    dxrem = srcWidth;
                }
            }
            if ( ( dyrem -= amty ) == 0 ) {
                process4CompRow( destWidth, row_data );
                do {
                    destBuffer.put( row_data );
                    dy++;
                } while ( ( ( syrem -= amty ) >= amty ) && ( amty == srcHeight ) );
            } else {
                syrem -= amty;
            }
            if ( syrem == 0 ) {
                syrem = destHeight;
                sy++;
                srcRowByteOffset += srcWidth * srcCmp;
            }
        }
    }
    
    /**
     * Initialize the image data for a destination row.
     *
     * @param width The width of the target image row
     * @param row_data The byte array to initialize with image data
     */
    private void process4CompRow( int width, byte[] row_data ) {
        int index = 0;
        for ( int x = 0; x < width; x++ ) {
            float mult = srcPixelCount;
            int a = Math.round( alpha[x] / mult );
            if ( a <= 0 ) {
                a = 0;
            } else if ( a >= 255 ) {
                a = 255;
            } else {
                // un-premultiply the components (by modifying mult here, we
                // are effectively doing the divide by mult and divide by
                // alpha in the same step)
                mult = alpha[x] / 255;
            }
            int r = Math.round( red[x] / mult );
            int g = Math.round( green[x] / mult );
            int b = Math.round( blue[x] / mult );
            
            if ( r < 0 ) { 
                r = 0; 
            } else if ( r > 255 ) { 
                r = 255; 
            }
            if ( g < 0 ) { 
                g = 0; 
            } else if ( g > 255 ) { 
                g = 255; 
            }
            if ( b < 0 ) { 
                b = 0; 
            } else if ( b > 255 ) { 
                b = 255; 
            }
            
            row_data[index++] = (byte)r;
            row_data[index++] = (byte)g;
            row_data[index++] = (byte)b;
            row_data[index++] = (byte)a;
        }
    }
    
    /**
     * Initialize the destination byte buffer with image data scaled to the
     * width and height specified from the source byte buffer.
     *
     * @param destWidth The width of the destination image
     * @param destHeight The height of the destination image
     * @param srcBuffer The image source data
     * @param destBuffer The buffer to initialize with the scaled image data
     */
    private void fillThreeCompBuffer( 
        int destWidth, 
        int destHeight, 
        ByteBuffer srcBuffer, 
        ByteBuffer destBuffer ) {
        
        red = new float[destWidth];
        green = new float[destWidth];
        blue = new float[destWidth];
        
        byte[] row_data = new byte[destWidth*srcCmp];
        
        int sy = 0;
        int syrem = destHeight;
        int dy = 0;
        int dyrem = 0;
        
        int srcRowByteOffset = 0;
        
        while ( sy < srcHeight ) {
            int amty;
            if ( dyrem == 0 ) {
                for ( int i = 0; i < destWidth; i++ ) {
                    red[i] = green[i] = blue[i] = 0f;
                }
                dyrem = srcHeight;
            }
            if ( syrem < dyrem ) {
                amty = syrem;
            } else {
                amty = dyrem;
            }
            int sx = 0;
            int dx = 0;
            int sxrem = 0;
            int dxrem = srcWidth;
            float r = 0f, g = 0f, b = 0f;
            while ( sx < srcWidth ) {
                if ( sxrem == 0 ) {
                    sxrem = destWidth;
                    int srcColByteOffset = sx * srcCmp;
                    r = 0xff & srcBuffer.get( srcRowByteOffset + srcColByteOffset++ );
                    g = 0xff & srcBuffer.get( srcRowByteOffset + srcColByteOffset++ );
                    b = 0xff & srcBuffer.get( srcRowByteOffset + srcColByteOffset );
                }
                int amtx;
                if ( sxrem < dxrem ) {
                    amtx = sxrem;
                } else {
                    amtx = dxrem;
                }
                float mult = ((float)amtx) * amty;
                red[dx] += mult * r;
                green[dx] += mult * g;
                blue[dx] += mult * b;
                if ( ( sxrem -= amtx ) == 0 ) {
                    sx++;
                }
                if ( ( dxrem -= amtx ) == 0 ) {
                    dx++;
                    dxrem = srcWidth;
                }
            }
            if ( ( dyrem -= amty ) == 0 ) {
                process3CompRow( destWidth, row_data );
                do {
                    destBuffer.put( row_data );
                    dy++;
                } while ( ( ( syrem -= amty ) >= amty ) && ( amty == srcHeight ) );
            } else {
                syrem -= amty;
            }
            if ( syrem == 0 ) {
                syrem = destHeight;
                sy++;
                srcRowByteOffset += srcWidth * srcCmp;
            }
        }
    }
    
    /**
     * Initialize the image data for a destination row.
     *
     * @param width The width of the target image row
     * @param row_data The byte array to initialize with image data
     */
    private void process3CompRow( int width, byte[] row_data ) {
        int index = 0;
        for ( int x = 0; x < width; x++ ) {
            float mult = srcPixelCount;
            int r = Math.round( red[x] / mult );
            int g = Math.round( green[x] / mult );
            int b = Math.round( blue[x] / mult );
            
            if ( r < 0 ) { 
                r = 0; 
            } else if ( r > 255 ) { 
                r = 255; 
            }
            if ( g < 0 ) { 
                g = 0; 
            } else if ( g > 255 ) { 
                g = 255; 
            }
            if ( b < 0 ) { 
                b = 0; 
            } else if ( b > 255 ) { 
                b = 255; 
            }
            
            row_data[index++] = (byte)r;
            row_data[index++] = (byte)g;
            row_data[index++] = (byte)b;
        }
    }
    
    /**
     * Initialize the destination byte buffer with image data scaled to the
     * width and height specified from the source byte buffer.
     *
     * @param destWidth The width of the destination image
     * @param destHeight The height of the destination image
     * @param srcBuffer The image source data
     * @param destBuffer The buffer to initialize with the scaled image data
     */
    private void fillTwoCompBuffer( 
        int destWidth, 
        int destHeight, 
        ByteBuffer srcBuffer, 
        ByteBuffer destBuffer ) {
        
        red = new float[destWidth];
        alpha = new float[destWidth];
        
        byte[] row_data = new byte[destWidth*srcCmp];
        
        int sy = 0;
        int syrem = destHeight;
        int dy = 0;
        int dyrem = 0;
        
        int srcRowByteOffset = 0;
        
        while ( sy < srcHeight ) {
            int amty;
            if ( dyrem == 0 ) {
                for ( int i = 0; i < destWidth; i++ ) {
                    alpha[i] = red[i] = 0f;
                }
                dyrem = srcHeight;
            }
            if ( syrem < dyrem ) {
                amty = syrem;
            } else {
                amty = dyrem;
            }
            int sx = 0;
            int dx = 0;
            int sxrem = 0;
            int dxrem = srcWidth;
            float a = 0f, r = 0f;
            while ( sx < srcWidth ) {
                if ( sxrem == 0 ) {
                    sxrem = destWidth;
                    int srcColByteOffset = sx * srcCmp;
                    r = 0xff & srcBuffer.get( srcRowByteOffset + srcColByteOffset++ );
                    a = 0xff & srcBuffer.get( srcRowByteOffset + srcColByteOffset );
                    // premultiply the components if necessary
                    if ( a != 255.0f ) {
                        float ascale = a / 255.0f;
                        r *= ascale;
                    }
                }
                int amtx;
                if ( sxrem < dxrem ) {
                    amtx = sxrem;
                } else {
                    amtx = dxrem;
                }
                float mult = ((float)amtx) * amty;
                alpha[dx] += mult * a;
                red[dx] += mult * r;
                if ( ( sxrem -= amtx ) == 0 ) {
                    sx++;
                }
                if ( ( dxrem -= amtx ) == 0 ) {
                    dx++;
                    dxrem = srcWidth;
                }
            }
            if ( ( dyrem -= amty ) == 0 ) {
                process2CompRow( destWidth, row_data );
                do {
                    destBuffer.put( row_data );
                    dy++;
                } while ( ( ( syrem -= amty ) >= amty ) && ( amty == srcHeight ) );
            } else {
                syrem -= amty;
            }
            if ( syrem == 0 ) {
                syrem = destHeight;
                sy++;
                srcRowByteOffset += srcWidth * srcCmp;
            }
        }
    }
    
    /**
     * Initialize the image data for a destination row.
     *
     * @param width The width of the target image row
     * @param row_data The byte array to initialize with image data
     */
    private void process2CompRow( int width, byte[] row_data ) {
        int index = 0;
        for ( int x = 0; x < width; x++ ) {
            float mult = srcPixelCount;
            int a = Math.round( alpha[x] / mult );
            if ( a <= 0 ) {
                a = 0;
            } else if ( a >= 255 ) {
                a = 255;
            } else {
                // un-premultiply the components (by modifying mult here, we
                // are effectively doing the divide by mult and divide by
                // alpha in the same step)
                mult = alpha[x] / 255;
            }
            int r = Math.round( red[x] / mult );
            
            if ( r < 0 ) { 
                r = 0; 
            } else if ( r > 255 ) { 
                r = 255; 
            }
            
            row_data[index++] = (byte)r;
            row_data[index++] = (byte)a;
        }
    }
    
    /**
     * Initialize the destination byte buffer with image data scaled to the
     * width and height specified from the source byte buffer.
     *
     * @param destWidth The width of the destination image
     * @param destHeight The height of the destination image
     * @param srcBuffer The image source data
     * @param destBuffer The buffer to initialize with the scaled image data
     */
    private void fillOneCompBuffer( 
        int destWidth, 
        int destHeight, 
        ByteBuffer srcBuffer, 
        ByteBuffer destBuffer ) {
        
        red = new float[destWidth];
        
        byte[] row_data = new byte[destWidth*srcCmp];
        
        int sy = 0;
        int syrem = destHeight;
        int dy = 0;
        int dyrem = 0;
        
        int srcRowByteOffset = 0;
        
        while ( sy < srcHeight ) {
            int amty;
            if ( dyrem == 0 ) {
                for ( int i = 0; i < destWidth; i++ ) {
                    red[i] = 0f;
                }
                dyrem = srcHeight;
            }
            if ( syrem < dyrem ) {
                amty = syrem;
            } else {
                amty = dyrem;
            }
            int sx = 0;
            int dx = 0;
            int sxrem = 0;
            int dxrem = srcWidth;
            float r = 0f;
            while ( sx < srcWidth ) {
                if ( sxrem == 0 ) {
                    sxrem = destWidth;
                    int srcColByteOffset = sx * srcCmp;
                    r = 0xff & srcBuffer.get( srcRowByteOffset + srcColByteOffset );
                }
                int amtx;
                if ( sxrem < dxrem ) {
                    amtx = sxrem;
                } else {
                    amtx = dxrem;
                }
                float mult = ((float)amtx) * amty;
                red[dx] += mult * r;
                if ( ( sxrem -= amtx ) == 0 ) {
                    sx++;
                }
                if ( ( dxrem -= amtx ) == 0 ) {
                    dx++;
                    dxrem = srcWidth;
                }
            }
            if ( ( dyrem -= amty ) == 0 ) {
                process1CompRow( destWidth, row_data );
                do {
                    destBuffer.put( row_data );
                    dy++;
                } while ( ( ( syrem -= amty ) >= amty ) && ( amty == srcHeight ) );
            } else {
                syrem -= amty;
            }
            if ( syrem == 0 ) {
                syrem = destHeight;
                sy++;
                srcRowByteOffset += srcWidth * srcCmp;
            }
        }
    }
    
    /**
     * Initialize the image data for a destination row.
     *
     * @param width The width of the target image row
     * @param row_data The byte array to initialize with image data
     */
    private void process1CompRow( int width, byte[] row_data ) {
        int index = 0;
        for ( int x = 0; x < width; x++ ) {
            float mult = srcPixelCount;
            int r = Math.round( red[x] / mult );
            
            if ( r < 0 ) { 
                r = 0; 
            } else if ( r > 255 ) { 
                r = 255; 
            }
            
            row_data[index++] = (byte)r;
        }
    }
}
