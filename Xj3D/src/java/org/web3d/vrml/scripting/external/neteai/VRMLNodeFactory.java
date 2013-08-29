package org.web3d.vrml.scripting.external.neteai;

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
  * VRMLNodeFactory is the intermediary intended to break what would be two
  * dependency loops in the EAI implementation--between EventIn*NodeWrapper
  * and SimpleWrappingNode and between SimpleWrappingNode and 
  * EventOut*Node Wrapper.  
  * <P>
  * Given a VRMLNodeType or vrml.eai.Node, this class can produce or locate
  * the corresponding instance.  Whether the instance returned is unique or
  * simply equivalent between subsequent calls is implementation dependent.
  * <P>
  * @author Brad Vender
  */

interface VRMLNodeFactory {

    /** Create or locate a vrml.eai.Node instance corresponding to this
     *  VRMLNodeType instance.
     *  @param vrmlNode the VMRLNodeType instance
     */
     Node getEAINode(VRMLNodeType vrmlNode);

    /** Create or locate a VRMLNodeType corresponding to this vrml.eai.Node.
     *  @param aNode the vrml.eai.Node instance
     */
    VRMLNodeType getVRMLNode(Node aNode);

}

