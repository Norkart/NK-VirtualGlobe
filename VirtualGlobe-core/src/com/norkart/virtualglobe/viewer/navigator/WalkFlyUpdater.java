//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.navigator;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author unascribed
 * @version 1.0
 */


public class WalkFlyUpdater extends NavigatorUpdater {
  private double          v_forward  = 0;
  // private double          v_sideways = 0;
  // private double          v_up_down  = 0;
  private long            prev_time  = 0;

  public WalkFlyUpdater(GlobeNavigator navigator) {
    super(navigator);
  }

  public double getSpeedForward() {
    return v_forward;
  }
/*
  public double getSpeedSideways() {
    return v_sideways;
  }

   public double getSpeedUpdown() {
     return v_up_down;
  }
*/
  public void setSpeedForward(double v_forward) {
    this.v_forward = v_forward;
  }
/*
  public synchronized void setSpeedSideways(double v_sideways) {
    this.v_sideways = v_sideways;
    notify();
  }

  public synchronized void setSpeedUpdown(double v_up_down) {
    this.v_up_down = v_up_down;
    notify();
  }
*/
  public void stopSpeed() {
    v_forward = 0; //v_sideways = v_up_down = 0;
  }

  public  void update() {
    if (v_forward != 0/* || v_sideways != 0 || v_up_down != 0*/) {
      long curr_time = System.currentTimeMillis();
      if (prev_time != 0) {
        double s = (curr_time-prev_time)*navigator.getMaxSpeed()/1000;
        // System.out.println("Max: " + getMaxSpeed() + " vf " + v_forward + " Speed : " + getMaxSpeed()*v_forward);
        // if (v_forward != 0)
          navigator.translateForward(s*v_forward);
        /*
        if (v_sideways != 0)
          translateSideway(s*v_sideways);
        if (v_up_down != 0)
          translateUpDown(s*v_up_down);*/
      }
      prev_time = curr_time;
    }
    else
      prev_time = 0;
  }

  public boolean isActive() {
    return v_forward != 0;
  }
  }
