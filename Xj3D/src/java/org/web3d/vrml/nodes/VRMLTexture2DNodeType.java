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

// External imports
// None

// Local imports
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.image.NIOBufferImage;

/**
 * Specifies a 2D texture for associated geometry.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public interface VRMLTexture2DNodeType extends VRMLTextureNodeType {

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
     * Get the image representation of this texture.
     *
     * @return The image.
     */
    public NIOBufferImage getImage();

    /**
     * Get node content for the textureProperties field. This field is only
     * available for X3D 3.2 or later.
     *
     * @return The current field value
     * @throws InvalidFieldException This field was request in a field with
     *    spec version < 3.2
     */
    public VRMLNodeType getTextureProperties()
        throws InvalidFieldException;

    /**
     * Set node content as replacement for the textureProperties field. This
     * field is only available for X3D 3.2 or later.
     *
     * @param props The new value for geometry.  Null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     * @throws InvalidFieldException This field was request in a field with
     *    spec version < 3.2
     */
    public void setTextureProperties(VRMLNodeType props)
        throws InvalidFieldValueException, InvalidFieldException;
}
