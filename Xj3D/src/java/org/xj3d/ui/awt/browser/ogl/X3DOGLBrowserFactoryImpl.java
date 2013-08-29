/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.browser.ogl;

// External imports
import java.applet.Applet;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Map;

// Local imports
import org.web3d.x3d.sai.BrowserFactoryImpl;
import org.web3d.x3d.sai.ConnectionException;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.NoSuchBrowserException;
import org.web3d.x3d.sai.NotSupportedException;
import org.web3d.x3d.sai.X3DComponent;

import org.xj3d.sai.BrowserConfig;
import org.xj3d.sai.BrowserInterfaceTypes;

/**
 * Factory implementation for X3D SAI which will produce components using
 * the OpenGL renderer.
 * <p>
 *
 * This implementation allows you to create a new component that is ready to
 * place content in. Parameters can be supplied in the Map as defined by
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
 *  <td><code>Xj3D_InterfaceType</code></td>
 *  <td>String</td>
 *  <td><code>awt</code>|<code>swing</code>|<code>swing-lightweight</code>.
 *      Indication as to whether the UI should be AWT or SWING based. The SWING
 *      option may also use a purely lightweight renderer that does not suffer
 *      from the usual heavyweight rendering problems with menus etc. However,
 *      If you are not using the OpenGL 2D pipeline in Java 6, then you're
 *      likely to have significant performance loss.If not supplied the default
 *      is to use the heavyweight swing renderer.</td>
 * </tr>
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
 *  <td>"low"|"medium"|"high". Default is "low".
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
 * @author Bradley Vender
 * @version $Revision: 1.6 $
 */
public class X3DOGLBrowserFactoryImpl implements BrowserFactoryImpl {

    /**
     * Create a new instance of this factory.
     */
    public X3DOGLBrowserFactoryImpl() {
    }

    /**
     * @see org.web3d.x3d.sai.BrowserFactoryImpl#createComponent(java.util.Map)
     */
    public X3DComponent createComponent(Map params)
        throws NotSupportedException {

        BrowserConfig parameters = new BrowserConfig(params);

        X3DComponent ret_val = null;

        switch(parameters.interfaceType) {
            case LIGHTWEIGHT:
            case PARTIAL_LIGHTWEIGHT:
                ret_val = new X3DBrowserJPanel(parameters);
                break;

            case HEAVYWEIGHT:
                ret_val = new X3DBrowserAWTPanel(parameters);
                break;

            case OFFSCREEN:
                ret_val = new X3DOffscreenSurface(parameters);
                break;

        }

        return ret_val;
    }

    /**
     * @see org.web3d.x3d.sai.BrowserFactoryImpl#getBrowser(java.applet.Applet)
     */
    public ExternalBrowser getBrowser(Applet applet)
        throws NotSupportedException, NoSuchBrowserException, ConnectionException {
        throw new NotSupportedException();
    }

    /**
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
     * @see org.web3d.x3d.sai.BrowserFactoryImpl#getBrowser(java.net.InetAddress,
     *      int)
     */
    public ExternalBrowser getBrowser(InetAddress address, int port)
        throws
            NotSupportedException,
            NoSuchBrowserException,
            UnknownHostException,
            ConnectionException {
        throw new NotSupportedException();
    }
}
