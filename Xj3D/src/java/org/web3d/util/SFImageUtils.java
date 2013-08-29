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

package org.web3d.util;

// External Imports
import java.awt.color.ColorSpace;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;

import javax.imageio.ImageTypeSpecifier;

// Local Imports
// none

/**
 * Utility class for dealing with converting image data to and from the
 * SFImage integer format.
 *
 * @author Bradley Vender, Rex Melton
 * @version $Revision: 1.5 $
 */
public class SFImageUtils {
	
	/** 
	 * Utility method for MFImageWrapper and SFImageWrapper to convert
	 * a RenderedImage to the SFImage integer format 
	 *
	 * @param img The rendered image to convert
	 * @param targetArray The integer array to place the data in
	 * @param startOffset Starting offset in array
	 */
	public static void convertRenderedImageToData(RenderedImage img, int targetArray[], int startOffset) {
		
		int height = img.getHeight( );
		int width = img.getWidth( );
		int components = img.getColorModel( ).getNumComponents();
		
		Raster raster = img.getData( );
		DataBuffer imgData = raster.getDataBuffer( );
		ColorModel colorModel = img.getColorModel( );
		
		targetArray[  startOffset] = width;
		targetArray[1+startOffset] = height;
		targetArray[2+startOffset] = components;
		
		int pixel_index = startOffset + 3;
		
		switch ( components ) {
		case 1:
			
			for( int y = height - 1; y >= 0; y-- ) {
				int image_index = y * width;
				for( int x = 0; x < width; x++ ) {
					targetArray[pixel_index++] = imgData.getElem( image_index++ );
				}
			}
			break;
			
		case 2:

			Object pixel_data = null;
			int[] cmp = null;
			
			for( int y = height - 1; y >= 0; y-- ) {
				for( int x = 0; x < width; x++ ) {
					pixel_data = raster.getDataElements( x, y, pixel_data );
					cmp = colorModel.getComponents( pixel_data, cmp, 0 );
					targetArray[pixel_index++] = cmp[0] << 8 | cmp[1];
				}
			}
			break;
			
		case 3:
			
			pixel_data = null;
			
			for( int y = height - 1; y >= 0; y-- ) {
				for( int x = 0; x < width; x++ ) {
					pixel_data = raster.getDataElements( x, y, pixel_data );
					targetArray[pixel_index++] = colorModel.getRGB( pixel_data );
				}
			}
			break;
			
		case 4:
			
			pixel_data = null;
			
			for( int y = height - 1; y >= 0; y-- ) {
				for( int x = 0; x < width; x++ ) {
					pixel_data = raster.getDataElements( x, y, pixel_data );
					targetArray[pixel_index++] = 
						colorModel.getRGB( pixel_data ) << 8 | 
						colorModel.getAlpha( pixel_data );
				}
			}
			break;
			
		default:
			throw new IllegalArgumentException("No idea how to deal with "+components+" components in an image.");
		}
	}
	
	/**
	 * Perform a bitBlt from a source image to an SFImage field.
	 * Does not do wrapping.
	 *
	 * @param img The image the sample is taken from
	 * @param targetArray The array containing the target field
	 * @param startOffset The index where the target field starts
	 * @param srcWidth The width of the sub image to copy
	 * @param srcHeight The height of the sub image to copy
	 * @param srcXOffset The X offset of the sub image to copy
	 * @param srcYOffset The Y offset of the sub image to copy
	 * @param destXOffset The destination X offset
	 * @param destYOffset The destination Y offset
	 */
	public static void convertSubRenderedImageToData(
		RenderedImage img, 
		int targetArray[], 
		int startOffset,
		int srcWidth,
		int srcHeight,
		int srcXOffset,
		int srcYOffset,
		int destXOffset,
		int destYOffset) {
		
		int destinationWidth = targetArray[startOffset];
		int destinationHeight = targetArray[startOffset+1];
		int components = targetArray[startOffset+2];
		
		if (( srcWidth + destXOffset > destinationWidth ) || ( srcHeight + destYOffset > destinationHeight )) {
			throw new IllegalArgumentException("Exceeds bounds of destination");
		}
		
		int imgWidth = img.getWidth();
		int imgHeight = img.getHeight();
		
		if (( srcWidth + srcXOffset > imgWidth ) || ( srcHeight + srcYOffset > imgHeight )) {
			throw new IllegalArgumentException("Exceeds bounds of source");
		}
		
		Raster raster = img.getData( );
		DataBuffer imgData = raster.getDataBuffer( );
		ColorModel colorModel=img.getColorModel();
		
		int pixel_offset = startOffset + 3;
		
		switch ( components ) {
		case 1:
			
			int img_y_inv = srcYOffset + srcHeight - 1;
			
			for( int y = destYOffset; y < destYOffset + srcHeight; y++ ) {
				int image_index = img_y_inv * imgWidth + srcXOffset;
				int pixel_index = y * destinationWidth + destXOffset + pixel_offset;
				for( int x = 0; x < srcWidth; x++ ) {
					targetArray[pixel_index++] = imgData.getElem( image_index++ );
				}
				img_y_inv--;
			}
			break;
			
		case 2:
			
			Object pixel_data = null;
			img_y_inv = srcYOffset + srcHeight - 1;
			int[] cmp = null;
			
			for( int y = destYOffset; y < destYOffset + srcHeight; y++ ) {
				int pixel_index = y * destinationWidth + destXOffset + pixel_offset;
				for( int x = srcXOffset; x < srcXOffset + srcWidth; x++ ) {
					pixel_data = raster.getDataElements( x, img_y_inv, pixel_data );
					cmp = colorModel.getComponents( pixel_data, cmp, 0 );
					targetArray[pixel_index++] = cmp[0] << 8 | cmp[1];
				}
				img_y_inv--;
			}
			break;
			
		case 3:
			
			pixel_data = null;
			img_y_inv = srcYOffset + srcHeight - 1;
			
			for( int y = destYOffset; y < destYOffset + srcHeight; y++ ) {
				int pixel_index = y * destinationWidth + destXOffset + pixel_offset;
				for( int x = 0; x < srcWidth; x++ ) {
					pixel_data = raster.getDataElements( x, img_y_inv, pixel_data );
					targetArray[pixel_index++] = colorModel.getRGB( pixel_data );
				}
				img_y_inv--;
			}
			break;
			
		case 4:
			
			pixel_data = null;
			img_y_inv = srcYOffset + srcHeight - 1;
			
			for( int y = destYOffset; y < destYOffset + srcHeight; y++ ) {
				int pixel_index = y * destinationWidth + destXOffset + pixel_offset;
				for( int x = 0; x < srcWidth; x++ ) {
					pixel_data = raster.getDataElements( x, img_y_inv, pixel_data );
					targetArray[pixel_index++] = 
						colorModel.getRGB( pixel_data ) << 8 | 
						colorModel.getAlpha( pixel_data );
				}
				img_y_inv--;
			}
			break;
			
		default:
			throw new IllegalArgumentException("No idea how to deal with "+components+" components in an image.");
		}
	}
	
