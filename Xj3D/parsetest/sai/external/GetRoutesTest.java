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

import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.X3DRoute;
import org.web3d.x3d.sai.X3DScene;

/**
 * Test that the route query feature works.
 */

public class GetRoutesTest {
	
	/** The scene to test with */
	static String scene=        
		"#VRML V3.0 utf8\n"+
		"PROFILE Interactive\n"+
		"DEF a Group {}\n"+
		"DEF b Group {}\n"+
		"DEF c Transform {}\n"+
		"DEF d PositionInterpolator {}\n"+
		"ROUTE a.children_changed TO a.set_children\n"+
		"ROUTE d.value_changed TO c.set_translation";
		
  public static void main(String[] args) {
    System.out.println("The routes in a scene.");
    ExternalBrowser b=SAITestFactory.getBrowser();
    X3DScene s=b.createX3DFromString(scene);
    System.out.println(scene);
    System.out.println("And the routes are:");
    X3DRoute routes[]=s.getRoutes();
    for (int counter=0; counter<routes.length; counter++) {
        System.out.println("Route #"+counter+" is "+routes[counter]);
        System.out.println(" is from "+routes[counter].getSourceNode().getNodeName()
        +"."+routes[counter].getSourceField());
        System.out.println(" is to "+routes[counter].getDestinationNode().getNodeName()
        +"."+routes[counter].getDestinationField());
    }
    System.out.println("Done.");
  }
}
