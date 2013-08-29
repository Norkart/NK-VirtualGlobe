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

import vrml.eai.InvalidNodeException;
import vrml.eai.Node;
import vrml.eai.field.EventIn;
import vrml.eai.field.EventOut;
import vrml.eai.field.InvalidEventInException;
import vrml.eai.field.InvalidEventOutException;

/**
 * @author vender
 * NetEAINode is the node wrapper for the networked version of the EAI classes.
 */
public class NetEAINode extends Node {
    
    /** The type name for the node */
    String nodeType;
    
    /** The request processor for this node */
    FieldAndNodeRequestProcessor requestProcessor;
    
    /** The ID for this node */
    int nodeID;
    
    /** Basic constructor.
     * @param ID The network ID for this node
     * @param processor Object used to do node requests.
     */
    NetEAINode(int ID, FieldAndNodeRequestProcessor processor) {
        nodeID=ID;
        requestProcessor=processor;
    }
    
    /** * @see vrml.eai.Node#getType()  */
    public String getType() throws InvalidNodeException {
        if (nodeType==null)
        	nodeType=requestProcessor.getNodeType(nodeID);
        return nodeType;
    }

    /** * @see vrml.eai.Node#getEventIn(java.lang.String)  */
    public EventIn getEventIn(String name) throws InvalidEventInException,
            InvalidNodeException {
        return requestProcessor.getEventIn(nodeID,name);
    }

    /** * @see vrml.eai.Node#getEventOut(java.lang.String)  */
    public EventOut getEventOut(String name) throws InvalidEventOutException,
            InvalidNodeException {
        return requestProcessor.getEventOut(nodeID,name);
    }

    /** * @see vrml.eai.Node#dispose()  */
    public void dispose() throws InvalidNodeException {
        requestProcessor.disposeNode(nodeID);
    }

    /** * @see java.lang.Object#equals(java.lang.Object)  */
    public boolean equals(Object obj) {
        if (obj instanceof NetEAINode)
            return nodeID==((NetEAINode)obj).nodeID;
        return super.equals(obj);
    }
    
}
