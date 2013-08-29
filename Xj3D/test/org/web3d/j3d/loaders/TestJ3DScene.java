/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.j3d.loaders;

// Standard imports
import java.net.URL;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

// Application specific imports
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.j3d.J3DNodeFactory;
import org.web3d.vrml.lang.UnsupportedProfileException;

/**
 * A test case to check the functionality of the J3DScene implementation.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class TestJ3DScene extends TestCase {

    /** The J3DScene instance we are testing */
    private J3DScene scene;

    private static final String version = "3.0";

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestJ3DScene(String name) {
        super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestJ3DScene("emptyScene"));
        suite.addTest(new TestJ3DScene("loadEmpty"));
        suite.addTest(new TestJ3DScene("checkSingleItems"));

        return suite;
    }

    /**
     * Provide initialisation of this test instance.
     */
    public void setUp() {
        scene = new J3DScene();
    }

    /**
     * Test an empty scene to make sure that it does not contain anything.
     */
    public void emptyScene() {
        assertNull("Description not null", scene.getDescription());
        assertEquals("Non-empty background",
                     scene.getBackgroundNodes().length,
                     0);
        assertEquals("Non-empty behaviour",
                     scene.getBehaviorNodes().length,
                     0);
        assertEquals("Non-empty fog", scene.getFogNodes().length, 0);
        assertEquals("Non-empty views", scene.getViewGroups().length, 0);
        assertEquals("Non-empty fov", scene.getHorizontalFOVs().length, 0);
        assertEquals("Non-empty lights", scene.getLightNodes().length, 0);
        assertEquals("Non-empty sounds", scene.getSoundNodes().length, 0);
        assertNull("Non-empty scenegraph", scene.getSceneGroup());
        assertEquals("Non-empty DEF map",
                     scene.getNamedObjects().size(),
                     0);
    }

    /**
     * Test loading the scene with a world that contains nothing. Creates an
     * empty TestVRMLScene, asks the scene to load it and then calls the
     * emptyScene test to make sure that nothing has changed.
     */
    public void loadEmpty() {
        scene.setValues(new TestVRMLScene());
        emptyScene();
    }

    /**
     * Test for given types of nodes being available. The process we go through
     * is to set the scene with one item and then check to make sure none of
     * the other lists have accidently picked this up.
     * <p>
     * This test does not check for the factory generating node instances for
     * the requests. If the factory cannot create a requested node, we just
     * ignore that test and move on rather than barfing on the assert.
     * <p>
     * This test currently ignores behaviours as we don't know what they are
     * in this context.
     */
    public void checkSingleItems() {
        TestVRMLScene vrml;
        J3DNodeFactory factory = J3DNodeFactory.getJ3DNodeFactory();

        try {
            factory.setProfile("Interactive");
        } catch (UnsupportedProfileException upe) {
            fail("No profile named: " + upe.getMessage());
        }

        // Background nodes
        VRMLNodeType bg = factory.createVRMLNode("Background", version, false);

        if(bg != null) {
            vrml = new TestVRMLScene();
            scene = new J3DScene();
            vrml.registerBindableType("BackgroundNodeType");
            vrml.addBindableNode("BackgroundNodeType", bg);

            scene.setValues(vrml);

            assertNull("Background: Description not null",
                       scene.getDescription());
            assertEquals("Background node not set",
                         scene.getBackgroundNodes().length,
                         1);
            assertEquals("Background: Non-empty behaviour",
                         scene.getBehaviorNodes().length,
                         0);
            assertEquals("Background: Non-empty fog",
                         scene.getFogNodes().length,
                         0);
            assertEquals("Background: Non-empty views",
                         scene.getViewGroups().length,
                         0);
            assertEquals("Background: Non-empty fov",
                         scene.getHorizontalFOVs().length,
                         0);
            assertEquals("Background: Non-empty lights",
                         scene.getLightNodes().length,
                         0);
            assertEquals("Background: Non-empty sounds",
                         scene.getSoundNodes().length, 0);
            assertNull("Background: Non-empty scenegraph",
                       scene.getSceneGroup());
            assertEquals("Background: Non-empty DEF map",
                         scene.getNamedObjects().size(),
                         0);
        }

        // Fog nodes
        VRMLNodeType fog = factory.createVRMLNode("Fog", version, false);
        if(fog != null) {
            vrml = new TestVRMLScene();
            scene = new J3DScene();
            vrml.registerBindableType("Fog");
            vrml.addBindableNode("Fog", fog);

            scene.setValues(vrml);

            assertNull("Fog: Description not null", scene.getDescription());
            assertEquals("Fog: Non-empty Background",
                         scene.getBackgroundNodes().length,
                         0);
            assertEquals("Fog: Non-empty behaviour",
                         scene.getBehaviorNodes().length,
                         0);
            assertEquals("Fog not set", scene.getFogNodes().length, 1);
            assertEquals("Fog: Non-empty views", scene.getViewGroups().length, 0);
            assertEquals("Fog: Non-empty fov", scene.getHorizontalFOVs().length, 0);
            assertEquals("Fog: Non-empty lights", scene.getLightNodes().length, 0);
            assertEquals("Fog: Non-empty sounds", scene.getSoundNodes().length, 0);
            assertNull("Fog: Non-empty scenegraph", scene.getSceneGroup());
            assertEquals("Fog: Non-empty DEF map",
                         scene.getNamedObjects().size(),
                         0);
        }

        // Light nodes
        VRMLLightNodeType light =
            (VRMLLightNodeType)factory.createVRMLNode("DirectionalLight", version, false);

        if(light == null)
            light = (VRMLLightNodeType)factory.createVRMLNode("SpotLight", version, false);

        if(light == null)
            light = (VRMLLightNodeType)factory.createVRMLNode("PointLight", version, false);

        if(light != null) {
            vrml = new TestVRMLScene();
            scene = new J3DScene();
            vrml.addLight(light);
            scene.setValues(vrml);

            assertNull("Light: Description not null", scene.getDescription());
            assertEquals("Light: Non-empty Background",
                         scene.getBackgroundNodes().length,
                         0);
            assertEquals("Light: Non-empty behaviour",
                         scene.getBehaviorNodes().length,
                         0);
            assertEquals("Light: Non-empty Fog",
                         scene.getFogNodes().length,
                         0);
            assertEquals("Light: Non-empty views",
                         scene.getViewGroups().length,
                         0);
            assertEquals("Light: Non-empty fov",
                         scene.getHorizontalFOVs().length,
                         0);
            assertEquals("Lights not set", scene.getLightNodes().length, 2);
            assertEquals("Light: Non-empty sounds",
                         scene.getSoundNodes().length,
                         0);
            assertNull("Light: Non-empty scenegraph", scene.getSceneGroup());
            assertEquals("Light: Non-empty DEF map",
                         scene.getNamedObjects().size(),
                         0);
        }

        // Viewpoint nodes
        VRMLNodeType view = factory.createVRMLNode("Viewpoint", version, false);

        if(view != null) {
            vrml = new TestVRMLScene();
            scene = new J3DScene();
            vrml.registerBindableType("ViewpointNodeType");
            vrml.addBindableNode("ViewpointNodeType", view);

            scene.setValues(vrml);

            assertNull("Viewpoint: Description not null",
                       scene.getDescription());
            assertEquals("Viewpoint: Non-empty Background",
                         scene.getBackgroundNodes().length,
                         0);
            assertEquals("Viewpoint: Non-empty behaviour",
                         scene.getBehaviorNodes().length,
                         0);
            assertEquals("Viewpoint: Non-empty fog",
                         scene.getFogNodes().length,
                         0);
            assertEquals("Views not set",
                         scene.getViewGroups().length,
                         1);
            assertEquals("FOV not set",
                         scene.getHorizontalFOVs().length,
                         1);
            assertEquals("Viewpoint: Non-empty lights",
                         scene.getLightNodes().length,
                         0);
            assertEquals("Viewpoint: Non-empty sounds",
                         scene.getSoundNodes().length,
                         0);
            assertNull("Viewpoint: Non-empty scenegraph",
                       scene.getSceneGroup());
            assertEquals("Viewpoint: Non-empty DEF map",
                         scene.getNamedObjects().size(),
                         0);
        }

        // Sound nodes
        VRMLAudioClipNodeType clip =
            (VRMLAudioClipNodeType)factory.createVRMLNode("AudioClip", version, false);

        if(clip == null)
            clip =
                (VRMLAudioClipNodeType)factory.createVRMLNode("MovieTexture", version, false);

        if(clip != null) {
            vrml = new TestVRMLScene();
            scene = new J3DScene();
            vrml.addAudioClip(clip);
            scene.setValues(vrml);

            assertNull("Sound: Description not null", scene.getDescription());
            assertEquals("Sound: Non-empty Background",
                         scene.getBackgroundNodes().length,
                         0);
            assertEquals("Sound: Non-empty behaviour",
                         scene.getBehaviorNodes().length,
                         0);
            assertEquals("Sound: Non-empty Fog",
                         scene.getFogNodes().length,
                         0);
            assertEquals("Sound: Non-empty views",
                         scene.getViewGroups().length,
                         0);
            assertEquals("Sound: Non-empty fov",
                         scene.getHorizontalFOVs().length,
                         0);
            assertEquals("Sound: Non-empty lights",
                         scene.getLightNodes().length,
                         0);
            assertEquals("Sounds not set",
                         scene.getSoundNodes().length,
                         1);
            assertNull("Sound: Non-empty scenegraph", scene.getSceneGroup());
            assertEquals("Sound: Non-empty DEF map",
                         scene.getNamedObjects().size(),
                         0);
        }

        // Test the DEF handling
        VRMLNodeType group = factory.createVRMLNode("Group", version, false);

        if(group != null) {
            vrml = new TestVRMLScene();
            scene = new J3DScene();
            vrml.addDEFNode("MY_NODE", group);
            scene.setValues(vrml);

            assertNull("DEF: Description not null", scene.getDescription());
            assertEquals("DEF: Non-empty Background",
                         scene.getBackgroundNodes().length,
                         0);
            assertEquals("DEF: Non-empty behaviour",
                         scene.getBehaviorNodes().length,
                         0);
            assertEquals("DEF: Non-empty Fog",
                         scene.getFogNodes().length,
                         0);
            assertEquals("DEF: Non-empty views",
                         scene.getViewGroups().length,
                         0);
            assertEquals("DEF: Non-empty fov",
                         scene.getHorizontalFOVs().length,
                         0);
            assertEquals("DEF: Non-empty lights",
                         scene.getLightNodes().length,
                         0);
            assertEquals("DEF: Non-empty sounds",
                         scene.getSoundNodes().length,
                         0);
            assertNull("DEF: Non-empty scenegraph", scene.getSceneGroup());

            Map object_map = scene.getNamedObjects();
            assertEquals("DEF map is empty",
                         object_map.size(),
                         1);

            Object node = object_map.get("MY_NODE");
            assertTrue("DEF Object is not the same", node == group);
        }
    }

    /**
     * Main method to kick everything off with.
     */
    public static void main(String[] argv) {
        TestRunner.run(suite());
    }
}

