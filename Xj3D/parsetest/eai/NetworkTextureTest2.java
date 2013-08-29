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
import vrml.eai.BrowserFactory;
import vrml.eai.ConnectionException;
import vrml.eai.NoSuchBrowserException;
import vrml.eai.Node;
import vrml.eai.NotSupportedException;
import vrml.eai.VrmlComponent;
import vrml.eai.field.*;
import vrml.eai.event.*;


import java.awt.*;
import java.awt.event.WindowAdapter;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A test of SFImage using a dynamically modified PixelTexture.
 * The test was modified from createFromString.
 */

public class NetworkTextureTest2 {

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

    try {
        Thread.sleep(1000);
    } catch (InterruptedException e3) {
        // TODO Auto-generated catch block
        e3.printStackTrace();
    }
    
    System.out.println("Creating scene.");
    
    Node levelOne[]=clientBrowser.createVrmlFromString(
      "Viewpoint {}\n"+
      "Group {}\n"
    );
    Node levelTwo[]=clientBrowser.createVrmlFromString(
      "Transform {\n"+
      "  children [\n"+
      "    Shape {\n"+
      "      appearance Appearance {\n"+
      "        texture PixelTexture {\n"+
      "          image 8 8 3\n"+
"0x00FF00 0x00FF00 0x00FF00 0x00FF00 0x00FF00 0x0000FF 0x0000FF 0x0000FF\n"+
"0x0000FF 0x00FF00 0x00FF00 0x00FF00 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"+
"0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"+
"0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"+
"0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"+
"0x0000FF 0x0000FF 0x0000FF 0xFF0000 0xFF0000 0xFF0000 0x0000FF 0x0000FF\n"+
"0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"+
"0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"+
"        }\n"+
      /* "        material Material {\n"+
      "          emissiveColor 1 0 0\n"+
      "        }\n"+ */
      "      }\n"+
      "    geometry Box {}\n"+
      "    }\n"+
      "  ]\n"+
      "}"
    );
    ((EventInMFNode)nodes[0].getEventIn("set_children")).setValue(levelOne);
    ((EventInMFNode)levelOne[1].getEventIn("set_children")).setValue(levelTwo);

    Node[] children=((EventOutMFNode)(levelTwo[0].getEventOut("children"))).getValue();
    Node appearance=((EventOutSFNode)(children[0].getEventOut("appearance"))).getValue();
    Node pixelTexture=((EventOutSFNode)(appearance.getEventOut("texture"))).getValue();
    EventOutSFImage imageOutput=(EventOutSFImage)(pixelTexture.getEventOut("image"));
    EventInSFImage imageInput=(EventInSFImage)(pixelTexture.getEventIn("image"));
    imageOutput.addVrmlEventListener(
      new BasicImageMutator(imageOutput,imageInput,2)
    );
    imageOutput.addVrmlEventListener(new GenericFieldListener());
    // The image mutator will manipulate the values it receives, but we need
    // to trigger an event first.
    imageInput.setValue(8,8,3,imageOutput.getPixels());

  }
}
