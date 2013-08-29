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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// Local imports
// none

//////////////////////////////////////////////////////////////////////////////
// rem
//
// Not sure how robust this really is. Algorithm taken from here:
// 
// http://www.codeproject.com/cs/media/imageprocessing4.asp?df=100&forumid=3657&exp=0&select=992091
//
// At a minimum, I think the handling of any image with an alpha channel is
// incorrect. The original algorithm only handled RGB.
//////////////////////////////////////////////////////////////////////////////

/**
 * A ScaleFilter implementation for scaling NIOBufferImages.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class BilinearScaleFilter implements ScaleFilter {
    
    /** The image object that will rescaled */
    private NIOBufferImage srcImage;
    
    /** The width of the source image. */
    private int srcWidth;
    
    /** The height of the source image. */
    private int srcHeight;
    
    /** The number of components in the source image */
    private int srcComponents;
    
    /**
     * Constructor
     *
     * @param image The source image that will be rescaled
     */
    public BilinearScaleFilter( NIOBufferImage image ) {
        srcImage = image;
        srcWidth = image.getWidth( );
        srcHeight = image.getHeight( );
        srcComponents = image.getType( ).size;
    }
    
    //----------------------------------------------------------
    // Method defined by ScaleFilter
    //----------------------------------------------------------
    
    /**
     * Return an image scaled to the specified width and height
     *
     * @param destWidth The width of the returned image
     * @param destHeight The height of the returned image
     * @return The scaled image
     */
    public NIOBufferImage getScaledImage( int destWidth, int destHeight ) {
        
        ByteBuffer destBuffer = ByteBuffer.allocate( destWidth * destHeight * srcComponents );
        destBuffer.order( ByteOrder.nativeOrder( ) );
        
        ByteBuffer srcBuffer = srcImage.getBuffer( );
        srcBuffer.rewind( );

        process( destWidth, destHeight, srcBuffer, destBuffer );
        
        NIOBufferImage scaledImage = new NIOBufferImage( 
            destWidth, 
            destHeight, 
            srcImage.getType( ), 
            srcImage.isGrayScale( ), 
            destBuffer );
        
        return( scaledImage );
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
    private void process ( 
        int destWidth, 
        int destHeight, 
        ByteBuffer srcBuffer, 
        ByteBuffer destBuffer ) {
        
        float nXFactor = ((float)srcWidth)/destWidth;
        float nYFactor = ((float)srcHeight)/destHeight;
        
        float fraction_x, fraction_y, one_minus_x, one_minus_y;
        int ceil_x, ceil_y, floor_x, floor_y;

        for ( int y = 0; y < destHeight; ++y ) {
            for ( int x = 0; x < destWidth; ++x ) {
                
                floor_x = (int)Math.floor(x * nXFactor);
                floor_y = (int)Math.floor(y * nYFactor);
                
                ceil_x = floor_x + 1;
                if (ceil_x >=srcWidth) {
                    ceil_x = floor_x;
                }
                ceil_y = floor_y + 1;
                if (ceil_y >= srcHeight) {
                    ceil_y = floor_y;
                }
                fraction_x = x * nXFactor - floor_x;
                fraction_y = y * nYFactor - floor_y;
                
                one_minus_x = 1.0f - fraction_x;
                one_minus_y = 1.0f - fraction_y;
                
                int floor_y_off = floor_y * srcWidth * srcComponents;
                int ceil_y_off = ceil_y * srcWidth * srcComponents;
                
                int floor_x_off = floor_x * srcComponents;
                int ceil_x_off = ceil_x * srcComponents;
                
                int c1_off = floor_y_off + floor_x_off;
                int c2_off = floor_y_off + ceil_x_off;
                int c3_off = ceil_y_off + floor_x_off;
                int c4_off = ceil_y_off + ceil_x_off;
                
                for ( int i = 0; i < srcComponents; i++ ) {
                    
                    float tmp1 = one_minus_x * ( 0xff & srcBuffer.get(c1_off++) ) + 
                        fraction_x * ( 0xff & srcBuffer.get(c2_off++) );
                    
                    float tmp2 = one_minus_x * ( 0xff & srcBuffer.get(c3_off++) ) + 
                        fraction_x * ( 0xff & srcBuffer.get(c4_off++) );
                    
                    byte cmp = (byte)( one_minus_y * tmp1 + fraction_y * tmp2 );
                    
                    destBuffer.put( cmp );
                }
            }
        }
    }
}
