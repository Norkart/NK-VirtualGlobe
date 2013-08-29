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

import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;

/** EAICreateVrmlFromURL is the ExternalEvent version of Browser.createVrmlFromURL.
  * The constructors should be doing parameter checking, and throwing
  * errors, but the current structure makes that somewhat more difficult.
  * */
class EAICreateVrmlFromUrl implements ExternalEvent {

    /** The CommonBrowser which will receive the call. */
    VRML97CommonBrowser theBrowser;

    /** The URLs to create geometry from */
    String urls[];

    /** The Node to receive the results of createVrmlFromURL */
    VRMLNodeType theTarget;

    /** The eventIn to receive the results of createVrmlFromURL */
    String theEventIn;

    /** Basic constructor.
      * Really, this should take a fieldIndex instead of eventIn name, but
      * the BrowserCore isn't set up to do that yet. 
      *
      * @param browser The browser to call
      * @param url The URLs to create geometry from
      * @param target The Node to receive the event
      * @param eventIn The field to receive the event
      */
    EAICreateVrmlFromUrl(
        VRML97CommonBrowser browser, String url[], VRMLNodeType target, 
        String eventIn
    ) {
        theBrowser=browser;
        urls=new String[url.length];
        System.arraycopy(url,0,urls,0,url.length);
        theTarget=target;
        theEventIn=eventIn;
    }

    /** Do the createVrmlFromURL */
    public void doEvent() {
        try {
            theBrowser.createVrmlFromURL(urls,theTarget,theEventIn);
        } catch (InvalidFieldException ife) {
            throw new RuntimeException("Unknown target for createVrmlFromURL");
        }
    }

    /**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
	 */
	public boolean isConglomerating() {
		return false;
	}    
    
}

