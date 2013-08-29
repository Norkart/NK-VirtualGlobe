/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.eventmodel;

// External imports
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

// Local imports
import org.web3d.browser.NavigationStateListener;
import org.web3d.browser.SensorStatusListener;
import org.web3d.browser.ViewpointStatusListener;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLLayerNodeType;
import org.web3d.vrml.nodes.VRMLViewportNodeType;
import org.web3d.vrml.nodes.VRMLWorldRootNodeType;
import org.xj3d.sai.Xj3DBrowser;

/**
 * An abstract representation of a class that would be responsible for
 * performing management of a single layer.
 * <p>
 *
 * The manager is responsible for all input and sensor handling for this
 * layer instance. Sensors and other interactions can be enabled on a per-layer
 * basis. Typically, for any scene, only one layer is the focus for navigation
 * input.
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public interface LayerManager {

    /**
     * The layer contains a viewport that is currently undefined due to externproto
     * resolution. In this case, treat the viewport as having zero width and height -
     * ie invisible. This is defined to be the same value as
     * {@link VRMLViewportNodeType#VIEWPORT_UNDEFINED}
     */
    public static final int VIEWPORT_UNDEFINED =
        VRMLViewportNodeType.VIEWPORT_UNDEFINED;

    /**
     * The layer contains a viewport that takes up the full window space. This
     * is defined to be the same value as
     * {@link VRMLViewportNodeType#VIEWPORT_FULLWINDOW}
     */
    public static final int VIEWPORT_FULLWINDOW =
        VRMLViewportNodeType.VIEWPORT_FULLWINDOW;

    /**
     * The layer contains a viewport that takes up a percentage of the window. This
     * is defined to be the same value as
     * {@link VRMLViewportNodeType#VIEWPORT_PROPORTIONAL}
     */
    public static final int VIEWPORT_PROPORTIONAL =
        VRMLViewportNodeType.VIEWPORT_PROPORTIONAL;

    /**
     * The layer contains a viewport that takes up a fixed pixel size. This is
     * defined to be the same value as
     * {@link VRMLViewportNodeType#VIEWPORT_FIXED}
     */
    public static final int VIEWPORT_FIXED =
        VRMLViewportNodeType.VIEWPORT_FIXED;

    /**
     * The layer contains a viewport that is configurable to use either fixed
     * or proportional size on each dimension. This is defined to be the same
     * value as {@link VRMLViewportNodeType#VIEWPORT_CUSTOM}
     */
    public static final int VIEWPORT_CUSTOM =
        VRMLViewportNodeType.VIEWPORT_CUSTOM;

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Complete the initialisation of the layer manager now.
     *
     * @param sensors The sensor manager to start with from the global list
     */
    public void initialise(SensorManager sensors);

    /**
     * Set or reset the layer ID to the new ID value.
     *
     * @param id A non-negative ID for the layer
     */
    public void setLayerId(int id);

    /**
     * Set the specification version that should be handled by this manager.
     * This is needed so that the correct version of the default bindables are
     * instantiated before the rest of the world loads. For example, the default
     * nav type for VRML is different to X3D, so this makes sure all the right
     * spec stuff is catered for.
     *
     * @param major The spec major version number
     * @param minor The spec minor version number
     */
    public void setSpecVersion(int major, int minor);

    /**
     * Enable or disable this layer to be currently navigable layer. The
     * navigable layer takes the input from the input devices and interacts
     * with the currently bound viewpoint etc.
     *
     * @param state True to enable this layer as navigable
     */
    public void setActiveNavigationLayer(boolean state);

    /**
     * Check to see if this is the active navigation layer.
     *
     * @return true if this is the currently active layer for navigation
     */
    public boolean isActiveNavigationLayer();

    /**
     * Set the desired navigation mode. The mode string is one of the
     * spec-defined strings for the NavigationInfo node in the VRML/X3D
     * specification.
     *
     * @param mode The requested mode.
     * @return Whether the mode is valid.
     */
    public boolean setNavigationMode(String mode);

    /**
     * Get the user's location and orientation.  This will use the viewpoint
     * bound in the active layer.
     *
     * @param pos The current user position
     * @param ori The current user orientation
     */
    public void getUserPosition(Vector3f pos, AxisAngle4f ori);

    /**
     * Move the user's location to see the entire world in this layer. Change
     * the users orientation to look at the center of the world.
     *
     * @param animated Should the transistion be animated.  Defaults to FALSE.
     */
    public void fitToWorld(boolean animated);

    /**
     * Set the contents that this layer manages to be the ungrouped nodes
     * of the scene. The code should take all the children nodes from this node
     * that are not part of a layer and render them as this layer.
     *
     * @param root The root of the world to handle
     */
    public void setManagedNodes(VRMLWorldRootNodeType root);

    /**
     * Set the contents that this layer manages the specific layer instance
     * provided.
     *
     * @param layer The root of the layer to handle
     */
    public void setManagedLayer(VRMLLayerNodeType layer);

    /**
     * Override the file field of view values with a value that suits
     * the given output device. A value of 0 = no, otherwise use this
     * instead of content
     *
     * @param fov The fov in degrees.
     */
    public void setHardwareFOV(float fov);

    /**
     * Set whether stereo is enabled for all layers.
     */
    public void setStereoEnabled(boolean enabled);

    /**
     * Change the rendering style that the browser should currently be using
     * for all layers. Various options are available based on the constants
     * defined in this interface.
     *
     * @param style One of the RENDER_* constants from LayerRenderingManager
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     */
    public void setRenderingStyle(int style)
        throws IllegalArgumentException;

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants from LayerRenderingManager
     */
    public int getRenderingStyle();

    /**
     * Perform the initial bind for a new scene. This is typically called some
     * time just after the clear() method with a new scene. This will
     * automatically reset the current navigation state for this layer to be
     * inactive, even if it was previously active.
     */
    public void initialBind();

    /**
     * Get the bindable node manager for the given node type. If the node type
     * does not have a bindable manager for it, one will be created.
     *
     * @param type The type constant of the node type for the manager
     * @return The bindable manager for it
     * @see org.web3d.vrml.lang.TypeConstants
     */
    public BindableNodeManager getBindableManager(int type);

    /**
     * Check to see if this is an unmanaged size layer. A layer that has no
     * specific viewport set, or a percentage size.
     *
     * @return One of the VIEWPORT_* constants
     */
    public int getViewportType();

    /**
     * Get the Viewport node that this layer uses. If the layer does not have
     * a viewport set, then it returns null. The value is the real
     * X3DViewportNode instance stripped from any surrounding proto shells etc.
     *
     * @return The current viewport node instance used by the layer
     */
    public VRMLViewportNodeType getViewport();

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown();

    /**
     * Update the viewing matrix.  Call this when you want the LayerManager
     * to update the viewing matrix.  Typically after all user input and events
     * have resolved.
     */
    public void updateViewMatrix();

    /**
     * Force clearing all currently managed nodes from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now. Everything also includes all the
     * currently registered listener instances.
     */
    public void clear();

    /**
     * Add a listener for navigation state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addNavigationStateListener(NavigationStateListener l);

    /**
     * Remove a navigation state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeNavigationStateListener(NavigationStateListener l);

    /**
     * Add a listener for sensor state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addSensorStatusListener(SensorStatusListener l);

    /**
     * Remove a sensor state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeSensorStatusListener(SensorStatusListener l);

    /**
     * Add a listener for viewpoint status changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addViewpointStatusListener(ViewpointStatusListener l);

    /**
     * Remove a viewpoint state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeViewpointStatusListener(ViewpointStatusListener l);
}
