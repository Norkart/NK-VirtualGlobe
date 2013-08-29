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

package org.web3d.vrml.renderer.common.nodes.hanim;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

import org.j3d.geom.hanim.HAnimSite;
import org.j3d.geom.hanim.HAnimFactory;
import org.j3d.geom.hanim.HAnimObject;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.nodes.VRMLHAnimNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;

/**
 * Common implementation for the field handling of a HAnimSite node.
 * <p>
 * The node is defined as follows:
 * <pre>
 *  HAnimSite : X3DGroupingNode {
 *    MFNode     [in]     addChildren               [X3DChildNode]
 *    MFNode     [in]     removeChildren            [X3DChildNode]
 *    SFVec3f    [in,out] center           0 0 0    (-inf,inf)
 *    MFNode     [in,out] children         []       [X3DChildNode]
 *    SFNode     [in,out] metadata         NULL     [X3DMetadataObject]
 *    SFString   [in,out] name             ""
 *    SFRotation [in,out] rotation         0 0 1 0  (-inf,inf)|[-1,1]
 *    SFVec3f    [in,out] scale            1 1 1    (0,inf)
 *    SFRotation [in,out] scaleOrientation 0 0 1 0  (-inf,inf)|[-1,1]
 *    SFVec3f    [in,out] translation      0 0 0    (-inf,inf)|[-1,1]
 *    SFVec3f    []       bboxCenter       0 0 0    (-inf,inf)
 *    SFVec3f    []       bboxSize         -1 -1 -1 [0,inf) or -1 -1 -1
 *  }
 * </pre>
 *
 * This class does not pass the children field values along to the render
 * implementation node because that needs to be a renderer-specific object.
 * Derived classes need to handle this in the setHAnimFactory() call and
 * any time the children are set.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public abstract class BaseHAnimSite extends BaseGroupingNode
    implements VRMLHAnimNodeType {

    /** Field Index for the field: center */
    protected static final int FIELD_CENTER = LAST_GROUP_INDEX + 1;

    /** Field Index for the field: rotation */
    protected static final int FIELD_ROTATION = LAST_GROUP_INDEX + 2;

    /** Field Index for the field: scale */
    protected static final int FIELD_SCALE = LAST_GROUP_INDEX + 3;

    /** Field Index for the field: scale orientation */
    protected static final int FIELD_SCALE_ORIENTATION = LAST_GROUP_INDEX + 4;

    /** Field Index for the field: translation */
    protected static final int FIELD_TRANSLATION = LAST_GROUP_INDEX + 5;

    /** Field Index for the field: name */
    protected static final int FIELD_NAME = LAST_GROUP_INDEX + 6;


    /** The last field index used by this class */
    protected static final int LAST_SITE_INDEX = FIELD_NAME;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_SITE_INDEX + 1;

    /** High-Side epsilon float = 0 */
    private static final float ZEROEPS = 0.0001f;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** SFVec3f center */
    protected float[] vfCenter;

    /** SFRotation rotation */
    protected float[] vfRotation;

    /** SFVec3f scale */
    protected float[] vfScale;

    /** SFRotation scaleOrientation */
    protected float[] vfScaleOrientation;

    /** SFVec3f translation */
    protected float[] vfTranslation;

    /** The name of this displacer */
    protected String vfName;

    /** The generic internal representation of the node */
    protected HAnimSite hanimImpl;

    /** Factory used to generate the implementation node */
    protected HAnimFactory hanimFactory;

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        nodeFields = new int[] { FIELD_CHILDREN, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
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
        fieldDecl[FIELD_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "center");
        fieldDecl[FIELD_ROTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFRotation",
                                     "rotation");
        fieldDecl[FIELD_SCALE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "scale");
        fieldDecl[FIELD_SCALE_ORIENTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFRotation",
                                     "scaleOrientation");
        fieldDecl[FIELD_TRANSLATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "translation");
        fieldDecl[FIELD_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "name");


        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        idx = new Integer(FIELD_ADDCHILDREN);
        fieldMap.put("addChildren", idx);
        fieldMap.put("set_addChildren", idx);

        idx = new Integer(FIELD_REMOVECHILDREN);
        fieldMap.put("removeChildren", idx);
        fieldMap.put("set_removeChildren", idx);

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));

        idx = new Integer(FIELD_CENTER);
        fieldMap.put("center", idx);
        fieldMap.put("set_center", idx);
        fieldMap.put("center_changed", idx);

        idx = new Integer(FIELD_ROTATION);
        fieldMap.put("rotation", idx);
        fieldMap.put("set_rotation", idx);
        fieldMap.put("rotation_changed", idx);

        idx = new Integer(FIELD_SCALE);
        fieldMap.put("scale", idx);
        fieldMap.put("set_scale", idx);
        fieldMap.put("scale_changed", idx);

        idx = new Integer(FIELD_SCALE_ORIENTATION);
        fieldMap.put("scaleOrientation", idx);
        fieldMap.put("set_scaleOrientation", idx);
        fieldMap.put("scaleOrientation_changed", idx);

        idx = new Integer(FIELD_TRANSLATION);
        fieldMap.put("translation", idx);
        fieldMap.put("set_translation", idx);
        fieldMap.put("translation_changed", idx);

        idx = new Integer(FIELD_NAME);
        fieldMap.put("name", idx);
        fieldMap.put("set_name", idx);
        fieldMap.put("name_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseHAnimSite() {
        super("HAnimSite");

        hasChanged = new boolean[LAST_SITE_INDEX + 1];

        vfCenter = new float[] {0, 0, 0};
        vfRotation = new float[] {0, 0, 1, 0};
        vfScale = new float[] {1, 1, 1};
        vfScaleOrientation = new float[] {0, 0, 1, 0};
        vfTranslation = new float[] {0, 0, 0};
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseHAnimSite(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("center");
            VRMLFieldData field = node.getFieldValue(index);

            vfCenter[0] = field.floatArrayValue[0];
            vfCenter[1] = field.floatArrayValue[1];
            vfCenter[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("rotation");
            field = node.getFieldValue(index);

            vfRotation[0] = field.floatArrayValue[0];
            vfRotation[1] = field.floatArrayValue[1];
            vfRotation[2] = field.floatArrayValue[2];
            vfRotation[3] = field.floatArrayValue[3];

            index = node.getFieldIndex("scale");
            field = node.getFieldValue(index);

            vfScale[0] = field.floatArrayValue[0];
            vfScale[1] = field.floatArrayValue[1];
            vfScale[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("scaleOrientation");
            field = node.getFieldValue(index);

            vfScaleOrientation[0] = field.floatArrayValue[0];
            vfScaleOrientation[1] = field.floatArrayValue[1];
            vfScaleOrientation[2] = field.floatArrayValue[2];
            vfScaleOrientation[3] = field.floatArrayValue[3];

            index = node.getFieldIndex("translation");
            field = node.getFieldValue(index);

            vfTranslation[0] = field.floatArrayValue[0];
            vfTranslation[1] = field.floatArrayValue[1];
            vfTranslation[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("name");
            field = node.getFieldValue(index);
            vfName = field.stringValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLHAnimNodeType
    //----------------------------------------------------------

    /**
     * Set the current node factory to use. If this is set again, replace the
     * current implementation node with a new one from this new instance. This
     * may be needed at times when the user makes a change that forces the old
     * way to be incompatible and thus needing a different implementation.
     *
     * @param fac The new factory instance to use
     */
    public void setHAnimFactory(HAnimFactory fac) {
        hanimFactory = fac;

        hanimImpl = fac.createSite();

        // Setup all the fields as needed.
        hanimImpl.setName(vfName);
        hanimImpl.setCenter(vfCenter);
        hanimImpl.setScale(vfScale);
        hanimImpl.setRotation(vfRotation);
        hanimImpl.setTranslation(vfTranslation);
        hanimImpl.setScaleOrientation(vfScaleOrientation);

        // The children field needs to be handled by the derived class.
    }

    /**
     * Get the HAnim implementation node. Since the HAnim class instance is not
     * the same as the basic geometry instance of the particular rendering API, we
     * need to fetch this higher-level construct so that the scene graph can be
     * constructed.
     *
     * @return The HAnimObject instance for this node
     */
    public HAnimObject getHAnimObject() {
        return hanimImpl;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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
        if(index < 0  || index > LAST_SITE_INDEX)
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

        fieldData.clear();
        fieldData.numElements = 1;
        fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;

        switch(index) {
            case FIELD_CENTER:
                fieldData.floatArrayValue = vfCenter;
                break;

            case FIELD_ROTATION:
                fieldData.floatArrayValue = vfRotation;
                break;

            case FIELD_SCALE:
                fieldData.floatArrayValue = vfScale;
                break;

            case FIELD_SCALE_ORIENTATION:
                fieldData.floatArrayValue = vfScaleOrientation;
                break;

            case FIELD_TRANSLATION:
                fieldData.floatArrayValue = vfTranslation;
                break;

            case FIELD_NAME:
                fieldData.stringValue = vfName;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
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
                case FIELD_CENTER :
                    destNode.setValue(destIndex, vfCenter, 3);
                    break;

                case FIELD_ROTATION :
                    destNode.setValue(destIndex, vfRotation, 4);
                    break;

                case FIELD_SCALE :
                    destNode.setValue(destIndex, vfScale, 3);
                    break;

                case FIELD_SCALE_ORIENTATION :
                    destNode.setValue(destIndex, vfScaleOrientation, 4);
                    break;

                case FIELD_TRANSLATION:
                    destNode.setValue(destIndex, vfTranslation, 3);
                    break;

                case FIELD_NAME:
                    destNode.setValue(destIndex, vfName);
                    break;

                default:
                    super.sendRoute(time,srcIndex,destNode,destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseHAnimSite.sendRoute: No field!" + srcIndex);
            ife.printStackTrace();
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
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_CENTER:
                setCenter(value);
                break;

            case FIELD_ROTATION:
                setRotation(value);
                break;

            case FIELD_SCALE:
                setScale(value);
                break;

            case FIELD_SCALE_ORIENTATION:
                setScaleOrientation(value);
                break;

            case FIELD_TRANSLATION:
                setTranslation(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_NAME:
                vfName = value;

                if(!inSetup) {
                    hanimImpl.setName(vfName);
                    hasChanged[FIELD_NAME] = true;
                    fireFieldChanged(FIELD_NAME);
                }
                break;

            default :
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Internal methods of the class
    //----------------------------------------------------------

    /**
     * Set the rotation component of the of transform. Setting a value
     * of null is an error
     *
     * @param rot The new rotation component
     * @throws InvalidFieldValueException The rotation was null
     */
    public void setRotation(float[] rot)
        throws InvalidFieldValueException {

        if(rot == null)
            throw new InvalidFieldValueException("Rotation value null");

        vfRotation[0] = rot[0];
        vfRotation[1] = rot[1];
        vfRotation[2] = rot[2];
        vfRotation[3] = rot[3];

        // Save recalcs during the setup phase
        if(!inSetup) {
            hasChanged[FIELD_ROTATION] = true;
            fireFieldChanged(FIELD_ROTATION);
        }
    }

    /**
     * Set the translation component of the of transform. Setting a value
     * of null is an error
     *
     * @param tx The new translation component
     * @throws InvalidFieldValueException The translation was null
     */
    public void setTranslation(float[] tx)
        throws InvalidFieldValueException {

        if(tx == null)
            throw new InvalidFieldValueException("Translation value null");

        vfTranslation[0] = tx[0];
        vfTranslation[1] = tx[1];
        vfTranslation[2] = tx[2];

        // Save recalcs during the setup phase
        if(!inSetup) {
            hasChanged[FIELD_TRANSLATION] = true;
            fireFieldChanged(FIELD_TRANSLATION);
        }
    }

    /**
     * Set the scale component of the of transform. Setting a value
     * of null is an error
     *
     * @param scale The new scale component
     * @throws InvalidFieldValueException The scale was null
     */
    public void setScale(float[] scale)
        throws InvalidFieldValueException {

        if(scale == null)
            throw new InvalidFieldValueException("Scale value null");

        vfScale[0] = scale[0];
        vfScale[1] = scale[1];
        vfScale[2] = scale[2];

        // Save recalcs during the setup phase
        if(!inSetup) {
            hasChanged[FIELD_SCALE] = true;
            fireFieldChanged(FIELD_SCALE);
        }
    }

    /**
     * Set the scale orientation component of the of transform. Setting a value
     * of null is an error
     *
     * @param so The new scale orientation component
     * @throws InvalidFieldValueException The scale orientation was null
     */
    public void setScaleOrientation(float[] so)
        throws InvalidFieldValueException {
        if(so == null)
            throw new InvalidFieldValueException("Scale Orientation value null");

        vfScaleOrientation[0] = so[0];
        vfScaleOrientation[1] = so[1];
        vfScaleOrientation[2] = so[2];
        vfScaleOrientation[3] = so[3];

        // Save recalcs during the setup phase
        if(!inSetup) {
            hasChanged[FIELD_SCALE_ORIENTATION] = true;
            fireFieldChanged(FIELD_SCALE_ORIENTATION);
        }
    }

    /**
     * Set the center component of the of transform. Setting a value
     * of null is an error
     *
     * @param center The new center component
     * @throws InvalidFieldValueException The center was null
     */
    public void setCenter(float[] center)
        throws InvalidFieldValueException {

        if(center == null)
            throw new InvalidFieldValueException("Center value null");

        vfCenter[0] = center[0];
        vfCenter[1] = center[1];
        vfCenter[2] = center[2];

        // Save recalcs during the setup phase
        if(!inSetup) {
            hasChanged[FIELD_CENTER] = true;
            fireFieldChanged(FIELD_CENTER);
        }
    }
}
