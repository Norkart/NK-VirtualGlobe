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

// Standard imports
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;

/**
 * EAILoadURL is the ExternalEvent form of the Browser.LoadURL method.
 * Like the other ExternalEvent forms of Browser requests, this method suffers
 * from timing problems in the parameter checking.
 */
class EAILoadURL implements ExternalEvent {

    /** The CommonBrowser to receive the call */
    VRML97CommonBrowser theBrowser;

    /** The URLs to load */
    String[] urls;

    /** The parameters for the URLs */
    HashMap params;

    /** Construct an event record for later execution
      * @param browser The browser to receive the call
      * @param url The URLs for the call (copied)
      * @param parameters The paramters for the URLs (copied)
      */
    EAILoadURL(VRML97CommonBrowser browser, String[] url, String[] parameters) {
        theBrowser=browser;
        urls=new String[url.length];
        System.arraycopy(url,0,urls,0,url.length);

        // parse the parameter list and place the values into the map
        params = new HashMap();
        int num_params = (parameters == null) ? 0 : parameters.length;
        for(int i = 0; i < num_params; i++) {
            int eq_idx = parameters[i].indexOf('=');
            String key = parameters[i].substring(0, eq_idx);
            String value = parameters[i].substring(eq_idx + 1);
            params.put(key, value);
        }
    }

    /** Execute the LoadURL call */
    public void doEvent() {
        theBrowser.loadURL(urls,params);
    }

    /**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
	 */
	public boolean isConglomerating() {
		return false;
	}    

}

