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

package org.web3d.vrml.renderer.common.nodes.texture;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.BaseGeometricPropertyNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Base implementation of a multi texture coordinate.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public abstract class BaseMultiTextureCoordinate extends BaseGeometricPropertyNode
  implements VRMLTextureCoordinateNodeType {

    /** Message for when the proto is not a TextureTransform */
    private static final String COORDINATE_PROTO_MSG =
        "Proto does not describe a TextureCoordinate object";

    /** Message for when the node in setValue() is not a TextureTransform */
    private static final String COORDINATE_NODE_MSG =
        "Node does not describe a TextureCoordinate object";

    /** Field index for texCoord */
    protected static final int FIELD_TEXCOORD = LAST_NODE_INDEX + 1;

    /** ID of the last field index in this class */
    protected static final int LAST_MULTITEXTURECOORDINATE_INDEX = FIELD_TEXCOORD;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_MULTITEXTURECOORDINATE_INDEX + 1;

    /* VRML Field declarations */
    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // TODO:
    // Does not handle PROTOs

    /** exposedField MFNode textureCoordinate */
    protected VRMLNodeType[] vfTextureCoordinate;

    /** The node index to add new textureCoordinates */
    protected int lastTC;

    /** A mapping between the DEF and USE nodes inside this node */
    private HashMap tcMap;

    /** The last TextureCoordinate Set allocated */
    private int lastTCSet;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA, FIELD_TEXCOORD };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_TEXCOORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "texCoord");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_TEXCOORD);
        fieldMap.put("texCoord", idx);
        fieldMap.put("set_texCoord", idx);
        fieldMap.put("texCoord_changed", idx);
    }

    /**
     * Construct a new default instance of this class.
     */
    protected BaseMultiTextureCoordinate() {
        super("MultiTextureCoordinate");

        vfTextureCoordinate = new VRMLNodeType[0];
        lastTC = 0;
        lastTCSet = 0;
        tcMap = new HashMap(2);
        hasChanged = new boolean[LAST_MULTITEXTURECOORDINATE_INDEX + 1];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseMultiTextureCoordinate(VRMLNodeType node) {
        this();

        checkNodeType(node);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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

        for(int i=0; i < vfTextureCoordinate.length; i++)
            vfTextureCoordinate[i].setupFinished();
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
        if (index < 0  || index > LAST_MULTITEXTURECOORDINATE_INDEX) {
            return null;
        }

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
        return TypeConstants.TextureCoordinateNodeType;
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
            case FIELD_TEXCOORD:
                fieldData.clear();
                fieldData.nodeArrayValue = vfTextureCoordinate;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = vfTextureCoordinate.length;
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
                case FIELD_TEXCOORD:
                    destNode.setValue(destIndex,
                                      vfTextureCoordinate,
                                      vfTextureCoordinate.length);
                    break;
                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("TextureTransform sendRoute: No field!" +
                ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("TextureTransform sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_TEXCOORD:
                if(!inSetup) {
                    vfTextureCoordinate = new VRMLNodeType[1];
                    lastTC=0;
                    lastTCSet=0;
                } else {
                    VRMLNodeType tmp[] = new VRMLNodeType[vfTextureCoordinate.length + 1];
                    System.arraycopy(vfTextureCoordinate, 0, tmp, 0, vfTextureCoordinate.length);
                    vfTextureCoordinate = tmp;
                }

                if(child != null)
                    addTextureCoordinateNode(child);

                if(!inSetup) {
                    hasChanged[FIELD_TEXCOORD] = true;
                    fireFieldChanged(FIELD_TEXCOORD);
                }
                break;

            default:
                super.setValue(index, child);
        }
    }


    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_TEXCOORD:
                if(!inSetup) {
                    vfTextureCoordinate = new VRMLNodeType[numValid];
                    lastTC=0;
                    lastTCSet=0;
                } else {
                    VRMLNodeType tmp[] = new VRMLNodeType[vfTextureCoordinate.length + numValid];
                    System.arraycopy(vfTextureCoordinate, 0, tmp, 0, vfTextureCoordinate.length);
                    vfTextureCoordinate = tmp;
                }

                for(int i = 0; i < numValid; i++ )
                    addTextureCoordinateNode(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_TEXCOORD] = true;
                    fireFieldChanged(FIELD_TEXCOORD);
                }
                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    /**
     * Add a single texturetransform node to the list of textures.
     * Override this to add render-specific behavior, but remember to call
     * this method.
     *
     * This method assumes the vfTextureCoordinate array has been allocated to
     * the correct size.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addTextureCoordinateNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        Integer tcIdx;

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl = (VRMLNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            if (!(impl instanceof VRMLTextureCoordinateNodeType))
                throw new InvalidFieldValueException(COORDINATE_PROTO_MSG);

            vfTextureCoordinate[lastTC++] = impl;

            tcIdx = (Integer) tcMap.get(impl);
            if (tcIdx == null) {
                tcMap.put(impl, new Integer(lastTCSet++));
            }
        } else {
            if (!(node instanceof VRMLTextureCoordinateNodeType))
                throw new InvalidFieldValueException(COORDINATE_NODE_MSG);

            vfTextureCoordinate[lastTC++] = node;
            tcIdx = (Integer) tcMap.get(node);
            if (tcIdx == null) {
                tcMap.put(node, new Integer(lastTCSet++));
            }

        }
    }

    //-----------------------------------------------------------------
    // Methods required by the VRMLTextureCoordinateNodeType interface.
    //-----------------------------------------------------------------

    /**
     * Get the number of components defined for this texture type. SHould
     * be one of 2, 3 or 4 for 2D, 3D or time-driven textures. Multi-textures
     * only ever have 2D textures available.
     *
     * @return one of 2, 3 or 4
     */
    public int getNumTextureComponents() {
        return 2;
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets() {
        return vfTextureCoordinate.length;
    }

    /**
     * Get the size of the specified set
     *
     * @param setNum The set to size
     * @throws ArrayIndexOutOfBoundsException
     */
    public int getSize(int setNum) {
        return ((VRMLTextureCoordinateNodeType)vfTextureCoordinate[setNum]).getSize(0);
    }

    /**
     * Accessor method to set a new value for field attribute point.  Attempts
     * to set nodes > numSets will throw an exception.
     *
     * @param setNum The set which this point belongs.
     * @param newPoint New value for the point field
     * @param numValid number of valid items to use from the size array
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setPoint(int setNum, float[] newPoint, int numValid) {
        if (setNum > vfTextureCoordinate.length)
            throw new ArrayIndexOutOfBoundsException();

        VRMLNodeType node = vfTextureCoordinate[setNum];

        if(node instanceof VRMLProtoInstance) {
            VRMLTextureCoordinateNodeType impl = (VRMLTextureCoordinateNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            impl.setPoint(0, newPoint, numValid);
        } else {
            ((VRMLTextureCoordinateNodeType)node).setPoint(0, newPoint, numValid);
        }

        fireComponentChanged(FIELD_TEXCOORD);

        hasChanged[FIELD_TEXCOORD] = true;
        fireFieldChanged(FIELD_TEXCOORD);
    }

    /**
     * Accessor method to get current value of field point.  Sets outside
     * the numSize will throw an exception.
     *
     * @return The current value of point
     * @throws ArrayIndexOutOfBoundsException
     */
    public void getPoint(int setNum, float[] point) {
        if (setNum > vfTextureCoordinate.length)
            throw new ArrayIndexOutOfBoundsException();

        VRMLNodeType node = vfTextureCoordinate[setNum];

        if(node instanceof VRMLProtoInstance) {
            VRMLTextureCoordinateNodeType impl = (VRMLTextureCoordinateNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            impl.getPoint(0, point);
        } else {
            ((VRMLTextureCoordinateNodeType)node).getPoint(0, point);
        }

    }

    /**
     * Determine if this index is shared via DEF/USE inside this set
     *
     * @param index The index to check
     * @return The index if not shared or the original index DEFed
     */
    public int isShared(int index) {
        Integer idx = (Integer) tcMap.get(vfTextureCoordinate[index]);

        if (idx == null)
            return index;
        return idx.intValue();
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum) {
        VRMLNodeType node = vfTextureCoordinate[setNum];

        if(node instanceof VRMLProtoInstance) {
            VRMLTextureCoordinateNodeType impl = (VRMLTextureCoordinateNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            return impl.getTexCoordGenMode(0);
        } else {
            return ((VRMLTextureCoordinateNodeType)node).getTexCoordGenMode(0);
        }
    }
}
