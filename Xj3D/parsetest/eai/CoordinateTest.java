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

import vrml.eai.Browser;
import vrml.eai.Node;
import vrml.eai.field.*;


/**
 * A test of Coordinate, IndexedLineSet, and set1Value on MFVec3f's.
 * The test is using IndexedLineSets because I couldn't come up with a
 * simple manipulation that wouldn't end up shredding polygons.
 * <P>
 * Most importantly, this test should plod along at moving two points
 * per second.  If it speeds up, that means that the set1Value coalescing
 * isn't working correctly.
 */

public class CoordinateTest {

  public static void main(String[] args) {
    Browser browser=TestFactory.getBrowser();

    browser.addBrowserListener(new GenericBrowserListener());
    Node nodes[]=browser.createVrmlFromString(
      "DEF Root Transform {}"
    );
    System.out.println("Number of nodes from create:"+nodes.length);
    System.out.println("Replacing world...");
    browser.replaceWorld(nodes);
    System.out.println("World replaced.");

    Node levelOne[]=browser.createVrmlFromString(
      "Viewpoint {}\n"+
      "Group {}\n"
    );
    // Gosh its annoying to walk down to a Coordinate node from
    // a peice of geometry.
    Node levelTwo[]=browser.createVrmlFromString(
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
    ((EventInMFNode)nodes[0].getEventIn("set_children")).setValue(levelOne);
    ((EventInMFNode)levelOne[1].getEventIn("set_children")).setValue(levelTwo);

    Node[] children=((EventOutMFNode)(levelTwo[0].getEventOut("children"))).getValue();
    Node geometry=((EventOutSFNode)(children[0].getEventOut("geometry"))).getValue();
    Node coord=((EventOutSFNode)(geometry.getEventOut("coord"))).getValue();
    EventOutMFVec3f coordOutput=(EventOutMFVec3f)
      (coord.getEventOut("point"));
    EventInMFVec3f coordInput=(EventInMFVec3f)
      (coord.getEventIn("point"));
    coordOutput.addVrmlEventListener(
      new BasicCoordinateMutator(browser,coordOutput,coordInput)
    );
    coordInput.setValue(coordOutput.getValue());
  }
}
