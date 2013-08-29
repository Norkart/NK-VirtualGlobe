/*****************************************************************************
 *                        Yumetech, Inc Copyright (c) 2007
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
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.web3d.x3d.sai.BrowserEvent;
import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.BrowserListener;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.ProfileInfo;
import org.web3d.x3d.sai.ComponentInfo;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DException;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

import org.xj3d.sai.Xj3DBrowser;
import org.xj3d.sai.Xj3DCADView;
import org.xj3d.sai.Xj3DCADViewListener;

import org.web3d.x3d.sai.cadgeometry.CADAssembly;
import org.web3d.x3d.sai.cadgeometry.CADLayer;

// Local imports
// none

/**
 * A testcase for the Xj3DCADView functionality exposed in the Xj3DBrowser interface. 
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class Xj3DCADBrowser extends JFrame implements BrowserListener, Xj3DCADViewListener, ActionListener {
	
	Container contentPane;
	
	X3DComponent x3dComponent;
	//ExternalBrowser browser;
	
	Xj3DBrowser browser;
	Xj3DCADView CADView;
	X3DScene scene;
	
	JButton abutton;
	JButton lbutton;
	JButton newButton;
	
	public Xj3DCADBrowser( ) {
		super( "Xj3DBrowser" );
		
		System.setProperty( "x3d.sai.factory.class", 
			"org.xj3d.ui.awt.browser.ogl.X3DOGLBrowserFactoryImpl" );
		
		//System.setProperty("org.xj3d.core.loading.threads", "4");
		
		contentPane = this.getContentPane( );
		contentPane.setLayout( new BorderLayout( ) );
		
		createBrowser( );
		contentPane.add( (Component)x3dComponent, BorderLayout.CENTER );
		
		abutton = new JButton( "List Assemblies" );
		abutton.addActionListener( this );
		
		lbutton = new JButton( "List Layers" );
		lbutton.addActionListener( this );
		
		//newButton = new JButton( "Add Assembly" );
		//newButton.addActionListener( this );
		//JPanel panel = new JPanel( new GridLayout( 1, 3, 1, 1 ) );
		JPanel panel = new JPanel( new GridLayout( 1, 2, 1, 1 ) );
		panel.add( abutton );
		panel.add( lbutton );
		//panel.add( newButton );
		
		contentPane.add( panel, BorderLayout.SOUTH );
	}
	
	//---------------------------------------------------------
	// Methods defined by ActionListener
	//---------------------------------------------------------
	
	public void actionPerformed( ActionEvent ae ) {
		Object source = ae.getSource( );
		if ( source == abutton ) {
			X3DNode[] node = CADView.getCADAssemblies( );
			if ( node != null ) {
				for ( int i = 0; i < node.length; i++ ) {
					System.out.println( node[i] );
				}
			} else {
				System.out.println( "No Assemblies" );
			}
		} else if ( source == lbutton ) {
			X3DNode[] node = CADView.getCADLayers( );
			if ( node != null ) {
				for ( int i = 0; i < node.length; i++ ) {
					System.out.println( node[i] );
				}
			} else {
				System.out.println( "No Layers" );
			}
		} else if ( newButton == source ) {
			CADAssembly asm = (CADAssembly)scene.createNode( "CADAssembly" );
			scene.addRootNode( asm );
			CADLayer layer = (CADLayer)scene.createNode( "CADLayer" );
			scene.addRootNode( layer );
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
	// Methods defined by Xj3DCADViewListener
	//---------------------------------------------------------
	
	public void assemblyAdded( X3DNode assembly ) {
		System.out.println( "assemblyAdded: "+ assembly );
	}
	public void assemblyRemoved( X3DNode assembly ) {
		System.out.println( "assemblyRemoved: "+ assembly );
	}
	public void layerAdded( X3DNode layer ) {
		System.out.println( "layerAdded: "+ layer );
	}
	public void layerRemoved( X3DNode layer ) {
		System.out.println( "layerRemoved: "+ layer );
	}
	
	//---------------------------------------------------------
	// Local Methods
	//---------------------------------------------------------
	
	public static void main( String[] args ) {
		Xj3DCADBrowser frame = new Xj3DCADBrowser( );
		frame.pack( );
		frame.setSize( 512, 512 );
		frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
		frame.setVisible( true );
	}
	
	private void createBrowser( ) {
		HashMap params = new HashMap( );
		params.put( "Xj3D_ShowConsole", Boolean.FALSE );
		params.put( "Xj3D_NavbarShown", Boolean.TRUE );
		params.put( "Xj3D_StatusBarShown", Boolean.TRUE );
		params.put( "Xj3D_FPSShown", Boolean.TRUE );
		params.put( "Xj3D_NavbarPosition", "bottom" );
		params.put( "Xj3D_LocationShown", Boolean.TRUE );
		params.put( "Xj3D_LocationPosition", "top" );
		//params.put( "Xj3D_ContentDirectory", System.getProperty( "user.dir" ) );
		params.put( "Xj3D_OpenButtonShown", Boolean.TRUE );
		params.put( "Xj3D_ReloadButtonShown", Boolean.TRUE );
		params.put( "Xj3D_Culling_Mode", "none" );
		
		x3dComponent = BrowserFactory.createX3DComponent( params );
		browser = (Xj3DBrowser)x3dComponent.getBrowser( );
		browser.addBrowserListener( this );
		
		CADView = browser.getCADView( );
		CADView.addCADViewListener( this );
		
		ProfileInfo profile = browser.getProfile( "Immersive" );
		ComponentInfo component = browser.getComponent( "CADGeometry", 2 );
		scene = browser.createScene( profile, new ComponentInfo[]{ component } );
		
		browser.replaceWorld( scene );
	}
}

