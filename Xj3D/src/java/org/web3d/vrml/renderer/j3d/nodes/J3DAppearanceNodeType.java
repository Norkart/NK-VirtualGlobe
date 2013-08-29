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
import javax.media.j3d.Appearance;
import javax.media.j3d.Texture2D;

// Application specific imports
import org.web3d.vrml.nodes.VRMLAppearanceNodeType;

/**
 * A class that determines the appearance of an object
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */
public interface J3DAppearanceNodeType
    extends VRMLAppearanceNodeType, J3DVRMLNode {

    /**
     * Returns a J3D Appearance node representation used by this object
     *
     * @return The appearance to use in the parent Shape3D
     */
    public Appearance getAppearance();

    /**
     * Set or clear the current alpha mask texture. Used for setting an alpha
     * mask that comes from another node instance, such as Text. A value of
     * null will clear the current alpha mask.
     *
     * @param texture The alpha texture to use or null
     */
    public void setAlphaTexture(Texture2D texture);

    /**
     * Set the texture coordinate generation mode for a texture set.  If
     * its not set then texture coordinates will be used.  A value of
     * null will clear the setting.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public void setTexCoordGenMode(int setNum, String mode);

    /**
     * Add a listener for appearance changed events. If the listener is null or
     * already registered it will silently ignore the requests.
     *
     * @param l The listener to add
     */
    public void addAppearanceListener(J3DAppearanceListener l);

    /**
     * Remove a listener for appearance changed events. If the listener is null
     * or has not been registered it will silently ignore the requests.
     *
     * @param l The listener to remove
     */
    public void removeAppearanceListener(J3DAppearanceListener l);
}
