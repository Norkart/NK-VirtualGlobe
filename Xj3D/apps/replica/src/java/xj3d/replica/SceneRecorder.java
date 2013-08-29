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

package xj3d.replica;

// External imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.imageio.ImageIO;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// Local imports
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.sav.InputSource;

import org.xj3d.ui.construct.BlockingWorldLoader;
import org.xj3d.ui.construct.LoggingErrorReporter;
import org.xj3d.ui.construct.SystemErrorReporter;

import org.xj3d.ui.construct.event.RecorderEvent;
import org.xj3d.ui.construct.event.RecorderListener;

import org.xj3d.ui.construct.ogl.ShutdownManager;

import org.xj3d.ui.awt.browser.ogl.AWTOGLConstruct;
import org.xj3d.ui.awt.browser.ogl.OffscreenOGLConstruct;
import org.xj3d.ui.awt.browser.ogl.TimeSensorSceneRecorder;

/**
 * A simple application intended primarily for command line operation
 * for executing a sequenced image capture of an X3D world animation
 * that is embedded in the world.
 *
 * @author Rex Melton
 * @version $Revision: 1.7 $
 */
public class SceneRecorder implements ActionListener, RecorderListener { 
	
	/** The logging identifer of this app */
	private static final String LOG_NAME = "SceneRecorder";
	
	/** The default image encoding */
	private static final String DEFAULT_ENCODING = "png";
	
	/** The default screen size */
	private static final int DEFAULT_WIDTH = 320;
	private static final int DEFAULT_HEIGHT = 240;
	
	/** The default frame rate */
	private static final int DEFAULT_FRAME_RATE = 10;
	
	/** The default number of antialias samples */
	private static final int DEFAULT_ANTIALIAS_SAMPLES = 1;
	
	/** The default anisotropic degree setting */
	private static final int DEFAULT_ANISOTROPIC_DEGREE = 1;
	
	/** Usage message with command line options */
	private static final String USAGE =
		//"0---------1---------2---------3---------4---------5---------6---------7---------8"+
		//"012345678901234567890123456789012345678901234567890123456789012345678901234567890"+
		"Usage: Recorder [options] sourcefile \n" +
		"  -help                  Print out this message to the stdout \n" +
		"  -log filename          The name of the log file. If unspecified, logging \n" +
		"                         output is directed to stdout. \n" +
		"  -outdir dirname        The name of the output directory to save the image \n" +
		"                         sequence in. If unspecified, a directory named output \n" +
		"                         will be created in the current user directory. \n" +
		"  -encode name           The name of the image encoding type [png|jpg|gif] \n" +
		"                         If unspecified, defaults to png. \n" +
		"  -size widthxheight     The size of the image capture in pixels. \n" +
		"                         Defaults to "+ DEFAULT_WIDTH +"x"+ DEFAULT_HEIGHT +" \n" +
		"  -fps n                 The number of frames-per-second to capture. \n" +
		"                         Defaults to "+ DEFAULT_FRAME_RATE +" \n" +
		"  -sbuffer               Use single buffering. If unspecified, double \n" +
		"                         buffering is used. \n" +
		"  -mipmaps               Use mipmaps. If unspecified, mipmap generation is \n" +
		"                         disabled. \n" +
		"  -antialias n           The number of antialias samples to use. If unspecified, \n" +
		"                         antialiasing is disabled. \n" +
		"  -anisotropicDegree n   The anistropic degree setting to use. If unspecified, \n" +
		"                         anisotropic filtering is disabled. \n" +
		"  -verbose               Enable message logging level. \n" +
		"  -stats                 Enable statistics generation on rendering and image file \n" +
		"                         creation times. Used with the -verbose option to display. \n" +
		"  -show                  Set the X3D browser window visible while recording. \n" +
		"  -interactive           Run the application in interactive mode. The sourcefile \n" +
		"                         argument is ignored and the x3d browser window is displayed. \n" +
		"                         A file chooser dialog is available for selecting the file \n" +
		"                         to record. \n";
	
	//////////////////////////////////////////////////////////////////////
	// Browser construct function modules
	
