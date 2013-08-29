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
public class OffscreenSAIDemo {

    /**
     * Constructor for the demo.
     */
    public OffscreenSAIDemo() {
        // Setup browser parameters
        HashMap requestedParameters=new HashMap();
        requestedParameters.put("TextureQuality", "medium");
        requestedParameters.put("Antialiased", Boolean.TRUE);
        requestedParameters.put("Xj3D_ShowConsole",Boolean.TRUE);
        requestedParameters.put("Xj3D_StatusBarShown",Boolean.TRUE);
        requestedParameters.put("Xj3D_FPSShown",Boolean.TRUE);
        requestedParameters.put("Xj3D_NavBarShown",Boolean.TRUE);
        requestedParameters.put("Xj3D_LocationShown",Boolean.TRUE);
        requestedParameters.put("Xj3D_OpenButtonShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_ReloadButtonShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_AntialiasingQuality", "high");
        requestedParameters.put("Xj3D_InterfaceType", "offscreen");
        requestedParameters.put("Xj3D_PreferredDimensions", "400x400");

        // Create an SAI component
        X3DComponent x3dComp = BrowserFactory.createX3DComponent(requestedParameters);

        // Add the component to the UI
        // Get an external browser
        ExternalBrowser x3dBrowser = x3dComp.getBrowser();

        // Create an X3D scene by loading a file
        X3DScene mainScene = x3dBrowser.createX3DFromURL(new String[] { "moving_box.x3dv" });

        // Replace the current world with the new one
        x3dBrowser.replaceWorld(mainScene);
    }

    /**
     * Main method.
     *
     * @param args None handled
     */
    public static void main(String[] args) {

        OffscreenSAIDemo demo = new OffscreenSAIDemo();
    }

}
