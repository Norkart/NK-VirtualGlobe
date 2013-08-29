package org.xj3d.browser.sai_app;

import java.util.HashMap;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

import org.eclipse.swt.layout.FillLayout;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.part.ViewPart;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.ProfileInfo;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFColor;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

import org.xj3d.sai.Xj3DBrowser;

public class View extends ViewPart implements DisposeListener {
	public static final String ID = "org.xj3d.browser.sai_app.view";

	/** The X3D component, a subclass of Composite */
	private X3DComponent x3dComponent;

	/** The Browser object */
	private ExternalBrowser browser;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout( new FillLayout( ) );
        parent.addDisposeListener( this );

        HashMap params = new HashMap( );
        // hard coded defaults for now....
        params.put( "Xj3D_SWT_Parent_Composite", parent );
        params.put( "Xj3D_Eclipse_View", this );
        params.put( "Xj3D_NavbarShown", Boolean.FALSE );
        params.put( "Xj3D_LocationShown", Boolean.FALSE );

        System.setProperty( "x3d.sai.factory.class",
            "org.xj3d.ui.swt.browser.ogl.X3DSWTOGLBrowserFactoryImpl" );

        x3dComponent = BrowserFactory.createX3DComponent( params );
        browser = x3dComponent.getBrowser( );
        // throttle the frame rate to a max of 50fps
        ((Xj3DBrowser)browser).setMinimumFrameInterval( 20 );
        
        ProfileInfo profile = browser.getProfile( "Immersive" );
        X3DScene scene = browser.createScene( profile, null );
        
        // put the obligatory red box into the scene
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
        geometry.setValue( box );
        
        X3DNode navInfo = scene.createNode("NavigationInfo");
        MFString type = (MFString)navInfo.getField( "type" );
        type.setValue( 1, new String[]{ "EXAMINE" } );
        SFBool bind = (SFBool)navInfo.getField( "set_bind" );
        scene.addRootNode( navInfo );
        bind.setValue( true );

        addChildren.setValue( 1, new X3DNode[]{ shape } );
        
        browser.replaceWorld( scene );
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}
	
    /**
     * We're being disposed of. Shutdown and dispose of the
     * browser and it's ui components.
     */
    public void widgetDisposed( DisposeEvent evt )
    {
        browser.dispose( );
        x3dComponent.shutdown( );
        ((Composite)x3dComponent).dispose( );

        browser = null;
        x3dComponent = null;
    }
}