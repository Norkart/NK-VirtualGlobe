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

// Application specific imports
import org.web3d.vrml.nodes.VRMLTextureNodeType;

/**
 * VRML representation of 2D texture properties.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface VRMLTextureProperties2DNodeType extends VRMLAppearanceChildNodeType {
    /**
     * Get the boundary color.  This is a 4 component color.
     *
     * @param color A preallocated 4 component color array;
     */
    public void getBoundaryColor(float[] color);

    /**
     * Get the boundary width.
     *
     * @return The boundary width
     */
     public int getBoundaryWidth();

     /**
      * Get the boundary mode for S.
      *
      * @return The boundary mode.  Defined in TextureConstants.
      */
     public int getBoundaryModeS();

     /**
      * Get the boundary mode for T.
      *
      * @return The boundary mode.  Defined in TextureConstants.
      */
     public int getBoundaryModeT();

     /**
      * Get the magnigification filter.
      *
      * @return The mag filter.  Defined in TextureConstants.
      */
     public int getMagnificationFilter();

     /**
      * Get the minification filter.
      *
      * @return The min filter.  Defined in TextureConstants.
      */
     public int getMinificationFilter();

     /**
      * Get the generateMipsMaps field.
      *
      * @return Should mips be generated for this object.
      */
     public boolean getGenerateMipMaps();

     /**
      * Get the Anisotropic Mode.
      *
      * @return The anistropic mode.  Defined in TextureConstants.
      */
     public int getAnisotropicMode();

     /**
      * Get the AnisotropicFilter Degree.
      *
      * @return The anisotropic degree.
      */
     public float getAnisotropicDegree();
}
