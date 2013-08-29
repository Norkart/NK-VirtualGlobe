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

package org.web3d.vrml.renderer.common.nodes.geospatial;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

// Application specific imports
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;

import org.web3d.vrml.renderer.common.geospatial.GTTransformUtils;

import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;

/**
 * Common implementation of the GeoTransform node.
 * <p>
 *
 * This base class does not automatically update the underlying transform
 * with each set() call. These calls only update the local field values,
 * but not the transform that would be used in the rendering code. To make
 * sure this is updated, call the {@link #updateMatrix()} method and then
 * use the updated matrix in your rendering code.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class BaseGeoTransform extends BaseGroupingNode {

    /** Index of the geoCenter field */
    protected static final int FIELD_GEO_CENTER = LAST_GROUP_INDEX + 1;

	/** Index of the rotation field */
    protected static final int FIELD_ROTATION = LAST_GROUP_INDEX + 2;
	
	/** Index of the scale field */
    protected static final int FIELD_SCALE = LAST_GROUP_INDEX + 3;
	
	/** Index of the scale orientation field */
    protected static final int FIELD_SCALE_ORIENTATION = LAST_GROUP_INDEX + 4;
	
	/** Index of the translation field */
    protected static final int FIELD_TRANSLATION = LAST_GROUP_INDEX + 5;

    /** Index of the geoOrigin field */
    protected static final int FIELD_GEO_ORIGIN = LAST_GROUP_INDEX + 6;

    /** Index of the geoSystem field */
    protected static final int FIELD_GEO_SYSTEM = LAST_GROUP_INDEX + 7;

    /** The last field index used by this class */
    protected static final int LAST_GEOTRANSFORM_INDEX = FIELD_GEO_SYSTEM;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_GEOTRANSFORM_INDEX + 1;

    /** Message for when the proto is not a GeoOrigin */
    private static final String GEO_ORIGIN_PROTO_MSG =
        "Proto does not describe a GeoOrigin object";

    /** Message for when the node in setValue() is not a GeoOrigin */
    private static final String GEO_ORIGIN_NODE_MSG =
        "Node does not describe a GeoOrigin object";

    /** Message during setupFinished() when geotools issues an error */
    private static final String FACTORY_ERR_MSG =
        "Unable to create an appropriate set of operations for the defined " +
        "geoSystem setup. May be either user or tools setup error";

    /** Message when the mathTransform.transform() fails */
    private static final String TRANSFORM_ERR_MSG =
        "Unable to transform the geoCoord value for some reason.";

    /** High-Side epsilon float = 0 */
    private static final float ZEROEPS = 0.0001f;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** field SFVec3d geoCenter */
    protected double[] vfGeoCenter;

    /** SFRotation rotation */
    protected float[] vfRotation;

    /** SFVec3f scale */
    protected float[] vfScale;

    /** SFRotation scaleOrientation */
    protected float[] vfScaleOrientation;

    /** SFVec3f translation */
    protected float[] vfTranslation;
	
	/** field MFString geoSystem ["GD","WE"] */
    protected String[] vfGeoSystem;

    /** Proto version of the geoOrigin */
    protected VRMLProtoInstance pGeoOrigin;

    /** field SFNode geoOrigin */
    protected VRMLNodeType vfGeoOrigin;

    /**
     * The calculated local version of the geoCenter taking into account both the
     * projection information and the GeoOrigin setting.
     */
    protected double[] localCenter;

    /**
     * Transformation used to make the coordinates to the local system. Does
     * not include the geoOrigin offset calcs.
     */
    private MathTransform geoTransform;

    /**
     * Flag to say if the translation geo coords need to be swapped before
     * conversion.
     */
    private boolean geoCoordSwap;

    /** Y-UP Vector */
    private Vector3d YUP = new Vector3d(0,1,0);

    /** X-UP Vector */
    private Vector3d XUP = new Vector3d(1,0,0);

    /** Working variables for the computation */
    private Vector3f tempVec;
    private Vector3d tempVec3d;
    private AxisAngle4f tempAxis;
    private Matrix4f tempMtx1;
    private Matrix4f tempMtx2;
	private Quat4d origQuat;
	private Quat4d rotQuat;
	
	/** Rotation matrix for the localCenter coordinate */
	private Matrix4f locMtx = new Matrix4f();
	
	/** The GeoOrigin coordinate value */
    protected double[] geoOriginCoord;
	
	/** THE transform matrix */
    protected Matrix4f tmatrix;

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        nodeFields = new int[] { 
			FIELD_CHILDREN, 
			FIELD_METADATA,
            FIELD_GEO_ORIGIN };

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
        fieldDecl[FIELD_GEO_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3d",
                                     "geoCenter");
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
        fieldDecl[FIELD_GEO_ORIGIN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "geoOrigin");
        fieldDecl[FIELD_GEO_SYSTEM] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "geoSystem");

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

        idx = new Integer(FIELD_GEO_CENTER);
        fieldMap.put("geoCenter", idx);
        fieldMap.put("set_geoCenter", idx);
        fieldMap.put("geoCenter_changed", idx);

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
		
        fieldMap.put("geoOrigin", new Integer(FIELD_GEO_ORIGIN));
        fieldMap.put("geoSystem", new Integer(FIELD_GEO_SYSTEM));
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseGeoTransform() {
        super("GeoTransform");

        hasChanged = new boolean[NUM_FIELDS];

        vfGeoCenter = new double[3];
        vfRotation = new float[] {0, 0, 1, 0};
        vfScale = new float[] {1, 1, 1};
        vfScaleOrientation = new float[] {0, 0, 1, 0};
        vfTranslation = new float[] {0, 0, 0};
        vfGeoSystem = new String[] {"GD","WE"};

        localCenter = new double[3];
		locMtx = new Matrix4f();
		
        tmatrix = new Matrix4f();
		
        tempVec = new Vector3f();
        tempVec3d = new Vector3d();
        tempAxis = new AxisAngle4f();
        tempMtx1 = new Matrix4f();
        tempMtx2 = new Matrix4f();
		origQuat = new Quat4d();
		rotQuat = new Quat4d();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseGeoTransform(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("geoCenter");
            VRMLFieldData field = node.getFieldValue(index);

            vfGeoCenter[0] = field.doubleArrayValue[0];
            vfGeoCenter[1] = field.doubleArrayValue[1];
            vfGeoCenter[2] = field.doubleArrayValue[2];

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
			
            index = node.getFieldIndex("geoSystem");
            field = node.getFieldValue(index);
            if (field.numElements != 0) {
                vfGeoSystem = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfGeoSystem, 0,
                    field.numElements);
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
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
        updateMatrix();
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNode interface.
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
        if(index < 0  || index > LAST_GEOTRANSFORM_INDEX)
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

        switch(index) {
		case FIELD_GEO_CENTER:
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.doubleArrayValue = vfGeoCenter;
                break;

            case FIELD_ROTATION:
		        fieldData.numElements = 1;
		        fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfRotation;
                break;

            case FIELD_SCALE:
		        fieldData.numElements = 1;
		        fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfScale;
                break;

            case FIELD_SCALE_ORIENTATION:
		        fieldData.numElements = 1;
		        fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfScaleOrientation;
                break;

            case FIELD_TRANSLATION:
		        fieldData.numElements = 1;
		        fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.floatArrayValue = vfTranslation;
                break;

            case FIELD_GEO_ORIGIN:
                if(pGeoOrigin != null) {
                    fieldData.nodeValue = pGeoOrigin;
				} else {
                    fieldData.nodeValue = vfGeoOrigin;
				}
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_GEO_SYSTEM:
                fieldData.numElements = vfGeoSystem.length;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.stringArrayValue = vfGeoSystem;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
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

        if(pGeoOrigin != null) {
            pGeoOrigin.setupFinished();
			geoOriginCoord = ((BaseGeoOrigin)pGeoOrigin).getConvertedCoordRef();
		}
		else if(vfGeoOrigin != null) {
            vfGeoOrigin.setupFinished();
			geoOriginCoord = ((BaseGeoOrigin)vfGeoOrigin).getConvertedCoordRef();
		}

        // Fetch the geo transform and shift the first set of points
        try {
            GTTransformUtils gtu = GTTransformUtils.getInstance();
            boolean[] swap = new boolean[1];

            geoTransform = gtu.createSystemTransform(vfGeoSystem, swap);
            geoCoordSwap = swap[0];

            if(geoCoordSwap) {
                double tmp = vfGeoCenter[0];
                vfGeoCenter[0] = vfGeoCenter[1];
                vfGeoCenter[1] = tmp;
                geoTransform.transform(vfGeoCenter, 0, localCenter, 0, 1);

                tmp = vfGeoCenter[0];
                vfGeoCenter[0] = vfGeoCenter[1];
                vfGeoCenter[1] = tmp;
            } else
                geoTransform.transform(vfGeoCenter, 0, localCenter, 0, 1);

            if(geoOriginCoord != null) {
                localCenter[0] -= geoOriginCoord[0];
                localCenter[1] -= geoOriginCoord[1];
                localCenter[2] -= geoOriginCoord[2];
            }

        } catch(FactoryException fe) {
            errorReporter.errorReport(FACTORY_ERR_MSG, fe);
        } catch(TransformException te) {
            errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
        }
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
				
				case FIELD_GEO_CENTER:
                    destNode.setValue(destIndex, vfGeoCenter, 3);
                    break;
				
                case FIELD_ROTATION:
                    destNode.setValue(destIndex, vfRotation, 4);
                    break;

                case FIELD_SCALE:
                    destNode.setValue(destIndex, vfScale, 3);
                    break;

                case FIELD_SCALE_ORIENTATION:
                    destNode.setValue(destIndex, vfScaleOrientation, 4);
                    break;

                case FIELD_TRANSLATION:
                    destNode.setValue(destIndex, vfTranslation, 3);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseGeoTransform.sendRoute: No field!" + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid fieldValue: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d, SFVec3d, SFVec4d and SFTime
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     */
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
			
			case FIELD_GEO_CENTER:
                setGeoCenter(value);
                break;

            default:
                super.setValue(index, value, numValid);
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
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set the MFString field type "type".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_GEO_SYSTEM:
               if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "geoSystem");

                if(vfGeoSystem.length != numValid)
                    vfGeoSystem = new String[numValid];

                for(int i = 0; i < numValid; i++)
                    vfGeoSystem[i] = value[i];
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;

        switch(index) {
            case FIELD_GEO_ORIGIN:
                setGeoOrigin(child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the geo center coordinate. If we're not in the setup, also do some
     * coordinate conversion to the local center.
     *
     * @param coords The new coordinate values to use
     */
    protected void setGeoCenter(double[] coords)
        throws InvalidFieldValueException {

        if(coords == null)
            throw new InvalidFieldValueException("geoCenter value null");

        vfGeoCenter[0] = coords[0];
        vfGeoCenter[1] = coords[1];
        vfGeoCenter[2] = coords[2];

        if(inSetup)
            return;

        if(geoTransform != null) {
            try {
                if(geoCoordSwap) {
                    double tmp = vfGeoCenter[0];
                    vfGeoCenter[0] = vfGeoCenter[1];
                    vfGeoCenter[1] = tmp;
                    geoTransform.transform(vfGeoCenter, 0, localCenter, 0, 1);

                    tmp = vfGeoCenter[0];
                    vfGeoCenter[0] = vfGeoCenter[1];
                    vfGeoCenter[1] = tmp;
                } else
                    geoTransform.transform(vfGeoCenter, 0, localCenter, 0, 1);

                if(geoOriginCoord != null) {
                    localCenter[0] -= geoOriginCoord[0];
                    localCenter[1] -= geoOriginCoord[1];
                    localCenter[2] -= geoOriginCoord[2];
                }
            } catch(TransformException te) {
                errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
            }
        }

        stateManager.addEndOfThisFrameListener(this);
        hasChanged[FIELD_GEO_CENTER] = true;
        fireFieldChanged(FIELD_GEO_CENTER);
    }
	
    /**
     * Set the rotation component of the of transform. Setting a value
     * of null is an error
     *
     * @param rot The new rotation component
     * @throws InvalidFieldValueException The rotation was null
     */
    protected void setRotation(float[] rot)
        throws InvalidFieldValueException {

        if(rot == null)
            throw new InvalidFieldValueException("Rotation value null");

        vfRotation[0] = rot[0];
        vfRotation[1] = rot[1];
        vfRotation[2] = rot[2];
        vfRotation[3] = rot[3];

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_ROTATION] = true;
            fireFieldChanged(FIELD_ROTATION);
        }
    }

    /**
     * Get the current rotation component of the transform.
     *
     * @return The current rotation
     */
    protected float[] getRotation() {
        return vfRotation;
    }

    /**
     * Set the translation component of the of transform. Setting a value
     * of null is an error
     *
     * @param tx The new translation component
     * @throws InvalidFieldValueException The translation was null
     */
    protected void setTranslation(float[] tx)
        throws InvalidFieldValueException {

        if(tx == null)
            throw new InvalidFieldValueException("Translation value null");

        vfTranslation[0] = tx[0];
        vfTranslation[1] = tx[1];
        vfTranslation[2] = tx[2];

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_TRANSLATION] = true;
            fireFieldChanged(FIELD_TRANSLATION);
        }
    }

    /**
     * Get the current translation component of the transform.
     *
     * @return The current translation
     */
    protected float[] getTranslation() {
        return vfTranslation;
    }

    /**
     * Set the scale component of the of transform. Setting a value
     * of null is an error
     *
     * @param scale The new scale component
     * @throws InvalidFieldValueException The scale was null
     */
    protected void setScale(float[] scale)
        throws InvalidFieldValueException {

        if(scale == null)
            throw new InvalidFieldValueException("Scale value null");

        vfScale[0] = scale[0];
        vfScale[1] = scale[1];
        vfScale[2] = scale[2];

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_SCALE] = true;
            fireFieldChanged(FIELD_SCALE);
        }
    }

    /**
     * Get the current scale component of the transform.
     *
     * @return The current scale
     */
    protected float[] getScale() {
        return vfScale;
    }

    /**
     * Set the scale orientation component of the of transform. Setting a value
     * of null is an error
     *
     * @param so The new scale orientation component
     * @throws InvalidFieldValueException The scale orientation was null
     */
    protected void setScaleOrientation(float[] so)
        throws InvalidFieldValueException {
        if(so == null)
            throw new InvalidFieldValueException("Scale Orientation value null");

        vfScaleOrientation[0] = so[0];
        vfScaleOrientation[1] = so[1];
        vfScaleOrientation[2] = so[2];
        vfScaleOrientation[3] = so[3];

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_SCALE_ORIENTATION] = true;
            fireFieldChanged(FIELD_SCALE_ORIENTATION);
        }
    }

    /**
     * Get the current scale orientation component of the transform.
     *
     * @return The current scale orientation
     */
    protected float[] getScaleOrientation() {
        return vfScaleOrientation;
    }

    /**
     * Set node content for the geoOrigin node.
     *
     * @param geo The new geoOrigin
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setGeoOrigin(VRMLNodeType geo)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        if(!inSetup)
            throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                  "geoOrigin");

        BaseGeoOrigin node;
        VRMLNodeType old_node;

        if(pGeoOrigin != null)
            old_node = pGeoOrigin;
        else
            old_node = vfGeoOrigin;

        if(geo instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)geo).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof BaseGeoOrigin))
                throw new InvalidFieldValueException(GEO_ORIGIN_PROTO_MSG);

            node = (BaseGeoOrigin)impl;
            pGeoOrigin = (VRMLProtoInstance)geo;

        } else if(geo != null && !(geo instanceof BaseGeoOrigin)) {
            throw new InvalidFieldValueException(GEO_ORIGIN_NODE_MSG);
        } else {
            pGeoOrigin = null;
            node = (BaseGeoOrigin)geo;
        }

        vfGeoOrigin = node;
        if(geo != null)
            updateRefs(geo, true);

        if(old_node != null)
            updateRefs(old_node, false);
    }

    /**
     * Compares to floats to determine if they are equal or very close
     *
     * @param val1 The first value to compare
     * @param val2 The second value to compare
     * @return true if they are equal within the given epsilon, false otherwise
     */
    private boolean floatEq(float val1, float val2) {
        float diff = val1 - val2;

        if(diff < 0)
            diff *= -1;

        return (diff < ZEROEPS);
    }

    /**
     * Calculate transforms needed to handle VRML semantics and place the
     * results in the matrix variable of this class.
     *  formula: T x C x R x SR x S x -SR x -C
     */
    protected void updateMatrix() {

		////////////////////////////////////////////////////////////////////
		// get the localCenter's rotation matrix
		getLocalOrientation(localCenter, locMtx);
		////////////////////////////////////////////////////////////////////
		
        //System.out.println(this);
        tempVec3d.x = -localCenter[0];
        tempVec3d.y = -localCenter[1];
        tempVec3d.z = -localCenter[2];
		tempVec.set(tempVec3d);

        tmatrix.setIdentity();
        tmatrix.setTranslation(tempVec);

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
            tempMtx2.mul(tempMtx1, tmatrix);

            // Set the scale by individually setting each element
            tempMtx1.setIdentity();
            tempMtx1.m00 = vfScale[0];
            tempMtx1.m11 = vfScale[1];
            tempMtx1.m22 = vfScale[2];

            tmatrix.mul(tempMtx1, tempMtx2);

            tempAxis.x = vfScaleOrientation[0];
            tempAxis.y = vfScaleOrientation[1];
            tempAxis.z = vfScaleOrientation[2];
            tempAxis.angle = vfScaleOrientation[3];
            tempMtx1.set(tempAxis);
        }

        tempMtx2.mul(tempMtx1, tmatrix);

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
		////////////////////////////////////////////////////////////////////
		// transform the local rotation axis with the localCenter's rotation
		tempVec.set( tempAxis.x, tempAxis.y, tempAxis.z );
		locMtx.transform( tempVec );
		tempAxis.x = tempVec.x;
		tempAxis.y = tempVec.y;
		tempAxis.z = tempVec.z;
		////////////////////////////////////////////////////////////////////
        tempMtx1.set(tempAxis);
        //System.out.println("R" + tempMtx1);

        tmatrix.mul(tempMtx1, tempMtx2);
        //System.out.println("RxSx-C" + matrix);

        tempVec3d.x = localCenter[0];
        tempVec3d.y = localCenter[1];
        tempVec3d.z = localCenter[2];
		tempVec.set(tempVec3d);

        tempMtx1.setIdentity();
        tempMtx1.setTranslation(tempVec);
        //System.out.println("C" + tempMtx1);

        tempMtx2.mul(tempMtx1, tmatrix);
        //System.out.println("CxRxSx-C" + tempMtx2);

        tempVec.x = vfTranslation[0];
        tempVec.y = vfTranslation[1];
        tempVec.z = vfTranslation[2];

		////////////////////////////////////////////////////////////////////
		// transform the local translation with the localCenter's rotation
		locMtx.transform( tempVec );
		////////////////////////////////////////////////////////////////////
        tempMtx1.setIdentity();
        tempMtx1.setTranslation(tempVec);
		
        // TODO: Try reversing order of ops
        tmatrix.mul(tempMtx1, tempMtx2);
        //tmatrix.mul(tempMtx2, tempMtx1);
    }

    /**
     * Get the orientation of the coordinate for the local coordinate system.
	 *
	 * @param position The position coordinate to determine the rotation for.
	 * @param mat The matrix object to initialize with the rotation.
     */
	private void getLocalOrientation(double[] position, Matrix4f mat) {
		
        tempVec3d.x = position[0];
        tempVec3d.y = position[1];
        tempVec3d.z = position[2];

        if(geoOriginCoord != null) {
            tempVec3d.x += geoOriginCoord[0];
            tempVec3d.y += geoOriginCoord[1];
            tempVec3d.z += geoOriginCoord[2];
        }

        double norm = 
			tempVec3d.x * tempVec3d.x + 
			tempVec3d.y * tempVec3d.y + 
			tempVec3d.z * tempVec3d.z;

        if(norm != 0) {
            norm = 1 / Math.sqrt(norm);
            tempVec3d.x *= norm;
            tempVec3d.y *= norm;
            tempVec3d.z *= norm;
        } else {
            tempVec3d.x = 0.0f;
            tempVec3d.y = 1.0f;
            tempVec3d.z = 0.0f;
        }

        // Align Y and X axis
        double angle = YUP.angle(tempVec3d);

        tempVec3d.cross(YUP, tempVec3d);

        tempAxis.x = (float) tempVec3d.x;
        tempAxis.y = (float) tempVec3d.y;
        tempAxis.z = (float) tempVec3d.z;
        tempAxis.angle = (float) angle;

        angle = XUP.angle(tempVec3d);

        tempVec3d.cross(XUP, tempVec3d);

        origQuat = new Quat4d();
        origQuat.set(tempAxis);
        rotQuat = new Quat4d();
        rotQuat.set(new AxisAngle4d(tempVec3d.x, tempVec3d.y, tempVec3d.z, angle));
        origQuat.mul(rotQuat);
		mat.set(origQuat);
    }
}
