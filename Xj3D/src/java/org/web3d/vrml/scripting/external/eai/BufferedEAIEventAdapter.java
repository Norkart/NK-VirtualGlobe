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

// Standard imports
import java.util.Enumeration;
import java.util.Vector;

// Other imports
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

import vrml.eai.event.VrmlEvent;
import vrml.eai.event.VrmlEventListener;
import vrml.eai.field.EventOut;

/**
 * BufferedEAIEventAdapter implements the 'broadcast in seperate thread'
 * implementation of the ExternalEventAdapter interface.  In other words,
 * the end user cannot slow down the pace of the simulation by delays in
 * the callback processing.
 * <P>
 * ExternalEventAdapter is an adapter between the underlying event model and
 * the vrml.eai.EventOut callbacks.
 * The purpose of having this interface is to support having the event
 * changed callbacks occuring in the same thread as the event model, or
 * occuring outside the event model's thread.
 * <P>
*/
class BufferedEAIEventAdapter extends BaseExternalEventAdapter implements ExternalEventAdapter, VRMLNodeListener,
    ExternalOutputBufferReclaimer
{
    /** The VRMLNodeType from the underlying implementation that we broadcast
      * for. */
    //VRMLNodeType parentNode;

    /** The factory to construct the eventOut's with */
    EAIFieldFactory theFieldFactory;

    /** The mapping of fieldID to listener lists */
    //Vector listenerArray[];

    /** The queue of eventOut buffers for each field with a listener. */
    //LinkedList buffers[];

    /** The thread pool to send events to. */
    EAIEventAdapterThreadPool thePool;

    /** The clock for producing consistent time stamps */
    //VRMLClock timeClock;
    
    /** Basic constructor. */
    BufferedEAIEventAdapter(
        EAIFieldFactory aFieldFactory, VRMLNodeType aNode, 
        EAIEventAdapterThreadPool aPool,
		VRMLClock clock
    ) {
    	super(aNode,clock);
        theFieldFactory=aFieldFactory;
        thePool=aPool;
    }

    /** Lazy initialization of listener array.
     * Listener vectors will be constructed, and the array in which they are
     * stored grown to accomodate, as they are requested.
     * This method performs a similar function for the output buffers.
     */
/*    synchronized Vector checkListeners(int fieldID) {
        if (fieldID<0)
            throw new IllegalArgumentException("Invalid field ID");
        if (listenerArray!=null) {
            // Both listenerArray and buffers are initialized together.
            if (fieldID>=listenerArray.length) {
                // This should probably be the next power of two for speed
                Vector newListenerArray[]=new Vector[fieldID+1];
                LinkedList newBuffers[]=new LinkedList[fieldID+1];
                System.arraycopy(listenerArray,0,newListenerArray,0,
                    listenerArray.length
                );
                System.arraycopy(buffers,0,newBuffers,0,buffers.length);
                newListenerArray[fieldID]=new Vector();
                newBuffers[fieldID]=new LinkedList();
                listenerArray=newListenerArray;
                buffers=newBuffers;
            } else if (listenerArray[fieldID]==null) {
                listenerArray[fieldID]=new Vector();
                buffers[fieldID]=new LinkedList();
            }
        } else {
            // Both listenerArray and buffers start as null together.
            Vector newListenerArray[]=new Vector[fieldID+1];
            LinkedList newBuffers[]=new LinkedList[fieldID+1];
            newListenerArray[fieldID]=new Vector();
            newBuffers[fieldID]=new LinkedList();
            listenerArray=newListenerArray;
            buffers=newBuffers;
        }
        return listenerArray[fieldID];
    }
*/

    /** The underlying Xj3D renderer says that a field has changed
      * on our node.  Broadcast the appropriate events.
      * @param fieldID Which field changed
      */
/*    public void fieldChanged(int fieldID) {
        generateBroadcast(fieldID,timeClock.getTime());
    }
*/

    /** Broadcast an eventOutChanged event for a given field
     * Modifications to the buffers arrays are synchronized
     * so this needs to be too for safety.
     * @param fieldID The fieldID which changed
     * @param timestamp The timestamp to use
     */
    public synchronized void generateBroadcast(int fieldID, double timestamp) {
        Vector listeners=getListeners(fieldID);
        if (listeners!=null) {
            // Everyone gets the same userData, but
            // everything else gets allocated for each listener.
            Object userData;
            try {
                userData=parentNode.getUserData(fieldID);
            } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
                throw new RuntimeException(
                    "Unexpectedly bad field ID "+fieldID+" for "+parentNode
                    +" in event broadcast."
                );
            }
            /* The clone call is present to avoid problems if someone removes a
             * listener during processing. */
            Enumeration e=((Vector)listeners.clone()).elements();
            while (e.hasMoreElements()) {
                /* Its easier to splurge on memory then to try and figure 
                 * out when all of the listeners are done with a buffer */
                ExternalOutputBuffer buffer;
                // Check buffers for buffer element before allocating a new one.
                if (buffers[fieldID].size()!=0) {
                    buffer=(ExternalOutputBuffer)(buffers[fieldID].removeFirst());
                    buffer.initialize(parentNode,fieldID);
                    buffer.loadOutputValue();
                } else {
                    // Fall back on field factory to generate new event.
                    buffer=
                        (ExternalOutputBuffer)(theFieldFactory.getStoredEventOut(
                            parentNode,fieldID,parentNode.getFieldDeclaration(fieldID).getName()
                        ));
                }
                VrmlEvent event;
                event=new VrmlEvent((EventOut)buffer, timestamp, userData);
                VrmlEventListener who=(VrmlEventListener)(e.nextElement());
                thePool.sendEvent(who,event,buffer,fieldID,this);
            }
        }
    }

    /** Get the listeners for a given fieldID.
     * This doesn't perform initialization, and returns null for elements
     * which don't exist yet.
     * @param fieldID The ID of the field.
     */
/*    synchronized Vector getListeners(int fieldID) {
        if (fieldID<0)
            throw new IllegalArgumentException("Invalid field ID");
        if ((listenerArray==null) || (listenerArray.length<=fieldID))
            return null;
        else return listenerArray[fieldID];
    }
*/
    
    /** Reclaim the event out buffer that the broadcast system finished with
      * @param buffer The buffer that finished
      * @param fieldID We send the broadcast system the fieldID as the
      *        shipping tag
      */
    public void reclaimEventOutBuffer(
        ExternalOutputBuffer buffer, int fieldID
    ) {
        buffer.reset();
        synchronized (this) {
            buffers[fieldID].addLast(buffer);
        }
    }

    /** Remove a listener for one of the fields of this node.
      * Will also prune the buffer size if all listeners are removed.
      */
/*    public void removeListener(int fieldID, Object who) {
        Vector listeners=getListeners(fieldID);
        if (listeners!=null) {
            listeners.removeElement(who);
            // If no one is listening, there's no point in keeping the buffer.
            if (listeners.size()==0)
                // Modifications to the buffers arrays are synchronized
                // so this needs to be too for safety.
                synchronized (this) {
                    buffers[fieldID].clear();
                }
        }
    }
*/
        
}

