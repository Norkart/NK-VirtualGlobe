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

import org.web3d.x3d.sai.*;

/**
 * Test that the addRootNode method works.
 * Variation of the original test which uses a button to trigger
 * the addition of the root node.
 */

public class InteractiveAddRootNodeTest {

    public static void main(String[] args) {
        ExternalBrowser b = SAITestFactory.getBrowser();
        X3DScene s = b.createX3DFromString(
                "#VRML V3.0 utf8\n" 
        		+ "PROFILE Interactive\n"
				//+"Transform { translation -3 0 0 children [Shape { geometry Sphere {}} ]}\n"
		);
		b.replaceWorld(s);
		JFrame f=new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JButton doIt=new JButton("Add Root Node");
		f.getContentPane().add(doIt);
		doIt.addActionListener(new ModifySceneAction(s));
		f.pack();
		f.show();
    }
}

class ModifySceneAction implements ActionListener {

	/** Make the simple actor */
	ModifySceneAction(X3DScene s) {
		scene=s;
	}
	
	/** The scene to modify */
	X3DScene scene;
	
	/** 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			X3DNode trans = scene.createNode("Transform");
			MFNode trans_children = (MFNode) (trans.getField("children"));
			X3DNode shape = scene.createNode("Shape");
			SFNode shape_geometry = (SFNode) (shape.getField("geometry"));
			X3DNode box = scene.createNode("Box");
			trans_children.setValue(1, new X3DNode[] { shape });
			SFNode shape_appearance = (SFNode) (shape.getField("appearance"));
			X3DNode appearance = scene.createNode("Appearance");
			shape_appearance.setValue(appearance);
			shape_geometry.setValue(box);
			X3DNode rootNodes[] = scene.getRootNodes();
			System.out.println(
					"There are " + rootNodes.length + " root node(s) before addition");
			int counter;
			for (counter = 0; counter < rootNodes.length; counter++) {
				System.out.println(
						"Root #" + counter + " is " + rootNodes[counter].getNodeName());
			}
			trans.realize();
			scene.addRootNode(trans);
			System.out.println("There are now "+scene.getRootNodes().length);
			rootNodes=scene.getRootNodes();
			for (counter = 0; counter < rootNodes.length; counter++) {
				System.out.println(
						"Root #" + counter + " is " + rootNodes[counter].getNodeName());
			}
			
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
		}
	}
}