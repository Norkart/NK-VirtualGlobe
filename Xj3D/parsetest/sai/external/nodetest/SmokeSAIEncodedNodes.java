/*

*/

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.BrowserFactoryImpl;
import org.web3d.x3d.sai.InvalidX3DException;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

/**
 *
 */
public class SmokeSAIEncodedNodes extends tController {

    /** Immersive profile ID */
    static final String IMMERSIVE = "Immersive";

    /** Renderer choice */
    static BrowserFactoryImpl bfi = new org.web3d.ogl.browser.X3DOGLBrowserFactoryImpl( );

    /** Encoding choice */
    static final int XML = 0;
    static final int CLASSIC = 1;
    static int encode = XML;

    /** X3D console display choice */
    static boolean displayConsole;

    /** Print encoded node choice */
    static boolean print;

    /** Exit condition choice */
    static final int EXIT_ON_COMPLETION = 0;
    static final int EXIT_ON_ERROR = 1;
    static final int EXIT_ON_CLOSE = 2;
    static int exitCondition = EXIT_ON_COMPLETION;

    /** Input filename, file containing list of nodes to test */
    static String infile = "./x3d_immersive_node_list.txt";

    /** Output filename, file to direct the encoded output to */
    static String outfile;

    /** Usage message */
    static final String USAGE =
        "Usage: SmokeSAIEncodedNodes [options]\n" +
        "  -help                                 Print this usage message and exit\n" +
        "  -render [ogl|j3d]                     Renderer selection, default is ogl\n" +
        "  -encode [xml|classic]                 Encoding selection, default is xml\n" +
        "  -exit [onClose|onCompletion|onError]  Exit condition selection, default is onCompletion\n" +
        "  -console                              Display the X3D browser console\n"+
        "  -print                                Print the encoded nodes prior to parsing\n"+
        "  -infile filename                      Optional input filename, the list of nodes to encode,\n"+
        "                                        the default is \"./x3d_immersive_node_list.txt\"\n"+
        "  -outfile filename                     Optional output filename for the encoded nodes,\n"+
        "                                        the file extension is determined by the encoding\n"+
        "                                        \"x3d\" for xml encoding, \"x3dv\" for classic encoding\n";

    /**
     *
     */
    public static void main( final String[] args ) {
        //
        for ( int i = 0; i < args.length; i++ ) {
            final String arg = args[i];
            if ( arg.startsWith( "-" ) ) {
                if ( arg.equals( "-render" ) ) {
                    final String subArg = args[++i];
                    if ( subArg.equals( "j3d" ) ) {
                        bfi = new org.web3d.j3d.browser.X3DJ3DBrowserFactoryImpl( );
                    }
                    else if ( subArg.equals( "ogl" ) ) {
                        //bfi = new org.web3d.ogl.browser.X3DOGLBrowserFactoryImpl( );
                    }
                    else {
                        System.out.println( "Unknown renderer: " + subArg + " - using default" );
                    }
                }
                else if ( arg.equals( "-encode" ) ) {
                    final String subArg = args[++i];
                    if ( subArg.equals( "xml" ) ) { encode = XML; }
                    else if ( subArg.equals( "classic" ) ) { encode = CLASSIC; }
                    else {
                        System.out.println( "Unknown encoding: " + subArg + " - using default" );
                    }
                }
                else if ( arg.equals( "-exit" ) ) {
                    final String subArg = args[++i];
                    if ( subArg.equals( "onCompletion" ) ) {
                        //exitCondition = EXIT_ON_COMPLETION;
                    }
                    else if ( subArg.equals( "onError" ) ) { exitCondition = EXIT_ON_ERROR; }
                    else if ( subArg.equals( "onClose" ) ) { exitCondition = EXIT_ON_CLOSE; }
                    else {
                        System.out.println( "Unknown exit condition: " + subArg + " - using default" );
                    }
                }
                else if ( arg.equals( "-console" ) ) {
                    displayConsole = true;
                }
                else if ( arg.equals( "-print" ) ) {
                    print = true;
                }
                else if ( arg.equals( "-infile" ) ) {
                    infile = args[++i];
                }
                else if ( arg.equals( "-outfile" ) ) {
                    outfile = args[++i];
                }
                else if ( arg.equals( "-help" ) ) {
                    System.out.println( USAGE );
                    System.exit( 0 );
                }
                else { System.out.println( "Unknown argument: " + arg + " - ignored" ); }
            }
            else { System.out.println( "Unknown argument: " + arg + " - ignored" ); }
        }
        final int exitStatus = new SmokeSAIEncodedNodes( ).exec( );
        //
        switch ( exitCondition ) {
        case EXIT_ON_COMPLETION:
        case EXIT_ON_ERROR:
            System.exit( exitStatus );
            break;
        case EXIT_ON_CLOSE:
        }
    }

