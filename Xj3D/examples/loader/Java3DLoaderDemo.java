/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
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
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import javax.swing.*;

import java.net.URL;
import java.net.MalformedURLException;

// Application specific imports
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;

import com.sun.j3d.loaders.Scene;

import org.ietf.uri.ContentHandlerFactory;
import org.ietf.uri.FileNameMap;
import org.ietf.uri.URI;

// Local
import org.web3d.j3d.loaders.VRML97Loader;
import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.vrml.renderer.j3d.input.J3DPickingManager;

/**
 * An example of how to use the Java3D loader interface with the Xj3D codebase.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class Java3DLoaderDemo extends JFrame {

    /** Flag to say if this should be a static load of files */
    private static boolean staticLoad = false;

    /** The J3D universe to hold everything */
    private SimpleUniverse universe;

    /** A canvas that can display timing information */
    private Canvas3D canvas;

    /** The whole scenegraph's root */
    private BranchGroup sceneRoot;

    /** A transform for examine navigation style */
    private TransformGroup examineGroup;

    /* A group to hold the loaded scene */
    private BranchGroup sceneGroup;

    /* A transform for the viewer position */
    private TransformGroup vpTransGroup;

    // User interface variables
    /** A label for the URL field */
    private Label urlLabel;

    /**
     * Create a new loader
     *
     * @param initLocation The world to load
     */
    public Java3DLoaderDemo() {
        super("VRML97 Loader demo");

        Container content_pane = getContentPane();
        content_pane.setLayout(new BorderLayout());

        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(template.REQUIRED);
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        GraphicsConfiguration gfxConfig = dev.getBestConfiguration(template);
        Canvas3D canvas = new Canvas3D(gfxConfig);

        content_pane.add("Center",canvas);

        urlLabel = new Label("File:");

        content_pane.add("North",urlLabel);

        universe = new SimpleUniverse(canvas);
        universe.setJ3DThreadPriority(Thread.NORM_PRIORITY);

        ViewingPlatform viewingPlatform = universe.getViewingPlatform();
        vpTransGroup = viewingPlatform.getViewPlatformTransform();
        Viewer viewer = universe.getViewer();

        View view = viewer.getView();

        // TODO: Decide a real value for this.  Can't be infinite because
        // front/back ratio decides z precision
        view.setBackClipDistance(1000);

        setupNavigation();

        setSize(800, 600);
        setLocation(0, 40);
        setVisible(true);
    }

    //----------------------------------------------------------
    // Methods local to loader
    //----------------------------------------------------------

    /**
     * Setup the navigation system.  We use a simple examine behavior
     */
    private void setupNavigation() {

        sceneRoot = new BranchGroup();
        sceneRoot.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);

        examineGroup = new TransformGroup();
        examineGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        examineGroup.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        examineGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        examineGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        examineGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        examineGroup.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);
        sceneRoot.addChild(examineGroup);

        BoundingSphere behaviorBounds = new BoundingSphere(new Point3d(),
            Double.MAX_VALUE);

        MouseRotate mr = new MouseRotate();
        mr.setTransformGroup(examineGroup);
        mr.setSchedulingBounds(behaviorBounds);
        sceneRoot.addChild(mr);

        MouseTranslate mt = new MouseTranslate();
        mt.setTransformGroup(examineGroup);
        mt.setSchedulingBounds(behaviorBounds);
        sceneRoot.addChild(mt);

        MouseZoom mz = new MouseZoom();
        mz.setTransformGroup(examineGroup);
        mz.setSchedulingBounds(behaviorBounds);
        sceneRoot.addChild(mz);

        universe.addBranchGraph(sceneRoot);
    }

    /**
     * Load the given file into the scene.
     *
     * @param filename The name of the file or the URL to load
     */
    private void loadFile(String file) {

        int flag = VRML97Loader.LOAD_ALL;

        if(staticLoad)
            flag &= ~VRML97Loader.LOAD_BEHAVIOR_NODES;

        VRML97Loader loader = new VRML97Loader(flag);

        // if the file is a directory, ignore it
        File f = new File(file);
        if(f.exists() && !f.isFile()) {
            System.out.println("Can't load directories specified");
            System.exit(1);
        }

        URL url = null;
        Scene scene = null;

        try {
            url = new URL(file);
        } catch (MalformedURLException badUrl) {
            // if the location is not a URL, this is what you get
        }

        try {
            if(url != null)
                scene = loader.load(url);
            else
                scene = loader.load(file);
        } catch(Exception e) {
            System.out.println("Exception loading URL:" + e);
            e.printStackTrace();
            System.exit(0);
        }

        urlLabel.setText("File " + file);

        if (scene != null) {
            // get the scene group
            sceneGroup = scene.getSceneGroup();
            sceneGroup.setCapability(BranchGroup.ALLOW_DETACH);
            sceneGroup.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
            sceneGroup.compile();

            // add the scene group  to the scene
            examineGroup.addChild(sceneGroup);

            // now that the scene group is "live" we can inquire the bounds
            setViewpoint();
            setupLighting(scene);
        }
    }

    /**
     * Setup the scene's view
     */
    private void setViewpoint() {
        Transform3D viewTrans = new Transform3D();
        Transform3D eyeTrans = new Transform3D();

        // put the View at the standard VRML default position 0,0,10
        Vector3f pos = new Vector3f(0,0,10);
        eyeTrans.set(pos);
        viewTrans.mul(eyeTrans);

        // set the view transform
        vpTransGroup.setTransform(viewTrans);
    }

    /**
     * Setup the worlds lighting.  If none is provided in the VRML file then
     * we create a simple headlight
     *
     * @param scene The scene to source the lights from
     */
    private void setupLighting(Scene scene) {
        Light lights[] = scene.getLightNodes();

        if (lights == null) {
            BranchGroup lightBG = new BranchGroup();
            BoundingSphere lightBounds =
                new BoundingSphere(new Point3d(), Double.MAX_VALUE);
            DirectionalLight headLight =
                new DirectionalLight(new Color3f(1.0f,1.0f,1.0f),
                                     new Vector3f(0,0,-1));
            headLight.setCapability(Light.ALLOW_STATE_WRITE);
            headLight.setInfluencingBounds(lightBounds);

            lightBG.addChild(headLight);
            sceneRoot.addChild(lightBG);
        }
    }

    /**
     * A main body for running as an application
     *
     * @param args The arugment array
     */
    public static void main(String[] args) {

        String locString = null;

        if (args.length == 0) {
            System.out.println("No file to display");
            System.out.println("Usage: java Java3DLoaderDemo [-static] pathname | URL");
            System.exit(0);
        }

        staticLoad = args[0].equals("-static");

        if(((args.length == 1) && staticLoad) ||
           ((args.length == 2) && !staticLoad)) {

            System.out.println("No file to display");
            System.out.println("Usage: java Java3DLoaderDemo [-static] pathname | URL");
            System.exit(0);
        }

        String filename = (args.length == 1) ? args[0] : args[1];

        Java3DLoaderDemo demo = new Java3DLoaderDemo();
        demo.loadFile(filename);
    }
}
