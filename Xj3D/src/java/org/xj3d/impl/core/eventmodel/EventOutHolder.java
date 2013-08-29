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

/**
 * A simple convenience holder for Node/eventOut combo information.
 * <p>
 * This holder just places the node and event out into a single structure to
 * make it easier for using it as a key for tables and trees.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
final class EventOutHolder {

    /** Reference to the source node of this route */
    VRMLNodeType srcNode;

    /** Index of the source field of this route */
    int srcIndex;

    /**
     * Test to see if we should send a route
     *
     * @return true if we have an event to process
     */
    final boolean needsProcessing() {
        return srcNode.hasFieldChanged(srcIndex);
    }

    public String toString() {
        return "srcNode: " + srcNode + " srcIndex: " + srcIndex;
    }

    public int hashCode() {
        return srcNode.hashCode() + srcIndex;
    }


    public boolean equals(Object obj) {
        if (obj instanceof EventOutHolder) {
            if (((EventOutHolder)obj).srcNode == srcNode &&
                ((EventOutHolder)obj).srcIndex == srcIndex) {
                return true;
            }
        }
        return false;
    }
}
