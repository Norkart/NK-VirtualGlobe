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

import org.web3d.x3d.sai.BrowserEvent;
import org.web3d.x3d.sai.BrowserListener;

/** A generic Browser event listener, which simply generates diagnostic
 *  text when called.  */
public class GenericSAIBrowserListener implements BrowserListener {

  /** Process an event that has occurred in the VRML Browser.
   *  @see org.web3d.x3d.sai.BrowserListener
   */
  public void browserChanged(BrowserEvent event) {
    System.out.println("Browser "+event.getSource()+" has generated an event.");
    switch (event.getID()) {
      case BrowserEvent.INITIALIZED:
        
        System.out.println("INITIALIZED.  The browser has (re)started.");
        break;
      case BrowserEvent.SHUTDOWN:
        System.out.println("SHUTDOWN.  The browser is shutting down or restarting.");
        break;
      case BrowserEvent.URL_ERROR:
        System.out.println("URL_ERROR.  The browser has failed to load a URL.");
        break;
      case BrowserEvent.CONNECTION_ERROR:
        System.out.println("  CONNECTION_ERROR.");
        break;
      case BrowserEvent.LAST_IDENTIFIER:
        System.out.println("The LAST_IDENTIFIER event has occurred.  This should not happen.");
        break;
      default:
        System.out.println("An unknown, presumably browser specific event has occurred.");
    }
  }
}