	/** The browser construct world loader */
	private BlockingWorldLoader loader;
	
	/** The image recorder */
	private TimeSensorSceneRecorder recorder;
	
	//////////////////////////////////////////////////////////////////////
	// configuration parameters
	
	/** Run in interactive mode */
	private boolean interactive;
	
	/** In non-interactive mode, show the browser window while recording */
	private boolean showWindow;
	
	/** Enable message logging level */
	private boolean verbose;
	
	/** Enable statistics reporting */
	private boolean stats;
	
	/** The image encoding identifier */
	private String encoding;
	
	/** The browser/recorder width */
	private int width;
	
	/** The browser/recorder height */
	private int height;
	
	/** The recorder's frame rate */
	private int frameRate;
	
	/** Use mipmaps */
	private boolean useMipMaps;
	
	/** Use single buffering */
	private boolean sbuffer;
	
	/** The antialias samples */
	private int antialiasSamples;
	
	/** The anisotropic degree */
	private int anisotropicDegree;
	
	/** The directory where the image sequence will be written */
	private File outputDir;
	
	/** The file to record */
	private File sourceFile;
	
	//////////////////////////////////////////////////////////////////////
	// UI components
	
	/** The UI Window */
	private JFrame frame;
	
	/** UI control for opening the open file chooser dialog */
	private JMenuItem openItem;
	
	/** File chooser for picking an x3d world to open */
	private JFileChooser chooser;
	
	//////////////////////////////////////////////////////////////////////
	
	/** The error reporter */
	private static ErrorReporter logger;
	
	/**
	 * Constructor
	 *
	 * @param arg The command line arguments
	 */
	public SceneRecorder ( String[] arg ) {
		
		//////////////////////////////////////////////////////////////////////
		// parse the command line arguments, set up the working parameters
		boolean success = false;
		Exception exc = null;
		try {
			success = parseArgs( arg );
		} catch ( Exception e ) {
			exc = e;
		}
		
		if ( !success ) {
			if ( logger != null ) {
				logger.fatalErrorReport( 
					LOG_NAME +": Error parsing command line arguments", exc );
			}
			System.exit( -1 );
		}
		
		//////////////////////////////////////////////////////////////////////
		// configure a browser and the functional units to manage the recording
		
		// we like multi-threaded content loading
		System.setProperty( "org.xj3d.core.loading.threads", "4" );
		
		//////////////////////////////////////////////////////////////////////
		// uncomment to see what the GLCapabilitiesChooser is doing
		//System.setProperty( "jogl.debug.DefaultGLCapabilitiesChooser", "true" );
		//////////////////////////////////////////////////////////////////////
		
		boolean headless = false;
		AWTOGLConstruct construct = null;
		if ( showWindow || interactive ) {
			construct = new SceneRecorderConstruct( logger );
			headless = GraphicsEnvironment.isHeadless( );
			if ( interactive && headless ) {
				interactive = false;
			}
		} else {
			construct = new OffscreenSceneRecorderConstruct( logger, width, height );
		}
		
		// push the command line preferences for graphics capabilities into
		// the browser construct before the renderer & capabilities are built
		((ConfigGraphicsCapabilities)construct).setGraphicsCapabilitiesParameters( 
			useMipMaps, 
			!sbuffer, 
			antialiasSamples, 
			anisotropicDegree );
		
		// instantiate and configure all the xj3d objects
		construct.buildAll( );
		
		// the shutdown controller must be instantiated AFTER the renderer
		// and x3d manager's have been created
		ShutdownManager disposer = new ShutdownManager( construct );
		
		// create and configure the world loader and scene recorder
		loader = new BlockingWorldLoader( construct );
		recorder = new TimeSensorSceneRecorder( construct );
		recorder.setSize( width, height );
		recorder.setFrameRate( frameRate );
		recorder.setOutputDirectory( outputDir );
		recorder.setEncoding( encoding );
		
		if ( showWindow || interactive ) {
			if ( headless ) {
				Container contentPane = new Container( );
				contentPane.setLayout( new BorderLayout( ) );
				
				// set the image size for recording
				contentPane.setPreferredSize( new Dimension( width, height ) );
				contentPane.add( (Component)construct.getGraphicsObject( ), BorderLayout.CENTER );
			} else {
				// create the 'UI' components
				frame = new JFrame( LOG_NAME );
				
				Container contentPane = frame.getContentPane( );
				contentPane.setLayout( new BorderLayout( ) );
				
				// set the image size for recording
				contentPane.setPreferredSize( new Dimension( width, height ) );
				contentPane.add( (Component)construct.getGraphicsObject( ), BorderLayout.CENTER );
				
				if ( interactive ) {
					JPopupMenu.setDefaultLightWeightPopupEnabled( false );
					JMenuBar mb = new JMenuBar( );
					frame.setJMenuBar( mb );
					
					JMenu fileMenu = new JMenu( "File" );
					mb.add( fileMenu );
					
					openItem = new JMenuItem( "Open World" );
					openItem.addActionListener( this );
					fileMenu.add( openItem );
					
					File dir = new File( System.getProperty( "user.dir" ) );
					chooser = new JFileChooser( dir );
				}
				
				// MUST call pack(), else 'mysterious' problems occur
				frame.pack( );
				frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
				
				Dimension screenSize = frame.getToolkit( ).getScreenSize( );
				Dimension frameSize = frame.getSize( );
				frame.setLocation( 
					( screenSize.width - frameSize.width )/2, 
					( screenSize.height - frameSize.height )/2 );
				frame.setVisible( true );
			}
		}
		//////////////////////////////////////////////////////////////////////
		// configuration complete, lock & load
		
		if ( !interactive ) {
			InputSource is = new InputSource( sourceFile );
			success = loader.load( is );
			if ( success ) {
				// start the recording, the completion will be handled
				// by the RecorderListener method
				recorder.start( this );
			} else {
				logger.fatalErrorReport( 
					LOG_NAME +": Error loading source file", null );
				System.exit( -1 );
			}
		}
	}
	
