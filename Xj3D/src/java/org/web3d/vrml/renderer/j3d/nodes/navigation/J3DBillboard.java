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

package org.web3d.vrml.renderer.j3d.nodes.navigation;

// External imports
import javax.media.j3d.*;

import java.util.HashMap;

import javax.vecmath.*;

import org.j3d.util.MatrixUtils;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.renderer.j3d.nodes.*;

import org.web3d.util.ObjectArray;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;


/**
 * Java3D implementation of a billboard node.
 * <p>
 *
 * This code assigns a Group node as the implGroup and then uses a
 * {@link javax.media.j3d.TransformGroup} below that so that we can properly
 * detach the code. There is an acknowledged, serious performance bug if we
 * don't do this in j3d 1.2.1
 *
 * @author Alan Hudson
 * @version $Revision: 1.18 $
 */
public class J3DBillboard extends J3DGroupingNode
    implements J3DVisibilityListener, J3DPathAwareNodeType {

    /** Vector pointing upwards */
    private static final Vector3f Y_UP = new Vector3f(0, 1, 0);

    /** Vector pointing upwards */
    private static final Vector3f Z_UP = new Vector3f(0, 0, 1);

    /** Position representing the local origin */
    private static final Point3f ORIGIN = new Point3f(0, 0, 0);

    /** Field Index */
    private static final int FIELD_AXISOFROTATION = LAST_GROUP_INDEX + 1;

    /** The last field index used by this class */
    private static final int LAST_BILLBOARD_INDEX = FIELD_AXISOFROTATION;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_BILLBOARD_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** SFVec3f axisOfRotation */
    private float[] vfAxisOfRotation;

    /** The implTransformation above the bounding box for size and center */
    private TransformGroup implTrans;

    /** The implTransformation above the children for rotation */
    private TransformGroup childTrans;

    /** Used to update the implTransGroup when fields change */
    private Transform3D implTransform;

    /** The basic shape object used for this node */
    private Shape3D shape;

    /** Vector version of the axis of rotation */
    private Vector3f axis;

    /** This is the current parent path pointer used to construct the path */
    private J3DParentPathRequestHandler parentPathHandler;

    /** A listing of all path handlers registered */
    private ObjectArray allParentPaths;

    /** Temp arrays for copying stuff for the scene graph path */
    private Object[] tmpPathArray;

    /** Temp array for holding nodes for generating the scene graph path */
    private Node[] tmpNodeArray;

    /** Used to update the implTransGroup when fields change */
    private Matrix4f transform;

    /** Inverted version of the above transform matrix */
    private Matrix4f invertedTransform;

    /** Scratch matrix for parent localtovworld */
    private Transform3D parent;

    /** Eye/Camera position working vector */
    private Point3f posTmp;

    /** Eye/Camera position working vector */
    private Point3f eyepos;

    /** Eye/Camera vector working vector */
    private Vector3f eyevec;

    /** Z or right axis working vector */
    private Vector3f right;

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

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_CHILDREN };
        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 2);

        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "MFNode",
                                 "children");
        fieldDecl[FIELD_ADDCHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "MFNode",
                                 "addChildren");
        fieldDecl[FIELD_REMOVECHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "MFNode",
                                 "removeChildren");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                  "SFVec3f",
                                 "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFVec3f",
                                 "bboxSize");
        fieldDecl[FIELD_AXISOFROTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "SFVec3f",
                                 "axisOfRotation");

        Integer idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        idx = new Integer(FIELD_AXISOFROTATION);
        fieldMap.put("axisOfRotation", idx);
        fieldMap.put("set_axisOfRotation", idx);
        fieldMap.put("axisOfRotation_changed", idx);

        fieldMap.put("addChildren",new Integer(FIELD_ADDCHILDREN));
        fieldMap.put("removeChildren",new Integer(FIELD_REMOVECHILDREN));

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));

    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public J3DBillboard() {
        super("Billboard");

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
    public J3DBillboard(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("axisOfRotation");

            VRMLFieldData field = node.getFieldValue(index);

            vfAxisOfRotation[0] = field.floatArrayValue[0];
            vfAxisOfRotation[1] = field.floatArrayValue[1];
            vfAxisOfRotation[2] = field.floatArrayValue[2];
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }

        init();
    }

    //-------------------------------------------------------------
    // Methods from J3DVisibilityListener
    //-------------------------------------------------------------

    /**
     * Invoked when the user enters or leaves an area.
     *
     * @param visible true when the user enters the area
     * @param position The position of the user on entry/exit
     * @param orientation The orientation of the user there
     * @param localPosition The vworld implTransform object for the class
     *   that implemented this listener
     */
    public void visibilityStateChange(boolean visible,
                                      Point3d position,
                                      AxisAngle4d orientation,
                                      Transform3D localPosition) {

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
    public void viewPositionChanged(Point3d position,
                                    AxisAngle4d orientation,
                                    Transform3D localPosition) {

        j3dImplGroup.getLocalToVworld(getSceneGraphPath(), parent);

        ax.x = axis.x;
        ax.y = axis.y;
        ax.z = axis.z;
        float alen = ax.lengthSquared();
        boolean align = (alen <= 0.001);

        bbpos.x = 0;
        bbpos.y = 0;
        bbpos.z = 0;

        parent.transform(bbpos);

        vpos.x = (float) (position.x - bbpos.x);
        vpos.y = (float) (position.y - bbpos.y);
        vpos.z = (float) (position.z - bbpos.z);

        vpos.normalize();

//System.out.println("Pos: " + position + " vpos: " + vpos);
        if (align) {
            ax.x = (float) orientation.x;
            ax.y = (float) orientation.y;
            ax.z = (float) orientation.z;
        }

        parent.transform(Z_UP, up);

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
            aa.angle = (float) -orientation.angle;

            transform.setIdentity();
            transform.setRotation(aa);

            transform.setIdentity();
            transform.setRotation(aa);

            implTransform.set(transform);
            childTrans.setTransform(implTransform);

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

        implTransform.set(transform);
        childTrans.setTransform(implTransform);
    }

    /**
     * Notification that the object is still visible, but that the
     * viewer reference point has changed.
     *
     * @param position The new position of the user
     * @param orientation The orientation of the user there
     * @param localPosition The vworld implTransform object for the class
     *   that implemented this listener
     */
    public void viewPositionChanged2(Point3d position,
                                    Vector3d orientation,
                                    Transform3D localPosition) {

        j3dImplGroup.getLocalToVworld(getSceneGraphPath(), parent);

        double alen = axis.lengthSquared();

        if (alen <= 0.0001) {
            parent.transform(ORIGIN, eyepos);
            parent.transform(Y_UP, eyevec);
            posTmp.set(position);

            matrixUtils.lookAt(eyepos, posTmp, eyevec, invertedTransform);
            matrixUtils.inverse(invertedTransform, transform);

            implTransform.set(transform);
        } else {
            right.set(orientation);
            parent.transform(right, eyevec);

            right.cross(eyevec, axis);
            right.normalize();

            eyevec.cross(right, axis);

            transform.m00 = right.x;
            transform.m01 = axis.x;
            transform.m02 = eyevec.x;
            transform.m03 = 0;

            transform.m10 = right.y;
            transform.m11 = axis.y;
            transform.m12 = eyevec.y;
            transform.m13 = 0;

            transform.m20 = right.z;
            transform.m21 = axis.z;
            transform.m22 = eyevec.z;
            transform.m23 = 0;

            transform.m30 = 0;
            transform.m31 = 0;
            transform.m32 = 0;
            transform.m33 = 1;

            matrixUtils.inverse(transform, invertedTransform);
            implTransform.set(transform);
        }

        childTrans.setTransform(implTransform);
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNodeType interface.
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

        axis.set(vfAxisOfRotation[0],
                 vfAxisOfRotation[1],
                 vfAxisOfRotation[2]);
        implGroup.setPickable(true);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the version of VRML that this node should represent. Different
     * versions have different capabilities, even within the same node.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param isStatic true if this node is under a static group and won't
     *    change after the setup is finished
     */
    public void setVersion(int major, int minor, boolean isStatic) {
        super.setVersion(major, minor, isStatic);

        childTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        if(isStatic)
            return;

        implTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        implGroup.setCapability(BranchGroup.ALLOW_DETACH);
        implGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        implGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
    }

    //----------------------------------------------------------
    // Methods overriding VRMLNode class.
    //----------------------------------------------------------
    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer) fieldMap.get(fieldName);

        return (index == null) ? -1 : index.intValue();
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if (index < 0  || index > LAST_BILLBOARD_INDEX) {
            return null;
        }

        return fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @param The number of fields.
     */
    public int getNumFields() {
        return fieldDecl.length;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.GroupingNodeType;
    }

   /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_AXISOFROTATION:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;

                fieldData.floatArrayValue = vfAxisOfRotation;
                break;

            default:
                return(super.getFieldValue(index));
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_AXISOFROTATION :
                    destNode.setValue(destIndex, vfAxisOfRotation, 3);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid fieldValue: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_AXISOFROTATION :
                vfAxisOfRotation[0] = value[0];
                vfAxisOfRotation[1] = value[1];
                vfAxisOfRotation[2] = value[2];

                if(!inSetup) {
                    axis.set(vfAxisOfRotation[0],
                             vfAxisOfRotation[1],
                             vfAxisOfRotation[2]);

                    hasChanged[FIELD_AXISOFROTATION] = true;
                    fireFieldChanged(FIELD_AXISOFROTATION);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * A check to see if the parent scene graph path has changed from last
     * time we checked for this node. Assumes that the call is being made on
     * a node that we checked on last frame. If this has been just changed with
     * a new binding call then the caller should just immediately request the
     * current path anyway.
     *
     * @return true if the parent path has changed since last frame
     */
    public boolean hasScenePathChanged() {
        if(parentPathHandler == null)
            return true;
        else
            return parentPathHandler.hasParentPathChanged();
    }

    /**
     * Fetch the scene graph path from the root of the scene to this node.
     * Typically used for the getLocalToVWorld implTransformation handling. If
     * the node returns null then there is no path to the root of the scene
     * ie this node is somehow orphaned during the last frame.
     *
     * @return The fully qualified path from the root to here or null
     */
    public SceneGraphPath getSceneGraphPath() {
        if(parentPathHandler == null) {
            if(allParentPaths.size() == 0)
                return null;
            else
                parentPathHandler =
                    (J3DParentPathRequestHandler)allParentPaths.get(0);
        }

        ObjectArray path_array = parentPathHandler.getParentPath(this);

        if(path_array == null)
            return null;

        int path_size = path_array.size();
        if((tmpPathArray == null) || tmpPathArray.length < path_size) {
            tmpPathArray = new Object[path_size];
            tmpNodeArray = new Node[path_size - 1];
        }

        path_array.toArray(tmpPathArray);
        Locale locale = (Locale)tmpPathArray[0];

        for(int i = 1; i < path_size; i++)
            tmpNodeArray[i - 1] = (Node)tmpPathArray[i];

        return new SceneGraphPath(locale, tmpNodeArray, j3dImplGroup);
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
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Commmon internal setup code.
     */
    private void init() {
        allParentPaths = new ObjectArray();
        implTransform = new Transform3D();
        parent = new Transform3D();

        transform = new Matrix4f();
        invertedTransform = new Matrix4f();
        eyepos = new Point3f();
        posTmp = new Point3f();
        axis = new Vector3f();
        eyevec = new Vector3f();
        right = new Vector3f(0,0,1);

        up = new Vector3f();
        z = new Vector3f();
        ax = new Vector3f();
        bbpos = new Point3f();
        vpos = new Vector3f();
        arcp = new Vector3f();
        cp = new Vector3f();
        cp2 = new Vector3f();
        aa = new AxisAngle4f();

        hasChanged = new boolean[LAST_BILLBOARD_INDEX + 1];
        vfAxisOfRotation = new float[] {0, 1, 0};

        transform.setIdentity();
        invertedTransform.setIdentity();

        matrixUtils = new MatrixUtils();

        // setup the local user data to be not part of the collision
        // system.

        J3DUserData user_data = new J3DUserData();
        user_data.collidable = false;
        user_data.isTerrain = false;
        user_data.visibilityListener = this;

        shape = new Shape3D();
        shape.setAppearance(J3DGlobalStatus.invisibleAppearance);
        shape.setGeometry(J3DGlobalStatus.boxGeometry);
        shape.setUserData(user_data);

        implTrans = new TransformGroup();
        implTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        implTrans.addChild(shape);

        childTrans = new TransformGroup();
        implGroup = childTrans;
        implGroup.addChild(implTrans);
    }
}
