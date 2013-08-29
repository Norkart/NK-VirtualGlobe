import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.util.HashMap;
import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFImage;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;
/**
 * TextureDemo1
 * 
 * Create your very own animated texture by modifying it every frame.
 */
public class TextureDemo1 {
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
				+ "}");
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
		SFImage imageField = (SFImage) (pixelTexture.getField("image"));
		imageField.addX3DEventListener(new SAIImageMutatorOne());
		// The image mutator will manipulate the values it receives, but we
		// need
		// to trigger an event first.
		int pixels[] = new int[imageField.getComponents()
				* imageField.getHeight() * imageField.getWidth()];
		imageField.getPixels(pixels);
		imageField.setValue(8, 8, 3, pixels);
	}
}
/**
 * A demo utility for PixelTexture. All this class does is wait for an event on
 * its SFImage input, do some preselected manipulation on that event's value,
 * and then send the event off to its output. A thread.sleep is thrown in to
 * avoid running too fast, but it would be much better to do this work in a
 * seperate process.
 */
class SAIImageMutatorOne implements X3DFieldEventListener {
	/** Buffer for the pixel data */
	int pixels[];
	public SAIImageMutatorOne() {
		pixels = new int[64];
	}
	/**
	 * Respond to the field changing. This demo depends on the fact that it can
	 * modify the field that it is receiving an event from. This is not always
	 * the case
	 */
	public void readableFieldChanged(X3DFieldEvent evt) {
		/** Get the event, change it, and then send a new value. */
		SFImage src = (SFImage) (evt.getSource());
		src.getPixels(pixels);
		int height = src.getHeight();
		int width = src.getWidth();
		int components = src.getComponents();
		int pixelZero = pixels[0];
		for (int counter = 1; counter < 64; counter++) {
			pixels[counter - 1] = pixels[counter];
		}
		pixels[63] = pixelZero;
		src.setValue(height, width, components, pixels);
	}
}
