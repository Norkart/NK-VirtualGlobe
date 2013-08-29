//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.navigator;

import java.util.List;
import java.util.Iterator;

import com.norkart.geopos.Ellipsoid;
import com.norkart.virtualglobe.util.PathPoint;
// import com.norkart.virtualglobe.components.FlyPath;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author unascribed
 * @version 1.0
 */

public class PathUpdater extends ChainedUpdater {
  protected long timeleft;
  protected long prevtime = Long.MAX_VALUE;
  protected double to_lat;
  protected double to_lon;
  protected double to_h;
  protected double to_ha;
  protected double to_az;
  protected List<PathPoint> path;
  protected boolean loop;
  protected Iterator vp_it;

  public PathUpdater(GlobeNavigator navigator, List<PathPoint> path) {
    super(navigator);
    this.path = path;
    vp_it = path.iterator();
    if (vp_it.hasNext()) {
      PathPoint pp = (PathPoint)vp_it.next();
      setValues(pp.data, pp.movetime);
    }
  }

  public PathUpdater(GlobeNavigator navigator, double [] vp_data, long movetime) {
    super(navigator);
    setValues(vp_data, movetime);
  }

  protected void setValues(double [] vp_data, long movetime) {
    timeleft += movetime;
    to_lon = vp_data[0];
    to_lat = vp_data[1];
    to_h   = vp_data[2];
    to_az  = vp_data[3];
    to_ha  = vp_data[4];
  }

  public boolean isActive() {
    return timeleft > 0 || (path != null && loop) || (vp_it != null && vp_it.hasNext());
  }

  public  void update() {
    if (navigator.getGlobe() == null) return;
    long currtime = System.currentTimeMillis();
    long interval = currtime - prevtime;
    prevtime = currtime;
    if (interval <= 0) return;
    timeleft -= interval;
    while (timeleft < interval) {
      if (vp_it != null && vp_it.hasNext()) {
        PathPoint pp = (PathPoint)vp_it.next();
        setValues(pp.data, pp.movetime);
      }
      else if (path != null && loop) {
        vp_it = path.iterator();
        if (vp_it.hasNext()) {
          PathPoint pp = (PathPoint)vp_it.next();
          setValues(pp.data, pp.movetime);
        }
      }
      else
        break;
    }
    if (timeleft >= interval) {
      daz = navigator.getGlobe().getEllipsoid().inverseGeodesic(navigator.getLat(), navigator.getLon(), to_lat, to_lon, daz);
      if (daz.dist > 0) {
        llaz = navigator.getGlobe().getEllipsoid().forwGeodesic(navigator.getLat(), navigator.getLon(), daz.dist*interval/timeleft, daz.az12, llaz);
        navigator.setLat(llaz.lat);
        navigator.setLon(llaz.lon);
      }
      navigator.setAzimut(navigator.getAzimut() - Ellipsoid.adjlon(navigator.getAzimut() - to_az)*interval/timeleft);
      navigator.setHeightAngle(navigator.getHeightAngle() - (navigator.getHeightAngle() - to_ha)*interval/timeleft);
      navigator.setEllipsHeight(navigator.getEllipsHeight() - (navigator.getEllipsHeight() - to_h)*interval/timeleft);
    }
    else {
      navigator.setLat(to_lat);
      navigator.setLon(to_lon);
      navigator.setHeightAngle(to_ha);
      navigator.setAzimut(to_az);
      navigator.setEllipsHeight(to_h);
      navigator.setUpdater(next);
    }
  }
}
