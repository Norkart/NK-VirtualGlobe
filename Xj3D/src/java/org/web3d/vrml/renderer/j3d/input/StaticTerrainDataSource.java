/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.input;

// Standard imports
import java.awt.Rectangle;

import javax.media.j3d.Texture;

import javax.vecmath.Point2d;

// Application specific imports
import org.j3d.terrain.AbstractStaticTerrainData;
import org.j3d.util.interpolator.ColorInterpolator;

/**
 * A static source of terrain data.  Used for overhead maps or small areas.
 * <p>
 *
 * If a color interpolator is not provided, then color is not supported in this
 * terrain (unless set by some implementing class).
 *
 * @author  Alan Hudson
 * @version $Revision: 1.2 $
 */
public class StaticTerrainDataSource extends AbstractStaticTerrainData
{
    // Global Implementation Note:
    // There is a lot of mixing of single and double precision floating point
    // data here. Internally, Java3D turns everything into single precision,
    // and as this is concerned with the rendering rather than representation
    // precision, we favour single precision calculations. The advantage here
    // is that we can use single FP parts of the CPU rather than needing to
    // use the more expensive double precision versions of the same
    // calculations. For this reason, you will see in the code that anywhere
    // we clash with single and double precision, we attempt to force the
    // values to single precision as early as possible rather than letting
    // everything bubble up to the greatest precision and then casting the
    // final result back to single precision floats.

    /** The width of the terrain in grid points. (X coordinate) */
    private int gridWidth;

    /** The depth of the terrain in grid points. (Z coordinate) */
    private int gridDepth;

    /** The height values */
    private double[] heightMap;

    /** A mapping of gridX.gridY to texture coordinates */
    public float[][][] tcMap;

    /** The colour interpolator used by this class */
    private ColorInterpolator colorInterp;

    private int hmWidth;
    private int hmDepth;

    private float xMin = -2707029 / (100000 / 2);
    private float zMin = -4866123 / (100000 / 2);
    private float xRange = 442404 / (100000 / 2);
    private float zRange = 392237 / (100000 / 2);

    /**
     * Create a new instance that uses the passed height map data to this
     * loader. The data passed can be either referenced or copied, depending
     * on the value of the <code>mustCopy</code> parameter. If it is not
     * copied, then the calling code should make sure that it does not change
     * values in the array after calling this method. If copying, the code
     * assumes a rectangular grid of points where the second dimension size is
     * based on <code>data[0].length</code>.
     *
     * @param data The source data to use in [length][width] order
     * @param mustCopy true to request an internal copy be made of the data
     *    false for it to just reference the data
     * @param stepDetails The distance between each height value in the X and
     *    Z coordinates (Y in terrain parlance)
     * @param xDimension The number of data points in the X direction
     * @param zDimension The number of data points in the Z direction
     */
    public StaticTerrainDataSource(double[] data,
                                boolean mustCopy,
                                double[] stepDetails,
                                int xDimension, int zDimension,
                                int gd, int gw)
    {
        if(mustCopy)
        {
            int size = xDimension*zDimension;
            heightMap = new double[size];

            System.arraycopy(data, 0, heightMap, 0, size);

            gridDepth = (gd+1)*2;
            gridWidth = (gw+1)*2;
            hmWidth = xDimension;
            hmDepth = zDimension;
        }
        else
        {
            heightMap = data;

            gridDepth = (gd+1)*2;
            gridWidth = (gw+1)*2;
            hmWidth = xDimension;
            hmDepth = zDimension;
        }

        gridStepX = stepDetails[0];
        gridStepY = stepDetails[1];
    }

    /**
     * Check to see if this terrain data has any texturing at all - either
     * tiled or simple.
     *
     * @return true If a texture(s) is available
     */
    public boolean hasTexture()
    {
        return true;
    }

    //----------------------------------------------------------
    // Methods required by StaticTerrainData
    //----------------------------------------------------------

    /**
     * Get the width (number of points on the Y axis) of the grid.
     *
     * @return The number of points in the width of the grid
     */
    public int getGridWidth() {
        return gridWidth + 1;
    }

    /**
     * Get the depth (number of points on the X axis) of the grid.
     *
     * @return The number of points in the depth of the grid
     */
    public int getGridDepth() {
        return gridDepth + 1;
    }

    //----------------------------------------------------------
    // Methods required by HeightDataSource
    //----------------------------------------------------------

