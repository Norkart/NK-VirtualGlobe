/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005
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
 * Specifies an origin to perform high precision math around.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface VRMLLocalOriginNodeType extends VRMLNodeType {
    /**
     * Get the pointer to the local converted form of the geo coords. This
     * doesn't change over time, so the caller can fetch once and hold the
     * reference.
     */
    public double[] getConvertedCoordRef();
}
