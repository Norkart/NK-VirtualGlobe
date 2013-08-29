/*****************************************************************************
 * Copyright North Dakota State University, 2004
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.vrml.scripting.external.neteai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.web3d.browser.BrowserCoreListener;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import vrml.eai.event.BrowserEvent;
import vrml.eai.field.BaseField;
import vrml.eai.field.InvalidFieldException;

import vrml.InvalidVRMLSyntaxException;
import vrml.eai.InvalidNodeException;

/**
 * ServerProcessingTask is the runnable which embodies the read
 * and processing loop for an individual connection.
 */
class ServerProcessingTask implements Runnable, BrowserCoreListener, FieldChangeTransmitter {

    /** The clock for generating time stamps */
    VRMLClock timeClock;

    /**
     * The server processing task and each of the broadcast generators each need
     * to write to the output stream. Writers synchronized here.
     */
    Object writeLock;

    /** The Server Browser instance */
    ServerBrowser theServerBrowser;

    /** The Socket used for communicating with the client */
    Socket clientConnection;

    /** Flag for termination of processing loop */
    boolean remainOpen;

    /** Output stream for broadcasts */
    DataOutputStream dos;

    /**
     * Create a new instance to deal with a specified connection
     *
     * @param socket
     *            The connection to deal with
     */
    ServerProcessingTask(Socket socket, ServerBrowser browser, VRMLClock clock) {
        clientConnection = socket;
        remainOpen = true;
        theServerBrowser = browser;
        writeLock = new Object();
        theServerBrowser.setCoreListener(this);
        theServerBrowser.setFieldChangeTransmitter(this);
        timeClock = clock;
    }

