/*****************************************************************************
 * Copyright North Dakota State University, 2005
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import org.web3d.x3d.sai.BrowserEvent;
import org.web3d.x3d.sai.BrowserListener;
import org.web3d.x3d.sai.ExternalBrowser;

/**
 * Simple listener to wait for the browser to generate an 
 * initialization event rather than putting demo code in a callback.
 * Create an instance of this class, call replaceWorld, and then
 * call waitForInit().
 * */
public class SAIWaitForBrowserInit implements BrowserListener {

	ExternalBrowser theBrowser;
	
	public SAIWaitForBrowserInit(ExternalBrowser b) {
		theBrowser=b;
		theBrowser.addBrowserListener(this);
	}
	
	boolean sawEvent;
	
	/** (non-Javadoc)
	 * @see vrml.eai.event.BrowserListener#browserChanged(vrml.eai.event.BrowserEvent)
	 */
	public void browserChanged(BrowserEvent evt) {
		// TODO Auto-generated method stub
		synchronized (this) {
			sawEvent=true;
			theBrowser.removeBrowserListener(this);
			notifyAll();
		}
	}

	/** wait() until the initialize event is sent, or return
	 *  immediately if it was already seen.
	 */
	public void waitForInit() {
		synchronized (this) {
			while (!sawEvent) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


}
