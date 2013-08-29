import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.util.HashMap;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.MFImage;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFImage;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

/**
 * Created on 2004/05/11 
 */
/**
 * MFImageTest1
 * 
 * Relatively simple test of MFImage setting and getting.
 * 
 * @author
 */
public class MFImageTest1 {
	public static void main(String[] args) {
		// Step One: Create the browser component
		HashMap requestedParameters = new HashMap();
		requestedParameters.put("Xj3D_ConsoleShown", Boolean.TRUE);
		requestedParameters.put("Xj3D_LocationShown", Boolean.FALSE);
		X3DComponent comp = BrowserFactory
				.createX3DComponent(requestedParameters);
		ExternalBrowser browser = comp.getBrowser();
		Frame f = new Frame();
		f.setLayout(new BorderLayout());
		f.setBackground(Color.blue);
		f.add((Component) comp, BorderLayout.CENTER);
		f.show();
		f.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			}
		});
		f.setSize(400, 400);
		// Step Two: Initialize your scene
		browser.addBrowserListener(new GenericSAIBrowserListener());
		// In this case we're building the scene the hard old way using
		// createX3DFromString because this was originally an EAI
		// demo that's been rewritten slightly for X3D.
		X3DScene scene = browser.createX3DFromString("PROFILE Interactive\n"
				+ "DEF Root Transform {}");
		X3DNode nodes[] = scene.getRootNodes();
		System.out.println("Number of nodes from create:" + nodes.length);
		System.out.println("Replacing world...");
		browser.replaceWorld(scene);
		System.out.println("World replaced.");
		X3DScene firstScene = browser.createX3DFromString("PROFILE Interchange\n" 
				+ "Viewpoint {}\n"
				+ "Group {}\n"
				);
		X3DNode levelOne[] = firstScene.getRootNodes();
		for (int counter=0; counter<levelOne.length; counter++)
			firstScene.removeRootNode(levelOne[counter]);
		// No ROUTEs to transfer from the temporary scene.
		X3DScene secondScene = browser.createX3DFromString(
				"PROFILE Interactive\n"
				+ "Transform {\n"
				+ "  translation 1.5 0 0"
				+ "  children [\n"
				+ "    Shape {\n"
				+ "      appearance Appearance {\n"
				+ "        texture PixelTexture {\n"
				+ "          image 8 8 3\n"
				+ "0x00FF00 0x00FF00 0x00FF00 0x00FF00 0x00FF00 0x0000FF 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x00FF00 0x00FF00 0x00FF00 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x0000FF 0x0000FF 0xFF0000 0xFF0000 0xFF0000 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"
				+ "        }\n"
				+ "        material Material {\n"
				+ "          emissiveColor 1 0 0\n"
				+ "        }\n" + "      }\n"
				+ "    geometry Box {}\n" + "    }\n" + "  ]\n"
				+ "}"
				+ "Transform {\n"
				+ "  translation -1.5 0 0"
				+ "  children [\n"
				+ "    Shape {\n"
				+ "      appearance Appearance {\n"
				+ "        texture PixelTexture {\n"
				+ "          image 7 7 3\n"
				+ "0x00FF00 0x00FF00 0x00FF00 0x00FF00 0x00FF00 0x0000FF 0x0000FF \n"
				+ "0x0000FF 0x00FF00 0x00FF00 0x00FF00 0x0000FF 0x0000FF 0x0000FF \n"
				+ "0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF \n"
				+ "0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF \n"
				+ "0xF000FF 0xF000FF 0xF000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF \n"
				+ "0xF000FF 0x0000FF 0xF000FF 0xFF0000 0xFF0000 0xFF0000 0x0000FF \n"
				+ "0xF000FF 0xF000FF 0xF000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF \n"
				+ "        }\n"
				+ "        material Material {\n"
				+ "          emissiveColor 1 0 0\n"
				+ "        }\n" + "      }\n"
				+ "    geometry Box {}\n" + "    }\n" + "  ]\n"
				+ "}"				
				+ "PROTO Stuff [\n"
				+ "  inputOutput MFImage im [0 0 0, 0 0 0]\n"
				+ "] { Group{} }\n"
				+ "Stuff {}"
		);
		X3DNode levelTwo[] = secondScene.getRootNodes();
		for (int counter=0; counter<levelTwo.length; counter++)
			secondScene.removeRootNode(levelTwo[counter]);
		// No routes to transfer from the temporary scene
		// Construct the scene by adding the second set of nodes to the first
		// set and the third set of nodes to the second set.
		((MFNode) nodes[0].getField("set_children")).setValue(levelOne.length,
				levelOne);
		((MFNode) levelOne[1].getField("set_children")).setValue(
				levelTwo.length, levelTwo);
		// Walk down the fields to the image field of the pixel texture.
		X3DNode[] children = new X3DNode[((MFNode) levelTwo[0]
				.getField("children")).getSize()];
		((MFNode) (levelTwo[0].getField("children"))).getValue(children);
		X3DNode appearance = ((SFNode) (children[0].getField("appearance")))
				.getValue();
		X3DNode pixelTexture = ((SFNode) (appearance.getField("texture")))
				.getValue();
		SFImage imageField1 = (SFImage) (pixelTexture.getField("image"));
		children = new X3DNode[((MFNode) levelTwo[1].getField("children")).getSize()];
		((MFNode) (levelTwo[1].getField("children"))).getValue(children);
		appearance = ((SFNode) (children[0].getField("appearance")))
		.getValue();
		pixelTexture = ((SFNode) (appearance.getField("texture")))
		.getValue();
		SFImage imageField2 = (SFImage) (pixelTexture.getField("image"));
		MFImage MFImageField = (MFImage) levelTwo[2].getField("im");
		MFImageField.addX3DEventListener(new GenericSAIFieldListener());
		imageField1.addX3DEventListener(new GenericSAIFieldListener());
		// Event 1
		browser.beginUpdate();
		MFImageField.setImage(0,imageField1.getImage());
		browser.endUpdate();
		// Hello race condition.
		// Event 2
		browser.beginUpdate();
		MFImageField.setImage(1,imageField2.getImage());
		browser.endUpdate();
		// Event 3
		browser.beginUpdate();
		MFImageField.setImage(1,imageField1.getImage());
		MFImageField.setImage(0,imageField2.getImage());
		browser.endUpdate();
		//imageField.setImage(imageField.getImage());
		//imageField.getImage().getWritableTile(0,0).		
	}
}
