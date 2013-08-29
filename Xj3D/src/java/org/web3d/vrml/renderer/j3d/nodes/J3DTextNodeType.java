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
import javax.media.j3d.Texture2D;

// Application specific imports
import org.web3d.vrml.nodes.VRMLTextNodeType;

/**
 * An abstract representation geometry that renders text.
 * <p>
 *
 * Text based geometry may also use a texture to render it. It is possible
 * to use 3D text objects, but it is not required. If the implementation uses
 * textures to render the text then they can be returned from the appropriate
 * getter method. It is assumed that the underlying geometry is correctly set
 * up.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface J3DTextNodeType
    extends J3DGeometryNodeType, VRMLTextNodeType {

    /**
     * Fetch the texture instance that this node is rendering to. If the
     * implementation uses a texture to render text, return it here. If
     * the implementation doesn't use textures, return null.
     *
     * @return The texture used or null
     */
    public Texture2D getTextTexture();
}
