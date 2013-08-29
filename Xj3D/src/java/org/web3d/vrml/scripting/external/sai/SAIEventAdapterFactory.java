/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.external.sai;

// External imports
// none

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventAdapter;

/**
 * A factory interface for SAIEventAdapters.
 * <P>
 */

interface SAIEventAdapterFactory {
    /** 
     * Retrieve or generate the SAIEventAdapter associated with a node.
     * This is intended for lazy initialization of the event adapters.
     * @param node The node for which an adapter is needed.
     * @return Returns the adapter.
     */
    ExternalEventAdapter getAdapter(VRMLNodeType node);
}
