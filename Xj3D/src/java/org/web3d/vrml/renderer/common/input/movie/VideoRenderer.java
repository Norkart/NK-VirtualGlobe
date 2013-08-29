/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.input.movie;

// External imports
import java.awt.Dimension;

import java.nio.ByteBuffer;

import javax.media.Format;
import javax.media.Buffer;

import javax.media.format.VideoFormat;
import javax.media.format.RGBFormat;

// Local imports
import org.web3d.image.NIOBufferImage;
import org.web3d.image.NIOBufferImageType;

// DEBUGGING ONLY - REMOVE ME
//import org.web3d.util.Debug;

/**
 * Implementation of the javax.media.renderer.VideoRenderer interface
 * for use within the MovieTexture node.  Captures the video frames and
 * formats them as BufferedImages and delivers them to the VideoStreamHandler.
 *
 * @author Guy Carpenter
 * @version $Revision: 1.9 $
 */
public class VideoRenderer extends Renderer implements javax.media.renderer.VideoRenderer
{
	/** List of formats we are prepared to accept.  Initialized in static initializer. */
	private VideoFormat supportedFormats[];
	
	/** Image used to receive each frame */
	private NIOBufferImage image;
	
	/** Width in pixels of current image format */
	private int imageWidth;
	
	/** Height in pixels of current image format */
	private int imageHeight;
	
	/** Current format line stride in bytes */
	private int imageLineStride;
	
	/** Object to receive frames once they are decoded */
	private VideoStreamHandler streamHandler;
	
	/* used to filter out duplicate stop events from the engine */
	private boolean started;
	
	// REVISIT - make this an interface
	private boolean callbacksEnabled = true;
	
	/**
	 * Create a new VideoRenderer.
	 * @param videoStreamHandler - will be called with each completed frame.
	 */
	public VideoRenderer(VideoStreamHandler videoStreamHandler)
	{
		streamHandler = videoStreamHandler;
		started = false;
		
		// Prepare supported input formats and preferred format
		int rMask = 0x00FF0000;
		int gMask = 0x0000FF00;
		int bMask = 0x000000FF;
		
		VideoFormat supportedRGB;
		
		supportedRGB = new RGBFormat
			(null,                   // size
			Format.NOT_SPECIFIED,   // maxDataLength
			int[].class,            // buffer type
			Format.NOT_SPECIFIED,   // frame rate
			32,                     // bitsPerPixel
			rMask, gMask, bMask,    // component masks
			1,                      // pixel stride
			Format.NOT_SPECIFIED,   // line stride
			Format.FALSE,           // flipped
			Format.NOT_SPECIFIED    // endian
			);
		
		supportedFormats = new VideoFormat[1];
		supportedFormats[0] = supportedRGB;
		
	}
	
	/**
	 * Enable or disable callbacks to the videoStreamHandler.
	 * We allow callbacks to be disabled because certain transport
	 * operations (rewind) have the sideeffect of calling the
	 * start/stop methods in this class, and we do not want them
	 * propogated to the videoStreamhandler.
	 *
	 * @param enabled - Callbacks will be enabled if true, disabled if false.
	 */
	public void enableCallbacks(boolean enabled)
	{
		callbacksEnabled = enabled;
	}
	
	//----------------------------------------------------------------------
	// PlugIn interface
	//----------------------------------------------------------------------
	
	/**
	 * Gets the name of this plug-in as a human-readable string.
	 *
	 * @return - A String that contains the descriptive name of the plug-in.
	 */
	public String getName()
	{
		//Debug.trace("");
		return "Xj3D Video Renderer";
	}
	
	//----------------------------------------------------------------------
	// VideoRenderer interface
	//----------------------------------------------------------------------
	
	/**
	 * Returns the awt component.  Always returns null.
	 */
	public java.awt.Component getComponent()
	{
		//Debug.trace("");
		return null;  // no awt component
	}
	
	/**
	 * Sets the awt component.  Always returns false indicating
	 * that we cannot draw to that component.
	 */
	public boolean setComponent(java.awt.Component comp)
	{
		//Debug.trace("");
		return false;  // cannot draw to that component
	}
	
	/**
	 * Sets the region in the component where the video is to be
	 * rendered to. Video is to be scaled if necessary. If rect is
	 * null, then the video occupies the entire component.
	 * This call is ignored in this implementation.
	 * @param rect - the rect that defines the region to be rendered to.
	 */
	public void setBounds(java.awt.Rectangle rect)
	{
		//Debug.trace("");
	}
	
	/**
	 * Returns the region in the component where the video will be
	 * rendered to. Returns null if the entire component is being
	 * used.  (Always returns null.)
	 */
	public java.awt.Rectangle getBounds()
	{
		//Debug.trace("");
		return null;
	}
	
	//----------------------------------------------------------------------
	// Renderer interface
	//----------------------------------------------------------------------
	
