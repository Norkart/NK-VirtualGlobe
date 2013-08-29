/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2006
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

// Local imports
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;
import javax.swing.filechooser.*;

// Local imports
import org.web3d.util.FileHandler;

/**
 * An action that reloads the last file.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class ReloadAction extends AbstractAction {

    /** The handler for dealing with file open actions */
    private FileHandler fileHandler;

    /** Parent frame used to handle the file dialog */
    private Component parent;

    /** The combo box field containing the current url */
    private JComboBox url;

    /**
     * Create an instance of the action class.
     *
     * @param parent The parent component
     * @param handler A handler for opening files
     * @param url The text field containing the current url
     */
    public ReloadAction(Component parent,
                        FileHandler handler,
                        JComboBox url) {
        super("Reload");

        this.parent = parent;
        fileHandler = handler;
        this.url = url;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_R,
                                                   KeyEvent.CTRL_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        putValue(SHORT_DESCRIPTION, "Reload the current file");
    }

    /**
     * An action has been performed. This is the Go button being pressed.
     * Grab the URL and check with the file to see if it exists first as
     * a local file, and then try to make a URL of it. Finally, if this all
     * works, call the abstract gotoLocation method.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        try {
            fileHandler.loadURL((String)url.getSelectedItem());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
