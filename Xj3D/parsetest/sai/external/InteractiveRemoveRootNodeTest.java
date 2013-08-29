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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

/**
 * Test that the removeRootNode method works.
 * This test runs into the problem that sometimes things are getting implemented as shared groups and
 * sometimes they are getting marked pure.
 */
public class InteractiveRemoveRootNodeTest {
	
  public static void main(String[] args) {
  	X3DScene scene;
    System.out.println("Remove a root node from a scene.");
    ExternalBrowser b=SAITestFactory.getBrowser();
    System.out.println("Before the removal:");
    scene=b.createX3DFromString(
        "#VRML V3.0 utf8\n"+
        "PROFILE Interactive\n"+
        "Transform { translation -3 0 0 children Shape { geometry Sphere {}}}\n"+
        "Transform { translation 3 0 0 children Shape { geometry Box {}}}\n"+
        "Transform { translation 0 2 0 children Shape { geometry Cone {}}}\n"
    );
    X3DNode rootNodes[]=scene.getRootNodes();
    X3DScene otherScene=b.createX3DFromString(
            "#VRML V3.0 utf8\n"+
	        "PROFILE Interactive\n"
    		);
    for (int counter=0; counter<rootNodes.length; counter++) {
    	scene.removeRootNode(rootNodes[counter]);
    	otherScene.addRootNode(rootNodes[counter]);
    }
    scene=otherScene;
	b.replaceWorld(scene);

    MFNode root1Children=(MFNode)(rootNodes[1].getField("children"));
    X3DNode kids[]=new X3DNode[root1Children.getSize()];
    root1Children.getValue(kids);
    for (int counter=0; counter<kids.length;counter++)
    	System.out.println(kids[counter].getNodeName());

    JFrame demoFrame=new JFrame("Demo");
    JButton removeButton=new JButton("Remove root node");
    demoFrame.getContentPane().add(removeButton);
    removeButton.addActionListener(new RemoveRootNodeAction(scene));
    demoFrame.pack();
    demoFrame.show();
  }
}  
