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

package org.web3d.vrml.renderer.common.nodes.picking;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.HashSet;

/**
 * Implementation of the common VolumePicker node for all renderers.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public abstract class BaseVolumePicker extends BasePickingNode {

    /** Last index used by this base node */
    protected static final int LAST_VOLUME_INDEX = LAST_PICK_INDEX;

    /** The total number of fields in this node */
    protected static final int NUM_FIELDS = LAST_VOLUME_INDEX + 1;

    /** Message for when the proto is not a Geometry */
    protected static final String VOLUME_PROTO_MSG =
        "Proto does not describe a volumetric object type";

    /** Message for when the node in setValue() is not a Geometry */
    protected static final String VOLUME_NODE_MSG =
        "Node does not describe a volumetric object type";

    /** Error message for the geometry pick type */
    private static final String PICK_GEOM_MSG = "The pickGeometry type is " +
        "invalid for VolumePicker. It must be one of the " +
        "X3DComposedGeometryNode node types ";

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
        nodeFields = new int[] {
            FIELD_PICK_TARGET,
            FIELD_PICKING_GEOMETRY,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_PICKING_GEOMETRY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "pickingGeometry");
        fieldDecl[FIELD_PICK_TARGET] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "pickTarget");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_PICKED_GEOMETRY] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFNode",
                                     "pickedGeometry");
        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");
        fieldDecl[FIELD_INTERSECTION_TYPE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "intersectionType");
        fieldDecl[FIELD_SORT_ORDER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "sortOrder");
        fieldDecl[FIELD_OBJECT_TYPE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "objectType");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_PICKING_GEOMETRY);
        fieldMap.put("pickingGeometry", idx);
        fieldMap.put("set_pickingGeometry", idx);
        fieldMap.put("pickingGeometry_changed", idx);

        idx = new Integer(FIELD_PICK_TARGET);
        fieldMap.put("pickTarget", idx);
        fieldMap.put("set_pickTarget", idx);
        fieldMap.put("pickTarget_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = new Integer(FIELD_OBJECT_TYPE);
        fieldMap.put("objectType", idx);
        fieldMap.put("set_objectType", idx);
        fieldMap.put("objectType_changed", idx);

        fieldMap.put("sortOrder",new Integer(FIELD_SORT_ORDER));
        fieldMap.put("intersectionType",new Integer(FIELD_INTERSECTION_TYPE));

        fieldMap.put("isActive",new Integer(FIELD_IS_ACTIVE));
        fieldMap.put("pickedGeometry",new Integer(FIELD_PICKED_GEOMETRY));
    }

    /**
     * Construct a new time sensor object
     */
    public BaseVolumePicker() {
        super("VolumePicker", PICK_GEOM_MSG);

        hasChanged = new boolean[NUM_FIELDS];

        validGeometryNodeNames.add("Box");
        validGeometryNodeNames.add("Cone");
        validGeometryNodeNames.add("Cylinder");
        validGeometryNodeNames.add("IndexedFaceSet");
        validGeometryNodeNames.add("TriangleSet");
        validGeometryNodeNames.add("IndexedTriangleSet");
        validGeometryNodeNames.add("TriangleFanSet");
        validGeometryNodeNames.add("IndexedTriangleFanSet");
        validGeometryNodeNames.add("TriangleStripSet");
        validGeometryNodeNames.add("IndexedTriangleStripSet");
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseVolumePicker(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLPickingSensorNodeType)node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLPickingSensorNodeType
    //----------------------------------------------------------

    /**
     * Get the picking type that this class represents. A shortcut way of
     * quickly determining the picking strategy to be used by the internal
     * implementation to avoid unnessary calculations.
     *
     * @return One of the *_PICK constants
     */
    public int getPickingType() {
        return VOLUME_PICK;
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
        if(index < 0  || index > LAST_VOLUME_INDEX)
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

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------
}
