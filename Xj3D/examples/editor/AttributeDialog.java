/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

import javax.swing.*;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog for setting the Attributes values of an element
 * <p>
 * The dialog runs as a modal dialog. When it is shown, it will not return to
 * the caller until it is closed.
 */
class AttributeDialog extends JDialog implements ActionListener
{
    /** The title of the dialog box */
    private static final String DIALOG_TITLE = "Add Attribute";

    /** Attribute Panel for containing the information */
    private AttributePanel panel;

    /** Flag to say it was cancelled */
    private boolean canceled;

    /** The OK button */
    private JButton okButton;

    /** The cancel button */
    private JButton cancelButton;

    /**
     * Create a new dialog with the given frame as parent.
     *
     * @param parent The parent frame of this dialog
     */
    AttributeDialog(JFrame parent)
    {
        super(parent, DIALOG_TITLE, true);

        panel = new AttributePanel();

        okButton = new JButton("  OK  ");
        okButton.addActionListener(this);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        JPanel p1 = new JPanel();
        p1.add(okButton);
        p1.add(cancelButton);

        Container content = getContentPane();
        content.add(panel, BorderLayout.CENTER);
        content.add(p1, BorderLayout.SOUTH);

        pack();
    }

    /**
     * Get the name string the user entered.
     *
     * @return The string representing the name
     */
    String getAttributeName()
    {
        return panel.getAttributeName();
    }

    /**
     * Get the value string the user entered.
     *
     * @return The string representing the value
     */
    String getAttributeValue()
    {
        return panel.getAttributeValue();
    }

    /**
     * Get the button that was pressed
     *
     * True if this dialog was cancelled
     */
    boolean isCanceled()
    {
        return canceled;
    }

    /**
     * Override the standard method to do internal stuff
     *
     * @param vis True to show this dialog
     */
    public void setVisible(boolean vis)
    {
        if(vis)
            canceled = false;

        super.setVisible(vis);
    }

    /**
     * Process an action event from one of buttons.
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        canceled = (evt.getSource() == cancelButton);
        super.setVisible(false);
    }
}
