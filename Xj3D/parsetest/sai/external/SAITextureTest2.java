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

import org.web3d.x3d.sai.*;

/**
 * A test of SFImage using a dynamically modified PixelTexture.
 * The test was modified from createFromString.
 */

public class SAITextureTest2 {

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
      "PROFILE Interactive\n"+
      "Viewpoint {}\n"+
      "Group {}\n"
    ));
    X3DNode levelTwo[]=SAIUtilities.extractRootNodes(browser.createX3DFromString(
      "PROFILE Interactive\n"+
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
    ));
    ((MFNode)nodes[0].getField("set_children")).setValue(levelOne.length,levelOne);
    ((MFNode)levelOne[1].getField("set_children")).setValue(levelTwo.length,levelTwo);

    X3DNode[] children=new X3DNode[((MFNode)(levelTwo[0].getField("children"))).getSize()];
    ((MFNode)(levelTwo[0].getField("children"))).getValue(children);
    X3DNode appearance=((SFNode)(children[0].getField("appearance"))).getValue();
    X3DNode pixelTexture=((SFNode)(appearance.getField("texture"))).getValue();
    SFImage imageOutput=(SFImage)(pixelTexture.getField("image"));
    SFImage imageInput=(SFImage)(pixelTexture.getField("image"));
    imageOutput.addX3DEventListener(
      new BasicSAIImageMutator(imageOutput,imageInput,2)
    );
    // The image mutator will manipulate the values it receives, but we need
    // to trigger an event first.
    int pixels[]=new int[imageOutput.getComponents()*imageOutput.getHeight()*imageOutput.getWidth()];
	imageOutput.getPixels(pixels);
    imageInput.setValue(8,8,3,pixels);
  }
}
