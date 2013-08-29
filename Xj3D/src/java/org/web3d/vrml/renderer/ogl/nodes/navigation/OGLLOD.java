/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
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
import java.util.LinkedList;
import java.util.HashMap;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

import org.j3d.geom.GeometryData;
import org.j3d.renderer.aviatrix3d.nodes.LODGroup;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLChildNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLPointingDeviceSensorNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.renderer.common.nodes.navigation.BaseLOD;
import org.web3d.vrml.renderer.ogl.nodes.OGLGlobalStatus;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.ogl.nodes.OGLVisibilityListener;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * OpenGL-renderer implementation of a LOD node.
 * <p>
 *
 * Note that we always change right on the suggested LOD ranges, so the
 * forceTransitions field introduced in 3.1 is ignored for our implementation.
 *
 * @author Justin Couch
 * @version $Revision: 1.13 $
 */
public class OGLLOD extends BaseLOD
    implements OGLVRMLNode,
               NodeUpdateListener {

    /** Message when an externproto does not fit the ChildNode requirement */
    private static final String BAD_PROTO_MSG =
        "The resolved proto instance is not a X3DChildNode type. Grouping " +
        "nodes may only use ChildNode types for the children field.";

    /** The group returned to aviatrix */
    private Group oglImplGroup;

    /** The group used to select the closest to the viewer */
    private LODGroup implLod;

    /** Mapping of the VRMLNodeType to the OGL Group instance */
    private HashMap oglChildMap;

    /** List of children to add next update node */
    private LinkedList addedChildren;

    /** List of children to remove next update node */
    private LinkedList removedChildren;

    /** A sensor node if we have one set. Only valid during construction */
    private ArrayList sensorList;

    /** Geometry data associated with the basic box */
    private GeometryData geomData;

    /** Flag indicating the selected child has changed */
    private boolean activeChildChanged;

    /** The index of the currently active object */
    private int activeObject;

    /** Scratch var for transfering center(translation) values */
    private Vector3f translation;

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public OGLLOD() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLLOD(VRMLNodeType node) {
        super(node);
        init();
    }

    //-------------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //-------------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return oglImplGroup;
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

        implLod = new LODGroup();

        if(isStatic && shareCount == 0)
            oglImplGroup = new Group();
        else
            oglImplGroup = new SharedGroup();

        oglImplGroup.addChild(implLod);

        // Check what sensors we have available and register those with the
        // user data information.
        if(!isStatic && (sensorList.size() != 0)) {
            OGLUserData user_data = new OGLUserData();
            user_data.collidable = false;
            user_data.isTerrain = false;
            implLod.setUserData(user_data);

            user_data.sensors =
                new VRMLPointingDeviceSensorNodeType[sensorList.size()];

            sensorList.toArray(user_data.sensors);
        }

        sensorList = null;

        for(int i = 0; i < childCount; i++) {
            OGLVRMLNode node = (OGLVRMLNode)vfChildren.get(i);

            Node ogl_node = (Node)node.getSceneGraphObject();
            if(ogl_node != null)
                implLod.addChild(ogl_node);

            oglChildMap.put(node, ogl_node);
        }

        activeObject = (rangeLen > childCount) ? childCount: rangeLen;

        if(!isStatic) {
            removedChildren = new LinkedList();
            addedChildren = new LinkedList();
        }

        if(vfBboxSize[0] != -1 && vfBboxSize[1] != -1 && vfBboxSize[2] != -1) {
            float[] min = new float[3];
            min[0] = vfBboxCenter[0] - vfBboxSize[0] / 2;
            min[1] = vfBboxCenter[1] - vfBboxSize[1] / 2;
            min[2] = vfBboxCenter[2] - vfBboxSize[2] / 2;

            float[] max = new float[3];
            max[0] = vfBboxCenter[0] + vfBboxSize[0] / 2;
            max[1] = vfBboxCenter[1] + vfBboxSize[1] / 2;
            max[2] = vfBboxCenter[2] + vfBboxSize[2] / 2;

            BoundingBox bbox = new BoundingBox(min, max);
            oglImplGroup.setBounds(bbox);
        }

        updateNodeDataChanges(implLod);
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
        if(oglImplGroup.isLive()) {
            oglImplGroup.boundsChanged(this);
        } else {
            updateNodeBoundsChanges(oglImplGroup);
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

        if(src == implLod) {
            int size = (addedChildren == null) ? 0 : addedChildren.size();
            Node kid;

            for(int i = 0; i < size; i++) {
                kid = (Node)addedChildren.get(i);
                implLod.addChild(kid);
            }

            size = (removedChildren == null) ? 0 : removedChildren.size();

            for(int i = 0; i < size; i++) {
                kid = (Node)removedChildren.get(i);
                implLod.removeChild(kid);
            }
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        // update the range
        if(src != implLod)
            return;

        int num_kids = (rangeLen < childCount) ? rangeLen : childCount;

        for(int i = 0; i < num_kids; i++)
            implLod.setRange(i, vfRange[i]);

        implLod.setCenter(vfCenter);
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
            if(oglImplGroup.isLive()) {
                if (addedChildren == null)
                    addedChildren = new LinkedList();

                addedChildren.add(ogl_node);
                stateManager.addEndOfThisFrameListener(this);
            } else
                implLod.addChild(ogl_node);
        }
    }

    //----------------------------------------------------------
    // Methods defined by BaseLOD
    //----------------------------------------------------------

    /**
     * Set the center component of the of transform. Setting a value
     * of null is an error
     *
     * @param center The new center component
     * @throws InvalidFieldValueException The center was null
     */
    protected void setCenter(float[] center)
        throws InvalidFieldValueException {

        super.setCenter(center);

        if(!inSetup)
            implLod.dataChanged(this);
    }

    /**
     * Set the range to the new series of values. A null value is the same as
     * removing the range altogether.
     *
     * @param range the new range values to use
     * @param numValid The number of valid values to copy from the array
     */
    protected void setRange(float[] range, int numValid)
        throws InvalidFieldValueException {

        super.setRange(range, numValid);

        if(!inSetup)
            implLod.dataChanged(this);
    }

    //----------------------------------------------------------
    // Methods defined by BaseGroupingNode
    //----------------------------------------------------------

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
            if (implLod.isLive())
                implLod.boundsChanged(this);
            else
                updateNodeBoundsChanges(implLod);
        }

        if (sensorList != null)
            sensorList.clear();

        OGLUserData data = (OGLUserData)implLod.getUserData();

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
            addedChildren.add(ogl_node);

            oglChildMap.put(node, ogl_node);
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
            else {
                OGLUserData data = (OGLUserData)implLod.getUserData();

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

        if(!inSetup) {
            if (implLod.isLive())
                implLod.boundsChanged(this);
            else
                updateNodeBoundsChanges(implLod);
        }
    }

    /**
     * Remove the given node from this grouping node. If the node is not a
     * child of this node, the request is silently ignored.
     *
     * @param node The node to remove
     */
    protected void removeChildNode(VRMLNodeType node)
        throws InvalidFieldValueException {
        if(!oglChildMap.containsKey(node))
            return;

        if(!inSetup) {
            Node ogl_node = (Node)oglChildMap.get(node);
            removedChildren.add(ogl_node);
            oglChildMap.remove(node);
            if(implLod.isLive())
                implLod.boundsChanged(this);
            else
                updateNodeBoundsChanges(implLod);
        }

        super.removeChildNode(node);
    }

    /**
     * Common Initialization code.
     */
    private void init() {
        activeChildChanged = false;

        sensorList = new ArrayList();
        translation = new Vector3f();
    }
}
