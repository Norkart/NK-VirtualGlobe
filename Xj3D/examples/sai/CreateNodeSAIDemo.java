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
import java.awt.*;
import java.util.HashMap;
import javax.swing.*;

import org.web3d.x3d.sai.*;

/**
 * A simple example of how to use SAI to load a scene and modify a value.
 *
 * @author Alan Hudson
 * @version
 */
public class CreateNodeSAIDemo extends JFrame {

    /**
     * Constructor for the demo.
     */
    public CreateNodeSAIDemo() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container contentPane = getContentPane();

        // Setup browser parameters
        HashMap requestedParameters=new HashMap();
        requestedParameters.put("Xj3D_ShowConsole",Boolean.FALSE);
        requestedParameters.put("Xj3D_StatusBarShown",Boolean.TRUE);
        requestedParameters.put("Xj3D_FPSShown",Boolean.FALSE);

        // Create an SAI component
        X3DComponent x3dComp = BrowserFactory.createX3DComponent(requestedParameters);

        // Add the component to the UI
        JComponent x3dPanel = (JComponent)x3dComp.getImplementation();
        contentPane.add(x3dPanel, BorderLayout.CENTER);

        // Get an external browser
        ExternalBrowser x3dBrowser = x3dComp.getBrowser();

        setSize(500,500);
        show();


        // Create an X3D scene by loading a file
        X3DScene mainScene = x3dBrowser.createX3DFromURL(new String[] { "create_nodes.x3dv" });

        // Replace the current world with the new one
        x3dBrowser.replaceWorld(mainScene);

        x3dBrowser.beginUpdate();
        X3DNode shape = mainScene.createNode("Shape");
        SFNode shape_geometry = (SFNode) (shape.getField("geometry"));
        X3DNode box = mainScene.createNode("Box");

        shape_geometry.setValue(box);

        shape.realize();

        mainScene.addRootNode(shape);
        x3dBrowser.endUpdate();
    }

    /**
     * Main method.
     *
     * @param args None handled
     */
    public static void main(String[] args) {

        CreateNodeSAIDemo demo = new CreateNodeSAIDemo();
    }

}
