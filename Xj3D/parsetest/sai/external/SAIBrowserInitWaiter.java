/*****************************************************************************
 * Copyright North Dakota State University, 2004
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


/**
 * Simple utility to allow waiting for the browserInitialized
 * event rather than putting code in a browser listener.
 */
public class SAIBrowserInitWaiter implements BrowserListener {

    boolean browserInitialised;
    
    public void waitForInit() throws InterruptedException {
        synchronized (this) {
            while (!browserInitialised)
                wait();
        }
    }
    
    /** * @see org.web3d.x3d.sai.BrowserListener#browserChanged(org.web3d.x3d.sai.BrowserEvent)  */
    public void browserChanged(BrowserEvent evt) {
        synchronized (this) {
            browserInitialised=true;
            notifyAll();
        }
    }

}
