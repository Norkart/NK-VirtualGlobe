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
import org.web3d.vrml.nodes.VRMLNavigationInfoNodeType;

/**
 * Java3D specific representation of a navigation info node.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface MobileNavigationInfoNodeType
    extends VRMLNavigationInfoNodeType, MobileBindableNodeType {

    // There will be specific listeners in here for changing component types
    // shortly.
}
