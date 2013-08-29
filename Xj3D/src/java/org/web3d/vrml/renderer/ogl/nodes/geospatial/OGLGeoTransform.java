/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
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
import org.j3d.aviatrix3d.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;

import javax.vecmath.Matrix4f;

import org.j3d.aviatrix3d.picking.PickableObject;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.ogl.nodes.*;
import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoTransform;

/**
 * OpenGL implementation of a GeoTransform node.
 * <p>
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class OGLGeoTransform extends BaseGeoTransform
    implements OGLVRMLNode, OGLPickableTargetNodeType, NodeUpdateListener,
               OGLTransformNodeType {

    /** Message when an externproto does not fit the ChildNode requirement */
    private static final String BAD_PROTO_MSG =
        "The resolved proto instance is not a X3DChildNode type. Grouping " +
        "nodes may only use ChildNode types for the children field.";

    /** The group holding the children */
    private TransformGroup implGroup;

    /** The group returned to aviatrix */
    private Node oglImplGroup;

    /** Mapping of the VRMLNodeType to the OGL Group instance */
    private HashMap oglChildMap;

    /** List of children to add next update node */
    private LinkedList addedChildren;

    /** List of children to remove next update node */
    private LinkedList removedChildren;

    /** Flag to say the tx matrix has changed */
    private boolean matrixChanged;

    /** A sensor node if we have one set. Only valid during construction */
    protected ArrayList sensorList;

    /** Was an extern proto loaded */
    private boolean epLoaded;

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public OGLGeoTransform() {
		super();
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
    public OGLGeoTransform(VRMLNodeType node) {
        super(node);

        init();
    }

    //----------------------------------------------------------
    // Methods defined by OGLPickableTargetNodeType
    //----------------------------------------------------------

    /**
     * Set the flag convertor. Ignored for this node.
     *
     * @param conv The convertor instance to use, or null
     */
    public void setTypeConvertor(OGLPickingFlagConvertor conv) {
        // ignored for this node.
    }

    /**
     * Fetch the object that this target will pick against.
     *
     * @return The valid branchgroup to use
     */
    public PickableObject getPickableObject() {
        return implGroup;
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame. If the derived class needs to propogate the
     * changes then it should override the updateMatrix() method or this
     * and make sure this method is called first.
     */
    public void allEventsComplete() {
        if (epLoaded) {
            epLoaded = false;

            if (implGroup.isLive())
                implGroup.boundsChanged(this);
            else
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
        if(matrixChanged) {
            implGroup.setTransform(tmatrix);
            matrixChanged = false;
        }

        Node kid;
        int addSize = (addedChildren == null) ? 0 : addedChildren.size();
        int remSize = (removedChildren == null) ? 0 : removedChildren.size();

        // if an add and remove of the same node is queued, delete the node from both lists
        if ( ( addSize > 0 ) && ( remSize > 0 ) ) {
            for( int i = addSize - 1; i >= 0; i-- ) {
                kid = (Node)addedChildren.get( i );
                if ( removedChildren.contains( kid ) ) {
                    addedChildren.remove( kid );
                    addSize--;
                    removedChildren.remove( kid );
                    remSize--;
                }
            }
        }

        for(int i = 0; i < remSize; i++) {
            kid = (Node)removedChildren.get(0);
            removedChildren.remove(0);
            implGroup.removeChild(kid);
        }
        
        for(int i = 0; i < addSize; i++) {
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

        oglChildMap = new HashMap();

        if(isStatic && shareCount == 0) {
            oglImplGroup = implGroup;
        } else {
            oglImplGroup = new SharedNode();
            ((SharedNode)oglImplGroup).setChild(implGroup);

        }

        OGLUserData data = new OGLUserData();
        data.isTransform = true;
        data.owner = this;
        implGroup.setUserData(data);

        // Check what sensors we have available and register those with the
        // user data information.
        if(!isStatic && (sensorList.size() != 0)) {
            data.sensors =
                new VRMLPointingDeviceSensorNodeType[sensorList.size()];

            sensorList.toArray(data.sensors);
        }

        sensorList = null;

        for(int i = 0; i < childCount; i++) {
            OGLVRMLNode node = (OGLVRMLNode)vfChildren.get(i);

            Node ogl_node = (Node)node.getSceneGraphObject();
            if(ogl_node != null)
                implGroup.addChild(ogl_node);

            oglChildMap.put(node, ogl_node);
        }

        if(!isStatic) {
            removedChildren = new LinkedList();
            addedChildren = new LinkedList();
        }

        updateMatrix();
        implGroup.setTransform(tmatrix);

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

        oglChildMap.put(node, ogl_node);

        if(ogl_node != null) {
            if(implGroup.isLive()) {
                addedChildren.add(ogl_node);
                epLoaded = true;
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
        return oglImplGroup;
    }

    //----------------------------------------------------------
    // Methods defined by OGLTransformNodeType
    //----------------------------------------------------------

    /**
     * Get the transform matrix for this node.  A reference is ok as
     * the users of this method will not modify the matrix.
     *
     * @return The matrix.
     */
    public Matrix4f getTransform() {
        return tmatrix;
    }

    //----------------------------------------------------------
    // Internal convenience methods
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
            if(implGroup.isLive()) {
                implGroup.boundsChanged(this);
            } else {
                removedChildren.clear();
                implGroup.removeAllChildren();
            }
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

        // Finally check for sensors that we need to deal with.
        VRMLPointingDeviceSensorNodeType sensor = null;

        if(node instanceof VRMLPointingDeviceSensorNodeType)
            sensor = (VRMLPointingDeviceSensorNodeType)node;
        else if(node instanceof VRMLPickingSensorNodeType)
            ((OGLPickingSensorNodeType)node).setParentGroup(implGroup);
        else if(node instanceof VRMLProtoInstance) {
            Object impl = ((VRMLProtoInstance)node).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if(impl instanceof VRMLPointingDeviceSensorNodeType)
                sensor = (VRMLPointingDeviceSensorNodeType)impl;
            else if(impl instanceof VRMLPickingSensorNodeType)
                ((OGLPickingSensorNodeType)impl).setParentGroup(implGroup);
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
            OGLUserData data = (OGLUserData)oglImplGroup.getUserData();

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
     * Overridde updateMatrix.  Notifies aviatrix of bounds changed.
     */
    protected void updateMatrix() {
        super.updateMatrix();

        if(implGroup != null) {
            if (implGroup.isLive()) {
                matrixChanged = true;
                implGroup.boundsChanged(this);
            } else {
                implGroup.setTransform(tmatrix);
            }
        }
    }

    /**
     * Common Initialization code.
     */
    private void init() {
        sensorList = new ArrayList();
        implGroup = new TransformGroup();

        matrixChanged = true;
    }
}
