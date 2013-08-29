/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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
import org.web3d.vrml.lang.VRMLNodeTemplate;

/**
 * VRMLExternProtoDeclare is a node interface, used by implementations of VRML's
 * ExternProtoDeclare node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 * @see org.web3d.vrml.nodes.VRMLProtoDeclare
 * @see org.web3d.vrml.nodes.VRMLProtoInstance
 *
 */
public interface VRMLExternProtoDeclare
    extends VRMLNodeTemplate, VRMLSingleExternalNodeType {

    /**
     * Get the real prototype information that this external reference
     * maps to. If the proto information has not been loaded yet, or it is
     * invalid this will return null.
     *
     * @return The underlying proto definition or null
     */
    public VRMLProtoDeclare getProtoDetails();
}
