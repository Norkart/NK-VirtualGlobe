package org.web3d.vrml.scripting.external.buffer;

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

import org.web3d.browser.BrowserCore;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;

/** EAIRemoveRoute is the ExternalEvent form of the Browser.deleteRoute request.
  * As with the other ExternalEvent forms of Browser methods, the parameter
  * checking for this object happens fairly late in execution. */

class EAIRemoveRoute implements ExternalEvent {

    /** The CommonBrowser to receive the call */
    VRML97CommonBrowser theBrowser;

    /** The BrowserCore is needed for the execution context of the call */
    BrowserCore theCore;

    /** The node routed from */
    VRMLNodeType fromNode;

    /** The field routed from */
    String fromEventName;
  
    /** The node routed to */
    VRMLNodeType toNode;
    
    /** The field routed to */
    String toEventName;

    /** Basic constructor.
     *  @param aBrowser The CommonBrowser to receive the call
     *  @param aCore The BrowserCore for the execution context
     *  @param from The node routed from
     *  @param fromEvent The field routed from
     *  @param to The node routed to
     *  @param toEvent The field routed to
     */
    EAIRemoveRoute(
        VRML97CommonBrowser aBrowser, BrowserCore aCore,
        VRMLNodeType from, String fromEvent, VRMLNodeType to, String toEvent
    ) {
        theBrowser=aBrowser;
        theCore=aCore;
        fromNode=from;
        fromEventName=fromEvent;
        toNode=to;
        toEventName=toEvent;
    }

    /** Perform the event (remove the route) */
    public void doEvent() {
        try {
            theBrowser.deleteRoute(
                theCore.getWorldExecutionSpace(), fromNode,
                fromEventName, toNode, toEventName
            );
        } catch (InvalidFieldException ife) {
            throw new RuntimeException("Error deleting route.");
        }
    }

    /**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
	 */
	public boolean isConglomerating() {
		return false;
	}

}

