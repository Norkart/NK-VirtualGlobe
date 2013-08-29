/*

*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
//import org.web3d.x3d.sai.InvalidNodeException;
import org.web3d.x3d.sai.ProfileInfo;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

/**
 *
 */
public class X3DNodeGen {
    //
    public static final String profileID = "Immersive";
    public static final String outputDestination = "./node";
    public static final String nodeList = "./x3d_immersive_node_list.txt";
    public static final String packageID = "com.warudo.x3d";
    //
    static X3DScene scene;

    /**
     *
     */
    public static void main( String[] args ) {
        //
        BufferedReader input = null;
        try { input = new BufferedReader( new FileReader( nodeList ) ); }
        catch ( FileNotFoundException fnfe ) {
            System.out.println( "Node list: " + nodeList + " not found" );
        }
        //
        File outputDirectory = new File( outputDestination );
        if ( outputDirectory.exists( ) ) {
            if ( !outputDirectory.isDirectory( ) ) {
                System.out.println( "Destination: " + outputDestination + " is not a directory" );
                outputDirectory = null;
            }
        }
        else  { outputDirectory.mkdir( ); }
        //
        if ( ( input != null ) && ( outputDirectory != null ) ) {
            //
            HashMap params=new HashMap( );
            params.put( "Xj3D_ShowConsole", Boolean.FALSE );
            params.put( "Xj3D_LocationShown", Boolean.FALSE );
            params.put( "Xj3D_NavbarShown", Boolean.FALSE );
            //
            //BrowserFactory.setBrowserFactoryImpl( new org.web3d.j3d.browser.X3DJ3DBrowserFactoryImpl( ) );
            //BrowserFactory.setBrowserFactoryImpl( new org.web3d.ogl.browser.X3DOGLBrowserFactoryImpl( ) );
            X3DComponent component = BrowserFactory.createX3DComponent( params );
            ExternalBrowser browser = component.getBrowser( );
            //
            ProfileInfo profile = browser.getProfile( profileID );
            scene = browser.createScene( profile, null );
            //
            try {
                String nodeName = null;
                while( ( nodeName = input.readLine( ) ) != null ) {
                    if ( nodeName.startsWith( "#" ) ) {
                        System.out.println( "Skipping processing of " + nodeName.substring( 1 ) );
                    }
                    else if ( compose( nodeName.trim( ), outputDirectory ) ) {
                        //System.out.println( nodeName + ".java" + " successfully created" );
                    }

                }
            }
            catch( IOException ioe ) { System.out.println( ioe.getMessage( ) ); }
            //
            browser.dispose( );
        }
        System.exit( 0 );
    }
    /**
     *
     */
    static boolean compose( final String nodeName, final File outputDirectory ) {
        X3DNode node = null;
        X3DFieldDefinition[] fieldDefs = null;
        try {
            node = scene.createNode( nodeName );
            fieldDefs = node.getFieldDefinitions( );
        }
        // apparently a bug - a different runtime exception type is thrown....
        //catch( InvalidNodeException ine ) {
        //catch( org.web3d.vrml.lang.UnsupportedNodeException ine ) {
        //
        // plus, other exceptions occur during both methods.....
        catch( Exception e ) {
            System.out.println( "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" );
            System.out.println( nodeName + " node processing failed:" );
            e.printStackTrace( );
            System.out.println( "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" );
            return( false );
        }
        BufferedWriter writer = null;
System.out.println("Node: " + nodeName);
        try {
            writer = new BufferedWriter( new FileWriter(
                new File( outputDirectory, nodeName + ".java" ) ) );
            addLine( writer, PACKAGE_DECLARATION + packageID + EOS );
            writer.newLine( );
            final TreeSet imports = new TreeSet( );
            imports.add( "X3DNode" );
            imports.add( "X3DScene" );
            for ( int i = 0; i < fieldDefs.length; i++ ) {
                imports.add( fieldDefs[i].getFieldTypeString( ) );
            }
            for ( Iterator field = imports.iterator( ); field.hasNext( ); ) {
                addLine( writer, IMPORT_DECLARATION + (String)field.next( ) + EOS );
            }
            writer.newLine( );
            addLine( writer, CLASS_DOC_HEAD + nodeName + CLASS_DOC_TAIL );
            addLine( writer, CLASS_DECLARATION + nodeName +" "+ OPEN_BRACKET );
            writer.newLine( );
            addLine( writer, NODE_DOC );
            addLine( writer, NODE_DECLARATION );
            writer.newLine( );
            for ( int i = 0; i < fieldDefs.length; i++ ) {
                final X3DFieldDefinition def = fieldDefs[i];
                final String name = def.getName( );
                final String type = def.getFieldTypeString( );
                final int access = def.getAccessType( );

System.out.println("\t" + name + " " + type + " " + access);
                switch( access ) {
                case X3DFieldTypes.INITIALIZE_ONLY:
                    addLine( writer, FIELD_DOC_HEAD + name +" "+ FIELD + FIELD_DOC_TAIL );
                    break;
                case X3DFieldTypes.INPUT_ONLY:
                    addLine( writer, FIELD_DOC_HEAD + name +" "+ EVENT_IN + FIELD_DOC_TAIL );
                    break;
                case X3DFieldTypes.OUTPUT_ONLY:
                    addLine( writer, FIELD_DOC_HEAD + name +" "+ EVENT_OUT + FIELD_DOC_TAIL );
                    break;
                case X3DFieldTypes.INPUT_OUTPUT:
                    addLine( writer, FIELD_DOC_HEAD + name +" "+ EXPOSED_FIELD + FIELD_DOC_TAIL );
                    break;
                default:
                    System.out.println( "Invalid access type" );
                }
                addLine( writer, FIELD_DECLARATION + type +" "+ name + EOS );
                writer.newLine( );
            }
            addLine( writer, CONSTRUCTOR_DOC );
            addLine( writer, CONSTRUCTOR_DECLARATION_HEAD + nodeName + CONSTRUCTOR_DECLARATION_PARAMS +" "+ OPEN_BRACKET );
            addLine( writer, CREATE_SCENE_HEAD + OPEN_PAREN + QUOTE + nodeName + QUOTE + CLOSE_PAREN + EOS );
            for ( int i = 0; i < fieldDefs.length; i++ ) {
                final X3DFieldDefinition def = fieldDefs[i];
                final String name = def.getName( );
                final String type = def.getFieldTypeString( );
                addLine( writer, name + EQUALS + OPEN_PAREN + type + CLOSE_PAREN + GET_FIELD +
                    OPEN_PAREN + QUOTE + name + QUOTE + CLOSE_PAREN + EOS );
            }
            addLine( writer, CLOSE_BRACKET );
            addLine( writer, CLOSE_BRACKET );
            //
            writer.flush( );
            writer.close( );
        }
        catch ( IOException ioe ) {
            System.out.println( ioe.getMessage( ) );
            return( false );
        }
        return( true );
    }
    /**
     *
     */
    static void addLine( final BufferedWriter writer, final String line ) throws IOException {
        writer.write( line, 0, line.length( ) );
        writer.newLine( );
    }
    /** Fragments of class file */
    public static final String PACKAGE_DECLARATION = "package ";
    public static final String IMPORT_DECLARATION = "import org.web3d.x3d.sai.";
    public static final String CLASS_DOC_HEAD = "/** An SAI ";
    public static final String CLASS_DOC_TAIL = " node wrapper. */";
    public static final String CLASS_DECLARATION = "public class ";
    public static final String NODE_DOC = "/** The node */";
    public static final String NODE_DECLARATION = "public final X3DNode node;";
    public static final String FIELD_DOC_HEAD = "/** The ";
    public static final String FIELD_DOC_TAIL = " */";
    public static final String FIELD_DECLARATION = "public final ";
    public static final String CONSTRUCTOR_DOC = "/** Constructor */";
    public static final String CONSTRUCTOR_DECLARATION_HEAD = "public ";
    public static final String CONSTRUCTOR_DECLARATION_PARAMS = "( final X3DScene scene )";
    public static final String CREATE_SCENE_HEAD = "node = scene.createNode";
    public static final String GET_FIELD = "node.getField";
    //
    public static final String COMMENT = "// ";
    public static final String OPEN_BRACKET = "{";
    public static final String CLOSE_BRACKET = "}";
    public static final String OPEN_PAREN = "(";
    public static final String CLOSE_PAREN = ")";
    public static final String QUOTE = "\"";
    public static final String EQUALS = "=";
    public static final String EOS = ";";
    public static final String EOL = "\n";
    //
    public static final String FIELD = "field";
    public static final String EXPOSED_FIELD = "exposedField";
    public static final String EVENT_IN = "eventIn";
    public static final String EVENT_OUT = "eventOut";
}

