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
import java.awt.Toolkit;
import java.awt.Component;
import com.norkart.virtualglobe.viewer.MapView;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class MapNavigationAdapter extends MouseKeyNavigationAdapter {
    private MapView camera;
  /*
   * Mouse and key listeners
   */
    private int             x_last;
    private int             y_last;
    
    private double          x_factor = 0.001;
    private double          y_factor = 0.001;
    
    
    // private final double    max_speed = 50;
    // private final double    speed_step = Math.pow(max_speed, 1./20);
    
    public MapNavigationAdapter(MapView camera) {
        super(camera.getNavigator());
        this.camera = camera;
    }
    
    /**
     * Receive mouse event.
     * Mouse move (without buttonpress).
     */
    public void mouseMoved(MouseEvent e) {
        x_last = e.getX();
        y_last = e.getY();
    }
    
    /**
     * Receive mouse event.
     * Mouse move, with buttonpress. Does all the work.
     */
    public void mouseDragged(MouseEvent e) {
        
        int x = e.getX();
        int y = e.getY();
        double dx = x-x_last;
        double dy = y-y_last;
        if (e.isShiftDown()) {
            dx *= 4;
            dy *= 4;
        }
        // long time = System.currentTimeMillis();
        
        boolean north_up = false;
        if (camera instanceof MapView)
            north_up = ((MapView)camera).isNorthUp();
        
        if (navigator == null) return;
        navigator.getWalkFlyUpdater();
        if (SwingUtilities.isRightMouseButton(e) ||
                (SwingUtilities.isLeftMouseButton(e) &&
                e.isAltDown())) {
            int center_x = e.getComponent().getWidth()/2;
            int center_y = (north_up?2:3)*e.getComponent().getHeight()/4;
            int v1_x = x - center_x;
            int v1_y = y - center_y;
            int v2_x = x_last - center_x;
            int v2_y = y_last - center_y;
            
            double az1 = Math.atan2(v1_x, v1_y);
            double az2 = Math.atan2(v2_x, v2_y);
            double daz = az1-az2;
            if (e.isShiftDown())
                daz *= 4;
            if (north_up)
                daz = -daz;
            navigator.rotateAz(daz);
            
        } else if (SwingUtilities.isMiddleMouseButton(e) ||
                (SwingUtilities.isLeftMouseButton(e) &&
                SwingUtilities.isRightMouseButton(e)) ||
                (SwingUtilities.isLeftMouseButton(e) &&
                e.isControlDown())) {
            navigator.translate(dy *y_factor* 2. * navigator.getTerrainHeight(), 0, -Math.PI/2);
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            double d = Math.sqrt(dx*dx+dy*dy);
            double az = Math.atan2(-dx, dy);
            // Compute scale from elevation
            double          scale_factor = 100000;
            int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            
            float w = (float)(.254*((Component)e.getSource()).getWidth()/dpi);
            float h = (float)(.254*((Component)e.getSource()).getHeight()/dpi);
            
            double ch = Math.max(navigator.getEllipsHeight(),
                    navigator.getTerrainHeight());
            double r = navigator.getGlobe() == null ? 0 :
                navigator.getGlobe().getEllipsoid().getN(navigator.getLat());
            
            // Near and far clipping planes
            // double horizon  = 0;
            // horizon += Math.sqrt((2*r + 100.)*100.);
            double horizon = Math.sqrt((2*r + ch)*ch);
            if (camera instanceof MapView)
                horizon /= ((MapView)camera).getHorizonScale();
            
            scale_factor = horizon / Math.min(w, h);
            
            // navigator.translate(dy*.254*scale_factor/dpi);
            // navigator.translate(dx*.254*scale_factor/dpi);
            d *= .254*scale_factor/dpi;
            if (north_up)
                navigator.translate(d, az, 0);
            else {
                az += navigator.getAzimut();
                navigator.translate(d, az, 0, true);
            }
            
        }
        
        x_last = x;
        y_last = y;
        navigator.fireDrawNow(this);
    }
    
    public void mouseWheelMoved(MouseWheelEvent e) {
        double y_angle = e.getWheelRotation()*0.2;
        if (navigator == null) return;
        navigator.getWalkFlyUpdater();
        navigator.translate(y_angle * (e.isShiftDown() ? 4. : 1.) * navigator.getTerrainHeight(), 0, -Math.PI/2);
        navigator.fireDrawNow(this);
    }
    
    public void keyPressed(KeyEvent e) {
        double step = 0.05*(e.isShiftDown() ? 4. : 1.);
        boolean north_up = false;
        if (camera instanceof MapView)
            north_up = ((MapView)camera).isNorthUp();
        double az = north_up?0:navigator.getAzimut();
        if (navigator == null) return;
        navigator.getWalkFlyUpdater();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                if (e.isAltDown() || e.isControlDown())
                    navigator.rotateAz(step);
                else
                    navigator.translate(step * navigator.getTerrainHeight(), az + Math.PI/2, 0, !north_up);
                break;
            case KeyEvent.VK_LEFT:
                if (e.isAltDown() || e.isControlDown())
                    navigator.rotateAz(-step);
                else
                    navigator.translate(step * navigator.getTerrainHeight(), az + Math.PI*3/2, 0, !north_up);
                break;
            case KeyEvent.VK_UP:
                if (e.isAltDown())
                    navigator.translate(step * navigator.getTerrainHeight(), 0, Math.PI/2);
                else if (e.isControlDown())
                    navigator.translate(step * navigator.getTerrainHeight(), navigator.getAzimut(), 0, true);
                else
                    navigator.translate(step * navigator.getTerrainHeight(), az, 0, !north_up);
                break;
            case KeyEvent.VK_DOWN:
                if (e.isAltDown())
                    navigator.translate(step * navigator.getTerrainHeight(), 0, -Math.PI/2);
                else if (e.isControlDown())
                    navigator.translate(step * navigator.getTerrainHeight(), navigator.getAzimut()+Math.PI, 0, true);
                else
                    navigator.translate(step * navigator.getTerrainHeight(), az+Math.PI, 0, !north_up);
                break;
            case KeyEvent.VK_PAGE_UP:
                navigator.translate(step * navigator.getTerrainHeight(), 0, Math.PI/2);
                break;
            case KeyEvent.VK_PAGE_DOWN:
                navigator.translate(step * navigator.getTerrainHeight(), 0, -Math.PI/2);
                break;
       /*
     case KeyEvent.VK_A: {
       double speed = getFlyUpdater().getSpeedForward()*max_speed;
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
     double speed = getFlyUpdater().getSpeedForward()*max_speed;
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
        */
        }
        
        navigator.fireDrawNow(this);
    }
}