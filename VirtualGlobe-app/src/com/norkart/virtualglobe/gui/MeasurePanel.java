/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  MeasurePanel.java
 *
 * Created on 11. mai 2007, 11:11
 *
 */

package com.norkart.virtualglobe.gui;

import com.norkart.virtualglobe.util.ApplicationSettings;
import java.awt.Component;
import java.awt.PointerInfo;
import javax.swing.JPanel;

import javax.vecmath.*;

import javax.media.opengl.GL;

import java.util.*;
import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.nio.FloatBuffer;

import java.text.NumberFormat;
import java.text.DecimalFormat;

import javax.swing.*;

import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;
import com.norkart.geopos.Ellipsoid;
import com.norkart.virtualglobe.util.SpringUtilities;
import com.norkart.virtualglobe.globesurface.GlobeElevationModel;
import com.norkart.virtualglobe.viewer.PostDrawListener;

import com.sun.opengl.util.BufferUtil;
// import com.norkart.VirtualGlobe.Util.Navigation.GlobeNavigatorUpdateListener;

/**
 *
 * @author runaas
 */
public class MeasurePanel extends JPanel implements
        MouseInputListener, PostDrawListener {
    private ArrayList<Point3d> points = new ArrayList();
    private double total_dist  = 0;
    private Ellipsoid.DistAz next_daz = new Ellipsoid.DistAz();
    private double next_dh = 0;
    
    private JFormattedTextField total_dist_field;
    private JFormattedTextField next_dist_field;
    private JFormattedTextField next_dh_field;
    private JFormattedTextField next_az_field;
    
    private GlobeNavigator navigator;
    
    private FloatBuffer line_buffer = BufferUtil.newFloatBuffer(128*3);
    private FloatBuffer point_buffer = BufferUtil.newFloatBuffer(32*3);
    
    private boolean is_completed = false;
    
    /** Creates a new instance of MeasurePanel */
    public MeasurePanel(GlobeNavigator navigator) {
        this.navigator = navigator;
        
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        
        JPanel p = new JPanel();
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setLayout(new SpringLayout());
        
        NumberFormat dist_format =  NumberFormat.getInstance();
        dist_format.setMaximumFractionDigits(1);
        dist_format.setMinimumFractionDigits(1);
        
        total_dist_field = new JFormattedTextField(dist_format);
        total_dist_field.setColumns(10);
        next_dist_field  = new JFormattedTextField(dist_format);
        next_dh_field    = new JFormattedTextField(dist_format);
        next_az_field    = new JFormattedTextField(dist_format);
        
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        p.add(new JLabel(settings.getResourceString("TOTAL_LENGTH")));
        total_dist_field.setEditable(false);
        p.add(total_dist_field);
        
        p.add(new JLabel(settings.getResourceString("NEXT_LENGTH")));
        next_dist_field.setEditable(false);
        p.add(next_dist_field);
        
        p.add(new JLabel(settings.getResourceString("NEXT_DH")));
        next_dh_field.setEditable(false);
        p.add(next_dh_field);
        
        p.add(new JLabel(settings.getResourceString("NEXT_AZ")));
        next_az_field.setEditable(false);
        p.add(next_az_field);
        
        total_dist_field.setHorizontalAlignment(JTextField.RIGHT);
        next_dist_field.setHorizontalAlignment(JTextField.RIGHT);
        next_dh_field.setHorizontalAlignment(JTextField.RIGHT);
        next_az_field.setHorizontalAlignment(JTextField.RIGHT);
        
        SpringUtilities.makeCompactGrid(p, //parent
                4, 2, // row, col
                3, 3,  //initX, initY
                3, 3); //xPad, yPad
        
        add(p);
        
        Box buttonBox = Box.createHorizontalBox();
        buttonBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton rmLastBt = new JButton(settings.getResourceString("CLEAR_LAST"));
        rmLastBt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                deleteLast();
            }
        });
        buttonBox.add(rmLastBt);
        buttonBox.add(Box.createHorizontalGlue());
        
        JButton clearBt = new JButton(settings.getResourceString("CLEAR_ALL"));
        clearBt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                clear();
            }
        });
        buttonBox.add(clearBt);
        
        add(buttonBox);
        
        clear();
        updateGui();
    }
    
    
    private void updateGui() {
        {
            JFormattedTextField.AbstractFormatter formatter = total_dist_field.getFormatter();
            try {
                total_dist_field.setText(formatter.valueToString(new Double(total_dist+next_daz.dist)));
            } catch (java.text.ParseException ex) {}
        }
        {
            JFormattedTextField.AbstractFormatter formatter = next_dist_field.getFormatter();
            try {
                next_dist_field.setText(formatter.valueToString(new Double(next_daz.dist)));
            } catch (java.text.ParseException ex) {}
        }
        {
            JFormattedTextField.AbstractFormatter formatter = next_dh_field.getFormatter();
            try {
                next_dh_field.setText(formatter.valueToString(new Double(next_dh)));
            } catch (java.text.ParseException ex) {}
        }
        {
            JFormattedTextField.AbstractFormatter formatter = next_az_field.getFormatter();
            try {
                next_az_field.setText(formatter.valueToString(new Double(Math.toDegrees(next_daz.az12 >= 0 ? next_daz.az12 : next_daz.az12+Math.PI*2))));
            } catch (java.text.ParseException ex) {}
        }
    }
    
    public void clear() {
        points.clear();
        total_dist = 0;
        next_dh = 0;
        next_daz.dist = 0;
        next_daz.az12 = 0;
        next_daz.az21 = 0;
        
        // Update Fields
        updateProfile();
        updateGui();
    }
    
    private void addPoint() {
        if (is_completed) {
            points.clear();
            total_dist = 0;
            is_completed = false;
        }
        
        // Les verdier fra navigator legg inn punkt
        double lon = navigator.getPointerLon();
        double lat = navigator.getPointerLat();
        double h   = navigator.getPointerH();
        
        if (!points.isEmpty()) {
            Point3d p = points.get(points.size()-1);
            total_dist +=
                    navigator.getGlobe().getEllipsoid().inverseGeodesic(p.y, p.x, lat, lon, next_daz).dist;
        }
        points.add(new Point3d(lon, lat, h));
        
        next_dh = 0;
        next_daz.dist = 0;
        next_daz.az12 = 0;
        next_daz.az21 = 0;
        updateProfile();
        updateGui();
    }
    
    private void deleteLast() {
        if (!points.isEmpty()) {
            if (points.size() >= 2) {
                Point3d p1 = points.get(points.size()-1);
                Point3d p2 = points.get(points.size()-2);
                total_dist -=
                        navigator.getGlobe().getEllipsoid().inverseGeodesic(p1.y, p1.x, p2.y, p2.x, null).dist;
            } else
                total_dist = 0;
            points.remove(points.size()-1);
        }
        updateNext();
    }
    
    private void updateNext() {
        if (!points.isEmpty() && !is_completed) {
            Point3d p = points.get(points.size()-1);
            next_dh = navigator.getPointerH() - p.z;
            navigator.getGlobe().getEllipsoid().inverseGeodesic(p.y, p.x, navigator.getPointerLat(), navigator.getPointerLon(), next_daz);
        }  else {
            next_dh = 0;
            next_daz.dist = 0;
            next_daz.az12 = 0;
            next_daz.az21 = 0;
        }
        updateProfile();
        updateGui();
    }
    
    private void complete() {
        next_dh = 0;
        next_daz.dist = 0;
        next_daz.az12 = 0;
        next_daz.az21 = 0;
        is_completed = true;
        updateProfile();
        updateGui();
    }
    
    private synchronized void addToProfile(double lon1, double lat1, double lon2, double lat2,
            double step_len, double line_h,
            Ellipsoid.DistAz daz, Ellipsoid.LatLonAz llaz, Point3d p3d) {
        
        daz = navigator.getGlobe().getEllipsoid().inverseGeodesic(lat1, lon1, lat2, lon2, daz);
        
        if (line_buffer.remaining() <= 3*(2+(int)Math.ceil(daz.dist/step_len))) {
            FloatBuffer tmp = BufferUtil.newFloatBuffer(line_buffer.capacity()*2 + 3*(2+(int)Math.ceil(daz.dist/step_len)));
            tmp.put(line_buffer);
            line_buffer = tmp;
        }
        
        navigator.getGlobe().getEllipsoid().toCartesian(lat1, lon1, navigator.getGlobe().getElevation(lon1, lat1)+line_h, p3d);
        p3d.sub(navigator.getOrigin());
        line_buffer.put((float)p3d.x);
        line_buffer.put((float)p3d.y);
        line_buffer.put((float)p3d.z);
        
        double lat = lat1;
        double lon = lon1;
        while (daz.dist > step_len) {
            llaz = navigator.getGlobe().getEllipsoid().forwGeodesic(lat, lon, step_len, daz.az12, llaz);
            lat = llaz.lat;
            lon = llaz.lon;
            
            navigator.getGlobe().getEllipsoid().toCartesian(lat, lon, navigator.getGlobe().getElevation(lon, lat)+line_h, p3d);
            p3d.sub(navigator.getOrigin());
            line_buffer.put((float)p3d.x);
            line_buffer.put((float)p3d.y);
            line_buffer.put((float)p3d.z);
            
            daz = navigator.getGlobe().getEllipsoid().inverseGeodesic(lat, lon, lat2, lon2, daz);
        }
        
        navigator.getGlobe().getEllipsoid().toCartesian(lat2, lon2, navigator.getGlobe().getElevation(lon2, lat2)+line_h, p3d);
        p3d.sub(navigator.getOrigin());
        line_buffer.put((float)p3d.x);
        line_buffer.put((float)p3d.y);
        line_buffer.put((float)p3d.z);
    }
    
    private synchronized void updateProfile() {
        line_buffer.clear();
        point_buffer.clear();
        if (!points.isEmpty()) {
            double step_len = navigator.getTerrainHeight()/20;
            double line_h = navigator.getTerrainHeight()/200;
            if (line_h > 5000)
                line_h = 5000;
            if (line_h < 0.2)
                line_h = 0.2;
            if (step_len < 1)
                step_len = 1;
            if (total_dist/1000 > step_len)
                step_len = total_dist/1000;
            
            
            Point3d p3d = new Point3d();
            Ellipsoid.DistAz daz    = new Ellipsoid.DistAz();
            Ellipsoid.LatLonAz llaz = new Ellipsoid.LatLonAz();
            
            if (point_buffer.remaining() <= 3*points.size()) {
                FloatBuffer tmp = BufferUtil.newFloatBuffer(point_buffer.capacity()*2 + 3*points.size());
                tmp.put(point_buffer);
                point_buffer = tmp;
            }
            
            if (!points.isEmpty()) {
                Point3d prev_pos = points.get(0);
                navigator.getGlobe().getEllipsoid().toCartesian(prev_pos.y, prev_pos.x, navigator.getGlobe().getElevation(prev_pos.x, prev_pos.y)+line_h+.1, p3d);
                p3d.sub(navigator.getOrigin());
                point_buffer.put((float)p3d.x);
                point_buffer.put((float)p3d.y);
                point_buffer.put((float)p3d.z);
            }
            
            for (int i = 1; i < points.size(); ++i) {
                Point3d prev_pos = points.get(i-1);
                Point3d next_pos = points.get(i);
                
                addToProfile(prev_pos.x, prev_pos.y, next_pos.x, next_pos.y,
                        step_len, line_h, daz, llaz, p3d);
                
                navigator.getGlobe().getEllipsoid().toCartesian(next_pos.y, next_pos.x,
                        navigator.getGlobe().getElevation(next_pos.x, next_pos.y)+line_h+.1, p3d);
                p3d.sub(navigator.getOrigin());
                point_buffer.put((float)p3d.x);
                point_buffer.put((float)p3d.y);
                point_buffer.put((float)p3d.z);
            }
            
            if (!points.isEmpty() && !is_completed) {
                Point3d prev_pos = points.get(points.size()-1);
                addToProfile(prev_pos.x, prev_pos.y, navigator.getPointerLon(), navigator.getPointerLat(),
                        step_len, line_h, daz, llaz, p3d);
            }
        }
        line_buffer.flip();
        point_buffer.flip();
    }
    
    private synchronized void draw(GL gl) {
        if (point_buffer.limit() < 3)
            return;
        gl.glColor3f(1, 1, 1);
        gl.glDisable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_VERTEX_ARRAY);
        if (line_buffer.limit() >= 6) {
            gl.glLineWidth(3);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, line_buffer);
            gl.glDrawArrays(GL.GL_LINE_STRIP, 0, line_buffer.limit()/3);
            gl.glLineWidth(1);
        }
        gl.glPointSize(5);
        gl.glColor3f(1, 0, 0);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, point_buffer);
        gl.glDrawArrays(GL.GL_POINTS, 0, point_buffer.limit()/3);
        gl.glDisable(GL.GL_VERTEX_ARRAY);
        gl.glColor3f(1, 1, 1);
        gl.glPointSize(1);
        // System.out.println("Pb: " + point_buffer.limit() + " Lb: " + line_buffer.limit());
    }
    
    public void postDraw(GL gl) {
        draw(gl);
    }
    
    public void	mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (e.getClickCount() == 1)
                addPoint();
            else if (e.getClickCount() == 2)
                complete();
        }
    }
    
    public void	mouseEntered(MouseEvent e)  {
    }
    
    public void	mouseExited(MouseEvent e)  {
    }
    
    public void	mousePressed(MouseEvent e)  {
    }
    
    public void	mouseReleased(MouseEvent e) {
    }
    
    public void	mouseDragged(MouseEvent e) {
        updateNext();
    }
    
    public void	mouseMoved(MouseEvent e){
        updateNext();
    }
}
