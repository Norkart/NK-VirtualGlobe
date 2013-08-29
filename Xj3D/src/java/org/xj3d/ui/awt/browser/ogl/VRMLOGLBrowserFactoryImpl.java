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

// Local imports
import vrml.eai.BrowserFactory;
import vrml.eai.BrowserFactoryImpl;
import vrml.eai.NotSupportedException;
import vrml.eai.VrmlComponent;
import vrml.eai.ConnectionException;
import vrml.eai.Browser;
import vrml.eai.NoSuchBrowserException;

import org.xj3d.sai.BrowserConfig;
import org.xj3d.sai.BrowserInterfaceTypes;

/**
 * An implementation of the EAI {@link vrml.eai.BrowserFactoryImpl} interface
 * that creates a browser that uses OpenGL for the renderer.
 * <p>
 *
 * <b>Supported Actions</b>
 * <p>
 *
 * Currently the factory implementation only supports creating a new component.
 * We do expect to add other support at a later date, such as fetching an
 * existing instance.
 * <p>
 *
 * <b>Component Creation</b>
 * <p>
 *
 * This implementation allows you to create a new component that is ready to
 * place content in. Parameters can be supplied and are declared in the form
 * <pre>
 *   <i>param name</i>=<i>param value</i>
 * </pre>
 * There should be no whitespace either side of the equals sign.
 * <p>
 *
 * When creating a new browser component, the following parameters are
 * supported:
 * <table BORDER CELLPADDING=2 WIDTH="100%">
 * <tr>
 *  <td><b>Parameter Name String</b></td>
 *  <td><b>Parameter Value String - Description</b></td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_InterfaceType</code></td>
 *  <td><code>awt</code>|<code>swing</code>|<code>swing-lightweight</code>.
 *      Indication as to whether the UI should be AWT or SWING based. The SWING
 *      option may also use a purely lightweight renderer that does not suffer
 *      from the usual heavyweight rendering problems with menus etc. However,
 *      If you are not using the OpenGL 2D pipeline in Java 6, then you're
 *      likely to have significant performance loss.If not supplied the default
 *      is to use the heavyweight swing renderer.</td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_NavbarShown</code></td>
 *  <td><code>true</code>|<code>false</code>. Show or hide the navigation
 *     bar on the screen. If not provided, the navigation bar will be shown.
 * </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_NavbarPosition</code></td>
 *  <td><code>top</code>|<code>bottom</code>. If the navigation bar is shown,
 *     it should be placed at the desired location in the panel. If not
 *     provided, the navigation bar will be on the bottom.</td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_LocationShown</code></td>
 *  <td><code>true</code>|<code>false</code>. Show or hide the panel that
 *     describes the current URL and allows the user to enter new URLs.
 *     If not provided, the location bar will be shown.</td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_LocationPosition</code></td>
 *  <td><code>top</code>|<code>bottom</code>. If the URL bar is shown,
 *     it should be placed at the desired location in the panel. If not
 *     provided, the position will be at the top of the panel.</td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_LocationReadOnly</code></td>
 *  <td><code>true</code>|<code>false</code>. If the URL bar is shown,
 *     you can make it read-only (ie not allow the user to change the URL).
 *     If not provided, the bar will be writable.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_ShowConsole</code></td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      component should automatically show the console on startup. Default
 *      is to hide.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_ShowConsole</code></td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      component should automatically show the console on startup. Default
 *      is to hide.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_OpenButtonShown</code></td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      component should have an Open button to load content.  Default is false.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_ReloadButtonShown</code></td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      component should have a Reload button to reload content.  Default is false.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_StatusBarShown</code></td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      component should have StatusBar.  Default is false.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_FPSShown</code></td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      StatusBar should have a frames-per-second display.  Default is false.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_ContentDirectory</code></td>
 *  <td>Initial directory to use for
 *      locating content. Default is obtained from the System property "user.dir".
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_AntialiasingQuality</code></td>
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
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      antialiasing should be enabled.  Default is false.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Shading</code></td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      shading should be enabled.  Default is false.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>PrimitiveQuality</code></td>
 *  <td>"low"|"medium"|"high". Primitive geometry quality. Default is "medium".
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>TextureQuality</code></td>
 *  <td>"low"|"medium"|"high". Texture quality. Default is "medium".
 *  </td>
 * </tr>
 * </table>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class VRMLOGLBrowserFactoryImpl implements BrowserFactoryImpl {

    /**
     * Create a new instance of this factory.
     */
    public VRMLOGLBrowserFactoryImpl() {
    }

    /**
     * Create a VRML browser that can be used as an AWT component. The component
     * returned is guaranteed to be an instance of VrmlComponent.
     *
     * @param paramList Parameters to control the look and feel.
     * @return The component browser initialised to be empty.
     * @exception NotSupportedException The implementation does not support this
     *    type of VRML browser.
     * @see VrmlComponent
     */
    public VrmlComponent createComponent(String[] paramList)
        throws NotSupportedException {

        BrowserConfig parameters = new BrowserConfig(paramList);
        parameters.vrml97Only = true;

        VrmlComponent ret_val = null;
        switch(parameters.interfaceType) {
            case LIGHTWEIGHT:
            case PARTIAL_LIGHTWEIGHT:
                ret_val = new VRMLBrowserJPanel(parameters);
                break;

            case HEAVYWEIGHT:
                ret_val = new VRMLBrowserAWTPanel(parameters);
                break;

            case OFFSCREEN:
// Not supported yet
//                ret_val = new X3DOffscreenSurface(parameters);
                break;

        }

        return ret_val;
    }

    /**
     * Get a browser from the given java applet reference as a base in the
     * current HTML page. Used when attempting to access a browser on the current
     * page as this applet and is the first browser on the page. Generically, the
     * same as calling getBrowser(applet, "", 0);
     *
     * @param applet The applet reference to use
     * @return A reference to the Browser implementation
     * @exception NotSupportedException The implementation does not support this
     *    type of VRML browser
     * @exception NoSuchBrowserException Could not locate a VRML browser on the
     *    same page as the applet.
     * @exception ConnectionException An error occurred during the connecting
     *    process
     */
    public Browser getBrowser(Applet applet)
        throws NotSupportedException, NoSuchBrowserException, ConnectionException {

        throw new NotSupportedException();
    }

    /**
     * Get a browser from the given java applet reference one some named page and
     * at some embbed location. Used when attempting to access a browser on
     * another HTML page within a multi-framed environment, or if there are a
     * number of VRML browser instances located on the same page.
     * <P>
     * If the frame name is a zero length string or null then it is assumed to be
     * located on the same HTML page as the applet. The index is the number of
     * the embbed VRML browser starting from the top of the page. If there are
     * other non-VRML plugins embedded in the page these are not taken into
     * account in calculating the embed index.
     *
     * @param applet The applet reference to use
     * @param frameName The name of the frame to look into for the browser
     * @param index The embed index of the VRML browser in the page
     * @return A reference to the Browser implementation
     * @exception NotSupportedException The implementation does not support this
     *    type of VRML browser.
     * @exception NoSuchBrowserException Could not locate a VRML browser on the
     *    same page as the applet.
     * @exception ConnectionException An error occurred during the connecting
     *    process
     */
    public Browser getBrowser(Applet applet, String frameName, int index)
        throws NotSupportedException,
               NoSuchBrowserException,
               ConnectionException {

        throw new NotSupportedException();
    }

    /**
     * Get a reference to a browser that is located on a remote machine. This
     * a server application to send scene updates to a number of client browsers
     * located on remote machines. If there are a number of browsers running on
     * a remote machine, they can be differentiated by the port number they are
     * listening on.
     * <P>
     * There is no default port number for VRML browsers.
     *
     * @param address The address of the machine to connect to
     * @param port The port number on that machine to connect to.
     * @return A reference to the Browser implementation
     * @exception NotSupportedException The implementation does not support this
     *    type of VRML browser.
     * @exception NoSuchBrowserException Could not locate a VRML browser on the
     *    same page as the applet.
     * @exception UnknownHostException Could not find the machine named in the
     *    address.
     * @exception ConnectionException An error occurred during the connecting
     *    process
     */
    public Browser getBrowser(InetAddress address, int port)
        throws NotSupportedException,
               NoSuchBrowserException,
               UnknownHostException,
               ConnectionException {

        throw new NotSupportedException();
    }
}
