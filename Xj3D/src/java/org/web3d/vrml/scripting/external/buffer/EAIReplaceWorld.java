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

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;

/** EAIReplaceWorld is the ExternalEvent form for the Browser.replaceWorld method.
  * Like the other ExternalEvent versions of Browser methods, this version
  * suffers from a timing problem in the parameter validation section. */

class EAIReplaceWorld implements ExternalEvent {
    /** The CommonBrowser to receive the event */
    VRML97CommonBrowser theBrowser;

    /** The nodes to make the new world */
    VRMLNodeType theNodes[];

    /** Create a new replaceWorld event for later execution
      * @param browser The CommonBrowser to perform the event
      * @param nodes The nodes to make the new world
      */
    EAIReplaceWorld(
        VRML97CommonBrowser browser, VRMLNodeType nodes[]
    ) {
        theBrowser=browser;
        theNodes=new VRMLNodeType[nodes.length];
        System.arraycopy(nodes,0,theNodes,0,nodes.length);
    }

    /** Perform the event actions.
      * Replace the world with the specified nodes.
      */
    public void doEvent() {
        theBrowser.replaceWorld(theNodes);
    }

    /**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
	 */
	public boolean isConglomerating() {
		return false;
	}

}

