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

package org.web3d.vrml.scripting.external.sai;


import org.web3d.x3d.sai.X3DRoute;
import org.web3d.x3d.sai.X3DNode;


/**
 * Representation of the route wrapper for X3D SAI.
 * @author Brad Vender (copied from X3DRoute by Justin Couch)
 * @version $Revision: 1.3 $
 */
class SAIRoute implements X3DRoute {

    /** The source node for the route. */
    X3DNode sourceNode;

    /** The source field for the route. */
    String sourceField;

    /** The destination node for the route. */
    X3DNode destinationNode;

    /** The destination field for the route. */
    String destinationField;

    /** Make a new route descriptor.
      * @param srcNode The source node.
      * @param srcField The source field.
      * @param dstNode The destination node.
      * @param dstField The destination field.
      */
    SAIRoute(
        X3DNode srcNode, String srcField, X3DNode dstNode, String dstField
    ) {
    	if ((srcField==null)||(dstField==null))
    		throw new IllegalArgumentException("Field names cannot be null");
        sourceNode=srcNode;
        sourceField=srcField;
        destinationNode=dstNode;
        destinationField=dstField;
    }

    /**
     * Get the reference to the source node of this route.
     *
     * @return The source node reference
     */
    public X3DNode getSourceNode() {
        return sourceNode;
    }

    /**
     * Get the name of the source field of this route.
     *
     * @return The source node field's name
     */
    public String getSourceField() {
        return sourceField;
    }

    /**
     * Get the reference to the destination node of this route.
     *
     * @return The destination node reference
     */
    public X3DNode getDestinationNode() {
        return destinationNode;
    }

    /**
     * Get the name of the destination field of this route.
     *
     * @return The destination node field's name
     */
    public String getDestinationField() {
        return destinationField;
    }
    
    /** @see java.lang.Object#hasCode */
    public int hashCode() {
    	return sourceNode.hashCode()+destinationNode.hashCode()
    	      +sourceField.hashCode()+destinationField.hashCode();
    }
    
    /** @see java.lang.Object#equals */
    public boolean equals(Object obj) {
    	if (obj instanceof SAIRoute) {
    		SAIRoute other=(SAIRoute)obj;
    		return ((other.sourceField.equals(sourceField))&&
    				(other.sourceNode==sourceNode)&&
    				(other.destinationField.equals(destinationField))&&
    				(other.destinationNode==destinationNode));
    	} else return false;
    }
    
}
