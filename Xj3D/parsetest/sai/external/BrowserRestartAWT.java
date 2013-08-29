/*****************************************************************************
 *                        Yumetech, Inc Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.web3d.x3d.sai.BrowserEvent;
import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.BrowserListener;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.ProfileInfo;
import org.web3d.x3d.sai.SFColor;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DException;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

// Local imports
// none

/**
 * A testcase for starting/shutting down/restarting an SAI'ed 
 * browser instance using the AWT UI toolkit. 
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class BrowserRestartAWT extends JFrame implements BrowserListener, ActionListener {
    
    Container contentPane;
    JButton button;
    
    X3DComponent x3dComponent;
    ExternalBrowser browser;
    
    public BrowserRestartAWT( ) {
        super( "AWT Xj3DBrowser" );
        
        System.setProperty( "x3d.sai.factory.class", 
            "org.xj3d.ui.awt.browser.ogl.X3DOGLBrowserFactoryImpl" );
        
        contentPane = this.getContentPane( );
        contentPane.setLayout( new BorderLayout( ) );
        
        button = new JButton ( "Add Browser" );
        button.addActionListener( this );
        
        contentPane.add( button, BorderLayout.SOUTH );
    }
    
    //---------------------------------------------------------
    // Methods defined by ActionListener
    //---------------------------------------------------------
    
    public void actionPerformed( ActionEvent ae ) {
        if ( browser != null ) {
            browser.dispose( );
            x3dComponent.shutdown( );
            contentPane.remove( (Component)x3dComponent );
            contentPane.repaint( );
            browser = null;
            x3dComponent = null;
            // inform the garbage collector that now would be a good time
            System.gc( );
            
            button.setText( "Add Browser" );
        }
        else {
            createBrowser( );
            contentPane.add( (Component)x3dComponent, BorderLayout.CENTER );
            contentPane.validate( );
            addSomething( );
            
            button.setText( "Remove Browser" );
        }
    }
    
    //---------------------------------------------------------
    // Methods defined by BrowserListener
    //---------------------------------------------------------
    
    /** The Browser Listener. */
    public void browserChanged( final BrowserEvent be ) {
        final int id = be.getID( );
        if ( id == BrowserEvent.INITIALIZED ) {
            System.out.println( "INITIALIZED" );
        }
        else if ( id == BrowserEvent.SHUTDOWN ) {
            System.out.println( "SHUTDOWN" );
        }
    }
    
    //---------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------

    public static void main( String[] args ) {
        BrowserRestartAWT frame = new BrowserRestartAWT( );
        frame.pack( );
        frame.setSize( 512, 512 );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setVisible( true );
    }
    
    private void createBrowser( ) {
        HashMap params = new HashMap( );
        params.put( "Xj3D_ShowConsole", Boolean.TRUE );
        params.put( "Xj3D_NavbarShown", Boolean.TRUE );
        params.put( "Xj3D_StatusBarShown", Boolean.TRUE );
        params.put( "Xj3D_FPSShown", Boolean.TRUE );
        params.put( "Xj3D_NavbarPosition", "bottom" );
        params.put( "Xj3D_LocationShown", Boolean.TRUE );
        params.put( "Xj3D_LocationPosition", "top" );
        params.put( "Xj3D_ContentDirectory", System.getProperty( "user.dir" ) );
        params.put( "Xj3D_OpenButtonShown", Boolean.TRUE );
        params.put( "Xj3D_ReloadButtonShown", Boolean.TRUE );

        x3dComponent = BrowserFactory.createX3DComponent( params );
        browser = x3dComponent.getBrowser( );
        browser.addBrowserListener( this );
    }
    
    private void addSomething( ) {
        ProfileInfo profile = browser.getProfile( "Immersive" );
        X3DScene scene = browser.createScene( profile, null );
        
        X3DNode group = scene.createNode( "Group" );
        MFNode addChildren = (MFNode)group.getField( "addChildren" );
        scene.addRootNode( group );
        
        X3DNode shape = scene.createNode( "Shape" );
        SFNode geometry = (SFNode)shape.getField( "geometry" );
        X3DNode box = scene.createNode( "Box" );
        SFNode shape_appearance = (SFNode)shape.getField( "appearance" );
        X3DNode appearance = scene.createNode( "Appearance" );
        shape_appearance.setValue( appearance );
        SFNode appearance_material = (SFNode)appearance.getField( "material" );
        X3DNode material = scene.createNode( "Material" );
        appearance_material.setValue( material );
        SFColor diffuseColor = (SFColor)material.getField( "diffuseColor" );
        diffuseColor.setValue( new float[]{ 1, 0, 0 } );
        SFColor emissiveColor = (SFColor)material.getField( "emissiveColor" );
        emissiveColor.setValue( new float[]{ 1, 0, 0 } );
        geometry.setValue( box );
        
        addChildren.setValue( 1, new X3DNode[]{ shape } );

        browser.replaceWorld( scene );
    }
}

