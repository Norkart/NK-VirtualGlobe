package org.web3d.vrml.scripting.external.eai;

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
import vrml.eai.Node;

/** 
  * NonMappingVRMLNodeFactory is a simple implementation of the VRMLNodeFactory
  * interface which does not maintain a static 1-1 mapping between VRMLNodeType
  * and vrml.eai.Node instances.
  * <P>
  * This implementation just creates new instances of the SimpleWrappingNode
  * each time, and returns what may be the unique VRMLNodeType corresponding
  * to that SimpleWrappingNode as appropriate.
  * @author Brad Vender
  */

class NonMappingVRMLNodeFactory implements VRMLNodeFactory {
    /** The EAIFieldFactory for the nodes to use. */
    EAIFieldFactory theFieldFactory;
  
    /** The basic constructor for this factory */
    NonMappingVRMLNodeFactory(EAIFieldFactory aFieldFactory) {
        theFieldFactory=aFieldFactory;
    }

    /** @see org.web3d.vrml.scripting.external.eai.VRMLNodeFactory#getEAINode */
    public Node getEAINode(VRMLNodeType vrmlNode) {
    	if (vrmlNode != null)
    		return new EAINode(vrmlNode,this,theFieldFactory);
    	else
    		return null;
    }

    /** @see org.web3d.vrml.scripting.external.eai.VRMLNodeFactory#getVRMLNode */
    public VRMLNodeType getVRMLNode(Node aNode) {
    	if (aNode == null)
    		return null;
        else if (aNode instanceof EAINode)
            return ((EAINode)aNode).getVRMLNode();
        else
            throw new RuntimeException("Incorrect node factory for Node");
    }

}

