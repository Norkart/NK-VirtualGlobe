/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import javax.media.opengl.GLCapabilities;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.pipeline.graphics.SimpleFrustumCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.StateAndTransparencyDepthSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.rendering.BoundingVolume;

import org.j3d.renderer.aviatrix3d.loader.AVModel;
import org.j3d.renderer.aviatrix3d.loader.AVLoader;
import org.j3d.renderer.aviatrix3d.loader.AVRuntimeComponent;
import org.j3d.renderer.aviatrix3d.texture.TextureCreateUtils;

import org.xj3d.loaders.ogl.Web3DLoader;

/**
 * Example application that demonstrates how to use the Aviatrix3D loader
 * interface to load a file into the scene graph.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public class AV3DLoaderDemo extends Frame
    implements WindowListener, ApplicationUpdateObserver
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** Utility for munging textures to power of 2 size */
    private TextureCreateUtils textureUtils;

    /** The runtime components */
    private AVRuntimeComponent[] runtimeComponents;

    /** A Transform for rotations if added */
    TransformGroup rot_group;

    public AV3DLoaderDemo()
    {
        super("Basic Aviatrix Loader Demo");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();

        setSize(600, 600);
        setLocation(40, 40);

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);

        textureUtils = new TextureCreateUtils();
    }

    /**
     * Setup the avaiatrix pipeline here
     */
    private void setupAviatrix()
    {
        // Assemble a simple single-threaded pipeline.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        GraphicsCullStage culler = new SimpleFrustumCullStage();
        culler.setOffscreenCheckEnabled(false);

        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();
        surface = new SimpleAWTSurface(caps);
        surface.setClearColor(0.2f, 0.2f, 0.2f, 1);
        DefaultGraphicsPipeline pipeline = new DefaultGraphicsPipeline();

        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.addPipeline(pipeline);
        sceneManager.setGraphicsOutputDevice(surface);
        sceneManager.setMinimumFrameInterval(30);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component comp = (Component)surface.getSurfaceObject();
        add(comp, BorderLayout.CENTER);
    }

    /**
     * Load the requested file and add it to the scene graph.
     *
     * @param filename Name of the file to load
     */
    private void load(String filename)
    {
        File tex_file = null;
        try
        {
            File file = new File(filename);
            Web3DLoader loader = new Web3DLoader();
            boolean loadAll = true;

            if (loadAll)
                loader.setLoadFlags(Web3DLoader.LOAD_ALL);
            else
                loader.setLoadFlags(Web3DLoader.GEOMETRY);

            AVModel model = loader.load(file);

            SimpleScene scene = setupSceneGraph(model.getModelRoot());

            // Now go off and load textures.
            Map externals = model.getExternallyDefinedFiles();

            // Xj3D loads textures for you if in LOAD_ALL mode
            if(!loadAll && externals.size() != 0)
            {
                File parent_dir = file.getParentFile();

                Set entries = externals.entrySet();
                Iterator itr = entries.iterator();

                while(itr.hasNext())
                {
                    Map.Entry e = (Map.Entry)itr.next();

                    String tex_str = null;
                    Object obj = e.getValue();

                    if((obj instanceof String))
                    {
                        tex_str = (String)obj;
                    }
                    else if(obj instanceof String[])
                    {
                        String[] sobj = (String[])obj;

                        // Only handle one url right now
                        if(sobj.length > 0)
                            tex_str = sobj[0];
                    }

                    if(tex_str == null)
                        continue;

                    // Assume a texture object right now as that's all we
                    // care about in .3ds files.
                    Texture2D texture = (Texture2D)e.getKey();

                    if (tex_str.startsWith("file:///")) {
                        tex_str = tex_str.substring(8);
                    }

                    tex_file = new File(parent_dir, tex_str);

                    TextureComponent2D[] tex_comp = { loadImage(tex_file) };

                    if(tex_comp[0] == null)
                        continue;

                    int format = Texture.FORMAT_RGB;

                    switch(tex_comp[0].getFormat(0))
                    {
                        case TextureComponent.FORMAT_RGBA:
                            format = Texture.FORMAT_RGBA;
                            break;

                        case TextureComponent.FORMAT_INTENSITY_ALPHA:
                            format = Texture.FORMAT_INTENSITY_ALPHA;
                            break;

                        case TextureComponent.FORMAT_SINGLE_COMPONENT:
                            format = Texture.FORMAT_INTENSITY;
                            break;

                    }

                    texture.setSources(Texture.MODE_BASE_LEVEL,
                                      format,
                                      tex_comp,
                                      1);
                }
            }

            // Then the basic layer and viewport at the top:
            SimpleViewport view = new SimpleViewport();
            view.setDimensions(0, 0, 600, 600);
            view.setScene(scene);

            SimpleLayer layer = new SimpleLayer();
            layer.setViewport(view);

            Layer[] layers = { layer };
            sceneManager.setLayers(layers, 1);
            sceneManager.setEnabled(true);

            List rtComps = model.getRuntimeComponents();

            int size = rtComps.size();

            if (size > 0) {
                runtimeComponents = new AVRuntimeComponent[rtComps.size()];
                runtimeComponents = (AVRuntimeComponent[])
                    rtComps.toArray((AVRuntimeComponent[])runtimeComponents);

                sceneManager.setApplicationObserver(this);
            }
        }
        catch(IOException ioe)
        {
            System.out.println("IO Error reading file: " + tex_file);
            System.out.println(ioe.getMessage());
        }
    }


    /**
     * Setup the basic scene which is a viewpoint along with the model that
     * was loaded.
     */
    private SimpleScene setupSceneGraph(Group loadedScene)
    {
        // View group

        Viewpoint vp = new Viewpoint();
        vp.setHeadlightEnabled(true);

        Vector3f trans = new Vector3f(0, 0, 3);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Create our loader now and read in the given file
        loadedScene.requestBoundsUpdate();

        BoundingVolume bd = loadedScene.getBounds();

        TransformGroup tg = new TransformGroup();

        if (bd instanceof BoundingVoid)
        {
            System.out.println("Got bounding void for bounds?");
            tg.addChild(loadedScene);
        }
        else
        {
            // Scale the scene so that it fits in a 1 unit box in front of the
            // camera.
            float[] center = new float[3];
            float[] min_ext = new float[3];
            float[] max_ext = new float[3];

            bd.getCenter(center);
            bd.getExtents(min_ext, max_ext);

            float max = max_ext[0] - min_ext[0];
            if(Math.abs(max_ext[1] - min_ext[1]) > max)
                max = Math.abs(max_ext[1] - min_ext[1]);
            if(Math.abs(max_ext[2] - min_ext[2]) > max)
                max = Math.abs(max_ext[2] - min_ext[2]);

            if (max == 0)
                max = 1.0f;

            trans.set(center[0] / max, center[1] / max, center[2] / max);

            System.out.println("Original model bounds " + bd);
            System.out.println("Scaling by  " + (1 / max));

            mat.setIdentity();
            mat.setScale(1 / max);
            mat.setTranslation(trans);

            tg.setTransform(mat);

            tg.addChild(loadedScene);
        }

        // A separate TG to rotate the model with as the setRot method
        // on the matrix trashes the rest of the matrix including scale and
        // translation.
        rot_group = new TransformGroup();
        rot_group.addChild(tg);

        scene_root.addChild(rot_group);

        // Add some lights to help illuminate the model
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(0, -1, -1);
        dl.setEnabled(true);
        dl.setAmbientColor(new float[] { 0.5f, 0.5f, 0.5f });
        dl.setDiffuseColor(new float[] { 0.5f, 0.5f, 0.5f });
        scene_root.addChild(dl);


        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        return scene;
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    /**
     * Ignored
     */
    public void windowActivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowClosed(WindowEvent evt)
    {
    }

    /**
     * Exit the application
     *
     * @param evt The event that caused this method to be called.
     */
    public void windowClosing(WindowEvent evt)
    {
        sceneManager.shutdown();
        System.exit(0);
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowDeiconified(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowIconified(WindowEvent evt)
    {
    }

    /**
     * When the window is opened, start everything up.
     */
    public void windowOpened(WindowEvent evt)
    {
//        sceneManager.setEnabled(true);
    }

    //---------------------------------------------------------------
    // ApplicationUpdateObserver methods
    //---------------------------------------------------------------

    public void appShutdown() {
    }

    public void updateSceneGraph() {
        int len = runtimeComponents.length;

        for(int i=0; i < len; i++) {
            runtimeComponents[i].executeModelBehavior();
        }
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Load a single image
     */
    private TextureComponent2D loadImage(File file)
    {
        TextureComponent2D comp = null;

        System.out.println("Loading external file: " + file);
        try
        {
            if(!file.exists())
            {
                System.out.println("Can't find texture source file");
                return null;
            }

            FileInputStream is = new FileInputStream(file);

            BufferedInputStream stream = new BufferedInputStream(is);
            BufferedImage img = ImageIO.read(stream);

            if(img == null)
                return null;

            int img_width = img.getWidth(null);
            int img_height = img.getHeight(null);

            int tex_width = textureUtils.nearestPowerTwo(img_width, true);
            int tex_height = textureUtils.nearestPowerTwo(img_height, true);

            if(tex_width != img_width || tex_height != img_height)
            {
                System.out.println("Rescaling image to " + tex_width +
                                   "x" + tex_height);
                img = (BufferedImage)textureUtils.scaleTexture(img,
                                                               tex_width,
                                                               tex_height);
            }

            int format = TextureComponent.FORMAT_RGB;
            ColorModel cm = img.getColorModel();
            boolean alpha = cm.hasAlpha();

            switch(img.getType())
            {
                case BufferedImage.TYPE_3BYTE_BGR:
                case BufferedImage.TYPE_BYTE_BINARY:
                case BufferedImage.TYPE_INT_BGR:
                case BufferedImage.TYPE_INT_RGB:
                case BufferedImage.TYPE_USHORT_555_RGB:
                case BufferedImage.TYPE_USHORT_565_RGB:
                    format = TextureComponent.FORMAT_RGB;
                    break;

                case BufferedImage.TYPE_CUSTOM:
                    // no idea what this should be, so default to RGBA
                case BufferedImage.TYPE_INT_ARGB:
                case BufferedImage.TYPE_INT_ARGB_PRE:
                case BufferedImage.TYPE_4BYTE_ABGR:
                case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                    format = TextureComponent.FORMAT_RGBA;
                    break;

                case BufferedImage.TYPE_BYTE_GRAY:
                case BufferedImage.TYPE_USHORT_GRAY:
                    format = TextureComponent.FORMAT_SINGLE_COMPONENT;
                    break;

                case BufferedImage.TYPE_BYTE_INDEXED:
                    if(alpha)
                        format = TextureComponent.FORMAT_RGBA;
                    else
                        format = TextureComponent.FORMAT_RGB;
                    break;
            }

            comp = new ImageTextureComponent2D(format,
                                               tex_width,
                                               tex_height,
                                               img);
        }
        catch(IOException ioe)
        {
            System.out.println("Error reading image: " + ioe);
        }

        return comp;
    }

    public static void main(String[] args)
    {
        if (args.length == 0) {
            System.out.println("No file to display");
            System.out.println("Usage: java AV3DLoaderDemo file");
            System.exit(0);
        }

        AV3DLoaderDemo demo = new AV3DLoaderDemo();
        demo.load(args[0]);
        demo.setVisible(true);
    }
}
