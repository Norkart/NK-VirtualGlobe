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
import vrml.eai.event.BrowserEvent;


/**
 *   <TITLE>CreateFromString Variant 2</TITLE>
 *   A test to try out createVrmlFromString, and try constructing a scene
 *   graph from components using eventIn's.
 *   The variant here is that it is necessary to call loadURL, and then
 *   wait for the INITIALIZED event. 
 *   <P>
 *   The sequence of operations is
 *   1.  setBrowserFactory, getBrowserComponent, getBrowser, etc.
 *   2.  loadURL
 *   3.  createVrmlFromString
 *   4.  Add new elements to the scene graph
 *   5.  Expect to see a sphere on the display
 */

public class CreateFromString2 implements vrml.eai.event.BrowserListener {
  public static void main(String[] args) {
    new CreateFromString2().performTest();
  }

  Browser browser;

  public void performTest() {
    browser=TestFactory.getBrowser();

    browser.addBrowserListener(this);
    browser.addBrowserListener(new GenericBrowserListener());
    browser.loadURL(new String[]{"file:root.wrl"},new String[0]);
  }

  public void performStep2() {
    /* Test two */
    Node nullResult[]=browser.createVrmlFromString("");
    if (nullResult==null)
      System.out.println("Returned a null array for no nodes.");
    else if (nullResult.length!=0)
      System.out.println("Returned nodes for the empty string.");
    else
      System.out.println("Returned the correct result for empty string.");
    try {
      if (browser.getNode("ROOT")==null)
        System.out.println("FAIL:  Failed to add to namespace.");
      System.out.println("PASS:  Got the root node.");
    } catch (vrml.eai.InvalidNodeException ine) {
      System.out.println("FAIL:  Incorrectly threw invalid node exception.");
    }
    Node nodes[]=new Node[1];
    nodes[0]=browser.getNode("ROOT");

    /* Test three */
    Node levelOne[]=browser.createVrmlFromString(
      "Viewpoint {}\n"+
      "Group {}\n"
    );
    Node levelTwo[]=browser.createVrmlFromString(
      "Transform {\n"+
      "  children [\n"+
      "    Shape {\n"+
      "      appearance Appearance {\n"+
      "        material Material {\n"+
      "          emissiveColor 1 0 0\n"+
      "        }\n"+
      "      }\n"+
      "    geometry Sphere {radius 1}\n"+
      "    }\n"+
      "  ]\n"+
      "}"+
      "Shape {\n"+
      "  appearance Appearance {\n"+
      "    material Material {\n"+
      "      emissiveColor 0 0 1\n"+
      "    }\n"+
      "  }\n"+
      "  geometry Sphere {radius 1}\n"+
      "}"
    );
    ((EventInMFNode)nodes[0].getEventIn("set_children")).setValue(levelOne);
    ((EventInMFNode)levelOne[1].getEventIn("set_children")).setValue(levelTwo);
    ((EventInSFVec3f)levelTwo[0].getEventIn("translation")).setValue(new float[]{2.0f,0.0f,0.0f});
  }

  public void browserChanged(BrowserEvent event) {
    if (event.getSource()==browser && event.getID()==BrowserEvent.INITIALIZED)
      performStep2();
  }
}
