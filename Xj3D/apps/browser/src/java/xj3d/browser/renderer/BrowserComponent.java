/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser.renderer;

// External imports
import java.awt.Canvas;
import java.awt.Component;
import javax.swing.JPanel;

// Local imports
import org.web3d.browser.BrowserCore;

import org.xj3d.core.eventmodel.PickingManager;
import org.xj3d.core.eventmodel.EventModelEvaluator;
import org.xj3d.core.eventmodel.SensorManager;
import org.xj3d.core.loading.SceneBuilderFactory;

/**
 * A specific rendering component.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.14 $
 */
public interface BrowserComponent {
    public static final int MODE_FILLED = 0;
    public static final int MODE_LINES = 1;
    public static final int MODE_POINTS = 2;

    /**
     * Get the AWT component holding this browser.
     *
     * @return The component
     */
    public Canvas getCanvas();

    /**
     * Get the surface which issues events.  This will need to change to
     * handle multiple canvases.
     */
    public Object getSurface();

    /**
     * Get the scene containing this universe.
     *
     * @return the aviatrix3d scene.
     */
    public Object getRendererScene();

    /**
     * Get the renderer specific scenebuilder for creating nodes.
     *
     * @return The scene builder
     */
    public SceneBuilderFactory getSceneBuilderFactory();

    /**
     * Get the renderer specific picking manager.
     *
     * @return The picking manager
     */
    public PickingManager getPickingManager();

    /**
     * Get the renderer type.
     *
     * @return The BrowserCore type
     */
    public int getRendererType();

    /**
     * Get the core browser implementation.
     *
     * @return the BrowserCore
     */
    public BrowserCore getBrowserCore();

    /**
     * Get the event model.  Not really renderer specific
     * but universe constructors require this.
     */
    public EventModelEvaluator getEventModel();

    /**
     * Get the render specific sensor manager.
     *
     * @return The sensor manager.
     */
    public SensorManager getSensorManager();

    /**
     * Set the number of channels to render to.  Not all
     * BrowserComponents will be affected by this call.
     *
     * @param channels The nunber of chanels.
     */
    public void setNumberOfChannels(int channels);

    /**
     * Set the minimum frame interval time to limit the CPU resources
     * taken up by the 3D renderer.  By default it will use all of them.
     *
     * @param The minimum time in milleseconds.
     */
    public void setMinimumFrameInterval(int millis);

    /**
     * Tell render to start or stop rendering. If currently running, it
     * will wait until all the pipelines have completed their current cycle
     * and will then halt.
     *
     * @param state True if to enable rendering
     */
    public void setEnabled(boolean state);

    /**
     * Set the rendering mode.  This allows selection between filled, lines and
     * point mode.
     *
     * @param mode The mode to render in
     */
    public void setPolygonMode(int mode);

    /**
     * Shutdown this component.
     */
    public void shutdown();

    public void setElumensMode(JPanel p1, boolean enabled);

    /**
     * Cycle through antialiasing options.
     *
     * @param p1 The panel the surface is in.
     */
    public void cycleAntiaAliasing(JPanel p1);

    /**
     * Get the configuration panel.
     *
     * @return The panel.
     */
    public Component getConfigurationComponent();

    /**
     * Get the current user position.
     *
     * @param pos The position
     */
    public void getUserLocation(float[] pos);

    /**
     * Print the contents of the scenegraph to the console.
     */
    public void printScene();
}
