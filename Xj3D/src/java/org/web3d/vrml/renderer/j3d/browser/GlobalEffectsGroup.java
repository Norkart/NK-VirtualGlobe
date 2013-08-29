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

package org.web3d.vrml.renderer.j3d.browser;

// Standard imports
import javax.media.j3d.*;

import java.util.BitSet;
import java.util.Map;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.vecmath.Point3d;

import org.j3d.geom.NormalUtils;
import org.j3d.geom.SphereGenerator;
import org.j3d.geom.GeometryData;
import org.j3d.geom.terrain.ColorRampGenerator;

// Application specific imports
import org.web3d.vrml.renderer.j3d.input.J3DGlobalEffectsHandler;
import org.web3d.vrml.renderer.j3d.nodes.J3DBackgroundNodeType;

/**
 * Represents all of the global rendering effects in the world - viewpoint,
 * background and fog.
 * <p>
 *
 * TODO:<BR>
 * - Figure out the correct density calculations for exponential fog based
 * on visibilityRange.
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
class GlobalEffectsGroup extends Group
    implements J3DGlobalEffectsHandler {

    /** Number of facets to include in the X-Z plane of the sky/ground */
    private static final int DEFAULT_NUM_SPHERE_FACES = 32;

    /** Number of vertices in a triangle strip for one side */
    private static final int BOX_SIDE_VERTEX_COUNT = 4;

    /** The list of strip counts for the background box */
    private static final int[] BOX_STRIP_COUNTS = { BOX_SIDE_VERTEX_COUNT };

    /**
     * The size of the sky box - just small enough to fit inside the containing
     * sky sphere, which is radius 1: sqrt(2) / 2.
     */
    private static final float BOX_SIZE = 0.7071f;

    /** Table for the sine values for generating the sky sphere */
    private static final float[] SIN_TABLE;

    /** Table for the cosine values for generating the sky sphere */
    private static final float[] COS_TABLE;

    /** Format flags used for the geometry array */
    private static final int BOX_FORMAT =
        GeometryArray.COORDINATES |
        GeometryArray.TEXTURE_COORDINATE_2 |
        GeometryArray.NORMALS;

    /** The 2D texture coordinates for the box side */
    private static final float[] BOX_TEX_COORDS = {
        0, 1,  0, 0,  1, 0, 1, 1
    };

    // Side constants for readability when generating the background box.
    private static final int BACK   = 0;
    private static final int FRONT  = 1;
    private static final int LEFT   = 2;
    private static final int RIGHT  = 3;
    private static final int TOP    = 4;
    private static final int BOTTOM = 5;
    private static final int SKY_SPHERE = 6;
    private static final int GROUND_SPHERE = 7;
    private static final int NUM_BG_OBJECTS = 8;

    /** Flag to say if this node is static (eg in a loader) */
    private boolean isStatic;

    /** Transform group holding the camera model */
    private TransformGroup cameraTransform;

    /** Light bounds for all headlights */
    private BoundingSphere lightBounds;

    /** The ViewPlatform needed for rendering */
    private ViewPlatform viewPlatform;

    /** The headlight attached to the viewpoint */
    private DirectionalLight headlight;

    /** Transform group holding the fog */
    private TransformGroup fogTransform;

    /** Switch used to control which of the two fog models to use */
    private Switch fogSwitch;

    /** Exponential fog model when in use */
    private ExponentialFog exponentialFog;

    /** Linear fog model when in use */
    private LinearFog linearFog;

    /** Transform group holding the background */
    private TransformGroup backgroundTransform;

    /** Set for controlling the linear fog node to be rendered */
    private BitSet linearFogSet;

    /** Set for controlling the exponential fog node to be rendered */
    private BitSet exponentialFogSet;

    /** Parent of the background management system */
    private Background background;

    /** The array of Appearance objects for the texture setting */
    private Appearance[] backgroundAppearances;

    /** The array of rendering attributes for the texture visibility */
    private RenderingAttributes[] backgroundAttributes;

    /** Current number of colours being used in the sky sphere */
    private int currentNumSkyColors;

    /** Current number of colours being used in the ground hemisphere */
    private int currentNumGroundColors;

    /** Color interpolator used for generating sky/ground color values */
    private ColorRampGenerator skyColorCreator;

    /** Color interpolator used for generating sky/ground color values */
    private ColorRampGenerator gndColorCreator;


    /** Data holder for the sky geometry */
    private GeometryData skyData;

    /** Data holder for the sky geometry */
    private GeometryData groundData;

    /** The maximum number of facets used in the sky sphere */
    private int maxSkyColors;

    /**
     * The angle increment in radians for the sky color. Increment is a
     * fixed value based on pi / numSkyFacets.
     */
    private float skyAngleInc;

    /** Temp array to create sky color heights with for the ramp */
    private float[] skyHeights;

    /** Temp array to create sky colors with for the ramp */
    private float[][] skyColors;

    /** Temp array to create sky color heights with for the ramp */
    private float[] gndHeights;

    /** Max angle for the ground. Used to work out if we need to retessellate */
    private float gndMaxAngle;

    /** J3D Geometry used by the sky sphere */
    private IndexedTriangleStripArray skyGeometry;

    /** J3D Geometry used by the sky sphere */
    private IndexedTriangleStripArray groundGeometry;

    /** Shape3D holding the skyGeometry so that it can be replaced as needed */
    private Shape3D skyShape;

    /** Shape3D holding the groundGeometry so that it can be replaced as needed */
    private Shape3D groundShape;


    /**
     * Static initializer to create the sin/cos tables and the values
     * contained in them.
     */
    static {
        COS_TABLE = new float[DEFAULT_NUM_SPHERE_FACES];
        SIN_TABLE = new float[DEFAULT_NUM_SPHERE_FACES];

        double segment_angle = 2.0 * Math.PI / DEFAULT_NUM_SPHERE_FACES;

        for(int i = 0; i < DEFAULT_NUM_SPHERE_FACES; i++)
        {
            COS_TABLE[i] = (float)Math.cos(segment_angle * i + Math.PI / 2);
            SIN_TABLE[i] = (float)Math.sin(segment_angle * i + Math.PI / 2);
        }
    }

    /**
     * Create a new instance with the headlight off and controls over whether
     * the code will be used in a static or dynamic environment.
     *
     * @param isStatic True if this is a static camera
     */
    GlobalEffectsGroup(boolean isStatic) {

        this.isStatic = isStatic;

        lightBounds = new BoundingSphere(new Point3d(), Double.MAX_VALUE);

        // Setup the camera model
        viewPlatform = new ViewPlatform();
        viewPlatform.setActivationRadius(Float.MAX_VALUE);

        headlight = new DirectionalLight();
        headlight.setInfluencingBounds(lightBounds);
        headlight.setEnable(false);

        cameraTransform = new TransformGroup();
        cameraTransform.addChild(viewPlatform);
        cameraTransform.addChild(headlight);

        // Setup the background model
        backgroundTransform = new TransformGroup();

        constructBackgroundShapes(backgroundTransform);
        constructBackgroundBox(backgroundTransform);

        BranchGroup bg_geom = new BranchGroup();
        bg_geom.addChild(backgroundTransform);

        Bounds infinite_bounds =
            new BoundingSphere(new Point3d(), Double.MAX_VALUE);

        background = new Background();
        background.setApplicationBounds(infinite_bounds);
        background.setGeometry(bg_geom);

        // Setup the fog model. The constants here match with the adding done
        // to the switch node earlier.
        linearFogSet = new BitSet();
        linearFogSet.set(0);

        exponentialFogSet = new BitSet();
        exponentialFogSet.set(1);

        exponentialFog = new ExponentialFog();
        exponentialFog.setCapability(Fog.ALLOW_COLOR_WRITE);
        exponentialFog.setCapability(ExponentialFog.ALLOW_DENSITY_WRITE);
        exponentialFog.setInfluencingBounds(infinite_bounds);

        linearFog = new LinearFog();
        linearFog.setCapability(Fog.ALLOW_COLOR_WRITE);
        linearFog.setCapability(LinearFog.ALLOW_DISTANCE_WRITE);
        linearFog.setInfluencingBounds(infinite_bounds);

        fogSwitch = new Switch();
        fogSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        fogSwitch.setWhichChild(Switch.CHILD_NONE);
        fogSwitch.addChild(linearFog);
        fogSwitch.addChild(exponentialFog);

        fogTransform = new TransformGroup();
        fogTransform.addChild(fogSwitch);

        addChild(cameraTransform);
        addChild(background);
        addChild(fogTransform);

        if(!isStatic) {
            headlight.setCapability(DirectionalLight.ALLOW_STATE_WRITE);

            fogSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);

            exponentialFog.setCapability(ExponentialFog.ALLOW_DENSITY_WRITE);
            linearFog.setCapability(LinearFog.ALLOW_DISTANCE_WRITE);

            cameraTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            backgroundTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            fogTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

            background.setCapability(Background.ALLOW_COLOR_WRITE);
        }

        currentNumSkyColors = 0;
        currentNumGroundColors = 0;
        maxSkyColors = 0;
        gndMaxAngle = 0;

        skyData = new GeometryData();
        skyData.geometryType = GeometryData.INDEXED_TRIANGLE_STRIPS;
        skyData.geometryComponents = GeometryData.NORMAL_DATA;

        groundData = new GeometryData();
        groundData.geometryType = GeometryData.INDEXED_TRIANGLE_STRIPS;
        groundData.geometryComponents = GeometryData.NORMAL_DATA;
    }

    //----------------------------------------------------------
    // Methods required by the GlobalEffectsHandler interface.
    //----------------------------------------------------------

    /**
     * Update the view matrix to be this new matrix.
     *
     * @param transform The new view matrix settings
     */
    public void setViewMatrix(Transform3D transform) {
        cameraTransform.setTransform(transform);
    }

    /**
     * Set the background rotation matrix this new matrix.
     *
     * @param transform The new background matrix settings
     */
    public void setBackgroundMatrix(Transform3D transform) {
        backgroundTransform.setTransform(transform);
    }

    /**
     * Set the fog coordinate matrix this new matrix.
     *
     * @param transform The new fog matrix settings
     */
    public void setFogMatrix(Transform3D transform) {
        fogTransform.setTransform(transform);
    }

    /**
     * Update the background textures to this new set.
     *
     * @param textures The list of textures to use
     * @param flags The list of flags indicating a texture change
     */
    public void updateBackgroundTextures(Texture2D[] textures,
                                         boolean[] flags) {

        for(int i = 0; i < 6; i++) {
            if(flags[i]) {
                backgroundAppearances[i].setTexture(textures[i]);
                backgroundAttributes[i].setVisible(textures[i] != null);
            }
        }
    }

    /**
     * Update the background ground color sphere to the new values.
     *
     * @param color The color values to use as a flat array
     * @param angles The angles to use at each colour boundary
     * @param num The number of color values to read from the arrays
     */
    public void updateBackgroundGround(float[] color, float[] angles, int num) {

        if(num < 2) {
            groundShape.setGeometry(null);
        } else {
            updateGroundSphereGeom(color, angles, num);
            groundShape.setGeometry(groundGeometry);
        }
    }

    /**
     * Update the background sky color sphere to the new values.
     *
     * @param color The color values to use as a flat array
     * @param angles The angles to use at each colour boundary
     * @param num The number of color values to read from the arrays
     */
    public void updateBackgroundSky(float[] color, float[] angles, int num) {

        // If there is only a single sky colour, just use the blit form rather than
        // geometry. Should give a slight performance increase.
        if(num == 1) {
            skyShape.setGeometry(null);
            background.setColor(color[0], color[1], color[2]);
        } else if(num == 0) {
            skyShape.setGeometry(null);
            background.setColor(0, 0, 0);
        } else {
            // so we have more than one colour. Time to build a sky sphere.
            background.setColor(0, 0, 0);
            updateSkySphereGeom(color, angles, num);
            skyShape.setGeometry(skyGeometry);
        }
    }

    /**
     * Update the fog type in use. Can be used to turn the current fog off
     * too.
     *
     * @param state The fog to use right now
     */
    public void enableFog(int state) {
        switch(state) {
            case FOG_DISABLE:
                fogSwitch.setWhichChild(Switch.CHILD_NONE);
                break;

            case FOG_LINEAR:
                fogSwitch.setWhichChild(Switch.CHILD_MASK);
                fogSwitch.setChildMask(linearFogSet);
                break;

            case FOG_EXPONENTIAL:
                fogSwitch.setWhichChild(Switch.CHILD_MASK);
                fogSwitch.setChildMask(exponentialFogSet);
                break;
        }
    }

    /**
     * Update the fog with different setups. New colour and visibility range.
     *
     * @param visLimit The visibility limit. Zero to disable it
     * @param r The red component of the fog color
     * @param g The green component of the fog color
     * @param b The blue component of the fog color
     */
    public void setFogDetails(float visLimit, float r, float g, float b) {
        linearFog.setColor(r, g, b);
        exponentialFog.setColor(r, g, b);

        linearFog.setBackDistance(visLimit);
        exponentialFog.setDensity(visLimit);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the viewPlatform defined here so that views can be attached to it.
     *
     * @return The ViewPlatform instance in use
     */
    ViewPlatform getViewPlatform() {
        return viewPlatform;
    }

    /**
     * Set the embedded headlight to be on or off.
     *
     * @param on true if the headlight should be turned on
     */
    void useHeadlight(boolean on) {
        headlight.setEnable(on);
    }

    /**
     * Add an arbitrary child that depends on being view-aligned to the view
     * group.
     *
     * @param group The child to add
     */
    void addViewDependentChild(Group group) {
        cameraTransform.addChild(group);
    }

    //----------------------------------------------------------
    // Internal Methods
    //----------------------------------------------------------

    /**
     * Build the background box structure. The box is composed of 6 separate
     * sides so that they can individually have a texture assigned to them.
     *
     * @param bgGroup The group to add the geometry to
     */
    private void constructBackgroundBox(Group bgGroup) {
        // Create all the geometry at once....

        // Setup the texture attributes. These nominate the defaults, so its
        // probably a waste currently, but we may want to change these at a
        // later date depending on the requirements/performance.
        TextureAttributes tex_attr = new TextureAttributes();
        tex_attr.setTextureMode(TextureAttributes.REPLACE);
        tex_attr.setPerspectiveCorrectionMode(TextureAttributes.FASTEST);

        backgroundAppearances = new Appearance[6];
        backgroundAttributes = new RenderingAttributes[6];

        // Create the shape geometrys for each side. There is no need for appearance as
        // we are going to create this as an unlit, white box.
        Shape3D shape;
        Appearance app;

        // unit box back coordinates
        float[] back_coords = {
             BOX_SIZE,  BOX_SIZE, BOX_SIZE,
             BOX_SIZE, -BOX_SIZE, BOX_SIZE,
            -BOX_SIZE, -BOX_SIZE, BOX_SIZE,
            -BOX_SIZE,  BOX_SIZE, BOX_SIZE,
        };

        float[] back_normals = {
            0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1
        };

        backgroundAttributes[BACK] = new RenderingAttributes();
        backgroundAttributes[BACK].setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        backgroundAttributes[BACK].setVisible(false);

        TransparencyAttributes ta = new TransparencyAttributes();
        ta.setTransparencyMode(TransparencyAttributes.BLENDED);

        app = new Appearance();
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        app.setTextureAttributes(tex_attr);
        app.setRenderingAttributes(backgroundAttributes[BACK]);
        app.setTransparencyAttributes(ta);

        backgroundAppearances[BACK] = app;

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(back_coords, back_normals));
        bgGroup.addChild(shape);

        // unit box front coordinates
        float[] front_coords = {
            -BOX_SIZE,  BOX_SIZE, -BOX_SIZE,
            -BOX_SIZE, -BOX_SIZE, -BOX_SIZE,
             BOX_SIZE, -BOX_SIZE, -BOX_SIZE,
             BOX_SIZE,  BOX_SIZE, -BOX_SIZE
        };

        float[] front_normals = {
             0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1
        };

        backgroundAttributes[FRONT] = new RenderingAttributes();
        backgroundAttributes[FRONT].setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        backgroundAttributes[FRONT].setVisible(false);

        app = new Appearance();
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        app.setTextureAttributes(tex_attr);
        app.setRenderingAttributes(backgroundAttributes[FRONT]);
        app.setTransparencyAttributes(ta);
        backgroundAppearances[FRONT] = app;

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(front_coords, front_normals));
        bgGroup.addChild(shape);

        // unit box left coordinates
        float[] left_coords = {
            -BOX_SIZE,  BOX_SIZE,  BOX_SIZE,
            -BOX_SIZE, -BOX_SIZE,  BOX_SIZE,
            -BOX_SIZE, -BOX_SIZE, -BOX_SIZE,
            -BOX_SIZE,  BOX_SIZE, -BOX_SIZE
        };

        float[] left_normals = {
             1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0
        };

        backgroundAttributes[LEFT] = new RenderingAttributes();
        backgroundAttributes[LEFT].setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        backgroundAttributes[LEFT].setVisible(false);

        app = new Appearance();
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        app.setTextureAttributes(tex_attr);
        app.setRenderingAttributes(backgroundAttributes[LEFT]);
        app.setTransparencyAttributes(ta);
        backgroundAppearances[LEFT] = app;

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(left_coords, left_normals));
        bgGroup.addChild(shape);

        // unit box right coordinates
        float[] right_coords = {
             BOX_SIZE,  BOX_SIZE, -BOX_SIZE,
             BOX_SIZE, -BOX_SIZE, -BOX_SIZE,
             BOX_SIZE, -BOX_SIZE,  BOX_SIZE,
             BOX_SIZE,  BOX_SIZE,  BOX_SIZE
        };

        float[] right_normals = {
             -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0
        };

        backgroundAttributes[RIGHT] = new RenderingAttributes();
        backgroundAttributes[RIGHT].setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        backgroundAttributes[RIGHT].setVisible(false);

        app = new Appearance();
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        app.setTextureAttributes(tex_attr);
        app.setRenderingAttributes(backgroundAttributes[RIGHT]);
        app.setTransparencyAttributes(ta);
        backgroundAppearances[RIGHT] = app;

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(right_coords, right_normals));
        bgGroup.addChild(shape);

        // unit box top coordinates
        float[] top_coords = {
            -BOX_SIZE,  BOX_SIZE,  BOX_SIZE,
            -BOX_SIZE,  BOX_SIZE, -BOX_SIZE,
             BOX_SIZE,  BOX_SIZE, -BOX_SIZE,
             BOX_SIZE,  BOX_SIZE,  BOX_SIZE
        };

        float[] top_normals = {
             0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0
        };

        backgroundAttributes[TOP] = new RenderingAttributes();
        backgroundAttributes[TOP].setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        backgroundAttributes[TOP].setVisible(false);

        app = new Appearance();
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        app.setTextureAttributes(tex_attr);
        app.setRenderingAttributes(backgroundAttributes[TOP]);
        app.setTransparencyAttributes(ta);
        backgroundAppearances[TOP] = app;

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(top_coords, top_normals));
        bgGroup.addChild(shape);

        // unit box bottom coordinates
        float[] bottom_coords = {
            -BOX_SIZE, -BOX_SIZE, -BOX_SIZE,
            -BOX_SIZE, -BOX_SIZE,  BOX_SIZE,
             BOX_SIZE, -BOX_SIZE,  BOX_SIZE,
             BOX_SIZE, -BOX_SIZE, -BOX_SIZE
        };


        float[] bottom_normals = {
             0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0
        };

        backgroundAttributes[BOTTOM] = new RenderingAttributes();
        backgroundAttributes[BOTTOM].setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        backgroundAttributes[BOTTOM].setVisible(false);

        app = new Appearance();
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        app.setTextureAttributes(tex_attr);
        app.setRenderingAttributes(backgroundAttributes[BOTTOM]);
        app.setTransparencyAttributes(ta);
        backgroundAppearances[BOTTOM] = app;

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(bottom_coords, bottom_normals));
        bgGroup.addChild(shape);
    }

    /**
     * Convenience method to create the side geometry
     *
     * @param coords THe coordinates to use
     * @param normals The normals to use
     * @return The geometry representing this side
     */
    private GeometryArray createSideGeom(float[] coords, float[] normals) {

        QuadArray array =
            new QuadArray(BOX_SIDE_VERTEX_COUNT, BOX_FORMAT);

        array.setCoordinates(0, coords);
        array.setNormals(0, normals);
        array.setTextureCoordinates(0, 0, BOX_TEX_COORDS);

        return array;
    }

    /**
     * Construct the background geometry for the spheres. Only generates
     * the Shape3D instances. The real geometry is set when the information
     * is passed through.
     */
    private void constructBackgroundShapes(Group bgGroup) {
        // Use polygon offset to make sure that the sky sphere is always
        // rendered just behind the ground one, if present. It's easier
        // doing it this way, and less prone to rendering artifacts compared
        // to creating spheres with different radii.
        PolygonAttributes sky_attr = new PolygonAttributes();
        sky_attr.setCullFace(PolygonAttributes.CULL_FRONT);
        sky_attr.setBackFaceNormalFlip(true);
        sky_attr.setPolygonOffset(-1f);

        PolygonAttributes gnd_attr = new PolygonAttributes();
        gnd_attr.setCullFace(PolygonAttributes.CULL_FRONT);
        gnd_attr.setBackFaceNormalFlip(true);

        Material mat = new Material();
        mat.setLightingEnable(false);

        Appearance sky_app = new Appearance();
        sky_app.setPolygonAttributes(sky_attr);
        sky_app.setMaterial(mat);

        Appearance gnd_app = new Appearance();
        gnd_app.setPolygonAttributes(gnd_attr);
        gnd_app.setMaterial(mat);

        skyShape = new Shape3D();
        skyShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        skyShape.clearCapabilityIsFrequent(Shape3D.ALLOW_GEOMETRY_WRITE);
        skyShape.setAppearance(sky_app);

        groundShape = new Shape3D();
        groundShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        groundShape.clearCapabilityIsFrequent(Shape3D.ALLOW_GEOMETRY_WRITE);
        groundShape.setAppearance(gnd_app);

        bgGroup.addChild(groundShape);
        bgGroup.addChild(skyShape);
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
            skyColors = new float[num_colors][3];
        }

        int ci = 0;
        skyHeights[0] = 1;
        skyColors[0][0] = color[ci++];
        skyColors[0][1] = color[ci++];
        skyColors[0][2] = color[ci++];

        for(int i = 1; i < num_colors - 1; i++) {
            skyHeights[i] = (float)Math.cos(angles[i - 1]);
            skyColors[i][0] = color[ci++];
            skyColors[i][1] = color[ci++];
            skyColors[i][2] = color[ci++];
        }

        int last = num_colors - 1;
        skyHeights[last] = -1;

        if(extra_color) {
            skyColors[last][0] = color[ci - 3];
            skyColors[last][1] = color[ci - 2];
            skyColors[last][2] = color[ci - 1];
        } else {
            skyColors[last][0] = color[ci++];
            skyColors[last][1] = color[ci++];
            skyColors[last][2] = color[ci++];
        }

        skyColorCreator.setColorRamp(skyHeights, skyColors);

        // Do we need to completely regenerate the geometry, or just update the
        // existing collection. So long as the total number of angles hasn't
        // changed, we can reuse the geometry array. The assumption is that
        // we don't change the number of colors/angles very often, so it's
        // alright to be costly here, because we want to save on memory in
        // the general case.
        if(num > maxSkyColors) {
            maxSkyColors = num > DEFAULT_NUM_SPHERE_FACES ?
                           num :
                           DEFAULT_NUM_SPHERE_FACES;

            skyAngleInc = (float)Math.PI / maxSkyColors;

            SphereGenerator gen = new SphereGenerator(1, maxSkyColors);
            gen.generate(skyData);


            // Allocate the color array directly because the generate()
            // call will not have done that. We'll need it for later on
            // anyway.
            skyData.colors = new float[skyData.vertexCount * 3];

            int format = IndexedTriangleStripArray.COORDINATES |
                         IndexedTriangleStripArray.NORMALS |
                         IndexedTriangleStripArray.COLOR_3;

            skyGeometry =
                new IndexedTriangleStripArray(skyData.vertexCount,
                                              format,
                                              skyData.indexesCount,
                                              skyData.stripCounts);
            skyGeometry.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
            skyGeometry.setCoordinates(0, skyData.coordinates);
            skyGeometry.setNormals(0, skyData.normals);
            skyGeometry.setCoordinateIndices(0, skyData.indexes);
            skyGeometry.setColorIndices(0, skyData.indexes);
            skyGeometry.setNormalIndices(0, skyData.indexes);
        }

        // update the geometry color value here.
        skyColorCreator.generate(skyData);
        skyGeometry.setColors(0, skyData.colors);
    }

    /**
     * Create the array holding the coordinates, normals and colors for the gnd
     * sphere.
     *
     * @param color The color values to use as a flat array
     * @param angles The angles to use at each colour boundary
     * @param num The number of color values to read from the arrays
     */
    private void updateGroundSphereGeom(float[] color, float[] angles, int num) {

        // Update the color interpolator for the gnd. First color is always
        // straight down the -Y axis, so assumed angle of zero.
        if(gndColorCreator == null)
            gndColorCreator = new ColorRampGenerator();

        boolean extra_color = (angles[num - 1] < Math.PI);

        if((gndHeights == null) || (gndHeights.length != num))
            gndHeights = new float[num];

        // Ground heights start with 0.0 radians as being straight
        // down the -Y axis.
        gndHeights[0] = -1;
        for(int i = 1; i < num; i++)
            gndHeights[i] = -(float)Math.cos(angles[i - 1]);

        gndColorCreator.setColorRamp(gndHeights, color, num, false);

        if(angles[num - 2] != gndMaxAngle) {
            generateGroundCoords(angles[num - 2], num);
            gndMaxAngle = angles[num - 2];
        }

        // update the geometry color value here.
        gndColorCreator.generate(groundData);
        groundGeometry.setColors(0, groundData.colors);
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

        int num_coords = DEFAULT_NUM_SPHERE_FACES * (num_inc + 1);
        groundData.vertexCount = num_coords;

        // Allocate arrays for the geometry as well as strip counts
        // and index counts.
        if((groundData.coordinates == null) ||
           (groundData.vertexCount < (num_coords * 3))) {
            groundData.coordinates = new float[num_coords * 3];
            groundData.colors = new float[num_coords * 3];
        }

        // Create index arrays, Since we don't change this often, no
        // point keeping the info about as an array in groundData.
        int num_indexes = num_inc * 2 * (DEFAULT_NUM_SPHERE_FACES + 1);
        int[] indexes = new int[num_indexes];
        int[] strips = new int[num_inc];
        int cnt = 0;

        for(int i = 0; i < num_inc; i++) {
            int i_facet = i * DEFAULT_NUM_SPHERE_FACES;
            strips[i] = (DEFAULT_NUM_SPHERE_FACES + 1) << 1;

            for(int j = 0; j < DEFAULT_NUM_SPHERE_FACES; j++) {
                int pos = j + i_facet;

                indexes[cnt++] = pos + DEFAULT_NUM_SPHERE_FACES;
                indexes[cnt++] = pos;
            }

            indexes[cnt++] = i_facet + DEFAULT_NUM_SPHERE_FACES;
            indexes[cnt++] = i_facet;
        }

        // create new geometry
        int format = IndexedTriangleStripArray.COORDINATES |
                     IndexedTriangleStripArray.NORMALS |
                     IndexedTriangleStripArray.COLOR_3;

        groundGeometry =
            new IndexedTriangleStripArray(groundData.vertexCount,
                                          format,
                                          num_indexes,
                                          strips);
        groundGeometry.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
        groundGeometry.setCoordinateIndices(0, indexes);
        groundGeometry.setColorIndices(0, indexes);
        groundGeometry.setNormalIndices(0, indexes);

        // For each row of the face, generate the coordinates. First
        // row is fixed - always at 0, -1, 0
        int coord_idx = 1;
        for(int i = 0; i < DEFAULT_NUM_SPHERE_FACES; i++) {
            groundData.coordinates[coord_idx] = -1;
            coord_idx += 3;
        }

        // re-adjust coord_idx to the x coordinate.
        coord_idx--;

        for(int i = 1; i <= num_inc; i++) {
            float r = 0.8f * (float)Math.sin(angle_inc * i);
            float h = -(float)Math.cos(angle_inc * i);

            for(int j = 0; j < DEFAULT_NUM_SPHERE_FACES; j++) {
                groundData.coordinates[coord_idx++] = r * SIN_TABLE[j];
                groundData.coordinates[coord_idx++] = h;
                groundData.coordinates[coord_idx++] = r * COS_TABLE[j];
            }
        }

        groundGeometry.setCoordinates(0, groundData.coordinates);
        groundGeometry.setNormals(0, groundData.coordinates);

        groundShape.setGeometry(groundGeometry);
    }
}
