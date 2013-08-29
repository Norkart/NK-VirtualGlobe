/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geospatial;

// External imports
import org.j3d.geom.GeometryGenerator;
import org.j3d.geom.GeometryData;
import org.j3d.geom.InvalidArraySizeException;
import org.j3d.geom.UnsupportedTypeException;
import javax.vecmath.Vector3f;

import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

// Local imports
// None

/**
 * A generator that takes a set of height values as a grid and turns it into
 * geometry suitable for local graphics projections.
 * <p>
 *
 * Points are defined in the height arrays in width first order. Normals, are
 * always smooth blended.
 * <p>
 *
 * Alan: There are some cases where texture generation is not complete.
 * Especially in regards to 3D textures.
 * <p>
 *
 * This class originally came from the j3d.org ElevationGridGenerator, but has
 * been mostly gutted and special cased for the needs of this system.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class ElevationGridGenerator {

    /** The default size of the terrain */
    private static final float DEFAULT_SIZE = 100;

    /** The default number of points in each direction */
    private static final int DEFAULT_POINT_COUNT = 2;

    /** The default base height of the terrain */
    private static final float DEFAULT_HEIGHT = 2;

    /** Current width of the terrain */
    private double terrainWidth;

    /** Depth of the terrain to generate */
    private double terrainDepth;

    /** Number of points in the width direction */
    private int widthPoints;

    /** Number of points in the depth direction */
    private int depthPoints;

    /** The yScale applied after coordinate conversion */
    private float yScale;

    /** The points to use as a 1D array. */
    private double[] flatHeights;

    /** The number of terrain coordinates in use */
    private int numTerrainValues;

    /** The number of texture coordinates in use */
    private int numTexcoordValues;

    /** The array holding all of the vertices during construction */
    private float[] terrainCoordinates;

    /** The array holding all of the normals during construction */
    private float[] terrainNormals;

    /** The array holding all of the texture coordinates during construction */
    private float[] terrainTexcoords;

    /** The number of quads in the terrain */
    private int facetCount;

    /** Utility class to supply the source coordinates for transformation */
    private GeoPosition inPosition;

    /** Utility class to fetch the output coordinates from transformation */
    private GeoPosition outPosition;

    /** work array to output normal generation into */
    private float[] normal;

    /** Are we calculating per vertex normals, hardcode for now */
    private boolean perVertexNormals = true;

    /**
     * Construct a customised terrain according to the full set of configurable
     * data.
     *
     * @param w The width of the terrain
     * @param d The depth of the terrain
     * @param wPnts The number of heights in the width
     * @param dPnts The number of heights in the depth
     * @param heights The array of height values to use
     * @throws IllegalArgumentException One of the points were <= 1 or the
     *   dimensions are non-positive
     */
    public ElevationGridGenerator(double w,
                                  double d,
                                  int wPnts,
                                  int dPnts,
                                  float yScale,
                                  double[] heights)
    {
        if((wPnts < 2) || (dPnts < 2))
            throw new IllegalArgumentException("Point count <= 1");

        if((w <= 0) || (d <= 0))
            throw new IllegalArgumentException("Dimension <= 0");

        terrainWidth = w;
        terrainDepth = d;
        widthPoints = wPnts;
        depthPoints = dPnts;
        this.yScale = yScale;

        facetCount = (depthPoints - 1) * (widthPoints - 1);

        flatHeights = heights;

        inPosition = new GeoPosition();
        outPosition = new GeoPosition();
        normal = new float[3];
    }

    /**
     * Change the dimensions of the cone to be generated. Calling this will
     * make the points be re-calculated next time you ask for geometry or
     * normals.
     *
     * @param w The width of the terrain
     * @param d The depth of the terrain
     * @param wPnts The number of heights in the width
     * @param dPnts The number of heights in the depth
     * @throws IllegalArgumentException One of the points were <= 1 or the
     *   dimensions are non-positive
     */
    public void setDimensions(double w, double d, int wPnts, int dPnts)
    {
        if((terrainWidth != w) || (terrainDepth != d))
        {
            terrainDepth = d;
            terrainWidth = w;
        }

        if((wPnts != widthPoints) || (dPnts != depthPoints))
        {
            widthPoints = wPnts;
            depthPoints = dPnts;

            facetCount = (depthPoints - 1) * (widthPoints - 1);
        }
    }

    /**
     * Set the details of the terrain height to use a flat array of values.
     *
     * @param heights The array of height values to use
     */
    public void setTerrainDetail(double[] heights)
    {
        flatHeights = heights;
    }

    /**
     * Get the number of vertices that this generator will create for the
     * shape given in the definition based on the current width and height
     * information.
     *
     * @param indexed True if this is to be indexed triangle strips, false
     *    otherwise
     * @return The vertex count for the object
     * @throws UnsupportedTypeException The generator cannot handle the type
     *   of geometry you have requested.
     */
    public int getVertexCount(boolean indexed)
    {
        int ret_val;

        if(indexed)
            ret_val = facetCount * 2;
        else
            ret_val = widthPoints * 2 * (depthPoints - 1);

        return ret_val;
    }


    /**
     * Generate a new set of geometry items based on the passed data. If the
     * data does not contain the right minimum array lengths an exception will
     * be generated. If the array reference is null, this will create arrays
     * of the correct length and assign them to the return value.
     *
     * @param data The data to base the calculations on
     * @param transform The geodetic transformation needed
     * @param gridOrigin The location of the SW corner of the grid
     * @param localOrigin an origin offset if needed. Null if not
     * @param indexed True if this is to be indexed triangle strips, false
     *    otherwise
     * @param creaseAngle angle over which we want to create separate normals
     *   rather than smoothed normals
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    public void generate(GeometryData data,
                         MathTransform transform,
                         double[] gridOrigin,
                         double[] localOrigin,
                         boolean indexed,
                         double creaseAngle)
        throws InvalidArraySizeException, TransformException
    {
        if(indexed)
            indexedTriangleStrips(data,
                                  transform,
                                  gridOrigin,
                                  localOrigin,
                                  creaseAngle);
        else
            triangleStrips(data,
                           transform,
                           gridOrigin,
                           localOrigin,
                           creaseAngle);

    }

    /**
     * Generate indexed quads.
     *
     * @param data The data to base the calculations on
     * @param transform The geodetic transformation needed
     * @param gridOrigin The location of the SW corner of the grid
     * @param localOrigin an origin offset if needed. Null if not
     * @param indexed True if this is to be indexed triangle strips, false
     *    otherwise
     * @param creaseAngle angle over which we want to create separate normals
     *   rather than smoothed normals
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    public void generateIndexedQuads(GeometryData data,
                         MathTransform transform,
                         double[] gridOrigin,
                         double[] localOrigin,
                         boolean indexed,
                         double creaseAngle)
        throws InvalidArraySizeException, TransformException {


        int vtx_cnt = widthPoints * depthPoints;

        if(data.coordinates == null)
            data.coordinates = new float[vtx_cnt * 3];
        else if(data.coordinates.length < vtx_cnt * 3)
            throw new InvalidArraySizeException("Coordinates",
                                                data.coordinates.length,
                                                vtx_cnt * 3);

        if(data.normals == null)
            data.normals = new float[vtx_cnt * 3];
        else if(data.normals.length < vtx_cnt * 3)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt * 3);

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt * 2];
        else if(data.textureCoordinates.length < vtx_cnt * 2)
            throw new InvalidArraySizeException("TextureCoordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt * 2);

        int index_size = (widthPoints - 1) * ( depthPoints - 1 ) * 4;

        if(data.indexes == null)
            data.indexes = new int[index_size];
        else if(data.indexes.length < index_size)
            throw new InvalidArraySizeException("Indexes",
                                                data.indexes.length,
                                                index_size);

        float[] coords = data.coordinates;
        float[] texCoords = data.textureCoordinates;
        float[] normals = data.normals;
        data.vertexCount = vtx_cnt;

        double w = gridOrigin[1];
        double d = gridOrigin[0];
        double width_inc = terrainWidth / (widthPoints - 1);
        double depth_inc = terrainDepth / (depthPoints - 1);
        float widthDiv = 1.0f/(  widthPoints - 1.0f );
        float depthDiv = 1.0f/( depthPoints - 1.0f );

        int count = 0;
        int num = widthPoints * depthPoints;
        int texIndex = 0;
        double height;

        if(localOrigin != null) {
            int i = 0;
            for(int z = 0; z < depthPoints; z++) {
                for(int x = 0; x <widthPoints; x++) {
                    i++;

                    // Proposed behavior for missing values, issue warning?
                    if (i > flatHeights.length)
                        height = 0;
                    else
                        height = flatHeights[i - 1];

                    inPosition.setPosition(d, w, height * yScale);

                    transform.transform(inPosition, outPosition);

                    double[] out = outPosition.getCoordinates();

                    coords[count] = (float)(out[0] - localOrigin[0]);
                    coords[count + 1] = (float)(out[1] - localOrigin[1]);
                    coords[count + 2] = (float)(out[2] - localOrigin[2]);
                    texCoords[texIndex++] = (float) (x * widthDiv);
                    texCoords[texIndex++] = (float) (z * depthDiv);

                    count += 3;
                    w += width_inc;

                    if(((i % (widthPoints)) == 0)) {
                        d += depth_inc;
                        w = gridOrigin[1];
                    }
                }
            }
        } else {
            int i = 0;

            for(int z = 0; z < depthPoints; z++) {
                for(int x = 0; x <widthPoints; x++) {
                    i++;
                    // Proposed behavior for missing values, issue warning?
                    if (i > flatHeights.length)
                        height = 0;
                    else
                        height = flatHeights[i - 1];

                    inPosition.setPosition(d, w, height * yScale);

                    transform.transform(inPosition, outPosition);

                    double[] out = outPosition.getCoordinates();

                    coords[count] = (float)out[0];
                    coords[count + 1] = (float)out[1];
                    coords[count + 2] = (float)out[2];

                    texCoords[texIndex++] = (float) (x * widthDiv);
                    texCoords[texIndex++] = (float) (z * depthDiv);
                    count += 3;

                    w += width_inc;

                    if(((i % (widthPoints)) == 0)) {
                        d += depth_inc;
                        w = gridOrigin[1];
                    }

                }
            }
        }


        int index = 0;
        int[] values = data.indexes;
        data.indexesCount = data.indexes.length;

//System.out.println("depth: " + depthPoints + " widthPoints: " + widthPoints);
        for ( int z = 0; z < depthPoints - 1; z++ ) {
            for ( int x = 0; x < widthPoints - 1; x++ ) {
                values[index]   = x + z * widthPoints;
                values[index+1] = values[index] + 1;
                values[index+2] = values[index+1] + widthPoints;
                values[index+3] = values[index+2] - 1;
/*
      System.out.println( "Poly: " + values[index] + " " +values[index+1] +
                  " " + values[index+2] + " " + values[index+3] +
                  "  (" + x + "," + z + ": " + index + ")" );
*/

                index += 4;
            }
        }

        regenerateNormals(data.coordinates, data.normals);
    }

    /**
     * Generate a new set of points for a triangle strip array. There is one
     * strip for the side and one strip each for the ends.
     *
     * @param data The data to base the calculations on
     * @param transform The geodetic transformation needed
     * @param gridOrigin The location of the SW corner of the grid
     * @param localOrigin an origin offset if needed. Null if not
     * @param creaseAngle angle over which we want to create separate normals
     *   rather than smoothed normals
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void triangleStrips(GeometryData data,
                                MathTransform transform,
                                double[] gridOrigin,
                                double[] localOrigin,
                                double creaseAngle)
        throws InvalidArraySizeException, TransformException
    {
        generateUnindexedTriStripCoordinates(data,
                                             transform,
                                             gridOrigin,
                                             localOrigin);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateUnindexedTriStripNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateUnindexedTriStripTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateTriTexture3D(data);

        int num_strips = depthPoints - 1;
        data.numStrips = num_strips;

        if(data.stripCounts == null)
            data.stripCounts = new int[num_strips];
        else if(data.stripCounts.length < num_strips)
            throw new InvalidArraySizeException("Strip counts",
                                                data.stripCounts.length,
                                                num_strips);

        for(int i = num_strips; --i >= 0; )
            data.stripCounts[i] = widthPoints * 2;
    }

    /**
     * Generates new set of unindexed points for triangles strips. The array
     * consists of one strip per width row.
     *
     * @param data The data to base the calculations on
     * @param transform The geodetic transformation needed
     * @param gridOrigin The location of the SW corner of the grid
     * @param localOrigin an origin offset if needed. Null if not
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriStripCoordinates(GeometryData data,
                                                      MathTransform transform,
                                                      double[] gridOrigin,
                                                      double[] localOrigin)
        throws InvalidArraySizeException, TransformException
    {
        int vtx_cnt = widthPoints * (depthPoints - 1) * 2;
        if(data.coordinates == null)
            data.coordinates = new float[vtx_cnt * 3];
        else if(data.coordinates.length < vtx_cnt * 3)
            throw new InvalidArraySizeException("Coordinates",
                                                data.coordinates.length,
                                                vtx_cnt * 3);

        float[] coords = data.coordinates;
        data.vertexCount = vtx_cnt;

        numTerrainValues = widthPoints * depthPoints * 3;
        terrainCoordinates = new float[numTerrainValues];

        double d = gridOrigin[0];
        double w = gridOrigin[1];
        double width_inc = terrainWidth / (widthPoints - 1);
        double depth_inc = terrainDepth / (depthPoints - 1);

        if (perVertexNormals) {
            terrainNormals = new float[coords.length];
        }

        int count = 0;
        int tc_count = 0;
        int num = widthPoints * (depthPoints - 1);

        // TODO: All of the coordinates are -x,-z to align with GeoLocation and GeoCoordinate
        // note sure why this is needed.

        if(localOrigin != null) {
            for(int i = 1; i <= num; i++) {
                inPosition.setPosition(d + depth_inc,
                                       w,
                                       flatHeights[i + widthPoints - 1] * yScale);

                transform.transform(inPosition, outPosition);

                double[] out = outPosition.getCoordinates();
                coords[count] = -(float)(out[0] - localOrigin[0]);
                coords[count + 1] = (float)(out[1] - localOrigin[1]);
                coords[count + 2] = -(float)(out[2] - localOrigin[2]);

                if (perVertexNormals) {
                    double x = out[0];
                    double y = out[1];
                    double z = out[2];

                    double mag = x * x + y * y + z * z;

                    if(mag != 0.0)
                    {
                        mag = 1.0 / Math.sqrt(mag);
                        terrainNormals[count] = (float) (x * mag);
                        terrainNormals[count + 1] = (float) (y * mag);
                        terrainNormals[count + 2] = (float) (z * mag);
                    }
                    else
                    {
                        terrainNormals[count] = 0;
                        terrainNormals[count + 1] = 1;
                        terrainNormals[count + 2] = 0;
                    }
                }

                // update the first row from data calculated. Otherwise, always
                // take the next row's worth of data.
                if(d == 0) {
                    terrainCoordinates[tc_count] = coords[count];
                    terrainCoordinates[tc_count + 1] = coords[count + 1];
                    terrainCoordinates[tc_count + 2] = coords[count + 2];
                    tc_count += 3;
                }

                inPosition.setPosition(d, w, flatHeights[i - 1] * yScale);

                transform.transform(inPosition, outPosition);

                out = outPosition.getCoordinates();

                if (perVertexNormals) {
                    double x = out[0];
                    double y = out[1];
                    double z = out[2];

                    double mag = x * x + y * y + z * z;

                    if(mag != 0.0)
                    {
                        mag = 1.0 / Math.sqrt(mag);
                        terrainNormals[count + 3] = (float) (x * mag);
                        terrainNormals[count + 4] = (float) (y * mag);
                        terrainNormals[count + 5] = (float) (z * mag);
                    }
                    else
                    {
                        terrainNormals[count + 3] = 0;
                        terrainNormals[count + 4] = 1;
                        terrainNormals[count + 5] = 0;
                    }
                }

                coords[count + 3] = -(float)(out[0] - localOrigin[0]);
                coords[count + 4] = (float)(out[1] - localOrigin[1]);
                coords[count + 5] = -(float)(out[2] - localOrigin[2]);

                terrainCoordinates[tc_count] = coords[count + 3];
                terrainCoordinates[tc_count + 1] = coords[count + 4];
                terrainCoordinates[tc_count + 2] = coords[count + 5];

                count += 6;
                tc_count += 3;

                w += width_inc;

                if(((i % (widthPoints)) == 0))
                {
                    d += depth_inc;
                    w = 0;
                }
            }
        } else {
System.out.println("*** EG no origin: " + num);
            for(int i = 1; i <= num; i++) {
                inPosition.setPosition(d + depth_inc,
                                       w,
                                       flatHeights[i + widthPoints - 1] * yScale);

                transform.transform(inPosition, outPosition);

                double[] out = outPosition.getCoordinates();


                coords[count] = -(float)out[0];
                coords[count + 1] = (float)out[1];
                coords[count + 2] = -(float)out[2];

System.out.println("out1: " + out[0] + " " + out[1] + " " + out[2]);
/*
                coords[count] = -(float)out[0];
                coords[count + 1] = (float)out[1];
                coords[count + 2] = -(float)out[2];
*/
                if (perVertexNormals) {
                    double x = out[0];
                    double y = out[1];
                    double z = out[2];

                    double mag = x * x + y * y + z * z;

                    if(mag != 0.0)
                    {
                        mag = 1.0 / Math.sqrt(mag);
                        terrainNormals[count] = (float) (x * mag);
                        terrainNormals[count + 1] = (float) (y * mag);
                        terrainNormals[count + 2] = (float) (z * mag);
                    }
                    else
                    {
                        terrainNormals[count] = 0;
                        terrainNormals[count + 1] = 1;
                        terrainNormals[count + 2] = 0;
                    }
                }

                // update the first row from data calculated. Otherwise, always
                // take the next row's worth of data.
                if(d == 0) {
                    terrainCoordinates[tc_count] = coords[count];
                    terrainCoordinates[tc_count + 1] = coords[count + 1];
                    terrainCoordinates[tc_count + 2] = coords[count + 2];
                    tc_count += 3;
                }

                inPosition.setPosition(d, w, flatHeights[i - 1] * yScale);

                transform.transform(inPosition, outPosition);

                out = outPosition.getCoordinates();
