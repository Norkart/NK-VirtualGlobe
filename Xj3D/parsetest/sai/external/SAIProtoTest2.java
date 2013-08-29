/*****************************************************************************
 * Copyright North Dakota State University, 2004
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import org.web3d.x3d.sai.*;

/**
 * Test of making PROTO instances
 */

public class SAIProtoTest2 {

  public static void main(String[] args) {
    ExternalBrowser browser=SAITestFactory.getBrowser();

    browser.addBrowserListener(new GenericSAIBrowserListener());
    X3DScene scene=browser.createX3DFromString(
      "PROFILE Interactive\n"+
      "DEF Root Transform {}"+
      "PROTO Bob [\n"+
      "]\n"+
      "{\n"+
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
      "}"+
  	  "}"
    );
    SAIWaitForBrowserInit waiter=new SAIWaitForBrowserInit(browser);
    browser.replaceWorld(scene);
    waiter.waitForInit();
    X3DNode nodes[]=scene.getRootNodes();
    System.out.println("Number of nodes from create:"+nodes.length);
    // The two equivalent methods for convenience.
    //scene.addRootNode(scene.getProto("Bob").createInstance());
    scene.addRootNode(scene.createProto("Bob"));
    X3DNode Root=scene.getRootNodes()[0];
    MFNode children=(MFNode) Root.getField("children");
    children.addX3DEventListener(new GenericSAIFieldListener());
    X3DNode bobNode=scene.createProto("Bob");
    children.setValue(1,new X3DNode[]{bobNode});
    System.out.println("Bob's types:");
    for (int counter=0; counter<bobNode.getNodeType().length; counter++)
    	System.out.println("Type "+counter+":"+bobNode.getNodeType()[counter]);
  }
}
