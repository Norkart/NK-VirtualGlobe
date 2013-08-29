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
package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
// None

/**
 * Denotes a node type that is part of the HAnim component.
 * <p>
 *
 * The Hanim component is internally implemented using the abstract HAnim
 * system from the <http://code.j3d.org/">j3d.org Code Repository</a>.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLHAnimHumanoidNodeType extends VRMLHAnimNodeType {

    /**
     * Notification that the event model is complete and skeleton should
     * perform all it's updates now.
     */
    public void updateMesh();
}
