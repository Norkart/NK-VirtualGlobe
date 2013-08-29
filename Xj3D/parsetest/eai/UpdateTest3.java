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
 *   <TITLE>Begin/EndUpdate Test 3</TITLE>
 *   A test to make sure that route maintenance works with begin/endUpdate.
 *   Naturally, this test depends on having much of the rest of the API
 *   working.  Like so many other tests, this one has an ackward structure
 *   to avoid calling replaceWorld.
 */

public class UpdateTest3 implements vrml.eai.event.BrowserListener {
  public static void main(String[] args) {
    new UpdateTest3().performTest();
  }

  Browser browser;

  public void performTest() {
    browser=TestFactory.getBrowser();

    browser.addBrowserListener(this);
    browser.loadURL(new String[]{"file:root.wrl"},new String[0]);
  }

  public void performStep2() {
    System.out.println("Starting step 2.");
    /* Test two */
    /* This body is confusing because it was initially copied from another
     * test to avoid writing everything from scratch. */
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

    Node shapeB[]=browser.createVrmlFromString(
      "Transform{}"
    );
    Node shapeA[]=browser.createVrmlFromString(
      "Transform{}"
    );
    Node transforms[]=browser.createVrmlFromString(
      "Transform {translation -1 0 0} Transform {translation 1 0 0}");
    ((EventInMFNode)(nodes[0].getEventIn("children"))).setValue(transforms);
    ((EventInMFNode)(transforms[0].getEventIn("children"))).setValue(shapeA);
    ((EventInMFNode)(transforms[1].getEventIn("children"))).setValue(shapeB);
    ((EventOut)(shapeA[0].getEventOut("translation"))).addVrmlEventListener(new GenericFieldListener("Transform A"));
    ((EventOut)(shapeB[0].getEventOut("translation"))).addVrmlEventListener(new GenericFieldListener("Transform B"));
    int x;
    for (x=0; x<10; x++)
      browser.addRoute(shapeA[0],"translation",shapeB[0],"translation");

    ((EventInSFVec3f)(shapeA[0].getEventIn("translation"))).setValue(new float[]{0.5f,0.5f,0.5f});

    browser.deleteRoute(shapeA[0],"translation_changed",shapeB[0],"set_translation");
    ((EventInSFVec3f)(shapeA[0].getEventIn("translation"))).setValue(new float[]{0.3f,0.3f,0.3f});
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
