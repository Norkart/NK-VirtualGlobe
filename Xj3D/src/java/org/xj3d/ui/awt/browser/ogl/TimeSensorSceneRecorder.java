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
 *****************************************************************************/

package org.xj3d.ui.awt.browser.ogl;

// External imports
import java.awt.Component;
import java.awt.Dimension;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import java.text.NumberFormat;

import javax.imageio.ImageIO;

import javax.media.opengl.GLDrawable;

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;

import org.web3d.vrml.renderer.common.nodes.time.BaseTimeSensor;

import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.browser.ScreenCaptureListener;

import org.xj3d.core.eventmodel.TimeSensorManager;

import org.xj3d.impl.core.eventmodel.VirtualTimeSensorManager;

import org.xj3d.ui.construct.event.RecorderEvent;
import org.xj3d.ui.construct.event.RecorderListener;

import org.xj3d.ui.construct.ogl.OGLConstruct;

/**
 * A function module that performs a sequenced image capture of an
 * embedded X3D world animation. The requirements of the X3D world are:
 * <ul>
 * <li>There is a DEF'ed TimeSensor with a known DEF name.</li>
 * <li>By default the TimeSensor's DEF name is "TIME". The TimeSensor's 
 * DEF name is programatically configurable in this module.</li>
 * </ul>
 * The image capture is managed as follows:
 * <ul>
 * <li>The image capture is initiated on the completion of the world and
 * content loading.</li>
 * <li>The image capture is terminated when the DEF'ed TimeSensor completes
 * it's first loop. All animation that happens prior to the TimeSensor
 * completing a cycle will be captured.</li>
 * <li>In the simplest case, there is an animated Viewpoint in the scene
 * that is bound on entry to the world and whose parameters are controlled
 * by Script or Interpolator nodes that are driven by the DEF'ed TimeSensor.
 * More complicated scenarios, perhaps involving multiple viewpoints -
 * driven by sources other than the DEF'ed TimeSensor are possible. But their
 * construction such that this module can determine the beginning and ending 
 * of the recording sequence from the DEF'ed TimeSensor are the responsibility 
 * of the world creator.</li>
 * </ul>
 * 
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class TimeSensorSceneRecorder implements ScreenCaptureListener, VRMLNodeListener {
	
	/** The logging identifer of this class */
	private static final String LOG_NAME = "TimeSensorSceneRecorder";
	
	/** The construct instance to record from */
	protected OGLConstruct construct;
	
	/** The browser core */
	protected OGLStandardBrowserCore core;
	
	/** The clock controller */
	protected VirtualTimeSensorManager clock;
	
	/** The error reporting mechanism */
	protected ErrorReporter errorReporter;
	
	/** The rendering surface */
	protected Object canvas;
	
	/** The sequence capture number, used as the file name */
	protected int number;
	
	/** Formatter for creating the image sequence identifier */
	protected NumberFormat fmt;
	
	/** The x3d time sensor node that controls the viewpoint animation */
	protected BaseTimeSensor timeSensor;
	
	/** The name of the x3d time sensor node that controls the viewpoint
	*  animation. Default value is "TIME" */
	protected String timeSensorName = "TIME";
	
	/** The field index of the time sensor's fraction_changed field.
	*  Used to determine when the time sensor has completed it's cycle */
	protected int fraction_changed_index;
	
	/** The output directory for the captured images */
	protected File outputDir;
	
	/** Listener for recorder status events */
	protected RecorderListener listener;
	
	/** Flag indicating that the timer sensor has completed a cycle */
	protected boolean recordingComplete;
	
	/** The width of the output images */
	protected int width;
	
	/** The height of the output images */
	protected int height;
	
	/** The image encoding type */
	protected String type = "png";
	
	/////////////////////////////////////////////////////////////////////////
	// statistic variables
	
	/** The total file write time */
	protected long fileTime;
	
	/** The total frame rendering time */
	protected long renderTime;
	
	/** The last start of frame rendering time */
	protected long startFrameTime;
	
	/////////////////////////////////////////////////////////////////////////
	
	/** 
	 * Constructor
	 * 
	 * @param construct The construct instance to record from
	 */
	public TimeSensorSceneRecorder( OGLConstruct construct ) {
		if ( construct == null ) {
			throw new IllegalArgumentException( 
				LOG_NAME +": construct instance must be non-null" );
		}
		TimeSensorManager tsm = construct.getTimeSensorManager( );
		if ( !( tsm instanceof VirtualTimeSensorManager ) ) {
			throw new IllegalArgumentException( 
				LOG_NAME +": TimeSensorManager "+
				"must be an instanceof VirtualTimeSensorManager" );
		}
		
		this.construct = construct;
		errorReporter = construct.getErrorReporter( );
		core = construct.getBrowserCore( );
		clock = (VirtualTimeSensorManager)tsm;
		canvas = construct.getGraphicsObject( );
		
		fmt = NumberFormat.getIntegerInstance( );
		fmt.setMinimumIntegerDigits( 5 );
		fmt.setGroupingUsed( false );
	}
	
	//----------------------------------------------------------
	// Method required for ScreenCaptureListener
	//----------------------------------------------------------
	
	/**
	 * Notification of a new screen capture.
	 * The buffer will be in openGL pixel order.
	 *
	 * @param buffer The screen capture
	 */
	public void screenCaptured( Buffer buffer ) {
		
		renderTime += System.nanoTime( ) - startFrameTime;
		
		String name = fmt.format( number );
		
		saveScreen( buffer, name, width, height );
		
		number++;
		
		if ( recordingComplete ) {
			stop( );
			timeSensor.removeNodeListener( this );
			if ( listener != null ) {
				listener.recorderStatusChanged( 
					new RecorderEvent( 
					this, 
					RecorderEvent.COMPLETE, 
					number ) );
			}
		} else {
			startFrameTime = System.nanoTime( );
			clock.tick( );
		}
	}
	
	//----------------------------------------------------------
	// Method defined by VRMLNodeListener
	//----------------------------------------------------------
	
	/**
	 * Listener for the end of the animation sequence
	 */
	public void fieldChanged( int index ) {
		if ( index == fraction_changed_index ) {
			float fraction = timeSensor.getFraction( );
			// using the fraction_changed field for comparison so
			// that we stop after a single loop (if loop is enabled).
			// for some reason - the first value back is 1.0f - so,
			// we also check how many frames have been recorded
			if ( ( fraction == 1.0f ) && ( number != 0 ) ) {
				recordingComplete = true;
			}
		}
	}
	
	//----------------------------------------------------------
	// Local Methods 
	//----------------------------------------------------------
	
	/**
	 * Return the average frame rendering time in milliseconds
	 *
	 * @return the average frame rendering time in milliseconds
	 */
	public double getAverageRenderTime( ) {
		return( (renderTime/number)/1000000.0 );
	}

	/**
	 * Return the average file write time in milliseconds
	 *
	 * @return the average file write time in milliseconds
	 */
	public double getAverageFileTime( ) {
		return( (fileTime/number)/1000000.0 );
	}

	/**
	 * Set the image encoding type
	 *
	 * @param type The image encoding type
	 */
	public void setEncoding( String type ) {
		boolean found = false;
		String[] format = ImageIO.getWriterFormatNames( );
		for ( int i = 0; i < format.length; i++ ) {
			if ( type.equals( format[i] ) ) {
				this.type = type;
				found = true;
				break;
			}
		}
		if ( !found ) {
			errorReporter.errorReport( 
				LOG_NAME +": Unknown image encoding type: "+ type +
				", defaulting to: "+ this.type, null );
		}
	}
	
	/**
	 * Set the image size
	 *
	 * @param width The image width
	 * @param height The image height
	 */
	public void setSize( int width, int height ) {
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Set the recording rate 
	 *
	 * @param framesPerSecond The frames per virtual second to record
	 */
	public void setFrameRate( int framesPerSecond ) {
		int framePeriod = (int)(1000.0 / framesPerSecond);
		clock.setTickIncrement( framePeriod );
	}
	
	/**
	 * Set the output directory for images
	 *
	 * @param dir The output directory for images
	 */
	public void setOutputDirectory( File dir ) {
		outputDir = dir;
	}
	
	/**
	 * Set the DEF'ed name of the time sensor
	 *
	 * @param name The DEF'ed name of the time sensor
	 */
	public void setTimeSensorName( String name ) {
		timeSensorName = name;
	}
	
	/**
	 * Initiate the capture sequence
	 */
	public void start( RecorderListener listener ) {
		fileTime = 0;
		renderTime = 0;
		if ( outputDir == null ) {
			errorReporter.warningReport( 
				LOG_NAME +": Unable to record, output directory "+
				"is not initialized.", null );
			return;
		}
		
		this.listener = listener;
		
		int tmp_width = 0;
		int tmp_height = 0;
		if ( canvas instanceof Component ) {
			Dimension size = ((Component)canvas).getSize( );
			tmp_width = (int)size.getWidth( );
			tmp_height = (int)size.getHeight( );
		} else if ( canvas instanceof GLDrawable ) {
			tmp_width = ((GLDrawable)canvas).getWidth( );
			tmp_height = ((GLDrawable)canvas).getHeight( );
		}
		
		// configure the size from the rendering surface,
		// only if the surface returns something non-zero
		if ( ( tmp_width > 0 ) && ( tmp_height > 0 ) ) {
			width = tmp_width;
			height = tmp_height;
		}
		
		number = 0;
		recordingComplete = false;
		
		VRMLScene scene = core.getScene( );
		VRMLNodeType node = (VRMLNodeType)(scene.getDEFNodes( ).get( timeSensorName ));
		if ( node != null ) {
			timeSensor = (BaseTimeSensor)node;
			fraction_changed_index = timeSensor.getFieldIndex( "fraction_changed" );
			timeSensor.addNodeListener( this );

			startFrameTime = System.nanoTime( );
			
			core.captureScreenStart( this, width, height );
			
			if ( listener != null ) {
				listener.recorderStatusChanged( 
					new RecorderEvent( 
					this, 
					RecorderEvent.ACTIVE, 
					number ) );
			}
		} else {
			errorReporter.warningReport( 
				LOG_NAME +": Unable to record, No TimeSensor named "+ 
				timeSensorName +" found.", null );
		}
	}
	
	/**
	 * Terminate the capture sequence
	 */
	public void stop( ) {
		core.captureScreenEnd( );
	}
	
	/**
	 * Process the screen capture buffer into a BufferedImage and save it to a file
	 *
	 * @param buffer The screen capture buffer
	 * @param name The file name prefix
	 * @param width The width of the image
	 * @param height The height of the image
	 */
	public void saveScreen( Buffer buffer, String name, int width, int height ) {
		
		String filename = name;
		
		ByteBuffer pixelsRGB = (ByteBuffer)buffer;
		int[] pixelInts = new int[width * height];
		
		// Convert RGB bytes to ARGB ints with no transparency.
		// Flip image vertically by reading the rows of pixels 
		// in the byte buffer in reverse - 
		// (0,0) is at bottom left in OpenGL.
		
		int p = width * height * 3;	// Points to first byte (red) in each row.
		int q;						// Index into ByteBuffer
		int i = 0;					// Index into target int[]
		int w3 = width*3;			// Number of bytes in each row
		
		for ( int row = 0; row < height; row++ ) {
			p -= w3;
			q = p;
			
			for ( int col = 0; col < width; col++ ) {
				int iR = pixelsRGB.get( q++ );
				int iG = pixelsRGB.get( q++ );
				int iB = pixelsRGB.get( q++ );
				
				pixelInts[i++] = 
					0xFF000000 | 
					((iR & 0x000000FF) << 16) |
					((iG & 0x000000FF) << 8) |
					(iB & 0x000000FF);
			}
		}
		
		BufferedImage bufferedImage =
			new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
		
		bufferedImage.setRGB( 0, 0, width, height, pixelInts, 0, width );
		
		File outputFile = new File( outputDir, filename +"."+ type );
		try {
			errorReporter.messageReport( 
				LOG_NAME +": Writing image file: "+ outputFile );
			
			long startTime = System.nanoTime( );
			ImageIO.write( bufferedImage, type, outputFile );
			fileTime += System.nanoTime( ) - startTime;
			
		} catch ( IOException e ) {
			errorReporter.errorReport( 
				LOG_NAME +": Error writing image file: "+ outputFile, e );
		}
	}
}
