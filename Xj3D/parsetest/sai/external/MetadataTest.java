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

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import org.web3d.x3d.sai.*;

/**
 * Test metdata access.
 *
 * @author Alan Hudson
 * @version
 */
public class MetadataTest extends JFrame {

    /**
     * Constructor for the demo.
     */
    public MetadataTest() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container contentPane = getContentPane();

        // Setup browser parameters
        HashMap requestedParameters = new HashMap();

        // Create an SAI component
        X3DComponent x3dComp = BrowserFactory.createX3DComponent(requestedParameters);

        // Add the component to the UI
        JComponent x3dPanel = (JComponent)x3dComp.getImplementation();
        contentPane.add(x3dPanel, BorderLayout.CENTER);

        // Get an external browser
        ExternalBrowser x3dBrowser = x3dComp.getBrowser();

        setSize(600,500);
        show();

        // Create an X3D scene by loading a file.  Blocks till the world is loaded.
        X3DScene mainScene = x3dBrowser.createX3DFromURL(new String[] { "meta.x3dv" });

        // Replace the current world with the new one
        x3dBrowser.replaceWorld(mainScene);

        Map mmap = mainScene.getMetaData();

        System.out.println("Map(x3dv): " + mmap.size());

        System.out.println("Foo(x3dv): " + mmap.get("foo"));

        // Create an X3D scene by loading a file.  Blocks till the world is loaded.
        mainScene = x3dBrowser.createX3DFromURL(new String[] { "meta.x3d" });

        // Replace the current world with the new one
        x3dBrowser.replaceWorld(mainScene);

        mmap = mainScene.getMetaData();

        System.out.println("Map(x3d): " + mmap.size());

        System.out.println("Foo(x3d): " + mmap.get("foo"));

    }

    /**
     * Main method.
     *
     * @param args None handled
     */
    public static void main(String[] args) {

        MetadataTest demo = new MetadataTest();
    }
}
