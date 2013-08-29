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

package org.web3d.vrml.renderer.common.nodes;

// External imports
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldAccessException;
import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.VRMLException;

/**
 * An abstract implementation of any node that uses component nodes to provide
 * coordinate, normal and texture information.
 * <p>
 * This includes normal and texture indexes.  These may not be needed by some
 * implementations like IndexedLineSet.  Just don't define the fields in the
 * static setup.
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.15 $
 */
public abstract class BaseIndexedGeometryNode
    extends BaseComponentGeometryNode {

    /** Index of the colorIndex MFInt32 field */
    protected static final int FIELD_COLORINDEX = LAST_GEOMETRY_INDEX + 1;

    /** Index of the set_colorIndex MFInt32 eventIn */
    protected static final int FIELD_SET_COLORINDEX = LAST_GEOMETRY_INDEX + 2;

    /** Index of the coordIndex SFInt32 field */
    protected static final int FIELD_COORDINDEX = LAST_GEOMETRY_INDEX + 3;

    /** Index of the set_coordIndex SFInt32 eventIn */
    protected static final int FIELD_SET_COORDINDEX = LAST_GEOMETRY_INDEX + 4;

    /** Index of the texCoordIndex SFInt32 field */
    protected static final int FIELD_TEXCOORDINDEX = LAST_GEOMETRY_INDEX + 5;

    /** Index of the set_texCoordIndex SFInt32 field */
    protected static final int FIELD_SET_TEXCOORDINDEX = LAST_GEOMETRY_INDEX + 6;

    /** Index of the normalIndex SFInt32 field */
    protected static final int FIELD_NORMALINDEX = LAST_GEOMETRY_INDEX + 7;

    /** Index of the set_normalIndex SFInt32 eventIn */
    protected static final int FIELD_SET_NORMALINDEX = LAST_GEOMETRY_INDEX + 8;

    /** The last field index used by this class */
    protected static final int LAST_INDEXEDGEOMETRY_INDEX = FIELD_SET_NORMALINDEX;

    /** Message for when the proto is not a Coord */
    protected static final String COORD_PROTO_MSG =
        "Proto does not describe a Coord object";

    /** Message for when the node in setValue() is not a Coord */
    protected static final String COORD_NODE_MSG =
        "Node does not describe a Coord object";

    /** Message for when the proto is not a Color */
    protected static final String COLOR_PROTO_MSG =
        "Proto does not describe a Color object";

    /** Message for when the node in setValue() is not a Color */
    protected static final String COLOR_NODE_MSG =
        "Node does not describe a Color object";

    /** Message for when the proto is not a Normal */
    protected static final String NORMAL_PROTO_MSG =
        "Proto does not describe a Normal object";

    /** Message for when the node in setValue() is not a Normal */
    protected static final String NORMAL_NODE_MSG =
        "Node does not describe a Normal object";

    /** Message for when the proto is not a TexCoord */
    protected static final String TEXCOORD_PROTO_MSG =
        "Proto does not describe a TexCoord object";

    /** Message for when the node in setValue() is not a TexCoord */
    protected static final String TEXCOORD_NODE_MSG =
        "Node does not describe a TexCoord object";


    /** field MFInt32 colorIndex */
    protected int[] vfColorIndex;

    /** Number of valid values in vfColorIndex */
    protected int numColorIndex;

    /** field MFInt32 coordIndex */
    protected int[] vfCoordIndex;

    /** Number of valid values in vfColorIndex */
    protected int numCoordIndex;

    /** field MFInt32 normalIndex */
    protected int[] vfNormalIndex;

    /** Number of valid values in vfColorIndex */
    protected int numNormalIndex;

    /** field MFInt32 texCoordIndex */
    protected int[] vfTexCoordIndex;

    /** Number of valid values in vfColorIndex */
    protected int numTexCoordIndex;

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     *
     * @param name The name of the type of node
     */
    protected BaseIndexedGeometryNode(String name) {
        super(name);

        vfCoordIndex = FieldConstants.EMPTY_MFINT32;
        numCoordIndex = 0;
        vfColorIndex = FieldConstants.EMPTY_MFINT32;
        numColorIndex = 0;
        vfNormalIndex = FieldConstants.EMPTY_MFINT32;
        numNormalIndex = 0;
        vfTexCoordIndex = FieldConstants.EMPTY_MFINT32;
        numTexCoordIndex = 0;

        changeFlags = 0;
    }

    /**
     * Set the fields of the binadble node that has the fields set
     * based on the fields of the passed in node. This directly copies the
     * bind state, so could cause some interesting problems. Not sure what
     * we should do with this currently.
     *
     * @param node The bindable node to copy info from
     */
    protected void copy(VRMLComponentGeometryNodeType node) {

        super.copy(node);

        try {
            int index = node.getFieldIndex("colorIndex");
            VRMLFieldData field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfColorIndex = new int[field.numElements];
                numColorIndex = field.numElements;
                System.arraycopy(field.intArrayValue,
                                 0,
                                 vfColorIndex,
                                 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("coordIndex");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfCoordIndex = new int[field.numElements];
                numCoordIndex = field.numElements;
                System.arraycopy(field.intArrayValue,
                                 0,
                                 vfCoordIndex,
                                 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("normalIndex");

            if (index >= 0) {
                field = node.getFieldValue(index);

                if(field.numElements != 0) {
                    vfNormalIndex = new int[field.numElements];
                    numNormalIndex = field.numElements;
                    System.arraycopy(field.intArrayValue,
                                     0,
                                     vfNormalIndex,
                                     0,
                                     field.numElements);
                }
            }

            index = node.getFieldIndex("texCoordIndex");
            if (index >= 0) {
                field = node.getFieldValue(index);

                if(field.numElements != 0) {
                    vfTexCoordIndex = new int[field.numElements];
                    numTexCoordIndex = field.numElements;
                    System.arraycopy(field.intArrayValue,
                                     0,
                                     vfTexCoordIndex,
                                     0,
                                     field.numElements);
                }
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_COORDINDEX:
                fieldData.clear();
                fieldData.intArrayValue = vfCoordIndex;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = numCoordIndex;
                break;

            case FIELD_COLORINDEX:
                fieldData.clear();
                fieldData.intArrayValue = vfColorIndex;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = numColorIndex;
                break;

            case FIELD_NORMALINDEX:
                fieldData.clear();
                fieldData.intArrayValue = vfNormalIndex;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = numNormalIndex;
                break;

            case FIELD_TEXCOORDINDEX:
                fieldData.clear();
                fieldData.intArrayValue = vfTexCoordIndex;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = numTexCoordIndex;
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

        if(srcIndex <= LAST_GEOMETRY_INDEX) {
            super.sendRoute(time, srcIndex, destNode, destIndex);
            return;
        }

        switch(srcIndex) {
            case FIELD_COLORINDEX:
            case FIELD_COORDINDEX:
            case FIELD_NORMALINDEX:
            case FIELD_TEXCOORDINDEX:
                System.err.println("These are fields. Can't be routed");
                break;

            default:
                super.sendRoute(time, srcIndex, destNode, destIndex);
        }
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        // correct the parameter if is zero length. The code assumes that
        // a field that is not set will be a null value, rather than a
        // zero length array. We may somehow end up with a zero length
        // array here as we can't trust the sender, so just make sure
        // everything is consistent.
        if(value != null && numValid == 0)
            value = null;

        if(!inSetup) {
            switch(index) {
                case FIELD_SET_COORDINDEX:
                    vfCoordIndex = value;
                    numCoordIndex = numValid;
                    changeFlags |= COORDS_INDEX_CHANGED;
                    break;

                case FIELD_SET_COLORINDEX:
                    vfColorIndex = value;
                    numColorIndex = numValid;
                    changeFlags |= COLORS_INDEX_CHANGED;
                    break;

                case FIELD_SET_NORMALINDEX:
                    vfNormalIndex = value;
                    numNormalIndex = numValid;
                    changeFlags |= NORMALS_INDEX_CHANGED;
                    break;

                case FIELD_SET_TEXCOORDINDEX:
                    vfTexCoordIndex = value;
                    numTexCoordIndex = numValid;
                    changeFlags |= TEXCOORDS_INDEX_CHANGED;
                    break;

                case FIELD_COORDINDEX:
                case FIELD_COLORINDEX:
                case FIELD_NORMALINDEX:
                case FIELD_TEXCOORDINDEX:
                    throw new InvalidFieldAccessException("Attempting to write " +
                        "an initializeOnly field at runtime");

                default:
                    super.setValue(index, value, numValid);
            }

            stateManager.addEndOfThisFrameListener(this);
        } else {
            switch(index) {
                case FIELD_COORDINDEX :
                    vfCoordIndex = value;
                    numCoordIndex = numValid;
                    break;

                case FIELD_COLORINDEX :
                    vfColorIndex = value;
                    numColorIndex = numValid;
                    break;

                case FIELD_NORMALINDEX :
                    vfNormalIndex = value;
                    numNormalIndex = numValid;
                    break;

                case FIELD_TEXCOORDINDEX :
                    vfTexCoordIndex = value;
                    numTexCoordIndex = numValid;
                    break;

                case FIELD_SET_COORDINDEX :
                case FIELD_SET_COLORINDEX :
                case FIELD_SET_NORMALINDEX :
                case FIELD_SET_TEXCOORDINDEX :
                    throw new InvalidFieldAccessException("Attempting to write " +
                        "an initializeOnly at setup time");

                default :
                    super.setValue(index, value, numValid);
            }
        }
    }

    //----------------------------------------------------------
    // Local public methods
    //----------------------------------------------------------

    /**
     * Set the coordIndex field. Override to provide.renderer-specific behaviour,
     * but remember to also call this implementation too.
     *
     * @param value The list of index values to use
     * @param numValid The number of valid indices in the array
     */
    protected void setCoordIndex(int[] value, int numValid) {
        vfCoordIndex = value;
        numCoordIndex = numValid;
        changeFlags |= COORDS_INDEX_CHANGED;
    }

    /**
     * Set the normalIndex field. Override to provide.renderer-specific behaviour,
     * but remember to also call this implementation too.
     *
     * @param value The list of index values to use
     * @param numValid The number of valid indices in the array
     */
    protected void setNormalIndex(int[] value, int numValid) {
        vfNormalIndex = value;
        numNormalIndex = numValid;
        changeFlags |= NORMALS_INDEX_CHANGED;
    }

    /**
     * Set the colorIndex field. Override to provide.renderer-specific behaviour,
     * but remember to also call this implementation too.
     *
     * @param value The list of index values to use
     * @param numValid The number of valid indices in the array
     */
    protected void setColorIndex(int[] value, int numValid) {
        vfColorIndex = value;
        numColorIndex = numValid;
        changeFlags |= COLORS_INDEX_CHANGED;
    }

    /**
     * Set the texCoordIndex field. Override to provide.renderer-specific behaviour,
     * but remember to also call this implementation too.
     *
     * @param value The list of index values to use
     * @param numValid The number of valid indices in the array
     */
    protected void setTexCoordIndex(int[] value, int numValid) {
        vfTexCoordIndex = value;
        numTexCoordIndex = numValid;
        changeFlags |= TEXCOORDS_INDEX_CHANGED;
    }
}
