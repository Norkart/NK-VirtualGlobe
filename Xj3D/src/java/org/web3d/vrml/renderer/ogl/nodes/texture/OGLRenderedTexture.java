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

package org.web3d.vrml.renderer.ogl.nodes.texture;

// External imports
import org.j3d.aviatrix3d.*;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;

import javax.media.opengl.GLCapabilities;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.ogl.nodes.*;

import org.web3d.vrml.renderer.common.nodes.texture.BaseRenderedTexture;
import org.web3d.vrml.renderer.ogl.browser.GlobalEffectsGroup;

/**
 * RenderedTexture node implementation for OpenGL.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public class OGLRenderedTexture extends BaseRenderedTexture
    implements OGLTextureNodeType,
               FrameStateListener,
               VRMLExternalSynchronizedNodeType,
               NodeUpdateListener {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.ExternalSynchronizedNodeType };

    /** Index for the main scene in the root group child list */
    private static final int SCENE_CHILD_IDX = 1;

    /** Index for the fog in the root group child list */
    private static final int FOG_CHILD_IDX = 2;

    /** Index for the viewpoint in the root group child list */
    private static final int VIEW_CHILD_IDX = 3;

    /** Index for the background in the root group child list */
    private static final int BG_CHILD_IDX = 4;

    /** The Aviatrix scene that contains the content */
    private SimpleScene avScene;

    /** The aviatrix texture instance that is used */
    private OffscreenTexture2D avTexture;

    /** Handler for the local global effects */
    private GlobalEffectsGroup localGlobals;

    /** The world group that contains everything */
    private Group rootGroup;

    /** Matrix for fetching vp position. */
    private Matrix4f vpMatrix;
    private Matrix4f localMatrix;

    /** Working list of texture objects for the background updates */
    private Texture2D[] tmpTextures;

    /** Working list of texture objects for the background updates */
    private boolean[] tmpTextureFlags;

    /** Working list to get the background colors */
    private float[] tmpColor;

    /** Working list to get the background angles */
    private float[] tmpAngle;

    /** List for collecting path information to calculate inverse matrices */
    private ArrayList pathList;

    /** Array for fetching lists of shared parent nodes */
    private Node[] pathNodes;

    /** Flag indicating if the fog has changed this last frame */
    private boolean fogChanged;

    /** Flag indicating if the fog has changed this last frame */
    private boolean backgroundChanged;

    /** Flag indicating if the fog has changed this last frame */
    private boolean viewpointChanged;

    /** Flag to say the fog type */
    private boolean fogTypeChanged;

    /** Flag to say fog color or range value changed. */
    private boolean fogDetailsChanged;

    /** Flag to say the background sky values changed */
    private boolean backgroundSkyChanged;

    /** Flag to say the background ground values changed */
    private boolean backgroundGroundChanged;

    /** Listener instance for dealing with the current background node */
    private BackgroundListener backgroundListener;

    /** Listener instance for dealing with the current fog node */
    private FogListener fogListener;

    /**
     * One time flag to make sure that painting really happens if this is
     * the first frame and the node is initially set to RENDER_NEXT.
     */
    private boolean initialPaint;

    //
    // Inner classes to do with the field callbacks. VRMLNodeListener doesn't
    // include a Node reference so we can't tell which field really got called.
    // The inner classes are used to set the various *Changed booleans above so
    // that during the next iteration of the event model the values can be
    // updated when the flags change. This is more efficient than performing a
    // check of every value from the last frame compared to the values from
    // this frame.
    //

    /**
     * Inner class for handling background field changes.
     */
    private class BackgroundListener implements VRMLNodeListener {

        /** Field index for the ground angle.*/
        private int gndAngleField;

        /** Field index for the ground colour.*/
        private int gndColorField;

        /** Field index for the ground angle.*/
        private int skyAngleField;

        /** Field index for the ground colour.*/
        private int skyColorField;

        /**
         * Construct a field listener that uses the given index values for
         * the appropriate field.
         */
        BackgroundListener(int gndAngle,
                           int gndColor,
                           int skyAngle,
                           int skyColor) {

            gndAngleField = gndAngle;
            gndColorField = gndColor;
            skyAngleField = skyAngle;
            skyColorField = skyColor;
        }

        public void fieldChanged(int index) {
            if(index == gndAngleField || index == gndColorField)
                backgroundGroundChanged = true;

            if(index == skyAngleField || index == skyColorField)
                backgroundSkyChanged = true;
        }
    }

    /**
     * Inner class for handling fog field changes.
     */
    private class FogListener implements VRMLNodeListener {

        /** Field index for the fog type.*/
        private int typeField;

        /** Field index for the visibility range.*/
        private int visibilityField;

        /** Field index for the colour.*/
        private int colourField;

        /**
         * Construct a field listener that uses the given index values for
         * the appropriate field.
         */
        FogListener(int type, int visibility, int colour) {

            typeField = type;
            visibilityField = visibility;
            colourField = colour;
        }

        public void fieldChanged(int index) {
            if(index == typeField)
                fogTypeChanged = true;

            if(index == visibilityField || index == colourField)
                fogDetailsChanged = true;
        }
    }

    /**
     * Construct a default texture instance.
     */
    public OGLRenderedTexture() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public OGLRenderedTexture(VRMLNodeType node) {
        super(node);

        init();
    }

    //----------------------------------------------------------
    // Methods defined by OGLTextureNodeType
    //----------------------------------------------------------

    /**
     * Set the Aviatrix3D texture representation back into the node
     * implementation.
     *
     * @param index The index of the texture (for multitexture)
     * @param tex The texture object to set
     */
    public void setTexture(int index, Texture tex) {
        // Ignored for this class.
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return avTexture;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        createFogListener();
        createBackgroundListener();

        if(vfScene != null) {
            OGLVRMLNode n = (OGLVRMLNode)vfScene;
            Node geom_group = (Node)n.getSceneGraphObject();

            rootGroup.setChild(geom_group, SCENE_CHILD_IDX);
        }


        if(vfViewpoint != null) {
            Node n = (Node)((OGLVRMLNode)vfViewpoint).getSceneGraphObject();
            rootGroup.setChild(n, VIEW_CHILD_IDX);
        }

        if(vfBackground != null) {
            Node n = (Node)((OGLVRMLNode)vfBackground).getSceneGraphObject();
            rootGroup.setChild(n, BG_CHILD_IDX);
        }

        if(vfFog != null) {
            Node n = (Node)((OGLVRMLNode)vfFog).getSceneGraphObject();
            rootGroup.setChild(n, FOG_CHILD_IDX);
        }

        // set up the GLCapabilities from the vfDimensions field values
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(false);
        caps.setPbufferRenderToTexture(true);

        // TODO: not sure why this has to be here now
        caps.setPbufferRenderToTextureRectangle(true);

        avTexture = new OffscreenTexture2D(caps,
                                           vfDimensions[0],
                                           vfDimensions[1]);

        SimpleViewport vp = new SimpleViewport();
        vp.setDimensions(0, 0, vfDimensions[0], vfDimensions[1]);
        vp.setScene(avScene);

        SimpleLayer l = new SimpleLayer();
        l.setViewport(vp);

        Layer[] layers = { l };

        avTexture.setLayers(layers, 1);

        postEventEvaluation();

        if(updateFlag != UPDATE_NONE)
            avTexture.setRepaintRequired(true);
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
        if(src != rootGroup)
            return;

        OGLVRMLNode n = (OGLVRMLNode)vfScene;
        Node geom = (Group)n.getSceneGraphObject();

        rootGroup.setChild(geom, SCENE_CHILD_IDX);

        geom = null;

        if(vfViewpoint != null)
            geom = (Node)((OGLVRMLNode)vfViewpoint).getSceneGraphObject();

        rootGroup.setChild(geom, VIEW_CHILD_IDX);

        geom = null;

        if(vfBackground != null)
            geom = (Node)((OGLVRMLNode)vfBackground).getSceneGraphObject();

        rootGroup.setChild(geom, BG_CHILD_IDX);

        geom = null;

        if(vfFog != null)
            geom = (Node)((OGLVRMLNode)vfFog).getSceneGraphObject();

        rootGroup.setChild(geom, FOG_CHILD_IDX);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
    }

    //----------------------------------------------------------
    // Methods defined by VRMLExternalSynchronizedNodeType
    //----------------------------------------------------------

    /**
     * Notification that event evaluation is about to start.  This is a safer
     * time to modify the underlying rendering structures.
     */
    public void preEventEvaluation() {
        // do nothing
        if(!initialPaint && updateFlag == UPDATE_NEXT) {
            avTexture.setRepaintRequired(false);

            vfUpdate = "NONE";
            updateFlag = UPDATE_NONE;
            initialPaint = false;

            hasChanged[FIELD_UPDATE] = true;
            fireFieldChanged(FIELD_UPDATE);
        }
    }

    /**
     * Notification that event evaluation is about to start.  This is a safer
     * time to modify the underlying rendering structures.
     */
    public void postEventEvaluation() {

        if(updateFlag == UPDATE_NONE)
            return;

        // Now the fun begins. Most of this code is ripped from the handling
        // in DefaultSensorHandler as the basic premise is the same - update
        // the various matrix information in the GlobalEffectsGroup.


        if(vfBackground != null) {
            OGLBackgroundNodeType bg = (OGLBackgroundNodeType)vfBackground;

            if(backgroundChanged) {
                Texture2D[] textures = bg.getBackgroundTextures();

                for(int i = 0; i < 6; i++)
                    tmpTextureFlags[i] = true;

                localGlobals.updateBackgroundTextures(textures,
                                                      tmpTextureFlags);
            } else {
                // Has anything changed this frame?
                if(bg.getChangedTextures(tmpTextures, tmpTextureFlags))
                    localGlobals.updateBackgroundTextures(tmpTextures,
                                                           tmpTextureFlags);
            }


            // Trick the system into updating
            backgroundSkyChanged = true;
            backgroundGroundChanged = true;

            updateBackgroundSphere();
            backgroundChanged = false;

            // Now that the viewpoint is complete, reuse the matrix for the
            // background position information.
            Group node = (Group)((OGLVRMLNode)vfBackground).getSceneGraphObject();
            getLocalToVworld(node, vpMatrix);
            localGlobals.setBackgroundMatrix(vpMatrix);

        } else if(backgroundChanged) {
            for(int i = 0; i < 6; i++) {
                tmpTextures[i] = null;
                tmpTextureFlags[i] = true;
            }

            localGlobals.updateBackgroundTextures(tmpTextures,
                                                  tmpTextureFlags);

            // do we need to clean out the background sphere too?
            vpMatrix.setIdentity();
            localGlobals.setBackgroundMatrix(vpMatrix);

            backgroundChanged = false;
        }

        if(vfFog != null) {
            float dist = vfFog.getVisibilityRange();
            vfFog.getColor(tmpColor);
            int type = vfFog.getFogType();

            if(fogChanged) {
                localGlobals.enableFog(type);
                localGlobals.setFogDetails(dist,
                                           tmpColor[0],
                                           tmpColor[1],
                                           tmpColor[2]);

                fogChanged = false;
            } else {
                // Has anything changed this frame?
                if(fogTypeChanged) {
                    localGlobals.enableFog(type);
                    fogTypeChanged = false;
                }

                if(fogDetailsChanged) {
                    localGlobals.setFogDetails(dist,
                                               tmpColor[0],
                                               tmpColor[1],
                                               tmpColor[2]);
                    fogDetailsChanged = false;
                }
            }

            Group node = (Group)((OGLVRMLNode)vfFog).getSceneGraphObject();
            getLocalToVworld(node, vpMatrix);
            localGlobals.setFogMatrix(vpMatrix);

            fogChanged = false;

        } else if(fogChanged) {
            vpMatrix.setIdentity();
            localGlobals.setFogMatrix(vpMatrix);

            localGlobals.enableFog(VRMLFogNodeType.FOG_TYPE_DISABLE);
            localGlobals.setFogDetails(1, 0, 0, 0);

            fogChanged = false;
        }

        if(vfViewpoint != null) {
            TransformGroup transform =
                ((OGLViewpointNodeType)vfViewpoint).getPlatformGroup();

            getLocalToVworld(transform, vpMatrix);

            localGlobals.setViewMatrix(vpMatrix);

            viewpointChanged = false;
        } else if(viewpointChanged) {
            // set up the default VP in the usual spot
            vpMatrix.setIdentity();
            vpMatrix.m23 = 10;

            localGlobals.setViewMatrix(vpMatrix);

            viewpointChanged = false;
        }
    }


    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * The update state has changed. Override if need be for any
     * renderer-specific needs.
     *
     * @param state The new state for updates
     */
    protected void setUpdate(String state) {
        super.setUpdate(state);

        if(!inSetup)
            avTexture.setRepaintRequired(updateFlag != UPDATE_NONE);
    }

    /**
     * Set new value to be used for the background field.
     *
     * @param bg The new background.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setBackground(VRMLNodeType bg)
        throws InvalidFieldValueException {

        if(vfBackground != null)
            vfBackground.removeNodeListener(backgroundListener);

        super.setBackground(bg);

        if(backgroundListener == null)
            createBackgroundListener();

        vfBackground.addNodeListener(backgroundListener);

        if(inSetup)
            return;

        backgroundChanged = true;
        if (rootGroup.isLive())
            rootGroup.boundsChanged(this);
        else
            updateNodeBoundsChanges(rootGroup);
    }

    /**
     * Set new value to be used for the fog field.
     *
     * @param fog The new fog.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setFog(VRMLNodeType fog)
        throws InvalidFieldValueException {

        if(vfFog != null) {

            vfFog.removeNodeListener(fogListener);
        }

        super.setFog(fog);

        if(fogListener == null)
            createFogListener();

        vfFog.addNodeListener(fogListener);

        if(inSetup)
            return;

        fogChanged = true;
        if (rootGroup.isLive())
            rootGroup.boundsChanged(this);
        else
            updateNodeBoundsChanges(rootGroup);
    }

    /**
     * Set new value to be used for the viewpoint field.
     *
     * @param vp The new viewpoint.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setViewpoint(VRMLNodeType vp)
        throws InvalidFieldValueException {

        super.setViewpoint(vp);

        if(inSetup)
            return;

        viewpointChanged = true;
        if (rootGroup.isLive())
            rootGroup.boundsChanged(this);
        else
            updateNodeBoundsChanges(rootGroup);
    }

    /**
     * Set new value to be used for the scene field.
     *
     * @param scn The new scene.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setScene(VRMLNodeType scn)
        throws InvalidFieldValueException {

        super.setScene(scn);

        if(rootGroup.isLive())
            rootGroup.boundsChanged(this);
        else if(vfScene != null) {
            OGLVRMLNode n = (OGLVRMLNode)vfScene;
            Group geom_group = (Group)n.getSceneGraphObject();

            rootGroup.setChild(geom_group, 1);
        }
    }

    /**
     * Convenience method to walk to the root of the scene and calculate the
     * root to virtual world coordinate location of the given node. If a
     * sharedGroup is found, then take the first parent listed always.
     *
     * @param terminal The end node to calculate from
     * @param mat The matrix to put the final result into
     */
    private void getLocalToVworld(Node terminal, Matrix4f mat) {

        Node parent = terminal.getParent();
        pathList.clear();
        OGLUserData udata;

        if(terminal instanceof TransformGroup) {
            udata = (OGLUserData) terminal.getUserData();
            if (udata != null) {
                pathList.add(udata.owner);
            }
        }
        while(parent != null) {
            if(parent instanceof SharedGroup) {
                SharedGroup sg = (SharedGroup)parent;

                int num_parents = sg.numParents();

                if(num_parents == 0)
                    break;
                else if(num_parents > pathNodes.length)
                    pathNodes = new Node[num_parents];

                sg.getParents(pathNodes);
                parent = pathNodes[0];
            } else {
                if(parent instanceof TransformGroup) {
                    udata = (OGLUserData) parent.getUserData();
                    if (udata != null) {
                        pathList.add(udata.owner);
                    }
                }
                parent = parent.getParent();
            }
        }

        int num_nodes = pathList.size();
        mat.setIdentity();

        // use vwTransform for fetching the tx. It is only a temp var anyway
        for(int i = num_nodes - 1; i >= 0; i--) {
            OGLTransformNodeType tg = (OGLTransformNodeType) pathList.get(i);
            localMatrix.set(tg.getTransform());
            mat.mul(localMatrix);
        }
    }

    /**
     * Convenience method to update the background colours used in the sky/ground
     * sphere.
     */
    private void updateBackgroundSphere() {
        if(backgroundGroundChanged) {
            backgroundGroundChanged = false;
            int num_col = vfBackground.getNumGroundColors();

            if(num_col * 3 > tmpColor.length) {
                tmpColor = new float[num_col * 3];
                tmpAngle = new float[num_col - 1];
            }

            if(num_col != 0)
                vfBackground.getGroundValues(tmpColor, tmpAngle);

            localGlobals.updateBackgroundGround(tmpColor, tmpAngle, num_col);
        }

        if(backgroundSkyChanged) {
            backgroundSkyChanged = false;
            int num_col = vfBackground.getNumSkyColors();
            if(num_col * 3 > tmpColor.length) {
                tmpColor = new float[num_col * 3];
                tmpAngle = new float[num_col - 1];
            }

            if(num_col != 0)
                vfBackground.getSkyValues(tmpColor, tmpAngle);

            localGlobals.updateBackgroundSky(tmpColor, tmpAngle, num_col);
        }
    }

    /**
     * Convenience method to setup a new fog listener instance.
     */
    private void createFogListener() {
        if(vfFog != null) {
            int fog_type = vfFog.getFieldIndex("fogType");
            int fog_vis = vfFog.getFieldIndex("visibilityRange");
            int fog_colour = vfFog.getFieldIndex("color");

            fogListener = new FogListener(fog_type, fog_vis, fog_colour);
        }
    }

    /**
     * Convenience method to setup a new background listener instance.
     */
    private void createBackgroundListener() {
        if(vfBackground != null) {

            int gnd_angle = vfBackground.getFieldIndex("groundAngle");
            int gnd_color = vfBackground.getFieldIndex("groundColor");
            int sky_angle = vfBackground.getFieldIndex("skyAngle");
            int sky_color = vfBackground.getFieldIndex("skyColor");

            backgroundListener = new BackgroundListener(gnd_angle,
                                                        gnd_color,
                                                        sky_angle,
                                                        sky_color);
        }
    }

    /**
     * Common internal initialization process.
     */
    private void init() {
        avScene = new SimpleScene();

        localGlobals = new GlobalEffectsGroup(avScene);
        localGlobals.initialize();
        localGlobals.useHeadlight(true);

        rootGroup = new Group();
        rootGroup.addChild(localGlobals);
        rootGroup.addChild(null);
        rootGroup.addChild(null);
        rootGroup.addChild(null);
        rootGroup.addChild(null);

        avScene.setRenderedGeometry(rootGroup);

        vpMatrix = new Matrix4f();
        localMatrix = new Matrix4f();

        pathList = new ArrayList();
        pathNodes = new Node[20];

        tmpTextures = new Texture2D[6];
        tmpTextureFlags = new boolean[6];
        tmpColor = new float[24];
        tmpAngle = new float[7];

        fogChanged = false;
        fogTypeChanged = false;
        fogDetailsChanged = false;
        backgroundChanged = false;
        backgroundSkyChanged = false;
        backgroundGroundChanged = false;

        // set up the default VP in the usual spot
        vpMatrix.setIdentity();
        vpMatrix.m23 = 10;

        localGlobals.setViewMatrix(vpMatrix);
        localGlobals.updateBackgroundSky(tmpColor, tmpAngle, 1);

        initialPaint = true;
    }
}
