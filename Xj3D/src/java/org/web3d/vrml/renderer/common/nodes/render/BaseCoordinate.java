/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.render;

// External imports
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.util.ArrayUtils;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLCoordinateNodeType;
import org.web3d.vrml.renderer.common.nodes.BaseGeometricPropertyNode;

/**
 * Common base implementation of a Coordinate node.
 * <p>
 * Points are held internally as a flat array of values. The point list
 * returned will always be flat. We do this because renderers like point values
 * as a single flat array. The array returned will always contain exactly the
 * number of points specified.
 * <p>
 * The effect of this is that point values may be routed out of this node as
 * a flat array of points rather than a 2D array. Receiving nodes should check
 * for this version as well. This implementation will handle being routed
 * either form.
 *
 * @author Alan Hudson
 * @version $Revision: 1.17 $
 */
public abstract class BaseCoordinate extends BaseGeometricPropertyNode
    implements VRMLCoordinateNodeType {

    /** Index of the point field */
    protected static final int FIELD_POINT = LAST_NODE_INDEX + 1;

    /** The last field index used by this class */
    protected static final int LAST_COORDINATE_INDEX = FIELD_POINT;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_COORDINATE_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** exposedField MFVec3f */
    protected float[] vfPoint;

    /** actual length of vfPoint */
    protected int numPoint;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFVec3f",
                                     "point");
        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_POINT);
        fieldMap.put("point", idx);
        fieldMap.put("set_point", idx);
        fieldMap.put("point_changed", idx);
    }

    /**
     * Construct a default instance of this node.
     */
    public BaseCoordinate() {
        super("Coordinate");

        vfPoint = FieldConstants.EMPTY_MFFLOAT;
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
    public BaseCoordinate(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("point");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfPoint = new float[field.numElements * 3];
                System.arraycopy(field.floatArrayValue, 0, vfPoint, 0,
                                 field.numElements * 3);

                numPoint = field.numElements * 3;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLCoordinateNodeType
    //-------------------------------------------------------------

    /**
     * Accessor method to set a new value for field attribute <b>point</b>
     * point is an array of Vec2f doubles
     *
     * @param newPoint New value for the point field
     * @param numValid The number of valid values to copy from the array
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setPoint(float[] newPoint, int numValid) {
        if(numValid > vfPoint.length)
            vfPoint = new float[numValid];

        numPoint = numValid;
        System.arraycopy(newPoint,0, vfPoint, 0, numPoint);

        // We have to send the new value here because it will be the
        // correct length.
        if(!inSetup) {
            fireComponentChanged(FIELD_POINT);

            hasChanged[FIELD_POINT] = true;
            fireFieldChanged(FIELD_POINT);
        }
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
     * Accessor method to get current value of field point.
     * Point is an array of Vec3f float triples. Don't call if there are no
     * points in the array.
     *
     * @param points The array to copy the values into
     */
    public void getPoint(float[] points) {
        if(numPoint != 0)
            System.arraycopy(vfPoint, 0, points, 0, numPoint);
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
        return vfPoint;
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
                fieldData.floatArrayValue = vfPoint;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numPoint / 3;
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
                case FIELD_POINT:
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
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_POINT:
                setPoint(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }
}
