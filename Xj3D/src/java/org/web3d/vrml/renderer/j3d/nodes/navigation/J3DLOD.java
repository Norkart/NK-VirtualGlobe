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

package org.web3d.vrml.renderer.j3d.nodes.navigation;

// External imports
import javax.media.j3d.*;

import java.util.HashMap;
import java.util.Map;
import java.util.BitSet;

import javax.vecmath.Point3d;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Vector3d;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLViewDependentNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DGlobalStatus;
import org.web3d.vrml.renderer.j3d.nodes.J3DGroupingNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DUserData;
import org.web3d.vrml.renderer.j3d.nodes.J3DVisibilityListener;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;

/**
 * Java3D implementation of a LOD node.
 * <p>
 *
 * This code assigns a LOD node as the implGroup. As this code is a
 * grouping node, we allow the use of the children being specified as
 * either the <code>childre</code> field or the <code>level</code> field.
 * The former is VRML3.0 and the latter VRML 2.0
 * <p>
 *
 * The implementation of this node uses a Branch/SharedGroup as the root
 * object for the node and then uses a switch node for the rendered geometry
 * and a Transform and Shape combination for the pickable item. The idea is
 * to make this object active in changing the visible object only once the
 * user gets inside the maximum range object. Once inside, this code will
 * determine the appropriate object to render and select the right object
 * from the switch node. The pickable object is sphere
 * <p>
 *
 * Note that we always change right on the suggested LOD ranges, so the
 * forceTransitions field introduced in 3.1 is ignored for our implementation.
 *
 * @author Justin Couch
 * @version $Revision: 1.25 $
 */
