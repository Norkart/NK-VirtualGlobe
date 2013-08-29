/*****************************************************************************
 *                     Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.layering;

// External imports
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldAccessException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLViewportNodeType;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Base representation of the FixedViewport node.
 * <p>
 *
 * The node is defined as:
 * <pre>
 * FixedViewport : X3DViewportNode {
 *   SFNode  [in,out] metadata  NULL
 *   SFInt32 [] x      0
 *   SFInt32 [] y      0
 *   SFInt32 [] width  0
 *   SFInt32 [] height 0
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class BaseFixedViewport extends AbstractNode
    implements VRMLViewportNodeType {

    /** Index of the X field */
    private static final int FIELD_X = LAST_NODE_INDEX+1;

    /** Index of the Y field */
    private static final int FIELD_Y = LAST_NODE_INDEX+2;

    /** Index of the width field */
    private static final int FIELD_WIDTH = LAST_NODE_INDEX+3;

    /** Index of the height field */
    private static final int FIELD_HEIGHT = LAST_NODE_INDEX+4;

    /** Number of fields constant */
    private static final int NUM_FIELDS = FIELD_HEIGHT + 1;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** initializeOnly SFInt32 x 0 */
    protected int vfX;

    /** initializeOnly SFInt32 y 0 */
    protected int vfY;

    /** initializeOnly SFInt32 width 0 */
    protected int vfWidth;

    /** initializeOnly SFInt32 height 0 */
    protected int vfHeight;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {

        nodeFields = new int[]{ FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_X] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                    "SFInt32",
                    "x");
        fieldDecl[FIELD_Y] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                    "SFInt32",
                    "y");
        fieldDecl[FIELD_WIDTH] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                    "SFInt32",
                    "width");
        fieldDecl[FIELD_HEIGHT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                    "SFInt32",
                    "height");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("x", new Integer(FIELD_X));
        fieldMap.put("y", new Integer(FIELD_Y));
        fieldMap.put("width", new Integer(FIELD_WIDTH));
        fieldMap.put("height", new Integer(FIELD_HEIGHT));
    }

    /**
     * Construct a new default instance of this class.
     */
    protected BaseFixedViewport() {
        super("FixedViewport");

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a light node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseFixedViewport(VRMLNodeType other) {
        this();
        checkNodeType(other);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLViewportNodeType
    //----------------------------------------------------------

    /**
     * Get the type of viewport layout policy that this node represents.
     * This determines how the viewport is managed by the system during window
     * resizes etc. It is a fixed value that never changes for the node
     * implementation.
     *
     * @return One of the VIEWPORT_* constant values
     */
    public int getViewportType() {
        return VIEWPORT_FIXED;
    }

    /**
     * Query whether the viewport X component uses fixed (pixel) addressing
     * or is proportional to the screen size.
     *
     * @return True if fixed addressing is to be used
     */
    public boolean isFixedX() {
        return true;
    }

    /**
     * Query whether the viewport Y component uses fixed (pixel) addressing
     * or is proportional to the screen size.
     *
     * @return True if fixed addressing is to be used
     */
    public boolean isFixedY() {
        return true;
    }

    /**
     * Query whether the viewport width uses fixed (pixel) addressing
     * or is proportional to the screen size.
     *
     * @return True if fixed addressing is to be used
     */
    public boolean isFixedWidth() {
        return true;
    }

    /**
     * Query whether the viewport height uses fixed (pixel) addressing
     * or is proportional to the screen size.
     *
     * @return True if fixed addressing is to be used
     */
    public boolean isFixedHeight() {
        return true;
    }

    /**
     * Get the starting X coordinate of the viewport. Whether this is treated
     * as a pixel or percentage depends on the type of viewport node this node
     * is. If it is a pixel value, it will always be an integer value.
     *
     * @return The X coordinate to start the viewport at
     */
    public float getViewX() {
        return vfX;
    }

    /**
     * Get the starting Y coordinate of the viewport. Whether this is treated
     * as a pixel or percentage depends on the type of viewport node this node
     * is. If it is a pixel value, it will always be an integer value.
     *
     * @return The Y coordinate to start the viewport at
     */
    public float getViewY() {
        return vfY;
    }

    /**
     * Get the width of the viewport. Whether this is treated as a
     * pixel or percentage depends on the type of viewport node this node is.
     * If it is a pixel value, it will always be an integer value.
     *
     * @return The width of the viewport
     */
    public float getViewWidth() {
        return vfWidth;
    }

    /**
     * Get the height of the viewport. Whether this is treated as a
     * pixel or percentage depends on the type of viewport node this node is.
     * If it is a pixel value, it will always be an integer value.
     *
     * @return The height of the viewport
     */
    public float getViewHeight() {
        return vfHeight;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        return fieldDecl[index];
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
     * Get the number of fields.
     *
     * @param The number of fields.
     */
    public int getNumFields() {
        return NUM_FIELDS;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.ViewportNodeType;
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
            case FIELD_X:
                fieldData.nodeValue = null;
                fieldData.intValue = vfX;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_Y:
                fieldData.nodeValue = null;
                fieldData.intValue = vfY;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_WIDTH:
                fieldData.nodeValue = null;
                fieldData.intValue = vfWidth;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_HEIGHT:
                fieldData.nodeValue = null;
                fieldData.intValue = vfHeight;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the field
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldAccessException {

        switch(index) {
            case FIELD_X:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG, "x");

                vfX = value;
                break;

            case FIELD_Y:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG, "y");

                vfY = value;
                break;

            case FIELD_WIDTH:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG, "width");

                vfWidth = value;
                break;

            case FIELD_HEIGHT:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG, "height");

                vfHeight = value;
                break;

            default:
                super.setValue(index, value);
        }
    }
}
