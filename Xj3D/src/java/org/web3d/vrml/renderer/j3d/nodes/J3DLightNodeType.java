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
import javax.media.j3d.Light;

// Application specific imports
import org.web3d.vrml.nodes.VRMLLightNodeType;

/**
 * An abstract representation of any form of light node.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface J3DLightNodeType extends VRMLLightNodeType, J3DVRMLNode {
    /**
     * Get the lights making up this LightNode
     * Java3D lights are different then VRML lights, ie they
     * seperate out the ambient component.  So our lights may
     * be composed of several J3D lights
     *
     * @return A list of J3D lights making up this Light
     */
    public Light[] getLights();
}
