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
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

import org.web3d.x3d.sai.grouping.*;
import org.web3d.x3d.sai.geometry3d.*;
import org.web3d.x3d.sai.pointingdevicesensor.*;
import org.web3d.x3d.sai.shape.*;

import org.xj3d.sai.Xj3DBrowser;

// Local imports
// none

/** touch the box - see it change color */
public class SAITouchSensorTest extends JFrame implements X3DFieldEventListener {
    
    TouchSensor touchSensor;
    Material material;
    
    float[][] color = new float[][] {
        { 1, 0, 0 },
        { 0, 1, 0 },
        { 0, 0, 1 },
    };
    
    int color_index = 0;
    
    public SAITouchSensorTest( ) {
        super( "SAITouchSensorTest" );
        
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
            
            Group group = (Group)scene.createNode( "Group" );
            Shape shape = (Shape)scene.createNode( "Shape" );
            
            Sphere sphere = (Sphere)scene.createNode( "Sphere" );
            shape.setGeometry( sphere );
            
            Appearance appearance = (Appearance)scene.createNode( "Appearance" );
            material = (Material)scene.createNode( "Material" );
            material.setDiffuseColor( new float[]{ 0.5f, 0.5f, 0.5f } );
            material.setEmissiveColor( color[color_index] );
            appearance.setMaterial( material );
            
            shape.setAppearance( appearance );
            
            touchSensor = (TouchSensor)scene.createNode( "TouchSensor" );
            touchSensor.getField( "isActive" ).addX3DEventListener( this );
            
            group.setChildren( new X3DNode[]{ shape, touchSensor } );
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
        SAITouchSensorTest frame = new SAITouchSensorTest( );
        frame.pack( );
        frame.setSize( 512, 512 );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setVisible( true );
    }
    
    public void readableFieldChanged( X3DFieldEvent xfe ) {
        if ( touchSensor.getIsActive( ) ) {
            if ( ++color_index > 2 ) {
                color_index = 0;
            }
            material.setEmissiveColor( color[color_index] );
        }
    }
}
