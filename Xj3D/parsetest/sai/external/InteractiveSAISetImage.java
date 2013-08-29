import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.InvalidFieldException;
import org.web3d.x3d.sai.InvalidFieldValueException;
import org.web3d.x3d.sai.InvalidOperationTimingException;
import org.web3d.x3d.sai.InvalidWritableFieldException;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFImage;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

import sun.awt.image.URLImageSource;

/**
 * Load a graphic into an SFImage field.
 */
public class InteractiveSAISetImage {
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
				+ "  translation -1 0 0\n"
				+ "  children [\n"
				+ "    Shape {\n"
				+ "      appearance Appearance {\n"
				+ "        texture PixelTexture {\n"
				+ "          image 3 1 3\n"
				+ "0xFF0000 0x00FF00 0x0000FF"/*" 0x00FF00 0x00FF00 0x0000FF 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x00FF00 0x00FF00 0x00FF00 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x0000FF 0x0000FF 0xFF0000 0xFF0000 0xFF0000 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"
				+ "0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF 0x0000FF\n"*/
				+ "        }\n"
				+ "        material Material {\n"
				+ "          emissiveColor 1 0 0\n"
				+ "        }\n" + "      }\n"
				+ "    geometry Box {}\n" + "    }\n" + "  ]\n"
				+ "}"
				+ "Transform {\n"
				+ "  translation 1 0 0\n"
				+ "  children [\n"
				+ "    Shape {\n"
				+ "      geometry Box {}\n"
				+ "      appearance Appearance {\n"
				+ "        texture ImageTexture {\n"
				+ "          url [\"http://xj3d.org/Xj3Dlogo-128.gif\"]\n"
				+ "        }\n"
				+ "        material Material {\n"
				+ "          emissiveColor 1 0 0\n"
				+ "        }\n"
				+ "      }\n"
				+ "    }\n"
				+ "  ]\n"
				+ "}"
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
		SFImage imageField = (SFImage) (pixelTexture.getField("image"));
		imageField.addX3DEventListener(new GenericSAIFieldListener());

		// And find the matching URL field
		((MFNode)(levelTwo[1].getField("children"))).getValue(children);
		appearance = ((SFNode)(children[0].getField("appearance"))).getValue();
		X3DNode imageTexture=((SFNode)(appearance.getField("texture"))).getValue();
		MFString urlField=(MFString)(imageTexture.getField("url"));
		urlField.addX3DEventListener(new GenericSAIFieldListener());
		
	    // Maka a simple set of controls
	    JFrame windowFrame=new JFrame();    
	    windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    windowFrame.getContentPane().setLayout(new GridLayout(2,1));
	    JPanel p1=new JPanel();
	    p1.setLayout(new GridLayout(2,1));
	    p1.add(new JLabel("URL of image to load"));
	    String initURL;
	    if (args.length>0)
	        initURL=args[0];
	    else
	        initURL="http://xj3d.org/Xj3Dlogo-128.gif";

	    JTextField URL=new JTextField(initURL);
	    p1.add(URL);
	    JPanel p2=new JPanel();
	    p2.setLayout(new GridLayout(1,2));
	    JButton doIt=new JButton("Load from URL");
	    windowFrame.getContentPane().setLayout(new FlowLayout());
	    windowFrame.getContentPane().add(p1);
	    p2.add(doIt);
	    windowFrame.getContentPane().add(p2);
	    //windowFrame.getContentPane().add(doIt);
	    SAILoadImageAction urlLoading=new SAILoadImageAction(imageField,urlField,URL,windowFrame);
	    doIt.addActionListener(urlLoading);
	    //windowFrame.getContentPane().add(newBrowser);
	    JButton browseFile=new JButton("Open file...");
	    browseFile.addActionListener(new SAIBrowseImageAction(imageField,urlField,URL));
	    p2.add(browseFile);
	    windowFrame.pack();
	    windowFrame.show();

	}


}

class SAILoadImageAction implements ActionListener {

	SFImage imageField;
	
	MFString urlField;
	
	JTextField sourceField;
	
	JFrame titleDest;
	
	SAILoadImageAction(SFImage target, MFString target2, JTextField t, JFrame f) {
		imageField=target;
		urlField=target2;
		sourceField=t;
		titleDest=f;
	}
	
	/** 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
	    new Thread() {
			public void run() {
				URL u=null;
				Image i=null;
				try {
					u = new URL(sourceField.getText());
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				try {
					i=(Image)u.getContent(new Class[]{Image.class});
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					return;
				}
				System.out.println("Loading "+sourceField.getText());
				MediaTracker r=new MediaTracker(sourceField);
				r.addImage(i,0);
				try {
					r.waitForAll();
				} catch (InterruptedException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
					return;
				}
				BufferedImage transpose=new BufferedImage(i.getWidth(null),i.getHeight(null),BufferedImage.TYPE_INT_RGB);
				transpose.createGraphics().drawImage(i,0,0,null);
				imageField.setImage(transpose);
				urlField.setValue(1,new String[]{u.toExternalForm()});
				System.out.println("Done");
				//imageField.setImage(imageField.getImage());
				//imageField.getImage().getWritableTile(0,0).
			}
		}.start();
	}
}

class SAIBrowseImageAction implements ActionListener {
	/** The field to send the event to */
	SFImage imageField;
	/** Other field to send the event to */
	MFString urlField;
	/** The text area to change with new file name */
	JTextField textTarget;
	/** The file chooser to select images with */
	JFileChooser chooser = new JFileChooser();
	
	/**
	 * @param browser
	 *            The browser to send the event to
	 */
	public SAIBrowseImageAction(SFImage field, MFString target2,JTextField anEditor) {
		imageField = field;
		urlField=target2;
		textTarget = anEditor;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		new Thread() {
			public void run() {
				// TODO Auto-generated method stub
				String urls[] = new String[1];
				//JFileChooser chooser = new JFileChooser();
				switch (chooser.showOpenDialog(textTarget)) {
					case JFileChooser.APPROVE_OPTION :
						try {
							urls[0] = chooser.getSelectedFile().toURL()
									.toExternalForm();
						} catch (MalformedURLException e) {
							e.printStackTrace();
							return;
						}
						URL u = null;
						Image i = null;
						try {
							u = chooser.getSelectedFile().toURL();
							textTarget.setText(u.toExternalForm());
						} catch (MalformedURLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							i = (Image) u.getContent(new Class[]{Image.class});
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						MediaTracker r = new MediaTracker(textTarget);
						r.addImage(i, 0);
						try {
							r.waitForAll();
						} catch (InterruptedException e3) {
							// TODO Auto-generated catch block
							e3.printStackTrace();
						}
						BufferedImage transpose = new BufferedImage(i
								.getWidth(null), i.getHeight(null),
								BufferedImage.TYPE_INT_RGB);
						transpose.createGraphics().drawImage(i, 0, 0, null);
						imageField.setImage(transpose);
						urlField.setValue(1,urls);
						break;
					default :
						System.err.println("Selection not approved.");
				}
				System.out.println("Done");
			}
		}.start();
	}
}