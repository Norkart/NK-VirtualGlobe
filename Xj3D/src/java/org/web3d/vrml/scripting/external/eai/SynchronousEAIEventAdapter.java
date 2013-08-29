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

/**
 * SynchronousEAIEventAdapter implements the 'broadcast in thread'
 * implementation of the ExternalEventAdapter interface.
 * <P>
 * ExternalEventAdapter is an adapter between the underlying event model and
 * the vrml.eai.EventOut callbacks.
 * The purpose of having this interface is to support having the event
 * changed callbacks occuring in the same thread as the event model, or
 * occuring outside the event model's thread.
 * <P>
*/
class SynchronousEAIEventAdapter extends BaseExternalEventAdapter implements ExternalEventAdapter,
VRMLNodeListener {

    /** The factory to construct the eventOut's with */
    EAIFieldFactory theFieldFactory;

    /** Basic constructor. */
    SynchronousEAIEventAdapter(
        EAIFieldFactory aFieldFactory, VRMLNodeType aNode, VRMLClock clock
    ) {
    	super(aNode,clock);
        theFieldFactory=aFieldFactory;
    }

    /** Broadcast an eventOutChanged event for a given field */
    public void generateBroadcast(int fieldID, double timestamp) {
        Vector listeners=getListeners(fieldID);
        if (listeners!=null) {
            VrmlEvent event;
            try {
                event= new VrmlEvent(
                    theFieldFactory.getEventOut(
                        parentNode,fieldID,parentNode.getFieldDeclaration(fieldID).getName()
                    ),
                    timestamp, parentNode.getUserData(fieldID)
                );
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
                VrmlEventListener who=(VrmlEventListener)(e.nextElement());
                who.eventOutChanged(event);
            }
        }
    }

}

