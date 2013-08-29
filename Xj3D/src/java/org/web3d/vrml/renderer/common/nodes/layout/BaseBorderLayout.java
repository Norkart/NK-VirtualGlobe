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
import java.util.ArrayList;
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
 * Common implementation of a BorderLayout node.
 * <p>
 *
 * A border layout places the contained children relative to the border using
 * an alignment and fraction setup. It operates on the two axis, screen
 * coordinate setup.
 * <p>
 *
 * Because this layout only has a single set of conditions, yet has many
 * children, it does not make sense to have more than one child node actually
 * assigned. If more than one child is provided, the layout will place them
 * all at the same position wrt the alignment requirements.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class BaseBorderLayout extends BaseSurfaceLayoutNode {

    // Field index constants

    /** The field index for horizontalAlign. */
    protected static final int FIELD_HORIZONTAL_ALIGN = LAST_LAYOUT_INDEX + 1;

    /** The field index for verticalAlign. */
    protected static final int FIELD_VERTICAL_ALIGN = LAST_LAYOUT_INDEX + 2;

    /** The field index for horizontalFraction. */
    protected static final int FIELD_HORIZONTAL_FRACTION = LAST_LAYOUT_INDEX + 3;

    /** The field index for verticalFraction. */
    protected static final int FIELD_VERTICAL_FRACTION = LAST_LAYOUT_INDEX + 4;


    /** The last field index used by this class */
    protected static final int LAST_BORDER_LAYOUT_INDEX =
        FIELD_VERTICAL_FRACTION;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_BORDER_LAYOUT_INDEX + 1;

    /** The field value for the LEFT horizontal alignment */
    protected static final String LEFT = "LEFT";

    /** The field value for the RIGHT horizontal alignment */
    protected static final String RIGHT = "RIGHT";

    /** The field value for the CENTER horizontal and vertical alignments */
    protected static final String CENTER = "CENTER";

    /** The field value for the TOP vertical alignment */
    protected static final String TOP = "TOP";

    /** The field value for the BOTTOM vertical alignment */
    protected static final String BOTTOM = "BOTTOM";


    /** The internal ID value for the LEFT horizontal alignment */
    protected static final int LEFT_ALIGN = 1;

    /** The internal ID value for the RIGHT horizontal alignment */
    protected static final int RIGHT_ALIGN = 2;

    /** The internal ID value for the CENTER horizontal and vertical alignments */
    protected static final int CENTER_ALIGN = 3;

    /** The internal ID value for the TOP vertical alignment */
    protected static final int TOP_ALIGN = 4;

    /** The internal ID value for the BOTTOM vertical alignment */
    protected static final int BOTTOM_ALIGN = 5;

    /** Message when the alignment value provided is incorrect */
    private static final String BAD_ALIGN_MSG =
        "The alignment value provided is unknown: ";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Mapping of valid field values to their Integer ID equivalents */
    protected static HashMap alignmentMap;

    /** Set of allowed horiztonal alignment field values */
    protected static HashSet allowedHorizontalValues;

    /** Set of allowed vertical alignment field values */
    protected static HashSet allowedVerticalValues;

    // The VRML field values

    /** The value of the horizontalAlign field. */
    protected String vfHorizontalAlign;

    /** The value of the verticalAlign field. */
    protected String vfVerticalAlign;

    /** The value of the horizontalFraction field. */
    protected float vfHorizontalFraction;

    /** The value of the verticalFraction field. */
    protected float vfVerticalFraction;

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
        fieldDecl[FIELD_HORIZONTAL_ALIGN] =
           new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFString",
                                    "horizontalAlign");
        fieldDecl[FIELD_VERTICAL_ALIGN] =
           new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFString",
                                    "verticalAlign");
        fieldDecl[FIELD_HORIZONTAL_FRACTION] =
           new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFFloat",
                                    "horizontalFraction");
        fieldDecl[FIELD_VERTICAL_FRACTION] =
           new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFFloat",
                                    "verticalFraction");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_VISIBLE);
        fieldMap.put("visible", idx);
        fieldMap.put("set_visible", idx);
        fieldMap.put("visible_changed", idx);

        idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        idx = new Integer(FIELD_HORIZONTAL_ALIGN);
        fieldMap.put("horizontalAlign", idx);
        fieldMap.put("set_horizontalAlign", idx);
        fieldMap.put("horizontalAlign_changed", idx);

        idx = new Integer(FIELD_HORIZONTAL_FRACTION);
        fieldMap.put("horizontalFraction", idx);
        fieldMap.put("set_horizontalFraction", idx);
        fieldMap.put("horizontalFraction_changed", idx);

        idx = new Integer(FIELD_VERTICAL_ALIGN);
        fieldMap.put("verticalAlign", idx);
        fieldMap.put("set_verticalAlign", idx);
        fieldMap.put("verticalAlign_changed", idx);

        idx = new Integer(FIELD_VERTICAL_FRACTION);
        fieldMap.put("verticalFraction", idx);
        fieldMap.put("set_verticalFraction", idx);
        fieldMap.put("verticalFraction_changed", idx);

        fieldMap.put("bboxSize", new Integer(FIELD_BBOX_SIZE));

        alignmentMap = new HashMap();
        alignmentMap.put(LEFT, new Integer(LEFT_ALIGN));
        alignmentMap.put(RIGHT, new Integer(RIGHT_ALIGN));
        alignmentMap.put(CENTER, new Integer(CENTER_ALIGN));
        alignmentMap.put(TOP, new Integer(TOP_ALIGN));
        alignmentMap.put(BOTTOM, new Integer(BOTTOM_ALIGN));

        allowedHorizontalValues = new HashSet(3);
        allowedHorizontalValues.add(LEFT);
        allowedHorizontalValues.add(RIGHT);
        allowedHorizontalValues.add(CENTER);

        allowedVerticalValues = new HashSet(3);
        allowedVerticalValues.add(TOP);
        allowedVerticalValues.add(BOTTOM);
        allowedVerticalValues.add(CENTER);
    }

    /**
     * Construct a new default Overlay object
     */
    protected BaseBorderLayout() {
        super("BorderLayout");

        hasChanged = new boolean[NUM_FIELDS];

        // Set the default values for the fields
        vfHorizontalAlign = LEFT;
        vfVerticalAlign = TOP;
        vfHorizontalFraction = 0;
        vfVerticalFraction = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseBorderLayout(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLSurfaceLayoutNodeType)node);

        try {
            int index = node.getFieldIndex("horizontalAlign");
            VRMLFieldData data = node.getFieldValue(index);
            vfHorizontalAlign = data.stringValue;

            index = node.getFieldIndex("verticalAlign");
            data = node.getFieldValue(index);
            vfVerticalAlign = data.stringValue;

            index = node.getFieldIndex("horizontalFraction");
            data = node.getFieldValue(index);
            vfHorizontalFraction = data.floatValue;

            index = node.getFieldIndex("verticalFraction");
            data = node.getFieldValue(index);
            vfVerticalFraction = data.floatValue;

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

        if(index == null)
            return -1;
        else
            return index.intValue();
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
        if(index < 0  || index > LAST_BORDER_LAYOUT_INDEX)
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
            case FIELD_HORIZONTAL_ALIGN:
                fieldData.clear();
                fieldData.stringValue = vfHorizontalAlign;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_VERTICAL_ALIGN:
                fieldData.clear();
                fieldData.stringValue = vfVerticalAlign;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_HORIZONTAL_FRACTION:
                fieldData.clear();
                fieldData.floatValue = vfHorizontalFraction;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_VERTICAL_FRACTION:
                fieldData.clear();
                fieldData.floatValue = vfVerticalFraction;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
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
                case FIELD_HORIZONTAL_ALIGN:
                    destNode.setValue(destIndex, vfHorizontalAlign);
                    break;

                case FIELD_VERTICAL_ALIGN:
                    destNode.setValue(destIndex, vfVerticalAlign);
                    break;

                case FIELD_HORIZONTAL_FRACTION:
                    destNode.setValue(destIndex, vfHorizontalFraction);
                    break;

                case FIELD_VERTICAL_FRACTION:
                    destNode.setValue(destIndex, vfVerticalFraction);
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
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_HORIZONTAL_FRACTION:
                vfHorizontalFraction = value;
                break;

            case FIELD_VERTICAL_FRACTION:
                vfVerticalFraction = value;
                break;

            default:
                super.setValue(index, value);
        }

        if(!inSetup) {
            updateManagedNodes();
            hasChanged[index] = true;
            fireFieldChanged(index);
        }
    }

    /**
     * Set the value of the field at the given index as a string.
     * This would be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the field
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_HORIZONTAL_ALIGN:
                if(allowedHorizontalValues.contains(value))
                    vfHorizontalAlign = value;
                else
                    throw new InvalidFieldValueException(BAD_ALIGN_MSG +
                                                         value);
                break;

            case FIELD_VERTICAL_ALIGN:
                if(allowedVerticalValues.contains(value))
                    vfVerticalAlign = value;
                else
                    throw new InvalidFieldValueException(BAD_ALIGN_MSG +
                                                         value);
                break;

            default:
                super.setValue(index, value);
        }

        if(!inSetup) {
            updateManagedNodes();
            hasChanged[index] = true;
            fireFieldChanged(index);
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
        int x = screenBounds.x +
                (int)(screenBounds.width * vfHorizontalFraction);
        int y = screenBounds.y +
                (int)(screenBounds.height * vfVerticalFraction);

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

            float[] bounds = kid.getBboxSize();
            Rectangle rect = kid.getRealBounds();
            int x_bound = (bounds[0] == -1) ? rect.width : (int)bounds[0];
            int y_bound = (bounds[1] == -1) ? rect.height : (int)bounds[1];
            int top = 0;
            int left = 0;

            // horizontal and then vertical calcs
            Integer align = (Integer)alignmentMap.get(vfHorizontalAlign);
            switch(align.intValue()) {
                case LEFT_ALIGN:
                    // leave x,y as is
                    left = x;
                    break;

                case RIGHT_ALIGN:
                    left = x - x_bound;
                    break;

                case CENTER_ALIGN:
                    left = x - (x_bound >> 1);
                    break;
            }

            align = (Integer)alignmentMap.get(vfVerticalAlign);
            switch(align.intValue()) {
                case TOP_ALIGN:
                    // leave x,y as is
                    top = y;
                    break;

                case BOTTOM_ALIGN:
                    top = y - y_bound;
                    break;

                case CENTER_ALIGN:
                    top = y - (y_bound >> 1);
                    break;

            }

            kid.setLocation(left, top);
        }
    }
}
