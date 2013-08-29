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

// Standard imports

// Application specific imports

/**
 * Notification that a network node has changed roles.  Roles
 * are defined in VRMLNetworkInterfaceNodeType.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface NetworkRoleListener {
    /**
     * The role of this node has changed.
     *
     * @param newRole The new role, reader, writer, inactive.
     * @param node The node which changed roles.
     */
    public void roleChanged(int newRole, Object node);
}
