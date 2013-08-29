//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.navigator;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import javax.swing.SwingUtilities;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.Component;

import com.norkart.virtualglobe.viewer.PerspectiveCamera;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class WalkPerspectiveNavigationAdapter
        extends MouseKeyNavigationAdapter {
    PerspectiveCamera camera;
  /*
   * Mouse and key listeners
   */
    // private boolean inertia = false;
    private int             x_last;
    private int             y_last;
    private long            time_last;
    
    //private double          x_factor = 0.001;
    //private double          y_factor = 0.001;
    
    private final double    max_speed = 50;
    private final double    speed_step = Math.pow(max_speed, 1./20);
    
    
    
    public WalkPerspectiveNavigationAdapter(PerspectiveCamera camera) {
        super(camera.getNavigator());
        this.camera = camera;
    }
    
    // protected GlobeNavigator.FlyUpdater updater = null;
    
    
    protected void setSpeedForward(double v) {
        navigator.getWalkFlyUpdater().setSpeedForward(v);
    }
    
    protected void stopSpeed() {
        navigator.getWalkFlyUpdater().stopSpeed();
    }
    
    public void mousePressed(MouseEvent e) {
        if (navigator == null) return;
        if (SwingUtilities.isMiddleMouseButton(e) ||
                (SwingUtilities.isLeftMouseButton(e) &&
                SwingUtilities.isRightMouseButton(e)) ||
                (SwingUtilities.isLeftMouseButton(e) &&
                e.isAltDown())) {
      /*
      if (inertia)
        setSpeedForward(0);*/
        } else if (SwingUtilities.isRightMouseButton(e) ||
                (SwingUtilities.isLeftMouseButton(e) &&
                e.isControlDown())) {
     /* if (inertia) {
        setSpeedUpdown(0);
        setSpeedSideways(0);
      }*/
        }
        navigator.fireDrawNow(this);
    }
    
    /**
     * Receive mouse event.
     * Mouse move (without buttonpress).
     */
    public void mouseMoved(MouseEvent e) {
        
        x_last = e.getX();
        y_last = e.getY();
        time_last = System.currentTimeMillis();
    }
    
    /**
     * Receive mouse event.
     * Mouse move, with buttonpress. Does all the work.
     */
    public void mouseDragged(MouseEvent e) {
        
        int x = e.getX();
        int y = e.getY();
        long time = System.currentTimeMillis();
        double factor = (e.isShiftDown() ? 4. : 1.)*camera.getFov()*Math.PI/180./e.getComponent().getWidth();
        double x_angle = (x-x_last)*factor;
        double y_angle = (y-y_last)*factor;
        // System.out.println("X ang " + x_angle + " y ang " + y_angle);
        
        
        if (navigator == null) return;
        
        navigator.getWalkFlyUpdater();
        if (SwingUtilities.isRightMouseButton(e) ||
                (SwingUtilities.isLeftMouseButton(e) &&
                e.isAltDown())) {
      /*if (inertia) {
        if (time>time_last) {
          setSpeedUpdown(y_angle  * mult * navigator.getTerrainHeight()*1000/(time-time_last)/navigator.getSpeedFactor());
          setSpeedSideways(x_angle * mult * navigator.getTerrainHeight()*1000/(time-time_last)/navigator.getSpeedFactor());
        }
      }
       else
       */
            
            navigator.rotateAroundPointer(x_angle, y_angle);
            //navigator.translateUpDown(y_angle  * mult * navigator.getTerrainHeight());
            //navigator.translateSideway(x_angle * mult * navigator.getTerrainHeight());
            
        } else if (SwingUtilities.isMiddleMouseButton(e) ||
                (SwingUtilities.isLeftMouseButton(e) &&
                SwingUtilities.isRightMouseButton(e)) ||
                (SwingUtilities.isLeftMouseButton(e) &&
                e.isControlDown())) {
      /*
      if (inertia) {
        if (time>time_last)
          setSpeedForward(y_angle * mult * navigator.getTerrainHeight()*1000/(time-time_last)/navigator.getSpeedFactor());
      }
      else*/
            // navigator.translateForward(y_angle * mult * navigator.getTerrainHeight());
            navigator.rotateAz(-x_angle);
            double ha = navigator.getHeightAngle() + y_angle;
            if (ha >  Math.PI/2) ha =  Math.PI/2;
            if (ha < -Math.PI/2) ha = -Math.PI/2;
            navigator.setHeightAngle(ha);
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            double dip = navigator.getHeightAngle()-(y-e.getComponent().getHeight()/2)*factor;
            double d = navigator.getPointerDist()*Math.sin(y_angle)/Math.sin(Math.PI-Math.abs(y_angle)-Math.abs(dip));
            if (Math.abs(d) > navigator.getTerrainHeight()/10)
                d = navigator.getTerrainHeight()*(d>=0?.1:-.1);
            navigator.translate(d, navigator.getAzimut(), 0, true);
            
            d = navigator.getPointerDist()*Math.sin(x_angle);
            if (Math.abs(d) > navigator.getTerrainHeight()/10)
                d = navigator.getTerrainHeight()*(d>=0?.1:-.1);
            navigator.translate(d, navigator.getAzimut()-Math.PI/2, 0, true);
        }
        
        x_last = x;
        y_last = y;
        time_last = time;
        navigator.fireDrawNow(this);
    }
    
    public void mouseWheelMoved(MouseWheelEvent e) {
        
        double y_angle = e.getWheelRotation()*0.2;
        if (navigator == null) return;
        navigator.getWalkFlyUpdater();
        navigator.translateForward(y_angle * (e.isShiftDown() ? 4. : 1.) * navigator.getTerrainHeight());
        navigator.fireDrawNow(this);
    }
    
    public void keyPressed(KeyEvent e) {
        double step = 0.05*(e.isShiftDown() ? 4. : 1.);
        
        if (navigator == null) return;
        navigator.getWalkFlyUpdater();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                if (e.isControlDown())
                    navigator.rotateAz(step);
                else
                    navigator.translateSideway(-step * navigator.getTerrainHeight());
                break;
            case KeyEvent.VK_LEFT:
                if (e.isControlDown())
                    navigator.rotateAz(-step);
                else
                    navigator.translateSideway(step * navigator.getTerrainHeight());
                break;
            case KeyEvent.VK_UP:
                if (e.isAltDown())
                    navigator.translateUpDown(step * navigator.getTerrainHeight());
                else if (e.isControlDown()) {
                    double ha = navigator.getHeightAngle() + step;
                    if (ha >  Math.PI/2) ha =  Math.PI/2;
                    if (ha < -Math.PI/2) ha = -Math.PI/2;
                    navigator.setHeightAngle(ha);
                } else
                    navigator.translate(step * navigator.getTerrainHeight(), navigator.getAzimut(), 0, true);
                break;
            case KeyEvent.VK_DOWN:
                if (e.isAltDown())
                    navigator.translateUpDown(-step * navigator.getTerrainHeight());
                else if (e.isControlDown()) {
                    double ha = navigator.getHeightAngle() - step;
                    if (ha >  Math.PI/2) ha =  Math.PI/2;
                    if (ha < -Math.PI/2) ha = -Math.PI/2;
                    navigator.setHeightAngle(ha);
                } else
                    navigator.translate(step * navigator.getTerrainHeight(), navigator.getAzimut()+Math.PI, 0, true);
                
                // navigator.rotateHa(-step);
                break;
            case KeyEvent.VK_PAGE_UP:
                navigator.translateForward(step * navigator.getTerrainHeight());
                break;
            case KeyEvent.VK_PAGE_DOWN:
                navigator.translateForward(-step * navigator.getTerrainHeight());
                break;
            case KeyEvent.VK_A: {
                double speed = navigator.getWalkFlyUpdater().getSpeedForward()*max_speed;
                double ss = e.isShiftDown() ? speed_step * speed_step * speed_step * speed_step * speed_step : speed_step;
                if (speed < -1)
                    speed /= ss;
                else if (speed < 0)
                    speed = 0;
                else if (speed > 1)
                    speed *= ss;
                else
                    speed = ss;
                if (speed > max_speed) speed = max_speed;
                setSpeedForward(speed/max_speed);
                break;
            }
            case KeyEvent.VK_Z: {
                double speed = navigator.getWalkFlyUpdater().getSpeedForward()*max_speed;
                double ss = e.isShiftDown() ? speed_step * speed_step * speed_step * speed_step * speed_step : speed_step;
                if (speed > 1)
                    speed /= ss;
                else if (speed > 0)
                    speed = 0;
                else if (speed < -1)
                    speed *= ss;
                else
                    speed = -ss;
                if (speed < -max_speed) speed = -max_speed;
                setSpeedForward(speed/max_speed);
                break;
            }
            case KeyEvent.VK_SPACE:
                stopSpeed();
                break;
        }
        
        navigator.fireDrawNow(this);
    }
    
    public final AbstractAction stopAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            stopSpeed();
        }
    };
    public final AbstractAction accelerateAction = new AbstractAction() {
        public void actionPerformed(ActionEvent ev) {
            
            double speed = navigator.getWalkFlyUpdater().getSpeedForward()*max_speed;
            double ss = speed_step * speed_step * speed_step * speed_step * speed_step;
            if (speed < -1)
                speed /= ss;
            else if (speed < 0)
                speed = 0;
            else if (speed > 1)
                speed *= ss;
            else
                speed = ss;
            if (speed > max_speed) speed = max_speed;
            navigator.getWalkFlyUpdater().setSpeedForward(speed/max_speed);
        }
    };
    public final AbstractAction deccelerateAction = new AbstractAction() {
        public void actionPerformed(ActionEvent ev) {
            double speed = navigator.getWalkFlyUpdater().getSpeedForward()*max_speed;
            double ss = speed_step * speed_step * speed_step * speed_step * speed_step;
            if (speed > 1)
                speed /= ss;
            else if (speed > 0)
                speed = 0;
            else if (speed < -1)
                speed *= ss;
            else
                speed = -ss;
            if (speed < -max_speed) speed = -max_speed;
            navigator.getWalkFlyUpdater().setSpeedForward(speed/max_speed);
        }
    };
}
