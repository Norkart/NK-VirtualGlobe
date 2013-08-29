/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004-2005
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

import javax.vecmath.Matrix4f;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Quat4d;
import org.opengis.referencing.FactoryException;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.ogl.nodes.*;

import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoLocation;
import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoOrigin;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.common.geospatial.GTTransformUtils;


/**
 * OpenGL implementation of an GeoLocation
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public class OGLGeoLocation extends BaseGeoLocation
    implements OGLVRMLNode, OGLTransformNodeType, NodeUpdateListener {

    /** Message when an externproto does not fit the ChildNode requirement */
    private static final String BAD_PROTO_MSG =
        "The resolved proto instance is not a X3DChildNode type. Grouping " +
        "nodes may only use ChildNode types for the children field.";

    /** Message during setupFinished() when geotools issues an error */
    private static final String FACTORY_ERR_MSG =
        "Unable to create an appropriate set of operations for the defined " +
        "geoSystem setup. May be either user or tools setup error";

    /** The group holding the children */
    private TransformGroup implGroup;

    /** The group returned to aviatrix */
    private Node oglImplGroup;

    /** Mapping of the VRMLNodeType to the OGL Group instance */
    private HashMap oglChildMap;

    /** List of children to add next update node */
    private ArrayList addedChildren;

    /** List of children to remove next update node */
    private ArrayList removedChildren;

    /** Flag to say the tx matrix has changed */
    private boolean matrixChanged;

    /** A sensor node if we have one set. Only valid during construction */
    protected ArrayList sensorList;

    /** Was an extern proto loaded */
    private boolean epLoaded;

    /** Matrix to put the converted location information into */
    private Matrix4f locationMatrix;

    /** Y-UP Vector */
    private Vector3d YUP = new Vector3d(0,1,0);

    /** X-UP Vector */
    private Vector3d XUP = new Vector3d(1,0,0);

    /** Z-UP Vector */
    private Vector3d ZUP = new Vector3d(0,0,1);

    // Scratch vars
    private Vector3f trans;
    private AxisAngle4f axis;
    private Vector3d posVec;

    /**
     * Default constructor
     */
    public OGLGeoLocation() {
        super();
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLGeoLocation(VRMLNodeType node) {
        super(node);
        init();
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
        if(epLoaded) {
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
            implGroup.setTransform(locationMatrix);
            matrixChanged = false;
        }

        Node kid;
        int size = (removedChildren == null) ? 0 : removedChildren.size();

        for(int i = 0; i < size; i++) {
            kid = (Node)removedChildren.get(i);
            implGroup.removeChild(kid);
        }

        size = (addedChildren == null) ? 0 : addedChildren.size();

        for(int i = 0; i < size; i++) {
            kid = (Node)addedChildren.get(i);
            implGroup.addChild(kid);
        }

        if(addedChildren != null) {
            addedChildren.clear();
            removedChildren.clear();
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
        implGroup.setUserData(data);
        data.owner = this;

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
            removedChildren = new ArrayList();
            addedChildren = new ArrayList();
        }


        updateMatrix();
        implGroup.setTransform(locationMatrix);

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
    // Methods defined by OGLTransformNodeType
    //----------------------------------------------------------

    /**
     * Get the transform matrix for this node.  A reference is ok as
     * the users of this method will not modify the matrix.
     *
     * @return The matrix.
     */
    public Matrix4f getTransform() {
        return locationMatrix;
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
     * Set the geo coordinates now. If we're not in the setup, also do some
     * coordinate conversion to the local position now.
     *
     * @param coords The new coordinate values to use
     */
    protected void setGeoCoords(double[] coords) {
        super.setGeoCoords(coords);

        updateMatrix();

        matrixChanged = true;
        if(implGroup.isLive())
            implGroup.boundsChanged(this);
        else
            updateNodeBoundsChanges(implGroup);
    }

    /**
     * Transform the orientation of the object to one in the local coordinate
     * system.
     */
    private void getLocalOrientation(double[] position, AxisAngle4f axis) {
        posVec.x = position[0];
        posVec.y = position[1];
        posVec.z = position[2];

        double norm = posVec.x * posVec.x + posVec.y * posVec.y + posVec.z * posVec.z;

        if(norm != 0) {
            norm = 1 / Math.sqrt(norm);
            posVec.x *= norm;
            posVec.y *= norm;
            posVec.z *= norm;
        } else {
            posVec.x = 0.0f;
            posVec.y = 1.0f;
            posVec.z = 0.0f;
        }

        // Align Y and X axis
        double angle = YUP.angle(posVec);

        posVec.cross(YUP, posVec);

        axis.x = (float) posVec.x;
        axis.y = (float) posVec.y;
        axis.z = (float) posVec.z;
        axis.angle = (float) angle;

        angle = XUP.angle(posVec);

        posVec.cross(XUP, posVec);

        Quat4d orig = new Quat4d();
        orig.set(axis);
        Quat4d rot = new Quat4d();
        rot.set(new AxisAngle4d(posVec.x, posVec.y, posVec.z, angle));
        orig.mul(rot);
        axis.set(orig);
    }

    /**
     * Mulitply the location and transofmration through to a matrix and apply that
     * to the OpenGL transformation.
     */
    private void updateMatrix() {
        try {
            GTTransformUtils gtu = GTTransformUtils.getInstance();

            if(vfGeoOrigin != null) {
                double[] pos = ((BaseGeoOrigin)vfGeoOrigin).getConvertedCoordRef();
                localCoords[0] += pos[0];
                localCoords[1] += pos[1];
                localCoords[2] += pos[2];
            }

            getLocalOrientation(localCoords, axis);

            if(vfGeoOrigin != null) {
                double[] pos = ((BaseGeoOrigin)vfGeoOrigin).getConvertedCoordRef();
                localCoords[0] -= pos[0];
                localCoords[1] -= pos[1];
                localCoords[2] -= pos[2];
            }
        } catch(FactoryException fe) {
            errorReporter.errorReport(FACTORY_ERR_MSG, fe);
        }

        locationMatrix.setIdentity();
        locationMatrix.set(axis);

        trans.x = (float)localCoords[0];
        trans.y = (float)localCoords[1];
        trans.z = (float)localCoords[2];


        locationMatrix.setTranslation(trans);
    }

    /**
     * Common Initialization code.
     */
    private void init() {
        sensorList = new ArrayList();
        implGroup = new TransformGroup();
        locationMatrix = new Matrix4f();
        locationMatrix.setIdentity();

        trans = new Vector3f();
        axis = new AxisAngle4f();
        posVec = new Vector3d();
    }
}
