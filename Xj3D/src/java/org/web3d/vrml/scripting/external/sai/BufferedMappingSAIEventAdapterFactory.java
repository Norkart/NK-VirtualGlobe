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
import java.util.Hashtable;

// Local imports
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventAdapter;

/**
 * Implementation of SAIEventAdapterFactory.
 * <P>
 * This implementation of SAIEventAdapterFactory maintains a mapping
 * from VRMLNodeType to SAIEventAdapter so that a one-to-one mapping occurs.
 * <P>
 * This class uses the BufferedSAIEventAdapter class for its SAIEventAdapter
 * instances.
 */

class BufferedMappingSAIEventAdapterFactory implements SAIEventAdapterFactory {
    
    /** Mapping of VRMLNodeType to SAIEventAdapter */
    Hashtable adapterTable;

    /** The SAIFieldFactory to give the SAIEventAdapter instances */
    SAIFieldFactory theFieldFactory;

    /** The thread pool to send events to. */
    SAIEventAdapterThreadPool thePool;

    /** The time clock to ensure consistent time stamps */
    VRMLClock timeClock;
    
    /** Basic constructor. */
    BufferedMappingSAIEventAdapterFactory(VRMLClock clock) {
        this(2,clock);
    }
    /** Basic constructor specifying the size of the thread pool. */
    BufferedMappingSAIEventAdapterFactory(int numThreads, VRMLClock clock) {
        adapterTable=new Hashtable();
        thePool=new SAIEventAdapterThreadPool(numThreads);
        timeClock=clock;
    }

    /** Retrieve or generate the SAIEventAdapter associated with a node.
      * This is intended for lazy initialization of the event adapters.
      */
    public ExternalEventAdapter getAdapter(VRMLNodeType node) {
        BufferedSAIEventAdapter result=(BufferedSAIEventAdapter)
            (adapterTable.get(node));
        if (result==null) {
            result=new BufferedSAIEventAdapter(theFieldFactory,node,thePool,timeClock);
            adapterTable.put(node,result);
            node.addNodeListener(result);
        }
        return result;
    }

    /** Set the field factory to pass on. */
    void setFieldFactory(SAIFieldFactory aFactory) {
        theFieldFactory=aFactory;
    }

    /** Shutdown the event adapter system. */
    public void shutdown() {
        thePool.shutdown();
    }
}