    /**
     * *
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            DataInputStream dis = null;
            try {
                dos = new DataOutputStream(clientConnection.getOutputStream());
                dis = new DataInputStream(clientConnection.getInputStream());
                // Connection prolog
                int checkByte = dis.readInt();
                if (checkByte != NetworkProtocolConstants.CONNECTION_MAGIC_NUMBER_CLIENT) {
                    System.out.println("Improper handshake.");
                    return;
                } else {
                    System.out.println("Server Handshake complete.");
                }
                dos
                        .writeInt(NetworkProtocolConstants.CONNECTION_MAGIC_NUMBER_SERVER);
                while (remainOpen) {
                    int packetType = dis.readInt();
                    int requestID = dis.readInt();
                    System.out.println("Server received type " + packetType
                            + " ID" + requestID);
                    switch (packetType) {
                    case NetworkProtocolConstants.ADD_EVENT_OUT_LISTENER_REQUEST:
                        {
                            int fieldID=dis.readInt();
                            synchronized (writeLock) {
                                dos
                                    .writeInt(NetworkProtocolConstants.ADD_EVENT_OUT_LISTENER_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    theServerBrowser.enableFieldBroadcast(fieldID);
                                    dos
                                        .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.ADD_BROWSER_LISTENER_REQUEST:
                        {
                            synchronized (writeLock) {
                                dos
                                        .writeInt(NetworkProtocolConstants.ADD_BROWSER_LISTENER_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    theServerBrowser.enableBrowserListener();
                                    dos
                                            .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.CREATE_VRML_FROM_STRING_REQUEST:
                        {
                            String vrml = dis.readUTF();
                            //System.out.println("CreateVrml using:"+vrml);
                            synchronized (writeLock) {
                                dos
                                        .writeInt(NetworkProtocolConstants.CREATE_VRML_FROM_STRING_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    int nodeIDs[] = theServerBrowser
                                            .createVrmlFromString(vrml);
                                    dos
                                            .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                    if (nodeIDs == null)
                                        dos.writeInt(-1);
                                    else {
                                        dos.writeInt(nodeIDs.length);
                                        for (int counter = 0; counter < nodeIDs.length; counter++)
                                            dos.writeInt(nodeIDs[counter]);
                                    }
                                } catch (InvalidVRMLSyntaxException ivse) {
                                    sendExceptionPacket(ivse, dos);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.CREATE_VRML_FROM_URL_REQUEST:
                        {
                            int numURLs = dis.readInt();
                            String URLs[] = new String[numURLs];
                            for (int counter=0; counter<numURLs; counter++)
                                URLs[counter]=dis.readUTF();
                            int nodeID=dis.readInt();
                            String eventInName=dis.readUTF();
                            synchronized (writeLock) {
                                dos.writeInt(NetworkProtocolConstants.CREATE_VRML_FROM_URL_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    theServerBrowser.createVrmlFromURL(URLs,nodeID,eventInName);
                                    dos.writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                } catch (Throwable t) {
                                    sendErrorPacket(t,dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.DISPOSE_NODE_REQUEST:
                        {
                            int nodeID = dis.readInt();
                            synchronized (writeLock) {
                                dos
                                        .writeInt(NetworkProtocolConstants.DISPOSE_NODE_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    theServerBrowser.disposeNode(nodeID);
                                    dos
                                            .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                    /** Node.dispose returns void */
                                } catch (InvalidNodeException ine) {
                                    sendExceptionPacket(ine, dos);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.GET_CURRENT_FRAME_RATE_REQUEST:
                        {
                            synchronized (writeLock){
                                dos.writeInt(NetworkProtocolConstants.GET_CURRENT_FRAME_RATE_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    float rate=theServerBrowser.getCurrentFrameRate();
                                    dos.writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                    dos.writeFloat(rate);
                                } catch (Throwable t) {
                                    sendErrorPacket(t,dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.GET_CURRENT_SPEED_REQUEST:
                    {
                        synchronized (writeLock){
                            dos.writeInt(NetworkProtocolConstants.GET_CURRENT_SPEED_REPLY);
                            dos.writeInt(requestID);
                            try {
                                float rate=theServerBrowser.getCurrentSpeed();
                                dos.writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                dos.writeFloat(rate);
                            } catch (Throwable t) {
                                sendErrorPacket(t,dos);
                            }
                        }
                    }
                    break;
                    case NetworkProtocolConstants.GET_EVENTIN_REQUEST:
                        {
                            int nodeID = dis.readInt();
                            String fieldName = dis.readUTF();
                            synchronized (writeLock) {
                                dos
                                        .writeInt(NetworkProtocolConstants.GET_EVENTIN_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    int fieldID = theServerBrowser.getEventIn(
                                            nodeID, fieldName);
                                    int fieldType = theServerBrowser
                                            .getFieldType(fieldID);
                                    dos
                                            .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                    dos.writeInt(fieldID);
                                    dos.writeInt(fieldType);
                                } catch (InvalidNodeException ine) {
                                    sendExceptionPacket(ine, dos);
                                } catch (InvalidFieldException ife) {
                                    sendExceptionPacket(ife, dos);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.GET_EVENTOUT_REQUEST:
                        {
                            int nodeID = dis.readInt();
                            String fieldName = dis.readUTF();
                            synchronized (writeLock) {
                                dos
                                        .writeInt(NetworkProtocolConstants.GET_EVENTOUT_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    int fieldID = theServerBrowser.getEventOut(
                                            nodeID, fieldName);
                                    int fieldType = theServerBrowser
                                            .getFieldType(fieldID);
                                    dos
                                            .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                    dos.writeInt(fieldID);
                                    dos.writeInt(fieldType);
                                } catch (InvalidNodeException ine) {
                                    sendExceptionPacket(ine, dos);
                                } catch (InvalidFieldException ife) {
                                    sendExceptionPacket(ife, dos);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.GET_FIELD_VALUE_REQUEST:
                        {
                            int fieldID = dis.readInt();
                            synchronized (writeLock) {
                                dos
                                        .writeInt(NetworkProtocolConstants.GET_FIELD_VALUE_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    VRMLNodeType node = theServerBrowser
                                            .getNodeFromFieldID(fieldID);
                                    int localFieldID = theServerBrowser
                                            .getLocalFieldID(fieldID);
                                    dos
                                            .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                    transmitFieldValue(fieldID,node, localFieldID);
                                } catch (InvalidNodeException ine) {
                                    sendExceptionPacket(ine, dos);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.GET_IMAGE_SUB_VALUE_REQUEST:
                        {
                        int fieldID = dis.readInt();
                        byte subRequestType=dis.readByte();
                        synchronized (writeLock) {
                            dos
                                    .writeInt(NetworkProtocolConstants.GET_IMAGE_SUB_VALUE_REPLY);
                            dos.writeInt(requestID);
                            try {
                                VRMLNodeType node = theServerBrowser
                                        .getNodeFromFieldID(fieldID);
                                int localFieldID = theServerBrowser
                                        .getLocalFieldID(fieldID);
                                dos
                                        .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                transmitSubFieldValue(node, localFieldID, subRequestType,dos);
                            } catch (InvalidNodeException ine) {
                                sendExceptionPacket(ine, dos);
                            } catch (Throwable t) {
                                sendErrorPacket(t, dos);
                            }
                        }
                        }
                        break;
                    case NetworkProtocolConstants.GET_NODE_REQUEST:
                        {
                            String nodeName = dis.readUTF();
                            synchronized (writeLock) {
                                dos
                                        .writeInt(NetworkProtocolConstants.GET_NODE_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    int nodeID = theServerBrowser
                                            .getNodeID(nodeName);
                                    dos
                                            .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                    dos.writeInt(nodeID);
                                } catch (InvalidNodeException ine) {
                                    sendExceptionPacket(ine, dos);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.GET_NUM_FIELD_VALUES_REQUEST:
                        {
                            int fieldID = dis.readInt();
                            synchronized (writeLock) {
                                dos
                                        .writeInt(NetworkProtocolConstants.GET_NUM_FIELD_VALUES_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    VRMLNodeType node = theServerBrowser
                                            .getNodeFromFieldID(fieldID);
                                    int localFieldID = theServerBrowser
                                            .getLocalFieldID(fieldID);
                                    VRMLFieldData data = node
                                            .getFieldValue(localFieldID);
                                    int numElements = data.numElements;
                                    dos
                                            .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                    dos.writeInt(numElements);
                                } catch (InvalidNodeException ine) {
                                    sendExceptionPacket(ine, dos);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }

                        }
                        break;
                    case NetworkProtocolConstants.GET_NODE_TYPE_REQUEST:
                        {
                            int nodeID=dis.readInt();
                            synchronized (writeLock) {
                                dos.writeInt(NetworkProtocolConstants.GET_NODE_TYPE_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    VRMLNodeType node=theServerBrowser.getNode(nodeID);
                                    String nodeType=node.getVRMLNodeName();
                                    dos.writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                    dos.writeUTF(nodeType);
                                } catch (InvalidNodeException ine) {
                                    sendExceptionPacket(ine,dos);
                                } catch (Throwable t) {
                                    sendErrorPacket(t,dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.GET_WORLD_URL_REQUEST:
                    {
                        int nodeID=dis.readInt();
                        synchronized (writeLock) {
                            dos.writeInt(NetworkProtocolConstants.GET_WORLD_URL_REPLY);
                            dos.writeInt(requestID);
                            try {
                                String worldURL=theServerBrowser.getWorldURL();
                                dos.writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                dos.writeUTF(worldURL);
                            } catch (InvalidNodeException ine) {
                                sendExceptionPacket(ine,dos);
                            } catch (Throwable t) {
                                sendErrorPacket(t,dos);
                            }
                        }
                    }
                    break;
                    case NetworkProtocolConstants.LOAD_URL_REQUEST:
                        {
                            int numURLs = dis.readInt();
                            String URLs[];
                            if (numURLs > -1) {
                                URLs = new String[numURLs];
                                for (int counter = 0; counter < numURLs; counter++)
                                    URLs[counter] = dis.readUTF();
                            } else
                                URLs = null;
                            int numParams = dis.readInt();
                            String params[];
                            if (numParams > -1) {
                                params = new String[numParams];
                                for (int counter = 0; counter < numParams; counter++)
                                    params[counter] = dis.readUTF();
                            } else
                                params = null;
                            synchronized (writeLock) {
                                dos
                                        .writeInt(NetworkProtocolConstants.LOAD_URL_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    theServerBrowser.loadURL(URLs, params);
                                    dos.writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                    /** LoadURL returns void */
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.REMOVE_BROWSER_LISTENER_REQUEST:
                        {
                            synchronized (writeLock) {
                                dos
                                        .writeInt(NetworkProtocolConstants.REMOVE_BROWSER_LISTENER_REQUEST);
                                dos.writeInt(requestID);
                                try {
                                    theServerBrowser.disableBrowserListener();
                                    dos
                                            .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.REMOVE_EVENT_OUT_LISTENER_REQUEST:
                        {
                            int fieldID=dis.readInt();
                            synchronized (writeLock) {
                                dos
                                    .writeInt(NetworkProtocolConstants.ADD_EVENT_OUT_LISTENER_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    theServerBrowser.disableFieldBroadcast(fieldID);
                                    dos
                                        .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.REPLACE_WORLD_REQUEST:
                        {
                            int nodeArray[];
                            int arrayLength = dis.readInt();
                            if (arrayLength < 0)
                                nodeArray = null;
                            else {
                                nodeArray = new int[arrayLength];
                                for (int counter = 0; counter < arrayLength; counter++)
                                    nodeArray[counter] = dis.readInt();
                            }
                            //System.out.println("Replace world with
                            // "+arrayLength+" nodes");
                            synchronized (writeLock) {
                                dos
                                        .writeInt(NetworkProtocolConstants.REPLACE_WORLD_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    theServerBrowser.replaceWorld(nodeArray);
                                    dos
                                            .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.SET_DESCRIPTION_REQUEST:
                            {
                            String newDescription=dis.readUTF();
                            synchronized (writeLock) {
                                dos
                                    .writeInt(NetworkProtocolConstants.SET_DESCRIPTION_REPLY);
                                dos.writeInt(requestID);
                                try {
                                    theServerBrowser.setDescription(newDescription);
                                    dos
                                        .writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                } catch (Throwable t) {
                                    sendErrorPacket(t, dos);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.SET_FIELD_VALUE_REQUEST:
                        {
                            int fieldID = dis.readInt();
                            boolean error = false;
                            try {
                                readAndSetField(fieldID, dis);
                            } catch (Throwable t) {
                                error = true;
                                synchronized (writeLock) {
                                    dos.writeInt(NetworkProtocolConstants.SET_FIELD_VALUE_REPLY);
                                    dos.writeInt(requestID);
                                    sendErrorPacket(t, dos);
                                }
                            }
                            if (!error) {
                                synchronized (writeLock) {
                                    dos.writeInt(NetworkProtocolConstants.SET_FIELD_VALUE_REPLY);
                                    dos.writeInt(requestID);
                                    dos.writeByte(NetworkProtocolConstants.RESPONSE_OKAY);
                                }
                            }
                        }
                        break;
                    case NetworkProtocolConstants.SHUTDOWN_SYSTEM:
                        {
                            remainOpen=false;

                        }
                        break;
                    default:
                        System.err.println("Unhandled request received.  Packet type:"+packetType+" ID"+requestID);
                        throw new RuntimeException("Unknown request received.");
                    }
                    System.out.println("Server done with type " + packetType
                            + " ID" + requestID);
                }
            } finally {
                if (dos != null)
                    dos.close();
                if (dis != null)
                    dis.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Transmit the components of an SFImage field value
     * type 0 for height,
     * type 1 for width,
     * type 2 for components,
     * anything else for the pixels.
     * @param node The local SFImage node
     * @param localFieldID The index of the field on node
     * @param subRequestType Which component of the field to send
     */
    private void transmitSubFieldValue(VRMLNodeType node, int localFieldID, byte subRequestType, DataOutputStream dos) throws IOException {
        VRMLFieldData data = node.getFieldValue(localFieldID);
        switch (subRequestType) {
            case 0:
                dos.writeInt(data.intArrayValue[0]);
                break;
            case 1:
                dos.writeInt(data.intArrayValue[1]);
                break;
            case 2:
                dos.writeInt(data.intArrayValue[2]);
                break;
            default:
                int numPixels=data.intArrayValue[0]*data.intArrayValue[1];
                dos.writeInt(numPixels);
                for (int counter=0; counter<numPixels; counter++)
                    dos.writeInt(data.intArrayValue[3+counter]);
        }
    }

    /**
     * Reads field value from network and then call setFieldValue.
     *
     * @param fieldID The network ID of the field
     * @param dis The input stream to read the field value from
     */
    private void readAndSetField(int fieldID, DataInputStream dis)
            throws IOException {
        VRMLNodeType node = theServerBrowser.getNodeFromFieldID(fieldID);
        int fieldIndex = theServerBrowser.getLocalFieldID(fieldID);
        int numFields;
        switch (theServerBrowser.getFieldType(fieldID)) {
        case BaseField.MFColor:
            numFields = dis.readInt();
            {
                float data[] = new float[numFields * 3];
                for (int counter = 0; counter < numFields * 3; counter++)
                    data[counter] = dis.readFloat();
                node.setValue(fieldIndex, data, data.length);
            }
            break;
        case BaseField.MFFloat:
            numFields = dis.readInt();
            {
                float data[] = new float[numFields];
                for (int counter = 0; counter < numFields; counter++)
                    data[counter] = dis.readFloat();
                node.setValue(fieldIndex, data, numFields);
            }
            break;
        case BaseField.MFInt32:
            numFields = dis.readInt();
            {
                int data[] = new int[numFields];
                for (int counter = 0; counter < numFields; counter++)
                    data[counter] = dis.readInt();
                node.setValue(fieldIndex, data, numFields);
            }
            break;
        case BaseField.MFNode:
            numFields = dis.readInt();
            {
                VRMLNodeType nodes[] = new VRMLNodeType[numFields];
                for (int counter = 0; counter < numFields; counter++)
                    nodes[counter] = theServerBrowser.getNode(dis.readInt());
                node.setValue(fieldIndex, nodes, numFields);
            }
            break;
        case BaseField.MFRotation:
            numFields = dis.readInt();
            {
                float data[] = new float[numFields * 4];
                for (int counter = 0; counter < numFields * 4; counter++)
                    data[counter] = dis.readFloat();
                node.setValue(fieldIndex, data, data.length);
            }
            break;
        case BaseField.MFString:
            numFields = dis.readInt();
            {
                String data[] = new String[numFields];
                for (int counter = 0; counter < numFields; counter++)
                    data[counter] = dis.readUTF();
                node.setValue(fieldIndex, data, numFields);
            }
            break;
        case BaseField.MFTime:
            numFields = dis.readInt();
            {
                double data[] = new double[numFields];
                for (int counter = 0; counter < numFields; counter++)
                    data[counter] = dis.readDouble();
                node.setValue(fieldIndex, data, numFields);
            }
            break;
        case BaseField.MFVec2f:
            numFields = dis.readInt();
            {
                float data[] = new float[numFields * 2];
                for (int counter = 0; counter < numFields * 2; counter++)
                    data[counter] = dis.readFloat();
                node.setValue(fieldIndex, data, data.length);
            }
            break;
        case BaseField.MFVec3f:
            numFields = dis.readInt();
            {
                float data[] = new float[numFields * 3];
                for (int counter = 0; counter < numFields * 3; counter++)
                    data[counter] = dis.readFloat();
                node.setValue(fieldIndex, data, data.length);
            }
            break;
        case BaseField.SFBool:
            node.setValue(fieldIndex, dis.readBoolean());
            break;
        case BaseField.SFColor:
            node.setValue(fieldIndex, new float[] { dis.readFloat(),
                    dis.readFloat(), dis.readFloat() }, 3);
            break;
        case BaseField.SFFloat:
            node.setValue(fieldIndex, dis.readFloat());
            break;
        case BaseField.SFImage:
            numFields = dis.readInt();
        {
            int data[] = new int[numFields];
            for (int counter = 0; counter < numFields; counter++)
                data[counter] = dis.readInt();
            node.setValue(fieldIndex, data, numFields);
        }

            break;
        case BaseField.SFInt32:
            node.setValue(fieldIndex, dis.readInt());
            break;
        case BaseField.SFNode:
            node.setValue(fieldIndex, theServerBrowser.getNode(dis.readInt()));
            break;
        case BaseField.SFRotation:
            node.setValue(fieldIndex, new float[] { dis.readFloat(),
                    dis.readFloat(), dis.readFloat(), dis.readFloat() }, 4);
            break;
        case BaseField.SFString:
            node.setValue(fieldIndex, dis.readUTF());
            break;
        case BaseField.SFTime:
            node.setValue(fieldIndex, dis.readDouble());
            break;
        case BaseField.SFVec2f:
            node.setValue(fieldIndex, new float[] { dis.readFloat(),
                    dis.readFloat() }, 2);
            break;
        case BaseField.SFVec3f:
            node.setValue(fieldIndex, new float[] { dis.readFloat(),
                    dis.readFloat(), dis.readFloat() }, 3);
            break;
        default:
            System.err.println("Unknown field type.");
        }
    }

    void sendExceptionPacket(Throwable t, DataOutputStream dos)
            throws IOException {
        dos.writeByte(NetworkProtocolConstants.RESPONSE_EXCEPTION);
        dos.writeUTF(t.getClass().getName());
        String errorMessage = t.getMessage();
        if (errorMessage == null)
            dos.writeUTF("");
        else
            dos.writeUTF(errorMessage);
    }

    void sendErrorPacket(Throwable t, DataOutputStream dos) throws IOException {
        t.printStackTrace();
        dos.writeByte(NetworkProtocolConstants.RESPONSE_ERROR);
        dos.writeUTF(t.getClass().getName());
        String errorMessage = t.getMessage();
        if (errorMessage == null)
            dos.writeUTF("");
        else
            dos.writeUTF(errorMessage);
    }

    /** * @see org.web3d.browser.BrowserCoreListener#browserInitialized(org.web3d.vrml.nodes.VRMLScene)  */
    public void browserInitialized(VRMLScene scene) {
        synchronized (writeLock) {
            try {
                dos.writeInt(NetworkProtocolConstants.BROWSER_CHANGED_BROADCAST);
                dos.writeInt(BrowserEvent.INITIALIZED);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** * @see org.web3d.browser.BrowserCoreListener#urlLoadFailed(java.lang.String)  */
    public void urlLoadFailed(String msg) {
        synchronized (writeLock) {
            try {
                dos.writeInt(NetworkProtocolConstants.BROWSER_CHANGED_BROADCAST);
                dos.writeInt(BrowserEvent.URL_ERROR);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** * @see org.web3d.browser.BrowserCoreListener#browserShutdown()  */
    public void browserShutdown() {
        synchronized (writeLock) {
            try {
                dos.writeInt(NetworkProtocolConstants.BROWSER_CHANGED_BROADCAST);
                dos.writeInt(BrowserEvent.SHUTDOWN);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * The browser has been disposed, all resources may be freed.
     */
    public void browserDisposed() {
        // ignore
    }

    /**
     * Transmit a fieldChanged notification.
     * @param fieldID Network field ID for this field
     * @param fieldType Field type using vrml.eai.field.FieldTypes
     * @param node The node to get field value from
     * @param fieldIndex The field's index on the node
     */
    public void transmitFieldChanged(int fieldID, VRMLNodeType node,
            int fieldIndex) {
        synchronized (writeLock) {
            try {
                dos.writeInt(NetworkProtocolConstants.EVENTOUT_CHANGED_BROADCAST);
                dos.writeInt(fieldID);
                dos.writeDouble(timeClock.getTime());
                transmitFieldValue(fieldID, node, fieldIndex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Write a field's current value onto the network.
     * @param fieldID The network field ID
     * @param node The node to get the field value from
     * @param fieldIndex The index of the field to get the value of
     */
    private void transmitFieldValue(int fieldID, VRMLNodeType node, int fieldIndex)
            throws IOException {
        VRMLFieldData data = node.getFieldValue(fieldIndex);
        //dos.writeInt(data.dataType);
        int innerCounter=0;
        switch (theServerBrowser.getFieldType(fieldID)) {
            case BaseField.MFColor:
                dos.writeInt(data.numElements);
                innerCounter=0;
                for (int counter=0; counter<data.numElements; counter++) {
                    dos.writeFloat(data.floatArrayValue[innerCounter++]);
                    dos.writeFloat(data.floatArrayValue[innerCounter++]);
                    dos.writeFloat(data.floatArrayValue[innerCounter++]);
                }
                break;
            case BaseField.MFFloat:
                dos.writeInt(data.numElements);
                for (int counter = 0; counter < data.numElements; counter++)
                    dos.writeFloat(data.floatArrayValue[counter]);
                break;
            case BaseField.MFInt32:
                dos.writeInt(data.numElements);
                for (int counter = 0; counter < data.numElements; counter++)
                    dos.writeInt(data.intArrayValue[counter]);
                break;
            case BaseField.MFNode:
                dos.writeInt(data.numElements);
                for (int counter = 0; counter < data.numElements; counter++)
                    dos
                        .writeInt(theServerBrowser
                                .generateNodeID((VRMLNodeType) data.nodeArrayValue[counter]));
                break;
            case BaseField.MFRotation:
                dos.writeInt(data.numElements);
                innerCounter=0;
                for (int counter=0; counter<data.numElements; counter++) {
                    dos.writeFloat(data.floatArrayValue[innerCounter++]);
                    dos.writeFloat(data.floatArrayValue[innerCounter++]);
                    dos.writeFloat(data.floatArrayValue[innerCounter++]);
                    dos.writeFloat(data.floatArrayValue[innerCounter++]);
                }
                break;
            case BaseField.MFString:
                dos.writeInt(data.numElements);
                for (int counter = 0; counter < data.numElements; counter++)
                    dos.writeUTF(data.stringArrayValue[counter]);
                break;
            case BaseField.MFTime:
                dos.writeInt(data.numElements);
                for (int counter = 0; counter < data.numElements; counter++)
                    dos.writeDouble(data.doubleArrayValue[counter]);
                break;
            case BaseField.MFVec2f:
                dos.writeInt(data.numElements);
                innerCounter=0;
                for (int counter=0; counter<data.numElements; counter++) {
                    dos.writeFloat(data.floatArrayValue[innerCounter++]);
                    dos.writeFloat(data.floatArrayValue[innerCounter++]);
                }
                break;
            case BaseField.MFVec3f:
                dos.writeInt(data.numElements);
                innerCounter=0;
                for (int counter=0; counter<data.numElements; counter++) {
                    dos.writeFloat(data.floatArrayValue[innerCounter++]);
                    dos.writeFloat(data.floatArrayValue[innerCounter++]);
                    dos.writeFloat(data.floatArrayValue[innerCounter++]);
                }
                break;
            case BaseField.SFBool:
                dos.writeBoolean(data.booleanValue);
                break;
            case BaseField.SFColor:
                dos.writeFloat(data.floatArrayValue[0]);
                dos.writeFloat(data.floatArrayValue[1]);
                dos.writeFloat(data.floatArrayValue[2]);
                break;
            case BaseField.SFFloat:
                dos.writeFloat(data.floatValue);
                break;
            case BaseField.SFImage:
                dos.writeInt(data.numElements);
                for (int counter=0; counter<data.numElements; counter++)
                    dos.writeInt(data.intArrayValue[counter]);
                break;
            case BaseField.SFInt32:
                dos.writeInt(data.intValue);
                break;
            case BaseField.SFNode:
                dos
                .writeInt(theServerBrowser
                        .generateNodeID((VRMLNodeType) data.nodeValue));
                break;
            case BaseField.SFRotation:
                dos.writeFloat(data.floatArrayValue[0]);
                dos.writeFloat(data.floatArrayValue[1]);
                dos.writeFloat(data.floatArrayValue[2]);
                dos.writeFloat(data.floatArrayValue[3]);
                break;
            case BaseField.SFString:
                dos.writeUTF(data.stringValue);
                break;
            case BaseField.SFTime:
                dos.writeDouble(data.doubleValue);
                break;
            case BaseField.SFVec2f:
                dos.writeFloat(data.floatArrayValue[0]);
                dos.writeFloat(data.floatArrayValue[1]);
                break;
            case BaseField.SFVec3f:
                dos.writeFloat(data.floatArrayValue[0]);
                dos.writeFloat(data.floatArrayValue[1]);
                dos.writeFloat(data.floatArrayValue[2]);
                break;
        }
     }
}