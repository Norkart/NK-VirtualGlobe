/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.widgets;

// External imports
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.ImageIcon;

// Local imports
// None

/**
 * A self-configured button implementation that can be used to show and hide
 * the console window.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class SwingConsoleButton extends JButton
    implements ActionListener {

    /** Default properties object */
    private static final Properties DEFAULT_PROPERTIES = new Properties();

    /** Property in browser skin which determines 'show console' image */
    private static final String CONSOLE_BUTTON_PROPERTY = "CONSOLE.button";

    /** Default image to use for 'show console' button */
    private static final String DEFAULT_CONSOLE_BUTTON =
        "images/navigation/ButtonConsole.gif";

    /** Area to push error messages to */
    private SwingConsoleWindow console;

    /**
     * Create an instance of the button configured to show or hide the console
     * window.
     *
     * @param console The console window instance to use
     */
    public SwingConsoleButton(SwingConsoleWindow console) {
        this(console, null);
    }

    /**
     * Create an instance of the button configured to show or hide the console
     * window.
     *
     * @param window The console window instance to use
     * @param skinProperties Properties object specifying image names
     */
    public SwingConsoleButton(SwingConsoleWindow window,
                              Properties skinProperties) {
        //super("Console");

        console = window;

        if(skinProperties == null)
            skinProperties = DEFAULT_PROPERTIES;


        String img_name = skinProperties.getProperty(CONSOLE_BUTTON_PROPERTY,
                                                     DEFAULT_CONSOLE_BUTTON);
        ImageIcon icon = IconLoader.loadIcon(img_name, console);

        if (icon != null)
            setIcon(icon);
        else
            setText("Console");
        setToolTipText("Show browser console");
        setMargin(new Insets(0,0,0,0));
        addActionListener(this);
    }

    //----------------------------------------------------------
    // Methods defined by ActionListener
    //----------------------------------------------------------

    /**
     * Process the action event of the button being pressed. Will cause the
     * console to be shown.
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt) {
        console.setVisible(true);
    }
}
