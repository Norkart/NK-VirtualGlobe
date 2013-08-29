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
import org.web3d.vrml.renderer.ogl.input.OGLGlobalEffectsHandler;

/**
 * Represents the geometry needed to create the sky portion of the skyColor
 * fields of the background.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
class SkySphere extends Shape3D
    implements NodeUpdateListener {

    /** Number of facets to include in the X-Z plane of the sky/ground */
    private static final int DEFAULT_NUM_SPHERE_FACES = 32;

    /** Table for the sine values for generating the sky sphere */
    private static final float[] SIN_TABLE;

    /** Table for the cosine values for generating the sky sphere */
    private static final float[] COS_TABLE;

    /** Appearance for handling the visibility state */
    private Appearance appearance;

    /** Material for handling the amount of transparency */
    private Material material;

    /** Current number of colours being used in the sky sphere */
    private int currentNumSkyColors;

    /** Color interpolator used for generating sky/ground color values */
    private ColorRampGenerator skyColorCreator;

    /** Data holder for the sky geometry */
    private GeometryData skyData;

    /** The maximum number of facets used in the sky sphere */
    private int maxSkyColors;

    /** Temp array to create sky color heights with for the ramp */
    private float[] skyHeights;

    /** Temp array to create sky colors with for the ramp */
    private float[][] skyColors;

    /** Number of valid values in the sky colour array */
    private int numSkyColors;

    /** The current transparency value */
    private float transparency;

    /** Aviatrix Geometry used by the sky sphere */
    private TriangleStripArray skyGeometry;

    /**
     * Static initializer to create the sin/cos tables and the values
     * contained in them.
     */
    static {
        COS_TABLE = new float[DEFAULT_NUM_SPHERE_FACES];
        SIN_TABLE = new float[DEFAULT_NUM_SPHERE_FACES];

        double segment_angle = 2.0 * Math.PI / DEFAULT_NUM_SPHERE_FACES;

        for(int i = 0; i < DEFAULT_NUM_SPHERE_FACES; i++) {
            COS_TABLE[i] = (float)Math.cos(segment_angle * i + Math.PI / 2);
            SIN_TABLE[i] = (float)Math.sin(segment_angle * i + Math.PI / 2);
        }
    }

    /**
     * Create a new instance with the headlight off and controls over whether
     * the code will be used in a static or dynamic environment.
     */
    SkySphere() {

        skyColors = new float[1][4];

        currentNumSkyColors = 0;
        maxSkyColors = 0;
        transparency = 1;

        skyData = new GeometryData();
        skyData.geometryType = GeometryData.TRIANGLE_STRIPS;
        skyData.geometryComponents = GeometryData.NORMAL_DATA;

        generateSphereGeom(DEFAULT_NUM_SPHERE_FACES);

        // Allocate the color array directly because the generate()
        // call will not have done that. We'll need it for later on
        // anyway.
        skyData.colors = new float[skyData.vertexCount * 4];

        material = new Material();
        material.setLightingEnabled(false);

        BlendAttributes ba = new BlendAttributes();
        ba.setSourceBlendFactor(ba.BLEND_SRC_ALPHA);
        ba.setDestinationBlendFactor(ba.BLEND_ONE_MINUS_SRC_ALPHA);

        appearance = new Appearance();
        appearance.setMaterial(material);
        appearance.setBlendAttributes(ba);
        appearance.setVisible(false);

        skyGeometry = new TriangleStripArray(true, VertexGeometry.VBO_HINT_STATIC);
        skyGeometry.setVertices(TriangleStripArray.COORDINATE_3,
                                skyData.coordinates,
                                skyData.vertexCount);
        skyGeometry.setStripCount(skyData.stripCounts, skyData.numStrips);

        setAppearance(appearance);
        setGeometry(skyGeometry);
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
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        if(src == appearance) {
            // If there is only a single sky colour and we have fully opaque
            // geometry, just use the blit form rather than geometry. Should
            // give a slight performance increase.
            boolean visible = numSkyColors > 1 ||
                              transparency != 0;
            appearance.setVisible(visible);
        } else if(src == skyGeometry) {
            skyGeometry.setColors(true, skyData.colors);
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

        if(numSkyColors > 1) {
            if (appearance.isLive())
                appearance.dataChanged(this);
            else
                updateNodeDataChanges(appearance);
        }
    }

    /**
     * Update the background sky color sphere to the new values.
     *
     * @param color The color values to use as a flat array
     * @param angles The angles to use at each colour boundary
     * @param num The number of color values to read from the arrays
     */
    void updateSkyColors(float[] color, float[] angles, int num) {

        if(num != 0)
            updateSkySphereGeom(color, angles, num);

        numSkyColors = num;

        if(appearance.isLive())
            appearance.dataChanged(this);
        else
            updateNodeDataChanges(appearance);
    }

    /**
     * Create the array holding the coordinates, normals and colors for the sky
     * sphere.
     *
     * @param color The color values to use as a flat array
     * @param angles The angles to use at each colour boundary
     * @param num The number of color values to read from the arrays
     */
    private void updateSkySphereGeom(float[] color, float[] angles, int num) {
        // Update the color interpolator for the sky. First color is always
        // straight down the -Y axis, so assumed angle of zero.
        if(skyColorCreator == null)
            skyColorCreator = new ColorRampGenerator();

        boolean extra_color = (angles[num - 1] < Math.PI);

        int num_colors = num;
        if(extra_color)
            num_colors++;

        if((skyHeights == null) || (skyHeights.length != num_colors)) {
            skyHeights = new float[num_colors];
            skyColors = new float[num_colors][4];
        }

        int ci = 0;

        skyHeights[0] = 1f;
        skyColors[0][0] = color[ci++];
        skyColors[0][1] = color[ci++];
        skyColors[0][2] = color[ci++];
        skyColors[0][3] = transparency;

        for(int i = 1; i < num_colors - 1; i++) {
            skyHeights[i] = (float)Math.cos(angles[i - 1]);
            skyColors[i][0] = color[ci++];
            skyColors[i][1] = color[ci++];
            skyColors[i][2] = color[ci++];
            skyColors[i][3] = transparency;
        }

        int last = num_colors - 1;
        skyHeights[last] = -1f;

        if(extra_color) {
            skyColors[last][0] = color[ci - 3];
            skyColors[last][1] = color[ci - 2];
            skyColors[last][2] = color[ci - 1];
            skyColors[last][3] = transparency;
        } else {
            skyColors[last][0] = color[ci++];
            skyColors[last][1] = color[ci++];
            skyColors[last][2] = color[ci++];
            skyColors[last][3] = transparency;
        }

        // update the geometry color value here.
        skyColorCreator.setColorRamp(skyHeights, skyColors);
        skyColorCreator.generate(skyData);

        if (skyGeometry.isLive())
            skyGeometry.dataChanged(this);
        else
            updateNodeDataChanges(skyGeometry);
    }

    /**
     * Generate the data needed for the skysphere.
     *
     * @param facetCount Number of faces around the circumferance
     */
    private void generateSphereGeom(int facetCount) {
        int stripLength = (facetCount + 1) << 1;
        skyData.numStrips = facetCount >> 1;
        int vtx_count = (facetCount + 1) * facetCount;

        skyData.stripCounts = new int[facetCount];

        for(int i = 0; i < facetCount; i++)
            skyData.stripCounts[i] = stripLength;

        skyData.vertexCount = vtx_count;

        skyData.coordinates = new float[vtx_count * 3];
        skyData.normals = new float[vtx_count * 3];

        // local constant to make math calcs faster
        double segment_angle = 2.0 * Math.PI / facetCount;
        float tex_angle = 1 / (float)facetCount;
        float[] cos_table;
        float[] sin_table;
        float[] s_table = new float[facetCount];

        cos_table = new float[facetCount];
        sin_table = new float[facetCount];

        for(int i = 0; i < facetCount; i++) {
            cos_table[i] = (float)Math.cos(segment_angle * i);
            sin_table[i] = (float)Math.sin(segment_angle * i);

            s_table[i] = i * tex_angle;
        }

        // Start at the top and work our way to the bottom. Top to bottom on
        // the outer loop, one strip all the way around on the inner.
        int half_face = facetCount / 2;
        float x, y, z;
        int v_idx = 0;
        int n_idx = 0;

        for(int i = 0; i < half_face; i++) {
            float y_top = (float)(Math.cos(segment_angle * i));
            float yr_top = (float)(Math.sin(segment_angle * i));
            float y_low = (float)(Math.cos(segment_angle * (i + 1)));
            float yr_low = (float)(Math.sin(segment_angle * (i + 1)));

            for(int j = 0; j < facetCount; j++) {
                x = -sin_table[j] * yr_top;
                y = y_top;
                z = cos_table[j] * yr_top;

                skyData.coordinates[v_idx++] = x;
                skyData.coordinates[v_idx++] = y;
                skyData.coordinates[v_idx++] = z;

                skyData.normals[n_idx++] = -x;
                skyData.normals[n_idx++] = -y;
                skyData.normals[n_idx++] = -z;

                x = -sin_table[j] * yr_low;
                y = y_low;
                z = cos_table[j] * yr_low;

                skyData.coordinates[v_idx++] = x;
                skyData.coordinates[v_idx++] = y;
                skyData.coordinates[v_idx++] = z;

                skyData.normals[n_idx++] = -x;
                skyData.normals[n_idx++] = -y;
                skyData.normals[n_idx++] = -z;
            }

            x = -sin_table[0] * yr_top;
            y = y_top;
            z = cos_table[0] * yr_top;

            skyData.coordinates[v_idx++] = x;
            skyData.coordinates[v_idx++] = y;
            skyData.coordinates[v_idx++] = z;

            skyData.normals[n_idx++] = -x;
            skyData.normals[n_idx++] = -y;
            skyData.normals[n_idx++] = -z;

            x = -sin_table[0] * yr_low;
            y = y_low;
            z = cos_table[0] * yr_low;

            skyData.coordinates[v_idx++] = x;
            skyData.coordinates[v_idx++] = y;
            skyData.coordinates[v_idx++] = z;

            skyData.normals[n_idx++] = -x;
            skyData.normals[n_idx++] = -y;
            skyData.normals[n_idx++] = -z;
        }
    }
}
