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
import java.awt.Color;
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
import org.xj3d.ui.awt.browser.ogl.ThumbnailRecorder;

import org.xj3d.ui.construct.ogl.JFrameNotifyWrapper;

/**
 * A simple application intended primarily for command line operation
 * for capturing a thumbnail image of an X3D scene.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class ThumbnailImager implements ActionListener, RecorderListener {

    /** The logging identifer of this app */
    private static final String LOG_NAME = "ThumbnailImager";

    /** The default screen size */
    private static final int DEFAULT_WIDTH = 128;
    private static final int DEFAULT_HEIGHT = 128;

    /** The default number of antialias samples */
    private static final int DEFAULT_ANTIALIAS_SAMPLES = 1;

    /** The default anisotropic degree setting */
    private static final int DEFAULT_ANISOTROPIC_DEGREE = 1;

    /** Usage message with command line options */
    private static final String USAGE =
        //"0---------1---------2---------3---------4---------5---------6---------7---------8"+
        //"012345678901234567890123456789012345678901234567890123456789012345678901234567890"+
        "Usage: "+ LOG_NAME +" [options] sourcefile \n" +
        "  -help                  Print out this message to the stdout \n" +
        "  -log filename          The name of the log file. If unspecified, logging \n" +
        "                         output is directed to stdout. \n" +
        "  -outfile filename      The name of the output file to save the image in. \n" +
        "  -size widthxheight     The size of the image capture in pixels. \n" +
        "                         Defaults to "+ DEFAULT_WIDTH +"x"+ DEFAULT_HEIGHT +" \n" +
        "  -background r g b a    Specify the background. r, g, b, a are float values in \n" +
        "                         the range of 0.0 to 1.0. \n" +
        "  -view name             Specify a named viewpoint to capture. If the name \"AUTO\" \n"+
        "                         is given, the imager will attempt to configure a viewpoint. \n" +
        "                         If unspecified, the default view name is \"ICON_VIEWPOINT\" \n" +
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
    private ThumbnailRecorder recorder;

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

    /** The background color */
    private Color background;

    /** The name of the viewpoint to capture */
    private String viewpoint_name;

    /** Use mipmaps */
    private boolean useMipMaps;

    /** Use single buffering */
    private boolean sbuffer;

    /** The antialias samples */
    private int antialiasSamples;

    /** The anisotropic degree */
    private int anisotropicDegree;

    /** The file where the thumbnail image will be written */
    private File outputFile;

    /** The x3d file to record */
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
    public ThumbnailImager ( String[] arg ) {

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
        // uncomment to disable the ImageIO native plugins
        //System.setProperty( "com.sun.media.imageio.disableCodecLib", "true" );
        //////////////////////////////////////////////////////////////////////
        // uncomment to see what the GLCapabilitiesChooser is doing
        //System.setProperty( "jogl.debug.DefaultGLCapabilitiesChooser", "true" );
        //////////////////////////////////////////////////////////////////////

        boolean headless = false;
        AWTOGLConstruct construct = null;
        if ( showWindow || interactive ) {
            construct = new ThumbnailConstruct( logger );
            headless = GraphicsEnvironment.isHeadless( );
            if ( interactive && headless ) {
                interactive = false;
            }
        } else {
            construct = new OffscreenThumbnailConstruct( logger, width, height );
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

        // create and configure the world loader and thumbnail recorder
        loader = new BlockingWorldLoader( construct );
        recorder = new ThumbnailRecorder( construct );
        recorder.setSize( width, height );
        if ( background != null ) {
            int alpha = background.getAlpha( );
            if ( alpha != 255 ) {
                // if transparency is specified in the background, we must
                // configure the recorder to postprocess the image. the capture
                // takes place with a 'green screen' background, which the
                // recorder then replaces with the designated background color
                loader.setScenePreprocessor( new ConfigureBackground( Color.GREEN ) );
                recorder.setBackgroundColor( Color.GREEN, background );
            } else {
                // no transparency, then just take the snap with the designated color
                loader.setScenePreprocessor( new ConfigureBackground( background ) );
            }
        }
        if ( viewpoint_name != null ) {
            recorder.setViewpointName( viewpoint_name );
        }
        // the image encoding type is determined by the outputFile's extension
        boolean validEncodingType = recorder.setOutputFile( outputFile );
        if ( !validEncodingType ) {
            logger.fatalErrorReport(
                LOG_NAME +": Invalid image encoding type"+
                " specified for output file", null );
            System.exit( -1 );
        }

        if ( showWindow || interactive ) {
            if ( headless ) {
                Container contentPane = new Container( );
                contentPane.setLayout( new BorderLayout( ) );

                // set the image size for recording
                contentPane.setPreferredSize( new Dimension( width, height ) );
                contentPane.add( (Component)construct.getGraphicsObject( ), BorderLayout.CENTER );
                construct.getRenderManager().setEnabled( true );
            } else {
                // create the 'UI' components
                frame = new JFrameNotifyWrapper( LOG_NAME, construct.getRenderManager());

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
        } else {
System.out.println("Enable render manager");
            construct.getRenderManager().setEnabled( true );
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
                    ": Rendering time per image: "+
                    recorder.getRenderTime( ) +" ms" );
                logger.messageReport( LOG_NAME +
                    ": IO time per image file: "+
                    recorder.getFileTime( ) +" ms" );
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
        String output_file_name = null;
        String size_string = null;
        String[] rgba = null;
        String view_name = null;
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
                    } else if ( argument.equals( "-outfile" ) ) {
                        output_file_name = arg[i+1];
                        argIndex = i+1;
                    } else if ( argument.equals( "-show" ) ) {
                        showWindow = true;
                        argIndex = i;
                    } else if ( argument.equals( "-size" ) ) {
                        size_string = arg[i+1];
                        argIndex = i+1;
                    } else if ( argument.equals( "-background" ) ) {
                        rgba = new String[4];
                        rgba[0] = arg[i+1];
                        rgba[1] = arg[i+2];
                        rgba[2] = arg[i+3];
                        rgba[3] = arg[i+4];
                        argIndex = i+4;
                    } else if ( argument.equals( "-view" ) ) {
                        view_name = arg[i+1];
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
        // validate the destination file

        if ( output_file_name == null ) {
            logger.errorReport( LOG_NAME +
                ": No output file specified.", null );
            return( false );
        }  else {
            try {
                outputFile = new File( output_file_name );
                if ( outputFile.exists( ) ) {
                    if ( !outputFile.isFile( ) ) {
                        logger.errorReport( LOG_NAME +
                            ": Output directory: "+ output_file_name +
                            " is not a file.", null );
                        return( false );
                    }
                }
            } catch ( Exception e ) {
                logger.errorReport( LOG_NAME +
                    ": Output file error.", e );
                return( false );
            }
        }

        //////////////////////////////////////////////////////////////////////
        // options....

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

        // background color
        if ( rgba != null ) {
            try {
                background = new Color(
                    Float.valueOf( rgba[0] ).floatValue( ),
                    Float.valueOf( rgba[1] ).floatValue( ),
                    Float.valueOf( rgba[2] ).floatValue( ),
                    Float.valueOf( rgba[3] ).floatValue( ) );
            } catch ( Exception e ) {
                logger.warningReport(
                    LOG_NAME +": Unable to parse background color values: "+
                    java.util.Arrays.toString( rgba ) +", using default background.", null );
                background = null;
            }
        }

        // viewpoint name
        if ( view_name != null ) {
            viewpoint_name = view_name;
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
        ThumbnailImager test = new ThumbnailImager( arg );
    }
}
