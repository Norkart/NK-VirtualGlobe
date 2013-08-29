/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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

import org.web3d.vrml.renderer.common.nodes.BaseSensorNode;
import org.web3d.vrml.renderer.common.geospatial.GTTransformUtils;

/**
 * Common base implementation of a GeoTouchSensor node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class BaseGeoTouchSensor extends BaseSensorNode
    implements VRMLTouchSensorNodeType {

    // Field index constants

    /** The field index for hitNormal_Changed */
    protected static final int FIELD_HITNORMAL_CHANGED = LAST_SENSOR_INDEX + 1;

    /** The field index for hitPoint_Changed */
    protected static final int FIELD_HITPOINT_CHANGED = LAST_SENSOR_INDEX + 2;

    /** The field index for hitTexCoord_Changed */
    protected static final int FIELD_HITTEXCOORD_CHANGED = LAST_SENSOR_INDEX + 3;

    /** The field index for hitGeoCoord_Changed */
    protected static final int FIELD_HITGEOCOORD_CHANGED = LAST_SENSOR_INDEX + 4;

    /** The field index for isOver  */
    protected static final int FIELD_IS_OVER = LAST_SENSOR_INDEX + 5;

    /** The field index for touchTime */
    protected static final int FIELD_TOUCH_TIME = LAST_SENSOR_INDEX + 6;

    /** The field index for description */
    protected static final int FIELD_DESCRIPTION = LAST_SENSOR_INDEX + 7;

    /** Index of the geoOrigin field */
    protected static final int FIELD_GEO_ORIGIN = LAST_SENSOR_INDEX + 8;

    /** Index of the geoSystem field */
    protected static final int FIELD_GEO_SYSTEM = LAST_SENSOR_INDEX + 9;

    /** The last field index used by this class */
    protected static final int LAST_TOUCHSENSOR_INDEX = FIELD_GEO_SYSTEM;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_TOUCHSENSOR_INDEX + 1;

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

    // The VRML field values

    /** The value of the hitNormal_changed field */
    protected float[] vfHitNormalChanged;

    /** The value of the hitPoint_changed field*/
    protected float[] vfHitPointChanged;

    /** The value of the hitTexCoord_changed field */
    protected float[] vfHitTexCoordChanged;

    /** The value of the hitGeoCoord_changed field */
    protected double[] vfHitGeoCoordChanged;

    /** The value of the isOver field */
    protected boolean vfIsOver;

    /** The value of the touchTime field */
    protected double vfTouchTime;

    /** The value of the description field */
    protected String vfDescription;

    /** field MFString geoSystem ["GD","WE"] */
    protected String[] vfGeoSystem;

    /** Proto version of the geoOrigin */
    protected VRMLProtoInstance pGeoOrigin;

    /** field SFNode geoOrigin */
    protected VRMLNodeType vfGeoOrigin;

    /**
     * The calculated local version of the points taking into account both the
     * projection information and the GeoOrigin setting.
     */
    private double[] localCoords;

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
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_GEO_ORIGIN
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_DESCRIPTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "description");
        fieldDecl[FIELD_HITNORMAL_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec3f",
                                     "hitNormal_changed");
        fieldDecl[FIELD_HITPOINT_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec3f",
                                     "hitPoint_changed");
        fieldDecl[FIELD_HITTEXCOORD_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec2f",
                                     "hitTexCoord_changed");
        fieldDecl[FIELD_HITGEOCOORD_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec3d",
                                     "hitGeoCoord_changed");
        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");
        fieldDecl[FIELD_IS_OVER] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isOver");
        fieldDecl[FIELD_TOUCH_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "touchTime");
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

        idx = new Integer(FIELD_DESCRIPTION);
        fieldMap.put("description", idx);
        fieldMap.put("set_description", idx);
        fieldMap.put("description_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        fieldMap.put("hitNormal_changed",new Integer(FIELD_HITNORMAL_CHANGED));
        fieldMap.put("hitPoint_changed",new Integer(FIELD_HITPOINT_CHANGED));
        fieldMap.put("hitTexCoord_changed",
            new Integer(FIELD_HITTEXCOORD_CHANGED));
        fieldMap.put("hitGeoCoord_changed",
            new Integer(FIELD_HITGEOCOORD_CHANGED));

        fieldMap.put("isActive",new Integer(FIELD_IS_ACTIVE));
        fieldMap.put("isOver",new Integer(FIELD_IS_OVER));
        fieldMap.put("touchTime",new Integer(FIELD_TOUCH_TIME));
        fieldMap.put("geoSystem", new Integer(FIELD_GEO_SYSTEM));
        fieldMap.put("geoOrigin", new Integer(FIELD_GEO_ORIGIN));
    }

    /**
     * Construct a new time sensor object
     */
    public BaseGeoTouchSensor() {
        super("GeoTouchSensor");

        hasChanged = new boolean[NUM_FIELDS];

        vfGeoSystem = new String[] {"GD","WE"};

        vfHitNormalChanged = new float[3];
        vfHitPointChanged = new float[3];
        vfHitTexCoordChanged = new float[2];
        vfHitGeoCoordChanged = new double[3];

        localCoords = new double[3];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseGeoTouchSensor(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLSensorNodeType)node);
    }

    //--------------------------------------------------------------------
    // Methods defined by VRMLPointingDeviceSensorNodeType
    //--------------------------------------------------------------------

    /**
     * Flag to notify the user whether the node implementation only needs the
     * hit point information, or it needs everything else as well. This is an
     * optimisation method that allows the internals of the event model to
     * avoid doing unnecessary work. If the return value is true, then the
     * hitNormal and hitTexCoord parameter values will not be supplied (they'll
     * be null references).
     *
     * @return true if the node implementation only requires hitPoint information
     */
    public boolean requiresPointOnly() {
        return false;
    }

    /**
     * Get the description to associate with the link. This is a line of text
     * suitable for mouseovers, status information etc. If there is no
     * description set then it returns null.
     *
     * @return The current description or null
     */
    public String getDescription() {
        return vfDescription;
    }

    /**
     * Set the description string for this link. Setting a value of null will
     * clear the current description.
     *
     * @param desc The new description to set
     */
    public void setDescription(String desc) {
        vfDescription = desc;

        if(!inSetup) {
            hasChanged[FIELD_DESCRIPTION] = true;
            fireFieldChanged(FIELD_DESCRIPTION);
        }
    }

    /**
     * Set the flag describing whether the pointing device is over this sensor.
     * The result should be that isOver SFBool output only field is set
     * appropriately at the node level.
     *
     * @param newIsOver The new value for isOver
     */
    public void setIsOver(boolean newIsOver) {
        vfIsOver = newIsOver;
        hasChanged[FIELD_IS_OVER] = true;
        fireFieldChanged(FIELD_IS_OVER);
    }

    /**
     * Get the current value of the isOver field.
     *
     * @return The current value of isOver
     */
    public boolean getIsOver() {
        return vfIsOver;
    }

    //--------------------------------------------------------------------
    // Methods defined by VRMLTouchSensorNodeType
    //--------------------------------------------------------------------

    /**
     * Notify the node that a button was pushed down
     *
     * @param button The button that was pressed
     * @param simTime The VRML simulation time it happened
     * @param hitPoint The location clicked in object space coordinates
     * @param hitNormal Surface normal vector at the intersection point
     * @param hitTexCoord The texture coordinate at the intersection point
     */
    public void notifyPressed(int button,
                              double simTime,
                              float[] hitPoint,
                              float[] hitNormal,
                              float[] hitTexCoord) {

        if(!vfEnabled)
            return;

        vfIsActive = true;
        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);
    }

    /**
     * Notify the node that a button was released
     *
     * @param button The button that was released
     * @param simTime The VRML simulation time it happened
     * @param hitPoint The location clicked in object space coordinates
     * @param hitNormal Surface normal vector at the intersection point
     * @param hitTexCoord The texture coordinate at the intersection point
     */
    public void notifyReleased(int button,
                               double simTime,
                               float[] hitPoint,
                               float[] hitNormal,
                               float[] hitTexCoord) {

        if(!vfEnabled)
            return;

        vfIsActive = false;
        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);

        if (vfIsOver) {
            vfTouchTime = simTime;
            hasChanged[FIELD_TOUCH_TIME] = true;
            fireFieldChanged(FIELD_TOUCH_TIME);
        }
    }

    /**
     * Notify the node that the device moved.
     *
     * @param hitPoint The current location in object space coordinates
     * @param hitNormal Surface normal vector at the intersection point
     * @param hitTexCoord The texture coordinate at the intersection point
     */
    public void notifyHitChanged(float[] hitPoint,
                                 float[] hitNormal,
                                 float[] hitTexCoord) {
        if(!vfEnabled)
            return;

        vfHitPointChanged[0] = hitPoint[0];
        vfHitPointChanged[1] = hitPoint[1];
        vfHitPointChanged[2] = hitPoint[2];

        vfHitNormalChanged[0] = hitNormal[0];
        vfHitNormalChanged[1] = hitNormal[1];
        vfHitNormalChanged[2] = hitNormal[2];

        vfHitTexCoordChanged[0] = hitTexCoord[0];
        vfHitTexCoordChanged[1] = hitTexCoord[1];

        try {
            localCoords[0] = hitPoint[0];
            localCoords[1] = hitPoint[1];
            localCoords[2] = hitPoint[2];

            if(vfGeoOrigin != null) {
                double[] pos = ((BaseGeoOrigin)vfGeoOrigin).getConvertedCoordRef();
                localCoords[0] += pos[0];
                localCoords[1] += pos[1];
                localCoords[2] += pos[2];
            }

            geoTransform.transform(localCoords, 0, vfHitGeoCoordChanged, 0, 1);

            // Swap the two output coords if required by the geoSystem.
            if(geoCoordSwap) {
                double tmp = vfHitGeoCoordChanged[0];
                vfHitGeoCoordChanged[0] = vfHitGeoCoordChanged[1];
                vfHitGeoCoordChanged[1] = tmp;
            }

        } catch(TransformException te) {
            errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
        }

        hasChanged[FIELD_HITPOINT_CHANGED] = true;
        fireFieldChanged(FIELD_HITPOINT_CHANGED);

        hasChanged[FIELD_HITNORMAL_CHANGED] = true;
        fireFieldChanged(FIELD_HITNORMAL_CHANGED);

        hasChanged[FIELD_HITTEXCOORD_CHANGED] = true;
        fireFieldChanged(FIELD_HITTEXCOORD_CHANGED);

        hasChanged[FIELD_HITGEOCOORD_CHANGED] = true;
        fireFieldChanged(FIELD_HITGEOCOORD_CHANGED);
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

        // Fetch the geo transform and then fetch the inverse. For the
        // GeoTouchSensor data is always going to be coming from the graphics
        // system - ie in floats. We need to then convert back to geospatial
        // reference frame, so need the reverse transform.
        try {
            GTTransformUtils gtu = GTTransformUtils.getInstance();

            boolean[] swap = new boolean[1];
            MathTransform forward_tx = gtu.createSystemTransform(vfGeoSystem, swap);
            geoCoordSwap = swap[0];
            geoTransform = forward_tx.inverse();
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
        if(index < 0  || index > LAST_TOUCHSENSOR_INDEX)
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
        return TypeConstants.PointingDeviceSensorNodeType;
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
            case FIELD_HITNORMAL_CHANGED:
                fieldData.clear();
                fieldData.floatArrayValue = vfHitNormalChanged;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_HITPOINT_CHANGED:
                fieldData.clear();
                fieldData.floatArrayValue = vfHitPointChanged;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_HITTEXCOORD_CHANGED:
                fieldData.clear();
                fieldData.floatArrayValue = vfHitTexCoordChanged;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_HITGEOCOORD_CHANGED:
                fieldData.clear();
                fieldData.doubleArrayValue = vfHitGeoCoordChanged;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = 1;
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

            case FIELD_IS_OVER:
                fieldData.clear();
                fieldData.booleanValue = vfIsOver;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_TOUCH_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfTouchTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_DESCRIPTION:
                fieldData.clear();
                fieldData.stringValue = vfDescription;
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
                case FIELD_HITNORMAL_CHANGED:
                    destNode.setValue(destIndex, vfHitNormalChanged, 3);
                    break;

                case FIELD_HITPOINT_CHANGED:
                    destNode.setValue(destIndex, vfHitPointChanged, 3);
                    break;

                case FIELD_HITGEOCOORD_CHANGED:
                    destNode.setValue(destIndex, vfHitGeoCoordChanged, 3);
                    break;

                case FIELD_HITTEXCOORD_CHANGED:
                    destNode.setValue(destIndex, vfHitTexCoordChanged, 2);
                    break;

                case FIELD_IS_OVER:
                    destNode.setValue(destIndex, vfIsOver);
                    break;

                case FIELD_TOUCH_TIME:
                    destNode.setValue(destIndex, vfTouchTime);
                    break;

                case FIELD_DESCRIPTION:
                    destNode.setValue(destIndex, vfDescription);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseTouchSensor.sendRoute: No field! " + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseTouchSensor.sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFString field "title".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_DESCRIPTION:
                setDescription(value);
                break;

            default:
                super.setValue(index, value);
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
}
