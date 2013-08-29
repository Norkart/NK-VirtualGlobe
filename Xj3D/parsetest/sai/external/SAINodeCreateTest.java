/*****************************************************************************
 * Copyright North Dakota State University, 2005
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import java.awt.*;
import javax.swing.*;
import java.util.HashMap;
import org.web3d.x3d.sai.*;

/** Create a few nodes, change their field values and then
 *  check a few boundary conditions.
 */
public class SAINodeCreateTest {

	public static void main(String[] args) {
		ExternalBrowser x3dBrowser;
		X3DScene mainScene;
		JFrame testFrame = new JFrame();
		testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = testFrame.getContentPane();
		HashMap requestedParameters = new HashMap();
		X3DComponent x3dComp = BrowserFactory.createX3DComponent(requestedParameters);
		JComponent x3dPanel = (JComponent) x3dComp.getImplementation();
		contentPane.add(x3dPanel, BorderLayout.CENTER);
		x3dBrowser = x3dComp.getBrowser();
		testFrame.setSize(600, 500);
		testFrame.setVisible(true);

		ProfileInfo profile = null;
		try {
			profile = x3dBrowser.getProfile("Immersive");
		} catch (NotSupportedException nse) {
			System.out.println("Immersive Profile not supported");
			System.exit(-1);
		}
		mainScene = x3dBrowser.createScene(profile, null);

		int[] iValues = { 2, 1, 0, -1 };
		float[][] mfValues = { { 0, 0, 1 }, { 10, 10, 1 }, { 10, 0, 1 } };

		x3dBrowser.beginUpdate();
		X3DNode node = mainScene.createNode("Shape");
		SFNode sChNode = (SFNode) (node.getField("geometry"));

		X3DNode indexedFaceSet = mainScene.createNode("IndexedFaceSet");
		X3DNode coordNode = mainScene.createNode("Coordinate");

		X3DField coord = indexedFaceSet.getField("coord");
		SFNode sCoord = (SFNode) coord;
		sCoord.setValue(coordNode);
		X3DField point = coordNode.getField("point");
		MFVec3f mPoint = (MFVec3f) point;
		mPoint.setValue(3, mfValues);

		X3DField coordIndex = indexedFaceSet.getField("coordIndex");
		MFInt32 mCoordIndex = (MFInt32) coordIndex;
		mCoordIndex.setValue(4, iValues);

		sChNode.setValue(indexedFaceSet);

		mainScene.addRootNode(node);
		x3dBrowser.endUpdate();

		System.out.println("Checking boundary case one.");

		x3dBrowser.beginUpdate();
		X3DNode transform = mainScene.createNode("Transform");
		MFNode addChildren = (MFNode) transform.getField("addChildren");
		System.out.println("addChilren readable?" + addChildren.isReadable()
				+ " writable?" + addChildren.isWritable() + " Node realized?"
				+ transform.isRealized());
		boolean okay = !addChildren.isReadable() && !addChildren.isWritable()
				&& !transform.isRealized();
		if (okay)
			System.out.println("Case one part one okay.");
		else
			System.err.println("Case one part one bad.");
		mainScene.addRootNode(transform);
		System.out.println("addChilren readable?" + addChildren.isReadable()
				+ " writable?" + addChildren.isWritable() + " Node realized?"
				+ transform.isRealized());
		okay = !addChildren.isReadable() && addChildren.isWritable()
				&& transform.isRealized();
		if (okay)
			System.out.println("Case one part two okay.");
		else
			System.err.println("Case one part two bad.");
		x3dBrowser.endUpdate();
		x3dBrowser.beginUpdate();
		addChildren.setValue(1, new X3DNode[] { null });
		x3dBrowser.endUpdate();

		System.out.println("Checking boundary case two.");
		x3dBrowser.beginUpdate();
		X3DNode shape2=mainScene.createNode("Shape");
		X3DNode box=mainScene.createNode("Box");
		SFVec3f boxSize=(SFVec3f)box.getField("size");
		System.out.println("size readable?"+boxSize.isReadable()+" isWritable?"+boxSize.isWritable()+" realized?"+box.isRealized());
		boxSize.setValue(new float[]{10.0f,0.5f,2.5f});
		((SFNode)shape2.getField("geometry")).setValue(box);
		System.out.println("size readable?"+boxSize.isReadable()+" isWritable?"+boxSize.isWritable()+" realized?"+box.isRealized());
		addChildren.setValue(1, new X3DNode[] {shape2});
		System.out.println("Shape realized?"+shape2.isRealized());
		x3dBrowser.endUpdate();
		
	}

}