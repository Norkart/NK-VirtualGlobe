/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
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
import org.j3d.aviatrix3d.Appearance;

// Local imports
import org.web3d.vrml.nodes.VRMLAppearanceNodeType;

/**
 * OpenGL-specific rendering requirements for appearance properties.
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */
public interface OGLAppearanceNodeType
    extends VRMLAppearanceNodeType, OGLVRMLNode {

    /**
     * Returns the Appearance node representation used by this object
     *
     * @return The appearance to use in the parent Shape3D
     */
    public Appearance getAppearance();

    /**
     * Set the texture coordinate generation mode for a texture set.  If
     * its not set then texture coordinates will be used.  A value of
     * null will clear the setting.
     *
     * @param setNum The set which this tex gen mode refers
     * @param mode The mode or NULL
     */
    public void setTexCoordGenMode(int setNum, String mode);
}
