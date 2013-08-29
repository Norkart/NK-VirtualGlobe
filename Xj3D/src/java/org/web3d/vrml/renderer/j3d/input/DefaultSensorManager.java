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

package org.web3d.vrml.renderer.j3d.input;

// External imports
import javax.media.j3d.*;

import java.awt.event.KeyEvent;
import java.util.ArrayList;


import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.AxisAngle4d;

// Local imports
import org.web3d.vrml.nodes.*;

import org.xj3d.core.eventmodel.runtime.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.IntHashMap;
import org.web3d.util.ObjectArray;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLDeviceSensorNodeType;
import org.web3d.vrml.util.NodeArray;
import org.web3d.vrml.renderer.common.input.BaseSensorManager;
import org.web3d.vrml.renderer.common.input.TimeSensorManager;
import org.web3d.vrml.renderer.common.nodes.environment.BaseProximitySensor;
import org.web3d.vrml.renderer.common.nodes.environment.BaseVisibilitySensor;
import org.web3d.vrml.renderer.j3d.nodes.J3DBackgroundNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DFogNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DViewpointNodeType;

/**
 * Default implementation of the SensorManager interface for the Java3D
 * renderer.
 *
 * @author Justin Couch
 * @version $Revision: 1.47 $
 */
public class DefaultSensorManager extends BaseSensorManager
    implements J3DSensorManager {

    /** The -z axis to get for the orientation */
    private static final Vector3d Z_AXIS = new Vector3d(0, 0, -1);

    /** The bound viewpoint used last frame */
    private J3DViewpointNodeType currentViewpoint;

    /** The bound background used last frame */
    private J3DBackgroundNodeType currentBackground;

    /** The bound fog used last frame */
    private J3DFogNodeType currentFog;

    /** Matrix for fetching vp position. */
    private Transform3D vpMatrix;
    private Transform3D localMatrix;

    /** Scratch var for the VP position from the transform */
    private Vector3d t3dPosition;

    /** Scratch var for getting the position of the VP */
    private Point3d position;

    /** Scratch var for getting the orientation of the VP */
    private Vector3d orientation;

    /** Scratch var for getting the orientation of the VP */
    private AxisAngle4d oriAxisAngle;

    /** Scratch var for converting a Transform3D to a matrix */
    private Matrix4d mat;

    /** List of available sensor handlers */
    private VisibilityManager visibilityHandler;

    /** Global effects manager for viewpoints, fogs etc */
    private J3DGlobalEffectsHandler globalEffects;

    /** Renderer effects manager for terrains, overlays, particles etc */
    private J3DRendererEffectsHandler rendererEffects;

    /** List of available view-dependent node handlers */
    private AreaManager areaHandler;

    /** Picking Manager */
    private J3DPickingManager pickManager;

    /** Working list of texture objects for the background updates */
    private Texture2D[] tmpTextures;

    /** Working list of texture objects for the background updates */
    private boolean[] tmpTextureFlags;

    /** Working list to get the background colors */
    private float[] tmpColor;

    /** Working list to get the background angles */
    private float[] tmpAngle;

    /** The current scene graph path for viewpoints */
    private SceneGraphPath viewpointPath;

    /** The current scene graph path for backgrounds */
    private SceneGraphPath backgroundPath;

    /** The current scene graph path for fog */
    private SceneGraphPath fogPath;

    /** Flag to say the viewpoint path has link nodes */
    private boolean viewpointSharedPath;

    /** Flag to say the background path has link nodes */
    private boolean backgroundSharedPath;

    /** Flag to say the fog path has link nodes */
    private boolean fogSharedPath;

    /** Have we issued a warning about not supporting OrthoCamera */
    private boolean orthoWarning;

    /**
     * Create a new default instance of the manager. It will only register a
     * handler for TimeSensors. Anything other than that will require the end
     * user code to register an appropriate manager.
     */
    public DefaultSensorManager() {
        super();

        inputHandler = new DefaultUserInputHandler();
        inputHandler.setVRMLClock(timeSensors);

        keyEvents = new KeyEvent[DEFAULT_EVENT_SIZE];

        vpMatrix = new Transform3D();
        localMatrix = new Transform3D();
        t3dPosition = new Vector3d();
        position = new Point3d();
        orientation = new Vector3d();
        oriAxisAngle = new AxisAngle4d();

        visibilityHandler = new VisibilityManager();
        areaHandler = new AreaManager();

        tmpTextures = new Texture2D[6];
        tmpTextureFlags = new boolean[6];
        tmpColor = new float[24];
        tmpAngle = new float[7];
        mat = new Matrix4d();

        orthoWarning = false;
    }

    //-------------------------------------------------------------
    // Methods required by SensorManager
    //-------------------------------------------------------------

    /**
     * Set the manager that is responsible for handling picking sensors.
     *
     * @param picker Reference to the manager instance to use or null
     */
    public void setPickingManager(PickingManager picker) {
        if((picker != null) && !(picker instanceof J3DPickingManager))
            throw new IllegalArgumentException("Pick manager not suitable for Java3D");

        pickManager = (J3DPickingManager)picker;

        if(picker != null)
            picker.setErrorReporter(errorReporter);
    }

    /**
     * Set the bindable stacks used for viewpoint and navigation nodes.
     * Used by the sensor for computing navigation and sensor information.
     *
     * @param vp The stack for viewpoints
     * @param nav The stack for navigationInfo nodes
     * @param back The stack for background nodes
     * @param fog The stack for fog nodes
     */
    public void setNavigationStacks(BindableNodeManager vp,
                                    BindableNodeManager nav,
                                    BindableNodeManager back,
                                    BindableNodeManager fog) {
        viewStack = vp;
        navInfoStack = nav;
        backgroundStack = back;
        fogStack = fog;
    }

    /**
     * Set the global effects handler for this sensor manager.
     *
     * @param handler The new handler instance to use
     */
    public void setGlobalEffectsHandler(J3DGlobalEffectsHandler handler) {
        globalEffects = handler;
    }

    /**
     * Set the renderer effects handler for this sensor manager.
     *
     * @param handler The new handler instance to use
     */
    public void setRendererEffectsHandler(J3DRendererEffectsHandler handler) {
        rendererEffects = handler;
    }

    /**
     * Set the branchgroup that acts as the world root for doing picking
     * against.
     *
     * @param root The world root to pick against
     */
    public void setWorldRoot(BranchGroup root) {
        areaHandler.setWorldRoot(root);
        visibilityHandler.setWorldRoot(root);
        if (pickManager != null)
            pickManager.setWorldRoot(root);
    }

    /**
     * Set the world scale applied.  This will scale down navinfo parameters
     * to fit into the world.
     *
     * @param scale The new world scale.
     */
    public void setWorldScale(float scale) {
        worldScale = scale;
        invWorldScale = 1f / scale;

        // TODO: Need to do something with this
/*
        if (currentViewpoint != null)
            currentViewpoint.setWorldScale(scale);

        visibilityHandler.setWorldScale(scale);
*/
    }

    /**
     * Process the user input to the scene now. User input is the mouse and
     * keyboard processing that would be used to send events to
     * Key/StringSensors and perform navigation and picking duties as well as
     * adjust items like billboards and LODs.
     *
     * @param time The clock time, in Java coordinates, not VRML
     */
    public void processUserInput(long time) {
        timeSensors.clockTick(time);

        if(!navigationEnabled)
            return;

        if(!initialised)
            initialise();

        // Fetch the view global position and orientation for use in the
        // sensor evaluation.
        J3DViewpointNodeType vp =
            (J3DViewpointNodeType)viewStack.getBoundNode();

        if(vp != currentViewpoint) {
            inputHandler.setCenterOfRotation(vp.getCenterOfRotation());
            currentViewpoint = vp;
            viewpointPath = currentViewpoint.getSceneGraphPath();
            viewpointSharedPath = checkForLinks(viewpointPath);
            ((J3DUserInputHandler)inputHandler).setViewPath(viewpointSharedPath ? viewpointPath : null);
        } else if(currentViewpoint.hasScenePathChanged()) {
            viewpointPath = currentViewpoint.getSceneGraphPath();
            viewpointSharedPath = checkForLinks(viewpointPath);
            ((J3DUserInputHandler)inputHandler).setViewPath(viewpointSharedPath ? viewpointPath : null);
        }

        J3DBackgroundNodeType bg =
            (J3DBackgroundNodeType)backgroundStack.getBoundNode();

        if(bg != currentBackground) {
            Texture2D[] textures = bg.getBackgroundTextures();
            for(int i = 0; i < 6; i++)
                tmpTextureFlags[i] = true;

            globalEffects.updateBackgroundTextures(textures,
                                                   tmpTextureFlags);
            // Trick the system into updating
            backgroundSkyChanged = true;
            backgroundGroundChanged = true;

            updateBackgroundSphere(bg);

            if(currentBackground != null)
                currentBackground.removeNodeListener(backgroundListener);

            bg.addNodeListener(backgroundListener);

            currentBackground = bg;
            backgroundPath = currentBackground.getSceneGraphPath();
            backgroundSharedPath = checkForLinks(backgroundPath);
        } else {
            // Has anything changed this frame?
            if(bg.getChangedTextures(tmpTextures, tmpTextureFlags))
                globalEffects.updateBackgroundTextures(tmpTextures,
                                                       tmpTextureFlags);

            updateBackgroundSphere(currentBackground);

            if(currentBackground.hasScenePathChanged()) {
                backgroundPath = currentBackground.getSceneGraphPath();
                backgroundSharedPath = checkForLinks(backgroundPath);
            }
        }

        J3DFogNodeType fog = (J3DFogNodeType)fogStack.getBoundNode();

        float dist = fog.getVisibilityRange();
        fog.getColor(tmpColor);
        int type = fog.getFogType();

        if(fog != currentFog) {
            globalEffects.enableFog(type);
            globalEffects.setFogDetails(dist,
                                        tmpColor[0],
                                        tmpColor[1],
                                        tmpColor[2]);

            if(currentFog != null)
                currentFog.removeNodeListener(fogListener);

            fog.addNodeListener(fogListener);

            currentFog = fog;
            fogPath = currentFog.getSceneGraphPath();
            fogSharedPath = checkForLinks(fogPath);
        } else {
            // Has anything changed this frame?
            if(fogTypeChanged)
                globalEffects.enableFog(type);

            if(fogDetailsChanged)
                globalEffects.setFogDetails(dist,
                                            tmpColor[0],
                                            tmpColor[1],
                                            tmpColor[2]);

            if(currentFog.hasScenePathChanged()) {
                fogPath = currentFog.getSceneGraphPath();
                fogSharedPath = checkForLinks(fogPath);
            }
        }
/*
        if (terrainManager != null) {
            ObjectArray removedTerrains = terrainManager.getRemovedSectors();
            if (removedTerrains.size() != 0)
                rendererEffects.sectorsRemoved(removedTerrains);

            ObjectArray addedTerrains = terrainManager.getAddedSectors();
            if (addedTerrains.size() != 0)
                rendererEffects.sectorsAdded(addedTerrains);
        }
*/
        clearChangeFlags();

        // First process mouse events etc.
        if(inputManager != null) {
            inputManager.processTrackers();

            int num_events = inputManager.getKeyEvents(keyEvents);
            if(num_events != 0)
                keySensors.sendEvents(keyEvents, num_events);
        }

        VRMLNavigationInfoNodeType nav =
            (VRMLNavigationInfoNodeType)navInfoStack.getBoundNode();

        TransformGroup transform = currentViewpoint.getPlatformGroup();
        try {
            if(viewpointSharedPath)
                transform.getLocalToVworld(viewpointPath, vpMatrix);
            else
                transform.getLocalToVworld(vpMatrix);
        } catch (IllegalArgumentException iae) {
            // Java3D is still moving the viewpoint around.
            // Ignore because there's nothing to do.
            return;
        }
        transform.getTransform(localMatrix);
        vpMatrix.mul(localMatrix);

        vpMatrix.transform(Z_AXIS, orientation);
        vpMatrix.get(t3dPosition);
        position.set(t3dPosition);

        if(globalEffects != null)
            globalEffects.setViewMatrix(vpMatrix);
        if (rendererEffects != null)
            rendererEffects.setViewMatrix(vpMatrix);

        float vis_limit = nav.getVisibilityLimit();

        if (numAreaListeners > 0) {
            vpMatrix.invert();
            areaHandler.processFrame(position, orientation, vpMatrix, vis_limit);
        }

        // Now that the viewpoint is complete, reuse the matrix for the
        // background position information.
        BranchGroup branch =
            (BranchGroup)currentBackground.getSceneGraphObject();

        if(backgroundSharedPath)
            branch.getLocalToVworld(backgroundPath, vpMatrix);
        else
            branch.getLocalToVworld(vpMatrix);

        if(globalEffects != null)
            globalEffects.setBackgroundMatrix(vpMatrix);

        branch = (BranchGroup)currentFog.getSceneGraphObject();

        if(fogSharedPath)
            branch.getLocalToVworld(fogPath, vpMatrix);
        else
            branch.getLocalToVworld(vpMatrix);

        if(globalEffects != null)
            globalEffects.setFogMatrix(vpMatrix);

        if (numVisibilityListeners > 0) {
            vpMatrix.get(mat);
            oriAxisAngle.set(mat);

            float[] fovArray = vp.getFieldOfView();
            float fov;

            if (fovArray.length == 1) {
                fov = fovArray[1];
            } else {
                // TODO: Not really right for OrthoViewpoint
                fov = Math.PI;

                if (orthoWarning == false) {
                    errorReporter.warningReport("OrthoCamera not supported in Java3D", null);
                    orthoWarning = true;
                }
            }

            visibilityHandler.processFrame(position, orientation, oriAxisAngle, vis_limit, fov);
        }

        pickManager.processPickSensors(time);
    }

    /**
     * Update the viewing matrix.  Call this when you want the SensorManager to update
     * the viewing matrix.  Typically after all user input and events have resolved.
     */
    public void updateViewMatrix() {

        try {
            if(!navigationEnabled)
                return;

            TransformGroup transform = currentViewpoint.getPlatformGroup();

            if(viewpointSharedPath)
                transform.getLocalToVworld(viewpointPath, vpMatrix);
            else
                transform.getLocalToVworld(vpMatrix);

            transform.getTransform(localMatrix);
            vpMatrix.mul(localMatrix);

            if(globalEffects != null)
                globalEffects.setViewMatrix(vpMatrix);
            if (rendererEffects != null)
                rendererEffects.setViewMatrix(vpMatrix);
        } catch (IllegalArgumentException iae) {
            // Java3D is still moving the viewpoint around.
            // Ignore because there's nothing to do.
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Convenience method to check whether the given scene graph path contains
     * any link nodes.
     *
     * @param path The path to check
     * @return true if there are link nodes found
     */
    private boolean checkForLinks(SceneGraphPath path) {
        boolean ret_val = false;

        if (path == null)
           return false;

        int size = path.nodeCount();

        for(int i = 0; i < size && !ret_val; i++) {
            if(path.getNode(i) instanceof Link)
                ret_val = true;
        }

        return ret_val;
    }

    /**
     * Convenience method to update the background colours used in the sky/ground
     * sphere.
     *
     * @param bg The background node to use as the basis
     */
    private void updateBackgroundSphere(VRMLBackgroundNodeType bg) {
        if(backgroundGroundChanged) {
            int num_col = bg.getNumGroundColors();

            if(num_col * 3 > tmpColor.length) {
                tmpColor = new float[num_col * 3];
                tmpAngle = new float[num_col - 1];
            }

            if(num_col != 0)
                bg.getGroundValues(tmpColor, tmpAngle);

            globalEffects.updateBackgroundGround(tmpColor, tmpAngle, num_col);
        }

        if(backgroundSkyChanged) {
            int num_col = bg.getNumSkyColors();
            if(num_col * 3 > tmpColor.length) {
                tmpColor = new float[num_col * 3];
                tmpAngle = new float[num_col - 1];
            }

            if(num_col != 0)
                bg.getSkyValues(tmpColor, tmpAngle);

            globalEffects.updateBackgroundSky(tmpColor, tmpAngle, num_col);
        }
    }
}
