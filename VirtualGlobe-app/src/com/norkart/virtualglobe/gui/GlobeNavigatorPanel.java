/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  GlobeNavigatorPanel.java
 *
 * Created on 22. april 2008, 13:29
 *
 */

package com.norkart.virtualglobe.gui;

import com.norkart.virtualglobe.viewer.navigator.*;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.BorderFactory;

import com.norkart.virtualglobe.util.ApplicationSettings;
import com.norkart.virtualglobe.util.SpringUtilities;
        
/**
 *
 * @author runaas
 */
public class GlobeNavigatorPanel extends JPanel implements GlobeNavigatorUpdateListener {
    protected JFormattedTextField lat_field;
    protected JFormattedTextField lon_field;
    protected JFormattedTextField hsea_field;
    protected JFormattedTextField hterr_field;
    protected JFormattedTextField az_field;
    protected JFormattedTextField ha_field;
    protected JFormattedTextField point_lat_field;
    protected JFormattedTextField point_lon_field;
    protected JFormattedTextField point_h_field;
    
    protected GlobeNavigator navigator;
    
    /** Creates a new instance of GlobeNavigatorPanel */
    public GlobeNavigatorPanel(GlobeNavigator nav) {
        super(new BorderLayout());
        
        navigator = nav;
        navigator.addGlobeNavigatorUpdateListener(this);
        
        NumberFormat lat_format = NumberFormat.getInstance();
        lat_format.setMaximumFractionDigits(7);
        lat_format.setMinimumFractionDigits(7);
        if (lat_format instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat)lat_format;
            df.setPositivePrefix("");
            df.setNegativePrefix("");
            df.setPositiveSuffix(" N");
            df.setNegativeSuffix(" S");
        }
        
