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

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;


// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.geospatial.GTTransformUtils;
import org.web3d.vrml.renderer.common.nodes.BaseGeometricPropertyNode;

/**
 * Common base implementation of a Coordinate node.
 * <p>
 * Points are held internally as a flat array of values. The point list
 * returned will always be flat. We do this because renderers like point values
 * as a single flat array.
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public abstract class BaseGeoCoordinate extends BaseGeometricPropertyNode
    implements VRMLCoordinateNodeType {

    /** Field Index */
    protected static final int FIELD_POINT = LAST_NODE_INDEX + 1;

    /** Index of the geoOrigin field */
    protected static final int FIELD_GEO_ORIGIN = LAST_NODE_INDEX + 2;

    /** Index of the geoSystem field */
    protected static final int FIELD_GEO_SYSTEM = LAST_NODE_INDEX + 3;

    /** The last field index used by this class */
    protected static final int LAST_COORDINATE_INDEX = FIELD_GEO_SYSTEM;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_COORDINATE_INDEX + 1;

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
        "Unable to transform the coordinate values for some reason.";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** exposedField MFVec3f */
    protected double[] vfPoint;

    /** actual length of vfPoint */
    protected int numPoint;

    /** field MFString geoSystem ["GD","WE"] */
    protected String[] vfGeoSystem;

    /** Proto version of the geoOrigin */
    protected VRMLProtoInstance pGeoOrigin;

    /** field SFNode geoOrigin */
    protected VRMLNodeType vfGeoOrigin;

    /** The converted floating point array */
    private float[] renderPoints;

    /**
     * The calculated local version of the points taking into account both the
     * projection information and the GeoOrigin setting.
     */
    protected double[] localCoords;

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

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        nodeFields = new int[] { FIELD_METADATA, FIELD_GEO_ORIGIN };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFVec3d",
                                     "point");
        fieldDecl[FIELD_GEO_SYSTEM] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "geoSystem");
        fieldDecl[FIELD_GEO_ORIGIN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "geoOrigin");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_POINT);
        fieldMap.put("point", idx);
        fieldMap.put("set_point", idx);
        fieldMap.put("point_changed", idx);

        fieldMap.put("geoSystem", new Integer(FIELD_GEO_SYSTEM));
        fieldMap.put("geoOrigin", new Integer(FIELD_GEO_ORIGIN));
    }

    /**
     * Empty constructor
     */
    public BaseGeoCoordinate() {
        super("GeoCoordinate");

        vfPoint = FieldConstants.EMPTY_MFDOUBLE;
        localCoords = FieldConstants.EMPTY_MFDOUBLE;

        vfGeoSystem = new String[] {"GD","WE"};

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseGeoCoordinate(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("point");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfPoint = new double[field.numElements * 3];
                System.arraycopy(field.floatArrayValue, 0, vfPoint, 0,
                                 field.numElements * 3);

                numPoint = field.numElements * 3;
            }

            index = node.getFieldIndex("geoSystem");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfGeoSystem = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfGeoSystem, 0,
                    field.numElements);
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLCoordinateNodeType
    //-------------------------------------------------------------

    /**
     * Set a new set of values for the point field, as floats. Auto converts
     * up to doubles.
     *
     * @param newPoint New value for the point field
     * @param numValid The number of valid values to copy from the array
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setPoint(float[] newPoint, int numValid) {
        if(numValid > vfPoint.length)
            vfPoint = new double[numValid];

        numPoint = numValid;

        // Auto up-cast that a System.arraycopy would barf at
        for(int i = 0; i < numValid; i++)
            vfPoint[i] = newPoint[i];

        if(inSetup)
            return;

        // We have to send the new value here because it will be the
        // correct length.
        if(geoTransform != null) {
            if(renderPoints == null || renderPoints.length < numValid)
                renderPoints = new float[numValid];

            try {
                // go straight to floats without using the double version
                // since we need floats anyway for rendering.
                if(geoCoordSwap) {
                    // temporarily reverse the values in vfPoint, then swap
                    // them back after doing the coordinate coversion.
                    for(int i = 0; i < numValid; i+= 3) {
                        float tmp = newPoint[i];
                        newPoint[i] = newPoint[i + 1];
                        newPoint[i + 1] = tmp;
                    }

                    geoTransform.transform(newPoint,
                                           0,
                                           renderPoints,
                                           0,
                                           numValid / 3);

                    for(int i = 0; i < numValid; i+= 3) {
                        float tmp = newPoint[i + 1];
                        newPoint[i + 1] = newPoint[i];
                        newPoint[i] = tmp;
                    }
                } else {
                    geoTransform.transform(newPoint,
                                           0,
                                           renderPoints,
                                           0,
                                           numValid / 3);
                }
            } catch(TransformException te) {
                errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
            }
        }

        fireComponentChanged(FIELD_POINT);

        hasChanged[FIELD_POINT] = true;
        fireFieldChanged(FIELD_POINT);
    }


    /**
     * Get the number of items in the point array now. The number returned is
     * the total number of values in the flat array. This will allow the caller
     * to construct the correct size array for the getPoint() call.
     *
     * @return The number of values in the array
     */
    public int getNumPoints() {
        return numPoint;
    }

    /**
     * Get the current value of field point while down casting it to a float.
     * Point is an array of Vec3f float triples. Don't call if there are no
     * points in the array.
     *
     * @param points The array to copy the values into
     */
    public void getPoint(float[] points) {
        for(int i = 0; i < numPoint; i++)
            points[i] = (float)vfPoint[i];
    }

    /**
     * Get the internal reference to the raw or converted point array. Some
     * of the concrete node types end up needing to convert the point values
     * from double precision to single precision or needing to make geo-spatial
     * projections. This is a reference to the post-processed data that may be
     * directly used for rendering. In the case of CoordinateDouble, then the
     * array may be a set of down-cast values to floats.
     * <p>
     * Note that the array may well be longer than the actual number of valid
     * coordinates. Use {@link #getNumPoints()} to determine the number of
     * valid entries.
     *
     * @return An array of float[] values for rendering process
     */
    public float[] getPointRef() {
        return renderPoints;
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

        if(pGeoOrigin != null)
            pGeoOrigin.setupFinished();
        else if(vfGeoOrigin != null)
            vfGeoOrigin.setupFinished();

        // Fetch the geo transform and shift the first set of points
        try {
            GTTransformUtils gtu = GTTransformUtils.getInstance();
            boolean[] swap = new boolean[1];

            geoTransform = gtu.createSystemTransform(vfGeoSystem, swap);
            geoCoordSwap = swap[0];

            if(geoCoordSwap) {
                // temporarily reverse the values in vfPoint, then swap them back
                // after doing the coordinate coversion.
                for(int i = 0; i < numPoint; i+= 3) {
                    double tmp = vfPoint[i];
                    vfPoint[i] = vfPoint[i + 1];
                    vfPoint[i + 1] = tmp;
                }

                geoTransform.transform(vfPoint, 0, localCoords, 0, numPoint / 3);

                for(int i = 0; i < numPoint; i+= 3) {
                    double tmp = vfPoint[i + 1];
                    vfPoint[i + 1] = vfPoint[i];
                    vfPoint[i] = tmp;
                }
            } else {
                geoTransform.transform(vfPoint, 0, localCoords, 0, numPoint / 3);
            }

            if(renderPoints == null || renderPoints.length < numPoint)
                renderPoints = new float[numPoint];

            // now generate float versions of each for rendering
            if(vfGeoOrigin == null) {
                for(int i = 0; i < numPoint / 3; i++) {
                    renderPoints[i * 3] = (float)localCoords[i * 3];
                    renderPoints[i * 3 + 1] = (float)localCoords[i * 3 + 1];
                    renderPoints[i * 3 + 2] = (float)localCoords[i * 3 + 2];
                }
            } else {
                double[] pos = ((BaseGeoOrigin)vfGeoOrigin).getConvertedCoordRef();
                for(int i = 0; i < numPoint / 3; i++) {
                    renderPoints[i * 3] = (float)(localCoords[i * 3] - pos[0]);
                    renderPoints[i * 3 + 1] = (float)(localCoords[i * 3 + 1] - pos[1]);
                    renderPoints[i * 3 + 2] = (float)(localCoords[i * 3 + 2] - pos[2]);
                }
            }

            if(isStatic)
                localCoords = null;

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
        Integer index = (Integer)fieldMap.get(fieldName);

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
        if (index < 0  || index > LAST_COORDINATE_INDEX)
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.CoordinateNodeType;
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
            case FIELD_POINT:
                fieldData.clear();
                fieldData.doubleArrayValue = vfPoint;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = numPoint / 3;
                break;

            case FIELD_GEO_ORIGIN:
                fieldData.clear();
                if (pGeoOrigin != null)
                    fieldData.nodeValue = pGeoOrigin;
                else
                    fieldData.nodeValue = vfGeoOrigin;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_GEO_SYSTEM:
                fieldData.clear();
                fieldData.stringArrayValue = vfGeoSystem;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfGeoSystem.length;
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
                case FIELD_POINT :
                    destNode.setValue(destIndex, vfPoint, numPoint);
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
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     */
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_POINT:
                setPoint(value, numValid);
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
    // Local methods
    //----------------------------------------------------------

    /**
     * Set a new set of values for the point field, as floats. Auto converts
     * up to doubles.
     *
     * @param newPoint New value for the point field
     * @param numValid The number of valid values to copy from the array
     * @throws ArrayIndexOutOfBoundsException
     */
    private void setPoint(double[] newPoint, int numValid) {
        if(numValid > vfPoint.length) {
            vfPoint = new double[numValid];
            localCoords = new double[numValid];
        }

        numPoint = numValid;
        System.arraycopy(newPoint, 0, vfPoint, 0, numPoint);

        // We have to send the new value here because it will be the
        // correct length.
        if(inSetup)
            return;

        if(geoTransform != null) {
            try {
                if(geoCoordSwap) {
                    // temporarily reverse the values in vfPoint, then swap
                    // them back after doing the coordinate coversion.
                    for(int i = 0; i < numPoint; i+= 3) {
                        double tmp = vfPoint[i];
                        vfPoint[i] = vfPoint[i + 1];
                        vfPoint[i + 1] = tmp;
                    }

                    geoTransform.transform(vfPoint,
                                           0,
                                           localCoords,
                                           0,
                                           numPoint / 3);

                    for(int i = 0; i < numPoint; i+= 3) {
                        double tmp = vfPoint[i + 1];
                        vfPoint[i + 1] = vfPoint[i];
                        vfPoint[i] = tmp;
                    }
                } else {
                    geoTransform.transform(vfPoint,
                                           0,
                                           localCoords,
                                           0,
                                           numPoint / 3);
                }

                if(renderPoints == null || renderPoints.length < numValid)
                    renderPoints = new float[numValid];

                // now generate float versions of each for rendering
                if(vfGeoOrigin == null) {
                    for(int i = 0; i < numPoint / 3; i++) {
                        renderPoints[i * 3] = (float)localCoords[i * 3];
                        renderPoints[i * 3 + 1] = (float)localCoords[i * 3 + 1];
                        renderPoints[i * 3 + 2] = (float)localCoords[i * 3 + 2];
                    }
                } else {
                    double[] pos = ((BaseGeoOrigin)vfGeoOrigin).getConvertedCoordRef();

                    for(int i = 0; i < numPoint / 3; i++) {
                        renderPoints[i * 3] = (float)(localCoords[i * 3] - pos[0]);
                        renderPoints[i * 3 + 1] = (float)(localCoords[i * 3 + 1] - pos[1]);
                        renderPoints[i * 3 + 2] = (float)(localCoords[i * 3 + 2] - pos[2]);
                    }
                }
            } catch(TransformException te) {
                errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
            }
        }

        fireComponentChanged(FIELD_POINT);

        hasChanged[FIELD_POINT] = true;
        fireFieldChanged(FIELD_POINT);
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
}
