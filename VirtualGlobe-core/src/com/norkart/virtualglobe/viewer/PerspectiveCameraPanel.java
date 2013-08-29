/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  PerspectiveCameraPanel.java
 *
 * Created on 23. april 2008, 15:30
 *
 */

package com.norkart.virtualglobe.viewer;

import java.awt.Component;
import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.text.MessageFormat;

import com.norkart.virtualglobe.util.ApplicationSettings;

import javax.media.opengl.GL;

/**
 *
 * @author runaas
 */
public class PerspectiveCameraPanel extends JPanel implements PostDrawListener {
    private PerspectiveCamera camera;
    
    private JLabel framerate_label;
    private Object[] framerate_arg = { new Float(0) };
    private MessageFormat framerate_format;
    
    private long prevechotime = System.currentTimeMillis();
    
    private int framecnt= 0;
    
    /** Creates a new instance of PerspectiveCameraPanel */
    public PerspectiveCameraPanel(PerspectiveCamera cam) {
        this.camera = cam;
        camera.addPostDrawListener(this);
        
        
        setLayout(new BorderLayout());
        
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        Box box = Box.createVerticalBox();
        Box sub_box;
        
        sub_box = Box.createVerticalBox();
        sub_box.setBorder(BorderFactory.createTitledBorder(settings.getResourceString("FOV_BOX_TITLE")));
        final Object[] ma2 = { new Double(camera.getFov()) };
        final MessageFormat mf2 = new MessageFormat( settings.getResourceString("FOV_LABEL"));
        final JLabel fovLabel = new JLabel(mf2.format(ma2));
        fovLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        final JSlider fovSlider = new JSlider();
        fovSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        fovSlider.setOrientation(JSlider.HORIZONTAL);
        fovSlider.setMaximum(120);
        fovSlider.setMinimum(10);
        fovSlider.setValue((int)camera.getFov());
        fovSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == fovSlider) {
                    float value = fovSlider.getValue();
                    ma2[0] = new Double(value);
                    fovLabel.setText(mf2.format(ma2));
                    camera.setFov(value);
                }
            }
        });
        fovSlider.setToolTipText(settings.getResourceString("FOV_TIPS"));
        sub_box.add(fovSlider);
        sub_box.add(fovLabel);
        box.add(sub_box);
        
        sub_box = Box.createVerticalBox();
        sub_box.setBorder(BorderFactory.createTitledBorder(settings.getResourceString("DETAIL_SIZE_BOX_TITLE")));
        final Object[] ma1 = { new Double(camera.getDetailSizeFactor()) };
        final MessageFormat mf1 = new MessageFormat( settings.getResourceString("DETAIL_SIZE_LABEL"));
        final JLabel detailLabel = new JLabel(mf1.format(ma1));
        detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        final JSlider detailSlider = new JSlider();
        detailSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailSlider.setOrientation(JSlider.HORIZONTAL);
        detailSlider.setMaximum(100);
        detailSlider.setMinimum(10);
        detailSlider.setValue((int)(camera.getDetailSizeFactor()*10));
        detailSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == detailSlider) {
                    float value = detailSlider.getValue();
                    value /= 10.0;
                    ma1[0] = new Double(value);
                    detailLabel.setText(mf1.format(ma1));
                    camera.setDetailSizeFactor(value);
                }
            }
        });
        detailSlider.setToolTipText(settings.getResourceString("DETAIL_SIZE_TIPS"));
        sub_box.add(detailSlider);
        sub_box.add(detailLabel);
        box.add(sub_box);
        
        
        framerate_format = new MessageFormat( settings.getResourceString("FRAMERATE_LABEL"));
        framerate_label = new JLabel();
        framerate_label.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(framerate_label);
        
        add(box);
    }
    
    public void postDraw(GL gl) {
        // Compute framerate
        framecnt++;
        long currtime = System.currentTimeMillis();
        if (currtime-prevechotime > 1000) {
            float fps = framecnt*1000.f/(currtime-prevechotime);
            framerate_arg[0] = new Float(fps);
            framerate_label.setText(framerate_format.format(framerate_arg));
            prevechotime = currtime;
            framecnt = 0;
        }
    }
}
