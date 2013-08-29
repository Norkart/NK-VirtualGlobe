/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.ui.swt.browser.ogl;

// External imports
import java.applet.Applet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.part.ViewPart;

// Local imports
import org.web3d.x3d.sai.BrowserFactoryImpl;
import org.web3d.x3d.sai.ConnectionException;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.NoSuchBrowserException;
import org.web3d.x3d.sai.NotSupportedException;
import org.web3d.x3d.sai.X3DComponent;

import org.xj3d.sai.BrowserConfig;

/**
 * Factory implementation for X3D SAI which will produce components using
 * the OpenGL renderer.
 * <p>
 * This implementation allows you to create a new component that is ready to
 * place content in. Parameters are supplied in the Map as defined by
 * the SAI.
 * <p>
 *
 * When creating a new browser component, the following parameters are
 * supported:
 * <table BORDER CELLPADDING=2 WIDTH="100%">
 * <tr>
 *  <td><b>Parameter Name String</b></td>
 *  <td><b>Data Type</b></td>
 *  <td><b>Description</b></td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_SWT_Parent_Composite</code></td>
 *  <td>Composite</td>
 *  <td>Required. The Composite object that will be the parent of the browser.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_Eclipse_View</code></td>
 *  <td>ViewPart</td>
 *  <td>Optional. The ViewPart that the browser instance will be associated with.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_NavbarShown</code></td>
 *  <td>Boolean</td>
 *  <td><code>true</code>|<code>false</code>. Show or hide the navigation
 *     bar on the screen. If not provided, the navigation bar will be shown.
 * </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_NavbarPosition</code></td>
 *  <td>String</td>
 *  <td><code>top</code>|<code>bottom</code>. If the navigation bar is shown,
 *     it should be placed at the desired location in the panel. If not
 *     provided, the navigation bar will be on the bottom.</td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_LocationShown</code></td>
 *  <td>Boolean</td>
 *  <td><code>true</code>|<code>false</code>. Show or hide the panel that
 *     describes the current URL and allows the user to enter new URLs.
 *     If not provided, the location bar will be shown.</td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_LocationPosition</code></td>
 *  <td>String</td>
 *  <td><code>top</code>|<code>bottom</code>. If the URL bar is shown,
 *     it should be placed at the desired location in the panel. If not
 *     provided, the position will be at the top of the panel.</td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_LocationReadOnly</code></td>
 *  <td>Boolean</td>
 *  <td><code>true</code>|<code>false</code>. If the URL bar is shown,
 *     you can make it read-only (ie not allow the user to change the URL).
 *     If not provided, the bar will be writable.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_ShowConsole</code></td>
 *  <td>Boolean</td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      component should automatically show the console on startup. Default
 *      is to hide.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_OpenButtonShown</code></td>
 *  <td>Boolean</td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      component should have an Open button to load content.  Default is false.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_ReloadButtonShown</code></td>
 *  <td>Boolean</td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      component should have a Reload button to reload content.  Default is false.
 *  </td>
 * </tr> 
 * <tr>
 *  <td><code>Xj3D_StatusBarShown</code></td>
 *  <td>Boolean</td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      component should have StatusBar.  Default is false.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_FPSShown</code></td>
 *  <td>Boolean</td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      StatusBar should have a frames-per-second display.  Default is false.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_ContentDirectory</code></td>
 *  <td>String</td>
 *  <td>Initial directory to use for
 *      locating content. Default is obtained from the System property "user.dir".
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_AntialiasingQuality</code></td>
 *  <td>String</td>
 *  <td>"low"|"medium"|"high". Antialiasing Quality Default is "low".
 *  </td>
 * </tr>   
 * <tr>
 *  <td><code>Xj3D_Skin_Properties</code></td>
 *  <td>Properties</td>
 *  <td>Optional. If provided, the factory will use as the default skin
 *     properties file. Otherwise, the factory will search the class path
 *     for a properties file "xj3d-skin.properties".
 *  </td>
 * </tr>  
 * <tr>
 *  <td><code>Xj3D_Skin_Resources</code></td>
 *  <td>Map</td>
 *  <td>Optional. If provided, the Map will be used to initialize the image cache
 *     with the resources provided to be used as the skin. Otherwise, the resources
 *     will be searched for in the class path.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_Culling_Mode</code></td>
 *  <td>String</td>
 *  <td>"none"|"frustum". OGL Culling mode. Default is "frustum".
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Antialiased</code></td>
 *  <td>Boolean</td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      antialiasing should be enabled.  Default is false.
 *  </td>
 * </tr> 
 * <tr>
 *  <td><code>Shading</code></td>
 *  <td>Boolean</td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      shading should be enabled.  Default is false.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>PrimitiveQuality</code></td>
 *  <td>String</td>
 *  <td>"low"|"medium"|"high". Primitive geometry quality. Default is "medium".
 *  </td>
 * </tr> 
 * <tr>
 *  <td><code>TextureQuality</code></td>
 *  <td>String</td>
 *  <td>"low"|"medium"|"high". Texture quality. Default is "medium".
 *  </td>
 * </tr> 
 * </table>
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class X3DSWTOGLBrowserFactoryImpl implements BrowserFactoryImpl {
	
	// Parameters unique to SWT/Eclipse impl
	private static final String SWT_PARENT_COMPOSITE_PARAM = "Xj3D_SWT_Parent_Composite";
	private static final String ECLIPSE_VIEW_PARAM = "Xj3D_Eclipse_View";
	
	/** 
	 * Create a new instance of this factory.
	 */
	public X3DSWTOGLBrowserFactoryImpl( ) {
	}
	
    //-----------------------------------------------------------------------
    // Methods defined by BrowserFactoryImpl
    //-----------------------------------------------------------------------

	/**
     * Create an X3D browser as an SWT widget. The component
     * returned is guaranteed to be an instance of X3DComponent.
     *
     * @param params Parameters to control the look and feel.
     * @return The component browser initialised to be empty.
     * @exception NotSupportedException The implementation does not support this
     *    type of browser.
     * @see X3DComponent
     */
	public X3DComponent createComponent( Map params ) throws NotSupportedException {
		
		Composite parentComposite = null;
		ViewPart viewPart = null;
		
		if ( params != null ) {
			
			Object obj = params.get( SWT_PARENT_COMPOSITE_PARAM );
			if ( obj == null ) {
				throw new IllegalArgumentException("createComponent." + SWT_PARENT_COMPOSITE_PARAM + 
					" is a required parameter");
			}
			else if ( !( obj instanceof Composite ) ) {
				throw new IllegalArgumentException("createComponent." + SWT_PARENT_COMPOSITE_PARAM + 
					" must be a Composite");
			}
			else {
				parentComposite = (Composite)obj;
			}
			
			obj = params.get( ECLIPSE_VIEW_PARAM );
			if ( obj != null && !( obj instanceof ViewPart ) ) {
				throw new IllegalArgumentException(
					"createComponent." + ECLIPSE_VIEW_PARAM + " must be a ViewPart object");
			}
			else {
				viewPart = (ViewPart)obj;
			}
		}
		else {
			throw new IllegalArgumentException("createComponent." + SWT_PARENT_COMPOSITE_PARAM + 
				" is a required parameter");
		}
		
		BrowserConfig parameters = new BrowserConfig( params );
		
		return (
			new X3DBrowserComposite(
			parentComposite,
			viewPart,
			parameters ) );
	}
	
	/**
	 * Not Implemented.
	 * @see org.web3d.x3d.sai.BrowserFactoryImpl#getBrowser(java.applet.Applet)
	 */
	public ExternalBrowser getBrowser(Applet applet)
		throws NotSupportedException, NoSuchBrowserException, ConnectionException {
		throw new NotSupportedException();
	}
	
	/**
	 * Not Implemented.
	 * @see org.web3d.x3d.sai.BrowserFactoryImpl#getBrowser(java.applet.Applet,
	 *      java.lang.String, int)
	 */
	public ExternalBrowser getBrowser(
		Applet applet,
		String frameName,
		int index)
		throws NotSupportedException, NoSuchBrowserException, ConnectionException {
		throw new NotSupportedException();
	}
	
	/**
	 * Not Implemented.
	 * @see org.web3d.x3d.sai.BrowserFactoryImpl#getBrowser(java.net.InetAddress,
	 *      int)
	 */
	public ExternalBrowser getBrowser(InetAddress address, int port)
		throws NotSupportedException, NoSuchBrowserException, UnknownHostException, ConnectionException {
		throw new NotSupportedException();
	}
}
