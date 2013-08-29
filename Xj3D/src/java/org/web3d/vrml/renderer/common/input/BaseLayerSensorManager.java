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

package org.web3d.vrml.renderer.common.input;

// External imports
import java.util.ArrayList;
import java.util.HashSet;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

// Local imports
import org.web3d.vrml.nodes.*;

import org.xj3d.core.eventmodel.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.IntHashMap;
import org.web3d.util.ObjectArray;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.renderer.common.nodes.AreaListener;
import org.web3d.vrml.renderer.common.nodes.VisibilityListener;
import org.web3d.vrml.util.NodeArray;

/**
 * Common implementation of the LayerSensorManager interface for all renderers.
 * <p>
 *
 * This base class handles the basic management needs of the sensor manager,
 * such as sorting and processing the various sensor types. Renderer-specific
 * extensions then process the sensors according to their specific needs.
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.9 $
 */
public abstract class BaseLayerSensorManager implements LayerSensorManager {

    /** Message for when addSensor has an unknown sensor type supplied */
    private static final String ADD_SENSOR_UNKNOWN_TYPE =
        "An unknown sensor type has been added to the sensor manager: ";

    /** Message for when addSensor has an unknown sensor type supplied */
    private static final String REMOVE_SENSOR_UNKNOWN_TYPE =
        "An unknown sensor type has been removed from the sensor manager: ";

    /** Default size of the event arrays */
    protected static final int DEFAULT_EVENT_SIZE = 128;

    /** Flag to say whether this class has completed its initialisation */
    protected boolean initialised;

    /** Picking manager for intersection testing */
    protected PickingManager pickManager;

    /** Buffer for input events */
    protected InputDeviceManager inputManager;

    /** Reporter instance for handing out errors */
    protected ErrorReporter errorReporter;

    /** Is this layer pickable */
    private boolean isPickable;

    /**
     * Input manager for doing the user interaction from the input devices
     * This can be used for navigation and various pointing device sensor
     * implementations.
     */
    protected UserInputHandler inputHandler;

    /** Bindable stack for viewpoints */
    protected BindableNodeManager viewStack;

    /** Bindable stack for navigationInfo nodes */
    protected BindableNodeManager navInfoStack;

    /** Bindable stack for backgrounds */
    protected BindableNodeManager backgroundStack;

    /** Bindable stack for fog */
    protected BindableNodeManager fogStack;

    /** Flag to say if navigation handling should be disabled */
    protected boolean navigationEnabled;

    /** Flag to say the fog type */
    protected boolean fogTypeChanged;

    /** Flag to say fog color or range value changed. */
    protected boolean fogDetailsChanged;

    /** Flag to say the background sky values changed */
    protected boolean backgroundSkyChanged;

    /** Flag to say the background ground values changed */
    protected boolean backgroundGroundChanged;

    /** Listener instance for dealing with the current background node */
    protected BackgroundListener backgroundListener;

    /** Listener instance for dealing with the current fog node */
    protected FogListener fogListener;

    /** The world scale */
    protected float worldScale;
    protected float invWorldScale;

    /** The number of visibility sensors */
    protected int numVisibilityListeners;

    /** The number of proximity sensors */
    protected int numAreaListeners;

    /** The number of pointing device sensors */
    protected int numPointingDeviceSensors;

    /** The identifier of this layer manager. Defaults to the invalid -1 value */
    protected int layerId;

    /** The collection of all sensors currently managed by this sensor manager */
    protected HashSet allSensorNodes;

    //
    // Inner classes to do with the field callbacks. VRMLNodeListener doesn't
    // include a Node reference so we can't tell which field really got called.
    // The inner classes are used to set the various *Changed booleans above so
    // that during the next iteration of the event model the values can be
    // updated when the flags change. This is more efficient than performing a
    // check of every value from the last frame compared to the values from
    // this frame.
    //

    /**
     * Inner class for handling background field changes.
     */
    private class BackgroundListener implements VRMLNodeListener {

        /** Field index for the ground angle.*/
        private int gndAngleField;

        /** Field index for the ground colour.*/
        private int gndColorField;

        /** Field index for the ground angle.*/
        private int skyAngleField;

        /** Field index for the ground colour.*/
        private int skyColorField;

