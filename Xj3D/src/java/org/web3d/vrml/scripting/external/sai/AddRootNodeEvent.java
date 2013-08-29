package org.web3d.vrml.scripting.external.sai;

/*****************************************************************************
 *                    Copyright North Dakota State University, 2002
 *              Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLWorldRootNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEvent;

/**
 * Simple queue element for posting addRootNode calls
 *
 * @author Brad Vender
 * @version $Revision: 1.6 $
 */
class AddRootNodeEvent implements ExternalEvent {

    /**
     * Create a new add root node event
     * @param root The scene root to modify
     * @param child The root node to add
     */
    AddRootNodeEvent(VRMLWorldRootNodeType root, VRMLNodeType child) {
        rootNode=root;
        childNode=child;
    }

    /** Root node to modify */
    VRMLWorldRootNodeType rootNode;

    /** Child node to add */
    VRMLNodeType childNode;

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#doEvent()
     */
    public void doEvent() {
        rootNode.addChild(childNode);
    }

    /**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
	 */
	public boolean isConglomerating() {
		return false;
	}    
    
}
