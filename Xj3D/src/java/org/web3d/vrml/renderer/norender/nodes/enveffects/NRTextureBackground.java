/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.norender.nodes.enveffects;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.enveffects.BaseTextureBackground;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * A node that can represents a VRML Background node.
 * <p>
 *
 * A background node in VRML is quite different to the Java3D background. It
 * is represented by a 6 sided box inside a sphere at a nominal infinite
 * distance. Each side of the box may have a different image on it.
 * <p>
 *
 * @author Alan Hidson
 * @version $Revision: 1.2 $
 */
public class NRTextureBackground extends BaseTextureBackground
    implements NRVRMLNode {

    /**
     * Create a new, default instance of this class.
     */
    public NRTextureBackground() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node.
     * <P>
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the right type.
     */
    public NRTextureBackground(VRMLNodeType node) {
        super(node);
    }

    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This should
     * be one of the forms that the prefered class type call generates.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArguementException The content object is not supported
     */
    public void setContent(int index, String mimetype, Object content)
        throws IllegalArgumentException {
    }
}
