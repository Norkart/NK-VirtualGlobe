/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
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

/**
 * Nodes which can appear in an appearance field of an Appearance Node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface VRMLProductStructureChildNodeType extends VRMLNodeType {
    /**
     * Set the name of this node.
     *
     * @param name The new name.
     */
    public void setName(String name);

    /**
     * Get the name of this node.
     *
     * @return The name.
     */
    public String getName();
}
