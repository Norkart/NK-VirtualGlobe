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
 *   Currently hardwired to depend on two samples from the
 *   geometry samples.
 *   <P>
 */

public class CreateFromURL {

  static String file1="file:../geometry/cone.wrl";

  public static void main(String[] args) {

    if (args.length>0)
        file1=args[0];
    System.out.println("File 1: "+file1);
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
        "Transform { translation 0 0 0}"
    );
    ((EventInMFNode)(nodes[0].getEventIn("children"))).setValue(base);
    browser.createVrmlFromURL(new String[]{file1},base[0],"children");
  }
}
