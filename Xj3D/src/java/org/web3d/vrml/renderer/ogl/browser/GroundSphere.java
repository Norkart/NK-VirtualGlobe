/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.browser;

// External imports
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;

import org.j3d.geom.NormalUtils;
import org.j3d.geom.SphereGenerator;
import org.j3d.geom.GeometryData;
import org.j3d.geom.terrain.ColorRampGenerator;

import org.j3d.aviatrix3d.*;

// Local imports
// None

/**
 * Convenience class that represents the ground sphere part of the background
 * as a simple container class.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
class GroundSphere extends Shape3D
    implements NodeUpdateListener {

    /** Number of facets to include in the X-Z plane of the sky/ground */
    private static final int DEFAULT_NUM_SPHERE_FACES = 32;

    /** Table for the sine values for generating the sky sphere */
    private static final float[] SIN_TABLE;

    /** Table for the cosine values for generating the sky sphere */
    private static final float[] COS_TABLE;

    /** Data holder for the ground geometry */
    private GeometryData groundData;

    /** Temp array to create sky color heights with for the ramp */
    private float[] gndHeights;

    /** Max angle for the ground. Used to work out if we need to retessellate */
    private float gndMaxAngle;

    /** Temp array to create sky colors with for the ramp. Also, if single  */
    private float[][] gndColors;

    /** Number of valid values in the sky colour array */
    private int numGndColors;

    /** The current transparency value */
    private float transparency;

    /** Color interpolator used for generating sky/ground color values */
    private ColorRampGenerator gndColorCreator;

    /** Aviatrix Geometry used by the sky sphere */
    private TriangleStripArray groundGeometry;

    /** Appearance used to control this item's visibility */
    private Appearance appearance;

    /** Material for handling the amount of transparency */
    private Material material;

    /**
     * Static initializer to create the sin/cos tables and the values
     * contained in them.
     */
    static {
        COS_TABLE = new float[DEFAULT_NUM_SPHERE_FACES+1];
        SIN_TABLE = new float[DEFAULT_NUM_SPHERE_FACES+1];

        double segment_angle = 2.0 * Math.PI / DEFAULT_NUM_SPHERE_FACES;

        for(int i = 0; i < DEFAULT_NUM_SPHERE_FACES + 1; i++) {
            COS_TABLE[i] = (float)Math.cos(segment_angle * i + Math.PI / 2);
            SIN_TABLE[i] = (float)Math.sin(segment_angle * i + Math.PI / 2);
        }
    }

    /**
     * Create a new instance with the headlight off and controls over whether
     * the code will be used in a static or dynamic environment.
     */
    GroundSphere() {

        gndMaxAngle = 0;
        transparency = 1;
        groundData = new GeometryData();
        groundData.geometryType = GeometryData.TRIANGLE_STRIPS;
        groundData.geometryComponents = GeometryData.NORMAL_DATA;

        material = new Material();
        material.setLightingEnabled(false);

        BlendAttributes ba = new BlendAttributes();
        ba.setSourceBlendFactor(ba.BLEND_SRC_ALPHA);
        ba.setDestinationBlendFactor(ba.BLEND_ONE_MINUS_SRC_ALPHA);

        appearance = new Appearance();
        appearance.setMaterial(material);
        appearance.setBlendAttributes(ba);
        appearance.setVisible(false);

        groundGeometry = new TriangleStripArray(true, VertexGeometry.VBO_HINT_STATIC);

        setAppearance(appearance);
        setGeometry(groundGeometry);
    }

    /**
     * Update the background ground color sphere to the new values.
     *
     * @param color The color values to use as a flat array
     * @param angles The angles to use at each colour boundary
     * @param num The number of color values to read from the arrays
     */
    void updateGroundColors(float[] color, float[] angles, int num) {

        if(num > 1)
            updateGroundSphereGeom(color, angles, num);

        numGndColors = num;

        if(appearance.isLive())
            appearance.dataChanged(this);
        else
            updateNodeDataChanges(appearance);
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
        groundGeometry.setVertices(TriangleStripArray.COORDINATE_3,
                                   groundData.coordinates,
                                   groundData.vertexCount);

        groundGeometry.setStripCount(groundData.stripCounts, groundData.numStrips);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        if(src == appearance) {
            appearance.setVisible(numGndColors != 0);
        } else if(src == groundGeometry) {
            groundGeometry.setColors(true, groundData.colors);
        } else if(src == material) {
            material.setTransparency(transparency);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Update the background transparency to this new value. If the value is
     * 1.0, then disable all the background rendering. A value of 0 is
     * completely opaque and a value of 1 is clear.
     * <p>
     * Make sure to call this before updateSkyColors so that the array can
     * pick up this information too when regenerating the sphere.
     *
     * @param transparency A value between 0 and 1
     */
    void updateBackgroundTransparency(float transparency) {
        this.transparency = 1.0f - transparency;

        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(material);

        if(numGndColors > 1) {
            if (appearance.isLive())
                appearance.dataChanged(this);
            else
                updateNodeDataChanges(appearance);
        }
    }

    /**
     * Create the array holding the coordinates, normals and colors for the gnd
     * sphere.
     *
     * @param color The color values to use as a flat array
     * @param angles The angles to use at each colour boundary
     * @param num The number of color values to read from the arrays
     */
    void updateGroundSphereGeom(float[] color, float[] angles, int num) {
        // Update the color interpolator for the gnd. First color is always
        // straight down the -Y axis, so assumed angle of zero.
        if(gndColorCreator == null)
            gndColorCreator = new ColorRampGenerator();

        boolean extra_color = (angles[num - 1] < Math.PI);

        if((gndHeights == null) || (gndHeights.length != num)) {
            gndHeights = new float[num];
        }

        // Ground heights start with 0.0 radians as being straight
        // down the -Y axis.
        gndHeights[0] = -1;
        for(int i = 1; i < num; i++)
            gndHeights[i] = -(float)Math.cos(angles[i - 1]);

        // Add alpha channel to the colors.
        float[] alpha_color = new float[num * 4];
        for(int i = 0; i < num; i++) {
            alpha_color[i * 4] = color[i * 3];
            alpha_color[i * 4 + 1] = color[i * 3 + 1];
            alpha_color[i * 4 + 2] = color[i * 3 + 2];
            alpha_color[i * 4 + 3] = transparency;
        }

        gndColorCreator.setColorRamp(gndHeights, alpha_color, num, true);

        if(angles[num - 2] != gndMaxAngle) {
            generateGroundCoords(angles[num - 2], num);
            gndMaxAngle = angles[num - 2];

            if (groundGeometry.isLive())
                groundGeometry.dataChanged(this);
            else
                updateNodeDataChanges(groundGeometry);
        }

        // update the geometry color value here.
        gndColorCreator.generate(groundData);

        if (groundGeometry.isLive())
            groundGeometry.boundsChanged(this);
        else
            updateNodeBoundsChanges(groundGeometry);
    }

    /**
     * Generate the geometry needed for a partial sphere of the given
     * maximum angle.
     */
    private void generateGroundCoords(float maxAngle, int numColors) {

        int num_inc = numColors > DEFAULT_NUM_SPHERE_FACES ?
                      numColors :
                      DEFAULT_NUM_SPHERE_FACES;

        float angle_inc = maxAngle / num_inc;
        int num_coords = (DEFAULT_NUM_SPHERE_FACES + 1) * (num_inc);

        // Allocate arrays for the geometry as well as strip counts
        // and index counts.
        if((groundData.coordinates == null) ||
           (groundData.vertexCount < (num_coords * 6))) {
            groundData.coordinates = new float[num_coords * 6];
        }

        int[] strips = new int[num_inc];

        groundData.stripCounts = strips;
        groundData.numStrips = num_inc;

        int coord_idx = 0;

        for(int i = 0; i < num_inc; i++) {
            float r = 0.8f * (float)Math.sin(angle_inc * i);
            float r2 = 0.8f * (float)Math.sin(angle_inc * (i+1));
            float h = -(float)Math.cos(angle_inc * i);
            float h2 = -(float)Math.cos(angle_inc * (i + 1));

            strips[i] = (DEFAULT_NUM_SPHERE_FACES+1) << 1;

            for(int j = 0; j <= DEFAULT_NUM_SPHERE_FACES; j++) {
                groundData.coordinates[coord_idx++] = r * SIN_TABLE[j];
                groundData.coordinates[coord_idx++] = h;
                groundData.coordinates[coord_idx++] = r * COS_TABLE[j];

                groundData.coordinates[coord_idx++] = r2 * SIN_TABLE[j];
                groundData.coordinates[coord_idx++] = h2;
                groundData.coordinates[coord_idx++] = r2 * COS_TABLE[j];
            }
        }
        groundData.vertexCount = coord_idx / 3;

        int color_cnt = groundData.vertexCount * 4;
        if (groundData.colors == null || groundData.colors.length < color_cnt)
            groundData.colors = new float[color_cnt];
    }
}
