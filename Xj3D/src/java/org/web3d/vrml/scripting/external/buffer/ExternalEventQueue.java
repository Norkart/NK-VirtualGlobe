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

// Local imports
// None

// External imports
import org.web3d.browser.BrowserCore;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.util.ObjectArray;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DFieldDefinition;

import org.xj3d.core.eventmodel.ExternalView;

/** The ExternalEventQueue provides the update batching functionality for the
  * EAI browser and the external SAI browser.
  *  This class handles the buffering of event setValue calls and the
  *  various browser requests.
  * <P>
  * This queue handles all of the event types, including the replaceWorld
  * methods.  Because I do not fully understand the intention of the
  * beginUpdate/endUpdate queue system for handling browser requests,
  * it is possible to change between queued and unqueued behavior
  * by modifying the appropriate static constants.
  * <P>
  * Amalgamation of distinct set1Value events into single setValue events
  * is the responsibility of the event buffers/field wrapper classes.
  * The only functionality that this class exposes is the ability to locate
  * the appropriate previous buffers, and exposing the synchronization lock
  * for thread safety.
  * <P>
  * This class was previously named the EAIEventQueue, which became a misnomer
  * when it was repurposed for both EAI and external SAI support.  It has not
  * been seen as sufficiently useful to split this queue into an EAI and an
  * SAI version, thus the compilation dependencies from the VRML and X3D
  * systems.
  * <P>
  * The responsibility for registering the event queue with the appropriate
  * triggering mechanisms (current the EventModelEvaluator) is external
  * to this class.
  */

public class ExternalEventQueue implements ExternalView, NodeRealizationProcessor {

    /** Initial size for the event queues */
    static final int INITIAL_EVENT_QUEUE_SIZE=20;

    /** Used for matching set1Value calls.  Since EventIn's which are targeted
      * at the same (node,id) pair are equal and have the same hashCode, this
      * is used to find matching set1Value calls. */
    private HashSet conglomerativeEvents;

    /** Used for recording which nodes have been marked as realizing.
     *  Nodes get removed from this list as they complete realization.
     */
    private HashSet realizingNodes;

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
    ObjectArray eventQueue;

    /** The events which will be processed at the end of the frame.
     *  These are the events waiting for the end of frame. */
    ObjectArray waitingQueue;

    /** Temporary queue to avoid locking the event queue during processing */
    ObjectArray eventsInProcessing;