	/**
	 * Sets the Format of the input data.
	 *
	 * @return The Format that was set. This is typically the
	 * supported Format that most closely matches the specified
	 * Format. If possible, the format fields that were not specified
	 * are set to the preferred values in the returned Format. Returns
	 * null if the specified Format is not supported.
	 */
	public Format setInputFormat(Format format)
	{
		//Debug.trace(format.toString());
		RGBFormat inFormat = (RGBFormat)format;
		RGBFormat outFormat;
		Dimension dim = inFormat.getSize();
		int width = (int)dim.getWidth();
		int height = (int)dim.getHeight();
		imageLineStride =  inFormat.getLineStride();
		
		// It seems we always get called twice - once to set the format,
		// and again to confirm?  We are really only interested
		// in the image size and line stride.
		
		if (image==null || width != imageWidth || height != imageHeight) {
			imageWidth = width;
			imageHeight = height;
			image = new NIOBufferImage(imageWidth,
				imageHeight,
				NIOBufferImageType.RGB);
			
			//Debug.trace("Created image"+image);
			streamHandler.videoStreamFormat(imageWidth, imageHeight);
		}
		
		
		
		// Create an RGB format with virtually identical values
		
		outFormat = new RGBFormat(
			dim,
			inFormat.getMaxDataLength(),
			inFormat.getDataType(),
			inFormat.getFrameRate(),
			inFormat.getBitsPerPixel(),
			inFormat.getRedMask(),
			inFormat.getGreenMask(),
			inFormat.getBlueMask(),
			inFormat.getPixelStride(),
			inFormat.getLineStride(),
			Format.FALSE, // not flipped
			inFormat.getEndian()
			);
		return outFormat;
	}
	
	/**
	 * Lists the input formats supported by this Renderer.
	 *
	 * @return An array of Format objects that represent the input
	 * formats supported by this Renderer.
	 */
	public Format[] getSupportedInputFormats()
	{
		//Debug.trace("");
		return supportedFormats;
	}
	
	/**
	 * Processes the data and renders it to the output device
	 * represented by this Renderer.
	 *
	 * @return BUFFER_PROCESSED_OK if the processing is
	 * successful. Other possible return codes are defined in PlugIn.
	 */
	public int process(Buffer buffer)
	{
		// maybe we are interested in the flags
		if (false) {
			int flags = buffer.getFlags();
			if ((flags & Buffer.FLAG_EOM)>0) //Debug.trace("FLAG_EOM");
			if ((flags & Buffer.FLAG_DISCARD)>0) //Debug.trace("FLAG_DISCARD");
			if ((flags & Buffer.FLAG_SILENCE)>0) //Debug.trace("FLAG_SILENCE");
			if ((flags & Buffer.FLAG_SID)>0) //Debug.trace("FLAG_SID");
			if ((flags & Buffer.FLAG_KEY_FRAME)>0) //Debug.trace("FLAG_KEY_FRAME");
			if ((flags & Buffer.FLAG_NO_DROP)>0) //Debug.trace("FLAG_NO_DROP");
			if ((flags & Buffer.FLAG_NO_WAIT)>0) //Debug.trace("FLAG_NO_WAIT");
			if ((flags & Buffer.FLAG_NO_SYNC)>0) //Debug.trace("FLAG_NO_SYNC");
			if ((flags & Buffer.FLAG_RELATIVE_TIME)>0) //Debug.trace("FLAG_RELATIVE_TIME");
			if ((flags & Buffer.FLAG_FLUSH)>0) //Debug.trace("FLAG_FLUSH");
			if ((flags & Buffer.FLAG_SYSTEM_MARKER)>0) //Debug.trace("FLAG_SYSTEM_MARKER");
			if ((flags & Buffer.FLAG_RTP_MARKER) > 0) {} //Debug.trace("FLAG_RTP_MARKER");
		}
		
		int[] data = (int[])buffer.getData();
		
		// The data buffer may be null.  This is typically true when
		// flag==FLAG_EOM.  Always check.
		
		if (data!=null) {
			
			ByteBuffer img_buffer = image.getBuffer( );
			
			int y_inv = imageHeight - 1;
			
			for( int y = 0; y < imageHeight; y++ ) {
				
				int data_offset = y_inv * imageWidth;
				
				for( int x = 0; x < imageWidth; x++ ) {
					
					int tmp = data[data_offset + x];
					img_buffer.put((byte)((tmp >> 16) & 0xFF));
					img_buffer.put((byte)((tmp >> 8) & 0xFF));
					img_buffer.put((byte)(tmp & 0xFF));
				}
				
				y_inv--;
			}
			streamHandler.videoStreamFrame(image);
		}
		
		return BUFFER_PROCESSED_OK;
	}
	
	/**
	 * Called when the video stream is stopping.
	 */
	public void stop()
	{
		//Debug.trace();
		if (callbacksEnabled && started) {
			started = false;
			streamHandler.videoStreamStop();
		}
	}
	
	/**
	 * Called when the video stream is about to begin.
	 */
	public void start()
	{
		//Debug.trace();
		
		if (callbacksEnabled && !started) {
			started = true;
			streamHandler.videoStreamStart();
		}
	}
}
