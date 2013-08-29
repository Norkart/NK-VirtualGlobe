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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import vrml.eai.Browser;
import vrml.eai.BrowserFactory;
import vrml.eai.ConnectionException;
import vrml.eai.NoSuchBrowserException;
import vrml.eai.Node;
import vrml.eai.NotSupportedException;
import vrml.eai.VrmlComponent;
import vrml.eai.field.*;


/**
 *   A simple test of the networked EAI code.  Just create a server a client browser,
 *   and then have the client browser call createVrmlFromString.
 */

public class NetworkCreateFromString {
  public static void main(String[] args) {
    VrmlComponent comp=BrowserFactory.createVrmlComponent(
                           new String[]{
                               "Xj3D_ServerPort=4000"
                           }
                       );
    Browser serverBrowser=comp.getBrowser();
    
    Frame f=new Frame();
    f.setLayout(new BorderLayout());
    f.setBackground(Color.blue);
    f.add((Component)comp, BorderLayout.CENTER);
    f.show();
        f.addWindowListener(new WindowAdapter(){
                            /* Normal adapter to make dispose work. */
                            public void windowClosing(java.awt.event.WindowEvent e) {
                                System.exit(0);
                            }
                        });
    f.setSize(400,400);

    serverBrowser.addBrowserListener(new GenericBrowserListener());
    /* Test one */
    
    Browser clientBrowser;
    try {
        clientBrowser = BrowserFactory.getBrowser(InetAddress.getLocalHost(),4000);
    } catch (NotSupportedException e1) {
        e1.printStackTrace();
        System.err.println("Unable to connect.");
        return;
    } catch (NoSuchBrowserException e1) {
        e1.printStackTrace();
        System.err.println("Unable to connect.");
        return;
    } catch (ConnectionException e1) {
        e1.printStackTrace();
        System.err.println("Unable to connect.");
        return;
    } catch (UnknownHostException e1) {
        e1.printStackTrace();
        System.err.println("Unable to connect.");
        return;
    }
    Node nodes[]=clientBrowser.createVrmlFromString(
      "DEF Root Transform {}"
    );
    System.out.println("Number of nodes from create:"+nodes.length);
    System.out.println("Replacing world...");
    clientBrowser.replaceWorld(nodes);
    System.out.println("World replaced.");

    /* Test two */
    Node nullResult[]=clientBrowser.createVrmlFromString("");
    System.out.println("After third create.");
    if (nullResult==null)
      System.err.println("Returned a null array for no nodes.");
    else if (nullResult.length!=0)
      System.err.println("Returned nodes for the empty string.");
    else
      System.out.println("Returned the correct result for empty string.");
    try {
      if (clientBrowser.getNode("Root")!=null)
        System.err.println("Incorrectly added to namespace.");
      System.err.println("Failed to throw invalid node exception.");
    } catch (vrml.eai.InvalidNodeException ine) {
      System.out.println("Correctly threw invalid node exception.");
    }
    /* Test three */
    Node levelOne[]=clientBrowser.createVrmlFromString(
      "Viewpoint {}\n"+
      "Group {}\n"
    );
    System.out.println("After fourth create.");
    Node levelTwo[]=clientBrowser.createVrmlFromString(
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
    nodes[0].getEventOut("children").addVrmlEventListener(new GenericFieldListener("nodes[0]"));
    levelOne[1].getEventOut("children").addVrmlEventListener(new GenericFieldListener("levelOne[1]"));
    levelTwo[0].getEventOut("translation").addVrmlEventListener(new GenericFieldListener("levelTwo[0]"));
    ((EventInMFNode)nodes[0].getEventIn("set_children")).setValue(levelOne);
    ((EventInMFNode)levelOne[1].getEventIn("set_children")).setValue(levelTwo);
    ((EventInSFVec3f)levelTwo[0].getEventIn("translation")).setValue(new float[]{2.0f,0.0f,0.0f});
    ((EventInSFVec3f)levelTwo[0].getEventIn("translation")).setValue(new float[]{2.0f,1.0f,0.0f});

  }
}
