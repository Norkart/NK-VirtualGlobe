/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

/**
 * Defines a 2D transformation that is applied to a texture.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface X3DTextureTransform2DNode extends X3DTextureTransformNode {

    /**
     * Get the value of the current center position.
     *
     * @param position The array to copy the position into
     */
    public void getCenter(float[] position);

    /**
     * Set the center to a new position.
     *
     * @param position The new value to use
     */
    public void setCenter(float[] position);

    /**
     * Get the value of the current center position.
     *
     * @return The current rotation value.
     */
    public float getRotation();

    /**
     * Set the rotation of the texture to a new value. Rotations are in
     * radians.
     *
     * @param angle The new value to use
     */
    public void setRotation(float angle);

    /**
     * Get the value of the current scale applied to the texture.
     *
     * @param scale The array to copy the scale into
     */
    public void getScale(float[] scale);

    /**
     * Set the scale amount to a new value.
     *
     * @param scale The new value to use
     */
    public void setScale(float[] scale);

    /**
     * Get the value of the current translation.
     *
     * @param trans The array to copy the position into
     */
    public void getTranslation(float[] trans);

    /**
     * Set the translation value of the texture to a new position.
     *
     * @param trans The new value to use
     */
    public void setTranslation(float[] trans);
}
