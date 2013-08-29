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

package org.xj3d.core.eventmodel;

// External imports
// None

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;

import org.web3d.vrml.util.NodeArray;
import org.web3d.vrml.util.Xj3DKeyEvent;
import org.web3d.vrml.util.KeySensorDevice;
import org.web3d.vrml.util.KeySequence;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLKeyDeviceSensorNodeType;

/**
 * Generalised manager for KeyDeviceSensor nodes.
 * <p>
 *
 * Key device sensors must operate on an exclusive basis. If the sensor changes
 * state then it must turn off the other key device sensors and send events to
 * the new sensor.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class KeyDeviceSensorManager {

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Collection of all key device sensors */
    private HashSet keyNodes;

    /** The array version of all key device sensors for fast assesment */
    private NodeArray sensors;

    /** The device that queues up key events between frames */
    private KeySensorDevice keySensorDevice;

    /** The ordered list of key events retrieved from the KeySensorDevice */
    private KeySequence keySequence;

    /** The device factory for creating devices, hooked into
     *  the UI systems event listeners */
    private DeviceFactory deviceFactory;

    /**
     * Create a manager for KeyDeviceSensor nodes.
     */
    public KeyDeviceSensorManager( DeviceFactory factory ) {
        if ( factory != null ) {
            deviceFactory = factory;
            keySensorDevice = factory.getKeySensorDevice( );
            keySequence = new KeySequence( );
        }
        keyNodes = new HashSet();
        sensors = new NodeArray();

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Force clearing all state from this manager now. This is used to indicate
     * that a new world is about to be loaded and everything should be cleaned
     * out now.
     */
    public void clear() {
        keyNodes.clear();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------


    /**
     * Set a new device factory to use.  Reinitialize devices.
     *
     * @param factory The new DeviceFactory
     */
    public void reinitialize(DeviceFactory factory) {
        deviceFactory = factory;

        if (deviceFactory != null) {
            keySensorDevice = deviceFactory.getKeySensorDevice();
            keySequence = new KeySequence();
        } else {
            keySensorDevice = null;
            keySequence = null;
        }
    }

    /**
     * Send events through to the current enabled key sensors.
     */
    public void processEvents( ) {
        if ( keySensorDevice != null ) {
            keySensorDevice.getEvents( keySequence );
            int num_events = keySequence.size( );
            if ( num_events != 0 ) {
                int num_sensors = sensors.size();

                for(int i = 0; i < num_sensors; i++) {
                    VRMLKeyDeviceSensorNodeType node =
                        (VRMLKeyDeviceSensorNodeType)sensors.get(i);

                    if(!node.getEnabled())
                        continue;

                    int start_idx = node.requiresLastEventOnly() ? num_events - 1 : 0;

                    for(int j = start_idx; j < num_events; j++) {
                        sendEvent(node, keySequence.get( j ) );
                    }
                }
            }
        }
    }

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
     * Add a key device sensor node to the managed list. Only adds it to the
     * internal list if it is not already active.
     *
     * @param sensor The new sensor instance to add
     */
    public void addSensor(VRMLKeyDeviceSensorNodeType sensor) {
        if(!keyNodes.contains(sensor)) {
            keyNodes.add(sensor);
            sensors.add(sensor);
        }
    }

    /**
     * Remove the key device sensor from the management list. If the sensor is
     * not being managed by this instance, the request is ignored.
     *
     * @param sensor The new sensor instance to remove
     */
    public void removeSensor(VRMLKeyDeviceSensorNodeType sensor) {
        if(keyNodes.contains(sensor)) {
            keyNodes.remove(sensor);
            sensors.remove(sensor);
        }
    }

    /**
     * Send one event to the active node now.
     *
     * @param evt The event to send
     */
    private void sendEvent(VRMLKeyDeviceSensorNodeType sensor, Xj3DKeyEvent evt) {
        try {
            // rem: why an exception block ?
            switch(evt.getID()) {
            case Xj3DKeyEvent.KEY_PRESSED:
                sensor.keyPressed(evt);
                break;

            case Xj3DKeyEvent.KEY_RELEASED:
                sensor.keyReleased(evt);
                break;

            }
        } catch(Exception e) {
            errorReporter.warningReport("Error propogating key event", e);
        }
    }
}
