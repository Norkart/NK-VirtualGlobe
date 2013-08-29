/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004-2007
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.vecmath.*;

import org.j3d.geom.GeometryData;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoLOD;
import org.web3d.vrml.renderer.common.nodes.geospatial.SceneWrapper;
import org.web3d.vrml.renderer.ogl.nodes.OGLGlobalStatus;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.ogl.nodes.OGLVisibilityListener;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoOrigin;

/**
 * OpenGL-renderer implementation of a GeoLOD node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public class OGLGeoLOD extends BaseGeoLOD implements
    OGLVRMLNode, OGLVisibilityListener, NodeUpdateListener, VRMLExecutionSpace {

    /** Message when an externproto does not fit the ChildNode requirement */
    private static final String BAD_PROTO_MSG =
        "The resolved proto instance is not a X3DChildNode type. Grouping " +
        "nodes may only use ChildNode types for the children field.";

    /** The group returned to aviatrix */
    private Group oglImplGroup;

    /** The group used to select the closest to the viewer */
    private SwitchGroup implSwitch;

    /** The set of children nodes - either URLs or geometry */
    private Group childGroup;

    /** The root nodes/URL contents */
    private Group rootGroup;

    /** Proxy shape used to make sure this object is picked */
    private Shape3D proxyShape;

    /** Proxy geometry that is updated each frame */
    private QuadArray proxyGeometry;

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

    /** Scratch var for transfering center(translation) values */
    private Vector3f translation;

    /** Set of flags indicating which childScene has just been loaded */
    private boolean[] loadedChildUrls;

    /**
     * Was the root group changed, false if it was the children.
     * Used from setContent and externproto loading.
     */
    private boolean rootGroupChanged;

    /** Did either of the groups change */
    private boolean groupsChanged;

    /** Was the selected child changed */
    private boolean levelChanged;

    /////////////////////////////////////////////////////////////////////////
    // rem: load monitor variables, an experiment to limit
    // the visible popping when levels are switched

    /** List of external nodes that are contained in the child nodes */
    protected ArrayList<VRMLSingleExternalNodeType> childExternalList;

    /////////////////////////////////////////////////////////////////////////
    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public OGLGeoLOD() {
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
    public OGLGeoLOD(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLExecutionSpace
    //----------------------------------------------------------

    public BasicScene getContainedScene() {
        if (childrenShown) {
            // Not sure what to do here
            return null;
        } else {
            return rootScene;
        }
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateManagerListener
    //----------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the scene graph with the
     * loaded scene structure at the end of the frame to avoid issues with
     * multiple access to the scene graph.
     */
    public void allEventsComplete() {
        if (groupsChanged) {
            if (rootGroupChanged) {
                if (rootGroup.isLive()) {
                    rootGroup.boundsChanged(this);
                } else {
                    updateNodeBoundsChanges(rootGroup);
                }
                rootGroupChanged = false;
            } else {
                if (childGroup.isLive()) {
                    childGroup.boundsChanged(this);
                } else {
                    updateNodeBoundsChanges(childGroup);
                }
            }
            groupsChanged = false;
        }

        if (levelChanged) {
            boolean doSwitch = true;
            // block the switch until child content completes loading
            for (int i = 0; i < childExternalList.size(); i++) {
                VRMLSingleExternalNodeType node = childExternalList.get(i);
                if ((node != null) && (node.getLoadState() != VRMLExternalNodeType.LOAD_COMPLETE)) {
                    doSwitch = false;
                    break;
                }
            }
            if ( doSwitch ) {
                if (implSwitch.isLive()) {
                    implSwitch.boundsChanged(this);

                    hasChanged[FIELD_CHILDREN] = true;
                    fireFieldChanged(FIELD_CHILDREN);

                    hasChanged[FIELD_LEVEL_CHANGED] = true;
                    fireFieldChanged(FIELD_LEVEL_CHANGED);

                } else {
                    updateNodeBoundsChanges(implSwitch);
                }
                levelChanged = false;

                // after queueing the switch, queue the removal
                // of the nodes in the inactive level.
                if (childrenShown) {
                    if (rootGroup.isLive()) {
                        rootGroup.boundsChanged(this);
                    } else {
                        updateNodeBoundsChanges(rootGroup);
                    }
                } else {
                    if (childGroup.isLive()) {
                        childGroup.boundsChanged(this);
                    } else {
                        updateNodeBoundsChanges(childGroup);
                    }
                }
            } else {
                // reschedule ourselves until the switch happens
                stateManager.addEndOfThisFrameListener(this);
            }
        }
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

        if(isStatic && shareCount == 0)
            oglImplGroup = new Group();
        else
            oglImplGroup = new SharedGroup();

        implSwitch = new SwitchGroup();
        oglImplGroup.addChild(proxyShape);
        oglImplGroup.addChild(implSwitch);

        rootGroup = new Group();

        // 4 blank spots for the children geometry. These will be replaced with
        // either the URL contents of that of the children field at the same
        // index, if something is set.
        childGroup = new Group();
        childGroup.addChild(null);
        childGroup.addChild(null);
        childGroup.addChild(null);
        childGroup.addChild(null);

        implSwitch.addChild(rootGroup);
        implSwitch.addChild(childGroup);

        // Check what sensors we have available and register those with the
        // user data information.
        if(!isStatic && (sensorList.size() != 0)) {
            OGLUserData user_data = new OGLUserData();
            user_data.collidable = false;
            user_data.isTerrain = false;
            user_data.visibilityListener = this;
            implSwitch.setUserData(user_data);

            user_data.sensors =
                new VRMLPointingDeviceSensorNodeType[sensorList.size()];

            sensorList.toArray(user_data.sensors);
        }

        sensorList = null;

        int min_kids = (childCount < 4) ? childCount : 4;

        for(int i = 0; i < min_kids; i++) {
            OGLVRMLNode node = (OGLVRMLNode)vfChildren.get(i);

            if(node == null)
                continue;

            Node ogl_node = (Node)node.getSceneGraphObject();

            if(ogl_node != null)
                childGroup.setChild(ogl_node, i);

            oglChildMap.put(node, ogl_node);
        }

        implSwitch.setActiveChild(0);

        if(!isStatic) {
            removedChildren = new LinkedList();
            addedChildren = new LinkedList();
        }

        int size = vfRootNode.size();

        OGLVRMLNode kid;
        Node ogl_node;

        for(int i = 0; i < size; i++) {
            kid = (OGLVRMLNode)vfRootNode.get(i);
            ogl_node = (Node)kid.getSceneGraphObject();

            rootGroup.addChild(ogl_node);
        }

        setupShape();

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
    }

    /**
     * Handle notification that an ExternProto has resolved.
     *
     * @param index The field index that got loaded
     * @param node The owner of the node
     */
    public synchronized void notifyExternProtoLoaded(int index,
        VRMLNodeType node) {

        if(!(node instanceof VRMLChildNodeType) &&
            !(node instanceof VRMLProtoInstance))
            throw new InvalidFieldValueException(BAD_PROTO_MSG);

        // TODO: This does not totally guard against notifications during setupFinished as
        // the base class sets inSetup finish true before J3D structures are complete

        if(inSetup)
            return;

        OGLVRMLNode kid = (OGLVRMLNode)node;

        // Make sure the child is finished first.
        kid.setupFinished();
        Node ogl_node = (Node)kid.getSceneGraphObject();

        if(index == FIELD_ROOT_NODE) {
            if(rootGroup.isLive()) {
                rootGroupChanged = true;
                groupsChanged = true;

                stateManager.addEndOfThisFrameListener(this);
            } else {
                rootGroup.addChild(ogl_node);
            }
        } else {
            oglChildMap.put(node, ogl_node);

            if(ogl_node != null) {
                if(childGroup.isLive()) {
                    if (addedChildren == null)
                        addedChildren = new LinkedList();

                    addedChildren.add(ogl_node);
                    groupsChanged = true;

                    stateManager.addEndOfThisFrameListener(this);
                } else {
                    childGroup.addChild(ogl_node);
                }
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLMultiExternalNodeType
    //----------------------------------------------------------

    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This should
     * be one of the forms that the prefered class type call generates.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArguementException The content object is not supported
     */
    public void setContent(int index, String mimetype, Object content)
        throws IllegalArgumentException {

        if(content == null)
            return;

        if(!(content instanceof VRMLScene))
            throw new IllegalArgumentException("Not a VRML Scene instance");

        // All of these are screwed currently because we don't know which URL was
        // the final one that got loaded. So we punt and use the first one.

        // TODO:
        // Not correctly handling the case when this node is not currently
        // live. Assumes that it always is at the point this is called.

        VRMLNodeType node = null;

        switch(index) {
        case FIELD_ROOT_URL:
            rootScene = (VRMLScene)content;
            rootGroupChanged = true;
            rootSceneWrapper = new SceneWrapper(rootScene);

            stateManager.registerAddedScene(rootSceneWrapper);

            node = (VRMLNodeType)rootScene.getRootNode();

            break;

        case FIELD_CHILD1_URL:
            loadedChildUrls[0] = true;
            VRMLScene scene = (VRMLScene)content;
            childScenes[0] = scene;
            loadedScenes[0] = new SceneWrapper(scene);

            stateManager.registerAddedScene(loadedScenes[0]);

            node = (VRMLNodeType)scene.getRootNode();

            getExternalNodeTypes(scene);

            break;

        case FIELD_CHILD2_URL:
            loadedChildUrls[1] = true;
            scene = (VRMLScene)content;
            childScenes[1] = scene;
            loadedScenes[1] = new SceneWrapper(scene);

            stateManager.registerAddedScene(loadedScenes[1]);

            node = (VRMLNodeType)scene.getRootNode();

            getExternalNodeTypes(scene);

            break;

        case FIELD_CHILD3_URL:
            loadedChildUrls[2] = true;
            scene = (VRMLScene)content;
            childScenes[2] = scene;
            loadedScenes[2] = new SceneWrapper(scene);

            stateManager.registerAddedScene(loadedScenes[2]);

            node = (VRMLNodeType)scene.getRootNode();

            getExternalNodeTypes(scene);

            break;

        case FIELD_CHILD4_URL:
            loadedChildUrls[3] = true;
            scene = (VRMLScene)content;
            childScenes[3] = scene;
            loadedScenes[3] = new SceneWrapper(scene);

            stateManager.registerAddedScene(loadedScenes[3]);

            node = (VRMLNodeType)scene.getRootNode();

            getExternalNodeTypes(scene);

            break;
        }

        if (node != null) {
            for(int j = 0; j < layerIds.length; j++) {
                node.updateRefCount(j, true);
            }
        }

        if (rootGroupChanged) {
            if (rootGroup.isLive()) {
                groupsChanged = true;
                stateManager.addEndOfThisFrameListener(this);
            } else {
                updateNodeBoundsChanges(rootGroup);
            }
        } else {
            boolean allChildrenLoaded = true;
            if (vfChild1Url.length != 0) {
                allChildrenLoaded &= loadedChildUrls[0];
            }
            if (vfChild2Url.length != 0) {
                allChildrenLoaded &= loadedChildUrls[1];
            }
            if (vfChild3Url.length != 0) {
                allChildrenLoaded &= loadedChildUrls[2];
            }
            if (vfChild4Url.length != 0) {
                allChildrenLoaded &= loadedChildUrls[3];
            }
            if (allChildrenLoaded) {
                if (childGroup.isLive()) {
                    groupsChanged = true;
                    stateManager.addEndOfThisFrameListener(this);
                } else {
                    updateNodeBoundsChanges(childGroup);
                }
            }
        }
    }

    //-------------------------------------------------------------
    // Methods defined by OGLVisibilityListener
    //-------------------------------------------------------------

    /**
     * Invoked when the user enters or leaves an area.
     *
     * @param visible true when the user enters the area
     * @param position The position of the user on entry/exit
     * @param orientation The orientation of the user there
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    public void visibilityStateChange(boolean visible,
        Point3f position,
        AxisAngle4f orientation,
        Matrix4f localPosition) {
        /*
        // Visibility information is wrong as it accounts for the
                // the center of the LOD in world coordinates.  Ignore for now

                // if is not visible, we optimise by setting the switch to be
                // showing nothing at all.
                if(!visible) {
                    if (implSwitch.isLive())
                        implSwitch.boundsChanged(this);
                    else
                        updateNodeBoundsChanges(implSwitch);
                } else {
                    localPosition.get(translation);
                    translation.x += localCenter[0];
                    translation.y += localCenter[1];
                    translation.z += localCenter[2];

                    translation.sub(position);

                    double total_d = translation.lengthSquared();

                    boolean inside = total_d <= (double) vfRange * vfRange;

                    if(inside != childrenShown) {
                        childrenShown = inside;

                        loadScene(inside);

                        if (implSwitch.isLive())
                            implSwitch.boundsChanged(this);
                        else
                            updateNodeBoundsChanges(implSwitch);
                        }
                }
        */
    }

    /**
     * Notification that the object is still visible, but that the
     * viewer reference point has changed. Ignored for this implementation.
     *
     * @param position The new position of the user
     * @param orientation The orientation of the user there
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    public void viewPositionChanged(Point3f position,
        AxisAngle4f orientation,
        Matrix4f localPosition) {

        // Do not account for the transformation stack for distance

        translation.x = (float) (localCenter[0]);
        translation.y = (float) (localCenter[1]);
        translation.z = (float) (localCenter[2]);

        translation.sub(position);
        double total_d = translation.lengthSquared();


        boolean inside = total_d <= (double) vfRange * vfRange;
        //System.out.println("VPC d: " + total_d + " range: " + ((double)vfRange*vfRange) + " inside: " + inside);

        if(inside != childrenShown) {
            childrenShown = inside;
            levelChanged = true;

            if (childrenShown) {
                // the range threshold to active the child level has been crossed
                boolean allLoaded = true;
                if (vfChild1Url.length != 0) {
                    if (loadedScenes[0] == null) {
                        fireUrlChanged(FIELD_CHILD1_URL);
                        allLoaded = false;
                    } else {
                        stateManager.registerAddedScene(loadedScenes[0]);
                    }
                }
                if (vfChild2Url.length != 0) {
                    if (loadedScenes[1] == null) {
                        fireUrlChanged(FIELD_CHILD2_URL);
                        allLoaded = false;
                    } else {
                        stateManager.registerAddedScene(loadedScenes[1]);
                    }
                }
                if (vfChild3Url.length != 0) {
                    if (loadedScenes[2] == null) {
                        fireUrlChanged(FIELD_CHILD3_URL);
                        allLoaded = false;
                    } else {
                        stateManager.registerAddedScene(loadedScenes[2]);
                    }
                }
                if (vfChild4Url.length != 0) {
                    if (loadedScenes[3] == null) {
                        fireUrlChanged(FIELD_CHILD4_URL);
                        allLoaded = false;
                    } else {
                        stateManager.registerAddedScene(loadedScenes[3]);
                    }
                }
                if (allLoaded) {
                    //levelChanged = true;
                    groupsChanged = true;
                    stateManager.addEndOfThisFrameListener(this);
                }
                if (rootSceneWrapper != null) {
                    stateManager.registerRemovedScene(rootSceneWrapper);
                    rootScene = null;
                    rootSceneWrapper = null;
                }
            } else {
                // the range threshold to active the root level has been crossed
                if (vfRootNode.size() == 0) {
                    // no root node, use the root url
                    if (rootSceneWrapper == null) {
                        fireUrlChanged(FIELD_ROOT_URL);
                    } else {
                        // this should probably never happen since we null
                        // out the rootSceneWrapper when the children level
                        // goes active....
                        groupsChanged = true;
                        rootGroupChanged = true;
                        stateManager.registerAddedScene(rootSceneWrapper);
                        stateManager.addEndOfThisFrameListener(this);
                    }
                } else {
                    // use the root node
                    //levelChanged = true;
                    groupsChanged = true;
                    rootGroupChanged = true;
                    stateManager.addEndOfThisFrameListener(this);
                }
                ////////////////////////////////////////////////////////////////////
                // mark all the child externals as loaded. thus, if a request for
                // loading is on the load queue - it will be ignored. requests that
                // are already in progress will continue.....
                for (int i = 0; i < childExternalList.size(); i++) {
                    VRMLSingleExternalNodeType node = childExternalList.get(i);
                    if (node != null) {
                        node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                    }
                }
                childExternalList.clear();
                ////////////////////////////////////////////////////////////////////
                for (int i = 0; i < 4; i++) {
                    if (loadedScenes[i] != null) {
                        stateManager.registerRemovedScene(loadedScenes[i]);
                        loadedChildUrls[i] = false;
                        childScenes[i] = null;
                        loadedScenes[i] = null;
                    }
                }
            }
        }
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
        if(src == implSwitch) {
            implSwitch.setActiveChild(childrenShown ? 1 : 0);
        } else if(src == childGroup) {
            if (childrenShown) {
                for(int i = 0; i < 4; i++) {
                    if(loadedChildUrls[i]) {
                        OGLVRMLNode root_node = (OGLVRMLNode)childScenes[i].getRootNode();
                        Group grp = (Group)root_node.getSceneGraphObject();

                        childGroup.setChild(grp, i);
                    }
                }
            } else {
                for(int i = 0; i < 4; i++) {
                    childGroup.setChild(null, i);
                }
            }
        } else if (src == rootGroup) {
            rootGroup.removeAllChildren();

            if (!childrenShown) {
                int size = vfRootNode.size();
                if ( size != 0 ) {
                    OGLVRMLNode kid;
                    Node ogl_node;
                    for(int i = 0; i < size; i++) {
                        kid = (OGLVRMLNode)vfRootNode.get(i);
                        ogl_node = (Node)kid.getSceneGraphObject();
                        rootGroup.addChild(ogl_node);
                    }
                } else if ( rootScene != null ) {
                    // Means the contents of the rootUrl have been now loaded. Grab
                    // those and replace the existing children with the new values.
                    OGLVRMLNode root_node = (OGLVRMLNode)rootScene.getRootNode();
                    Group grp = (Group)root_node.getSceneGraphObject();
                    rootGroup.addChild(grp);
                }
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
            if (implSwitch.isLive())
                implSwitch.boundsChanged(this);
            else
                updateNodeBoundsChanges(implSwitch);
        }

        if (sensorList != null)
            sensorList.clear();

        OGLUserData data = (OGLUserData)proxyShape.getUserData();

        if(data != null) {
            data.sensors = null;
        }

        super.clearChildren();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Common Initialization code.
     */
    private void init() {
        childrenShown = false;
        levelChanged = false;

        sensorList = new ArrayList();
        translation = new Vector3f();

        // setup the local user data to be not part of the collision
        // system.
        geomData = new GeometryData();
        geomData.geometryType = GeometryData.QUADS;
        geomData.coordinates = new float[4 * 6 * 3];
        geomData.vertexCount = 4 * 6;

        OGLUserData user_data = new OGLUserData();
        user_data.geometryData = geomData;
        user_data.collidable = false;
        user_data.isTerrain = false;
        user_data.visibilityListener = this;

        proxyGeometry = new QuadArray();

        proxyShape = new Shape3D();
        proxyShape.setAppearance(OGLGlobalStatus.invisibleAppearance);
        proxyShape.setGeometry(proxyGeometry);
        proxyShape.setUserData(user_data);
        proxyShape.setPickMask(Shape3D.PROXIMITY_OBJECT | Shape3D.VISIBLE_OBJECT);

        childScenes = new VRMLScene[4];
        loadedChildUrls = new boolean[4];
        loadedScenes = new SceneWrapper[4];

        loadState[FIELD_CHILD1_URL] = LOAD_COMPLETE;
        loadState[FIELD_CHILD2_URL] = LOAD_COMPLETE;
        loadState[FIELD_CHILD3_URL] = LOAD_COMPLETE;
        loadState[FIELD_CHILD4_URL] = LOAD_COMPLETE;

        childExternalList = new ArrayList<VRMLSingleExternalNodeType>( );
    }

    /**
     * Update the underlying box that is used for the visibility picking.
     */
    private void setupShape() {

        float[] coord = geomData.coordinates;

        // face 1: +ve Z axis
        coord[0] = (float)localCenter[0] + vfRange;
        coord[1] = (float)localCenter[1] - vfRange;
        coord[2] = (float)localCenter[2] + vfRange;

        coord[3] = (float)localCenter[0] + vfRange;
        coord[4] = (float)localCenter[1] + vfRange;
        coord[5] = (float)localCenter[2] + vfRange;

        coord[6] = (float)localCenter[0] - vfRange;
        coord[7] = (float)localCenter[1] + vfRange;
        coord[8] = (float)localCenter[2] + vfRange;

        coord[9] = (float)localCenter[0] - vfRange;
        coord[10] = (float)localCenter[1] - vfRange;
        coord[11] = (float)localCenter[2] + vfRange;

        // face 2: +ve X axis
        coord[12] = (float)localCenter[0] + vfRange;
        coord[13] = (float)localCenter[1] - vfRange;
        coord[14] = (float)localCenter[2] - vfRange;

        coord[12] = (float)localCenter[0] + vfRange;
        coord[13] = (float)localCenter[1] + vfRange;
        coord[14] = (float)localCenter[2] - vfRange;

        coord[12] = (float)localCenter[0] + vfRange;
        coord[13] = (float)localCenter[1] + vfRange;
        coord[14] = (float)localCenter[2] + vfRange;

        coord[12] = (float)localCenter[0] + vfRange;
        coord[13] = (float)localCenter[1] - vfRange;
        coord[14] = (float)localCenter[2] + vfRange;

        // face 3: -ve Z axis
        coord[12] = (float)localCenter[0] - vfRange;
        coord[13] = (float)localCenter[1] - vfRange;
        coord[14] = (float)localCenter[2] - vfRange;

        coord[12] = (float)localCenter[0] - vfRange;
        coord[13] = (float)localCenter[1] + vfRange;
        coord[14] = (float)localCenter[2] - vfRange;

        coord[12] = (float)localCenter[0] + vfRange;
        coord[13] = (float)localCenter[1] + vfRange;
        coord[14] = (float)localCenter[2] - vfRange;

        coord[12] = (float)localCenter[0] + vfRange;
        coord[13] = (float)localCenter[1] - vfRange;
        coord[14] = (float)localCenter[2] - vfRange;

        // face 4: -ve X axis
        coord[12] = (float)localCenter[0] - vfRange;
        coord[13] = (float)localCenter[1] - vfRange;
        coord[14] = (float)localCenter[2] + vfRange;

        coord[12] = (float)localCenter[0] - vfRange;
        coord[13] = (float)localCenter[1] + vfRange;
        coord[14] = (float)localCenter[2] + vfRange;

        coord[12] = (float)localCenter[0] - vfRange;
        coord[13] = (float)localCenter[1] + vfRange;
        coord[14] = (float)localCenter[2] - vfRange;

        coord[12] = (float)localCenter[0] - vfRange;
        coord[13] = (float)localCenter[1] - vfRange;
        coord[14] = (float)localCenter[2] - vfRange;

        // face 5: +ve Y axis
        coord[12] = (float)localCenter[0] + vfRange;
        coord[13] = (float)localCenter[1] + vfRange;
        coord[14] = (float)localCenter[2] + vfRange;

        coord[12] = (float)localCenter[0] + vfRange;
        coord[13] = (float)localCenter[1] + vfRange;
        coord[14] = (float)localCenter[2] - vfRange;

        coord[12] = (float)localCenter[0] - vfRange;
        coord[13] = (float)localCenter[1] + vfRange;
        coord[14] = (float)localCenter[2] - vfRange;

        coord[12] = (float)localCenter[0] - vfRange;
        coord[13] = (float)localCenter[1] + vfRange;
        coord[14] = (float)localCenter[2] + vfRange;

        // face 6: -ve Y axis
        coord[12] = (float)localCenter[0] - vfRange;
        coord[13] = (float)localCenter[1] - vfRange;
        coord[14] = (float)localCenter[2] - vfRange;

        coord[12] = (float)localCenter[0] - vfRange;
        coord[13] = (float)localCenter[1] - vfRange;
        coord[14] = (float)localCenter[2] + vfRange;

        coord[12] = (float)localCenter[0] + vfRange;
        coord[13] = (float)localCenter[1] - vfRange;
        coord[14] = (float)localCenter[2] + vfRange;

        coord[12] = (float)localCenter[0] + vfRange;
        coord[13] = (float)localCenter[1] - vfRange;
        coord[14] = (float)localCenter[2] - vfRange;

        proxyGeometry.setVertices(QuadArray.COORDINATE_3,
            geomData.coordinates,
            geomData.vertexCount);
    }

    /**
     * Get references to any VRMLSingleExternalNodeTypes from the
     * argument VRMLScene.
     */
    private void getExternalNodeTypes(VRMLScene scene) {
        childExternalList.addAll(
            scene.getBySecondaryType(TypeConstants.SingleExternalNodeType));
    }
}
