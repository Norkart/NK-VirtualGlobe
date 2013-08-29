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

import java.io.IOException;
import java.net.InetAddress;

import org.web3d.vrml.scripting.external.buffer.NetworkEventQueue;

import vrml.eai.Browser;
import vrml.eai.ConnectionException;
import vrml.eai.InvalidBrowserException;
import vrml.eai.InvalidNodeException;
import vrml.eai.InvalidURLException;
import vrml.eai.InvalidVrmlException;
import vrml.eai.Node;
import vrml.eai.URLUnavailableException;
import vrml.eai.event.BrowserListener;
import vrml.eai.field.InvalidEventInException;
import vrml.eai.field.InvalidEventOutException;

/**
 * NetworkBrowserClient communicates over the network to a
 * NetworkBrowserServer.
 */
public class NetworkBrowserClient implements Browser, NetworkStartupListener {
    
    /** Error message for calling services on a disposed browser instance */
    static final String DISPOSED_BROWSER_MESSAGE = "Browser has been disposed.";
    
    /** Client's identifier string */
    static final String CLIENT_NAME="Xj3D NetEAI client";
    
    /** Client's version string */
    static final String CLIENT_VERSION="Client:1.0";
    
    /** The event queue for set field requests */
    NetworkEventQueue eventQueue;
    
    /** Flag for whether network initialization finished */
    boolean networkInitialized;
    
    /** The object communicating with the server */
    ClientProcessingTask requestProcessor;
    
    /** Create a browser client which will communicate to a server
     *  listening at some network location.
     * @param addr The address the server resides at.
     * @param port The port the server is listening on.
     * @throws IOException Indicates unable to connect to server.
     */
    NetworkBrowserClient(InetAddress addr, int port) throws IOException {
        eventQueue=new NetworkEventQueue(null);
        synchronized (this) {
            requestProcessor=new ClientProcessingTask(addr,port,this, new BrowserBroadcaster(this), eventQueue);
            new Thread(requestProcessor).start();
            try {
                while (!networkInitialized)
                    wait();
            } catch (InterruptedException e) {
                throw new ConnectionException("Interrupted waiting for network init.");
            }
        }
    }

