/***************************************************************************** 
 *                        Web3d.org Copyright (c) 2001 - 2007 
 *                               Java Source 
 * 
 * This source is licensed under the GNU LGPL v2.1 
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information 
 * 
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it. 
 * 
 ****************************************************************************/ 

package org.web3d.vrml.scripting.external.sai;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;

import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;

import org.web3d.x3d.sai.X3DNodeTypes;
import org.web3d.x3d.sai.X3DProtoDeclaration;
import org.web3d.x3d.sai.X3DProtoInstance;

import org.xj3d.sai.X3DNodeTypeMapper;

/**
 * SAIProtoInstance is a node wrapper which extends SAINode to implement
 * the X3DProtoInstance interface as well as the X3DNode interface.
 */
public class SAIProtoInstance extends SAINode implements X3DProtoInstance {

    /** The internal proto representation */
    private VRMLProtoInstance protoInst;
    
    /** The implementation node types */
    int implNodeTypeArray[];
    
    /**
     * @param theRealNode The underlying node
     * @param aNodeFactory Node factory that some field's need
     * @param aFieldFactory Answers field related questions
     * @param template The PROTO declaration this instance was made from
     * @param queue The event queue for realization requests
     * This version of the constructor reads the node type from the proto template.
     */
    SAIProtoInstance(VRMLNodeType theRealNode, SAINodeFactory aNodeFactory, SAIFieldFactory aFieldFactory,
        X3DProtoDeclaration template, ExternalEventQueue queue) {
        super(theRealNode, aNodeFactory, aFieldFactory, queue);
        
        protoInst = (VRMLProtoInstance)theRealNode;
    }
    
    /**
     * @param theRealNode The underlying node
     * @param aNodeFactory Node factory that some field's need
     * @param aFieldFactory Answers field related questions
     * @param queue The event queue for realization requests
     * This version of the constructor reads the node type from the proto instance
     * since the proto template is not available.
     */
    SAIProtoInstance(VRMLNodeType theRealNode, SAINodeFactory aNodeFactory, SAIFieldFactory aFieldFactory,
            ExternalEventQueue queue) {
        super(theRealNode, aNodeFactory, aFieldFactory, queue);
        
        protoInst = (VRMLProtoInstance)theRealNode;
    }
    
    /** * @see org.web3d.x3d.sai.X3DProtoInstance#getImplementationTypes()  */
    public int[] getImplementationTypes() {
        if ( implNodeTypeArray == null ) {
            X3DNodeTypeMapper typeMapper = X3DNodeTypeMapper.getInstance( );
            VRMLNodeType implNode = protoInst.getImplementationNode( );
            implNodeTypeArray = typeMapper.getInterfaceTypes( implNode.getVRMLNodeName( ) );
        }
        return( implNodeTypeArray );
    }
    
    /** * @see org.web3d.x3d.sai.X3DNode#getNodeType()  */
    public int[] getNodeType() {
        // should throw InvalidNodeException when this has been disposed of
        return( new int[]{ X3DNodeTypes.X3DProtoInstance } );
    }
}
