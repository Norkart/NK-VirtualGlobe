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
import vrml.eai.field.EventIn;
import vrml.eai.field.EventOut;

/**
 * A test of getUserData/setUserData and field equivalence.
 * This test checks both the proper storage and retrieval of user data
 * (admittedly in a basic manner), and whether the eventIn and eventOut
 * objects associated with various exposedField's on a specially designed
 * PROTO node are in fact equal.
 * These tests don't rule out spurious false equivalences.
 */

public class UserDataTest {
  public static void main(String[] args) {
    // Generic counter
    int counter;
    // An array of objects to use as unique data items.  20 is arbitrary.
    Object testItems[]=new Object[20];

    // Set up the test tags
    for (counter=0; counter<testItems.length; counter++)
      testItems[counter]=new Object();

    // Set up the test environment
    Browser browser=TestFactory.getBrowser();

    // Get the first set of test nodes
    Node stuff[]=TestFactory.getTestNodes(browser,5);
    // Dump types
    for (counter=0; counter<stuff.length; counter++) {
      System.out.println("Node "+counter+" is a "+stuff[counter].getType());
    }

    checkEquivalence(stuff[0],"center");
    checkEquivalence(stuff[0],"translation");
    checkEquivalence(stuff[0],"children");
    checkEquivalence(stuff[0],"rotation");
    checkEquivalence(stuff[0],"scale");
    checkEquivalence(stuff[0],"scaleOrientation");

    checkEquivalence(stuff[1],"children");

    checkEquivalence(stuff[2],"appearance");
    checkEquivalence(stuff[2],"geometry");

    checkGetAndSet(stuff[0],"translation",testItems[0]);
    checkGetAndSet(stuff[0],"center",testItems[1]);
    checkGetAndSet(stuff[0],"children",testItems[2]);
    checkGetAndSet(stuff[0],"rotation",testItems[3]);
    checkGetAndSet(stuff[1],"children",testItems[4]);
    checkGetAndSet(stuff[2],"appearance",testItems[5]);
    checkGetAndSet(stuff[2],"geometry",testItems[6]);

    Node testProto=TestFactory.getTestNodes(browser,6)[0];
    checkEquivalence(testProto,"SFBool");
    checkEquivalence(testProto,"SFColor");
    checkEquivalence(testProto,"MFColor");
    checkEquivalence(testProto,"SFFloat");
    checkEquivalence(testProto,"MFFloat");
    checkEquivalence(testProto,"SFImage");
    checkEquivalence(testProto,"SFInt32");
    checkEquivalence(testProto,"MFInt32");
    checkEquivalence(testProto,"SFNode");
    checkEquivalence(testProto,"MFNode");
    checkEquivalence(testProto,"SFRotation");
    checkEquivalence(testProto,"MFRotation");
    checkEquivalence(testProto,"SFString");
    checkEquivalence(testProto,"MFString");
    checkEquivalence(testProto,"SFTime");
    checkEquivalence(testProto,"MFTime");
    checkEquivalence(testProto,"SFVec2f");
    checkEquivalence(testProto,"MFVec2f");
    checkEquivalence(testProto,"SFVec3f");
    checkEquivalence(testProto,"MFVec3f");

    Object protoTestItems[]=new Object[21];
    for (counter=0; counter<21; counter++)
      protoTestItems[counter]=new Object();
    // Demonstrate/test the CRProtoInstane (192) problem.
    checkGetAndSet(testProto,"SFBool",protoTestItems[0]);
    checkGetAndSet(testProto,"SFColor",protoTestItems[1]);
    checkGetAndSet(testProto,"MFColor",protoTestItems[2]);
    checkGetAndSet(testProto,"SFFloat",protoTestItems[3]);
    checkGetAndSet(testProto,"MFFloat",protoTestItems[4]);
    checkGetAndSet(testProto,"SFImage",protoTestItems[5]);
    checkGetAndSet(testProto,"SFInt32",protoTestItems[6]);
    checkGetAndSet(testProto,"MFInt32",protoTestItems[7]);
    checkGetAndSet(testProto,"SFNode",protoTestItems[8]);
    checkGetAndSet(testProto,"MFNode",protoTestItems[9]);
    checkGetAndSet(testProto,"SFRotation",protoTestItems[10]);
    checkGetAndSet(testProto,"MFRotation",protoTestItems[11]);
    checkGetAndSet(testProto,"SFString",protoTestItems[12]);
    checkGetAndSet(testProto,"MFString",protoTestItems[13]);
    checkGetAndSet(testProto,"SFTime",protoTestItems[14]);
    checkGetAndSet(testProto,"MFTime",protoTestItems[15]);
    checkGetAndSet(testProto,"SFVec2f",protoTestItems[16]);
    checkGetAndSet(testProto,"MFVec2f",protoTestItems[17]);
    checkGetAndSet(testProto,"SFVec3f",protoTestItems[18]);
    checkGetAndSet(testProto,"MFVec3f",protoTestItems[19]);

  }

  /** Set the user data for a field, and then see if it stays.
   *  Naturally, this isn't a good test against incorrect field mapping */
  static void checkGetAndSet(Node target, String field, Object data) {
    EventOut in=target.getEventOut(field);
    in.setUserData(data);
    if (in.getUserData()!=data)
      System.err.println("get/setTest fails for "+field+" on "+target.getType());
    else
      System.out.println("get/setTest ok for "+field+" on "+target.getType());
  }

  /** Check that the exposedField field is equal both ways for target .*/
  static void checkEquivalence(Node target,String field) {
    EventIn in=target.getEventIn(field);
    EventOut out=target.getEventOut(field);
    if (in.equals(out) && out.equals(in))
      System.out.println("Equivalence for "+field+" for "+target.getType()+" is held.");
    else {
      System.err.println("Equivalence for "+field+" for "+target.getType()+" is not held.");
        if (!in.equals(out))
          System.err.println("  In!=out");
        if (!out.equals(in))
          System.err.println("  Out!=in");
      }
  }
}
