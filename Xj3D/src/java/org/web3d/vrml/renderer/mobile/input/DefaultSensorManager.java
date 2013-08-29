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

package org.web3d.vrml.renderer.mobile.input;

// External imports
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.IntHashMap;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.util.NodeArray;
import org.xj3d.core.eventmodel.PickingManager;
import org.xj3d.core.eventmodel.SensorManager;

//TODO: remove
import org.xj3d.core.eventmodel.InputDeviceManager;
import org.xj3d.core.eventmodel.KeyDeviceSensorManager;
import org.xj3d.core.eventmodel.BindableNodeManager;
import org.web3d.vrml.renderer.common.input.TimeSensorManager;
import org.web3d.vrml.renderer.mobile.nodes.MobileViewpointNodeType;
import org.web3d.vrml.renderer.mobile.sg.Group;

/**
 * Default implementation of the SensorManager interface for the OpenGL
 * renderer.
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class DefaultSensorManager implements MobileSensorManager {

    /** Default size of the event arrays */
    private static final int DEFAULT_EVENT_SIZE = 128;

    /** Manager of TimeSensor nodes */
    private TimeSensorManager timeSensors;

    /** Manager of key devices */
    private KeyDeviceSensorManager keySensors;

    /** Picking manager for intersection testing */
    private PickingManager pickManager;

    /** Buffer for input events */
    private InputDeviceManager inputManager;

    /** holder of mouse events each frame */
    private MouseEvent[] mouseEvents;

    /** holder of key events each frame */
    private KeyEvent[] keyEvents;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Bindable stack for viewpoints */
    private BindableNodeManager viewStack;

    /** Bindable stack for navigationInfo nodes */
    private BindableNodeManager navInfoStack;

    /** Bindable stack for backgrounds */
    private BindableNodeManager backgroundStack;

    /** Bindable stack for fog */
    private BindableNodeManager fogStack;

    /**
     * Create a new default instance of the manager. It will only register a
     * handler for TimeSensors. Anything other than that will require the end
     * user code to register an appropriate manager.
     */
    public DefaultSensorManager() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        timeSensors = new TimeSensorManager();
        keySensors = new KeyDeviceSensorManager();

