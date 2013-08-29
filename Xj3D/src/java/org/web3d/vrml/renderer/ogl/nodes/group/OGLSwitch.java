/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.group;

// External imports
import org.j3d.aviatrix3d.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.j3d.aviatrix3d.picking.PickableObject;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.ogl.nodes.OGLPickableTargetNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLPickingFlagConvertor;
import org.web3d.vrml.renderer.ogl.nodes.OGLPickingSensorNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.common.nodes.group.BaseSwitch;


/**
 * OpenGL version of a Switch node.
 * <p>
 *
 * The implementation works by doing the shuffling itself of the
 * internal scene graph structures.
 *
 * @author Justin Couch
 * @version $Revision: 1.22 $
 */
public class OGLSwitch extends BaseSwitch
    implements OGLVRMLNode, OGLPickableTargetNodeType, NodeUpdateListener {

    /** Message when an externproto does not fit the ChildNode requirement */
    private static final String BAD_PROTO_MSG =
        "The resolved proto instance is not a X3DChildNode type. Grouping " +
        "nodes may only use ChildNode types for the children field.";

    /** The group returned to aviatrix */
    private Group oglImplGroup;

    /** The renderable scenegraph node */
    private SwitchGroup implGroup;

    /** Mapping of the VRMLNodeType to the OGL Group instance */
    private HashMap oglChildMap;

    /** List of children to add next update node */
    private ArrayList addedChildren;

    /** List of children to remove next update node */
    private ArrayList removedChildren;

    /** Array of the basic OGL nodes that get used - in order */
    private ArrayList oglChildList;

    /** A sensor node if we have one set. Only valid during construction */
    protected ArrayList sensorList;

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public OGLSwitch() {
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
    public OGLSwitch(VRMLNodeType node) {
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
    // Methods required by the FrameStateListener interface.
    //----------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. If the node needs to update itself for this
     * frame, it should do so now before the render pass takes place.
     */
    public void allEventsComplete() {
        if (implGroup.isLive())
            implGroup.boundsChanged(this);
        else
            updateNodeBoundsChanges(implGroup);
    }

    //----------------------------------------------------------
    // Methods required by the NodeUpdateListener interface.
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {

        Node kid;
        OGLVRMLNode vkid;

        int size = (removedChildren == null) ? 0 : removedChildren.size();

        for(int i = 0; i < size; i++) {
            kid = (Node)removedChildren.get(i);
            implGroup.removeChild(kid);
        }

        size = (addedChildren == null) ? 0 : addedChildren.size();
        int len;
        boolean found = false;

        for(int i = 0; i < size; i++) {
            vkid = (OGLVRMLNode) addedChildren.get(i);
            kid = (Node)vkid.getSceneGraphObject();

            len = vfChildren.size();

            for(int j=0; j < len; j++) {
                if (vfChildren.get(j) == vkid) {
                    implGroup.setChild(kid, j);
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("ExternProto not found in child list of OGLSwitch");
            }
        }

        addedChildren.clear();
        removedChildren.clear();

        if (vfWhichChoice > childCount - 1)
            implGroup.setActiveChild(-1);
        else
            implGroup.setActiveChild(vfWhichChoice);
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
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_WHICH_CHOICE :
                setWhichChoice(value);
                break;

            default:
                super.setValue(index, value);
        }
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

        if(isStatic && shareCount == 0) {
            oglImplGroup = implGroup;
        } else {
            oglImplGroup = new SharedGroup();
            oglImplGroup.addChild(implGroup);
        }

        // Check what sensors we have available and register those with the
        // user data information.
        if(!isStatic && (sensorList.size() != 0)) {
            OGLUserData data = new OGLUserData();
            implGroup.setUserData(data);

            data.sensors =
                new VRMLPointingDeviceSensorNodeType[sensorList.size()];

            sensorList.toArray(data.sensors);
        }

        sensorList = null;

        oglChildMap = new HashMap();
        oglChildList = new ArrayList();

        for(int i = 0; i < childCount; i++) {
            OGLVRMLNode node = (OGLVRMLNode)vfChildren.get(i);

            Node ogl_node = (Node)node.getSceneGraphObject();
            implGroup.addChild(ogl_node);

            oglChildList.add(ogl_node);
            oglChildMap.put(node, ogl_node);
        }

        if(!isStatic) {
            removedChildren = new ArrayList();
            addedChildren = new ArrayList();
        }
        if (vfWhichChoice < childCount) {
            implGroup.setActiveChild(vfWhichChoice);
        } else {
            // ignore
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

        Node ogl_node = (Node)kid.getSceneGraphObject();

        oglChildMap.put(node, ogl_node);

        if(ogl_node != null) {
            if(implGroup.isLive()) {
                addedChildren.add(node);
                stateManager.addEndOfThisFrameListener(this);
            } else {
                // TODO: change to setChild
                implGroup.addChild(ogl_node);
            }
        }
    }

    //----------------------------------------------------------
    // Methods from OGLVRMLNode class.
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
    // Internal methods required by the class
    //----------------------------------------------------------

    /**
     * Set which child to display.  -1 = NONE.
     *
     * @param newChoice The new child to display
     * @throws InvalidFieldValueException
     */
    protected void setWhichChoice(int newChoice)
        throws InvalidFieldValueException {

        super.setWhichChoice(newChoice);

        if(!inSetup) {
            if(implGroup.isLive())
                implGroup.boundsChanged(this);
            else {
                if (newChoice > childCount - 1)
                    implGroup.setActiveChild(-1);
                else
                    implGroup.setActiveChild(newChoice);
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

        if (sensorList != null)
            sensorList.clear();

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
                addedChildren.add(node);
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
     * Common Initialization code.
     */
    private void init() {
        implGroup = new SwitchGroup();
        sensorList = new ArrayList();
    }
}
