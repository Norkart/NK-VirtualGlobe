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

package org.web3d.vrml.renderer.common.nodes.geospatial;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.lang.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.renderer.common.geospatial.GTTransformUtils;

/**
 * Common implementation of an GeoOrigin node.
 * <p>
 * This may not be needed since we have doubles now.  But it could
 * help in z-buffer setting.  This node will be ignored for now.
 *
 * NOTE: Why is geoSystem in/out here but initializeOnly everywhere else?
 *       Why is geoCoords in/out, bad idea.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.11 $
 */
public class BaseGeoOrigin extends AbstractNode implements VRMLLocalOriginNodeType  {

    /** Index of the geoOrigin field */
    protected static final int FIELD_GEO_ORIGIN = LAST_NODE_INDEX + 1;

    /** Index of the geoSystem field */
    protected static final int FIELD_GEO_SYSTEM = LAST_NODE_INDEX + 2;

    /** Index of the geoCoords field */
    protected static final int FIELD_GEO_COORDS = LAST_NODE_INDEX + 3;

    /** Index of the rotateYUp field */
    protected static final int FIELD_ROTATE_Y_UP = LAST_NODE_INDEX + 4;

    /** The last index of the nodes used by the GeoOrigin */
    protected static final int LAST_GEOORIGIN_INDEX = FIELD_ROTATE_Y_UP;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_GEOORIGIN_INDEX + 1;

    /** Message during setupFinished() when geotools issues an error */
    private static final String FACTORY_ERR_MSG =
        "Unable to create an appropriate set of operations for the defined " +
        "geoSystem setup. May be either user or tools setup error";

    /** Message when the mathTransform.transform() fails */
    private static final String TRANSFORM_ERR_MSG =
        "Unable to transform the geoCoord value for some reason.";

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration fieldDecl[];

    /** Hashmap between a field name and its index */
    protected static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** field SFBool rotateYUp */
    protected boolean vfRotateYUp;

    /** field SFVec3d geoCoords 0 0 0 */
    protected double[] vfGeoCoords;

    /** field MFString geoSystem ["GD","WE"] */
    protected String[] vfGeoSystem;

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

    /** The converted offset values */
    protected double[] localCoords;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_GEO_COORDS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3d",
                                     "geoCoords");

        fieldDecl[FIELD_GEO_SYSTEM] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "geoSystem");

        fieldDecl[FIELD_ROTATE_Y_UP] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "rotateYUp");


        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_GEO_COORDS);
        fieldMap.put("geoCoords",idx);
        fieldMap.put("set_geoCoords",idx);
        fieldMap.put("geoCoords_changed",idx);

        fieldMap.put("rotateYUp", new Integer(FIELD_ROTATE_Y_UP));

        idx = new Integer(FIELD_GEO_SYSTEM);
        fieldMap.put("geoSystem", idx);
        fieldMap.put("set_geoSystem", idx);
        fieldMap.put("geoSystem_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * X3D specification.
     */
    public BaseGeoOrigin() {
        super("GeoOrigin");

        hasChanged = new boolean[NUM_FIELDS];

        vfGeoCoords = new double[3];
        vfGeoSystem = new String[] {"GD","WE"};
        vfRotateYUp = false;

        localCoords = new double[3];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseGeoOrigin(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("rotateYUp");
            VRMLFieldData field = node.getFieldValue(index);
            vfRotateYUp = field.booleanValue;

            index = node.getFieldIndex("geoCoords");
            field = node.getFieldValue(index);
            vfGeoCoords[0] = field.doubleArrayValue[0];
            vfGeoCoords[1] = field.doubleArrayValue[1];
            vfGeoCoords[2] = field.doubleArrayValue[2];

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
    // Methods defined by BaseVRMLNodeType
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

        // Fetch the geo transform and shift the first set of points
        try {
            GTTransformUtils gtu = GTTransformUtils.getInstance();
            boolean[] swap = new boolean[1];

            geoTransform = gtu.createSystemTransform(vfGeoSystem, swap);
            geoCoordSwap = swap[0];

            if(geoCoordSwap) {
                double tmp = vfGeoCoords[0];
                vfGeoCoords[0] = vfGeoCoords[1];
                vfGeoCoords[1] = tmp;
                geoTransform.transform(vfGeoCoords, 0, localCoords, 0, 1);

                tmp = vfGeoCoords[0];
                vfGeoCoords[0] = vfGeoCoords[1];
                vfGeoCoords[1] = tmp;
            } else
                geoTransform.transform(vfGeoCoords, 0, localCoords, 0, 1);
        } catch(FactoryException fe) {
            errorReporter.errorReport(FACTORY_ERR_MSG, fe);
        } catch(TransformException te) {
            errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
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
        return (index < 0 || index > LAST_GEOORIGIN_INDEX) ?
            null : fieldDecl[index];
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
        return TypeConstants.ChildNodeType;
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
            case FIELD_ROTATE_Y_UP:
                fieldData.clear();
                fieldData.booleanValue = vfRotateYUp;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_GEO_SYSTEM:
                fieldData.clear();
                fieldData.stringArrayValue = vfGeoSystem;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfGeoSystem.length;
                break;

            case FIELD_GEO_COORDS:
                fieldData.clear();
                fieldData.doubleArrayValue = vfGeoCoords;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfGeoCoords.length;
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
                case FIELD_GEO_COORDS:
                    destNode.setValue(destIndex, vfGeoCoords, vfGeoCoords.length);
                    break;

                case FIELD_GEO_SYSTEM:
                    destNode.setValue(destIndex, vfGeoSystem, vfGeoSystem.length);
                    break;

                case FIELD_ROTATE_Y_UP:
                    destNode.setValue(destIndex, vfRotateYUp);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ROTATE_Y_UP:
                if(!inSetup)
                    throw new InvalidFieldAccessException(
                        "rotateYUp is an initializeOnly field");

                vfRotateYUp = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFDouble, SFVec2d, SFVec3d
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_GEO_COORDS:
                setGeoCoords(value);
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
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_GEO_SYSTEM:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG + "geoSystem");

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
     * Set the geo coordinates now. If we're not in the setup, also do some
     * coordinate conversion to the local position now.
     *
     * @param coords The new coordinate values to use
     */
    protected void setGeoCoords(double[] coords) {
        vfGeoCoords[0] = coords[0];
        vfGeoCoords[1] = coords[1];
        vfGeoCoords[2] = coords[2];

        if(inSetup)
            return;

        if(geoTransform != null) {
            try {
                if(geoCoordSwap) {
                    double tmp = vfGeoCoords[0];
                    vfGeoCoords[0] = vfGeoCoords[1];
                    vfGeoCoords[1] = tmp;
                    geoTransform.transform(vfGeoCoords, 0, localCoords, 0, 1);

                    tmp = vfGeoCoords[0];
                    vfGeoCoords[0] = vfGeoCoords[1];
                    vfGeoCoords[1] = tmp;
                } else
                    geoTransform.transform(vfGeoCoords, 0, localCoords, 0, 1);
            } catch(TransformException te) {
                errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
            }
        }

        hasChanged[FIELD_GEO_COORDS] = true;
        fireFieldChanged(FIELD_GEO_COORDS);
    }

    /**
     * Get the pointer to the local converted form of the geo coords. This
     * doesn't change over time, so the caller can fetch once and hold the
     * reference.
     */
    public double[] getConvertedCoordRef() {
        return localCoords;
    }
}
