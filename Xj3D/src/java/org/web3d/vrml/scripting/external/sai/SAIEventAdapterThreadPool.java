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
import java.security.AccessController;
import java.security.PrivilegedAction;

// Local imports
import org.web3d.util.BlockingQueue;

import org.web3d.vrml.scripting.external.buffer.ExternalOutputBufferReclaimer;
import org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer;

import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;

/** 
 * The SAIEventAdapterThreadPool manages the pool of notifier threads
 * used by the SAIEventAdapter classes to send X3DFieldEvent's out.
 * The use scenario for this class involves calling sendEvent with a
 * X3DFieldEvent object and allowing the thread pool manage things from there.
 *
 * @author Brad Vendor
 * @version $Revision: 1.5 $
 */
class SAIEventAdapterThreadPool {
    
    /** The name of the threads in the pool */
    private static final String THREAD_NAME = "Xj3D SAI Event Messenger";
    
    /** The name of the thread group */
    private static final String GROUP_NAME = "Xj3D SAI Event Delivery";
    
    /** The thread group */
    private ThreadGroup group;
    
    /** Simple data record for notifications. */
    class EventRecord {
        
        /** The receiver of the event */
        X3DFieldEventListener receiver;
        
        /** The event to process */
        X3DFieldEvent event;
        
        /** Buffer to reclaim after processing event */
        ExternalOutputBuffer buffer;
        
        /** Reclaimation tag associated with buffer */
        int tag;
        
        /** Who gets the buffer for reclaiming */
        ExternalOutputBufferReclaimer reclaimer;
        
        /** Make a new record.  Mandatory to call load. */
        EventRecord () {};
        
        /** Load the EventRecord with useful information for dispatching
         * @param aListener The receiver of the event
         * @param anEvent The event we're dispatching
         * @param aBuffer The buffer to clear after dispatching
         * @param aTag The reclaimation tag associated with the buffer
         */
        void load(
            X3DFieldEventListener aListener, X3DFieldEvent anEvent, 
            ExternalOutputBuffer aBuffer, int aTag,
            ExternalOutputBufferReclaimer aReclaimer
            ) {
            receiver=aListener;
            event=anEvent;
            buffer=aBuffer;
            tag=aTag;
            reclaimer=aReclaimer;
        }
        
        /** Null out all references to prevent leaks in user code */
        void reset() {
            receiver=null;
            event=null;
            buffer=null;
            reclaimer=null;
            tag=-1;
        }
        
    }
    
    /** Simple notifier threads designed to stop when they see a
     * null record in the queue.
     */
    class BroadcastThread extends Thread {
        /** Flag for termination of process. */
        boolean shouldStop;
        
        /** The queue to get events from. */
        BlockingQueue theSourceQueue;
        
        /** Used notifications go to this queue for recycling */
        BlockingQueue theRecyclingQueue;
        
        /** Construct a thread which generates events from the records
         * in a queue filled by other means.
         * @param sourceQueue The queue to take event out of
         * @param recyclingQueue The queue to put events back into
         */
        BroadcastThread(BlockingQueue sourceQueue, BlockingQueue recyclingQueue) {
            super( group, THREAD_NAME );
            theSourceQueue=sourceQueue;
            theRecyclingQueue=recyclingQueue;
            start();
        }
        
        /** Read from the event queue and notify the appropriate parties. */
        public void run() {
            while (!shouldStop) {
                EventRecord e=(EventRecord)(theSourceQueue.getNext());
                if (e==null) {
                    theRecyclingQueue=null;
                    return;
                }
                e.receiver.readableFieldChanged(e.event);
                e.reclaimer.reclaimEventOutBuffer(e.buffer,e.tag);
                e.reset();
                theRecyclingQueue.add(e);
            }
            theRecyclingQueue=null;
        }
        
        /** Set the stop flag for this instance. */
        void shutdown() {
            shouldStop=true;
        }
    }
    
    /** The set of worker threads. */
    BroadcastThread threads[];
    
    /** The queue common to all threads in the pool. */
    BlockingQueue theSourceQueue;
    
    /** The queue for used notifications from all threads in the pool. */
    BlockingQueue theRecyclingQueue;
    
    /** Create an pool of adapter threads.  Each thread
     * generates X3D field changed events when the underlying system
     * notifies them of changes.
     * @param numThreads The number of threads to create. 
     */
    SAIEventAdapterThreadPool(int numThreads) {
        threads=new BroadcastThread[numThreads];
        theSourceQueue=new BlockingQueue();
        theRecyclingQueue=new BlockingQueue();
        if(group == null) {
            group = new ThreadGroup(GROUP_NAME);
        }
        while (numThreads-->0)
            threads[numThreads]=createThread(theSourceQueue,theRecyclingQueue);
    }
    
    /** Method to encapsulate the annoying security restriction surrounding
     * thread creation in applets.
     */
    BroadcastThread createThread(BlockingQueue source, BlockingQueue output) {
        return (BroadcastThread)(
            AccessController.doPrivileged(
            new CreateBroadcastThreadAction(source,output)
            ) );
    }
    
    
    void sendEvent(
        X3DFieldEventListener receiver, 
        X3DFieldEvent event,
        ExternalOutputBuffer buf,
        int shippingTag,
        ExternalOutputBufferReclaimer reclaimer
        ) {
        EventRecord record;
        // If the recycling queue is null, then we've been shut down.
        // Read the value once to avoid threading issues.
        if (theRecyclingQueue==null)
            return;
        record=(EventRecord)(theRecyclingQueue.peekNext());
        if (record==null)
            record=new EventRecord();
        else
            record=(EventRecord)(theRecyclingQueue.getNext());
        record.load(receiver,event,buf,shippingTag,reclaimer);
        theSourceQueue.add(record);
    }
    
    void shutdown() {
        int counter;
        for (counter=0; counter<threads.length; counter++)
            threads[counter].shutdown();
        theSourceQueue.purge();
        theRecyclingQueue=null;
        // To make sure that none of the threads are stuck in waiting.
        for (counter=0; counter<threads.length; counter++)
            theSourceQueue.add(null);
    }
    
    /** Utility class for constructing the event broadcast threads
     * as a PrivilegedAction so that this will work in unblessed
     * applets. */
    class CreateBroadcastThreadAction implements PrivilegedAction {
        /** The source queue for the thread */
        BlockingQueue theSourceQueue;
        
        /** The recycling queue for the thread */
        BlockingQueue theRecyclingQueue;
        
        /** Constructor required because it is necessary to pass
         * values from a method parameter
         * @param sourceQueue The source queue for the new thread to use
         * @param recyclingQueue The recycling queue for the new thread
         */
        CreateBroadcastThreadAction(BlockingQueue sourceQueue, 
            BlockingQueue recyclingQueue) 
        {
            theSourceQueue=sourceQueue;
            theRecyclingQueue=recyclingQueue;
        }
        
        /** Create the thread */
        public Object run() {
            return new BroadcastThread(theSourceQueue, theRecyclingQueue );
        }
    }
}

