/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.eventmodel;

// External imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.browser.Xj3DConstants;

import org.xj3d.device.DeviceManager;
import org.web3d.vrml.util.KeySensorDevice;

// Local imports
// None


/**
 * Partial implementation of a DeviceFactory, with plumbing in place
 * for the ui toolkit specific implementation to create pointing and
 * key sensor devices.
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public abstract class DeviceFactory {

    /** Name of the property file that defines the devices
     *  to be loaded */
    private static final String PROPERTY_FILE =
        "xj3d-devices.properties";

    /** Error loading device properties file message */
    private static final String LOAD_DEVICE_PROPERTIES_ERR_MSG =
        "Error getting device properties";

    /** The identifier String of the ui toolkit type */
    protected String toolkitID;

    /** The identifier String of the renderer type */
    protected String rendererID;

    /** The toolkit specific object that is the source of device events */
    protected Object canvas;

    /** The renderer specific object, used for instantiating certain
     *  DeviceManagers */
    protected Object surface;

    /** The class that handles external error messaging */
    //protected static ErrorReporter errorReporter;
    protected ErrorReporter errorReporter;

    /** Protected Default Constructor */
    protected DeviceFactory( ) {
    }

    /**
     * Return the array of DeviceManagers that are available per the
     * constructor parameters. The InputDevices instantiated will be
     * initialized with the appropriate event listeners per the ui toolkit.
     *
     * @return the array of DeviceManagers. If no DeviceManagers are
     * available, an empty (size 0) array is returned.
     */
    public abstract DeviceManager[] getDeviceManagers( );

    /**
     * Return the KeySensorDevice associated with the rendering surface
     * initialized with the toolkit appropriate key event listener.
     *
     * @return the KeySensorDevice
     */
    public abstract KeySensorDevice getKeySensorDevice( );

    /**
     * Register an error reporter with the factory so that any errors
     * generated can be directed appropriately. Setting a value of null
     * will clear the currently set reporter and causes the factory to
     * use the DefaultErrorReporter. If an error reporter is already set,
     * the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter( ErrorReporter reporter ) {
        errorReporter = ( reporter == null ) ?
            DefaultErrorReporter.getDefaultReporter( ) : reporter;
    }

    /**
     * Create and return the List of InputDevices defined in the
     * the default properties file.
     *
     * @return the List of DeviceManagers. An empty (size 0) List will
     * be returned if the properties file cannot be opened or if not
     * valid DeviceManagers are created.
     */
    protected List createDevices( ) {
        final ArrayList newManagers = new ArrayList();

        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction () {
                    public Object run() {
                        String user_dir = System.getProperty("user.dir");
                        InputStream is;
                        String file = user_dir + File.separator + PROPERTY_FILE;

                        // Using File.separator does not work for defLoc, not sure why
                        String defLoc = "config/common/" + PROPERTY_FILE;

                        try {
                            is = new FileInputStream(file);
                        } catch(FileNotFoundException fnfe) {
                            // Fallback to default
                            is = (InputStream) ClassLoader.getSystemResourceAsStream(defLoc);
                        }

                        // Fallback for WebStart
                        if(is == null) {
                            ClassLoader cl = DeviceFactory.class.getClassLoader();
                            is = (InputStream)cl.getResourceAsStream(defLoc);
                        }
                        if(is == null) {
                            errorReporter.warningReport("No property file found in " +
                                defLoc, null);
                        } else {
                            Properties props = new Properties();
                            try {
                                props.load(is);
                                is.close();
                            } catch(IOException ioe) {
                                errorReporter.warningReport(
                                    "Error reading" + defLoc, null);
                            }

                            // Fetch the property defining the list of profiles
                            String devList = "devices." + toolkitID +"."+ rendererID + ".list";
                            String str = props.getProperty(devList);
                            if(str == null) {
                                errorReporter.warningReport("No devices list found: " +
                                    devList, null);
                                return null;
                            }

                            StringTokenizer strtok = new StringTokenizer(str);
                            String keyword;
                            String deviceName;
                            String className;
                            String paramsStr;
                            int params = 0;

                            while (strtok.hasMoreTokens()) {
                                params = 0;
                                keyword = strtok.nextToken();
                                deviceName = props.getProperty(keyword + ".name");
                                className = props.getProperty(keyword + ".class");
                                paramsStr = props.getProperty(keyword + ".params");

                                Object[] constParams = new Object[params];

                                if(paramsStr != null) {
                                    params = Integer.parseInt(paramsStr);

                                    if(params > 0) {
                                        constParams = new Object[params];

                                        for(int i=0; i < params; i++) {
                                            paramsStr = props.getProperty(keyword + ".param." + i);
                                            if(paramsStr != null) {
                                                if(paramsStr.equals("PARAM_CANVAS")) {
                                                    constParams[i] = canvas;
                                                } else if(paramsStr.equals("PARAM_SURFACE")) {
                                                    constParams[i] = surface;
                                                } else {
                                                    errorReporter.warningReport(
                                                        "Unhandled param in device: " +
                                                        deviceName, null);
                                                }
                                            }
                                        }
                                    }
                                }

                                Class devClass = null;
                                Class[] paramTypes;

                                Object dev=null;
                                boolean found=false;
                                try {
                                    devClass = Class.forName(className);

                                    Constructor[] consts = devClass.getConstructors();

                                    for(int i = 0; i < consts.length; i++) {
                                        paramTypes = consts[i].getParameterTypes();
                                        if(paramTypes.length == params) {
                                            dev = consts[i].newInstance(constParams);
                                            found = true;
                                            break;
                                        }
                                    }
                                } catch(Exception e) {
                                    errorReporter.warningReport(
                                        "Error loading " + deviceName +
                                        " at: " + className,
                                        e);
                                }

                                if(found) {
                                    newManagers.add(dev);
                                } else {
                                    errorReporter.warningReport(
                                        "Cannot load " + deviceName +
                                        " at: " + className,
                                        null);
                                }
                            }
                        }
                        return( null );
                    }
                }
                );
        } catch ( PrivilegedActionException pae ) {
            errorReporter.errorReport( LOAD_DEVICE_PROPERTIES_ERR_MSG, pae );
        }
        return( newManagers );
    }
}

