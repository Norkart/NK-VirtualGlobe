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

package org.xj3d.ui.awt.browser.j3d;

// External imports
import java.applet.Applet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.HashSet;

// Local imports
import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.BrowserFactoryImpl;
import org.web3d.x3d.sai.NotSupportedException;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.ConnectionException;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.NoSuchBrowserException;

/**
 * An implementation of the SAI
 * {@link org.web3d.x3d.sai.BrowserFactoryImpl} interface
 * that creates a browser that uses Java3D for the renderer.
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
 * place content in. Parameters are supplied in a map of String parameter names
 * to primitive wrapper class values, i.e. BooleanValue.TRUE instead of "TRUE".
 * <p>
 *
 * When creating a new browser component, the following parameters are
 * supported:
 * <table>
 * <tr>
 *  <td><code>Xj3D_InterfaceType</code></td>
 *  <td><code>awt</code>|<code>swing</code>. Indication as to whether the UI
 *      should be AWT or SWING based. If not supplied the default is to use
 *      SWING.</td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_NavbarShown</code></td>
 *  <td><code>Boolean.TRUE</code>|<code>Boolean.FALSE</code>. Show or hide the navigation
 *     bar on the screen. If not provided, the navigation bar will be shown.
 * </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_NavbarPosition</code></td>
 *  <td><code>"top"</code>|<code>"bottom"</code>. If the navigation bar is shown,
 *     it should be placed at the desired location in the panel. If not
 *     provided, the navigation bar will be on the bottom.</td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_LocationShown</code></td>
 *  <td><code>Boolean.TRUE</code>|<code>Boolean.FALSE</code>. Show or hide the panel that
 *     describes the current URL and allows the user to enter new URLs.
 *     If not provided, the location bar will be shown.</td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_LocationPosition</code></td>
 *  <td><code>"top"</code>|<code>"bottom"</code>. If the URL bar is shown,
 *     it should be placed at the desired location in the panel. If not
 *     provided, the position will be at the top of the panel.</td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_LocationReadOnly</code></td>
 *  <td><code>Boolean.TRUE</code>|<code>Boolean.FALSE</code>. If the URL bar is shown,
 *     you can make it read-only (ie not allow the user to change the URL).
 *     If not provided, the bar will be writable.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>Xj3D_ShowConsole</code></td>
 *  <td><code>Boolean.TRUE</code>|<code>Boolean.FALSE</code>. Indication as to whether the
 *      component should automatically show the console on startup. Default
 *      is to hide.
 *  </td>
 *  <td><code>Xj3D_ShowOpenButton</code></td>
 *  <td>Boolean</td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      component should have an Open button to load content.  Default is false.
 *  </td>
 *  <td><code>Xj3D_ShowReloadButton</code></td>
 *  <td>Boolean</td>
 *  <td><code>true</code>|<code>false</code>. Indication as to whether the
 *      component should have a Reload button to reload content.  Default is false.
 *  </td>
 * </tr>
 * </table>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class X3DJ3DBrowserFactoryImpl implements BrowserFactoryImpl {

    /** Error message for passing a null String in loadURL */
    private static final String NULL_PARAMETER_ERROR = "Null parameter strings not allowed.";

    /** Error message for malformed parameter String in loadURL */
    private static final String MALFORMED_PARAMETER_STRING_ERROR =
        "Malformed parameter string."+
        "  Expecting strings of the form A=B";

    /** Property file defining the appearance of the browser */
    private static final String SKIN_PROPERTY_FILE="xj3d-skin.properties";

    // Parameter names. Kept private because we don't want people directly
    // accessing this class in their code because that would stuff portability.
    // TODO These values were copies from X3DJ3DBrowserFactoryImpl. Is that
    // okay?
    private static final String SWING_PARAM = "Xj3D_InterfaceType";
    private static final String NAV_SHOW_PARAM = "Xj3D_NavbarShown";
    private static final String NAV_POS_PARAM = "Xj3D_NavbarPosition";
    private static final String LOC_SHOW_PARAM = "Xj3D_LocationShown";
    private static final String LOC_POS_PARAM = "Xj3D_LocationPosition";
    private static final String LOC_READONLY_PARAM = "Xj3D_LocationReadOnly";
    private static final String SHOW_CONSOLE_PARAM = "Xj3D_ShowConsole";
    private static final String OPEN_BUTTON_SHOW_PARAM = "Xj3D_OpenButtonShown";
    private static final String RELOAD_BUTTON_SHOW_PARAM = "Xj3D_ReloadButtonShown";
    private static final String STATUS_BAR_SHOW_PARAM = "Xj3D_StatusBarShown";
    private static final String FPS_SHOW_PARAM = "Xj3D_FPSShown";
    private static final String CONTENT_DIRECTORY_PARAM = "Xj3D_ContentDirectory";
    private static final String ANTIALIASED_PARAM = "Antialiased";
    private static final String PRIMITIVE_QUALITY_PARAM = "PrimitiveQuality";
    private static final String SHADING_PARAM = "Shading";
    private static final String TEXTURE_QUALITY_PARAM = "TextureQuality";
    private static final String ANTIALIASING_QUALITY_PARAM = "Xj3D_AntialiasingQuality";

    /** Properties object defining appearance of browser panel */
    private Properties browserSkin;

    /** Set of quality params */
    private static HashSet qualitySet;

    /**
     * Initialize the quality set
     */
    static {
        qualitySet = new HashSet(3);
        qualitySet.add("low");
        qualitySet.add("medium");
        qualitySet.add("high");
    }

    /** Create a new instance of this factory.
     *  Fills in the browserSkin field using loadBrowserSkin.
     */
    public X3DJ3DBrowserFactoryImpl() {
        loadBrowserSkin();
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

        boolean use_swing = true;
        boolean show_nav = true;
        boolean show_loc = true;
        boolean nav_top = false;
        boolean loc_top = true;
        boolean loc_readonly = false;
        boolean show_console = false;
        boolean show_open_button = false;
        boolean show_reload_button = false;
        boolean show_status_bar = false;
        boolean show_fps = false;
        String content_directory = null;
        boolean antialiased = false;
        String primitive_quality = "medium";
        String texture_quality = "medium";
        String antialiasing_quality = "low";

        Object obj;

        if (params != null) {
            obj = params.get(SWING_PARAM);
            if (obj != null && !(obj instanceof String))
                throw new IllegalArgumentException("createComponent." + SWING_PARAM + " must be a String");

            String val = (String)obj;
            if(val != null)
                use_swing = !val.equalsIgnoreCase("awt");

            obj = params.get(NAV_SHOW_PARAM);
            if (obj != null && !(obj instanceof Boolean))
                throw new IllegalArgumentException("createComponent." + NAV_SHOW_PARAM + " must be a Boolean");

            Boolean booleanVal = (Boolean)obj;
            if(booleanVal != null)
                show_nav = booleanVal.booleanValue();

            obj = params.get(NAV_POS_PARAM);
            if (obj != null && !(obj instanceof String))
                throw new IllegalArgumentException("createComponent." + NAV_POS_PARAM + " must be a String");

            val = (String)obj;
            if(val != null)
                nav_top = val.equalsIgnoreCase("top");

            obj = params.get(LOC_SHOW_PARAM);
            if (obj != null && !(obj instanceof Boolean))
                throw new IllegalArgumentException("createComponent." + LOC_SHOW_PARAM + " must be a Boolean");

            booleanVal = (Boolean)obj;
            if(booleanVal != null)
                show_loc = booleanVal.booleanValue();

            obj = params.get(LOC_POS_PARAM);
            if (obj != null && !(obj instanceof String))
                throw new IllegalArgumentException("createComponent." + LOC_POS_PARAM + " must be a String");

            val = (String)obj;
            if(val != null)
                loc_top = val.equalsIgnoreCase("top");

            obj = params.get(LOC_READONLY_PARAM);
            if (obj != null && !(obj instanceof Boolean))
                throw new IllegalArgumentException("createComponent." + LOC_READONLY_PARAM + " must be a Boolean");

            booleanVal = (Boolean)obj;
            if(booleanVal != null)
                loc_readonly = booleanVal.booleanValue();

            obj = params.get(SHOW_CONSOLE_PARAM);
            if (obj != null && !(obj instanceof Boolean))
                throw new IllegalArgumentException("createComponent." + SHOW_CONSOLE_PARAM + " must be a Boolean");

            booleanVal = (Boolean)obj;
            if(booleanVal != null)
                show_console = booleanVal.booleanValue();

            obj = params.get(OPEN_BUTTON_SHOW_PARAM);

            if (obj != null && !(obj instanceof Boolean))
                throw new IllegalArgumentException("createComponent." + OPEN_BUTTON_SHOW_PARAM + " must be a Boolean");

            booleanVal = (Boolean)obj;
            if(booleanVal != null)
                show_open_button = booleanVal.booleanValue();

            obj = params.get(RELOAD_BUTTON_SHOW_PARAM);
            if (obj != null && !(obj instanceof Boolean))
                throw new IllegalArgumentException("createComponent." + RELOAD_BUTTON_SHOW_PARAM + " must be a Boolean");

            booleanVal = (Boolean)obj;
            if(booleanVal != null)
                show_reload_button = booleanVal.booleanValue();

            obj = params.get(STATUS_BAR_SHOW_PARAM);
            if (obj != null && !(obj instanceof Boolean))
                throw new IllegalArgumentException("createComponent." + STATUS_BAR_SHOW_PARAM + " must be a Boolean");

            booleanVal = (Boolean)obj;
            if(booleanVal != null)
                show_status_bar = booleanVal.booleanValue();

            obj = params.get(FPS_SHOW_PARAM);
            if (obj != null && !(obj instanceof Boolean))
                throw new IllegalArgumentException("createComponent." + FPS_SHOW_PARAM + " must be a Boolean");

            booleanVal = (Boolean)obj;
            if(booleanVal != null)
                show_fps = booleanVal.booleanValue();

            obj = params.get(CONTENT_DIRECTORY_PARAM);
            if (obj != null && !(obj instanceof String))
                throw new IllegalArgumentException("createComponent." + CONTENT_DIRECTORY_PARAM + " must be a String");

            val = (String)obj;
            content_directory = (String)obj;

            obj = params.get(ANTIALIASED_PARAM);
            if (obj != null && !(obj instanceof Boolean))
                throw new IllegalArgumentException("createComponent." + ANTIALIASED_PARAM + " must be a Boolean");

            booleanVal = (Boolean)obj;
            if(booleanVal != null)
                antialiased = booleanVal.booleanValue();

            obj = params.get(PRIMITIVE_QUALITY_PARAM);
            if (obj != null && !(obj instanceof String))
                throw new IllegalArgumentException("createComponent." + PRIMITIVE_QUALITY_PARAM + " must be a String");

            val = (String)obj;
            if (val != null)
                primitive_quality = (String)obj;

            if (primitive_quality != null && !qualitySet.contains(primitive_quality))
                throw new IllegalArgumentException("createComponent." + PRIMITIVE_QUALITY_PARAM + " must low, medium or high");

            obj = params.get(TEXTURE_QUALITY_PARAM);
            if (obj != null && !(obj instanceof String))
                throw new IllegalArgumentException("createComponent." + TEXTURE_QUALITY_PARAM + " must be a String");

            val = (String)obj;
            if (val != null)
                texture_quality = (String)obj;

            if (!qualitySet.contains(texture_quality))
                throw new IllegalArgumentException("createComponent." + TEXTURE_QUALITY_PARAM + " must low, medium or high");

            obj = params.get(ANTIALIASING_QUALITY_PARAM);
            if (obj != null && !(obj instanceof String))
                throw new IllegalArgumentException("createComponent." + ANTIALIASING_QUALITY_PARAM + " must be a String");

            val = (String)obj;
            if (val != null)
                antialiasing_quality = (String)obj;
            if (!qualitySet.contains(antialiasing_quality))
                throw new IllegalArgumentException("createComponent." + ANTIALIASING_QUALITY_PARAM + " must low, medium or high");

        }

        X3DComponent ret_val = null;

        if(use_swing)
            ret_val = new X3DBrowserJPanel(false,
                                        show_nav,
                                        nav_top,
                                        show_loc,
                                        loc_top,
                                        loc_readonly,
                                        show_console,
                                        show_open_button,
                                        show_reload_button,
                                        show_status_bar,
                                        show_fps,
                                        content_directory,
                                        antialiased,
                                        antialiasing_quality,
                                        primitive_quality,
                                        texture_quality,
                                        browserSkin);
        else
            ret_val = new X3DBrowserAWTPanel(
                                          false,
                                          nav_top,
                                          show_loc,
                                          loc_top,
                                          loc_readonly,
                                          show_console,
                                          show_open_button,
                                          show_reload_button,
                                          show_console,
                                          show_status_bar,
                                          show_fps,
                                          content_directory,
                                          antialiased,
                                          antialiasing_quality,
                                          primitive_quality,
                                          texture_quality,
                                          browserSkin);
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

    /**
     * Parse all the strings and place them into a map so it is easy to look
     * them up. Assumes the list is non-null.
     *
     * @param params The given parameter list
     * @return a map of the parsed parameters
     */
    private Map parseParameters(String[] params) {
        HashMap ret_val = new HashMap();

        for(int i = 0; i < params.length; i++) {
            int index = params[i].indexOf('=');
            ret_val.put(params[i].substring(0, index),
                        params[i].substring(index + 1));
        }

        return ret_val;
    }

    /** Load the browser skin properties.  These
     *  properties are passed along to configure the
     *  appearance of the VrmlComponents created by this class.
     */
    private void loadBrowserSkin() {
        try {
            browserSkin=(Properties)AccessController.doPrivileged(
                    new PrivilegedExceptionAction () {
                        public Object run() {
                            String user_dir = System.getProperty("user.dir");
                            InputStream is;
                            String file = user_dir + File.separator + SKIN_PROPERTY_FILE;
                            // Using File.separator does not work for defLoc, not sure why
                            String defLoc = "config/common/" + SKIN_PROPERTY_FILE;

                            try {
                                is = new FileInputStream(file);
                            } catch(FileNotFoundException fnfe) {
                                // Fallback to default
                                is = (InputStream) ClassLoader.getSystemClassLoader().getResourceAsStream(SKIN_PROPERTY_FILE);
                            }

                            // Fallback for WebStart
                            if (is == null) {
                                is = (InputStream) J3DBrowserFactoryImpl.class.getClassLoader().getResourceAsStream(defLoc);
                            }
                            if (is == null) {
                                System.out.println("No skin defined in " + defLoc);
                            } else {

                                Properties props = new Properties();
                                try {
                                    props.load(is);
                                    is.close();
                                } catch(IOException ioe) {
                                    System.out.println("Error reading properties from "+defLoc);
                                }
                                return props;
                            }
                            System.out.println("Supplying default properties.");
                            return new Properties();

                        }
                    });
            } catch (PrivilegedActionException pae) {
                System.out.println("Error getting cursor properties");
            }
    }

}
