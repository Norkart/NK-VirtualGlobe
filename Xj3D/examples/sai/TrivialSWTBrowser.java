/*****************************************************************************
 *                        Yumetech, Inc Copyright (c) 2001 - 2006
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

import org.eclipse.swt.layout.FillLayout;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.X3DComponent;

// Local imports
// none

/**
 * A very simple demo of an Xj3D browser instantiated via the SAI.
 * The browser is implemented with the SWT user interface toolkit
 * and uses an OpenGL renderer.
 * 
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class TrivialSWTBrowser {
    
    public static void main( String[] args ) {
        Display display = new Display( );
        Shell shell = new Shell( display );
        shell.setSize( 800, 600 );
        shell.setLayout( new FillLayout( SWT.HORIZONTAL ) );
        shell.setText( "SWT Browser" );
        
        HashMap params = new HashMap( );
        params.put( "Xj3D_SWT_Parent_Composite", shell );
        params.put( "Xj3D_NavbarShown", Boolean.TRUE );
        params.put( "Xj3D_StatusBarShown", Boolean.TRUE );
        params.put( "Xj3D_FPSShown", Boolean.TRUE );
        params.put( "Xj3D_NavbarPosition", "bottom" );
        params.put( "Xj3D_LocationShown", Boolean.TRUE );
        params.put( "Xj3D_LocationPosition", "top" );
        params.put( "Xj3D_ContentDirectory", System.getProperty( "user.dir" ) );
        params.put( "Xj3D_OpenButtonShown", Boolean.TRUE );
        params.put( "Xj3D_ReloadButtonShown", Boolean.TRUE );

        System.setProperty( "x3d.sai.factory.class", 
            "org.xj3d.ui.swt.browser.ogl.X3DSWTOGLBrowserFactoryImpl" );
        X3DComponent component = BrowserFactory.createX3DComponent( params );
        ExternalBrowser browser = component.getBrowser( );
        
        shell.open( );
        
        while ( !shell.isDisposed( ) ) {
            if ( !display.readAndDispatch( ) ) {
                display.sleep( );
            }
        }
        display.dispose( );
    }
}

