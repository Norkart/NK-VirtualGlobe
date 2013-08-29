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

package org.web3d.vrml.renderer.ogl.nodes.navigation;

// External imports
import org.j3d.aviatrix3d.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.navigation.BaseCollision;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;


/**
 * OGL implementation of a collision node.
 * <p>
 *
 * NOTE:<br>
 * The implementation does not support the use of proxy geometry yet.
 *
 * @author Alan Hudson
 * @version $Revision: 1.14 $
 */
public class OGLCollision extends BaseCollision
    implements OGLVRMLNode, NodeUpdateListener {

    /** Message when an externproto does not fit the ChildNode requirement */
    private static final String BAD_PROTO_MSG =
        "The resolved proto instance is not a X3DChildNode type. Collision " +
        "nodes may only use ChildNode types for the children and proxy fields.";

    /** The Java3D group node that we are using to place children in. */
    private Group implGroup;

    /** Mapping of the VRMLNodeType to the OGL BranchGroup instance */
    private HashMap oglChildMap;

    /** A sensor node if we have one set. Only valid during construction */
    private ArrayList sensorList;

    /** List of children to add next update node */
    private LinkedList addedChildren;

    /** List of children to remove next update node */
    private LinkedList removedChildren;

    /** The bounds acting as the proxy geometry */
    private BoundingGeometry proxyBounds;

    /** Flag for the proxy bounds changed, from an externproto loading */
    private boolean proxyChanged;

    /**
     * The number of children nodes we added to OGL, not the total. We don't
     * Add nodes to OGL that have no scenegraph object (such as
     * interpolators and timesensors). This count is so that we don't have
     * to enable the ALLOW_CHILDREN_READ on the group (an optimisation step)
     */
    private int oglChildCount;

    /**
     * Construct a new default instance.
     */
    public OGLCollision() {
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
    public OGLCollision(VRMLNodeType node) {
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
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {

        int size = (addedChildren == null) ? 0 : addedChildren.size();
        Node kid;

        for(int i = 0; i < size; i++) {
            kid = (Node)addedChildren.get(i);
            implGroup.addChild(kid);
        }

        size = (removedChildren == null) ? 0 : removedChildren.size();

        for(int i = 0; i < size; i++) {
            kid = (Node)removedChildren.get(i);
            implGroup.removeChild(kid);
        }

        if(proxyChanged)
            implGroup.setBounds(proxyBounds);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        int mask = Group.GENERAL_OBJECT;

        if(vfCollide)
            mask |= Group.COLLIDABLE_OBJECT;

        implGroup.setPickMask(mask);
    }

    //----------------------------------------------------------
    // Methods defined by BaseCollision
    //----------------------------------------------------------

    /**
     * Set the value of the collide field. Used to change the collision
     * status.
     *
     * @param state true if the collision is being set
     */
    protected void setCollide(boolean state) {

        boolean old_state = vfCollide;

        super.setCollide(state);

        if (inSetup)
            return;

        if(old_state != state)
            if (implGroup.isLive())
                implGroup.dataChanged(this);
            else
                updateNodeDataChanges(implGroup);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. This
     * should never be called before setupFinished() is called.
     *
     * @return The OGL representation of this grouping node
     */
    public SceneGraphObject getSceneGraphObject() {
        return implGroup;
    }

    //----------------------------------------------------------
    // Methods defined by OGLGroupingNode
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

        int num_kids = vfChildren.size();
        OGLVRMLNode kid;

        for(int i = 0; i < num_kids; i++) {
            kid = (OGLVRMLNode)vfChildren.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
            Node ogl_node = (Node)kid.getSceneGraphObject();

            if(ogl_node == null)
                continue;

            oglChildCount++;
            implGroup.addChild(ogl_node);
        }

        if(!isStatic) {
            removedChildren = new LinkedList();
            addedChildren = new LinkedList();
        }

        createProxyBounds();

        int mask = Group.GENERAL_OBJECT | Group.PROXIMITY_OBJECT | Group.VISIBLE_OBJECT;

        if(vfCollide)
            mask |= Group.COLLIDABLE_OBJECT;

        implGroup.setPickMask(mask);

        if(proxyBounds == null && vfBboxSize[0] != -1 && vfBboxSize[1] != -1 &&
           vfBboxSize[2] != -1) {
            float[] min = new float[3];
            min[0] = vfBboxCenter[0] - vfBboxSize[0] / 2;
            min[1] = vfBboxCenter[1] - vfBboxSize[1] / 2;
            min[2] = vfBboxCenter[2] - vfBboxSize[2] / 2;

            float[] max = new float[3];
            max[0] = vfBboxCenter[0] + vfBboxSize[0] / 2;
            max[1] = vfBboxCenter[1] + vfBboxSize[1] / 2;
            max[2] = vfBboxCenter[2] + vfBboxSize[2] / 2;

            BoundingBox bbox = new BoundingBox(min, max);
            implGroup.setBounds(bbox);
        }
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
        // the base class sets inSetup finish true before J3D structures are complete

        if(inSetup)
            return;

        OGLVRMLNode kid = (OGLVRMLNode)node;

        // Make sure the child is finished first.
        kid.setupFinished();
        Node ogl_node = (Node)kid.getSceneGraphObject();

        if(index == FIELD_PROXY) {
            createProxyBounds();
        } else {
            // rest of the children.
            oglChildMap.put(node, ogl_node);

            if(ogl_node != null) {
                if(implGroup.isLive()) {
                    if (addedChildren == null)
                        addedChildren = new LinkedList();

                    addedChildren.add(ogl_node);
                    stateManager.addEndOfThisFrameListener(this);
                } else
                    implGroup.addChild(ogl_node);
            }
        }
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Clear the child node list of all children - both VRML and Java3D.
     */
    protected void clearChildren() {

        oglChildCount = 0;

        super.clearChildren();

        if (sensorList != null)
            sensorList.clear();

        OGLUserData data = (OGLUserData)implGroup.getUserData();

        if(data != null) {
            data.sensors = null;
        }

        if(!inSetup)
            return;

        if (implGroup.isLive())
            implGroup.boundsChanged(this);
        else
            updateNodeBoundsChanges(implGroup);
    }

    /**
     * Add a single child node to the list of available children. This auto
     * matically deals with DEF/USE and adds links and branchgroups where
     * appropriate. When nodes are null, we do not add them to the OGL
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
            addedChildren.add(ogl_node);

            oglChildMap.put(node, ogl_node);

            if(implGroup.isLive()) {
                addedChildren.add(ogl_node);
                implGroup.boundsChanged(this);
            } else
                implGroup.addChild(ogl_node);
        }

        // Finally check for sensors that we need to deal with.
        VRMLPointingDeviceSensorNodeType sensor = null;

        if(node instanceof VRMLPointingDeviceSensorNodeType)
            sensor = (VRMLPointingDeviceSensorNodeType)node;
        else if(node instanceof VRMLProtoInstance) {
            Object impl = ((VRMLProtoInstance)node).getImplementationNode();

            if(impl instanceof VRMLPointingDeviceSensorNodeType)
                sensor = (VRMLPointingDeviceSensorNodeType)impl;
        }

        if(sensor != null) {
            // So we have a valid sensor. Let's now add it to the
            // system. We only add the sensor itself, even if wrapped in a
            // proto. This is so that the processing of sensors doesn't need
            // to stuff around with the details of protos. As far as the proto
            // node is concerned it just wants the full events, not the
            // restricted view the outside of the proto would give.
            if(inSetup)
                sensorList.add(sensor);
            else
            {
                OGLUserData data = (OGLUserData)implGroup.getUserData();

                if(data == null) {
                    data = new OGLUserData();
                    implGroup.setUserData(data);
                }

                if(data.sensors == null) {
                    data.sensors = new VRMLPointingDeviceSensorNodeType[1];
                    data.sensors[0] = sensor;
                } else {
                    int size = data.sensors.length;
                    VRMLPointingDeviceSensorNodeType[] tmp =
                        new VRMLPointingDeviceSensorNodeType[size + 1];

                    System.arraycopy(data.sensors, 0, tmp, 0, size);
                    tmp[size] = sensor;
                    data.sensors = tmp;
                }
            }
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
            } else
                implGroup.removeChild(ogl_node);
        }

        // Check to see if it is a sensor node and in therefore needs to be
        // removed from the current list.

        VRMLPointingDeviceSensorNodeType sensor = null;

        if(node instanceof VRMLPointingDeviceSensorNodeType)
            sensor = (VRMLPointingDeviceSensorNodeType)node;
        else if(node instanceof VRMLProtoInstance) {
            Object impl = ((VRMLProtoInstance)node).getImplementationNode();

            if(impl instanceof VRMLPointingDeviceSensorNodeType)
                sensor = (VRMLPointingDeviceSensorNodeType)impl;
        }

        if(sensor != null) {
            OGLUserData data = (OGLUserData)implGroup.getUserData();

            int size = data.sensors.length;

            if(size == 1) {
                data.sensors = null;
            } else {
                int i;

                for(i = 0; i < size; i++) {
                    if(data.sensors[i] == sensor)
                        break;
                }

                for( ; i < size - 1; i++)
                    data.sensors[i] = data.sensors[i + 1];

                // now resize the array to suit
                VRMLPointingDeviceSensorNodeType[] tmp =
                    new VRMLPointingDeviceSensorNodeType[size - 1];

                System.arraycopy(data.sensors, 0, tmp, 0, size - 1);
                data.sensors = tmp;
            }
        }

        super.removeChildNode(node);
    }

    /**
     * Set node content as replacement for <code>proxy</code>.
     *
     * @param app The new proxy.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setProxy(VRMLNodeType app)
        throws InvalidFieldValueException {

        super.setProxy(app);

        createProxyBounds();
    }

    /**
     * Setup the proxy geometry for the bounds.
     */
    private void createProxyBounds() {
        if(vfProxy == null)
            return;

        OGLVRMLNode ogl_node = (OGLVRMLNode)vfProxy;
        SceneGraphObject obj = ogl_node.getSceneGraphObject();

        if(obj == null || !(obj instanceof Node))
            return;

        proxyBounds = new BoundingGeometry((Node)obj);

        if(implGroup.isLive()) {
            proxyChanged = true;
            stateManager.addEndOfThisFrameListener(this);
        } else
            implGroup.setBounds(proxyBounds);
    }

    /**
     * Common internal initialisation method.
     */
    private void init() {
        implGroup = new Group();

        oglChildCount = 0;
        oglChildMap = new HashMap();
        sensorList = new ArrayList();

        proxyChanged = false;
    }
}
