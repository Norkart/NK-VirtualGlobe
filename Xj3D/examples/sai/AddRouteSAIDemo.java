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
import java.io.File;

import org.web3d.x3d.sai.*;

/**
 * A simple example of how to use SAI to load a scene and modify a value.
 *
 * @author Alan Hudson
 * @version
 */
public class AddRouteSAIDemo extends JFrame implements BrowserListener {

    /**
     * Constructor for the demo.
     */
    public AddRouteSAIDemo() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container contentPane = getContentPane();

        // Setup browser parameters
        HashMap requestedParameters=new HashMap();
        requestedParameters.put("Xj3D_ShowConsole",Boolean.TRUE);

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
        X3DScene mainScene = x3dBrowser.createX3DFromURL(new String[] { "still_box.x3dv" });

        // Replace the current world with the new one
        x3dBrowser.replaceWorld(mainScene);

        // Find a timesensor named TS
        X3DNode TS = mainScene.getNamedNode("TS");
        X3DNode PI = mainScene.getNamedNode("PI");
        X3DNode TG = mainScene.getNamedNode("TG");

        mainScene.addRoute(TS,"fraction_changed",PI,"set_fraction");
        mainScene.addRoute(PI,"value_changed",TG,"translation");
    }

    public void browserChanged(BrowserEvent event) {
        int id = event.getID();

        switch(id) {
            case BrowserEvent.INITIALIZED:
                System.out.println("World Initialized");
                break;
            case BrowserEvent.URL_ERROR:
                System.out.println("Error loading world");
                break;
        }
    }

    /**
     * Main method.
     *
     * @param args None handled
     */
    public static void main(String[] args) {

        AddRouteSAIDemo demo = new AddRouteSAIDemo();
    }

}
