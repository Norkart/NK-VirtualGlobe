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

import java.awt.GridLayout;
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * A panel for putting in the attribute name and value.
 * <p>
 * Quick hack to get things going.
 */
class AttributePanel extends JPanel
{
    /** The textfield containing the name string */
    private JTextField nameText;

    /** The textfield containing the value string */
    private JTextField valueText;

    /**
     * Create a new default attribute value with empty textfields
     */
    AttributePanel()
    {
        setLayout(new GridLayout(2, 1));

        JPanel p1 = new JPanel(new BorderLayout());
        JLabel l1 = new JLabel("Name: ");
        nameText = new JTextField(20);
        p1.add(l1, BorderLayout.WEST);
        p1.add(nameText, BorderLayout.EAST);

        JPanel p2 = new JPanel(new BorderLayout());
        JLabel l2 = new JLabel("Value: ");
        valueText = new JTextField(20);
        p2.add(l2, BorderLayout.WEST);
        p2.add(valueText, BorderLayout.EAST);

        add(p1);
        add(p2);
    }

    /**
     * Get the name string the user entered.
     *
     * @return The string representing the name
     */
    String getAttributeName()
    {
        return nameText.getText();
    }

    /**
     * Get the value string the user entered.
     *
     * @return The string representing the value
     */
    String getAttributeValue()
    {
        return valueText.getText();
    }
}
