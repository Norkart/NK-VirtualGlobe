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
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;
import org.web3d.vrml.renderer.common.geospatial.GTTransformUtils;

/**
 * Common implementation of a GeoLocation node functionality.
 * <p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.15 $
 */
public class BaseGeoLocation extends BaseGroupingNode {

    /** Index of the geoOrigin field */
    protected static final int FIELD_GEO_ORIGIN = LAST_GROUP_INDEX + 1;

    /** Index of the geoSystem field */
    protected static final int FIELD_GEO_SYSTEM = LAST_GROUP_INDEX + 2;

    /** Index of the geoCoords field */
    protected static final int FIELD_GEO_COORDS = LAST_GROUP_INDEX + 3;

    /** The last field index used by this class */
    protected static final int LAST_GEOLOCATION_INDEX = FIELD_GEO_COORDS;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_GEOLOCATION_INDEX + 1;

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

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations
    /** field MFString geoSystem ["GD","WE"] */
    protected String[] vfGeoSystem;

    /** field SFVec3d geoGridOrigin 0 0 0 */
    protected double[] vfGeoCoords;

    /** Proto version of the geoOrigin */
    protected VRMLProtoInstance pGeoOrigin;

    /** field SFNode geoOrigin */
    protected VRMLNodeType vfGeoOrigin;

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
        nodeFields = new int[] {
            FIELD_CHILDREN,
            FIELD_METADATA,
            FIELD_GEO_ORIGIN
        };

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
        fieldDecl[FIELD_GEO_SYSTEM] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "geoSystem");
        fieldDecl[FIELD_GEO_ORIGIN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "geoOrigin");
        fieldDecl[FIELD_GEO_COORDS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3d",
                                     "geoCoords");

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

        idx = new Integer(FIELD_GEO_COORDS);
        fieldMap.put("geoCoords", idx);
        fieldMap.put("set_geoCoords", idx);
        fieldMap.put("geoCoords_changed", idx);

        fieldMap.put("geoSystem", new Integer(FIELD_GEO_SYSTEM));
        fieldMap.put("geoOrigin", new Integer(FIELD_GEO_ORIGIN));
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * X3D specification.
     */
    public BaseGeoLocation() {
        super("GeoLocation");

        hasChanged = new boolean[NUM_FIELDS];

        vfGeoSystem = new String[] {"GD","WE"};
        vfGeoCoords = new double[3];
        localCoords = new double[3];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseGeoLocation(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("geoCoords");
            VRMLFieldData field = node.getFieldValue(index);

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
//            geoCoordSwap = false;

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

            if(vfGeoOrigin != null) {
                double[] pos = ((BaseGeoOrigin)vfGeoOrigin).getConvertedCoordRef();
                localCoords[0] -= pos[0];
                localCoords[1] -= pos[1];
                localCoords[2] -= pos[2];
            }

        } catch(FactoryException fe) {
            errorReporter.errorReport(FACTORY_ERR_MSG, fe);
        } catch(TransformException te) {
            errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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
        return (index < 0 || index > LAST_GEOLOCATION_INDEX) ?
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
            case FIELD_GEO_COORDS:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.doubleArrayValue = vfGeoCoords;
                break;

            case FIELD_GEO_ORIGIN:
                fieldData.clear();
                if(pGeoOrigin != null)
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
                case FIELD_GEO_COORDS:
                    destNode.setValue(destIndex, vfGeoCoords, vfGeoCoords.length);
                    break;

                default:
                    super.sendRoute(time,srcIndex,destNode,destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseGeoLocation.sendRoute: No field!" + srcIndex);
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

                if(vfGeoOrigin != null) {
                    double[] pos = ((BaseGeoOrigin)vfGeoOrigin).getConvertedCoordRef();
                    localCoords[0] -= pos[0];
                    localCoords[1] -= pos[1];
                    localCoords[2] -= pos[2];
                }
            } catch(TransformException te) {
                errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
            }
        }

        hasChanged[FIELD_GEO_COORDS] = true;
        fireFieldChanged(FIELD_GEO_COORDS);
    }
}
