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
import org.j3d.aviatrix3d.*;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;

import org.j3d.geom.NormalUtils;
import org.j3d.geom.SphereGenerator;
import org.j3d.geom.GeometryData;
import org.j3d.geom.terrain.ColorRampGenerator;

// Local imports
import org.web3d.vrml.renderer.ogl.input.OGLGlobalEffectsHandler;

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
 * @version $Revision: 1.23 $
 */
public class GlobalEffectsGroup extends Group
    implements OGLGlobalEffectsHandler, NodeUpdateListener {

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
    private static final float BOX_SIZE = 0.62f;
    // TODO: Not sure why this need to be closer.  Tearinh happens otherwise.  A change to
    // Aviaitrx3d background far clip plane to 1.3 would do the same thing.
//    private static final float BOX_SIZE = 0.7071f;

    /** Table for the sine values for generating the sky sphere */
    private static final float[] SIN_TABLE;

    /** Table for the cosine values for generating the sky sphere */
    private static final float[] COS_TABLE;

    /** The 2D texture coordinates for the box side */
    private static final float[][] BOX_TEX_COORDS = { {
        0, 1,  0, 0,  1, 0, 1, 1
    } };

    /** The tex coord type to pass to the geometry for sky box */
    private static final int[] BOX_TEX_COORD_TYPES = {
        VertexGeometry.TEXTURE_COORDINATE_2
    };

    // Side constants for readability when generating the background box.
    private static final int BACK   = 0;
    private static final int FRONT  = 1;
    private static final int LEFT   = 2;
    private static final int RIGHT  = 3;
    private static final int TOP    = 4;
    private static final int BOTTOM = 5;

    /** Render manager for handling the backgrounds and fogs */
    private SimpleScene globalScene;

    /** Transform group holding the camera model */
    private TransformGroup cameraTransform;

    /** Matrix for the fog effect transformation */
    private Matrix4f cameraMatrix;

    /** The ViewPlatform needed for rendering */
    private Viewpoint viewpoint;

    /** State to set the headlight to */
    private boolean headlightState;


    /** The currently active fog object */
    private int activeFog;

    /** Transform group holding the fog */
    private TransformGroup fogTransform;

    /** Matrix for the fog effect transformation */
    private Matrix4f fogMatrix;

    /** Fog colour to set */
    private float[] fogColor;

    /** Visibility limit on the fog */
    private float fogVisLimit;

    /** Exponential fog model when in use */
    private Fog exponentialFog;

    /** Linear fog model when in use */
    private Fog linearFog;


    /** Transform group holding the background */
    private TransformGroup backgroundTransform;

    /** Matrix for the fog effect transformation */
    private Matrix4f backgroundMatrix;

    /** Background used for rendering the skybox */
    private ShapeBackground geomBackground;

    /** The array of Appearance objects for the texture setting */
    private Appearance[] backgroundAppearances;

    /**
     * Shared Material instance for all backgroundAppearances. Used to
     * control the global alpha state from the currently bound background node.
     */
    private Material backgroundMaterial;

    /** Flags indicating the visibility state of each appearance */
    private boolean[] bgAppVisibility;

    /** Current number of colours being used in the sky sphere */
    private int currentNumSkyColors;

    /** Current number of colours being used in the ground hemisphere */
    private int currentNumGroundColors;

    /** Map a changed TextureUnit to the new texture to set */
    private HashMap texUnitToTexture;

    /** The current background transparency */
    private float bgTransparency;

    /** An array for doing temporary stuff */
    private float[] tmpArray;

    /** Class for handling ground sphere geometry */
    private GroundSphere ground;

    /** Class for handling sky sphere geometry */
    private SkySphere sky;

    /** Colour to clear the background to */
    private float[] clearColor;

    /** The ID of the layer this group belongs in. */
    private int layerId;

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
     *
     * @param scene The scene to use for the global effects
     */
    public GlobalEffectsGroup(SimpleScene scene) {

        globalScene = scene;
        bgTransparency = 0;
        layerId = -1;

        setPickMask(0);

        texUnitToTexture = new HashMap();

        tmpArray = new float[4];
        fogColor = new float[3];
        clearColor = new float[3];

        cameraMatrix = new Matrix4f();
        backgroundMatrix = new Matrix4f();
        fogMatrix = new Matrix4f();

        // Setup the camera model
        viewpoint = new Viewpoint();
        viewpoint.setHeadlightEnabled(true);
        viewpoint.setGlobalAmbientLightEnabled(true);
        viewpoint.setGlobalAmbientColor(new float[] {0,0,0,1});

        headlightState = true;

        cameraTransform = new TransformGroup();
        cameraTransform.setBounds(new BoundingVoid());
        cameraTransform.addChild(viewpoint);

        sky = new SkySphere();
        ground = new GroundSphere();

        // Setup the background model
        backgroundTransform = new TransformGroup();
        geomBackground = new ShapeBackground();

        geomBackground.addShape(sky);
        geomBackground.addShape(ground);

        constructBackgroundBox(geomBackground);

        // TODO: restore setBackgroundMatrix when this is fixed
        backgroundTransform.addChild(geomBackground);

        // Setup the fog model. The constants here match with the adding done
        // to the switch node earlier.
        activeFog = FOG_DISABLE;
        exponentialFog = new Fog(Fog.EXPONENTIAL);
        exponentialFog.setGlobalOnly(true);
        linearFog = new Fog(Fog.LINEAR);
        linearFog.setGlobalOnly(true);

        fogTransform = new TransformGroup();
        fogTransform.addChild(exponentialFog);
        fogTransform.addChild(linearFog);


        addChild(cameraTransform);
        addChild(backgroundTransform);
        addChild(fogTransform);
    }

    //----------------------------------------------------------
    // Methods defined by OGLGlobalEffectsHandler
    //----------------------------------------------------------

    /**
     * Set or reset the layer ID to the new ID value.
     *
     * @param id A non-negative ID for the layer
     */
    public void setLayerId(int id) {
        layerId = id;
        if(geomBackground.isLive())
            geomBackground.dataChanged(this);
        else
            updateNodeDataChanges(geomBackground);
    }

    /**
     * Update the view matrix to be this new matrix.
     *
     * @param transform The new view matrix settings
     */
    public void setViewMatrix(Matrix4f transform) {

        if(cameraTransform.isLive()) {
            // check the matrix for no change from the current one. If it hasn't,
            // ignore this.
            if(!isSameMatrix(transform, cameraMatrix)) {
                cameraMatrix.set(transform);

                cameraTransform.boundsChanged(this);
            }
        } else
            cameraTransform.setTransform(transform);
    }

    /**
     * Set the background rotation matrix this new matrix.
     *
     * @param transform The new background matrix settings
     */
    public void setBackgroundMatrix(Matrix4f transform) {
        if(cameraTransform.isLive()) {
            // check the matrix for no change from the current one. If it hasn't,
            // ignore this.
            if(!isSameMatrix(transform, backgroundMatrix)) {
                backgroundMatrix.set(transform);
                backgroundTransform.boundsChanged(this);
            }
        } else
            backgroundTransform.setTransform(transform);
    }

    /**
     * Set the fog coordinate matrix this new matrix.
     *
     * @param transform The new fog matrix settings
     */
    public void setFogMatrix(Matrix4f transform) {
        if(fogTransform.isLive()) {
            if(!isSameMatrix(transform, fogMatrix)) {
                fogMatrix.set(transform);
                fogTransform.boundsChanged(this);
            }
        } else
            fogTransform.setTransform(transform);
    }

    /**
     * Update the background transparency to this new value. If the value is
     * 1.0, then disable all the background rendering. A value of 0 is
     * completely opaque and a value of 1 is clear.
     *
     * @param transparency A value between 0 and 1
     */
    public void updateBackgroundTransparency(float transparency) {
        if(transparency == bgTransparency)
            return;

        boolean formerly_transparent = (bgTransparency == 1);

        bgTransparency = transparency;

        // If completely transparent, just turn everything off
        if(transparency == 1) {
            globalScene.setActiveBackground(null);
        } else {
            // if we were transparent before due to transparency, then we need
            // to undo that.
            if(formerly_transparent)
                globalScene.setActiveBackground(geomBackground);

            if(backgroundMaterial.isLive())
                backgroundMaterial.dataChanged(this);
            else
                updateNodeDataChanges(backgroundMaterial);
        }

        sky.updateBackgroundTransparency(transparency);
        ground.updateBackgroundTransparency(transparency);
    }

    /**
     * Update the background textures to this new set.
     *
     * @param textures The list of textures to use
     * @param flags The list of flags indicating a texture change
     */
    public void updateBackgroundTextures(Texture2D[] textures,
                                         boolean[] flags) {

        TextureUnit[] tu = new TextureUnit[1];

        for(int i = 0; i < 6; i++) {
            if(flags[i]) {
                backgroundAppearances[i].getTextureUnits(tu);

                if (tu[0].isLive())
                    tu[0].dataChanged(this);
                else
                    updateNodeDataChanges(tu[0]);

                texUnitToTexture.put(tu[0], textures[i]);
                bgAppVisibility[i] = (textures[i] != null);

                if(backgroundAppearances[i].isLive())
                    backgroundAppearances[i].dataChanged(this);
                else
                    updateNodeDataChanges(backgroundAppearances[i]);
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
        ground.updateGroundColors(color, angles, num);
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
            clearColor[0] = color[0];
            clearColor[1] = color[1];
            clearColor[2] = color[2];

            if(geomBackground.isLive())
                geomBackground.dataChanged(this);
            else
                geomBackground.setColor(clearColor[0],
                                        clearColor[1],
                                        clearColor[2],
                                        1);
        } else if(num == 0) {
            clearColor[0] = 0;
            clearColor[1] = 0;
            clearColor[2] = 0;

            if(geomBackground.isLive())
                geomBackground.dataChanged(this);
            else
                geomBackground.setColor(clearColor[0],
                                        clearColor[1],
                                        clearColor[2],
                                        1);
        }

        sky.updateSkyColors(color, angles, num);
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
                globalScene.setActiveFog(null);

                if(state == FOG_LINEAR) {
                    if(linearFog.isLive())
                        linearFog.dataChanged(this);
                    else
                        linearFog.setEnabled(true);
                } else if(state == FOG_EXPONENTIAL) {
                    if(exponentialFog.isLive())
                        exponentialFog.dataChanged(this);
                    else
                        exponentialFog.setEnabled(true);
                }
                break;

            case FOG_LINEAR:
                globalScene.setActiveFog(linearFog);
                if(linearFog.isLive())
                    linearFog.dataChanged(this);
                else
                    linearFog.setEnabled(true);

                if(state == FOG_EXPONENTIAL) {
                    if(exponentialFog.isLive())
                        exponentialFog.dataChanged(this);
                    else
                        exponentialFog.setEnabled(true);
                }
                break;

            case FOG_EXPONENTIAL:
                globalScene.setActiveFog(exponentialFog);

                if(exponentialFog.isLive())
                    exponentialFog.dataChanged(this);
                else
                    exponentialFog.setEnabled(true);

                if(state == FOG_LINEAR) {
                    if(linearFog.isLive())
                        linearFog.dataChanged(this);
                    else
                        linearFog.setEnabled(true);
                }
                break;
        }

        activeFog = state;
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
        fogColor[0] = r;
        fogColor[1] = g;
        fogColor[2] = b;
        fogVisLimit = visLimit;

        switch(activeFog) {
            case FOG_DISABLE:
                // Ignore setting anything in this case
                break;

            case FOG_LINEAR:
                if(isLive()) {
                    linearFog.dataChanged(this);
                } else {
                    linearFog.setColor(fogColor);
                    linearFog.setLinearDistance(0, fogVisLimit);
                }
                break;

            case FOG_EXPONENTIAL:
                if(isLive()) {
                    exponentialFog.dataChanged(this);
                } else {
                    linearFog.setColor(fogColor);
                    linearFog.setLinearDistance(0, fogVisLimit);
                }
                break;
        }
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
        if(src == cameraTransform)
            cameraTransform.setTransform(cameraMatrix);
        else if(src == backgroundTransform)
            backgroundTransform.setTransform(backgroundMatrix);
        else if(src == fogTransform)
            fogTransform.setTransform(fogMatrix);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        if(src == linearFog) {
            linearFog.setColor(fogColor);
            linearFog.setLinearDistance(0, fogVisLimit);
            linearFog.setEnabled(activeFog == FOG_LINEAR);
        } else if(src == exponentialFog) {
            exponentialFog.setColor(fogColor);
            exponentialFog.setDensityRate(fogVisLimit);
            exponentialFog.setEnabled(activeFog == FOG_EXPONENTIAL);
        } else if(src == geomBackground) {
            geomBackground.setColor(clearColor[0],
                                    clearColor[1],
                                    clearColor[2],
                                    1);
            boolean clear = (layerId == 0) || (bgTransparency == 0);
            geomBackground.setColorClearEnabled(clear);
        } else if(src == viewpoint)
            viewpoint.setHeadlightEnabled(headlightState);
        else if(src instanceof Appearance) {
            for(int i = 0; i < 6; i++) {
                if(src == backgroundAppearances[i])
                    backgroundAppearances[i].setVisible(bgAppVisibility[i]);
            }
        } else if(src instanceof TextureUnit) {
            Texture tex = (Texture)texUnitToTexture.remove(src);
            ((TextureUnit)src).setTexture(tex);
        } else if(src == backgroundMaterial) {
            backgroundMaterial.setTransparency(bgTransparency);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Instruct the effects group to start initialisation now
     */
    public void initialize() {
        globalScene.setActiveView(viewpoint);
        globalScene.setActiveBackground(geomBackground);
    }

    /**
     * Get the viewPlatform defined here so that views can be attached to it.
     *
     * @return The ViewPlatform instance in use
     */
    public Viewpoint getViewpoint() {
        return viewpoint;
    }

    /**
     * Set the embedded headlight to be on or off.
     *
     * @param on true if the headlight should be turned on
     */
    public void useHeadlight(boolean on) {
        headlightState = on;

        if (viewpoint.isLive())
            viewpoint.dataChanged(this);
        else
            updateNodeDataChanges(viewpoint);
    }

    /**
     * Add an arbitrary child that depends on being view-aligned to the view
     * group.
     *
     * @param group The child to add
     */
    public void addViewDependentChild(Group group) {
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
    private void constructBackgroundBox(ShapeBackground bg) {
        // Create all the geometry at once....

        // Setup the texture attributes. These nominate the defaults, so its
        // probably a waste currently, but we may want to change these at a
        // later date depending on the requirements/performance.
        TextureAttributes tex_attr = new TextureAttributes();
        tex_attr.setTextureMode(TextureAttributes.MODE_REPLACE);

// Not sure what to use for the equivalent here
//        tex_attr.setPerspectiveCorrectionMode(TextureAttributes.FASTEST);

        backgroundAppearances = new Appearance[6];
        bgAppVisibility = new boolean[6];

        // Create the shape geometrys for each side. There is no need for appearance as
        // we are going to create this as an unlit, white box.
        Shape3D shape;
        Appearance app;
        TextureUnit[] tex_unit = new TextureUnit[1];

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

        tex_unit[0] = new TextureUnit();
        tex_unit[0].setTextureAttributes(tex_attr);

        backgroundMaterial = new Material();
        backgroundMaterial.setLightingEnabled(false);

        BlendAttributes ba = new BlendAttributes();
        ba.setSourceBlendFactor(ba.BLEND_SRC_ALPHA);
        ba.setDestinationBlendFactor(ba.BLEND_ONE_MINUS_SRC_ALPHA);

        app = new Appearance();
        app.setTextureUnits(tex_unit, 1);
        app.setVisible(false);
        app.setBlendAttributes(ba);
        app.setMaterial(backgroundMaterial);

        backgroundAppearances[BACK] = app;
        bgAppVisibility[BACK] = false;

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(back_coords, back_normals));
        bg.addShape(shape);

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

        tex_unit[0] = new TextureUnit();
        tex_unit[0].setTextureAttributes(tex_attr);

        app = new Appearance();
        app.setTextureUnits(tex_unit, 1);
        app.setVisible(false);
        app.setBlendAttributes(ba);
        app.setMaterial(backgroundMaterial);
        backgroundAppearances[FRONT] = app;
        bgAppVisibility[FRONT] = false;

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(front_coords, front_normals));
        bg.addShape(shape);

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

        tex_unit[0] = new TextureUnit();
        tex_unit[0].setTextureAttributes(tex_attr);

        app = new Appearance();
        app.setTextureUnits(tex_unit, 1);
        app.setVisible(false);
        app.setBlendAttributes(ba);
        app.setMaterial(backgroundMaterial);
        backgroundAppearances[LEFT] = app;
        bgAppVisibility[LEFT] = false;

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(left_coords, left_normals));
        bg.addShape(shape);

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

        tex_unit[0] = new TextureUnit();
        tex_unit[0].setTextureAttributes(tex_attr);

        app = new Appearance();
        app.setTextureUnits(tex_unit, 1);
        app.setVisible(false);
        app.setBlendAttributes(ba);
        app.setMaterial(backgroundMaterial);
        backgroundAppearances[RIGHT] = app;
        bgAppVisibility[RIGHT] = false;

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(right_coords, right_normals));
        bg.addShape(shape);

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

        tex_unit[0] = new TextureUnit();
        tex_unit[0].setTextureAttributes(tex_attr);

        app = new Appearance();
        app.setBlendAttributes(ba);
        app.setTextureUnits(tex_unit, 1);
        app.setVisible(false);
        app.setMaterial(backgroundMaterial);
        backgroundAppearances[TOP] = app;
        bgAppVisibility[TOP] = false;

        shape = new Shape3D();
        shape.setAppearance(app);
        app.setBlendAttributes(ba);
        shape.setGeometry(createSideGeom(top_coords, top_normals));
        bg.addShape(shape);

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

        tex_unit[0] = new TextureUnit();
        tex_unit[0].setTextureAttributes(tex_attr);

        app = new Appearance();
        app.setTextureUnits(tex_unit, 1);
        app.setVisible(false);
        app.setBlendAttributes(ba);
        app.setMaterial(backgroundMaterial);
        backgroundAppearances[BOTTOM] = app;
        bgAppVisibility[BOTTOM] = false;

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(bottom_coords, bottom_normals));
        bg.addShape(shape);
    }

    /**
     * Convenience method to create the side geometry
     *
     * @param coords THe coordinates to use
     * @param normals The normals to use
     * @return The geometry representing this side
     */
    private VertexGeometry createSideGeom(float[] coords, float[] normals) {

        QuadArray array = new QuadArray();

        array.setVertices(QuadArray.COORDINATE_3, coords, 4);
        array.setNormals(normals);
        array.setTextureCoordinates(BOX_TEX_COORD_TYPES,
                                    BOX_TEX_COORDS);

        return array;
    }

    /**
     * Convenience matrix to check if they're the same values, and ignore if
     * they are.
     *
     * @param mat1 The first matrix
     * @param mat2 The second matrix
     * @return true if they are the same values
     */
    private boolean isSameMatrix(Matrix4f mat1, Matrix4f mat2) {
        return mat1.m00 == mat2.m00 &&
               mat1.m01 == mat2.m01 &&
               mat1.m02 == mat2.m02 &&
               mat1.m03 == mat2.m03 &&
               mat1.m10 == mat2.m10 &&
               mat1.m11 == mat2.m11 &&
               mat1.m12 == mat2.m12 &&
               mat1.m13 == mat2.m13 &&
               mat1.m20 == mat2.m20 &&
               mat1.m21 == mat2.m21 &&
               mat1.m22 == mat2.m22 &&
               mat1.m23 == mat2.m23 &&
               mat1.m30 == mat2.m30 &&
               mat1.m31 == mat2.m31 &&
               mat1.m32 == mat2.m32 &&
               mat1.m33 == mat2.m33;
    }
}
