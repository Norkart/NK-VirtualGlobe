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

package org.web3d.vrml.renderer.common.input.dis;

// External imports
import java.util.*;

import java.net.*;
import javax.vecmath.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.util.PropertyTools;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.TypeConstants;

import org.xj3d.core.eventmodel.NetworkProtocolHandler;

import org.web3d.xmsf.net.*;
import org.web3d.xmsf.dis.*;
import org.web3d.xmsf.disutil.*;

/**
 * The handler for DISXML protocol network traffic.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */
public class DISXMLProtocolHandler
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
    public DISXMLProtocolHandler() {
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

        LiveListEntryDX node = (LiveListEntryDX)liveList.head;
        LiveListEntryDX last = (LiveListEntryDX)liveList.head;
        VRMLDISNodeType di;
        float dt;
        int numProcessed = 0;
        EntityStatePduType espdu;

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

                    EulerAnglesType eat = espdu.getEntityOrientation();

                    eulersToMatrix(
                        eat.getPhi(),
                        eat.getTheta(),
                        eat.getPsi(),
                        rotationMatrix
                    );

                    rotationQuat.set(rotationMatrix);

                    // Convert to normal coordinates

                    Vector3Double location = espdu.getEntityLocation();

                    Vector3d translation = new Vector3d(
                        location.getX(),
                        -location.getZ(),
                        location.getY()
                    );

                    Vector3Float linearVelocity = espdu.getEntityLinearVelocity();

                    translationDerivatives[0].set(
                        linearVelocity.getX(),
                        -linearVelocity.getZ(),
                        linearVelocity.getY()
                    );

                    DeadReckoningParametersType drp = espdu.getDeadReckoningParameters();
                    Vector3Float angularVelocity = drp.getEntityAngularVelocity();

                    rotationDerivatives[0].set(
                        angularVelocity.getX(),
                        -angularVelocity.getZ(),
                        angularVelocity.getY()
                    );

                    Vector3Float linearAcceleration = drp.getEntityLinearAcceleration();

                    translationDerivatives[1].set(
                        linearAcceleration.getX(),
                        -linearAcceleration.getZ(),
                        linearAcceleration.getY()
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


                DeadReckoningParametersType drp = node.currEspdu.getDeadReckoningParameters();
                Vector3Float linearVelocity = node.currEspdu.getEntityLinearVelocity();
                Vector3Float linearAcceleration = drp.getEntityLinearAcceleration();

                // TODO: What about a rotating only entity?

                if (Math.abs(linearVelocity.getX()) <= 0.0001 &&
                    Math.abs(linearVelocity.getY()) <= 0.0001 &&
                    Math.abs(linearVelocity.getZ()) <= 0.0001 &&
                    Math.abs(linearAcceleration.getX()) <= 0.0001 &&
                    Math.abs(linearAcceleration.getY()) <= 0.0001 &&
                    Math.abs(linearAcceleration.getZ()) <= 0.0001) {

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
            node = (LiveListEntryDX)node.next;
        }

        if (checkInactive)
            lastCheck = currTime;

        WriterListEntryDX writer = (WriterListEntryDX)writerList.head;
        WriterListEntryDX last_writer = (WriterListEntryDX)writerList.head;

        while(writer != null) {
            di = (VRMLDISNodeType) writer.node;

            if (di.valuesToWrite()) {
                writer.lastTime = currTime;
                ProtocolDataUnitType pdu = di.getStateDX();

                disId.setValue(di.getSiteID(), di.getAppID(), di.getEntityID());
                WriterMapEntryDX entry = (WriterMapEntryDX) writerMap.get(disId);

                entry.writer.write(pdu);

            } else {
                if (currTime - writer.lastTime >= HEARTBEAT_CHECK_TIME) {
                    // Definately write a value
                    writer.lastTime = currTime;
                    ProtocolDataUnitType pdu = di.getStateDX();

                    disId.setValue(di.getSiteID(), di.getAppID(), di.getEntityID());
                    WriterMapEntryDX entry = (WriterMapEntryDX) writerMap.get(disId);
                    entry.writer.write(pdu);
                } else {
                    // TODO: Check Dead Reckon error
                }
            }

            last_writer = writer;
            writer = (WriterListEntryDX)writer.next;
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
        String xmppUsername;
        String xmppPassword;
        String[] xmppAuthServer;
        String xmppMucServer;
        String xmppMucRoom;

        VRMLDISNodeType di = (VRMLDISNodeType) node;
        DISConnectionId id;
        DISXMLConnectionHandler conn;
        int siteID;
        int appID;
        int entityID;
        DISId did;
        long timestamp;
        WriterListEntryDX newwle;
        WriterMapEntryDX entry;

        di.addNetworkRoleListener(this);

        address = di.getAddress();
        port = di.getPort();
        xmppUsername = di.getUsername();
        xmppPassword = di.getPassword();
        xmppAuthServer = di.getAuthServer();
        xmppMucServer = di.getMucServer();
        xmppMucRoom = di.getMucRoom();

        switch(di.getRole()) {
            case VRMLNetworkInterfaceNodeType.ROLE_MANAGER:
                id = new DISConnectionId(address, port);
                conn = (DISXMLConnectionHandler) connections.get(id);

                if (conn == null) {
                    conn = new DISXMLConnectionHandler(nodeMap, liveList, managerList, notifiedSet,
                        address, port,
                        xmppUsername, xmppPassword, xmppAuthServer, xmppMucServer, xmppMucRoom);

                    connections.put(id, conn);
                }

                siteID = di.getSiteID();
                appID = di.getAppID();
                entityID = di.getEntityID();

                managerList.add(di);
                break;
            case VRMLNetworkInterfaceNodeType.ROLE_READER:
                id = new DISConnectionId(address, port);
                conn = (DISXMLConnectionHandler) connections.get(id);

                if (conn == null) {
                    // TODO: When do we get rid of these?
                    conn = new DISXMLConnectionHandler(nodeMap, liveList, managerList, notifiedSet,
                        address, port,
                        xmppUsername, xmppPassword, xmppAuthServer, xmppMucServer, xmppMucRoom);

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
                newwle = new WriterListEntryDX(di);

                entry = new WriterMapEntryDX((VRMLDISNodeType)node,newwle, conn);
                writerMap.put(did, entry);

                break;
            case VRMLNetworkInterfaceNodeType.ROLE_WRITER:
                id = new DISConnectionId(address, port);
                conn = (DISXMLConnectionHandler) connections.get(id);

                if (conn == null) {
                    // TODO: When do we get rid of these?
                    conn = new DISXMLConnectionHandler(nodeMap, liveList, managerList, notifiedSet,
                        address, port,
                        xmppUsername, xmppPassword, xmppAuthServer, xmppMucServer, xmppMucRoom);

                    connections.put(id, conn);
                }

                siteID = di.getSiteID();
                appID = di.getAppID();
                entityID = di.getEntityID();
        //System.out.println("New DIS node: " + siteID + " " + appID + " " + entityID);
                did = new DISId(siteID,appID,entityID);

                // Add all nodes to writer map in case they change status
                newwle = new WriterListEntryDX(di);

                entry = new WriterMapEntryDX((VRMLDISNodeType)node,newwle, conn);
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
System.out.println("*** Now READER: " + this);
                // remove from writer list
                WriterListEntryDX wlist_node = (WriterListEntryDX)writerList.head;
                WriterListEntryDX wlast = (WriterListEntryDX)writerList.head;

                while(wlist_node != null) {
                    if (wlist_node.node == dis_node) {
                        writerList.remove(wlist_node, wlast);
                        break;
                    }

                    wlast = wlist_node;
                    wlist_node = (WriterListEntryDX)wlist_node.next;
                }

                // add to livelist
                NodeMapEntry entry = new NodeMapEntry((VRMLDISNodeType)dis_node,null);

                nodeMap.put(disId.clone(), entry);

                LiveListEntryDX newlle = new LiveListEntryDX(dis_node, timestamp);
                entry.listEntry = newlle;

                EntityStatePduType espdu = (EntityStatePduType) dis_node.getStateDX();

                newlle.lastEspdu = espdu;
                newlle.currEspdu = espdu;
                newlle.rotationConverger = new OrderNQuat4dConverger(DEFAULT_ORDER, DEFAULT_CONVERGENCE_INTERVAL, null);
                newlle.translationConverger = new OrderNVector3dConverger(DEFAULT_ORDER, DEFAULT_CONVERGENCE_INTERVAL, null);
                HeaderType header = espdu.getPduHeader();
                newlle.espduTimestamp = header.getTimestamp();
                newlle.closeEnough = false;
                newlle.avgTime = 0.01f;
                newlle.newPackets = true;

                liveList.add(newlle);
                break;
            case VRMLNetworkInterfaceNodeType.ROLE_WRITER:
System.out.println("*** Now WRITER: " + this);
                // remove from liveList

                LiveListEntryDX list_node = (LiveListEntryDX)liveList.head;
                LiveListEntryDX last = (LiveListEntryDX)liveList.head;

                while(list_node != null) {
                    if (list_node.node == dis_node) {
                        liveList.remove(list_node, last);
                        break;
                    }

                    last = list_node;
                    list_node = (LiveListEntryDX)list_node.next;
                }

                // add to writer list
                WriterListEntryDX newwle = new WriterListEntryDX(dis_node);
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
    private void DRPosition(EntityStatePduType espdu, float dt, float[] dRPosition) {
        float dtSq = dt*dt;

        Vector3Double location = espdu.getEntityLocation();
        Vector3Float linearVelocity = espdu.getEntityLinearVelocity();
        DeadReckoningParametersType drp = espdu.getDeadReckoningParameters();
        Vector3Float linearAcceleration = drp.getEntityLinearAcceleration();

        dRPosition[0] = (float) (location.getX()  +
        dt * linearVelocity.getX() +
        dtSq * linearAcceleration.getX());

        dRPosition[1] = (float) (-location.getZ()  -
        dt * linearVelocity.getZ() -
        dtSq * linearAcceleration.getZ());

        dRPosition[2] = (float) (location.getY()  +
        dt * linearVelocity.getY()  +
        dtSq * linearAcceleration.getY());
//System.out.println("DRPOS:");
//System.out.println("   Current Pos: " + espdu.getEntityLocationX() + " " + espdu.getEntityLocationY() + " " + espdu.getEntityLocationZ());
//System.out.println("   Velocity: " + espdu.getEntityLinearVelocityX() + " " + espdu.getEntityLinearVelocityY() + " " + espdu.getEntityLinearVelocityZ());
//System.out.println("   Calc Pos: " + dRPosition[0] + " " + dRPosition[1] + " " + dRPosition[2]);
    }

    /*
     * Calculate the deadreckon orientation of a EntityStatePDU
     */
    private void DROrientation(EntityStatePduType espdu, float dt, float[] dROrientation) {
        float yaw, pitch, roll;

        EulerAnglesType eat = espdu.getEntityOrientation();
        DeadReckoningParametersType drp = espdu.getDeadReckoningParameters();
        Vector3Float angularVelocity = drp.getEntityAngularVelocity();

        roll  = eat.getPhi()   + dt * angularVelocity.getX();
        pitch = eat.getTheta() + dt * angularVelocity.getY();
        yaw   = eat.getPsi()   + dt * angularVelocity.getZ();

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


