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
import org.j3d.aviatrix3d.surface.*;
import org.j3d.aviatrix3d.audio.OpenALAudioDevice;
import org.j3d.ui.navigation.*;
import org.j3d.device.output.elumens.SPI;
import org.j3d.renderer.aviatrix3d.util.SceneGraphTraverser;
import org.j3d.renderer.aviatrix3d.util.SceneGraphTraversalObserver;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.JLabel;

import java.util.*;

import java.io.*;

import net.java.games.jogl.*;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;
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
 * @version $Revision: 1.30 $
 */
public class OGLBrowserComponent
    implements BrowserComponent, ComponentListener,
       RenderEffectsProcessor, SceneGraphTraversalObserver {

    /** Name of the property file that defines everything we need */
    private static final String PROPERTY_FILE = "elumens.properties";

    // Should bounds be printed in debug print
    private static final boolean PRINT_BOUNDS = true;

    /** The global canvas for rendering */
    private Canvas canvas;

    /** Our drawing surface */
    private DrawableSurface surface;

    /** The audio device */
    private AudioDevice adevice;

    private OGLSceneBuilderFactory builder_fac;
    private PickingManager picker_manager;
    private OGLStandardBrowserCore universe;
    private EventModelEvaluator event_model;
    /** Manager for the scene graph handling */

    private RenderPipelineManager sceneManager;
    private DefaultSensorManager sensor_manager;

    private DefaultRenderPipeline pipeline;
    private DefaultAudioPipeline audioPipeline;

    /** The polygon mode to display in */
    private int polygonMode;

    /** Render in Stereo */
    private boolean stereo;

    /** The glCapabilities choosen */
    private GLCapabilities caps;

    /** Are we in Elumens Mode */
    private boolean elumensMode;

    /** Spi properties, null if not set from properties file */
    private static float[] eyePos;
    private static float[] lensPos;
    private static double[] screenOrientation;
    private static int[] chanSize;

    /** Number of antialiasing samples */
    private int numSamples;

    /** Max samples field, modified by MultiSampleChooser. */
    private static int maxSamples = -1;

    /** The status label */
    private JLabel statusLabel;

    private boolean initialSurfaceUsed = false;

    /**
     * Public constructor.  All Browser Component constructors must
     * use the same paramater list.
     */
    public OGLBrowserComponent(Frame parent, JLabel statusLabel, boolean stereo, int fullscreen,
        int desiredSamples, int numZBits, int numCpus) {

        polygonMode = PolygonAttributes.DRAW_FILLED;
        this.stereo = stereo;
        maxSamples = -1;
        numSamples = 1;
        this.statusLabel = statusLabel;

        // Assemble a simple single-threaded pipeline.
        caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        if (desiredSamples > 1) {
            caps.setSampleBuffers(true);
            caps.setNumSamples(desiredSamples);
        }

        if (numZBits > 0) {
            System.out.println("Requesting nondefault zbuffer: " + numZBits);
            caps.setDepthBits(numZBits);
        }

//System.out.println("Using NULL cull stage");
//        CullStage culler = new NullCullStage();
//        CullStage culler = new DebugFrustumCullStage();
        CullStage culler = new SimpleFrustumCullStage();
//        CullStage culler = new GenericCullStage();
        culler.setOffscreenCheckEnabled(true);

//        SortStage sorter = new NullSortStage();
//        SortStage sorter = new DepthSortedTransparencyStage();
//        SortStage sorter = new StateSortStage();
          SortStage sorter = new StateAndTransparencyDepthSortStage();

        if (stereo) {
            surface = new StereoAWTSurface(caps);
            surface.setStereoRenderingPolicy(DrawableSurface.ALTERNATE_FRAME_STEREO);
        } else {
            surface = new SimpleAWTSurface(caps);

//System.out.println("***Using Debug Surface");
//          surface = new DebugAWTSurface(caps);
        }

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

        if (numCpus == 1) {
            sceneManager = new SingleThreadRenderManager();

            sceneManager.addPipeline(pipeline);
            ((SingleThreadRenderManager)sceneManager).setDrawableSurface(surface);
            sceneManager.addAudioPipeline(audioPipeline);
            ((SingleThreadRenderManager)sceneManager).setAudioDevice(adevice);
        } else {
            sceneManager = new MultiThreadRenderManager();
            pipeline.setDrawableSurface(surface);
            sceneManager.addPipeline(pipeline);
            audioPipeline.setAudioDevice(adevice);
            sceneManager.addAudioPipeline(audioPipeline);
        }

        sceneManager.disableInternalShutdown();

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
    }

    /**
     * Get the AWT component holding this browser.
     *
     * @return The component
     */
    public Canvas getCanvas() {
        initialSurfaceUsed = true;
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

        if (elumensMode)
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
        if (elumensMode)
            ((ElumensAWTSurface)surface).setNumberOfChannels(channels);
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
     * Get the eye seperation panel.
     *
     * @return The panel.
     */
    public Component getConfigurationComponent() {
        //return eyePanel;
        return null;
    }

    /**
     * Get the current user position.
     *
     * @param pos The position
     */
    public void getUserLocation(float[] pos) {
        universe.getUserLocation(pos);
    }

    /**
     * Print the contents of the scenegraph to the console.
     *
     */
    public void printScene() {
        Scene scene = sceneManager.getScene();
        Group root = scene.getRenderedGeometry();

        SceneGraphTraverser sgt = new SceneGraphTraverser();
        sgt.setObserver(this);
        sgt.traverseGraph(root);
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

        sceneManager.requestFullSceneRender();
    }

    /**
     * Cycle through antialiasing options.
     *
     * @param p1 The panel the surface is in.
     */
    public void cycleAntiaAliasing(JPanel p1) {
        if (maxSamples < 0)
            maxSamples = getMaximumNumSamples();

        numSamples = numSamples * 2;
        if (numSamples > maxSamples)
            numSamples = 1;

        statusLabel.setText("Antialiasing samples: " + numSamples + " out of max: " + maxSamples);

        caps.setSampleBuffers(true);
        caps.setNumSamples(numSamples);

        resetSurface(p1);
    }

    public void setElumensMode(JPanel p1, boolean enabled) {
        elumensMode = enabled;

        if (!initialSurfaceUsed) {
            surface = new ElumensAWTSurface(caps,3);
            pipeline.setDrawableSurface(surface);
            ((SingleThreadRenderManager)sceneManager).setDrawableSurface(surface);

            //universe.setHardwareFOV(180);
            loadProperties();

        } else
            resetSurface(p1);
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


    //---------------------------------------------------------------
    // Methods defined by SceneGraphTraversalObserver
    //---------------------------------------------------------------

    /**
     * Notification of a scene graph object that has been traversed in the
     * scene.
     *
     * @param parent The parent node of this node
     * @param child The child node that is being observed
     * @param shared true if the object reference has already been traversed
     *    and this is beyond the first reference
     * @param depth The depth of traversal from the top of the tree.  Starts at 0 for top.
     */
    public void observedNode(SceneGraphObject parent,
                             SceneGraphObject child,
                             boolean shared,
                             int depth)
    {
        for(int i=0; i < depth; i++) {
            System.out.print("  ");
        }

        if (parent == null) {
            System.out.println("ROOT");
            return;
        } else {
            System.out.print(parent.getClass().getName());

            if (PRINT_BOUNDS) {
                if (parent instanceof Node) {
                    Node node = (Node) parent;
                    BoundingVolume bounds = node.getBounds();

                    if (bounds instanceof BoundingVoid) {
                        if (parent instanceof Group) {
                            Group grp = (Group) parent;
                            if (grp.numChildren() == 0) {
                                System.out.println(" Children: EMPTY");
                            } else {
                                System.out.print(" Bounds: VOID ");
                                System.out.println(" childCount: " + grp.numChildren());
                            }
                        }

                    } else {
                        System.out.println(" Bounds: " + bounds);
                    }
                } else {
                    System.out.println();
                }
            } else {
                System.out.println();
            }
        }

        for(int i=0; i < depth+1; i++) {
            System.out.print("  ");
        }

        if (child == null) {
            System.out.println("NULL child?  Parent: " + parent);
        } else {
            System.out.print(child.getClass().getName());

            if (PRINT_BOUNDS && child instanceof Node) {
                Node node = (Node) child;
                BoundingVolume bounds = node.getBounds();

                if (bounds instanceof BoundingVoid) {
                    if (child instanceof Group) {
                        Group grp = (Group) child;
                        if (grp.numChildren() == 0) {
                            System.out.print(" Children: EMPTY");
                        } else {
                            System.out.print(" Bounds: VOID ");
                            System.out.print(" childCount: " + grp.numChildren());
                        }
                    } else {
                        System.out.print(" Bounds: VOID");
                    }
                } else {
                    System.out.print(" Bounds: " + bounds);
                }
            }
            if(shared)
                System.out.println(" (copy) ");
            else
                System.out.println();
        }

    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the maximum number of samples we can use.
     */
    private int getMaximumNumSamples() {
System.out.println("***Getting maximum samples");
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
     * Load the elumens.properties file.
     */
    public void loadProperties() {
        String user_dir = System.getProperty("user.dir");
        InputStream is;
        String file = user_dir + File.separator + PROPERTY_FILE;

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
            ((ElumensAWTSurface)surface).setChanLensPosition(SPI.SPI_ALL_3_CHAN,lensPos[0],lensPos[1],lensPos[2]);
        }
        if (eyePos != null) {
            ((ElumensAWTSurface)surface).setChanEyePosition(SPI.SPI_ALL_3_CHAN,eyePos[0],eyePos[1],eyePos[2]);
        }
        if (screenOrientation != null) {
            ((ElumensAWTSurface)surface).setScreenOrientation(screenOrientation[0],
                screenOrientation[1],screenOrientation[2]);
        }
        if (chanSize != null) {
            ((ElumensAWTSurface)surface).setChanSize(SPI.SPI_ALL_3_CHAN,chanSize[0],chanSize[1]);
        }
    }

    /**
     * Reset the surface with a new mode or parameters.
     *
     * @param The panel the surface is in.
     */
    private void resetSurface(JPanel p1) {
        if (elumensMode) {
            canvas.removeComponentListener(this);

            surface = new ElumensAWTSurface(caps,3);
            pipeline.setDrawableSurface(surface);
            ((SingleThreadRenderManager)sceneManager).setDrawableSurface(surface);
            canvas = (Canvas)surface.getSurfaceObject();
            canvas.addComponentListener(this);
            p1.add(canvas, BorderLayout.CENTER);

            universe.setHardwareFOV(180);
            loadProperties();
        } else {
            canvas.removeComponentListener(this);

            if (stereo) {
                surface = new StereoAWTSurface(caps);
                surface.setStereoRenderingPolicy(DrawableSurface.ALTERNATE_FRAME_STEREO);
            } else {
                surface = new SimpleAWTSurface(caps);
            }
            pipeline.setDrawableSurface(surface);
            ((SingleThreadRenderManager)sceneManager).setDrawableSurface(surface);
            canvas = (Canvas)surface.getSurfaceObject();
            canvas.addComponentListener(this);

            p1.add(canvas, BorderLayout.CENTER);

            universe.setHardwareFOV(0);
        }
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

