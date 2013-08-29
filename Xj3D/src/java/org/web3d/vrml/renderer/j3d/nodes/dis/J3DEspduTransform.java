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

package org.web3d.vrml.renderer.j3d.nodes.dis;

// Standard imports
import java.util.*;

import javax.media.j3d.*;

import javax.vecmath.Point3d;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.ObjectArray;
import org.web3d.vrml.util.FieldValidator;
import org.web3d.vrml.renderer.common.nodes.dis.BaseEspduTransform;
import org.web3d.vrml.renderer.CRProtoInstance;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.*;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

/**
 * Java3D implementation of a EspduTransform.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class J3DEspduTransform extends BaseEspduTransform
    implements J3DVRMLNode,
               J3DParentPathRequestHandler,
               J3DPathAwareNodeType,
               J3DPickableTargetNodeType {

    /** High-Side epsilon float = 0 */
    private static final float ZEROEPS = 0.0001f;

    /** The Java3D group node that we are using to place children in. */
    protected TransformGroup implGroup;

    /** The node that gets returned to the caller of getSceneGraphObject() */
    protected Group j3dImplGroup;

    /** Mapping of the VRMLNodeType to the J3D BranchGroup instance */
    protected HashMap j3dChildMap;

    /**
     * Mapping of the VRMLNodeType to the J3D Link instance if used. Note that
     * if someone does something like this:
     * Group { children [ DEF G Group USE G ] }
     * then this map is going to have some problems. We hope that nobody is that
     * stupid, but it is an area that we have to consider will be a source of
     * bugs at some point in the future.
     */
    private HashMap j3dLinkMap;

    /** A sensor node if we have one set. Only valid during construction */
    protected ArrayList sensorList;

    /**
     * The number of children nodes we added to J3D, not the total. We don't
     * Add nodes to J3D that have no Java3D scenegraph object (such as
     * interpolators and timesensors). This count is so that we don't have
     * to enable the ALLOW_CHILDREN_READ on the group (an optimisation step)
     */
    protected int j3dChildCount;

    /** This is the current parent path pointer used to construct the path */
    private J3DParentPathRequestHandler parentPathHandler;

    /** A listing of all path handlers registered */
    private ObjectArray allParentPaths;

    /** Matrix used to hold the transformation */
    protected Matrix4f matrix;

    /** Working variables for the computation */
    private Vector3f tempVec;
    private AxisAngle4f tempAxis;
    private Matrix4f tempMtx1;
    private Matrix4f tempMtx2;

    /** The transform used to shift the node around */
    private Transform3D transform;

    /**
     * Construct a new default instance.
     */
    public J3DEspduTransform() {
        init();
    }

    private void init() {
        j3dChildCount = 0;
        j3dChildMap = new HashMap();
        j3dLinkMap = new HashMap();
        sensorList = new ArrayList();
        allParentPaths = new ObjectArray();

        hasChanged = new boolean[LAST_TRANSFORM_INDEX + 1];

        implGroup = new TransformGroup();

        matrix = new Matrix4f();
        tempVec = new Vector3f();
        tempAxis = new AxisAngle4f();
        tempMtx1 = new Matrix4f();
        tempMtx2 = new Matrix4f();

        transform = new Transform3D();
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame.
     */
    public void allEventsComplete() {
        updateTransform();

        ignoreEspdu = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public J3DEspduTransform(VRMLNodeType node) {
        super(node);

        init();
    }

    //----------------------------------------------------------
    // Methods from the J3DParentPathRequestHandler interface.
    //----------------------------------------------------------

    /**
     * Check to see if the parent path to the root of the scene graph has
     * changed in structure and the scene graph path needs to be regenerated.
     * This is a query only and if this level has not changed then the parent
     * level above should be automatically requested until the root of the
     * live scene graph is reached.
     *
     * @return true if this or a parent of this path has changed
     */
    public boolean hasParentPathChanged() {
        if(parentPathHandler == null)
            return true;
        else
            return parentPathHandler.hasParentPathChanged();
    }

    /**
     * Fetch the scene graph path from the root of the world to this node.
     * If this node's SceneGraphObject is represented by a SharedGroup, then
     * the last item in the given path will be the Link node that is attached
     * to this object.
     *
     * @param requestingChild A reference to the child that's making the request
     * @return The list of locales and nodes in the path down to this node or null
     */
    public ObjectArray getParentPath(J3DVRMLNode requestingChild) {
        if(parentPathHandler == null) {
            if(allParentPaths.size() == 0)
                return null;
            else
                parentPathHandler =
                    (J3DParentPathRequestHandler)allParentPaths.get(0);
        }

        Link link = (Link)j3dLinkMap.get(requestingChild);
        ObjectArray p_path = parentPathHandler.getParentPath(this);

        if(p_path != null) {
            if(link != null)
                p_path.add(link);
        }

        return p_path;
    }

    //----------------------------------------------------------
    // Methods from the J3DPathAwareNodeType interface.
    //----------------------------------------------------------

    /**
     * Add a handler for the parent path requesting. If the request is made
     * more than once, extra copies should be added (for example a  DEF and USE
     * of the same node in the same children field of a Group).
     *
     * @param h The new handler to add
     */
    public void addParentPathListener(J3DParentPathRequestHandler h) {
        allParentPaths.add(h);
    }

    /**
     * Remove a handler for the parent path requesting. If there are multiple
     * copies of this handler registered, then the first one should be removed.
     *
     * @param h The new handler to add
     */
    public void removeParentPathListener(J3DParentPathRequestHandler h) {
        allParentPaths.remove(h);
        if(parentPathHandler == h)
            parentPathHandler = null;
    }

    //----------------------------------------------------------
    // Methods defined by J3DPickableTargetNodeType
    //----------------------------------------------------------

    /**
     * Fetch the group that this target will pick against.
     *
     * @return The valid branchgroup to use
     */
    public Group getPickableGroup() {
        // TODO: Proto's don't work right without returning this
        return j3dImplGroup;
//        return implGroup;
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. This
     * should never be called before setupFinished() is called.
     *
     * @return The J3D representation of this grouping node
     */
    public SceneGraphObject getSceneGraphObject() {

        if(inSetup)
            throw new RuntimeException(getVRMLNodeName());

        return j3dImplGroup;
    }

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
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
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
    }

    /**
     * Notify a node that an ExternProto has resolved.  This will verify the objects
     * type and add it to the render sceneGraph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
     public synchronized void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        // TODO: This does not totally guard against notifications during setupFinished as
        // the base class sets inSetup finish true before J3D structures are complete

        if (inSetup)
            return;

        J3DVRMLNode kid = (J3DVRMLNode) node;

        if(isStatic) {
            // TODO: Not sure this will work for static structures

            // Make sure the child is finished first.
            Node j3d_node = (Node)kid.getSceneGraphObject();

            if(j3d_node == null)
                return;

            // In the static case, it if is not a SharedGroup, we don't
            // need to do anything with it at all and just add it directly
            // to the grouping node parent.
            if(j3d_node instanceof SharedGroup) {
                j3d_node = new Link((SharedGroup)j3d_node);
                j3dLinkMap.put(kid, j3d_node);
            }

            j3dChildCount++;
            implGroup.addChild(j3d_node);
        } else {
            // Make sure the child is finished first.
            kid.setupFinished();
            Node j3d_node = (Node)kid.getSceneGraphObject();

            if(j3d_node == null) {
                return;
            }

            // If the child is a DEF'd node (This could be a use) then we always
            // add a Link and then a BranchGroup above that so that it can be
            // detached later. If there is no child, don't add anything.
            if(j3d_node instanceof SharedGroup) {
                Link link = new Link((SharedGroup)j3d_node);
                BranchGroup bg = new BranchGroup();
                bg.setCapability(BranchGroup.ALLOW_DETACH);
                bg.addChild(link);

                j3dLinkMap.put(kid, link);
                j3d_node = bg;
            } else {
                // just make sure that we can actually detach it later on
                if(!j3d_node.isCompiled())
                    j3d_node.setCapability(BranchGroup.ALLOW_DETACH);
            }

            j3dChildCount++;
            implGroup.addChild(j3d_node);
        }
     }

    /**
     * Notification that the construction phase of this node has finished.
     * This implementation just sets the capability bits. It checks to
     * see if the implGroup has a parent node. If it does not, then it
     * assumes that there needs to be one provided and automatically adds the
     * implGroup as a child of the j3dImplGroup. If there is a parent, it
     * does nothing.
     * <p>
     * Derived classes that do not like this behaviour should override this
     * method or ensure that the implGroup has a parent before this method
     * is called.
     */
    public void setupFinished() {

        if(!inSetup)
            return;

        // If someone asks for this and it is not a USEd DEF node then this won't
        // be set yet. Just make sure it is here.

        if(j3dImplGroup != null)
            throw new RuntimeException("j3d group is not null in setupFinish()");

        super.setupFinished();

        if(isStatic) {
            if(shareCount > 1)
                j3dImplGroup = new SharedGroup();
            else
                j3dImplGroup = new Group();
        } else {
//            if((isDEF || shareCount > 1) && !hasBindables)
            if(!hasBindables)
                j3dImplGroup = new SharedGroup();
            else
                j3dImplGroup = new BranchGroup();
        }

        // If the node has non-default bounds set, let's tell Java3D about
        // it.
        if(vfBboxSize[0] != -1 && vfBboxSize[1] != -1 && vfBboxSize[2] != -1) {
            Point3d min_bound = new Point3d();
            min_bound.x = vfBboxCenter[0] - vfBboxSize[0] / 2;
            min_bound.y = vfBboxCenter[1] - vfBboxSize[1] / 2;
            min_bound.z = vfBboxCenter[2] - vfBboxSize[2] / 2;

            Point3d max_bound = new Point3d();
            max_bound.x = vfBboxCenter[0] + vfBboxSize[0] / 2;
            max_bound.y = vfBboxCenter[1] + vfBboxSize[1] / 2;
            max_bound.z = vfBboxCenter[2] + vfBboxSize[2] / 2;

            BoundingBox bbox = new BoundingBox(min_bound, max_bound);
            j3dImplGroup.setBounds(bbox);
            j3dImplGroup.setBoundsAutoCompute(false);
        }

        // In order for picking, navigation and touch sensors to work these
        // must *always* be on.
        j3dImplGroup.setCapability(Node.ENABLE_PICK_REPORTING);
        j3dImplGroup.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
        j3dImplGroup.setPickable(true);

        // This only works when we are not part of a live scene, which we
        // should not be at this stage.
        Node parent = implGroup.getParent();

        if((parent == null) && (j3dImplGroup != implGroup))
            j3dImplGroup.addChild(implGroup);

        // Check what sensors we have available and register those with the
        // user data information.
        if(!isStatic && (sensorList.size() != 0)) {
            J3DUserData data = (J3DUserData)j3dImplGroup.getUserData();

            if(data == null) {
                data = new J3DUserData();
                j3dImplGroup.setUserData(data);
            }

            data.sensors =
                new VRMLPointingDeviceSensorNodeType[sensorList.size()];

            sensorList.toArray(data.sensors);
        }

        sensorList = null;

        int num_kids = vfChildren.size();
        J3DVRMLNode kid;

        if(isStatic) {
            for(int i = 0; i < num_kids; i++) {
                kid = (J3DVRMLNode)vfChildren.get(i);

                if (kid instanceof CRProtoInstance && ((CRProtoInstance)kid).getComplete() == false)
                    continue;

                if (!(kid instanceof VRMLChildNodeType) && !(kid instanceof VRMLProtoInstance)) {
                    throw new InvalidNodeTypeException(kid.getVRMLNodeName(),"Groups can only contain ChildNodes");
                }

                // Make sure the child is finished first.
                kid.setupFinished();

                Node j3d_node = (Node) kid.getSceneGraphObject();

                if(j3d_node == null) {
                    continue;
                }

                // In the static case, it if is not a SharedGroup, we don't
                // need to do anything with it at all and just add it directly
                // to the grouping node parent.
                if(j3d_node instanceof SharedGroup) {
                    j3d_node = new Link((SharedGroup)j3d_node);
                    j3dLinkMap.put(kid, j3d_node);
                }

                j3dChildCount++;
                implGroup.addChild(j3d_node);
            }
        } else {
            for(int i = 0; i < num_kids; i++) {
                kid = (J3DVRMLNode)vfChildren.get(i);

                if(kid instanceof CRProtoInstance &&
                   ((CRProtoInstance)kid).getComplete() == false)
                    continue;

                if(!(kid instanceof VRMLChildNodeType) &&
                    !(kid instanceof VRMLProtoInstance)) {
                    throw new InvalidNodeTypeException(kid.getVRMLNodeName(),
                                                "Groups can only contain ChildNodes");
                }

                // Make sure the child is finished first.
                kid.setupFinished();

                Node j3d_node = (Node) kid.getSceneGraphObject();

                if(j3d_node == null)
                    continue;

                // If the child is a DEF'd node (This could be a use) then we always
                // add a Link and then a BranchGroup above that so that it can be
                // detached later. If there is no child, don't add anything.
                if(j3d_node instanceof SharedGroup) {
                    Link link = new Link((SharedGroup)j3d_node);
                    BranchGroup bg = new BranchGroup();
                    bg.setCapability(BranchGroup.ALLOW_DETACH);
                    bg.addChild(link);

                    j3dLinkMap.put(kid, link);
                    j3d_node = bg;
                } else {
                    // just make sure that we can actually detach it later on
                    if(!j3d_node.isCompiled())
                        j3d_node.setCapability(BranchGroup.ALLOW_DETACH);
                }

                j3dChildCount++;
                implGroup.addChild(j3d_node);
            }
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the version of VRML that this node should represent. This
     * implementation only sets the capabilities on the j3dImplGroup and not on
     * any other grouping node, such as implGroup. Class that derive from this
     * base class should override this method, call it first and then add their
     * own capabilities settings for thier specific nodes.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param isStatic true if this node is under a static group and won't
     *    change after the setup is finished
     */
    public void setVersion(int major, int minor, boolean isStatic) {
        super.setVersion(major, minor, isStatic);

        if(isStatic)
            return;

        if(j3dImplGroup instanceof BranchGroup) {
            j3dImplGroup.setCapability(BranchGroup.ALLOW_DETACH);
            j3dImplGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
            j3dImplGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        }

        implGroup.setCapability(BranchGroup.ALLOW_DETACH);
        implGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        implGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        implGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        implGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        implGroup.setCapability(BranchGroup.ENABLE_PICK_REPORTING);
    }

    /**
     * Set the translation component of the of transform. Setting a value
     * of null is an error
     *
     * @param tx The new translation component
     * @throws InvalidFieldValueException The translation was null
     */
    public void setTranslation(float[] tx) {
        if (ignoreEspdu) {
            return;
        }

        super.setTranslation(tx);

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Set the rotation component of the of transform. Setting a value
     * of null is an error
     *
     * @param rot The new rotation component
     * @throws InvalidFieldValueException The rotation was null
     */
    public void setRotation(float[] rot)
        throws InvalidFieldValueException {

        if (ignoreEspdu)
            return;

        super.setRotation(rot);

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Clear the child node list of all children - both VRML and Java3D.
     */
    protected void clearChildren() {
        if(j3dChildCount != 0)
            implGroup.removeAllChildren();

        j3dChildCount = 0;

        J3DUserData data = (J3DUserData)j3dImplGroup.getUserData();

        if(j3dLinkMap.size() != 0) {
            Set key_set = j3dLinkMap.keySet();
            Iterator itr = key_set.iterator();
            while(itr.hasNext()) {
                Object obj = itr.next();
                if(obj instanceof J3DPathAwareNodeType) {
                    ((J3DPathAwareNodeType)obj).removeParentPathListener(this);
                }
            }
        }

        j3dLinkMap.clear();

        if(data != null)
            data.sensors = null;

        super.clearChildren();
    }

    /**
     * Add a single child node to the list of available children. This auto
     * matically deals with DEF/USE and adds links and branchgroups where
     * appropriate. When nodes are null, we do not add them to the J3D
     * representation, only to the vfChildren list.
     *
     * @param node The node to view
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addChildNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        super.addChildNode(node);

        J3DVRMLNode n = (J3DVRMLNode)node;

        if(!inSetup) {
            Node j3d_node = (Node)n.getSceneGraphObject();

            // If the child is a DEF'd node (This could be a use) then we always
            // add a Link and then a BranchGroup above that so that it can be
            // detached later.
            if(j3d_node != null) {
                if(j3d_node instanceof SharedGroup) {
                    Link link = new Link((SharedGroup)j3d_node);
                    BranchGroup bg = new BranchGroup();
                    bg.setCapability(BranchGroup.ALLOW_DETACH);
                    bg.addChild(link);

                    j3dLinkMap.put(node, link);
                    j3d_node = bg;
                } else {
                    // just make sure that we can actually detach it later on
                    if(!j3d_node.isCompiled())
                        j3d_node.setCapability(BranchGroup.ALLOW_DETACH);
                }

                j3dChildMap.put(node, j3d_node);
                j3dChildCount++;

                try {
                    implGroup.addChild(j3d_node);
                } catch(javax.media.j3d.MultipleParentException mpe) {
                    System.out.println("**** Group multi parent. Node: " +
                        node.getVRMLNodeName());
                }
            }
        }

        if(node instanceof J3DPathAwareNodeType)
            ((J3DPathAwareNodeType)node).addParentPathListener(this);

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
                J3DUserData data = (J3DUserData)j3dImplGroup.getUserData();

                if(data == null) {
                    data = new J3DUserData();
                    j3dImplGroup.setUserData(data);
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
        if(!j3dChildMap.containsKey(node))
            return;

        BranchGroup j3d_node = (BranchGroup)j3dChildMap.get(node);

        j3dLinkMap.remove(node);
        if(node instanceof J3DPathAwareNodeType)
            ((J3DPathAwareNodeType)node).removeParentPathListener(this);

        j3d_node.detach();

        j3dChildMap.remove(node);
        j3dChildCount--;

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
            J3DUserData data = (J3DUserData)j3dImplGroup.getUserData();

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

        // Finally get around to passing on the remove request to the base
        // class.
        super.removeChildNode(node);
    }

    /**
     * Compares to floats to determine if they are equal or very close
     *
     * @param val1 The first value to compare
     * @param val2 The second value to compare
     * @return True if they are equal within the given epsilon
     */
    private boolean floatEq(float val1, float val2) {
        float diff = val1 - val2;

        if(diff < 0)
            diff *= -1;

        return (diff < ZEROEPS);
    }

    /**
     * Calculate transforms needed to handle VRML semantics
     *  formula: T x C x R x SR x S x -SR x -C
     */
    private void updateTransform() {
        tempVec.x = -vfCenter[0];
        tempVec.y = -vfCenter[1];
        tempVec.z = -vfCenter[2];

        matrix.setIdentity();
        matrix.setTranslation(tempVec);

        float scaleVal = 1.0f;

        if (floatEq(vfScale[0], vfScale[1]) &&
            floatEq(vfScale[0], vfScale[2])) {

            scaleVal = vfScale[0];
            tempMtx1.set(scaleVal);
            //System.out.println("S" + tempMtx1);

        } else {
            // non-uniform scale
            //System.out.println("Non Uniform Scale");
            tempAxis.x = vfScaleOrientation[0];
            tempAxis.y = vfScaleOrientation[1];
            tempAxis.z = vfScaleOrientation[2];
            tempAxis.angle = -vfScaleOrientation[3];

            double tempAxisNormalizer =
                1 / Math.sqrt(tempAxis.x * tempAxis.x +
                              tempAxis.y * tempAxis.y +
                              tempAxis.z * tempAxis.z);

            tempAxis.x *= tempAxisNormalizer;
            tempAxis.y *= tempAxisNormalizer;
            tempAxis.z *= tempAxisNormalizer;

            tempMtx1.set(tempAxis);
            tempMtx2.mul(tempMtx1, matrix);

            // Set the scale by individually setting each element
            tempMtx1.setIdentity();
            tempMtx1.m00 = vfScale[0];
            tempMtx1.m11 = vfScale[1];
            tempMtx1.m22 = vfScale[2];

            matrix.mul(tempMtx1, tempMtx2);

            tempAxis.x = vfScaleOrientation[0];
            tempAxis.y = vfScaleOrientation[1];
            tempAxis.z = vfScaleOrientation[2];
            tempAxis.angle = vfScaleOrientation[3];
            tempMtx1.set(tempAxis);
        }

        tempMtx2.mul(tempMtx1, matrix);

        //System.out.println("Sx-C" + tempMtx2);
        float magSq = vfRotation[0] * vfRotation[0] +
                      vfRotation[1] * vfRotation[1] +
                      vfRotation[2] * vfRotation[2];

        if(magSq < ZEROEPS) {
            tempAxis.x = 0;
            tempAxis.y = 0;
            tempAxis.z = 1;
            tempAxis.angle = 0;
        } else {
            if ((magSq > 1.01) || (magSq < 0.99)) {

                float mag = (float)(1 / Math.sqrt(magSq));
                tempAxis.x = vfRotation[0] * mag;
                tempAxis.y = vfRotation[1] * mag;
                tempAxis.z = vfRotation[2] * mag;
            } else {
                tempAxis.x = vfRotation[0];
                tempAxis.y = vfRotation[1];
                tempAxis.z = vfRotation[2];
            }

            tempAxis.angle = vfRotation[3];
        }

        tempMtx1.set(tempAxis);
        //System.out.println("R" + tempMtx1);

        matrix.mul(tempMtx1, tempMtx2);
        //System.out.println("RxSx-C" + matrix);

        tempVec.x = vfCenter[0];
        tempVec.y = vfCenter[1];
        tempVec.z = vfCenter[2];

        tempMtx1.setIdentity();
        tempMtx1.setTranslation(tempVec);
        //System.out.println("C" + tempMtx1);

        tempMtx2.mul(tempMtx1, matrix);
        //System.out.println("CxRxSx-C" + tempMtx2);

        tempVec.x = vfTranslation[0];
        tempVec.y = vfTranslation[1];
        tempVec.z = vfTranslation[2];

        tempMtx1.setIdentity();
        tempMtx1.setTranslation(tempVec);

        matrix.mul(tempMtx1, tempMtx2);

        transform.set(matrix);
        implGroup.setTransform(transform);
    }

}