        /**
         * Construct a field listener that uses the given index values for
         * the appropriate field.
         */
        BackgroundListener(int gndAngle,
                           int gndColor,
                           int skyAngle,
                           int skyColor) {

            gndAngleField = gndAngle;
            gndColorField = gndColor;
            skyAngleField = skyAngle;
            skyColorField = skyColor;
        }

        public void fieldChanged(int index) {
            if(index == gndAngleField || index == gndColorField)
                backgroundGroundChanged = true;

            if(index == skyAngleField || index == skyColorField)
                backgroundSkyChanged = true;
        }
    }

    /**
     * Inner class for handling fog field changes.
     */
    private class FogListener implements VRMLNodeListener {

        /** Field index for the fog type.*/
        private int typeField;

        /** Field index for the visibility range.*/
        private int visibilityField;

        /** Field index for the colour.*/
        private int colourField;

        /**
         * Construct a field listener that uses the given index values for
         * the appropriate field.
         */
        FogListener(int type, int visibility, int colour) {

            typeField = type;
            visibilityField = visibility;
            colourField = colour;
        }

        public void fieldChanged(int index) {
            if(index == typeField)
                fogTypeChanged = true;

            if(index == visibilityField || index == colourField)
                fogDetailsChanged = true;
        }
    }

    /**
     * Create a new default instance of the manager. It will only register a
     * handler for TimeSensors. Anything other than that will require the end
     * user code to register an appropriate manager.
     */
    public BaseLayerSensorManager() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        worldScale = 1;
        invWorldScale = 1;

        layerId = -1;
        navigationEnabled = true;
        initialised = false;
        isPickable = true;

