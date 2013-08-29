//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.gui;

import com.norkart.virtualglobe.components.Universe;
import com.norkart.virtualglobe.util.ApplicationSettings;
import java.util.Vector;
import java.net.MalformedURLException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JFileChooser;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ComboBoxModel;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.HeadlessException;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class OpenWorldDialog extends JDialog {
  private Universe universe;
  private JFileChooser dataChooser = new JFileChooser();
  private JComboBox dataSetList;
  private ComboBoxModel dataSetListModel;

  public void setListModel(Vector v) {
    dataSetListModel = new DefaultComboBoxModel(v);
    dataSetList.setModel(dataSetListModel);
  }

  public OpenWorldDialog(JFrame frame, Universe u) throws HeadlessException {
    super(frame, ApplicationSettings.getApplicationSettings().getResourceString("OPEN_WORLD"));

    universe = u;

    // Create all internal components
    Box box = Box.createVerticalBox();

    // Editable list of known Worlds
    Box dataSetBox = Box.createHorizontalBox();
    // dataSetBox.setBorder(BorderFactory.createTitledBorder(universe.getResourceString("DATA_SET_BOX_TITLE")));
    dataSetList = new JComboBox();
    dataSetList.setEditable(true);
    dataSetList.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String newSelection = (String)dataSetList.getSelectedItem();
        universe.setDataSet(newSelection);
        setListModel(universe.getLastDataSets());
      }
    });
    dataSetBox.add(dataSetList);


    final JButton openButton = new JButton();
    openButton.setText("...");
    openButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // dataChooser.setSelectedFile(getCacheDirectory());
        int retval = dataChooser.showDialog(openButton, null);
        if (retval == JFileChooser.APPROVE_OPTION) {
          try {
            String selected = dataChooser.getSelectedFile().toURL().toString();
            universe.setDataSet(selected);
            setListModel(universe.getLastDataSets());
            // dataSetList.setSelectedItem(selected);
          }
          catch (MalformedURLException ex) {}
       }
     }
    });
    dataSetBox.add(openButton);


    // dataSetBox.setAlignmentX(Box.LEFT_ALIGNMENT);
    box.add(dataSetBox);

    box.add(new JSeparator());

    // Buttons
    Box button_box = Box.createHorizontalBox();
    button_box.add(Box.createHorizontalGlue());

    // Create buttons
    JButton ok_button = new JButton(ApplicationSettings.getApplicationSettings().getResourceString("OK"));
    ok_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        String newSelection = (String)dataSetList.getSelectedItem();
        universe.setDataSet(newSelection);
        setListModel(universe.getLastDataSets());
        setVisible(false);
      }
    });
    button_box.add(ok_button);
/*
    JButton apply_button = new JButton(universe.getResourceString("APPLY"));
    apply_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        String newSelection = (String)dataSetList.getSelectedItem();
        universe.setDataSelection(newSelection);
      }
    });
    button_box.add(apply_button);

    JButton cancel_button = new JButton(universe.getResourceString("CANCEL"));
    cancel_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        setVisible(false);
      }
    });
    button_box.add(cancel_button);
    */
    box.add(button_box);
    getContentPane().add(box);
  }
}