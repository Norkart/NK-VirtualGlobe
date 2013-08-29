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
 * Test case for processing of createVrmlFromURL calls.
 * 
 */

public class CreateVrmlFromURLTest {

  static String file1="http://vcell.ndsu.edu/client/WRL/lab_new.wrl";
  static String file2="http://vcell.ndsu.edu/client/WRL/lab_guy.wrl";
	  //"http://vcell.ndsu.edu/client/WRL/Door.wrl";
  static String file3="http://vcell.ndsu.edu/client/WRL/BlueBall.wrl";

  public static void main(String[] args) {

    if (args.length>0)
        file1=args[0];
    if (args.length>1)
        file2=args[1];
    if (args.length>2)
        file3=args[2];
    System.out.println("File 1: "+file1);
    System.out.println("File 2: "+file2);
    System.out.println("File 3: "+file3);
    Browser browser=TestFactory.getBrowser();

    browser.addBrowserListener(new GenericBrowserListener());
    WaitForBrowserInit waiter=new WaitForBrowserInit(browser);
    Node nodes[]=browser.createVrmlFromString(
      "DEF Root Transform {}"
    );
    
    System.out.println("Number of nodes from create:"+nodes.length);
    System.out.println("Replacing world...");
    browser.replaceWorld(nodes);
    System.out.println("World replaced.");
    waiter.waitForInit();

    Node base[]=browser.createVrmlFromString(
        "Transform { translation -5 0 0} Transform { translation 5 0 0} Transform { translation 0 0 0}"
    );
    base[0].getEventOut("children").addVrmlEventListener(new GenericFieldListener("Base0"));
    base[1].getEventOut("children").addVrmlEventListener(new GenericFieldListener("Base1"));
    base[2].getEventOut("children").addVrmlEventListener(new GenericFieldListener("Base2"));
    ((EventInMFNode)(nodes[0].getEventIn("children"))).setValue(base);
    browser.createVrmlFromURL(new String[]{file1},base[0],"children");
    browser.createVrmlFromURL(new String[]{file2},base[1],"children");
    //browser.createVrmlFromURL(new String[]{file2},base[1],"children");
    browser.createVrmlFromURL(new String[]{file3},base[2],"children");
  }
}
