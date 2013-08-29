/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.input.dis;

// Local imports
import java.util.*;

import java.net.*;
import javax.vecmath.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

// External imports
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.util.PropertyTools;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.TypeConstants;

import org.xj3d.core.eventmodel.NetworkProtocolHandler;

import mil.navy.nps.net.*;
import mil.navy.nps.dis.*;
import mil.navy.nps.disEnumerations.PduTypeField;
import mil.navy.nps.math.Quaternion;

/**
 * The handler for DIS protocol network traffic.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.23 $
 */
public class DISProtocolHandler
    implements NetworkProtocolHandler, NetworkRoleListener {
    private static final String PROTOCOL = "DIS";

    /** The amount of time between inative checks */
    private static final int INACTIVE_CHECK_TIME = 1000;

    /** The amount of time before we mark a ESPDU as inactive */
    private static final int INACTIVE_TIME = 5000;

    /** The amount of time between heartbeats.   */
    private static final int HEARTBEAT_CHECK_TIME = 4500;

    /**
     * The default order.
     */
    private static final int DEFAULT_ORDER = 2;

    /**
     * The default convergence interval.
     */
    private static final int DEFAULT_CONVERGENCE_INTERVAL = 200;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** A map of open connections.  Only open one per address/port */
    private HashMap connections;

    /** A map of DIS nodes wrappers and their unique ID's  */
    private Map nodeMap;

    /** A map of DIS nodes wrappers and their unique ID's  */
    private Map writerMap;

    /** Live list variables */
    private LinkedList liveList;

    /** Nodes which want to write to the network */
    private LinkedList writerList;

    /** The last time we checked for inactive pdus */
    private long lastCheck;

    /** The last time we checked for heartbeats */
    private long lastHeartCheck;

    /** The list of managers */
    private List managerList;

    /** The Entities we've placed on the addedEntities */
    private Set notifiedSet;

    // Scratch vars to avoid gc.  Do not store DISId as a Map id, clone it
    private DISId disId;
    float[] tempPositionArray;
    float[] tempPositionArray2;
    float[] goalOrientation;
    private Quaternion  quaternion = null;
    private float[] rotation;
    private float[] currOrientation;
    private float[] positionArray;

    /** Whether we should smooth the DIS traffic */
    private boolean smooth = true;

   /** The default dead reckon position value */
    protected static final boolean DEFAULT_DEADRECKON_POSITION = true;

    /** Property describing the rescalling method to use */
    protected static final String DEADRECKON_POSITION_PROP =
        "org.web3d.vrml.renderer.common.dis.input.deadreckonPosition";

    /** The value read from the system property for MIPMAPS */
    protected static final boolean deadreckonPosition;

   /** The default dead reckon position value */
    protected static final boolean DEFAULT_DEADRECKON_ROTATION = true;

    /** Property describing the dead reckon */
    protected static final String DEADRECKON_ROTATION_PROP =
        "org.web3d.vrml.renderer.common.dis.input.deadreckonRotation";

    /** The value read from the system property for deadReckonRotation */
    protected static final boolean deadreckonRotation;

    // Scratch matrixes for smoothing
    Matrix3d rotationMatrix;
    Matrix3d psiMat;
    Matrix3d thetaMat;
    Matrix3d phiMat;
    Quat4d rotationQuat;
    Vector3d translationVec;
    Vector3d[] translationDerivatives;
    Vector3d[] rotationDerivatives;
    RungeKuttaSolver solver;
    AxisAngle4d axisTemp;

    static {
        deadreckonPosition = PropertyTools.fetchSystemProperty(DEADRECKON_POSITION_PROP,
                                         DEFAULT_DEADRECKON_POSITION);
        deadreckonRotation = PropertyTools.fetchSystemProperty(DEADRECKON_ROTATION_PROP,
                                         DEFAULT_DEADRECKON_ROTATION);
    }

    /**
     * Create a new instance of the execution space manager to run all the
     * routing.
     */
    public DISProtocolHandler() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        connections = new HashMap();
        nodeMap = Collections.synchronizedMap(new HashMap());
        writerMap = Collections.synchronizedMap(new HashMap());

        liveList = new LinkedList();
        writerList = new LinkedList();
        managerList = Collections.synchronizedList(new ArrayList());
        notifiedSet = Collections.synchronizedSet(new java.util.HashSet());
        disId = new DISId(0,0,0);
        tempPositionArray = new float[3];
        tempPositionArray2 = new float[3];
        goalOrientation = new float[3];

        rotation = new float[4];
        quaternion  = new Quaternion();
        positionArray = new float[3];
        currOrientation = new float[3];

        rotationMatrix = new Matrix3d();
        psiMat = new Matrix3d();
        thetaMat = new Matrix3d();
        phiMat = new Matrix3d();
        rotationQuat = new Quat4d();
        translationVec = new Vector3d();
        translationDerivatives = new Vector3d[]
        {
            new Vector3d(),
            new Vector3d()
        };

        rotationDerivatives = new Vector3d[]
        {
            new Vector3d()
        };

        axisTemp = new AxisAngle4d();

    }

    //----------------------------------------------------------
    // Methods required by NetworkProtocolHandler
    //----------------------------------------------------------

    /**
     * Get the protocol this handler supports.
     */
    public String getProtocol() {
        return PROTOCOL;
    }

    /**
     * Register an error reporter with the manager so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;
    }

    /**
     * Process network traffic now.
     */
    public void processNetworkTraffic() {
        long currTime = System.currentTimeMillis();
        boolean checkInactive = (currTime - lastCheck >= INACTIVE_CHECK_TIME);

        LiveListEntry node = (LiveListEntry)liveList.head;
        LiveListEntry last = (LiveListEntry)liveList.head;
        VRMLDISNodeType di;
        float dt;
        int numProcessed = 0;
        EntityStatePdu espdu;

        while(node != null) {
            di = (VRMLDISNodeType) node.node;

            if (node.newPackets) {
                if (node.currFire != null) {
                    di.packetArrived(node.currFire);
                    node.currFire = null;
                } else if (node.currDetonate != null) {
                    di.packetArrived(node.currDetonate);

                    // Stop dead reckon on detonate. If still alive it will send more updates.
                    node.closeEnough = true;
                    node.currDetonate = null;
                } else if (node.currEspdu != null) {
                    di.packetArrived(node.currEspdu);
                    espdu = node.currEspdu;

//System.out.println("***Real Pos: " + espdu.getEntityLocationX() + " " + espdu.getEntityLocationY() + " " + espdu.getEntityLocationZ());

                    rotationMatrix.setIdentity();

                    eulersToMatrix(
                        espdu.getEntityOrientationPhi(),
                        espdu.getEntityOrientationTheta(),
                        espdu.getEntityOrientationPsi(),
                        rotationMatrix
                    );

                    rotationQuat.set(rotationMatrix);

                    // Convert to normal coordinates

                    Vector3d translation = new Vector3d(
                        espdu.getEntityLocationX(),
                        -espdu.getEntityLocationZ(),
                        espdu.getEntityLocationY()
                    );

                    translationDerivatives[0].set(
                        espdu.getEntityLinearVelocityX(),
                        -espdu.getEntityLinearVelocityZ(),
                        espdu.getEntityLinearVelocityY()
                    );

                    rotationDerivatives[0].set(
                        espdu.getEntityAngularVelocityX(),
                        -espdu.getEntityAngularVelocityZ(),
                        espdu.getEntityAngularVelocityY()
                    );


                    translationDerivatives[1].set(
                        espdu.getEntityLinearAccelerationX(),
                        -espdu.getEntityLinearAccelerationZ(),
                        espdu.getEntityLinearAccelerationY()
                    );

                    node.translationConverger.convergeTo(
                        translation,
                        translationDerivatives,
                        currTime,
                        currTime
                    );

                    node.rotationConverger.convergeTo(
                        rotationQuat,
                        rotationDerivatives,
                        currTime,
                        currTime
                    );
                }

                node.newPackets = false;
                numProcessed++;
                // TODO:  Handle notification of other packet types
            }

            if (checkInactive && (currTime - node.lastTime >= INACTIVE_TIME)) {
                node.node.setIsActive(false);

                liveList.remove(node, last);

                int siteID = di.getSiteID();
                int appID = di.getAppID();
                int entityID = di.getEntityID();

                disId.setValue(siteID, appID, entityID);

                NodeMapEntry entry = (NodeMapEntry) nodeMap.get(disId);

                if (entry == null) {
                    System.out.println("DIS Entry null on timeout");
                } else {
                    entry.listEntry = null;
                    // TODO: I'm not sure we want to remove entries from the nodeMap
                    // Removing from the map means restored entities with this ID do not update
//                    nodeMap.remove(disId);

                    int len = managerList.size();

                    VRMLDISManagerNodeType manager;

                    for(int i=0; i < len; i++) {
                        manager = (VRMLDISManagerNodeType) managerList.get(i);

                        manager.entityRemoved(di);
                        notifiedSet.remove(disId);
                    }
                }
            }

            if ((deadreckonPosition || deadreckonRotation) && !node.closeEnough) {
                // Handle dead reckoning
                dt = (currTime - node.lastTime) * 0.001f;
//                dt = (currTime - node.lastTime) * 0.01f;


                if (Math.abs(node.currEspdu.getEntityLinearVelocityX()) <= 0.0001 &&
                    Math.abs(node.currEspdu.getEntityLinearVelocityY()) <= 0.0001 &&
                    Math.abs(node.currEspdu.getEntityLinearVelocityZ()) <= 0.0001 &&
                    Math.abs(node.currEspdu.getEntityLinearAccelerationX()) <= 0.0001 &&
                    Math.abs(node.currEspdu.getEntityLinearAccelerationY()) <= 0.0001 &&
                    Math.abs(node.currEspdu.getEntityLinearAccelerationZ()) <= 0.0001) {

                    node.closeEnough = true;
                }

                int idx;


                if (deadreckonPosition) {
                    node.translationConverger.getValue(currTime, translationVec);
                    tempPositionArray[0] = (float) translationVec.x;
                    tempPositionArray[1] = (float) translationVec.y;
                    tempPositionArray[2] = (float) translationVec.z;
                    idx = di.getFieldIndex("translation");
                    di.setValue(idx, tempPositionArray, 3);
                }

                if (deadreckonRotation) {
                    node.rotationConverger.getValue(currTime, rotationQuat);
                    rotationQuat.normalize();


                    axisTemp.set(rotationQuat);
                    rotation[0] = (float) axisTemp.x;
                    rotation[1] = (float) axisTemp.y;
                    rotation[2] = (float) axisTemp.z;
                    rotation[3] = (float) axisTemp.angle;

                    idx = di.getFieldIndex("rotation");
                    di.setValue(idx, rotation, 4);
                }

/*
                DRPosition(node.currEspdu, dt, tempPositionArray);

                idx = di.getFieldIndex("translation");
                di.setValue(idx, tempPositionArray, 3);


                DROrientation(node.currEspdu, dt, dRorientation);
                axisTemp.set(dRorientation);
                rotation[0] = (float) axisTemp.x;
                rotation[1] = (float) axisTemp.y;
                rotation[2] = (float) axisTemp.z;
                rotation[3] = (float) axisTemp.angle;

                idx = di.getFieldIndex("rotation");
                di.setValue(idx, rotation, 4);
*/
                node.prevDt = dt;
            }

            last = node;
            node = (LiveListEntry)node.next;
        }

        if (checkInactive)
            lastCheck = currTime;

        WriterListEntry writer = (WriterListEntry)writerList.head;
        WriterListEntry last_writer = (WriterListEntry)writerList.head;

        while(writer != null) {
            di = (VRMLDISNodeType) writer.node;

            if (di.valuesToWrite()) {
                writer.lastTime = currTime;
                ProtocolDataUnit pdu = di.getState();

                disId.setValue(di.getSiteID(), di.getAppID(), di.getEntityID());
                WriterMapEntry entry = (WriterMapEntry) writerMap.get(disId);
                entry.writer.write(pdu);

            } else {
                if (currTime - writer.lastTime >= HEARTBEAT_CHECK_TIME) {
                    // Definately write a value
                    writer.lastTime = currTime;
                    ProtocolDataUnit pdu = di.getState();

                    disId.setValue(di.getSiteID(), di.getAppID(), di.getEntityID());
                    WriterMapEntry entry = (WriterMapEntry) writerMap.get(disId);
                    entry.writer.write(pdu);
                } else {
                    // TODO: Check Dead Reckon error
                }
            }

            last_writer = writer;
            writer = (WriterListEntry)writer.next;
        }
    }

    /**
     * Add a network node to the management system.
     *
     * @param node The instance to add to this manager
     */
    public void addNode(VRMLNetworkInterfaceNodeType node) {
        int port;
        String address;
        VRMLDISNodeType di = (VRMLDISNodeType) node;
        DISConnectionId id;
        DISConnectionHandler conn;
        int siteID;
        int appID;
        int entityID;
        DISId did;
        long timestamp;
        WriterListEntry newwle;
        WriterMapEntry entry;

        di.addNetworkRoleListener(this);

        switch(di.getRole()) {
            case VRMLNetworkInterfaceNodeType.ROLE_MANAGER:
                address = di.getAddress();
                port = di.getPort();

                id = new DISConnectionId(address, port);
                conn = (DISConnectionHandler) connections.get(id);

                if (conn == null) {
                    conn = new DISConnectionHandler(nodeMap, liveList, managerList, notifiedSet, address, port);

                    connections.put(id, conn);
                }

                siteID = di.getSiteID();
                appID = di.getAppID();
                entityID = di.getEntityID();

                managerList.add(di);
                break;
            case VRMLNetworkInterfaceNodeType.ROLE_READER:
                address = di.getAddress();
                port = di.getPort();

                id = new DISConnectionId(address, port);
                conn = (DISConnectionHandler) connections.get(id);

                if (conn == null) {
                    // TODO: When do we get rid of these?
                    conn = new DISConnectionHandler(nodeMap, liveList, managerList, notifiedSet, address, port);

                    connections.put(id, conn);
                }

                siteID = di.getSiteID();
                appID = di.getAppID();
                entityID = di.getEntityID();
                int idx = node.getFieldIndex("marking");
                VRMLFieldData field = node.getFieldValue(idx);
        System.out.println("New DIS node: siteID: " + siteID + " appID: " + appID + " " + " entityID: " + entityID + " marking: " + field.stringValue);

                did = new DISId(siteID,appID,entityID);

                nodeMap.put(did, new NodeMapEntry((VRMLDISNodeType)node,null));

                // Add all nodes to writer map in case they change status
                newwle = new WriterListEntry(di);

                entry = new WriterMapEntry((VRMLDISNodeType)node,newwle, conn.getWriter());
                writerMap.put(did, entry);

                break;
            case VRMLNetworkInterfaceNodeType.ROLE_WRITER:

                address = di.getAddress();
                port = di.getPort();

                id = new DISConnectionId(address, port);
                conn = (DISConnectionHandler) connections.get(id);

                if (conn == null) {
                    // TODO: When do we get rid of these?
                    conn = new DISConnectionHandler(nodeMap, liveList, managerList, notifiedSet, address, port);

                    connections.put(id, conn);
                }

                siteID = di.getSiteID();
                appID = di.getAppID();
                entityID = di.getEntityID();
        //System.out.println("New DIS node: " + siteID + " " + appID + " " + entityID);
                did = new DISId(siteID,appID,entityID);

                // Add all nodes to writer map in case they change status
                newwle = new WriterListEntry(di);

                entry = new WriterMapEntry((VRMLDISNodeType)node,newwle, conn.getWriter());
                writerMap.put(did, entry);

                writerList.add(newwle);
                break;
            case VRMLNetworkInterfaceNodeType.ROLE_INACTIVE:
                System.out.println("Logic to change Inactive to Writer not implemented");
                break;
        }
    }

    /**
     * Remove a network node from the management system.
     *
     * @param node The instance to add to this manager
     */
    public void removeNode(VRMLNetworkInterfaceNodeType node) {
        System.out.println("DISProtocolHandler: removeNode not implemented");
    }

    /**
     * Force clearing all currently managed nodes from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    public void clear() {
        if (connections.size() > 0)
            System.out.println("DISProtocolHandler: clear not implemented");
    }

    /**
     * Shutdown the protocol handler now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown() {
        if (connections.size() > 0)
            System.out.println("DISProtocolHandler: shutdown not implemented");
    }

    //----------------------------------------------------------
    // Methods required for NetworkRoleListener
    //----------------------------------------------------------

    /**
     * The role of this node has changed.
     *
     * @param newRole The new role, reader, writer, inactive.
     * @param node The node which changed roles.
     */
    public void roleChanged(int newRole, Object node) {
        VRMLDISNodeType dis_node = (VRMLDISNodeType) node;

        int siteID;
        int entityID;
        int appID;
        long timestamp = System.currentTimeMillis();

        switch(newRole) {
            case VRMLNetworkInterfaceNodeType.ROLE_INACTIVE:
                break;
            case VRMLNetworkInterfaceNodeType.ROLE_READER:
                // remove from writer list
                WriterListEntry wlist_node = (WriterListEntry)writerList.head;
                WriterListEntry wlast = (WriterListEntry)writerList.head;

                while(wlist_node != null) {
                    if (wlist_node.node == dis_node) {
                        writerList.remove(wlist_node, wlast);
                        break;
                    }

                    wlast = wlist_node;
                    wlist_node = (WriterListEntry)wlist_node.next;
                }

                // add to livelist
                NodeMapEntry entry = new NodeMapEntry((VRMLDISNodeType)dis_node,null);

                nodeMap.put(disId.clone(), entry);

                LiveListEntry newlle = new LiveListEntry(dis_node, timestamp);
                entry.listEntry = newlle;

                EntityStatePdu espdu = (EntityStatePdu) dis_node.getState();

                newlle.lastEspdu = espdu;
                newlle.currEspdu = espdu;
                newlle.rotationConverger = new OrderNQuat4dConverger(DEFAULT_ORDER, DEFAULT_CONVERGENCE_INTERVAL, null);
                newlle.translationConverger = new OrderNVector3dConverger(DEFAULT_ORDER, DEFAULT_CONVERGENCE_INTERVAL, null);
                newlle.espduTimestamp = espdu.getTimestamp().longValue();
                newlle.closeEnough = false;
                newlle.avgTime = 0.01f;
                newlle.newPackets = true;

                liveList.add(newlle);
                break;
            case VRMLNetworkInterfaceNodeType.ROLE_WRITER:
                // remove from liveList

                LiveListEntry list_node = (LiveListEntry)liveList.head;
                LiveListEntry last = (LiveListEntry)liveList.head;

                while(list_node != null) {
                    if (list_node.node == dis_node) {
                        liveList.remove(list_node, last);
                        break;
                    }

                    last = list_node;
                    list_node = (LiveListEntry)list_node.next;
                }

                // add to writer list
                WriterListEntry newwle = new WriterListEntry(dis_node);
                writerList.add(newwle);
                break;
        }
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Calculate the deadreckon position of a EntityStatePDU
     */
    private void DRPosition(EntityStatePdu espdu, float dt, float[] dRPosition) {
        float dtSq = dt*dt;

        dRPosition[0] = (float) (espdu.getEntityLocationX()  +
        dt * espdu.getEntityLinearVelocityX() +
        dtSq * espdu.getEntityLinearAccelerationX());

        dRPosition[1] = (float) (-espdu.getEntityLocationZ()  -
        dt * espdu.getEntityLinearVelocityZ() -
        dtSq * espdu.getEntityLinearAccelerationZ());

        dRPosition[2] = (float) (espdu.getEntityLocationY()  +
        dt * espdu.getEntityLinearVelocityY()  +
        dtSq * espdu.getEntityLinearAccelerationY());
//System.out.println("DRPOS:");
//System.out.println("   Current Pos: " + espdu.getEntityLocationX() + " " + espdu.getEntityLocationY() + " " + espdu.getEntityLocationZ());
//System.out.println("   Velocity: " + espdu.getEntityLinearVelocityX() + " " + espdu.getEntityLinearVelocityY() + " " + espdu.getEntityLinearVelocityZ());
//System.out.println("   Calc Pos: " + dRPosition[0] + " " + dRPosition[1] + " " + dRPosition[2]);
    }

    /*
     * Calculate the deadreckon orientation of a EntityStatePDU
     */
    private void DROrientation(EntityStatePdu espdu, float dt, float[] dROrientation) {
        float yaw, pitch, roll;

        roll  = espdu.getEntityOrientationPhi()   + dt * espdu.getEntityAngularVelocityX();
        pitch = espdu.getEntityOrientationTheta() + dt * espdu.getEntityAngularVelocityY();
        yaw   = espdu.getEntityOrientationPsi()   + dt * espdu.getEntityAngularVelocityZ();

        // note Kent's quaternion code has irregular ordering of Euler angles
        // which (by whatever method :) accomplishes the angle transformation
        // desired...   (results verified using NPS AUV)

        dROrientation[0] = -yaw;
        dROrientation[1] =  roll;
        dROrientation[2] =  pitch;
    }

    private void smooth3Floats(float[] drCurrent, float[] drPrevUpdate, float[] result,
       float currentTime, float averageUpdateTime)
    {
/*
        // the ratio of how long since the most recent update time to the averageUpdateTime
        float factor = currentTime / averageUpdateTime;

        result[0] = drPrevUpdate[0] + ( drCurrent[0] - drPrevUpdate[0] ) * factor;
        result[1] = drPrevUpdate[1] + ( drCurrent[1] - drPrevUpdate[1] ) * factor;
        result[2] = drPrevUpdate[2] + ( drCurrent[2] - drPrevUpdate[2] ) * factor;
*/

        result[0] = (drPrevUpdate[0] + drCurrent[0]) / 2.0f;
        result[1] = (drPrevUpdate[1] + drCurrent[1]) / 2.0f;
        result[2] = (drPrevUpdate[2] + drCurrent[2]) / 2.f;

    }

    private float normalize2( float input_angle ) {
        float angle = input_angle;
        float twoPI = (float)Math.PI * 2.0f;

        while( angle > Math.PI )
        {
            angle -= twoPI;
        }
        while(angle <= -Math.PI )
        {
            angle += twoPI;
        }
        return angle;
     }

            // returns: the magnitude and direction of the angle change.
        private void fixEulers( float[] goalOrientation, float[] currOrientation )
        {
            for( int idx = 0; idx < goalOrientation.length; idx++ )
            {
                goalOrientation[idx] = currOrientation[idx] +
                normalize2(normalize2(goalOrientation[idx]) - normalize2(currOrientation[idx]));
            }
        }

    /**
    * Sum of the squares of the diffs between two float arrays.
    */
    private float SqrDeltaFloats( float[] first, float[] second )
    {
        float sumOfSqrs = 0.0f;

        for( int idx = 0; idx < first.length; idx++ )
        {
            sumOfSqrs += (first[idx] - second[idx]) * (first[idx] - second[idx]);
        }

        return sumOfSqrs;
    }

    /**
     * Converts a set of Euler angles (phi, theta, psi)
     * to a rotation matrix.
     *
     * @param eulers the Euler angles to convert
     * @param rotMatrix a rotation matrix to hold the result
     */
    private void eulersToMatrix(double x, double y, double z, Matrix3d rotMatrix)
    {
        psiMat.setIdentity();
        psiMat.rotY(-z);

        thetaMat.rotZ(y);

        phiMat.rotX(x);

        rotMatrix.mul(phiMat,thetaMat);

        rotMatrix.mul(psiMat);
    }
}

