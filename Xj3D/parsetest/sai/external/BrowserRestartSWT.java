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
import java.util.HashMap;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

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
 * browser instance using the SWT UI toolkit. 
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class BrowserRestartSWT implements BrowserListener, SelectionListener {
    
    Shell shell;
    Button button;
    
    X3DComponent x3dComponent;
    ExternalBrowser browser;
    
    public BrowserRestartSWT( ) {
        
        System.setProperty( "x3d.sai.factory.class", 
            "org.xj3d.ui.swt.browser.ogl.X3DSWTOGLBrowserFactoryImpl" );
        
        Display display = new Display( );
        shell = new Shell( display );
        shell.setSize( 512, 512 );
        shell.setLayout( new FormLayout( ) );
        shell.setText( "SWT Xj3DBrowser" );
        
        button = new Button ( shell, SWT.PUSH );
        button.setText( "Add Browser" );
        button.addSelectionListener( this );
        
        FormData formData = new FormData( );
        formData.bottom = new FormAttachment( 100, 0 );
        formData.left = new FormAttachment ( 0, 0 );
        formData.right = new FormAttachment ( 100, 0 );
        button.setLayoutData( formData );
        
        shell.open( );
        
        while ( !shell.isDisposed( ) ) {
            if ( !display.readAndDispatch( ) ) {
                display.sleep( );
            }
        }
        display.dispose( );
    }
    
    //---------------------------------------------------------
    // Methods defined by SelectionListener
    //---------------------------------------------------------
    
    public void widgetSelected( SelectionEvent se ) {
        
        if ( browser != null ) {
            browser.dispose( );
            x3dComponent.shutdown( );
            ((Composite)x3dComponent).dispose( );
            browser = null;
            x3dComponent = null;
            // inform the garbage collector that now would be a good time
            System.gc( );
            
            button.setText( "Add Browser" );
        }
        else {
            addBrowser( );
            shell.layout( );
            addSomething( );
            
            button.setText( "Remove Browser" );
        }
    }
    
    public void widgetDefaultSelected( SelectionEvent se ) {
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
        BrowserRestartSWT test = new BrowserRestartSWT( );
        System.exit( 0 );
    }
    
    private void addBrowser( ) {
        HashMap params = new HashMap( );
        params.put( "Xj3D_SWT_Parent_Composite", shell );
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
        FormData formData = new FormData( );
        formData.bottom = new FormAttachment( button, 0 );
        formData.left = new FormAttachment ( 0, 0 );
        formData.right = new FormAttachment ( 100, 0 );
        formData.top = new FormAttachment( 0, 0 );
        ((Composite)x3dComponent).setLayoutData( formData );
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

