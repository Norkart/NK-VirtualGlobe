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

import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

/**
 * Helper class for the remove root node tests.
 * @author Bradley Vender
 *
 */

class RemoveRootNodeAction implements ActionListener {

  	X3DScene scene;
  	
  	RemoveRootNodeAction(X3DScene target) {
  		scene=target;
  	}
	/** 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		X3DNode rootNodes[]=scene.getRootNodes();
		for (int counter=0; counter<rootNodes.length; counter++) {
		    System.out.println("Root #"+counter+" is "+rootNodes[counter].getNodeName());
		}
		System.out.println("After removal:");
		scene.removeRootNode(rootNodes[0]);
		rootNodes=scene.getRootNodes();
		for (int counter=0; counter<rootNodes.length; counter++) {
			System.out.println("Root #"+counter+" is "+rootNodes[counter].getNodeName());
		}
		System.out.println("Done.");		
	}
  	
  }