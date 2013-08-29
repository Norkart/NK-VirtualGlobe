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
import javax.swing.*;
import java.awt.event.*;

/**
 * A hacked up test of adding Viewpoint nodes as children of PROTO instances,
 * with a few levels of Group{} nodes internall to check on reference 
 * counting.
 *
 * The scene graph for this test is supposed to be something like:
 *
 * root[0]==children==protoNodeA==setStuffB==protoNodeB==setStuffA==viewpointA
 *                                                 +=====setStuffB==viewpointB
 * where protoNodeA and protoNodeB are just two proto's which a few random
 * Group and Transform nodes.
 *
 * This version improves upon ViewpointProtoTest by adding in a background
 * and navigation info to discover more areas which have to deal with the
 * weird link under viewpoint behavior problems.
 *
 * Also adds the ability to remove the section of the scene graph that the
 * bound viewpoint is in to test the stack.
 * 
 * 
 * Note that sending viewpoint B in through protoNodeB's setStuffB eventIn causes
 * the Viewpoint to be outside of the scene graph, so binding it becomes an undefined behavior, so
 * only viewpoints A and C should work.  Viewpoint B really shouldn't be on the list anywhere.
 * 
 */

public class ViewpointProtoTest2 {

  /** Root node of scene. */
  static Node rootNodes[];

  /** The browser reference */
  static Browser browser;

  /** First proto node */
  static Node protoNodeA;

  /** Second proto node */
  static Node protoNodeB;

  /** Not yet used */
  static Node protoNodeC;

  /** First viewpoint */
  static Node viewpointA;

  /** Second viewpoint */
  static Node viewpointB;

  /** Third viewpoint */
  static Node viewpointC;

