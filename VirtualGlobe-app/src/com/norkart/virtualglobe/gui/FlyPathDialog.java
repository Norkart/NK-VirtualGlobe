//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.gui;

import com.norkart.virtualglobe.viewer.Camera;
import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;

import com.norkart.virtualglobe.util.PathPoint;
import com.norkart.virtualglobe.util.ApplicationSettings;

import com.norkart.virtualglobe.components.FlyPath;
import com.norkart.virtualglobe.components.Universe;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.Box;
import javax.swing.JFileChooser;

// import javax.swing.table.*;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class FlyPathDialog extends JDialog {
  protected Universe universe;
  protected GlobeNavigator navigator;

  protected long interval = 666;
  protected FlyPath flyPath = new FlyPath();
  protected JFileChooser fileChooser = new JFileChooser();
/*
  class FlyPathTableModel extends AbstractTableModel {
  };
  */

  private class Recorder extends Thread {
    long prevtime;
    boolean is_recording = true;

    synchronized void stopRecording() {
      is_recording = false;
      try { join(); }
      catch (InterruptedException ex) {}
      if (recorder == this) recorder = null;
    }
    public void run () {
      flyPath.clear();
      prevtime = System.currentTimeMillis() - 2000;
      while (true) {
        synchronized (this) {
          if (!is_recording) break;
        }
        PathPoint pp = new PathPoint();
        pp.data[0] = navigator.getLon();
        pp.data[1] = navigator.getLat();
        pp.data[2] = navigator.getEllipsHeight();
        pp.data[3] = navigator.getAzimut();
        pp.data[4] = navigator.getHeightAngle();

        long currtime = System.currentTimeMillis();
        pp.movetime = currtime - prevtime;
        flyPath.add(pp);
        prevtime = currtime;
        try { sleep(interval); }
        catch (InterruptedException ex) {}
      }
    }
  };
  Recorder recorder = null;

  public FlyPathDialog(JFrame frame, Universe u, Camera camera) throws HeadlessException {
    super(frame, ApplicationSettings.getApplicationSettings().getResourceString("FLYPATH"));
    ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
    universe = u;
    navigator = camera.getNavigator();
    flyPath.dataset = u.getUrl();
    Box box = Box.createVerticalBox();

    /*
    tableModel = new ViewpointTableModel();


    table = new JTable(tableModel);
    JScrollPane scrollPane = new JScrollPane(table);
    // table.setPreferredScrollableViewportSize(new Dimension(500, 70));
    box.add(scrollPane);

    box.add(new JSeparator());
    */

    Box record_box = Box.createHorizontalBox();
    // Create buttons

    final JButton record_button = new JButton(settings.getResourceString("RECORD"));
    final JButton stop_button = new JButton(settings.getResourceString("STOP"));
    final JButton play_button = new JButton(settings.getResourceString("PLAY"));
    final JButton saveVideo   = new JButton(settings.getResourceString("SAVE_VIDEO"));
    stop_button.setEnabled(false);

    record_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        synchronized (this) {
          if (recorder != null)
            recorder.stopRecording();
          recorder = new Recorder();
          recorder.start();
          stop_button.setEnabled(true);
          record_button.setEnabled(false);
        }
      }
    });
    record_box.add(record_button);

    stop_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        synchronized (this) {
          if (recorder != null)
            recorder.stopRecording();
          stop_button.setEnabled(false);
          record_button.setEnabled(true);
        }
      }
    });
    record_box.add(stop_button);

    play_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        synchronized (this) {
          if (recorder != null)
            recorder.stopRecording();
        }
        navigator.flyPath(flyPath.getPointList());
        record_button.setEnabled(true);
        stop_button.setEnabled(false);
      }
    });
    record_box.add(play_button);

    box.add(record_box);

    Box file_box = Box.createHorizontalBox();
    JButton load_button = new JButton(settings.getResourceString("LOAD"));
    load_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        int reply = fileChooser.showOpenDialog(FlyPathDialog.this);
        if (reply != JFileChooser.APPROVE_OPTION) return;
        File file = fileChooser.getSelectedFile();
        try {
          universe.getApplicationLoader().load(flyPath, new FileInputStream(file), null);
        }
        catch (IOException ex) {ex.printStackTrace();}
      }
    });
    file_box.add(load_button);

    JButton save_button = new JButton(settings.getResourceString("SAVE"));
    save_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        int reply = fileChooser.showSaveDialog(FlyPathDialog.this);
        if (reply != JFileChooser.APPROVE_OPTION) return;
        File file = fileChooser.getSelectedFile();
        try  {
          flyPath.save(new FileOutputStream(file));
        }
        catch (IOException ex) { ex.printStackTrace(); }
      }
    });
    file_box.add(save_button);

    file_box.add(Box.createHorizontalGlue());
    JButton close_button = new JButton(settings.getResourceString("CLOSE"));
    close_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        setVisible(false);
      }
    });
    file_box.add(close_button);
    box.add(file_box);

    getContentPane().add(box);
    pack();
  }


}