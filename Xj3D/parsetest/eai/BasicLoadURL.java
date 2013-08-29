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


/**
 *   BaseicLoadURL is a basic EAI test program.  The sequence of operations is
 *   <UL>
 *   <LI>  setBrowserFactory, getBrowserComponent, getBrowser, etc.
 *   <LI>  LoadURL
 *   </UL>
 *   Really all this is for is testing without having to deal with
 *   DIYBrowser.
 */

public class BasicLoadURL {
  public static String URL1="file:../geometry/cone.wrl";

  public static void main(String[] args) {
    if (args.length>0)
      URL1=args[0];
    Browser browser=TestFactory.getBrowser();

    // Attach diagnostic 
    browser.addBrowserListener(new GenericBrowserListener());

    // Load the first URL to get this test underway.
    browser.loadURL(new String[]{URL1},new String[]{""});
  }

}
