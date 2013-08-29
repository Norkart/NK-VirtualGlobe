/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.cadgeometry;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

import org.j3d.aviatrix3d.Group;
import org.j3d.aviatrix3d.SharedGroup;
import org.j3d.aviatrix3d.Node;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.NodeUpdateListener;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.common.nodes.cadgeometry.BaseCADFace;


/**
 * OpenGL implementation of a group node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class OGLCADFace extends BaseCADFace
    implements OGLVRMLNode, NodeUpdateListener {

    /** Message when an externproto does not fit the ChildNode requirement */
    private static final String BAD_PROTO_MSG =
        "The resolved proto instance is not a X3DChildNode type. Grouping " +
        "nodes may only use ChildNode types for the children field.";

    /** The renderable scenegraph node */
    private Group implGroup;

    /** The new shape */
    private Node newShape;

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public OGLCADFace() {
        init();

    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLCADFace(VRMLNodeType node) {
        super(node);

        init();
    }

    //-------------------------------------------------------------
    // Methods defined by FrameStateListener
    //-------------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame.
     */
    public void allEventsComplete() {
        if(implGroup.isLive()) {
            implGroup.boundsChanged(this);
        } else {
            updateNodeBoundsChanges(implGroup);
        }

        super.allEventsComplete();
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener interface.
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
        if (implGroup.numChildren() > 0)
            implGroup.setChild(newShape,1);
        else
            implGroup.addChild(newShape);
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
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        OGLVRMLNode node = null;

        if(pShape != null)
            node = (OGLVRMLNode)pShape;
        else if(vfShape != null)
            node = (OGLVRMLNode)vfShape;

        if(node != null) {
            Node ogl_node = (Node)node.getSceneGraphObject();

            if(ogl_node != null)
                implGroup.addChild(ogl_node);
        }
    }

    /**
     * Handle notification that an ExternProto has resolved.
     *
     * @param index The field index that got loaded
     * @param node The owner of the node
     */
    public synchronized void notifyExternProtoLoaded(int index, VRMLNodeType node) {

        if(!(node instanceof VRMLShapeNodeType) && !(node instanceof VRMLProtoInstance))
            throw new InvalidFieldValueException(BAD_PROTO_MSG);

        // TODO: This does not totally guard against notifications during setupFinished as
        // the base class sets inSetup finish true before J3D structures are complete

        if(inSetup)
            return;

        OGLVRMLNode kid = (OGLVRMLNode)node;

        // Make sure the child is finished first.
        kid.setupFinished();
        Node ogl_node = (Node)kid.getSceneGraphObject();

        if(ogl_node != null) {
            if(implGroup.isLive()) {
                newShape = ogl_node;
                stateManager.addEndOfThisFrameListener(this);
            } else
                implGroup.addChild(ogl_node);
        }
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
        return implGroup;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Common Initialization code.
     */
    private void init() {
        implGroup = new SharedGroup();
    }
}
