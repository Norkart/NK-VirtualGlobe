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

package org.web3d.vrml.renderer.ogl.nodes;

// External imports

// Local imports
import org.j3d.aviatrix3d.Material;
import org.web3d.vrml.nodes.VRMLMaterialNodeType;

/**
 * A class that determines the appearance of an object.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public interface OGLMaterialNodeType
    extends VRMLMaterialNodeType, OGLVRMLNode {

    /**
     * Returns the material node representation used by this object
     *
     * @return The material to use in the parent appearance
     */
    public Material getMaterial();

    /**
     * Set whether lighting will be used for this material.  In general
     * you should let the material node decide this.  Needed to handle
     * IndexedLineSets or other geometry that specifically declares lighting
     * be turned off.
     *
     * @param enable Whether lighting is enabled
     */
    public void setLightingEnable(boolean enable);

    /**
     * Set whether the geometry has local colors to override the diffuse color.
     *
     * @param enable Whether local color is enabled
     * @param hasAlpha true with the local color also contains alpha valuess
     */
    public void setLocalColor(boolean enable, boolean hasAlpha);

}
