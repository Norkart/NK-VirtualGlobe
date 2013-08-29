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
import org.web3d.x3d.sai.rendering.*;
import org.web3d.x3d.sai.shape.*;

import org.xj3d.sai.Xj3DBrowser;

// Local imports
// none

/** Test of constructing and an indexed line set using the SAI */
public class SAIIndexedFaceSetTest extends JFrame {
    
    public SAIIndexedFaceSetTest( ) {
        super( "SAIIndexedFaceSetTest" );
        
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
            IndexedFaceSet ifs = (IndexedFaceSet)scene.createNode( "IndexedFaceSet" );
            Coordinate coord = (Coordinate)scene.createNode( "Coordinate" );
            coord.setPoint( new float[]{ 
                    1, 1, 1, 
                    1, 1, -1, 
                    -1, 1, -1, 
                    -1, 1, 1,
                    1, -1, 1, 
                    1, -1, -1, 
                    -1, -1, -1, 
                    -1, -1, 1, 
                } );
            ifs.setCoord( coord );
            ifs.setCoordIndex( new int[]{ 
                    0, 1, 2, 3, -1,  // #top > +y
                    7, 6, 5, 4, -1,  // #bottom > -y
                    0, 4, 5, 1, -1,  // #right > +x
                    3, 2, 6, 7, -1,  // #left > -x
                    0, 3, 7, 4, -1,  // #front > +z
                    1, 5, 6, 2, -1,   // #back > -z
                } );
            ifs.setCcw( true );
            shape.setGeometry( ifs );
            
            Appearance appearance = (Appearance)scene.createNode( "Appearance" );
            Material material = (Material)scene.createNode( "Material" );
            material.setEmissiveColor( java.awt.Color.RED.getRGBComponents( null ) );
            appearance.setMaterial( material );
            
            shape.setAppearance( appearance );
            group.setChildren( new X3DNode[]{ shape } );
            scene.addRootNode( group );
            
            browser.replaceWorld( scene );
            //
            contentPane.add( (Component)component, BorderLayout.CENTER );
            
        } else {
            System.out.println( "Concrete Node type interfaces not available!" );
            System.exit( 0 );
        }
    }
    //
    public static void main( String[] args ) {
        SAIIndexedFaceSetTest frame = new SAIIndexedFaceSetTest( );
        frame.pack( );
        frame.setSize( 512, 512 );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setVisible( true );
    }
}
