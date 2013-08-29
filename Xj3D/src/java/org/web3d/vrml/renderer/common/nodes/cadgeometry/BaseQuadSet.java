/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.cadgeometry;

// External imports
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseComponentGeometryNode;

/**
 * Common QuadSet handling.
 * <p>
 *
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */
public abstract class BaseQuadSet extends BaseComponentGeometryNode {

    /** The number of fields in this node */
    private static final int NUM_FIELDS = LAST_GEOMETRY_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /**
     * Static constructor sets up the field declarations
     */
    static {
        nodeFields = new int[] {
            FIELD_COORD,
            FIELD_NORMAL,
            FIELD_TEXCOORD,
            FIELD_COLOR,
            FIELD_FOG_COORD,
            FIELD_ATTRIBS,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "color");
        fieldDecl[FIELD_COORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "coord");
        fieldDecl[FIELD_NORMAL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "normal");
        fieldDecl[FIELD_TEXCOORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "texCoord");
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
        fieldDecl[FIELD_CCW] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "ccw");
        fieldDecl[FIELD_COLORPERVERTEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "colorPerVertex");
        fieldDecl[FIELD_NORMALPERVERTEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "normalPerVertex");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_COLOR);
        fieldMap.put("color", idx);
        fieldMap.put("set_color", idx);
        fieldMap.put("color_changed", idx);

        idx = new Integer(FIELD_COORD);
        fieldMap.put("coord", idx);
        fieldMap.put("set_coord", idx);
        fieldMap.put("coord_changed", idx);

        idx = new Integer(FIELD_NORMAL);
        fieldMap.put("normal", idx);
        fieldMap.put("set_normal", idx);
        fieldMap.put("normal_changed", idx);

        idx = new Integer(FIELD_TEXCOORD);
        fieldMap.put("texCoord", idx);
        fieldMap.put("set_texCoord", idx);
        fieldMap.put("texCoord_changed", idx);

        idx = new Integer(FIELD_FOG_COORD);
        fieldMap.put("fogCoord", idx);
        fieldMap.put("set_fogCoord", idx);
        fieldMap.put("fogCoord_changed", idx);

        idx = new Integer(FIELD_ATTRIBS);
        fieldMap.put("attrib", idx);
        fieldMap.put("set_attrib", idx);
        fieldMap.put("attrib_changed", idx);

        fieldMap.put("solid",new Integer(FIELD_SOLID));
        fieldMap.put("ccw",new Integer(FIELD_CCW));
        fieldMap.put("colorPerVertex",new Integer(FIELD_COLORPERVERTEX));
        fieldMap.put("normalPerVertex", new Integer(FIELD_NORMALPERVERTEX));
    }

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     *
     * @param name The name of the type of node
     */
    protected BaseQuadSet() {
        super("QuadSet");
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseQuadSet(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLComponentGeometryNodeType)node);
    }

    //----------------------------------------------------------
    // Methods overriding VRMLGeometryNodeType
    //----------------------------------------------------------

    /**
     * Specified whether this node has color information.  If so, then it
     * will be used for diffuse terms instead of materials.
     *
     * @return true Use local color information for diffuse lighting.
     */
    public boolean hasLocalColors() {
        return localColors;
    }

    /**
     * Add a listener for local color changes.  Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    public void addLocalColorsListener(LocalColorsListener l) {
        if (l != null)
            localColorsListeners.add(l);
    }

    /**
     * Remove a listener for local color changes.  Nulls will be ignored.
     *
     * @param l The listener.
     */
    public void removeLocalColorsListener(LocalColorsListener l) {
        localColorsListeners.remove(l);
    }

    /**
     * Add a listener for texture coordinate generation mode changes.
     * Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    public void addTexCoordGenModeChanged(TexCoordGenModeListener l) {
        System.out.println("TexCoordGenMode changes not implemented");
    }

    /**
     * Remove a listener for texture coordinate generation mode changes.
     * Nulls will be ignored.
     *
     * @param l The listener.
     */
    public void removeTexCoordGenModeChanged(TexCoordGenModeListener l) {
    }

    //----------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType
    //----------------------------------------------------------

    /**
     * Specifies whether a geometry object is a solid opject.
     * If true, then back-face culling can be performed
     *
     * @return The current value of solid
     */
    public boolean isSolid() {
        return false;
    }

    /**
     * Get the value of the CCW field. If the node does not have one, this will
     * return true.
     *
     * @return true if the vertices are CCW ordered
     */
    public boolean isCCW() {
        return true;
    }

    /**
     * Specifies whether this node requires lighting.
     *
     * @return Should lighting be enabled
     */
    public boolean isLightingEnabled() {
        return true;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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
        if(index < 0  || index > LAST_GEOMETRY_INDEX)
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
    }
}
