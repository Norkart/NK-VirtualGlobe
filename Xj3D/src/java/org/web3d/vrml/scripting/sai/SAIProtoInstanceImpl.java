/*****************************************************************************
 *                    Web3d.org Copyright (c) 2001 - 2007
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
import java.lang.ref.ReferenceQueue;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;

import org.web3d.x3d.sai.X3DNodeTypes;
import org.web3d.x3d.sai.X3DProtoInstance;

import org.xj3d.sai.X3DNodeTypeMapper;

/**
 * The representation of any node that is constructed from a PROTO
 * declaration.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
class SAIProtoInstanceImpl extends BaseNode
    implements X3DProtoInstance {
    
    /** The internal proto representation */
    private VRMLProtoInstance protoInst;
    
    /** The implementation node types */
    private int implNodeTypeArray[];
    
    /**
     * Create a new script node implementation.
     *
     * @param node The source node this is wrapping
     * @param refQueue The queue used for dealing with field references
     * @param fac Factory used to create field wrappers
     * @param fal The access listener for propogating s2 requests
     * @param bnf The factory used to create node wrappers
     */
    public SAIProtoInstanceImpl(VRMLNodeType node,
        ReferenceQueue refQueue,
        FieldFactory fac,
        FieldAccessListener fal,
        BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
        
        protoInst = (VRMLProtoInstance)node;
    }
    
    //----------------------------------------------------------
    // Methods defined by X3DProtoInstance
    //----------------------------------------------------------
    
    /**
     * Get the listing of implementation types for proto nodes. This performs
     * the same functionality as the getType() method in the base class. For
     * ProtoInstances, that method is required to return a single value that
     * says it is a proto instance and this method is used to determine the
     * final type.
     *
     * @return The list of types of the implemented node
     */
    public int[] getImplementationTypes( ) {
        if ( implNodeTypeArray == null ) {
            X3DNodeTypeMapper typeMapper = X3DNodeTypeMapper.getInstance( );
            VRMLNodeType implNode = protoInst.getImplementationNode( );
            implNodeTypeArray = typeMapper.getInterfaceTypes( implNode.getVRMLNodeName( ) );
        }
        return( implNodeTypeArray );
    }
    
    /** @see org.web3d.x3d.sai.X3DNode#getNodeType()  */
    public int[] getNodeType() {
        // should throw InvalidNodeException when this has been disposed of
        return( new int[]{ X3DNodeTypes.X3DProtoInstance } );
    }
}
