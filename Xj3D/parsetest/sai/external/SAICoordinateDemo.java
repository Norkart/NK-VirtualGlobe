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
import java.util.Random;

import org.web3d.x3d.sai.*;

/**
 * A test of Coordinate, IndexedLineSet, and set1Value on MFVec3f's.
 * The test is using IndexedLineSets because I couldn't come up with a
 * simple manipulation that wouldn't end up shredding polygons.
 * <P>
 */

public class SAICoordinateDemo {

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
    X3DScene scene=browser.createX3DFromString(
      "PROFILE Interactive\n"+
      "DEF Root Transform {}"
    );
    X3DNode nodes[]=scene.getRootNodes();
    System.out.println("Number of nodes from create:"+nodes.length);
    System.out.println("Replacing world...");
    browser.replaceWorld(scene);
    System.out.println("World replaced.");

/*    X3DNode levelOne[]=SAIUtilities.extractRootNodes(browser.createX3DFromString(
        "PROFILE Interactive\n"+
	    "Viewpoint {}\n"+
	    "Group {}\n"
    ));*/
    // Gosh its annoying to walk down to a Coordinate node from
    // a peice of geometry.
    X3DScene tempScene = browser.createX3DFromString(
      "PROFILE Interactive\n"+
      "Transform {\n"+
      "  children [\n"+
      "    Shape {\n"+
      "      appearance Appearance {\n"+
      "        texture PixelTexture {\n"+
      "          image 0 0 0\n"+
      "        }\n"+
      "        material Material {\n"+
      "          emissiveColor 1 0 0\n"+
      "        }\n"+
      "      }\n"+
      "      geometry IndexedLineSet {\n"+
      "        color Color {\n"+
      "          color [\n"+
      "            1 0 0  0 1 0  0 0 1  0 1 1  1 0 1  1 1 0\n"+
      "          ]\n"+
      "        }\n"+
      "        colorPerVertex FALSE\n"+
      "        coord Coordinate {\n"+
      "          point [\n"+
      "             0  0  0\n"+
      "             5  0  0\n"+
      "             0  5  0\n"+
      "             0  0  5\n"+
      "             0  0 -5\n"+
      "             0 -5  0\n"+
      "            -5  0  0\n"+
      "          ]\n"+
      "        }\n"+
      "        coordIndex [0 1 -1 0 2 -1 0 3 -1 0 4 -1 0 5 -1 0 6]\n"+
      "      }\n"+
      "    }\n"+
      "  ]\n"+
      "}"
    );
    X3DNode tempNodes[]=tempScene.getRootNodes();
    for (int counter=0; counter<tempNodes.length; counter++)
    	tempScene.removeRootNode(tempNodes[counter]);
    ((MFNode)nodes[0].getField("set_children")).setValue(tempNodes.length,tempNodes);

    X3DNode children[]=new X3DNode[((MFNode)(tempNodes[0].getField("children"))).getSize()];
    ((MFNode)(tempNodes[0].getField("children"))).getValue(children);
    X3DNode geometry=((SFNode)(children[0].getField("geometry"))).getValue();
    X3DNode coord=((SFNode)(geometry.getField("coord"))).getValue();
    MFVec3f coordOutput=(MFVec3f)
      (coord.getField("point"));
    MFVec3f coordInput=(MFVec3f)
      (coord.getField("point"));
    coordOutput.addX3DEventListener(new BasicSAICoordinateMutatorOne(browser,coordOutput,coordInput));
    float temp[]=new float[coordOutput.getSize()*3];
    coordOutput.getValue(temp);
    coordInput.setValue(temp.length/3,temp);
  }
}

class BasicSAICoordinateMutatorOne implements X3DFieldEventListener {
	  ExternalBrowser theBrowser;
	  Random r;
	  int loopCounter;
	  int loopTop;
	  float positions[];
	  float originalPositions[];
	  float raised_positions[][];
	  float buffer[];
	  MFVec3f theDest;
	  MFVec3f theSource;
	  
	  float lastEventTime;

	  public BasicSAICoordinateMutatorOne(
	    ExternalBrowser b, MFVec3f source, MFVec3f dest
	  ) {
	    r=new Random();
	    theDest=dest;
	    theSource=source;
	    loopCounter=0;
	    loopTop=20;
	    theBrowser=b;
	    buffer=new float[3];
	  }

	  public void readableFieldChanged(X3DFieldEvent evt) {
		  //System.out.println("Timestamp"+evt.getTime());
	      loopCounter++;
	      if (loopCounter==loopTop) {
	         //System.out.println(".*.");
	         loopCounter=0;
	      }
	      /** Get the event, change it, and then send a new value. */
	      MFVec3f src=(MFVec3f)(evt.getSource());
	      int size=src.getSize();
	            // Random movement of two points.
	            theBrowser.beginUpdate();
	            int index=Math.abs(r.nextInt() % size);
	            src.get1Value(index,buffer);
	            buffer[0]=buffer[0]-0.5f+r.nextFloat();
	            buffer[1]=buffer[0]-0.5f+r.nextFloat();
	            buffer[2]=buffer[0]-0.5f+r.nextFloat();
	            theDest.set1Value(index,buffer);
	            theDest.set1Value(index,buffer);
	            index=Math.abs(r.nextInt() % size);
	            src.get1Value(index,buffer);
	            buffer[0]=buffer[0]-0.5f+r.nextFloat();
	            buffer[1]=buffer[0]-0.5f+r.nextFloat();
	            buffer[2]=buffer[0]-0.5f+r.nextFloat();
	            theDest.set1Value(index,buffer);
	            theBrowser.endUpdate();

		  

	  }

	}