    /** Constructor */
    public SmokeSAIEncodedNodes( ) {
        JFrame frame = new JFrame( );
        Container contentPane = frame.getContentPane( );
        contentPane.setLayout( new BorderLayout( ) );
        //
        HashMap params=new HashMap( );
        params.put( "Xj3D_ShowConsole", new Boolean( displayConsole ) );
        params.put( "Xj3D_LocationShown", Boolean.FALSE );
        params.put( "Xj3D_NavbarShown", Boolean.FALSE );
        //params.put("Xj3D_LocationReadOnly",Boolean.TRUE);
        //params.put("Xj3D_LocationPosition","Top");
        //params.put("Xj3D_NavigationPosition","Bottom");
        //
        BrowserFactory.setBrowserFactoryImpl( bfi );
        X3DComponent component = BrowserFactory.createX3DComponent( params );
        contentPane.add( (Component)component, BorderLayout.CENTER );
        this.browser = component.getBrowser( );
        this.profile = this.browser.getProfile( IMMERSIVE );
        this.scene = this.browser.createScene( profile, null );
        //
        initialize( );
        frame.pack( );
        frame.setSize( 200, 50 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        //
        ///////////////////////////////////////////////////////////////////////////
        // if setVisible( true ) is not called, the browser is never initialize,
        // no events happen, and writes to node fields are never processed.
        frame.setVisible( true );
        // additionally, the j3d browser will not process events unless the
        // frame is visible, iconifying the frame prevents events from being
        // processed. seems to have no effect on the ogl browser
        //test.setExtendedState( JFrame.ICONIFIED );
        //
        ///////////////////////////////////////////////////////////////////////////
    }

    /**
     * Execute.
     * @return the exit status
     */
    int exec( ) {
        //
        initWrapper( );
        //
        BufferedReader input = null;
        try { input = new BufferedReader( new FileReader( infile ) ); }
        catch ( FileNotFoundException fnfe ) {
            logMessage( tMessageType.WARNING, "Node list: " + infile + " not found" );
            return( ERROR );
        }
        BufferedWriter output = null;
        if ( outfile != null ) {
            String outfilename = null;
            if ( encode == XML ) { outfilename = outfile +"."+ tEncodingUtils.X3D_XML_EXT; }
            else if ( encode == CLASSIC ) { outfilename = outfile +"."+ tEncodingUtils.X3D_CLASSIC_EXT; }
            //
            try {
                output = new BufferedWriter( new FileWriter( new File( outfilename ) ) );
                switch( encode ) {
                case XML:
                    addLine( output, tEncodingUtils.XML_HEADER );
                    addLine( output, tEncodingUtils.X3D_XML_IMMERSIVE_HEADER );
                    addLine( output, tEncodingUtils.X3D_XML_SCENE_HEADER );
                    break;
                case CLASSIC:
                    addLine( output, tEncodingUtils.X3D_CLASSIC_IMMERSIVE_HEADER );
                    break;
                }
                output.newLine( );
            }
            catch ( IOException ioe ) {
                logMessage( tMessageType.WARNING, ioe.getMessage( ) );
                return( ERROR );
            }
            logMessage( tMessageType.STATUS, "Directing encoded output to: " + outfilename );
        }
        //
        // wait till the browser is ready to process events
        synchronized( this ) {
            try { while ( !browserInitialized ) { wait( ); } }
            catch ( InterruptedException ie ) { ; }
        }
        //
        try {
            String nodeName = null;
            while( ( nodeName = input.readLine( ) ) != null ) {
                if ( nodeName.startsWith( "#" ) ) {
                    logMessage( tMessageType.STATUS, "Skipping processing of " + nodeName.substring( 1 ) );
                }
                else {
                    X3DNode node = null;
                    X3DFieldDefinition[] fieldDefs = null;
                    boolean success;
                    try {
                        node = this.scene.createNode( nodeName );
                        fieldDefs = node.getFieldDefinitions( );
                        success = true;
                    }
                    catch( Exception e ) {
                        logMessage( tMessageType.ERROR, nodeName + " node processing failed:", e );
                        success = false;
                    }
                    if ( success ) {
                        //
                        // wait till the next event cascade to give the
                        // node time to complete the creation phase
                        flushUpdate( );
                        //
                        StringBuffer sceneBuffer = new StringBuffer( 256 );
                        StringBuffer nodeBuffer = new StringBuffer( 256 );
                        switch( encode ) {
                        case XML:
                            sceneBuffer.append( tEncodingUtils.X3D_XML_IMMERSIVE_HEADER +"\n" );
                            sceneBuffer.append( tEncodingUtils.X3D_XML_SCENE_HEADER +"\n" );
                            nodeBuffer.append( "<" + nodeName +"\n" );
                            break;
                        case CLASSIC:
                            sceneBuffer.append( tEncodingUtils.X3D_CLASSIC_IMMERSIVE_HEADER +"\n");
                            nodeBuffer.append( nodeName + " {\n");
                            break;
                        }
                        //
                        for ( int i = 0; i < fieldDefs.length; i++ ) {
                            final X3DFieldDefinition def = fieldDefs[i];
                            try {
                                tX3DField field = tX3DFieldFactory.getInstance( node, def, this );
                                if ( field == null ) {
                                    logMessage( tMessageType.ERROR, nodeName +":"+ def.getName( ) +":"+
                                        " unknown field type: " + def.getFieldTypeString( ) );
                                }
                                else {
                                    final int access = def.getAccessType( );
                                    //
                                    // do not include eventIns or eventOuts in the test encoding
                                    if ( ( access != X3DFieldTypes.INPUT_ONLY ) &&
                                        ( access != X3DFieldTypes.OUTPUT_ONLY ) ) {
                                        switch( encode ) {
                                        case XML:
                                            //
                                            // child nodes are implicit in the XML markup, so
                                            // do not attempt to include them in the encoding
                                            // as they are by default null
                                            final int type = def.getFieldType( );
                                            if ( ( type != X3DFieldTypes.SFNODE ) &&
                                                ( type != X3DFieldTypes.MFNODE ) ) {
                                                nodeBuffer.append( field.encode( tValue.SMOKE, tEncode.XML ) +"\n" );
                                            }
                                            break;
                                        case CLASSIC:
                                            //
                                            // child nodes are explicit in the classic encoding,
                                            // so include them despite the fact that they are empty
                                            nodeBuffer.append( field.encode( tValue.SMOKE, tEncode.CLASSIC ) +"\n" );
                                            break;
                                        }
                                    }
                                }
                            }
                            catch( Exception e ) {
                                logMessage( tMessageType.ERROR, nodeName +":"+ def.getName( ) +
                                    " field processing failed:", e );
                            }
                            if ( ( exitCondition == EXIT_ON_ERROR ) && ( exitStatus == ERROR ) ) { break; }
                        }
                        switch( encode ) {
                        case XML:
                            nodeBuffer.append( "/>\n" );
                            wrap( nodeName, tEncode.XML, nodeBuffer );
                            sceneBuffer.append( nodeBuffer );
                            sceneBuffer.append( tEncodingUtils.X3D_XML_SCENE_FOOTER +"\n" );
                            sceneBuffer.append( tEncodingUtils.X3D_XML_FOOTER +"\n" );
                            break;
                        case CLASSIC:
                            nodeBuffer.append( "}\n" );
                            wrap( nodeName, tEncode.CLASSIC, nodeBuffer );
                            sceneBuffer.append( nodeBuffer );
                            break;
                        }
                        try {
                            if ( print ) {
                                logMessage( tMessageType.STATUS, new String[]{
                                        nodeName +":"+ " beginning parse",
                                            sceneBuffer.toString( ) } );
                            }
                            final X3DScene scene = browser.createX3DFromString( sceneBuffer.toString( ) );
                            if ( scene != null ) {
                                logMessage( tMessageType.SUCCESS, nodeName +":"+ " createX3DFromString complete" );
                            }
                            else {
                                logMessage( tMessageType.ERROR, nodeName +":"+ " createX3DFromString returned null" );
                            }
                        }
                        catch ( Exception e ) {
                            logMessage( tMessageType.ERROR, nodeName +":"+ " createX3DFromString failed:", e );
                        }
                        if ( output != null ) { addLine( output, nodeBuffer.toString( ) ); }
                    }
                    if ( ( exitCondition == EXIT_ON_ERROR ) && ( exitStatus == ERROR ) ) { break; }
                }
            }
            if ( output != null ) {
                switch( encode ) {
                case XML:
                    addLine( output, tEncodingUtils.X3D_XML_SCENE_FOOTER );
                    addLine( output, tEncodingUtils.X3D_XML_FOOTER );
                    break;
                case CLASSIC:
                    break;
                }
                output.flush( );
                output.close( );
            }
        }
        catch( IOException ioe ) {
            logMessage( tMessageType.ERROR, "Exception reading node list: " + ioe.getMessage( ) );
        }
        return( exitStatus );
    }

    /** Add a <code>String</code> to the specified <code>BufferedWriter</code> output
     * @param output the <code>BufferedWriter</code>
     * @param line the <code>String</code> to add */
    static void addLine( final BufferedWriter output, final String line ) throws IOException {
        output.write( line, 0, line.length( ) );
        output.newLine( );
    }

    /** Map, keyed by container field names, value is a <code>List</code> of
     *  node names that must be included in a scene as a child node of the
     *  container field */
    static HashMap nodeTypeMap = new HashMap( );

    /** Initialize the nodeTypeMap from a set of text files. The file
     *  <code>child_node_types.txt</code> contains the set of map keys
     *  which also are the names of the files which contain the list
     *  of nodes. */
    static void initWrapper( ) {
        BufferedReader type_input = null;
        try { type_input = new BufferedReader( new FileReader( "child_node_types.txt" ) ); }
        catch ( FileNotFoundException fnfe ) { ; }
        try {
            String node_type = null;
            while( ( node_type = type_input.readLine( ) ) != null ) {
                BufferedReader node_input = null;
                try { node_input = new BufferedReader( new FileReader( node_type +".txt" ) ); }
                catch ( FileNotFoundException fnfe ) { ; }
                ArrayList nodeNameList = new ArrayList( );
                String node_name = null;
                while( ( node_name = node_input.readLine( ) ) != null ) {
                    nodeNameList.add( node_name );
                }
                nodeTypeMap.put( node_type, nodeNameList );
            }
        }
        catch( IOException ioe ) { ; }
    }

    /** Locate the node in the nodeTypeMap, thus determining the appropriate container field.
     *  Construct a valid child node encoding for the node based on the container. */
    static void wrap( final String nodeName, final tEncode encode, final StringBuffer encodedNode ) {
        String nodeType = null;
        boolean nodeFound = false;

        System.out.println("Wrapping: " + nodeName);

        for ( Iterator keys = nodeTypeMap.keySet( ).iterator( ); keys.hasNext( );  ) {
            nodeType = (String)keys.next( );
            final List nodeNameList = (List)nodeTypeMap.get( nodeType );
            for( int i = 0; i < nodeNameList.size( ); i++ ) {
                final String t_nodeName = (String)nodeNameList.get( i );
                if ( t_nodeName.equals( nodeName ) ) {
                    nodeFound = true;
                    break;
                }
            }
            if ( nodeFound ) { break; }
        }
        if ( nodeFound ) {
            //System.out.println( nodeName +" is of type "+ nodeType );
            if ( nodeType.equals( "children" ) ) { ; }
            else if ( ( nodeType.equals( "geometry" ) ) ||
                ( nodeType.equals( "appearance" ) ) ||
                ( nodeType.equals( "metadata" ) ) ) {
                if ( encode == tEncode.XML ) {
                    encodedNode.insert( 0, SHAPE_XML[0] );
                    encodedNode.append( SHAPE_XML[1] );
                }
                else if ( encode == tEncode.CLASSIC ) {
                    encodedNode.insert( 0, SHAPE_CLASSIC[0] + nodeType +" " );
                    encodedNode.append( SHAPE_CLASSIC[1] );
                }
            }
            else if ( ( nodeType.equals( "material" ) ) ||
                ( nodeType.equals( "texture" ) ) ||
                ( nodeType.equals( "textureTransform" ) ) ||
                ( nodeType.equals( "lineProperties" ) ) ) {
                if ( encode == tEncode.XML ) {
                    encodedNode.insert( 0, SHAPE_XML[0] + APPEARANCE_XML[0] );
                    encodedNode.append( APPEARANCE_XML[1] + SHAPE_XML[1] );
                }
                else if ( encode == tEncode.CLASSIC ) {
                    encodedNode.insert( 0, SHAPE_CLASSIC[0] + APPEARANCE + APPEARANCE_CLASSIC[0] + nodeType +" " );
                    encodedNode.append( APPEARANCE_CLASSIC[1] + SHAPE_CLASSIC[1] );
                }
            }
            else if ( ( nodeType.equals( "color" ) ) ||
                ( nodeType.equals( "coord" ) ) ||
                ( nodeType.equals( "texCoord" ) ) ||
                ( nodeType.equals( "normal" ) ) ) {
                if ( encode == tEncode.XML ) {
                    encodedNode.insert( 0, SHAPE_XML[0] + IFS_XML[0] );
                    encodedNode.append( IFS_XML[1] + SHAPE_XML[1] );
                }
                else if ( encode == tEncode.CLASSIC ) {
                    encodedNode.insert( 0, SHAPE_CLASSIC[0] + GEOMETRY + IFS_CLASSIC[0] + nodeType +" " );
                    encodedNode.append( IFS_CLASSIC[1] + SHAPE_CLASSIC[1] );
                }
            }
            else if ( nodeType.equals( "fontStyle" ) ) {
                if ( encode == tEncode.XML ) {
                    encodedNode.insert( 0, SHAPE_XML[0] + TEXT_XML[0] );
                    encodedNode.append( TEXT_XML[1] + SHAPE_XML[1] );
                }
                else if ( encode == tEncode.CLASSIC ) {
                    encodedNode.insert( 0, SHAPE_CLASSIC[0] + GEOMETRY + TEXT_CLASSIC[0] + nodeType +" " );
                    encodedNode.append( TEXT_CLASSIC[1] + SHAPE_CLASSIC[1] );
                }
            }
            else if ( nodeType.equals( "soundSource" ) ) {
                if ( encode == tEncode.XML ) {
                    encodedNode.insert( 0, SOUND_XML[0] );
                    encodedNode.append( SOUND_XML[1] );
                }
                else if ( encode == tEncode.CLASSIC ) {
                    encodedNode.insert( 0, SOUND_CLASSIC[0] + SOURCE + nodeType +" " );
                    encodedNode.append( SOUND_CLASSIC[1] );
                }
            }
        }
    }
    /** Strings used in wrap() */
    static String[] SHAPE_XML = { "<Shape>\n", "</Shape>\n" };
    static String[] SHAPE_CLASSIC = { "Shape { \n", "}\n" };
    static String[] APPEARANCE_XML = { "<Appearance>\n", "</Appearance>\n" };
    static String[] APPEARANCE_CLASSIC = { "Appearance { \n", "}\n" };
    static String APPEARANCE = "appearance ";
    static String GEOMETRY = "geometry ";
    static String SOURCE = "source ";
    static String[] TEXT_XML = { "<Text>\n", "</Text>\n" };
    static String[] TEXT_CLASSIC = { "Text { \n", "}\n" };
    static String[] IFS_XML = { "<IndexedFaceSet>\n", "</IndexedFaceSet>\n" };
    static String[] IFS_CLASSIC = { "IndexedFaceSet { \n", "}\n" };
    static String[] SOUND_XML = { "<Sound>\n", "</Sound>\n" };
    static String[] SOUND_CLASSIC = { "Sound { \n", "}\n" };
}

