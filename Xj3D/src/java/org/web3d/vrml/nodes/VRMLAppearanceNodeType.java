/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
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
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * Node which represents appearance attributes of an object.
 * <p>
 * The representation is a fraction more relaxed than the standard VRML
 * appearance node. Here we allow arbitrary material node types and
 * the textures are any type (which may include procedural textures,
 * environment maps and 3D texturing).
 *
 * @author Alan Hudson
 * @version $Revision: 1.15 $
 */
public interface VRMLAppearanceNodeType extends VRMLNodeType
{
    /**
     * Set the material that should be used for this appearance. Setting a
     * value of null will clear the current material.
     *
     * @param newMaterial The new material instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setMaterial(VRMLNodeType newMaterial)
        throws InvalidFieldValueException;

    /**
     * Get the current material used by the appearance. If none has been set
     * this will return null.
     *
     * @return The currently set Material
     */
    public VRMLNodeType getMaterial();

    /**
     * Set the texture that should be used for this appearance. Setting a
     * value of null will clear the current texture.
     *
     * @param newTexture The new texture instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setTexture(VRMLNodeType newTexture)
        throws InvalidFieldValueException;

    /**
     * Get the current texture used by the appearance. If none has been sety
     * this will return null.
     *
     * @return The currently set Texture
     */
    public VRMLNodeType getTexture();

    /**
     * Set the texture transform that should be used for this appearance.
     * Setting a value of null will clear the current texture transform.
     *
     * @param newTransform The new texture transform instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setTextureTransform(VRMLNodeType newTransform)
        throws InvalidFieldValueException;

    /**
     * Get the current texture transform used by the appearance. If none has
     * been sety this will return null.
     *
     * @return The currently set TextureTransform
     */
    public VRMLNodeType getTextureTransform();

    /**
     * Set the line properties that should be used for this appearance. Setting a
     * value of null will clear the current property setting.
     *
     * @param prop The new property instance to be used or null
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setLineProperties(VRMLNodeType prop)
        throws InvalidFieldValueException;

    /**
     * Get the current line properties used by the appearance. If none has been set
     * this will return null.
     *
     * @return The currently set LineProperties
     */
    public VRMLNodeType getLineProperties();

    /**
     * Set the fill properties that should be used for this appearance. Setting a
     * value of null will clear the current property setting.
     *
     * @param prop The new property instance to be used or null
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setFillProperties(VRMLNodeType prop)
        throws InvalidFieldValueException;

    /**
     * Get the current fill properties used by the appearance. If none has been set
     * this will return null.
     *
     * @return The currently set FillProperties node
     */
    public VRMLNodeType getFillProperties();

    /**
     * Specify whether an object is solid. The default is true. This will
     * determine if we do backface culling and flip backface normals.
     * Can only be set during setup
     *
     * @param solid Whether the object is solid
     */
    public void setSolid(boolean solid);

    /**
     * Set whether lighting will be used for this appearance.  In general
     * you should let the material node decide this.  Needed to handle
     * IndexedLineSets or other geometry that specifically declares lighting
     * be turned off.  This method will notify the related Material node for
     * this Appearance.
     *
     * @param enable Whether lighting is enabled
     */
    public void setLightingEnabled(boolean enable);

    /**
     * Set whether the geometry has local colors to override the diffuse color.
     * If the local color contains alpha values we want to ignore the
     * material's transparency values.
     *
     * @param enable Whether local color is enabled
     * @param hasAlpha true with the local color also contains alpha valuess
     */
    public void setLocalColor(boolean enable, boolean hasAlpha);

    /**
     * Specify whether the geometry's triangles are in counter clockwise order
     * (the default) or clockwise. The default is true. This will
     * determine if we do backface culling and flip backface normals.
     * Can only be set during setup
     *
     * @param ccw True for counter-clockwise ordering
     */
    public void setCCW(boolean ccw);

    /**
     * Add a listener instance for the material color change notifications.
     * Adding the same instance more than once is ignored. Adding null values
     * are ignored.
     *
     * @param l The new instance to add
     */
    public void addMaterialColorListener(MaterialColorListener l);

    /**
     * Remove a listener instance from this node. If the listener is not
     * currently registered, the request is silently ignored.
     *
     * @param l The new instance to remove
     */
    public void removeMaterialColorListener(MaterialColorListener l);
}
