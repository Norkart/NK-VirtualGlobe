//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components;

import com.norkart.virtualglobe.cache.CacheManager;
import org.w3c.dom.*;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public interface DomLoadable {
  static public class LoadException extends Exception {
    public LoadException(String message) {
      super(message);
    }
  }

  public void    load(Element domElement) throws LoadException;
  public Element save(Document doc);
}