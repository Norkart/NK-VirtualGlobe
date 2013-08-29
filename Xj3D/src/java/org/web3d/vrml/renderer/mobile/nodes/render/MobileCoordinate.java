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


package org.web3d.vrml.renderer.mobile.nodes.render;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.render.BaseCoordinate;

import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;

/**
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class MobileCoordinate extends BaseCoordinate implements MobileVRMLNode {

    /**
     * Default constructor for a MobileCoordinate
     */
    public MobileCoordinate() {
        super();
    }

    /**
     * Copy constructor to build a copy of this node from the given node.
     *
     * @param node The source node to copy values from
     */
    public MobileCoordinate(VRMLNodeType node) {
        super(node);
    }

    /**
     * Get the Mobile scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The null representation.
     */
     public SceneGraphObject getSceneGraphObject() {
         return null;
     }
}
