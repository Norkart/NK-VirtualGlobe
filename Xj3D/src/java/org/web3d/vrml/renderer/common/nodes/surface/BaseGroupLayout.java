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

package org.web3d.vrml.renderer.common.nodes.surface;

// Standard imports
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.util.HashSet;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.nodes.VRMLSurfaceChildNodeType;
import org.web3d.vrml.nodes.VRMLSurfaceLayoutNodeType;

/**
 * Common implementation of a GroupLayout node.
 * <p>
 *
 * A group layout is a container for allowing the use of more than one
 * child layout. Typically used to group a bunch of BorderLayouts together
 * under the Overlay node. It has no additional properties beyond the simple
 * layout capabilities.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public abstract class BaseGroupLayout extends BaseSurfaceLayoutNode {

    // Field index constants

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_LAYOUT_INDEX + 1;

    /** Mesage when the one of the grid size values < 1 */
    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

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
        fieldMap.put("bboxSize", new Integer(FIELD_BBOX_SIZE));
    }

    /**
     * Construct a new default Overlay object
     */
    protected BaseGroupLayout() {
        super("GroupLayout");

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
    protected BaseGroupLayout(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLSurfaceLayoutNodeType)node);
    }

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

        if((vfBboxSize[0] != -1) && (vfBboxSize[1] != -1))
            return;

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
        if(index < 0  || index > LAST_LAYOUT_INDEX)
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
}
