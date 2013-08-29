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

package org.web3d.vrml.renderer.common.nodes.layout;

// External imports
import java.awt.Rectangle;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.util.HashSet;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.nodes.VRMLSurfaceChildNodeType;
import org.web3d.vrml.nodes.VRMLSurfaceLayoutNodeType;

/**
 * Common implementation of a XYLayout node.
 * <p>
 *
 * An XY layout places its children in relative positions to its layout location.
 * So a position of 0,64 would place the child 64 units down from the layout 0,0.
 *
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public abstract class BaseXYLayout extends BaseSurfaceLayoutNode {

    // Field index constants

    /** The field index for horizontalAlign. */
    protected static final int FIELD_POSITIONS = LAST_LAYOUT_INDEX + 1;

    /** The last field index used by this class */
    protected static final int LAST_XY_LAYOUT_INDEX =
        FIELD_POSITIONS;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_XY_LAYOUT_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // The VRML field values

    /** The value of the positions field. */
    protected float[] vfPositions;

    /** Number of valid values in vfPositions */
    protected int numPositions;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] { FIELD_CHILDREN, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_VISIBLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFBool",
                                    "visible");
        fieldDecl[FIELD_BBOX_SIZE] =
           new VRMLFieldDeclaration(FieldConstants.FIELD,
                                    "SFVec2f",
                                    "bboxSize");
        fieldDecl[FIELD_CHILDREN] =
           new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "MFNode",
                                    "children");
        fieldDecl[FIELD_POSITIONS] =
           new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "MFVec2f",
                                    "positions");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        idx = new Integer(FIELD_VISIBLE);
        fieldMap.put("visible", idx);
        fieldMap.put("set_visible", idx);
        fieldMap.put("visible_changed", idx);

        idx = new Integer(FIELD_POSITIONS);
        fieldMap.put("positions", idx);
        fieldMap.put("set_positions", idx);
        fieldMap.put("positions_changed", idx);

        fieldMap.put("bboxSize", new Integer(FIELD_BBOX_SIZE));
    }

    /**
     * Construct a new default Overlay object
     */
    protected BaseXYLayout() {
        super("XYLayout");

        hasChanged = new boolean[NUM_FIELDS];

        // Set the default values for the fields
        vfPositions = FieldConstants.EMPTY_MFVEC2F;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseXYLayout(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLSurfaceLayoutNodeType)node);

        try {
            int index = node.getFieldIndex("horizontalAlign");
            VRMLFieldData data = node.getFieldValue(index);

            if(data.numElements != 0) {
                vfPositions = new float[data.numElements * 2];
                System.arraycopy(data.floatArrayValue,
                                 0,
                                 vfPositions,
                                 0,
                                 data.numElements * 2);
                numPositions = data.numElements * 2;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
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

        // Try to establish basic bounds:
        if((vfBboxSize[0] != -1) && (vfBboxSize[1] != -1))
            return;

        float max_width = 0;
        float max_height = 0;
        int i;

        for(i = 0; i < vfChildren.length; i++) {
            VRMLSurfaceChildNodeType kid =
                (VRMLSurfaceChildNodeType)vfChildren[i];
            Rectangle bbox = kid.getRealBounds();

            max_width = (max_width > bbox.width) ? max_width : bbox.width;
            max_height = (max_height > bbox.height) ? max_height : bbox.height;
        }

        if(vfBboxSize[0] == -1)
            screenBounds.width = (int)max_width;

        if(vfBboxSize[1] == -1)
            screenBounds.height = (int)max_height;

        updateManagedNodes();
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
        if(index < 0  || index > LAST_XY_LAYOUT_INDEX)
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

        switch(index) {
            case FIELD_POSITIONS:
                fieldData.clear();
                fieldData.floatArrayValue = vfPositions;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numPositions / 2;
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
                case FIELD_POSITIONS:
                    destNode.setValue(destIndex, vfPositions, numPositions);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field! " + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                               ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the field
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_POSITIONS:
                if(vfPositions.length < numValid)
                    vfPositions = new float[numValid];

                System.arraycopy(value, 0, vfPositions, 0, numValid);
                numPositions = numValid;

                if(!inSetup) {
                    updateManagedNodes();
                    hasChanged[FIELD_POSITIONS] = true;
                    fireFieldChanged(FIELD_POSITIONS);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLSurfaceChildNodeType interface.
    //-------------------------------------------------------------

    /**
     * Tell this overlay that it's position in window coordinates has been
     * changed to this new value. The position is always that of the top-left
     * corner of the bounding box in screen coordinate space.
     *
     * @param x The x location of the window in pixels
     * @param y The y location of the window in pixels
     */
    public void setLocation(int x, int y) {
        int x_offset = x - (int)screenLocation[0];
        int y_offset = y - (int)screenLocation[1];

        super.setLocation(x, y);

        int new_x, new_y;

        for(int i = 0; i < vfChildren.length; i++) {
            VRMLSurfaceChildNodeType kid =
                (VRMLSurfaceChildNodeType)vfChildren[i];
            Rectangle bbox = kid.getRealBounds();

            new_x = bbox.x + x_offset;
            new_y = bbox.y + y_offset;

            kid.setLocation(new_x, new_y);
        }
    }

    //----------------------------------------------------------
    // Methods overriding base class.
    //----------------------------------------------------------

    /**
     * Convenience method to update the children node(s) to the new positions.
     * The position may have changed because the window size changed, or the
     * alignment fractions have changed. Derived classes may override this
     * method but should call this as well to make sure the correct values
     * are updated.
     */
    protected void updateManagedNodes() {
        // Where are we now?

        int x = screenBounds.x;
        int y = screenBounds.y;
        int pos = 0;

        // Cycle through all the children and place them at the new position.
        for(int i = 0; i < vfChildren.length; i++) {
            VRMLNodeType n = vfChildren[i];
            VRMLSurfaceChildNodeType kid = null;

            if(n == null)
                continue;

            if(n instanceof VRMLSurfaceChildNodeType)
                kid = (VRMLSurfaceChildNodeType)n;
            else {
                VRMLProtoInstance p = (VRMLProtoInstance)n;
                kid = (VRMLSurfaceChildNodeType)p.getImplementationNode();
            }

            // With the node in hand, we now need to calculate the top-left
            // position to use.

            if(numPositions == 0) {
                kid.setLocation(x, y);
            } else {
                if(pos >= numPositions)
                    pos = numPositions - 1;

                kid.setLocation(x + (int)vfPositions[pos++],
                                y + (int)vfPositions[pos++]);
            }
        }
    }
}
