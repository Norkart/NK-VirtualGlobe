//
//   Trigger.java
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
import java.awt.*;

import javax.swing.*;

public class Trigger extends JPanel {
    // declare GUI components here
    protected GridBagLayout gridbag;
    protected GridBagConstraints constraints;
    protected JPanel headerPanel;
    protected JPanel contentPanel;
    protected JPanel buttonPanel;
    protected JLabel name;
    protected JTextField nameEntry;
    protected JButton ok;
    protected JButton cancel;

    protected JPanel actionPanel;
    protected JPanel actionSubPanel;
    protected JLabel action;
    protected JComboBox actionCombo;
    protected JButton actionAdd;
    protected JButton actionDelete;
    protected JList actionList;
    public static final String actions[] = {"Enable",
					    "Disable",
					    "LMSInitialize",
					    "LMSFinish"};

    protected JPanel targetPanel;
    protected JPanel targetSubPanel;
    protected JLabel target;
    protected JComboBox targetCombo;
    protected JButton targetAdd;
    protected JButton targetDelete;
    protected JList targetList;
    public static final String targets[] = {"need",
					    "routine",
					    "for",
					    "collecting",
					    "DEFS",
					    "from",
					    "scenegraph"};

    public Trigger() {
	gridbag = new GridBagLayout();
	constraints = new GridBagConstraints();
	constraints.insets = new Insets(4, 4, 4, 4);

	// create action
	actionPanel = new JPanel(gridbag);
	actionSubPanel = new JPanel(gridbag);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	action = new JLabel("Action: ");
	actionSubPanel.add(action, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	actionCombo = new JComboBox(actions);
	actionSubPanel.add(actionCombo, constraints);

	constraints.gridwidth = 2;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	actionPanel.add(actionSubPanel, constraints);

	actionAdd = new JButton("Add");
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	actionPanel.add(actionAdd, constraints);

	actionDelete = new JButton("Delete");
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	actionPanel.add(actionDelete, constraints);

	constraints.gridwidth = 2;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	actionList = new JList();
	actionPanel.add(actionList, constraints);

	// create target
	targetPanel = new JPanel(gridbag);
	targetSubPanel = new JPanel(gridbag);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	target = new JLabel("Target: ");
	targetSubPanel.add(target, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	targetCombo = new JComboBox(targets);
	targetSubPanel.add(targetCombo, constraints);

	constraints.gridwidth = 2;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	targetPanel.add(targetSubPanel, constraints);

	targetAdd = new JButton("Add");
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	targetPanel.add(targetAdd, constraints);

	targetDelete = new JButton("Delete");
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	targetPanel.add(targetDelete, constraints);

	constraints.gridwidth = 2;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	targetList = new JList();
	targetPanel.add(targetList, constraints);

	// overall
	setLayout(gridbag);

	headerPanel = new JPanel(gridbag);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	name = new JLabel("Name: ");
	headerPanel.add(name, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	nameEntry = new JTextField(20);
	headerPanel.add(nameEntry, constraints);

	// add header panel
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.WEST;
	add(headerPanel, constraints);

	constraints.anchor = GridBagConstraints.CENTER;
	contentPanel = new JPanel(gridbag);

	// add action panel
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	contentPanel.add(actionPanel, constraints);

	// add target panel
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	contentPanel.add(targetPanel, constraints);

	// add content panel
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	add(contentPanel, constraints);

	buttonPanel = new JPanel(gridbag);

	ok = new JButton("OK");
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
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	add(buttonPanel, constraints);

    }
}
