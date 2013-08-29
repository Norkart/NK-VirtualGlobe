/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  LightModel.java
 *
 * Created on 23. april 2008, 11:09
 *
 */

package com.norkart.virtualglobe.viewer;

import java.awt.Component;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import java.text.MessageFormat;

import com.norkart.virtualglobe.util.ApplicationSettings;

/**
 *
 * @author runaas
 */
public class LightModel {
     protected Component light_panel;

  protected float spec_int = .95f;
  protected float diffuse_int = .95f;
  protected float ambient_int = 0.05f;
  protected double light_elev = Math.toRadians(50);
  protected double light_dir  = Math.toRadians(-60);

    /** Creates a new instance of LightModel */
    public LightModel() {
    }
    
    public float getSpecularIntensity() {
        return spec_int;
    }
    
    public float getDiffuseIntensity() {
        return diffuse_int;
    }
    public float getAmbientIntensity() {
        return ambient_int;
    }
    
    public double getLightElevation() {
        return light_elev;
    }
    
    public double getLightAzimuth() {
        return light_dir;
    }
    
      public  Component getLightPanel( ) {
    if (light_panel != null)
      return light_panel;
    ApplicationSettings settings = ApplicationSettings.getApplicationSettings();

    Box box = Box.createVerticalBox();
    Box sub_box;
    // The light direction part
    sub_box = Box.createVerticalBox();
    sub_box.setBorder(BorderFactory.createTitledBorder(settings.getResourceString("LIGHT_DIRECTION_TITLE")));
    final Object[] ma1 = { new Double(Math.toDegrees(light_dir)) };
    final MessageFormat mf1 = new MessageFormat( settings.getResourceString("LIGHT_DIRECTION_LABEL"));
    final JLabel lightDirLabel = new JLabel(mf1.format(ma1));
    lightDirLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    final JSlider lightDirSlider = new JSlider();
    lightDirSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    lightDirSlider.setOrientation(JSlider.HORIZONTAL);
    lightDirSlider.setMaximum(180);
    lightDirSlider.setMinimum(-180);
    lightDirSlider.setValue((int)Math.toDegrees(light_dir));
    lightDirSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (e.getSource() == lightDirSlider) {
          float value = lightDirSlider.getValue();
          ma1[0] = new Double(value);
          lightDirLabel.setText(mf1.format(ma1));
          light_dir = Math.toRadians(value);
        }
      }
    });
    lightDirSlider.setToolTipText(settings.getResourceString("LIGHT_DIRECTION_TIPS"));
    sub_box.add(lightDirSlider);
    sub_box.add(lightDirLabel);

    final Object[] ma2 = { new Double(Math.toDegrees(light_elev)) };
    final MessageFormat mf2 = new MessageFormat( settings.getResourceString("LIGHT_ELEVATION_LABEL"));
    final JLabel lightElevLabel = new JLabel(mf2.format(ma2));
    lightElevLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    final JSlider lightElevSlider = new JSlider();
    lightElevSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    lightElevSlider.setOrientation(JSlider.HORIZONTAL);
    lightElevSlider.setMaximum(180);
    lightElevSlider.setMinimum(-180);
    lightElevSlider.setValue((int)Math.toDegrees(light_elev));
    lightElevSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (e.getSource() == lightElevSlider) {
          float value = lightElevSlider.getValue();
          ma2[0] = new Double(value);
          lightElevLabel.setText(mf2.format(ma2));
          light_elev = Math.toRadians(value);
        }
      }
    });
    lightElevSlider.setToolTipText(settings.getResourceString("LIGHT_ELEVATION_TIPS"));
    sub_box.add(lightElevSlider);
    sub_box.add(lightElevLabel);
    box.add(sub_box);

    // The light intencity part
    sub_box = Box.createVerticalBox();
    sub_box.setBorder(BorderFactory.createTitledBorder(settings.getResourceString("LIGHT_INTENCITY_TITLE")));
    final Object[] ma3 = { new Float(diffuse_int*100) };
    final MessageFormat mf3 = new MessageFormat( settings.getResourceString("LIGHT_DIRECTIONAL_INTENCITY_LABEL"));
    final JLabel lightDirIntLabel = new JLabel(mf3.format(ma3));
    lightDirIntLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    final JSlider lightDirIntSlider = new JSlider();
    lightDirIntSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    lightDirIntSlider.setOrientation(JSlider.HORIZONTAL);
    lightDirIntSlider.setMaximum(100);
    lightDirIntSlider.setMinimum(0);
    lightDirIntSlider.setValue((int)(diffuse_int*100));
    lightDirIntSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (e.getSource() == lightDirIntSlider) {
          float value = lightDirIntSlider.getValue();
          ma3[0] = new Float(value);
          lightDirIntLabel.setText(mf3.format(ma3));
          diffuse_int = value/100;
        }
      }
    });
    lightDirIntSlider.setToolTipText(settings.getResourceString("LIGHT_DIRECTIONAL_INTENCITY_TIPS"));
    sub_box.add(lightDirIntSlider);
    sub_box.add(lightDirIntLabel);

    final Object[] ma4 = { new Float(spec_int*100) };
    final MessageFormat mf4 = new MessageFormat( settings.getResourceString("LIGHT_SPECULAR_INTENCITY_LABEL"));
    final JLabel lightSpecIntLabel = new JLabel(mf4.format(ma4));
    lightSpecIntLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    final JSlider lightSpecIntSlider = new JSlider();
    lightSpecIntSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    lightSpecIntSlider.setOrientation(JSlider.HORIZONTAL);
    lightSpecIntSlider.setMaximum(100);
    lightSpecIntSlider.setMinimum(0);
    lightSpecIntSlider.setValue((int)(spec_int*100));
    lightSpecIntSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (e.getSource() == lightSpecIntSlider) {
          float value = lightSpecIntSlider.getValue();
          ma4[0] = new Float(value);
          lightSpecIntLabel.setText(mf4.format(ma4));
          spec_int = value/100;
        }
      }
    });
    lightSpecIntSlider.setToolTipText(settings.getResourceString("LIGHT_SPECULAR_INTENCITY_TIPS"));
    sub_box.add(lightSpecIntSlider);
    sub_box.add(lightSpecIntLabel);

    final Object[] ma5 = { new Float(ambient_int*100) };
    final MessageFormat mf5 = new MessageFormat( settings.getResourceString("LIGHT_AMBIENT_INTENCITY_LABEL"));
    final JLabel lightAmbIntLabel = new JLabel(mf5.format(ma5));
    lightAmbIntLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    final JSlider lightAmbIntSlider = new JSlider();
    lightAmbIntSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    lightAmbIntSlider.setOrientation(JSlider.HORIZONTAL);
    lightAmbIntSlider.setMaximum(100);
    lightAmbIntSlider.setMinimum(0);
    lightAmbIntSlider.setValue((int)(ambient_int*100));
    lightAmbIntSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (e.getSource() == lightAmbIntSlider) {
          float value = lightAmbIntSlider.getValue();
          ma5[0] = new Float(value);
          lightAmbIntLabel.setText(mf5.format(ma5));
          ambient_int = value/100;
        }
      }
    });
    lightAmbIntSlider.setToolTipText(settings.getResourceString("LIGHT_AMBIENT_INTENCITY_TIPS"));
    sub_box.add(lightAmbIntSlider);
    sub_box.add(lightAmbIntLabel);


    box.add(sub_box);

    light_panel = box;

    return light_panel;
  }
}
