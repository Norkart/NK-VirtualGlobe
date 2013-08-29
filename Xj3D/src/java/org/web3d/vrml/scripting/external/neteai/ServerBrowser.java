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
import java.util.HashMap;
import java.util.Map;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.util.IntHashMap;
import org.web3d.vrml.lang.InvalidNodeTypeException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.sav.VRMLParseException;
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;

import vrml.eai.InvalidBrowserException;
import vrml.eai.InvalidNodeException;
import vrml.eai.InvalidVrmlException;

/**
 * ServerBrowser is a simplified version of EAIBrowser intended
 * to simplify the processing loop in ServerProcessingTask.
 * 
 */
public class ServerBrowser {
    
    /** The party responsible for sending field change messages */
    FieldChangeTransmitter changeTransmitter;
    
    /** Used to generate unique field ID's */
    IDGenerator fieldIDGenerator;
    
    /** Maps from network field ID's to VRMLNodeTypeAdapter instances */
    IntHashMap nodeAdapterTable;
	
    /** Maps from field ID's to VRMLNodeType instances */
    FieldToVRMLNodeTypeMapper fieldMapper;
    
    /** The listener who will process the browser changed events */
    BrowserCoreListener coreListener;
    
	/** BrowserCore required for some browser services */
	BrowserCore theBrowserCore;

	/** The CommonBrowser required for some browser services */
	VRML97CommonBrowser theBrowserImpl;
	
	/** Table for mapping integer node ID's back to the local VRMLNodeType objects */
	IntHashMap nodeIDToNodeTable;

	/** Table for mapping VRMLNodeType objects to integer node ID's.  Should not be
	 * a WeakHashMap because referenced nodes are potentially live until the connection
	 * is closed.  */
	HashMap nodeToNodeIDTable;

	public ServerBrowser(BrowserCore browserCore, VRML97CommonBrowser vrmlBrowser) {
	    theBrowserCore=browserCore;
	    theBrowserImpl=vrmlBrowser;
	    nodeToNodeIDTable=new HashMap();
	    nodeIDToNodeTable=new IntHashMap();
	    fieldMapper=new FieldToVRMLNodeTypeMapper();
	    nodeAdapterTable=new IntHashMap();
	    fieldIDGenerator=new IDGenerator();
	}
	
	/**
	 * Create VRML nodes from a string and acquire node ID's for the
	 * top level nodes.
	 * @param vrmlCode The string specifying the VRML code.
	 * @return Array of node ID's for the top level nodes.
	 */
	int[] createVrmlFromString(String vrmlCode)
        throws InvalidBrowserException, InvalidVrmlException {

        if (theBrowserImpl==null)
            throw new InvalidBrowserException();
        else {
            int result[]=null;
            /* Just translation of CommonBrowser's results to what we need */
            try {
                VRMLNodeType children[]=
                    theBrowserImpl.createVrmlFromString(vrmlCode,null);
                if (children!=null) {
                    result=new int[children.length];
                    int counter=0;
                    for (counter=0;counter<result.length; counter++)
                        result[counter]= generateNodeID(children[counter]);
                }
            } catch (VRMLParseException p) {
                throw new InvalidVrmlException(p.getMessage());
            } catch (InvalidNodeTypeException v) {
                //throw new RuntimeException(v.getMessage());
            	throw new InvalidVrmlException(v.getMessage());
            } catch (IOException ioe) {
                throw new RuntimeException(ioe.getMessage());
            }
            return result;
        }
    }
	
	/** Remove a node ID from the list of known IDs 
	 * @param nodeID The node to dispose of
	 */
	void disposeNode(int nodeID) throws InvalidNodeException {
		VRMLNodeType node=(VRMLNodeType) nodeIDToNodeTable.get(nodeID);
		if (node==null)
			throw new InvalidNodeException("Node ID"+Integer.toString(nodeID));
		nodeToNodeIDTable.remove(node);
		nodeIDToNodeTable.remove(nodeID);
	}
 
