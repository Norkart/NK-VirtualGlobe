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

package org.web3d.vrml.renderer.j3d.nodes;

// Standard imports
import javax.media.j3d.Material;

// Application specific imports
import org.web3d.vrml.nodes.VRMLMaterialNodeType;

/**
 * An abstract representation of any form of material
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface J3DMaterialNodeType
    extends VRMLMaterialNodeType, J3DVRMLNode {

    /**
     * Returns a J3D Material node representation of the contents
     *
     * @return The material used to render the object
     */
    public Material getMaterial();

    /**
     * Add a listener for transparency changes
     *
     * @param tl The listener to add
     */
     public void addTransparencyListener(J3DTransparencyListener tl);

    /**
     * Remove a listener for transparency changes
     *
     * @param tl The listener to remove
     */
     public void removeTransparencyListener(J3DTransparencyListener tl);

    /**
     * Set whether lighting will be used for this material.  In general
     * you should let the material node decide this.  Needed to handle
     * IndexedLineSets or other geometry that specifically declares lighting
     * be turned off.
     *
     * @param enable Whether lighting is enabled
     */
    public void setLightingEnable(boolean enable);
}
