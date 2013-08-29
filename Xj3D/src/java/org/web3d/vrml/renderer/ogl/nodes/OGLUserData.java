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

// External imports
import org.j3d.util.UserSupplementData;

// Local imports
import org.web3d.vrml.nodes.VRMLPointingDeviceSensorNodeType;
import org.web3d.vrml.nodes.VRMLLinkNodeType;

/**
 * Extended version of the supplemental node data class with information that
 * is specific to Xj3D.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class OGLUserData extends UserSupplementData {

    /**
     * The list of sensors held by this grouping node. If no sensors are
     * registered, this will be null.
     */
    public VRMLPointingDeviceSensorNodeType[] sensors;

    /**
     * The link from this node. A grouping node only ever has one link, so this
     * represents that information. If none is set, the value is null.
     */
    public VRMLLinkNodeType linkReference;

    /**
     * The visibility listener that is using this node for rendering. Set by
     * the implementing VRML node, in the picked object at the end. If none is
     * set, the value is null.
     */
    public OGLVisibilityListener visibilityListener;

    /**
     * The visibility listener that is using this node for rendering. Set by
     * the implementing VRML node, in the picked object at the end. If none is
     * set, the value is null.
     */
    public OGLAreaListener areaListener;

    /**
     * Flag to say that this is a node that will cause a transformation of the
     * scene graph rather than just an agregation. Basically only Transform
     * nodes should set this to true. It is an optimisation flag so that we
     * know whether to perform coordinate transformations on the picked points
     * when dealing with point device sensors because the normal node
     * implemenation will only expose the BranchGroup/SharedGroup surrounding
     * the VRML node's internal representation to the pick reporting. Therefore
     * the picking code working on the internals of the J3D scene graph will
     * have no idea about transformations, so this is a performance hint
     * allowing faster picking. Defaults to false;
     */
    public boolean isTransform;

    /**
     * A back pointer to the OGL node for those containing TransformGroups.
     */
    public OGLTransformNodeType owner;

    /**
     * Construct a new instance of the transform node and set the default
     * values.
     */
    public OGLUserData() {
        isTransform = false;
    }
}
