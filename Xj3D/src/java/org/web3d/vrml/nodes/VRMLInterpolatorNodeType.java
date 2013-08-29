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

package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
// None

/**
 * Interpolator nodes are designed for linear keyframed animation.
 * <p>
 * Interpolators are driven by an input key ranging [0..1] and produce
 * corresponding piecewise-linear output functions.
 * <p>
 *
 * This interface represents the X3D abstract node type X3DInterpolatorNode,
 * which is defined as:
 *
 * <pre>
 *  X3DInterpolatorNode : X3DChildNode {
 *    SFFloat      [in]     set_fraction       (-inf,inf)
 *    MFFloat      [in,out] key           []   (-inf,inf)
 *    MF<type>     [in,out] keyValue      []
 *    SFNode       [in,out] metadata      NULL [X3DMetadataObject]
 *    [S|M]F<type> [out]    value_changed
 *  }
 * </pre>
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public interface VRMLInterpolatorNodeType extends VRMLChildNodeType {
    /**
     * Set a new value for the fraction field. This should cause an
     * output interpolation.
     *
     * @param fraction The new value for fraction
     */
    public void setFraction(float fraction);

    /**
     * Get the current value of the fraction field.
     *
     * @return The current value for fraction
     */
    public float getFraction();

    /**
     * Set a new value for the key field. A value of <code>null</code> will
     * delete all key values.
     *
     * @param keys The new key values
     * @param numValid The number of valid values to copy from the array
     */
    public void setKey(float[] keys, int numValid);

    /**
     * Get current value of the key field. If no keys exist a zero-length
     * float[] will be returned. Use getNumKey to find out how many keys
     * need to be set.
     *
     * @return The current key values
     */
    public float[] getKey();

    /**
     * Get the number of valid keys defined for this interpolator.
     *
     * @return a value >= 0
     */
    public int getNumKey();
}
