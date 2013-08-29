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

package org.xj3d.ui.construct.ogl;

// External imports
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;

import org.j3d.aviatrix3d.management.DisplayCollection;
import org.j3d.aviatrix3d.management.RenderManager;

import org.j3d.aviatrix3d.output.audio.OpenALAudioDevice;

import org.j3d.aviatrix3d.pipeline.audio.DefaultAudioPipeline;
import org.j3d.aviatrix3d.pipeline.audio.AudioCullStage;
import org.j3d.aviatrix3d.pipeline.audio.AudioOutputDevice;
import org.j3d.aviatrix3d.pipeline.audio.AudioRenderPipeline;
import org.j3d.aviatrix3d.pipeline.audio.AudioSortStage;

import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsRenderPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;

// Local imports
import org.web3d.browser.Xj3DConstants;

import org.web3d.util.ErrorReporter;

import org.web3d.vrml.renderer.ogl.OGLSceneBuilderFactory;

import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;

import org.xj3d.core.eventmodel.DeviceFactory;

import org.xj3d.ui.construct.Construct;

/**
 * An abstract sub-class of Construct that provides OGL render capabilities
 * to the base Construct.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public abstract class OGLConstruct extends Construct {
	
	///////////////////////////////////////////////////////////////////
	// Renderer classes
	
	/** The render manager class */
	protected String RENDER_MANAGER = 
		"org.j3d.aviatrix3d.management.SingleThreadRenderManager";
	
	/** The display manager class */
	protected String DISPLAY_MANAGER = 
		"org.j3d.aviatrix3d.management.SingleDisplayCollection";
	
	/** The audio pipeline class */
	protected String AUDIO_PIPELINE =
		"org.j3d.aviatrix3d.pipeline.audio.DefaultAudioPipeline";
	
	/** The audio cull stage class */
	protected String AUDIO_SORT_STAGE =
		"org.j3d.aviatrix3d.pipeline.audio.NullAudioSortStage";
	
	/** The audio sort stage class */
	protected String AUDIO_CULL_STAGE =
		"org.j3d.aviatrix3d.pipeline.audio.NullAudioCullStage";
	
	/** The graphics pipeline class */
	protected String GRAPHICS_PIPELINE =
		"org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline";
	
	/** The graphics sort stage class */
	protected String GRAPHICS_SORT_STAGE =
		"org.j3d.aviatrix3d.pipeline.graphics.StateAndTransparencyDepthSortStage";
	
	/** The graphics cull stage class */
	protected String GRAPHICS_CULL_STAGE =
		"org.j3d.aviatrix3d.pipeline.graphics.FrustumCullStage";
	
	///////////////////////////////////////////////////////////////////
	// Renderer specific manager classes
	
	/** The layer manager factory class */
	protected String OGL_LAYER_MANAGER_FACTORY =
		"org.web3d.vrml.renderer.ogl.browser.OGLLayerManagerFactory";
	
	///////////////////////////////////////////////////////////////////
	// Toolkit specific classes, must be set by sub-class
	
	/** Toolkit specific device factory class */
	protected String UI_DEVICE_FACTORY;
	
	///////////////////////////////////////////////////////////////////
	// rendering preferences
	
	/** The graphics capabilites for the graphics device */
	protected GLCapabilities glCapabilities;
	
	/** The graphics capabilites chooser */
	protected GLCapabilitiesChooser glCapabilitiesChooser;
	
	///////////////////////////////////////////////////////////////////
	// rendering (aviatrix) device objects
	
	/** The graphics rendering surface */
	protected GraphicsOutputDevice graphicsDevice;
	
	/** The audio device */
	protected AudioOutputDevice audioDevice;
	
	///////////////////////////////////////////////////////////////////
	// rendering (aviatrix) control objects
	
	/** The graphics rendering pipeline */
	protected DefaultGraphicsPipeline graphicsPipeline;
	
	/** The audio rendering pipeline */
	protected DefaultAudioPipeline audioPipeline;
	
	/** Manager for the layers */
	protected DisplayCollection displayManager;
	
	/** The scene rendering manager */
	protected RenderManager renderManager;
	
	///////////////////////////////////////////////////////////////////
	// renderer specific manager (xj3d) objects
	
	/** The browser core */
	protected OGLStandardBrowserCore core;
	
	///////////////////////////////////////////////////////////////////
	
	/** 
	 * Restricted Constructor 
	 */
	protected OGLConstruct( ) {
		this( null );
	}
	
	/** 
	 * Restricted Constructor 
	 * 
	 * @param reporter The error reporter
	 */
	protected OGLConstruct( ErrorReporter reporter ) {
		super( reporter );
		renderer = Xj3DConstants.OPENGL_RENDERER;
		renderer_id = Xj3DConstants.OPENGL_ID;
		LAYER_MANAGER_FACTORY = OGL_LAYER_MANAGER_FACTORY;
	}
	
	//----------------------------------------------------------
	// Methods defined by Construct
	//----------------------------------------------------------
	
	/** 
	 * Return the rendering surface 
	 * 
	 * @return The rendering surface
	 */
	public Object getGraphicsObject( ) {
		
		return( graphicsDevice.getSurfaceObject( ) );
	}
	
	/**
	 * Create the audio rendering device
	 */
	protected void buildAudioRenderingDevice( ) {
		
		audioDevice = new OpenALAudioDevice( );
	}
	
	/**
	 * Create the browser core
	 */
	protected void buildBrowserCore( ) {
		
		core = new OGLStandardBrowserCore(
			eventModel,
			renderManager,
			displayManager );
		super.core = core;
		
		GraphicsResizeListener[] listeners = 
			core.getGraphicsResizeListeners( );
		
		for( int i=0; i < listeners.length; i++ ) {
			graphicsDevice.addGraphicsResizeListener( listeners[i] );
		}
	}
	
	/**
	 * Create the scene builder factory
	 */
	protected void buildSceneBuilderFactory( ) {
		
		sceneBuilderFactory = new OGLSceneBuilderFactory(
			( supportVRML & !supportX3D ),
			true,
			true,
			true,
			true,
			true,
			true );
	}
	
	/**
	 * Return the browser core. Overrides Construct method to
	 * return the OGL specific browser core.
	 *
	 * @return The browser core.
	 */
	public OGLStandardBrowserCore getBrowserCore( ) {
		return( core );
	}
	
	//----------------------------------------------------------
	// Methods defined by ConstructBuilder
	//----------------------------------------------------------
	
	/**
	 * Create the rendering capabilities
	 */
	public void buildRenderingCapabilities( ) {
		
		if ( glCapabilities == null ) {
			// if not explicitly set, configure graphics preferences
			glCapabilities = new GLCapabilities( );
			glCapabilities.setDoubleBuffered( doubleBuffered );
			glCapabilities.setHardwareAccelerated( hardwareAccelerated );
			
			if ( antialiasSamples > 1 ) {
				glCapabilities.setSampleBuffers( true );
				glCapabilities.setNumSamples( antialiasSamples );
			} else {
				glCapabilities.setSampleBuffers( false );
			}
		}       
		super.buildRenderingCapabilities( );
	}
	
	/**
	 * Create the access to the user interface devices
	 */
	public void buildInterfaceDevices( ) {
		
		deviceFactory = (DeviceFactory)loader.loadClass(
			UI_DEVICE_FACTORY,
			new Object[] { 
				graphicsDevice.getSurfaceObject( ), 
				renderer_id,
				graphicsDevice,
				errorReporter },
			new Class[] {
				Object.class,
				String.class,
				Object.class,
				ErrorReporter.class },
			false );
	}
	
	/**
	 * Create the rendering pipelines and render manager
	 *
	 * @exception InvalidConfigurationException If a required class
	 * class cannot be loaded.
	 */
	public void buildRenderer( ) {
		
		displayManager = 
			(DisplayCollection)loader.loadClass( DISPLAY_MANAGER, true );
		
		///////////////////////////////////////////////////////////////////
		// graphics
		GraphicsSortStage gsorter = 
			(GraphicsSortStage)loader.loadClass( GRAPHICS_SORT_STAGE, true );
		GraphicsCullStage gculler = 
			(GraphicsCullStage)loader.loadClass( GRAPHICS_CULL_STAGE, true );
		gculler.setOffscreenCheckEnabled( true );
		
		// note: using default instance, not GraphicsRendererPipeline interface
		graphicsPipeline = 
			(DefaultGraphicsPipeline)loader.loadClass( GRAPHICS_PIPELINE, true );
		
		graphicsPipeline.setCuller( gculler );
		graphicsPipeline.setSorter( gsorter );
		
		graphicsPipeline.setGraphicsOutputDevice( graphicsDevice );

		displayManager.addPipeline( graphicsPipeline );
		
		///////////////////////////////////////////////////////////////////
		// audio
		if ( audioDevice != null ) {
			AudioSortStage asorter = 
				(AudioSortStage)loader.loadClass( AUDIO_SORT_STAGE, true );
			AudioCullStage aculler = 
				(AudioCullStage)loader.loadClass( AUDIO_CULL_STAGE, true );
			
			// note: using default instance, not AudioRendererPipeline interface
			audioPipeline = 
				(DefaultAudioPipeline)loader.loadClass( AUDIO_PIPELINE, true );
			
			audioPipeline.setCuller( aculler );
			audioPipeline.setSorter( asorter );
			
			audioPipeline.setAudioOutputDevice( audioDevice );
			// rem ////////////////////////////////////////////////////////
			// removing to prevent the occasional lockups associated with joal
			displayManager.addPipeline( audioPipeline );
			///////////////////////////////////////////////////////////////
		}
		
		///////////////////////////////////////////////////////////////////
		
		renderManager = (RenderManager)loader.loadClass( RENDER_MANAGER, true );
		renderManager.addDisplay( displayManager );
	}
	
	//----------------------------------------------------------
	// Local Methods
	//----------------------------------------------------------
	
	///////////////////////////////////////////////////////////////////
	// convenience accessor methods for commonly needed class references
	
	/** 
	 * Return the graphics rendering pipeline.
	 * 
	 * @return The graphics rendering pipeline.
	 */
	public GraphicsRenderPipeline getGraphicsRenderPipeline( ) {
		return( graphicsPipeline );
	}
	
	/** 
	 * Return the audio rendering pipeline. 
	 * 
	 * @return The audio rendering pipeline. 
	 */
	public AudioRenderPipeline getAudioRenderPipeline( ) {
		return( audioPipeline );
	}
	
	/** 
	 * Return the display manager.
	 * 
	 * @return The display manager.
	 */
	public DisplayCollection getDisplayCollection( ) {
		return( displayManager );
	}
	
	/** 
	 * Return the render manager.
	 * 
	 * @return The render manager.
	 */
	public RenderManager getRenderManager( ) {
		return( renderManager );
	}
	///////////////////////////////////////////////////////////////////
}
