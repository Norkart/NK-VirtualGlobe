/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.browser;

// Standard imports
import java.applet.Applet;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.ietf.uri.FileNameMap;
import org.ietf.uri.URI;

// Application specific imports
import org.web3d.x3d.sai.BrowserFactoryImpl;
import org.web3d.x3d.sai.NotSupportedException;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.ConnectionException;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.NoSuchBrowserException;

/**
 * An implementation of the SAI
 * {@link org.web3d.x3d.sai.BrowserFactoryImpl} interface
 * that creates a browser that delegates to the OpenGL and Java3D
 * specific browser factories.
 * <p>
 * This browser factory delegates to the other known browser factories based on
 * a hard coded search sequence and an optional user supplied renderer hint.
 *
 * @author Brad Vender
 * @version $Revision: 1.4 $
 */
public class X3DBrowserFactoryImpl implements BrowserFactoryImpl {

    /** Flag for preferring the OpenGL or Java3D renderer */
    private static final boolean PREFER_JAVA3D = false;

    /** The factory to use if OpenGL is requested */
    private static BrowserFactoryImpl openglFactory;

    /** The factory to use if Java3D is requested */
    private static BrowserFactoryImpl java3dFactory;

    /** The factory to use if no hint is supplied */
    private static BrowserFactoryImpl defaultFactory;

    /** The renderer hint */
    private static final String RENDERER_TYPE_PARAM = "Xj3D_RendererType";

    /** Error message for passing a null String in loadURL */
    private static final String NULL_PARAMETER_ERROR = "Null parameter strings not allowed.";

    /** Error message for malformed parameter String in loadURL */
    private static final String MALFORMED_PARAMETER_STRING_ERROR =
        "Malformed parameter string."+
        "  Expecting strings of the form A=B";

    /** Class name for OpenGL browser factory */
    private static final String OPENGL_FACTORY_CLASS =
        "org.xj3d.ui.awt.browser.ogl.X3DOGLBrowserFactoryImpl";

    /** Class name for Java3D browser factory */
    private static final String JAVA3D_FACTORY_CLASS =
        "org.xj3d.ui.awt.browser.j3d.X3DJ3DBrowserFactoryImpl";

    /** Locate the existing browser factories */
    static {
        try {
            openglFactory=(BrowserFactoryImpl) Class.forName(OPENGL_FACTORY_CLASS).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            java3dFactory=(BrowserFactoryImpl) Class.forName(JAVA3D_FACTORY_CLASS).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (PREFER_JAVA3D)
            if (java3dFactory!=null)
                defaultFactory=java3dFactory;
            else
                defaultFactory=openglFactory;
        else
            if (openglFactory!=null)
                defaultFactory=openglFactory;
            else
                defaultFactory=java3dFactory;
    }

    /**
     * Create a VRML browser that can be used as an AWT component. The component
     * returned is guaranteed to be an instance of X3DComponent.
     *
     * @param params Parameters to control the look and feel.
     * @return The component browser initialised to be empty.
     * @exception NotSupportedException The implementation does not support this
     *    type of VRML browser.
     * @see X3DComponent
     */
    public X3DComponent createComponent(Map params)
        throws NotSupportedException {

        Object obj;

        BrowserFactoryImpl selected=defaultFactory;

        if (params != null) {
            obj = params.get(RENDERER_TYPE_PARAM);
            if (obj != null && !(obj instanceof String))
                throw new IllegalArgumentException("createComponent." + RENDERER_TYPE_PARAM + " must be a String");
            if (obj != null) {
                String renderer = (String) obj;
                if (renderer.equalsIgnoreCase("opengl"))
                    selected=openglFactory;
                else if (renderer.equalsIgnoreCase("java3d"))
                    selected=java3dFactory;
            }
        }

        if (selected==null)
                selected=defaultFactory;
        return selected.createComponent(params);
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
    public ExternalBrowser getBrowser(Applet applet)
        throws NotSupportedException,
               NoSuchBrowserException,
               ConnectionException {
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
    public ExternalBrowser getBrowser(Applet applet, String frameName, int index)
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
    public ExternalBrowser getBrowser(InetAddress address, int port)
        throws NotSupportedException,
               NoSuchBrowserException,
               UnknownHostException,
               ConnectionException {
        throw new NotSupportedException();
    }
}
