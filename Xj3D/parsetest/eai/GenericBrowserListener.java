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


import vrml.eai.event.BrowserListener;
import vrml.eai.event.BrowserEvent;

/** A generic Browser event listener, which simply generates diagnostic
 *  text when called.  */
public class GenericBrowserListener implements BrowserListener {

  /** Optional browser tag for distinguishing sources */
  String browserTag;
    
  /** BrowserListener with no tag on messages*/
  public GenericBrowserListener() {
      
  }
  
  /**
     * @param string Tag for messages to this listener.
     */
    public GenericBrowserListener(String string) {
        
        browserTag=string;
    }

/** Process an event that has occurred in the VRML Browser.
   *  @see vrml.eai.event.BrowserListener
   */
  public void browserChanged(BrowserEvent event) {
    String message;
    if (browserTag!=null)
        message=browserTag+":";
    else
        message="";
    //System.out.println("Browser "+event.getSource()+" has generated an event.");
    switch (event.getID()) {
      case BrowserEvent.INITIALIZED:
        
        message=message+"INITIALIZED.  The browser has (re)started.";
        break;
      case BrowserEvent.SHUTDOWN:
        message=message+"SHUTDOWN.  The browser is shutting down or restarting.";
        break;
      case BrowserEvent.URL_ERROR:
        message=message+"URL_ERROR.  The browser has failed to load a URL.";
        break;
      case BrowserEvent.CONNECTION_ERROR:
        message=message+"  CONNECTION_ERROR.";
        break;
      case BrowserEvent.LAST_IDENTIFIER:
        message=message+"The LAST_IDENTIFIER event has occurred.  This should not happen.";
        break;
      default:
        message=message+"An unknown, presumably browser specific event has occurred.";
    }
    System.out.println(message);
  }
}
