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

package org.web3d.vrml.renderer.ogl.nodes.geospatial;

// External imports
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoPositionInterpolator;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * OpenGL implementation of an GeoPositionInterpolator
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class OGLGeoPositionInterpolator extends BaseGeoPositionInterpolator
    implements OGLVRMLNode {

    /**
     * Default constructor
     */
    public OGLGeoPositionInterpolator() {
        super();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLGeoPositionInterpolator(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }
}
