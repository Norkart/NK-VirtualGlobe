//
//   AnimationDialog.java
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

public class AnimationDialog extends JDialog {
    // declare GUI components here
    protected GridBagLayout gridbag;
    protected GridBagConstraints constraints;
    protected Container mainPane;
    protected JPanel buttonPanel1;
    protected JPanel buttonPanel2;
    protected JLabel name;
    protected JTextField nameEntry;
    protected JLabel keys;
    protected JTextField keysEntry;
    protected JLabel values;
    protected JTextField valuesEntry;
    protected JLabel cycleInterval;
    protected JTextField cycleIntervalEntry;
    protected JCheckBox enabledCB;
    protected JCheckBox loopCB;
    protected JButton ok;
    protected JButton cancel;
    protected boolean enabled = false;
    protected boolean loop = false;
    protected short status = CANCEL;

    public static short OK = 0;
    public static short CANCEL = 1;

    public AnimationDialog(Frame owner,
			   String title,
			   boolean modal,
			   String nameData,
			   String keyData,
			   String valueData) {
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
	constraints.anchor = GridBagConstraints.CENTER;
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
	constraints.anchor = GridBagConstraints.CENTER;
	keys = new JLabel("Keys: ");
	mainPane.add(keys, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	keysEntry = new JTextField();
	keysEntry.setText(keyData);
	mainPane.add(keysEntry, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	values = new JLabel("Values: ");
	mainPane.add(values, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 2;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	valuesEntry = new JTextField();
	valuesEntry.setText(valueData);
	mainPane.add(valuesEntry, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	cycleInterval = new JLabel("Cycle Interval: ");
	mainPane.add(cycleInterval, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 3;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	cycleIntervalEntry = new JTextField();
	mainPane.add(cycleIntervalEntry, constraints);

	buttonPanel1 = new JPanel(gridbag);

	enabledCB = new JCheckBox("Enabled");
	enabledCB.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    switch (e.getStateChange()) {
		    case ItemEvent.DESELECTED:
			enabled = false;
			break;
		    case ItemEvent.SELECTED:
			enabled = true;
			break;
		    }
		}
	    });
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	buttonPanel1.add(enabledCB, constraints);

	loopCB = new JCheckBox("Loop");
	loopCB.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    switch (e.getStateChange()) {
		    case ItemEvent.DESELECTED:
			loop = false;
			break;
		    case ItemEvent.SELECTED:
			loop = true;
			break;
		    }
		}
	    });
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	buttonPanel1.add(loopCB, constraints);

	constraints.gridwidth = 2;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	mainPane.add(buttonPanel1, constraints);

	buttonPanel2 = new JPanel(gridbag);

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
	buttonPanel2.add(ok, constraints);

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
	buttonPanel2.add(cancel, constraints);


	// add button panel
	constraints.gridwidth = 2;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 5;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	mainPane.add(buttonPanel2, constraints);
    }

    public boolean getEnabled() {
	return enabled;
    }

    public void setEnabled(boolean enabled) {
	this.enabled = enabled;
	enabledCB.setSelected(enabled);
    }

    public boolean getLoop() {
	return loop;
    }

    public void setLoop(boolean loop) {
	this.loop = loop;
	loopCB.setSelected(loop);
    }

    public String getName() {
	return nameEntry.getText();
    }

    public void setName(String name) {
	nameEntry.setText(name);
    }

    public String getKeys() {
	return keysEntry.getText();
    }

    public void setKeys(String keys) {
	keysEntry.setText(keys);
    }

    public String getValues() {
	return valuesEntry.getText();
    }

    public void setValues(String values) {
	valuesEntry.setText(values);
    }

    public String getCycleInterval() {
	return cycleIntervalEntry.getText();
    }

    public void setCycleInterval(String cycleInterval) {
	cycleIntervalEntry.setText(cycleInterval);
    }

    public short getStatus() {
	return status;
    }
}
