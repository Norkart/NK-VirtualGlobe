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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.BitSet;

// Local imports
import org.xj3d.device.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLDeviceSensorNodeType;

/**
 * Sits between input devices and the UserInputHandler.
 * Determines which one has control of navigation and picking.
 * Maintains picking state.
 * <p>
 *
 * KeySensor handling is performed separately by the KeyDeviceSensorManager
 * <p>
 * Each sensor will have a current active touch, drag and anchor sensor.
 * It cannot activate another till that's cleared.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class InputDeviceManager {
    
    /** Devices managed by this class */
    private InputDevice[] devices;
    
    /** A scratch tracker state value for performance */
    private TrackerState state;
    
    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;
    
    /** Current list of trackerDevices */
    private TrackerDevice[] trackerDevices;
    
    /** Current list of ControllerDevices */
    private ControllerDevice[] controllerDevices;
    
    /** Current list of MidiDevices */
    private MidiDevice[] midiDevices;
    
    /** Trackers managed by this class.  Unrolled from trackerDevices for speed */
    private Tracker[] trackers;
    
    /** Scratch variable for getting gamepadState */
    private GamepadState gamepadState;
    
    /** Scratch variable for getting joystickState */
    private JoystickState joystickState;
    
    /** Scratch variable for getting joystickState */
    private WheelState wheelState;
    
    /** A mapping of real devices to X3D Nodes */
    private HashMap controllerMap;
    
    /** The device factory for creating devices, hooked into
    *  the UI systems event listeners */
    private DeviceFactory deviceFactory;
    
    /** Has this device manager been initialized */
    private boolean initialized;
    
    /** Should trackers activate sensors */
    private boolean activateSensors;
    
    /**
     * Create a new device manager.
     *
     * @param factory The DeviceFactory that provides access to the devices
	 * and the UI event system.
     */
    public InputDeviceManager( DeviceFactory factory ) {
        deviceFactory = factory;
        initialized = false;
        
        errorReporter = DefaultErrorReporter.getDefaultReporter();
        gamepadState = new GamepadState();
        joystickState = new JoystickState();
        wheelState = new WheelState();
        controllerMap = new HashMap();
        
        state = new TrackerState();
    }
    
    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------
    
    /**
     * Set a new device factory to use.  Reinitialize devices.
     *
     * @param factory The new DeviceFactory
     */
    public void reinitialize(DeviceFactory factory) {
        deviceFactory = factory;
        initialize();
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
     * Initialize the device manager.  Called after all devices
     * are registered.  Handled by the SensorManager.
     */
    public void initialize() {
        if ( deviceFactory != null ) {
            DeviceManager[] newManagers = deviceFactory.getDeviceManagers( );
            int len = newManagers.length;
            DeviceManager manager;
            
            InputDevice[] newDevices;
            ArrayList newControllers = new ArrayList();
            ArrayList newTrackers = new ArrayList();
            ArrayList newMidis = new ArrayList();
            InputDevice dev;
            
            for(int i = 0; i < len; i++) {
                manager = newManagers[i];
                manager.setErrorReporter(errorReporter);
                newDevices = manager.getDevices();
                for(int j = 0; j < newDevices.length; j++) {
                    dev = newDevices[j];
                    
                    errorReporter.messageReport("Device found: " + dev.getName());
                    
                    // May want to add an API call to InputDevice that allows
                    // setting of the errorReporter.
                    
                    if(dev instanceof TrackerDevice)
                        newTrackers.add(dev);
                    
                    if(dev instanceof ControllerDevice)
                        newControllers.add(dev);
                    
                    if(dev instanceof MidiDevice)
                        newMidis.add(dev);
                }
            }
            
            if( ( newTrackers.size() == 0 ) && ( newControllers.size() == 0 ) &&
                ( newMidis.size() == 0 ) ) {
                errorReporter.messageReport( "No devices registered in InputDeviceManager" );
            }
            
            trackerDevices = new TrackerDevice[newTrackers.size()];
            newTrackers.toArray(trackerDevices);
            
            controllerDevices = new ControllerDevice[newControllers.size()];
            newControllers.toArray(controllerDevices);
            
            midiDevices = new MidiDevice[newMidis.size()];
            newMidis.toArray(midiDevices);
            
            newTrackers.clear();
            newControllers.clear();
            newMidis.clear();
            
            // Move devices from arraylist to array for speed
            len = trackerDevices.length;
            
            TrackerDevice device;
            int totalTrackers = 0;
            
            for(int i=0; i < len; i++) {
                device = (TrackerDevice) trackerDevices[i];
                totalTrackers += device.getTrackerCount();
            }
            
            trackers = new Tracker[totalTrackers];
            int cnt = 0;
            
            for(int i = 0; i < len; i++) {
                Tracker[] newts = trackerDevices[i].getTrackers();
                for(int j = 0; j < newts.length; j++) {
                    trackers[cnt++] = newts[j];
                }
            }
        } else {
            trackerDevices = new TrackerDevice[0];
            controllerDevices = new ControllerDevice[0];
            midiDevices = new MidiDevice[0];
            trackers = new Tracker[0];
        }
        initialized = true;
    }
    
    /**
     * Add a new X3DDeviceSensorNode.
     *
     * @param node The new sensor
     */
    public void addX3DNode(VRMLDeviceSensorNodeType node) {
        String name = node.getName();
        
        ArrayList list;
        int len;
        
        list = (ArrayList) controllerMap.get(name);
        if(list == null) {
            list = new ArrayList();
            list.add(node);
            controllerMap.put(name, list);
            
            // Find device and notify node
            len = controllerDevices.length;
            for(int i = 0; i < len; i++) {
                if(controllerDevices[i].getName().equals(name))
                    node.setDevice(controllerDevices[i]);
            }
            
        } else {
            list.add(node);
        }
    }
    
    /**
     * Remove a X3DDeviceSensorNode.
     *
     * @param node The sensor
     */
    public void removeX3DNode(VRMLDeviceSensorNodeType node) {
System.out.println("Got remove sensor node: " + node);
    }
    
    /**
     * Clear all of the currently registered nodes. Used in preparation for
     * loading a new scene.
     */
    public void clear() {
        controllerMap.clear();
    }
    
    /**
     * Notification that tracker processing is starting for a frame.
     */
    public void beginTrackerProcessing() {
        int len = trackers.length;
        
        activateSensors = true;
        
        for(int i = 0; i < len; i++) {
            trackers[i].beginPolling();
        }
    }
    
    /**
     * Notification that tracker processing is ending for a frame.
     */
    public void endTrackerProcessing() {
        int len = trackers.length;
        
        for(int i = 0; i < len; i++) {
            trackers[i].endPolling();
        }
    }
    
    /**
     * Processes input from sensors and issues commands to the UserInputHandler.
     * Called by the event model at the appropriate time.
     * Do not call from elsewhere.
     *
     * @param layerId The layer that we're reading from
     * @param navigate Should navigation be processed
     * @param uiHandler The uiHandler for this layer
     * @param pickable Is the layer pickable
     */
    public void processTrackers(int layerId, boolean navigate,
        UserInputHandler uiHandler, boolean pickable) {
        
        // Devices must be called in this order Controllers, Trackers, Midi.
        // Implementations can use this order to avoid polling extra times
        // for devices which implement multiple interfaces.
        
        if(!initialized)
            return;
        
        int len = controllerDevices.length;
        
        if (navigate) {
            // Only process these once
            
            DeviceState cstate = null;
            
            for(int i = 0; i < len; i++) {
                if(controllerDevices[i] instanceof GamepadDevice) {
                    ((GamepadDevice)controllerDevices[i]).getState(gamepadState);
                    cstate = gamepadState;
                } else if(controllerDevices[i] instanceof JoystickDevice) {
                    ((JoystickDevice)controllerDevices[i]).getState(joystickState);
                    cstate = joystickState;
                } else if(controllerDevices[i] instanceof WheelDevice) {
                    ((WheelDevice)controllerDevices[i]).getState(wheelState);
                    cstate = wheelState;
                }
                
                // update VRML nodes here
                ArrayList list =
                    (ArrayList)controllerMap.get(controllerDevices[i].getName());
                int x3dLen;
                
                if(list != null) {
                    x3dLen = list.size();
                    
                    for(int j = 0; j < x3dLen; j++) {
                        VRMLDeviceSensorNodeType device =
                            (VRMLDeviceSensorNodeType)list.get(j);
                        device.update(cstate);
                    }
                }
                
                if(cstate != null)
                    cstate.clearChanged();
            }
        }
        
        len = trackers.length;
        
        for(int i = 0; i < len; i++) {
            trackers[i].getState(layerId, 0, state);
            if (pickable)
                uiHandler.setActivateSensors(activateSensors);
            else
                uiHandler.setActivateSensors(false);
            
            switch(state.actionType) {
            case TrackerState.TYPE_BUTTON:
                uiHandler.trackerButton(i,state);
                break;
            case TrackerState.TYPE_PRESS:
                uiHandler.trackerPressed(i,state);
                break;
            case TrackerState.TYPE_RELEASE:
                uiHandler.trackerReleased(i,state);
                break;
            case TrackerState.TYPE_MOVE:
                uiHandler.trackerMoved(i,state);
                break;
            case TrackerState.TYPE_CLICK:
                uiHandler.trackerClicked(i,state);
                break;
            case TrackerState.TYPE_DRAG:
                uiHandler.trackerDragged(i,state);
                break;
            case TrackerState.TYPE_ORIENTATION:
                uiHandler.trackerOrientation(i,state);
                break;
            case TrackerState.TYPE_WHEEL:
                uiHandler.trackerWheel(i,state);
                break;
            }
        }
        
        // Don't navigate if a sensor has been activated
        if (navigate && activateSensors) {
            uiHandler.processNavigation();
        }
        
        if (uiHandler.trackerIntersected()) {
            activateSensors = false;
        }
        
        // TODO:
        // Move the MIDI device handling into here rather than have it as a standalone
        // class.
        //        len = midiDevices.length;
        //
        //        for(int i=0; i < len; i++) {
        //            midiDevices[i].getState(state);
        // update VRML nodes here
        //        }
        
    }
}
