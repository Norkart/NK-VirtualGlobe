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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import javax.media.opengl.GLDrawable;

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;

import org.web3d.vrml.renderer.common.nodes.navigation.BaseViewpoint;

import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.browser.ScreenCaptureListener;

import org.xj3d.core.eventmodel.TimeSensorManager;

import org.xj3d.ui.construct.event.RecorderEvent;
import org.xj3d.ui.construct.event.RecorderListener;

import org.xj3d.ui.construct.ogl.AutoConfigureViewpoint;
import org.xj3d.ui.construct.ogl.OGLConstruct;

/**
 * A function module that performs an image capture of an
 * X3D scene for the purpose of creating a thumbnail image.
 * The requirements of the X3D world are:
 * <ul>
 * <li>Optionally there will be a DEF'ed Viewpoint with a known DEF name.</li>
 * <li>By default the Viewpoint's DEF name is "ICON_VIEWPOINT". The Viewpoint's 
 * DEF name is programatically configurable in this module.</li>
 * <li>In the absense of a DEF'ed Viewpoint, the default viewpoint will be 
 * captured.</li>
 * </ul>
 * The image capture is managed as follows:
 * <ul>
 * <li>On the completion of the world and content loading, the DEF'ed Viewpoint
 * node is searched for and bound if necessary.</li>
 * <li>The image capture is initiated immediately if the Def'ed Viewpoint is not found,
 * or the DEF'ed Viewpoint is found and currently bound.</li>
 * <li>Otherwise, the capture is initiated immediately upon notification that the
 * DEF'ed Viewpoint has been bound.</li>
 * </ul>
 * 
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public class ThumbnailRecorder implements ScreenCaptureListener, VRMLNodeListener {

    /** Default def name used to specify a viewpoint */
    private static final String DEFAULT_VIEWPOINT = "ICON_VIEWPOINT";
    
	/** The logging identifer of this class */
	private static final String LOG_NAME = "ThumbnailRecorder";
	
	/** The construct instance to record from */
	protected OGLConstruct construct;
	
	/** The browser core */
	protected OGLStandardBrowserCore core;
	
	/** The error reporting mechanism */
	protected ErrorReporter errorReporter;
	
	/** The rendering surface */
	protected Object canvas;
	
	/** The sequence capture number, somewhat unnecessary as in this
	*  application, it only ever reaches one (1) */
	protected int number;
	
	/** The name of the x3d viewpoint to capture. 
	*  Default value is "ICON_VIEWPOINT" */
	protected String viewpointName = DEFAULT_VIEWPOINT;
	
	/** The viewpoint node, used when we have to wait for the 
	*  named viewpoint to be bound */
	protected BaseViewpoint viewpoint;
	
	/** The index of the viewpoint's isBound field. */
	protected int isBound_index;
	
	/** The output file for the captured images */
	protected File outputFile;
	
	/** Listener for recorder status events */
	protected RecorderListener listener;
	
	/** The width of the output images */
	protected int width;
	
	/** The height of the output images */
	protected int height;
	
	/** The image encoding type */
	protected String type = "png";
	
	/** Flag indicating that the selected encoding type may have an alpha */
	protected boolean hasAlpha;
	
	/** Image encoding types that may have an alpha */
	protected String[] alphaTypes = { "png", "gif", };
	
	/////////////////////////////////////////////////////////////////////////
	// statistic variables
	
	/** The total file write time */
	protected long fileTime;
	
	/** The total frame rendering time */
	protected long renderTime;
	
	/** The last start of frame rendering time */
	protected long startFrameTime;
	
	/////////////////////////////////////////////////////////////////////////
	
	/** Flag indicating that the image capture should be post processed */
	protected boolean postProcess;
	
	/** RGB value for snap pixels that should be replaced */
	protected int snapRGB;
	
	/* ARGB value to replace the designated snap pixels */
	protected int imageARGB;
	
	/** 
	 * Constructor
	 * 
	 * @param construct The construct instance to record from
	 */
	public ThumbnailRecorder( OGLConstruct construct ) {
		if ( construct == null ) {
			throw new IllegalArgumentException( 
				LOG_NAME +": construct instance must be non-null" );
		}
		
		this.construct = construct;
		errorReporter = construct.getErrorReporter( );
		core = construct.getBrowserCore( );
		canvas = construct.getGraphicsObject( );
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
		
		saveScreen( buffer, width, height );
		
		number++;
		
		if ( listener != null ) {
			listener.recorderStatusChanged( 
				new RecorderEvent( 
				this, 
				RecorderEvent.COMPLETE, 
				number ) );
		}
	}
	
	//----------------------------------------------------------
	// Method defined by VRMLNodeListener
	//----------------------------------------------------------
	
	/**
	 * Listener for the viewpoint binding, if necessary
	 */
	public void fieldChanged( int index ) {
		if ( index == isBound_index ) {
			initiateCapture( );
			viewpoint.removeNodeListener( this );
		}
	}
	
	//----------------------------------------------------------
	// Local Methods 
	//----------------------------------------------------------
	
	/**
	 * Return the frame rendering time in milliseconds
	 *
	 * @return the frame rendering time in milliseconds
	 */
	public double getRenderTime( ) {
		return( (renderTime/number)/1000000.0 );
	}
	
	/**
	 * Return the file write time in milliseconds
	 *
	 * @return the file write time in milliseconds
	 */
	public double getFileTime( ) {
		return( (fileTime/number)/1000000.0 );
	}
	
	/**
	 * Set the image encoding type
	 *
	 * @param type The image encoding type
	 * @return true if the encoding type is valid, false otherwise.
	 */
	public boolean setEncoding( String type ) {
		boolean found = false;
		String[] format = ImageIO.getWriterFormatNames( );
		for ( int i = 0; i < format.length; i++ ) {
			if ( type.equals( format[i] ) ) {
				this.type = type;
				found = true;
				break;
			}
		}
		hasAlpha = false;
		if ( !found ) {
			errorReporter.errorReport( 
				LOG_NAME +": Unknown image encoding type: "+ type, null );
		} else {
			for ( int i = 0; i < alphaTypes.length; i++ ) {
				if ( type.equalsIgnoreCase( alphaTypes[i] ) ) {
					hasAlpha = true;
					break;
				}
			}
		}
		return( found );
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
	 * Set the background color for the image.
	 *
	 * @param x3dBackgroundColor The background color of the X3D model to
	 * replace with the image background color. If null, the image background
	 * will not be changed from the capture.
	 * @param imageBackgroundColor The color to set for the image background. 
	 * If null, the image background will not be changed from the capture.
	 */
	public void setBackgroundColor( 
		Color x3dBackgroundColor, 
		Color imageBackgroundColor ) {
		
		postProcess = ( x3dBackgroundColor != null ) & ( imageBackgroundColor != null );
		if ( postProcess ) {
			// mask off the alpha, the snap image is RGB only
			snapRGB = 0x00FFFFFF & x3dBackgroundColor.getRGB( );
			
			imageARGB = imageBackgroundColor.getRGB( );
		}
	}
	/**
	 * Set the output file for the image
	 *
	 * @param file The output file for the image
	 * @return true if the output file is of a valid encoding type, 
	 * false otherwise.
	 */
	public boolean setOutputFile( File file ) {
		outputFile = null;
		String filename = file.toString( );
		int index = filename.lastIndexOf( "." );
		if ( index == -1 ) {
			errorReporter.errorReport( 
				LOG_NAME +": Invalid output file: Unknown image "+
				"encoding type for file name: "+ filename, null );
		} else if ( setEncoding( filename.substring( index+1 ) ) ) {
			outputFile = file;
		}
		return( outputFile != null );
	}
	
	/**
	 * Set the DEF'ed name of the Viewpoint
	 *
	 * @param name The DEF'ed name of the Viewpoint
	 */
	public void setViewpointName( String name ) {
		viewpointName = name;
	}
	
	/**
	 * Initiate the capture
	 */
	public void start( RecorderListener listener ) {
		fileTime = 0;
		renderTime = 0;
		if ( outputFile == null ) {
			errorReporter.warningReport( 
				LOG_NAME +": Unable to record, output file "+
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
		
		// determine how to take the screen capture
		if ( viewpointName != null ) {
		    
		    if ( viewpointName.equalsIgnoreCase( "FIT" ) ) {
	            
		        // Always fit to world
		        
                // auto configure the viewpoint for the snap
                AutoConfigureViewpoint auto = new AutoConfigureViewpoint( construct );
                boolean success = auto.configure( );
                // regardless of whether the configure succeeded, do the capture
                initiateCapture( ); 
                
		    } else if ( viewpointName.equalsIgnoreCase( "AUTO" ) ) {
			    
		        // check to see if there is a user specified, use it
		        // otherwise fit to world
		        
                VRMLScene scene = core.getScene( );
                VRMLNodeType node = (VRMLNodeType)(scene.getDEFNodes( ).get( DEFAULT_VIEWPOINT ));
                if ( node != null ) {

                    // if the named node exists, ensure that it is bound
                    // prior to performing the capture operation.
                    viewpoint = (BaseViewpoint)node;
                    if ( viewpoint.getIsBound( ) ) {
                        initiateCapture( );
                    } else {
                        // wait for the named viewpoint to be bound.
                        isBound_index = viewpoint.getFieldIndex( "isBound" );
                        viewpoint.addNodeListener( this );
                        construct.getViewpointManager( ).setViewpoint( viewpoint );
                    }
                } else {
                    
                    // otherwise, auto configure the viewpoint for the snap
                    AutoConfigureViewpoint auto = new AutoConfigureViewpoint( construct );
                    boolean success = auto.configure( );
                    // regardless of whether the configure succeeded, do the capture
                    initiateCapture( );
                }
	                
			} else {
			    
			    // assume it is specified, if found use it
			    // otherwise default to basic xj3d viewpoint logic
			   
				VRMLScene scene = core.getScene( );
				VRMLNodeType node = (VRMLNodeType)(scene.getDEFNodes( ).get( viewpointName ));
				if ( node != null ) {
					// if the named node exists, ensure that it is bound
					// prior to performing the capture operation.
					viewpoint = (BaseViewpoint)node;
					if ( viewpoint.getIsBound( ) ) {
						initiateCapture( );
					} else {
						// wait for the named viewpoint to be bound.
						isBound_index = viewpoint.getFieldIndex( "isBound" );
						viewpoint.addNodeListener( this );
						construct.getViewpointManager( ).setViewpoint( viewpoint );
					}
				} else {
					// otherwise, just capture the default bound viewpoint
					initiateCapture( );
				}
			}
		} else {
			// otherwise, just capture the default bound viewpoint
			initiateCapture( );
		}
		
	}
	
	/**
	 * Process the screen capture buffer into a BufferedImage and save it to a file
	 *
	 * @param buffer The screen capture buffer
	 * @param width The width of the image
	 * @param height The height of the image
	 */
	public void saveScreen( Buffer buffer, int width, int height ) {
		
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
		
		if ( postProcess ) {
			for (int row = 0; row < height; row++) {
				p -= w3;
				q = p;
				
				for (int col = 0; col < width; col++) {
					int iR = pixelsRGB.get(q++);
					int iG = pixelsRGB.get(q++);
					int iB = pixelsRGB.get(q++);
					
					int color = ((iR & 0x000000FF) << 16)
						| ((iG & 0x000000FF) << 8)
						| (iB & 0x000000FF);
					
					if ( color == snapRGB ) {
						pixelInts[i++] = imageARGB;
					} else {
						pixelInts[i++] = 0xFF000000 | color;
					}
				}
			}
		} else {
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
		}
		
		BufferedImage bufferedImage = null;
		if ( hasAlpha ) {
			bufferedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		} else {
			bufferedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
		}
		
		bufferedImage.setRGB( 0, 0, width, height, pixelInts, 0, width );
		
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
	
	/**
	 * Initiate the capture
	 */
	private void initiateCapture( ) {
		
		startFrameTime = System.nanoTime( );
		
		core.captureScreenOnce( this, width, height );
		
		if ( listener != null ) {
			listener.recorderStatusChanged( 
				new RecorderEvent( 
				this, 
				RecorderEvent.ACTIVE, 
				number ) );
		}
	}
}