    /**
     * Get the height at the given X,Z coordinate in the local coordinate
     * system. The
     *
     * @param x The x coordinate for the height sampling
     * @param z The z coordinate for the height sampling
     * @return The height at the current point or NaN
     */
    public float getHeight(float x, float z) {

        // work out where we are in the grid first. Rememeber that we have
        // to convert between coordinate systems
        float rel_x_pos = x / (float)gridStepX;
        float rel_y_pos = z / (float)gridStepY;

        // fetch the coords of the four heights surrounding this point
        int x_coord = (int)Math.floor(rel_x_pos);
        int y_coord = (int)Math.floor(rel_y_pos);

        // This algorithm sucks. It should be much nicer, but I'm lazy and
        // want to do some other things ATM......

        if((x_coord < 0) || (y_coord < 0) ||
           (x_coord + 1 >= gridWidth) || (y_coord + 1 >= gridDepth))
        {
           return Float.NaN;
        }

        double h1 = heightMap[x_coord + y_coord * gridWidth];
        double h2 = heightMap[x_coord + (y_coord + 1) * gridWidth];
        double h3 = heightMap[x_coord + 1 + (y_coord * gridWidth)];
        double h4 = heightMap[x_coord + 1 + (y_coord + 1) * gridWidth];

        // return the average height
        return (float)((h1 + h2 + h3 + h4) * 0.25);
    }

    //----------------------------------------------------------
    // Methods required by TerrainData
    //----------------------------------------------------------

    /**
     * Get the coordinate of the point in the grid.
     *
     * @param coord the x, y, and z coordinates will be placed in the
     *    first three elements of the array.
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     */
    public void getCoordinate(float[] coord, int gridX, int gridY)
    {

        coord[0] = gridX * (float)gridStepX;
        coord[2] = -gridY * (float)gridStepY;

        int g_x = 0;
        int g_y = 0;

/*        if(gridX >= gridWidth)
            g_x = gridWidth - 1;
        else if(gridX > 0)
            g_x = gridX;

        if(gridY >= gridDepth)
            g_y = gridDepth - 1;
        else if(gridY > 0)
            g_y = gridY;

        g_x = (gridX / (gridWidth)) * (hmWidth-1);
        g_y = (gridX / (gridDepth)) * (hmDepth-1);
*/
        coord[1] = (float) heightMap[g_x + (g_y * gridWidth)];
    }

    /**
     * Get the coordinate with all the information - texture and colors.
     *
     * @param coord he x, y, and z coordinates will be placed in the first
     *   three elements of the array.
     * @param tex 2D coordinates are placed in the first two elements
     * @param color 3 component colors are placed in the first 3 elements
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     */
    public void getCoordinate(float[] coord,
                              float[] tex,
                              float[] color,
                              int gridX,
                              int gridY)
    {
        int g_x = 0;
        int g_y = 0;

        if(gridX >= gridWidth)
            g_x = gridWidth - 1;
        else if(gridX > 0)
            g_x = gridX;

        if(gridY >= gridDepth)
            g_y = gridDepth - 1;
        else if(gridY > 0)
            g_y = gridY;

        coord[1] = (float) heightMap[g_x + (g_y * gridWidth)];

        coord[0] = gridX * (float)gridStepX;
        coord[2] = -gridY * (float)gridStepY;

        tex[0] = ((float)gridX) / (gridWidth - 1);
        tex[1] = ((float)gridY) / (gridDepth - 1);

        if(gridX >= 0 && gridY >= 0 && gridX < gridWidth && gridY < gridDepth)
        {
            float[] rgb = colorInterp.floatRGBValue(coord[1]);
            color[0] = rgb[0];
            color[1] = rgb[1];
            color[2] = rgb[2];
        }
        else
        {
            color[0] = 1;
            color[1] = 1;
            color[2] = 1;
        }
    }

