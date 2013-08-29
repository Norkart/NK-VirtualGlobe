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

// Application specific imports
import org.web3d.vrml.renderer.mobile.sg.Appearance;
import org.web3d.vrml.nodes.VRMLAppearanceNodeType;

/**
 * A class that determines the appearance of an object
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface MobileAppearanceNodeType
    extends VRMLAppearanceNodeType, MobileVRMLNode {

    /**
     * Returns the Appearance node representation used by this object
     *
     * @return The appearance to use in the parent Shape3D
     */
    public Appearance getAppearance();
}