        NumberFormat lon_format = NumberFormat.getInstance();
        lon_format.setMaximumFractionDigits(7);
        lon_format.setMinimumFractionDigits(7);
        if (lon_format instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat)lon_format;
            df.setPositivePrefix("");
            df.setNegativePrefix("");
            df.setPositiveSuffix(" E");
            df.setNegativeSuffix(" W");
        }
        
        NumberFormat h_format =  NumberFormat.getInstance();
        h_format.setMaximumFractionDigits(1);
        h_format.setMinimumFractionDigits(1);
        NumberFormat ang_format = NumberFormat.getInstance();
        ang_format.setMaximumFractionDigits(1);
        ang_format.setMinimumFractionDigits(1);
        
        lat_field   = new JFormattedTextField(lat_format);
        lon_field   = new JFormattedTextField(lon_format);
        hsea_field  = new JFormattedTextField(h_format);
        hterr_field = new JFormattedTextField(h_format);
        az_field    = new JFormattedTextField(ang_format);
        ha_field    = new JFormattedTextField(ang_format);
        point_lat_field = new JFormattedTextField(lat_format);
        point_lon_field = new JFormattedTextField(lon_format);
        point_h_field = new JFormattedTextField(h_format);
        
        lat_field.setHorizontalAlignment(JTextField.RIGHT);
        lon_field.setHorizontalAlignment(JTextField.RIGHT);
        hsea_field.setHorizontalAlignment(JTextField.RIGHT);
        hterr_field.setHorizontalAlignment(JTextField.RIGHT);
        az_field.setHorizontalAlignment(JTextField.RIGHT);
        ha_field.setHorizontalAlignment(JTextField.RIGHT);
        point_lat_field.setHorizontalAlignment(JTextField.RIGHT);
        point_lon_field.setHorizontalAlignment(JTextField.RIGHT);
        point_h_field.setHorizontalAlignment(JTextField.RIGHT);
        
        Box hor_box = Box.createHorizontalBox();
        
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        {
            JPanel p = new JPanel(new SpringLayout());
            p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), settings.getResourceString("CAMERA_POSITION")));
            
            // Latitude
            p.add(new JLabel(settings.getResourceString("LAT_LABEL")));
            lat_field.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (lat_field.getValue() == null) return;
                    double d = ((Number)lat_field.getValue()).doubleValue();
                    if (!Double.isInfinite(d) && !Double.isNaN(d))
                        navigator.setLat(Math.toRadians(d));
                    // System.out.println("Bredde " + Math.toDegrees(lat));
                }
            });
            p.add(lat_field);
            
            // Azimuth
            p.add(new JLabel(settings.getResourceString("AZ_LABEL")));
            az_field.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (az_field.getValue() == null) return;
                    double d = ((Number)az_field.getValue()).doubleValue();
                    if (!Double.isInfinite(d) && !Double.isNaN(d))
                        navigator.setAzimut(Math.toRadians(d));
                    // System.out.println("Azimut: " + Math.toDegrees(az));
                }
            });
            p.add(az_field);
            
            // Longitude
            p.add(new JLabel(settings.getResourceString("LON_LABEL")));
            lon_field.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (lon_field.getValue() == null) return;
                    double d = ((Number)lon_field.getValue()).doubleValue();
                    if (!Double.isInfinite(d) && !Double.isNaN(d))
                        navigator.setLon(Math.toRadians(d));
                    // System.out.println("Lengde " + Math.toDegrees(lon));
                }
            });
            p.add(lon_field);
            
            // Height angle
            p.add(new JLabel(settings.getResourceString("HA_LABEL")));
            ha_field.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (ha_field.getValue() == null) return;
                    double d = ((Number)ha_field.getValue()).doubleValue();
                    if (!Double.isInfinite(d) && !Double.isNaN(d))
                        navigator.setHeightAngle(Math.toRadians(d));
                    // System.out.println("Høydevinkel: " + Math.toDegrees(ha));
                }
            });
            p.add(ha_field);
            
            // Height over sea level
            p.add(new JLabel(settings.getResourceString("H_SEA_LABEL")));
            hsea_field.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (hsea_field.getValue() == null) return;
                    double d = ((Number)hsea_field.getValue()).doubleValue();
                    if (!Double.isInfinite(d) && !Double.isNaN(d))
                        navigator.setEllipsHeight(d);
                    // System.out.println("Høyde (h): " + hEllps);
                }
            });
            p.add(hsea_field);
            
            
            // Height over terrain
            p.add(new JLabel(settings.getResourceString("H_TERR_LABEL")));
            hterr_field.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (hterr_field.getValue() == null) return;
                    double d = ((Number)hterr_field.getValue()).doubleValue();
                    if (!Double.isInfinite(d) && !Double.isNaN(d))
                        navigator.setTerrainHeight(d);
                    // System.out.println("Høyde (t): " + hTerrain);
                }
            });
            p.add(hterr_field);
            
            SpringUtilities.makeCompactGrid(p, //parent
                    3, 4,
                    3, 3,  //initX, initY
                    3, 3); //xPad, yPad
            
            hor_box.add(p);
        }
        
        {
            JPanel p = new JPanel(new SpringLayout());
            p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), settings.getResourceString("POINTER_POSITION")));
            
            // Latitude
            p.add(new JLabel(settings.getResourceString("LAT_LABEL")));
            point_lat_field.setEditable(false);
            p.add(point_lat_field);
            
            // Longitude
            p.add(new JLabel(settings.getResourceString("LON_LABEL")));
            point_lon_field.setEditable(false);
            p.add(point_lon_field);
            
            // Height
            p.add(new JLabel(settings.getResourceString("H_SEA_LABEL")));
            point_h_field.setEditable(false);
            p.add(point_h_field);
            
            SpringUtilities.makeCompactGrid(p, //parent
                    3, 2,
                    3, 3,  //initX, initY
                    3, 3); //xPad, yPad
            
            hor_box.add(p);
        }
        
        hor_box.add(Box.createGlue());
        hor_box.add(new JPanel());
        
        add(hor_box);
    }
    
    
    public void updateNavigatorChanges(GlobeNavigator nav) {
        if (navigator != nav)
            return;
        
        /*
        long curr_time = System.currentTimeMillis();
        if (curr_time - last_gui_update_time < 200)
            return;
        last_gui_update_time = curr_time;
         */
        if (navigator.isLatChanged()) {
            JFormattedTextField.AbstractFormatter formatter = lat_field.getFormatter();
            try {
                lat_field.setText(formatter.valueToString(new Double(Math.toDegrees(navigator.getLat()))));
            } catch (java.text.ParseException ex) {}
        }
        
        if (navigator.isLonChanged()) {
            JFormattedTextField.AbstractFormatter formatter = lon_field.getFormatter();
            try {
                lon_field.setText(formatter.valueToString(new Double(Math.toDegrees(navigator.getLon()))));
            } catch (java.text.ParseException ex) {}
        }
        if (navigator.isElevationChanged()) {
            JFormattedTextField.AbstractFormatter formatter = hsea_field.getFormatter();
            try {
                hsea_field.setText(formatter.valueToString(new Double(navigator.getEllipsHeight())));
            } catch (java.text.ParseException ex) {}
            formatter = hterr_field.getFormatter();
            try {
                hterr_field.setText(formatter.valueToString(new Double(navigator.getTerrainHeight())));
            } catch (java.text.ParseException ex) {}
        }
        if (navigator.isHeightAngleChanged()) {
            JFormattedTextField.AbstractFormatter formatter = ha_field.getFormatter();
            try {
                ha_field.setText(formatter.valueToString(new Double(Math.toDegrees(navigator.getHeightAngle()))));
            } catch (java.text.ParseException ex) {}
        }
        
        if (navigator.isAzimuthChanged()){
            JFormattedTextField.AbstractFormatter formatter = az_field.getFormatter();
            try {
                az_field.setText(formatter.valueToString(new Double(Math.toDegrees(navigator.getAzimut()))));
            } catch (java.text.ParseException ex) {}
        }
        
        if (navigator.isPointerChanged()) {
            JFormattedTextField.AbstractFormatter formatter = point_lon_field.getFormatter();
            try {
                point_lon_field.setText(formatter.valueToString(new Double(Math.toDegrees(navigator.getPointerLon()))));
            } catch (java.text.ParseException ex) {}
            formatter = point_lat_field.getFormatter();
            try {
                point_lat_field.setText(formatter.valueToString(new Double(Math.toDegrees(navigator.getPointerLat()))));
            } catch (java.text.ParseException ex) {}
            formatter = point_h_field.getFormatter();
            try {
                point_h_field.setText(formatter.valueToString(new Double(navigator.getPointerH())));
            } catch (java.text.ParseException ex) {}
        }
    }
}

