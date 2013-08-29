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

package org.web3d.vrml.renderer.j3d.nodes.navigation;

// Standard imports
import java.util.Map;

import javax.media.j3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.common.nodes.navigation.BaseNavigationInfo;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;

/**
 * Java3D implementation of a NavigationInfo node.
 * <p>
 *
 * The NavigationInfo node does not occupy a space in the Java 3D
 * scenegraph. This is used as a VRML construct only. When VRML changes the
 * values here, we pass them back courtesy of the listeners to the children
 * nodes.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class J3DNavigationInfo extends BaseNavigationInfo
    implements J3DVRMLNode {

    /**
     * Construct a default node with all of the values set to the given types.
     */
    public J3DNavigationInfo() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public J3DNavigationInfo(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNodeTypeType interface.
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
    }

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }
}