	//----------------------------------------------------------
	// Methods defined by ActionListener
	//----------------------------------------------------------
	
	/**
	 * Handle the file chooser in interactive mode
	 */
	public void actionPerformed( ActionEvent ae ) {
		Object source = ae.getSource( );
		if ( source == openItem ) {
			int returnVal = chooser.showDialog( frame, "Open World" );
			if( returnVal == JFileChooser.APPROVE_OPTION ) {
				File file = chooser.getSelectedFile( );
				if ( file != null ) {
					InputSource is = new InputSource( file );
					boolean success = loader.load( is );
					if ( success ) {
						recorder.start( this );
					}
				}
			}
		} 
	}
	
	//----------------------------------------------------------
	// Methods defined by RecorderListener
	//----------------------------------------------------------
	
	/**
	 * Wait for recording completion event, then exit.
	 */
	public void recorderStatusChanged( RecorderEvent evt ) {
		switch( evt.id ) {
		case RecorderEvent.ACTIVE:
			break;
			
		case RecorderEvent.COMPLETE:
			if ( stats ) {
				logger.messageReport( LOG_NAME +
					": Average rendering time per frame: "+ 
					recorder.getAverageRenderTime( ) +" ms" );
				logger.messageReport( LOG_NAME +
					": Average IO time per image file: "+ 
					recorder.getAverageFileTime( ) +" ms" );
			}
			if ( !interactive ) {
				System.exit( 0 );
			}
			break;
		}
	}
	
	//----------------------------------------------------------
	// Local Methods
	//----------------------------------------------------------
	
