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

package org.xj3d.impl.core.eventmodel;

// External imports
import org.odejava.ode.*;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.web3d.util.HashSet;
import org.web3d.util.IntHashMap;

import org.odejava.Body;
import org.odejava.PlaceableGeom;
import org.odejava.Odejava;
import org.odejava.World;


// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.ComponentInfo;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.util.NodeArray;

import org.xj3d.core.eventmodel.NodeManager;

/**
 * Manager for the rigid body physics model nodes.
 * <p>
 *
 * Keeps track of both the collections of bodies and the individual joints.
 * The collection nodes are evaluated at the end of the frame so as to modify
 * the final object locations for this frame. The joints are managed so that
 * those that need to produce output will be evaluated at the start of the
 * next frame. This requires the manager to register as both pre and post
 * event model node manager, as it manages both sets of nodes, though
 * independently.
 * <p>
 *
 * The physics model is run at a somewhat fixed frame rate. Physics models
 * don't like to have variable frame rate as input, so we smooth these out
 * over a fixed number of frames. Every set of frame resets the calculation
 * interval based on recent history.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class DefaultRigidBodyPhysicsManager implements NodeManager {

    /** List of managed node types */
    private static final int[] MANAGED_NODE_TYPES = {
        TypeConstants.RigidBodyCollectionNodeType,
        TypeConstants.RigidJointNodeType,
        TypeConstants.RigidBodyNodeType,
        TypeConstants.nBodyCollidableNodeType,
        TypeConstants.nBodyCollisionCollectionNodeType,
        TypeConstants.nBodyCollisionSensorNodeType
    };

    /** Average out the timesteps every so often */
    private static final int RECALC_INTERVAL = 10;

    /** No ODE, so don't do anything */
    private static boolean odeLoadFailed;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Manager for all the RigidBodyCollection nodes here */
    private NodeArray collections;

    /** The updated set of collections */
    private HashSet collectionSet;

    /** Manager for all the joint nodes here */
    private NodeArray joints;

    /** The updated set of collections */
    private HashSet jointSet;

    /** Manager for all the body nodes here */
    private NodeArray bodies;

    /** The updated set of collections */
    private HashSet bodySet;

    /** Manager for the nbody collidable nodes */
    private NodeArray collidables;

    /** The updated set of collidables */
    private HashSet collidableSet;

    /** Manager for the nbody collision space nodes */
    private NodeArray collisionSpaces;

    /** The updated set of collision spaces */
    private HashSet collisionSpaceSet;

    /** Manager for the nbody sensor nodes */
    private NodeArray sensors;

    /** The updated set of sensors */
    private HashSet sensorSet;

    /** Time in seconds this was last called. The dT passed to ODE. */
    private long lastTime;

    /** Elapsed time since the last recalc interval */
    private long elapsedTime;

    /** The current counter in the recalc time */
    private int countTick;

    /** The current deltaT calculated to feed to the physics model */
    private float deltaT;

    /** The clock used to reinitialise time with */
    private VRMLClock clock;

    /** Map of the native address of a body to the VRML node */
    private IntHashMap bodyIdMap;

    /** Map of the native address of a geometry to the VRML node */
    private IntHashMap geomIdMap;

    /**
     * Static initialiser to take care of the loading of ODE.
     */
    static {
        try {
            Boolean val = (Boolean)AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() {
                        Boolean ret_val = Boolean.FALSE;

                        try {
                            if(!Odejava.init())
                                ret_val = Boolean.TRUE;
                        } catch(NoClassDefFoundError ncdfe) {
                            System.err.println("Unable to initialise ODE due " +
                                               "to missing class definitions: " +
                                               ncdfe.getMessage());
                            ret_val = Boolean.TRUE;
                        }

                        return ret_val;
                    }
                }
            );

            odeLoadFailed = val.booleanValue();
        } catch(PrivilegedActionException pae) {
            System.err.println("Failed to partake priviledged action to load " +
                               "odejava DLL");
        }
    }

    /**
     * Create a new, empty instance of the humanoid manager.
     */
    public DefaultRigidBodyPhysicsManager() {
        collections = new NodeArray();
        joints = new NodeArray();
        bodies = new NodeArray();
        collidables = new NodeArray();
        collisionSpaces = new NodeArray();
        sensors = new NodeArray();

        collectionSet = new HashSet();
        jointSet = new HashSet();
        bodySet = new HashSet();
        collidableSet = new HashSet();
        collisionSpaceSet = new HashSet();
        sensorSet = new HashSet();

        bodyIdMap = new IntHashMap();
        geomIdMap = new IntHashMap();

        countTick = 0;
        elapsedTime = 0;
        deltaT = 0.02f;
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //-------------------------------------------------------------
    // Methods defined by NodeManager
    //-------------------------------------------------------------

    /**
     * Initialise the node manager now with any per-manager setup that is
     * needed. If this returns false, then the node manager is assumed to have
     * failed some part of the setup and will be removed from the system
     *
     * @return true if initialisation was successful
     */
    public boolean initialize() {
        return !odeLoadFailed;
    }

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown() {
        if(!odeLoadFailed) {
            Ode.dCloseODE();
            odeLoadFailed = true;
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
     * Get the list of component names that this manager would normally manage.
     * The component definition is asssumed to be the same across all versions
     * of the specifications that the browser supports. The level of the
     * component is assumed to be the lowest level supported (ie if the given
     * level fails, then levels above this cannot be supported, but those below
     * can still be).
     * <p>
     * Mostly this is used for when initialisation fails and we wish to disable
     * support for loading of nodes in that component.
     *
     * @return The collection of components that this manager supports
     */
    public ComponentInfo[] getSupportedComponents() {
        return new ComponentInfo[] {
            new ComponentInfo("xj3d_RigidBodyPhysics", 1)
        };
    }

    /**
     * Set the VRMLClock instance in use by this manager. Ignored for this
     * manager.
     *
     * @param clk A reference to the clock to use
     */
    public void setVRMLClock(VRMLClock clk) {
        clock = clk;
        lastTime = clk.getWallTime();
    }

    /**
     * Reset the local time zero for the manager. This is called when a new
     * root world has been loaded and any manager that needs to rely on delta
     * time from the start of the world loading can reset it's local reference
     * from the passed in {@link VRMLClock} instance.
     */
    public void resetTimeZero() {
        lastTime = clock.getWallTime();
        countTick = 0;
        elapsedTime = 0;
        deltaT = 0.02f;
    }

    /**
     * Get the list of node type IDs that this manager wants to handle. These
     * should be the constants from {@link org.web3d.vrml.lang.TypeConstants}.
     *
     * @return A list of managed node identifiers
     */
    public int[] getManagedNodeTypes() {
        return MANAGED_NODE_TYPES;
    }

    /**
     * Ask whether this manager should run before the event model has been
     * evaluated for this frame.
     *
     * @return true if this is pre event model, false otherwise
     */
    public boolean evaluatePreEventModel() {
        if(odeLoadFailed)
            return false;

        return true;
    }

    /**
     * Ask whether this manager should run after the event model has been
     * evaluated for this frame.
     *
     * @return true if this is post event model, false otherwise
     */
    public boolean evaluatePostEventModel() {
        if(odeLoadFailed)
            return false;

        return true;
    }

    /**
     * Add a node of the require type to be managed.
     *
     * @param node The node instance to add for management
     */
    public void addManagedNode(VRMLNodeType node) {
        int type = node.getPrimaryType();

        switch(type) {
            case TypeConstants.RigidBodyCollectionNodeType:
                if(!collectionSet.contains(node)) {
                    collections.add(node);
                    collectionSet.add(node);
                    ((VRMLRigidBodyGroupNodeType)node).setTimestep(deltaT);
                }
                break;

            case TypeConstants.RigidJointNodeType:
                if(!jointSet.contains(node)) {
                    joints.add(node);
                    jointSet.add(node);
                }
                break;

            case TypeConstants.RigidBodyNodeType:
                if(!bodySet.contains(node)) {
                    bodies.add(node);
                    bodySet.add(node);

                    // Fetch the ID of the node and register it in the map.
                    VRMLRigidBodyNodeType r_body =
                        (VRMLRigidBodyNodeType)node;
                    Body body = r_body.getODEBody();
                    int addr = body.getNativeAddr();
                    bodyIdMap.put(addr, r_body);
                }
                break;

            case TypeConstants.nBodyCollidableNodeType:
                if(!collidableSet.contains(node)) {
                    collidables.add(node);
                    collidableSet.add(node);

                    // Fetch the ID of the node and register it in the map.
                    VRMLNBodyCollidableNodeType coll =
                        (VRMLNBodyCollidableNodeType)node;
                    PlaceableGeom geom = coll.getODEGeometry();
                    int addr = geom.getNativeAddr();
                    geomIdMap.put(addr, coll);
                }
                break;

            case TypeConstants.nBodyCollisionSensorNodeType:
                if(!sensorSet.contains(node)) {
                    sensors.add(node);
                    sensorSet.add(node);
                }
                break;

            case TypeConstants.nBodyCollisionCollectionNodeType:
                if(!collisionSpaceSet.contains(node)) {
                    collisionSpaces.add(node);
                    collisionSpaceSet.add(node);
                }
                break;

            default:
                errorReporter.warningReport("Non-Physics node added to the manager",
                                            null);
        }
    }

    /**
     * Remove a node of the require type to be managed.
     *
     * @param node The node instance to add for management
     */
    public void removeManagedNode(VRMLNodeType node) {
        int type = node.getPrimaryType();

        switch(type) {
            case TypeConstants.RigidBodyCollectionNodeType:
                collections.remove(node);
                collectionSet.remove(node);
                break;

            case TypeConstants.RigidBodyNodeType:
                bodies.remove(node);
                bodySet.remove(node);

                // Fetch the ID of the node and register it in the map.
                VRMLRigidBodyNodeType r_body =
                    (VRMLRigidBodyNodeType)node;
                Body body = r_body.getODEBody();
                int addr = body.getNativeAddr();
                bodyIdMap.remove(addr);
                break;

            case TypeConstants.RigidJointNodeType:
                joints.remove(node);
                jointSet.remove(node);

                VRMLNBodyCollidableNodeType coll =
                    (VRMLNBodyCollidableNodeType)node;
                PlaceableGeom geom = coll.getODEGeometry();
                addr = geom.getNativeAddr();
                geomIdMap.remove(addr);
                break;

            case TypeConstants.nBodyCollidableNodeType:
                collidables.remove(node);
                collidableSet.remove(node);
                break;

            case TypeConstants.nBodyCollisionSensorNodeType:
                sensors.remove(node);
                sensorSet.remove(node);
                break;

            case TypeConstants.nBodyCollisionCollectionNodeType:
                collisionSpaces.remove(node);
                collisionSpaceSet.remove(node);
                break;

            default:
                errorReporter.warningReport("Non-physics node removed from the manager",
                                            null);
        }
    }

    /**
     * Run the pre-event modelling for this frame now. This is a blocking call
     * and does not return until the event model is complete for this frame.
     * The time should be system clock time, not VRML time.
     *
     * @param time The timestamp of this frame to evaluate
     */
    public void executePreEventModel(long time) {

        double dt = (time - lastTime);

        // Avoid div-zero issues in the physics model.
        if(dt == 0)
            return;

        // First evaluate collision detection. Use the values from here to
        // feed values into the physics model.
        int size = collisionSpaces.size();

        for(int i = 0; i < size; i++) {
            VRMLNBodyGroupNodeType group =
                (VRMLNBodyGroupNodeType)collisionSpaces.get(i);

            if(group.isEnabled())
                group.evaluateCollisions();
        }


        // Update the joint outputs
        size = joints.size();

        for(int i = 0; i < size; i++) {
            VRMLRigidJointNodeType joint =
                (VRMLRigidJointNodeType)joints.get(i);

            if(joint.numOutputs() != 0)
                joint.updateRequestedOutputs();
        }

        // Finally have the sensors dump their stuff.
        size = sensors.size();
        for(int i = 0; i < size; i++) {
            VRMLNBodySensorNodeType sensor =
                (VRMLNBodySensorNodeType)sensors.get(i);

            sensor.updateContacts(bodyIdMap, geomIdMap);
        }

        size = collections.size();

        for(int i = 0; i < size; i++) {
            VRMLRigidBodyGroupNodeType group =
                (VRMLRigidBodyGroupNodeType)collections.get(i);

            if(group.isEnabled())
                group.updatePostSimulation();
        }
    }

    /**
     * Run the post-event modelling for this frame now. This is a blocking call
     * and does not return until the event model is complete for this frame.
     * The time should be system clock time, not VRML time.
     *
     * @param time The timestamp of this frame to evaluate
     */
    public void executePostEventModel(long time) {

        int size = collections.size();

        if(++countTick == RECALC_INTERVAL) {
            deltaT = (elapsedTime / (float)countTick) * 0.001f;
            countTick = 0;
            elapsedTime = 0;

            for(int i = 0; i < size; i++) {
                VRMLRigidBodyGroupNodeType group =
                    (VRMLRigidBodyGroupNodeType)collections.get(i);

                group.setTimestep(deltaT);
            }
        } else {
            elapsedTime += time - lastTime;
        }

        lastTime = time;

        // Do stuff here to push the collision stuff over to the physics
        // model.

        for(int i = 0; i < size; i++) {
            VRMLRigidBodyGroupNodeType group =
                (VRMLRigidBodyGroupNodeType)collections.get(i);

            if(group.isEnabled()) {
                group.processInputContacts();
                group.evaluateModel();
            }
        }

        size = collidables.size();

        for(int i = 0; i < size; i++) {
            VRMLNBodyCollidableNodeType geom =
                (VRMLNBodyCollidableNodeType)collidables.get(i);

            geom.updateFromODE();
        }
    }

    /**
     * Force clearing all currently managed nodes from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    public void clear() {
        int size = joints.size();

        for(int i = 0; i < size; i++) {
            VRMLRigidJointNodeType joint =
                (VRMLRigidJointNodeType)joints.get(i);

            joint.delete();
        }

        size = collisionSpaces.size();

        for(int i = 0; i < size; i++) {
            VRMLNBodyGroupNodeType group =
                (VRMLNBodyGroupNodeType)collisionSpaces.get(i);

            group.delete();
        }

        size = collections.size();

        for(int i = 0; i < size; i++) {
            VRMLRigidBodyGroupNodeType group =
                (VRMLRigidBodyGroupNodeType)collections.get(i);

            group.delete();
        }

        collections.clear();
        joints.clear();
        bodies.clear();
        collidables.clear();
        collisionSpaces.clear();
        sensors.clear();

        collectionSet.clear();
        jointSet.clear();
        bodySet.clear();
        collidableSet.clear();
        collisionSpaceSet.clear();
        sensorSet.clear();

        bodyIdMap.clear();
        geomIdMap.clear();
    }
}
