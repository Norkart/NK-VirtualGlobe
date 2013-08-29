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
 *   A test to try out createVrmlFromString, and try constructing a scene
 *   graph from components using eventIn's.  In this variant, we're testing
 *   that routes are recovered when a universe is constructed by calling
 *   createVrmlFromString, loadURL, and then adding the created nodes to
 *   the new root node, all to test whether or not the route info
 *   has to survive a scene change.
 *   <P>
 *   The sequence of operations is
 *   1.  setBrowserFactory, getBrowserComponent, getBrowser, etc.
 *   2.  createVrmlFromString
 *   3.  loadURL a new root world
 *   4.  add the created nodes to the world
 *   5.  Expect to see a moving sphere on the display
 *   <P>
 *   This test differs from CreateFromString in that its testing a 
 *   timer node to find out if it works.
 */

public class CreateFromString6 {

  public static void main(String[] args) {
    Browser browser=TestFactory.getBrowser();

    browser.addBrowserListener(new GenericBrowserListener());
    /* The root */
    Node nodes[]=browser.createVrmlFromString(
      "DEF Root Transform {} Transform{}"
    );
    System.out.println("Number of nodes from create:"+nodes.length);

    /* The basic geometry */
    Node levelOne[]=browser.createVrmlFromString(
     "DEF T2 Transform {\n"+
     "  children [\n"+
     "    Group {\n"+
     "      children [\n"+
     "        Group {\n"+
     "          #addChildren IS addChildren\n"+
     "          #set_children IS set_children\n"+
     "          #removeChildren IS removeChildren\n"+
     "          #children_changed IS children_changed\n"+
     "        },\n"+
     "        DEF T1 Transform {\n"+
     "          #set_scale IS set_scale\n"+
     "          #addChildren IS addGeometry\n"+
     "          #set_children IS set_geometry\n"+
     "          #removeChildren IS removeGeometry\n"+
     "          #children_changed IS geometry_changed\n"+
     "        }\n"+
     "      ]\n"+
     "    }\n"+
     "  ]\n"+
     "}\n"+
     "DEF Route_Maker Script {\n"+
     "eventIn SFVec3f set_position #IS set_position\n"+
     "eventIn SFRotation set_rotation #IS set_rotation\n"+
     "eventOut SFVec3f skip_position\n"+
     "eventOut SFRotation skip_rotation\n"+
     "url \"javascript:\n"+
     "function initialize() {\n"+
     "}\n"+
     "function set_rotation(value) {\n"+
     //"  skip_rotation[0]=1.0; skip_rotation[1]=2.0; skip_rotation[2]=3.0;\n"+
     "  skip_rotation=value;\n"+
     "}\n"+
     "function set_position(value, timestamp) {\n"+
     "  skip_position=value;\n"+
     "}\n"+
     "\"\n"+
     "},\n"+
     "ROUTE Route_Maker.skip_position TO T2.set_translation\n"+
     "ROUTE Route_Maker.skip_rotation TO T2.set_rotation\n"
    );

    /* The geometry with routes and time dependencies. */
    Node levelTwo[]=browser.createVrmlFromString(
      "DEF	T TimeSensor {\n"+
      "	startTime 0\n"+
      " stopTime -1\n"+
      "	enabled	TRUE"+
      "	loop TRUE"+
      "	cycleInterval 5"+
      "}"+
      "DEF	I PositionInterpolator {"+
      "	key [0, 1]"+
      "	keyValue [0 0 0 5 0 0]"+
      "}"+
      "DEF	AT Transform {"+
      "	children ["+
      "		Shape {"+
      "			geometry Sphere	{"+
      "			}"+
      "			appearance Appearance {"+
      "				material Material {"+
      "					emissiveColor 0 1 0"+
      "				}"+
      "			}"+
      "		}"+
      "	]"+
      "}"+
      "ROUTE T.fraction_changed TO	I.set_fraction"
    );


    levelTwo[0].getEventOut("fraction_changed").addVrmlEventListener(
      new GenericFieldListener("outer timer")
    );

    levelTwo[1].getEventOut("value_changed").addVrmlEventListener(
      new GenericFieldListener("interp value")
    );
    
    ((EventInMFNode)(nodes[0].getEventIn("addChildren"))).setValue(levelOne);
    ((EventInMFNode)(nodes[1].getEventIn("addChildren"))).setValue(levelTwo);

    // And now do the scene change.

    EAIBrowserInitWaiter waiter=new EAIBrowserInitWaiter();
    browser.addBrowserListener(waiter);
    browser.replaceWorld(nodes);
    try {
        waiter.waitForInit();
    } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    browser.addRoute(levelTwo[1],"value_changed",levelOne[0],"set_position");
  }
}

