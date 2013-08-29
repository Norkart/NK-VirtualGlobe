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

package org.web3d.vrml.scripting.external.buffer;

// External imports
import java.util.LinkedList;
import java.util.HashMap;

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.xj3d.core.eventmodel.ExternalView;

/**
 * NetworkEventQueue is a modified version of ExternalEventQueue from eai.buffer which
 * which differs mainly in that it does not have an external view to synchronize with.
  */

public class NetworkEventQueue implements ExternalView {
    /** Used for matching set1Value calls.  Since EventIn's which are targeted
      * at the same (node,id) pair are equal and have the same hashCode, this
      * is used to find matching set1Value calls. */
    private HashMap queuedEvents;

    /** External lock for the event list.  This has to be public because
      * the logic for merging set1Value calls isn't thread safe otherwise.
      */
    public Object eventLock;

    /** Are events being processed immediately or being queued? */
    private boolean queueing;

    /** The ErrorReporter to send messages to */
    protected ErrorReporter errorReporter;

    /** The queue for events since the last beginUpdate.
     *  These are the events which will be transferred to
     *  the waitingQueue when endUpdate is called. */
    LinkedList eventQueue;

    /** The events which will be processed at the end of the frame.
     *  These are the events waiting for the end of frame. */
    LinkedList waitingQueue;

    /** Basic constructor.
      * @param reporter The ErrorReporter to use.
      */
    public NetworkEventQueue(ErrorReporter reporter) {
        eventLock=new Object();
        eventQueue=new LinkedList();
        queuedEvents=new HashMap();
        waitingQueue=new LinkedList();
        if (reporter==null)
            errorReporter=DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter=reporter;
    }

    /** Start queueing updates.
      * It isn't an error to call this multiple
      * times, but extra calls are ignored.
      */
    public void beginUpdate() {
        queueing=true;
    }

    /** End queueing of updates, and send the current set to be processed.
      * It isn't an error to call this multiple
      * times, but extra calls are ignored.
      */
    public void endUpdate() {
        synchronized (eventLock) {
            if (queueing) {
                queueing=false;
                waitingQueue.addAll(eventQueue);
                eventQueue.clear();
                queuedEvents.clear();
                processEvents();
            }
        }
    }

    /** Return the ExternalEvent which is equal to the sent type.
      * This relies on the trick that EventIn instances are equal based on
      * node and field ID properties.
      */
    public ExternalEvent getLast(ExternalEvent type) {
        // We want the element in the set equal to type
        return (ExternalEvent)(queuedEvents.get(type));
    }

    /** Process an addition to the event queue.
      * This will either add the event to the queue, or execute it immediately
      * depending on the queueing state */
    public void processEvent(ExternalEvent event) {
        synchronized (eventLock) {
            if (queueing) {
                eventQueue.addLast(event);
                queuedEvents.put(event,event);
            } else {
                event.doEvent();
            }
        }
    }

    /***********************************************************************
     * Methods for ExternalView
     ***********************************************************************/

    /**
     * Process all waiting events.
     * @see org.xj3d.core.eventmodel.ExternalView#processEvents
     */
    public void processEvents() {
        //System.out.println("Processing events at "+System.currentTimeMillis());
        // Lock the event system just long enough
        // to get the to event list.
        // This is to deal better with
        // having multiple tight event loops.
        LinkedList queue;
        synchronized (eventLock) {
            queue=waitingQueue;
            waitingQueue=new LinkedList();
        }
        while (!queue.isEmpty()) {
            ExternalEvent event=(ExternalEvent)(queue.removeFirst());
            try {
                event.doEvent();
            } catch (Exception e) {
                errorReporter.errorReport(
                    "Error encountered processing buffered event.",e
                );
            }
        }
    }

    /**
     * Change the ErrorReporter this instance is using
     *  @see org.xj3d.core.eventmodel.ExternalView#setErrorReporter
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter=reporter;
    }

}
