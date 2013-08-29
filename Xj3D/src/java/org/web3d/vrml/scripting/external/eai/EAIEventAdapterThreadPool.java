package org.web3d.vrml.scripting.external.eai;

// Standard imports
import java.security.AccessController;
import java.security.PrivilegedAction;

// Application specific imports
import org.web3d.util.BlockingQueue;
import org.web3d.vrml.scripting.external.buffer.*;

import vrml.eai.event.VrmlEvent;
import vrml.eai.event.VrmlEventListener;

/** The EAIEventAdapterThreadPool manages the pool of notifier threads
  * used by the ExternalEventAdapter classes to send VrmEvent's out.
  * The use scenario for this class involves calling sendEvent with a
  * VrmlEvent object and allowing the thread pool manage things from there.
  */
class EAIEventAdapterThreadPool {
    /** Simple data record for notifications. */
    class EventRecord {
        /** The receiver of the event */
        VrmlEventListener receiver;
        /** The event to process */
        VrmlEvent event;
        /** Buffer to reclaim after processing event */
        ExternalOutputBuffer buffer;
        /** Reclaimation tag associated with buffer */
        int tag;
        /** Who gets the buffer for reclaiming */
        ExternalOutputBufferReclaimer reclaimer;

        /** Make a new event record.  Mandatory to call load. */
        EventRecord() {};

        /** Load the EventRecord with useful information for dispatching
          * @param aListener The receiver of the event
          * @param anEvent The event we're dispatching
          * @param aBuffer The buffer to clear after dispatching
          * @param aTag The reclaimation tag associated with the buffer
          */
        void load(
            VrmlEventListener aListener, VrmlEvent anEvent, 
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
        /** Shutdown signal */
        boolean shouldStop;

        /** Event notifications come from this queue */
        BlockingQueue theSourceQueue;

        /** Used notifications go to this queue for recycling */
        BlockingQueue theRecyclingQueue;

        /** Construct and automatically start a new event dispatch thread.
         * @param sourceQueue The queue to take event out of
         * @param recyclingQueue The queue to put events back into
         */
        BroadcastThread(BlockingQueue sourceQueue, BlockingQueue recyclingQueue) {
            theSourceQueue=sourceQueue;
            theRecyclingQueue=recyclingQueue;
            start();
        }

        /** Broadcast events until shouldStop becomes true */
        public void run() {
            while (!shouldStop) {
                EventRecord e=(EventRecord)(theSourceQueue.getNext());
                if (e==null) {
                    theRecyclingQueue=null;
                    return;
                }
                e.receiver.eventOutChanged(e.event);
                e.reclaimer.reclaimEventOutBuffer(e.buffer,e.tag);
                e.reset();
                theRecyclingQueue.add(e);
            }
            theRecyclingQueue=null;
        }

        /** Signal clean shutdown */
        void shutdown() {
            shouldStop=true;
        }
    }

    /** The pool of worker threads */
    BroadcastThread threads[];

    /** The queue common to all threads in the pool. */
    BlockingQueue theSourceQueue;

    /** The queue for used notifications from all threads in the pool. */
    BlockingQueue theRecyclingQueue;

    /** Set up a broadcast thread pool with some number of threads
      * @param numThreads The number of threads to use
      */
    EAIEventAdapterThreadPool(int numThreads) {
        threads=new BroadcastThread[numThreads];
        theSourceQueue=new BlockingQueue();
        theRecyclingQueue=new BlockingQueue();
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
            )
        );
    }

    /** Queue an event for broadcast by the worker threads.
      * After sending the event, reclaim the buffer associated with that
      * event.
      * @param receiver Who gets the event
      * @param event The event to send
      * @param buf The buffer to reclaim
      * @param shippingTag The buffer's reclaimation tag
      * @param reclaimer The object which reclaims the buffer
      */
    void sendEvent(
        VrmlEventListener receiver, 
		VrmlEvent event, 
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

    /** Shutdown the system and notify all threads to stop. */
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