	/** * @see vrml.eai.Browser#getName()  */
	public String getName() throws InvalidBrowserException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    String serverName=requestProcessor.getServerName();
	    return CLIENT_NAME+" "+serverName;
	}
	
	/** * @see vrml.eai.Browser#getVersion()  */
	public String getVersion() throws InvalidBrowserException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    String serverVersion=requestProcessor.getServerVersion();
	    return CLIENT_VERSION+" "+serverVersion;
	}

	/** * @see vrml.eai.Browser#getCurrentSpeed()  */
	public float getCurrentSpeed() throws InvalidBrowserException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    return requestProcessor.getCurrentSpeed();
	}

	/** * @see vrml.eai.Browser#getCurrentFrameRate()  */
	public float getCurrentFrameRate() throws InvalidBrowserException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    return requestProcessor.getCurrentFrameRate();
	}

	/** * @see vrml.eai.Browser#getWorldURL()  */
	public String getWorldURL() throws InvalidBrowserException, URLUnavailableException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    return requestProcessor.getWorldURL();
	}

	/** * @see vrml.eai.Browser#replaceWorld(vrml.eai.Node[])  */
	public void replaceWorld(Node[] nodes) throws IllegalArgumentException, InvalidBrowserException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
		int nodeIDs[];
		if (nodes==null)
		    nodeIDs=null;
		else {
		    nodeIDs=new int[nodes.length];
		    for (int counter=0; counter<nodes.length; counter++)
		        nodeIDs[counter]=((NetEAINode)nodes[counter]).nodeID;
		}
		requestProcessor.replaceWorld(nodeIDs);
	}

	/** * @see vrml.eai.Browser#loadURL(java.lang.String[], java.lang.String[])  */
	public void loadURL(String[] url, String[] parameter) throws InvalidBrowserException, InvalidURLException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    requestProcessor.loadURL(url,parameter);
	}

	/** * @see vrml.eai.Browser#setDescription(java.lang.String)  */
	public void setDescription(String desc) throws InvalidBrowserException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    requestProcessor.setDescription(desc);
	}

	/** * @see vrml.eai.Browser#createVrmlFromString(java.lang.String)  */
	public Node[] createVrmlFromString(String vrmlString) throws InvalidBrowserException, InvalidVrmlException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    int nodeIDs[]=requestProcessor.createVrmlFromString(vrmlString);
	    if (nodeIDs==null) {
	        System.out.println("createVrmlFromString returning null.");
	        return null;
	    } else {
	        Node result[]=new Node[nodeIDs.length];
	        for (int counter=0; counter<result.length; counter++)
	            result[counter]=new NetEAINode(nodeIDs[counter], requestProcessor);
	        System.out.println("createVrmlFromString returning.");
	        return result;
	    }
	}

	/** * @see vrml.eai.Browser#createVrmlFromURL(java.lang.String[], vrml.eai.Node, java.lang.String)  */
	public void createVrmlFromURL(String[] url, Node node, String eventIn) throws InvalidBrowserException, InvalidNodeException, InvalidURLException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    requestProcessor.createVrmlFromURL(url,((NetEAINode)node).nodeID,eventIn);
	}

	/** * @see vrml.eai.Browser#getNode(java.lang.String)  */
	public Node getNode(String name) throws InvalidNodeException, InvalidBrowserException, URLUnavailableException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    int nodeID=requestProcessor.getNode(name);
	    if (nodeID==-1)
	        return null;
	    else
	        return new NetEAINode(nodeID,requestProcessor);
	}

	/** * @see vrml.eai.Browser#addRoute(vrml.eai.Node, java.lang.String, vrml.eai.Node, java.lang.String)  */
	public void addRoute(Node fromNode, String eventOut, Node toNode, String eventIn) throws InvalidBrowserException, InvalidEventOutException, InvalidEventInException, InvalidNodeException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    int fromNodeID=((NetEAINode)fromNode).nodeID;
	    int toNodeID=((NetEAINode)toNode).nodeID;
	    requestProcessor.addRoute(fromNodeID,eventOut,toNodeID,eventIn);
	}

	/** * @see vrml.eai.Browser#deleteRoute(vrml.eai.Node, java.lang.String, vrml.eai.Node, java.lang.String)  */
	public void deleteRoute(Node fromNode, String eventOut, Node toNode, String eventIn) throws InvalidBrowserException, InvalidEventOutException, InvalidEventInException, InvalidNodeException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    int fromNodeID=((NetEAINode)fromNode).nodeID;
	    int toNodeID=((NetEAINode)toNode).nodeID;
	    requestProcessor.removeRoute(fromNodeID,eventOut,toNodeID,eventIn);
	}

	/** * @see vrml.eai.Browser#beginUpdate()  */
	public void beginUpdate() throws InvalidBrowserException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    eventQueue.beginUpdate();
	}

	/** * @see vrml.eai.Browser#endUpdate()  */
	public void endUpdate() throws InvalidBrowserException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    eventQueue.endUpdate();
	}

	/** * @see vrml.eai.Browser#addBrowserListener(vrml.eai.event.BrowserListener)  */
	public void addBrowserListener(BrowserListener l) throws InvalidBrowserException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
       requestProcessor.addBrowserListener(l);
	}

	/** * @see vrml.eai.Browser#removeBrowserListener(vrml.eai.event.BrowserListener)  */
	public void removeBrowserListener(BrowserListener l) throws InvalidBrowserException {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
	    requestProcessor.addBrowserListener(l);
	}

	/** * @see vrml.eai.Browser#dispose()  */
	public void dispose() {
	    if (requestProcessor==null)
	        throw new InvalidBrowserException(DISPOSED_BROWSER_MESSAGE);
		requestProcessor.shutdownBrowser();
		requestProcessor=null;
	}

    /** * @see org.web3d.vrml.scripting.external.neteai.NetworkStartupListener#notifyNetworkInitializationComplete()  */
    synchronized public void notifyNetworkInitializationComplete() {
        networkInitialized=true;
        notify();
    }

}
