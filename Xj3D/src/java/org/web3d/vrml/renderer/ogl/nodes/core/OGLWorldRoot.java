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

package org.web3d.vrml.renderer.ogl.nodes.core;

// External imports
import java.util.LinkedList;
import java.util.HashMap;

import org.j3d.aviatrix3d.Group;
import org.j3d.aviatrix3d.Node;
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLChildNodeType;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.nodes.VRMLPointingDeviceSensorNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.renderer.common.nodes.core.BaseWorldRoot;
import org.web3d.vrml.renderer.ogl.nodes.OGLWorldRootNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;

/**
 * Implementation of the world root class.
 * <p>
 *
 * Extends the basic root node functionality with Java3D specific capabilities
 * - namely the ability to get the root node BranchGroup instance. This is the
 * object returned by the <code>getSceneGraphObject()</code> method.
 * <p>
 * The world root has 3 fields available to use:
 * <ul>
 * <li>children</li>
 * <li>bboxSize</li>
 * <li>bboxCenter</li>
 * </ul>
 *
 * <b>Note:</b> This may want to extend BranchGroup instead.
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class OGLWorldRoot extends BaseWorldRoot
    implements OGLWorldRootNodeType {

    /** Message when an externproto does not fit the ChildNode requirement */
    private static final String BAD_PROTO_MSG =
        "The resolved proto instance is not a X3DChildNode type. Grouping " +
        "nodes may only use ChildNode types for the children field.";

    /** Mapping of the VRMLNodeType to the OGL Group instance */
    private HashMap oglChildMap;

    /** The renderable scenegraph node */
    private Group implGroup;

    /** List of children to add next update node */
    private LinkedList addedChildren;

    /** List of children to remove next update node */
    private LinkedList removedChildren;

    /**
     * Construct an instance of this node.
     */
    public OGLWorldRoot() {
        implGroup = new Group();

        // These are linked lists to allow externs to modify while reading
        removedChildren = new LinkedList();
        addedChildren = new LinkedList();
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
        Node kid;
        int size = (removedChildren == null) ? 0 : removedChildren.size();

        for(int i = 0; i < size; i++) {
            kid = (Node)removedChildren.get(0);
            removedChildren.remove(0);
            implGroup.removeChild(kid);
        }

        size = (addedChildren == null) ? 0 : addedChildren.size();

        for(int i = 0; i < size; i++) {
            kid = (Node)addedChildren.get(0);
            addedChildren.remove(0);
            implGroup.addChild(kid);
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        // Don't need to do anything
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
    }

    //----------------------------------------------------------
    // Overrding base methods
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

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        oglChildMap = new HashMap();

        int size = vfChildren.size();

        for(int i = 0; i < size; i++) {
            OGLVRMLNode n = (OGLVRMLNode)vfChildren.get(i);

            n.setupFinished();

            Node ogl_node = (Node)n.getSceneGraphObject();

            if(ogl_node != null) {
                oglChildMap.put(n, ogl_node);

                implGroup.addChild(ogl_node);
            }
        }
    }

    /**
     * Clear the child node list of all children - both VRML and OpenGL.
     */
    protected void clearChildren() {
        int size = vfChildren.size();
        for(int i = 0; i < size; i++) {
            OGLVRMLNode node = (OGLVRMLNode)vfChildren.get(i);
            Node ogl_node = (Node)oglChildMap.get(node);
            removedChildren.add(ogl_node);
            oglChildMap.remove(node);
        }

        if(!inSetup) {
            if(implGroup.isLive()) {
                implGroup.boundsChanged(this);
            } else {
                removedChildren.clear();
                implGroup.removeAllChildren();
            }
        }

        OGLUserData data = (OGLUserData)implGroup.getUserData();

        if(data != null) {
            data.sensors = null;
        }

        super.clearChildren();
    }

    /**
     * Add a single child node to the list of available children. This auto
     * matically deals with DEF/USE and adds links and branchgroups where
     * appropriate. When nodes are null, we do not add them to the GL
     * representation, only to the vfChildren list.
     *
     * @param node The node to view
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addChildNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        super.addChildNode(node);

        OGLVRMLNode n = (OGLVRMLNode)node;

        if(!inSetup) {

            Node ogl_node = (Node)n.getSceneGraphObject();

            oglChildMap.put(node, ogl_node);

            if(implGroup.isLive()) {
                addedChildren.add(ogl_node);
                implGroup.boundsChanged(this);
            } else
                implGroup.addChild(ogl_node);
        }
    }

    /**
     * Remove the given node from this grouping node. If the node is not a
     * child of this node, the request is silently ignored.
     *
     * @param node The node to remove
     */
    protected void removeChildNode(VRMLNodeType node) {
        if(!oglChildMap.containsKey(node))
            return;

        if(!inSetup) {
            Node ogl_node = (Node)oglChildMap.get(node);
            oglChildMap.remove(node);

            if(implGroup.isLive()) {
                removedChildren.add(ogl_node);
                implGroup.boundsChanged(this);
            } else {
                implGroup.removeChild(ogl_node);
            }
        }

        super.removeChildNode(node);
    }

    /**
     * Handle notification that an ExternProto has resolved.
     *
     * @param index The field index that got loaded
     * @param node The owner of the node
     */
    public synchronized void notifyExternProtoLoaded(int index, VRMLNodeType node) {

        if(!(node instanceof VRMLChildNodeType) && !(node instanceof VRMLProtoInstance))
            throw new InvalidFieldValueException(BAD_PROTO_MSG);

        // TODO: This does not totally guard against notifications during setupFinished as
        // the base class sets inSetup finish true before OGL structures are complete

        if(inSetup)
            return;

        OGLVRMLNode kid = (OGLVRMLNode)node;

        // Make sure the child is finished first.
        kid.setupFinished();
        Node ogl_node = (Node)kid.getSceneGraphObject();

        oglChildMap.put(node, ogl_node);

        if(ogl_node != null) {
            if(implGroup.isLive()) {
                addedChildren.add(ogl_node);
                stateManager.addEndOfThisFrameListener(this);
            } else
                implGroup.addChild(ogl_node);
        }
    }
}
