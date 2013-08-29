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
 * A test to determine if INITIALIZE/SHUTDOWN broadcasts are being generated.
 * This test also requires createVrmlFromString,  and replaceWorld function.
 */

public class ListenerTest {
  public static void main(String[] args) {
    Browser browser=TestFactory.getBrowser();

    browser.addBrowserListener(new GenericBrowserListener());

    browser.replaceWorld(TestFactory.getTestNodes(browser,1));

    // Using test 3 because test 2 currently doesn't work.
    browser.replaceWorld(TestFactory.getTestNodes(browser,3));

    System.out.println("Done.");
  }
}
