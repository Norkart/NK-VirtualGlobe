/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
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
 * Base interface for all texture types for associated geometry.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public interface VRMLTextureNodeType extends VRMLAppearanceChildNodeType {
    /**
     * Get the texture type of this texture.  Valid entries are defined
     * in the vrml.lang.TextureConstants.
     */
    public int getTextureType();

    /**
     * Get a string for cacheing this object.  Null means do not cache this
     * texture.
     *
     * @param stage The stage number,  0 for all single stage textures.
     * @return A string to use in lookups.  Typically the url loaded.
     */
    public String getCacheString(int stage);

    /**
     * Add a listener for texture changes. If the listener is already
     * registered then this request is ignored.
     *
     * @param l The listener instance to be added
     */
    public void addTextureListener(VRMLTextureListener l);

    /**
     * Removes a listener for texture changes. If the listener is not already
     * registered, the request is ignored.
     *
     * @param l The listener to be removed
     */
    public void removeTextureListener(VRMLTextureListener l);
}