	/** Server implementation of Node.getEventIn
	 * @param nodeID Network ID of the node
	 * @param fieldName Name of event In
	 * @return Network ID of eventIn
	 */
	int getEventIn(int nodeID, String fieldName) {
	    VRMLNodeType node=getNode(nodeID);
	    VRMLNodeTypeAdapter adapter=(VRMLNodeTypeAdapter) nodeAdapterTable.get(nodeID);
	    if (adapter==null) {
	        adapter=new VRMLNodeTypeAdapter(node,fieldIDGenerator,changeTransmitter);
	        nodeAdapterTable.put(nodeID,adapter);
	    }
	    int fieldID=fieldMapper.registerEventIn(adapter,fieldName);
	    return fieldID;
	}
	
	/** Server implementation of Node.getEventOut
	 * @param nodeID Network ID of host node
	 * @param fieldName name of eventOut
	 * @return Network ID of eventOut
	 */
	int getEventOut(int nodeID,String fieldName) {
	    VRMLNodeType node=getNode(nodeID);
	    VRMLNodeTypeAdapter adapter=(VRMLNodeTypeAdapter) nodeAdapterTable.get(nodeID);
	    if (adapter==null) {
	        adapter=new VRMLNodeTypeAdapter(node,fieldIDGenerator,changeTransmitter);
	        nodeAdapterTable.put(nodeID,adapter);
	    }
	    int fieldID=fieldMapper.registerEventOut(adapter,fieldName);
	    return fieldID;	    
	}
	
	/** Determine the vrml.eai.BaseField type for a field
	 * @param fieldID The network ID of the field.
	 * @return
	 */
	int getFieldType(int fieldID) {
	    return fieldMapper.findNode(fieldID).getFieldType(fieldID);
	}
	
	/** Map from network node ID back to VRMLNodeType instances */
	public VRMLNodeType getNode(int nodeID) {
	    if (nodeID==-1)
	        return null;
	    VRMLNodeType node=(VRMLNodeType) nodeIDToNodeTable.get(nodeID);
	    if (node==null)
	        throw new InvalidNodeException();
	    else
	        return node;
	}
	
	/** Locate or generate a node ID for a node.  Node ID's are
	 * unique to the target node and multiple calls for the same
	 * node will result in the same ID.
	 * @param node The node to generate an ID for.
	 * @return The node's new ID.
	 */
	public int generateNodeID(VRMLNodeType node) {
		Integer oldID=(Integer) nodeToNodeIDTable.get(node);
		if (oldID!=null)
			return oldID.intValue();
		else {	    
		    int nodeID=getNextNodeID();
		    nodeIDToNodeTable.put(nodeID,node);
		    nodeToNodeIDTable.put(node,new Integer(nodeID));
		    return nodeID;
		}
	}
	
	/** Determine the local field ID for a field given its
	 *  network field ID
	 * @param fieldID The network field ID
	 * @return The local field ID (field index)
	 */
	int getLocalFieldID(int fieldID) {
	    return fieldMapper.findNode(fieldID).getLocalFieldID(fieldID);
	}
	
	/** Find the node associated with a given fieldID
	 * @param fieldID The network field ID.
	 * @return The node if it exists.
	 */
	VRMLNodeType getNodeFromFieldID(int fieldID) {
	    VRMLNodeTypeAdapter adapter=fieldMapper.findNode(fieldID);
	    if (adapter==null)
	        throw new InvalidNodeException();
	    else
	        return adapter.underlyingNode;
	}
	
	/** Try to find a node by name and assigns an ID to it automatically */
	int getNodeID(String nodeName) throws InvalidNodeException {
		VRMLNodeType node=(VRMLNodeType) theBrowserCore.getDEFMappings().get(nodeName);
		if (node==null)
			throw new InvalidNodeException(nodeName);
		    return generateNodeID(node);
	}
	
	/** Counter for generating node ID's */
	int nextNodeID;
	
