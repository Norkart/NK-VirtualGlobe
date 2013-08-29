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

package xj3d.browser.renderer.ogl;

// External imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.*;
import org.j3d.aviatrix3d.surface.ElumensAWTSurface;
import org.j3d.aviatrix3d.audio.OpenALAudioDevice;
import org.j3d.ui.navigation.*;
import org.j3d.device.output.elumens.SPI;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import java.io.*;

import net.java.games.jogl.*;
import javax.swing.JPanel;

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

import org.web3d.vrml.renderer.ogl.OGLSceneBuilderFactory;
//import org.web3d.vrml.renderer.j3d.browser.OverlayHandler;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.input.DefaultPickingManager;
import org.web3d.vrml.renderer.ogl.input.DefaultSensorManager;

import xj3d.browser.renderer.BrowserComponent;


/**
 * An Aviatrix3d specific rendering component.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.18 $
 */
public class ElumensBrowserComponent
    implements BrowserComponent, ComponentListener,
       RenderEffectsProcessor {

    /** Name of the property file that defines everything we need */
    private static final String PROPERTY_FILE = "elumens.properties";

    /** The global canvas for rendering */
    private Canvas canvas;

    /** Our drawing surface */
    private ElumensAWTSurface surface;

    /** The audio device */
    private AudioDevice adevice;

    private OGLSceneBuilderFactory builder_fac;
    private PickingManager picker_manager;
    private OGLStandardBrowserCore universe;
    private EventModelEvaluator event_model;
    /** Manager for the scene graph handling */

    private SingleThreadRenderManager sceneManager;
    private DefaultSensorManager sensor_manager;

    private DefaultRenderPipeline pipeline;
    private DefaultAudioPipeline audioPipeline;

    /** The polygon mode to display in */
    private int polygonMode = PolygonAttributes.DRAW_FILLED;

    /** Spi properties, null if not set from properties file */
    private static float[] eyePos;
    private static float[] lensPos;
    private static double[] screenOrientation;
    private static int[] chanSize;

    /** The glCapabilities choosen */
    private GLCapabilities caps;

    /**
     * Public constructor.  All Browser Component constructors must
     * use the same paramater list.
     */
    public ElumensBrowserComponent(Frame parent, boolean stereo, int fullscreen,
        int desiredSamples, int numCpus) {

        // Assemble a simple single-threaded pipeline.
        caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        if (desiredSamples > 1) {
            caps.setSampleBuffers(true);
            caps.setNumSamples(getMaximumNumSamples());
        }

//        CullStage culler = new NullCullStage();
        CullStage culler = new SimpleFrustumCullStage();
//        SortStage sorter = new NullSortStage();
//        SortStage sorter = new DepthSortedTransparencyStage();
//        SortStage sorter = new StateSortStage();
        SortStage sorter = new StateAndTransparencyDepthSortStage();

        surface = new ElumensAWTSurface(caps,3);
        pipeline = new DefaultRenderPipeline();

        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setDrawableSurface(surface);

        adevice = new OpenALAudioDevice();

        AudioCullStage aculler = new NullAudioCullStage();
        AudioSortStage asorter = new NullAudioSortStage();

        audioPipeline = new DefaultAudioPipeline();
        audioPipeline.setCuller(aculler);
        audioPipeline.setSorter(asorter);
        audioPipeline.setAudioDevice(adevice);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.addPipeline(pipeline);
        sceneManager.setDrawableSurface(surface);
        sceneManager.addAudioPipeline(audioPipeline);
        sceneManager.setAudioDevice(adevice);

        boolean use_fullscreen = fullscreen >= 0 ? true : false;

        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev;

        if (fullscreen >= 0) {
            GraphicsDevice[] gs = env.getScreenDevices();
            if (gs.length < fullscreen + 1) {
                System.out.println("Invalid fullscreen device.  Using default");
                dev = env.getDefaultScreenDevice();
            } else {
                dev = gs[fullscreen];
            }
        } else {
            dev = env.getDefaultScreenDevice();
        }

        if(use_fullscreen && !dev.isFullScreenSupported()) {
            System.out.println("Fullscreen not supported");
            use_fullscreen = false;
        }

        // If we are fullscreen mode, make the frame do that,
        // but don't put any of the normal decorations like buttons,
        // URL bars etc.
        if(use_fullscreen) {
            parent.setUndecorated(true);
            dev.setFullScreenWindow(parent);
        }

        builder_fac =
            new OGLSceneBuilderFactory(false,
                                       true,
                                       true,
                                       true,
                                       true,
                                       true,
                                       true);

        picker_manager = new DefaultPickingManager();
        sensor_manager = new DefaultSensorManager();
        event_model = new DefaultEventModelEvaluator();

        if(stereo) {
            System.out.println("Stereo not supported in OGL");
/*
            PhysicalBody avatar = new PhysicalBody();
            EyeSeparationPanel eyePanel = new EyeSeparationPanel(avatar);
            universe.setPhysicalBody(avatar);
*/
        }

        canvas = (Canvas)surface.getSurfaceObject();
        canvas.addComponentListener(this);

        loadProperties();
    }

    /**
     * Get the AWT component holding this browser.
     *
     * @return The component
     */
    public Canvas getCanvas() {
        return (Canvas)surface.getSurfaceObject();
    }

    /**
     * Get the surface which issues events.  This will need to change to
     * handle multiple canvases.
     */
    public Object getSurface() {
        return surface;
    }

    /**
     * Get the scene containing this universe.
     *
     * @return the aviatrix3d scene.
     */
    public Object getRendererScene() {
        return universe.getRendererScene();
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
        return BrowserCore.OPENGL_RENDERER;
    }

    /**
     * Get the core browser implementation.
     *
     * @return the BrowserCore
     */
    public BrowserCore getBrowserCore() {
        if (universe != null)
            return universe;


        universe = new OGLStandardBrowserCore(event_model, sceneManager);
        universe.setHardwareFOV(180);

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
     * Set the number of channels to render to.  Not all
     * BrowserComponents will be affected by this call.
     *
     * @param channels The nunber of chanels.
     */
    public void setNumberOfChannels(int channels) {
        surface.setNumberOfChannels(channels);
    }

    /**
     * Set the minimum frame interval time to limit the CPU resources
     * taken up by the 3D renderer.  By default it will use all of them.
     *
     * @param The minimum time in milleseconds.  0 means no limit.
     */
    public void setMinimumFrameInterval(int millis) {
        sceneManager.setMinimumFrameInterval(millis);
    }

    /**
     * Tell render to start or stop rendering. If currently running, it
     * will wait until all the pipelines have completed their current cycle
     * and will then halt.
     *
     * @param state True if to enable rendering
     */
    public void setEnabled(boolean state) {
        //sceneManager.setEnabled(state);
        if (state == false) {
            // For now, just crank the renderer back a lot
            setMinimumFrameInterval(250);
        } else {
            setMinimumFrameInterval(0);
        }

    }

    /**
     * Close down the application safely by destroying all the resources
     * currently in use.
     */
    public void shutdown()
    {
        // TODO: Audio shutdown needs to be first for some reason
        adevice.dispose();
        sceneManager.setEnabled(false);

        surface.dispose();

        pipeline.halt();
        audioPipeline.halt();
    }

    /**
     * Set the rendering mode.  This allows selection between filled, lines and
     * point mode.
     *
     * @param mode The mode to render in
     */
    public void setPolygonMode(int mode) {
        switch(mode) {
            case MODE_FILLED:
                polygonMode = PolygonAttributes.DRAW_FILLED;
                break;
            case MODE_LINES:
                polygonMode = PolygonAttributes.DRAW_LINE;
                break;
            case MODE_POINTS:
                polygonMode = PolygonAttributes.DRAW_POINT;
                break;
            default:
                System.out.println("Unknown polygon mode");
        }

        Scene scene = (Scene) universe.getRendererScene();
        scene.setRenderEffectsProcessor(this);
    }

    public void setElumensMode(JPanel p1, boolean enabled) {
        surface = new ElumensAWTSurface(caps,3);
        pipeline.setDrawableSurface(surface);
    }

    //----------------------------------------------------------
    // Methods required by the ComponentListener interface.
    //----------------------------------------------------------

    public void componentHidden(ComponentEvent evt) {
    }

    public void componentMoved(ComponentEvent evt) {
    }

    public void componentResized(ComponentEvent evt) {
        Dimension size = canvas.getSize();

        int width;
        int height;

        width = (int) size.getWidth();
        height = (int) size.getHeight();

        if (width > 0 && height > 0)
            universe.setViewport(new Rectangle(0,0,width,height));
    }

    public void componentShown(ComponentEvent evt) {
    }

    //----------------------------------------------------------
    // Methods required by the RenderEffects interface.
    //----------------------------------------------------------

    /**
     * Perform any pre-rendering setup that you may need for this scene. After
     * this call, all normal scene graph rendering is performed by the surface.
     *
     * @param drawable The output surface that is being drawn to
     * @param userData Some identifiable data provided by the user
     */
    public void preDraw(GLDrawable drawable, Object userData) {
        GL gl = drawable.getGL();
        gl.glPolygonMode(GL.GL_FRONT, polygonMode);
        gl.glPolygonMode(GL.GL_BACK, polygonMode);
    }

    /**
     * Perform any post-rendering actions that you may need for this scene.
     * Called after the renderer has completed all drawing and just before the
     * buffer swap. The only thing to be called after calling this method is
     * glFlush().
     *
     * @param drawable The output surface that is being drawn to
     * @param userData Some identifiable data provided by the user
     */
    public void postDraw(GLDrawable drawable, Object userData) {
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Load the elumens.properties file.
     */
    public void loadProperties() {
        String user_dir = System.getProperty("user.dir");
        InputStream is;
        String file = user_dir + File.separator + PROPERTY_FILE;
System.out.println("Loading properties: " + file);

        try {
            is = new FileInputStream(file);
        } catch(FileNotFoundException fnfe) {
            // Fallback to default
            is = (InputStream) ClassLoader.getSystemClassLoader().getResourceAsStream(PROPERTY_FILE);
        }

        if (is == null) {
            System.out.println("No elumens properties loaded");
        } else {

            Properties props = new Properties();
            try {
                props.load(is);
                is.close();
            } catch(IOException ioe) {
                System.out.println("Error reading elumens.properties");
            }

            StringTokenizer st;
            int fnum;
            String str = props.getProperty("EyePosition");

            if (str != null) {
                eyePos = new float[3];

                try {
                    fnum=0;
                    st = new StringTokenizer(str);
                    while (st.hasMoreTokens()) {
                        eyePos[fnum++] = Float.parseFloat(st.nextToken());
                    }
                } catch(NumberFormatException pe) {
                    pe.printStackTrace();
                }
            }

            str = props.getProperty("LensPosition");

            if (str != null) {
                lensPos = new float[3];

                try {
                    fnum=0;
                    st = new StringTokenizer(str);
                    while (st.hasMoreTokens()) {
                        lensPos[fnum++] = Float.parseFloat(st.nextToken());
                    }
                } catch(NumberFormatException pe) {
                    pe.printStackTrace();
                }
            }

            str = props.getProperty("ScreenOrientation");

            if (str != null) {
                screenOrientation = new double[3];

                try {
                    fnum=0;
                    st = new StringTokenizer(str);
                    while (st.hasMoreTokens()) {
                        screenOrientation[fnum++] = Double.parseDouble(st.nextToken());
                    }
                } catch(NumberFormatException pe) {
                    pe.printStackTrace();
                }
            }

            str = props.getProperty("ChanSize");

            if (str != null) {
                chanSize = new int[2];

                try {
                    fnum=0;
                    st = new StringTokenizer(str);
                    while (st.hasMoreTokens()) {
                        chanSize[fnum++] = Integer.parseInt(st.nextToken());
                    }
                } catch(NumberFormatException pe) {
                    pe.printStackTrace();
                }
            }
        }

        if (lensPos != null) {
            surface.setChanLensPosition(SPI.SPI_ALL_3_CHAN,lensPos[0],lensPos[1],lensPos[2]);
        }
        if (eyePos != null) {
            surface.setChanEyePosition(SPI.SPI_ALL_3_CHAN,eyePos[0],eyePos[1],eyePos[2]);
        }
        if (screenOrientation != null) {
            surface.setScreenOrientation(screenOrientation[0],
                screenOrientation[1],screenOrientation[2]);
        }
        if (chanSize != null) {
            surface.setChanSize(SPI.SPI_ALL_3_CHAN,chanSize[0],chanSize[1]);
        }
    }

    /** Max samples field, modified by MultiSampleChooser. */
    private static int maxSamples = -1;

    /**
     * Get the maximum number of samples we can use.
     */
    private int getMaximumNumSamples() {
        GLCapabilities caps = new GLCapabilities();
        GLCapabilitiesChooser chooser = new MultisampleChooser();
        caps.setSampleBuffers(true);

        canvas = GLDrawableFactory.getFactory().createGLCanvas(caps, chooser);
        Frame frame = new Frame();
        canvas.setSize(16, 16);
        frame.add(canvas, BorderLayout.CENTER);
        frame.pack();
        frame.show();

        while(maxSamples < 0) {
            try {
                Thread.sleep(50);
            } catch(Exception e) {}
        }

        frame.hide();
        frame.dispose();

        return maxSamples;
    }


    /**
     * Static class to find the number of samples available.  Returns
     * value by setting maxSamples field.
     */
    static class MultisampleChooser extends DefaultGLCapabilitiesChooser {
        public int chooseCapabilities(GLCapabilities desired,
            GLCapabilities[] available,
            int windowSystemRecommendedChoice) {

                boolean anyHaveSampleBuffers = false;
                for (int i = 0; i < available.length; i++) {
                    GLCapabilities caps = available[i];
                    if (caps != null) {
                        if (caps.getNumSamples() > maxSamples)
                            maxSamples = caps.getNumSamples();
                        if (caps.getSampleBuffers())
                            anyHaveSampleBuffers = true;
                    }
                }
                int selection = super.chooseCapabilities(desired, available, windowSystemRecommendedChoice);
                if (!anyHaveSampleBuffers) {
                    System.err.println("WARNING: antialiasing will be disabled because none of the available pixel formats had it to offer");
                } else {
                if (!available[selection].getSampleBuffers()) {
                    System.err.println("WARNING: antialiasing will be disabled because the DefaultGLCapabilitiesChooser didn't supply it");
                }
            }
            return selection;
        }
    }
}
