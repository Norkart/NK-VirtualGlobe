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
import java.util.HashMap;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.MFInt32;
import org.web3d.x3d.sai.MFVec3f;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DScene;

/** Test of constructing and manipulating an indexed line set using the SAI */
public class SAIIndexedLinesetTest {

	public static void main(String[] args) {
		HashMap suppliedParams=new HashMap();
	    suppliedParams.put("Xj3D_ConsoleShown",Boolean.TRUE);
	    suppliedParams.put("Xj3D_LocationShown",Boolean.FALSE);
	    //suppliedParams.put("Xj3D_RendererType","Java3D");
		X3DComponent comp=BrowserFactory.createX3DComponent(suppliedParams);
		ExternalBrowser browser=comp.getBrowser();

		Frame f=new Frame();
		f.setLayout(new BorderLayout());
		f.setBackground(Color.blue);
		f.add((Component)comp, BorderLayout.CENTER);
		f.setSize(100,100);
		f.show();
	X3DScene scene=browser.createX3DFromString(
			"#X3D V3.0 utf8\n"+
			"PROFILE Immersive\n"+
			"Shape {\n"+
			"  appearance Appearance {\n"+
			"  material Material {\n"+
			"    emissiveColor 1 1 1\n"+
			"  }\n"+
			"  }\n"+
			"  geometry DEF LineSet IndexedLineSet{\n"+
			"  coord DEF XYcoord Coordinate {\n"+
			"    point []\n"+
			"  }\n"+
			"  coordIndex []\n"+
			"  }\n"+
			"}\n"
	);
	SAIWaitForBrowserInit waiter=new SAIWaitForBrowserInit(browser);
	browser.replaceWorld(scene);
	waiter.waitForInit();
	MFVec3f points=(MFVec3f) scene.getNamedNode("XYcoord").getField("point");
	MFInt32 indices=(MFInt32) scene.getNamedNode("LineSet").getField("set_coordIndex");
	browser.beginUpdate();
	points.setValue(3,new float[]{2,0,-1,2,2,2,-2,-2,-2});
	indices.setValue(9,new int[]{0,1,-1,0,2,-1,1,2,-1});
	browser.endUpdate();
	}
}
