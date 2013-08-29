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
 * Specifies a 3D texture for associated geometry.
 * <p>
 *
 * 3D (or Volume) Textures work with an extra dimension to normal. This
 * interface allows access to all the extra information that is associated
 * with that extra dimension.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface VRMLTexture3DNodeType extends VRMLTextureNodeType {

    /**
     * Get the depth of the texture. This is the number of 2D slices that are
     * provided (by the user) and should always be a multiple of 2.
     *
     * @return A positive multiple of 2 or zero if none defined.
     */
    public int getDepth();

    /**
     * Get the value of field repeatS. The field is not writable.
     * Default value is <code>true</code>.
     *
     * @return The current value of repeatS
     */
    public boolean getRepeatS();

    /**
     * Get the value of field repeatT. The field is not writable.
     * Default value is <code>true</code>.
     *
     * @return The current value of repeatT
     */
    public boolean getRepeatT();

    /**
     * Get the value of field repeatR. The field is not writable.
     * Default value is <code>true</code>.
     *
     * @return The current value of repeatR
     */
    public boolean getRepeatR();
}
