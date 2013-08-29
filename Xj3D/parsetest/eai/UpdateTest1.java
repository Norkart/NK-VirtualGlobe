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
import vrml.eai.event.BrowserListener;
import vrml.eai.event.BrowserEvent;

import java.awt.*;

/**
 *   <TITLE>Begin/EndUpdate Test 1</TITLE>
 *   A test to check that event transmission works for both
 *   buffered and unbuffered mode.
 *   The test is structured like it is to avoid using replaceWorld,
 *   which may not be working.
 */

public class UpdateTest1 implements vrml.eai.event.BrowserListener {
  public static void main(String[] args) {
    new UpdateTest1().performTest();
  }

  Browser browser;

  public void performTest() {
    browser=TestFactory.getBrowser();

    browser.addBrowserListener(new GenericBrowserListener());
    browser.addBrowserListener(this);
    browser.addBrowserListener(new GenericBrowserListener());
    browser.loadURL(new String[]{"file:root.wrl"},new String[0]);
  }

  public void performStep2() {
    System.out.println("Starting step 2.");
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
    System.out.println("Regular update.");
    browser.beginUpdate();
    ((EventOut)nodes[0].getEventOut("children_changed")).addVrmlEventListener(new GenericFieldListener());
    ((EventInMFNode)nodes[0].getEventIn("set_children")).setValue(levelOne);
    ((EventOut)levelOne[1].getEventOut("children_changed")).addVrmlEventListener(new GenericFieldListener());
    ((EventInMFNode)levelOne[1].getEventIn("set_children")).setValue(levelTwo);
    browser.endUpdate();
    ((EventOut)levelTwo[0].getEventOut("translation")).addVrmlEventListener(new GenericFieldListener());
    ((EventInSFVec3f)levelTwo[0].getEventIn("translation")).setValue(new float[]{2.0f,0.0f,0.0f});
    System.out.println("Empty update.");
    browser.beginUpdate();
    browser.endUpdate();
  }

  public void browserChanged(BrowserEvent event) {
    System.out.println("Processing a browser event.");
    if (event.getSource()==browser && event.getID()==BrowserEvent.INITIALIZED)
      performStep2();
    else {
      System.out.println("Unexected browser event.");
      System.out.println("EventID:"+event.getID());
    }
  }
}