System.out.println("out2: " + out[0] + " " + out[1] + " " + out[2]);


                coords[count + 3] = -(float)out[0];
                coords[count + 4] = (float)out[1];
                coords[count + 5] = -(float)out[2];
/*
                coords[count + 3] = -(float)out[0];
                coords[count + 4] = (float)out[1];
                coords[count + 5] = -(float)out[2];
*/
                if (perVertexNormals) {
                    double x = out[0];
                    double y = out[1];
                    double z = out[2];

                    double mag = x * x + y * y + z * z;

                    if(mag != 0.0)
                    {
                        mag = 1.0 / Math.sqrt(mag);
                        terrainNormals[count + 3] = (float) (x * mag);
                        terrainNormals[count + 4] = (float) (y * mag);
                        terrainNormals[count + 5] = (float) (z * mag);
                    }
                    else
                    {
                        terrainNormals[count + 3] = 0;
                        terrainNormals[count + 4] = 1;
                        terrainNormals[count + 5] = 0;
                    }
                }

                terrainCoordinates[tc_count] = coords[count + 3];
                terrainCoordinates[tc_count + 1] = coords[count + 4];
                terrainCoordinates[tc_count + 2] = coords[count + 5];

                count += 6;
                tc_count += 3;

                w += width_inc;

                if(((i % (widthPoints)) == 0))
                {
                    d += depth_inc;
                    w = 0;
                }
            }
        }


    }

    /**
     * Generate a new set of normals for a normal set of unindexed points.
     * Smooth normals are used for the sides at the average between the faces.
     * Bottom normals always point down.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to base the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriStripNormals(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.normals == null)
            data.normals = new float[vtx_cnt];
        else if(data.normals.length < vtx_cnt)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt);

        regenerateNormals();

        int i;
        float[] normals = data.normals;
        int count = 0;
        int base_count = 0;
        int width_inc = widthPoints * 3;
        int total_points = widthPoints * (depthPoints - 1);

        if (!perVertexNormals) {
            // Start of with one less row (width) here because we don't have two
            // sets of coordinates for those.
            for(i = total_points; --i >= 0; )
            {
                normals[count++] = terrainNormals[base_count];
                normals[count++] = terrainNormals[base_count + 1];
                normals[count++] = terrainNormals[base_count + 2];

                normals[count++] = terrainNormals[base_count + width_inc];
                normals[count++] = terrainNormals[base_count + width_inc + 1];
                normals[count++] = terrainNormals[base_count + width_inc + 2];

                base_count += 3;
            }
        } else {
            // Straight copy of generated normals
            int len2 = terrainNormals.length;

            for(i=0; i < len2; i++) {
                normals[i] = terrainNormals[i];
            }
        }
    }

    /**
     * Generates new set of unindexed texture coordinates for triangles strips.
     * The array consists of one strip per width row.
     *
     * @param data The data to base the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriStripTexture2D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = widthPoints * (depthPoints - 1) * 2;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt * 2];
        else if(data.textureCoordinates.length < vtx_cnt * 2)
            throw new InvalidArraySizeException("Coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt * 2);

        float[] coords = data.textureCoordinates;

        regenerateTexcoords();

        int i;
        int count = 0;
        int base_count = 0;
        int width_inc = widthPoints * 2;
        int total_points = widthPoints * (depthPoints - 1);

        // Start of with one less row (width) here because we don't have two
        // sets of coordinates for those.
        for(i = total_points; --i >= 0; )
        {
            coords[count++] = terrainTexcoords[base_count + width_inc];
            coords[count++] = terrainTexcoords[base_count + width_inc + 1];
            coords[count++] = terrainTexcoords[base_count];
            coords[count++] = terrainTexcoords[base_count + 1];

            base_count += 2;
        }
    }

    // Indexed generation routines

    /**
     * Generate a new set of points for an indexed triangle strip array. We
     * build the strip from the existing points starting by working around the
     * side and then doing the top and bottom. To create the ends we start at
     * on radius point and then always refer to the center for each second
     * item. This wastes every second triangle as a degenerate triangle, but
     * the gain is less strips needing to be transmitted - ie less memory
     * usage.
     *
     * @param data The data to base the calculations on
     * @param transform The geodetic transformation needed
     * @param gridOrigin The location of the SW corner of the grid
     * @param localOrigin an origin offset if needed. Null if not
     * @param creaseAngle angle over which we want to create separate normals
     *   rather than smoothed normals
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void indexedTriangleStrips(GeometryData data,
                                       MathTransform transform,
                                       double[] gridOrigin,
                                       double[] localOrigin,
                                       double creaseAngle)
        throws InvalidArraySizeException, TransformException {

        generateIndexedCoordinates(data, transform, gridOrigin, localOrigin);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateIndexedNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateTriTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateTriTexture3D(data);

        // now let's do the index list
        int index_size = widthPoints * (depthPoints - 1) * 2;
        int num_strips = depthPoints - 1;

        if(data.indexes == null)
            data.indexes = new int[index_size];
        else if(data.indexes.length < index_size)
            throw new InvalidArraySizeException("Indexes",
                                                data.indexes.length,
                                                index_size);

        if(data.stripCounts == null)
            data.stripCounts = new int[num_strips];
        else if(data.stripCounts.length < num_strips)
            throw new InvalidArraySizeException("Strip counts",
                                                data.stripCounts.length,
                                                num_strips);

        int[] indexes = data.indexes;
        int[] stripCounts = data.stripCounts;
        data.indexesCount = index_size;
        data.numStrips = num_strips;
        int idx = 0;
        int vtx = 0;
        int total_points = widthPoints * (depthPoints - 1);

        // The side is one big strip
        for(int i = total_points; --i >= 0; ) {
            indexes[idx++] = vtx;
            indexes[idx++] = vtx + widthPoints;

            vtx++;
        }

        for(int i = num_strips; --i >= 0; )
            stripCounts[i] = widthPoints * 2;
    }

    /**
     * Generates new set of indexed points for triangles or quads. The array
     * consists of the side coordinates, followed by the center for top, then
     * its points then the bottom center and its points. We do this as they
     * use a completely different set of normals. The side
     * coordinates are interleved as top and then bottom values.
     *
     * @param data The data to base the calculations on
     * @param transform The geodetic transformation needed
     * @param gridOrigin The location of the SW corner of the grid
     * @param localOrigin an origin offset if needed. Null if not
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateIndexedCoordinates(GeometryData data,
                                            MathTransform transform,
                                            double[] gridOrigin,
                                            double[] localOrigin)
        throws InvalidArraySizeException, TransformException {

        int vtx_cnt = widthPoints * depthPoints;

        if(data.coordinates == null)
            data.coordinates = new float[vtx_cnt * 3];
        else if(data.coordinates.length < vtx_cnt * 3)
            throw new InvalidArraySizeException("Coordinates",
                                                data.coordinates.length,
                                                vtx_cnt * 3);

        float[] coords = data.coordinates;
        data.vertexCount = vtx_cnt;

        double d = gridOrigin[0];
        double w = gridOrigin[1];
        double width_inc = terrainWidth / (widthPoints - 1);
        double depth_inc = terrainDepth / (depthPoints - 1);

        int count = 0;
        int num = widthPoints * depthPoints;

        if(localOrigin != null) {
            for(int i = 1; i <= num; i++) {
                inPosition.setPosition(w, flatHeights[i - 1], d);

                transform.transform(inPosition, outPosition);

                double[] out = outPosition.getCoordinates();

                coords[count] = (float)(out[0] - localOrigin[0]);
                coords[count + 1] = (float)(out[1] - localOrigin[1]);
                coords[count + 2] = (float)(out[2] - localOrigin[2]);

                count += 3;

                w += width_inc;

                if(((i % (widthPoints)) == 0)) {
                    d += depth_inc;
                    w = 0;
                }
            }
        } else {
            for(int i = 1; i <= num; i++) {
                inPosition.setPosition(w, flatHeights[i - 1], d);

                transform.transform(inPosition, outPosition);

                double[] out = outPosition.getCoordinates();

                coords[count] = (float)out[0];
                coords[count + 1] = (float)out[1];
                coords[count + 2] = (float)out[2];

                count += 3;

                w += width_inc;

                if(((i % (widthPoints)) == 0)) {
                    d += depth_inc;
                    w = 0;
                }
            }
        }
    }

    /**
     * Generate a new set of normals for a normal set of indexed points.
     * Smooth normals are used for the sides at the average between the faces.
     * Bottom normals always point down.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to base the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateIndexedNormals(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.normals == null)
            data.normals = new float[vtx_cnt];
        else if(data.normals.length < vtx_cnt)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt);

        regenerateNormals();

        System.arraycopy(terrainNormals, 0, data.normals, 0, numTerrainValues);
    }

    /**
     * Generate a new set of texCoords for a set of unindexed points.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to base the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateTriTexture2D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 2;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("2D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        regenerateTexcoords();

        System.out.println("Unhandled textured generation case in " +
            "ElevationGridGenerator");
    }


    /**
     * Generate a new set of texCoords for a set of unindexed points.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to base the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateTriTexture3D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 2;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("3D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        float[] texCoords = data.textureCoordinates;

        System.out.println("Unhandled textured generation case in " +
            "ElevationGridGenerator");
    }

    /**
     * Regenerate the base normals points. These are the flat circle that
     * makes up the base of the code. The normals are generated based the
     * smoothing of normal averages for interior points. Around the edges,
     * we use the average of the edge value polygons.
     */

    private final void regenerateNormals()
    {
        // Already calculated?
        if (perVertexNormals)
            return;

        // This code has been changed around to go from left to right instead
        // of bottom to top, but it hasn't been tested.
        terrainNormals = new float[numTerrainValues];

        int count = 0;
        int base_count = 0;
        int i, j;
        int depth_inc = depthPoints * 3;

        // The first edge
        // corner point - normal based on only that face

        createFaceNormal(terrainCoordinates, depth_inc, 0, 3);

        terrainNormals[count] = normal[0];
        terrainNormals[count + 1] = normal[1];
        terrainNormals[count + 2] = normal[2];

        count = 3;
        base_count = 3;

        for(i = 1; i < (depthPoints - 1); i++)
        {
            calcSideAverageNormal(terrainCoordinates,
                                  base_count,
                                  base_count + 3,
                                  base_count + depth_inc,
                                  base_count - 3);

            terrainNormals[count] = normal[0];
            terrainNormals[count + 1] = normal[1];
            terrainNormals[count + 2] = normal[2];

            count += 3;
            base_count += 3;
        }

        // Last corner point of the first row
        createFaceNormal(terrainCoordinates,
                         base_count,
                         base_count + depth_inc,
                         base_count - 3);

        terrainNormals[count] = normal[0];
        terrainNormals[count + 1] = normal[1];
        terrainNormals[count + 2] = normal[2];

        count += 3;
        base_count += 3;

        // Now, process all of the internal points
        for(i = 1; i < (widthPoints - 1); i++)
        {

            calcSideAverageNormal(terrainCoordinates,
                                  base_count,
                                  base_count - depth_inc,
                                  base_count + 3,
                                  base_count + depth_inc);

            terrainNormals[count] = normal[0];
            terrainNormals[count + 1] = normal[1];
            terrainNormals[count + 2] = normal[2];

            base_count += 3;
            count += 3;

            for(j = 1; j < (depthPoints - 1); j++)
            {

                calcQuadAverageNormal(terrainCoordinates,
                                      base_count,
                                      base_count + 3,
                                      base_count + depth_inc,
                                      base_count - 3,
                                      base_count - depth_inc);

                terrainNormals[count] = normal[0];
                terrainNormals[count + 1] = normal[1];
                terrainNormals[count + 2] = normal[2];

                base_count += 3;
                count += 3;
            }

            // Last point of the row
            calcSideAverageNormal(terrainCoordinates,
                                  base_count,
                                  base_count + depth_inc,
                                  base_count - 3,
                                  base_count - depth_inc);

            terrainNormals[count] = normal[0];
            terrainNormals[count + 1] = normal[1];
            terrainNormals[count + 2] = normal[2];

            base_count += 3;
            count += 3;
        }

        // The last edge
        // corner point - normal based on only that face
        createFaceNormal(terrainCoordinates,
                         base_count,
                         base_count - depth_inc,
                         base_count + 3);

        terrainNormals[count] = normal[0];
        terrainNormals[count + 1] = normal[1];
        terrainNormals[count + 2] = normal[2];

        base_count += 3;
        count += 3;

        for(i = 1; i < (depthPoints - 1); i++)
        {
            calcSideAverageNormal(terrainCoordinates,
                                  base_count,
                                  base_count - 3,
                                  base_count - depth_inc,
                                  base_count + 3);

            terrainNormals[count] = normal[0];
            terrainNormals[count + 1] = normal[1];
            terrainNormals[count + 2] = normal[2];

            base_count += 3;
            count += 3;
        }

        // Last corner point of the first row
        createFaceNormal(terrainCoordinates,
                         base_count,
                         base_count - 3,
                         base_count - depth_inc);

        terrainNormals[count] = normal[0];
        terrainNormals[count + 1] = normal[1];
        terrainNormals[count + 2] = normal[2];
    }

    // This is the original bottom to top code
