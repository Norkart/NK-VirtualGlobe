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
import java.net.InetAddress;
import java.net.Socket;

import org.web3d.util.IntHashMap;
import org.web3d.vrml.scripting.external.buffer.NetworkEventQueue;

import vrml.eai.ConnectionException;
import vrml.eai.event.BrowserListener;
import vrml.eai.event.VrmlEventListener;
import vrml.eai.field.EventIn;
import vrml.eai.field.EventOut;

/**
 * ClientProcessingTask is the body of processing loop for
 * reading and writing data from the foreign browser.
 *
 * Access to the input stream is controlled by mutual exclusion using
 * SuspendedTaskData instances and readLock.
 *
 */
public class ClientProcessingTask implements Runnable, FieldAndNodeRequestProcessor {

    /** SuspendedTaskData is used to organize the thread resumption code.
     *  Each thread gets a SuspendedTaskData instance which is entered into the
     *  blocking task list and then calls block to block until its data arrives.
     *  The main processing task then calls complete when the data arrives.
     */
    class SuspendedTaskData {

        /** Flag for occupation of critical section */
        boolean dataAvailable;

        /** The type of the request */
        int requestType;

        /** The ID assigned to this task */
        int taskID;

        SuspendedTaskData(int requestType) {
            taskID=getNextTaskID();
            this.requestType=requestType;
        }

        /** * @see java.lang.Object#equals(java.lang.Object)  */
        public boolean equals(Object obj) {
            if (obj instanceof SuspendedTaskData)
                return (taskID==((SuspendedTaskData)obj).taskID);
            else
                return super.equals(obj);
        }

        /**
         * @return task ID assigned to the request
         */
        int getTaskID() {
            return taskID;
        }

        /** * @see java.lang.Object#hashCode()  */
        public int hashCode() {
            return taskID;
        }

        /** notify */
        synchronized void notifyDataAvailable() {
            dataAvailable=true;
            notify();
        }

        /** wait */
        synchronized void waitForData() throws InterruptedException {
            while (!dataAvailable)
                wait();
            dataAvailable=false;
        }
    }

    /** Table of blocking tasks. */
    private IntHashMap blockingTaskTable;

    /** Broadcaster responsible for sending browser change messages */
    private BrowserBroadcaster browserBroadcaster;

    /** Input stream from server */
    private DataInputStream dis;

    /** Output stream to server */
    private DataOutputStream dos;

    /** The queue for set field value requests */
    private NetworkEventQueue eventQueue;

    private EAIFieldAndNodeFactory fieldAndNodeFactory;

    /** Broadcaster responsible for sending field changed messages */
    private FieldBroadcaster fieldBroadcaster;

    /** Counter for generating node ID's */
    private int nextTaskID;

    /** Processing task wait's on this object
     *  while user tasks are reading from the input stream.
     *  Misusing SuspendedTaskData for the Mutex lock functions. */
    private SuspendedTaskData readLock;

    /** Flag for maintaining connection */
    private boolean remainOpen;

    /** The socket connecting to the server */
    private Socket serverConnection;

    /** The object waiting for the connection to finish */
    private NetworkStartupListener startupListener;

    /** Object to synchronize writing on */
    private Object writeLock;

    ClientProcessingTask(InetAddress addr, int port,
        NetworkStartupListener listener,
        BrowserBroadcaster broadcaster,
        NetworkEventQueue queue) throws IOException {

        remainOpen=true;
        serverConnection=new Socket(addr,port);
        blockingTaskTable=new IntHashMap();
        readLock=new SuspendedTaskData(0);
        writeLock=new Object();
        startupListener=listener;
        browserBroadcaster=broadcaster;
        eventQueue=queue;
        fieldAndNodeFactory=new DefaultEAIFieldAndNodeFactory(this,eventQueue);
        fieldBroadcaster=new GenericFieldBroadcaster(fieldAndNodeFactory);
    }

