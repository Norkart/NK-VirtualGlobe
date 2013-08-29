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

package org.web3d.vrml.renderer.mobile.nodes;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLBindableNodeType;

/**
 * An abstract representation of any bindable node.
 * <p>
 *
 * Bindable nodes are activatable nodes as well. The difference is that there
 * is a stack of them and they change their output of the isBound event out.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface MobileBindableNodeType
    extends MobileVRMLNode, VRMLBindableNodeType {

    /**
     * Notify the bindable node that it is on the stack, or not on the
     * stack, as the case may be and that it should send bind events as
     * appropriate
     *
     * @param onStack true if this node is now on the stack
     */
    public void setOnStack(boolean onStack);
}