// TODO: Move all these classes into seperate files

class DISConnectionHandler implements BehaviorConsumerIF {
    /**
     * The default order.
     */
    private static final int DEFAULT_ORDER = 2;

    /**
     * The default convergence interval.
     */
    private static final int DEFAULT_CONVERGENCE_INTERVAL = 200;

    BehaviorConsumerThreaded consumer;
    BehaviorProducerUDP        writer;
    DatagramSocket     socket;
    InetAddress         address;
    Thread              readThread;
    private int port;
    private String group;
    int cnt;
    private LinkedList liveList;

    // Scratch id to avoid gc
    private DISId disId;

    // Scratch translation field
    private float[] translation;

    // Scratch rotation field
    private float[] rotation;
    private float[] dRorientation;
    private Quaternion  quaternion = null;

    /** The node to ID mapping */
    private Map nodeMap;

    /** The list of managers */
    private List managerList;

    /** The Entities we've placed on the addedEntities */
    private Set notifiedSet;

    public DISConnectionHandler(Map nodeMap, LinkedList liveList, List managerList, Set notifiedSet, String group, int port) {
        this.nodeMap = nodeMap;
        this.group = group;
        this.port = port;
        this.liveList = liveList;
        this.managerList = managerList;
        this.notifiedSet = notifiedSet;

        disId = new DISId(0,0,0);
        translation = new float[3];
        rotation = new float[4];
        dRorientation = new float[3];
        quaternion  = new Quaternion();

        try {
            System.out.println("DIS Listening to port: " + port + " group: " + group);
            address = InetAddress.getByName(group);

            try {
                socket = new MulticastSocket(port);
                ((MulticastSocket)socket).joinGroup(address);
            } catch(Exception e) {
                System.out.println("Failed to listen to multicast port.  Trying unicast");
                socket.close();
                socket = new DatagramSocket(port);
                //socket.bind(address);
            }

            writer = new BehaviorProducerUDP(socket);
            writer.setUseCopies(false);
            writer.setDefaultDestination(address, new Integer(port));
            readThread = new Thread(writer);
            readThread.start();
            writer.addListener(this);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a writer for this connection.
     *
     * @return The writer.
     */
    public BehaviorProducerUDP getWriter() {
        return writer;
    }

    //----------------------------------------------------------
    // Methods required by BehaviorConsumerIF
    //----------------------------------------------------------

    /**
     * Receives a PDU from the BehaviorProducer.
     *
     * @param pdu the ProtocolDataUnit generated by the BehaviorProducer
     * @param producer the producer that generated the call
     */
    public void receivePdu(ProtocolDataUnit pdu, BehaviorProducerIF producer) {
        cnt++;

        if (cnt % 100 == 0) {
//            System.out.println("Packets received: " + cnt);
        }

        int type = pdu.getPduTypeValue();
        EntityID eid;
        NodeMapEntry entry;
        VRMLDISNodeType di;
        long time;
        long timestamp;

        switch(type) {
            case PduTypeField.FIREFI:
            case PduTypeField.FIRE:
                FirePdu firepdu = (FirePdu) pdu;

                eid = firepdu.getTargetEntityID();
                disId.setValue(eid.getSiteIDValue(), eid.getApplicationIDValue(), eid.getEntityIDValue());

                entry = (NodeMapEntry) nodeMap.get(disId);

                if(entry == null) {
                    //System.out.println("Unknown espdu: " + disId);
                    return;
                }

                di = (VRMLDISNodeType) entry.node;

                if (di.getRole() != VRMLNetworkInterfaceNodeType.ROLE_READER) {
                    // Ignore for non readers
                    return;
                }

                time = System.currentTimeMillis();
                timestamp = pdu.getTimestampValue();
                if (di != null) {
                    if (entry.listEntry != null) {
                        // update last time
                        LiveListEntry lle = (LiveListEntry)entry.listEntry;

                        if (timestamp > lle.espduTimestamp) {

                            lle.avgTime = lle.avgTime + (time - lle.lastTime) / 5.0f;
                            lle.lastTime = time;
                            if (lle.currEspdu != null) {
                                lle.lastEspdu = lle.currEspdu;
                            }
                            lle.currFire = firepdu;
                            lle.newPackets = true;
                        } else {
                            System.out.println("Tossing packet: " + timestamp + " last: " + lle.espduTimestamp);
                        }
                   } else {
                        // create new entry
                        LiveListEntry newlle = new LiveListEntry(di, System.currentTimeMillis());
                        entry.listEntry = newlle;
                        newlle.lastEspdu = null;
                        newlle.currEspdu = null;
                        newlle.currDetonate = null;
                        newlle.currFire = firepdu;
                        newlle.espduTimestamp = timestamp;
                        newlle.closeEnough = false;
                        newlle.avgTime = 0.01f;
                        newlle.newPackets = true;


                        liveList.add(newlle);
                        di.setIsActive(true);
                    }

                } else {
                    System.out.println("Unknown entity: " + eid);
                }

                break;
            case PduTypeField.DETONATIONFI:
            case PduTypeField.DETONATION:
                DetonationPdu dpdu = (DetonationPdu) pdu;
                eid = dpdu.getTargetEntityID();
                disId.setValue(eid.getSiteIDValue(), eid.getApplicationIDValue(), eid.getEntityIDValue());

                entry = (NodeMapEntry) nodeMap.get(disId);

                if(entry == null) {
                    //System.out.println("Unknown espdu: " + disId);
                    return;
                }

                di = (VRMLDISNodeType) entry.node;

                if (di.getRole() != VRMLNetworkInterfaceNodeType.ROLE_READER) {
                    // Ignore for non readers
                    return;
                }

                time = System.currentTimeMillis();
                timestamp = pdu.getTimestampValue();
                if (di != null) {
                    if (entry.listEntry != null) {
                        // update last time
                        LiveListEntry lle = (LiveListEntry)entry.listEntry;

                        if (timestamp > lle.espduTimestamp) {

                            lle.avgTime = lle.avgTime + (time - lle.lastTime) / 5.0f;
                            lle.lastTime = time;
                            if (lle.currEspdu != null) {
                                lle.lastEspdu = lle.currEspdu;
                            }
                            lle.currDetonate = dpdu;
                            lle.closeEnough = false;
                            lle.newPackets = true;
                        } else {
                            System.out.println("Tossing packet: " + timestamp + " last: " + lle.espduTimestamp);
                        }
                   } else {
                        // create new entry
                        LiveListEntry newlle = new LiveListEntry(di, System.currentTimeMillis());
                        entry.listEntry = newlle;
                        newlle.lastEspdu = null;
                        newlle.currEspdu = null;
                        newlle.currDetonate = dpdu;
                        newlle.espduTimestamp = timestamp;
                        newlle.closeEnough = false;
                        newlle.avgTime = 0.01f;
                        newlle.newPackets = true;


                        liveList.add(newlle);
                        di.setIsActive(true);
                    }

                } else {
//                    System.out.println("Unknown entity: " + eid);
                }

                break;
            case PduTypeField.ENTITYSTATE:
                EntityStatePdu espdu = (EntityStatePdu)pdu;

                eid = espdu.getEntityID();

                disId.setValue(eid.getSiteIDValue(), eid.getApplicationIDValue(), eid.getEntityIDValue());

                entry = (NodeMapEntry) nodeMap.get(disId);

                if(entry == null) {
                    int len = managerList.size();

                    VRMLDISManagerNodeType manager;

                    for(int i=0; i < len; i++) {
                        manager = (VRMLDISManagerNodeType) managerList.get(i);

                        if (!notifiedSet.contains(disId)) {
                            manager.entityArrived(espdu);

                            // Clone Id to put on list
                            notifiedSet.add(disId.clone());
                        }
                    }
                    return;
                }

                di = (VRMLDISNodeType) entry.node;

                if (di.getRole() != VRMLNetworkInterfaceNodeType.ROLE_READER) {
                    System.out.println("Ignoring ESPDU");
                    // Ignore for non readers
                    return;
                }

                time = System.currentTimeMillis();
                timestamp = pdu.getTimestampValue();

                if (di != null) {
                    if (entry.listEntry != null) {
                        // update last time
                        LiveListEntry lle = (LiveListEntry)entry.listEntry;

                        if (timestamp > lle.espduTimestamp) {
                            lle.avgTime = lle.avgTime + (time - lle.lastTime) / 5.0f;
                            lle.lastTime = time;
                            lle.lastEspdu = lle.currEspdu;
                            lle.currEspdu = espdu;
                            lle.closeEnough = false;
                            lle.newPackets = true;
                        } else {
                            System.out.println("Tossing packet: " + timestamp + " last: " + lle.espduTimestamp);
                        }
                   } else {
                        // create new entry
                        LiveListEntry newlle = new LiveListEntry(di, System.currentTimeMillis());
                        entry.listEntry = newlle;
                        newlle.lastEspdu = espdu;
                        newlle.currEspdu = espdu;
                        newlle.rotationConverger = new OrderNQuat4dConverger(DEFAULT_ORDER, DEFAULT_CONVERGENCE_INTERVAL, null);
                        newlle.translationConverger = new OrderNVector3dConverger(DEFAULT_ORDER, DEFAULT_CONVERGENCE_INTERVAL, null);
                        newlle.espduTimestamp = timestamp;
                        newlle.closeEnough = false;
                        newlle.avgTime = 0.01f;
                        newlle.newPackets = true;


                        liveList.add(newlle);
                        di.setIsActive(true);
                    }

                } else {
                    //System.out.println("Unknown entity: " + eid);
                }

               break;
            default:
                System.out.println("Unhandled DIS node:  type: " + type + " DET: " + PduTypeField.DETONATION);
        }
    }

    /**
     * Does the same as receivePdu(), but can also pass in an arbitrary
     * object with the other information.
     */
    public void receivePdu(ProtocolDataUnit pdu, BehaviorProducerIF producer, Object data) {
        System.out.println("Got pdu from producer2");
    }
}

