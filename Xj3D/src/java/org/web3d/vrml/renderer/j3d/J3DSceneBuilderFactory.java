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

package org.web3d.vrml.renderer.j3d;

// External imports
import java.util.Map;

// Local imports
import org.xj3d.core.loading.SceneBuilder;
import org.xj3d.core.loading.SceneBuilderFactory;

/**
 * Java3D factory used to create new instances of the scene builder
 * on demand.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class J3DSceneBuilderFactory implements SceneBuilderFactory {

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

    /** The map of the override capability bit settings */
    private Map overrideCapBitsMap;

    /** The map of the required capability bit settings */
    private Map requiredCapBitsMap;

    /** The map of the override capability bit settings */
    private Map overrideFreqBitsMap;

    /** The map of the required capability bit settings */
    private Map requiredFreqBitsMap;

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
    public J3DSceneBuilderFactory(boolean forceVRML97,
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
     * Provide the set of mappings that override anything that the loader
     * might set.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits)
    {
        overrideCapBitsMap = capBits;
        overrideFreqBitsMap = freqBits;
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits)
    {
        requiredCapBitsMap = capBits;
        requiredFreqBitsMap = freqBits;
    }

    /**
     * Create a new scene builder instance that has been pre-configured
     * according to the factory parameters.
     *
     * @return A fresh instance
     */
    public SceneBuilder createBuilder() {

        J3DVRMLSceneBuilder builder = new J3DVRMLSceneBuilder();
        builder.allowVRML97Only(vrml97Only);
        builder.setLoadRequirements(loadBackgrounds,
                                    loadFogs,
                                    loadLights,
                                    loadAudio,
                                    loadViewpoints,
                                    loadExternals);

        if((overrideCapBitsMap != null) && (overrideFreqBitsMap != null))
            builder.setCapabilityOverrideMap(overrideCapBitsMap,
                                             overrideFreqBitsMap);

        if((requiredCapBitsMap != null) && (requiredFreqBitsMap != null))
            builder.setCapabilityRequiredMap(requiredCapBitsMap,
                                             requiredFreqBitsMap);

        return builder;
    }
}

