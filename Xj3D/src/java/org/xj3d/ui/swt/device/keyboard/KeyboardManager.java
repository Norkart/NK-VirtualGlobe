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

package org.xj3d.ui.swt.device.keyboard;

// External imports
import java.util.ArrayList;

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.util.DefaultErrorReporter;

import org.xj3d.device.InputDevice;
import org.xj3d.device.DeviceManager;
import org.xj3d.device.DeviceListener;

/**
 * Manages Keyboard devices.  Assumes only 1 device currently.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class KeyboardManager implements DeviceManager {
    /** The device list */
    private ArrayList devices;

    /** List of those who want to know about device changes. */
    private ArrayList deviceListeners;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    public KeyboardManager() {
        devices = new ArrayList();

        deviceListeners = new ArrayList();

        KeyboardDevice device = new KeyboardDevice("Keyboard-0");
        devices.add(device);
    }

    //------------------------------------------------------------------------
    // Methods for DeviceManager interface
    //------------------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Get the number of devices discovered.
     *
     * @return The number of devices.
     */
    public int getNumDevices() {
        return devices.size();
    }

    /**
     * Get the device discovered by this manager.  All devices discovered
     * after this call will be reported to DeviceListeners.
     *
     * @return InputDevice[] An array of discovered devices.
     */
    public InputDevice[] getDevices() {
        InputDevice[] devs = new InputDevice[devices.size()];

        devices.toArray(devs);

        return devs;
    }

    /**
     * Add a listener for devices additions and removals.
     *
     * @param l The listener.  Nulls and duplicates will be ignored.
     */
    public void addDeviceListener(DeviceListener l) {
        if(!deviceListeners.contains(l))
            deviceListeners.add(l);

    }

    /**
     * Remove a listener for device additions and removals.
     *
     * @param l The listener.  Nulls and not found listeners will be ignored.
     */
    public void removeDeviceListener(DeviceListener l) {
        deviceListeners.remove(l);
    }
}
