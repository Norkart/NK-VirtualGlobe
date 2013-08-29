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
 * A hacked up version of adding Script containing proto instances to a
 * scene to see when the script initializations occur.
 *
 */

public class ScriptProtoTest {

  /** Root node of scene. */
  static Node rootNodes[];

  /** The browser reference */
  static Browser browser;

  /** The root proto instance */
  static Node rootStub;

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
            ScriptProtoTest.addRootStub();
        }
    });
    f.getContentPane().setLayout(new FlowLayout());
    f.getContentPane().add(b);
    b=new JButton("Step 2");
    b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            addStub();
        }
    });
    f.getContentPane().add(b);
    /*
    b=new JButton("Step 2(b)");
    b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            ScriptProtoTest.doBindTwo();
        }
    });
    f.getContentPane().add(b);
    */
    /*
    b=new JButton("Step 2(c)");
    b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            ScriptProtoTest.doBindThree();
        }
    });
    f.getContentPane().add(b);
    */
    f.show();
    f.pack();

  }

  static Node getNode() {
      Node s=
      browser.createVrmlFromString(
      "PROTO s [\n"+
      "  eventIn MFNode addKid\n"+
      "  eventIn MFNode removeKid\n"+
      "  eventIn MFNode addStuff\n"+
      "  eventIn MFNode removeStuff\n"+
      "  eventOut SFBool doneSetup\n"+
      "] {\n"+
      "    Transform {\n"+
      "        children [\n"+
      "            Script {\n"+
      "                eventOut SFBool doneSetup IS doneSetup\n"+
      "                url \""+
      "                    javascript:function initialize() {print('initialized'); doneSetup=true;}\n"+
      "                \""+
      "            }\n"+
      "            Group {\n"+
      "                addChildren IS addKid\n"+
      "                removeChildren IS removeKid\n"+
      "            }\n"+
      "            Group { addChildren IS addStuff removeChildren IS removeStuff } \n"+
      "        ]\n"+
      "    }\n"+
      "}\n"+
      "s{}"
      )[0];
      s.getEventOut("doneSetup").addVrmlEventListener(new GenericFieldListener());
      return s;
  }

  static void addRootStub() {
    if (rootStub==null) {
        Node temp[]= new Node[1];
        temp[0]=getNode();
        rootStub=temp[0];
        ((EventInMFNode)(rootNodes[0].getEventIn("addChildren"))).setValue(temp);
    }
    
  }

  static void addStub() {

    for (int counter=0; counter<10; counter++) {
    /* The basic geometry */
    protoNodeA=getNode();

    Node temp[]=new Node[1];

    temp[0]=protoNodeA;
    try {
        Thread.sleep(200);
    } catch (InterruptedException e) {
    }
    ((EventInMFNode)(rootStub.getEventIn("addKid"))).setValue(temp);
    }

  }
}
