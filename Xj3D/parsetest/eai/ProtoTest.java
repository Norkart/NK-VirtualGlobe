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
import java.awt.event.*;
import javax.swing.*;

/**
 * Testing whether dynamically added PROTO nodes are getting and sending
 * events as they are supposed to.  The PROTO's are done using
 * createVrmlFromString.
 *
 * The Script node outside the PROTO node is used to provide comparison
 * testing.
 */

/** Utility class for sending an event with varying value to the
  * underlying code */
class SendEventAction implements ActionListener {
    EventInSFVec3f target;

    EventGeneratingPanel parent;

    SendEventAction(
        EventInSFVec3f anEventIn,
        EventGeneratingPanel aPanel
    ) {
        target=anEventIn;
        parent=aPanel;
    }

    public void actionPerformed(ActionEvent e) {
        parent.data[0]=parent.data[0]+0.2f;
        System.out.println("Sending "+parent.data[0]+","+parent.data[1]+","+parent.data[2]);
        target.setValue(parent.data);
    }
}

/** Utility class for generating a 'Push here to do work' button */
class EventGeneratingPanel extends JFrame {
    float data[]=new float[]{1.0f,2.0f,3.0f};
    EventGeneratingPanel(
        String label,
        EventIn target
    ) {
        super(label);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridLayout(2,1));
        JButton doIt=new JButton("Send event");
        doIt.addActionListener(new SendEventAction((EventInSFVec3f)target,this));
        getContentPane().add(doIt);
    }
}

/** The main test case class */
public class ProtoTest {
  public static void main(String[] args) {
    Browser browser=TestFactory.getBrowser();

    browser.addBrowserListener(new GenericBrowserListener());
    /* Test one */
    Node nodes[]=browser.createVrmlFromString(
      "DEF Root Transform {}"
    );
    System.out.println("Number of nodes from create:"+nodes.length);
    System.out.println("Replacing world...");
    browser.replaceWorld(nodes);
    System.out.println("World replaced.");

    // The tests in question
    Node levelTwo[]=browser.createVrmlFromString(
"#VRML V2.0 utf8\n"+
"\n"+
"PROTO Test [\n"+
"   eventIn  SFVec3f input\n"+
"   eventOut SFInt32 output\n"+
"   field    SFInt32 start 0\n"+
"] {\n"+
"   DEF A Script {\n"+
"       field SFInt32 start IS start\n"+
"       eventIn SFVec3f input IS input\n"+
"       eventOut SFInt32 output IS output\n"+
"       url \"javascript:\n"+
"       function initialize() {\n"+
"           Browser.println('proto script init');\n"+
"       }\n"+
"       function input(value,time) {\n"+
"           Browser.println('proto input');\n"+
"           start=start+1;\n"+
"           Browser.println(start);\n"+
"           Browser.println(value[0]);\n"+
"           Browser.println(value[1]);\n"+
"           Browser.println(value[2]);\n"+
"           Browser.println(time);\n"+
"           output=start;\n"+
"       }\n"+
"       \"\n"+
"   }\n"+
"       DEF B Script {\n"+
"           eventIn SFInt32 input\n"+
"           url \"javascript:\n"+
"           function input(value,time) {\n"+
"               Browser.println('proto B input');\n"+
"           }\n"+
"           \"\n"+
"       }\n"+
"       ROUTE A.output TO B.input\n"+
"}\n"+
"\n"+
"Test {}\n"+
"Script {\n"+
"   field SFInt32 start 0\n"+
"   eventIn SFVec3f input\n"+
"   eventOut SFInt32 output\n"+
"   url \"javascript:\n"+
"   function initialize() {\n"+
"       Browser.println('script init');\n"+
"   }\n"+
"   function input(value,time) {\n"+
"       Browser.println('script input');\n"+
"       start=start+1;\n"+
"       Browser.println(start);\n"+
"       Browser.println(value[0]);\n"+
"       Browser.println(value[1]);\n"+
"       Browser.println(value[2]);\n"+
"       Browser.println(time);\n"+
"       output=start;\n"+
"   }\n"+
"   \"\n"+
"}\n"
    );

    // Rig up the listeners and the event triggers

nodes[0].getEventOut("children").addVrmlEventListener(new GenericFieldListener("nodes[0]"));
levelTwo[0].getEventOut("output").addVrmlEventListener(new GenericFieldListener("levelTwo[0]"));
levelTwo[1].getEventOut("output").addVrmlEventListener(new GenericFieldListener("levelTwo[1]"));
((EventInMFNode)(nodes[0].getEventIn("children"))).setValue(levelTwo);

    JFrame frame1 = new EventGeneratingPanel("zero",levelTwo[0].getEventIn("input"));
    frame1.setSize(new Dimension(50,70));
    frame1.setLocation(new Point(100,0));
    frame1.show();
    JFrame frame2 = new EventGeneratingPanel("one",levelTwo[1].getEventIn("input"));
    frame2.setSize(new Dimension(50,70));
    frame2.setLocation(new Point(100,70));
    frame2.show();

  }
}
