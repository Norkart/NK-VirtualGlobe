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

package org.web3d.vrml.renderer.j3d.nodes;

// External imports
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.media.j3d.Geometry;
import javax.media.j3d.IndexedLineArray;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

/**
 * Abstract Java3D implementation of a Indexed Geometry node.
 * <p>
 *
 *
 * @author Alan Hudson
 * @version $Revision: 1.18 $
 */
public abstract class J3DIndexedGeometry extends J3DGeometryNode
    implements VRMLComponentGeometryNodeType {

    /** Index of the color SFNode field */
    protected static final int FIELD_COLOR = LAST_NODE_INDEX + 1;

    /** Index of the coord SFNode field */
    protected static final int FIELD_COORD = LAST_NODE_INDEX + 2;

    /** Index of the colorIndex MFInt32 field */
    protected static final int FIELD_COLORINDEX = LAST_NODE_INDEX + 3;

    /** Index of the set_colorIndex MFInt32 eventIn */
    protected static final int FIELD_SET_COLORINDEX = LAST_NODE_INDEX + 4;

    /** Index of the colorPerVertex SFBool field */
    protected static final int FIELD_COLORPERVERTEX = LAST_NODE_INDEX + 5;

    /** Index of the the colorIndex SFInt32 field */
    protected static final int FIELD_COORDINDEX = LAST_NODE_INDEX + 6;

    /** Index of the the set_colorIndex SFInt32 eventIn */
    protected static final int FIELD_SET_COORDINDEX = LAST_NODE_INDEX + 7;

    /** Last index used by this class */
    protected static final int LAST_INDEXEDGEOMETRY_INDEX = FIELD_SET_COORDINDEX;

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

    /** Proto version of the color */
    protected VRMLProtoInstance pColor;

    /** exposedField SFNode color */
    protected VRMLColorNodeType vfColor;

    /** Proto version of the coord */
    protected VRMLProtoInstance pCoord;

    /** exposedField SFNode coord */
    protected VRMLCoordinateNodeType vfCoord;

    /** field MFInt32 colorIndex */
    protected int[] vfColorIndex;

    /** The number of valid values in vfColorIndex */
    protected int numColorIndex;

    /** field SFBool colorPerVertex TRUE */
    protected boolean vfColorPerVertex;

    /** field MFInt32 coordIndex */
    protected int[] vfCoordIndex;

    /** The number of valid values in vfColorIndex */
    protected int numCoordIndex;

    /** The number of elements defined in the coordIndex */
    protected int numPieces;

    /** The number of indices defined in the coordIndex */
    protected int numIndices;

    /** Are the elements all the same size */
    protected boolean isConstSize;

    /** What size are the elements */
    protected int constSize;

    // Copies of fields in a useful format
    protected float[] lfColor;
    protected float[] lfCoord;
    protected int[] lfColorIndex;
    protected int[] lfCoordIndex;
    protected int[] pieceSizes;

    /**
     * Construct a basic indexed geometry object with the given name and all
     * values set to defaults.
     *
     * @param name The name of the type of node
     */
    public J3DIndexedGeometry(String name) {
        super(name);

        vfColorIndex = FieldConstants.EMPTY_MFINT32;
        vfCoordIndex = FieldConstants.EMPTY_MFINT32;

        vfColorPerVertex = true;

        numPieces = 0;
        numIndices = 0;
        isConstSize = false;
        constSize = 0;
    }

    /**
     * Set the fields of the indexed geometry node that has the fields set
     * based on the fields of the passed in node.
     *
     * @param node The indexed geometry node to copy info from
     */
    protected void copy(VRMLComponentGeometryNodeType node) {
        try {
            int index = node.getFieldIndex("colorIndex");
            VRMLFieldData field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfColorIndex = new int[field.numElements];
                System.arraycopy(field.intArrayValue,
                                 0,
                                 vfColorIndex,
                                 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("coordIndex");
            field = node.getFieldValue(index);

            if (field.numElements != 0) {
               vfCoordIndex = new int[field.numElements];
               System.arraycopy(field.intArrayValue,
                                0,
                                vfCoordIndex,
                                0,
                                field.numElements);
            }

            index = node.getFieldIndex("colorPerVertex");
            field = node.getFieldValue(index);
            vfColorPerVertex = field.booleanValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType
    //----------------------------------------------------------

    /**
     * Specified whether this node has alpha values in the local colour
     * information. If so, then it will be used for to override the material's
     * transparency value.
     *
     * @return true when the local color value has inbuilt alpha
     */
    public boolean hasLocalColorAlpha() {
        return (vfColor != null) && (vfColor.getNumColorComponents() == 4);
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

        if(pCoord != null)
            pCoord.setupFinished();
        else if(vfCoord != null)
            vfCoord.setupFinished();

        if(pColor != null)
            pColor.setupFinished();
        else if(vfColor != null)
            vfColor.setupFinished();
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
            case FIELD_COLOR:
                fieldData.clear();
                fieldData.nodeValue = vfColor;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_COORD:
                fieldData.clear();
                fieldData.nodeValue = vfCoord;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_COLORINDEX:
                fieldData.clear();
                fieldData.intArrayValue = vfColorIndex;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = numColorIndex;
                break;

            case FIELD_COLORPERVERTEX:
                fieldData.clear();
                fieldData.booleanValue = vfColorPerVertex;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_COORDINDEX:
                fieldData.clear();
                fieldData.intArrayValue = vfCoordIndex;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = numCoordIndex;
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
                case FIELD_COLOR:
                    if(pColor != null)
                        destNode.setValue(destIndex, pColor);
                    else
                        destNode.setValue(destIndex, vfColor);
                    break;
                case FIELD_COORD:
                    if(pCoord != null)
                        destNode.setValue(destIndex, pCoord);
                    else
                        destNode.setValue(destIndex, vfCoord);
                    break;
                default:
                    System.err.println("J3DIndexedGeometry.sendRoute: No index: " + srcIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an int array. This would
     * be used to set MFint32 field types.
     *
     * @param index The index of destination field to set
     * @param value The raw value string to be parsed
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldFormatException The string was not in a correct form
     *    for this field.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException,
               InvalidFieldFormatException,
               InvalidFieldValueException {

        if(inSetup) {
            switch(index) {
                case FIELD_COORDINDEX :
                    vfCoordIndex = value;
                    numCoordIndex = numValid;
                    break;

                case FIELD_COLORINDEX :
                    vfColorIndex = value;
                    numColorIndex = numValid;
                    break;

                case FIELD_SET_COORDINDEX:
                    throw new InvalidFieldAccessException(
                        "set_coordIndex is write only");

                case FIELD_SET_COLORINDEX:
                    throw new InvalidFieldAccessException(
                        "set_colorIndex is write only");

                default:
                    super.setValue(index, value, numValid);
            }
        } else {
            switch(index) {
                case FIELD_SET_COORDINDEX:
                    vfCoordIndex = value;
                    numCoordIndex = numValid;
                    break;

                case FIELD_SET_COLORINDEX:
                    vfColorIndex = value;
                    numColorIndex = numValid;
                    break;

                case FIELD_COORDINDEX:
                    throw new InvalidFieldAccessException(
                        "coordIndex is initializeOnly");

                case FIELD_COLORINDEX:
                    throw new InvalidFieldAccessException(
                        "colorIndex is initializeOnly");

                default:
                    super.setValue(index, value, numValid);
            }
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        // Runtime semantics not yet implemented

        switch(index) {
            case FIELD_COLORPERVERTEX :
                if(!inSetup)
                    throw new InvalidFieldAccessException(
                        "colorPerVertex is initializeOnly");

                vfColorPerVertex = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Methods internal to J3DIndexedGeometry
    //----------------------------------------------------------

    /**
     * Count the numPieces and numIndices in the coordIndex list.
     */
   protected void countIndex() {
        boolean lastCoord = false;
        int curSize = 0;

        numPieces = 0;
        numIndices = 0;
        isConstSize = true;
        constSize = 0;

        if (vfCoordIndex == null)
            return;

        for(int i = 0; i < numCoordIndex; i++) {
            if(vfCoordIndex[i] == -1) {
                if (numPieces == 0)
                    constSize = curSize;
                else if (curSize != constSize) {
                    isConstSize = false;
                    constSize = curSize;
                }

                numPieces++;
                curSize = 0;
                lastCoord = false;
            } else {
                numIndices++;
                curSize++;
                lastCoord = true;
            }
        }

        if(lastCoord) {
            // coord list ended with a coord, finish off the last face
            if(numPieces == 0) {
                isConstSize = true;
                constSize = curSize;;
            } else if (curSize != constSize) {
                isConstSize = false;
            }

            numPieces++;
        }
    }

    /**
     * Fill the implSize array with the size of each prim and
     * the implIndex array with the indices for the prims.
     *
     * @param value The vrml specified index values
     * @param numValue The number of valid items in value
     * @param implSize An array to hold the sizes of each primitive
     * @param implIndex An array to hold the indices for each primitive
     */
    protected void fillImplArrays(int[] value,
                                  int numValue,
                                  int[] implSize,
                                  int[] implIndex) {
        int curPrim = 0;
        int curSize = 0;
        int curIndex = 0;;
        boolean lastValue = false;

        for(int i = 0; i < numValue; i++) {
            if(value[i] == -1) {
                implSize[curPrim++] = curSize;
                curSize = 0;
                lastValue = false;
            } else {
                implIndex[curIndex++] = value[i];
                curSize++;
                lastValue = true;
            }
        }

        if(lastValue) {
            // finish off the last face
            implSize[curPrim++] = curSize;
        }
    }

    /**
     * Fill a "subordinate" implIndex array.  Use the implSize array
     * from the coord index parse.  If the current face size does not
     * match the impl face size, try to manage as best we can.  Return
     * true if the face sizes matched, false if the data looked screwy
     *
     * @param value The vrml specified index values
     * @param numValue The number of valid items in value
     * @param implSize An array to hold the sizes of each primitive
     * @param implIndex An array to hold the indices for each primitive
     */
    protected boolean fillImplArraysTest(int[] value,
                                         int numValue,
                                         int[] implSize,
                                         int[] implIndex) {

        int curPrim = 0;
        int curSize = 0;
        int inIndex = 0;
        int outIndex = 0;
        int size = value.length;
        boolean dataOK = true;
        int useValue;

        while(outIndex < implIndex.length) {
            if(inIndex >= size) {
                useValue = value[size-1];
                dataOK = false;
                return false;
            } else {
                useValue = value[inIndex];
            }

            if(useValue == -1) {
                if(implSize[curPrim] != curSize)
                    dataOK = false;

                curPrim++;
                if(curPrim >= implSize.length) {
                    dataOK = false;
                    curPrim--;
                }
                curSize = 0;
            } else {
                implIndex[outIndex++] = useValue;
                if(curSize++ > implSize[curPrim])
                    dataOK = false;
            }
            inIndex++;
        }

        return dataOK;
    }

    /**
     * Setup the arrays required by the GeometryInfo class.
     *
     * @return The indices in proper format
     */
    protected int[] setupIndex(int[] srcIndex,
                               int numIndex,
                               boolean perVertex) {

        int[] list = srcIndex;
        int[] destIndex;

        if(perVertex == true) {
            if((list == null) || (numIndex == 0))
                list = vfCoordIndex;

            if(list == vfCoordIndex)
                destIndex = lfCoordIndex;
            else {
                destIndex = new int[numIndices];

                if(!fillImplArraysTest(list, numIndex, pieceSizes, destIndex))
                    System.out.println("Index does not match coordIndex");
            }
        } else {
            // Per facet
            if((numIndex > 0) && (numIndex != numPieces))
                System.out.println("Index size != num faces");

            // set up the indicies
            destIndex = new int[numIndices];
            int curIndex = 0;
            for(int curFace = 0; curFace < numPieces; curFace++) {
                for(int j = 0; j < pieceSizes[curFace]; j++) {
                    if(curFace < numIndex)
                        destIndex[curIndex++] = list[curFace];
                    else {
                        // this is the std defn for List == null
                        destIndex[curIndex++] = curFace;
                    }
                }
            }
        }

        return destIndex;
    }
}
