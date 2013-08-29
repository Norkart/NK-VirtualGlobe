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
public class SimpleSAIDemo extends JFrame {

    /**
     * Constructor for the demo.
     */
    public SimpleSAIDemo() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container contentPane = getContentPane();

        // Setup browser parameters
        HashMap requestedParameters=new HashMap();
        requestedParameters.put("Xj3D_ShowConsole",Boolean.TRUE);
        requestedParameters.put("Xj3D_StatusBarShown",Boolean.TRUE);
        requestedParameters.put("Xj3D_FPSShown",Boolean.TRUE);
        requestedParameters.put("Xj3D_NavBarShown",Boolean.TRUE);
        requestedParameters.put("Xj3D_LocationShown",Boolean.TRUE);
        requestedParameters.put("Xj3D_OpenButtonShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_ReloadButtonShown", Boolean.TRUE);
        requestedParameters.put("TextureQuality", "medium");
        requestedParameters.put("Antialiased", Boolean.TRUE);
        requestedParameters.put("Xj3D_AntialiasingQuality", "high");

        // Create an SAI component
        X3DComponent x3dComp = BrowserFactory.createX3DComponent(requestedParameters);

        // Add the component to the UI
        JComponent x3dPanel = (JComponent)x3dComp.getImplementation();
        contentPane.add(x3dPanel, BorderLayout.CENTER);

        // Get an external browser
        ExternalBrowser x3dBrowser = x3dComp.getBrowser();

        setSize(500,500);
        show();

/*
        // Create an X3D scene by loading a file
        X3DScene mainScene = x3dBrowser.createX3DFromURL(new String[] { "moving_box.x3dv" });

        // Replace the current world with the new one
        x3dBrowser.replaceWorld(mainScene);
*/
/*
        // Find a node named MAT
        X3DNode mat = mainScene.getNamedNode("MAT");
        if (mat == null) {
            System.out.println("Couldn't find material named: MAT");
            return;
        }

        // Get the diffuseColor field
        SFColor color = (SFColor) mat.getField("diffuseColor");

        // Set its value to blue
        float[] blue = {0,0,1};
        color.setValue(blue);
*/
    }

    /**
     * Main method.
     *
     * @param args None handled
     */
    public static void main(String[] args) {

        SimpleSAIDemo demo = new SimpleSAIDemo();
    }

}
