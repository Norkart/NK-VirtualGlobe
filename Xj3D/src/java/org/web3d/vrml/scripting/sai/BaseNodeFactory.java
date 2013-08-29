/***************************************************************************** 
 *                        Web3d.org Copyright (c) 2007 
 *                               Java Source 
 * 
 * This source is licensed under the GNU LGPL v2.1 
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information 
 * 
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it. 
 * 
 ****************************************************************************/ 

package org.web3d.vrml.scripting.sai;

// External imports
// none

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.x3d.sai.X3DNode;

/**
 * Defines the requirements of the node wrapper factory.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface BaseNodeFactory {
    
    /**
     * Given the argument VRMLNodeType, create and return a cooresponding 
     * BaseNode instance.
     * 
     * @param vrmlNode the node to wrap in a BaseNode instance
     * @return The BaseNode instance
     */
    public X3DNode getBaseNode(VRMLNodeType vrmlNode);
}
