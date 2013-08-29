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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.ProfileInfo;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

import org.web3d.x3d.sai.grouping.*;
import org.web3d.x3d.sai.geometry3d.*;
import org.web3d.x3d.sai.interpolation.*;
import org.web3d.x3d.sai.lighting.*;
import org.web3d.x3d.sai.navigation.*;
import org.web3d.x3d.sai.shape.*;
import org.web3d.x3d.sai.time.*;

import org.xj3d.sai.Xj3DBrowser;

// Local imports
// none

/** watch the light move around the sphere */
public class SAIPositionInterpolatorTest extends JFrame {
    
    static final float[] POSITION_VALUE = new float[] {
        0, 0, 10,
        10, 0, 0,
        0, 0, -10,
        -10, 0, 0,
        0, 0, 10,
    };
       
    static final float[] KEY = new float[] {
        0,
        0.25f,
        0.50f,
        0.75f,
        1,
    };
      
    public SAIPositionInterpolatorTest( ) {
        super( "SAIPositionInterpolatorTest" );
        
        System.setProperty( "x3d.sai.factory.class", 
            "org.xj3d.ui.awt.browser.ogl.X3DOGLBrowserFactoryImpl" );
        
        Container contentPane = this.getContentPane( );
        contentPane.setLayout( new BorderLayout( ) );
        
        HashMap params = new HashMap( );
        params.put( "Xj3D_LocationShown", Boolean.FALSE );
        params.put( "Xj3D_OpenButtonShown", Boolean.FALSE );
        params.put( "Xj3D_ReloadButtonShown", Boolean.FALSE );
        
        X3DComponent component = BrowserFactory.createX3DComponent( params ); 
        ExternalBrowser browser = component.getBrowser( );
        ((Xj3DBrowser)browser).setMinimumFrameInterval(20);
        
        Map props = browser.getBrowserProperties( );
        if ( props.get( "CONCRETE_NODES" ).equals( Boolean.TRUE ) ) {
            
            ProfileInfo profile = browser.getProfile( "Immersive" );
            X3DScene scene = browser.createScene( profile, null ); 
            X3DNode[] rootNodes = scene.getRootNodes( );
            System.out.println( "Num root nodes = " + rootNodes.length );
            for ( int i = 0; i < rootNodes.length; i++ ) {
                System.out.println( rootNodes[i] );
            }
            
            Group group = (Group)scene.createNode( "Group" );
            Shape shape = (Shape)scene.createNode( "Shape" );
            
            Sphere sphere = (Sphere)scene.createNode( "Sphere" );
            
            shape.setGeometry( sphere );
            
            Appearance appearance = (Appearance)scene.createNode( "Appearance" );
            Material material = (Material)scene.createNode( "Material" );
            material.setDiffuseColor( new float[]{ 1, 1, 1 } );
            appearance.setMaterial( material );
            
            shape.setAppearance( appearance );
            
            TimeSensor timeSensor = (TimeSensor)scene.createNode( "TimeSensor" );
            timeSensor.setLoop( true );
            timeSensor.setCycleInterval( 10 );
            scene.addRootNode( timeSensor );
            
            PositionInterpolator positionInterpolator = 
                (PositionInterpolator)scene.createNode( "PositionInterpolator" );
            positionInterpolator.setKey( KEY );
            positionInterpolator.setKeyValue( POSITION_VALUE );
            scene.addRootNode( positionInterpolator );
            positionInterpolator.setFraction( 0 );
            
            PointLight pointLight = (PointLight)scene.createNode( "PointLight" );
            pointLight.setColor( new float[]{ 1, 1, 0 } );
            
            // apparently, the headlight won't go off, so this is pointless
            NavigationInfo navInfo = (NavigationInfo)scene.createNode( "NavigationInfo" );
            navInfo.setType( new String[]{ "EXAMINE" } );
            navInfo.setHeadlight( false );
            scene.addRootNode( navInfo );
            navInfo.setBind( true );
            
            scene.addRoute( timeSensor, "fraction_changed", positionInterpolator, "set_fraction" );
            scene.addRoute( positionInterpolator, "value_changed", pointLight, "location" );
            
            group.setChildren( new X3DNode[]{ shape, pointLight } );
            scene.addRootNode( group );
            
            browser.replaceWorld( scene );
            //
            contentPane.add( (Component)component, BorderLayout.CENTER );
            
        } else {
            System.out.println( "Concrete Node type interfaces not available!" );
            System.exit( 0 );
        }
    }
    
    public static void main( String[] args ) {
        SAIPositionInterpolatorTest frame = new SAIPositionInterpolatorTest( );
        frame.pack( );
        frame.setSize( 512, 512 );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setVisible( true );
    }
}
