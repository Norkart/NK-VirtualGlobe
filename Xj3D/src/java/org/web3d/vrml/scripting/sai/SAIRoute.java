/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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

// Standard imports
// None

// Application specific imports
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DRoute;

/**
 * Representation of a ROUTE structure in X3D.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class SAIRoute implements X3DRoute {

    /** The source node of the route */
    final BaseNode srcNode;

    /** The source field name of the route */
    final String srcField;

    /** The destination node of the route */
    final BaseNode destNode;

    /** The destination field name of the route */
    final String destField;

    /**
     * Construct a profile descriptor for the given information.
     */
    public SAIRoute(BaseNode srcNode,
                    String srcField,
                    BaseNode destinationNode,
                    String destinationField) {
    	if ((srcField==null)||(destinationField==null))
    		throw new IllegalArgumentException("Field names cannot be null");
        this.srcNode = srcNode;
        this.srcField = srcField;
        this.destNode = destinationNode;
        this.destField = destinationField;
    }

    /**
     * Get the reference to the source node of this route.
     *
     * @return The source node reference
     */
    public X3DNode getSourceNode() {
        return srcNode;
    }

    /**
     * Get the name of the source field of this route.
     *
     * @return The source node field's name
     */
    public String getSourceField() {
        return srcField;
    }

    /**
     * Get the reference to the destination node of this route.
     *
     * @return The destination node reference
     */
    public X3DNode getDestinationNode() {
        return destNode;
    }

    /**
     * Get the name of the destination field of this route.
     *
     * @return The destination node field's name
     */
    public String getDestinationField() {
        return destField;
    }
    
    /** @see java.lang.Object#hasCode */
    public int hashCode() {
    	return srcField.hashCode()+destField.hashCode()
    	      +destNode.hashCode()+srcNode.hashCode();
    }
    
    /** @see java.lang.Object#equals */
    public boolean equals(Object obj) {
    	if (obj instanceof SAIRoute) {
    		SAIRoute other=(SAIRoute)obj;
    		return ((other.srcField.equals(srcField))&&
    				(other.srcNode==srcNode)&&
    				(other.destField.equals(destField))&&
    				(other.destNode==destNode));
    	} else
    		return false;
    }
}
