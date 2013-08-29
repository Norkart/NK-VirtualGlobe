//
//   TriggerDialog.java
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

import org.w3c.dom.*;

public class TriggerDialog extends JDialog {
    // declare GUI components here
    protected GridBagLayout gridbag;
    protected GridBagConstraints constraints;
    protected Container mainPane;
    protected JPanel headerPanel;
    protected JPanel contentPanel;
    protected JPanel buttonPanel;
    protected JLabel name;
    protected JTextField nameEntry;
    protected JCheckBox oneTimeCB;
    protected JCheckBox enabledCB;
    protected JButton ok;
    protected JButton cancel;

    protected JPanel actionPanel;
    protected JPanel actionSubPanel;
    protected JLabel action;
    protected JComboBox actionCombo;
    protected JButton actionAdd;
    protected JButton actionDelete;
    protected JList actionList;
    protected DefaultListModel actionListModel;
    public static final String EnabledAction = "Enable";
    public static final String DisabledAction = "Disable";
    public static final String TouchTimeAction = "Start Animation";
    public static final String StartTimeAction = "startTime";
    public static final String LMSInitializeAction = "LMSInitialize";
    public static final String LMSContinueAction = "LMSMarkSCOComplete";
    public static final String LMSFinishAction = "LMSFinish";
    
    public static final String actions[] = {EnabledAction,
					    DisabledAction,
					    TouchTimeAction,
					    LMSInitializeAction,
					    LMSContinueAction,
					    LMSFinishAction};

    public static final String sensors[] = {"TimeSensor", "TouchSensor"};
    public static final String timeSensor[] = {"TimeSensor"};

    protected JPanel targetPanel;
    protected JPanel targetSubPanel;
    protected JLabel target;
    protected JComboBox targetCombo;
    protected JButton targetAdd;
    protected JButton targetDelete;
    protected JList targetList;
    protected DefaultListModel targetListModel;

    public static short OK = 0;
    public static short CANCEL = 1;
    public Hashtable actionTable = new Hashtable();

    protected short status = CANCEL;
    public boolean oneTime = false;
    public boolean enabled = false;

    public TriggerDialog(Frame owner,
			 String title,
			 boolean modal,
			 final Document document) {
	super(owner, title, modal);
	setLocationRelativeTo(owner);

	gridbag = new GridBagLayout();
	constraints = new GridBagConstraints();
	constraints.insets = new Insets(4, 4, 4, 4);

	mainPane = getContentPane();
	mainPane.setLayout(gridbag);

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

	actionListModel = new DefaultListModel();
	actionAdd = new JButton("Add");
	actionAdd.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    String item = (String)actionCombo.getSelectedItem();

		    actionTable.put(item, new Vector());
		    actionListModel.addElement(item);
		    actionList.setSelectedValue(item, true);
		}
	    });
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
	actionDelete.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    String item = (String)actionList.getSelectedValue();

		    if (item == null)
			return;

		    actionTable.remove(item);
		    actionListModel.removeElement(item);

		    try {
			if ((item = (String)actionListModel.lastElement()) == null)
			    return;
		    } catch (Exception notUsed) {
			return;
		    }

		    actionList.setSelectedValue(item, true);
		}
	    });
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
	actionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	actionList.setModel(actionListModel);
	actionList.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
		    String item = null;
		    String key = (String)actionList.getSelectedValue();

		    //		    System.out.println("actionList: key - " + key);

		    targetCombo.removeAllItems();
		    if (key.equals(TouchTimeAction)) {
			Vector targetDefs = XmlUtil.getDefs(document.getDocumentElement(),
							    TriggerDialog.timeSensor);
			if (targetDefs != null) {
			    Enumeration items = targetDefs.elements();

			    while(items.hasMoreElements()) {
				item = (String)items.nextElement();
				targetCombo.addItem(item);
			    }
			}
		    } else if (key.equals(EnabledAction) ||
			       key.equals(DisabledAction)) {
			Vector targetDefs = XmlUtil.getDefs(document.getDocumentElement(),
							    TriggerDialog.sensors);
			if (targetDefs != null) {
			    Enumeration items = targetDefs.elements();

			    while(items.hasMoreElements()) {
				item = (String)items.nextElement();
				targetCombo.addItem(item);
			    }
			}
		    }

		    Enumeration targets = ((Vector)actionTable.get(key)).elements();
		    targetListModel.clear();
		    while (targets.hasMoreElements()) {
			item = (String)targets.nextElement();
			targetListModel.addElement(item);
		    }
		    targetList.setSelectedValue(item, true);
		}
	    });
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
	//	targetCombo = new JComboBox(targetDefs);
	targetCombo = new JComboBox();
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

	targetListModel = new DefaultListModel();
	targetAdd = new JButton("Add");
	targetAdd.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    String item = (String)targetCombo.getSelectedItem();
		    String key = (String)actionList.getSelectedValue();

		    if (key == null)
			return;

		    ((Vector)actionTable.get(key)).add(item);
		    targetListModel.addElement(item);
		    targetList.setSelectedValue(item, true);
		}
	    });
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
	targetDelete.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    String item = (String)targetList.getSelectedValue();
		    String key = (String)actionList.getSelectedValue();

		    if (key == null)
			return;

		    ((Vector)actionTable.get(key)).remove(item);
		    targetListModel.removeElement(item);

		    try {
			if ((item = (String)targetListModel.lastElement()) == null)
			    return;
		    } catch (Exception notUsed) {
			return;
		    }

		    targetList.setSelectedValue(item, true);
		}
	    });
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
	targetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	targetList.setModel(targetListModel);
	targetPanel.add(targetList, constraints);

	// overall
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
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	nameEntry = new JTextField();
	headerPanel.add(nameEntry, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 2;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	oneTimeCB = new JCheckBox("One Time");
	oneTimeCB.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    switch (e.getStateChange()) {
		    case ItemEvent.DESELECTED:
			oneTime = false;
			break;
		    case ItemEvent.SELECTED:
			oneTime = true;
			break;
		    }
		}
	    });
	headerPanel.add(oneTimeCB, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 3;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
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
	headerPanel.add(enabledCB, constraints);

	// add header panel
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.WEST;
	mainPane.add(headerPanel, constraints);

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
	mainPane.add(contentPanel, constraints);

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
	constraints.gridwidth = 1;
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

    public boolean getOneTime() {
	return oneTime;
    }

    public void setOneTime(boolean oneTime) {
	this.oneTime = oneTime;
	oneTimeCB.setSelected(oneTime);
    }

    public boolean getEnabled() {
	return enabled;
    }

    public void setEnabled(boolean enabled) {
	this.enabled = enabled;
	enabledCB.setSelected(enabled);
    }

    public short getStatus() {
	return status;
    }
}
