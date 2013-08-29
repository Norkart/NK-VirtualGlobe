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


/**
 *   ReplaceWorld is a basic EAI test program.  The sequence of operations is
 *   <UL>
 *   <LI>  setBrowserFactory, getBrowserComponent, getBrowser, etc.
 *   <LI>  createVrmlFromString
 *   <LI>  replaceWorld with the newly created nodes
 *   <LI>  Expect to see a sphere on the display
 *   </UL>
 */

public class BasicReplaceWorld {
  public static void main(String[] args) {
    Browser browser=TestFactory.getBrowser();

    Node nodes[]=browser.createVrmlFromString(
      "     Shape {\n"+
      "appearance Appearance {\n"+
      " material Material {\n"+
      "  emissiveColor 0 0 1\n"+
      " }\n"+
      "}\n"+
      "geometry Sphere {radius 1}\n"+
      "}\n"+
      "Viewpoint {}\n"
    );
    if (nodes.length==2)
      System.out.println("Correct number of nodes produced.");
    else
      System.out.println("Expected two nodes but got :"+nodes.length);
    browser.replaceWorld(nodes);
    System.out.println("Done.");
  }
}
