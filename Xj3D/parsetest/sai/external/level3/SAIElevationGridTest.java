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
//import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.ProfileInfo;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

import org.web3d.x3d.sai.grouping.*;
import org.web3d.x3d.sai.geometry3d.*;
import org.web3d.x3d.sai.rendering.*;
import org.web3d.x3d.sai.shape.*;

import org.xj3d.sai.Xj3DBrowser;

// Local imports
// none

/** Test of constructing and an indexed line set using the SAI */
public class SAIElevationGridTest extends JFrame implements ActionListener {
    
    // a pyramid, center height of 2
    float[] height = new float[]{
        0, 0, 0,
        0, 2, 0,
        0, 0, 0,
    };
    
    // red with a blue center
    float[] color_1 = new float[]{
        1, 0, 0, 1, 0, 0, 1, 0, 0,
        1, 0, 0, 0, 0, 1, 1, 0, 0,
        1, 0, 0, 1, 0, 0, 1, 0, 0,
    };
    
    // green with a red center
    float[] color_2 = new float[]{
        0, 1, 0, 0, 1, 0, 0, 1, 0,
        0, 1, 0, 1, 0, 0, 0, 1, 0,
        0, 1, 0, 0, 1, 0, 0, 1, 0,
    };
    
    boolean increase;
    
    ElevationGrid grid;
    Color color;
    
    public SAIElevationGridTest( ) {
        super( "SAIElevationGridTest" );
        
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
            
            Transform transform = (Transform)scene.createNode( "Transform" );
            transform.setTranslation( new float[]{ -3, 0, -3 } );
            
            Shape shape = (Shape)scene.createNode( "Shape" );
            
            grid = (ElevationGrid)scene.createNode( "ElevationGrid" );
            grid.setXDimension( 3 );
            grid.setZDimension( 3 );
            grid.setXSpacing( 3 );
            grid.setZSpacing( 3 );
            grid.setHeight( height );
            
            grid.setSolid( false );
            
            color = (Color)scene.createNode( "Color" );
            color.setColor( color_1 );
            
            grid.setColor( color );
            
            shape.setGeometry( grid );
            
            Appearance appearance = (Appearance)scene.createNode( "Appearance" );
            Material material = (Material)scene.createNode( "Material" );
            //material.setEmissiveColor( java.awt.Color.WHITE.getRGBComponents( null ) );
            material.setEmissiveColor( new float[]{ 0.2f, 0.2f, 0.2f } );
            appearance.setMaterial( material );
            
            shape.setAppearance( appearance );
            transform.setChildren( new X3DNode[]{ shape } );
            scene.addRootNode( transform );
            
            browser.replaceWorld( scene );
            //
            contentPane.add( (Component)component, BorderLayout.CENTER );
            
            new Timer( 50, this ).start( );
            
        } else {
            System.out.println( "Concrete Node type interfaces not available!" );
            System.exit( 0 );
        }
    }
    //
    public static void main( String[] args ) {
        SAIElevationGridTest frame = new SAIElevationGridTest( );
        frame.pack( );
        frame.setSize( 512, 512 );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setVisible( true );
    }
    
    /** animate the center peak of the grid between a height of 2 and -2, 
     *  switching color when it reaches its max or min. */
    public void actionPerformed( ActionEvent ae ) {
        float center = height[4];
        if ( increase ) {
            center += 0.05f;
        } else {
            center -= 0.05f;
        }
        if ( center > 2.0f ) {
            center = 2.0f;
            increase = false;
            color.setColor( color_1 );
        } else if ( center < -2.0f ) {
            center = -2.0f;
            increase = true;
            color.setColor( color_2 );
        }
        height[4] = center;
        grid.setHeight( height );
    }
}
