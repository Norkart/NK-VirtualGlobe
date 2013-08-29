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

package org.web3d.vrml.scripting.external.sai;

import java.lang.reflect.Constructor;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;

import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;

import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoDeclaration;
import org.web3d.x3d.sai.X3DProtoInstance;

import org.xj3d.sai.X3DNodeComponentMapper;

/** 
 * MappingSAINodeFactory is an implementation of the SAINodeFactory
 * interface which maps between VRMLNodeType and X3DNode instances.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */

class MappingSAINodeFactory implements SAINodeFactory {
    
    /** Queue for processing realization requests for nodes */
    private ExternalEventQueue theEventQueue;
    
    /** The SAIFieldFactory for the nodes to use. */
    private SAIFieldFactory theFieldFactory;
    
    /** Mapper to determine the package for each node */
    private X3DNodeComponentMapper packageMapper;
    
    /** The basic constructor for this factory */
    MappingSAINodeFactory(SAIFieldFactory aFieldFactory, ExternalEventQueue queue) {
        theFieldFactory=aFieldFactory;
        theEventQueue=queue;
        packageMapper = X3DNodeComponentMapper.getInstance( );
    }
    
    /** @see org.web3d.vrml.scripting.external.sai.SAINodeFactory#getSAINode */
    public X3DNode getSAINode(VRMLNodeType vrmlNode) {
        if (vrmlNode != null) {
            SAINode node = null;
            if (vrmlNode instanceof VRMLProtoInstance) {
                return new SAIProtoInstance( vrmlNode, this, theFieldFactory, theEventQueue);
            }
            else {
                String nodeName = vrmlNode.getVRMLNodeName( );
                String pkgName = packageMapper.getComponentName( nodeName );
                if ( pkgName != null ) {
                    String className = "org.xj3d.sai.external.node." + pkgName +".SAI"+ nodeName;
                    try {
                        // get the class instance
                        Class nodeClass = Class.forName( className );
                        
                        // get the class's constructor with the appropriate arguments
                        Constructor constructor = nodeClass.getConstructor( new Class[]{ 
                                VRMLNodeType.class, 
                                SAINodeFactory.class, 
                                SAIFieldFactory.class, 
                                ExternalEventQueue.class } );
                        
                        // instantiate the object
                        Object object = constructor.newInstance( new Object[]{ 
                                vrmlNode, 
                                this, 
                                theFieldFactory, 
                                theEventQueue } );
                        
                        // cast it to ensure it's the type we want
                        node = (SAINode)object;
                    }
                    catch ( Exception e ) { 
                    }
                } 
                if ( node == null ) {
                    node = new SAINode( 
                        vrmlNode, 
                        this, 
                        theFieldFactory, 
                        theEventQueue );
                }
                return( node );
            }
        } else {
            return( null );
        }
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

