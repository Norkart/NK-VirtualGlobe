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

import vrml.eai.Browser;
import vrml.eai.event.BrowserEvent;
import vrml.eai.event.BrowserListener;


/**
 *   LoadURL is a basic EAI test program.  The sequence of operations is
 *   <UL>
 *   <LI>  setBrowserFactory, getBrowserComponent, getBrowser, etc.
 *   <LI>  LoadURL
 *   <LI>  LoadURL
 *   </UL>
 *   This is more of a potential stress test to make sure that things aren't
 *   getting left between worlds.
 */

public class LoadURL {
  public static int initialLoopCount=2;
  public static String URL1="file:../geometry/cone.wrl";
  public static String URL2="file:../geometry/bad.wrl";

  public static void main(String[] args) {
    if (args.length>0) {
      try {
        initialLoopCount=Integer.parseInt(args[0]);
      } catch (NumberFormatException infe) {
        System.err.println("Invalid loop count.  Using "+initialLoopCount);
      }
    }
    if (args.length>1)
      URL1=args[1];
    if (args.length>2)
      URL2=args[2];
    System.out.println("Performing "+initialLoopCount+" loops.");
    Browser browser=TestFactory.getBrowser();

    // Attach diagnostic 
    browser.addBrowserListener(new GenericBrowserListener());

    // Attach the test listener
    browser.addBrowserListener(new BrowserListener() {
      int loopCount=initialLoopCount;

      /** Respond to browser changed events by loading the next
       *  repetition */
      public void browserChanged(BrowserEvent event) {
        if (loopCount>0 && event.getID()==BrowserEvent.INITIALIZED) {
          loopCount--;
          event.getSource().loadURL(new String[]{URL2},null);
        } else if (loopCount>0 && event.getID()==BrowserEvent.URL_ERROR) {
          loopCount--;
          event.getSource().loadURL(new String[]{URL2},null);
        }
      }
    });

    // Load the first URL to get this test underway.
    browser.loadURL(new String[]{URL1},null);
  }

}