    /** Ask the server to activate browser changed broadcasts */
    void activateBrowserBroadcasts() {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.ADD_BROWSER_LISTENER_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /**
     * Turn on field changed notifications for a given field
     * @param fieldID The network field ID of the field
     */
    private void activateFieldBroadcasts(int fieldID) {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.ADD_EVENT_OUT_LISTENER_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(fieldID);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** Register a brower changed listener and if needed turn on the broadcast
     * @param l The listener to add.
     */
    void addBrowserListener(BrowserListener l) {
        if (browserBroadcaster.addBrowserListener(l))
            activateBrowserBroadcasts();
        System.out.println("Done adding browser listener.");
    }

    /**
     * Send addRoute request to the server
     * @param fromNodeID Network ID of the originating node
     * @param eventOut Name of originating event
     * @param toNodeID Network ID of the destination ndoe
     * @param eventIn Name of the destination event
     */
    public void addRoute(int fromNodeID, String eventOut, int toNodeID, String eventIn) {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.ADD_ROUTE_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(fromNodeID);
                dos.writeUTF(eventOut);
                dos.writeInt(toNodeID);
                dos.writeUTF(eventIn);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldAndNodeRequestProcessor#addVrmlEventListener(int, vrml.eai.event.VrmlEventListener)  */
    public void addVrmlEventListener(int fieldID, int fieldType, VrmlEventListener l) {
        if (fieldBroadcaster.addVrmlEventListener(fieldID,fieldType,l))
            activateFieldBroadcasts(fieldID);
    }

    /** Check for and handle the RESPONSE_EXCEPTION and RESPONSE_ERROR
     *  return types.  This method will throw an exception as appropriate
     *  for the error or exception the server is sending along.
     * @param data Data for this task
     * @throws IOException Thrown if error reading from data input stream.
     */
    void checkNormalResume(SuspendedTaskData data) throws IOException {
        byte returnType = dis.readByte();
        if (returnType != NetworkProtocolConstants.RESPONSE_OKAY) {
            String exceptionName = dis.readUTF();
            String exceptionDescription = dis.readUTF();
            blockingTaskTable.remove(data.getTaskID());
            readLock.notifyDataAvailable();
            System.out.println("Reconstituting exception:" + exceptionName);
            throw generateException(exceptionName, exceptionDescription);
        }
    }

    /** Transmit a createVrmlFromString request to the server
     *  and wait for the reply.
     * @param vrml The VRML to create.
     * @return The node IDs from the VRML.
     */
    int []createVrmlFromString(String vrml) {
        int result[];
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.CREATE_VRML_FROM_STRING_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeUTF(vrml);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        // We've been woken up, read result from from dis
        try {
            checkNormalResume(data);
            int numNodes=dis.readInt();
            if (numNodes==-1)
                result=null;
            else {
                result=new int[numNodes];
                for (int counter=0;counter<numNodes;counter++)
                    result[counter]=dis.readInt();
            }
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
        return result;
    }

    /** Ask the browser to stop sending browser broadcasts */
    void deactivateBrowserBroadcasts() {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.REMOVE_BROWSER_LISTENER_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }

    }

    /**
     * Request the server to stop sending field broadcasts for a field.
     * @param fieldID The network field ID of the field
     */
    private void deactivateFieldBroadcasts(int fieldID) {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.REMOVE_EVENT_OUT_LISTENER_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(fieldID);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** Notify server that node is no longer referenced.
     *  Does not check for remaining instances.
     * @param nodeID The node to dispose
     */
    public void disposeNode(int nodeID) {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.DISPOSE_NODE_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(nodeID);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }

    }

     /**
      * Create an exception by name
      * @param exceptionName The fully qualified class name of the exception
      * @param exceptionDescription The description parameter for the constructor
      * @return The constructed exception
      */
    private RuntimeException generateException(String exceptionName, String exceptionDescription) {
        Class exceptionClass;
        try {
            exceptionClass = Class.forName(exceptionName);
        } catch (ClassNotFoundException e) {
            return new RuntimeException("Unable to reconstitute "+exceptionName+" to generate error.");
        }
        if (exceptionClass==null)
            System.err.println("Null class trying to reconstruct "+exceptionName);
        try {
            return (RuntimeException) exceptionClass.getConstructor(new Class[]{String.class}).newInstance(new Object[]{exceptionDescription});
        } catch (Exception e) {
            return new RuntimeException("Unable to reconstitute "+exceptionName+
                    " with message "+exceptionDescription);
        }
    }

    /**
     *  * @see org.web3d.vrml.scripting.external.neteai.FieldAndNodeRequestProcessor#getEventIn(int, java.lang.String)
     */
    public EventIn getEventIn(int nodeID, String fieldName) {
        SuspendedTaskData data;
        synchronized(writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.GET_EVENTIN_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(nodeID);
                dos.writeUTF(fieldName);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        // We've been woken up, read result from from dis
        try {
            checkNormalResume(data);
            int fieldID=dis.readInt();
            int fieldType=dis.readInt();
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
            return fieldAndNodeFactory.generateEventIn(fieldID,fieldType);
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }

    }

    /**
     *  * @see org.web3d.vrml.scripting.external.neteai.FieldAndNodeRequestProcessor#getEventOut(int, java.lang.String)
     */
    public EventOut getEventOut(int nodeID, String fieldName) {
        SuspendedTaskData data;
        synchronized(writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.GET_EVENTOUT_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(nodeID);
                dos.writeUTF(fieldName);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        // We've been woken up, read result from from dis
        try {
            checkNormalResume(data);
            int fieldID=dis.readInt();
            int fieldType=dis.readInt();
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
            return fieldAndNodeFactory.getEventOut(fieldID,fieldType);
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldAndNodeRequestProcessor#getFieldValue(int, org.web3d.vrml.scripting.external.neteai.EventWrapper)  */
    public void getFieldValue(int fieldID, EventWrapper buffer) {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.GET_FIELD_VALUE_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(fieldID);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            buffer.loadFieldValue(dis);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** Ugly utility method for generating nodeID's.*/
    synchronized int getNextTaskID() {
        nextTaskID++;
        while (blockingTaskTable.get(nextTaskID)!=null) {
            nextTaskID++;
            if (nextTaskID==Integer.MAX_VALUE)
                nextTaskID=0;
        }
        return nextTaskID;
    }

    /** Transmit a getNode request to the server and wait for reply.
     * @param nodeName The nodeName to request
     * @return The ID for the node if found.
     */
    public int getNode(String nodeName) {
        SuspendedTaskData data;
        synchronized(writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.GET_NODE_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeUTF(nodeName);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        // We've been woken up, read result from from dis
        try {
            checkNormalResume(data);
            int nodeID=dis.readInt();
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
            return nodeID;
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }

    }

    /** Returns the type name for a given node.
     * @param nodeID The network ID of the node.
     * @return The type name of the node.
     */
    public String getNodeType(int nodeID) {
        SuspendedTaskData data;
        synchronized(writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.GET_NODE_TYPE_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(nodeID);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        // We've been woken up, read result from from dis
        try {
            checkNormalResume(data);
            String nodeType=dis.readUTF();
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
            return nodeType;
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }

    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldAndNodeRequestProcessor#getNumFieldValues(int)  */
    public int getNumFieldValues(int fieldID) {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.GET_NUM_FIELD_VALUES_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(fieldID);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            int numValues=dis.readInt();
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
            return numValues;
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldAndNodeRequestProcessor#getUserData(int)  */
    public Object getUserData(int fieldID) {
        return fieldBroadcaster.getUserData(fieldID);
    }

    /** Transmit a loadURL request to the server and wait
     *  for the reply.
     * @param URLs URL list for request.
     * @param params Parameter list for request.
     */
    void loadURL(String URLs[], String params[]) {
        SuspendedTaskData data;
        synchronized(writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.LOAD_URL_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                if (URLs==null)
                    dos.writeInt(-1);
                else {
                    dos.writeInt(URLs.length);
                    for (int counter=0; counter<URLs.length; counter++)
                        dos.writeUTF(URLs[counter]);
                }
                if (params==null)
                    dos.writeInt(-1);
                else {
                    dos.writeInt(params.length);
                    for (int counter=0; counter<params.length; counter++)
                        dos.writeUTF(params[counter]);
                }
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        // We've been woken up, read result from from dis
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** The connection has generated errors.  Close down.*/
    void markAbortConnection() {
        System.out.println("Connection shutting down due to errors.");
        remainOpen=false;
    }

    /**
     * Remove a browser listener and if none left deactivate the broadcasts.
     * @param l The listener to remove.
     */
    void removeBrowserListener(BrowserListener l) {
       if (browserBroadcaster.removeBrowserListener(l))
           deactivateBrowserBroadcasts();
    }

    /**
     * Remove a route from the scene
     * @param fromNodeID Source node network ID
     * @param eventOut Name of source field
     * @param toNodeID Destination node network ID
     * @param eventIn Name of destination field
     */
    public void removeRoute(int fromNodeID, String eventOut, int toNodeID, String eventIn) {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.REMOVE_ROUTE_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(fromNodeID);
                dos.writeUTF(eventOut);
                dos.writeInt(toNodeID);
                dos.writeUTF(eventIn);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldAndNodeRequestProcessor#removeVrmlEventListener(int, vrml.eai.event.VrmlEventListener)  */
    public void removeVrmlEventListener(int fieldID, VrmlEventListener l) {
        if (fieldBroadcaster.removeVrmlEventListener(fieldID,l))
            deactivateFieldBroadcasts(fieldID);
    }

    /** Transmit a replaceWorld request to the server and
     * wait for method competion.
     * @param nodeIDs The node IDs to use as top level nodes.
     */
    void replaceWorld(int nodeIDs[]) {
        SuspendedTaskData data;
        synchronized(writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.REPLACE_WORLD_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                if (nodeIDs==null)
                    dos.writeInt(-1);
                else {
                    dos.writeInt(nodeIDs.length);
                    for (int counter=0; counter<nodeIDs.length; counter++)
                        dos.writeInt(nodeIDs[counter]);
                }
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        // We've been woken up, read result from from dis
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** * @see java.lang.Runnable#run()
     *   Handle packets coming in from the server
     * */
    public void run() {
        try {
            try {
                synchronized(writeLock) {
                    dis=new DataInputStream(serverConnection.getInputStream());
                    dos=new DataOutputStream(serverConnection.getOutputStream());
                    dos.writeInt(NetworkProtocolConstants.CONNECTION_MAGIC_NUMBER_CLIENT);
                    int checkByte=dis.readInt();
                    if (checkByte!=NetworkProtocolConstants.CONNECTION_MAGIC_NUMBER_SERVER) {
                        System.out.println("Improper handshake.");
                        return;
                    } else {
                        System.out.println("Client handshake complete.");
                    }
                }
                synchronized (readLock) {
                startupListener.notifyNetworkInitializationComplete();
                // Not necessary to synchronize on a readLock, since the individual
                // process locks will take care of that.
                    while (remainOpen) {
                        int packetType=dis.readInt();
                        int taskID=-1;
                        switch (packetType) {
                            // All of the replies use the same code here.
                            case NetworkProtocolConstants.ADD_BROWSER_LISTENER_REPLY:
                            case NetworkProtocolConstants.ADD_EVENT_OUT_LISTENER_REPLY:
                            case NetworkProtocolConstants.ADD_ROUTE_REPLY:
                            case NetworkProtocolConstants.CREATE_VRML_FROM_STRING_REPLY:
                            case NetworkProtocolConstants.CREATE_VRML_FROM_URL_REPLY:
                            case NetworkProtocolConstants.DISPOSE_NODE_REPLY:
                            case NetworkProtocolConstants.GET_EVENTIN_REPLY:
                            case NetworkProtocolConstants.GET_EVENTOUT_REPLY:
                            case NetworkProtocolConstants.GET_FIELD_VALUE_REPLY:
                            case NetworkProtocolConstants.GET_CURRENT_FRAME_RATE_REPLY:
                            case NetworkProtocolConstants.GET_CURRENT_SPEED_REPLY:
                            case NetworkProtocolConstants.GET_IMAGE_SUB_VALUE_REPLY:
                            case NetworkProtocolConstants.GET_SERVER_NAME_REPLY:
                            case NetworkProtocolConstants.GET_SERVER_VERSION_REPLY:
                            case NetworkProtocolConstants.GET_NODE_REPLY:
                            case NetworkProtocolConstants.GET_NODE_TYPE_REPLY:
                            case NetworkProtocolConstants.GET_NUM_FIELD_VALUES_REPLY:
                            case NetworkProtocolConstants.GET_WORLD_URL_REPLY:
                            case NetworkProtocolConstants.LOAD_URL_REPLY:
                            case NetworkProtocolConstants.REMOVE_BROWSER_LISTENER_REPLY:
                            case NetworkProtocolConstants.REMOVE_EVENT_OUT_LISTENER_REPLY:
                            case NetworkProtocolConstants.REMOVE_ROUTE_REPLY:
                            case NetworkProtocolConstants.REPLACE_WORLD_REPLY:
                            case NetworkProtocolConstants.SET_DESCRIPTION_REPLY:
                            case NetworkProtocolConstants.SET_FIELD_VALUE_REPLY:
                                {
                                    taskID=dis.readInt();
                                    System.out.println("Client reading packet type "+packetType+" ID"+taskID);
                                    SuspendedTaskData task=(SuspendedTaskData) blockingTaskTable.get(taskID);
                                    try {
                                        task.notifyDataAvailable();
                                        readLock.waitForData();
                                    } catch (InterruptedException e) {
                                        System.out.println("Client wait interrupted.  Exiting.");
                                        remainOpen=false;
                                    }
                                }
                                break;
                            case NetworkProtocolConstants.EVENTOUT_CHANGED_BROADCAST:
                                System.out.println("EventOutChanged received.");
                                int fieldID=dis.readInt();
                                fieldBroadcaster.generateFieldBroadcast(fieldID,dis);
                                System.out.println("Done with eventOutChanged.");
                                break;
                            case NetworkProtocolConstants.BROWSER_CHANGED_BROADCAST: {
                                System.out.println("BrowserChanged received.");
                                int broadcastType=dis.readInt();
                                browserBroadcaster.queueBroadcast(broadcastType);
                                }
                                break;
                            default:
                                System.err.println("Unknown packet type "+packetType+" on client.");
                                break;
                        }
                        System.out.println("Client finished reading packet of type "+packetType+" ID"+taskID);
                    }
                }
            } finally {
                if (dos!=null)
                    dos.close();
                if (dis!=null)
                    dis.close();
                serverConnection.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     *  * @see org.web3d.vrml.scripting.external.neteai.FieldAndNodeRequestProcessor#setFieldValue(int, org.web3d.vrml.scripting.external.neteai.EventWrapper)
     */
    public void setFieldValue(int fieldID, EventWrapper buffer) {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.SET_FIELD_VALUE_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(fieldID);
                buffer.writeFieldValue(dos);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldAndNodeRequestProcessor#setUserData(int, java.lang.Object)  */
    public void setUserData(int fieldID, Object data) {
        fieldBroadcaster.setUserData(fieldID,data);
    }

    /**
     *
     * @param url The URLs to try
     * @param nodeID The network ID of the node to receive the event
     * @param eventIn The name of the receiving eventIn
     */
    public void createVrmlFromURL(String[] url, int nodeID, String eventIn) {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.CREATE_VRML_FROM_URL_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(url.length);
                for (int counter=0; counter<url.length; counter++)
                    dos.writeUTF(url[counter]);
                dos.writeInt(nodeID);
                dos.writeUTF(eventIn);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /**
     * @see vrml.eai.Browser#getName()
     * @return The identifier the server is using.
     */
    public String getServerName() {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.GET_SERVER_NAME_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            String serverName=dis.readUTF();
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
            return serverName;
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** * @see vrml.eai.Browser#getVersion()  */
    public String getServerVersion() {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.GET_SERVER_VERSION_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            String serverName=dis.readUTF();
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
            return serverName;
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** * @see vrml.eai.Browser#getCurrentSpeed()  */
    public float getCurrentSpeed() {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.GET_CURRENT_SPEED_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            float currentSpeed=dis.readFloat();
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
            return currentSpeed;
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** * @see vrml.eai.Browser#getCurrentFrameRate()  */
    public float getCurrentFrameRate() {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.GET_CURRENT_FRAME_RATE_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            float currentFrameRate=dis.readFloat();
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
            return currentFrameRate;
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** * @see vrml.eai.Browser#getWorldURL()  */
    public String getWorldURL() {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.GET_WORLD_URL_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            String worldURL=dis.readUTF();
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
            return worldURL;
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /** * @see vrml.eai.Browser#setDescription(java.lang.String)  */
    public void setDescription(String desc) {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.SET_DESCRIPTION_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeUTF(desc);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }
    }

    /**
     * Tell the server that everything should go away.
     */
    public void shutdownBrowser() {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.SHUTDOWN_SYSTEM);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        markAbortConnection();
        // There should be no need to wait for the server at this point.
        /*try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }*/
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldAndNodeRequestProcessor#getImageComponents(int)  */
    public int getImageComponents(int fieldID) {
        return getImageSubData(fieldID,(byte)2);
    }

    /**
     * Request one of the components of an SFImage field.
     * @param fieldID The network field ID
     * @param component The image component to request
     * @return The requested component
     */
    private int getImageSubData(int fieldID, byte component) {
        SuspendedTaskData data;
        synchronized (writeLock) {
            data=new SuspendedTaskData(NetworkProtocolConstants.GET_IMAGE_SUB_VALUE_REQUEST);
            blockingTaskTable.put(data.getTaskID(),data);
            try {
                dos.writeInt(data.requestType);
                dos.writeInt(data.getTaskID());
                dos.writeInt(fieldID);
                dos.writeByte(component);
            } catch (IOException ioe) {
                blockingTaskTable.remove(data.getTaskID());
                markAbortConnection();
                throw new ConnectionException("Unable to send request.");
            }
        }
        try {
            data.waitForData();
        } catch (InterruptedException ie) {
            throw new ConnectionException("Interrupted waiting for data.");
        }
        try {
            checkNormalResume(data);
            int componentValue=dis.readInt();
            readLock.notifyDataAvailable();
            blockingTaskTable.remove(data.getTaskID());
            return componentValue;
        } catch (IOException ioe) {
            blockingTaskTable.remove(data.getTaskID());
            markAbortConnection();
            readLock.notifyDataAvailable();
            throw new ConnectionException("Error reading results after resume.");
        }

    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldAndNodeRequestProcessor#getImageHeight(int)  */
    public int getImageHeight(int fieldID) {
        return getImageSubData(fieldID,(byte)1);
    }

    /** * @see org.web3d.vrml.scripting.external.neteai.FieldAndNodeRequestProcessor#getImageWidth(int)  */
    public int getImageWidth(int fieldID) {
        return getImageSubData(fieldID,(byte)0);
    }

}
