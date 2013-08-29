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

package xj3d.browser.renderer.java3d;

// External imports
import org.j3d.ui.navigation.*;

import java.awt.*;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import javax.media.j3d.*;
import javax.swing.JPanel;
import javax.swing.JLabel;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.input.*;
import org.xj3d.core.loading.*;
import org.xj3d.core.eventmodel.*;

import org.web3d.browser.BrowserCore;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;

import org.web3d.vrml.renderer.j3d.J3DSceneBuilderFactory;
import org.web3d.vrml.renderer.j3d.browser.OverlayHandler;
import org.web3d.vrml.renderer.j3d.browser.J3DStandardBrowserCore;
import org.web3d.vrml.renderer.j3d.input.J3DPickingManager;
import org.web3d.vrml.renderer.j3d.input.DefaultSensorManager;
//import org.web3d.vrml.renderer.j3d.input.DefaultPickingManager;

import xj3d.browser.renderer.BrowserComponent;

/**
 * A Java3D specific rendering component.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.22 $
 */
public class J3DBrowserComponent
    implements BrowserComponent, OverlayHandler {

    /** The global canvas for rendering */
    private Canvas3D canvas;
    private J3DSceneBuilderFactory builder_fac;
    private J3DPickingManager picker_manager;
    private J3DStandardBrowserCore universe;
    private EventModelEvaluator event_model;
    private View view;
    private DefaultSensorManager sensor_manager;
    private boolean stereo;
    private EyeSeparationPanel eyePanel;

    public J3DBrowserComponent(Frame parent, JLabel statusLabel, boolean stereo, int fullscreen,
        int desiredSamples, int numZBits, int numCpus) {

        this.stereo = stereo;
        // TODO: Desired samples ignored for now
        // TODO: ZBits samples ignored for now
        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(GraphicsConfigTemplate3D.REQUIRED);
        if(stereo)
            template.setStereo(GraphicsConfigTemplate3D.REQUIRED);

        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        boolean use_fullscreen = fullscreen >= 0 ? true : false;

        if(use_fullscreen && !dev.isFullScreenSupported()) {
            System.out.println("Fullscreen not supported");
            use_fullscreen = false;
        }

        GraphicsConfiguration gfxConfig = dev.getBestConfiguration(template);

        // couldn't create stereo mode?
        if(gfxConfig == null) {
            System.out.println("Unable to initialize graphics requirements");
            System.exit(1);
        }

        // If we are fullscreen mode, make the frame do that,
        // but don't put any of the normal decorations like buttons,
        // URL bars etc.
        if(use_fullscreen) {
            parent.setUndecorated(true);
            dev.setFullScreenWindow(parent);
        }

        builder_fac =
            new J3DSceneBuilderFactory(false,
                                       true,
                                       true,
                                       true,
                                       true,
                                       true,
                                       true);

        // We also need a canvas to display stuff with and a universe to set
        // the content in.
        canvas = new Canvas3D(gfxConfig);
        //System.out.println("number of texture units: " +  ((Integer)canvas.queryProperties().get("textureUnitStateMax")).intValue());
        view = new View();
        view.addCanvas3D(canvas);
        view.setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);

        picker_manager = new DefaultPickingManager();
        sensor_manager = new DefaultSensorManager();
        event_model = new DefaultEventModelEvaluator();

//        VirtualUniverse.setJ3DThreadPriority(Thread.NORM_PRIORITY - 1);
    }

    /**
     * Get the AWT component holding this browser.
     *
     * @return The component
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Get the surface which issues events.  This will need to change to
     * handle multiple canvases.
     */
    public Object getSurface() {
        // The Canvas3D is the right object to return here.
        return canvas;
    }

    /**
     * Get the renderer specific scenebuilder for creating nodes.
     *
     * @return The scene builder
     */
    public SceneBuilderFactory getSceneBuilderFactory() {
        return builder_fac;
    }

    /**
     * Get the renderer specific picking manager.
     *
     * @return The picking manager
     */
    public PickingManager getPickingManager() {
        return picker_manager;
    }

    /**
     * Get the renderer type.
     *
     * @return The BrowserCore type
     */
    public int getRendererType() {
        return BrowserCore.JAVA3D_RENDERER;
    }

    /**
     * Get the core browser implementation.
     *
     * @return the BrowserCore
     */
    public BrowserCore getBrowserCore() {
        if (universe != null)
            return universe;


        universe = new J3DStandardBrowserCore(event_model, true, this);
        universe.setPrimaryView(view);

        if(stereo) {
            PhysicalBody avatar = new PhysicalBody();
            eyePanel = new EyeSeparationPanel(avatar,universe);
            universe.setPhysicalBody(avatar);
        }

        return universe;
    }

    /**
     * Get the event model.  Not really renderer specific
     * but universe constructors require this.
     */
    public EventModelEvaluator getEventModel() {
        return event_model;
    }

    /**
     * Get the render specific sensor manager.
     *
     * @return The sensor manager.
     */
    public SensorManager getSensorManager() {
        return sensor_manager;
    }

    /**
     * Set the rendering mode.  This allows selection between filled, lines and
     * point mode.
     *
     * @param mode The mode to render in
     */
    public void setPolygonMode(int mode) {
        System.out.println("PolygonMode not implemented in Java3D");
    }

    public void setElumensMode(JPanel p1, boolean enabled) {
        // ignored
    }

    /**
     * Close down the application safely by destroying all the resources
     * currently in use.
     */
    public void shutdown()
    {
        // TODO: What should this do?
    }


    /**
     * Cycle through antialiasing options.
     *
     * @param p1 The panel the surface is in.
     */
    public void cycleAntiaAliasing(JPanel p1) {
        System.out.println("AntiaAliasing not supported in Java3D");
    }

    //----------------------------------------------------------
    // Methods required by the OverlayHandler interface.
    //----------------------------------------------------------

    /**
     * Fetch the canvas that will be responsible for having the overlays
     * composited on them.
     *
     * @return The canvas instance to use
     */
    public Canvas3D getPrimaryCanvas() {
        return canvas;
    }

    /**
     * Get the scene containing this universe.
     *
     * @return the aviatrix3d scene.
     */
    public Object getRendererScene() {
        // TODO: Can we generalise this concept?  VirtualUniverse?
        return null;
    }

    /**
     * Set the number of channels to render to.  Not all
     * BrowserComponents will be affected by this call.
     *
     * @param channels The nunber of chanels.
     */
    public void setNumberOfChannels(int channels) {
        //ignore
    }

    /**
     * Set the minimum frame interval time to limit the CPU resources
     * taken up by the 3D renderer.  By default it will use all of them.
     *
     * @param The minimum time in milleseconds.  0 means no limit.
     */
    public void setMinimumFrameInterval(int millis) {
        view.setMinimumFrameCycleTime(millis);
    }

    /**
     * Tell render to start or stop rendering. If currently running, it
     * will wait until all the pipelines have completed their current cycle
     * and will then halt.
     *
     * @param state True if to enable rendering
     */
    public void setEnabled(boolean state) {
        if (state)
            canvas.startRenderer();
        else
            canvas.stopRenderer();
    }

    /**
     * Get the eye seperation panel.
     *
     * @return The panel.
     */
    public Component getConfigurationComponent() {
        return eyePanel;
    }

    /**
     * Get the current user position.
     *
     * @param pos The position
     */
    public void getUserLocation(float[] pos) {
        System.out.println("getUserLocation not implemented");
    }

    /**
     * Print the contents of the scenegraph to the console.
     *
     */
    public void printScene() {
        System.out.println("Print scene not implemented");
    }

}
