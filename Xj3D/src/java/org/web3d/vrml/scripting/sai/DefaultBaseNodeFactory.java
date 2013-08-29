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
import java.lang.ref.ReferenceQueue;

import java.lang.reflect.Constructor;

//Local imports
import org.web3d.x3d.sai.X3DNode;

import org.web3d.vrml.lang.VRMLNode;

import org.web3d.vrml.nodes.VRMLNodeType;

import org.xj3d.sai.X3DNodeComponentMapper;

/**
 * The factory for producing SAI node wrappers 
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class DefaultBaseNodeFactory implements BaseNodeFactory {
    
    /** Reference queue used for keeping track of field object instances */
    private ReferenceQueue fieldQueue;
    
    /** Factory used for field generation */
    private FieldFactory fieldFactory;
    
    /** Listener for dealing with the script wrapper for field access */
    private FieldAccessListener fieldAccessListener;
    
    /** Flag indicating that concrete node implementations are available */
    private boolean useConcreteNodeWrappers;
    
    /** Mapper to determine the package for each node */
    private X3DNodeComponentMapper packageMapper;
    
    /**
     * Constructor
     *
     * @param useConcreteNodeWrappers Flag indicating that concrete node
     * implementations are available and should be returned.
     * @param fieldQueue The queue used for dealing with field references
     * @param fieldFactory Factory used to create field wrappers
     * @param fieldAccessListener The access listener for propogating s2 requests
     */
    DefaultBaseNodeFactory ( 
        boolean useConcreteNodeWrappers,
        ReferenceQueue fieldQueue,
        FieldFactory fieldFactory,
        FieldAccessListener fieldAccessListener ) {
        
        this.useConcreteNodeWrappers = useConcreteNodeWrappers;
        this.fieldQueue = fieldQueue;
        this.fieldFactory = fieldFactory;
        this.fieldAccessListener = fieldAccessListener;
        
        packageMapper = X3DNodeComponentMapper.getInstance( );
    }
    
    /**
     * Given the argument VRMLNodeType, create and return a cooresponding 
     * BaseNode instance.
     * 
     * @param vrmlNode the node to wrap in a BaseNode instance
     * @return The BaseNode instance
     */
    public X3DNode getBaseNode(VRMLNodeType vrmlNode) {
        if (vrmlNode != null) {
            BaseNode node = null;
            if ( useConcreteNodeWrappers ) {
                String nodeName = vrmlNode.getVRMLNodeName( );
                String pkgName = packageMapper.getComponentName( nodeName );
                if ( pkgName != null ) {
                    String className = "org.xj3d.sai.internal.node." + pkgName +".SAI"+ nodeName;
                    try {
                        // get the class instance
                        Class nodeClass = Class.forName( className );
                        
                        // get the class's constructor with the appropriate arguments
                        Constructor constructor = nodeClass.getConstructor( new Class[]{ 
                                VRMLNodeType.class, 
                                ReferenceQueue.class, 
                                FieldFactory.class, 
                                FieldAccessListener.class,
                                BaseNodeFactory.class } );
                        
                        // instantiate the object
                        Object object = constructor.newInstance( new Object[]{ 
                                vrmlNode, 
                                fieldQueue, 
                                fieldFactory, 
                                fieldAccessListener, 
                                this } );
                        
                        // cast it to ensure it's the type we want
                        node = (BaseNode)object;
                        
                    } catch ( Exception e ) { 
                    }
                }
            } 
            if ( node == null ) {
                node = new BaseNode( 
                    vrmlNode,
                    fieldQueue,
                    fieldFactory,
                    fieldAccessListener,
                    this );
            }
            return( node );
        } else {
            return( null );
        }
    }
}
