/*****************************************************************************
 * Copyright North Dakota State University, 2005
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/
package org.web3d.vrml.scripting.external.buffer;

import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Simple event class for processing node realization in
 * the external SAI classes.
 * @author Bradley Vender
 */
public class SAIRealizeNode implements ExternalEvent {

	/** The object which is processing the realizations */
	NodeRealizationProcessor realizationProcessor;
	
	/** The node which is realizing */
	VRMLNodeType realizingNode;

	/**
	 * Basic event constructor.
	 * @param nodeToRealize The node to realize.
	 * @param processor The instance processing the realization.
	 */
	SAIRealizeNode(VRMLNodeType nodeToRealize,
			NodeRealizationProcessor processor) {
		realizingNode=nodeToRealize;
		realizationProcessor=processor;
	}
	
	public void doEvent() {
		realizingNode.setupFinished();
		realizationProcessor.markRealizationComplete(realizingNode);
	}

	/** @see ExternalEvent#isConglomerating
	 *  Node realizationations don't conglomerate
	 */
	public boolean isConglomerating() {
		return false;
	}

}