public class J3DLOD extends J3DGroupingNode
    implements VRMLViewDependentNodeType, J3DVisibilityListener {

    /** A point used to represent 0, 0, 0 for easy distance calcs */
    private static final Point3d ORIGIN;

    /** Index of the center field */
    private static final int FIELD_CENTER = LAST_GROUP_INDEX + 1;

    /** The index of the range field */
    private static final int FIELD_RANGE = LAST_GROUP_INDEX + 2;

    /** The index of the level_changed field */
    protected static final int FIELD_LEVEL_CHANGED = LAST_GROUP_INDEX + 3;

    /** The index of the forceTransitions field */
    protected static final int FIELD_FORCE_TRANSITIONS = LAST_GROUP_INDEX + 4;

    /** The last field index used by this class */
    private static final int LAST_LOD_INDEX = FIELD_FORCE_TRANSITIONS;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_LOD_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** SFVec3f center */
    private float[] vfCenter;

    /** MFFloat range */
    private float[] vfRange;

    /** The number of range items in use in the array */
    private int rangeLen;

    /** The values of vfRange squared */
    private float[] rangeSquared;

    /** This is where we keep the switch children values */
    private Switch implSwitch;

    /** The group for the combined switch and shape for picking. */
    private Group implRoot;

    /** The transformation above the bounding box for size and center */
    private TransformGroup implTrans;

    /** The basic shape object used for this node */
    private Shape3D shape;

    /** The index of the currently active object */
    private int activeObject;

    /** Bitset used for controlling the visible child */
    private BitSet viewableChild;

    /** Scratch var for transfering center(translation) values */
    private Vector3d translation;

    private Vector3d temp;

    /** The value of the forceTransitions field */
    protected boolean vfForceTransitions;

    /** The value of the outputOnly field level_changed */
    protected int vfLevelChanged;

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        nodeFields = new int[] { FIELD_CHILDREN };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "children");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxSize");
        fieldDecl[FIELD_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "center");
        fieldDecl[FIELD_RANGE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFFloat",
                                     "range");

        fieldDecl[FIELD_FORCE_TRANSITIONS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "forceTransitions");

        fieldDecl[FIELD_LEVEL_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFInt32",
                                     "level_changed");

        Integer idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        fieldMap.put("level", idx);
        fieldMap.put("set_level", idx);
        fieldMap.put("level_changed", idx);

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));

        fieldMap.put("center",new Integer(FIELD_CENTER));
        fieldMap.put("range",new Integer(FIELD_RANGE));

        fieldMap.put("level_changed",new Integer(FIELD_LEVEL_CHANGED));
        fieldMap.put("forceTransitions",new Integer(FIELD_FORCE_TRANSITIONS));

        ORIGIN = new Point3d();
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public J3DLOD() {
        super("LOD");

        hasChanged = new boolean[NUM_FIELDS];

        vfCenter = new float[] { 0, 0, 0 };

        vfRange = new float[3];
        rangeSquared = new float[3];
        rangeLen = 0;
        activeObject = 0;

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
    public J3DLOD(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("center");
            VRMLFieldData field = node.getFieldValue(index);

            vfCenter[0] = field.floatArrayValue[0];
            vfCenter[1] = field.floatArrayValue[1];
            vfCenter[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("range");
            field = node.getFieldValue(index);

            if(field.numElements > rangeLen) {
                vfRange = new float[field.numElements];
                rangeSquared = new float[field.numElements];
            }

            rangeLen = field.numElements;

            System.arraycopy(field.floatArrayValue, 0, vfRange, 0, rangeLen);

            for(int i = 0; i < rangeLen; i++)
                rangeSquared[i] = vfRange[i] * vfRange[i];

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by J3DVisibilityListener
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
                                      Point3d position,
                                      AxisAngle4d orientation,
                                      Transform3D localPosition) {

        // if is not visible, we optimise by setting the switch to be
        // showing nothing at all.
        if(!visible) {
            implSwitch.setWhichChild(Switch.CHILD_NONE);
            // do this for JDK 1.3 handling which doesn't have a clearAll()
            for(int i = 0; i < rangeLen; i++)
                viewableChild.clear(i);
        } else {
            // Needs to account for vfCenter
            localPosition.get(translation);
            translation.sub(position);

            double total_d = translation.lengthSquared();

            // look through the range array and see if this matches. Only change
            // the visible object if they don't match.
            int old_active_object = activeObject;

            activeObject = rangeLen;

            for(int i = 0; i < rangeLen; i++) {
                if(total_d <= rangeSquared[i]) {
                    activeObject = i;
                    break;
                }
            }

            if(old_active_object != activeObject) {
                viewableChild.clear(old_active_object);
                viewableChild.set(activeObject);
                implSwitch.setWhichChild(Switch.CHILD_MASK);
                implSwitch.setChildMask(viewableChild);

                setLevelChanged(activeObject);
            }
        }
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
    public void viewPositionChanged(Point3d position,
                                    AxisAngle4d orientation,
                                    Transform3D localPosition) {

        // Needs to account for vfCenter
        localPosition.get(translation);
        translation.sub(position);

        double total_d = translation.lengthSquared();

        // look through the range array and see if this matches. Only change
        // the visible object if they don't match.

        int active_now = rangeLen;
        for(int i = 0; i < rangeLen; i++) {
            if(total_d <= rangeSquared[i]) {
                active_now = i;
                break;
            }
        }

        if(active_now != activeObject) {
            viewableChild.clear(activeObject);
            viewableChild.set(active_now);
            implSwitch.setWhichChild(Switch.CHILD_MASK);
            implSwitch.setChildMask(viewableChild);
            activeObject = active_now;

            setLevelChanged(activeObject);
        }
    }

    //----------------------------------------------------------
    // Methods from J3DVRMLNode
    //---------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Ignored by this implementation currently.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
        int[] bits;
        int i;
        int size;

        if(capBits != null) {
            if(capBits.containsKey(BranchGroup.class)) {
                bits = (int[])capBits.get(BranchGroup.class);
                size = (bits == null) ? 0 : bits.length;

                if(size != 0) {
                    for(i = 0; i < size; i++)
                        implGroup.clearCapability(bits[i]);
                } else if(!isStatic) {
                    implGroup.clearCapability(BranchGroup.ALLOW_DETACH);
                    implGroup.clearCapability(BranchGroup.ALLOW_PICKABLE_WRITE);
                }
            }
            // Never set anything on the Shape, Group or TransformGroups,
            // so ignore that.
        }

        // Now do the same for the frequency bit API
        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null)
            return;

        if(freqBits.containsKey(BranchGroup.class)) {
            bits = (int[])freqBits.get(BranchGroup.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implGroup.clearCapabilityIsFrequent(bits[i]);
            } else if(!isStatic) {
                implGroup.clearCapabilityIsFrequent(BranchGroup.ALLOW_DETACH);
                implGroup.clearCapabilityIsFrequent(
                    BranchGroup.ALLOW_PICKABLE_WRITE);
            }
        }

        if(freqBits.containsKey(TransformGroup.class)) {
            bits = (int[])freqBits.get(TransformGroup.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implGroup.clearCapabilityIsFrequent(bits[i]);
            } else if(!isStatic) {
                implTrans.clearCapabilityIsFrequent(
                    TransformGroup.ALLOW_TRANSFORM_WRITE);
            }
        }
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. Ignored by this implementation currently.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
        int[] bits;
        int i;
        int size;

        if(capBits != null) {
            bits = (int[])capBits.get(Shape3D.class);
            size = (bits == null) ? 0 : bits.length;

            for(i = 0; i < size; i++)
                shape.setCapability(bits[i]);

            bits = (int[])capBits.get(BranchGroup.class);
            size = (bits == null) ? 0 : bits.length;

            for(i = 0; i < size; i++)
                implGroup.setCapability(bits[i]);

            bits = (int[])capBits.get(TransformGroup.class);
            size = (bits == null) ? 0 : bits.length;

            for(i = 0; i < size; i++)
                implTrans.setCapability(bits[i]);

            bits = (int[])capBits.get(Group.class);
            size = (bits == null) ? 0 : bits.length;

            for(i = 0; i < size; i++)
                implRoot.setCapability(bits[i]);
        }

        // Now do the same for the frequency bit API
        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null)
            return;

        bits = (int[])freqBits.get(Shape3D.class);
        size = (bits == null) ? 0 : bits.length;

        for(i = 0; i < size; i++)
            shape.setCapabilityIsFrequent(bits[i]);

        bits = (int[])freqBits.get(BranchGroup.class);
        size = (bits == null) ? 0 : bits.length;

        for(i = 0; i < size; i++)
            implGroup.setCapabilityIsFrequent(bits[i]);

        bits = (int[])freqBits.get(TransformGroup.class);
        size = (bits == null) ? 0 : bits.length;

        for(i = 0; i < size; i++)
            implTrans.setCapabilityIsFrequent(bits[i]);

        bits = (int[])freqBits.get(Group.class);
        size = (bits == null) ? 0 : bits.length;

        for(i = 0; i < size; i++)
            implRoot.setCapabilityIsFrequent(bits[i]);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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

        if(isStatic)
            return;

        implSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        implSwitch.setCapability(Switch.ALLOW_CHILDREN_WRITE);
        implSwitch.setCapability(Switch.ALLOW_CHILDREN_EXTEND);

        implRoot.setCapability(Group.ALLOW_CHILDREN_WRITE);
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

        // If there are no ranges set, we set the max range to zero.
        float max_radius = (rangeLen == 0) ? 0 : vfRange[rangeLen - 1] * 2;

        Vector3d scale = new Vector3d(max_radius, max_radius, max_radius);
        Vector3d translation = new Vector3d(vfCenter[0], vfCenter[1], vfCenter[2]);

        Transform3D transform = new Transform3D();
        transform.setScale(scale);
        transform.setTranslation(translation);

        implTrans.setTransform(transform);

        activeObject = rangeLen;
        viewableChild = new BitSet(rangeLen);

        if(activeObject < 0)
            implSwitch.setWhichChild(Switch.CHILD_NONE);
        else {
            viewableChild.set(activeObject);
            implSwitch.setChildMask(viewableChild);
        }

        // Add the local implementation root to the one that is returned
        // to the end user. This works because the implGroup already has
        // a parent and so the base J3DGroupingNode does not automatically
        // add a implGroup as a child to j3dImplGroup.
        j3dImplGroup.addChild(implRoot);
    }

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer)fieldMap.get(fieldName);

        int ret_val = (index == null) ? -1 : index.intValue();

        // forceTransitions and level_changed are added in 3.1. Change
        // the field index to say that they don't exist for VRML or
        // X3D 3.0.
        if((ret_val == FIELD_FORCE_TRANSITIONS ||
            ret_val == FIELD_LEVEL_CHANGED) &&
           ((vrmlMajorVersion == 2) ||
            ((vrmlMajorVersion == 3) && (vrmlMinorVersion == 0))))
            ret_val = -1;

        return ret_val;
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
        if(index < 0  || index > LAST_LOD_INDEX)
            return null;

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
            case FIELD_CENTER:
                fieldData.clear();
                fieldData.floatArrayValue = vfCenter;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_RANGE:
                fieldData.clear();
                fieldData.floatArrayValue = vfRange;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = rangeLen;
                break;

            case FIELD_LEVEL_CHANGED:
                fieldData.clear();
                fieldData.intValue = vfLevelChanged;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.numElements = 1;
                break;

            default:
                super.getFieldValue(index);
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
                case FIELD_CENTER:
                    destNode.setValue(destIndex, vfCenter, 3);
                    break;

                case FIELD_RANGE:
                    destNode.setValue(destIndex, vfRange, rangeLen);
                    break;

                case FIELD_LEVEL_CHANGED:
                    destNode.setValue(destIndex, vfLevelChanged);
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
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_CENTER:
                setCenter(value);
                break;

            case FIELD_RANGE:
                setRange(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Convenience method to call when the implementation has detected that
     * the currently active level has changed.
     *
     * @param level The new level to use
     */
    protected void setLevelChanged(int level) {
        vfLevelChanged = level;

        if(!inSetup) {
            hasChanged[FIELD_LEVEL_CHANGED] = true;
            fireFieldChanged(FIELD_LEVEL_CHANGED);
        }
    }

    /**
     * Set the center component of the of transform. Setting a value
     * of null is an error
     *
     * @param center The new center component
     * @throws InvalidFieldValueException The center was null
     */
    private void setCenter(float[] center)
        throws InvalidFieldValueException {

        if(!inSetup)
            throw new InvalidFieldValueException(
                "Center is an initialize-only field");

        if(center == null)
            throw new InvalidFieldValueException("Center value null");

        vfCenter[0] = center[0];
        vfCenter[1] = center[1];
        vfCenter[2] = center[2];

        translation = new Vector3d(vfCenter[0], vfCenter[1], vfCenter[2]);
    }

    /**
     * Set the range to the new series of values. A null value is the same as
     * removing the range altogether.
     *
     * @param range the new range values to use
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    private void setRange(float[] range, int numValid)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        if(!inSetup)
            throw new InvalidFieldAccessException(
                "Range is an initialize-only field");

        rangeLen = numValid;

        if(numValid != 0) {
            int i;

            // check for all the range values being >= 0
            for(i = range.length; --i >= 0; ) {
                if(range[i] < 0) {
                    throw new InvalidFieldValueException(
                        "Negative range value " +  range[i]);
                }
            }

            // reallocate if the range needed is greater than we have
            if(numValid > vfRange.length) {
                vfRange = new float[numValid];
                rangeSquared = new float[numValid];
            }

            rangeLen = numValid;

            for(i = 0; i < rangeLen; i++) {
                vfRange[i] = range[i];
                rangeSquared[i] = vfRange[i] * vfRange[i];
            }
        }
    }

    /**
     * Commmon internal setup code.
     */
    private void init() {
        translation = new Vector3d();

        // setup the local user data to be not part of the collision
        // system.
        J3DUserData user_data = new J3DUserData();
        user_data.collidable = false;
        user_data.isTerrain = false;
        user_data.visibilityListener = this;

        shape = new Shape3D();
        shape.setAppearance(J3DGlobalStatus.invisibleAppearance);
        shape.setGeometry(J3DGlobalStatus.sphereGeometry);
        shape.setUserData(user_data);

        implTrans = new TransformGroup();
        implTrans.addChild(shape);

        implSwitch = new Switch(Switch.CHILD_MASK);
        implGroup = implSwitch;

        implRoot = new Group();
        implRoot.addChild(implGroup);
        implRoot.addChild(implTrans);

        temp = new Vector3d();
    }
}
