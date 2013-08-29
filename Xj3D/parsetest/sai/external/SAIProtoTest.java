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

public class SAIProtoTest {

  public static void main(String[] args) {
    ExternalBrowser browser=SAITestFactory.getBrowser();

    browser.addBrowserListener(new GenericSAIBrowserListener());
    X3DScene scene=browser.createX3DFromString(
      "PROFILE Interactive\n"+
      "DEF Root Transform {}"
    );
    X3DNode nodes[]=scene.getRootNodes();
    System.out.println("Number of nodes from create:"+nodes.length);
    System.out.println("Replacing world...");
    browser.replaceWorld(scene);
    System.out.println("World replaced.");

    X3DNode levelOne[]=SAIUtilities.extractRootNodes(browser.createX3DFromString(
      "PROFILE Interchange\n"+
      "Viewpoint {}\n"+
      "Group {}\n"
    ));
    X3DScene partTwo=browser.createX3DFromString(
      "PROFILE Interactive\n"+
      "PROTO Bob [\n"+
      "	inputOnly	SFVec3f	setPos\n"+
      "	inputOnly	MFNode addChildren\n"+
      "]\n"+
      "{\n"+
      "Transform {\n"+
      " set_translation IS setPos\n"+
      " addChildren	IS addChildren\n"+
	  "}\n"+
  	  "}"
    );

    String names[]=partTwo.getProtosNames();
    System.out.println("There are "+names.length+" PROTO's in the scene.");
    for (int counter=0; counter<names.length; counter++)
        System.out.println("Proto #"+counter+"="+names[counter]);
    X3DProtoDeclaration p1Decl=partTwo.getProtoDeclaration(names[0]);
    X3DProtoInstance p1Instance=p1Decl.createInstance();
    X3DProtoInstance p2Instance=p1Decl.createInstance();
    SFVec3f f1=(SFVec3f) p1Instance.getField("setPos");
    SFVec3f f2=(SFVec3f) p2Instance.getField("setPos");
  }
}
