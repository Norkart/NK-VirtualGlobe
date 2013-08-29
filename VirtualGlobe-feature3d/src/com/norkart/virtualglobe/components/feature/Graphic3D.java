//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components.feature;

import org.w3c.dom.*;
import com.norkart.virtualglobe.cache.CacheManager;
import com.norkart.virtualglobe.components.DomLoadable;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public abstract class Graphic3D implements DomLoadable {
  Feature3DPoint feature;

  public Graphic3D(Feature3DPoint feature) {
    this.feature = feature;
  }
  
  public CacheManager getCacheManager() {
      return feature.getCacheManager();
  }
}