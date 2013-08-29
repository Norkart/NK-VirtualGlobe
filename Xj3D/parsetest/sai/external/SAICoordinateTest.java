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
 * A test of Coordinate, IndexedLineSet, and set1Value on MFVec3f's.
 * The test is using IndexedLineSets because I couldn't come up with a
 * simple manipulation that wouldn't end up shredding polygons.
 * <P>
 * Most importantly, this test should plod along at moving two points
 * per second.  If it speeds up, that means that the set1Value coalescing
 * isn't working correctly.
 */

public class SAICoordinateTest {

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
    // Gosh its annoying to walk down to a Coordinate node from
    // a peice of geometry.
    X3DNode levelTwo[]=SAIUtilities.extractRootNodes(browser.createX3DFromString(
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
    ));
    ((MFNode)nodes[0].getField("set_children")).setValue(levelOne.length,levelOne);
    ((MFNode)levelOne[1].getField("set_children")).setValue(levelTwo.length,levelTwo);

    X3DNode children[]=new X3DNode[((MFNode)(levelTwo[0].getField("children"))).getSize()];
    ((MFNode)(levelTwo[0].getField("children"))).getValue(children);
    X3DNode geometry=((SFNode)(children[0].getField("geometry"))).getValue();
    X3DNode coord=((SFNode)(geometry.getField("coord"))).getValue();
    MFVec3f coordOutput=(MFVec3f)
      (coord.getField("point"));
    MFVec3f coordInput=(MFVec3f)
      (coord.getField("point"));
    coordOutput.addX3DEventListener(new BasicSAICoordinateMutator(browser,coordOutput,coordInput));
    float temp[]=new float[coordOutput.getSize()*3];
    coordOutput.getValue(temp);
    coordInput.setValue(temp.length/3,temp);
  }
}
