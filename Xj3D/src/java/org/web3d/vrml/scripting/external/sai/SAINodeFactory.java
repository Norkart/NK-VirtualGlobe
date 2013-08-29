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
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoDeclaration;
import org.web3d.x3d.sai.X3DProtoInstance;

/** 
  * SAINodeFactory is the intermediary intended to break what would be two
  * dependency loops in the SAI implementation--between EventIn*NodeWrapper
  * and SimpleWrappingNode and between SimpleWrappingNode and 
  * EventOut*Node Wrapper.  
  * <P>
  * Given a VRMLNodeType or X3DNode, this class can produce or locate
  * the corresponding instance.  Whether the instance returned is unique or
  * simply equivalent between subsequent calls is implementation dependent.
  * <P>
  * @author Brad Vender
  */

public interface SAINodeFactory {

    /** Create or locate a X3DNode instance corresponding to this
     *  VRMLNodeType instance.
     *  @param vrmlNode the VRMLNodeType instance
     *  @return The X3DNode instance
     */
    X3DNode getSAINode(VRMLNodeType vrmlNode);

    /** Create or locate a VRMLNodeType corresponding to this vrml.eai.Node.
        @param aNode the X3DNode instance
        @return The underlying VRMLNodeType instance
     */
    VRMLNodeType getVRMLNode(X3DNode aNode);

    /**
     * Create or locate an X3DProtoInstance node corresponding to this
     * VRMLNodeType instance.
     * @param node The VRMLNodeType instance
     * @param template The originating template
     * @return The X3DProtoInstance instance
     */
    X3DProtoInstance getSAIProtoNode(VRMLNodeType node, X3DProtoDeclaration template);
}

