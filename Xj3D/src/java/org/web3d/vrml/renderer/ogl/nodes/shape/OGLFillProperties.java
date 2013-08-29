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

package org.web3d.vrml.renderer.ogl.nodes.shape;

// Standard imports
import java.util.Map;

import javax.media.ogl.SceneGraphObject;
import javax.media.ogl.PolygonAttributes;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.shape.BaseFillProperties;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * Aviatrix3D renderer implementation of an FillProperties node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class OGLFillProperties extends BaseFillProperties
    implements OGLVRMLNode {

    /** Java3D line attribute handling */
    private PolygonAttributes oglAttribs;

    /**
     * Default constructor.
     */
    public OGLFillProperties() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a FillProperties node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLFillProperties(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods required by the OGLVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. Scripts
     * always return null.
     *
     * @return null
     */
    public SceneGraphObject getSceneGraphObject() {
        return oglAttribs;
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Common initialisation routines for the OpenGL code.
     */
    private void init() {
        oglAttribs = new PolygonAttributes();
    }
}
