/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes;

// Standard imports

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
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface OGLTextNodeType
    extends OGLGeometryNodeType, VRMLTextNodeType {

}