/*
    private final void regenerateNormals()
    {
        terrainNormals = new float[numTerrainValues];

        int count = 0;
        int base_count = 0;
        int i, j;
        int width_inc = widthPoints * 3;

        // The first edge
        // corner point - normal based on only that face

        createFaceNormal(terrainCoordinates, width_inc, 0, 3);

        terrainNormals[count] = normal[0];
        terrainNormals[count + 1] = normal[1];
        terrainNormals[count + 2] = normal[2];

        count = 3;
        base_count = 3;

        for(i = 1; i < (widthPoints - 1); i++)
        {
            calcSideAverageNormal(terrainCoordinates,
                                  base_count,
                                  base_count + 3,
                                  base_count + width_inc,
                                  base_count - 3);

            terrainNormals[count] = normal[0];
            terrainNormals[count + 1] = normal[1];
            terrainNormals[count + 2] = normal[2];

            count += 3;
            base_count += 3;
        }

        // Last corner point of the first row
        createFaceNormal(terrainCoordinates,
                         base_count,
                         base_count + width_inc,
                         base_count - 3);

        terrainNormals[count] = normal[0];
        terrainNormals[count + 1] = normal[1];
        terrainNormals[count + 2] = normal[2];

        count += 3;
        base_count += 3;

        // Now, process all of the internal points
        for(i = 1; i < (depthPoints - 1); i++)
        {

            calcSideAverageNormal(terrainCoordinates,
                                  base_count,
                                  base_count - width_inc,
                                  base_count + 3,
                                  base_count + width_inc);

            terrainNormals[count] = normal[0];
            terrainNormals[count + 1] = normal[1];
            terrainNormals[count + 2] = normal[2];

            base_count += 3;
            count += 3;

            for(j = 1; j < (widthPoints - 1); j++)
            {

                calcQuadAverageNormal(terrainCoordinates,
                                      base_count,
                                      base_count + 3,
                                      base_count + width_inc,
                                      base_count - 3,
                                      base_count - width_inc);

                terrainNormals[count] = normal[0];
                terrainNormals[count + 1] = normal[1];
                terrainNormals[count + 2] = normal[2];

                base_count += 3;
                count += 3;
            }

            // Last point of the row
            calcSideAverageNormal(terrainCoordinates,
                                  base_count,
                                  base_count + width_inc,
                                  base_count - 3,
                                  base_count - width_inc);

            terrainNormals[count] = normal[0];
            terrainNormals[count + 1] = normal[1];
            terrainNormals[count + 2] = normal[2];

            base_count += 3;
            count += 3;
        }

        // The last edge
        // corner point - normal based on only that face
        createFaceNormal(terrainCoordinates,
                         base_count,
                         base_count - width_inc,
                         base_count + 3);

        terrainNormals[count] = normal[0];
        terrainNormals[count + 1] = normal[1];
        terrainNormals[count + 2] = normal[2];

        base_count += 3;
        count += 3;

        for(i = 1; i < (widthPoints - 1); i++)
        {
            calcSideAverageNormal(terrainCoordinates,
                                  base_count,
                                  base_count - 3,
                                  base_count - width_inc,
                                  base_count + 3);

            terrainNormals[count] = normal[0];
            terrainNormals[count + 1] = normal[1];
            terrainNormals[count + 2] = normal[2];

            base_count += 3;
            count += 3;
        }

        // Last corner point of the first row
        createFaceNormal(terrainCoordinates,
                         base_count,
                         base_count - 3,
                         base_count - width_inc);

        terrainNormals[count] = normal[0];
        terrainNormals[count + 1] = normal[1];
        terrainNormals[count + 2] = normal[2];
    }
*/
    /**
     * Convenience method to calculate the average normal value between
     * two quads - ie along the side of an object
     *
     * @param coords The coordinates to generate from
     * @param p The centre point
     * @param p1 The first point of the first side
     * @param p2 The middle, shared side point
     * @param p3 The last point of the second side
     * @return The averaged vector
     */
    private void calcSideAverageNormal(float[] coords,
                                       int p,
                                       int p1,
                                       int p2,
                                       int p3)
    {
        float x, y, z;

        // Normal first for the previous quad
        createFaceNormal(coords, p, p1, p2);
        x = normal[0];
        y = normal[1];
        z = normal[2];

        // Normal for the next quad
        createFaceNormal(coords, p, p2, p3);

        // create the average of each compoenent for the final normal
        normal[0] = (normal[0] + x) * 0.5f;
        normal[1] = (normal[1] + y) * 0.5f;
        normal[2] = (normal[2] + z) * 0.5f;

        float mag = normal[0] * normal[0] +
                    normal[1] * normal[1] +
                    normal[2] * normal[2];

        if(mag != 0.0f)
        {
            mag = 1.0f / ((float) Math.sqrt(mag));
            normal[0] = normal[0] * mag;
            normal[1] = normal[1] * mag;
            normal[2] = normal[2] * mag;
        }
        else
        {
            normal[0] = 0;
            normal[1] = 0;
            normal[2] = 0;
        }
    }

    /**
     * Convenience method to create quad average normal amongst four
     * quads based around a common centre point (the one having the normal
     * calculated).
     *
     * @param coords The coordinates to generate from
     * @param p The centre point
     * @param p1 shared point between first and last quad
     * @param p2 shared point between first and second quad
     * @param p3 shared point between second and third quad
     * @param p4 shared point between third and fourth quad
     * @return The averaged vector
     */
    private void calcQuadAverageNormal(float[] coords,
                                       int p,
                                       int p1,
                                       int p2,
                                       int p3,
                                       int p4)
    {
        float x, y, z;

        // Normal first for quads 1 & 2
        createFaceNormal(coords, p, p2, p1);
        x = normal[0];
        y = normal[1];
        z = normal[2];

        // Normal for the quads 2 & 3
        createFaceNormal(coords, p, p2, p3);
        x += normal[0];
        y += normal[1];
        z += normal[2];

        // Normal for quads 3 & 4
        createFaceNormal(coords, p, p3, p4);
        x += normal[0];
        y += normal[1];
        z += normal[2];

        // Normal for quads 1 & 4
        createFaceNormal(coords, p, p4, p1);

        // create the average of each compoenent for the final normal
        normal[0] = (normal[0] + x) * 0.25f;
        normal[1] = (normal[1] + y) * 0.25f;
        normal[2] = (normal[2] + z) * 0.25f;

        float mag = normal[0] * normal[0] +
                    normal[1] * normal[1] +
                    normal[2] * normal[2];

        if(mag != 0.0f)
        {
            mag = 1.0f / ((float) Math.sqrt(mag));
            normal[0] = normal[0] * mag;
            normal[1] = normal[1] * mag;
            normal[2] = normal[2] * mag;
        }
        else
        {
            normal[0] = 0;
            normal[1] = 0;
            normal[2] = 0;
        }
    }

    /**
     * Regenerate the texture coordinate points.
     * Assumes regenerateBase has been called before this
     */
    private final void regenerateTexcoords()
    {
        numTexcoordValues = widthPoints * depthPoints * 2;
        terrainTexcoords = new float[numTexcoordValues];

        float d = 0;
        float w = 0;
        float width_inc = 1.0f / (widthPoints - 1);
        float depth_inc = 1.0f / (depthPoints - 1);

        int count = 0;

        if(flatHeights != null)
        {
            int num = numTerrainValues / 3;
            for(int i = 1; i <= num; i++)
            {

                terrainTexcoords[count++] = w;
                terrainTexcoords[count++] = d;
                w += width_inc;

                if(((i % (widthPoints)) == 0))
                {
                    d += depth_inc;
                    w = 0;
                }
            }
        }
        else
        {
            for(int i = 0; i < depthPoints; i++)
            {
                for(int j = 0;  j < widthPoints; j++)
                {

                    terrainTexcoords[count++] = w;
                    terrainTexcoords[count++] = d;


                    w += width_inc;
                }

                d += depth_inc;
                w = 0;
            }
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
     * @return A temporary value containing the normal value
     */
    private void createFaceNormal(float[] coords, int p, int p1, int p2)
    {
        float x1 = coords[p1]     - coords[p];
        float y1 = coords[p1 + 1] - coords[p + 1];
        float z1 = coords[p1 + 2] - coords[p + 2];

        float x2 = coords[p]     - coords[p2];
        float y2 = coords[p + 1] - coords[p2 + 1];
        float z2 = coords[p + 2] - coords[p2 + 2];

        float x = y1 * z2 - z1 * y2;
        float y = z1 * x2 - x1 * z2;
        float z = x1 * y2 - y1 * x2;

        float mag = x * x + y * y + z * z;

// TODO: Flipping normals, not sure why this is needed
        if(mag != 0.0f)
        {
            mag = 1.0f / ((float) Math.sqrt(mag));
            normal[0] = -x * mag;
            normal[1] = -y * mag;
            normal[2] = -z * mag;
        }
        else
        {
            normal[0] = 0;
            normal[1] = 0;
            normal[2] = 0;
        }
    }

    /**
     * Regenerate the base normals points. These are the flat circle that
     * makes up the base of the code. The normals are generated based the
     * smoothing of normal averages for interior points. Around the edges,
     * we use the average of the edge value polygons.
     */
    private final void regenerateNormals(float[] coords, float[] normals)
    {
        int count = 0;
        int base_count = 0;
        int i, j;
        int width_inc = widthPoints * 3;

        // The first edge
        // corner point - normal based on only that face
        createFaceNormal(coords, width_inc, 0, 3);

        normals[count++] = normal[0];
        normals[count++] = normal[1];
        normals[count++] = normal[2];

        base_count = 3;

        for(i = 1; i < (widthPoints - 1); i++)
        {
            calcSideAverageNormal(coords,
                                         base_count,
                                         base_count + 3,
                                         base_count + width_inc,
                                         base_count - 3);

            normals[count++] = normal[0];
            normals[count++] = normal[1];
            normals[count++] = normal[2];

            base_count += 3;
        }

        // Last corner point of the first row
        createFaceNormal(coords,
                                base_count,
                                base_count + width_inc,
                                base_count - 3);

        normals[count++] = normal[0];
        normals[count++] = normal[1];
        normals[count++] = normal[2];

        base_count += 3;

        // Now, process all of the internal points
        for(i = 1; i < (depthPoints - 1); i++)
        {

            calcSideAverageNormal(coords,
                                         base_count,
                                         base_count - width_inc,
                                         base_count + 3,
                                         base_count + width_inc);

            normals[count++] = normal[0];
            normals[count++] = normal[1];
            normals[count++] = normal[2];

            base_count += 3;

            for(j = 1; j < (widthPoints - 1); j++)
            {

                calcQuadAverageNormal(coords,
                                             base_count,
                                             base_count + 3,
                                             base_count + width_inc,
                                             base_count - 3,
                                             base_count - width_inc);

                normals[count++] = normal[0];
                normals[count++] = normal[1];
                normals[count++] = normal[2];

                base_count += 3;
            }

            // Last point of the row
            calcSideAverageNormal(coords,
                                         base_count,
                                         base_count + width_inc,
                                         base_count - 3,
                                         base_count - width_inc);

            normals[count++] = normal[0];
            normals[count++] = normal[1];
            normals[count++] = normal[2];

            base_count += 3;
        }

        // The last edge
        // corner point - normal based on only that face
        createFaceNormal(coords,
                                base_count,
                                base_count - width_inc,
                                base_count + 3);

        normals[count++] = normal[0];
        normals[count++] = normal[1];
        normals[count++] = normal[2];

        base_count += 3;

        for(i = 1; i < (widthPoints - 1); i++)
        {
            calcSideAverageNormal(coords,
                                         base_count,
                                         base_count - 3,
                                         base_count - width_inc,
                                         base_count + 3);

            normals[count++] = normal[0];
            normals[count++] = normal[1];
            normals[count++] = normal[2];

            base_count += 3;
        }

        // Last corner point of the first row
        createFaceNormal(coords,
                                base_count,
                                base_count - 3,
                                base_count - width_inc);

        normals[count++] = normal[0];
        normals[count++] = normal[1];
        normals[count++] = normal[2];
    }

    // Float pretty printer

    private String pp(float[] num, int places) {
        StringBuffer buff = new StringBuffer();

        for (int j=0; j < num.length; j++) {
            String sfloat = Float.toString(num[j]);

            int needs = places - sfloat.length();

            if (needs == 0) {
                buff.append(sfloat);
                buff.append(" ");
            } else if (needs < 0) {
                buff.append(sfloat.substring(0,places));
                buff.append(" ");
            } else {
                for(int i=0; i < needs; i++) {
                    sfloat += " ";
                }

                buff.append(sfloat);
                buff.append(" ");
            }
        }

        return buff.toString();
    }

}
