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
import java.awt.EventQueue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Map;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ComponentInfo;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.ProfileInfo;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

import org.web3d.x3d.sai.environmentaleffects.*;
import org.web3d.x3d.sai.grouping.*;
import org.web3d.x3d.sai.geometry3d.*;
import org.web3d.x3d.sai.shape.*;

import org.xj3d.sai.Xj3DBrowser;

// Local imports
// none

/** See the box - emerge from the fog..... */
public class SAIFogTest extends JFrame implements ActionListener {
    
    Fog fog;
    boolean isShrinking;
    
    public SAIFogTest( ) {
        super( "SAIFogTest" );
        //
        System.setProperty( "x3d.sai.factory.class", 
            "org.xj3d.ui.awt.browser.ogl.X3DOGLBrowserFactoryImpl" );
        
        Container contentPane = this.getContentPane( );
        contentPane.setLayout( new BorderLayout( ) );
        //
        HashMap params = new HashMap( );
        params.put( "Xj3D_LocationShown", Boolean.FALSE );
        params.put( "Xj3D_OpenButtonShown", Boolean.FALSE );
        params.put( "Xj3D_ReloadButtonShown", Boolean.FALSE );
        X3DComponent component = BrowserFactory.createX3DComponent( params ); 
        ExternalBrowser browser = component.getBrowser( );
        ((Xj3DBrowser)browser).setMinimumFrameInterval(20);
        
        Map props = browser.getBrowserProperties( );
        System.out.println( props.get( "CONCRETE_NODES" ) );
        //
        ProfileInfo profile = browser.getProfile( "Immersive" );
        ComponentInfo effects4 = browser.getComponent( "EnvironmentalEffects", 4 );
        X3DScene scene = browser.createScene( profile, new ComponentInfo[]{ effects4 } ); 
        
        Group group = (Group)scene.createNode( "Group" );
        Shape shape = (Shape)scene.createNode( "Shape" );
        Box box = (Box)scene.createNode( "Box" );
        shape.setGeometry( box );
        Appearance appearance = (Appearance)scene.createNode( "Appearance" );
        Material material = (Material)scene.createNode( "Material" );
        material.setDiffuseColor( java.awt.Color.RED.getRGBComponents( null ) );
        appearance.setMaterial( material );
        shape.setAppearance( appearance );
        
        fog = (Fog)scene.createNode( "Fog" );
        fog.setColor( java.awt.Color.GRAY.getRGBComponents( null ) );
        
        Background background = (Background)scene.createNode( "Background" );
        background.setSkyColor( java.awt.Color.GRAY.getRGBComponents( null ) );
        background.setGroundColor( java.awt.Color.GRAY.getRGBComponents( null ) );
        String[] empty = new String[]{ "" };
        background.setBackUrl( empty );
        background.setFrontUrl( empty );
        background.setRightUrl( empty );
        background.setLeftUrl( empty );
        background.setTopUrl( empty );
        background.setBottomUrl( empty );
        
        group.setChildren( new X3DNode[]{ shape, fog, background } );
        fog.setBind( true );
        background.setBind( true );
        scene.addRootNode( group );
        
        browser.replaceWorld( scene );
        //
        contentPane.add( (Component)component, BorderLayout.CENTER );
        
        new Timer( 50, this ).start( );
    }
    
    public static void main( String[] args ) {
        SAIFogTest frame = new SAIFogTest( );
        frame.pack( );
        frame.setSize( 512, 512 );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setVisible( true );
    }
    
    public void actionPerformed( ActionEvent ae ) {
        float range = fog.getVisibilityRange( );
        if ( isShrinking ) {
            range -= 0.4f;
        } else {
            range += 0.4f;
        }
        if ( range >= 25.0f ) {
            isShrinking = true;
        } else if ( range < 0.05f ) {
            range = 0.05f;
            isShrinking = false;
        }
        fog.setVisibilityRange( range );
    }
}

