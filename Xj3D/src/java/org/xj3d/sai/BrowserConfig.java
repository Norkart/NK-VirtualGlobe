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

package org.xj3d.sai;

// External Imports
import java.io.*;

import java.util.*;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;


// Local Imports
// None

/**
 * Utility class to load, qualify and store SAI and EAI browser configuration
 * parameters, including the properties defining the browser 'skin'.
 * Used by the various BrowserFactoryImpl's as a common parameter loader.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class BrowserConfig {

    //////////////////////////////////////////////////////////////////////////////////////
    // Known Xj3D parameter Strings

    // Parameters defined in spec
    private static final String ANTIALIASED_PARAM = "Antialiased";
    private static final String PRIMITIVE_QUALITY_PARAM = "PrimitiveQuality";
    private static final String SHADING_PARAM = "Shading";
    private static final String TEXTURE_QUALITY_PARAM = "TextureQuality";

    // Parameters common to Xj3D factory impls
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
    private static final String ANTIALIASING_QUALITY_PARAM = "Xj3D_AntialiasingQuality";
    private static final String DIMENSIONS_PARAM = "Xj3D_PreferredDimensions";

    // AWT / Swing toolkit specific parameter
    private static final String SWING_PARAM = "Xj3D_InterfaceType";

    // SAI only parameters
    private static final String SKIN_PROPERTIES = "Xj3D_Skin_Properties";
    private static final String SKIN_RESOURCES = "Xj3D_Skin_Resources";

    // Rendering configuration parameters
    private static final String CULLING_MODE = "Xj3D_Culling_Mode";

    //////////////////////////////////////////////////////////////////////////////////////

    /** Error message for passing a null String in loadURL */
    private static final String NULL_PARAMETER_ERROR = "Null parameter strings not allowed.";

    /** Error message for malformed parameter String in loadURL */
    private static final String MALFORMED_PARAMETER_STRING_ERROR =
        "Malformed parameter string."+
        "  Expecting strings of the form A=B";

    /** Property file defining the appearance of the browser */
    private static final String SKIN_PROPERTY_FILE = "xj3d-skin.properties";

    /** Set of quality params */
    private static HashSet qualitySet;

    /** Set of culling mode params */
    private static HashSet cullingSet;

    /** Should the browser be restricted to VRML97 only. Default false. */
    public boolean vrml97Only;

    /** The type of GUI interface that should be created */
    public BrowserInterfaceTypes interfaceType;

    /** Should the navigation bar be visible. Default true. */
    public boolean showDash;

    /** Placement of the navigation bar if visible. Default false. */
    public boolean dashTop;

    /** Should the location bar be visible. Default true. */
    public boolean showUrl;

    /** Placement of the location bar if visible. Default true. */
    public boolean urlTop;

    /** Should the location bar be read only. Default false. */
    public boolean urlReadOnly;

    /** Should the console be visible. Default false. */
    public boolean showConsole;

    /** Should the open button, on the location bar, be visible. Default false. */
    public boolean showOpenButton;

    /** Should the reload button, on the location bar, be visible. Default false. */
    public boolean showReloadButton;

    /** Should StatusBar be shown. Default false. */
    public boolean showStatusBar;

    /** Should StatusBar have a frames-per-second display. Default false. */
    public boolean showFPS;

    /** Initial directory to use for locating content. Defaults to System.getProperty("user.dir")*/
    public String contentDirectory;

    /** Antialiasing enabled. Default false. */
    public boolean antialiased;

    /** Antialiasing quality. "low"|"medium"|"high". Default "low". */
    public String antialiasingQuality;

    /** Primitive geometry quality. "low"|"medium"|"high". Default "medium". */
    public String primitiveQuality;

    /** Texture quality. "low"|"medium"|"high". Default "medium". */
    public String textureQuality;

    /** Properties object defining appearance of browser panel. Defaults to an empty Properties object. */
    public Properties browserSkin;

    /** Map object containing default resources refered to by the skinProperties. Default null. */
    public Map resourceMap;

    /** Culling Mode. "none"|"frustum". Default "frustum". */
    public String cullingMode;

    /** The preferred width of the window. */
    public int preferredWidth;

    /** The preferred height of the window. */
    public int preferredHeight;

    /**
     * Create a BrowserConfig instance initialized with defaults.
     */
    public BrowserConfig() {

        vrml97Only = false;
        interfaceType = BrowserInterfaceTypes.PARTIAL_LIGHTWEIGHT;
        showDash = true;
        dashTop = false;
        showUrl = true;
        urlTop = true;
        urlReadOnly = false;
        showConsole = false;
        showOpenButton = false;
        showReloadButton = false;
        showStatusBar = false;
        showFPS = false;
        antialiased = false;
        antialiasingQuality = "low";
        primitiveQuality = "medium";
        textureQuality = "medium";
        cullingMode = "frustum";

        browserSkin = new Properties();
        contentDirectory = System.getProperty("user.dir");
    }

    /**
     * Create a BrowserConfig instance initialized with defaults which will be
     * overridden by any parameters passed in the argument array.
     *
     * @param params Parameters to control the look and feel.
     * @throws IllegalArgumentException if a parameter in the argument Map is invalid.
     */
    public BrowserConfig(String[] params) {
        this();

        if((params != null) && (params.length != 0)) {
            Map paramMap = parseParameters(params);
            initialize(paramMap, false);
        } else {
            initialize(null, false);
        }
    }

    /**
     * Create a BrowserConfig instance initialized with defaults which will be
     * overridden by any parameters passed in the argument Map.
     *
     * @param params Parameters to control the look and feel.
     * @throws IllegalArgumentException if a parameter in the argument Map is invalid.
     */
    public BrowserConfig(Map params) {
        this();
        initialize(params, true);
    }

    //-----------------------------------------------------------------------
    // Local Methods
    //-----------------------------------------------------------------------

    /**
     * Overridden the default parameters with values from the argument Map.
     *
     * @param params Parameters to control the look and feel.
     * @param isSAI Indication of the source of the parameters. <code>false</code> if
     * the Map values are derived from an EAI String[] and must be cast to
     * their type (if the required type is not a String). <code>true</code> if the
     * Map has been passed from the SAI constructor - and are already in their required
     * types.
     * @throws IllegalArgumentException if a parameter in the argument Map is invalid.
     */
    private void initialize(Map params, boolean isSAI) {

        if((params != null) && (params.size() != 0)) {

            if(qualitySet == null) {
                qualitySet = new HashSet(3);
                qualitySet.add("low");
                qualitySet.add("medium");
                qualitySet.add("high");
            }

            if(cullingSet == null) {
                cullingSet = new HashSet(2);
                cullingSet.add("none");
                cullingSet.add("frustum");
            }

            String stringVal = getStringValue(params, SWING_PARAM, isSAI);
            if(stringVal != null) {
                if(stringVal.equalsIgnoreCase("awt"))
                    interfaceType = BrowserInterfaceTypes.HEAVYWEIGHT;
                else if(stringVal.equalsIgnoreCase("swing"))
                    interfaceType = BrowserInterfaceTypes.PARTIAL_LIGHTWEIGHT;
                else if(stringVal.equalsIgnoreCase("swing-lightweight"))
                    interfaceType = BrowserInterfaceTypes.LIGHTWEIGHT;
                else if(stringVal.equalsIgnoreCase("offscreen"))
                    interfaceType = BrowserInterfaceTypes.OFFSCREEN;
            }

            Boolean booleanVal = getBooleanValue(params, NAV_SHOW_PARAM, isSAI);
            if(booleanVal != null) {
                showDash = booleanVal.booleanValue();
            }

            stringVal = getStringValue(params, NAV_POS_PARAM, isSAI);
            if(stringVal != null) {
                dashTop = stringVal.equalsIgnoreCase("top");
            }

            booleanVal = getBooleanValue(params, LOC_SHOW_PARAM, isSAI);
            if(booleanVal != null) {
                showUrl = booleanVal.booleanValue();
            }

            stringVal = getStringValue(params, LOC_POS_PARAM, isSAI);
            if(stringVal != null) {
                urlTop = stringVal.equalsIgnoreCase("top");
            }

            booleanVal = getBooleanValue(params, LOC_READONLY_PARAM, isSAI);
            if(booleanVal != null) {
                urlReadOnly = booleanVal.booleanValue();
            }

            booleanVal = getBooleanValue(params, SHOW_CONSOLE_PARAM, isSAI);
            if(booleanVal != null) {
                showConsole = booleanVal.booleanValue();
            }

            booleanVal = getBooleanValue(params, OPEN_BUTTON_SHOW_PARAM, isSAI);
            if(booleanVal != null) {
                showOpenButton = booleanVal.booleanValue();
            }

            booleanVal = getBooleanValue(params, RELOAD_BUTTON_SHOW_PARAM, isSAI);
            if(booleanVal != null) {
                showReloadButton = booleanVal.booleanValue();
            }

            booleanVal = getBooleanValue(params, STATUS_BAR_SHOW_PARAM, isSAI);
            if(booleanVal != null) {
                showStatusBar = booleanVal.booleanValue();
            }

            booleanVal = getBooleanValue(params, FPS_SHOW_PARAM, isSAI);
            if(booleanVal != null) {
                showFPS = booleanVal.booleanValue();
            }

            stringVal = getStringValue(params, CONTENT_DIRECTORY_PARAM, isSAI);
            if(stringVal != null) {
                contentDirectory = stringVal;
            }

            booleanVal = getBooleanValue(params, ANTIALIASED_PARAM, isSAI);
            if(booleanVal != null) {
                antialiased = booleanVal.booleanValue();
            }

            stringVal = getStringValue(params, PRIMITIVE_QUALITY_PARAM, isSAI);
            if(stringVal != null) {
                if(!qualitySet.contains(stringVal)) {
                    throw new IllegalArgumentException(
                        "BrowserConfig: " + PRIMITIVE_QUALITY_PARAM + " must be low, medium or high");
                }
                primitiveQuality = stringVal;
            }

            stringVal = getStringValue(params, TEXTURE_QUALITY_PARAM, isSAI);
            if(stringVal != null) {
                if(!qualitySet.contains(stringVal)) {
                    throw new IllegalArgumentException(
                        "BrowserConfig: " + TEXTURE_QUALITY_PARAM + " must be low, medium or high");
                }
                textureQuality = stringVal;
            }

            stringVal = getStringValue(params, ANTIALIASING_QUALITY_PARAM, isSAI);
            if(stringVal != null) {
                if(!qualitySet.contains(stringVal)) {
                    throw new IllegalArgumentException(
                        "BrowserConfig: " + ANTIALIASING_QUALITY_PARAM + " must be low, medium or high");
                }
                antialiasingQuality = stringVal;
            }

            Object obj = params.get(SKIN_PROPERTIES);
            if(obj != null && !(obj instanceof Properties)) {
                throw new IllegalArgumentException(
                    "BrowserConfig: " + SKIN_PROPERTIES + " must be a Properties object");
            }
            else {
                browserSkin = (Properties)obj;
            }

            obj = params.get(SKIN_RESOURCES);
            if(obj != null && !(obj instanceof Map)) {
                throw new IllegalArgumentException(
                    "BrowserConfig: "+ SKIN_RESOURCES + " must be a Map object");
            }
            else {
                resourceMap = (Map)obj;
            }

            stringVal = getStringValue(params, CULLING_MODE, isSAI);
            if(stringVal != null) {
                if(!cullingSet.contains(stringVal)) {
                    throw new IllegalArgumentException(
                        "BrowserConfig: " + CULLING_MODE + " must be none or frustum");
                }
                cullingMode = stringVal;
            }

            stringVal = getStringValue(params, DIMENSIONS_PARAM, isSAI);
            if(stringVal != null) {
               StringTokenizer strtok = new StringTokenizer(stringVal, "x");
               String width_str = strtok.nextToken();
               String height_str = strtok.nextToken();

               try {
                   preferredWidth = Integer.parseInt(width_str);
               } catch(NumberFormatException nfe) {
                   // Should do something here, but no error reporter.
               }

               try {
                   preferredHeight = Integer.parseInt(height_str);
               } catch(NumberFormatException nfe) {
                   // Should do something here, but no error reporter.
               }
            }
        }

        if(browserSkin == null) {
            loadBrowserSkin();
        }
        if(contentDirectory == null) {
            contentDirectory = System.getProperty("user.dir");
        }
    }

    /**
     * Extract the specified parameter from the argument Map, qualify and return it.
     *
     * @param params The Map containing the parameters
     * @param paramName The key of the parameter in the Map
     * @param isSAI The 'qualification' switch
     * @return The qualified parameter value, or null if it does not exist.
     * @throws IllegalArgumentException if the parameter isSAI, and is not an instanceof Boolean.
     */
    private Boolean getBooleanValue(Map params, String paramName, boolean isSAI) {
        Boolean booleanVal = null;
        Object obj = params.get(paramName);
        if(obj != null) {
            if(isSAI) {
                if(!(obj instanceof Boolean)) {
                    throw new IllegalArgumentException("BrowserConfig: " + paramName + " must be a Boolean");
                } else {
                    booleanVal = (Boolean)obj;
                }
            } else {
                booleanVal = Boolean.valueOf((String)obj);
            }
        }
        return(booleanVal);
    }

    /**
     * Extract the specified parameter from the argument Map, qualify and return it.
     *
     * @param params The Map containing the parameters
     * @param paramName The key of the parameter in the Map
     * @param isSAI The 'qualification' switch
     * @return The qualified parameter value, or null if it does not exist.
     * @throws IllegalArgumentException if the parameter isSAI, and is not an instanceof String.
     */
    private String getStringValue(Map params, String paramName, boolean isSAI) {
        String stringVal = null;
        Object obj = params.get(paramName);
        if(obj != null) {
            if(isSAI) {
                if(!(obj instanceof String)) {
                    throw new IllegalArgumentException("BrowserConfig: " + paramName + " must be a String");
                }
            }
            stringVal = (String)obj;
        }
        return(stringVal);
    }

    /**
     * Parse the strings from the parameter array and place them into a map
     * so it is easy to look them up. Assumes the list is non-null.
     *
     * @param params The given parameter list
     * @return a map of the parsed parameters
     */
    private Map parseParameters(String[] params) {
        HashMap ret_val = new HashMap();

        for(int i = 0; i < params.length; i++) {
            if(params[i]==null)
                throw new IllegalArgumentException(NULL_PARAMETER_ERROR);
            int index = params[i].indexOf('=');
            if(index<1)
                throw new IllegalArgumentException(MALFORMED_PARAMETER_STRING_ERROR);
            ret_val.put(params[i].substring(0, index),
                params[i].substring(index + 1));
        }

        return ret_val;
    }

    /**
     * Load the browser skin properties. These properties are
     * passed along to configure the appearance of the user
     * interface components associated with the X3DComponent.
     */
    private void loadBrowserSkin() {
        try {
            browserSkin = (Properties)AccessController.doPrivileged(
                new PrivilegedExceptionAction () {
                    public Object run() {
                        InputStream is = null;
                        Properties props = new Properties();
                        String search_path = null;

                        String user_dirname = System.getProperty("user.dir");
                        String user_filename = user_dirname + File.separator + SKIN_PROPERTY_FILE;
                        // Using File.separator does not work for defLoc, not sure why
                        String default_filename = "config/common/" + SKIN_PROPERTY_FILE;
                        try {
                            is = new FileInputStream(user_filename);
                            search_path = user_filename;
                        } catch(FileNotFoundException fnfe) {
                            // Fallback to default
                            is = (InputStream)ClassLoader.getSystemClassLoader().getResourceAsStream(default_filename);
                            search_path = default_filename;
                        }

                        // Fallback for WebStart
                        if(is == null) {
                            is = (InputStream)BrowserConfig.class.getClassLoader().getResourceAsStream(default_filename);
                            search_path = default_filename;
                        }

                        if(is == null) {
                            System.out.println("Skin properties file: "+ search_path +" not found");
                            System.out.println("Returning default properties.");
                        } else {
                            try {
                                props.load(is);
                                is.close();
                            } catch(IOException ioe) {
                                System.out.println("Error reading skin properties file: "+ search_path +" "+ ioe.getMessage());
                                System.out.println("Returning default properties.");
                            }
                        }
                        return(props);
                    }
                }
               );
        } catch (PrivilegedActionException pae) {
            System.out.println("Error getting skin properties");
        }
    }
}

