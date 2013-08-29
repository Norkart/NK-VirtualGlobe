package org.web3d.vrml.scripting.external.eai;

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
import org.web3d.vrml.scripting.external.buffer.*;

/**
  * A factory interface for EAIEventAdapters.
  * <P>
 */

interface EAIEventAdapterFactory {
    /** Retreive or generate the ExternalEventAdapter associated with a node.
      * This is intended for lazy initialization of the event adapters.
      * @param node The node for which an adapter is necessary.
      * @return The adapter.
      */
    ExternalEventAdapter getAdapter(VRMLNodeType node);

    /** Set the field factory to pass on to node fields
      * @param aFactory The factory to use.
      */
    void setFieldFactory(EAIFieldFactory aFactory);

    /** Shut down the event adpater system so that futher events are not
      * sent through this interface.
      */
    void shutdown();

}
