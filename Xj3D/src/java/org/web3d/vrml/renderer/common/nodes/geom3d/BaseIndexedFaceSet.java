/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geom3d;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.j3d.geom.GeometryData;
import org.j3d.geom.TriangulationUtils;

import org.web3d.util.IntHashMap;

import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseIndexedGeometryNode;

/**
 * An abstract implementation of an IndexedFaceSet
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.28 $
 */
public abstract class BaseIndexedFaceSet extends BaseIndexedGeometryNode {

    /** Index of the crease angle field */
    protected static final int FIELD_CREASEANGLE = LAST_INDEXEDGEOMETRY_INDEX + 1;

    /** Index of the convex field */
    protected static final int FIELD_CONVEX = LAST_INDEXEDGEOMETRY_INDEX + 2;

    /** The number of fields in this node */
    private static final int NUM_FIELDS = FIELD_CONVEX + 1;

    /** Last index used by this class */
    private static final int LAST_INDEXEDFACESET_INDEX = FIELD_CONVEX;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** field SFBool solid TRUE */
    protected boolean vfConvex;

    /** field SFFloat creaseAngle 0 */
    protected float vfCreaseAngle;

    /** Userdata kept in the triangle geometry */
    protected GeometryData geomData;

    /**
     * Static constructor sets up the field declarations
     */
    static {
        nodeFields = new int[] {
            FIELD_COORD,
            FIELD_NORMAL,
            FIELD_TEXCOORD,
            FIELD_COLOR,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_COORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "coord");
        fieldDecl[FIELD_TEXCOORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "texCoord");
        fieldDecl[FIELD_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "color");
        fieldDecl[FIELD_NORMAL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "normal");
        fieldDecl[FIELD_FOG_COORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "fogCoord");
        fieldDecl[FIELD_ATTRIBS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "attrib");
        fieldDecl[FIELD_SOLID] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "solid");
        fieldDecl[FIELD_CONVEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "convex");
        fieldDecl[FIELD_CCW] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "ccw");
        fieldDecl[FIELD_CREASEANGLE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "creaseAngle");
        fieldDecl[FIELD_COLORPERVERTEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "colorPerVertex");
        fieldDecl[FIELD_NORMALPERVERTEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "normalPerVertex");
        fieldDecl[FIELD_SET_NORMALINDEX] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFInt32",
                                     "set_normalIndex");
        fieldDecl[FIELD_SET_COORDINDEX] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFInt32",
                                     "set_coordIndex");
        fieldDecl[FIELD_SET_COLORINDEX] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFInt32",
                                     "set_colorIndex");
        fieldDecl[FIELD_SET_TEXCOORDINDEX] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFInt32",
                                     "set_texCoordIndex");
        fieldDecl[FIELD_NORMALINDEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFInt32",
                                     "normalIndex");
        fieldDecl[FIELD_COORDINDEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFInt32",
                                     "coordIndex");
        fieldDecl[FIELD_COLORINDEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFInt32",
                                     "colorIndex");
        fieldDecl[FIELD_TEXCOORDINDEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFInt32",
                                     "texCoordIndex");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_COORD);
        fieldMap.put("coord", idx);
        fieldMap.put("set_coord", idx);
        fieldMap.put("coord_changed", idx);

        idx = new Integer(FIELD_TEXCOORD);
        fieldMap.put("texCoord", idx);
        fieldMap.put("set_texCoord", idx);
        fieldMap.put("texCoord_changed", idx);

        idx = new Integer(FIELD_COLOR);
        fieldMap.put("color", idx);
        fieldMap.put("set_color", idx);
        fieldMap.put("color_changed", idx);

        idx = new Integer(FIELD_NORMAL);
        fieldMap.put("normal", idx);
        fieldMap.put("set_normal", idx);
        fieldMap.put("normal_changed", idx);

        idx = new Integer(FIELD_FOG_COORD);
        fieldMap.put("fogCoord", idx);
        fieldMap.put("set_fogCoord", idx);
        fieldMap.put("fogCoord_changed", idx);

        idx = new Integer(FIELD_ATTRIBS);
        fieldMap.put("attrib", idx);
        fieldMap.put("set_attrib", idx);
        fieldMap.put("attrib_changed", idx);

        fieldMap.put("solid",new Integer(FIELD_SOLID));
        fieldMap.put("convex",new Integer(FIELD_CONVEX));
        fieldMap.put("ccw",new Integer(FIELD_CCW));
        fieldMap.put("creaseAngle",new Integer(FIELD_CREASEANGLE));
        fieldMap.put("colorPerVertex",new Integer(FIELD_COLORPERVERTEX));
        fieldMap.put("normalPerVertex", new Integer(FIELD_NORMALPERVERTEX));

        fieldMap.put("normalIndex",new Integer(FIELD_NORMALINDEX));
        fieldMap.put("set_normalIndex",new Integer(FIELD_SET_NORMALINDEX));

        fieldMap.put("coordIndex",new Integer(FIELD_COORDINDEX));
        fieldMap.put("set_coordIndex",new Integer(FIELD_SET_COORDINDEX));

        fieldMap.put("colorIndex",new Integer(FIELD_COLORINDEX));
        fieldMap.put("set_colorIndex",new Integer(FIELD_SET_COLORINDEX));

        fieldMap.put("texCoordIndex", new Integer(FIELD_TEXCOORDINDEX));
        fieldMap.put("set_texCoordIndex",
                     new Integer(FIELD_SET_TEXCOORDINDEX));
    }

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     *
     * @param name The name of the type of node
     */
    protected BaseIndexedFaceSet() {
        super("IndexedFaceSet");

        hasChanged = new boolean[NUM_FIELDS];

        vfConvex = true;
        vfCreaseAngle = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseIndexedFaceSet(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLComponentGeometryNodeType)node);

        int idx = node.getFieldIndex("convex");
        VRMLFieldData field = node.getFieldValue(idx);
        vfConvex = field.booleanValue;

        idx = node.getFieldIndex("creaseAngle");
        field = node.getFieldValue(idx);
        vfCreaseAngle = field.floatValue;

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
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        fieldData.clear();

        switch(index) {
            case FIELD_CONVEX:
                fieldData.booleanValue = vfConvex;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_CREASEANGLE:
                fieldData.floatValue = vfCreaseAngle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer) fieldMap.get(fieldName);

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
        if(index < 0  || index > LAST_INDEXEDFACESET_INDEX)
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

    //-------------------------------------------------------------
    // Methods required by the VRMLNodeComponentListener interface.
    //-------------------------------------------------------------

    /**
     * Notification that the field from the node has changed.
     *
     * @param node The component node that changed
     * @param index The index of the field that has changed
     */
    public void fieldChanged(VRMLNodeType node, int index) {
        if(node == vfCoord)
            changeFlags |= COORDS_CHANGED;
        else if(node == vfColor)
            changeFlags |= COLORS_CHANGED;
        else if(node == vfNormal)
            changeFlags |= NORMALS_CHANGED;
        else if(node == vfTexCoord)
            changeFlags |= TEXCOORDS_CHANGED;
        else
            System.out.println("BaseIndexedFaceSet: Unknown field fieldChanged");

        stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Notify a node that an ExternProto has resolved.  This will verify the objects
     * type and add it to the render sceneGraph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
     public void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        if(inSetup)
            return;

        switch(index) {
            case FIELD_COLOR:
                if(node.getPrimaryType() != TypeConstants.ColorNodeType)
                    throw new InvalidFieldValueException(COLOR_PROTO_MSG);

                localColors = true;
                fireLocalColorsChanged(true);
                changeFlags |= COLORS_CHANGED;
                break;

            case FIELD_COORD:
                if(node.getPrimaryType() != TypeConstants.CoordinateNodeType)
                    throw new InvalidFieldValueException(COORD_PROTO_MSG);

                changeFlags |= COORDS_CHANGED;
                break;

            case FIELD_NORMAL:
                if (node.getPrimaryType() != TypeConstants.NormalNodeType)
                    throw new InvalidFieldValueException(NORMAL_PROTO_MSG);

                changeFlags |= NORMALS_CHANGED;
                break;

            case FIELD_TEXCOORD:
                if(node.getPrimaryType() != TypeConstants.TextureCoordinateNodeType)
                    throw new InvalidFieldValueException(TEXCOORD_PROTO_MSG);

                changeFlags |= TEXCOORDS_CHANGED;
                break;

            default:
                System.out.println("BaseIndexedFaceSet: Unknown field for notifyExternProtoLoaded");
        }

        stateManager.addEndOfThisFrameListener(this);
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

        switch(index) {
            case FIELD_CONVEX:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Convex", this);

                vfConvex = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        // Runtime semantics not yet implemented

        switch(index) {
            case FIELD_CREASEANGLE:
                if(!inSetup)
                    throw new InvalidFieldAccessException("creaseAngle", this);

                if(value < 0)
                    throw new InvalidFieldValueException("CreaseAngle must be [0,inf)");

                vfCreaseAngle = value;
                break;

            default:
                super.setValue(index, value);
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
        boolean notif = false;

        switch(index) {
            case FIELD_COORD:
                if (child == null) {
                    pCoord = null;
                    if (vfCoord != null) {
                            vfCoord.removeComponentListener(this);
                    }
                } else if (child instanceof VRMLProtoInstance) {
                    pCoord = (VRMLProtoInstance) child;
                    node = pCoord.getImplementationNode();

                    if (!(node instanceof VRMLCoordinateNodeType)) {
                        pCoord = null;
                        throw new InvalidFieldValueException(COORD_PROTO_MSG);
                    }
                } else if (!(node instanceof VRMLCoordinateNodeType)) {
                    throw new InvalidFieldValueException(COORD_NODE_MSG);
                }

                vfCoord = (VRMLCoordinateNodeType) node;

                if (vfCoord != null)
                    vfCoord.addComponentListener(this);

                changeFlags |= COORDS_CHANGED;
                notif = true;
                break;

            case FIELD_NORMAL:
                if (child == null) {
                    pNormal = null;
                    if (vfNormal != null) {
                        vfNormal.removeComponentListener(this);
                    }
                } else if (child instanceof VRMLProtoInstance) {
                    pNormal = (VRMLProtoInstance) child;
                    node = pNormal.getImplementationNode();

                    if (!(node instanceof VRMLNormalNodeType)) {
                        pNormal = null;
                        throw new InvalidFieldValueException(NORMAL_PROTO_MSG);
                    }
                } else if (!(node instanceof VRMLNormalNodeType)) {
                    System.out.println("node: " + node);
                    throw new InvalidFieldValueException(NORMAL_NODE_MSG);
                }

                vfNormal = (VRMLNormalNodeType) node;
                if (vfNormal != null)
                    vfNormal.addComponentListener(this);

                changeFlags |= NORMALS_CHANGED;
                notif = true;
                break;

            case FIELD_COLOR :
                if (child == null) {
                    pColor = null;
                    if (vfColor != null)
                        vfColor.removeComponentListener(this);
                } else if (child instanceof VRMLProtoInstance) {
                    pColor = (VRMLProtoInstance) child;
                    node = pColor.getImplementationNode();

                    if (!(node instanceof VRMLColorNodeType)) {
                        pColor = null;
                        throw new InvalidFieldValueException(COLOR_PROTO_MSG);
                    }
                } else if (!(node instanceof VRMLColorNodeType)) {
                    throw new InvalidFieldValueException(COLOR_NODE_MSG);
                }

                vfColor = (VRMLColorNodeType) node;
                if (vfColor != null) {
                    vfColor.addComponentListener(this);
                    if (!localColors)
                       fireLocalColorsChanged(true);
                    localColors = true;
                } else {
                    if (localColors)
                        fireLocalColorsChanged(false);
                    localColors = false;
                }
                changeFlags |= COLORS_CHANGED;
                notif = true;
                break;

            case FIELD_TEXCOORD :
                if (child == null) {
                    pTexCoord = null;
                    if (vfTexCoord != null) {
                        vfTexCoord.removeComponentListener(this);
                    }
                } else if (child instanceof VRMLProtoInstance) {
                    pTexCoord = (VRMLProtoInstance) child;
                    node = pTexCoord.getImplementationNode();

                    if (!(node instanceof VRMLTextureCoordinateNodeType)) {
                        pTexCoord = null;
                        throw new
                            InvalidFieldValueException(TEXCOORD_PROTO_MSG);
                    }
                } else if (!(node instanceof VRMLTextureCoordinateNodeType)) {
                    throw new InvalidFieldValueException(TEXCOORD_NODE_MSG);
                }

                vfTexCoord = (VRMLTextureCoordinateNodeType) node;

                if (vfTexCoord != null)
                    vfTexCoord.addComponentListener(this);

                changeFlags |= TEXCOORDS_CHANGED;
                notif = true;
                break;

            default:
                super.setValue(index, child);
        }

        if(!inSetup && notif) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[index] = true;
            fireFieldChanged(index);
        }
    }
}
