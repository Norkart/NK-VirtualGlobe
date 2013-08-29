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
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

/**
 * Test that the getRootNodes method works.
 */

public class GetRootNodesTest {
  public static void main(String[] args) {
    System.out.println("The root nodes in a scene.");
    ExternalBrowser b=SAITestFactory.getBrowser();
    X3DScene s=b.createX3DFromString(
        "#VRML V3.0 utf8\n"+
        "PROFILE Interactive\n"+
        "DEF a Group {}\n"+
        "DEF b Group {}\n"+
        "DEF c Transform {}\n"+
        "DEF d Viewpoint {}\n"+
        "ROUTE a.children_changed TO a.set_children\n"+
        "ROUTE b.children_changed TO c.set_children"
    );
    X3DNode rootNodes[]=s.getRootNodes();
    for (int counter=0; counter<rootNodes.length; counter++) {
        System.out.println("Root #"+counter+" is "+rootNodes[counter].getNodeName());
    }
    System.out.println("Done.");
  }
}
