//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
/*
 * MyBrowserLauncher.java
 *
 * Created on 9. januar 2006, 09:56
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.norkart.virtualglobe.util;

import java.net.URL;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;


/**
 *
 * @author raa
 */
public class MyBrowserLauncher {

  static BasicService bs = null;
  static {
    try {
      // Lookup the javax.jnlp.BasicService object
      bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
    } catch(UnavailableServiceException ue) {
      // Service is not supported
    }
  }

  /** Creates a new instance of MyBrowserLauncher */
  public MyBrowserLauncher() {
  }


    static public boolean openURL(URL url) {
      try {
        if (bs != null &&  bs.showDocument(url)) return true;
        BrowserLauncher.openURL(url.toString());
        return true;
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
      return false;
    }
}