  public static void main(String[] args) {
    browser=TestFactory.getBrowser(true);

    browser.addBrowserListener(new GenericBrowserListener());
    /* The root */
    rootNodes=browser.createVrmlFromString(
      "#VRML V2.0 utf8\n"+
      "NavigationInfo {\n"+
      "  speed 0.01\n"+
      "  headlight TRUE\n"+
      "  type [\"NONE\"]\n"+
      "}\n"+
      "Background {\n"+
      "  skyColor [0 .6 .7]\n"+
      "  groundColor [.2 .6 .7]\n"+
      "}\n"+
      "Transform {}\n"+
      "Transform { translation -5  5  0 children Shape { geometry Box {} }}\n"+
      "Transform { translation  0  5  0 children Shape { geometry Box {} }}\n"+
      "Transform { translation  5  5  0 children Shape { geometry Box {} }}\n"+
      "Transform { translation -5  0  0 children Shape { geometry Box {} }}\n"+
      "Transform { translation  0  0  0 children Shape { geometry Box {} }}\n"+
      "Transform { translation  5  0  0 children Shape { geometry Box {} }}\n"+
      "Transform { translation -5 -5  0 children Shape { geometry Box {} }}\n"+
      "Transform { translation  0 -5  0 children Shape { geometry Box {} }}\n"+
      "Transform { translation  5 -5  0 children Shape { geometry Box {} }}\n"
    );
    try {
        Thread.sleep(1000);
    } catch (InterruptedException ie) {
        System.err.println("Woke up early.");
    }
    System.out.println("Replacing world...");
    browser.replaceWorld(rootNodes);
    System.out.println("World replaced.");

    System.out.println("Number of nodes from create:"+rootNodes.length);

    JFrame f=new JFrame();
    JButton b=new JButton("Step 1");
    b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	System.out.println("Adding viewpoints to scene.");
            ViewpointProtoTest2.doAdd();
        }
    });
    f.getContentPane().setLayout(new FlowLayout());
    f.getContentPane().add(b);
    b=new JButton("Step 2(a)");
    b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	System.out.println("Binding viewpoint #1");
            ViewpointProtoTest2.doBindOne();
        }
    });
    f.getContentPane().add(b);
    b=new JButton("Step 2(b)");
    b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	System.out.println("Trying to bind viewpoint not in scene graph");
            ViewpointProtoTest2.doBindTwo();
        }
    });
    f.getContentPane().add(b);
    b=new JButton("Step 2(c)");
    b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	System.out.println("Binding viewpoint #2.");
            ViewpointProtoTest2.doBindThree();
        }
    });
    f.getContentPane().add(b);
    b=new JButton("Step 3(a)");
    b.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		System.out.println("Removing viewpoints from scene graph while bound.");
    		doRemoveOne();
    	}
    });
    f.getContentPane().add(b);
	b=new JButton("Step 3(b)");
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Setting children with same value from scene graph while bound.");
			doBounceOne();
		}
	});
	f.getContentPane().add(b);
	b=new JButton("Step 3(c)");
	b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Unbinding the viewpoints.");
			doUnbind();
		}
	});
	f.getContentPane().add(b);
    f.show();
    f.pack();

  }

  static void doRemoveOne() {
  Node temp[]=new Node[1];
  temp[0]=protoNodeA;
  ((EventInMFNode)(rootNodes[2].getEventIn("removeChildren"))).setValue(temp);
  }

  static void doBounceOne() {
  Node temp[]=new Node[1];
  temp[0]=protoNodeA;
  ((EventInMFNode)(rootNodes[2].getEventIn("children"))).setValue(temp);
  }

  static void doUnbind() {
	((EventInSFBool)(viewpointA.getEventIn("set_bind"))).setValue(false);
	((EventInSFBool)(viewpointB.getEventIn("set_bind"))).setValue(false);
	((EventInSFBool)(viewpointC.getEventIn("set_bind"))).setValue(false);
  }

  static void doBindOne() {
    ((EventInSFBool)(viewpointA.getEventIn("set_bind"))).setValue(true);

  }

  static void doBindTwo() {
    ((EventInSFBool)(viewpointB.getEventIn("set_bind"))).setValue(true);
  }

  static void doBindThree() {
    ((EventInSFBool)(viewpointC.getEventIn("set_bind"))).setValue(true);
  }

  static void doAdd() {

    /* The basic geometry */
    protoNodeA=browser.createVrmlFromString(
      "PROTO s [\n"+
      "  eventIn MFNode setStuffA\n"+
      "  eventIn MFNode setStuffB\n"+
      "] {\n"+
      " Transform {\n"+
      "  translation -5 0 -5\n"+
      "  children [\n"+
      "    Group {\n"+
      "        children IS setStuffA\n"+
      "    }\n"+
      "    Group {\n"+
      "        children [\n"+
      "            Group { children IS setStuffB } \n"+
      "        ]\n"+
      "    }\n"+
      "  ]\n"+
      " }\n"+
      "}\n"+
      "s{}"
    )[0];
    protoNodeB=browser.createVrmlFromString(
      "PROTO s [\n"+
      "  eventIn MFNode setStuffA\n"+
      "  eventIn MFNode setStuffB\n"+
      "] {\n"+
      "    Group {\n"+
      "        children IS setStuffA\n"+
      "    }\n"+
      "    Group {\n"+
      "        children [\n"+
      "            Group { children IS setStuffB } \n"+
      "        ]\n"+
      "    }\n"+
      "}\n"+
      "s{}"
    )[0];
    viewpointA=browser.createVrmlFromString(
      "Viewpoint { position .5 .5 0 description \"ViewpointA\" }"
    )[0];
    viewpointB=browser.createVrmlFromString(
      "Viewpoint { position 0 0 0 description \"ViewpointB\" }"
    )[0];
    viewpointC=browser.createVrmlFromString(
      "Viewpoint { position .8 .8 0 description \"Viewpoint C\" }"
    )[0];

    Node temp[]=new Node[1];

    temp[0]=protoNodeA;
    ((EventInMFNode)(rootNodes[2].getEventIn("set_children"))).setValue(temp);
    temp[0]=protoNodeB;
    ((EventInMFNode)(protoNodeA.getEventIn("setStuffB"))).setValue(temp);
    temp[0]=viewpointA;
    ((EventInMFNode)(protoNodeB.getEventIn("setStuffA"))).setValue(temp);
    temp[0]=viewpointB;
    ((EventInMFNode)(protoNodeB.getEventIn("setStuffB"))).setValue(temp);
    temp[0]=viewpointC;
    ((EventInMFNode)(rootNodes[2].getEventIn("addChildren"))).setValue(temp);

  }
}
