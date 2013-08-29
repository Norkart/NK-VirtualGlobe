/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import org.web3d.x3d.sai.*;

/**
 * Test that the addRootNode method works.
 * Variation of the original test which tries to create the scene,
 * and then modify it before using it for the current scene.
 */

public class AddRootNodeTest2 implements BrowserListener {
    private ExternalBrowser b;
    private X3DScene s;

    public void browserChanged(BrowserEvent event) {
        switch (event.getID()) {
            case BrowserEvent.INITIALIZED:
                System.out.println("Got initialized Event");
            break;
         }
    }

    public void run() {
        System.out.println("Add a root node to a scene.");
        ExternalBrowser b = SAITestFactory.getBrowser();
System.out.println("b: " + b);
        b.addBrowserListener(this);

        s =
            b.createX3DFromString(
                "#VRML V3.0 utf8\n" + "PROFILE Interactive\n"+"Transform { translation -3 0 0 children [Shape { geometry Sphere {}} ]}\n");
        
        X3DNode trans = s.createNode("Transform");
        MFNode trans_children = (MFNode) (trans.getField("children"));
        X3DNode shape = s.createNode("Shape");
        SFNode shape_geometry = (SFNode) (shape.getField("geometry"));
        X3DNode box = s.createNode("Box");
        trans_children.setValue(1, new X3DNode[] { shape });
        shape_geometry.setValue(box);
        X3DNode rootNodes[] = s.getRootNodes();
        System.out.println(
            "There are " + rootNodes.length + " root node(s) before addition");
        int counter;
        for (counter = 0; counter < rootNodes.length; counter++) {
            System.out.println(
                "Root #" + counter + " is " + rootNodes[counter].getNodeName());
        }
        trans.realize();

        s.addRootNode(trans);
        
        // Is addRootNode supposed to be a direct scene manipulation or is
        // it buffered?
        rootNodes = s.getRootNodes();
        System.out.println("Results after add.");
        for (counter=0; counter< rootNodes.length; counter++)
        	System.out.println("Root #"+counter+" is "+rootNodes[counter].getNodeName());

        
        b.replaceWorld(s);
    }

    public static void main(String[] args) {
        AddRootNodeTest2 arnt = new AddRootNodeTest2();
        arnt.run();

    }
}
