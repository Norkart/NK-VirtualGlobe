/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.render;

// External imports
import java.util.HashMap;

import org.j3d.geom.GeometryData;
// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseIndexedTriangleGeometryNode;

/**
 * An abstract implementation of an IndexedTriangleStripSet
 * <p>
 *
 * The node is defined by X3D as:
 * <pre>
 * IndexedTriangleStripSet : X3DComposedGeometryNode {
 *   MFInt32 [in]     set_index       []   [0,&#8734;) or -1
 *   SFNode  [in,out] color           NULL [X3DColorNode]
 *   SFNode  [in,out] coord           NULL [X3DCoordinateNode]
 *   SFFloat [in,out] creaseAngle     0    [0,&#8734;)
 *   SFNode  [in,out] metadata        NULL [X3DMetadataObject]
 *   SFNode  [in,out] normal          NULL [X3DNormalNode]
 *   SFNode  [in,out] texCoord        NULL [X3DTextureCoordinateNode]
 *   SFBool  []       ccw             TRUE
 *   SFBool  []       normalPerVertex TRUE
 *   SFBool  []       solid           TRUE
 *   MFInt32 []       index           []   [0,&#8734;) or -1
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public abstract class BaseIndexedTriangleStripSet extends BaseIndexedTriangleGeometryNode {

    /** The number of fields in this node */
    private static final int NUM_FIELDS = LAST_INDEXEDTRIANGLEGEOMETRY_INDEX + 1;

    /** Message when the indices are larger than the number of valid coords */
    private static final String MAX_INDEX_COUNT_MSG =
        "Max coordIndex value > number of coords in IndexedTriangleStripSet";

    /** Array of VRMLFieldDeclarations */
    private final static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private final static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private final static int[] nodeFields;

    /** Mapping of all the vertices in each face */
    //private int[][] vertexToFace;

    /** Processed set of strips that are valid */
    protected int[] stripCounts;

    /** The number of valid strip counts for this node */
    protected int numStripCounts;

    /** Value of the greatest index in vfIndex */
    //private int maxIndexValue;

    /** processed set of indices for nodes that have to deal with -1s */
    protected int[] processedIndex;

    /** Number of processed index values */
    protected int numProcessedIndex;

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
        fieldDecl[FIELD_INDEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFInt32",
                                     "index");

        fieldDecl[FIELD_SET_INDEX] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFInt32",
                                     "set_index");

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
        fieldMap.put("ccw",new Integer(FIELD_CCW));
        fieldMap.put("colorPerVertex",new Integer(FIELD_COLORPERVERTEX));
        fieldMap.put("normalPerVertex", new Integer(FIELD_NORMALPERVERTEX));

        fieldMap.put("index",new Integer(FIELD_INDEX));
        fieldMap.put("set_index",new Integer(FIELD_SET_INDEX));
    }

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     *
     * @param name The name of the type of node
     */
    protected BaseIndexedTriangleStripSet() {
        super("IndexedTriangleStripSet");

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseIndexedTriangleStripSet(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLComponentGeometryNodeType)node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

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
        if(index < 0  || index > LAST_INDEXEDTRIANGLEGEOMETRY_INDEX)
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
    // Local methods
    //----------------------------------------------------------

    /**
     * Update the index listing for the faces. This should only be called when
     * the index list has changed. It sets up all the internal mappings for faces
     * to index lists if normalPerVertex is false.
     */
    protected void updateIndexMaps() {
        // Now build up the processed list of vertices
        if(processedIndex == null || processedIndex.length < numIndex)
            processedIndex = new int[numIndex];

        numStripCounts = 1;
        for(int i = 0; i < numIndex - 1; i++) {
            if(vfIndex[i] == -1)
                numStripCounts++;
        }

        if((stripCounts == null) || stripCounts.length < numStripCounts)
            stripCounts = new int[numStripCounts];

        int current_strip = 0;
        numProcessedIndex = 0;

        for(int i = 0; i < numIndex; i++) {
            if(vfIndex[i] == -1)
                current_strip++;
            else {
                processedIndex[numProcessedIndex++] = vfIndex[i];
                stripCounts[current_strip]++;
            }
        }

        int maxIndexValue = 0;

        for(int i = 0; i < numProcessedIndex; i++) {
            if(processedIndex[i] > maxIndexValue)
                maxIndexValue = processedIndex[i];
        }

        if(vfCoord != null && ((maxIndexValue + 1) * 3 > vfCoord.getNumPoints()))
            errorReporter.warningReport(MAX_INDEX_COUNT_MSG, null);
        
        /////////////////////////////////////////////////////////////////////////
        // rem: the rest of this code initializes the vertexToFace arrays,
        // which are not used for anything. In addition, the for loop at the
        // end of this section will throw an indexoutofbounds exception.
        // commenting out for now.......
        /*
        if((vertexToFace == null) ||
           (vertexToFace.length < maxIndexValue + 1))
            vertexToFace = new int[maxIndexValue + 1][];

        // run through the index list and build another vertex user count listing.
        // The array is a temporary that will be reused a number of times.

        int[] idx_tmp = new int[maxIndexValue + 1];

        int strip_idx = 0;
        for(int i = 0; i < numStripCounts; i++) {
            for(int j = 0; j < stripCounts[i] - 1; j++) {
                idx_tmp[processedIndex[strip_idx]]++;
                idx_tmp[processedIndex[strip_idx + 1]]++;

                strip_idx++;
            }
        }

        // Finish allocating the vertexToFace list of the correct size
        // using the idx_tmp list above
        for(int i = 0; i < maxIndexValue + 1; i++) {
            if((vertexToFace[i] == null) ||
               (vertexToFace[i].length != idx_tmp[i]))
                vertexToFace[i] = new int[idx_tmp[i]];
        }

        // Finally, build the vertexToFace list. Use a temporary list to
        // keep track of where we are in the list as we're setting the face
        // numbers. First clear the temp array.
        for(int i = 0; i < maxIndexValue + 1; i++)
            idx_tmp[i] = 0;

        int current_face = 0;
        strip_idx = 0;

        for(int i = 0; i < numStripCounts; i++) {
            // The apex is shared across all faces in the strip
            for(int j = 0; j < stripCounts[i] - 2; j++) {
                int idx1 = processedIndex[strip_idx];
                int idx2 = processedIndex[strip_idx + 1];

                vertexToFace[idx1][idx_tmp[idx1]] = current_face;
                vertexToFace[idx2][idx_tmp[idx2]] = current_face;

                idx_tmp[idx1]++;
                idx_tmp[idx2]++;

                current_face++;
                strip_idx++;
            }
        }
        */
        /////////////////////////////////////////////////////////////////////////
    }
}
