//
//   SwitchDialog.java
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

public class SwitchDialog extends JDialog {
    // declare GUI components here
    protected GridBagLayout gridbag;
    protected GridBagConstraints constraints;
    protected Container mainPane;
    protected JPanel buttonPanel1;
    protected JPanel buttonPanel2;
    protected JLabel name;
    protected JTextField nameEntry;
    protected JButton ok;
    protected JButton cancel;
    protected short status = CANCEL;

    protected JLabel target;
    protected JComboBox targetCombo;
    protected JButton targetAdd;
    protected JButton targetDelete;
    protected JList targetList;
    public DefaultListModel targetListModel;

    public static final String sensors[] = {"TimeSensor", "TouchSensor"};
    public static short OK = 0;
    public static short CANCEL = 1;

    public SwitchDialog(Frame owner,
			String title,
			boolean modal,
			Document document) {
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
	constraints.fill = GridBagConstraints.HORIZONTAL;
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
	mainPane.add(nameEntry, constraints);

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	target = new JLabel("Target: ");
	mainPane.add(target, constraints);

	Vector targetDefs = XmlUtil.getDefs(document.getDocumentElement(),
					    SwitchDialog.sensors);
	if (targetDefs == null) {
	    targetDefs = new Vector();
	}

	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	targetCombo = new JComboBox(targetDefs);
	mainPane.add(targetCombo, constraints);

	buttonPanel1 = new JPanel(gridbag);
	targetListModel = new DefaultListModel();
	targetAdd = new JButton("Add");
	targetAdd.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    String item = (String)targetCombo.getSelectedItem();

		    targetListModel.addElement(item);
		    targetList.setSelectedValue(item, true);
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
	buttonPanel1.add(targetAdd, constraints);

	targetDelete = new JButton("Delete");
	targetDelete.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    String item = (String)targetList.getSelectedValue();

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
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	buttonPanel1.add(targetDelete, constraints);

	constraints.gridwidth = 2;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	mainPane.add(buttonPanel1, constraints);

	targetList = new JList();
	targetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	targetList.setModel(targetListModel);
	constraints.gridwidth = 2;
	constraints.gridheight = 1;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	mainPane.add(targetList, constraints);

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
	constraints.gridy = 4;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	mainPane.add(buttonPanel2, constraints);
    }

    public String getName() {
	return nameEntry.getText();
    }

    public void setName(String name) {
	nameEntry.setText(name);
    }

    public short getStatus() {
	return status;
    }
}