//        mouseHandler = new DefaultUserInputHandler();
//        mouseHandler.setVRMLClock(timeSensors);

        mouseEvents = new MouseEvent[DEFAULT_EVENT_SIZE];
        keyEvents = new KeyEvent[DEFAULT_EVENT_SIZE];
    }

    //-------------------------------------------------------------
    // Methods required by SensorManager
    //-------------------------------------------------------------

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

        timeSensors.setErrorReporter(reporter);
        keySensors.setErrorReporter(errorReporter);

        if(inputManager != null)
            inputManager.setErrorReporter(errorReporter);

        if(pickManager != null)
            pickManager.setErrorReporter(errorReporter);
    }

    /**
     * Get the user input handler. Exposed so that bindable node management
     * can interact with the navigation system.
     *
     * @return The user input handler instance in use
     */
    public UserInputHandler getUserInputHandler() {
        return null;
    }

    /**
     * Set the user input manager to be used by this implementation. User input
     * is generally independent of the main render loop. A value of null may be
     * used to clear the currently set manager and make the handler not process
     * user input.
     *
     * @param buffer The buffer instance to use or null
     */
    public void setInputManager(InputDeviceManager manager) {
        inputManager = manager;

        if(manager != null) {
            manager.setErrorReporter(errorReporter);
//            manager.initialize(mouseHandler);
        }
    }

    /**
     * Set the manager that is responsible for handling picking sensors.
     *
     * @param picker Reference to the manager instance to use or null
     */
    public void setPickingManager(PickingManager picker) {
        pickManager = picker;
    }


    /**
     * Set the bindable stacks used for viewpoint and navigation nodes.
     * Used by the sensor for computing navigation and sensor information.
     *
     * @param vp The stack for viewpoints
     * @param nav The stack for navigationInfo nodes
     * @param back The stack for background nodes
     * @param fog The stack for fog nodes
     */
    public void setNavigationStacks(BindableNodeManager vp,
                                    BindableNodeManager nav,
                                    BindableNodeManager back,
                                    BindableNodeManager fog) {
        viewStack = vp;
        navInfoStack = nav;
        backgroundStack = back;
        fogStack = fog;
    }

    /**
     * Set the branchgroup that acts as the world root for doing picking
     * against.
     *
     * @param root The world root to pick against
     */
    public void setWorldRoot(Group root) {
    }

    /**
     * Process the user input to the scene now. User input is the mouse and
     * keyboard processing that would be used to send events to
     * Key/StringSensors and perform navigation and picking duties as well as
     * adjust items like billboards and LODs.
     *
     * @param time The clock time, in Java coordinates, not VRML
     */
    public void processUserInput(long time) {
        timeSensors.clockTick(time);

        // First process mouse events etc.
        if(inputBuffer != null) {
            int num_events = inputBuffer.getMouseEvents(mouseEvents);
//            mouseHandler.processMouseEvents(mouseEvents, num_events);

            // Currently just shuffles the events off to the key sensors. We
            // don't do any keyboad navigation. We probably should.
            num_events = inputBuffer.getKeyEvents(keyEvents);
            if(num_events != 0)
                keySensors.sendEvents(keyEvents, num_events);
        }

        // Fetch the view global position and orientation for use in the
        // sensor evaluation.
        MobileViewpointNodeType vp =
            (MobileViewpointNodeType)viewStack.getBoundNode();

        VRMLNavigationInfoNodeType nav =
            (VRMLNavigationInfoNodeType)navInfoStack.getBoundNode();

        pickManager.processPickSensors(time);
    }

    /**
     * Cleanup the given sensors and remove them from the list of processing
     * to be done each frame. The list will be created elsewhere (typically the
     * per-frame behaviour as a result of the event model processing) and
     * passed to this manager. The given list will contain instances of
     * VRMLSensorNodeType. There will be no protos as this is just the raw
     * sensor nodes internally.
     *
     * @param sensors The list of sensors to process
     */
    public void removeSensors(NodeArray sensors) {
        int size = sensors.size();

        for(int i = 0; i < size; i++) {
            VRMLSensorNodeType node = (VRMLSensorNodeType)sensors.get(i);

            switch(node.getPrimaryType()) {
                case TypeConstants.KeyDeviceSensorNodeType:
                    keySensors.removeSensor((VRMLKeyDeviceSensorNodeType)node);
                    break;

                case TypeConstants.TimeDependentNodeType:
                case TypeConstants.TimeControlledNodeType:
                    ((VRMLTimeDependentNodeType)node).setVRMLClock(null);
                    break;

                case TypeConstants.PickingSensorNodeType:
                    pickManager.removeSensor((VRMLPickingSensorNodeType)node);
                    break;

                default:
                    System.out.println("Unhandled node type " +
                                       node.getVRMLNodeName());
            }
        }
    }

    /**
     * Initialise new sensors that are just about to be added to the scene.
     * These sensors should also be added to the processing list for dealing
     * with user input. Note that the adding process should only send the
     * initial events, but should not do any processing for environmental
     * effects like collisions or proximity sensing. It is assumed these will
     * be first processed in the next render pass.
     *
     * @param sensors The list of sensors to process
     */
    public void addSensors(NodeArray sensors) {
        int size = sensors.size();

        for(int i = 0; i < size; i++) {
            VRMLSensorNodeType node = (VRMLSensorNodeType)sensors.get(i);

            switch(node.getPrimaryType()) {
                case TypeConstants.KeyDeviceSensorNodeType:
                    keySensors.addSensor((VRMLKeyDeviceSensorNodeType)node);
                    break;

                case TypeConstants.TimeDependentNodeType:
                case TypeConstants.TimeControlledNodeType:
                    ((VRMLTimeDependentNodeType)node).setVRMLClock(timeSensors);
                    break;

                case TypeConstants.PickingSensorNodeType:
                    pickManager.addSensor((VRMLPickingSensorNodeType)node);
                    break;

                default:
                    System.out.println("Unhandled node type " +
                                       node.getVRMLNodeName());
            }
        }
    }

    /**
     * Add view-dependent nodes that need to be updated each frame based on
     * the user's position for rendering. These are not sensors nodes, but
     * others like Billboard, LOD etc.
     *
     * @param nodes List of nodes that need to be processed
     */
    public void addViewDependentNodes(NodeArray nodes) {
    }

    /**
     * Remove these view-dependent nodes from the scene.
     *
     * @param nodes List of nodes to be removed
     */
    public void removeViewDependentNodes(NodeArray nodes) {
    }

    /**
     * Load the contents of this scene into the sensor manager. The call does
     * not need to be recursive as it is expected the external caller will
     * work with this.
     *
     * @param scene The scene to source data from
     */
    public void loadScene(BasicScene scene) {
        ArrayList nodes =
            scene.getByPrimaryType(TypeConstants.KeyDeviceSensorNodeType);

        int i;
        int size = nodes.size();

        for(i = 0; i < size; i++)
            keySensors.addSensor((VRMLKeyDeviceSensorNodeType)nodes.get(i));

        nodes =
            scene.getBySecondaryType(TypeConstants.TimeDependentNodeType);

        size = nodes.size();
        for(i = 0; i < size; i++) {
            Object node = nodes.get(i);
            ((VRMLTimeDependentNodeType)node).setVRMLClock(timeSensors);
        }

        nodes =
            scene.getBySecondaryType(TypeConstants.TimeControlledNodeType);

        size = nodes.size();
        for(i = 0; i < size; i++) {
            Object node = nodes.get(i);
            ((VRMLTimeControlledNodeType)node).setVRMLClock(timeSensors);
        }


        nodes =
            scene.getByPrimaryType(TypeConstants.EnvironmentalSensorNodeType);

        size = nodes.size();
        for(i = 0; i < size; i++) {
            // visibility or prox sensor?
        }
    }

    /**
     * Force clearing all state from this manager now. This is used to indicate
     * that a new world is about to be loaded and everything should be cleaned
     * out now.
     */
    public void clear() {
        timeSensors.clear();
        keySensors.clear();
    }

    /**
     * Get the VRMLClock instance in use by this sensor manager.
     *
     * @return A reference to the clock
     */
    public VRMLClock getVRMLClock() {
        return timeSensors;
    }

    /**
     * Update the viewing matrix.  Call this when you want the SensorManager to update
     * the viewing matrix.  Typically after all user input and events have resolved.
     */
    public void updateViewMatrix() {
        // No-op for OpenGL
    }
}
