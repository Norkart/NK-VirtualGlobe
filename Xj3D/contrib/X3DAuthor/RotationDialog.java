//
//   RotationDialog.java
//
//------------------------------------------------------------------------
//
//      Portions Copyright (c) 2000 SURVICE Engineering Company.
//      All Rights Reserved.
//      This file contains Original Code and/or Modifications of Original
//      Code as defined in and that are subject to the SURVICE Public
//      Source License (Version 1.3, dated March 12, 2002)
//
//      A copy of this license can be found in the doc directory
//------------------------------------------------------------------------
//
//      Developed by SURVICE Engineering Co. (www.survice.com)
//      April 2002
//
//      Authors:
//              Bob Parker
//------------------------------------------------------------------------
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class RotationDialog extends JDialog {
    // declare GUI components here
    protected GridBagLayout gridbag;
    protected GridBagConstraints constraints;
    protected Container mainPane;
    protected JPanel buttonPanel;
    protected JLabel name;
    protected JTextField nameEntry;
    protected JLabel rpm;
    protected JTextField rpmEntry;
    protected JButton ok;
    protected JButton cancel;
    protected short status = CANCEL;

    public static short OK = 0;
    public static short CANCEL = 1;

    public RotationDialog(Frame owner,
			  String title,
			  boolean modal,
			  String nameData,
			  String rpmData) {
	super(owner, title, modal);
	setLocationRelativeTo(owner);

	gridbag = new GridBagLayout();
	constraints = new GridBagConstraints();
	constraints.insets = new Insets(4, 4, 4, 4);

	mainPane = getContentPane();
	mainPane.setLayout(gridbag);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.EAST;
	name = new JLabel("Name: ");
	mainPane.add(name, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	nameEntry = new JTextField();
	nameEntry.setText(nameData);
	mainPane.add(nameEntry, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.EAST;
	rpm = new JLabel("RPM: ");
	mainPane.add(rpm, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	rpmEntry = new JTextField();
	rpmEntry.setText(rpmData);
	mainPane.add(rpmEntry, constraints);

	buttonPanel = new JPanel(gridbag);

	ok = new JButton("OK");
	ok.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    status = OK;
		    hide();
		}
	    });
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	buttonPanel.add(ok, constraints);

	cancel = new JButton("Cancel");
	cancel.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    status = CANCEL;
		    hide();
		}
	    });
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	buttonPanel.add(cancel, constraints);

	// add button panel
	constraints.gridwidth = 2;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	mainPane.add(buttonPanel, constraints);
    }

    public String getName() {
	return nameEntry.getText();
    }

    public void setName(String name) {
	nameEntry.setText(name);
    }

    public String getRPM() {
	return rpmEntry.getText();
    }

    public void setRPM(String rpm) {
	rpmEntry.setText(rpm);
    }

    public short getStatus() {
	return status;
    }
}
