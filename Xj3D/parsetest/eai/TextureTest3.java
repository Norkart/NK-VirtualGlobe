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
import vrml.eai.event.*;


import java.awt.*;

/**
 * A test of SFImage using a dynamically modified PixelTexture.
 * The test was modified from createFromString.
 */

public class TextureTest3 {

  public static void main(String[] args) {
    Browser browser=TestFactory.getBrowser();

    browser.addBrowserListener(new GenericBrowserListener());
    Node nodes[]=browser.createVrmlFromString(
      "DEF Root Transform {}"
    );
    System.out.println("Number of nodes from create:"+nodes.length);
    System.out.println("Replacing world...");
    browser.replaceWorld(nodes);
    System.out.println("World replaced.");

    Node levelOne[]=browser.createVrmlFromString(
      "Viewpoint {}\n"+
      "Group {}\n"
    );
    Node levelTwo[]=browser.createVrmlFromString(
      "Transform {\n"+
      "  children [\n"+
      "    Shape {\n"+
      "      appearance Appearance {\n"+
      "        texture PixelTexture {\n"+
      "          image 0 0 0\n"+
      "        }\n"+
      "        material Material {\n"+
      "          emissiveColor .8 .5 .3\n"+
      "          diffuseColor .4 .25 .15\n"+
      "        }\n"+
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
      new BasicImageMutator(imageOutput,imageInput,3)
    );
    imageInput.setValue(imageOutput.getHeight(),imageOutput.getWidth(),imageOutput.getComponents(),imageOutput.getPixels());
  }
}
