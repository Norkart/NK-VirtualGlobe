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

// External Imports
import java.util.*;

import java.net.*;
import java.io.*;
import javax.vecmath.*;

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

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smackx.muc.*;

import org.web3d.xmsf.dis.*;
import org.web3d.xmsf.dis.impl.*;
import org.web3d.xmsf.disutil.*;

class DISXMLConnectionHandler implements PacketListener {
    /**
     * The default order.
     */
    private static final int DEFAULT_ORDER = 2;

    /**
     * The default convergence interval.
     */
    private static final int DEFAULT_CONVERGENCE_INTERVAL = 200;

    /** Property describing the XMPP login name to use */
    private static final String USERNAME_PROP =
        "org.web3d.vrml.renderer.common.input.dis.username";

    /** The default username value */
    private static final String DEFAULT_USERNAME = "guest";

    /** Property describing the XMPP login name to use */
    private static final String PASSWORD_PROP =
        "org.web3d.vrml.renderer.common.input.dis.password";

    /** The default password value */
    private static final String DEFAULT_PASSWORD = "guest";

    /** The username to use */
    private static String username;

    /** The password to use */
    private static String password;

    private
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

    /** The node to ID mapping */
    private Map nodeMap;

    /** The list of managers */
    private List managerList;

    /** The Entities we've placed on the addedEntities */
    private Set notifiedSet;

    /** Connection to the XMPP server */
    private XMPPConnection connection;

    /** Multiuser chat room */
    private MultiUserChat load;

    /** Packet Filter */
    private PacketFilter filter;

    /** Unmarhsall DIS packets */
    private DisUnmarshaller disUnmarshaller;

    /** Is this connection live */
    private boolean live;

    /** The simulation start time for calculating time stamps */
    private static long simStartTime;

    /** XML Marshaller */
    DisMarshaller marshaller;

    // XMPP connection vars
    private final String xmppUsername;
    private final String xmppPassword;
    private final String[] xmppAuthServer;
    private final String xmppMucServer;
    private final String xmppMucRoom;

    static {
        // Subtract out 1 day so clients starting at different times will not throw packets
        simStartTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000;

        username = PropertyTools.fetchSystemProperty(USERNAME_PROP,DEFAULT_USERNAME);
        password = PropertyTools.fetchSystemProperty(PASSWORD_PROP,DEFAULT_PASSWORD);
    }

