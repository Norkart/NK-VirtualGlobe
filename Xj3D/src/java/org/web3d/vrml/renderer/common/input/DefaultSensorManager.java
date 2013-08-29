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
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

// Local imports
import org.web3d.vrml.nodes.*;

import org.xj3d.core.eventmodel.*;

import org.xj3d.impl.core.eventmodel.RealTimeSensorManager;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.IntHashMap;
import org.web3d.util.ObjectArray;

import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.TypeConstants;

import org.web3d.vrml.util.NodeArray;

import org.web3d.vrml.renderer.common.nodes.AreaListener;
import org.web3d.vrml.renderer.common.nodes.VisibilityListener;

/**
 * Common implementation of the SensorManager interface for all renderers.
 * <p>
 *
 * This base class handles the basic management needs of the sensor manager,
 * such as sorting and processing the various sensor types. Renderer-specific
 * extensions then process the sensors according to their specific needs.
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public class DefaultSensorManager implements SensorManager {
    
    /** Message for when addSensor has an unknown sensor type supplied */
    private static final String ADD_SENSOR_UNKNOWN_TYPE =
        "An unknown sensor type has been added to the sensor manager: ";
    
    /** Message for when addSensor has an unknown sensor type supplied */
    private static final String REMOVE_SENSOR_UNKNOWN_TYPE =
        "An unknown sensor type has been removed from the sensor manager: ";
    
    /** Manager of TimeSensor nodes */
    protected TimeSensorManager timeSensors;
    
    /** Manager of key devices */
    protected KeyDeviceSensorManager keySensors;
    
    /** Picking manager for intersection testing */
    protected PickingManager pickManager;
    
    /** Managers for each layer keyed by layer number */
    protected LayerSensorManager[] layerManagers;
    
    /** Number of currently valid sensor layer managers */
    protected int numLayerManagers;
    
    /** Buffer for input events */
    protected InputDeviceManager inputManager;
    
    /** Reporter instance for handing out errors */
    protected ErrorReporter errorReporter;
    
    /** Flag to say if navigation handling should be disabled */
    protected boolean navigationEnabled;
    
    /** The world scale */
    protected float worldScale;
    protected float invWorldScale;
    
    /** The number of pointing device sensors */
    protected int numPointingDeviceSensors;
    
    /** The order that the layers are presented on screen visually */
    protected int[] renderOrder;
    
    /** The number of valid items in the render order list */
    protected int numRenderOrder;
    
    /**
     * Create a new default instance of the manager. It will only register a
     * handler for TimeSensors. Anything other than that will require the end
     * user code to register an appropriate manager.
     */
    public DefaultSensorManager() {
        this( null );
    }
    
    /**
     * Create a new default instance of the manager. It will only register a
     * handler for TimeSensors. Anything other than that will require the end
     * user code to register an appropriate manager.
     */
    public DefaultSensorManager( TimeSensorManager manager ) {
        errorReporter = DefaultErrorReporter.getDefaultReporter( );
        
        if ( manager != null ) {
            timeSensors = manager;
        } else {
            timeSensors = new RealTimeSensorManager( );
        }
        
        layerManagers = new LayerSensorManager[6];
        renderOrder = new int[1];
        
        worldScale = 1;
        invWorldScale = 1;
        numRenderOrder = 0;
        
        navigationEnabled = true;
    }
    
    //-------------------------------------------------------------
    // Methods defined by SensorManager
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
        
        if(inputManager != null)
            inputManager.setErrorReporter(errorReporter);
        
        if(keySensors != null)
            keySensors.setErrorReporter(errorReporter);
        
        if(pickManager != null)
            pickManager.setErrorReporter(errorReporter);
    }
    
    /**
     * Set the rendering order for all the layers. This is so that input
     * handling, such as drag sensors are evaluated in the correct order, from
     * front to back.
     *
     * @param order The index of the list of rendered layers ids
     * @param numValid The number of valid items in the order list
     */
    public void setRenderOrder(int[] order, int numValid) {
        if(renderOrder.length < numValid)
            renderOrder = new int[numValid];
        
        int idx = 0;
        
        for(int i = 0; i < numValid; i++) {
            // Check for valid order values. Anything invalid we just skip and
            // ignore. We'll assume the LayerSet node has already issued an
            // error message about this, so we just silently continue on.
            if(order[i] < numLayerManagers)
                renderOrder[idx++] = order[i];
        }
        
        numRenderOrder = idx;
    }
    
    /**
     * Process the user input to the scene now. User input is the mouse and
     * keyboard processing that would be used to send events to
     * Key/StringSensors and perform navigation and picking duties as well as
     * adjust items like billboards and LODs.
     * <p>
     * The base class version calls the individual layer sensor managers and
     * the picking manager and time sensor manager to set the appropriate
     * clock tick time.  If you need to override for
     * renderer-specific capabilities, make sure you call this too.
     *
     * @param layerId The ID of the layer that is active for navigation
     * @param time The clock time, in Java coordinates, not VRML
     */
    public void processUserInput(int layerId, long time) {
        timeSensors.clockTick(time);
        
        for(int i = numRenderOrder - 1; i >= 0; i--) {
            layerManagers[renderOrder[i]].processUserInput(time);
        }
        
        if (inputManager != null) {
            inputManager.beginTrackerProcessing();
            
            int curr_layer;
            
            for(int i = numRenderOrder - 1; i >= 0; i--) {
                curr_layer = renderOrder[i];
                
                inputManager.processTrackers(curr_layer, (curr_layer == layerId),
                    layerManagers[curr_layer].getUserInputHandler(),
                    layerManagers[curr_layer].getIsPickable());
            }
            
            inputManager.endTrackerProcessing();
        }
        
        // process key events / sensors
        if(keySensors != null)
            keySensors.processEvents();
        
        if(pickManager != null)
            pickManager.processPickSensors(time);
    }
    
    /**
     * Add a per-layer manager to the sensor manager. Managers are added once,
     * and calling a manager for a layer ID will replace any manager at that
     * current ID.
     *
     * @param mgr The layer manager instance to add
     */
    public void addLayerSensorManager(LayerSensorManager mgr) {
        boolean found = false;
        for(int i = 0; i < numLayerManagers && !found; i++) {
            if(layerManagers[i].getLayerId() == mgr.getLayerId()) {
                layerManagers[i] = mgr;
                found = true;
            }
        }
        
        if(!found) {
            if(layerManagers.length == numLayerManagers) {
                LayerSensorManager[] tmp = new LayerSensorManager[numLayerManagers + 4];
                System.arraycopy(layerManagers, 0, tmp, 0, numLayerManagers);
                layerManagers = tmp;
            }
            
            layerManagers[numLayerManagers++] = mgr;
        }
        
        mgr.setErrorReporter(errorReporter);
        mgr.setInputManager(inputManager);
        mgr.setPickingManager(pickManager);
        
        // Do not set the VRML clock here because it is already passed into the
        // LayerSensorManager as part of the initialisation of the
        // LayerManager. It should already be set by the time you get here.
        // mgr.setVRMLClock(timeSensors);
    }
    
    /**
     * Remove this layer sensor manager from the system.
     *
     * @param mgr The layer manager instance to remove
     */
    public void removeLayerSensorManager(LayerSensorManager mgr) {
        for(int i = 0; i < numLayerManagers; i++) {
            if(layerManagers[i].getLayerId() == mgr.getLayerId()) {
                System.arraycopy(layerManagers,
                    i + 1,
                    layerManagers,
                    i,
                    numLayerManagers - i - 1);
                
                numLayerManagers--;
                break;
            }
        }
        
        mgr.setPickingManager(null);
        mgr.setInputManager(null);
        mgr.setErrorReporter(null);
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
        
        if(manager != null) {
            manager.setErrorReporter(errorReporter);
            inputManager.initialize();
        }
        
        for(int i = 0; i < numLayerManagers; i++)
            layerManagers[i].setInputManager(manager);
    }
    
    /**
     * Set the manager responsible for handling key device sensor. A value of
     * null may be used to clear the currently set manager and make the handler
     * not process user input.
     *
     * @param manager Reference to the manager instance to use or null
     */
    public void setKeyDeviceSensorManager( KeyDeviceSensorManager manager ) {
        keySensors = manager;
        
        if( manager != null ) {
            keySensors.setErrorReporter(errorReporter);
        }
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
            
            switch(node.getPrimaryType()) {
            case TypeConstants.PointingDeviceSensorNodeType:
            case TypeConstants.LinkNodeType:
            case TypeConstants.DragSensorNodeType:
                numPointingDeviceSensors--;
                break;
                
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
                
            case TypeConstants.DeviceSensorNodeType:
                inputManager.removeX3DNode((VRMLDeviceSensorNodeType)node);
                break;
                
            case TypeConstants.SensorNodeType:
            case TypeConstants.EnvironmentalSensorNodeType:
                // Proximity and visibility sensors may require a clock
                secondaryTypes=node.getSecondaryType();
                for(int j = 0; j < secondaryTypes.length; j++)
                    switch(secondaryTypes[j]) {
                    case TypeConstants.KeyDeviceSensorNodeType:
                        keySensors.removeSensor((VRMLKeyDeviceSensorNodeType)node);
                        break;
                        
                    case TypeConstants.TimeDependentNodeType:
                    case TypeConstants.TimeControlledNodeType:
                        ((VRMLTimeDependentNodeType)node).setVRMLClock(null);
                        break;
                    }
                
                break;
                
            default:
                errorReporter.warningReport(REMOVE_SENSOR_UNKNOWN_TYPE +
                    node.getVRMLNodeName(), null);
            }
        }
        
        for(int i = 0; i < numLayerManagers; i++)
            layerManagers[i].removeSensors(sensors);
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
            VRMLSensorNodeType node = (VRMLSensorNodeType)sensors.get(i);
            
            switch(node.getPrimaryType()) {
            case TypeConstants.PointingDeviceSensorNodeType:
            case TypeConstants.LinkNodeType:
            case TypeConstants.DragSensorNodeType:
                numPointingDeviceSensors++;
                
                // LAYERS:
                // This needs to be pushed down to the appropriate per-layer test
                //                    inputHandler.setTestPointingDevices(true);
                break;
                
            case TypeConstants.KeyDeviceSensorNodeType:
                keySensors.addSensor((VRMLKeyDeviceSensorNodeType)node);
                break;
                
            case TypeConstants.TimeDependentNodeType:
            case TypeConstants.TimeControlledNodeType:
                ((VRMLTimeDependentNodeType)node).setVRMLClock(timeSensors);
                break;
                
            case TypeConstants.DeviceSensorNodeType:
                inputManager.addX3DNode((VRMLDeviceSensorNodeType)node);
                break;
                
            case TypeConstants.SensorNodeType:
            case TypeConstants.EnvironmentalSensorNodeType:
                // Proximity and visibility sensors may require a clock
                secondaryTypes = node.getSecondaryType();
                for(int j = 0; j < secondaryTypes.length; j++)
                    switch(secondaryTypes[j]) {
                    case TypeConstants.KeyDeviceSensorNodeType:
                        keySensors.addSensor((VRMLKeyDeviceSensorNodeType)node);
                        break;
                    case TypeConstants.TimeDependentNodeType:
                    case TypeConstants.TimeControlledNodeType:
                        ((VRMLTimeDependentNodeType)node).setVRMLClock(timeSensors);
                        break;
                    }
                
                break;
                
            case TypeConstants.PickingSensorNodeType:
                pickManager.addSensor((VRMLPickingSensorNodeType)node);
                break;
                
            default:
                errorReporter.warningReport(ADD_SENSOR_UNKNOWN_TYPE +
                    node.getVRMLNodeName(), null);
            }
        }
        
        for(int i = 0; i < numLayerManagers; i++)
            layerManagers[i].addSensors(sensors);
    }
    
    /**
     * Add view-dependent nodes that need to be updated each frame based on
     * the user's position for rendering. These are not sensors nodes, but
     * others like Billboard, LOD etc.
     *
     * @param nodes List of nodes that need to be processed
     */
    public void addViewDependentNodes(NodeArray nodes) {
        for(int i = 0; i < numLayerManagers; i++)
            layerManagers[i].addViewDependentNodes(nodes);
    }
    
    /**
     * Remove these view-dependent nodes from the scene.
     *
     * @param nodes List of nodes to be removed
     */
    public void removeViewDependentNodes(NodeArray nodes) {
        for(int i = 0; i < numLayerManagers; i++)
            layerManagers[i].removeViewDependentNodes(nodes);
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
            scene.getByPrimaryType(TypeConstants.PointingDeviceSensorNodeType);
        numPointingDeviceSensors += nodes.size();
        
        nodes = scene.getByPrimaryType(TypeConstants.DragSensorNodeType);
        numPointingDeviceSensors += nodes.size();
        
        nodes = scene.getBySecondaryType(TypeConstants.LinkNodeType);
        numPointingDeviceSensors += nodes.size();
        
        nodes =
            scene.getByPrimaryType(TypeConstants.DeviceSensorNodeType);
        
        size = nodes.size();
        for(i = 0; i < size; i++) {
            VRMLDeviceSensorNodeType node =
                (VRMLDeviceSensorNodeType)nodes.get(i);
            inputManager.addX3DNode(node);
        }
        
        if(pickManager != null)
            pickManager.loadScene(scene);
        
        for(i = 0; i < numLayerManagers; i++) {
            //            if(layerManagers[i].getLayerId() == scene.getLayerId())
            layerManagers[i].loadScene(scene);
        }
    }
    
    /**
     * UnLoad the contents of this scene into the sensor manager. The call does
     * not need to be recursive as it is expected the external caller will
     * work with this.
     *
     * @param scene The scene to source data from
     */
    public void unloadScene(BasicScene scene) {
        ArrayList nodes =
            scene.getByPrimaryType(TypeConstants.KeyDeviceSensorNodeType);
        
        int i;
        int size = nodes.size();
        
        for(i = 0; i < size; i++)
            keySensors.removeSensor((VRMLKeyDeviceSensorNodeType)nodes.get(i));
        
        if(pickManager != null)
            pickManager.unloadScene(scene);
        
        nodes =
            scene.getByPrimaryType(TypeConstants.DeviceSensorNodeType);
        
        size = nodes.size();
        for(i = 0; i < size; i++) {
            VRMLDeviceSensorNodeType node =
                (VRMLDeviceSensorNodeType)nodes.get(i);
            inputManager.removeX3DNode(node);
        }
        
        nodes =
            scene.getByPrimaryType(TypeConstants.PointingDeviceSensorNodeType);
        numPointingDeviceSensors -= nodes.size();
        
        nodes = scene.getByPrimaryType(TypeConstants.DragSensorNodeType);
        numPointingDeviceSensors -= nodes.size();
        
        nodes = scene.getBySecondaryType(TypeConstants.LinkNodeType);
        numPointingDeviceSensors -= nodes.size();
        
        for(i = 0; i < numLayerManagers; i++) {
            if(layerManagers[i].getLayerId() == scene.getLayerId())
                layerManagers[i].unloadScene(scene);
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
        inputManager.clear();
        
        numPointingDeviceSensors = 0;
    }
    
    /**
     * Get the VRMLClock instance in use by this sensor manager.
     *
     * @return A reference to the clock
     */
    public VRMLClock getVRMLClock() {
        return timeSensors;
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
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
    }
}
