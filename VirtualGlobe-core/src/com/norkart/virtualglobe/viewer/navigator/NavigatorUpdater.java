//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.navigator;

import com.norkart.geopos.Ellipsoid;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author unascribed
 * @version 1.0
 */


public abstract class NavigatorUpdater {
  protected GlobeNavigator navigator;
  protected Ellipsoid.DistAz daz;
  protected Ellipsoid.LatLonAz llaz;

  abstract void update();
  abstract public boolean isActive();

  NavigatorUpdater(GlobeNavigator navigator) {
    this.navigator = navigator;
  }

}

