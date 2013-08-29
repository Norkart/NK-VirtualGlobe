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

/** EAIAddRoute is the ExternalEvent form of the Browser.addRoute call.
  * Like the other queued Browser calls, this event suffers from a timing
  * problem about when the parameter checking occurs in queued form.
  * <P>
  * Both the CommonBrowser and BrowserCore instances are stored because it
  * it expected that the global execution space won't always be the same
  * when the event is actually processed.
  */

class EAIAddRoute implements ExternalEvent {

    /** The CommonBrowser to receive the call */
    VRML97CommonBrowser theBrowser;

    /** The execution space for the call */
    BrowserCore theCore;

    /** The node to route from */
    VRMLNodeType fromNode;

    /** The field to route from */
    String eventOutName;

    /** The node to route to */
    VRMLNodeType toNode;

    /** The field to route to */
    String eventInName;

    /** Construct an 'Add Route' event record.  This is a potentially
      * queued VRML97CommonBrowser.addRoute invocation.
      * 
      * @param aBrowser The CommonBrowser to call.
      * @param aCore The BrowserCore The core to get an execution space from
      * @param from The from node.
      * @param eventOut The from field.
      * @param to The to node.
      * @param eventIn The to field.
      */
    EAIAddRoute(
        VRML97CommonBrowser aBrowser, BrowserCore aCore, VRMLNodeType from,
        String eventOut, VRMLNodeType to, String eventIn
    ) {
        theBrowser=aBrowser;
        theCore=aCore;
        fromNode=from;
        eventOutName=eventOut;
        toNode=to;
        eventInName=eventIn;
    }

    /** Perform the addRoute */
    public void doEvent() {
        try {
            theBrowser.addRoute(
                theCore.getWorldExecutionSpace(), fromNode,
                eventOutName, toNode, eventInName
            );
        } catch (InvalidFieldException ife) {
            /** Which field is incorrect? */
            throw new RuntimeException("Error processing addRoute");
        }
    }

    /**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
	 */
	public boolean isConglomerating() {
		return false;
	}    
    
}

