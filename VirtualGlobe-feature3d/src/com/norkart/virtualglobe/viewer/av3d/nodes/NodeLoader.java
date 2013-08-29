//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.av3d.nodes;

import org.j3d.aviatrix3d.Node;
import org.j3d.aviatrix3d.rendering.BoundingVolume;

/** The node loader interface, works together with AutoLoadNode
 *
 * Implementations of NodeLoader shold initiate loading of a node when requestNode()
 * is called. It should then keep the node with a hard reference until takeNode
 * is called. getBounds should return a BoundingVolume that completely covers the
 * node.
 */
public interface NodeLoader {
    
    /** 
     * Called by AutoLoadNode to get an upper bound estimate of the node bounds
     */
      public BoundingVolume getBounds();
    /** 
     * Called by AutoLoadNode to initiate loading of the child node. It may be 
     * called several times before the node is finaly loaded and passed over to 
     * the AutoLoadNode.
     */
    public void           requestLoad();
    /**
     * Called by AutoLoadNode to complete loading of the child node. If the 
     * child node is not yet loaded it should return null. The child node is 
     * returned only once pr loading. After the child node is returned the Loader 
     * should clear all references to it and prepare for a possible new load 
     * request if the child node is later reclaimed by the garbage collector and 
     * even later requested for reloading.
     */
    public Node           takeNode();
  
}