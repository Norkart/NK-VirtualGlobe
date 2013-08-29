/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
import org.j3d.geom.TriangulationUtils;

// Local imports
import org.web3d.vrml.sav.*;

import xj3d.filter.AbstractFilter;


/**
 * Geometry holder that represents the X3D IndexedFaceSet node.
 * <p>
 *
 * The current implementation does not handle vertex attributes.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class IndexedFaceSetGeometry extends TriangulationGeometry  {

    /** The data to be processed is normals */
    private static final int NORMALS = 1;

    /** The data to be processed is normals */
    private static final int COLORS = 2;

    /** The data to be processed is normals */
    private static final int TEXCOORDS = 3;

    /** The coordinate values from the IFS */
    private float[] coordinates;

    /** The normal values from the IFS */
    private float[] normals;

    /** The color values from the IFS */
    private float[] colors;

    /** The fog coordinate values from the IFS */
    private float[] fogCoords;

    /** The number of valid coordinate indices in the coordIndex array */
    private int numCoordIndex;

    /** The set of coordinate indices from the IFS */
    private int[] coordIndices;

    /** The set of normal indices from the IFS */
    private int[] normalIndices;

    /** The set of color indices from the IFS */
    private int[] colorIndices;

    /** The set of texture indices from the IFS */
    private int[] texCoordIndices;

    /** Indicator if this IFS is solid or not */
    private boolean solid;

    /** Indicator if this IFS is wound conterclockwise */
    private boolean ccw;

    /** The crease angle for generating normals */
    private float creaseAngle;

    /** Indicator if this IFS contains only convex polygons */
    private boolean convex;

    /** Are colours provided per vertex or per face */
    private boolean colorPerVertex;

    /** Are normals provided per vertex or per face */
    private boolean normalPerVertex;

    /** Flag indicating the colour has 4 components and not 3 */
    private boolean colorHasAlpha;

    /** Temp holding on to the number of polygons in the system */
    private int polygonCount;

    /** Holder of number vertices per face during processing */
    private int[] rawVerticesPerFace;

    /**
     * Construct a default instance of this converter.
     */
    IndexedFaceSetGeometry() {
        solid = true;
        ccw = true;
        colorPerVertex = true;
        convex = true;
        normalPerVertex = true;
        colorHasAlpha = false;
    }

    //----------------------------------------------------------
    // Methods defined by TriangulationGeometry
    //----------------------------------------------------------

    /**
     * Clear the currently stored values and return to the defaults for
     * this geometry type.
     */
    void reset() {
        solid = true;
        ccw = true;
        colorPerVertex = true;
        convex = true;
        normalPerVertex = true;
        colorHasAlpha = false;

        coordinates = null;
        normals = null;
        colors = null;
        fogCoords = null;
        coordIndices = null;
        normalIndices = null;
        colorIndices = null;
        texCoordIndices = null;
    }

    /**
     * Add a new field value to the geometry. The form of the value is
     * not defined and is up to the implementing class to interpret it
     * according to the needed fields. Note that field names will be
     * compressed from the X3D structure. The coordinate node's point
     * field may be just "coordinate".
     *
     * @param name The name of the field that is to be added
     * @param value The value of the field
     */
    void addFieldValue(String name, Object value) {
        if(name.equals("solid")) {
            if(value instanceof String) {
                solid = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                solid = ((Boolean)value).booleanValue();
            }
        } else if(name.equals("ccw")) {
            if(value instanceof String) {
                ccw = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                ccw = ((Boolean)value).booleanValue();
            }
        } else if(name.equals("colorPerVertex")) {
            if(value instanceof String) {
                colorPerVertex = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                colorPerVertex = ((Boolean)value).booleanValue();
            }
        } else if(name.equals("convex")) {
            if(value instanceof String) {
                convex = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                convex = ((Boolean)value).booleanValue();
            }
        } else if(name.equals("normalPerVertex")) {
            if(value instanceof String) {
                normalPerVertex = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                normalPerVertex = ((Boolean)value).booleanValue();
            }
        } else if(name.equals("creaseAngle")) {
            if(value instanceof String) {
               creaseAngle = fieldReader.SFFloat((String)value);
            } else if(value instanceof float[]) {
                creaseAngle = ((Float)value).floatValue();
            }
        } else if(name.equals("Coordinate")) {
            if(value instanceof String) {
                coordinates = fieldReader.MFVec3f((String)value);
            } else if(value instanceof String[]) {
                coordinates = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                coordinates = (float[])value;
            }
        } else if(name.equals("Normal")) {
            if(value instanceof String) {
                normals = fieldReader.MFVec3f((String)value);
            } else if(value instanceof String[]) {
                normals = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                normals = (float[])value;
            }
        } else if(name.equals("Color")) {
            if(value instanceof String) {
                colors = fieldReader.MFVec3f((String)value);
            } else if(value instanceof String[]) {
                colors = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                colors = (float[])value;
            }
        } else if(name.equals("coordIndex")) {
            if(value instanceof String) {
                coordIndices = fieldReader.MFInt32((String)value);
                numCoordIndex = coordIndices.length;
            } else if(value instanceof String[]) {
                coordIndices = fieldReader.MFInt32((String[])value);
                numCoordIndex = coordIndices.length;
            } else if(value instanceof int[]) {
                coordIndices = (int[])value;
                numCoordIndex = coordIndices.length;
            }
        } else if(name.equals("normalIndex")) {
            if(value instanceof String) {
                normalIndices = fieldReader.MFInt32((String)value);
            } else if(value instanceof String[]) {
                normalIndices = fieldReader.MFInt32((String[])value);
            } else if(value instanceof int[]) {
                normalIndices = (int[])value;
            }
        } else if(name.equals("colorIndex")) {
            if(value instanceof String) {
                colorIndices = fieldReader.MFInt32((String)value);
            } else if(value instanceof String[]) {
                colorIndices = fieldReader.MFInt32((String[])value);
            } else if(value instanceof int[]) {
                colorIndices = (int[])value;
            }
        } else if(name.equals("texCoordIndex")) {
            if(value instanceof String) {
                texCoordIndices = fieldReader.MFInt32((String)value);
            } else if(value instanceof String[]) {
                texCoordIndices = fieldReader.MFInt32((String[])value);
            } else if(value instanceof int[]) {
                texCoordIndices = (int[])value;
            }
        } else if(name.startsWith("TextureCoordinate")) {
        }
    }

    /**
     * Add a new field value to the geometry using array data. The
     * form of the value is  not defined and is up to the implementing
     * class to interpret it according to the needed fields.  The
     * array length is the number of valid items in the passed array.
     * <p>
     * Note that field names will be
     * compressed from the X3D structure. The coordinate node's point
     * field may be just "coordinate".
     *
     * @param name The name of the field that is to be added
     * @param value The value of the field
     * @param len The length of the valid data in the array
     */
    void addFieldValue(String name, Object value, int len) {
        if(name.equals("Coordinate")) {
            if(value instanceof String[]) {
                coordinates = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                coordinates = (float[])value;
            }
        } else if(name.equals("Normal")) {
            if(value instanceof String[]) {
                normals = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                normals = (float[])value;
            }
        } else if(name.equals("Color")) {
            if(value instanceof String[]) {
                colors = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                colors = (float[])value;
            }
        } else if(name.equals("coordIndex")) {
            if(value instanceof String[]) {
                coordIndices = fieldReader.MFInt32((String[])value);
                numCoordIndex = coordIndices.length;
            } else if(value instanceof int[]) {
                coordIndices = (int[])value;
                numCoordIndex = len;
            }
        } else if(name.equals("normalIndex")) {
            if(value instanceof String[]) {
                normalIndices = fieldReader.MFInt32((String[])value);
            } else if(value instanceof int[]) {
                normalIndices = (int[])value;
            }
        } else if(name.equals("colorIndex")) {
            if(value instanceof String[]) {
                colorIndices = fieldReader.MFInt32((String[])value);
            } else if(value instanceof int[]) {
                colorIndices = (int[])value;
            }
        } else if(name.equals("texCoordIndex")) {
            if(value instanceof String[]) {
                texCoordIndices = fieldReader.MFInt32((String[])value);
            } else if(value instanceof int[]) {
                texCoordIndices = (int[])value;
            }
        } else if(name.startsWith("TextureCoordinate")) {
        }
    }

    /**
     * The geometry definition is now finished so take the given field
     * values and generate the triangle output.
     *
     *
     * @param ch The content handler instance to write to
     * @param sh The script handler instance to write to
     * @param ph The proto handler instance to write to
     * @param rh The route handler instance to write to
     */
    void generateOutput(ContentHandler ch,
                        ScriptHandler sh,
                        ProtoHandler ph,
                        RouteHandler rh) {

        // Check the output and adjust accordingly. For max size 3 then just
        // drop the coordinates out now and not do any processing. For anything
        // more than three we need to go through and triangulate whatever we
        // find.
        int max_poly_size = checkMaxPolySize();

        switch(max_poly_size) {
            case 0:
                errorReporter.messageReport("No valid polygons found: Zero sized polygons");
                return;

            case 1:
            case 2:
                errorReporter.messageReport("No valid polygons. Max size " +
                                            max_poly_size);
                return;
        }


        ch.startNode("IndexedTriangleSet", null);
        ch.startField("coord");
        ch.startNode("Coordinate", null );
        ch.startField("point");

        if(ch instanceof BinaryContentHandler) {
            ((BinaryContentHandler)ch).fieldValue(coordinates, coordinates.length);
        } else if(ch instanceof StringContentHandler) {
            StringBuffer buf = new StringBuffer();
            for(int i = 0; i < coordinates.length; i++) {
                buf.append(coordinates[i]);
                buf.append(' ');
            }

            ((StringContentHandler)ch).fieldValue(buf.toString());
        }

        ch.endNode();  // Coordinate
        ch.endField(); // coord

        ch.startField("index");

        int[] output_indices = null;
        int output_idx = 0;
        int input_idx = 0;

        if(max_poly_size == 3) {
            output_indices = new int[polygonCount * 3];

            for(int i = 0; i < polygonCount; i++) {
                if(rawVerticesPerFace[i] == 3) {
                    output_indices[output_idx] = coordIndices[input_idx];
                    output_indices[output_idx + 1] =
                        coordIndices[input_idx + 1];
                    output_indices[output_idx + 2] =
                        coordIndices[input_idx + 2];
                    output_idx += 3;
                }

                input_idx += rawVerticesPerFace[i] + 1;
            }
        } else {
            // greater than 3, so start doing triangulation.
            output_indices = new int[numCoordIndex * max_poly_size * 3];

            if(convex) {
                int face_index = 0;
                int i, j;
                for(i = 0; i < numCoordIndex; i++) {

                    if(rawVerticesPerFace[face_index] < 3) {
                        i += rawVerticesPerFace[face_index];
                        face_index++;
                        continue;
                    }

                    for(j = 0; j < rawVerticesPerFace[face_index] - 2; j++) {
                        if(i + 3 + j > numCoordIndex) {
                            j = rawVerticesPerFace[face_index] - 2;
                            break;
                        }

                        output_indices[output_idx] = coordIndices[i];
                        output_indices[output_idx + 1] =
                            coordIndices[i + 1 + j];
                        output_indices[output_idx + 2] =
                            coordIndices[i + 2 + j];
                        output_idx += 3;
                    }

                    i += j + 2;
                    face_index++;
                }
            } else {
                TriangulationUtils triangulator =
                    new TriangulationUtils(max_poly_size);

                int face_index = 0;
                float[] normal_tmp = new float[3];
                int[] triangle_output = new int[max_poly_size * 3];


                for(int i = 0; i < numCoordIndex; i++) {

                    if(rawVerticesPerFace[face_index] < 3) {
                        i += rawVerticesPerFace[face_index];
                        face_index++;
                        continue;
                    }

                    // Need to pre-calculate the normal here for the face.
                    createFaceNormal(coordinates,
                                     coordIndices,
                                     i,
                                     rawVerticesPerFace[face_index],
                                     normal_tmp);

                    int num_tris =
                        triangulator.triangulateConcavePolygon(coordinates,
                                                               i,
                                                               rawVerticesPerFace[face_index],
                                                               coordIndices,
                                                               triangle_output,
                                                               normal_tmp);

                    // Check for errors during triangulation. This will be a
                    // negative number if there was. Still has valid triangles
                    // though, so negate to get positive value for later use.
                    if(num_tris < 0) {
                        num_tris = -num_tris;
                    }

                    for(int j = 0; j < num_tris; j++) {
                        output_indices[output_idx] = triangle_output[j * 3];
                        output_indices[output_idx + 1] = triangle_output[j * 3 + 1];
                        output_indices[output_idx + 2] = triangle_output[j * 3 + 2];

                        output_idx += 3;
                    }

                    // Triagulate based on the face.
                    i += rawVerticesPerFace[face_index];
                    face_index++;
                }

            }
        }

        if(ch instanceof BinaryContentHandler) {
            BinaryContentHandler bch = (BinaryContentHandler)ch;


            ((BinaryContentHandler)ch).fieldValue(output_indices, output_idx);
        } else if(ch instanceof StringContentHandler) {
            StringBuffer buf = new StringBuffer();
            for(int i = 0; i < output_idx; i++) {
                buf.append(output_indices[i]);
                buf.append(' ');
            }

            ((StringContentHandler)ch).fieldValue(buf.toString());
        }

        ch.endField(); // index
/*
        if(normals != null) {
            ch.startField("normal");
            ch.startNode("Normal", null );
            ch.startField("vector");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(normals, normals.length);
            } else if(ch instanceof StringContentHandler) {
                StringBuffer buf = new StringBuffer();
                for(int i = 0; i < normals.length; i++) {
                    buf.append(normals[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endNode();  // Normal
            ch.endField(); // normals
        }

        if(colors != null) {
            ch.startField("color");
            if(colorHasAlpha)
                ch.startNode("ColorRGBA", null );
            else
                ch.startNode("Color", null );

            ch.startField("color");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(colors, colors.length);
            } else if(ch instanceof StringContentHandler) {
                StringBuffer buf = new StringBuffer();
                for(int i = 0; i < colors.length; i++) {
                    buf.append(colors[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endNode();  // Color[RGBA]
            ch.endField(); // color
        }
*/
        if(!solid) {
            ch.startField("solid");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(solid);
            } else if(ch instanceof StringContentHandler) {

                ((StringContentHandler)ch).fieldValue("FALSE");
            }
        }

        ch.endNode();  // IndexedTriangleSet
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Go through the coordIndex array and work out what the maximum polygon
     * size will be before we've done any processing. It does not define the
     * current maxPolySize variable.
     *
     * @return The maximum size that this check found
     */
    private int checkMaxPolySize() {
        int cur_size = 0;
        int max_size = 0;
        polygonCount = 0;

        for(int i = 0; i < numCoordIndex; i++) {
            if(coordIndices[i] == -1) {
                if(cur_size > max_size)
                    max_size = cur_size;

                cur_size = 0;
                polygonCount++;
            } else {
                cur_size++;
            }
        }

        // One last check on the last index. The spec allows the user to not
        // need to specify -1 as the last value. If we don't check for this,
        // the max size would never be set.
        if((numCoordIndex != 0) && (coordIndices[numCoordIndex - 1] != -1)) {
            if(cur_size > max_size)
                max_size = cur_size;

            polygonCount++;
        }

        rawVerticesPerFace = new int[polygonCount];
        int current_face = 0;

        for(int i = 0; i < numCoordIndex; i++) {
            if(coordIndices[i] != -1) {
                rawVerticesPerFace[current_face]++;
            } else {
                current_face++;
                if(current_face < polygonCount)
                    rawVerticesPerFace[current_face] = 0;
            }
        }

        return max_size;
    }

    /**
     * Rebuild the index list based on the logic defined in the spec.
     */
    private void buildIndexList(int fieldIndex) {
        int[] src_list = null;
        int[] final_list = null;
        boolean per_vertex = false;

        switch(fieldIndex) {
            case NORMALS:
                src_list = normalIndices;
                per_vertex = normalPerVertex;
                break;

            case COLORS:
                src_list = colorIndices;
                per_vertex = colorPerVertex;
                break;

            case TEXCOORDS:
                src_list = texCoordIndices;
                per_vertex = true;
                break;
        }

        // Construct a per-vertex list for internal use out of whatever
        // the source data is. This is based on the rules for the color field
        // defined in clause 13.3.6 of Part 1 of the X3D abstract spec. Assumes
        // that the coordIndex list is valid. If not set, we wouldn't be
        // rendering
        //
        // if the per-vertex flag is false
        //   if the index list is not empty
        //      per-face indexes are expanded to be per vertex
        //      based on the index list info in the coordIndex
        //   else
        //      build a per-vertex list that just lists each face
        //      for x number of times for the corresponding face in
        //      the coordIndex list
        // else
        //   if the index list is not empty
        //      use the index list
        //   else
        //      use the coordIndex values directly
        if(!per_vertex) {
            final_list = new int[numCoordIndex];

            if((src_list != null) && (src_list.length != 0)) {
                // Each index in the list is the index for the face, so just
                // repeat it for the number of times that the coordIndex
                // defines vertices for the face
                int src_pos = 0;
                for(int i = 0; i < numCoordIndex; i++) {
                    if(coordIndices[i] != -1)
                        final_list[i] = src_list[src_pos];
                    else {
                        final_list[i] = -1;
                        src_pos++;
                    }
                }
            } else {
                // We don't have anything, so the list becomes an index starting
                // at 0 and then just incrementing each time we hit a new face
                int src_pos = 0;
                for(int i = 0; i < numCoordIndex; i++) {
                    if(coordIndices[i] != -1)
                        final_list[i] = src_pos;
                    else {
                        final_list[i] = -1;
                        src_pos++;
                    }
                }
            }
        } else {
            if((src_list != null) && (src_list.length != 0)) {
                final_list = src_list;
            } else {
                final_list = coordIndices;
            }
        }

        // Now copy it back to the original list
        switch(fieldIndex) {
            case NORMALS:
                normalIndices = final_list;
                break;

            case COLORS:
                colorIndices = final_list;
                break;

            case TEXCOORDS:
                texCoordIndices = final_list;
                break;
        }
    }

    /**
     * Convenience method to create a normal for the given vertex coordinates
     * and normal array. This performs a cross product of the two vectors
     * described by the middle and two end points.
     *
     * @param coords The coordinate array to read values from
     * @param p The index of the middle point
     * @param p1 The index of the first point
     * @param p2 The index of the second point
     * @param res A temporary value containing the normal value
     */
    private void createFaceNormal(float[] coords,
                                  int[] coordIndex,
                                  int start,
                                  int numVertex,
                                  float[] res) {

        // Uses the Newell method to calculate the face normal
        float nx = 0;
        float ny = 0;
        float nz = 0;
        int x1, y1, z1, x2, y2, z2;

        x1 = coordIndex[start] * 3;
        y1 = x1 + 1;
        z1 = x1 + 2;

        for(int i = 0; i < numVertex - 1; i++) {

            x2 = coordIndex[start + i + 1] * 3;
            y2 = x2 + 1;
            z2 = x2 + 2;

            nx += (coords[y1] - coords[y2]) * (coords[z1] + coords[z2]);
            ny += (coords[z1] - coords[z2]) * (coords[x1] + coords[x2]);
            nz += (coords[x1] - coords[x2]) * (coords[y1] + coords[y2]);

            x1 = x2;
            y1 = y2;
            z1 = z2;
        }

        // The last vertex uses the start position
        x2 = coordIndex[start] * 3;
        y2 = x2 + 1;
        z2 = x2 + 2;

        nx += (coords[y1] - coords[y2]) * (coords[z1] + coords[z2]);
        ny += (coords[z1] - coords[z2]) * (coords[x1] + coords[x2]);
        nz += (coords[x1] - coords[x2]) * (coords[y1] + coords[y2]);

        res[0] = nx;
        res[1] = ny;
        res[2] = nz;

        double len = nx * nx + ny * ny + nz * nz;
        if(len != 0) {
            len = (ccw ? 1 : -1) / Math.sqrt(len);
            res[0] *= len;
            res[1] *= len;
            res[2] *= len;
        }
    }
}
