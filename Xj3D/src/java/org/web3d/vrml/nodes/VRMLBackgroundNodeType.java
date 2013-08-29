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

/**
 * Representation of a node that could form a background to the scene.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public interface VRMLBackgroundNodeType
    extends VRMLBindableNodeType {

    /**
     * Get the number of valid sky color values that are currently defined.
     *
     * @return The number of values
     */
    public int getNumSkyColors();

    /**
     * Get the number of valid ground color values that are currently defined.
     *
     * @return The number of values
     */
    public int getNumGroundColors();

    /**
     * Fetch the color and angles for the sky values.
     *
     * @param color The array to return the color values in
     * @param angle The array to return the angle values in
     */
    public void getSkyValues(float[] color, float[] angle);

    /**
     * Fetch the color and angles for the ground values.
     *
     * @param color The array to return the color values in
     * @param angle The array to return the angle values in
     */
    public void getGroundValues(float[] color, float[] angle);

    /**
     * Get the transparency of the background.
     */
    public float getTransparency();

    /**
     * Set the transparency of the background.
     *
     * @param val The transparency value
     */
    public void setTransparency(float val);
}
