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
 *   A test to try out createVrmlFromURL, which depends on
 *   replaceWorld() and createVrmlFromString() working to construct
 *   the basic scene.
 *   <P>
 *   The sequence of operations is
 *   1.  setBrowserFactory, getBrowserComponent, getBrowser, etc.
 *   2.  createVrmlFromString
 *   3.  replaceWorld with the newly created nodes
 *   4.  createVrmlFromURL to add geometry to the scene.
 *   Differs from CreateFromURL in that it attempts two sequential
 *   calls, which may end up simulatenous.
 *   <P>
 */

public class CreateFromURL2 {

  static String file1="file:../geometry/cone.wrl";
  static String file2="file:../geometry/sphere.wrl";

  public static void main(String[] args) {

    if (args.length>0)
        file1=args[0];
    if (args.length>1)
        file2=args[1];
    System.out.println("File 1: "+file1);
    System.out.println("File 2: "+file2);
    Browser browser=TestFactory.getBrowser();

    browser.addBrowserListener(new GenericBrowserListener());

    Node nodes[]=browser.createVrmlFromString(
      "DEF Root Transform {}"
    );
    System.out.println("Number of nodes from create:"+nodes.length);
    System.out.println("Replacing world...");
    browser.replaceWorld(nodes);
    System.out.println("World replaced.");

    Node base[]=browser.createVrmlFromString(
        "Transform { translation -5 0 0} Transform { translation 5 0 0}"
    );
    ((EventInMFNode)(nodes[0].getEventIn("children"))).setValue(base);
    browser.createVrmlFromURL(new String[]{file1},base[0],"children");
    browser.createVrmlFromURL(new String[]{file2},base[1],"children");
  }
}
