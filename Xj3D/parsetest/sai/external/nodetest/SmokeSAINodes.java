/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashMap;

import javax.swing.JFrame;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.BrowserFactoryImpl;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DNode;

/** A smoke test for the Xj3D SAI. The test consists of creating each node,
 *  getting a reference to every field of the node, reading every inputOnly field,
 *  writing a legal but non-default value to every outputOnly/inputOutput/initializeOnly
 *  field, reading the written value from each inputOuput/initializeOnly field and
 *  comparing the returned value to the written value.
 *
 * @author Rex Milton
 * @version
 */
public class SmokeSAINodes extends tController {

    /** Immersive profile ID */
    static final String IMMERSIVE = "Immersive";

    /** Renderer choice */
    static BrowserFactoryImpl bfi = new org.web3d.ogl.browser.X3DOGLBrowserFactoryImpl( );

    /** X3D console display choice */
    static boolean displayConsole;

    /** Exit condition choice */
    static final int EXIT_ON_COMPLETION = 0;
    static final int EXIT_ON_ERROR = 1;
    static final int EXIT_ON_CLOSE = 2;
    static int exitCondition = EXIT_ON_COMPLETION;

    /** Input filename, file containing list of nodes to test */
    static String infile = "./x3d_immersive_node_list.txt";

    /** Usage message */
    static final String USAGE =
        "Usage: SmokeSAINodes [options]\n" +
        "  -help                                 Print this usage message and exit\n" +
        "  -render [ogl|j3d]                     Renderer selection, default is ogl\n" +
        "  -exit [onClose|onCompletion|onError]  Exit condition selection, default is onCompletion\n" +
        "  -console                              Display the X3D browser console\n"+
        "  -infile filename                      Optional input filename, the list of nodes to encode,\n"+
        "                                        the default is \"./x3d_immersive_node_list.txt\"\n";

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
                else if ( arg.equals( "-infile" ) ) {
                    infile = args[++i];
                }
                else if ( arg.equals( "-help" ) ) {
                    System.out.println( USAGE );
                    System.exit( 0 );
                }
                else { System.out.println( "Unknown argument: " + arg + " - ignored" ); }
            }
            else { System.out.println( "Unknown argument: " + arg + " - ignored" ); }
        }
        final int exitStatus = new SmokeSAINodes( ).exec( );
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
    public SmokeSAINodes( ) {
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
        BufferedReader input = null;
        try { input = new BufferedReader( new FileReader( infile ) ); }
        catch ( FileNotFoundException fnfe ) {
            logMessage( tMessageType.WARNING, "Node list: " + infile + " not found" );
            return( ERROR );
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

                        // TODO: Is this necessary.  Should read/write be ok before realize
                        node.realize();
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
                        for ( int i = 0; i < fieldDefs.length; i++ ) {
                            final X3DFieldDefinition def = fieldDefs[i];
                            try {
                                tX3DField field = tX3DFieldFactory.getInstance( node, def, this );
                                if ( field == null ) {
                                    logMessage( tMessageType.ERROR, nodeName +":"+ def.getName( ) +":"+
                                        " unknown field type: " + def.getFieldTypeString( ) );
                                }
                                else { field.smoke( ); }
                            }
                            catch( Exception e ) {
                                logMessage( tMessageType.ERROR, nodeName +":"+ def.getName( ) + " field processing failed:", e );
                            }
                            if ( ( exitCondition == EXIT_ON_ERROR ) && ( exitStatus == ERROR ) ) { break; }
                        }
                    }
                    if ( ( exitCondition == EXIT_ON_ERROR ) && ( exitStatus == ERROR ) ) { break; }
                }
            }
        }
        catch( IOException ioe ) {
            logMessage( tMessageType.ERROR, "Exception reading node list: " + ioe.getMessage( ) );
        }
        return( exitStatus );
    }
}

