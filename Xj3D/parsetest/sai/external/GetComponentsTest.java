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

import org.web3d.x3d.sai.ComponentInfo;
import org.web3d.x3d.sai.ExternalBrowser;

/**
 * Test that the browser can be created and that getComponents[] works.
 */

public class GetComponentsTest {
  public static void main(String[] args) {
    System.out.println("Checking components supported by browser.");
    ExternalBrowser b=SAITestFactory.getBrowser();
    ComponentInfo info[]=b.getSupportedComponents();
    if (info!=null) {
      System.out.println("Number of supported components: "+info.length);
      for (int counter=0; counter<info.length; counter++)
        System.out.println(info[counter].toX3DString());
    } else
      System.out.println("Null result from getSupportedComponents.");
  }
}