	/** 
	 * Utility method to convert SFImage data representation to a WritableRenderedImage
	 * 
	 * @param width The image width
	 * @param height The image height
	 * @param components The number of components per pixel
	 * @param pixels The array of pixel data
	 */
	public static WritableRenderedImage convertDataToRenderedImage(
		int width, 
		int height, 
		int components, 
		int[] pixels ) {
		
		// identify the image type by number of components and setup
		// the type specific parameters for creating the BufferedImage
		ImageTypeSpecifier its = null;
		//int imageType;
		int bitMask[];
		switch(components) {
		case 1 :
			//imageType = BufferedImage.TYPE_BYTE_GRAY;
			its = ImageTypeSpecifier.createGrayscale( 8, DataBuffer.TYPE_BYTE, false );
			bitMask = new int[1];
			bitMask[0] = 0xFF;
			break;
			
		case 2 :
			//imageType = BufferedImage.TYPE_BYTE_GRAY;
			its = ImageTypeSpecifier.createGrayscale( 8, DataBuffer.TYPE_BYTE, false, false );
			bitMask = new int[2];
			bitMask[0] = 0xFF00;
			bitMask[1] = 0x00FF;
			break;
			
		case 3 :
			//imageType = BufferedImage.TYPE_INT_RGB;
			its = ImageTypeSpecifier.createPacked( 
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				0x00FF0000,
				0x0000FF00,
				0x000000FF,
				0x00,
				DataBuffer.TYPE_INT, 
				false );
			
			bitMask = new int[3];
			bitMask[0] = 0xFF0000;
			bitMask[1] = 0x00FF00;
			bitMask[2] = 0x0000FF;
			break;
			
		case 4 :
			//imageType = BufferedImage.TYPE_INT_ARGB;
			its = ImageTypeSpecifier.createPacked( 
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				0x00FF0000,
				0x0000FF00,
				0x000000FF,
				0xFF000000,
				DataBuffer.TYPE_INT, 
				false );
			
			bitMask = new int[4];
			bitMask[0] = 0xFF000000;
			bitMask[1] = 0x00FF0000;
			bitMask[2] = 0x0000FF00;
			bitMask[3] = 0x000000FF;
			break;
		default :
			throw new RuntimeException(
				"SFImage Unsupported #components: " + components);
		}
		
		// rearrage the pixel data to conform to the image format.
		// note: SFImage pixel data is y-inverted from the BufferedImage format.
		// we 'know' pixels[] is a scratch array - therefore, rearrange in place...
		int lower_row = height - 1;
		int iterations = height/2;
		for ( int y = 0; y < iterations; y++ ) {
			int upper_row_offset = y*width;
			int lower_row_offset = lower_row*width;
			for ( int x = 0; x < width; x++ ) {
				// transpose the rows, a pair at an iteration
				int upper_offset = upper_row_offset + x;
				int lower_offset = lower_row_offset + x;
				int upper_pixel = pixels[upper_offset];
				pixels[upper_offset] = pixels[lower_offset];
				pixels[lower_offset] = upper_pixel;
			}
			lower_row--;
		}
		
		DataBufferInt buff = new DataBufferInt( pixels, pixels.length, 0 );
		Raster raster = Raster.createPackedRaster(
			buff, width, height, width, bitMask, null);
		
		BufferedImage result = its.createBufferedImage(width, height);
		result.setData(raster);
		
		return( result );
	}
}
