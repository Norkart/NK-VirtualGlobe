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

import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

import java.util.Hashtable;

/**
  * Implmenation of EAIEventAdapterFactory.
  * <P>
  * This implementation of EAIEventAdapterFactory maintains a mapping
  * from VRMLNodeType to ExternalEventAdapter so that a one-to-one mapping occurs.
  * <P>
  * This class uses the BufferedEAIEventAdapter class for its ExternalEventAdapter
  * instances.
 */

class BufferedMappingEAIEventAdapterFactory implements EAIEventAdapterFactory {
    /** Mapping of VRMLNodeType to ExternalEventAdapter */
    Hashtable adapterTable;

    /** The EAIFieldFactory to give the ExternalEventAdapter instances */
    EAIFieldFactory theFieldFactory;

    /** The thread pool to send events to. */
    EAIEventAdapterThreadPool thePool;

    /** The time clock for producing consistent timestamps */
    VRMLClock timeClock;
    
    /** Basic constructor specifying the size of the thread pool. */
    BufferedMappingEAIEventAdapterFactory(int numThreads, VRMLClock clock) {
        adapterTable=new Hashtable();
        thePool=new EAIEventAdapterThreadPool(numThreads);
        timeClock=clock;
    }

    /** Retreive or generate the ExternalEventAdapter associated with a node.
      * This is intended for lazy initialization of the event adapters.
      */
    public ExternalEventAdapter getAdapter(VRMLNodeType node) {
        BufferedEAIEventAdapter result=(BufferedEAIEventAdapter)
            (adapterTable.get(node));
        if (result==null) {
            result=new BufferedEAIEventAdapter(theFieldFactory,node,thePool,timeClock);
            adapterTable.put(node,result);
            node.addNodeListener(result);
        }
        return result;
    }

    /** Set the field factory to pass on. */
    public void setFieldFactory(EAIFieldFactory aFactory) {
        theFieldFactory=aFactory;
    }

    /** Shutdown the event adapter system. */
    public void shutdown() {
        thePool.shutdown();
    }

}
