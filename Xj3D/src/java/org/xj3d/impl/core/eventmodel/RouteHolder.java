/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.impl.core.eventmodel;

// External imports
// none

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.lang.VRMLFieldDeclaration;

/**
 * A simple convenience holder for internal route information.
 * <p>
 * This holder just places all the route information in the right place so
 * that it is quick to send information. Holds all the references together
 * so that we don't need to go wandering about cache memory.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
final class RouteHolder {

    /** Reference to the source node of this route */
    VRMLNodeType srcNode;

    /** Index of the source field of this route */
    int srcIndex;

    /** Reference to the destination node of this route */
    VRMLNodeType destNode;

    /** Index of the destination field of this route */
    int destIndex;

    /**
     * Test to see if we should send a route
     *
     * @return true if we have an event to process
     */
    final boolean needsProcessing() {
        return srcNode.hasFieldChanged(srcIndex);
    }

    /**
     * Test for equality of routes.
     * Routes are equal the source and destination node and index fields
     * match up.
     * @param other The object to test equality with.
     * @see java.lang.Object#equals
     */
    public boolean equals(Object other) {
        if ((other !=null) && (other instanceof RouteHolder)) {
            RouteHolder x=(RouteHolder)other;
            return (srcNode==x.srcNode && srcIndex==x.srcIndex &&
                destNode==x.destNode && destIndex==x.destIndex);
        } else return super.equals(other);
    }

    /**
     * Compute a hashCode.
     * The default method in Object doesn't appear to be working with HashMap
     * at the moment.
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        int total=0;
        if (srcNode!=null)
            total+=srcNode.hashCode();
        if (destNode!=null)
            total+=destNode.hashCode();
        return total+srcIndex+destIndex;
    }

    /**
     * Convenience method to send a route from the source node to the
     * destination node. No testing is done to see if this should be sent
     * or not.
     *
     * @param timestamp The timestamp of this event
     */
    final void sendRoute(double timestamp) {
        srcNode.sendRoute(timestamp, srcIndex, destNode, destIndex);
    }

    public String toString() {
       StringBuffer buff = new StringBuffer();
       VRMLFieldDeclaration decl;
       buff.append("route   from: ");
       buff.append(srcNode.getVRMLNodeName());
       buff.append(" field: ");
       decl = srcNode.getFieldDeclaration(srcIndex);
       buff.append(decl.getName());
       buff.append(" to: ");
       buff.append(destNode.getVRMLNodeName());
       buff.append(" field: ");
       decl = destNode.getFieldDeclaration(destIndex);
       buff.append(decl.getName());
       return buff.toString();
    }
}
