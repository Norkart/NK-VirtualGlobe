package org.web3d.vrml.scripting.external.buffer;

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

import org.web3d.vrml.nodes.VRMLNodeType;

/** Interface for handling node realization requests.
 *  Due to the semantics of the external SAI, it is necessary to buffer
 *  requests to realize nodes while being able to determine which nodes have
 *  been buffered, and implementing that scheme ends up requiring a circular
 *  reference between the event and event queue, which this interface breaks.
 *  
 * @author Bradley Vender
 */

public interface NodeRealizationProcessor {

	/** Is the node realized or had realization requested?
	 * @param node The node in question
	 * @return The current status
	 */
	public boolean isNodeRealized(VRMLNodeType node);
	
	/** Mark a node as undergoing realization */
	public void markNodeRealized(VRMLNodeType node);

	/** Complete the realization of a node.  Removes the realization
	 * processing record to free up memory. */
	public void markRealizationComplete(VRMLNodeType node);

}
