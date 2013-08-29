/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2007
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
// none

// Local imports
import org.web3d.image.NIOBufferImage;

/**
 * The listener interface for receiving notice that a texture has changed.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public interface VRMLTextureListener {

	/**
     * Invoked when an underlying image has changed.
     *
     * @param idx The stage which changed.
     * @param node The texture which changed.
     * @param image The image as a data buffer for this texture.
     * @param url The url used to load this image.
     */
    public void textureImageChanged(int idx,
                                    VRMLNodeType node,
                                    NIOBufferImage image,
                                    String url);
	
    /**
     * Invoked when all of the underlying images have changed.
     *
     * @param len The number of valid entries in the image array.
     * @param node The textures which changed.
     * @param image The images for this texture.
     * @param url The urls used to load these images.
     */
    public void textureImageChanged(int len,
                                    VRMLNodeType[] node,
                                    NIOBufferImage[] image,
                                    String[] url);

    /**
     * Invoked when the texture parameters have changed.  The most
     * effecient route is to set the parameters before the image.
     *
     * @param idx The texture index which changed.
     * @param mode The mode for the stage.
     * @param source The source for the stage.
     * @param function The function to apply to the stage values.
     * @param alpha The alpha value to use for modes requiring it.
     * @param color The color to use for modes requiring it.  3 Component color.
     */
    public void textureParamsChanged(int idx, int mode,
                                     int source,
                                     int function,
                                     float alpha,
                                     float[] color);

    /**
     * Invoked when the texture parameters have changed.  The most
     * effecient route is to set the parameters before the image.
     *
     * @param idx The texture index which changed.
     * @param mode The mode for the stage.
     * @param source The source for the stage.
     * @param function The function to apply to the stage values.
     * @param alpha The alpha value to use for modes requiring it.
     * @param color The color to use for modes requiring it.  An array of 3 component colors.
     */
    public void textureParamsChanged(int idx,
                                     int mode[],
                                     int[] source,
                                     int[] function,
                                     float alpha,
                                     float[] color);
}
