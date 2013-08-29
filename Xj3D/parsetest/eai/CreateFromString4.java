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
import vrml.eai.Node;
import vrml.eai.field.*;


/**
 *   A test to try out createVrmlFromString, and try constructing a scene
 *   graph from components using eventIn's.  In this variant, we're testing
 *   that routes are recovered when a universe is constructed by calling
 *   createVrmlFromString, and then replaceWorld, so that the route info
 *   has to survive a scene change.
 *   <P>
 *   The sequence of operations is
 *   1.  setBrowserFactory, getBrowserComponent, getBrowser, etc.
 *   2.  createVrmlFromString
 *   3.  replaceWorld with the newly created nodes
 *   4.  Expect to see a sphere on the display
 *   <P>
 *   This test differs from CreateFromString in that its testing a 
 *   timer node to find out if it works.
 */

public class CreateFromString4 {
  public static void main(String[] args) {
    Browser browser=TestFactory.getBrowser();

    browser.addBrowserListener(new GenericBrowserListener());
    /* The root */
    Node nodes[]=browser.createVrmlFromString(
      "DEF Root Transform {}"
    );
    System.out.println("Number of nodes from create:"+nodes.length);

    /* The basic geometry */
    Node levelOne[]=browser.createVrmlFromString(
      "Viewpoint {}\n"+
      "Group {}\n"
    );

    /* The geometry with routes and time dependencies. */
    Node levelTwo[]=browser.createVrmlFromString(
      TestFactory.timerTest
    );
    nodes[0].getEventOut("children").addVrmlEventListener(new GenericFieldListener("nodes[0]"));
    levelOne[1].getEventOut("children").addVrmlEventListener(new GenericFieldListener("levelOne[1]"));
    levelTwo[0].getEventOut("fraction_changed").addVrmlEventListener(new GenericFieldListener("fraction changed"));
    ((EventInMFNode)nodes[0].getEventIn("set_children")).setValue(levelOne);
    ((EventInMFNode)levelOne[1].getEventIn("set_children")).setValue(levelTwo);

    // And now do the scene change.

    System.out.println("Replacing world...");
    browser.replaceWorld(nodes);
    System.out.println("World replaced.");
  }
}
