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
 *   <TITLE>Set/Get Test 1</TITLE>
 *   Testing of setValue and getValue variants for various fields.
 *   This section tests MFColor type exposed fields.
 *   <P>
 *   This test uses loadURL, thus the actual test occurs in performStep2().
 */

public class SetGetTest1 implements vrml.eai.event.BrowserListener {
  public static void main(String[] args) {
    new SetGetTest1().performTest();
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
    System.out.println("Starting getValue/setValue test.");
    /* Test two */

    float valueA[][]=new float[][]{
      {0.0f,0.1f,0.2f,0.3f,0.4f},
      {1.0f,1.1f,1.2f,1.3f,1.4f},
      {2.0f,2.1f,2.2f,3.3f,4.4f}};
    float valueB[][]=new float[][]{
      {-1.0f,2.0f,-3.0f,4.0f,-5.0f},
      {2.0f,2.0f,2.0f,2.0f,2.0f}};
    float valueC[]=new float[]{
      0.0f,0.1f,0.2f,0.3f,0.4f,
      1.0f,1.1f,1.2f,1.3f,1.4f,
      2.0f,2.1f,2.2f,2.3f,2.4f};
    float valueD[]=new float[]{
      -1.0f,2.0f,-3.0f,4.0f,-5.0f,
      2.0f,2.0f,2.0f,2.0f,2.0f};
    /* Note: a==c, b==d */

    /* Quick test to make sure that the test harness works. */
    if (!TestFactory.compareArray(valueA,valueC))
      System.out.println("Bug in TestFactory.compare.(a!=c)");
    if (!TestFactory.compareArray(valueB,valueD))
      System.out.println("Bug in TestFactory.compare.(b!=d)");
    valueD[2]=-9.0f;
    if (TestFactory.compareArray(valueB,valueD))
      System.out.println("Bug in TestFactory.compare.(b==d)");
    if (TestFactory.compareArray(valueA,valueD))
      System.out.println("Bug in TestFactory.compare.(a==d).");
    valueD[2]=-3.0f;
    if (!TestFactory.compareArray(valueB,valueD))
      System.out.println("Bug in TestFactory.compare.(b!=d)");

    /* And now the actual test */
    /* Nodes with MFColor exposed fields. */
    Node testA[]=browser.createVrmlFromString(
      "ColorInterpolator {}\n"+
      "ColorInterpolator {}\n"+
      //"Background {}\n"+
      "Color {}\n"
    );
    EventInMFColor inputA[]=new EventInMFColor[3];
    EventOutMFColor outputA[]=new EventOutMFColor[3];
    inputA[0]=(EventInMFColor)(testA[0].getEventIn("keyValue"));
    outputA[0]=(EventOutMFColor)(testA[0].getEventOut("keyValue"));
    outputA[0].addVrmlEventListener(new GenericFieldListener("interp:keyValue"));
    //inputA[1]=(EventInMFColor)(testA[1].getEventIn("groundColor"));
    //outputA[1]=(EventOutMFColor)(testA[1].getEventOut("groundColor"));
    //outputA[1].addVrmlEventListener(new GenericFieldListener("Background:groundColor"));
    inputA[2]=(EventInMFColor)(testA[2].getEventIn("color"));
    outputA[2]=(EventOutMFColor)(testA[2].getEventOut("color"));
    outputA[2].addVrmlEventListener(new GenericFieldListener("Color:Color"));
    int counter=0;
    for (counter=0; counter<3; counter++) {
      try {
        inputA[counter].setValue(valueA);
        System.out.println(TestFactory.compareArray(valueC,outputA[counter].getValue()));
      } catch (Exception e) {
        e.printStackTrace(System.out);
        System.err.println("Error in eventIn "+(counter+1)+".");
      }
    }
    System.out.println("Section 2");

    Node test[]=browser.createVrmlFromString(
      "PROTO X [\n"+
      "  exposedField MFColor test []\n"+
      "] { Group {} }\n"+
      "X {}"
    );
    EventInMFColor input=(EventInMFColor)(test[0].getEventIn("test"));
    EventOutMFColor output=(EventOutMFColor)(test[0].getEventOut("test"));
    output.addVrmlEventListener(new GenericFieldListener("Proto field:test"));
    input.setValue(valueA);
    System.out.println(TestFactory.compareArray(valueC,output.getValue()));
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
