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

package org.web3d.vrml.renderer.ogl.nodes;

// Standard imports
import org.j3d.aviatrix3d.Light;

// Application specific imports
import org.web3d.vrml.nodes.VRMLLightNodeType;

/**
 * An abstract representation of any form of light node in the OpenGL
 * scene graph.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface OGLLightNodeType extends VRMLLightNodeType, OGLVRMLNode {

    /**
     * Get the light making up this LightNode.
     *
     * @return The OGL light instance
     */
    public Light getLight();
}