    public DISXMLConnectionHandler(Map nodeMap, LinkedList liveList, List managerList,
        Set notifiedSet, String group, int port, String xUsername, String xPassword,
        String[] xAuthServer, String xMucServer, String xMucRoom) {
        this.nodeMap = nodeMap;
        this.group = group;
        this.port = port;
        this.liveList = liveList;
        this.managerList = managerList;
        this.notifiedSet = notifiedSet;
        disUnmarshaller = new DisUnmarshaller();
        live = false;

        if (xUsername != null)
            this.xmppUsername = xUsername;
        else
            this.xmppUsername = username;

        if (xPassword != null)
            this.xmppPassword = xPassword;
        else
            this.xmppPassword = password;

        this.xmppAuthServer = xAuthServer;
        this.xmppMucServer = xMucServer;
        this.xmppMucRoom = xMucRoom;

        disId = new DISId(0,0,0);
        translation = new float[3];
        rotation = new float[4];
        dRorientation = new float[3];
        marshaller = new DisMarshaller();

        try {
            System.out.println("DISXML Listening to port: " + port + " group: " + group);
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

            //writer = new BehaviorProducerUDP(socket);
            //setUseCopies(false);
            //setDefaultDestination(address, new Integer(port));
            //writer.addListener(this);

            Thread loginThread = new Thread() {
                public void run() {
/*
                    login(username, password, "xchat.movesinstitute.org",
                       "conference.xchat.movesinstitute.org", "auvw");
*/

System.out.println("XMPP Params: " + xmppAuthServer + " " + xmppMucRoom);
                    login(xmppUsername, xmppPassword, xmppAuthServer,
                       xmppMucServer, xmppMucRoom);
                }
            };

            loginThread.start();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Login into the XMPP chat room.
     *
     * @param username The username to use
     * @param password The password to use
     * @param authServer The authentification servers
     * @param mucServer The multiuser chat server
     * @param mucRoom The multiuser chat room to join
     */
    private void login(String username,
                      String password,
                      String[] authServer,
                      String mucServer,
                      String mucRoom) {

        try {
            //username += (int) (Math.round(Math.random() * 2)) + 1;
            System.out.println("Attempting login: " + username);
            String rid = "xj3d_" + Integer.toString((int)(Math.random() * Integer.MAX_VALUE), Character.MAX_RADIX);

            if (authServer == null || mucServer == null || mucRoom == null) {
                System.out.println("Invalid XMPP params.  Using defaults for now");
                authServer = new String[] {
                  "surfaris.cs.nps.navy.mil", "xchat.movesinstitute.org"
                };

                mucServer = "conference.xchat.movesinstitute.org";
                mucRoom = "auvw";
            }

            int tries = 2;

            while(connection == null && tries > 0) {
                // Authenticate to our local XMPP server
                for(int i=0; i < authServer.length; i++) {
                    try {
                        System.out.println("Xj3D trying authServer: " + authServer[i]);
                        connection = new XMPPConnection(authServer[i]);
                        connection.login(username, password, rid);
                    } catch(Exception e) {
                        // ignore and move on
                    }

                    if (connection != null) {
                        System.out.println("Xj3D connected to: " + authServer[i]);
                        break;
                    }
                }

                tries--;
            }

            // Establish a connection to the MUC room
            String mucJid = mucRoom + "@" + mucServer;
            System.out.println("Xj3D Connecting to MUC room: " + mucJid);
            DiscussionHistory dh = new DiscussionHistory();
            dh.setMaxStanzas(0);

            //RoomInfo info = MultiUserChat.getRoomInfo(connection, mucJid);
            //System.out.println("Number of occupants:" + info.getOccupantsCount());

            load  = new MultiUserChat(connection, mucJid);

            int numRetries = 0;
            boolean connected = false;

System.out.println("Xj3D Logging into chatroom with username: " + username + rid);
            while(!connected && numRetries < 5) {
                try {
                    load.join(username + rid, password, dh, SmackConfiguration.getPacketReplyTimeout());
                    connected = load.isJoined();
                } catch(Exception e) {
                    System.out.println("Failed to connect, retrying");
                    System.out.println("Msg: " + e);
                }
                numRetries++;
            }

            if (load.isJoined()) {
                System.out.println("Xj3D successfully joined chat");
                // set up a packet filter to listen for only the things we want
                PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class),
                                                    new FromContainsFilter(mucJid));

                connection.addPacketListener(this, filter);
                live = true;
            } else {
                System.out.println("***Couldn't join chat room");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    int idx = 0;

    public void processPacket(Packet packet) {
        EntityIDType eid;
        NodeMapEntry entry;
        VRMLDISNodeType di;
        long time;
        long timestamp;

        Iterator it = packet.getPropertyNames();

        if(it.hasNext() == false) {
            System.out.println("empty properties in packet");
            return;
        }

        // Properties set for DIS packets:
        // messageType:    dis
        // disInformation: the text representation of DIS
        // disFormat:      XML or base64
        // sender:         host that sent this
        // port:           port to send on
        // multicastAddress: multicast group to send on

        // Properties for rollcall messages:
        // messageType: rollcall
        // requestID:   request ID, a unique identifer for this request

        // Properties for rollcall response messages:
        // messageType: rollcallResponse
        // bridgeName: bridgeName (user this bridge is logged in as)
        // request ID: request ID, tied to the ID of the original request

        String messageType = (String)packet.getProperty("messageType");

        if(messageType == null)
            return;

        // This XMPP type carries a DIS payload.
        if(messageType.equalsIgnoreCase("dis")) {
            // Convert the XMPP XML DIS message to a java object, then marshall it to
            // IEEE-DIS format and send it out on the local network.

            String xml = (String)packet.getProperty("disInformation");
            String disFormat = (String)packet.getProperty("disFormat");

            if(disFormat.equalsIgnoreCase("xml")) {
                try {
                    // Translate the XML into java object(s)
                    List pduList = disUnmarshaller.unmarshallFromXML(new ByteArrayInputStream(xml.getBytes()));

                    // Loop through all the PDUs in the list we just recieved. Typically the list
                    // is one PDU long.
                    for(int idx = 0; idx < pduList.size(); idx++) {
                        // get one PDU from the list
                        ProtocolDataUnitType pdu = (ProtocolDataUnitType)pduList.get(idx);

                        // TODO: Need to handle other types
                        EntityStatePduType espdu = (EntityStatePduType)pdu;

                        eid = espdu.getEntityID();

                        disId.setValue(eid.getSite(), eid.getApplication(), eid.getEntity());

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
                        HeaderType header = pdu.getPduHeader();

                        timestamp = header.getTimestamp();

                        if (di != null) {
                            if (entry.listEntry != null) {
                                // update last time
                                LiveListEntryDX lle = (LiveListEntryDX)entry.listEntry;

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
                                LiveListEntryDX newlle = new LiveListEntryDX(di, System.currentTimeMillis());
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
                    }
                }
                catch(Exception e) {
                    System.out.println("Can't reconsitute XML XMPP information " + e);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Write a PDU to the default destination
     *
     * @param pdu the DIS PDU to be written
     */
    public void write(ProtocolDataUnitType pdu) {
        if (!live)
            return;

        pdu.getPduHeader().setTimestamp((int)(System.currentTimeMillis() - simStartTime));

        // TODO: Too much object creation in here

        try {
            ArrayList pduList = new ArrayList();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pduList.add(pdu);

            Vector3Double velo = ((EntityStatePduType)pdu).getEntityLocation();
            System.out.println("Write pdu: " + pdu + " Loc: " + velo.getX() + " " + velo.getZ());
            marshaller.marshallPdus(pduList, baos);
            String xmlPduString = baos.toString();

            // There are two ways to send messages: the "standard" way, via load.sendMessage(String),
            // and by attaching properties to the message. The second technique is pretty similar to
            // using packet extensions. You can add properties and values to packets. But it is also
            // non-standard for most clients, and the messages will not show up. The first method
            // is good for getting traffic to show up in chat rooms in exodous, iChat, etc.

            // Create a message using properties
            Message message = load.createMessage();
            message.setProperty("disInformation", xmlPduString);
            message.setProperty("messageType", "dis");
            message.setProperty("disFormat", "xml");

            // The properties method--send the message
            load.sendMessage(message);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