	/**
	 * Parse and validate the command line arguments, initialize the working 
	 * parameters.
	 *
	 * @param arg The command line arguments
	 * @return true if the app should continue, false if it should exit.
	 */
	private boolean parseArgs( String[] arg ) {
		
		int argIndex = -1;
		String log_file_name = null;
		String output_dir_name = null;
		String encoding_string = null;
		String size_string = null;
		String frame_rate_string = null;
		String antialias_samples_string = null;
		String anisotropic_degree_string = null;
		
		//////////////////////////////////////////////////////////////////////
		// parse the arguments, sort out a help request
		for ( int i = 0; i < arg.length; i++ ) {
			String argument = arg[i];
			if ( argument.startsWith( "-" ) ) {
				try {
					if ( argument.equals( "-help" ) ) {
						// presumably, a help request won't be generated
						// in a headless environment - send to stdout
						System.out.println( USAGE );
						return( false );
					} else if ( argument.equals( "-log" ) ) {
						log_file_name = arg[i+1];
						argIndex = i+1;
					} else if ( argument.equals( "-outdir" ) ) {
						output_dir_name = arg[i+1];
						argIndex = i+1;
					} else if ( argument.equals( "-encode" ) ) {
						encoding_string = arg[i+1];
						argIndex = i+1;
					} else if ( argument.equals( "-show" ) ) {
						showWindow = true;
						argIndex = i;
					} else if ( argument.equals( "-size" ) ) {
						size_string = arg[i+1];
						argIndex = i+1;
					} else if( argument.equals( "-fps" ) ) {
						frame_rate_string = arg[i+1];
						argIndex = i+1;
					} else if ( argument.equals( "-sbuffer" ) ) {
						sbuffer = true;
						argIndex = i;
					} else if ( argument.equals( "-mipmaps" ) ) {
						useMipMaps = true;
						argIndex = i;
					} else if( argument.equals( "-antialias" ) ) {
						antialias_samples_string = arg[i+1];
						argIndex = i+1;
					} else if( argument.equals( "-anisotropicDegree" ) ) {
						anisotropic_degree_string = arg[i+1];
						argIndex = i+1;
					} else if ( argument.equals( "-verbose" ) ) {
						verbose = true;
						argIndex = i;
					} else if ( argument.equals( "-stats" ) ) {
						stats = true;
						argIndex = i;
					} else if ( argument.equals( "-show" ) ) {
						showWindow = true;
						argIndex = i;
					} else if ( argument.equals( "-interactive" ) ) {
						interactive = true;
						argIndex = i;
					} 
				} catch ( Exception e ) {
					// this would be an IndexOutOfBounds - should arrange to log it
				}
			}
		}
		
		//////////////////////////////////////////////////////////////////////
		// establish the error logger first
		
		if ( log_file_name != null ) {
			try {
				File log_file = new File( log_file_name );
				if ( !log_file.exists( ) || log_file.isFile( ) ) {
					logger = new LoggingErrorReporter( log_file, verbose, true, true, true );
				}
			} catch ( Exception e ) {
			}
		}
		
		if ( logger == null ) {
			logger = new SystemErrorReporter( verbose, true, true, true );
		}
		
		//////////////////////////////////////////////////////////////////////
		// the input source file should be the last unused arg - 
		// validate if running in non-interactive mode, otherwise ignore
		
		if ( !interactive ) {
			String source_file_name = null;
			if ( ( arg.length > 0 ) && ( argIndex + 1 < arg.length ) ) {
				source_file_name = arg[arg.length - 1];
				try {
					sourceFile = new File( source_file_name );
					if ( !sourceFile.exists( ) ) {
						logger.errorReport( LOG_NAME +
							": Source file: "+ source_file_name +
							" does not exist.", null );
						return( false );
					} else if ( sourceFile.isDirectory( ) ) {
						logger.errorReport( LOG_NAME +
							": Source file: "+ source_file_name +
							" is a directory.", null );
						return( false );
					}
				} catch ( Exception e ) {
					logger.errorReport( LOG_NAME +
						": Source file error.", e );
					return( false );
				}
			} else {
				logger.errorReport( LOG_NAME +
					": No source file specified.", null );
				return( false );
			}
		}
		//////////////////////////////////////////////////////////////////////
		// validate the destination directory
		
		if ( output_dir_name == null ) {
			File dir = new File( System.getProperty( "user.dir" ) );
			outputDir = new File( dir, "output" );
			if ( outputDir.exists( ) ) {
				if ( !outputDir.isDirectory( ) ) {
					logger.errorReport( LOG_NAME +
						": Output directory: "+ outputDir.getName( ) +
						" is not a directory.", null );
					return( false );
				}
			} else if ( !outputDir.mkdir( ) ) {
				logger.errorReport( LOG_NAME +
					": Creating output directory failed.", null );
				return( false );
			}
		}  else {
			try {
				outputDir = new File( output_dir_name );
				if ( outputDir.exists( ) ) {
					if ( !outputDir.isDirectory( ) ) {
						logger.errorReport( LOG_NAME +
							": Output directory: "+ output_dir_name +
							" is not a directory.", null );
						return( false );
					}
				} else if ( !outputDir.mkdirs( ) ) {
					logger.errorReport( LOG_NAME +
						": Creating output directory failed.", null );
					return( false );
				}
			} catch ( Exception e ) {
				logger.errorReport( LOG_NAME +
					": Output directory error.", e );
				return( false );
			}
		}
		
		//////////////////////////////////////////////////////////////////////
		// options....
		
		// encoding
		if ( encoding_string == null ) {
			encoding = DEFAULT_ENCODING;
		}
		else {
			boolean found = false;
			String[] format = ImageIO.getWriterFormatNames( );
			for ( int i = 0; i < format.length; i++ ) {
				if ( encoding_string.equals( format[i] ) ) {
					encoding = encoding_string;
					found = true;
					break;
				}
			}
			if ( !found ) {
				logger.errorReport( 
					LOG_NAME +": Unknown image encoding type: "+ encoding_string, null );
				return( false );
			}
		}
		
		// width & height
		if ( size_string == null ) {
			width = DEFAULT_WIDTH;
			height = DEFAULT_HEIGHT;
		}
		else {
			boolean success = false;
			int separator_index = size_string.indexOf( "x" );
			try {
				width = Integer.valueOf( size_string.substring( 0, separator_index ) ).intValue( );
				height = Integer.valueOf( size_string.substring( separator_index+1 ) ).intValue( );
				success = true;
			} catch( Exception e ) {
			}
			if ( !success ) {
				logger.warningReport( 
					LOG_NAME +": Unable to parse width & height values: "+
					size_string +", using defaults.", null );
				width = DEFAULT_WIDTH;
				height = DEFAULT_HEIGHT;
			}
		}
		
		// frame rate
		if ( frame_rate_string == null ) {
			frameRate = DEFAULT_FRAME_RATE;
		}
		else {
			try {
				frameRate = Integer.valueOf( frame_rate_string ).intValue( );
			} catch( Exception e ) {
				logger.warningReport( 
					LOG_NAME +": Unable to parse frame rate value: "+ 
					frame_rate_string +", using default.", null );
				frameRate = DEFAULT_FRAME_RATE;
			}
		}
		
		// antialias
		if ( antialias_samples_string == null ) {
			antialiasSamples = DEFAULT_ANTIALIAS_SAMPLES;
		}
		else {
			try {
				antialiasSamples = Integer.valueOf( antialias_samples_string ).intValue( );
			} catch( Exception e ) {
				logger.warningReport( 
					LOG_NAME +": Unable to parse antialias sample value: "+
					antialias_samples_string +", using default.", null );
				antialiasSamples = DEFAULT_ANTIALIAS_SAMPLES;
			}
		}
		
		// anistropic filtering
		if ( anisotropic_degree_string == null ) {
			anisotropicDegree = DEFAULT_ANISOTROPIC_DEGREE;
		}
		else {
			try {
				anisotropicDegree = Integer.valueOf( anisotropic_degree_string ).intValue( );
			} catch( Exception e ) {
				logger.warningReport( 
					LOG_NAME +": Unable to parse anisotropic degree value: "+
					anisotropic_degree_string +", using default.", null );
				anisotropicDegree = DEFAULT_ANISOTROPIC_DEGREE;
			}
		}
		//////////////////////////////////////////////////////////////////////
		return( true );
	}
	
	/**
	 * Entry point. For a full list of valid arguments,
	 * invoke with the -help argument.
	 *
	 * @param arg The list of arguments
	 */
	public static void main( String[] arg ) {
		SceneRecorder test = new SceneRecorder( arg );
	}
}