        allSensorNodes = new HashSet();
    }

    //-------------------------------------------------------------
    // Methods defined by LayerSensorManager
    //-------------------------------------------------------------

    /**
     * Get the ID of the layer this sensor manager works with. The ID is the rendering
     * order, starting from 0. Once set, this shouldn't change.
     */
    public int getLayerId() {
        return layerId;
    }

    /**
     * Get the currently set navigation state.
     *
     * @return true for the current state
     */
    public boolean getNavigationEnabled() {
        return navigationEnabled;
    }

    /**
     * Enable or disable navigation processing sub-section of the
     * user input processing. By default the navigation processing is enabled.
     *
     * @param state true to enable navigation
     */
    public void setNavigationEnabled(boolean state) {
        navigationEnabled = state;

        inputHandler.setNavigationEnabled(state);
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
        return inputHandler;
    }

    /**
     * See if this layer is currently pickable.
     *
     * @return true if the contents of this layer can be picked
     */
    public boolean getIsPickable() {
        return isPickable;
    }

    /**
     * Set if this layer is currently pickable.
     *
     * @param val if the contents of this layer can be picked
     */
    public void setIsPickable(boolean val) {
        isPickable = val;
    }

    /**
     * Set the user input manager to be used by this implementation. User input
     * is generally independent of the main render loop. A value of null may be
     * used to clear the currently set manager and make the handler not process
     * user input.
     *
     * @param manager The input manager instance to use
     */
    public void setInputManager(InputDeviceManager manager) {
        inputManager = manager;

        if(manager != null)
            manager.setErrorReporter(errorReporter);
    }

    /**
     * Set the VRML clock instance that we are using so that we can set the
     * bind time information for nodes that require it. A value of null can
     * be used to clear the current clock instance.
     *
     * @param clk The new clock to set
     */
    public void setVRMLClock(VRMLClock clk) {
        inputHandler.setVRMLClock(clk);
    }

    /**
     * Set the manager that is responsible for handling picking sensors.
     *
     * @param picker Reference to the manager instance to use or null
     */
    public void setPickingManager(PickingManager picker) {
        pickManager = picker;

        if(picker != null)
            picker.setErrorReporter(errorReporter);
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
        int[] secondaryTypes;

        for(int i = 0; i < size; i++) {
            VRMLSensorNodeType node =
                (VRMLSensorNodeType)sensors.get(i);

            // ignore ones that are not in our layer or that we have
            // already processed.
            if(!checkLayerId(node) || allSensorNodes.contains(node))
                continue;

            allSensorNodes.add(node);

            if(node instanceof AreaListener)
                numAreaListeners++;
            else if(node instanceof VisibilityListener)
                numVisibilityListeners++;

            switch(node.getPrimaryType()) {
                case TypeConstants.DragSensorNodeType:
                case TypeConstants.LinkNodeType:
                case TypeConstants.PointingDeviceSensorNodeType:
                    numPointingDeviceSensors++;
                    break;

                case TypeConstants.KeyDeviceSensorNodeType:
                case TypeConstants.TimeDependentNodeType:
                case TypeConstants.TimeControlledNodeType:
                    // Do nothing. Handled at the global level
                    break;

                case TypeConstants.DeviceSensorNodeType:
                    inputManager.addX3DNode((VRMLDeviceSensorNodeType)node);
                    break;

                 case TypeConstants.SensorNodeType:
                 case TypeConstants.EnvironmentalSensorNodeType:
                    // Do nothing. Handled at the global level
                     break;

                case TypeConstants.PickingSensorNodeType:
                    pickManager.addSensor((VRMLPickingSensorNodeType)node);
                    break;

                default:
                    errorReporter.warningReport(ADD_SENSOR_UNKNOWN_TYPE +
                                                node.getVRMLNodeName(), null);
            }
        }

        if(numPointingDeviceSensors > 0)
            inputHandler.setTestPointingDevices(true);
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
        int[] secondaryTypes;

        for(int i = 0; i < size; i++) {
            VRMLSensorNodeType node = (VRMLSensorNodeType)sensors.get(i);

            // Removing it should already have the layer ID removed from the
            // node, so the first call should return false (if it returns true
            // we're probably screwed in some really bad way anyway). The 2nd
            // check is for it not being registered here. If it is registered,
            // then we really do want to process it because we now need to
            // remove it.
            if(!checkLayerId(node) && !allSensorNodes.contains(node))
                continue;

            allSensorNodes.remove(node);

            if(node instanceof AreaListener)
                numAreaListeners--;
            else if(node instanceof VisibilityListener)
                numVisibilityListeners--;

            switch(node.getPrimaryType()) {
                case TypeConstants.DragSensorNodeType:
                case TypeConstants.LinkNodeType:
                case TypeConstants.PointingDeviceSensorNodeType:
                    numPointingDeviceSensors--;

                    break;

                case TypeConstants.KeyDeviceSensorNodeType:
                case TypeConstants.TimeDependentNodeType:
                case TypeConstants.TimeControlledNodeType:
                    // Do nothing. Handled at the global level
                    break;

                case TypeConstants.PickingSensorNodeType:
                    pickManager.removeSensor((VRMLPickingSensorNodeType)node);
                    break;

                case TypeConstants.DeviceSensorNodeType:
                    inputManager.removeX3DNode((VRMLDeviceSensorNodeType)node);
                    break;

                case TypeConstants.SensorNodeType:
                case TypeConstants.EnvironmentalSensorNodeType:
                    // Do nothing. Handled at the global level
                    break;

                default:
                    errorReporter.warningReport(REMOVE_SENSOR_UNKNOWN_TYPE +
                                                node.getVRMLNodeName(), null);
            }
        }

        if(numPointingDeviceSensors == 0)
            inputHandler.setTestPointingDevices(false);
    }

    /**
     * Add view-dependent nodes that need to be updated each frame based on
     * the user's position for rendering. These are not sensors nodes, but
     * others like Billboard, LOD etc.
     *
     * @param nodes List of nodes that need to be processed
     */
    public void addViewDependentNodes(NodeArray nodes) {
        processAddedSensors(nodes);
    }

    /**
     * Remove these view-dependent nodes from the scene.
     *
     * @param nodes List of nodes to be removed
     */
    public void removeViewDependentNodes(NodeArray nodes) {
        processRemovedSensors(nodes);
    }

    /**
     * Load the contents of this scene into the sensor manager. The call does
     * not need to be recursive as it is expected the external caller will
     * work with this.
     *
     * @param scene The scene to source data from
     */
    public void loadScene(BasicScene scene) {
/*
        if(scene.getLayerId() != layerId)
            return;
*/
        ArrayList nodes =
            scene.getByPrimaryType(TypeConstants.EnvironmentalSensorNodeType);
        processAddedSensors(nodes);

        nodes =
            scene.getBySecondaryType(TypeConstants.ViewDependentNodeType);
        processAddedSensors(nodes);

        nodes =
            scene.getByPrimaryType(TypeConstants.PointingDeviceSensorNodeType);
        processAddedPointingDeviceSensors(nodes);

        nodes = scene.getByPrimaryType(TypeConstants.DragSensorNodeType);
        processAddedPointingDeviceSensors(nodes);

        nodes = scene.getBySecondaryType(TypeConstants.LinkNodeType);
        processAddedPointingDeviceSensors(nodes);

        nodes = scene.getByPrimaryType(TypeConstants.DeviceSensorNodeType);
        int size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLDeviceSensorNodeType node = (VRMLDeviceSensorNodeType) nodes.get(i);
            inputManager.addX3DNode(node);
        }

        inputHandler.setTestPointingDevices(numPointingDeviceSensors != 0);

        if(pickManager != null)
            pickManager.loadScene(scene);
    }

    /**
     * UnLoad the contents of this scene into the sensor manager. The call does
     * not need to be recursive as it is expected the external caller will
     * work with this.
     *
     * @param scene The scene to source data from
     */
    public void unloadScene(BasicScene scene) {
        if(scene.getLayerId() != layerId)
            return;

        if(pickManager != null)
            pickManager.unloadScene(scene);

        ArrayList nodes =
            scene.getByPrimaryType(TypeConstants.DeviceSensorNodeType);

        int i;
        int size = nodes.size();
        for(i = 0; i < size; i++) {
            VRMLDeviceSensorNodeType node = (VRMLDeviceSensorNodeType) nodes.get(i);
            inputManager.removeX3DNode(node);
        }

        nodes =
            scene.getByPrimaryType(TypeConstants.EnvironmentalSensorNodeType);
        processRemovedSensors(nodes);

        nodes =
            scene.getBySecondaryType(TypeConstants.ViewDependentNodeType);
        processRemovedSensors(nodes);

        nodes =
            scene.getByPrimaryType(TypeConstants.PointingDeviceSensorNodeType);
        processRemovePointingDeviceSensors(nodes);

        nodes = scene.getByPrimaryType(TypeConstants.DragSensorNodeType);
        processRemovePointingDeviceSensors(nodes);

        nodes = scene.getBySecondaryType(TypeConstants.LinkNodeType);
        processRemovePointingDeviceSensors(nodes);

        inputHandler.setTestPointingDevices(numPointingDeviceSensors != 0);
    }

    /**
     * Force clearing all state from this manager now. This is used to indicate
     * that a new world is about to be loaded and everything should be cleaned
     * out now.
     */
    public void clear() {
        numAreaListeners = 0;
        numVisibilityListeners = 0;
        numPointingDeviceSensors = 0;

        inputHandler.setNavigationEnabled(false);
        inputHandler.setNavigationInfo(null);

        // Clear all the stacks.
        fogStack.clearAll();
        viewStack.clearAll();
        navInfoStack.clearAll();
        backgroundStack.clearAll();

        allSensorNodes.clear();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the layer Id to a new value. This should only be called once as
     * reseting the ID to something else when nobody is expecting it, may
     * cause problems.
     *
     * @param id The ID to set
     * @throws IllegalStateException The id has already been set once
     */
    public void setLayerId(int id) throws IllegalStateException {
        layerId = id;
    }

    /**
     * One-shot to initialise the internals structures once the event model
     * has started to kick in.
     */
    protected void initialise() {
        initialised = true;

        // Grab the background and fog nodes and setup the fields and
        // listener instances for them.
        VRMLNodeType node = fogStack.getBoundNode();

        int fog_type = node.getFieldIndex("fogType");
        int fog_vis = node.getFieldIndex("visibilityRange");
        int fog_colour = node.getFieldIndex("color");

        fogListener = new FogListener(fog_type, fog_vis, fog_colour);

        node = backgroundStack.getBoundNode();
        int gnd_angle = node.getFieldIndex("groundAngle");
        int gnd_color = node.getFieldIndex("groundColor");
        int sky_angle = node.getFieldIndex("skyAngle");
        int sky_color = node.getFieldIndex("skyColor");

        backgroundListener =
            new BackgroundListener(gnd_angle, gnd_color, sky_angle, sky_color);
    }

    /**
     * Clear the flags currently set because the listener has changed or been
     * processed.
     */
    protected void clearChangeFlags() {
        fogTypeChanged = false;
        fogDetailsChanged = false;
        backgroundSkyChanged = false;
        backgroundGroundChanged = false;
    }

    /**
     * Check the given node for its set of Layer IDs for whether it has one
     * that matches this layer ID. If it does, return true, otherwise return
     * false.
     *
     * @param node The node to check against
     * @return true if the node belongs in this layer
     */
    private boolean checkLayerId(VRMLNodeType node) {
        int[] layerIds = node.getLayerIds();

        if(layerIds == null)
            return false;

        for(int i = 0; i < layerIds.length; i++)
            if(layerIds[i] == layerId)
                return true;

        return false;
    }

    /**
     * Run through the node list and check for area and vis sensors.
     *
     * @param nodes The list of nodes to check
     */
    private void processAddedSensors(NodeArray nodes) {
        int size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLNodeType node = (VRMLNodeType)nodes.get(i);

            // If it is not registered for this layer, or it is and we
            // already have it registered, ignore it.
            if(!checkLayerId(node) || allSensorNodes.contains(node))
                continue;

            if(node instanceof AreaListener)
                numAreaListeners++;
            else if(node instanceof VisibilityListener)
                numVisibilityListeners++;
        }
    }

    /**
     * Run through the node list and check for area and vis sensors.
     *
     * @param nodes The list of nodes to check
     */
    private void processAddedPointingDeviceSensors(ArrayList nodes) {
        int size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLNodeType node = (VRMLNodeType)nodes.get(i);

            // If it is not registered for this layer, or it is and we
            // already have it registered, ignore it.
            if(!checkLayerId(node) || allSensorNodes.contains(node))
                continue;

            numPointingDeviceSensors++;
        }
    }

    /**
     * Run through the node list and check for area and vis sensors.
     *
     * @param nodes The list of nodes to check
     */
    private void processAddedSensors(ArrayList nodes) {
        int size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLNodeType node = (VRMLNodeType)nodes.get(i);

            // If it is not registered for this layer, or it is and we
            // already have it registered, ignore it.
            if(!checkLayerId(node) || allSensorNodes.contains(node))
                continue;

            if(node instanceof AreaListener)
                numAreaListeners++;
            else if(node instanceof VisibilityListener)
                numVisibilityListeners++;
        }
    }

    /**
     * Run through the node list and check for area and vis sensors,
     * decrementing the two variables responsible.
     *
     * @param nodes The list of nodes to check
     */
    private void processRemovedSensors(NodeArray nodes) {
        int size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLNodeType node = (VRMLNodeType)nodes.get(i);

            // Removing it should already have the layer ID removed from the
            // node, so the first call should return false (if it returns true
            // we're probably screwed in some really bad way anyway). The 2nd
            // check is for it not being registered here. If it is registered,
            // then we really do want to process it because we now need to
            // remove it.
            if(!checkLayerId(node) && !allSensorNodes.contains(node))
                continue;

            allSensorNodes.remove(node);
            if(node instanceof AreaListener)
                numAreaListeners--;
            else if(node instanceof VisibilityListener)
                numVisibilityListeners--;
        }
    }

    /**
     * Run through the node list and check for area and vis sensors,
     * decrementing the two variables responsible.
     *
     * @param nodes The list of nodes to check
     */
    private void processRemovedSensors(ArrayList nodes) {
        int size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLNodeType node = (VRMLNodeType)nodes.get(i);

            // Removing it should already have the layer ID removed from the
            // node, so the first call should return false (if it returns true
            // we're probably screwed in some really bad way anyway). The 2nd
            // check is for it not being registered here. If it is registered,
            // then we really do want to process it because we now need to
            // remove it.
            if(!checkLayerId(node) && !allSensorNodes.contains(node))
                continue;

            allSensorNodes.remove(node);
            if(node instanceof AreaListener)
                numAreaListeners--;
            else if(node instanceof VisibilityListener)
                numVisibilityListeners--;
        }
    }

    /**
     * Run through the node list and check for pointing device sensors
     * decrementing the variable responsible.
     *
     * @param nodes The list of nodes to check
     */
    private void processRemovePointingDeviceSensors(ArrayList nodes) {
        int size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLNodeType node = (VRMLNodeType)nodes.get(i);

            // Removing it should already have the layer ID removed from the
            // node, so the first call should return false (if it returns true
            // we're probably screwed in some really bad way anyway). The 2nd
            // check is for it not being registered here. If it is registered,
            // then we really do want to process it because we now need to
            // remove it.
            if(!checkLayerId(node) && !allSensorNodes.contains(node))
                continue;

            allSensorNodes.remove(node);

            numPointingDeviceSensors--;
        }
    }

}