    /**
     * Get the coordinate of the point and correspond texture coordinate in
     * the grid. Assumes that the grid covers a single large texture rather
     * than multiple smaller textures.
     *
     * @param coord he x, y, and z coordinates will be placed in the first
     *   three elements of the array.
     * @param textureCoord 2D coordinates are placed in the first two elements
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     */
    public void getCoordinateWithTexture(float[] coord,
                                         float[] textureCoord,
                                         int gridX,
                                         int gridY,
                                         int patchX,
                                         int patchY)
    {
        int g_x = 0;
        int g_y = 0;

        float p = ((float)gridX / gridWidth);
        g_x = Math.round(p * ((float)hmWidth-1.0f));
        p = ((float)gridY / gridDepth);
        g_y = Math.round(p * ((float)hmDepth-1.0f));

        coord[1] = (float) heightMap[g_x + (g_y * hmWidth)];
//System.out.println("gridX: " + gridX + " gridY: " + gridY + " h: " + coord[1]);

        coord[0] = gridX * (float)gridStepX;
//        coord[2] = -gridY * (float)gridStepY;
        coord[2] = gridY * (float)gridStepY;

/*
        textureCoord[0] = ((float)gridX) / (gridWidth);
        textureCoord[1] = 1.0f - ((float)gridY) / (gridDepth);
*/

        textureCoord[0] = tcMap[gridX][gridY][0];
        textureCoord[1] = tcMap[gridX][gridY][1];

//System.out.println("x: " + coord[0] + " s: " + textureCoord[0] + " z: " + coord[2] + " t: " + textureCoord[1]);
//System.out.println("x: " + coord[0] + " z: " + coord[2] + " gx: " + gridX + " gy: " + gridY);
//System.out.println("gridX: " + gridX + " gridY: " + gridY + " gx: " + g_x + " gy: " + g_y + " tx: " + textureCoord[0] + " ty: " + textureCoord[1]);
    }

    /**
     * Get the coordinate of the point and the corresponding color value in
     * the grid. Color values are used when there is no texture supplied, so
     * this should always provide something useful.
     *
     * @param coord he x, y, and z coordinates will be placed in the first
     *   three elements of the array.
     * @param color 3 component colors are placed in the first 3 elements
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     */
    public void getCoordinateWithColor(float[] coord,
                                       float[] color,
                                       int gridX,
                                       int gridY)
    {
        int g_x = 0;
        int g_y = 0;

        if(gridX >= gridWidth)
            g_x = gridWidth - 1;
        else if(gridX > 0)
            g_x = gridX;

        if(gridY >= gridDepth)
            g_y = gridDepth - 1;
        else if(gridY > 0)
            g_y = gridY;

        float height = (float) heightMap[g_x + (g_y * gridWidth)];
        coord[0] = gridX * (float)gridStepX;
        coord[1] = height;
        coord[2] = -gridY * (float)gridStepY;

        if(gridX >= 0 && gridY >= 0 && gridX < gridWidth && gridY < gridDepth)
        {
            float[] rgb = colorInterp.floatRGBValue(coord[1]);
            color[0] = rgb[0];
            color[1] = rgb[1];
            color[2] = rgb[2];
        }
        else
        {
            color[0] = 1;
            color[1] = 1;
            color[2] = 1;
        }
    }

    /**
     * Get the height at the specified grid position.
     *
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     * @return The height at the given grid position
     */
    public float getHeightFromGrid(int gridX, int gridY)
    {
        int g_x = 0;
        int g_y = 0;
/*
        if(gridX >= gridWidth)
            g_x = gridWidth - 1;
        else if(gridX > 0)
            g_x = gridX;

        if(gridY >= gridDepth)
            g_y = gridDepth - 1;
        else if(gridY > 0)
            g_y = gridY;
*/

        float p = ((float)gridX / gridWidth);
        g_x = Math.round(p * ((float)hmWidth-1.0f));
        p = ((float)gridY / gridDepth);
        g_y = Math.round(p * ((float)hmDepth-1.0f));

//System.out.println("height: " + " gridX: " + gridX + " gridY: " + gridY + " x: " + g_x + " y: " + g_y + " hmWidth: " + hmWidth + " hmDepth: " + hmDepth + " idx: " + (g_x + (g_y * hmWidth)));
        return (float) heightMap[g_x + (g_y * hmWidth)];
    }


    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set up a height color ramp to provide colour information. This should
     * be set before passing the terrain data to a rendering algorithm as it
     * sets the hasColor() flag to true. Heights should be based on sea-level
     * as value zero. A value of null clears the current reference.
     *
     * @param interp The interpolator instance to use
     */
    public void setColorInterpolator(ColorInterpolator interp)
    {
        colorInterp = interp;

        colorAvailable = (colorInterp != null);
    }
}