	/** Ugly utility method for generating nodeID's.  Will break
	 *  if MAX_INT node ID's are in use.  */
	int getNextNodeID() {
	    nextNodeID++;
		while (nodeIDToNodeTable.get(nextNodeID)!=null) {
			nextNodeID++;
			if (nextNodeID==Integer.MAX_VALUE)
				nextNodeID=0;
		}
		return nextNodeID;
	}
	
	/** Pass loadURL request off to brower impl.
	 * @param URLs The URL list
	 * @param parameters The parameter list
	 */
	void loadURL(String URLs[], String parameters[]) {
        Map params = new HashMap();
        int num_params = (parameters == null) ? 0 : parameters.length;
        for(int i = 0; i < num_params; i++) {
            int eq_idx = parameters[i].indexOf('=');
            String key = parameters[i].substring(0, eq_idx);
            String value = parameters[i].substring(eq_idx + 1);
            params.put(key, value);
        }
        theBrowserImpl.loadURL(URLs,params);
	}
	
	/** Call replaceWorld using a set of node IDs which
	 *  will be converted to real nodes.
	 * @param nodeIDs The array of nodes.
	 */
	void replaceWorld(int nodeIDs[]) {
	    VRMLNodeType convertedNodes[];
	    if (nodeIDs==null)
	        convertedNodes=null;
	    else {
	        convertedNodes=new VRMLNodeType[nodeIDs.length];
	        for (int counter=0; counter<nodeIDs.length; counter++)
	            convertedNodes[counter]=getNode(nodeIDs[counter]);
	    }
	    theBrowserImpl.replaceWorld(convertedNodes);
	}

    /**
     * Turn on browser changed broadcasts
     */
    public void enableBrowserListener() {
        theBrowserCore.addCoreListener(coreListener);
    }

    /**
     * Turn off browser changed broadcasts
     */
    public void disableBrowserListener() {
        theBrowserCore.removeCoreListener(coreListener);
    }
	
    /** Register the field changed transmitter.  Needed to seperate from the
     *  constructor to avoid constructor loop.
     * @param transmitter The new transmitter
     */
    void setFieldChangeTransmitter(FieldChangeTransmitter transmitter) {
        changeTransmitter=transmitter;
    }
    
    /** Register the browser core listener.  Needed seperate from
     * constructor to avoid constructor loop.
     * @param listener The listener for browser changes.
     */
    public void setCoreListener(BrowserCoreListener listener) {
        coreListener=listener;
    }

    /**
     * Disable broadcasts for a specific field
     * @param fieldID The network ID of the field to disable broadcasts for
     */
    public void disableFieldBroadcast(int fieldID) {
        fieldMapper.findNode(fieldID).deactivateFieldListener(fieldID);
    }

    /**
     * Enable broadcasts for a specific field
     * @param fieldID The network ID of the field to enable broadcasts for
     */
    public void enableFieldBroadcast(int fieldID) {
        fieldMapper.findNode(fieldID).activateFieldListener(fieldID);
    }

    /**
     * createVrmlFromURL request.
     * @see  vrml.eai.Browser.createVrmlFromURL
     * @param URLs The URLs to try
     * @param nodeID The network ID of the target node
     * @param eventInName The name of the eventIn to target
     */
    public void createVrmlFromURL(String[] URLs, int nodeID, String eventInName) {
        theBrowserImpl.createVrmlFromURL(URLs,getNode(nodeID),eventInName);
    }

    /**
     * @return The current frame rate
     */
    public float getCurrentFrameRate() {
        return theBrowserImpl.getCurrentFrameRate();
    }
    
    /** @return The current navigation speed */
    public float getCurrentSpeed() {
        return theBrowserImpl.getCurrentSpeed();
    }

    /**
     * @return The current world URL for the browser
     */
    public String getWorldURL() {
        return theBrowserImpl.getWorldURL();
    }
    
    /** Try to change the browser description
     * @param description New description
     */
    public void setDescription(String description) {
        theBrowserImpl.setDescription(description);
    }
    
}
