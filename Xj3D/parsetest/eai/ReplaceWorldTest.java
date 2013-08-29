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

import java.awt.*;

/**
 *   A test of createVrmlFromString and replaceWorld.
 *   This is being used to test some possible scene graph traversal
 *   defects.
 */

public class ReplaceWorldTest {
  public static void main(String[] args) {
    Browser browser=TestFactory.getBrowser();

    browser.addBrowserListener(new GenericBrowserListener());
    /* Test one */
    Node nodes[]=browser.createVrmlFromString(
          "NavigationInfo {\n"+
          "  speed 100.0\n"+
          "  headlight TRUE\n"+
          "  type [\"FLY\", \"ANY\"]\n"+
          "}\n"+
          "Background {\n"+
          "  skyColor [0 .6 .7]\n"+
          "  groundColor [0 .6 .7]\n"+
          "}\n"+
          "Group {}\n"+
          "Transform {\n"+
          "     translation -10 -10 -10\n"+
          "     children\n"+
          "         Shape {\n"+
          "             geometry Sphere {}\n"+
          "         }\n"+
          "}\n"+
          "Viewpoint {\n"+
          "  position -10 -10 -5\n"+
          "  description \"Test1\"\n" +
          "}\n" +
          "Viewpoint {\n"+
          "  position -10 -10 -0\n"+
          "  description \"Test2\"\n" +
          "}\n"
    );
    System.out.println("Number of nodes from create:"+nodes.length);
    System.out.println("Replacing world...");
    browser.replaceWorld(nodes);
    System.out.println("World replaced.");

  }
}