    /** Basic constructor.
      * @param reporter The ErrorReporter to use.
      */
    public ExternalEventQueue(ErrorReporter reporter) {
        eventLock=new Object();
        eventQueue=new ObjectArray(INITIAL_EVENT_QUEUE_SIZE);
        conglomerativeEvents=new HashSet();
        realizingNodes=new HashSet();
        waitingQueue=new ObjectArray(INITIAL_EVENT_QUEUE_SIZE);
        eventsInProcessing=new ObjectArray(INITIAL_EVENT_QUEUE_SIZE);
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
                conglomerativeEvents.clear();
            }
        }
    }

    /** Return the ExternalEvent which is equal to the sent type.
      * This relies on the trick that EventIn instances are equal based on
      * node and field ID properties.
      */
    public ExternalEvent getLast(ExternalEvent type) {
        // We want the element in the set equal to type
        return (ExternalEvent)(conglomerativeEvents.get(type));
    }

    /** Process a Browser.addRoute request. */
    public void postAddRoute(VRML97CommonBrowser theBrowser,
        BrowserCore theCore, VRMLNodeType fromNode, String eventOut,
        VRMLNodeType toNode, String eventIn
    ) {
        processEvent(
            new EAIAddRoute(
                theBrowser, theCore, fromNode, eventOut, toNode, eventIn
            )
        );
    }

    /** Process a Browser.createVrmlFromURL request */
    public void postCreateVrmlFromURL(
        VRML97CommonBrowser theBrowser, String url[], VRMLNodeType node,
        String eventIn
    ) {
        new EAICreateVrmlFromUrl(theBrowser, url,node,eventIn).doEvent();
    }

    /** Process a browser.loadURL request. */
    public void postLoadURL(
        VRML97CommonBrowser browser, String url[], String param[]
    ) {
        new EAILoadURL(browser, url,param).doEvent();
    }

    /** Process a node realization request */
    public void postRealizeNode(VRMLNodeType nodeToRealize) {
        if (nodeToRealize==null)
            return;
        synchronized(eventLock) {
            if (!isNodeRealized(nodeToRealize)) {
                realizingNodes.add(nodeToRealize);
                processEvent(new SAIRealizeNode(nodeToRealize,this));
            }
        }
    }

    /** Process a browser.deleteRoute request.
      * The EAI specification doesn't specifically state that this should be
      * buffered, but it is a world state modification. */
    public void postRemoveRoute(
        VRML97CommonBrowser browser, BrowserCore core, VRMLNodeType from,
        String eventOut, VRMLNodeType to, String eventIn
    ) {
        processEvent(new EAIRemoveRoute(browser,core,from,eventOut,to,eventIn));
    }

    /** Process a browser.replaceWorld request */
    public void postReplaceWorld(
        VRML97CommonBrowser browser, VRMLNodeType value[]
    ) {
        new EAIReplaceWorld(browser, value).doEvent();
    }

    /** Process an addition to the event queue.
      * This will either add the event to the queue, or execute it immediately
      * depending on the queueing state */
    public void processEvent(ExternalEvent event) {
        synchronized (eventLock) {
            if (queueing) {
                eventQueue.add(event);
                if (event.isConglomerating())
                    conglomerativeEvents.add(event);
                else
                    conglomerativeEvents.remove(event);
            } else {
                waitingQueue.add(event);
            }
        }
    }

    /***********************************************************************
     * Methods for ExternalView
     ***********************************************************************/

    /**
     * Process all waiting events.
     *  @see org.xj3d.core.eventmodel.ExternalView#processEvents
     */
    public void processEvents() {
        //System.out.println("Processing events at "+System.currentTimeMillis());
        /* Lock the event system just long enough
          to get the to event list.  This is to deal better with
          having multiple tight event loops, but since its only
          swapping buffers. */
        synchronized (eventLock) {
            // Just swap the buffers, since the working buffer gets
            // cleared at the end of this method.
            ObjectArray temp=waitingQueue;
            waitingQueue=eventsInProcessing;
            eventsInProcessing=temp;
        }
        //processEvents should be the only code to use eventsInProcessing.
        int i;
        for (i=0;i<eventsInProcessing.size();i++) {
            ExternalEvent event=(ExternalEvent)(eventsInProcessing.get(i));
            try {
                event.doEvent();
            } catch (Exception e) {
                String msg;

                if (event instanceof X3DField) {
                    X3DField field = (X3DField) event;
                    X3DFieldDefinition def = field.getDefinition();

                    msg = "Error encountered processing buffered event for field named: "+
                        def.getName() + "of type: " + def.getFieldTypeString() + ".";
                } else {
                    msg = "Error encountered processing buffered event of type "+event.getClass().getName()+".";
                }

                errorReporter.errorReport(msg,e);
            }
        }
        eventsInProcessing.clear();
    }

    /** Change the ErrorReporter this instance is using
     *  @see org.xj3d.core.eventmodel#setErrorReporter
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter=reporter;
    }

    /***********************************************************************
     * Methods for NodeRealizationProcessor
     ***********************************************************************/

    public boolean isNodeRealized(VRMLNodeType node) {
        return node.isSetupFinished()||realizingNodes.contains(node);
    }

    public void markNodeRealized(VRMLNodeType node) {
        if (!node.isSetupFinished())
            realizingNodes.add(node);
    }

    public void markRealizationComplete(VRMLNodeType node) {
        realizingNodes.remove(node);
    }


}
