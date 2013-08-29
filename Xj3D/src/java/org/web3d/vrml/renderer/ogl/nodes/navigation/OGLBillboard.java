/*****************************************************************************
 *                        Web3f.org Copyright (c) 2001 - 2006
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

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.geom.GeometryData;
import org.j3d.util.MatrixUtils;

// Local imports
import org.web3d.vrml.renderer.ogl.nodes.*;

import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLChildNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLExternalSynchronizedNodeType;
import org.web3d.vrml.nodes.VRMLPointingDeviceSensorNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.renderer.common.nodes.navigation.BaseBillboard;

/**
 * OpenGL-renderer implementation of a Billboard node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.18 $
 */
public class OGLBillboard extends BaseBillboard
    implements OGLVRMLNode,
               VRMLExternalSynchronizedNodeType,
               OGLVisibilityListener,
               NodeUpdateListener,
               OGLTransformNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.ExternalSynchronizedNodeType,
          TypeConstants.ViewDependentNodeType };

    /** Message when an externproto does not fit the ChildNode requirement */
    private static final String BAD_PROTO_MSG =
        "The resolved proto instance is not a X3DChildNode type. Grouping " +
        "nodes may only use ChildNode types for the children field.";

    /** Vector pointing upwards */
    private static final Vector3f Y_UP = new Vector3f(0, 1, 0);

    /** Vector pointing upwards */
    private static final Vector3f Z_UP = new Vector3f(0, 0, 1);

    /** Position representing the local origin */
    private static final Point3f ORIGIN = new Point3f(0, 0, 0);

    /** The group returned to aviatrix */
    private Group oglImplGroup;

    /** The group used to rotate the object to face the viewer */
    private TransformGroup implGroup;

    /** The group holding the children */
    private Group kidsGroup;

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

    /** Flag to say the tx matrix has changed */
    private boolean matrixChanged;

    /** A sensor node if we have one set. Only valid during construction */
    private ArrayList sensorList;

    /** Used to update the implTransGroup when fields change */
    private Matrix4f transform;

    /** Inverted version of the above transform matrix */
    private Matrix4f invertedTransform;

    /** Scratch matrix */
    private Matrix4f scratchTransform;

    /** Vector version of the axis of rotation */
    private Vector3f axis;

    /** Geometry data associated with the basic box */
    private GeometryData geomData;

    /** The current minimum bounds extents */
    private float[] minExtents;

    /** The current maximum bounds extents */
    private float[] maxExtents;

    /** Flag indicating the transformation has changed */
    private boolean transformChanged;

    /** Utilities for doing matrix functions */
    private MatrixUtils matrixUtils;

    // Scratch Vars
    private Vector3f up;
    private Vector3f z;
    private Vector3f ax;
    private Point3f bbpos;
    private Vector3f vpos;
    private Vector3f arcp;
    private Vector3f cp;
    private Vector3f cp2;
    private AxisAngle4f aa;

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public OGLBillboard() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Collision node
     */
    public OGLBillboard(VRMLNodeType node) {
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

    //-------------------------------------------------------------
    // Methods defined by VRMLNodeType
    //-------------------------------------------------------------

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
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

        implGroup = new TransformGroup();
        implGroup.addChild(proxyShape);
        kidsGroup = new Group();
        implGroup.addChild(kidsGroup);

        if(isStatic && shareCount == 0) {
            oglImplGroup = implGroup;
        } else {
            oglImplGroup = new SharedGroup();
            oglImplGroup.addChild(implGroup);
        }

        OGLUserData user_data = new OGLUserData();
        implGroup.setUserData(user_data);
        user_data.owner = this;

        // Check what sensors we have available and register those with the
        // user data information.
        if(!isStatic && (sensorList.size() != 0)) {
            user_data.collidable = true;
            user_data.isTerrain = false;
            user_data.visibilityListener = this;

            user_data.sensors =
                new VRMLPointingDeviceSensorNodeType[sensorList.size()];

            sensorList.toArray(user_data.sensors);
        }

        sensorList = null;

        for(int i = 0; i < childCount; i++) {
            OGLVRMLNode node = (OGLVRMLNode)vfChildren.get(i);

            Node ogl_node = (Node)node.getSceneGraphObject();
            if(ogl_node != null)
                kidsGroup.addChild(ogl_node);

            oglChildMap.put(node, ogl_node);
        }


        if(!isStatic) {
            removedChildren = new LinkedList();
            addedChildren = new LinkedList();
        }

        axis.set(vfAxisOfRotation[0],
                 vfAxisOfRotation[1],
                 vfAxisOfRotation[2]);

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

    //----------------------------------------------------------
    // Methods defined by VRMLExternalSynchronizedNodeType
    //----------------------------------------------------------

    /**
     * Notification that event evaluation is about to start.  This is a safer
     * time to modify the underlying rendering structures.
     */
    public void preEventEvaluation() {
        // Save the old bounds
        float min_x = minExtents[0];
        float min_y = minExtents[1];
        float min_z = minExtents[2];

        float max_x = maxExtents[0];
        float max_y = maxExtents[1];
        float max_z = maxExtents[2];

        BoundingVolume bounds = kidsGroup.getBounds();
        bounds.getExtents(minExtents, maxExtents);

        // Big check to see if everything is still the same as last frame.
        // If not, register the bounds to update.
        if(min_x != minExtents[0] || min_y != minExtents[1] ||
           min_z != minExtents[2] || max_x != maxExtents[0] ||
           max_y != maxExtents[1] || max_z != maxExtents[2]) {

            if (proxyGeometry.isLive())
                proxyGeometry.boundsChanged(this);
            else
                updateNodeBoundsChanges(proxyGeometry);
        }

    }

    /**
     * Notification that event evaluation is about to start.  This is a safer
     * time to modify the underlying rendering structures.
     */
    public void postEventEvaluation() {
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

    }

    /**
     * Notification that the object is still visible, but that the
     * viewer reference point has changed.
     *
     * @param position The new position of the user
     * @param orientation The orientation of the user there
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    public void viewPositionChanged(Point3f position,
                                    AxisAngle4f orientation,
                                    Matrix4f localPosition) {

        // Pull off billboard calced transform, leaves vworld to parent
        scratchTransform.mul(localPosition,invertedTransform);

        ax.x = axis.x;
        ax.y = axis.y;
        ax.z = axis.z;
        float alen = ax.lengthSquared();
        boolean align = (alen <= 0.001);

        bbpos.x = 0;
        bbpos.y = 0;
        bbpos.z = 0;

        scratchTransform.transform(bbpos);

        vpos.x = position.x - bbpos.x;
        vpos.y = position.y - bbpos.y;
        vpos.z = position.z - bbpos.z;

        vpos.normalize();

        if (align) {
            ax.x = orientation.x;
            ax.y = orientation.y;
            ax.z = orientation.z;
        }

        scratchTransform.transform(Z_UP, up);

        arcp.cross(ax, up);

        if (arcp.lengthSquared() < 0.001)
            transform.setIdentity();

        if (ax.lengthSquared() < 0.001)
            transform.setIdentity();

        ax.normalize();

        cp.cross(vpos, ax);

        if (cp.length() < 0.0000001) {
            aa.x = ax.x;
            aa.y = ax.y;
            aa.z = ax.z;
            aa.angle = -orientation.angle;

            transform.setIdentity();
            transform.setRotation(aa);

            matrixUtils.inverse(transform, invertedTransform);
            //transform.mul(scratchTransform, transform);

            transformChanged = true;

            if (implGroup.isLive())
                implGroup.boundsChanged(this);
            else
                updateNodeBoundsChanges(implGroup);

            return;
        }

        cp.normalize();

        cp2.cross(cp, up);

        double len2 = cp.dot(up);
        double len = cp2.length();

        double sign;

        if (cp.dot(arcp) > 0)
            sign = -1;
        else
            sign = 1;

        float angle = (float) Math.atan2(len2, sign * len);

//System.out.println("Angle: " + angle + " sign: " + sign + " len: " + len);
        aa.x = ax.x;
        aa.y = ax.y;
        aa.z = ax.z;
        aa.angle = angle;

        transform.setIdentity();
        transform.setRotation(aa);
//System.out.println("Caled AA: " + aa);
//System.out.println("transform: \n " + transform);
        matrixUtils.inverse(transform, invertedTransform);

        //transform.mul(scratchTransform, transform);
        //System.out.println("Final Mat: \n" + transform);

        transformChanged = true;

        if (implGroup.isLive())
            implGroup.boundsChanged(this);
        else
            updateNodeBoundsChanges(implGroup);
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
        return transform;
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
        if(src == implGroup) {
            if(transformChanged) {
                implGroup.setTransform(transform);
                transformChanged = false;
            }
        } else if (src == kidsGroup) {
            int size = (addedChildren == null) ? 0 : addedChildren.size();
            Node kid;

            for(int i = 0; i < size; i++) {
                kid = (Node)addedChildren.get(i);
                kidsGroup.addChild(kid);
            }

            size = (removedChildren == null) ? 0 : removedChildren.size();

            for(int i = 0; i < size; i++) {
                kid = (Node)removedChildren.get(i);
                kidsGroup.removeChild(kid);
            }
        } else if(src == proxyGeometry) {
            updateShape();
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
                if (addedChildren == null)
                    addedChildren = new LinkedList();

                addedChildren.add(ogl_node);
                stateManager.addEndOfThisFrameListener(this);
            } else
                implGroup.addChild(ogl_node);
        }
    }

    //----------------------------------------------------------
    // Mehtods defined by BaseGroupingNode
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
            if (kidsGroup.isLive())
                kidsGroup.boundsChanged(this);
            else
                updateNodeBoundsChanges(kidsGroup);
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

        if(!inSetup) {
            if (kidsGroup.isLive())
                kidsGroup.boundsChanged(this);
            else
                updateNodeBoundsChanges(kidsGroup);
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
            if (kidsGroup.isLive())
                kidsGroup.boundsChanged(this);
            else
                updateNodeBoundsChanges(kidsGroup);
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
        transformChanged = false;

        sensorList = new ArrayList();

        transform = new Matrix4f();
        invertedTransform = new Matrix4f();
        scratchTransform = new Matrix4f();
        scratchTransform.setIdentity();

        axis = new Vector3f();
        up = new Vector3f();
        z = new Vector3f();
        ax = new Vector3f();
        bbpos = new Point3f();
        vpos = new Vector3f();
        arcp = new Vector3f();
        cp = new Vector3f();
        cp2 = new Vector3f();
        aa = new AxisAngle4f();

        minExtents = new float[3];
        maxExtents = new float[3];

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

        transform.setIdentity();
        invertedTransform.setIdentity();

        matrixUtils = new MatrixUtils();
    }

    /**
     * Update the underlying box that is used for the visibility picking.
     */
    private void updateShape() {

        float[] coord = geomData.coordinates;

        // TODO: This shape is totally wrong

        // face 1: +ve Z axis
        coord[0] = maxExtents[0];
        coord[1] = minExtents[1];
        coord[2] = maxExtents[2];

        coord[3] = maxExtents[0];
        coord[4] = maxExtents[1];
        coord[5] = maxExtents[2];

        coord[6] = minExtents[0];
        coord[7] = maxExtents[1];
        coord[8] = maxExtents[2];

        coord[9] = minExtents[0];
        coord[10] = minExtents[1];
        coord[11] = maxExtents[2];

        // face 2: +ve X axis
        coord[12] = maxExtents[0];
        coord[13] = minExtents[1];
        coord[14] = minExtents[2];

        coord[15] = maxExtents[0];
        coord[16] = maxExtents[1];
        coord[17] = minExtents[2];

        coord[18] = maxExtents[0];
        coord[19] = maxExtents[1];
        coord[20] = maxExtents[2];

        coord[21] = maxExtents[0];
        coord[22] = minExtents[1];
        coord[23] = maxExtents[2];

        // face 3: -ve Z axis
        coord[24] = minExtents[0];
        coord[25] = minExtents[1];
        coord[26] = minExtents[2];

        coord[27] = minExtents[0];
        coord[28] = maxExtents[1];
        coord[29] = minExtents[2];

        coord[30] = maxExtents[0];
        coord[31] = maxExtents[1];
        coord[32] = minExtents[2];

        coord[33] = maxExtents[0];
        coord[34] = minExtents[1];
        coord[35] = minExtents[2];

        // face 4: -ve X axis
        coord[36] = minExtents[0];
        coord[37] = minExtents[1];
        coord[38] = maxExtents[2];

        coord[39] = minExtents[0];
        coord[40] = maxExtents[1];
        coord[41] = maxExtents[2];

        coord[42] = minExtents[0];
        coord[43] = maxExtents[1];
        coord[44] = minExtents[2];

        coord[45] = minExtents[0];
        coord[46] = minExtents[1];
        coord[47] = minExtents[2];

        // face 5: +ve Y axis
        coord[48] = maxExtents[0];
        coord[49] = maxExtents[1];
        coord[50] = maxExtents[2];

        coord[51] = maxExtents[0];
        coord[52] = maxExtents[1];
        coord[53] = minExtents[2];

        coord[54] = minExtents[0];
        coord[55] = maxExtents[1];
        coord[56] = minExtents[2];

        coord[57] = minExtents[0];
        coord[58] = maxExtents[1];
        coord[59] = maxExtents[2];

        // face 6: -ve Y axis
        coord[60] = minExtents[0];
        coord[61] = minExtents[1];
        coord[62] = minExtents[2];

        coord[63] = minExtents[0];
        coord[64] = minExtents[1];
        coord[65] = maxExtents[2];

        coord[66] = maxExtents[0];
        coord[67] = minExtents[1];
        coord[68] = maxExtents[2];

        coord[69] = maxExtents[0];
        coord[70] = minExtents[1];
        coord[71] = minExtents[2];

        proxyGeometry.setVertices(QuadArray.COORDINATE_3,
                                  geomData.coordinates,
                                  geomData.vertexCount);
    }
}
