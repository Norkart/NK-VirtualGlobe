/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

import java.awt.*;
import java.util.HashMap;
import javax.swing.*;

import org.web3d.x3d.sai.*;

/**
 * An example server application which shows how to create sensors
 * using picking.
 *
 * @author Alan Hudson
 * @version
 */
public class PickServer implements X3DFieldEventListener {
    private SFBool isActiveField;

    /**
     * Constructor for the demo.
     */
    public PickServer(boolean visual) {

        // Setup browser parameters
        HashMap requestedParameters=new HashMap();
        requestedParameters.put("Xj3D_ShowConsole",Boolean.FALSE);

        if (visual)
            System.setProperty("x3d.sai.factory.class", "org.web3d.ogl.browser.X3DOGLBrowserFactoryImpl");
        else
            System.setProperty("x3d.sai.factory.class", "org.web3d.ogl.browser.X3DNRBrowserFactoryImpl");

        // Create an SAI component
        X3DComponent x3dComp = BrowserFactory.createX3DComponent(requestedParameters);

        // Get an external browser
        ExternalBrowser x3dBrowser = x3dComp.getBrowser();

        if (visual) {
            JFrame frame = new JFrame("Xj3D PickServer");
            Container contentPane = frame.getContentPane();

            JComponent x3dPanel = (JComponent)x3dComp.getImplementation();
            contentPane.add(x3dPanel, BorderLayout.CENTER);

            frame.setSize(512,512);
            frame.show();
        }

        System.out.println("Loading X3D World");

        // Create an X3D scene by loading a file
        X3DScene mainScene = x3dBrowser.createX3DFromURL(new String[] { "pick_world.x3dv" });

        // Replace the current world with the new one
        x3dBrowser.replaceWorld(mainScene);

        // Find a node named MAT
        X3DNode picker = mainScene.getNamedNode("PICKER");
        if (picker == null) {
            System.out.println("Couldn't find LinePicker named: PICKER");
            return;
        }

        System.out.println("Simulation running");
        // Get the isActive field
        isActiveField = (SFBool) picker.getField("isActive");
        isActiveField.addX3DEventListener(this);
    }

    public void readableFieldChanged(X3DFieldEvent evt) {
        if (isActiveField.getValue())
            System.out.println("Target found");
    }

    /**
     * Main method.
     *
     * @param args None handled
     */
    public static void main(String[] args) {
        boolean visual = false;

        if (args.length > 0) {
            if (args[0].equals("-visual")) {
                visual = true;
            }
        }

        PickServer server = new PickServer(visual);
    }

}
