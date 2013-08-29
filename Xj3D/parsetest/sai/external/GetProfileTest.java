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
import org.web3d.x3d.sai.ProfileInfo;

/**
 * Test that the browser can be created and that getProfiles[] works.
 */

public class GetProfileTest {
  public static void main(String[] args) {
    System.out.println("Displaying profiles supported by generic browser.");
    ExternalBrowser b=SAITestFactory.getBrowser();
    ProfileInfo info[]=b.getSupportedProfiles();
    if (info!=null) {
      System.out.println("Supports "+info.length+" profiles.");
      for (int counter=0; counter<info.length; counter++)
        System.out.println(info[counter].toX3DString());
    } else {
      System.out.println("Null result from profiles.");
    }
    System.out.println("Done.");
  }
}
