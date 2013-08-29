package org.web3d.vrml.scripting.external.sai;

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
import org.web3d.vrml.scripting.external.buffer.BaseExternalEventAdapter;
import org.web3d.vrml.scripting.external.buffer.ExternalOutputBufferReclaimer;
import org.web3d.vrml.scripting.external.buffer.ExternalEventAdapter;
import org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer;

import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;

/**
 * BufferedSAIEventAdapter implements the 'broadcast in seperate thread'
 * implementation of the ExternalEventAdapter interface.  In other words,
 * the end user cannot slow down the pace of the simulation by delays in
 * the callback processing.
 * <P>
 * SAIEventAdapter is an adapter between the underlying event model and
 * the listener callbacks.
 * The purpose of having this interface is to support having the event
 * changed callbacks occuring in the same thread as the event model, or
 * occuring outside the event model's thread.
 * <P>
*/
class BufferedSAIEventAdapter extends BaseExternalEventAdapter implements ExternalEventAdapter,
    VRMLNodeListener, ExternalOutputBufferReclaimer {
    /** The factory to construct the eventOut's with */
    SAIFieldFactory theFieldFactory;

    /** The thread pool to send events to. */
    SAIEventAdapterThreadPool thePool;

    /** Basic constructor. */
    BufferedSAIEventAdapter(SAIFieldFactory aFieldFactory,
        VRMLNodeType aNode, SAIEventAdapterThreadPool aPool,
		VRMLClock clock
    ) {
    	super(aNode,clock);
        theFieldFactory=aFieldFactory;
        thePool=aPool;
    }

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
            Object userData=null;
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
                    buffer=
                        (ExternalOutputBuffer)(theFieldFactory.getStoredField(
                                parentNode,fieldID,parentNode.getFieldDeclaration(fieldID).getName(),false
                        ));
                }
                X3DFieldEvent event;
                event=new X3DFieldEvent(buffer, timestamp, userData);
                X3DFieldEventListener who=(X3DFieldEventListener)(e.nextElement());
                thePool.sendEvent(who,event,buffer,fieldID,this);
            }            
        }
    }

	/**
	 * Reclaim the event out buffer that the broadcast system finished with
	 * 
	 * @param buffer
	 *            The buffer that finished
	 * @param fieldID
	 *            We send the broadcast system the fieldID as the shipping tag
	 */
	public void reclaimEventOutBuffer(ExternalOutputBuffer buffer, int fieldID) {
		buffer.reset();
		synchronized (this) {
			buffers[fieldID].addLast(buffer);
		}
	}
    
    
}

