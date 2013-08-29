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
 *   This section tests MFVec3f fields.
 *   <P>
 *   This test uses loadURL, thus the actual test occurs in performStep2().
 */

public class SetGetTest2 implements vrml.eai.event.BrowserListener {
  public static void main(String[] args) {
    new SetGetTest2().performTest();
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
      {0.0f,0.1f,0.2f},
      {1.0f,1.1f,1.2f},
      {2.0f,2.1f,2.2f}};
    float valueB[][]=new float[][]{
      {-1.0f,2.0f,-3.0f},
      {2.0f,2.0f,2.0f}};
    float valueC[]=new float[]{
      0.0f,0.1f,0.2f,
      1.0f,1.1f,1.2f,
      2.0f,2.1f,2.2f};
    float valueD[]=new float[]{
      -1.0f,2.0f,-3.0f,
      2.0f,2.0f,2.0f};
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
    /* Nodes with MFVec3f exposed fields. */
    /* Not testing Extrusion or NormalInterpolator since that isn't supported yet.*/
    Node testA[]=browser.createVrmlFromString(
      "PositionInterpolator {}\n"+
      "Normal {}\n"+
      "Coordinate {}\n"
      //+"NormalInterpolator {}\n"
    );
    if (testA.length!=4)
      System.err.println("Node missing from create.");
    EventInMFVec3f inputA[]=new EventInMFVec3f[4];
    EventOutMFVec3f outputA[]=new EventOutMFVec3f[4];
    inputA[0]=(EventInMFVec3f)(testA[0].getEventIn("keyValue"));
    outputA[0]=(EventOutMFVec3f)(testA[0].getEventOut("keyValue"));
    outputA[0].addVrmlEventListener(new GenericFieldListener("PositionInterpolator:keyValue"));
    inputA[1]=(EventInMFVec3f)(testA[1].getEventIn("vector"));
    outputA[1]=(EventOutMFVec3f)(testA[1].getEventOut("vector"));
    outputA[1].addVrmlEventListener(new GenericFieldListener("Normal:vector"));
    inputA[2]=(EventInMFVec3f)(testA[2].getEventIn("point"));
    outputA[2]=(EventOutMFVec3f)(testA[2].getEventOut("point"));
    outputA[2].addVrmlEventListener(new GenericFieldListener("Coordinate:point"));
    /* inputA[3]=(EventInMFVec3f)(testA[3].getEventIn("keyValue"));
    outputA[3]=(EventOutMFVec3f)(testA[3].getEventOut("keyValue"));
    outputA[3].addVrmlEventListener(new GenericFieldListener("NormalInterpolator:keyValue"));
    */
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
      "  exposedField MFVec3f test []\n"+
      "] { Group {} }\n"+
      "X {}"
    );
    EventInMFVec3f input=(EventInMFVec3f)(test[0].getEventIn("test"));
    EventOutMFVec3f output=(EventOutMFVec3f)(test[0].getEventOut("test"));
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
