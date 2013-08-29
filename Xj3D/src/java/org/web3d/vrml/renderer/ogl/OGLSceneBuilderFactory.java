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

package org.web3d.vrml.renderer.ogl;

// External imports
// None

// Local imports
import org.xj3d.core.loading.SceneBuilder;
import org.xj3d.core.loading.SceneBuilderFactory;

/**
 * OpenGL factory used to create new instances of the scene builder
 * on demand.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class OGLSceneBuilderFactory implements SceneBuilderFactory {

    /** Flag indicating the requirement to use VRML97 only */
    private boolean vrml97Only;

    /** Flag indicating if we should load backgrounds */
    private boolean loadBackgrounds;

    /** Flag indicating if we should load fogs */
    private boolean loadFogs;

    /** Flag indicating if we should load lights */
    private boolean loadLights;

    /** Flag indicating if we should load audio nodes */
    private boolean loadAudio;

    /** Flag indicating if we should load viewpoints */
    private boolean loadViewpoints;

    /** Flag indicating if we should load externals */
    private boolean loadExternals;

    /**
     * Create a new factory instance that will generate builders according to
     * the nominated requirements.
     *
     * @param forceVRML97 true if this should only handle VRML97 files
     * @param useBackgrounds true if BackgroundNodeTypes should be loaded
     * @param useFogs true if FogNodeTypes should be loaded
     * @param useLights true if LightNodeTypes should be loaded
     * @param useAudio true if AudioClipNodeTypes should be loaded
     * @param useViewpoints true if ViewpointNodeTypes should loaded
     * @param useExternals true if ExternalNodeTypes should be loaded
     */
    public OGLSceneBuilderFactory(boolean forceVRML97,
                                  boolean useBackgrounds,
                                  boolean useFogs,
                                  boolean useLights,
                                  boolean useAudio,
                                  boolean useViewpoints,
                                  boolean useExternals) {

        vrml97Only = forceVRML97;

        loadBackgrounds = useBackgrounds;
        loadFogs = useFogs;
        loadLights = useLights;
        loadAudio = useAudio;
        loadViewpoints = useViewpoints;
        loadExternals = useExternals;
    }

    /**
     * Create a new scene builder instance that has been pre-configured
     * according to the factory parameters.
     *
     * @return A fresh instance
     */
    public SceneBuilder createBuilder() {

        OGLVRMLSceneBuilder builder = new OGLVRMLSceneBuilder();
        builder.allowVRML97Only(vrml97Only);
        builder.setLoadRequirements(loadBackgrounds,
                                    loadFogs,
                                    loadLights,
                                    loadAudio,
                                    loadViewpoints,
                                    loadExternals);

        return builder;
    }
}

