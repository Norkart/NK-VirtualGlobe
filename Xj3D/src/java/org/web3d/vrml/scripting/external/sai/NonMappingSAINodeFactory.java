package org.web3d.vrml.scripting.external.sai;

/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoDeclaration;
import org.web3d.x3d.sai.X3DProtoInstance;

/** 
  * NonMappingSAINodeFactory is a simple implementation of the SAINodeFactory
  * interface which does not maintain a static 1-1 mapping between VRMLNodeType
  * and X3DNode instances.
  * <P>
  * This implementation just creates new instances of the SimpleWrappingNode
  * each time, and returns what may be the unique VRMLNodeType corresponding
  * to that SimpleWrappingNode as appropriate.
  * @author Brad Vender
  */

class NonMappingSAINodeFactory implements SAINodeFactory {
	
	/** Queue for processing realization requests for nodes */
	ExternalEventQueue theEventQueue;
	
    /** The SAIFieldFactory for the nodes to use. */
    SAIFieldFactory theFieldFactory;
  
    /** The basic constructor for this factory */
    NonMappingSAINodeFactory(SAIFieldFactory aFieldFactory, ExternalEventQueue queue) {
        theFieldFactory=aFieldFactory;
        theEventQueue=queue;
    }

    /** @see org.web3d.vrml.scripting.external.sai.SAINodeFactory#getSAINode */
    public X3DNode getSAINode(VRMLNodeType vrmlNode) {
    	if (vrmlNode != null)
    		if (vrmlNode instanceof VRMLProtoInstance)
    			return new SAIProtoInstance(vrmlNode,this,theFieldFactory, theEventQueue);
    		else
    			return new SAINode(vrmlNode,this,theFieldFactory, theEventQueue);
    	else
    		return null;
    }

    /** @see org.web3d.vrml.scripting.external.sai.SAINodeFactory#getVRMLNode */
    public VRMLNodeType getVRMLNode(X3DNode aNode) {
    	if (aNode == null)
    		return null;
        else if (aNode instanceof SAINode)
            return ((SAINode)aNode).getVRMLNode();
        else
            throw new RuntimeException("Incorrect node factory for Node");
    }

    /** * @see org.web3d.vrml.scripting.external.sai.SAINodeFactory#getSAIProtoNode(org.web3d.vrml.nodes.VRMLNodeType, org.web3d.x3d.sai.X3DProtoDeclaration)  */
    public X3DProtoInstance getSAIProtoNode(VRMLNodeType node, X3DProtoDeclaration template) {
        if (node!=null)
            return new SAIProtoInstance(node,this,theFieldFactory,template,theEventQueue);
        else
            return null;
    }

}

